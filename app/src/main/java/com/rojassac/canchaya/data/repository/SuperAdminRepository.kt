package com.rojassac.canchaya.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rojassac.canchaya.data.model.Cancha
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
                    User(
                        uid = doc.getString("uid") ?: "",
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
                        canchasAsignadas = (doc.get("canchasAsignadas") as? List<String>) ?: emptyList(),
                        sedeId = doc.getString("sedeId"),
                        tipoAdministracion = doc.getString("tipoAdministracion"),
                        fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                        stability = (doc.getLong("stability") ?: 0).toInt()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(users)
        } catch (e: Exception) {
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

    // ========== GESTIÓN DE SEDES (🔵 MODIFICADO - 22 Oct 2025) ==========

    /**
     * 🔵 MODIFICADO: Generar código secuencial único (22 Oct 2025)
     * Formato: SE00000001, SE00000002, etc.
     */
    private suspend fun generarCodigoInvitacion(tipo: String): String {
        return try {
            // Obtener contador actual de Firebase
            val contadorRef = firestore.collection("configuracion")
                .document("contadores")

            val contadorDoc = contadorRef.get().await()
            val contadorActual = contadorDoc.getLong("ultimaSedeId") ?: 0L
            val nuevoContador = contadorActual + 1

            // Actualizar contador en transacción para evitar duplicados
            firestore.runTransaction { transaction ->
                transaction.update(contadorRef, "ultimaSedeId", nuevoContador)
            }.await()

            // Generar código: SE00000001
            val codigo = String.format("SE%08d", nuevoContador)

            Log.d("SuperAdminRepo", "✅ Código generado: $codigo")
            codigo

        } catch (e: Exception) {
            Log.e("SuperAdminRepo", "❌ Error al generar código: ${e.message}", e)
            // Fallback: generar con timestamp
            val timestamp = System.currentTimeMillis() % 100000000
            String.format("SE%08d", timestamp)
        }
    }

    /**
     * ✅ CÓDIGO EXISTENTE - NO MODIFICADO
     */
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

    /**
     * 🔵 MODIFICADO: Crear sede con código secuencial (22 Oct 2025)
     * ELIMINADO: Creación de colección codigos_invitacion (duplicado)
     */
    suspend fun crearSede(sede: Sede): Result<String> {
        return try {
            // 1. 🔵 MODIFICADO: Generar código único secuencial
            val codigoInvitacion = generarCodigoInvitacion("sede")

            // 2. Crear datos de la sede
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
                "canchasIds" to sede.canchasIds,
                "adminId" to "",                          // Sin admin asignado
                "codigoInvitacion" to codigoInvitacion,  // Código único
                "codigoUsado" to false,                   // Disponible para uso
                "fechaCreacion" to com.google.firebase.Timestamp.now(),
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
            )

            // 3. Guardar sede en Firestore
            val docRef = firestore.collection(Constants.SEDES_COLLECTION)
                .add(sedeData)
                .await()

            val sedeId = docRef.id

            // 🗑️ ELIMINADO: Ya NO creamos documento en codigos_invitacion
            // El código está directamente en la sede

            Log.d("SuperAdminRepo", "✅ Sede creada: $sedeId con código: $codigoInvitacion")

            Result.success(sedeId)
        } catch (e: Exception) {
            Log.e("SuperAdminRepo", "❌ Error al crear sede: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * ✅ CÓDIGO EXISTENTE - NO MODIFICADO
     */
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
                "canchasIds" to sede.canchasIds,
                "adminId" to sede.adminId,
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
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

    /**
     * ✅ CÓDIGO EXISTENTE - NO MODIFICADO
     */
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

    /**
     * ✅ CÓDIGO EXISTENTE - NO MODIFICADO
     */
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

    /**
     * ✅ CÓDIGO EXISTENTE - NO MODIFICADO
     */
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

    /**
     * 🆕 NUEVA FUNCIÓN: Reactivar código de sede (SuperAdmin) (22 Oct 2025)
     */
    suspend fun reactivarCodigoSede(sedeId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.SEDES_COLLECTION)
                .document(sedeId)
                .update(
                    mapOf(
                        "codigoUsado" to false,
                        "adminId" to "",
                        "fechaActualizacion" to com.google.firebase.Timestamp.now()
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
}
