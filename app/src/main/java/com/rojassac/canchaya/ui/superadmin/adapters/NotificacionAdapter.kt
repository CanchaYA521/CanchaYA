package com.rojassac.canchaya.ui.superadmin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.EstadoNotificacion
import com.rojassac.canchaya.data.model.NotificacionMasiva
import com.rojassac.canchaya.data.model.TipoNotificacion
import com.rojassac.canchaya.databinding.ItemNotificacionMasivaBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ NUEVO (24 Oct 2025)
 * Adapter para mostrar notificaciones masivas en RecyclerView
 */
class NotificacionAdapter(
    private val onItemClick: (NotificacionMasiva) -> Unit,
    private val onCancelarClick: (NotificacionMasiva) -> Unit,
    private val onDuplicarClick: (NotificacionMasiva) -> Unit
) : ListAdapter<NotificacionMasiva, NotificacionAdapter.NotificacionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val binding = ItemNotificacionMasivaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificacionViewHolder(
        private val binding: ItemNotificacionMasivaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notificacion: NotificacionMasiva) {
            binding.apply {
                // 📝 Título y mensaje
                tvTitulo.text = notificacion.titulo
                tvMensaje.text = notificacion.mensaje

                // 🎨 Icono y color del tipo
                val tipo = notificacion.getTipoEnum()
                tvTipoIcono.text = tipo.icono
                cardTipo.setCardBackgroundColor(Color.parseColor(tipo.color))

                // 📊 Estado
                val estado = notificacion.getEstadoEnum()
                tvEstado.text = estado.name
                tvEstado.setTextColor(Color.parseColor(estado.color))

                // 👥 Destinatarios
                tvDestinatarios.text = notificacion.getDestinatariosEnum().descripcion

                // 📅 Fecha
                val fecha = if (notificacion.fechaEnvio != null) {
                    "Enviado: ${formatearFecha(notificacion.fechaEnvio)}"
                } else if (notificacion.fechaProgramada != null) {
                    "Programado: ${formatearFecha(notificacion.fechaProgramada)}"
                } else {
                    "Creado: ${formatearFecha(notificacion.fechaCreacion)}"
                }
                tvFecha.text = fecha

                // 📊 Estadísticas (solo para enviadas)
                if (estado == EstadoNotificacion.ENVIADA) {
                    layoutEstadisticas.visibility = View.VISIBLE

                    tvTotalEnviados.text = notificacion.totalEnviados.toString()
                    tvTotalVistos.text = "${notificacion.totalVistos} (${notificacion.getPorcentajeVisto()}%)"
                    tvTotalClics.text = "${notificacion.totalClics} (${notificacion.getPorcentajeClics()}%)"

                    // Barra de progreso de visualización
                    progressVistas.progress = notificacion.getPorcentajeVisto()
                } else {
                    layoutEstadisticas.visibility = View.GONE
                }

                // 🎯 Botones de acción
                configurarBotones(notificacion)

                // Click en el item completo
                root.setOnClickListener {
                    onItemClick(notificacion)
                }
            }
        }

        private fun configurarBotones(notificacion: NotificacionMasiva) {
            binding.apply {
                val estado = notificacion.getEstadoEnum()

                when (estado) {
                    EstadoNotificacion.PROGRAMADA, EstadoNotificacion.PENDIENTE -> {
                        // Mostrar botón cancelar
                        btnCancelar.visibility = View.VISIBLE
                        btnCancelar.setOnClickListener {
                            onCancelarClick(notificacion)
                        }

                        btnDuplicar.visibility = View.VISIBLE
                        btnDuplicar.setOnClickListener {
                            onDuplicarClick(notificacion)
                        }
                    }
                    EstadoNotificacion.ENVIADA, EstadoNotificacion.FALLIDA, EstadoNotificacion.CANCELADA -> {
                        // Solo mostrar duplicar
                        btnCancelar.visibility = View.GONE

                        btnDuplicar.visibility = View.VISIBLE
                        btnDuplicar.setOnClickListener {
                            onDuplicarClick(notificacion)
                        }
                    }
                    else -> {
                        btnCancelar.visibility = View.GONE
                        btnDuplicar.visibility = View.GONE
                    }
                }
            }
        }

        private fun formatearFecha(fecha: Date): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(fecha)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificacionMasiva>() {
        override fun areItemsTheSame(
            oldItem: NotificacionMasiva,
            newItem: NotificacionMasiva
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: NotificacionMasiva,
            newItem: NotificacionMasiva
        ): Boolean {
            return oldItem == newItem
        }
    }
}
