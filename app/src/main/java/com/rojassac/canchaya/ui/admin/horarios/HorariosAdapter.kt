package com.rojassac.canchaya.ui.admin.horarios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.EstadoHorario
import com.rojassac.canchaya.data.model.HorarioSlot
import com.rojassac.canchaya.databinding.ItemHorarioBinding

class HorariosAdapter(
    private val onHorarioClick: (HorarioSlot) -> Unit
) : ListAdapter<HorarioSlot, HorariosAdapter.HorarioViewHolder>(HorarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val binding = ItemHorarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HorarioViewHolder(binding, onHorarioClick)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HorarioViewHolder(
        private val binding: ItemHorarioBinding,
        private val onHorarioClick: (HorarioSlot) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(horario: HorarioSlot) {
            binding.tvHora.text = horario.hora

            // ✅ Color según estado
            val backgroundColor = when (horario.estado) {
                EstadoHorario.DISPONIBLE -> {
                    // 🟢 Verde - Disponible
                    ContextCompat.getColor(binding.root.context, R.color.green)
                }
                EstadoHorario.OCUPADO -> {
                    // 🔴 Rojo - Ocupado
                    ContextCompat.getColor(binding.root.context, R.color.red)
                }
                EstadoHorario.PASADO -> {
                    // ⚫ Gris - Pasado
                    ContextCompat.getColor(binding.root.context, R.color.gray)
                }
            }

            binding.cardHorario.setCardBackgroundColor(backgroundColor)

            // ✅ Click solo si NO es pasado
            binding.cardHorario.isEnabled = horario.estado != EstadoHorario.PASADO

            if (horario.estado != EstadoHorario.PASADO) {
                binding.cardHorario.setOnClickListener {
                    onHorarioClick(horario)
                }
            }
        }
    }

    class HorarioDiffCallback : DiffUtil.ItemCallback<HorarioSlot>() {
        override fun areItemsTheSame(oldItem: HorarioSlot, newItem: HorarioSlot): Boolean {
            return oldItem.hora == newItem.hora
        }

        override fun areContentsTheSame(oldItem: HorarioSlot, newItem: HorarioSlot): Boolean {
            return oldItem == newItem
        }
    }
}
