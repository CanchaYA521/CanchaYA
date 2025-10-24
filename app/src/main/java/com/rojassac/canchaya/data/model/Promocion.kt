package com.rojassac.canchaya.data.model

import com.google.firebase.Timestamp

/**
 * ✅ ACTUALIZADO (23 Oct 2025)
 * Data class para promociones y cupones de descuento
 */
data class Promocion(
    val id: String = "",
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",

    // Tipo de descuento
    val tipoDescuento: TipoDescuento = TipoDescuento.PORCENTAJE,
    val valorDescuento: Double = 0.0, // % o monto fijo según tipo

    // Aplicabilidad
    val aplicaATodos: Boolean = true, // Si aplica a todos los planes
    val planesAplicables: List<String> = emptyList(), // IDs de planes específicos

    // Límites de uso
    val usosMaximos: Int = -1, // -1 = ilimitado
    val usosMaximosPorUsuario: Int = 1,
    val usosActuales: Int = 0,

    // Vigencia
    val fechaInicio: Long = 0L,
    val fechaFin: Long = 0L,

    // Estado
    val activo: Boolean = true,

    // Metadata
    val creadoPor: String = "", // UID del SuperAdmin
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaModificacion: Long = System.currentTimeMillis()
)

enum class TipoDescuento {
    PORCENTAJE, // Descuento en %
    MONTO_FIJO  // Descuento en monto fijo
}

/**
 * ✅ NUEVO: Uso de promoción por usuario
 */
data class UsoPromocion(
    val id: String = "",
    val promocionId: String = "",
    val codigoUsado: String = "",
    val userId: String = "",
    val planId: String = "",
    val montoDescuento: Double = 0.0,
    val fechaUso: Long = System.currentTimeMillis()
)
