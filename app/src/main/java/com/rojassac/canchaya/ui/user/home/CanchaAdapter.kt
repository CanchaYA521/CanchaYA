package com.rojassac.canchaya.ui.user.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.databinding.ItemCanchaBinding

class CanchaAdapter(
    private val onCanchaClick: (Cancha) -> Unit
) : ListAdapter<Cancha, CanchaAdapter.CanchaViewHolder>(CanchaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanchaViewHolder {
        val binding = ItemCanchaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CanchaViewHolder(binding, onCanchaClick)
    }

    override fun onBindViewHolder(holder: CanchaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CanchaViewHolder(
        private val binding: ItemCanchaBinding,
        private val onCanchaClick: (Cancha) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cancha: Cancha) {
            binding.apply {
                tvNombre.text = cancha.nombre
                tvDireccion.text = "${cancha.direccion}, ${cancha.distrito}"
                tvPrecio.text = "S/ ${cancha.precioHora}/hora"
                tvCalificacion.text = cancha.calificacionPromedio.toString()

                // Cargar imagen con Glide
                if (cancha.imagenes.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(cancha.imagenes[0])
                        .placeholder(R.mipmap.ic_launcher)
                        .into(ivCancha)
                } else {
                    ivCancha.setImageResource(R.mipmap.ic_launcher)
                }

                // Click listener para navegar al detalle
                root.setOnClickListener { onCanchaClick(cancha) }
            }
        }
    }

    class CanchaDiffCallback : DiffUtil.ItemCallback<Cancha>() {
        override fun areItemsTheSame(oldItem: Cancha, newItem: Cancha): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cancha, newItem: Cancha): Boolean {
            return oldItem == newItem
        }
    }
}
