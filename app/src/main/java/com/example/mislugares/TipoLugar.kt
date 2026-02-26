package com.example.mislugares

enum class TipoLugar(
    val texto: String,
    val recurso: Int
) {
    OTROS("Otros", 0),
    RESTAURANTE("Restaurante", 0),
    BAR("Bar", 0),
    COPAS("Copas", 0),
    ESPECTACULO("Espectáculo", 0),
    HOTEL("Hotel", 0),
    COMPRAS("Compras", 0),
    EDUCACION("Educación", 0),
    DEPORTE("Deporte", 0),
    NATURALEZA("Naturaleza", 0),
    GASOLINERA("Gasolinera", 0);
}
