package com.rojassac.canchaya.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.rojassac.canchaya.utils.Constants
import kotlinx.coroutines.tasks.await

class NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val fcm = FirebaseMessaging.getInstance()

    suspend fun saveFCMToken(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
            val token = fcm.token.await()

            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("fcmToken", token)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendNotification(
        userId: String,
        title: String,
        body: String,
        type: String
    ): Result<Unit> {
        return try {
            // Obtener el token del usuario
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            val fcmToken = userDoc.getString("fcmToken") ?: throw Exception("Token no encontrado")

            // Crear documento de notificación
            val notification = hashMapOf(
                "userId" to userId,
                "title" to title,
                "body" to body,
                "type" to type,
                "token" to fcmToken,
                "timestamp" to System.currentTimeMillis(),
                "sent" to false
            )

            firestore.collection("notifications")
                .add(notification)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun notificarReservaConfirmada(reservaId: String, canchaName: String, userId: String) {
        sendNotification(
            userId = userId,
            title = "¡Reserva Confirmada!",
            body = "Tu reserva en $canchaName ha sido confirmada exitosamente.",
            type = "reserva_confirmada"
        )
    }

    suspend fun notificarCancelacion(canchaName: String, userId: String) {
        sendNotification(
            userId = userId,
            title = "Reserva Cancelada",
            body = "Tu reserva en $canchaName ha sido cancelada.",
            type = "cancelacion"
        )
    }

    suspend fun notificarResenaRecibida(canchaName: String, calificacion: Float, userId: String) {
        sendNotification(
            userId = userId,
            title = "Nueva Reseña",
            body = "$canchaName recibió una calificación de $calificacion estrellas.",
            type = "resena"
        )
    }
}
