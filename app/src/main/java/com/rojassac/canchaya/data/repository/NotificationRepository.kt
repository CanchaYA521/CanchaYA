package com.rojassac.canchaya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rojassac.canchaya.data.model.*
import com.rojassac.canchaya.utils.Constants.NOTIFICACIONES_COLLECTION
import com.rojassac.canchaya.utils.Constants.USERS_COLLECTION
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * ✅ NUEVO (24 Oct 2025)
 * Repository para gestionar notificaciones masivas con FCM
 * SIN HILT - Constructor normal
 */
class NotificacionRepository(
    private val firestore: FirebaseFirestore
) {

    // ═══════════════════════════════════════════════════════════════
    // 📝 CREAR Y ENVIAR NOTIFICACIONES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Crear una nueva notificación masiva
     */
    suspend fun crearNotificacion(notificacion: NotificacionMasiva): Resource<String> {
        return try {
            val docRef = firestore.collection(NOTIFICACIONES_COLLECTION).document()
            val notificacionConId = notificacion.copy(
                id = docRef.id,
                fechaCreacion = Date()
            )
            docRef.set(notificacionConId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al crear notificación")
        }
    }

    /**
     * Enviar notificación inmediatamente
     */
    suspend fun enviarNotificacionInmediata(notificacionId: String): Resource<Int> {
        return try {
            // 1. Actualizar estado a ENVIANDO
            actualizarEstadoNotificacion(notificacionId, EstadoNotificacion.ENVIANDO)

            // 2. Obtener la notificación
            val notifResult = obtenerNotificacionPorId(notificacionId)
            if (notifResult !is Resource.Success) {
                return Resource.Error("Notificación no encontrada")
            }
            val notificacion = notifResult.data!!

            // 3. Obtener destinatarios según filtro
            val destinatarios = obtenerDestinatarios(notificacion)

            // 4. Actualizar total de destinatarios
            firestore.collection(NOTIFICACIONES_COLLECTION)
                .document(notificacionId)
                .update(
                    mapOf(
                        "totalDestinatarios" to destinatarios.size,
                        "fechaEnvio" to Date(),
                        "estado" to EstadoNotificacion.ENVIADA.name
                    )
                ).await()

            Resource.Success(destinatarios.size)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al enviar notificación")
        }
    }

    /**
     * Cancelar una notificación programada
     */
    suspend fun cancelarNotificacion(notificacionId: String): Resource<Unit> {
        return try {
            firestore.collection(NOTIFICACIONES_COLLECTION)
                .document(notificacionId)
                .update("estado", EstadoNotificacion.CANCELADA.name)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al cancelar notificación")
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 📊 CONSULTAS Y LISTADOS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Obtener todas las notificaciones
     */
    suspend fun obtenerTodasNotificaciones(): Resource<List<NotificacionMasiva>> {
        return try {
            val snapshot = firestore.collection(NOTIFICACIONES_COLLECTION)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NotificacionMasiva::class.java)
            }
            Resource.Success(notificaciones)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al cargar notificaciones")
        }
    }

    /**
     * Obtener notificaciones por estado
     */
    suspend fun obtenerNotificacionesPorEstado(
        estado: EstadoNotificacion
    ): Resource<List<NotificacionMasiva>> {
        return try {
            val snapshot = firestore.collection(NOTIFICACIONES_COLLECTION)
                .whereEqualTo("estado", estado.name)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NotificacionMasiva::class.java)
            }
            Resource.Success(notificaciones)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al cargar notificaciones")
        }
    }

    /**
     * Obtener notificaciones programadas
     */
    suspend fun obtenerNotificacionesProgramadas(): Resource<List<NotificacionMasiva>> {
        return try {
            val ahora = Date()
            val snapshot = firestore.collection(NOTIFICACIONES_COLLECTION)
                .whereEqualTo("estado", EstadoNotificacion.PROGRAMADA.name)
                .whereGreaterThan("fechaProgramada", ahora)
                .orderBy("fechaProgramada", Query.Direction.ASCENDING)
                .get()
                .await()

            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NotificacionMasiva::class.java)
            }
            Resource.Success(notificaciones)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al cargar notificaciones programadas")
        }
    }

    /**
     * Obtener una notificación por ID
     */
    suspend fun obtenerNotificacionPorId(notificacionId: String): Resource<NotificacionMasiva> {
        return try {
            val snapshot = firestore.collection(NOTIFICACIONES_COLLECTION)
                .document(notificacionId)
                .get()
                .await()

            val notificacion = snapshot.toObject(NotificacionMasiva::class.java)
                ?: return Resource.Error("Notificación no encontrada")

            Resource.Success(notificacion)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener notificación")
        }
    }

    /**
     * Buscar notificaciones
     */
    suspend fun buscarNotificaciones(query: String): Resource<List<NotificacionMasiva>> {
        return try {
            val snapshot = firestore.collection(NOTIFICACIONES_COLLECTION)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NotificacionMasiva::class.java)
            }.filter { notif ->
                notif.titulo.contains(query, ignoreCase = true) ||
                        notif.mensaje.contains(query, ignoreCase = true)
            }

            Resource.Success(notificaciones)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error en búsqueda")
        }
    }

    /**
     * Duplicar notificación
     */
    suspend fun duplicarNotificacion(notificacionId: String): Resource<String> {
        return try {
            val notifResult = obtenerNotificacionPorId(notificacionId)
            if (notifResult !is Resource.Success) {
                return Resource.Error("Notificación no encontrada")
            }

            val notificacion = notifResult.data!!
            val nuevaNotificacion = notificacion.copy(
                id = "",
                titulo = "${notificacion.titulo} (Copia)",
                estado = EstadoNotificacion.PENDIENTE.name,
                fechaCreacion = Date(),
                fechaEnvio = null,
                fechaCompletado = null,
                totalDestinatarios = 0,
                totalEnviados = 0,
                totalEntregados = 0,
                totalVistos = 0,
                totalClics = 0,
                totalFallidos = 0
            )

            crearNotificacion(nuevaNotificacion)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al duplicar notificación")
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 📊 ESTADÍSTICAS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Obtener estadísticas generales
     */
    suspend fun obtenerEstadisticasGenerales(): Resource<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection(NOTIFICACIONES_COLLECTION)
                .get()
                .await()

            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NotificacionMasiva::class.java)
            }

            val estadisticas = mapOf(
                "totalNotificaciones" to notificaciones.size,
                "totalEnviadas" to notificaciones.count {
                    it.getEstadoEnum() == EstadoNotificacion.ENVIADA
                },
                "totalProgramadas" to notificaciones.count {
                    it.getEstadoEnum() == EstadoNotificacion.PROGRAMADA
                },
                "totalCanceladas" to notificaciones.count {
                    it.getEstadoEnum() == EstadoNotificacion.CANCELADA
                },
                "totalDestinatarios" to notificaciones.sumOf { it.totalDestinatarios },
                "totalEnvios" to notificaciones.sumOf { it.totalEnviados },
                "totalVistas" to notificaciones.sumOf { it.totalVistos },
                "totalClics" to notificaciones.sumOf { it.totalClics }
            )

            Resource.Success(estadisticas)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener estadísticas")
        }
    }

    /**
     * Obtener tasa de apertura promedio
     */
    suspend fun obtenerTasaAperturaPromedio(): Resource<Double> {
        return try {
            val snapshot = firestore.collection(NOTIFICACIONES_COLLECTION)
                .whereEqualTo("estado", EstadoNotificacion.ENVIADA.name)
                .get()
                .await()

            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NotificacionMasiva::class.java)
            }

            if (notificaciones.isEmpty()) {
                return Resource.Success(0.0)
            }

            val totalEntregados = notificaciones.sumOf { it.totalEntregados }
            val totalVistos = notificaciones.sumOf { it.totalVistos }

            val tasaApertura = if (totalEntregados > 0) {
                (totalVistos.toDouble() / totalEntregados) * 100
            } else 0.0

            Resource.Success(tasaApertura)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al calcular tasa de apertura")
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 🎯 FUNCIONES PRIVADAS - SEGMENTACIÓN
    // ═══════════════════════════════════════════════════════════════

    private suspend fun obtenerDestinatarios(notificacion: NotificacionMasiva): List<String> {
        return try {
            when (notificacion.getDestinatariosEnum()) {
                DestinatariosType.TODOS -> obtenerTodosLosUsuarios()
                DestinatariosType.PREMIUM -> obtenerUsuariosPorPlan("premium")
                DestinatariosType.BASICO -> obtenerUsuariosPorPlan("basico")
                DestinatariosType.SEDE_ESPECIFICA ->
                    obtenerUsuariosPorSede(notificacion.sedeId ?: "")
                DestinatariosType.CON_RESERVAS -> obtenerUsuariosConReservasActivas()
                DestinatariosType.INACTIVOS -> obtenerUsuariosInactivos(30)
                DestinatariosType.NUEVOS -> obtenerUsuariosNuevos(7)
                DestinatariosType.ADMINISTRADORES -> obtenerAdministradores()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerTodosLosUsuarios(): List<String> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).get().await()
            snapshot.documents.mapNotNull { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerUsuariosPorPlan(plan: String): List<String> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("planActual", plan)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerUsuariosPorSede(sedeId: String): List<String> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("sedeId", sedeId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerUsuariosConReservasActivas(): List<String> {
        return try {
            val ahora = Date()
            val snapshot = firestore.collection("reservas")
                .whereEqualTo("estado", "CONFIRMADA")
                .whereGreaterThan("fechaReserva", ahora)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.getString("userId") }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerUsuariosInactivos(dias: Int): List<String> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -dias)
            val fechaLimite = calendar.time

            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereLessThan("ultimaActividad", fechaLimite)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerUsuariosNuevos(dias: Int): List<String> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -dias)
            val fechaLimite = calendar.time

            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereGreaterThan("fechaRegistro", fechaLimite)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerAdministradores(): List<String> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("rol", "ADMIN")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun actualizarEstadoNotificacion(
        notificacionId: String,
        estado: EstadoNotificacion
    ): Resource<Unit> {
        return try {
            firestore.collection(NOTIFICACIONES_COLLECTION)
                .document(notificacionId)
                .update("estado", estado.name)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar estado")
        }
    }
}
