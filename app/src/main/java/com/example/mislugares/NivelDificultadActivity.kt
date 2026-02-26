package com.example.mislugares

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mislugares.databinding.ActivityNivelDificultadBinding

class NivelDificultadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNivelDificultadBinding
    private val repositorio: LugarRepositorio by lazy {
        (application as AplicacionMisLugares).repositorio
    }
    private val casosUsoLugar by lazy { CasosUsoLugar(this, repositorio) }
    private lateinit var adaptador: AdaptadorLugares

    private var rangeKm = 10
    private var todasLasListas: List<Lugar> = emptyList()
    private var selectedDificultad: Dificultad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNivelDificultadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rangeKm = intent.getIntExtra(MainActivity.EXTRA_RANGE_KM, 10)

        adaptador = AdaptadorLugares(emptyList()) { posicion ->
            val idLugar = repositorio.idPorPosicion(posicion)
            if (idLugar.isNotEmpty()) casosUsoLugar.mostrar(idLugar)
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@NivelDificultadActivity)
            adapter = adaptador
        }

        binding.btnBack.setOnClickListener { finish() }
        setupDifficultyCards()
    }

    private fun setupDifficultyCards() {
        binding.cardPrincipiante.setOnClickListener {
            selectDificultad(Dificultad.PRINCIPIANTE)
        }
        binding.cardIntermedio.setOnClickListener {
            selectDificultad(Dificultad.INTERMEDIO)
        }
        binding.cardAvanzado.setOnClickListener {
            selectDificultad(Dificultad.AVANZADO)
        }
    }

    private fun selectDificultad(dificultad: Dificultad) {
        selectedDificultad = dificultad

        // Reset card elevations
        binding.cardPrincipiante.cardElevation = 3f.dpToPx()
        binding.cardIntermedio.cardElevation = 3f.dpToPx()
        binding.cardAvanzado.cardElevation = 3f.dpToPx()

        // Highlight selected
        when (dificultad) {
            Dificultad.PRINCIPIANTE -> binding.cardPrincipiante.cardElevation = 8f.dpToPx()
            Dificultad.INTERMEDIO -> binding.cardIntermedio.cardElevation = 8f.dpToPx()
            Dificultad.AVANZADO -> binding.cardAvanzado.cardElevation = 8f.dpToPx()
        }

        binding.tvHint.visibility = View.GONE
        applyFilter()
    }

    private fun Float.dpToPx(): Float =
        this * resources.displayMetrics.density

    private fun applyFilter() {
        val dif = selectedDificultad ?: return
        val posActual = (application as AplicacionMisLugares).posicionActual
        val rangeMetros = rangeKm * 1000.0

        var filtered = todasLasListas.filter { it.dificultad == dif }

        if (posActual != GeoPunto.SIN_POSICION) {
            filtered = filtered.filter { lugar ->
                lugar.posicion == GeoPunto.SIN_POSICION ||
                        posActual.distancia(lugar.posicion) <= rangeMetros
            }
        }

        adaptador.actualizarLugares(filtered)

        val listVisible = filtered.isNotEmpty()
        binding.recyclerView.visibility = if (listVisible) View.VISIBLE else View.GONE
        binding.tvEmpty.visibility = if (listVisible) View.GONE else View.VISIBLE
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
