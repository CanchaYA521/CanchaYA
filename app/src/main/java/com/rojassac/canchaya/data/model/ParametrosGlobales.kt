package com.rojassac.canchaya.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * ✅ NUEVO (23 Oct 2025)
 * Modelo de datos para configuración global de la aplicación
 */
data class ParametrosGlobales(
    @DocumentId
    val id: String = "config_global", // ID fijo para singleton

    // ⏰ CONFIGURACIÓN DE RESERVAS
    val anticipacionMinima: Int = 2, // Horas mínimas de anticipación
    val anticipacionMaxima: Int = 30, // Días máximos de anticipación
    val duracionMinima: Int = 1, // Horas mínimas de reserva
    val duracionMaxima: Int = 4, // Horas máximas de reserva
    val tiempoGraciaCancelacion: Int = 24, // Horas antes para cancelar gratis

    // 💰 CONFIGURACIÓN DE PAGOS
    val porcentajeAnticipo: Int = 50, // % de anticipo requerido (0-100)
    val comisionPlataforma: Double = 10.0, // % de comisión
    val montoMinimo: Double = 20.0, // Monto mínimo de reserva en S/.
    val metodosPagoHabilitados: List<String> = listOf("YAPE", "PLIN", "EFECTIVO", "TARJETA"),

    // 🚫 POLÍTICAS DE CANCELACIÓN
    val politicaReembolso: String = "PARCIAL", // TOTAL, PARCIAL, NINGUNO
    val porcentajeReembolso: Int = 80, // % a devolver si aplica
    val penalizacionNoShow: Boolean = true, // Penalizar si no asiste
    val maxCancelacionesMes: Int = 3, // Cancelaciones permitidas por mes

    // 🏆 SISTEMA DE PUNTOS
    val puntosHabilitados: Boolean = true,
    val puntosPorCienSoles: Int = 10, // Puntos por cada S/. 100
    val puntosPorReferido: Int = 50,
    val conversionPuntos: Int = 100, // Puntos = S/. 5 (100 pts = S/. 5)
    val descuentoPorPuntos: Double = 5.0, // Valor en soles

    // 📱 CONFIGURACIÓN DE NOTIFICACIONES
    val recordatorioReserva: Int = 2, // Horas antes de recordar
    val confirmacionAutomatica: Boolean = false,
    val notificacionesPushHabilitadas: Boolean = true,

    // 🔒 LÍMITES Y RESTRICCIONES
    val maxReservasActivas: Int = 3, // Por usuario
    val diasBloqueo: Int = 7, // Días de bloqueo por mal comportamiento
    val maxCambiosHorario: Int = 1, // Cambios permitidos por reserva

    // 🎨 PERSONALIZACIÓN
    val modoMantenimiento: Boolean = false,
    val mensajeMantenimiento: String = "Estamos mejorando nuestro servicio. Volvemos pronto.",
    val versionMinimaRequerida: String = "1.0.0",
    val mensajeBienvenida: String = "¡Bienvenido a CanchaYA!",

    // 📊 METADATOS
    @ServerTimestamp
    val fechaCreacion: Date? = null,
    @ServerTimestamp
    val fechaActualizacion: Date? = null,
    val actualizadoPor: String = ""
) {
    // Función helper para validar si está en mantenimiento
    fun isAppDisponible(): Boolean = !modoMantenimiento

    // Función helper para validar método de pago
    fun isMetodoPagoValido(metodo: String): Boolean {
        return metodosPagoHabilitados.contains(metodo.uppercase())
    }

    // Función helper para calcular puntos por monto
    fun calcularPuntos(montoSoles: Double): Int {
        if (!puntosHabilitados) return 0
        return ((montoSoles / 100.0) * puntosPorCienSoles).toInt()
    }

    // Función helper para calcular descuento por puntos
    fun calcularDescuentoPorPuntos(puntos: Int): Double {
        if (!puntosHabilitados || puntos < conversionPuntos) return 0.0
        val multiplo = puntos / conversionPuntos
        return multiplo * descuentoPorPuntos
    }
}

/**
 * ✅ ENUM: Política de reembolso
 */
enum class PoliticaReembolso {
    TOTAL,
    PARCIAL,
    NINGUNO
}

/**
 * ✅ ENUM: Métodos de pago
 */
enum class MetodoPago {
    YAPE,
    PLIN,
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA
}
