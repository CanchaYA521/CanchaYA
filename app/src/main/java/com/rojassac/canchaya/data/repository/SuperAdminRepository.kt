package com.rojassac.canchaya.data.repository

import android.net.Uri
import com.rojassac.canchaya.data.model.ParametrosGlobales
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.data.model.Promocion // ✅ AGREGADO (23 Oct 2025)
import com.rojassac.canchaya.data.model.Sede
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.FirestoreConverter
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SuperAdminRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ========== GESTIÓN DE USUARIOS (✅ CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                try {
                    val fechaCreacion = try {
                        when (val fecha = doc.get("fechaCreacion")) {
                            is com.google.firebase.Timestamp -> fecha.toDate().time
                            is Long -> fecha
                            else -> System.currentTimeMillis()
                        }
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

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
                        activo = doc.getBoolean("activo") ?: true,
                        canchaId = doc.getString("canchaId"),
                        canchasAsignadas = (doc.get("canchasAsignadas") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        sedeId = doc.getString("sedeId"),
                        tipoAdministracion = doc.getString("tipoAdministracion"),
                        fechaCreacion = fechaCreacion,
                        stability = (doc.getLong("stability") ?: 0).toInt()
                    )
                } catch (e: Exception) {
                    Log.e("SuperAdminRepo", "Error parseando usuario ${doc.id}: ${e.message}")
                    null
                }
            }

            Log.d("SuperAdminRepo", "✅ Usuarios cargados: ${users.size}")
            Result.success(users)
        } catch (e: Exception) {
            Log.e("SuperAdminRepo", "❌ Error al cargar usuarios: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("rol", newRole.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleUserStatus(userId: String, isActive: Boolean): Result<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("activo", isActive)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignAdminToCancha(adminId: String, canchaId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(adminId)
                .update(
                    mapOf(
                        "rol" to UserRole.ADMIN.name,
                        "canchaId" to canchaId
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserCountByRole(): Result<Map<String, Int>> {
        return try {
            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .get()
                .await()

            val totalUsers = snapshot.size()
            val admins = snapshot.documents.count { doc ->
                doc.getString("rol") == UserRole.ADMIN.name
            }

            val activeUsers = snapshot.documents.count { doc ->
                doc.getBoolean("activo") == true
            }

            Result.success(
                mapOf(
                    "total" to totalUsers,
                    "admins" to admins,
                    "activos" to activeUsers
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== GESTIÓN DE CANCHAS (✅ CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    suspend fun getAllCanchas(): Result<List<Cancha>> {
        return try {
            val snapshot = firestore.collection(Constants.CANCHAS_COLLECTION)
                .get()
                .await()

            val canchas = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Cancha::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(canchas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCanchaApproval(canchaId: String, isActive: Boolean): Result<Unit> {
        return try {
            firestore.collection(Constants.CANCHAS_COLLECTION)
                .document(canchaId)
                .update("activo", isActive)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCancha(canchaId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.CANCHAS_COLLECTION)
                .document(canchaId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCanchasStats(): Result<Map<String, Int>> {
        return try {
            val snapshot = firestore.collection(Constants.CANCHAS_COLLECTION)
                .get()
                .await()

            val totalCanchas = snapshot.size()
            val activas = snapshot.documents.count { doc ->
                doc.getBoolean("activo") == true
            }

            Result.success(
                mapOf(
                    "total" to totalCanchas,
                    "activas" to activas,
                    "inactivas" to (totalCanchas - activas)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== GESTIÓN DE SEDES (✅ CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    private suspend fun generarCodigoInvitacion(tipo: String): String {
        return try {
            val contadorRef = firestore.collection("configuracion")
                .document("contadores")
            val contadorDoc = contadorRef.get().await()
            val contadorActual = contadorDoc.getLong("ultimaSedeId") ?: 0L
            val nuevoContador = contadorActual + 1

            firestore.runTransaction { transaction ->
                transaction.update(contadorRef, "ultimaSedeId", nuevoContador)
            }.await()

            val codigo = String.format("SE%08d", nuevoContador)
            Log.d("SuperAdminRepo", "✅ Código generado: $codigo")
            codigo
        } catch (e: Exception) {
            Log.e("SuperAdminRepo", "❌ Error al generar código: ${e.message}", e)
            val timestamp = System.currentTimeMillis() % 100000000
            String.format("SE%08d", timestamp)
        }
    }

    suspend fun getAllSedes(): Result<List<Sede>> {
        return try {
            val snapshot = firestore.collection(Constants.SEDES_COLLECTION)
                .get()
                .await()

            val sedes = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Sede::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(sedes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearSede(sede: Sede): Result<String> {
        return try {
            val codigoInvitacion = generarCodigoInvitacion("sede")

            val sedeData = hashMapOf(
                "nombre" to sede.nombre,
                "direccion" to sede.direccion,
                "descripcion" to sede.descripcion,
                "latitud" to sede.latitud,
                "longitud" to sede.longitud,
                "telefono" to sede.telefono,
                "email" to sede.email,
                "horaApertura" to sede.horaApertura,
                "horaCierre" to sede.horaCierre,
                "imageUrl" to sede.imageUrl,
                "activa" to sede.activa,
                "canchaIds" to sede.canchaIds,
                "adminId" to "",
                "codigoInvitacion" to codigoInvitacion,
                "codigoActivo" to true,
                "fechaCreacion" to com.google.firebase.Timestamp.now(),
                "fechaModificacion" to com.google.firebase.Timestamp.now(),
                "tieneDucha" to sede.tieneDucha,
                "tieneGaraje" to sede.tieneGaraje,
                "tieneLuzNocturna" to sede.tieneLuzNocturna,
                "tieneEstacionamiento" to sede.tieneEstacionamiento,
                "tieneBaños" to sede.tieneBaños,
                "tieneWifi" to sede.tieneWifi,
                "tieneCafeteria" to sede.tieneCafeteria,
                "tieneVestidores" to sede.tieneVestidores
            )

            val docRef = firestore.collection(Constants.SEDES_COLLECTION)
                .add(sedeData)
                .await()
            val sedeId = docRef.id
            Log.d("SuperAdminRepo", "✅ Sede creada: $sedeId con código: $codigoInvitacion")
            Result.success(sedeId)
        } catch (e: Exception) {
            Log.e("SuperAdminRepo", "❌ Error al crear sede: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun actualizarSede(sede: Sede): Result<Unit> {
        return try {
            val sedeData = hashMapOf(
                "nombre" to sede.nombre,
                "direccion" to sede.direccion,
                "descripcion" to sede.descripcion,
                "latitud" to sede.latitud,
                "longitud" to sede.longitud,
                "telefono" to sede.telefono,
                "email" to sede.email,
                "horaApertura" to sede.horaApertura,
                "horaCierre" to sede.horaCierre,
                "imageUrl" to sede.imageUrl,
                "activa" to sede.activa,
                "canchaIds" to sede.canchaIds,
                "adminId" to sede.adminId,
                "fechaModificacion" to com.google.firebase.Timestamp.now(),
                "tieneDucha" to sede.tieneDucha,
                "tieneGaraje" to sede.tieneGaraje,
                "tieneLuzNocturna" to sede.tieneLuzNocturna,
                "tieneEstacionamiento" to sede.tieneEstacionamiento,
                "tieneBaños" to sede.tieneBaños,
                "tieneWifi" to sede.tieneWifi,
                "tieneCafeteria" to sede.tieneCafeteria,
                "tieneVestidores" to sede.tieneVestidores
            )

            firestore.collection(Constants.SEDES_COLLECTION)
                .document(sede.id)
                .update(sedeData as Map<String, Any>)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSede(sedeId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.SEDES_COLLECTION)
                .document(sedeId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleSedeStatus(sedeId: String, isActive: Boolean): Result<Unit> {
        return try {
            firestore.collection(Constants.SEDES_COLLECTION)
                .document(sedeId)
                .update("activa", isActive)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSedesStats(): Result<Map<String, Int>> {
        return try {
            val snapshot = firestore.collection(Constants.SEDES_COLLECTION)
                .get()
                .await()

            val totalSedes = snapshot.size()
            val activas = snapshot.documents.count { doc ->
                doc.getBoolean("activa") == true
            }

            Result.success(
                mapOf(
                    "total" to totalSedes,
                    "activas" to activas,
                    "inactivas" to (totalSedes - activas)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reactivarCodigoSede(sedeId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.SEDES_COLLECTION)
                .document(sedeId)
                .update(
                    mapOf(
                        "codigoActivo" to true,
                        "adminId" to "",
                        "fechaModificacion" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            Log.d("SuperAdminRepo", "✅ Código reactivado para sede: $sedeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SuperAdminRepo", "❌ Error al reactivar código: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ✅ ========== GESTIÓN DE PLANES (NUEVO - 23 Oct 2025) ==========

    suspend fun getAllPlanes(): Result<List<Plan>> {
        return try {
            Log.d(TAG, "Obteniendo todos los planes...")

            val snapshot = firestore.collection(Constants.PLANS_COLLECTION)
                .get()
                .await()

            val planes = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Plan::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir plan: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Planes obtenidos: ${planes.size}")
            Result.success(planes)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener planes", e)
            Result.failure(e)
        }
    }

    suspend fun updatePlan(plan: Plan): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando plan: ${plan.id}")

            val planData = hashMapOf<String, Any>(
                "nombre" to plan.nombre,
                "precio" to plan.precio,
                "comision" to plan.comision,
                "maxCanchas" to plan.maxCanchas,
                "descripcion" to plan.descripcion,
                "activo" to plan.activo,
                "destacado" to plan.destacado,
                "caracteristicas" to plan.caracteristicas,
                "color" to plan.color,
                "plazoRetiro" to plan.plazoRetiro,
                "posicionPrioritaria" to plan.posicionPrioritaria,
                "marketingIncluido" to plan.marketingIncluido,
                "whiteLabel" to plan.whiteLabel,
                "soporte" to plan.soporte
            )

            firestore.collection(Constants.PLANS_COLLECTION)
                .document(plan.id)
                .update(planData)
                .await()

            Log.d(TAG, "Plan actualizado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar plan", e)
            Result.failure(e)
        }
    }

    suspend fun getSuscriptoresPorPlan(planId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection(Constants.SUBSCRIPTIONS_COLLECTION)
                .whereEqualTo("planId", planId)
                .whereEqualTo("estado", "ACTIVA")
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Log.e(TAG, "Error al contar suscriptores", e)
            Result.failure(e)
        }
    }

    suspend fun togglePlanStatus(planId: String, activo: Boolean): Result<Unit> {
        return try {
            Log.d(TAG, "Cambiando estado del plan $planId a $activo")

            firestore.collection(Constants.PLANS_COLLECTION)
                .document(planId)
                .update("activo", activo)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cambiar estado del plan", e)
            Result.failure(e)
        }
    }

    // ✅ ========== GESTIÓN DE PROMOCIONES (NUEVO - 23 Oct 2025) ==========

    suspend fun getAllPromociones(): Result<List<Promocion>> {
        return try {
            Log.d(TAG, "Obteniendo todas las promociones...")

            val snapshot = firestore.collection(Constants.PROMOCIONES_COLLECTION)
                .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val promociones = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Promocion::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al convertir promoción: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Promociones obtenidas: ${promociones.size}")
            Result.success(promociones)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener promociones", e)
            Result.failure(e)
        }
    }

    suspend fun crearPromocion(promocion: Promocion): Result<String> {
        return try {
            Log.d(TAG, "Creando promoción: ${promocion.codigo}")

            // Validar que el código no exista
            val existente = firestore.collection(Constants.PROMOCIONES_COLLECTION)
                .whereEqualTo("codigo", promocion.codigo)
                .get()
                .await()

            if (!existente.isEmpty) {
                return Result.failure(Exception("El código '${promocion.codigo}' ya existe"))
            }

            val promocionData = hashMapOf<String, Any>(
                "codigo" to promocion.codigo,
                "nombre" to promocion.nombre,
                "descripcion" to promocion.descripcion,
                "tipoDescuento" to promocion.tipoDescuento.name,
                "valorDescuento" to promocion.valorDescuento,
                "aplicaATodos" to promocion.aplicaATodos,
                "planesAplicables" to promocion.planesAplicables,
                "usosMaximos" to promocion.usosMaximos,
                "usosMaximosPorUsuario" to promocion.usosMaximosPorUsuario,
                "usosActuales" to 0,
                "fechaInicio" to promocion.fechaInicio,
                "fechaFin" to promocion.fechaFin,
                "activo" to promocion.activo,
                "creadoPor" to promocion.creadoPor,
                "fechaCreacion" to System.currentTimeMillis(),
                "fechaModificacion" to System.currentTimeMillis()
            )

            val docRef = firestore.collection(Constants.PROMOCIONES_COLLECTION)
                .add(promocionData)
                .await()

            Log.d(TAG, "Promoción creada exitosamente: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear promoción", e)
            Result.failure(e)
        }
    }

    suspend fun actualizarPromocion(promocion: Promocion): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando promoción: ${promocion.id}")

            val promocionData = hashMapOf<String, Any>(
                "nombre" to promocion.nombre,
                "descripcion" to promocion.descripcion,
                "tipoDescuento" to promocion.tipoDescuento.name,
                "valorDescuento" to promocion.valorDescuento,
                "aplicaATodos" to promocion.aplicaATodos,
                "planesAplicables" to promocion.planesAplicables,
                "usosMaximos" to promocion.usosMaximos,
                "usosMaximosPorUsuario" to promocion.usosMaximosPorUsuario,
                "fechaInicio" to promocion.fechaInicio,
                "fechaFin" to promocion.fechaFin,
                "activo" to promocion.activo,
                "fechaModificacion" to System.currentTimeMillis()
            )

            firestore.collection(Constants.PROMOCIONES_COLLECTION)
                .document(promocion.id)
                .update(promocionData)
                .await()

            Log.d(TAG, "Promoción actualizada exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar promoción", e)
            Result.failure(e)
        }
    }

    suspend fun eliminarPromocion(promocionId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Eliminando promoción: $promocionId")

            firestore.collection(Constants.PROMOCIONES_COLLECTION)
                .document(promocionId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar promoción", e)
            Result.failure(e)
        }
    }

    suspend fun togglePromocionStatus(promocionId: String, activo: Boolean): Result<Unit> {
        return try {
            Log.d(TAG, "Cambiando estado de promoción $promocionId a $activo")

            firestore.collection(Constants.PROMOCIONES_COLLECTION)
                .document(promocionId)
                .update(
                    mapOf(
                        "activo" to activo,
                        "fechaModificacion" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cambiar estado de promoción", e)
            Result.failure(e)
        }
    }

    suspend fun getEstadisticasPromocion(promocionId: String): Result<Map<String, Any>> {
        return try {
            val usosSnapshot = firestore.collection("usos_promociones")
                .whereEqualTo("promocionId", promocionId)
                .get()
                .await()

            val totalUsos = usosSnapshot.size()
            val usuariosUnicos = usosSnapshot.documents
                .map { it.getString("userId") }
                .distinct()
                .size

            val montoTotalDescontado = usosSnapshot.documents.sumOf {
                it.getDouble("montoDescuento") ?: 0.0
            }

            Result.success(
                mapOf(
                    "totalUsos" to totalUsos,
                    "usuariosUnicos" to usuariosUnicos,
                    "montoTotalDescontado" to montoTotalDescontado
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener estadísticas de promoción", e)
            Result.failure(e)
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ⚙️ PARÁMETROS GLOBALES (NUEVO - 23 Oct 2025)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Obtener configuración global (Singleton)
     * Si no existe, devuelve valores por defecto
     */
    suspend fun getParametrosGlobales(): Result<ParametrosGlobales> {
        return try {
            val document = firestore
                .collection(Constants.COLLECTION_PARAMETROS)
                .document(Constants.DOC_CONFIG_GLOBAL)
                .get()
                .await()

            if (document.exists()) {
                val parametros = document.toObject(ParametrosGlobales::class.java)
                if (parametros != null) {
                    Result.success(parametros)
                } else {
                    // Si hay error en la deserialización, devolver defaults
                    Result.success(ParametrosGlobales())
                }
            } else {
                // Si no existe el documento, crear uno con valores por defecto
                val defaultParams = ParametrosGlobales()
                firestore
                    .collection(Constants.COLLECTION_PARAMETROS)
                    .document(Constants.DOC_CONFIG_GLOBAL)
                    .set(defaultParams)
                    .await()
                Result.success(defaultParams)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar parámetros globales
     * Solo actualiza los campos que no sean null
     */
    suspend fun actualizarParametrosGlobales(
        parametros: ParametrosGlobales,
        userId: String
    ): Result<Unit> {
        return try {
            val parametrosActualizados = parametros.copy(
                actualizadoPor = userId,
                fechaActualizacion = Date()
            )

            firestore
                .collection(Constants.COLLECTION_PARAMETROS)
                .document(Constants.DOC_CONFIG_GLOBAL)
                .set(parametrosActualizados)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar un campo específico de los parámetros
     */
    suspend fun actualizarCampoParametros(
        campo: String,
        valor: Any,
        userId: String
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                campo to valor,
                "actualizadoPor" to userId,
                "fechaActualizacion" to Date()
            )

            firestore
                .collection(Constants.COLLECTION_PARAMETROS)
                .document(Constants.DOC_CONFIG_GLOBAL)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resetear parámetros a valores por defecto
     */
    suspend fun resetearParametros(userId: String): Result<Unit> {
        return try {
            val defaultParams = ParametrosGlobales(
                actualizadoPor = userId,
                fechaActualizacion = Date()
            )

            firestore
                .collection(Constants.COLLECTION_PARAMETROS)
                .document(Constants.DOC_CONFIG_GLOBAL)
                .set(defaultParams)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener configuración en tiempo real con Flow
     */
    fun getParametrosGlobalesFlow(): Flow<ParametrosGlobales> = callbackFlow {
        val listener = firestore
            .collection(Constants.COLLECTION_PARAMETROS)
            .document(Constants.DOC_CONFIG_GLOBAL)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // En caso de error, enviar valores por defecto
                    trySend(ParametrosGlobales())
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val parametros = snapshot.toObject(ParametrosGlobales::class.java)
                    trySend(parametros ?: ParametrosGlobales())
                } else {
                    trySend(ParametrosGlobales())
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Validar si la app está en mantenimiento
     */
    suspend fun isAppEnMantenimiento(): Result<Boolean> {
        return try {
            val parametros = getParametrosGlobales().getOrNull()
            Result.success(parametros?.modoMantenimiento ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Activar/Desactivar modo mantenimiento
     */
    suspend fun toggleModoMantenimiento(
        activar: Boolean,
        userId: String
    ): Result<Unit> {
        return actualizarCampoParametros("modoMantenimiento", activar, userId)
    }

    /**
     * Obtener versión mínima requerida
     */
    suspend fun getVersionMinimaRequerida(): Result<String> {
        return try {
            val parametros = getParametrosGlobales().getOrNull()
            Result.success(parametros?.versionMinimaRequerida ?: "1.0.0")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "SuperAdminRepository"
    }

}
