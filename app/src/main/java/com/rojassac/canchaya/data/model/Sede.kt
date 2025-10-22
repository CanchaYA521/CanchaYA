package com.rojassac.canchaya.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * 🆕 NUEVO MODELO: Sede (Conjunto de canchas en una ubicación)
 * Fecha: 21 de Octubre 2025
 * 🔧 CORREGIDO: 22 de Octubre 2025 - Sintaxis de funciones auxiliares
 */
@Parcelize
data class Sede(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val descripcion: String = "",

    // Coordenadas GPS
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,

    // Información de contacto
    val telefono: String = "",
    val email: String = "",

    // Horarios
    val horaApertura: String = "06:00",
    val horaCierre: String = "23:00",

    val imageUrl: String = "",

    // Estado de la sede
    val activa: Boolean = true,

    // Lista de IDs de canchas que pertenecen a esta sede
    val canchasIds: @RawValue List<String> = emptyList(),

    // Administrador responsable
    val adminId: String = "",

    // Timestamps
    val fechaCreacion: @RawValue Any? = null,
    val fechaActualizacion: @RawValue Any? = null,

    @Exclude
    @get:Exclude
    var fechaCreacionTimestamp: Timestamp? = null,

    @Exclude
    @get:Exclude
    var fechaActualizacionTimestamp: Timestamp? = null
) : Parcelable {

    /**
     * 🆕 NUEVA FUNCIÓN: Obtener cantidad de canchas (22 Oct 2025)
     * Usado por SedesAdapter para mostrar el contador de canchas
     */
    @Exclude
    fun getCantidadCanchas(): Int {
        return canchasIds.size
    }

    /**
     * 🆕 NUEVA FUNCIÓN: Validación de coordenadas GPS (22 Oct 2025)
     * Usado por SedesAdapter para verificar si se puede mostrar el mapa
     */
    @Exclude
    fun tieneCoordenadasValidas(): Boolean {
        return latitud != 0.0 && longitud != 0.0
    }

    /**
     * 🆕 NUEVA FUNCIÓN: Formato de horario de operación (22 Oct 2025)
     * Usado por SedesAdapter para mostrar el horario en formato legible
     */
    @Exclude
    fun getHorarioDisplay(): String {
        return "$horaApertura - $horaCierre"
    }

    /**
     * 🆕 NUEVA FUNCIÓN: Obtener estado como texto (22 Oct 2025)
     * Usado para mostrar "Activa" o "Inactiva"
     */
    @Exclude
    fun getEstadoTexto(): String {
        return if (activa) "Activa" else "Inactiva"
    }

    /**
     * 🆕 NUEVA FUNCIÓN: Validar si tiene admin asignado (22 Oct 2025)
     */
    @Exclude
    fun tieneAdminAsignado(): Boolean {
        return adminId.isNotEmpty()
    }
}
