package com.rojassac.canchaya.ui.superadmin.fragments

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.databinding.FragmentInformacionSistemaBinding
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ CORREGIDO (24 Oct 2025)
 * Fragment para mostrar información del sistema y estadísticas generales
 */
class InformacionSistemaFragment : Fragment() {

    private var _binding: FragmentInformacionSistemaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SuperAdminViewModel
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformacionSistemaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModel manualmente
        viewModel = ViewModelProvider(requireActivity())[SuperAdminViewModel::class.java]

        setupToolbar()
        cargarInformacionApp()
        cargarEstadisticas()
        setupRefresh()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun cargarInformacionApp() {
        try {
            // Información de la app
            val packageInfo = requireContext().packageManager.getPackageInfo(
                requireContext().packageName,
                0
            )

            val versionName = packageInfo.versionName ?: "N/A"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }

            binding.apply {
                tvAppVersion.text = "Versión: $versionName"
                tvAppVersionCode.text = "Build: $versionCode"
                tvAppName.text = packageInfo.applicationInfo?.loadLabel(requireContext().packageManager).toString()
                tvPackageName.text = "Package: ${requireContext().packageName}"

                // Fecha de instalación
                val fechaInstalacion = Date(packageInfo.firstInstallTime)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                tvFechaInstalacion.text = "Instalado: ${dateFormat.format(fechaInstalacion)}"

                // Información del dispositivo
                tvAndroidVersion.text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
                tvModelo.text = "${Build.MANUFACTURER} ${Build.MODEL}"
                tvArquitectura.text = Build.SUPPORTED_ABIS.firstOrNull() ?: "N/A"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun cargarEstadisticas() {
        mostrarCargando(true)

        // Cargar estadísticas desde Firestore
        cargarEstadisticasUsuarios()
        cargarEstadisticasSedes()
        cargarEstadisticasReservas()
        cargarEstadisticasPlanes()
    }

    private fun cargarEstadisticasUsuarios() {
        firestore.collection("users").get()
            .addOnSuccessListener { snapshot ->
                val totalUsuarios = snapshot.size()
                val adminCount = snapshot.documents.count {
                    it.getString("rol") == "ADMIN" || it.getString("role") == "ADMIN"
                }
                val usuariosActivos = snapshot.documents.count {
                    it.getBoolean("activo") == true
                }

                binding.apply {
                    tvTotalUsuarios.text = totalUsuarios.toString()
                    tvUsuariosActivos.text = "$usuariosActivos activos"
                    tvAdministradores.text = "$adminCount admins"
                }
            }
            .addOnFailureListener {
                binding.tvTotalUsuarios.text = "Error"
            }
    }

    private fun cargarEstadisticasSedes() {
        firestore.collection("sedes").get()
            .addOnSuccessListener { snapshot ->
                val totalSedes = snapshot.size()
                val sedesActivas = snapshot.documents.count {
                    it.getBoolean("activo") == true
                }

                binding.apply {
                    tvTotalSedes.text = totalSedes.toString()
                    tvSedesActivas.text = "$sedesActivas activas"
                }

                mostrarCargando(false)
            }
            .addOnFailureListener {
                binding.tvTotalSedes.text = "Error"
                mostrarCargando(false)
            }
    }

    private fun cargarEstadisticasReservas() {
        firestore.collection("reservas").get()
            .addOnSuccessListener { snapshot ->
                val totalReservas = snapshot.size()
                val reservasHoy = snapshot.documents.count { doc ->
                    val fecha = doc.getDate("fechaReserva")
                    fecha != null && esHoy(fecha)
                }

                binding.apply {
                    tvTotalReservas.text = totalReservas.toString()
                    tvReservasHoy.text = "$reservasHoy hoy"
                }
            }
            .addOnFailureListener {
                binding.tvTotalReservas.text = "Error"
            }
    }

    private fun cargarEstadisticasPlanes() {
        firestore.collection("subscriptions").get()
            .addOnSuccessListener { snapshot ->
                val totalSuscripciones = snapshot.size()
                val suscripcionesActivas = snapshot.documents.count {
                    it.getString("estado") == "activa"
                }

                binding.apply {
                    tvTotalSuscripciones.text = totalSuscripciones.toString()
                    tvSuscripcionesActivas.text = "$suscripcionesActivas activas"
                }
            }
            .addOnFailureListener {
                binding.tvTotalSuscripciones.text = "Error"
            }
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            cargarEstadisticas()
            cargarInformacionApp()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    private fun esHoy(fecha: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = fecha }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
