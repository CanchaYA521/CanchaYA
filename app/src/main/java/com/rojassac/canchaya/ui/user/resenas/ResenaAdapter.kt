package com.rojassac.canchaya.ui.user.resenas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Resena
import com.rojassac.canchaya.databinding.ItemResenaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ResenaAdapter(
    private val onLikeClick: (Resena) -> Unit
) : ListAdapter<Resena, ResenaAdapter.ResenaViewHolder>(ResenaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResenaViewHolder {
        val binding = ItemResenaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResenaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResenaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ResenaViewHolder(
        private val binding: ItemResenaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(resena: Resena) {
            binding.apply {
                // Usuario
                tvUserName.text = resena.userName

                // Foto de usuario
                if (resena.userPhotoUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(resena.userPhotoUrl)
                        .placeholder(R.drawable.ic_user)
                        .circleCrop()
                        .into(ivUserPhoto)
                } else {
                    ivUserPhoto.setImageResource(R.drawable.ic_user)
                }

                // Fecha relativa
                tvFecha.text = obtenerTiempoRelativo(resena.timestamp)

                // Calificación
                ratingBar.rating = resena.calificacion

                // Comentario
                tvComentario.text = resena.comentario

                // Likes
                tvLikes.text = resena.likes.toString()

                // Botón like
                btnLike.setOnClickListener {
                    onLikeClick(resena)
                }
            }
        }

        private fun obtenerTiempoRelativo(timestamp: Long): String {
            val ahora = System.currentTimeMillis()
            val diferencia = ahora - timestamp

            return when {
                diferencia < TimeUnit.MINUTES.toMillis(1) -> "Justo ahora"
                diferencia < TimeUnit.HOURS.toMillis(1) -> {
                    val minutos = TimeUnit.MILLISECONDS.toMinutes(diferencia)
                    "Hace $minutos ${if (minutos == 1L) "minuto" else "minutos"}"
                }
                diferencia < TimeUnit.DAYS.toMillis(1) -> {
                    val horas = TimeUnit.MILLISECONDS.toHours(diferencia)
                    "Hace $horas ${if (horas == 1L) "hora" else "horas"}"
                }
                diferencia < TimeUnit.DAYS.toMillis(7) -> {
                    val dias = TimeUnit.MILLISECONDS.toDays(diferencia)
                    "Hace $dias ${if (dias == 1L) "día" else "días"}"
                }
                else -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es", "PE"))
                    sdf.format(Date(timestamp))
                }
            }
        }
    }
}

class ResenaDiffCallback : DiffUtil.ItemCallback<Resena>() {
    override fun areItemsTheSame(oldItem: Resena, newItem: Resena): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Resena, newItem: Resena): Boolean {
        return oldItem == newItem
    }
}
