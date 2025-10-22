package com.rojassac.canchaya.ui.admin.perfil

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.databinding.ItemPlanSuscripcionBinding
import com.rojassac.canchaya.utils.Constants

class PlanesAdapter(
    private var planes: List<Plan>,
    private val planActualId: String?,
    private val onPlanSelected: (Plan) -> Unit
) : RecyclerView.Adapter<PlanesAdapter.PlanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = ItemPlanSuscripcionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(planes[position])
    }

    override fun getItemCount() = planes.size

    inner class PlanViewHolder(
        private val binding: ItemPlanSuscripcionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: Plan) {
            val context = binding.root.context

            // Nombre del plan
            binding.tvNombrePlan.text = plan.nombre

            // Precio
            if (plan.precio == 0.0) {
                binding.tvPrecio.text = context.getString(R.string.plan_gratis)
                binding.tvPeriodo.visibility = View.GONE
            } else {
                binding.tvPrecio.text = "S/ ${plan.precio.toInt()}"
                binding.tvPeriodo.visibility = View.VISIBLE
            }

            // Comisión
            val comisionTexto = "${(plan.comision * 100).toInt()}% ${context.getString(R.string.comision)}"
            binding.tvComision.text = comisionTexto

            // Plazo de retiro
            val plazoTexto = when (plan.plazoRetiro) {
                0 -> context.getString(R.string.caracteristica_retiro_inmediato)
                1 -> "Retiro en 24 horas"
                else -> "Retiro en ${plan.plazoRetiro} días"
            }
            binding.tvPlazoRetiro.text = plazoTexto

            // Características opcionales
            binding.layoutDestacado.visibility = if (plan.destacado) View.VISIBLE else View.GONE
            binding.layoutPrioridad.visibility = if (plan.posicionPrioritaria) View.VISIBLE else View.GONE
            binding.layoutMarketing.visibility = if (plan.marketingIncluido) View.VISIBLE else View.GONE

            // Badge popular (mostrar en plan PRO)
            binding.tvBadgePopular.visibility =
                if (plan.id == Constants.PLAN_PRO) View.VISIBLE else View.GONE

            // ✅ Verificar si es el plan actual
            val esPlanActual = plan.id == planActualId

            // ✅ Color y estilo del plan
            try {
                val colorPlan = Color.parseColor(plan.color)

                if (esPlanActual) {
                    // ✅ Si es el plan actual: color verde de éxito
                    binding.btnSeleccionarPlan.setBackgroundColor(
                        context.getColor(R.color.success)
                    )
                    binding.cardPlan.strokeColor = context.getColor(R.color.success)
                    binding.cardPlan.strokeWidth = 4
                } else {
                    // Color normal del plan
                    binding.btnSeleccionarPlan.setBackgroundColor(colorPlan)
                    binding.cardPlan.strokeColor = colorPlan
                    binding.cardPlan.strokeWidth = 2
                }
            } catch (e: Exception) {
                // Color por defecto si falla el parsing
                if (esPlanActual) {
                    binding.btnSeleccionarPlan.setBackgroundColor(
                        context.getColor(R.color.success)
                    )
                } else {
                    binding.btnSeleccionarPlan.setBackgroundColor(
                        context.getColor(R.color.primary_green)
                    )
                }
            }

            // ✅ Estado y texto del botón
            if (esPlanActual) {
                binding.btnSeleccionarPlan.text = "✓ Plan Actual"
                binding.btnSeleccionarPlan.isEnabled = false
                binding.btnSeleccionarPlan.alpha = 1f // Mantener visible
                binding.btnSeleccionarPlan.setTextColor(context.getColor(R.color.white))
            } else {
                binding.btnSeleccionarPlan.text = "Actualizar Plan"
                binding.btnSeleccionarPlan.isEnabled = true
                binding.btnSeleccionarPlan.alpha = 1f
                binding.btnSeleccionarPlan.setTextColor(context.getColor(R.color.white))
            }

            // ✅ Click listener (solo funciona si NO es el plan actual)
            binding.btnSeleccionarPlan.setOnClickListener {
                if (!esPlanActual) {
                    onPlanSelected(plan)
                }
            }

            // ✅ Hacer clickeable toda la tarjeta (opcional)
            binding.cardPlan.setOnClickListener {
                if (!esPlanActual) {
                    onPlanSelected(plan)
                }
            }
        }
    }

    /**
     * Actualiza la lista de planes
     */
    fun actualizarPlanes(nuevosPlanes: List<Plan>) {
        planes = nuevosPlanes
        notifyDataSetChanged()
    }
}
