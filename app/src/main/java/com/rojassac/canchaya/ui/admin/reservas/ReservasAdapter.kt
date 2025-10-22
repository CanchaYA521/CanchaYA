package com.rojassac.canchaya.ui.admin.reservas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.EstadoReserva
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.databinding.ItemReservaBinding
import java.text.SimpleDateFormat
import java.util.*

class ReservasAdapter(
    private val onReservaClick: (Reserva) -> Unit
) : ListAdapter<Reserva, ReservasAdapter.ReservaViewHolder>(ReservaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val binding = ItemReservaBinding.inflate(  // ✅ Usa el binding del usuario
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReservaViewHolder(binding, onReservaClick)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReservaViewHolder(
        private val binding: ItemReservaBinding,
        private val onReservaClick: (Reserva) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reserva: Reserva) {
            binding.apply {
                // Información del usuario
                tvUsuarioNombre.text = reserva.usuarioNombre
                tvUsuarioCelular.text = reserva.usuarioCelular

                // Fecha y hora
                tvFecha.text = formatearFecha(reserva.fecha)
                tvHorario.text = "${reserva.horaInicio} - ${reserva.horaFin}"

                // Precio
                tvPrecio.text = "S/ ${String.format("%.2f", reserva.precio)}"

                // Estado con color
                configurarEstado(reserva.estado)

                // Click
                root.setOnClickListener {
                    onReservaClick(reserva)
                }
            }
        }

        private fun configurarEstado(estado: EstadoReserva) {
            val (texto, colorResId) = when (estado) {
                EstadoReserva.PENDIENTE -> "Pendiente" to R.color.warning
                EstadoReserva.CONFIRMADA -> "Confirmada" to R.color.success
                EstadoReserva.COMPLETADA -> "Completada" to R.color.info
                EstadoReserva.CANCELADA -> "Cancelada" to R.color.error
            }

            binding.tvEstado.text = texto
            binding.tvEstado.setTextColor(
                ContextCompat.getColor(binding.root.context, colorResId)
            )
        }

        private fun formatearFecha(fecha: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd 'de' MMMM", Locale("es", "PE"))
                val date = inputFormat.parse(fecha)
                date?.let { outputFormat.format(it) } ?: fecha
            } catch (e: Exception) {
                fecha
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
