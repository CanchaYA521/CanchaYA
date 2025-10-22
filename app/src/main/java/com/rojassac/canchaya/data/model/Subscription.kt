package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subscription(
    val id: String = "",
    val userId: String = "", // UID del admin/dueño
    val planId: String = "", // ID del plan (BASICO, PRO, PREMIUM, ENTERPRISE)
    val estado: EstadoSuscripcion = EstadoSuscripcion.ACTIVA,
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaVencimiento: Long = 0L, // Para planes de pago, cuando vence
    val fechaProximoCobro: Long = 0L, // Cuando se cobra el próximo mes
    val metodoPago: String = "", // "tarjeta", "yape", "transferencia"
    val ultimoCobroId: String = "", // ID del último cargo realizado
    val autoRenovacion: Boolean = true, // Si se renueva automáticamente
    val canceladaFecha: Long? = null, // Si fue cancelada, cuándo
    val motivoCancelacion: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
) : Parcelable

enum class EstadoSuscripcion {
    ACTIVA,      // Suscripción funcionando
    VENCIDA,     // Se pasó la fecha de vencimiento
    CANCELADA,   // Usuario canceló
    SUSPENDIDA,  // Admin suspendió (por falta de pago)
    PRUEBA       // En período de prueba
}
