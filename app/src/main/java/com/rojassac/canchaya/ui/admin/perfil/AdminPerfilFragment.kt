package com.rojassac.canchaya.ui.admin.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.DialogCodigoInvitacionBinding
import com.rojassac.canchaya.databinding.FragmentAdminPerfilBinding
import com.rojassac.canchaya.ui.admin.AdminViewModel
import com.rojassac.canchaya.ui.admin.canchas.AdminCanchasFragment
import com.rojassac.canchaya.ui.admin.dashboard.AdminDashboardFragment
import com.rojassac.canchaya.ui.admin.resenas.AdminResenasFragment
import com.rojassac.canchaya.ui.admin.suscripciones.SuscripcionFragment
import com.rojassac.canchaya.ui.admin.usuarios.AdminUsuariosFragment
import com.rojassac.canchaya.ui.auth.LoginActivity
import com.rojassac.canchaya.utils.SessionManager
import kotlinx.coroutines.launch

class AdminPerfilFragment : Fragment() {
    private var _binding: FragmentAdminPerfilBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels()

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AdminPerfilAdapter
    private var codigoDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        setupUI()
        setupRecyclerView()
        setupObservers() // ✅ NUEVO: Configurar observers
        loadUserData()
    }

    private fun setupUI() {
        // Cargar datos del usuario desde SessionManager
        binding.tvUserName.text = sessionManager.getUserName() ?: "Admin Usuario"
        binding.tvUserEmail.text = sessionManager.getUserEmail() ?: "admin@canchaya.com"

        // Mostrar contador de canchas
        val canchasCount = sessionManager.contarCanchasAsignadas()
        binding.tvCanchasCount.text = canchasCount.toString()
    }

    // ========== ✅ NUEVO: SETUP DE OBSERVERS ==========
    // ========== ✅ NUEVO: SETUP DE OBSERVERS ==========
    private fun setupObservers() {
        // Observer para resultado de validación de código
        viewModel.validacionCodigo.observe(viewLifecycleOwner) { resultado ->
            resultado?.let {
                if (it.exito) {
                    // ✅ Validación exitosa
                    Toast.makeText(
                        requireContext(),
                        it.mensaje,
                        Toast.LENGTH_LONG
                    ).show()

                    // Cerrar diálogo
                    codigoDialog?.dismiss()

                    // 🔵 AGREGAR: Recargar sesión desde Firebase
                    lifecycleScope.launch {
                        try {
                            // Esperar 500ms para que Firebase confirme la escritura
                            kotlinx.coroutines.delay(500)

                            // Recargar datos desde Firebase
                            sessionManager.reloadUserData()

                            // Actualizar UI
                            setupUI()
                            loadUserData()

                            Toast.makeText(
                                requireContext(),
                                "✅ Cancha asignada y sesión actualizada",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Error al recargar datos: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    // Reset del estado
                    viewModel.resetValidacionCodigo()

                } else {
                    // ❌ Error en validación
                    Toast.makeText(
                        requireContext(),
                        it.mensaje,
                        Toast.LENGTH_LONG
                    ).show()

                    // Reset del estado
                    viewModel.resetValidacionCodigo()

                    // Rehabilitar el diálogo para reintentar
                    codigoDialog?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCodigo)?.isEnabled = true
                    codigoDialog?.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnValidar)?.isEnabled = true
                }
            }
        }

        // Observer para estado de carga
        viewModel.isValidatingCodigo.observe(viewLifecycleOwner) { isValidating ->
            codigoDialog?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCodigo)?.isEnabled = !isValidating
            codigoDialog?.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnValidar)?.apply {
                isEnabled = !isValidating
                text = if (isValidating) "Validando..." else "Validar"
            }
        }
    }

    private fun setupRecyclerView() {
        val menuItems = listOf(
            AdminPerfilMenuItem(
                id = 1,
                icon = R.drawable.ic_cancha,
                titulo = "Mis Canchas",
                descripcion = "Ver canchas asignadas"
            ),
            AdminPerfilMenuItem(
                id = 2,
                icon = R.drawable.ic_add,
                titulo = "Código de Invitación",
                descripcion = "Agregar nueva cancha"
            ),
            AdminPerfilMenuItem(
                id = 10,
                icon = R.drawable.ic_suscripcion,
                titulo = "Mi Suscripción",
                descripcion = "Gestionar plan y pagos"
            ),
            AdminPerfilMenuItem(
                id = 3,
                icon = R.drawable.ic_home,
                titulo = "Dashboard",
                descripcion = "Estadísticas generales"
            ),
            AdminPerfilMenuItem(
                id = 4,
                icon = R.drawable.ic_person,
                titulo = "Usuarios",
                descripcion = "Gestionar usuarios"
            ),
            AdminPerfilMenuItem(
                id = 5,
                icon = R.drawable.ic_star,
                titulo = "Reseñas",
                descripcion = "Ver opiniones"
            ),
            AdminPerfilMenuItem(
                id = 6,
                icon = R.drawable.ic_notification,
                titulo = "Notificaciones",
                descripcion = "Configurar alertas"
            ),
            AdminPerfilMenuItem(
                id = 7,
                icon = R.drawable.ic_settings,
                titulo = "Configuración",
                descripcion = "Ajustes de cuenta"
            ),
            AdminPerfilMenuItem(
                id = 8,
                icon = R.drawable.ic_help,
                titulo = "Ayuda",
                descripcion = "Soporte técnico"
            ),
            AdminPerfilMenuItem(
                id = 9,
                icon = R.drawable.ic_logout,
                titulo = "Cerrar Sesión",
                descripcion = "Salir"
            )
        )

        adapter = AdminPerfilAdapter(menuItems) { menuItem ->
            handleMenuClick(menuItem)
        }

        binding.rvOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOptions.adapter = adapter
    }

    private fun handleMenuClick(menuItem: AdminPerfilMenuItem) {
        when (menuItem.id) {
            1 -> navigateToMisCanchas()
            2 -> showCodigoInvitacionDialog()
            3 -> navigateToDashboard()
            4 -> navigateToUsuarios()
            5 -> navigateToResenas()
            6 -> navigateToNotificaciones()
            7 -> navigateToConfiguracion()
            8 -> navigateToAyuda()
            9 -> showLogoutDialog()
            10 -> showSuscripcionDialog()
        }
    }

    // ========== 🔵 MODIFICADO: FUNCIÓN DE VALIDACIÓN DE CÓDIGO ==========
    private fun showCodigoInvitacionDialog() {
        val dialogBinding = DialogCodigoInvitacionBinding.inflate(layoutInflater)

        codigoDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialogBinding.btnCancelar.setOnClickListener {
            codigoDialog?.dismiss()
        }

        // 🔵 MODIFICADO: Implementación real de validación
        dialogBinding.btnValidar.setOnClickListener {
            val codigo = dialogBinding.etCodigo.text.toString().trim().uppercase()

            if (codigo.isEmpty()) {
                dialogBinding.tilCodigo.error = "Ingresa un código"
                return@setOnClickListener
            }

            // Limpiar error
            dialogBinding.tilCodigo.error = null

            // Deshabilitar inputs mientras valida
            dialogBinding.etCodigo.isEnabled = false
            dialogBinding.btnValidar.isEnabled = false
            dialogBinding.btnValidar.text = "Validando..."

            // ✅ NUEVO: Llamar al ViewModel para validar
            viewModel.validarCodigoInvitacion(codigo)
        }
        // ========== FIN MODIFICADO ==========

        codigoDialog?.show()
    }

    private fun showSuscripcionDialog() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, SuscripcionFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        if (userId != null) {
            // TODO: Cargar canchas asignadas si es necesario
        }
    }

    private fun navigateToMisCanchas() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, AdminCanchasFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToDashboard() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, AdminDashboardFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToUsuarios() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, AdminUsuariosFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToResenas() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, AdminResenasFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToNotificaciones() {
        Toast.makeText(requireContext(), "Notificaciones - Próximamente", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToConfiguracion() {
        Toast.makeText(requireContext(), "Configuración - Próximamente", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAyuda() {
        Toast.makeText(requireContext(), "Ayuda - Próximamente", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí, cerrar") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        sessionManager.clearSession()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        codigoDialog?.dismiss()
        _binding = null
    }
}
