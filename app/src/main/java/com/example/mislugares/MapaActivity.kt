package com.example.mislugares

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mapa: GoogleMap
    private val repositorio: LugarRepositorio by lazy {
        (application as AplicacionMisLugares).repositorio
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapa)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap
        mapa.mapType = GoogleMap.MAP_TYPE_NORMAL
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapa.isMyLocationEnabled = true
        }
        mapa.uiSettings.isZoomControlsEnabled = true
        mapa.uiSettings.isCompassEnabled = true
        mapa.setOnInfoWindowClickListener(this)

        val lugares = repositorio.obtenerTodosSincrono()

        if (lugares.isNotEmpty()) {
            for ((indice, lugar) in lugares.withIndex()) {
                if (lugar.posicion != GeoPunto.SIN_POSICION) {
                    val punto = LatLng(lugar.posicion.latitud, lugar.posicion.longitud)
                    val icono = obtenerIconoMarcador(lugar.tipo)
                    val marcador = mapa.addMarker(
                        MarkerOptions()
                            .position(punto)
                            .title(lugar.nombre)
                            .snippet(lugar.direccion)
                            .icon(BitmapDescriptorFactory.fromBitmap(icono))
                    )
                    marcador?.tag = lugar.id
                }
            }
            val primerLugarConPos = lugares.firstOrNull { it.posicion != GeoPunto.SIN_POSICION }
            primerLugarConPos?.let {
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.posicion.latitud, it.posicion.longitud), 12f))
            }
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        val idLugar = marker.tag as? String
        idLugar?.let {
            if (it.isNotEmpty()) {
                val intent = Intent(this, VistaLugarActivity::class.java)
                intent.putExtra("id", it)
                startActivity(intent)
            }
        }
    }

    private fun obtenerIconoMarcador(tipo: TipoLugar): Bitmap {
        val recursoID = when (tipo) {
            TipoLugar.BAR -> R.drawable.ic_tipo_bar
            TipoLugar.EDUCACION -> R.drawable.ic_tipo_educacion
            TipoLugar.RESTAURANTE -> R.drawable.ic_tipo_restaurante
            TipoLugar.HOTEL -> R.drawable.ic_tipo_hotel
            TipoLugar.COMPRAS -> R.drawable.ic_tipo_compras
            TipoLugar.NATURALEZA -> R.drawable.ic_tipo_naturaleza
            TipoLugar.GASOLINERA -> R.drawable.ic_tipo_gasolinera
            else -> R.drawable.ic_tipo_otros
        }

        var drawable = ContextCompat.getDrawable(this, recursoID)
        if (drawable == null) {
            Log.w("MapaActivity", "Icono no encontrado para $tipo (ID: $recursoID). Usando 'ic_tipo_otros'.")
            drawable = ContextCompat.getDrawable(this, R.drawable.ic_tipo_otros)!!
        }

        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}