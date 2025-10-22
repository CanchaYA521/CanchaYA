package com.rojassac.canchaya.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.MainActivity
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.ActivitySplashBinding
import com.rojassac.canchaya.ui.auth.LoginActivity
import com.rojassac.canchaya.ui.superadmin.SuperAdminActivity
import com.rojassac.canchaya.utils.FirestoreConverter
import com.rojassac.canchaya.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, SPLASH_DELAY)
    }

    private fun checkAuthAndNavigate() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.d(TAG, "❌ No hay usuario autenticado")
            navigateToLogin()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d(TAG, "🔄 Usuario autenticado: ${currentUser.uid}")

                val userDoc = db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (!userDoc.exists()) {
                    Log.e(TAG, "❌ Documento de usuario no existe")
                    auth.signOut()
                    sessionManager.clearSession()
                    navigateToLogin()
                    return@launch
                }

                val user = FirestoreConverter.documentToUser(userDoc)

                if (user == null) {
                    Log.e(TAG, "❌ Error al convertir documento a User")
                    navigateToLogin()
                    return@launch
                }

                // 🔵 CORREGIDO: Guardar sesión con parámetros correctos
                sessionManager.saveUserSession(
                    userId = user.uid,
                    email = user.email,
                    nombre = user.nombre,
                    rol = user.rol,
                    sedeId = user.sedeId,
                    canchasAsignadas = user.canchasAsignadas,
                    tipoAdministracion = user.tipoAdministracion
                )

                Log.d(TAG, "✅ Sesión guardada - Rol: ${user.rol}")
                navigateByRole(user.rol)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al verificar usuario: ${e.message}", e)
                navigateToLogin()
            }
        }
    }

    private fun navigateByRole(role: UserRole) {
        val intent = when (role) {
            UserRole.USUARIO -> Intent(this, MainActivity::class.java)
            UserRole.ADMIN -> Intent(this, MainActivity::class.java)
            UserRole.SUPERADMIN -> Intent(this, SuperAdminActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
