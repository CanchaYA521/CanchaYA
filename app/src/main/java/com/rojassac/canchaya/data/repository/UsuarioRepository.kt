package com.rojassac.canchaya.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.FirestoreConverter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UsuarioRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usuariosCollection = firestore.collection(Constants.USERS_COLLECTION)

    // ✅ CORREGIDO: Obtener usuario actual con conversión robusta
    suspend fun obtenerUsuarioActual(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val snapshot = usuariosCollection.document(uid).get().await()

            // ✅ USA FirestoreConverter en lugar de toObject directo
            val usuario = FirestoreConverter.documentToUser(snapshot)

            if (usuario != null) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si el usuario es administrador
    suspend fun esAdministrador(): Boolean {
        return try {
            val resultado = obtenerUsuarioActual()
            resultado.getOrNull()?.rol?.name == "ADMIN"
        } catch (e: Exception) {
            false
        }
    }

    // Crear usuario en Firestore
    suspend fun crearUsuario(usuario: User): Result<Unit> {
        return try {
            usuariosCollection.document(usuario.uid).set(usuario).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar rol de usuario
    suspend fun actualizarRolUsuario(uid: String, nuevoRol: String): Result<Unit> {
        return try {
            if (!esAdministrador()) {
                return Result.failure(Exception("No tienes permisos de administrador"))
            }

            usuariosCollection.document(uid).update("rol", nuevoRol).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ CORREGIDO: Observar usuario en tiempo real con conversión robusta
    fun observarUsuario(uid: String): Flow<User?> = callbackFlow {
        val listener = usuariosCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // ✅ USA FirestoreConverter en lugar de toObject directo
                val usuario = snapshot?.let { FirestoreConverter.documentToUser(it) }
                trySend(usuario)
            }

        awaitClose { listener.remove() }
    }
}
