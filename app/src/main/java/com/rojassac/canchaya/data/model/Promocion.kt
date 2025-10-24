package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ✅ NUEVO ARCHIVO (23 Oct 2025)
 * Modelo de datos para promociones y cupones de descuento
 */
@Parcelize
data class Promocion(
    val id: String = "",
    val codigo: String = "", // Código del cupón (ej: "VERANO2025")
    val tipo: TipoDescuento = TipoDescuento.PORCENTAJE, // Porcentaje o monto fijo
    val valor: Double = 0.0, // 20 para 20% o 50 para S/ 50
    val descripcion: String = "",
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaExpiracion: Long = 0L,
    val limiteUsos: Int = 0, // 0 = ilimitado
    val usosActuales: Int = 0,
    val activo: Boolean = true,
    val aplicableA: TipoAplicacion = TipoAplicacion.SUSCRIPCIONES, // A qué se aplica
    val planesAplicables: List<String> = listOf(), // Lista de IDs de planes (vacío = todos)
    val usuariosUsados: List<String> = listOf(), // IDs de usuarios que ya lo usaron
    val fechaCreacion: Long = System.currentTimeMillis(),
    val creadoPor: String = "" // UID del superadmin que lo creó
) : Parcelable

/**
 * ✅ NUEVO ENUM: Tipo de descuento
 */
enum class TipoDescuento {
    PORCENTAJE, // Descuento en porcentaje (ej: 20%)
    MONTO_FIJO  // Descuento en soles (ej: S/ 50)
}

/**
 * ✅ NUEVO ENUM: A qué se puede aplicar la promoción
 */
enum class TipoAplicacion {
    SUSCRIPCIONES,  // Descuento en suscripciones de planes
    RESERVAS,       // Descuento en reservas de canchas
    AMBOS           // Se puede aplicar a ambos
}
