package com.example.mislugares

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mislugares.databinding.ActivityVistaLugarBinding
import java.text.DateFormat
import java.util.Date

class VistaLugarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVistaLugarBinding
    private val repositorio: LugarRepositorio by lazy {
        (application as AplicacionMisLugares).repositorio
    }
    private val casosUsoLugar: CasosUsoLugar by lazy {
        CasosUsoLugar(this, repositorio)
    }

    private var lugarActual: Lugar? = null
    private var idLugar: String = ""

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                lugarActual?.let { casosUsoLugar.llamarTelefono(it) }
            } else {
                Toast.makeText(this, "Permiso de llamada denegado.", Toast.LENGTH_SHORT).show()
            }
        }

    private val editarLugarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (idLugar.isNotEmpty()) {
            lugarActual = repositorio.elementoPorId(idLugar)
            if (lugarActual == null) {
                finish()
            } else {
                actualizarVistas()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaLugarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarVistaLugar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        idLugar = intent.getStringExtra("id") ?: ""
        if (idLugar.isEmpty()) {
            Toast.makeText(this, "Error: ID de lugar no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        lugarActual = repositorio.elementoPorId(idLugar)

        if (lugarActual != null) {
            actualizarVistas()
        } else {
            Toast.makeText(this, "Cargando lugar...", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.buttonEliminarFoto.setOnClickListener { mostrarDialogoConfirmarEliminarFoto() }

        binding.ratingBarValoracion.setIsIndicator(false)
        binding.ratingBarValoracion.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { _, valoracionNueva, fromUser ->
                if (fromUser) {
                    lugarActual?.let { lugar ->
                        val lugarActualizado = lugar.copy(valoracion = valoracionNueva)
                        repositorio.actualizaPorId(idLugar, lugarActualizado)
                        lugarActual = lugarActualizado
                    }
                }
            }
    }

    private fun actualizarVistas() {
        lugarActual?.let { lugar ->
            supportActionBar?.title = lugar.nombre
            binding.textViewNombre.text = lugar.nombre
            binding.textViewTipo.text = lugar.tipo.texto
            binding.textDificultad.text = "Dificultad: ${lugar.dificultad}"


            if (!lugar.fotoUri.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(lugar.fotoUri)
                    binding.imageViewFotoVista.setImageURI(uri)
                    binding.frameLayoutFoto.visibility = View.VISIBLE
                } catch (e: Exception) {
                    binding.frameLayoutFoto.visibility = View.GONE
                }
            } else {
                binding.frameLayoutFoto.visibility = View.GONE
            }

            if (!lugar.direccion.isNullOrEmpty()) {
                binding.textViewDireccion.text = lugar.direccion
                binding.layoutDireccion.setOnClickListener { casosUsoLugar.verMapa(lugar) }
                binding.layoutDireccion.visibility = View.VISIBLE
            } else {
                binding.layoutDireccion.visibility = View.GONE
            }

            if (lugar.telefono != 0L) {
                binding.textViewTelefono.text = "Tel: ${lugar.telefono}"
                binding.layoutTelefono.setOnClickListener { verificarPermisoLlamada(lugar) }
                binding.layoutTelefono.visibility = View.VISIBLE
            } else {
                binding.layoutTelefono.visibility = View.GONE
            }

            if (!lugar.url.isNullOrEmpty()) {
                binding.textViewUrl.text = lugar.url
                binding.layoutUrl.setOnClickListener { casosUsoLugar.verPgWeb(lugar) }
                binding.layoutUrl.visibility = View.VISIBLE
            } else {
                binding.layoutUrl.visibility = View.GONE
            }

            binding.textViewComentario.text = lugar.comentario
            binding.textViewFecha.text = DateFormat.getDateInstance(DateFormat.MEDIUM)
                .format(Date(lugar.fecha))

            binding.ratingBarValoracion.onRatingBarChangeListener = null
            binding.ratingBarValoracion.rating = lugar.valoracion
            binding.ratingBarValoracion.onRatingBarChangeListener =
                RatingBar.OnRatingBarChangeListener { _, valoracionNueva, fromUser ->
                    if (fromUser) {
                        lugarActual?.let { l ->
                            val lugarActualizado = l.copy(valoracion = valoracionNueva)
                            repositorio.actualizaPorId(idLugar, lugarActualizado)
                            lugarActual = lugarActualizado
                            MainActivity.debeRefrescarLista = true
                        }
                    }
                }

        } ?: run {
            supportActionBar?.title = "Error"
            binding.textViewNombre.text = "Lugar no disponible"
        }
    }

    private fun verificarPermisoLlamada(lugar: Lugar) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED -> {
                casosUsoLugar.llamarTelefono(lugar)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                AlertDialog.Builder(this)
                    .setTitle("Permiso Necesario")
                    .setMessage("Para realizar llamadas directamente, la aplicación necesita tu permiso.")
                    .setPositiveButton("Aceptar") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    private fun mostrarDialogoConfirmarEliminarFoto() {
        if (lugarActual?.fotoUri.isNullOrEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Eliminar Foto")
            .setMessage("¿Estás seguro de que deseas eliminar la foto de este lugar?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarFotoDelLugar() }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun eliminarFotoDelLugar() {
        lugarActual?.let { lugar ->
            val lugarSinFoto = lugar.copy(fotoUri = null)
            repositorio.actualizaPorId(idLugar, lugarSinFoto)
            lugarActual = lugarSinFoto
            actualizarVistas()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_vista_lugar, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.accion_compartir)?.isEnabled = (lugarActual != null)
        menu?.findItem(R.id.accion_llegar)?.isEnabled = (lugarActual != null)
        menu?.findItem(R.id.accion_editar)?.isEnabled = (lugarActual != null)
        menu?.findItem(R.id.accion_borrar)?.isEnabled = (lugarActual != null)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val lugar = lugarActual ?: return super.onOptionsItemSelected(item)

        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.accion_compartir -> { casosUsoLugar.compartir(lugar); true }
            R.id.accion_llegar -> { casosUsoLugar.verMapa(lugar); true }
            R.id.accion_editar -> {
                val intent = Intent(this, EdicionLugarActivity::class.java)
                intent.putExtra("id", idLugar)
                editarLugarLauncher.launch(intent)
                true
            }
            R.id.accion_borrar -> { confirmarBorradoLugar(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmarBorradoLugar() {
        val nombreLugar = lugarActual?.nombre ?: "este lugar"
        AlertDialog.Builder(this)
            .setTitle("Borrar Lugar")
            .setMessage("¿Estás seguro de que deseas borrar '$nombreLugar'?")
            .setPositiveButton("Borrar") { _, _ ->
                repositorio.borrarPorId(idLugar)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}