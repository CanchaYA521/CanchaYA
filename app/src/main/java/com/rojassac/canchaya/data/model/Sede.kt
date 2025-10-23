package com.rojassac.canchaya.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * ✅ CÓDIGO EXISTENTE MANTENIDO
 * ✨ ACTUALIZADO: Agregados campos de amenidades (22 Oct 2025)
 */
@Parcelize
data class Sede(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val descripcion: String = "",

    // ✅ CÓDIGO EXISTENTE: Coordenadas GPS
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,

    // ✅ CÓDIGO EXISTENTE: Información de contacto
    val telefono: String = "",
    val email: String = "",

    // ✅ CÓDIGO EXISTENTE: Horarios
    val horaApertura: String = "06:00",
    val horaCierre: String = "23:00",
    val imageUrl: String = "",

    // ✅ CÓDIGO EXISTENTE: Estado de la sede
    val activa: Boolean = true,

    // ✅ CÓDIGO EXISTENTE: Lista de IDs de canchas que pertenecen a esta sede
    val canchaIds: @RawValue List<String>? = null,

    // ✅ CÓDIGO EXISTENTE: Administrador asignado
    val adminId: String? = null,

    // ✅ CÓDIGO EXISTENTE: Código de invitación
    val codigoInvitacion: String = "",
    val codigoActivo: Boolean = true,

    // ✅ CÓDIGO EXISTENTE: Fechas
    val fechaCreacion: Timestamp? = null,
    val fechaModificacion: Timestamp? = null,

    // ✨ NUEVO: Amenidades de la sede (22 Oct 2025)
    val tieneDucha: Boolean = false,
    val tieneGaraje: Boolean = false,
    val tieneLuzNocturna: Boolean = false,
    val tieneEstacionamiento: Boolean = false,
    val tieneBaños: Boolean = false,
    val tieneWifi: Boolean = false,
    val tieneCafeteria: Boolean = false,
    val tieneVestidores: Boolean = false

) : Parcelable {

    // ✅ CÓDIGO EXISTENTE: Función auxiliar para mostrar horario
    @Exclude
    fun getHorarioDisplay(): String {
        return "$horaApertura - $horaCierre"
    }

    // ✅ CÓDIGO EXISTENTE: Función para validar si tiene admin asignado
    @Exclude
    fun tieneAdminAsignado(): Boolean {
        return !adminId.isNullOrEmpty()
    }

    // ✨ NUEVO: Función para obtener lista de amenidades activas
    @Exclude
    fun getAmenidadesActivas(): List<String> {
        val amenidades = mutableListOf<String>()
        if (tieneDucha) amenidades.add("Ducha")
        if (tieneGaraje) amenidades.add("Garaje")
        if (tieneLuzNocturna) amenidades.add("Luz Nocturna")
        if (tieneEstacionamiento) amenidades.add("Estacionamiento")
        if (tieneBaños) amenidades.add("Baños")
        if (tieneWifi) amenidades.add("WiFi")
        if (tieneCafeteria) amenidades.add("Cafetería")
        if (tieneVestidores) amenidades.add("Vestidores")
        return amenidades
    }
}
