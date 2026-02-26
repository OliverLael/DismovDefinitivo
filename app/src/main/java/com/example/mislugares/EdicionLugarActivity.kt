package com.example.mislugares

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.mislugares.databinding.ActivityEdicionLugarBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EdicionLugarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEdicionLugarBinding
    private val repositorio: LugarRepositorio by lazy {
        (application as AplicacionMisLugares).repositorio
    }

    private var lugarOriginal: Lugar? = null
    private var idLugar: String = ""
    private var fotoUriSeleccionada: Uri? = null
    private var uriCamaraTemporal: Uri? = null

    private val galeriaLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uriExterna ->
            val uriInterna = copiarImagenALocal(uriExterna)

            if (uriInterna != null) {
                fotoUriSeleccionada = uriInterna
                actualizarFoto(uriInterna)
            } else {
                Toast.makeText(this, "Error al copiar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copiarImagenALocal(uriExterna: Uri): Uri? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uriExterna)

            val imagesFolder = File(filesDir, "images")
            if (!imagesFolder.exists()) {
                imagesFolder.mkdir()
            }

            val archivoDestino = File(imagesFolder, "IMG_${System.currentTimeMillis()}.jpg")

            inputStream?.use { input ->
                FileOutputStream(archivoDestino).use { output ->
                    input.copyTo(output)
                }
            }

            FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                archivoDestino
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val camaraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        if (exito) {
            fotoUriSeleccionada = uriCamaraTemporal
            actualizarFoto(fotoUriSeleccionada)
        } else {
            uriCamaraTemporal?.let {
                try { contentResolver.delete(it, null, null) } catch (e: Exception) {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdicionLugarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarEdicionLugar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        idLugar = intent.getStringExtra("id") ?: ""

        configurarSpinnerTipoLugar()
        configurarSpinnerDificultad()

        if (idLugar.isNotEmpty()) {
            lugarOriginal = repositorio.elementoPorId(idLugar)
            if (lugarOriginal == null) {
                Toast.makeText(this, "Error: Lugar no encontrado", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            cargarDatosEnFormulario()
            supportActionBar?.title = "Editar: ${lugarOriginal?.nombre ?: "Lugar"}"
        } else {
            supportActionBar?.title = "Nuevo Lugar"
            actualizarFoto(null)
        }

        binding.buttonGaleria.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        binding.buttonCamara.setOnClickListener {
            lanzarCamara()
        }

        binding.buttonEliminarFoto.setOnClickListener {
            fotoUriSeleccionada = null
            actualizarFoto(null)
        }
    }
    private fun configurarSpinnerDificultad() {
        val dificultades = Dificultad.values().map { it.name }
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            dificultades
        )
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDificultad.adapter = adaptador
    }

    private fun lanzarCamara() {
        try {
            val imagesFolder = File(filesDir, "images")
            if (!imagesFolder.exists()) {
                imagesFolder.mkdir()
            }
            val file = File(imagesFolder, "IMG_${System.currentTimeMillis()}.jpg")

            uriCamaraTemporal = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )

            uriCamaraTemporal?.let { uri ->
                camaraLauncher.launch(uri)
            } ?: run {
                Toast.makeText(this, "Error al crear URI para la cámara", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al preparar la cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun configurarSpinnerTipoLugar() {
        val tiposDeLugarNombres = TipoLugar.values().map { it.texto }
        val adaptadorSpinner = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tiposDeLugarNombres
        )
        adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipoLugar.adapter = adaptadorSpinner
    }

    private fun cargarDatosEnFormulario() {
        lugarOriginal?.let { lugar ->
            binding.editTextNombre.setText(lugar.nombre)
            binding.editTextDireccion.setText(lugar.direccion)
            binding.editTextTelefono.setText(lugar.telefono.toString().takeIf { it != "0" } ?: "")
            binding.editTextUrl.setText(lugar.url)
            binding.editTextComentario.setText(lugar.comentario)
            binding.ratingBarValoracionEdicion.rating = lugar.valoracion
            binding.editTextLatitud.setText(lugar.posicion.latitud.toString())
            binding.editTextLongitud.setText(lugar.posicion.longitud.toString())

            val tipoSeleccionadoIndex = TipoLugar.values().indexOf(lugar.tipo)
            if (tipoSeleccionadoIndex != -1) {
                binding.spinnerTipoLugar.setSelection(tipoSeleccionadoIndex)
            }

            val indexDificultad = Dificultad.values().indexOf(lugar.dificultad)
            if (indexDificultad != -1) {
                binding.spinnerDificultad.setSelection(indexDificultad)
            }

            if (!lugar.fotoUri.isNullOrEmpty()) {
                val uriGuardada = Uri.parse(lugar.fotoUri)
                try {
                    contentResolver.takePersistableUriPermission(
                        uriGuardada,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
                fotoUriSeleccionada = uriGuardada
                actualizarFoto(fotoUriSeleccionada)
            } else {
                actualizarFoto(null)
            }
        }

    }

    private fun actualizarFoto(uri: Uri?) {
        if (uri != null) {
            binding.imageViewFotoEdicion.setImageURI(uri)
        } else {
            binding.imageViewFotoEdicion.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edicion_lugar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                true
            }
            R.id.accion_guardar -> {
                guardarLugar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun guardarLugar() {
        if (binding.editTextNombre.text.isNullOrEmpty()) {
            binding.tilNombre.error = "El nombre es obligatorio"
            return
        } else {
            binding.tilNombre.error = null
        }

        val nombre = binding.editTextNombre.text.toString()
        val direccion = binding.editTextDireccion.text.toString()
        val comentario = binding.editTextComentario.text.toString().takeIf { it.isNotEmpty() }
        val valoracion = binding.ratingBarValoracionEdicion.rating
        val tipoLugarSeleccionado = TipoLugar.values()[binding.spinnerTipoLugar.selectedItemPosition]
        val latitud = binding.editTextLatitud.text.toString().toDoubleOrNull() ?: 0.0
        val longitud = binding.editTextLongitud.text.toString().toDoubleOrNull() ?: 0.0
        val telefono = binding.editTextTelefono.text.toString().toLongOrNull() ?: 0
        val url = binding.editTextUrl.text.toString().takeIf { it.isNotEmpty() }
        val fotoUriParaGuardar = fotoUriSeleccionada?.toString()
        val dificultadSeleccionada =
            Dificultad.values()[binding.spinnerDificultad.selectedItemPosition]

        val lugarParaGuardar = Lugar(
            id = lugarOriginal?.id ?: "",
            nombre = nombre,
            direccion = direccion,
            posicion = GeoPunto(longitud, latitud),
            tipo = tipoLugarSeleccionado,
            dificultad = dificultadSeleccionada,
        fotoUri = fotoUriParaGuardar,
            telefono = telefono,
            url = url,
            comentario = comentario,
            fecha = lugarOriginal?.fecha ?: System.currentTimeMillis(),
            valoracion = valoracion
        )

        if (idLugar.isNotEmpty()) {
            repositorio.actualizaPorId(idLugar, lugarParaGuardar)
            Toast.makeText(this, "Lugar actualizado", Toast.LENGTH_SHORT).show()
        } else {
            repositorio.añade(lugarParaGuardar)
            Toast.makeText(this, "Lugar nuevo creado", Toast.LENGTH_SHORT).show()
        }

        setResult(Activity.RESULT_OK)
        finish()
    }
}