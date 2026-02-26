package com.example.mislugares

import com.google.firebase.firestore.DocumentId

data class Lugar(
    @DocumentId
    var id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val posicion: GeoPunto = GeoPunto.SIN_POSICION,
    val tipo: TipoLugar = TipoLugar.OTROS,
    val dificultad: Dificultad,
    var fotoUri: String? = null,
    var telefono: Long = 0L,
    val url: String? = null,
    val comentario: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val valoracion: Float = 0.0f
)