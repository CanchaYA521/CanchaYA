package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * 🆕 NUEVO MODELO: Sistema de precios dinámicos por horarios
 * Fecha: 21 de Octubre 2025
 */
@Parcelize
data class PrecioHorario(
    val id: String = "",
    val horaInicio: String = "06:00", // Formato: "HH:mm"
    val horaFin: String = "12:00",     // Formato: "HH:mm"
    val precio: Double = 0.0,
    val diasAplicables: @RawValue List<String> = listOf(
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    ),
    val activo: Boolean = true
) : Parcelable {

    /**
     * Display del rango horario
     */
    fun getRangoHorarioDisplay(): String {
        return "$horaInicio - $horaFin"
    }

    /**
     * Display del precio
     */
    fun getPrecioDisplay(): String {
        return "S/ ${"%.2f".format(precio)}"
    }

    /**
     * Display de días aplicables
     */
    fun getDiasDisplay(): String {
        return when {
            diasAplicables.size == 7 -> "Todos los días"
            diasAplicables.containsAll(listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"))
                    && !diasAplicables.contains("Sábado") && !diasAplicables.contains("Domingo") -> "Lunes a Viernes"
            diasAplicables.containsAll(listOf("Sábado", "Domingo"))
                    && diasAplicables.size == 2 -> "Fines de semana"
            else -> diasAplicables.joinToString(", ")
        }
    }

    /**
     * Validar si el horario es válido
     */
    fun esHorarioValido(): Boolean {
        return horaInicio < horaFin && precio > 0
    }
}
