package com.example.mislugares

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mislugares.databinding.ActivityLugaresInteresBinding

class LugaresInteresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLugaresInteresBinding
    private val repositorio: LugarRepositorio by lazy {
        (application as AplicacionMisLugares).repositorio
    }
    private val casosUsoLugar by lazy { CasosUsoLugar(this, repositorio) }
    private lateinit var adaptador: AdaptadorLugares

    private var rangeKm = 10
    private var todasLasListas: List<Lugar> = emptyList()
    private var selectedFilter: TipoLugar? = null

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

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@LugaresInteresActivity)
            adapter = adaptador
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
