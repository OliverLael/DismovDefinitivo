package com.example.mislugares

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class LugarOSM(
    val nombre: String,
    val categoria: String,
    val icono: String,
    val lat: Double,
    val lon: Double,
    val distanciaM: Double
)

class AdaptadorLugaresOSM(
    private var lugares: List<LugarOSM>
) : RecyclerView.Adapter<AdaptadorLugaresOSM.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tv_icono_osm)
        val tvNombre: TextView = view.findViewById(R.id.tv_nombre_osm)
        val tvCategoria: TextView = view.findViewById(R.id.tv_categoria_osm)
        val tvDistancia: TextView = view.findViewById(R.id.tv_distancia_osm)
        val btnMapa: ImageView = view.findViewById(R.id.btn_mapa_osm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lugar_osm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lugar = lugares[position]
        holder.tvIcono.text = lugar.icono
        holder.tvNombre.text = lugar.nombre
        holder.tvCategoria.text = lugar.categoria
        holder.tvDistancia.text = when {
            lugar.distanciaM >= 1000 -> "${"%.1f".format(lugar.distanciaM / 1000)} km"
            else -> "${lugar.distanciaM.toInt()} m"
        }
        holder.btnMapa.setOnClickListener {
            val uri = Uri.parse(
                "geo:${lugar.lat},${lugar.lon}?q=${lugar.lat},${lugar.lon}(${Uri.encode(lugar.nombre)})"
            )
            holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    override fun getItemCount() = lugares.size

    fun actualizar(nuevos: List<LugarOSM>) {
        lugares = nuevos
        notifyDataSetChanged()
    }
}
