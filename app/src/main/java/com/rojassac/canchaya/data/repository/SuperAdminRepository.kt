package com.rojassac.canchaya.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.FirestoreConverter
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SuperAdminRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ========== GESTIÓN DE USUARIOS ==========

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
                        rol = when (doc.getString("rol")?.uppercase()) {
                            "SUPERADMIN" -> UserRole.SUPERADMIN
                            "ADMIN" -> UserRole.ADMIN
                            "USUARIO" -> UserRole.USUARIO
                            else -> UserRole.USUARIO
                        },
                        canchaId = doc.getString("canchaId"),
                        fechaCreacion = doc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                        activo = doc.getBoolean("activo") ?: true,
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

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .delete()
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
                .update("canchaId", canchaId)
                .await()

            firestore.collection(Constants.CANCHAS_COLLECTION)
                .document(canchaId)
                .update("adminId", adminId)
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

            val stats = mutableMapOf<String, Int>()
            stats["total"] = snapshot.size()
            stats["superadmin"] = snapshot.documents.count {
                it.getString("rol")?.uppercase() == "SUPERADMIN"
            }
            stats["admin"] = snapshot.documents.count {
                it.getString("rol")?.uppercase() == "ADMIN"
            }
            stats["usuario"] = snapshot.documents.count {
                it.getString("rol")?.uppercase() == "USUARIO"
            }
            stats["activos"] = snapshot.documents.count {
                it.getBoolean("activo") == true
            }

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== GESTIÓN DE CANCHAS ==========

    // ✅ CORREGIDO: Usar FirestoreConverter en lugar de toObjects()
    suspend fun getAllCanchas(): Result<List<Cancha>> {
        return try {
            val snapshot = firestore.collection(Constants.CANCHAS_COLLECTION)
                .get()
                .await()

            // Usar el conversor centralizado
            val canchas = FirestoreConverter.documentsToCanchas(snapshot.documents)

            Result.success(canchas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCanchaApproval(canchaId: String, isApproved: Boolean): Result<Unit> {
        return try {
            firestore.collection(Constants.CANCHAS_COLLECTION)
                .document(canchaId)
                .update("activa", isApproved)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCancha(canchaId: String): Result<Unit> {
        return try {
            val canchaRef = firestore.collection(Constants.CANCHAS_COLLECTION)
                .document(canchaId)

            // Obtener la cancha para eliminar las imágenes
            val canchaDoc = canchaRef.get().await()
            val cancha = FirestoreConverter.documentToCancha(canchaDoc)

            // Eliminar imágenes del Storage si existen
            cancha?.imagenes?.forEach { imageUrl ->
                try {
                    val imageRef = storage.getReferenceFromUrl(imageUrl)
                    imageRef.delete().await()
                } catch (e: Exception) {
                    // Ignorar errores al eliminar imágenes
                }
            }

            // Eliminar documento de Firestore
            canchaRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadCanchaImage(canchaId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child("canchas/$canchaId/$fileName")

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCanchasStats(): Result<Map<String, Int>> {
        return try {
            val snapshot = firestore.collection(Constants.CANCHAS_COLLECTION)
                .get()
                .await()

            val stats = mutableMapOf<String, Int>()
            stats["total"] = snapshot.size()
            stats["activas"] = snapshot.documents.count {
                it.getBoolean("activa") == true || it.getBoolean("activo") == true
            }
            stats["inactivas"] = snapshot.documents.count {
                it.getBoolean("activa") == false || it.getBoolean("activo") == false
            }

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
