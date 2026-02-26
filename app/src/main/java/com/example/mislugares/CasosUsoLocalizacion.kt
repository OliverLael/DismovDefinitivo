package com.example.mislugares

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar

class CasosUsoLocalizacion(
    private val actividad: MainActivity,
    private val permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
) : LocationListener {

    private val manager: LocationManager =
        actividad.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val aplicacion = actividad.application as AplicacionMisLugares
    private var mejorLocaliz: Location? = null

    init {
        ultimaLocalizacion()
    }

    fun hayPermisoLocalizacion(): Boolean {
        return ActivityCompat.checkSelfPermission(
            actividad,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun ultimaLocalizacion() {
        if (!hayPermisoLocalizacion()) {
            solicitarPermiso()
            return
        }

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            actualizaMejorLocaliz(manager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            actualizaMejorLocaliz(manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
        }
    }

    private fun solicitarPermiso() {
        when {
            actividad.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    actividad.findViewById(android.R.id.content),
                    "Sin el permiso de localización no podemos mostrar la distancia a los lugares.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("OK") {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    fun permisoConcedido() {
        ultimaLocalizacion()
        activarProveedores()
        aplicacion.repositorio.adaptador?.notifyDataSetChanged() // Refresca la lista
    }

    @SuppressLint("MissingPermission")
    fun activarProveedores() {
        if (!hayPermisoLocalizacion()) return

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20_000, 5f, this)
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10_000, 10f, this)
        }
    }

    fun desactivarProveedores() {
        manager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        actualizaMejorLocaliz(location)
        aplicacion.repositorio.adaptador?.notifyDataSetChanged()
    }

    override fun onProviderDisabled(provider: String) {
        activarProveedores()
    }

    var onLocationUpdate: (() -> Unit)? = null

    private fun actualizaMejorLocaliz(location: Location?) {
        location ?: return
        if (mejorLocaliz == null || location.accuracy < 2 * (mejorLocaliz?.accuracy ?: 0f) || location.time - (mejorLocaliz?.time ?: 0) > 2 * 60 * 1000) {
            mejorLocaliz = location
            aplicacion.posicionActual = GeoPunto(location.longitude, location.latitude)
            onLocationUpdate?.invoke()
        }
    }
}