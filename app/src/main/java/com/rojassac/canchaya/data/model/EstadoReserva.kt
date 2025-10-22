package com.rojassac.canchaya.data.model

import com.google.firebase.firestore.PropertyName

enum class EstadoReserva {
    @PropertyName("pendiente")
    PENDIENTE,

    @PropertyName("confirmada")
    CONFIRMADA,

    @PropertyName("cancelada")
    CANCELADA,

    @PropertyName("completada")
    COMPLETADA;  // <-- Agregué punto y coma

    // Función helper para convertir de String
    companion object {
        fun fromString(estado: String): EstadoReserva {
            return when (estado.lowercase()) {
                "pendiente" -> PENDIENTE
                "confirmada" -> CONFIRMADA
                "cancelada" -> CANCELADA
                "completada" -> COMPLETADA
                else -> PENDIENTE
            }
        }
    }
}
