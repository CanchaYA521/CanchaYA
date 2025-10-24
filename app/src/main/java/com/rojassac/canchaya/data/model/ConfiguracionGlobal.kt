package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ✅ NUEVO ARCHIVO (23 Oct 2025)
 * Parámetros globales de configuración de la aplicación
 * Solo debe existir UN documento en Firebase con id = "global"
 */
@Parcelize
data class ConfiguracionGlobal(
    val id: String = "global", // ID fijo

    // ========== CONFIGURACIÓN DE RESERVAS ==========
    val horasAnticipadaMinima: Int = 2, // Horas mínimas de anticipación para reservar
    val diasAnticipadaMaxima: Int = 30, // Días máximos de anticipación para reservar
    val duracionMinimaMinutos: Int = 60, // Duración mínima de una reserva (minutos)
    val duracionMaximaMinutos: Int = 240, // Duración máxima de una reserva (minutos)

    // ========== CONFIGURACIÓN DE CANCELACIONES ==========
    val horasMaximaCancelacion: Int = 24, // Horas antes para cancelar sin penalización
    val penalizacionCancelacionTardia: Double = 0.0, // Porcentaje de penalización (0.50 = 50%)
    val permitirCancelacion: Boolean = true, // Si se permite cancelar reservas

    // ========== CONFIGURACIÓN DE PAGOS ==========
    val comisionPlataforma: Double = 0.05, // Comisión adicional de la plataforma (5%)
    val metodoPagoDefecto: String = "efectivo", // Método de pago por defecto
    val requiereVerificacionPago: Boolean = true, // Si requiere verificar pagos

    // ========== NOTIFICACIONES ==========
    val notificarReservaCreada: Boolean = true,
    val notificarReservaCancelada: Boolean = true,
    val notificarRecordatorio24h: Boolean = true,
    val notificarRecordatorio1h: Boolean = false,

    // ========== MANTENIMIENTO ==========
    val appEnMantenimiento: Boolean = false,
    val mensajeMantenimiento: String = "La aplicación está en mantenimiento. Vuelve pronto.",
    val versionMinimaRequerida: String = "1.0.0",

    // ========== INFORMACIÓN DE CONTACTO ==========
    val emailSoporte: String = "canchaya0988@gmail.com",
    val telefonoSoporte: String = "+51 919 020 325",
    val whatsappSoporte: String = "+51919020325",

    // ========== REDES SOCIALES ==========
    val urlFacebook: String = "",
    val urlInstagram: String = "",
    val urlTwitter: String = "",
    val urlTikTok: String = "",

    // ========== METADATA ==========
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val actualizadoPor: String = "" // UID del superadmin
) : Parcelable
