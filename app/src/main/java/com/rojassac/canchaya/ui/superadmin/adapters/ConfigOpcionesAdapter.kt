package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.databinding.ItemConfigOpcionBinding
import com.rojassac.canchaya.ui.superadmin.fragments.ConfiguracionFragment.ConfigOpcion

/**
 * ✅ ACTUALIZADO (24 Oct 2025)
 * Adapter para mostrar las opciones de configuración del SuperAdmin
 */
class ConfigOpcionesAdapter(
    private val onOpcionClick: (ConfigOpcion) -> Unit
) : ListAdapter<ConfigOpcion, ConfigOpcionesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConfigOpcionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onOpcionClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemConfigOpcionBinding,
        private val onOpcionClick: (ConfigOpcion) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(opcion: ConfigOpcion) {
            binding.apply {
                tvTitulo.text = opcion.titulo
                tvDescripcion.text = opcion.descripcion
                ivIcono.setImageResource(opcion.icono)

                root.setOnClickListener {
                    onOpcionClick(opcion)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ConfigOpcion>() {
        override fun areItemsTheSame(oldItem: ConfigOpcion, newItem: ConfigOpcion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ConfigOpcion, newItem: ConfigOpcion): Boolean {
            return oldItem == newItem
        }
    }
}
