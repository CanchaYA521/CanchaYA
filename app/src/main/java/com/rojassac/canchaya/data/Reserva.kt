package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reserva(
    val id: String = "",
    val canchaId: String = "",
    val canchaNombre: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val usuarioCelular: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val precio: Double = 0.0,
    val estado: EstadoReserva = EstadoReserva.PENDIENTE,
    val metodoPago: String = "Yape",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val comprobantePago: String = ""
) : Parcelable

