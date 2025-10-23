package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.ItemUsuarioBinding

/**
 * ✅ CÓDIGO EXISTENTE MANTENIDO
 * 🔧 MODIFICADO: Click en card para mostrar opciones (23 Oct 2025)
 */
class UsuariosAdapter(
    private val onUserClick: (User) -> Unit // 🆕 NUEVO callback
) : ListAdapter<User, UsuariosAdapter.UsuarioViewHolder>(UsuarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UsuarioViewHolder(
        private val binding: ItemUsuarioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                // Nombre y email
                tvNombre.text = user.nombre
                tvEmail.text = user.email

                // Inicial
                tvInicial.text = user.nombre.firstOrNull()?.toString()?.uppercase() ?: "?"

                // Celular con emoji
                tvCelular.text = if (!user.celular.isNullOrEmpty()) {
                    "📱 ${user.celular}"
                } else {
                    "📱 N/A"
                }

                // Badge de rol con colores
                chipRol.text = when (user.rol) {
                    UserRole.SUPERADMIN -> "SUPERADMIN"
                    UserRole.ADMIN -> "ADMIN"
                    UserRole.USUARIO -> "USUARIO"
                }

                chipRol.setChipBackgroundColorResource(
                    when (user.rol) {
                        UserRole.SUPERADMIN -> R.color.badge_superadmin_bg
                        UserRole.ADMIN -> R.color.badge_admin_bg
                        UserRole.USUARIO -> R.color.badge_usuario_bg
                    }
                )

                chipRol.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        when (user.rol) {
                            UserRole.SUPERADMIN -> R.color.badge_superadmin_text
                            UserRole.ADMIN -> R.color.badge_admin_text
                            UserRole.USUARIO -> R.color.badge_usuario_text
                        }
                    )
                )

                // Estado activo/inactivo
                chipEstado.text = if (user.activo) "Activo" else "Inactivo"
                chipEstado.setChipBackgroundColorResource(
                    if (user.activo) R.color.status_active_bg else R.color.status_inactive_bg
                )
                chipEstado.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        if (user.activo) R.color.status_active_text else R.color.status_inactive_text
                    )
                )

                // 🆕 NUEVO: Click en la card completa para mostrar opciones
                cardUsuario.setOnClickListener {
                    onUserClick(user)
                }
            }
        }
    }

    class UsuarioDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
