package com.rojassac.canchaya.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole

/**
 * Conversor centralizado para manejar la conversión de DocumentSnapshot a objetos del modelo
 * Maneja automáticamente incompatibilidades de tipos como Timestamp → Long
 */
object FirestoreConverter {

    /**
     * ✅ NUEVO: Convierte un DocumentSnapshot de Firestore a un objeto User
     * Maneja la conversión de Timestamp a Long automáticamente
     */
    fun documentToUser(doc: DocumentSnapshot): User? {
        return try {
            // Intentar conversión directa primero
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            // Si falla, conversión manual robusta
            try {
                User(
                    uid = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    celular = doc.getString("celular") ?: "",
                    email = doc.getString("email") ?: "",
                    rol = try {
                        UserRole.valueOf(doc.getString("rol") ?: "USUARIO")
                    } catch (e: Exception) {
                        UserRole.USUARIO
                    },
                    // Gestión de canchas
                    canchaId = doc.getString("canchaId"),
                    canchasAsignadas = (doc.get("canchasAsignadas") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList(),
                    // Gestión de sedes
                    sedeId = doc.getString("sedeId"),
                    tipoAdministracion = doc.getString("tipoAdministracion"),
                    // ✅ CONVERSIÓN ROBUSTA DE TIMESTAMP → LONG
                    fechaCreacion = convertToLong(doc.get("fechaCreacion")),
                    activo = doc.getBoolean("activo") ?: true,
                    stability = doc.getLong("stability")?.toInt() ?: 0
                )
            } catch (innerException: Exception) {
                null
            }
        }
    }

    /**
     * Convierte múltiples DocumentSnapshots a una lista de Users
     */
    fun documentsToUsers(docs: List<DocumentSnapshot>): List<User> {
        return docs.mapNotNull { documentToUser(it) }
    }

    /**
     * Convierte un DocumentSnapshot de Firestore a un objeto Cancha
     * Maneja la conversión de Timestamp a Long automáticamente
     */
    fun documentToCancha(doc: DocumentSnapshot): Cancha? {
        return try {
            // Intentar conversión directa primero
            val cancha = doc.toObject(Cancha::class.java)
            cancha?.copy(id = doc.id)
        } catch (e: Exception) {
            // Si falla, conversión manual robusta
            try {
                Cancha(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    direccion = doc.getString("direccion") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    distrito = doc.getString("distrito") ?: "",
                    ciudad = doc.getString("ciudad") ?: "",

                    // Coordenadas
                    latitud = doc.getDouble("latitud") ?: 0.0,
                    longitud = doc.getDouble("longitud") ?: 0.0,
                    precioHora = doc.getDouble("precioHora") ?: 0.0,

                    // Imágenes
                    imagenUrl = doc.getString("imagenUrl") ?: "",
                    imagenes = (doc.get("imagenes") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList(),

                    // Servicios
                    servicios = (doc.get("servicios") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList(),

                    horarioApertura = doc.getString("horarioApertura") ?: "08:00",
                    horarioCierre = doc.getString("horarioCierre") ?: "23:00",

                    // IDs relacionados
                    sedeId = doc.getString("sedeId"),
                    adminId = doc.getString("adminId"),
                    adminAsignado = doc.getString("adminAsignado"),
                    creadoPor = doc.getString("creadoPor"),

                    // Códigos de invitación
                    codigoVinculacion = doc.getString("codigoVinculacion") ?: "",
                    codigoInvitacion = doc.getString("codigoInvitacion") ?: "",
                    codigoExpiracion = doc.getLong("codigoExpiracion") ?: 0L,
                    codigoUsado = doc.getBoolean("codigoUsado") ?: false,
                    tipoNegocio = doc.getString("tipoNegocio") ?: "cancha_individual",

                    // Estado
                    activo = doc.getBoolean("activo") ?: true,
                    activa = doc.getBoolean("activa") ?: true,

                    // ✅ CONVERSIÓN ROBUSTA DE TIMESTAMP → LONG
                    fechaCreacion = convertToLong(doc.get("fechaCreacion")),

                    // Reseñas
                    totalResenas = doc.getLong("totalResenas")?.toInt() ?: 0,
                    calificacionPromedio = doc.getDouble("calificacionPromedio") ?: 0.0
                )
            } catch (innerException: Exception) {
                null
            }
        }
    }

    /**
     * Convierte múltiples DocumentSnapshots a una lista de Canchas
     */
    fun documentsToCanchas(docs: List<DocumentSnapshot>): List<Cancha> {
        return docs.mapNotNull { documentToCancha(it) }
    }

    /**
     * Convierte cualquier tipo de fecha de Firestore a Long (milisegundos)
     */
    private fun convertToLong(value: Any?): Long {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Long -> value
            is Number -> value.toLong()
            else -> System.currentTimeMillis()
        }
    }
}
