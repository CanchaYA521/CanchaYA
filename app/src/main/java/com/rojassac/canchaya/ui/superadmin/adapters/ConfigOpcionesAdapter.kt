package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.databinding.ItemConfigOptionBinding
import com.rojassac.canchaya.ui.superadmin.fragments.ConfigOpcion

/**
 * ✅ NUEVO ARCHIVO (23 Oct 2025)
 * Adapter para las opciones del menú de configuración del SuperAdmin
 */
class ConfigOpcionesAdapter(
    private val onOpcionClick: (ConfigOpcion) -> Unit
) : ListAdapter<ConfigOpcion, ConfigOpcionesAdapter.ConfigOpcionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigOpcionViewHolder {
        val binding = ItemConfigOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConfigOpcionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConfigOpcionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConfigOpcionViewHolder(
        private val binding: ItemConfigOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(opcion: ConfigOpcion) {
            binding.apply {
                tvTitulo.text = opcion.titulo
                tvDescripcion.text = opcion.descripcion
                iconOption.setImageResource(opcion.icono)

                cardConfigOption.setOnClickListener {
                    onOpcionClick(opcion)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ConfigOpcion>() {
        override fun areItemsTheSame(oldItem: ConfigOpcion, newItem: ConfigOpcion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ConfigOpcion, newItem: ConfigOpcion): Boolean {
            return oldItem == newItem
        }
    }
}
