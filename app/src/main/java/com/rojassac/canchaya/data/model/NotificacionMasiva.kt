package com.rojassac.canchaya.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * ✅ NUEVO (23 Oct 2025)
 * Modelo para notificaciones masivas enviadas desde SuperAdmin
 */
data class NotificacionMasiva(
    @PropertyName("id") val id: String = "",
    @PropertyName("titulo") val titulo: String = "",
    @PropertyName("mensaje") val mensaje: String = "",
    @PropertyName("tipo") val tipo: String = TipoNotificacion.INFO.name,
    @PropertyName("urlDestino") val urlDestino: String? = null,
    @PropertyName("imagenUrl") val imagenUrl: String? = null,

    // 🎯 Segmentación de destinatarios
    @PropertyName("destinatarios") val destinatarios: String = DestinatariosType.TODOS.name,
    @PropertyName("sedeId") val sedeId: String? = null,
    @PropertyName("planRequerido") val planRequerido: String? = null,

    // 📅 Programación
    @PropertyName("envioInmediato") val envioInmediato: Boolean = true,
    @PropertyName("fechaProgramada") val fechaProgramada: Date? = null,
    @PropertyName("estado") val estado: String = EstadoNotificacion.PENDIENTE.name,

    // 📊 Estadísticas de envío
    @PropertyName("totalDestinatarios") val totalDestinatarios: Int = 0,
    @PropertyName("totalEnviados") val totalEnviados: Int = 0,
    @PropertyName("totalEntregados") val totalEntregados: Int = 0,
    @PropertyName("totalVistos") val totalVistos: Int = 0,
    @PropertyName("totalClics") val totalClics: Int = 0,
    @PropertyName("totalFallidos") val totalFallidos: Int = 0,

    // 🔒 Metadata
    @PropertyName("creadoPor") val creadoPor: String = "",
    @PropertyName("nombreCreador") val nombreCreador: String = "",
    @PropertyName("fechaCreacion") val fechaCreacion: Date = Date(),
    @PropertyName("fechaEnvio") val fechaEnvio: Date? = null,
    @PropertyName("fechaCompletado") val fechaCompletado: Date? = null,

    // 🔔 FCM
    @PropertyName("fcmMessageId") val fcmMessageId: String? = null,
    @PropertyName("prioridad") val prioridad: String = PrioridadNotificacion.NORMAL.name
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(
        id = "",
        titulo = "",
        mensaje = "",
        tipo = TipoNotificacion.INFO.name
    )

    /**
     * Obtiene el tipo como enum
     */
    fun getTipoEnum(): TipoNotificacion {
        return try {
            TipoNotificacion.valueOf(tipo)
        } catch (e: Exception) {
            TipoNotificacion.INFO
        }
    }

    /**
     * Obtiene el estado como enum
     */
    fun getEstadoEnum(): EstadoNotificacion {
        return try {
            EstadoNotificacion.valueOf(estado)
        } catch (e: Exception) {
            EstadoNotificacion.PENDIENTE
        }
    }

    /**
     * Obtiene los destinatarios como enum
     */
    fun getDestinatariosEnum(): DestinatariosType {
        return try {
            DestinatariosType.valueOf(destinatarios)
        } catch (e: Exception) {
            DestinatariosType.TODOS
        }
    }

    /**
     * Calcula el porcentaje de entrega
     */
    fun getPorcentajeEntrega(): Int {
        return if (totalEnviados > 0) {
            ((totalEntregados.toDouble() / totalEnviados) * 100).toInt()
        } else 0
    }

    /**
     * Calcula el porcentaje de visualización
     */
    fun getPorcentajeVisto(): Int {
        return if (totalEntregados > 0) {
            ((totalVistos.toDouble() / totalEntregados) * 100).toInt()
        } else 0
    }

    /**
     * Calcula el porcentaje de clics (CTR)
     */
    fun getPorcentajeClics(): Int {
        return if (totalVistos > 0) {
            ((totalClics.toDouble() / totalVistos) * 100).toInt()
        } else 0
    }

    /**
     * Verifica si puede ser cancelada
     */
    fun puedeSerCancelada(): Boolean {
        val estadoEnum = getEstadoEnum()
        return estadoEnum == EstadoNotificacion.PENDIENTE ||
                estadoEnum == EstadoNotificacion.PROGRAMADA
    }

    /**
     * Verifica si puede ser reenviada
     */
    fun puedeSerRenviada(): Boolean {
        val estadoEnum = getEstadoEnum()
        return estadoEnum == EstadoNotificacion.FALLIDA ||
                estadoEnum == EstadoNotificacion.CANCELADA
    }
}

