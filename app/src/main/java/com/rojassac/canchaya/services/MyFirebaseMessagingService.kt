package com.rojassac.canchaya.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rojassac.canchaya.R
import com.rojassac.canchaya.MainActivity

/**
 * ✅ NUEVO (24 Oct 2025)
 * Servicio para recibir notificaciones push de FCM
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extraer datos de la notificación
        val titulo = remoteMessage.notification?.title ?: remoteMessage.data["titulo"] ?: "Notificación"
        val mensaje = remoteMessage.notification?.body ?: remoteMessage.data["mensaje"] ?: ""
        val tipo = remoteMessage.data["tipo"] ?: "INFO"
        val urlDestino = remoteMessage.data["urlDestino"]

        // Mostrar notificación
        mostrarNotificacion(titulo, mensaje, tipo, urlDestino)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // TODO: Guardar el token en Firestore para el usuario actual
        // Lo implementaremos después cuando implementes login
    }

    private fun mostrarNotificacion(
        titulo: String,
        mensaje: String,
        tipo: String,
        urlDestino: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "canchaya_notifications"

        // Crear canal de notificación (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones de CanchayA",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones importantes de la aplicación"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            urlDestino?.let { putExtra("destino", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
