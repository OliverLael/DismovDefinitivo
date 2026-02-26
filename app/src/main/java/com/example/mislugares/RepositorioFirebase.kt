package com.example.mislugares

import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects

class RepositorioFirebase(private val context: Context) : LugarRepositorio {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private var lugaresCache: List<Lugar> = emptyList()

    override var adaptador: AdaptadorLugares? = null

    private val userId: String?
        get() = auth.currentUser?.uid

    private val lugaresCollection
        get() = userId?.let {
            db.collection("usuarios").document(it).collection("lugares")
        }

    override fun iniciarEscuchador(actualizar: (List<Lugar>) -> Unit) {
        if (lugaresCollection == null) {
            Log.w("Firebase", "Usuario no autenticado, no se pueden cargar datos.")
            actualizar(emptyList())
            return
        }

        detenerEscuchador()

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val orden = pref.getString("orden", "1") ?: "1"
        val maximoStr = pref.getString("maximo", "12") ?: "12"
        val maximo = maximoStr.toLongOrNull() ?: 12

        var query: Query = lugaresCollection!!

        when (orden) {
            "1" -> query = query.orderBy("nombre", Query.Direction.ASCENDING)
            "2" -> query = query.orderBy("valoracion", Query.Direction.DESCENDING)
            "3" -> {
                Log.w("Firebase", "Orden por distancia no implementado en Firebase. Usando orden por nombre.")
                query = query.orderBy("nombre", Query.Direction.ASCENDING)
            }
            else -> query = query.orderBy("nombre", Query.Direction.ASCENDING)
        }

        query = query.limit(maximo)

        listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("Firebase", "Error al escuchar cambios en Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                lugaresCache = snapshot.toObjects()
                Log.d("Firebase", "Datos actualizados. ${lugaresCache.size} lugares cargados.")
                actualizar(lugaresCache)
            } else {
                Log.d("Firebase", "Snapshot nulo, sin datos.")
                actualizar(emptyList())
            }
        }
    }

    override fun detenerEscuchador() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun obtenerTodosSincrono(): List<Lugar> {
        return lugaresCache
    }

    override fun obtenerCursor(): Cursor? {
        return null
    }

    override fun elementoPorId(id: String): Lugar? {
        return lugaresCache.find { it.id == id }
    }

    override fun idPorPosicion(pos: Int): String {
        return if (pos >= 0 && pos < lugaresCache.size) {
            lugaresCache[pos].id
        } else {
            ""
        }
    }

    override fun añade(lugar: Lugar) {
        lugaresCollection?.add(lugar)
            ?.addOnSuccessListener {
                Log.d("Firebase", "Lugar añadido con ID: ${it.id}")
            }
            ?.addOnFailureListener {
                Log.e("Firebase", "Error al añadir lugar", it)
            }
    }

    override fun borrarPorId(id: String): Boolean {
        if (id.isEmpty()) return false
        lugaresCollection?.document(id)?.delete()
            ?.addOnSuccessListener {
                Log.d("Firebase", "Lugar borrado: $id")
            }
            ?.addOnFailureListener {
                Log.e("Firebase", "Error al borrar lugar: $id", it)
            }
        return true
    }

    override fun actualizaPorId(id: String, lugar: Lugar): Boolean {
        if (id.isEmpty()) return false
        lugaresCollection?.document(id)?.set(lugar)
            ?.addOnSuccessListener {
                Log.d("Firebase", "Lugar actualizado: $id")
            }
            ?.addOnFailureListener {
                Log.e("Firebase", "Error al actualizar lugar: $id", it)
            }
        return true
    }

    override fun tamaño(): Int {
        return lugaresCache.size
    }
}