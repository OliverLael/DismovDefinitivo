package com.example.mislugares

data class GeoPunto(
    var longitud: Double = 0.0,
    var latitud: Double = 0.0
) {
    companion object {
        val SIN_POSICION = GeoPunto(0.0, 0.0)
    }

    fun distancia(punto: GeoPunto): Double {
        val radioTierra = 6371.0
        val dLat = Math.toRadians(latitud - punto.latitud)
        val dLon = Math.toRadians(longitud - punto.longitud)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(punto.latitud)) *
                Math.cos(Math.toRadians(latitud)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radioTierra * c * 1000
    }
}