package com.rojassac.canchaya.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.FirestoreConverter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CanchaRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val canchasCollection = firestore.collection(Constants.CANCHAS_COLLECTION)

    // ========== OPERACIONES DE LECTURA ==========

    // Obtener todas las canchas activas (para usuarios)
    suspend fun obtenerCanchasActivas(): Result<List<Cancha>> {
        return try {
            val snapshot = canchasCollection
                .whereEqualTo("activa", true)  // ✅ CAMBIADO DE "activo" A "activa"
                .orderBy("nombre", Query.Direction.ASCENDING)
                .get()
                .await()

            // ✅ USA FirestoreConverter
            val canchas = FirestoreConverter.documentsToCanchas(snapshot.documents)
            Result.success(canchas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todas las canchas (para admin - incluye inactivas)
    suspend fun obtenerTodasLasCanchas(): Result<List<Cancha>> {
        return try {
            val snapshot = canchasCollection
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            // ✅ USA FirestoreConverter
            val canchas = FirestoreConverter.documentsToCanchas(snapshot.documents)
            Result.success(canchas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener canchas por Admin ID
    suspend fun obtenerCanchasPorAdmin(adminId: String): Result<List<Cancha>> {
        return try {
            val snapshot = canchasCollection
                .whereEqualTo("adminId", adminId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            // ✅ USA FirestoreConverter
            val canchas = FirestoreConverter.documentsToCanchas(snapshot.documents)
            Result.success(canchas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener cancha por ID
    suspend fun obtenerCanchaPorId(canchaId: String): Result<Cancha> {
        return try {
            val snapshot = canchasCollection.document(canchaId).get().await()

            // ✅ USA FirestoreConverter
            val cancha = FirestoreConverter.documentToCancha(snapshot)
                ?: throw Exception("Cancha no encontrada")

            Result.success(cancha)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Observar canchas en tiempo real
    fun observarCanchas(): Flow<List<Cancha>> = callbackFlow {
        val listener = canchasCollection
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // ✅ USA FirestoreConverter
                val canchas = FirestoreConverter.documentsToCanchas(snapshot?.documents ?: emptyList())
                trySend(canchas)
            }

        awaitClose { listener.remove() }
    }

    // ========== OPERACIONES DE ESCRITURA (SOLO ADMIN) ==========

    // Crear nueva cancha
    suspend fun crearCancha(cancha: Cancha): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: throw Exception("Usuario no autenticado")

            // Verificar que el usuario sea admin o superadmin
            if (!esAdministrador()) {
                throw Exception("No tienes permisos de administrador")
            }

            val nuevaCancha = cancha.copy(
                fechaCreacion = System.currentTimeMillis()
            )

            val docRef = canchasCollection.add(nuevaCancha).await()
            android.util.Log.d("CanchaRepo", "Cancha creada: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("CanchaRepo", "Error al crear cancha: ${e.message}")
            Result.failure(e)
        }
    }

    // Actualizar cancha existente
    suspend fun actualizarCancha(canchaId: String, cancha: Cancha): Result<Unit> {
        return try {
            if (!esAdministrador()) {
                throw Exception("No tienes permisos de administrador")
            }

            canchasCollection.document(canchaId)
                .set(cancha)
                .await()
            android.util.Log.d("CanchaRepo", "Cancha actualizada: $canchaId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CanchaRepo", "Error al actualizar: ${e.message}")
            Result.failure(e)
        }
    }

    // Eliminar cancha (soft delete - cambiar a inactivo)
    suspend fun eliminarCancha(canchaId: String): Result<Unit> {
        return try {
            if (!esAdministrador()) {
                throw Exception("No tienes permisos de administrador")
            }

            canchasCollection.document(canchaId)
                .update("activa", false)  // ✅ CAMBIADO DE "activo" A "activa"
                .await()
            android.util.Log.d("CanchaRepo", "Cancha eliminada: $canchaId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CanchaRepo", "Error al eliminar: ${e.message}")
            Result.failure(e)
        }
    }

    // Activar/Desactivar cancha
    suspend fun toggleActivoCancha(canchaId: String, activo: Boolean): Result<Unit> {
        return try {
            if (!esAdministrador()) {
                throw Exception("No tienes permisos de administrador")
            }

            canchasCollection.document(canchaId)
                .update("activa", activo)  // ✅ CAMBIADO DE "activo" A "activa"
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== OPERACIONES DE BÚSQUEDA ==========

    // Buscar canchas por distrito
    suspend fun buscarPorDistrito(distrito: String): Result<List<Cancha>> {
        return try {
            val snapshot = canchasCollection
                .whereEqualTo("distrito", distrito)
                .whereEqualTo("activa", true)  // ✅ CAMBIADO DE "activo" A "activa"
                .get()
                .await()

            // ✅ USA FirestoreConverter
            val canchas = FirestoreConverter.documentsToCanchas(snapshot.documents)
            Result.success(canchas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar canchas por rango de precio
    suspend fun buscarPorRangoPrecio(precioMin: Double, precioMax: Double): Result<List<Cancha>> {
        return try {
            val snapshot = canchasCollection
                .whereGreaterThanOrEqualTo("precioHora", precioMin)
                .whereLessThanOrEqualTo("precioHora", precioMax)
                .whereEqualTo("activa", true)  // ✅ CAMBIADO DE "activo" A "activa"
                .get()
                .await()

            // ✅ USA FirestoreConverter
            val canchas = FirestoreConverter.documentsToCanchas(snapshot.documents)
            Result.success(canchas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== MÉTODOS PARA CÓDIGOS DE VINCULACIÓN ==========

    // Generar código único de vinculación
    fun generarCodigoVinculacion(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val random = java.util.Random()
        val codigo = StringBuilder()

        // Formato: CANCHA-XXXX (ej: CANCHA-A3B7)
        codigo.append("CANCHA-")
        repeat(4) {
            codigo.append(chars[random.nextInt(chars.length)])
        }

        return codigo.toString()
    }

    // Verificar si el código ya existe
    suspend fun codigoExiste(codigo: String): Boolean {
        return try {
            val snapshot = canchasCollection
                .whereEqualTo("codigoInvitacion", codigo)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Validar código para registro de Admin
    suspend fun validarCodigoVinculacion(codigo: String): Result<Cancha> {
        return try {
            val snapshot = canchasCollection
                .whereEqualTo("codigoInvitacion", codigo)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Código no válido"))
            }

            // ✅ USA FirestoreConverter
            val cancha = FirestoreConverter.documentToCancha(snapshot.documents[0])
                ?: return Result.failure(Exception("Error al obtener cancha"))

            Result.success(cancha)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Vincular cancha con admin (marcar código como usado)
    suspend fun vincularCanchaConAdmin(canchaId: String, adminId: String): Result<Unit> {
        return try {
            canchasCollection.document(canchaId)
                .update(
                    mapOf(
                        "adminAsignado" to adminId
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== UTILIDADES ==========

    private suspend fun esAdministrador(): Boolean {
        return try {
            val uid = auth.currentUser?.uid ?: return false
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            val rol = userDoc.getString("rol")
            rol == UserRole.ADMIN.name || rol == UserRole.SUPERADMIN.name
        } catch (e: Exception) {
            false
        }
    }
}
