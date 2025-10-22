package com.rojassac.canchaya.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Cancha(
    @DocumentId
    val id: String = "",

    val nombre: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val distrito: String = "",
    val ciudad: String = "",

    // ✅ COORDENADAS PARA EL MAPA
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,

    val precioHora: Double = 0.0,

    // ✅ IMÁGENES (soporte para ambos formatos)
    val imagenUrl: String = "", // Imagen principal (compatibilidad con código viejo)
    val imagenes: List<String> = listOf(), // Lista de múltiples imágenes

    // ✅ SERVICIOS
    val servicios: List<String> = listOf(), // "Estacionamiento", "Vestuarios", etc.

    val horarioApertura: String = "08:00",
    val horarioCierre: String = "23:00",

    // IDs relacionados
    val sedeId: String? = null,
    val adminId: String? = null, // ✅ Para compatibilidad
    val adminAsignado: String? = null,
    val creadoPor: String? = null,

    // Código de invitación
    val codigoVinculacion: String = "", // ✅ Para compatibilidad
    val codigoInvitacion: String = "",
    val codigoExpiracion: Long = 0L,
    val codigoUsado: Boolean = false,

    val tipoNegocio: String = "cancha_individual", // "sede" o "cancha_individual"

    // Estado
    val activo: Boolean = true,
    val activa: Boolean = true, // ✅ Para compatibilidad

    // ✅ FECHA: Usar @RawValue para que Parcelize lo acepte
    @PropertyName("fechaCreacion")
    val fechaCreacion: @RawValue Any? = null, // ✅ Puede ser Long o Timestamp

    // Reseñas
    val totalResenas: Int = 0,
    val calificacionPromedio: Double = 0.0

) : Parcelable {

    // ✅ Helper para obtener la primera imagen
    @get:Exclude
    val imagenPrincipal: String
        get() = imagenUrl.ifEmpty { imagenes.firstOrNull() ?: "" }

    // ✅ Helper para verificar si tiene coordenadas válidas
    @get:Exclude
    val tieneCoordenadas: Boolean
        get() = latitud != 0.0 && longitud != 0.0

    // ✅ Helper: placeholder para Glide
    @get:Exclude
    val placeholder: Int
        get() = android.R.drawable.ic_menu_gallery

    // ✅ Helper: obtener fecha de creación como Long
    @get:Exclude
    val fechaCreacionMillis: Long
        get() = when (fechaCreacion) {
            is Long -> fechaCreacion
            is Timestamp -> fechaCreacion.toDate().time
            else -> 0L
        }
}
