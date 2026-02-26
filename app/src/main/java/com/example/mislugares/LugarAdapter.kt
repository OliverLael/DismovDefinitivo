package com.example.mislugares

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.mislugares.Dificultad
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdaptadorLugares(
    private var lugares: List<Lugar>,
    private val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<AdaptadorLugares.ViewHolder>() {

    private lateinit var aplicacion: AplicacionMisLugares

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreTextView: TextView = view.findViewById(R.id.nombre)
        val direccionTextView: TextView = view.findViewById(R.id.direccion)
        val distanciaTextView: TextView = view.findViewById(R.id.distancia)
        val fotoImageView: ImageView = view.findViewById(R.id.foto)
        val valoracionRatingBar: RatingBar = view.findViewById(R.id.valoracion)

        val dificultadTextView: TextView = view.findViewById(R.id.dificultad)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        aplicacion = parent.context.applicationContext as AplicacionMisLugares
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lugar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lugar = lugares[position]
        holder.nombreTextView.text = lugar.nombre
        holder.direccionTextView.text = lugar.direccion.takeIf { !it.isNullOrEmpty() } ?: "Sin dirección"
        holder.valoracionRatingBar.rating = lugar.valoracion

        val posActual = aplicacion.posicionActual
        if (posActual != GeoPunto.SIN_POSICION && lugar.posicion != GeoPunto.SIN_POSICION) {
            val distancia = posActual.distancia(lugar.posicion) // en metros
            val textoDistancia = when {
                distancia > 2000 -> "${"%.1f".format(distancia / 1000)} km"
                distancia >= 0 -> "${distancia.toInt()} m"
                else -> "..."
            }
            holder.distanciaTextView.text = textoDistancia
            holder.distanciaTextView.visibility = View.VISIBLE
        } else {
            holder.distanciaTextView.visibility = View.GONE
        }

        holder.dificultadTextView.text = when (lugar.dificultad) {
            Dificultad.PRINCIPIANTE -> "Dificultad: Principiante"
            Dificultad.INTERMEDIO -> "Dificultad: Intermedio"
            Dificultad.AVANZADO -> "Dificultad: Avanzado"
        }


        if (!lugar.fotoUri.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(lugar.fotoUri))
                .placeholder(R.drawable.mapa)
                .error(R.drawable.mapa)
                .into(holder.fotoImageView)
        } else {
            Glide.with(holder.itemView.context)
                .load(R.drawable.mapa)
                .into(holder.fotoImageView)
        }

        holder.itemView.setOnClickListener {
            onItemClicked(position)
        }
    }

    override fun getItemCount() = lugares.size

    fun actualizarLugares(nuevosLugares: List<Lugar>) {
        this.lugares = nuevosLugares
        notifyDataSetChanged()
    }
}