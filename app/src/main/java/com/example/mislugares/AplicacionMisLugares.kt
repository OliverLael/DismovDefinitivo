package com.example.mislugares

import android.app.Application
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth

class AplicacionMisLugares : Application() {

    val repositorio: LugarRepositorio by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val tipoGuardado = prefs.getString("tipo_guardado", "sqlite")

        if (tipoGuardado == "firebase") {
            RepositorioFirebase(this)
        } else {
            RepositorioSqlite(this)
        }
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate() {
        super.onCreate()
        iniciarSesionAnonima()
    }

    private fun iniciarSesionAnonima() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        android.util.Log.d("AplicacionMisLugares", "SignIn Anónimo OK: ${auth.currentUser?.uid}")
                    } else {
                        android.util.Log.e("AplicacionMisLugares", "SignIn Anónimo Fallido", task.exception)
                    }
                }
        } else {
            android.util.Log.d("AplicacionMisLugares", "Usuario ya logueado: ${auth.currentUser?.uid}")
        }
    }

    var posicionActual: GeoPunto = GeoPunto.SIN_POSICION
}