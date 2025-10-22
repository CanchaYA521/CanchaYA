package com.rojassac.canchaya.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * 🆕 ACTUALIZADO: Sistema de código de invitación simplificado (22 Oct 2025)
 * - Código formato: SE00000001
 * - No se reutiliza automáticamente
 * - SuperAdmin puede reactivar manualmente
 */
@Parcelize
data class  Sede(
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

    // 🆕 SISTEMA DE CÓDIGO DE INVITACIÓN (22 Oct 2025)
    val codigoInvitacion: String = "",      // Formato: SE00000001
    val codigoUsado: Boolean = false,       // true = código fue usado
    val adminId: String = "",               // ID del admin que usó el código (vacío si se liberó)

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
     * Obtener cantidad de canchas
     */
    @Exclude
    fun getCantidadCanchas(): Int {
        return canchasIds.size
    }

    /**
     * Validación de coordenadas GPS
     */
    @Exclude
    fun tieneCoordenadasValidas(): Boolean {
        return latitud != 0.0 && longitud != 0.0
    }

    /**
     * Formato de horario de operación
     */
    @Exclude
    fun getHorarioDisplay(): String {
        return "$horaApertura - $horaCierre"
    }

    /**
     * Obtener estado como texto
     */
    @Exclude
    fun getEstadoTexto(): String {
        return if (activa) "Activa" else "Inactiva"
    }

    /**
     * Validar si tiene admin asignado
     */
    @Exclude
    fun tieneAdminAsignado(): Boolean {
        return adminId.isNotEmpty() && codigoUsado
    }

    /**
     * 🆕 NUEVO: Validar si código está disponible para uso (22 Oct 2025)
     */
    @Exclude
    fun codigoDisponible(): Boolean {
        return codigoInvitacion.isNotEmpty() && !codigoUsado
    }

    /**
     * 🆕 NUEVO: Validar si código está liberado (puede ser reactivado) (22 Oct 2025)
     */
    @Exclude
    fun codigoLiberado(): Boolean {
        return codigoUsado && adminId.isEmpty()
    }
}
