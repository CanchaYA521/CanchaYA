package com.rojassac.canchaya.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rojassac.canchaya.utils.NotificationHelper

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Guardar token en Firestore para el usuario actual
        saveFCMToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message from: ${remoteMessage.from}")

        // Verificar si el mensaje contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Verificar si el mensaje contiene notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Message notification: ${it.title} - ${it.body}")
            NotificationHelper.showNotification(
                this,
                it.title ?: "CanchaYA",
                it.body ?: ""
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "CanchaYA"
        val body = data["body"] ?: ""
        val type = data["type"] ?: ""

        when (type) {
            "reserva_confirmada" -> {
                NotificationHelper.showNotification(this, title, body)
            }
            "recordatorio" -> {
                NotificationHelper.showNotification(this, title, body)
            }
            "cancelacion" -> {
                NotificationHelper.showNotification(this, title, body)
            }
            "resena" -> {
                NotificationHelper.showNotification(this, title, body)
            }
            else -> {
                NotificationHelper.showNotification(this, title, body)
            }
        }
    }

    private fun saveFCMToken(token: String) {
        // TODO: Implementar guardado en Firestore
        // Se implementará en el siguiente paso
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
