package com.example.mislugares

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mislugares.databinding.ActivitySenderismoCampingBinding

class SenderismoCampingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySenderismoCampingBinding
    private val repositorio: LugarRepositorio by lazy {
        (application as AplicacionMisLugares).repositorio
    }
    private val casosUsoLugar by lazy { CasosUsoLugar(this, repositorio) }
    private lateinit var adaptador: AdaptadorLugares

    private var rangeKm = 10
    private var todasLasListas: List<Lugar> = emptyList()
    private var isSenderismo = true

    // Types for senderismo (outdoor/sport/nature)
    private val tiposSenderismo = setOf(TipoLugar.NATURALEZA, TipoLugar.DEPORTE, TipoLugar.OTROS)
    // Types for camping (nature-focused)
    private val tiposCamping = setOf(TipoLugar.NATURALEZA, TipoLugar.OTROS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySenderismoCampingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rangeKm = intent.getIntExtra(MainActivity.EXTRA_RANGE_KM, 10)
        binding.tvRangeBadge.text = "${rangeKm}km"

        adaptador = AdaptadorLugares(emptyList()) { posicion ->
            val idLugar = repositorio.idPorPosicion(posicion)
            if (idLugar.isNotEmpty()) casosUsoLugar.mostrar(idLugar)
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@SenderismoCampingActivity)
            adapter = adaptador
        }

        binding.btnBack.setOnClickListener { finish() }
        setupToggle()
    }

    private fun setupToggle() {
        binding.toggleSenderismo.setOnClickListener {
            if (!isSenderismo) {
                isSenderismo = true
                updateToggleUI()
                applyFilter()
            }
        }
        binding.toggleCamping.setOnClickListener {
            if (isSenderismo) {
                isSenderismo = false
                updateToggleUI()
                applyFilter()
            }
        }
        updateToggleUI()
    }

    private fun updateToggleUI() {
        if (isSenderismo) {
            binding.toggleSenderismo.setBackgroundResource(R.drawable.bg_toggle_selected)
            binding.toggleSenderismo.setTextColor(getColor(R.color.text_on_dark))
            binding.toggleCamping.setBackgroundResource(R.drawable.bg_toggle_normal)
            binding.toggleCamping.setTextColor(getColor(R.color.text_medium))
        } else {
            binding.toggleCamping.setBackgroundResource(R.drawable.bg_toggle_selected)
            binding.toggleCamping.setTextColor(getColor(R.color.text_on_dark))
            binding.toggleSenderismo.setBackgroundResource(R.drawable.bg_toggle_normal)
            binding.toggleSenderismo.setTextColor(getColor(R.color.text_medium))
        }
    }

    private fun applyFilter() {
        val posActual = (application as AplicacionMisLugares).posicionActual
        val rangeMetros = rangeKm * 1000.0
        val tiposActivos = if (isSenderismo) tiposSenderismo else tiposCamping

        var filtered = todasLasListas.filter { it.tipo in tiposActivos }

        if (posActual != GeoPunto.SIN_POSICION) {
            filtered = filtered.filter { lugar ->
                lugar.posicion == GeoPunto.SIN_POSICION ||
                        posActual.distancia(lugar.posicion) <= rangeMetros
            }
        }

        adaptador.actualizarLugares(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

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
    }
}
