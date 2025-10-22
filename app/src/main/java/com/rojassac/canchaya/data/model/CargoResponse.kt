package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Respuesta del gateway de pagos cuando se crea un cargo
 */
@Parcelize
data class CargoResponse(
    val id: String = "", // ID del cargo (charge_id)
    val monto: Double = 0.0, // Monto cobrado
    val moneda: String = "PEN", // PEN = Soles
    val estado: String = "", // "exitosa", "pendiente", "fallida"
    val fechaCreacion: Long = System.currentTimeMillis(),
    val descripcion: String = "",
    val email: String = "",
    val ultimos4Digitos: String = "", // Últimos 4 dígitos de la tarjeta
    val metadata: Map<String, String> = mapOf() // Datos adicionales
) : Parcelable {

    /**
     * Verifica si el cargo fue exitoso
     */
    fun esExitoso(): Boolean {
        return estado.equals("exitosa", ignoreCase = true) ||
                estado.equals("success", ignoreCase = true)
    }

    /**
     * Verifica si el cargo está pendiente
     */
    fun esPendiente(): Boolean {
        return estado.equals("pendiente", ignoreCase = true) ||
                estado.equals("pending", ignoreCase = true)
    }

    /**
     * Verifica si el cargo falló
     */
    fun esFallido(): Boolean {
        return estado.equals("fallida", ignoreCase = true) ||
                estado.equals("failed", ignoreCase = true)
    }
}
