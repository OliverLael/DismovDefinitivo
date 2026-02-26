package com.example.mislugares

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.annotation.SuppressLint

class CasosUsoLugar(
    private val actividad: Activity,
    private val repositorio: LugarRepositorio
) {

    fun mostrar(id: String) {
        val intent = Intent(actividad, VistaLugarActivity::class.java)
        intent.putExtra("id", id)
        actividad.startActivity(intent)
    }

    fun mostrar(id: Long) {
        val intent = Intent(actividad, VistaLugarActivity::class.java)
        intent.putExtra("id", id)
        actividad.startActivity(intent)
    }

    fun compartir(lugar: Lugar) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            var textoACompartir = lugar.nombre
            if (!lugar.url.isNullOrEmpty()) {
                textoACompartir += "\n${lugar.url}"
            }
            putExtra(Intent.EXTRA_TEXT, textoACompartir)
        }
        if (intent.resolveActivity(actividad.packageManager) != null) {
            actividad.startActivity(Intent.createChooser(intent, "Compartir '${lugar.nombre}' vía..."))
        } else {
            android.widget.Toast.makeText(actividad, "No se encontró ninguna aplicación para compartir", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun llamarTelefono(lugar: Lugar) {
        if (lugar.telefono != 0L) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${lugar.telefono}"))
            if (intent.resolveActivity(actividad.packageManager) != null) {
                actividad.startActivity(intent)
            } else {
                android.widget.Toast.makeText(actividad, "No se encontró ninguna aplicación para llamar", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(actividad, "Este lugar no tiene teléfono", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun verPgWeb(lugar: Lugar) {
        if (!lugar.url.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lugar.url))
            if (intent.resolveActivity(actividad.packageManager) != null) {
                actividad.startActivity(intent)
            } else {
                android.widget.Toast.makeText(actividad, "No se encontró ninguna aplicación para abrir la web", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(actividad, "Este lugar no tiene página web", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun verMapa(lugar: Lugar) {
        val uri: Uri
        if (lugar.posicion != GeoPunto.SIN_POSICION && !(lugar.posicion.latitud == 0.0 && lugar.posicion.longitud == 0.0)) {
            uri = Uri.parse("geo:${lugar.posicion.latitud},${lugar.posicion.longitud}?q=${lugar.posicion.latitud},${lugar.posicion.longitud}(${Uri.encode(lugar.nombre)})")
        } else if (!lugar.direccion.isNullOrEmpty()) {
            uri = Uri.parse("geo:0,0?q=${Uri.encode(lugar.direccion)}")
        } else {
            android.widget.Toast.makeText(actividad, "No hay suficiente información para mostrar en el mapa", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(actividad.packageManager) != null) {
            actividad.startActivity(intent)
        } else {
            android.widget.Toast.makeText(actividad, "No se encontró aplicación de mapas", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}