package com.rojassac.canchaya.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import kotlinx.coroutines.tasks.await

// ✅ NUEVO: Data class para información de administración
data class ManagementInfo(
    val sedeId: String? = null,
    val canchaId: String? = null,
    val tipoAdministracion: String? = null,
    val canchasAsignadas: List<String> = emptyList()
)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "CanchaYA_Session",
        Context.MODE_PRIVATE
    )

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "SessionManager"

        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_CANCHA_ID = "canchaId"
        private const val KEY_SEDE_ID = "sedeId"
        private const val KEY_TIPO_ADMIN = "tipoAdmin"

        // ✅ NUEVO: Para lista de canchas
        private const val KEY_CANCHAS_ASIGNADAS = "canchasAsignadas"
        private const val KEY_CANCHAS_COUNT = "canchasCount"
    }

    // ========== GUARDAR SESIÓN ==========

    fun saveUserSession(
        userId: String,
        email: String,
        nombre: String,
        rol: UserRole,
        sedeId: String? = null,
        canchasAsignadas: List<String> = emptyList(),
        tipoAdministracion: String? = null
    ) {
        Log.d(TAG, "💾 Guardando sesión - User: $userId, Rol: $rol, SedeId: $sedeId, Canchas: ${canchasAsignadas.size}")

        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, nombre)
            putString(KEY_USER_ROLE, rol.name)
            putString(KEY_SEDE_ID, sedeId)
            putString(KEY_TIPO_ADMIN, tipoAdministracion)

            // ✅ NUEVO: Guardar lista de canchas como string separado por comas
            putString(KEY_CANCHAS_ASIGNADAS, canchasAsignadas.joinToString(","))
            putInt(KEY_CANCHAS_COUNT, canchasAsignadas.size)

            apply()
        }

        Log.d(TAG, "✅ Sesión guardada exitosamente")
    }

    // ========== OBTENER DATOS DE SESIÓN ==========

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserRole(): UserRole? {
        val roleName = prefs.getString(KEY_USER_ROLE, null)
        return try {
            roleName?.let { UserRole.valueOf(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener rol: ${e.message}")
            null
        }
    }

    fun getSedeId(): String? = prefs.getString(KEY_SEDE_ID, null)

    fun getTipoAdmin(): String? = prefs.getString(KEY_TIPO_ADMIN, null)

    // ✅ NUEVO: Obtener lista de canchas asignadas
    fun getCanchasAsignadas(): List<String> {
        val canchasString = prefs.getString(KEY_CANCHAS_ASIGNADAS, "") ?: ""
        return if (canchasString.isEmpty()) {
            emptyList()
        } else {
            canchasString.split(",").filter { it.isNotEmpty() }
        }
    }

    // ✅ NUEVO: Obtener la primera cancha asignada (para compatibilidad)
    fun getPrimeraCanchaAsignada(): String? {
        val sedeId = getSedeId()
        if (sedeId != null) return sedeId

        val canchas = getCanchasAsignadas()
        return canchas.firstOrNull()
    }

    // ✅ NUEVO: Obtener información de gestión completa
    fun getManagementInfo(): ManagementInfo {
        val tipoAdmin = getTipoAdmin()
        val sedeId = getSedeId()
        val canchasAsignadas = getCanchasAsignadas()

        // Si es sede, el primer elemento de canchasAsignadas es el sedeId
        // Si es cancha_individual, es el canchaId
        val canchaId = if (tipoAdmin == "cancha_individual" && canchasAsignadas.isNotEmpty()) {
            canchasAsignadas.first()
        } else null

        return ManagementInfo(
            sedeId = sedeId,
            canchaId = canchaId,
            tipoAdministracion = tipoAdmin,
            canchasAsignadas = canchasAsignadas
        )
    }

    // ✅ MODIFICADO: Contar canchas desde la lista
    fun contarCanchasAsignadas(): Int {
        return prefs.getInt(KEY_CANCHAS_COUNT, 0)
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null && auth.currentUser != null
    }

    fun clearSession() {
        Log.d(TAG, "🗑️ Limpiando sesión")
        prefs.edit().clear().apply()
    }

    // ========== 🔵 MODIFICADO: RECARGAR DATOS DESDE FIREBASE ==========

    suspend fun reloadUserData(): User? {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.e(TAG, "❌ No hay usuario autenticado")
                return null
            }

            Log.d(TAG, "🔄 Recargando datos del usuario: $userId")

            val userDoc = db.collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                Log.e(TAG, "❌ Documento de usuario no existe")
                return null
            }

            // ✅ NUEVO: Extraer datos correctamente
            val user = User(
                uid = userDoc.getString("uid") ?: userId,
                nombre = userDoc.getString("nombre") ?: "",
                celular = userDoc.getString("celular") ?: "",
                email = userDoc.getString("email") ?: "",
                rol = try {
                    UserRole.valueOf(userDoc.getString("rol") ?: "USUARIO")
                } catch (e: Exception) {
                    UserRole.USUARIO
                },
                sedeId = userDoc.getString("sedeId"),
                canchasAsignadas = (userDoc.get("canchasAsignadas") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                tipoAdministracion = userDoc.getString("tipoAdministracion"),
                canchaId = userDoc.getString("canchaId"),
                fechaCreacion = userDoc.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                activo = userDoc.getBoolean("activo") ?: true
            )

            // Guardar en sesión
            saveUserSession(
                userId = user.uid,
                email = user.email,
                nombre = user.nombre,
                rol = user.rol,
                sedeId = user.sedeId,
                canchasAsignadas = user.canchasAsignadas,
                tipoAdministracion = user.tipoAdministracion
            )

            Log.d(TAG, "✅ Datos recargados - Sede: ${user.sedeId}, Canchas: ${user.canchasAsignadas.size}")
            user

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al recargar datos: ${e.message}", e)
            null
        }
    }

    // ========== VERIFICAR ROL ==========

    fun isAdmin(): Boolean {
        return getUserRole() == UserRole.ADMIN
    }

    fun isSuperAdmin(): Boolean {
        return getUserRole() == UserRole.SUPERADMIN
    }

    fun isUsuario(): Boolean {
        return getUserRole() == UserRole.USUARIO
    }
}
