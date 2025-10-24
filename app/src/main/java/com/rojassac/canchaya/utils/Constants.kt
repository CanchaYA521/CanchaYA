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
    const val SEDES_COLLECTION = "sedes"

    // Colecciones - Sistema de Pagos y Suscripciones
    const val SUBSCRIPTIONS_COLLECTION = "subscriptions"
    const val PLANS_COLLECTION = "plans"
    const val CARGOS_COLLECTION = "cargos"

    // ✅ NUEVO: Colecciones - Sistema de Configuración SuperAdmin (23 Oct 2025)
    const val PROMOCIONES_COLLECTION = "promociones"
    const val CONFIGURACION_COLLECTION = "configuracion"
    const val NOTIFICACIONES_COLLECTION = "notificaciones"

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
    const val CODIGO_LENGTH = 10
    const val CODIGO_PREFIX = "SE"

    // ========================
    // Estados de Reserva
    // ========================
    const val ESTADO_PENDIENTE = "PENDIENTE"
    const val ESTADO_CONFIRMADA = "CONFIRMADA"
    const val ESTADO_COMPLETADA = "COMPLETADA"
    const val ESTADO_CANCELADA = "CANCELADA"

    // ========================
    // Roles de Usuario
    // ========================
    const val ROLE_USUARIO = "USUARIO"
    const val ROLE_ADMIN = "ADMIN"
    const val ROLE_SUPERADMIN = "SUPERADMIN"

    // ✅ NUEVO - IDs de Planes (23 Oct 2025)
    const val PLAN_BASICO = "basico"
    const val PLAN_PRO = "pro"
    const val PLAN_PREMIUM = "premium"
    const val PLAN_ENTERPRISE = "enterprise"

    // ========================
    // Deportes disponibles
    // ========================
    val DEPORTES_DISPONIBLES = listOf(
        "Fútbol",
        "Fútbol 7",
        "Fútbol 5",
        "Básquetbol",
        "Vóley",
        "Tenis",
        "Pádel",
        "Otro"
    )

    // ========================
    // Tipos de suelo
    // ========================
    val TIPOS_SUELO = listOf(
        "Grass Natural",
        "Grass Sintético",
        "Cemento",
        "Parquet",
        "Arcilla",
        "Otro"
    )

    // ✅ NUEVO - Formato de fecha (23 Oct 2025)
    const val DATE_FORMAT = "dd/MM/yyyy"

    // ✅ NUEVO - IDs de Configuración (23 Oct 2025)
    const val CONFIGURACION_GLOBAL_ID = "global"
}
