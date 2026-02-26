package com.example.mislugares

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
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
        val ctx = holder.itemView.context

        holder.nombreTextView.text = lugar.nombre
        holder.direccionTextView.text =
            lugar.direccion.takeIf { it.isNotEmpty() } ?: "Sin dirección"
        holder.valoracionRatingBar.rating = lugar.valoracion

        // Distance badge
        val posActual = aplicacion.posicionActual
        if (posActual != GeoPunto.SIN_POSICION && lugar.posicion != GeoPunto.SIN_POSICION) {
            val distancia = posActual.distancia(lugar.posicion)
            holder.distanciaTextView.text = when {
                distancia > 2000 -> "${"%.1f".format(distancia / 1000)} km"
                else -> "${distancia.toInt()} m"
            }
            holder.distanciaTextView.visibility = View.VISIBLE
        } else {
            holder.distanciaTextView.visibility = View.GONE
        }

        // Difficulty badge – colour-coded
        when (lugar.dificultad) {
            Dificultad.PRINCIPIANTE -> {
                holder.dificultadTextView.text = "🌿 Principiante"
                holder.dificultadTextView.setBackgroundResource(R.drawable.bg_dificultad_principiante)
                holder.dificultadTextView.setTextColor(ctx.getColor(R.color.dificultad_principiante_text))
            }
            Dificultad.INTERMEDIO -> {
                holder.dificultadTextView.text = "⛰️ Intermedio"
                holder.dificultadTextView.setBackgroundResource(R.drawable.bg_dificultad_intermedio)
                holder.dificultadTextView.setTextColor(ctx.getColor(R.color.dificultad_intermedio_text))
            }
            Dificultad.AVANZADO -> {
                holder.dificultadTextView.text = "🔥 Avanzado"
                holder.dificultadTextView.setBackgroundResource(R.drawable.bg_dificultad_avanzado)
                holder.dificultadTextView.setTextColor(ctx.getColor(R.color.dificultad_avanzado_text))
            }
        }

        // Photo
        if (!lugar.fotoUri.isNullOrEmpty()) {
            Glide.with(ctx)
                .load(Uri.parse(lugar.fotoUri))
                .placeholder(R.drawable.mapa)
                .error(R.drawable.mapa)
                .centerCrop()
                .into(holder.fotoImageView)
        } else {
            Glide.with(ctx)
                .load(R.drawable.mapa)
                .centerCrop()
                .into(holder.fotoImageView)
        }

        holder.itemView.setOnClickListener { onItemClicked(position) }
    }

    override fun getItemCount() = lugares.size

    fun actualizarLugares(nuevosLugares: List<Lugar>) {
        this.lugares = nuevosLugares
        notifyDataSetChanged()
    }
}
