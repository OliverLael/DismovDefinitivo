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
import com.bumptech.glide.Glide
import com.example.mislugares.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
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
        // Obtén tu clave gratuita en https://openweathermap.org/api
        private const val WEATHER_API_KEY = "TU_API_KEY_AQUI"
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
                val urlStr = "https://api.openweathermap.org/data/2.5/weather" +
                        "?lat=$lat&lon=$lon&appid=$WEATHER_API_KEY&units=metric&lang=es"
                val connection = URL(urlStr).openConnection() as HttpsURLConnection
                connection.connectTimeout = 6000
                connection.readTimeout = 6000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)
                    val temp = json.getJSONObject("main").getDouble("temp").toInt()
                    val city = json.getString("name")
                    val iconCode = json.getJSONArray("weather")
                        .getJSONObject(0).getString("icon")
                    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"

                    handler.post {
                        binding.weatherTemp.text = "$temp°C"
                        binding.weatherCity.text = city
                        Glide.with(this@MainActivity)
                            .load(iconUrl)
                            .placeholder(R.drawable.ic_weather_default)
                            .into(binding.weatherIcon)
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
