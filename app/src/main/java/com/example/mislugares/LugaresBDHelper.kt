package com.example.mislugares

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LugaresBDHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "lugares.db"
        const val TABLE_LUGARES = "lugares"

        const val KEY_ID = "_id"
        const val KEY_NOMBRE = "nombre"
        const val KEY_DIRECCION = "direccion"
        const val KEY_LONGITUD = "longitud"
        const val KEY_LATITUD = "latitud"
        const val KEY_TIPO = "tipo"
        const val KEY_FOTO_URI = "fotoUri"
        const val KEY_TELEFONO = "telefono"
        const val KEY_URL = "url"
        const val KEY_COMENTARIO = "comentario"
        const val KEY_FECHA = "fecha"
        const val KEY_VALORACION = "valoracion"

        const val KEY_DIFICULTAD = "dificultad"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_LUGARES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_NOMBRE TEXT,
                $KEY_DIRECCION TEXT,
                $KEY_LONGITUD REAL,
                $KEY_LATITUD REAL,
                $KEY_TIPO INTEGER,
                $KEY_DIFICULTAD INTEGER,
                $KEY_FOTO_URI TEXT,
                $KEY_TELEFONO INTEGER,
                $KEY_URL TEXT,
                $KEY_COMENTARIO TEXT,
                $KEY_FECHA BIGINT,
                $KEY_VALORACION REAL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        db.execSQL(
            "INSERT INTO $TABLE_LUGARES VALUES (null, " +
                    "'Cerro de la Silla', " +
                    "'Monterrey, Nuevo León', -100.2887, 25.6516, " +
                    "${TipoLugar.NATURALEZA.ordinal}, ${Dificultad.AVANZADO.ordinal}, " +
                    "null, 0, '', " +
                    "'Ruta exigente, solo para senderistas con experiencia.', ${System.currentTimeMillis()}, 4.5)"
        )

        db.execSQL(
            "INSERT INTO $TABLE_LUGARES VALUES (null, " +
                    "'Parque Ecológico Chipinque', " +
                    "'San Pedro Garza García, NL', -100.3500, 25.6167, " +
                    "${TipoLugar.NATURALEZA.ordinal}, ${Dificultad.PRINCIPIANTE.ordinal}, " +
                    "null, 0, '',"+
                    "'Senderos naturales con vistas increíbles.', ${System.currentTimeMillis()}, 5.0)"
        )

        db.execSQL(
            "INSERT INTO $TABLE_LUGARES VALUES (null, " +
                    "'Parque La Estanzuela', " +
                    "'Carretera Nacional, NL', -100.1783, 25.5466, " +
                    "${TipoLugar.NATURALEZA.ordinal}, ${Dificultad.INTERMEDIO.ordinal}, " +
                    "null, 0, '', " +
                    "'Cascadas y rutas de senderismo.', ${System.currentTimeMillis()}, 4.8)"
        )

        db.execSQL(
            "INSERT INTO $TABLE_LUGARES VALUES (null, " +
                    "'Parque Ecológico La Huasteca', " +
                    "'Santa Catarina, NL', -100.4442, 25.6458, " +
                    "${TipoLugar.NATURALEZA.ordinal}, ${Dificultad.PRINCIPIANTE.ordinal}, " +
                    "null, 0, '', " +
                    "'Imponentes cañones de roca caliza, ideal para ciclismo y escalada.', ${System.currentTimeMillis()}, 4.7)"
        )

        db.execSQL(
            "INSERT INTO $TABLE_LUGARES VALUES (null, " +
                    "'Cerro de las Mitras', " +
                    "'Monterrey/Santa Catarina, NL', -100.4208, 25.7042, " +
                    "${TipoLugar.NATURALEZA.ordinal}, ${Dificultad.AVANZADO.ordinal}, " +
                    "null, 0, '', " +
                    "'Ruta técnica con minas abandonadas y vistas panorámicas de la ciudad.', ${System.currentTimeMillis()}, 4.9)"
        )


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LUGARES")
        onCreate(db)
    }
}