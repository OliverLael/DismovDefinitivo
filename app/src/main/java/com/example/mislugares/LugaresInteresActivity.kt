package com.example.mislugares

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mislugares.databinding.ActivityLugaresInteresBinding
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class LugaresInteresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLugaresInteresBinding
    private val repositorio: LugarRepositorio by lazy {
        (application as AplicacionMisLugares).repositorio
    }
    private val casosUsoLugar by lazy { CasosUsoLugar(this, repositorio) }
    private lateinit var adaptador: AdaptadorLugares
    private lateinit var adaptadorOsm: AdaptadorLugaresOSM

    private var rangeKm = 10
    private var todasLasListas: List<Lugar> = emptyList()
    private var selectedFilter: TipoLugar? = null
    private var osmThread: Thread? = null

    // Types considered "places of interest" (urban/commercial)
    private val tiposInteres = setOf(
        TipoLugar.RESTAURANTE, TipoLugar.BAR, TipoLugar.COMPRAS,
        TipoLugar.ESPECTACULO, TipoLugar.HOTEL, TipoLugar.EDUCACION,
        TipoLugar.COPAS, TipoLugar.GASOLINERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLugaresInteresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rangeKm = intent.getIntExtra(MainActivity.EXTRA_RANGE_KM, 10)
        binding.tvRangeBadge.text = "${rangeKm}km"

        adaptador = AdaptadorLugares(emptyList()) { posicion ->
            val idLugar = repositorio.idPorPosicion(posicion)
            if (idLugar.isNotEmpty()) casosUsoLugar.mostrar(idLugar)
        }

        adaptadorOsm = AdaptadorLugaresOSM(emptyList())

        binding.recyclerView.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(this@LugaresInteresActivity)
            adapter = adaptador
        }

        binding.recyclerOsm.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(this@LugaresInteresActivity)
            adapter = adaptadorOsm
        }

        binding.btnBack.setOnClickListener { finish() }
        setupCategoryChips()
    }

    private fun setupCategoryChips() {
        val chips = listOf(
            binding.chipTodos to null,
            binding.chipRestaurante to TipoLugar.RESTAURANTE,
            binding.chipBar to TipoLugar.BAR,
            binding.chipCompras to TipoLugar.COMPRAS,
            binding.chipEducacion to TipoLugar.EDUCACION,
            binding.chipHotel to TipoLugar.HOTEL
        )
        chips.forEach { (chip, tipo) ->
            chip.setOnClickListener {
                selectedFilter = tipo
                chips.forEach { (c, _) -> setChipState(c, false) }
                setChipState(chip, true)
                applyFilter()
            }
        }
    }

    private fun setChipState(chip: TextView, selected: Boolean) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected)
            chip.setTextColor(getColor(R.color.chip_selected_text))
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_normal)
            chip.setTextColor(getColor(R.color.chip_normal_text))
        }
    }

    private fun applyFilter() {
        val posActual = (application as AplicacionMisLugares).posicionActual
        val rangeMetros = rangeKm * 1000.0

        var filtered = if (posActual != GeoPunto.SIN_POSICION) {
            todasLasListas.filter { lugar ->
                lugar.posicion != GeoPunto.SIN_POSICION &&
                        posActual.distancia(lugar.posicion) <= rangeMetros
            }
        } else {
            todasLasListas
        }

        // Apply type filter
        filtered = if (selectedFilter != null) {
            filtered.filter { it.tipo == selectedFilter }
        } else {
            filtered.filter { it.tipo in tiposInteres }
        }

        adaptador.actualizarLugares(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE

        // Fetch OSM nearby places matching the current filter
        if (posActual != GeoPunto.SIN_POSICION) {
            fetchOsmPlaces(posActual.latitud, posActual.longitud)
        }
    }

    // ── Overpass / OpenStreetMap ──────────────────────────────────────────────

    private fun fetchOsmPlaces(lat: Double, lon: Double) {
        osmThread?.interrupt()
        binding.progressOsm.visibility = View.VISIBLE
        binding.tvEmptyOsm.visibility = View.GONE
        adaptadorOsm.actualizar(emptyList())

        val query = buildOverpassQuery(rangeKm * 1000, lat, lon)

        osmThread = Thread {
            try {
                val connection = URL("https://overpass-api.de/api/interpreter")
                    .openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true
                connection.outputStream.use { out ->
                    out.write("data=${URLEncoder.encode(query, "UTF-8")}".toByteArray())
                }

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val elements = JSONObject(response).getJSONArray("elements")
                    val posActual = GeoPunto(lon, lat)
                    val lugares = mutableListOf<LugarOSM>()

                    for (i in 0 until elements.length()) {
                        val el = elements.getJSONObject(i)
                        if (!el.has("tags") || !el.has("lat")) continue
                        val tags = el.getJSONObject("tags")
                        val nombre = tags.optString("name").takeIf { it.isNotEmpty() } ?: continue
                        val elLat = el.getDouble("lat")
                        val elLon = el.getDouble("lon")
                        val distancia = posActual.distancia(GeoPunto(elLon, elLat))
                        val (categoria, icono) = categorizarOSM(tags)
                        lugares.add(LugarOSM(nombre, categoria, icono, elLat, elLon, distancia))
                    }

                    lugares.sortBy { it.distanciaM }
                    connection.disconnect()

                    runOnUiThread {
                        binding.progressOsm.visibility = View.GONE
                        adaptadorOsm.actualizar(lugares)
                        binding.tvEmptyOsm.visibility =
                            if (lugares.isEmpty()) View.VISIBLE else View.GONE
                    }
                } else {
                    connection.disconnect()
                    runOnUiThread { showOsmError() }
                }
            } catch (e: Exception) {
                if (!Thread.currentThread().isInterrupted) {
                    runOnUiThread { showOsmError() }
                }
            }
        }.also { it.start() }
    }

    private fun showOsmError() {
        binding.progressOsm.visibility = View.GONE
        binding.tvEmptyOsm.text = getString(R.string.osm_error)
        binding.tvEmptyOsm.visibility = View.VISIBLE
    }

    private fun buildOverpassQuery(radiusM: Int, lat: Double, lon: Double): String {
        val around = "(around:$radiusM,$lat,$lon)"
        val nodes = when (selectedFilter) {
            TipoLugar.RESTAURANTE -> "node[\"amenity\"~\"restaurant|cafe|fast_food\"]$around;"
            TipoLugar.BAR        -> "node[\"amenity\"~\"bar|pub|biergarten\"]$around;"
            TipoLugar.COMPRAS    -> "node[\"shop\"]$around;"
            TipoLugar.EDUCACION  -> "node[\"amenity\"~\"school|university|college|library\"]$around;"
            TipoLugar.HOTEL      -> buildString {
                append("node[\"tourism\"~\"hotel|hostel|motel|guest_house\"]$around;")
                append("node[\"amenity\"=\"hotel\"]$around;")
            }
            else -> buildString {
                append("node[\"amenity\"~\"restaurant|cafe|fast_food|bar|pub\"]$around;")
                append("node[\"tourism\"~\"hotel|museum|attraction\"]$around;")
                append("node[\"leisure\"=\"park\"]$around;")
                append("node[\"shop\"]$around;")
            }
        }
        return "[out:json][timeout:15];($nodes);out 60;"
    }

    private fun categorizarOSM(tags: JSONObject): Pair<String, String> {
        val amenity = tags.optString("amenity")
        val tourism = tags.optString("tourism")
        val leisure = tags.optString("leisure")
        val shop    = tags.optString("shop")
        return when {
            amenity == "restaurant" || amenity == "fast_food" -> "Restaurante"   to "🍽️"
            amenity == "cafe"                                  -> "Café"          to "☕"
            amenity == "bar" || amenity == "pub"              -> "Bar"           to "🍺"
            amenity == "biergarten"                           -> "Cervecería"    to "🍻"
            amenity == "fuel"                                  -> "Gasolinera"    to "⛽"
            amenity == "pharmacy"                              -> "Farmacia"      to "💊"
            amenity == "hospital" || amenity == "clinic"      -> "Salud"         to "🏥"
            amenity == "school" || amenity == "university" ||
                    amenity == "college"                       -> "Educación"     to "🎓"
            amenity == "library"                               -> "Biblioteca"    to "📚"
            tourism == "hotel"                                 -> "Hotel"         to "🏨"
            tourism == "hostel" || tourism == "motel" ||
                    tourism == "guest_house"                   -> "Alojamiento"   to "🛏️"
            tourism == "museum"                                -> "Museo"         to "🏛️"
            tourism == "attraction" || tourism == "viewpoint"  -> "Atracción"     to "🗺️"
            leisure == "park"                                  -> "Parque"        to "🌳"
            leisure == "sports_centre"                         -> "Deporte"       to "⚽"
            shop.isNotEmpty() && shop != "null"                -> "Tienda"        to "🛍️"
            else                                               -> "Lugar"         to "📍"
        }
    }

    // ── Lifecycle ─────────────────────────────────

    override fun onResume() {
        super.onResume()
        repositorio.iniciarEscuchador { lista ->
            todasLasListas = lista
            applyFilter()
        }
    }

    override fun onPause() {
        super.onPause()
        repositorio.detenerEscuchador()
        osmThread?.interrupt()
    }
}
