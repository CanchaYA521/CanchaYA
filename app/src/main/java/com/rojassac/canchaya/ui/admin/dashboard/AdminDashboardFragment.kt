package com.rojassac.canchaya.ui.admin.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.databinding.FragmentAdminPlaceholderBinding
import com.rojassac.canchaya.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminPlaceholderBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPlaceholderBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarInformacionAdmin()
    }

    private fun cargarInformacionAdmin() {
        // ✅ CRÍTICO: Usar viewLifecycleOwner.lifecycleScope en lugar de lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val managementInfo = sessionManager.getManagementInfo()

                // ✅ VERIFICAR que el binding aún existe antes de usarlo
                if (!isAdded || _binding == null) return@launch

                if (managementInfo != null) {
                    val texto = StringBuilder()
                    texto.append("Dashboard Estadísticas\n\n")

                    if (managementInfo.tipoAdministracion == "sede") {
                        // Administrador de Sede
                        val sedeDoc = db.collection("sedes")
                            .document(managementInfo.sedeId!!)
                            .get()
                            .await()

                        // ✅ VERIFICAR nuevamente después de await
                        if (!isAdded || _binding == null) return@launch

                        val nombreSede = sedeDoc.getString("nombre") ?: "Sin nombre"
                        val totalCanchas = sedeDoc.getLong("totalCanchas") ?: 0

                        texto.append("Administras: Sede\n")
                        texto.append("Nombre: $nombreSede\n")
                        texto.append("Canchas: $totalCanchas\n\n")

                    } else if (managementInfo.tipoAdministracion == "cancha_individual") {
                        // Administrador de Cancha Individual
                        val canchaDoc = db.collection("canchas")
                            .document(managementInfo.canchaId!!)
                            .get()
                            .await()

                        // ✅ VERIFICAR nuevamente después de await
                        if (!isAdded || _binding == null) return@launch

                        val nombreCancha = canchaDoc.getString("nombre") ?: "Sin nombre"
                        val precioHora = canchaDoc.getDouble("precioHora") ?: 0.0

                        texto.append("Administras: Cancha Individual\n")
                        texto.append("Nombre: $nombreCancha\n")
                        texto.append("Precio: S/ $precioHora/hora\n\n")
                    }

                    texto.append("Próximamente más estadísticas...")

                    // ✅ VERIFICACIÓN FINAL antes de actualizar UI
                    if (isAdded && _binding != null) {
                        binding.tvPlaceholder.text = texto.toString()
                    }
                } else {
                    // ✅ VERIFICACIÓN antes de actualizar UI
                    if (isAdded && _binding != null) {
                        binding.tvPlaceholder.text = "Dashboard Estadísticas\n\nPróximamente..."
                    }
                }
            } catch (e: Exception) {
                // ✅ VERIFICACIÓN antes de actualizar UI
                if (isAdded && _binding != null) {
                    binding.tvPlaceholder.text = "Dashboard Estadísticas\n\nError al cargar información"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
