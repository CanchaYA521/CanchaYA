package com.rojassac.canchaya.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.MainActivity
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.ActivityRegisterBinding
import com.rojassac.canchaya.utils.Resource
import com.rojassac.canchaya.utils.isValidEmail
import com.rojassac.canchaya.utils.isValidPhone
import com.rojassac.canchaya.utils.toast

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel
    private val db = FirebaseFirestore.getInstance()
    private var codigoValidado: CodigoValidado? = null
    private var rolSeleccionado: UserRole = UserRole.USUARIO

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
            Log.e("RegisterActivity", "Google sign in failed", e)
            toast("Error al registrarse con Google: ${e.message}")
            showLoading(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupGoogleSignIn()
        setupRolSpinner()
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupRolSpinner() {
        val roles = arrayOf("Usuario", "Dueño de Cancha/Sede")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRol.adapter = adapter
        binding.spinnerRol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                rolSeleccionado = if (position == 1) UserRole.ADMIN else UserRole.USUARIO
                toggleCodigoVinculacion(position == 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun toggleCodigoVinculacion(mostrar: Boolean) {
        binding.layoutCodigoVinculacion.visibility = if (mostrar) View.VISIBLE else View.GONE
        if (!mostrar) {
            codigoValidado = null
            binding.tvCodigoStatus.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // Registrar con Email
        binding.btnRegisterEmail.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val celular = binding.etCelular.text.toString().trim()
            val email = binding.etEmailReg.text.toString().trim()
            val password = binding.etPasswordReg.text.toString().trim()

            if (validateEmailRegister(nombre, celular, email, password)) {
                registrarUsuario(email, password, nombre, celular)
            }
        }

        // Registrar con Google
        binding.btnRegisterGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Validar Código
        binding.btnValidarCodigo.setOnClickListener {
            validarCodigoInvitacion()
        }

        // Volver a Login
        binding.tvYaTienesCuenta.setOnClickListener {
            finish()
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

    private fun validarCodigoInvitacion() {
        val codigo = binding.etCodigoVinculacion.text.toString().trim().uppercase()
        if (codigo.isEmpty()) {
            binding.etCodigoVinculacion.error = "Ingresa el código"
            return
        }

        binding.btnValidarCodigo.isEnabled = false
        binding.btnValidarCodigo.text = "Validando..."

        db.collection("codigos_invitacion")
            .document(codigo)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val usado = document.getBoolean("usado") ?: true
                    val activo = document.getBoolean("activo") ?: false

                    if (!usado && activo) {
                        val tipo = document.getString("tipo") ?: ""
                        val sedeId = document.getString("sedeId")
                        val canchaId = document.getString("canchaId")
                        val nombreNegocio = (document.get("metadata") as? Map<*, *>)?.get("nombreNegocio") as? String ?: "Negocio"

                        codigoValidado = CodigoValidado(
                            codigo = codigo,
                            tipo = tipo,
                            sedeId = sedeId,
                            canchaId = canchaId,
                            nombreNegocio = nombreNegocio
                        )

                        binding.tvCodigoStatus.text = "✓ Código válido: $nombreNegocio"
                        binding.tvCodigoStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                        binding.tvCodigoStatus.visibility = View.VISIBLE
                        binding.etCodigoVinculacion.isEnabled = false
                        toast("Código válido")
                    } else {
                        binding.tvCodigoStatus.text = "✗ Código inválido o ya usado"
                        binding.tvCodigoStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                        binding.tvCodigoStatus.visibility = View.VISIBLE
                        codigoValidado = null
                    }
                } else {
                    binding.tvCodigoStatus.text = "✗ Código no encontrado"
                    binding.tvCodigoStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                    binding.tvCodigoStatus.visibility = View.VISIBLE
                    codigoValidado = null
                }
                binding.btnValidarCodigo.isEnabled = true
                binding.btnValidarCodigo.text = "Validar Código"
            }
            .addOnFailureListener { e ->
                binding.tvCodigoStatus.text = "✗ Error al validar código"
                binding.tvCodigoStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                binding.tvCodigoStatus.visibility = View.VISIBLE
                binding.btnValidarCodigo.isEnabled = true
                binding.btnValidarCodigo.text = "Validar Código"
                toast("Error: ${e.message}")
            }
    }

    private fun registrarUsuario(email: String, password: String, nombre: String, celular: String) {
        showLoading(true)
        authViewModel.registerWithEmail(email, password).observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val userId = resource.data?.uid
                    if (userId != null) {
                        crearDocumentoUsuario(userId, nombre, email, celular)
                    } else {
                        showLoading(false)
                        toast("Error al obtener ID de usuario")
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    toast(resource.message ?: "Error al registrar")
                }
            }
        }
    }

    private fun crearDocumentoUsuario(
        userId: String,
        nombre: String,
        email: String,
        celular: String
    ) {
        // NOTA: El tipo debe ser HashMap<String, Any?> para permitir null sin error,
        // no usar solo Any.
        val userData = hashMapOf<String, Any?>(
            "nombre" to nombre,
            "email" to email,
            "celular" to celular,
            "rol" to rolSeleccionado.name,
            "activo" to true,
            "fechaCreacion" to FieldValue.serverTimestamp()
        )

        if (rolSeleccionado == UserRole.ADMIN && codigoValidado != null) {
            userData["sedeId"] = codigoValidado!!.sedeId
            userData["canchaId"] = codigoValidado!!.canchaId
            userData["tipoAdministracion"] = codigoValidado!!.tipo
            userData["codigoUsado"] = codigoValidado!!.codigo
        } else {
            userData["sedeId"] = null
            userData["canchaId"] = null
            userData["tipoAdministracion"] = null
            userData["codigoUsado"] = null
        }

        db.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                if (rolSeleccionado == UserRole.ADMIN && codigoValidado != null) {
                    marcarCodigoUsado(userId)
                    actualizarAdminAsignado(userId)
                } else {
                    showLoading(false)
                    toast(getString(R.string.registro_exitoso))
                    navigateToMain()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                toast("Error al crear usuario: ${e.message}")
            }
    }

    private fun marcarCodigoUsado(userId: String) {
        codigoValidado?.let { codigo ->
            val updates = hashMapOf<String, Any>(
                "usado" to true,
                "fechaUso" to FieldValue.serverTimestamp(),
                "adminAsignado" to userId
            )
            db.collection("codigos_invitacion")
                .document(codigo.codigo)
                .update(updates)
        }
    }

    private fun actualizarAdminAsignado(userId: String) {
        codigoValidado?.let { codigo ->
            val collection = if (codigo.tipo == "sede") "sedes" else "canchas"
            val documentId = codigo.sedeId ?: codigo.canchaId
            if (documentId != null) {
                db.collection(collection)
                    .document(documentId)
                    .update("adminAsignado", userId)
                    .addOnSuccessListener {
                        showLoading(false)
                        toast("Cuenta creada y vinculada exitosamente")
                        navigateToMain()
                    }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        toast("Cuenta creada pero error al vincular: ${e.message}")
                        navigateToMain()
                    }
            }
        }
    }

    private fun validateEmailRegister(nombre: String, celular: String, email: String, password: String): Boolean {
        return when {
            nombre.isEmpty() -> {
                binding.etNombre.error = getString(R.string.error_nombre_vacio)
                false
            }
            celular.isEmpty() -> {
                binding.etCelular.error = getString(R.string.error_celular_vacio)
                false
            }
            !celular.isValidPhone() -> {
                binding.etCelular.error = getString(R.string.error_celular_invalido)
                false
            }
            email.isEmpty() -> {
                binding.etEmailReg.error = getString(R.string.error_email_vacio)
                false
            }
            !email.isValidEmail() -> {
                binding.etEmailReg.error = getString(R.string.error_email_invalido)
                false
            }
            password.isEmpty() -> {
                binding.etPasswordReg.error = getString(R.string.error_password_vacio)
                false
            }
            password.length < 6 -> {
                binding.etPasswordReg.error = getString(R.string.error_password_corto)
                false
            }
            rolSeleccionado == UserRole.ADMIN && codigoValidado == null -> {
                toast("Debes validar el código de invitación primero")
                false
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegisterEmail.isEnabled = !isLoading
        binding.btnRegisterGoogle.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    data class CodigoValidado(
        val codigo: String,
        val tipo: String,
        val sedeId: String?,
        val canchaId: String?,
        val nombreNegocio: String
    )
}