/**
 * Tipo de notificación según su propósito
 */
enum class TipoNotificacion(val icono: String, val color: String) {
    INFO("🔔", "#2196F3"),        // Información general - Azul
    ALERTA("⚠️", "#FF9800"),      // Urgente/Importante - Naranja
    PROMOCION("🎉", "#4CAF50"),   // Marketing/Descuentos - Verde
    SISTEMA("⚙️", "#9E9E9E"),     // Actualizaciones técnicas - Gris
    EVENTO("📅", "#9C27B0")       // Eventos especiales - Morado
}

/**
 * A quiénes se enviará la notificación
 */
enum class DestinatariosType(val descripcion: String) {
    TODOS("Todos los usuarios"),
    PREMIUM("Solo usuarios premium"),
    BASICO("Solo usuarios plan básico"),
    SEDE_ESPECIFICA("Usuarios de una sede específica"),
    CON_RESERVAS("Usuarios con reservas activas"),
    INACTIVOS("Usuarios inactivos (30+ días)"),
    NUEVOS("Usuarios registrados últimos 7 días"),
    ADMINISTRADORES("Solo administradores de sedes")
}

/**
 * Estado del proceso de envío
 */
enum class EstadoNotificacion(val color: String) {
    PENDIENTE("#FFC107"),    // Amarillo - Creada pero no enviada
    PROGRAMADA("#2196F3"),   // Azul - Agendada para envío futuro
    ENVIANDO("#FF9800"),     // Naranja - En proceso de envío
    ENVIADA("#4CAF50"),      // Verde - Completada exitosamente
    CANCELADA("#9E9E9E"),    // Gris - Cancelada por admin
    FALLIDA("#F44336")       // Rojo - Error en el envío
}

/**
 * Prioridad de la notificación para FCM
 */
enum class PrioridadNotificacion {
    BAJA,      // No urgente, puede esperar
    NORMAL,    // Prioridad estándar
    ALTA       // Urgente, entregar inmediatamente
}

/**
 * Modelo para estadísticas detalladas de una notificación
 */
data class EstadisticasNotificacion(
    val notificacionId: String = "",
    val totalDestinatarios: Int = 0,
    val totalEnviados: Int = 0,
    val totalEntregados: Int = 0,
    val totalVistos: Int = 0,
    val totalClics: Int = 0,
    val totalFallidos: Int = 0,
    val tiempoPromedioVista: Long = 0, // Milisegundos
    val dispositivosPorPlataforma: Map<String, Int> = emptyMap(), // Android, iOS
    val horasPicoVista: Map<Int, Int> = emptyMap(), // 0-23
    val tasaApertura: Double = 0.0,
    val tasaClics: Double = 0.0,
    val fechaUltimaActualizacion: Date = Date()
)

/**
 * Registro individual de envío por usuario
 */
data class NotificacionUsuario(
    val id: String = "",
    val notificacionId: String = "",
    val userId: String = "",
    val fcmToken: String = "",
    val enviado: Boolean = false,
    val entregado: Boolean = false,
    val visto: Boolean = false,
    val clic: Boolean = false,
    val fechaEnvio: Date? = null,
    val fechaEntrega: Date? = null,
    val fechaVista: Date? = null,
    val fechaClic: Date? = null,
    val error: String? = null,
    val plataforma: String = "" // "android", "ios"
)
