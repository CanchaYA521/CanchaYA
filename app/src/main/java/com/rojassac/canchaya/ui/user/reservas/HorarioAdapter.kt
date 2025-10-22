package com.rojassac.canchaya.ui.user.reservas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.ItemHorarioBinding

class HorarioAdapter(
    private val onHorarioClick: (HorarioItem) -> Unit
) : ListAdapter<HorarioItem, HorarioAdapter.HorarioViewHolder>(HorarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val binding = ItemHorarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HorarioViewHolder(binding, onHorarioClick)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HorarioViewHolder(
        private val binding: ItemHorarioBinding,
        private val onHorarioClick: (HorarioItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(horario: HorarioItem) {
            binding.tvHora.text = horario.hora

            val context = itemView.context
            val backgroundColor = if (horario.disponible) {
                ContextCompat.getColor(context, R.color.accent_green)
            } else {
                ContextCompat.getColor(context, R.color.horario_reservado)
            }

            binding.root.setCardBackgroundColor(backgroundColor)
            binding.root.setOnClickListener {
                onHorarioClick(horario)
            }
        }
    }

    class HorarioDiffCallback : DiffUtil.ItemCallback<HorarioItem>() {
        override fun areItemsTheSame(oldItem: HorarioItem, newItem: HorarioItem): Boolean {
            return oldItem.hora == newItem.hora
        }

        override fun areContentsTheSame(oldItem: HorarioItem, newItem: HorarioItem): Boolean {
            return oldItem == newItem
        }
    }
}
