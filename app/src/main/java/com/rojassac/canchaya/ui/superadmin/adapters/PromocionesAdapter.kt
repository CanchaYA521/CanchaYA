package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.data.model.Promocion
import com.rojassac.canchaya.data.model.TipoDescuento
import com.rojassac.canchaya.databinding.ItemPromocionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PromocionesAdapter(
    private val onEditClick: (Promocion) -> Unit,
    private val onDeleteClick: (Promocion) -> Unit,
    private val onToggleClick: (Promocion, Boolean) -> Unit,
    private val onStatsClick: (Promocion) -> Unit
) : ListAdapter<Promocion, PromocionesAdapter.PromocionViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromocionViewHolder {
        val binding = ItemPromocionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PromocionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromocionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PromocionViewHolder(
        private val binding: ItemPromocionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(promocion: Promocion) {
            binding.apply {
                tvCodigo.text = promocion.codigo
                tvNombre.text = promocion.nombre
                tvDescripcion.text = promocion.descripcion

                tvDescuento.text = when (promocion.tipoDescuento) {
                    TipoDescuento.PORCENTAJE -> "${promocion.valorDescuento.toInt()}%"
                    TipoDescuento.MONTO_FIJO -> "S/. ${String.format("%.2f", promocion.valorDescuento)}"
                }

                val usosTexto = if (promocion.usosMaximos == -1) {
                    "${promocion.usosActuales} usos"
                } else {
                    "${promocion.usosActuales}/${promocion.usosMaximos} usos"
                }
                tvUsos.text = usosTexto

                val fechaInicioStr = dateFormat.format(Date(promocion.fechaInicio))
                val fechaFinStr = dateFormat.format(Date(promocion.fechaFin))
                tvVigencia.text = "Válido: $fechaInicioStr - $fechaFinStr"

                switchActivo.setOnCheckedChangeListener(null)
                switchActivo.isChecked = promocion.activo
                switchActivo.setOnCheckedChangeListener { _, isChecked ->
                    onToggleClick(promocion, isChecked)
                }

                btnEditar.setOnClickListener {
                    onEditClick(promocion)
                }

                btnEliminar.setOnClickListener {
                    onDeleteClick(promocion)
                }

                btnEstadisticas.setOnClickListener {
                    onStatsClick(promocion)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Promocion>() {
        override fun areItemsTheSame(oldItem: Promocion, newItem: Promocion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Promocion, newItem: Promocion): Boolean {
            return oldItem == newItem
        }
    }
}
