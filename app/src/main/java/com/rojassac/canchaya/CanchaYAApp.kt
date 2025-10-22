package com.rojassac.canchaya

import android.app.Application
import com.google.firebase.FirebaseApp
import com.rojassac.canchaya.utils.NotificationHelper

class CanchaYAApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Crear canal de notificaciones
        NotificationHelper.createNotificationChannel(this)
    }
}
