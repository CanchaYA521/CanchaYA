package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val nombre: String = "",
    val celular: String = "",
    val email: String = "",
    val rol: UserRole = UserRole.USUARIO,

    // ✅ GESTIÓN DE CANCHAS - COMPATIBILIDAD
    val canchaId: String? = null, // ⚠️ DEPRECADO - Mantener para compatibilidad con código viejo
    val canchasAsignadas: List<String> = listOf(), // ✅ NUEVO - Lista de IDs de canchas que administra

    // ✅ GESTIÓN DE SEDES
    val sedeId: String? = null, // ID de la sede si administra una sede completa
    val tipoAdministracion: String? = null, // "sede" | "cancha_individual" | "multiple"

    val fechaCreacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true,
    val stability: Int = 0
) : Parcelable

enum class UserRole {
    USUARIO,
    ADMIN,
    SUPERADMIN
}
