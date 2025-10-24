package com.rojassac.canchaya.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.Plan // ✅ AGREGADO IMPORT (23 Oct 2025)
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
                    // 🔧 NUEVA FORMA: Manejar fechaCreacion como Timestamp o Long
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
                        uid = doc.id, // ✅ Usar doc.id
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
                        fechaCreacion = fechaCreacion, // ✅ Usar la fecha parseada correctamente
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

    /**
     * ✅ NUEVA FUNCIÓN: Obtener todos los planes
     */
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

    /**
     * ✅ NUEVA FUNCIÓN: Actualizar un plan
     */
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

    /**
     * ✅ NUEVA FUNCIÓN: Obtener cantidad de suscriptores por plan
     */
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

    /**
     * ✅ NUEVA FUNCIÓN: Toggle estado de un plan
     */
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

    companion object {
        private const val TAG = "SuperAdminRepository"
    }
}
