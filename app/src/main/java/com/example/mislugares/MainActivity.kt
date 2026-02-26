package com.example.mislugares

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import android.location.Geocoder
import com.example.mislugares.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())

    private var selectedRangeKm = 10
    private var weatherLoaded = false

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) casosUsoLocalizacion.permisoConcedido()
        }

    private val casosUsoLocalizacion: CasosUsoLocalizacion by lazy {
        CasosUsoLocalizacion(this, requestPermissionLauncher)
    }

    private val preferenciasLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        finish()
        startActivity(intent)
    }

    companion object {
        const val EXTRA_RANGE_KM = "range_km"
        var debeRefrescarLista = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        selectedRangeKm = prefs.getInt("selected_range_km", 10)

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.musica_fondo)
            mediaPlayer?.isLooping = true
        }

        setupRangeChips()
        setupCardNavigation()
        casosUsoLocalizacion.ultimaLocalizacion()
        casosUsoLocalizacion.onLocationUpdate = { tryLoadWeather() }
    }

    // ── Range chips ───────────────────────────────

    private fun setupRangeChips() {
        updateRangeChipUI(selectedRangeKm)
        listOf(
            binding.chip5km to 5,
            binding.chip10km to 10,
            binding.chip25km to 25,
            binding.chip50km to 50
        ).forEach { (chip, km) ->
            chip.setOnClickListener {
                selectedRangeKm = km
                prefs.edit().putInt("selected_range_km", km).apply()
                updateRangeChipUI(km)
            }
        }
    }

    private fun updateRangeChipUI(selectedKm: Int) {
        listOf(
            binding.chip5km to 5,
            binding.chip10km to 10,
            binding.chip25km to 25,
            binding.chip50km to 50
        ).forEach { (chip, km) ->
            if (km == selectedKm) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(getColor(R.color.chip_selected_text))
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_normal)
                chip.setTextColor(getColor(R.color.chip_normal_text))
            }
        }
    }

    // ── Card navigation ───────────────────────────

    private fun setupCardNavigation() {
        binding.cardLugaresInteres.setOnClickListener {
            startActivity(
                Intent(this, LugaresInteresActivity::class.java)
                    .putExtra(EXTRA_RANGE_KM, selectedRangeKm)
            )
        }
        binding.cardMisLugares.setOnClickListener {
            startActivity(Intent(this, LugaresPorVisitarActivity::class.java))
        }
        binding.cardNivel.setOnClickListener {
            startActivity(
                Intent(this, NivelDificultadActivity::class.java)
                    .putExtra(EXTRA_RANGE_KM, selectedRangeKm)
            )
        }
        binding.cardSenderismo.setOnClickListener {
            startActivity(
                Intent(this, SenderismoCampingActivity::class.java)
                    .putExtra(EXTRA_RANGE_KM, selectedRangeKm)
            )
        }
        binding.btnMap.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            preferenciasLauncher.launch(Intent(this, PreferenciasActivity::class.java))
        }
    }

    // ── Weather API ───────────────────────────────

    private fun tryLoadWeather() {
        if (weatherLoaded) return
        val pos = (application as AplicacionMisLugares).posicionActual
        if (pos != GeoPunto.SIN_POSICION) {
            weatherLoaded = true
            fetchWeather(pos.latitud, pos.longitud)
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        Thread {
            try {
                // Nombre de ciudad usando Geocoder integrado de Android (sin API key)
                @Suppress("DEPRECATION")
                val cityName = try {
                    Geocoder(this, Locale.getDefault())
                        .getFromLocation(lat, lon, 1)
                        ?.firstOrNull()
                        ?.let { it.locality ?: it.subAdminArea }
                        ?: ""
                } catch (e: Exception) { "" }

                // Clima desde Open-Meteo: gratuito, sin registro, sin API key
                val urlStr = "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,weather_code"
                val connection = URL(urlStr).openConnection() as HttpsURLConnection
                connection.connectTimeout = 6000
                connection.readTimeout = 6000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val current = JSONObject(response).getJSONObject("current")
                    val temp = current.getDouble("temperature_2m").toInt()
                    val description = weatherCodeToDescription(current.getInt("weather_code"))

                    handler.post {
                        binding.weatherTemp.text = "$temp°C"
                        binding.weatherCity.text =
                            if (cityName.isNotEmpty()) "$cityName · $description" else description
                        binding.weatherIcon.setImageResource(R.drawable.ic_weather_default)
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                handler.post {
                    binding.weatherCity.text = getString(R.string.weather_error)
                }
            }
        }.start()
    }

    private fun weatherCodeToDescription(code: Int): String = when (code) {
        0          -> "Despejado"
        1          -> "Mayormente despejado"
        2          -> "Parcialmente nublado"
        3          -> "Nublado"
        45, 48     -> "Niebla"
        51, 53, 55 -> "Llovizna"
        61, 63, 65 -> "Lluvia"
        71, 73, 75, 77 -> "Nieve"
        80, 81, 82 -> "Chubascos"
        85, 86     -> "Chubascos de nieve"
        95         -> "Tormenta"
        96, 99     -> "Tormenta con granizo"
        else       -> "Variable"
    }

    // ── Music ─────────────────────────────────────

    private fun iniciarMusica() {
        mediaPlayer?.let {
            if (prefs.getBoolean("musica_habilitada", true) && !it.isPlaying) {
                try { it.start() } catch (e: IllegalStateException) { }
            }
        }
    }

    private fun pararMusica(liberarRecursos: Boolean = false) {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
            if (liberarRecursos) { it.release(); mediaPlayer = null }
        }
    }

    // ── Lifecycle ─────────────────────────────────

    override fun onResume() {
        super.onResume()
        casosUsoLocalizacion.activarProveedores()
        iniciarMusica()
        tryLoadWeather()
        handler.postDelayed({ tryLoadWeather() }, 3000)
    }

    override fun onPause() {
        super.onPause()
        casosUsoLocalizacion.desactivarProveedores()
        pararMusica(liberarRecursos = false)
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        pararMusica(liberarRecursos = true)
    }
}
