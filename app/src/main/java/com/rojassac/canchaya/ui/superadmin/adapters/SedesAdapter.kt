package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.ItemSedeCardBinding
import com.rojassac.canchaya.data.model.Sede

/**
 * 🆕 NUEVO ADAPTER: Adapter para lista de Sedes (21 Oct 2025)
 */
class SedesAdapter(
    private var sedes: List<Sede>,
    private val onVerCanchas: (Sede) -> Unit,
    private val onEditar: (Sede) -> Unit,
    private val onEliminar: (Sede) -> Unit,
    private val onAgregarCancha: (Sede) -> Unit
) : RecyclerView.Adapter<SedesAdapter.SedeViewHolder>() {

    inner class SedeViewHolder(private val binding: ItemSedeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sede: Sede) {
            binding.apply {
                // Nombre de la sede
                tvNombreSede.text = sede.nombre

                // Dirección
                tvDireccion.text = sede.direccion

                // Horario de operación
                tvHorario.text = "⏰ ${sede.getHorarioDisplay()}"

                // Coordenadas GPS
                if (sede.tieneCoordenadasValidas()) {
                    tvCoordenadas.text = "📍 ${sede.latitud}, ${sede.longitud}"
                    tvCoordenadas.setTextColor(tvCoordenadas.context.getColor(android.R.color.darker_gray))
                } else {
                    tvCoordenadas.text = "📍 Sin coordenadas"
                    // 🔵 CORREGIDO: Usar color directo en vez de R.color.error
                    tvCoordenadas.setTextColor(android.graphics.Color.parseColor("#F44336"))
                }

                // Cantidad de canchas
                tvCantidadCanchas.text = "${sede.getCantidadCanchas()} canchas"

                // Estado de la sede
                if (sede.activa) {
                    chipEstado.text = "Activa"
                    chipEstado.setChipBackgroundColorResource(android.R.color.holo_green_dark)
                } else {
                    chipEstado.text = "Inactiva"
                    chipEstado.setChipBackgroundColorResource(android.R.color.holo_red_dark)
                }

                // Imagen de la sede
                if (sede.imageUrl.isNotEmpty()) {
                    Glide.with(imgSede.context)
                        .load(sede.imageUrl)
                        .placeholder(R.drawable.ic_placeholder_cancha)
                        .error(R.drawable.ic_placeholder_cancha)
                        .centerCrop()
                        .into(imgSede)
                } else {
                    imgSede.setImageResource(R.drawable.ic_placeholder_cancha)
                }

                // Listeners de botones
                btnVerCanchas.setOnClickListener { onVerCanchas(sede) }
                btnEditar.setOnClickListener { onEditar(sede) }
                btnEliminar.setOnClickListener { onEliminar(sede) }
                btnAgregarCancha.setOnClickListener { onAgregarCancha(sede) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SedeViewHolder {
        val binding = ItemSedeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SedeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SedeViewHolder, position: Int) {
        holder.bind(sedes[position])
    }

    override fun getItemCount(): Int = sedes.size

    fun updateList(newList: List<Sede>) {
        sedes = newList
        notifyDataSetChanged()
    }
}
