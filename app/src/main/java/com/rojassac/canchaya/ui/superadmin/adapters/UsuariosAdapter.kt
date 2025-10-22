package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.ItemUsuarioBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsuariosAdapter(
    private val onEditClick: (User) -> Unit,
    private val onToggleStatusClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit,
    private val onAssignCanchaClick: (User) -> Unit
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

                // Avatar con inicial
                tvAvatar.text = user.nombre.firstOrNull()?.uppercase() ?: "U"

                // Badge de rol
                chipRol.text = when (user.rol) {
                    UserRole.USUARIO -> "Usuario"
                    UserRole.ADMIN -> "Admin"
                    UserRole.SUPERADMIN -> "SuperAdmin"
                }

                // Color del chip según rol
                val chipColor = when (user.rol) {
                    UserRole.USUARIO -> R.color.chip_usuario
                    UserRole.ADMIN -> R.color.chip_admin
                    UserRole.SUPERADMIN -> R.color.chip_superadmin
                }
                chipRol.setChipBackgroundColorResource(chipColor)

                // Estado activo/inactivo
                chipEstado.text = if (user.activo) "Activo" else "Inactivo"
                chipEstado.setChipBackgroundColorResource(
                    if (user.activo) R.color.chip_activo else R.color.chip_inactivo
                )

                // Fecha de registro
                val fechaFormateada = formatDate(user.fechaCreacion)
                tvFechaRegistro.text = "Registro: $fechaFormateada"

                // Cancha asignada (solo para admins)
                if (user.rol == UserRole.ADMIN && !user.canchaId.isNullOrEmpty()) {
                    tvCanchaAsignada.visibility = View.VISIBLE
                    tvCanchaAsignada.text = "Cancha: ${user.canchaId.take(8)}..."
                } else {
                    tvCanchaAsignada.visibility = View.GONE
                }

                // Botones de acción
                btnEdit.setOnClickListener { onEditClick(user) }
                btnToggleStatus.setOnClickListener { onToggleStatusClick(user) }
                btnDelete.setOnClickListener { onDeleteClick(user) }

                // Botón asignar cancha (solo visible para ADMIN)
                if (user.rol == UserRole.ADMIN) {
                    btnAssignCancha.visibility = View.VISIBLE
                    btnAssignCancha.setOnClickListener { onAssignCanchaClick(user) }
                } else {
                    btnAssignCancha.visibility = View.GONE
                }

                // Expandir/Colapsar detalles
                cardUsuario.setOnClickListener {
                    if (layoutDetalles.visibility == View.VISIBLE) {
                        layoutDetalles.visibility = View.GONE
                    } else {
                        layoutDetalles.visibility = View.VISIBLE
                    }
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
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
