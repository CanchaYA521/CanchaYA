package com.rojassac.canchaya.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.rojassac.canchaya.MainActivity
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.ActivityLoginBinding
import com.rojassac.canchaya.ui.superadmin.SuperAdminActivity
import com.rojassac.canchaya.utils.Resource
import com.rojassac.canchaya.utils.SessionManager
import com.rojassac.canchaya.utils.isValidEmail
import com.rojassac.canchaya.utils.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by lazy { AuthViewModel() }
    private lateinit var sessionManager: SessionManager

    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            handleGoogleSignInResult(account)
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google sign in failed", e)
            toast("Error al iniciar sesión con Google: ${e.message}")
            showLoading(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupGoogleSignIn()
        setupObservers()
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        // Login con Email
        binding.btnLoginEmail.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateEmailLogin(email, password)) {
                authViewModel.loginWithEmail(email, password)
            }
        }

        // Login con Google
        binding.btnLoginGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Ir a Registro
        binding.tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        account?.let {
            showLoading(true)
            authViewModel.signInWithGoogle(it.idToken!!)
        }
    }

    private fun validateEmailLogin(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etEmail.error = getString(R.string.error_email_vacio)
                false
            }
            !email.isValidEmail() -> {
                binding.etEmail.error = getString(R.string.error_email_invalido)
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = getString(R.string.error_password_vacio)
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = getString(R.string.error_password_corto)
                false
            }
            else -> true
        }
    }

    private fun setupObservers() {
        authViewModel.authState.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    val user = resource.data
                    if (user != null) {
                        // 🔵 CORREGIDO: Cambiar parámetros de saveUserSession
                        sessionManager.saveUserSession(
                            userId = user.uid,
                            email = user.email,           // ✅ era: userEmail
                            nombre = user.nombre,          // ✅ era: userName
                            rol = user.rol,               // ✅ era: userRole
                            sedeId = user.sedeId,         // ✅ NUEVO
                            canchasAsignadas = user.canchasAsignadas,  // ✅ NUEVO
                            tipoAdministracion = user.tipoAdministracion  // ✅ NUEVO
                        )

                        toast(getString(R.string.login_exitoso))
                        navigateToMain()
                    } else {
                        toast("Error: Usuario nulo")
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    toast(resource.message ?: "Error desconocido")
                }
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLoginEmail.isEnabled = !isLoading
        binding.btnLoginGoogle.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val userRole = sessionManager.getUserRole()
        val intent = when (userRole) {
            UserRole.SUPERADMIN -> Intent(this, SuperAdminActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
