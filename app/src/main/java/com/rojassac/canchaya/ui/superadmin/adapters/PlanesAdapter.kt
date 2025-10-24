package com.rojassac.canchaya.ui.superadmin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.databinding.ItemPlanEditableBinding

/**
 * ✅ NUEVO ARCHIVO (23 Oct 2025)
 * Adapter para mostrar planes editables en GestionPlanesFragment
 */
class PlanesAdapter(
    private val onEditClick: (Plan) -> Unit
) : ListAdapter<Plan, PlanesAdapter.PlanViewHolder>(DiffCallback()) {

    private val suscriptoresMap = mutableMapOf<String, Int>()

    fun updateSuscriptores(nuevosSuscriptores: Map<String, Int>) {
        suscriptoresMap.clear()
        suscriptoresMap.putAll(nuevosSuscriptores)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = ItemPlanEditableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlanViewHolder(
        private val binding: ItemPlanEditableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: Plan) {
            binding.apply {
                // Nombre
                tvNombrePlan.text = plan.nombre
                tvNombrePlan.setTextColor(Color.parseColor(plan.color))

                // Precio
                tvPrecio.text = if (plan.precio > 0) {
                    String.format("%.2f", plan.precio)
                } else {
                    "GRATIS"
                }
                tvPrecio.setTextColor(Color.parseColor(plan.color))

                // Descripción
                tvDescripcion.text = plan.descripcion

                // Comisión
                val comisionPorcentaje = (plan.comision * 100).toInt()
                tvComision.text = "$comisionPorcentaje%"

                // Max Canchas
                tvMaxCanchas.text = plan.maxCanchas.toString()

                // Suscriptores
                val cantidadSuscriptores = suscriptoresMap[plan.id] ?: 0
                tvSuscriptores.text = cantidadSuscriptores.toString()

                // Estado
                if (plan.activo) {
                    tvEstado.text = "Activo"
                    tvEstado.setBackgroundResource(R.drawable.bg_badge_success)
                } else {
                    tvEstado.text = "Inactivo"
                    tvEstado.setBackgroundResource(R.drawable.bg_badge_error)
                }

                // Border color del card
                cardPlan.strokeColor = Color.parseColor(plan.color)

                // Botón editar
                btnEditar.setOnClickListener {
                    onEditClick(plan)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Plan>() {
        override fun areItemsTheSame(oldItem: Plan, newItem: Plan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Plan, newItem: Plan): Boolean {
            return oldItem == newItem
        }
    }
}
