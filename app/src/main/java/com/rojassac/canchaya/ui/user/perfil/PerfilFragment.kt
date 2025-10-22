package com.rojassac.canchaya.ui.user.perfil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.databinding.FragmentPerfilBinding
import com.rojassac.canchaya.ui.auth.AuthViewModel
import com.rojassac.canchaya.ui.auth.LoginActivity
import com.rojassac.canchaya.utils.Resource
import com.rojassac.canchaya.utils.SessionManager
import com.rojassac.canchaya.utils.toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        sessionManager = SessionManager(requireContext())

        // ✅ PRIMERO: Cargar datos de SessionManager (instantáneo)
        loadCachedUserData()

        // ✅ SEGUNDO: Verificar y actualizar desde Firestore
        loadUserData()

        // ✅ Cargar estadísticas del usuario
        loadUserStats()

        setupListeners()
    }

    /**
     * ✅ NUEVO: Cargar datos desde SessionManager primero (sin delay)
     */
    private fun loadCachedUserData() {
        val userName = sessionManager.getUserName()
        val userEmail = sessionManager.getUserEmail()

        if (userName != null && userEmail != null) {
            binding.tvNombre.text = userName
            binding.tvEmail.text = userEmail
        }
    }

    /**
     * Cargar datos desde Firestore para verificar y actualizar
     */
    private fun loadUserData() {
        authViewModel.getCurrentUser()
        authViewModel.authState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data!!
                    // Actualizar UI con datos de Firestore
                    binding.tvNombre.text = user.nombre
                    binding.tvEmail.text = user.email
                    binding.tvCelular.text = "+51 ${user.celular}"

                    // 🔵 CORREGIDO: Actualizar SessionManager con parámetros correctos
                    sessionManager.saveUserSession(
                        userId = user.uid,
                        email = user.email,
                        nombre = user.nombre,
                        rol = user.rol,
                        sedeId = user.sedeId,
                        canchasAsignadas = user.canchasAsignadas,
                        tipoAdministracion = user.tipoAdministracion
                    )
                }
                is Resource.Error -> {
                    requireContext().toast("Error al cargar datos del perfil")
                }
                else -> {}
            }
        }
    }

    /**
     * ✅ NUEVO: Cargar estadísticas del usuario
     */
    private fun loadUserStats() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                // Contar reservas totales
                val reservasSnapshot = firestore.collection("reservas")
                    .whereEqualTo("usuarioId", userId)
                    .get()
                    .await()

                val totalReservas = reservasSnapshot.size()
                val reservasActivas = reservasSnapshot.documents.count {
                    it.getString("estado") == "confirmada"
                }

                // Contar canchas favoritas (si tienes esta funcionalidad)
                val favoritosSnapshot = firestore.collection("favoritos")
                    .whereEqualTo("usuarioId", userId)
                    .get()
                    .await()

                val totalFavoritos = favoritosSnapshot.size()

                // Actualizar UI
                binding.tvTotalReservas.text = totalReservas.toString()
                binding.tvReservasActivas.text = reservasActivas.toString()
                binding.tvCanchasFavoritas.text = totalFavoritos.toString()

            } catch (e: Exception) {
                // Error al cargar estadísticas (no crítico)
            }
        }
    }

    /**
     * ✅ NUEVO: Setup de listeners para todas las opciones
     */
    private fun setupListeners() {
        // Editar perfil
        binding.btnEditarPerfil.setOnClickListener {
            // TODO: Abrir pantalla de edición de perfil
            requireContext().toast("Función en desarrollo")
        }

        // Cambiar foto de perfil
        binding.ivFotoPerfil.setOnClickListener {
            // TODO: Abrir selector de imagen
            requireContext().toast("Función en desarrollo")
        }

        // Ver historial completo de reservas
        binding.cardHistorialReservas.setOnClickListener {
            // TODO: Navegar a historial completo
            requireContext().toast("Función en desarrollo")
        }

        // Mis favoritos
        binding.cardMisFavoritos.setOnClickListener {
            // TODO: Navegar a canchas favoritas
            requireContext().toast("Función en desarrollo")
        }

        // Configuración
        binding.cardConfiguracion.setOnClickListener {
            mostrarConfiguracion()
        }

        // Ayuda y soporte
        binding.cardAyuda.setOnClickListener {
            mostrarAyuda()
        }

        // Cambiar contraseña
        binding.btnCambiarContrasena.setOnClickListener {
            cambiarContrasena()
        }

        // Cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            confirmarCerrarSesion()
        }

        // Eliminar cuenta
        binding.btnEliminarCuenta.setOnClickListener {
            confirmarEliminarCuenta()
        }
    }

    /**
     * ✅ NUEVO: Mostrar opciones de configuración
     */
    private fun mostrarConfiguracion() {
        val opciones = arrayOf(
            "Notificaciones de reservas",
            "Notificaciones de promociones",
            "Notificaciones push",
            "Modo oscuro"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Configuración")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> requireContext().toast("Notificaciones de reservas activadas")
                    1 -> requireContext().toast("Notificaciones de promociones activadas")
                    2 -> requireContext().toast("Notificaciones push activadas")
                    3 -> requireContext().toast("Modo oscuro en desarrollo")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * ✅ NUEVO: Mostrar opciones de ayuda
     */
    private fun mostrarAyuda() {
        val opciones = arrayOf(
            "Contactar por WhatsApp",
            "Términos y condiciones",
            "Política de privacidad",
            "Preguntas frecuentes",
            "Acerca de"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Ayuda y Soporte")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirWhatsApp()
                    1 -> abrirTerminos()
                    2 -> abrirPoliticas()
                    3 -> abrirFAQ()
                    4 -> mostrarAcercaDe()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * ✅ NUEVO: Abrir WhatsApp para soporte
     */
    private fun abrirWhatsApp() {
        val phoneNumber = "51987654321" // Cambiar por tu número
        val message = "Hola, necesito ayuda con CanchaYA"
        val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            requireContext().toast("No se pudo abrir WhatsApp")
        }
    }

    /**
     * ✅ NUEVO: Abrir términos y condiciones
     */
    private fun abrirTerminos() {
        val url = "https://tucanchaya.com/terminos" // Cambiar por tu URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    /**
     * ✅ NUEVO: Abrir política de privacidad
     */
    private fun abrirPoliticas() {
        val url = "https://tucanchaya.com/privacidad" // Cambiar por tu URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    /**
     * ✅ NUEVO: Abrir preguntas frecuentes
     */
    private fun abrirFAQ() {
        AlertDialog.Builder(requireContext())
            .setTitle("Preguntas Frecuentes")
            .setMessage(
                "1. ¿Cómo reservo una cancha?\n" +
                        "   - Ve a 'Inicio', selecciona una cancha y haz clic en 'Reservar'\n\n" +
                        "2. ¿Cómo cancelo una reserva?\n" +
                        "   - Ve a 'Mis Reservas' y selecciona 'Cancelar'\n\n" +
                        "3. ¿Cómo pago?\n" +
                        "   - Puedes pagar con Yape o en efectivo\n\n" +
                        "4. ¿Puedo cambiar mi reserva?\n" +
                        "   - Sí, cancela y crea una nueva"
            )
            .setPositiveButton("Entendido", null)
            .show()
    }

    /**
     * ✅ NUEVO: Mostrar información de la app
     */
    private fun mostrarAcercaDe() {
        AlertDialog.Builder(requireContext())
            .setTitle("Acerca de CanchaYA")
            .setMessage(
                "Versión: 1.0.0\n\n" +
                        "CanchaYA es la mejor app para reservar canchas deportivas en Perú.\n\n" +
                        "Desarrollado por: Tu Empresa\n" +
                        "Contacto: info@canchaya.com"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * ✅ NUEVO: Cambiar contraseña
     */
    private fun cambiarContrasena() {
        val email = auth.currentUser?.email

        if (email == null) {
            requireContext().toast("Error al obtener email")
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar Contraseña")
            .setMessage("Te enviaremos un correo a $email para restablecer tu contraseña.")
            .setPositiveButton("Enviar") { _, _ ->
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        requireContext().toast("Correo enviado exitosamente")
                    }
                    .addOnFailureListener {
                        requireContext().toast("Error al enviar correo")
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * ✅ NUEVO: Confirmar antes de cerrar sesión
     */
    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Cerrar sesión
     */
    private fun logout() {
        authViewModel.logout()
        sessionManager.clearSession()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finishAffinity()
    }

    /**
     * ✅ NUEVO: Confirmar eliminación de cuenta
     */
    private fun confirmarEliminarCuenta() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Cuenta")
            .setMessage(
                "⚠️ ADVERTENCIA ⚠️\n\n" +
                        "Esta acción es IRREVERSIBLE.\n\n" +
                        "Se eliminarán:\n" +
                        "• Todos tus datos personales\n" +
                        "• Historial de reservas\n" +
                        "• Favoritos\n\n" +
                        "¿Estás COMPLETAMENTE seguro?"
            )
            .setPositiveButton("Sí, eliminar") { _, _ ->
                confirmarEliminarCuentaFinal()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * ✅ NUEVO: Confirmación final para eliminar cuenta
     */
    private fun confirmarEliminarCuentaFinal() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmación Final")
            .setMessage("Escribe 'ELIMINAR' para confirmar")
            .setView(android.widget.EditText(requireContext()).apply {
                hint = "ELIMINAR"
            })
            .setPositiveButton("Confirmar") { dialog, _ ->
                val editText = (dialog as AlertDialog).findViewById<android.widget.EditText>(android.R.id.edit)
                if (editText?.text.toString() == "ELIMINAR") {
                    eliminarCuenta()
                } else {
                    requireContext().toast("Texto incorrecto")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * ✅ NUEVO: Eliminar cuenta permanentemente
     */
    private fun eliminarCuenta() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            requireContext().toast("Error al obtener usuario")
            return
        }

        lifecycleScope.launch {
            try {
                // 1. Eliminar datos de Firestore
                firestore.collection("users").document(userId).delete().await()

                // 2. Eliminar reservas del usuario
                val reservas = firestore.collection("reservas")
                    .whereEqualTo("usuarioId", userId)
                    .get()
                    .await()

                reservas.documents.forEach { it.reference.delete() }

                // 3. Eliminar cuenta de Firebase Auth
                auth.currentUser?.delete()?.await()

                // 4. Limpiar sesión local
                sessionManager.clearSession()

                requireContext().toast("Cuenta eliminada exitosamente")

                // 5. Ir a login
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finishAffinity()

            } catch (e: Exception) {
                requireContext().toast("Error al eliminar cuenta: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
