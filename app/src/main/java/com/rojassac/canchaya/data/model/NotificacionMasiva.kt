package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ✅ NUEVO ARCHIVO (23 Oct 2025)
 * Modelo para notificaciones masivas enviadas por el SuperAdmin
 */
@Parcelize
data class NotificacionMasiva(
    val id: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val destinatarios: TipoDestinatario = TipoDestinatario.TODOS_USUARIOS,
    val fechaEnvio: Long = System.currentTimeMillis(),
    val cantidadEnviados: Int = 0,
    val cantidadLeidos: Int = 0,
    val urlImagen: String = "", // URL de imagen opcional
    val accion: String = "", // Acción al tocar la notificación (ej: "abrir_promociones")
    val enviadoPor: String = "", // UID del superadmin
    val estado: EstadoNotificacion = EstadoNotificacion.PENDIENTE
) : Parcelable

/**
 * ✅ NUEVO ENUM: A quién se envía la notificación
 */
enum class TipoDestinatario {
    TODOS_USUARIOS,     // Todos los usuarios de la app
    SOLO_ADMINS,        // Solo administradores de sedes
    SOLO_CLIENTES,      // Solo usuarios finales (no admins)
    USUARIOS_ACTIVOS,   // Solo usuarios con cuenta activa
    USUARIOS_INACTIVOS  // Usuarios que no han usado la app recientemente
}

/**
 * ✅ NUEVO ENUM: Estado de la notificación
 */
enum class EstadoNotificacion {
    PENDIENTE,   // Creada pero no enviada
    ENVIANDO,    // En proceso de envío
    ENVIADA,     // Enviada exitosamente
    ERROR        // Error al enviar
}
