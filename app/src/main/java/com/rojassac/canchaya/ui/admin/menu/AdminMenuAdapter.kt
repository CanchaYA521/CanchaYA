package com.rojassac.canchaya.ui.admin.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.databinding.ItemAdminMenuBinding
import com.rojassac.canchaya.ui.admin.AdminMenuItem

class AdminMenuAdapter(
    private val items: List<AdminMenuItem>,
    private val onItemClick: (AdminMenuItem) -> Unit
) : RecyclerView.Adapter<AdminMenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(
        private val binding: ItemAdminMenuBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminMenuItem) {
            binding.apply {
                tvTitulo.text = item.titulo
                tvDescripcion.text = item.descripcion
                ivIcono.setImageResource(item.icono)

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemAdminMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
