package com.rojassac.canchaya.utils

object Constants {

    // ========================
    // Colecciones Firestore
    // ========================
    const val USERS_COLLECTION = "users"
    const val CANCHAS_COLLECTION = "canchas"
    const val RESERVAS_COLLECTION = "reservas"
    const val HORARIOS_COLLECTION = "horarios"
    const val RESENAS_COLLECTION = "resenas"
    const val CODIGO_LOGS_COLLECTION = "codigo_logs"
    const val SEDES_COLLECTION = "sedes"  // 🆕 AGREGADO (22 Oct 2025)

    // 🆕 NUEVAS COLECCIONES - Sistema de Pagos y Suscripciones
    const val SUBSCRIPTIONS_COLLECTION = "subscriptions"
    const val PLANS_COLLECTION = "plans"
    const val CARGOS_COLLECTION = "cargos"

    // ========================
    // SharedPreferences
    // ========================
    const val PREFS_NAME = "CanchaYAPrefs"
    const val KEY_USER_ID = "userId"
    const val KEY_USER_ROLE = "userRole"

    // ========================
    // Códigos de vinculación
    // ========================
    const val CODIGO_EXPIRACION_DIAS = 7
    const val CODIGO_LENGTH = 8

    // 🆕 NUEVO: Formato de códigos de sede (22 Oct 2025)
    const val CODIGO_SEDE_PREFIX = "SE"
    const val CODIGO_SEDE_LENGTH = 10  // SE00000001 = 10 caracteres

    // ========================
    // Formato de fechas
    // ========================
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val TIME_FORMAT = "HH:mm"
    const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    // 🆕 NUEVAS CONSTANTES - Sistema de Suscripciones
    // ========================
    // IDs de Planes (deben coincidir con Firebase)
    // ========================
    const val PLAN_BASICO = "BASICO"
    const val PLAN_PRO = "PRO"
    const val PLAN_PREMIUM = "PREMIUM"
    const val PLAN_ENTERPRISE = "ENTERPRISE"

    // ========================
    // Duración de suscripciones
    // ========================
    const val DIAS_MES = 30L // Para calcular fechas de vencimiento
    const val MILISEGUNDOS_DIA = 86400000L // 24 horas en milisegundos

    // ========================
    // Comisiones por plan (en porcentaje)
    // ========================
    const val COMISION_BASICO = 0.40 // 40%
    const val COMISION_PRO = 0.25 // 25%
    const val COMISION_PREMIUM = 0.15 // 15%
    const val COMISION_ENTERPRISE = 0.0 // 0%

    // ========================
    // Plazos de retiro (en días)
    // ========================
    const val PLAZO_RETIRO_BASICO = 7
    const val PLAZO_RETIRO_PRO = 1
    const val PLAZO_RETIRO_PREMIUM = 0 // Mismo día
    const val PLAZO_RETIRO_ENTERPRISE = 0 // Mismo día
}
