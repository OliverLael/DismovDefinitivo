package com.example.mislugares

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mislugares.databinding.ActivityLugaresPorVisitarBinding

class LugaresPorVisitarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLugaresPorVisitarBinding
    private val repositorio: LugarRepositorio by lazy {
        (application as AplicacionMisLugares).repositorio
    }
    private val casosUsoLugar by lazy { CasosUsoLugar(this, repositorio) }
    private lateinit var adaptador: AdaptadorLugares

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLugaresPorVisitarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adaptador = AdaptadorLugares(emptyList()) { posicion ->
            val idLugar = repositorio.idPorPosicion(posicion)
            if (idLugar.isNotEmpty()) casosUsoLugar.mostrar(idLugar)
        }
        repositorio.adaptador = adaptador

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@LugaresPorVisitarActivity)
            adapter = adaptador
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this, EdicionLugarActivity::class.java))
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        repositorio.iniciarEscuchador { lista ->
            adaptador.actualizarLugares(lista)
            binding.tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        repositorio.detenerEscuchador()
    }
}
