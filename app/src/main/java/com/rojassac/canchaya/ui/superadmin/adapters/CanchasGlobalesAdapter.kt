package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.databinding.ItemCanchaGlobalBinding

class CanchasGlobalesAdapter(
    private var canchas: List<Cancha>,
    private val onToggleStatus: (Cancha) -> Unit,
    private val onDelete: (Cancha) -> Unit,
    private val onAssignAdmin: (Cancha) -> Unit
) : RecyclerView.Adapter<CanchasGlobalesAdapter.CanchaViewHolder>() {

    inner class CanchaViewHolder(private val binding: ItemCanchaGlobalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cancha: Cancha) {
            binding.tvNombre.text = cancha.nombre
            binding.tvDireccion.text = cancha.direccion
            binding.tvPrecio.text = "S/ ${cancha.precioHora}/hora"
            binding.tvEstado.text = if (cancha.activo) "Activa" else "Desactivada"
            binding.tvCodigoVinculacion.text = "Código: ${cancha.codigoVinculacion}"

            binding.tvEstado.setTextColor(
                if (cancha.activo)
                    binding.root.context.getColor(R.color.primary_green)
                else
                    binding.root.context.getColor(R.color.red_error)
            )

            // Cargar imagen con Glide
            Glide.with(binding.root.context)
                .load(cancha.imagenUrl)
                .placeholder(R.drawable.ic_cancha_placeholder)
                .into(binding.ivCancha)

            binding.btnToggleStatus.text = if (cancha.activo) "Desactivar" else "Activar"
            binding.btnToggleStatus.setOnClickListener { onToggleStatus(cancha) }

            binding.btnDelete.setOnClickListener { onDelete(cancha) }

            binding.btnAssignAdmin.setOnClickListener { onAssignAdmin(cancha) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanchaViewHolder {
        val binding = ItemCanchaGlobalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CanchaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CanchaViewHolder, position: Int) {
        holder.bind(canchas[position])
    }

    override fun getItemCount(): Int = canchas.size

    fun updateCanchas(newCanchas: List<Cancha>) {
        canchas = newCanchas
        notifyDataSetChanged()
    }
}
