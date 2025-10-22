package com.rojassac.canchaya.ui.admin.perfil

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.databinding.ItemAdminPerfilOptionBinding

class AdminPerfilAdapter(
    private val items: List<AdminPerfilMenuItem>,
    private val onItemClick: (AdminPerfilMenuItem) -> Unit
) : RecyclerView.Adapter<AdminPerfilAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminPerfilOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemAdminPerfilOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminPerfilMenuItem) {
            // ✅ CORREGIDO: Usar los nombres correctos del XML
            binding.ivOptionIcon.setImageResource(item.icon)
            binding.tvOptionTitle.text = item.titulo
            binding.tvOptionDescription.text = item.descripcion

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
