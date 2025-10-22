package com.rojassac.canchaya.data.model

enum class EstadoHorario {
    DISPONIBLE,
    OCUPADO,
    PASADO
}

data class HorarioSlot(
    val hora: String,
    val disponible: Boolean,
    val estado: EstadoHorario = EstadoHorario.DISPONIBLE
)
