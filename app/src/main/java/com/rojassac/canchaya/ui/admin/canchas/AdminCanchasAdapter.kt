package com.rojassac.canchaya.ui.admin.canchas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.databinding.ItemAdminCanchaBinding

class AdminCanchasAdapter(
    private val onEditClick: (Cancha) -> Unit,
    private val onDeleteClick: (Cancha) -> Unit,
    private val onToggleActivoClick: (Cancha, Boolean) -> Unit
) : ListAdapter<Cancha, AdminCanchasAdapter.CanchaViewHolder>(CanchaDiffCallback()) {

    inner class CanchaViewHolder(
        private val binding: ItemAdminCanchaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cancha: Cancha) {
            binding.apply {
                // Información básica
                tvNombreCancha.text = cancha.nombre
                tvDireccion.text = "${cancha.direccion}, ${cancha.distrito}"
                tvPrecio.text = "S/ ${String.format("%.2f", cancha.precioHora)}/hora"
                tvHorario.text = "${cancha.horarioApertura} - ${cancha.horarioCierre}"

                // Estado activo/inactivo
                switchActivo.isChecked = cancha.activo
                tvEstado.text = if (cancha.activo) "Activo" else "Inactivo"
                tvEstado.setTextColor(
                    if (cancha.activo)
                        itemView.context.getColor(R.color.success)
                    else
                        itemView.context.getColor(R.color.error)
                )

                // Servicios
                tvServicios.text = if (cancha.servicios.isNotEmpty()) {
                    cancha.servicios.joinToString(", ")
                } else {
                    "Sin servicios"
                }

                // Estadísticas
                tvCalificacion.text = String.format("%.1f", cancha.calificacionPromedio)
                tvTotalResenas.text = "(${cancha.totalResenas})"

                // Listeners
                btnEditar.setOnClickListener {
                    onEditClick(cancha)
                }

                btnEliminar.setOnClickListener {
                    onDeleteClick(cancha)
                }

                switchActivo.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != cancha.activo) {
                        onToggleActivoClick(cancha, isChecked)
                    }
                }

                // Click en la card para ver detalles
                root.setOnClickListener {
                    onEditClick(cancha)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanchaViewHolder {
        val binding = ItemAdminCanchaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CanchaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CanchaViewHolder, position: Int) {
        holder.bind(getItem(position))
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
