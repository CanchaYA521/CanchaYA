package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.ItemSedeCardBinding
import com.rojassac.canchaya.data.model.Sede

/**
 * ✅ CÓDIGO EXISTENTE MANTENIDO
 * ✨ ACTUALIZADO: Agregada lógica de expansión/contracción (22 Oct 2025)
 */
class SedesAdapter(
    private var sedes: List<Sede>,
    private val onVerCanchas: (Sede) -> Unit,
    private val onEditar: (Sede) -> Unit,
    private val onEliminar: (Sede) -> Unit,
    private val onAgregarCancha: (Sede) -> Unit
) : RecyclerView.Adapter<SedesAdapter.SedeViewHolder>() {

    // ✨ NUEVO: Mapa para trackear qué items están expandidos
    private val expandedItems = mutableSetOf<String>()

    inner class SedeViewHolder(private val binding: ItemSedeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sede: Sede) {
            binding.apply {
                // ✅ CÓDIGO EXISTENTE: Nombre de la sede
                tvNombreSede.text = sede.nombre

                // ✅ CÓDIGO EXISTENTE: Dirección
                tvDireccion.text = sede.direccion

                // ✅ CÓDIGO EXISTENTE: Horario de operación
                tvHorario.text = "⏰ ${sede.getHorarioDisplay()}"

                // ✅ CÓDIGO EXISTENTE: Coordenadas GPS (ahora en el TextView de teléfono)
                tvTelefono.text = if (sede.telefono.isNotEmpty()) {
                    "📞 ${sede.telefono}"
                } else {
                    "📍 ${sede.latitud}, ${sede.longitud}"
                }

                // ✅ CÓDIGO EXISTENTE: Número de canchas
                val numCanchas = sede.canchaIds?.size ?: 0
                tvNumCanchas.text = "🏟️ $numCanchas ${if (numCanchas == 1) "cancha" else "canchas"}"

                // ✅ CÓDIGO EXISTENTE: Estado de la sede
                if (sede.activa) {
                    tvEstado.text = "Activa"
                    badgeEstado.setCardBackgroundColor(
                        itemView.context.getColor(R.color.success)
                    )
                } else {
                    tvEstado.text = "Inactiva"
                    badgeEstado.setCardBackgroundColor(
                        itemView.context.getColor(R.color.error)
                    )
                }

                // ✅ CÓDIGO EXISTENTE: Cargar imagen con Glide
                Glide.with(itemView.context)
                    .load(sede.imageUrl)
                    .placeholder(R.drawable.ic_sede_placeholder)
                    .error(R.drawable.ic_sede_placeholder)
                    .centerCrop()
                    .into(ivSede)

                // ✨ NUEVO: Verificar si el item está expandido
                val isExpanded = expandedItems.contains(sede.id)
                layoutOpciones.visibility = if (isExpanded) View.VISIBLE else View.GONE

                // ✨ NUEVO: Rotar icono según estado
                iconExpand.rotation = if (isExpanded) 180f else 0f

                // ✨ NUEVO: Click en la tarjeta principal para expandir/contraer
                layoutPrincipal.setOnClickListener {
                    toggleExpansion(sede.id)
                }

                // ✅ CÓDIGO EXISTENTE: Botón ver canchas
                btnVerCanchas.setOnClickListener {
                    onVerCanchas(sede)
                }

                // ✅ CÓDIGO EXISTENTE: Botón editar
                btnEditar.setOnClickListener {
                    onEditar(sede)
                }

                // ✅ CÓDIGO EXISTENTE: Botón eliminar
                btnEliminar.setOnClickListener {
                    onEliminar(sede)
                }

                // ✅ CÓDIGO EXISTENTE: Botón agregar cancha
                btnAgregarCancha.setOnClickListener {
                    onAgregarCancha(sede)
                }
            }
        }
    }

    // ✨ NUEVO: Función para expandir/contraer items
    private fun toggleExpansion(sedeId: String) {
        if (expandedItems.contains(sedeId)) {
            expandedItems.remove(sedeId)
        } else {
            expandedItems.add(sedeId)
        }
        notifyDataSetChanged()
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

    // ✅ CÓDIGO EXISTENTE: Función para actualizar la lista
    fun updateList(newSedes: List<Sede>) {
        sedes = newSedes
        notifyDataSetChanged()
    }
}
