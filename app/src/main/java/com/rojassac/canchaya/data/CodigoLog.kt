package com.rojassac.canchaya.data.model

data class CodigoLog(
    val id: String = "",
    val canchaId: String = "",
    val canchaNombre: String = "",
    val codigo: String = "",
    val accion: String = "", // "CREADO", "USADO", "EXPIRADO", "TRANSFERIDO"
    val adminAnterior: String? = null,
    val adminNuevo: String? = null,
    val superadminId: String = "",
    val superadminNombre: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val detalles: String = ""
)
