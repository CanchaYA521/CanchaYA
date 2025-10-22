package com.rojassac.canchaya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rojassac.canchaya.data.model.Resena
import com.rojassac.canchaya.utils.Constants
import kotlinx.coroutines.tasks.await

class ResenaRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun crearResena(resena: Resena): Result<String> {
        return try {
            val docRef = firestore.collection(Constants.RESENAS_COLLECTION)
                .document()

            val nuevaResena = resena.copy(id = docRef.id)
            docRef.set(nuevaResena).await()

            // Actualizar calificación promedio de la cancha
            actualizarCalificacionCancha(resena.canchaId)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerResenasPorCancha(canchaId: String): Result<List<Resena>> {
        return try {
            val snapshot = firestore.collection(Constants.RESENAS_COLLECTION)
                .whereEqualTo("canchaId", canchaId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val resenas = snapshot.documents.mapNotNull { it.toObject(Resena::class.java) }
            Result.success(resenas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerResenasPorUsuario(userId: String): Result<List<Resena>> {
        return try {
            val snapshot = firestore.collection(Constants.RESENAS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val resenas = snapshot.documents.mapNotNull { it.toObject(Resena::class.java) }
            Result.success(resenas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun darLike(resenaId: String, userId: String): Result<Unit> {
        return try {
            val docRef = firestore.collection(Constants.RESENAS_COLLECTION).document(resenaId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val resena = snapshot.toObject(Resena::class.java)

                val likedBy = resena?.likedBy?.toMutableList() ?: mutableListOf()

                if (userId in likedBy) {
                    // Quitar like
                    likedBy.remove(userId)
                } else {
                    // Dar like
                    likedBy.add(userId)
                }

                transaction.update(docRef, mapOf(
                    "likes" to likedBy.size,
                    "likedBy" to likedBy
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun actualizarCalificacionCancha(canchaId: String) {
        try {
            val snapshot = firestore.collection(Constants.RESENAS_COLLECTION)
                .whereEqualTo("canchaId", canchaId)
                .get()
                .await()

            val resenas = snapshot.documents.mapNotNull { it.toObject(Resena::class.java) }

            if (resenas.isNotEmpty()) {
                val promedioCalificacion = resenas.map { it.calificacion }.average().toFloat()
                val totalResenas = resenas.size

                firestore.collection(Constants.CANCHAS_COLLECTION)
                    .document(canchaId)
                    .update(mapOf(
                        "calificacionPromedio" to promedioCalificacion,
                        "totalResenas" to totalResenas
                    ))
                    .await()
            }
        } catch (e: Exception) {
            // Log error
        }
    }
}
