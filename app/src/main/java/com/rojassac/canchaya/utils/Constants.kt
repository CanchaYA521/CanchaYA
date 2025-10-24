package com.rojassac.canchaya.utils

object Constants {

    // ════════════════════════════════════════════════════════════════
    // 📂 COLECCIONES FIRESTORE
    // ════════════════════════════════════════════════════════════════

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
    const val USER_NOTIFICATIONS_COLLECTION = "user_notifications"

    // ⚙️ PARÁMETROS GLOBALES (NUEVO - 23 Oct 2025)
    const val COLLECTION_PARAMETROS = "parametros_globales"
    const val DOC_CONFIG_GLOBAL = "config_global"

    // ════════════════════════════════════════════════════════════════
    // 💾 SHARED PREFERENCES
    // ════════════════════════════════════════════════════════════════

    const val PREFS_NAME = "CanchaYAPrefs"
    const val KEY_USER_ID = "userId"
    const val KEY_USER_ROLE = "userRole"

    // ════════════════════════════════════════════════════════════════
    // 🔑 CÓDIGOS DE VINCULACIÓN
    // ════════════════════════════════════════════════════════════════

    const val CODIGO_EXPIRACION_DIAS = 7
    const val CODIGO_LENGTH = 10
    const val CODIGO_PREFIX = "SE"

    // ════════════════════════════════════════════════════════════════
    // 📊 ESTADOS DE RESERVA
    // ════════════════════════════════════════════════════════════════

    const val ESTADO_PENDIENTE = "PENDIENTE"
    const val ESTADO_CONFIRMADA = "CONFIRMADA"
    const val ESTADO_COMPLETADA = "COMPLETADA"
    const val ESTADO_CANCELADA = "CANCELADA"

    // ════════════════════════════════════════════════════════════════
    // 👤 ROLES DE USUARIO
    // ════════════════════════════════════════════════════════════════

    const val ROLE_USUARIO = "USUARIO"
    const val ROLE_ADMIN = "ADMIN"
    const val ROLE_SUPERADMIN = "SUPERADMIN"

    // ✅ NUEVO - IDs de Planes (23 Oct 2025)
    const val PLAN_BASICO = "basico"
    const val PLAN_PRO = "pro"
    const val PLAN_PREMIUM = "premium"
    const val PLAN_ENTERPRISE = "enterprise"

    // ════════════════════════════════════════════════════════════════
    // ⚽ DEPORTES DISPONIBLES
    // ════════════════════════════════════════════════════════════════

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

    // ════════════════════════════════════════════════════════════════
    // 🏟️ TIPOS DE SUELO
    // ════════════════════════════════════════════════════════════════

    val TIPOS_SUELO = listOf(
        "Grass Natural",
        "Grass Sintético",
        "Cemento",
        "Parquet",
        "Arcilla",
        "Otro"
    )

    // ════════════════════════════════════════════════════════════════
    // 📅 FORMATO DE FECHA
    // ════════════════════════════════════════════════════════════════

    const val DATE_FORMAT = "dd/MM/yyyy"
    const val DATETIME_FORMAT = "dd/MM/yyyy HH:mm"

    // ✅ NUEVO - IDs de Configuración (23 Oct 2025)
    const val CONFIGURACION_GLOBAL_ID = "global"

    // ════════════════════════════════════════════════════════════════
    // ⚙️ VALORES POR DEFECTO - PARÁMETROS GLOBALES (23 Oct 2025)
    // ════════════════════════════════════════════════════════════════

    object DefaultParams {
        const val ANTICIPACION_MINIMA = 2         // horas
        const val ANTICIPACION_MAXIMA = 30        // días
        const val DURACION_MINIMA = 1             // hora
        const val DURACION_MAXIMA = 4             // horas
        const val PORCENTAJE_ANTICIPO = 50        // %
        const val COMISION_PLATAFORMA = 10.0      // %
        const val MONTO_MINIMO = 20.0             // S/.
    }

    // ════════════════════════════════════════════════════════════════
    // 🔔 NOTIFICACIONES (NUEVO - 24 Oct 2025)
    // ════════════════════════════════════════════════════════════════

    object Notifications {
        const val CHANNEL_ID = "canchaya_notifications"
        const val CHANNEL_NAME = "Notificaciones de CanchayA"
        const val CHANNEL_DESCRIPTION = "Notificaciones importantes de la aplicación"

        // Tipos de notificación
        const val TYPE_INFO = "INFO"
        const val TYPE_ALERTA = "ALERTA"
        const val TYPE_PROMOCION = "PROMOCION"
        const val TYPE_SISTEMA = "SISTEMA"
        const val TYPE_EVENTO = "EVENTO"

        // Estados de notificación
        const val ESTADO_PENDIENTE = "PENDIENTE"
        const val ESTADO_ENVIADA = "ENVIADA"
        const val ESTADO_PROGRAMADA = "PROGRAMADA"
        const val ESTADO_CANCELADA = "CANCELADA"
        const val ESTADO_ENVIANDO = "ENVIANDO"
    }
}
