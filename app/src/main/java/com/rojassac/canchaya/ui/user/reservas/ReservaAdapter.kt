package com.rojassac.canchaya.ui.user.reservas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.EstadoReserva
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.databinding.ItemReservaUsuarioBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReservaAdapter(
    private val onReservaClick: (Reserva) -> Unit,
    private val onCancelarClick: (Reserva) -> Unit
) : ListAdapter<Reserva, ReservaAdapter.ReservaViewHolder>(ReservaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val binding = ItemReservaUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReservaViewHolder(binding, onReservaClick, onCancelarClick)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReservaViewHolder(
        private val binding: ItemReservaUsuarioBinding,
        private val onReservaClick: (Reserva) -> Unit,
        private val onCancelarClick: (Reserva) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reserva: Reserva) {
            binding.apply {
                tvCanchaNombre.text = reserva.canchaNombre

                // Formato de fecha
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = inputFormat.parse(reserva.fecha)
                    tvFecha.text = "📅 ${date?.let { outputFormat.format(it) } ?: reserva.fecha}"
                } catch (e: Exception) {
                    tvFecha.text = "📅 ${reserva.fecha}"
                }

                tvHorario.text = "🕒 ${reserva.horaInicio} - ${reserva.horaFin}"
                tvPrecio.text = "S/ ${String.format("%.2f", reserva.precio)}"

                val context = itemView.context

                // Estados y colores
                when (reserva.estado) {
                    EstadoReserva.PENDIENTE -> {
                        tvEstado.text = "Pendiente"
                        tvEstado.setTextColor(ContextCompat.getColor(context, R.color.warning))
                        tvEstado.setBackgroundResource(R.drawable.rounded_background)
                    }
                    EstadoReserva.CONFIRMADA -> {
                        tvEstado.text = "Confirmada"
                        tvEstado.setTextColor(ContextCompat.getColor(context, R.color.success))
                        tvEstado.setBackgroundResource(R.drawable.rounded_background)
                    }
                    EstadoReserva.CANCELADA -> {
                        tvEstado.text = "Cancelada"
                        tvEstado.setTextColor(ContextCompat.getColor(context, R.color.error))
                        tvEstado.setBackgroundResource(R.drawable.rounded_background)
                    }
                    EstadoReserva.COMPLETADA -> {
                        tvEstado.text = "Completada"
                        tvEstado.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                        tvEstado.setBackgroundResource(R.drawable.rounded_background)
                    }
                }

                // Click en toda la card
                root.setOnClickListener {
                    onReservaClick(reserva)
                }

                // Mostrar botón cancelar solo si está confirmada
                btnCancelar.visibility = if (reserva.estado == EstadoReserva.CONFIRMADA) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                // Click en cancelar
                btnCancelar.setOnClickListener {
                    onCancelarClick(reserva)
                }
            }
        }
    }

    class ReservaDiffCallback : DiffUtil.ItemCallback<Reserva>() {
        override fun areItemsTheSame(oldItem: Reserva, newItem: Reserva): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reserva, newItem: Reserva): Boolean {
            return oldItem == newItem
        }
    }
}
