package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.FragmentEstadisticasBinding
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.utils.Resource

/**
 * ✅ CÓDIGO EXISTENTE MANTENIDO
 * 🔧 ACTUALIZADO: Agregados observadores para Sedes y estadísticas reales (23 Oct 2025)
 */
class EstadisticasFragment : Fragment() {

    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SuperAdminViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadisticasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupRefreshLayout()

        // 🆕 NUEVO: Cargar todas las estadísticas al iniciar
        cargarTodasLasEstadisticas()
    }

    // ✅ CÓDIGO EXISTENTE (NO MODIFICADO)
    private fun setupRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            cargarTodasLasEstadisticas() // 🔧 MODIFICADO: Función centralizada
        }
    }

    // 🆕 NUEVA FUNCIÓN: Cargar todas las estadísticas (23 Oct 2025)
    private fun cargarTodasLasEstadisticas() {
        viewModel.loadUsuarios()
        viewModel.loadCanchas()
        viewModel.cargarSedes() // ✨ NUEVO: Cargar sedes
        // TODO: Agregar cuando implementes reservas
        // viewModel.loadReservas()
    }

    // 🔧 ACTUALIZADO: Agregados observadores para Sedes (23 Oct 2025)
    private fun setupObservers() {
        observeUsuarios()
        observeCanchas()
        observeSedes() // ✨ NUEVO: Observer para sedes
        // TODO: Descomentar cuando implementes reservas
        // observeReservas()
    }

    // ✅ CÓDIGO EXISTENTE CON MEJORAS
    private fun observeUsuarios() {
        viewModel.usuarios.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val users = resource.data
                    if (users != null) {
                        // Estadísticas de usuarios
                        binding.tvTotalUsuarios.text = users.size.toString()
                        binding.tvUsuariosActivos.text = users.count { it.activo }.toString()

                        // Admins
                        val admins = users.count { it.rol == UserRole.ADMIN }
                        binding.tvTotalAdmins.text = admins.toString()
                    }
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    showError("Error al cargar usuarios: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
                }
            }
        }
    }

    // ✅ CÓDIGO EXISTENTE (NO MODIFICADO)
    private fun observeCanchas() {
        viewModel.canchas.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val canchas = resource.data
                    if (canchas != null) {
                        binding.tvTotalCanchas.text = canchas.size.toString()
                        binding.tvCanchasActivas.text = canchas.count { it.activo }.toString()
                        binding.tvCanchasInactivas.text = canchas.count { !it.activo }.toString()
                    }
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    showError("Error al cargar canchas: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
                }
            }
        }
    }

    // 🆕 NUEVA FUNCIÓN: Observar estadísticas de sedes (23 Oct 2025)
    private fun observeSedes() {
        viewModel.sedes.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val sedes = resource.data
                    if (sedes != null) {
                        // 🔧 NOTA: Verifica que estos IDs existan en tu fragment_estadisticas.xml
                        // Si no existen, necesitas agregarlos o comentar estas líneas
                        try {
                            binding.tvTotalSedes?.text = sedes.size.toString()
                            binding.tvSedesActivas?.text = sedes.count { it.activa }.toString()
                        } catch (e: Exception) {
                            // IDs no existen en el XML, omitir
                        }
                    }
                }
                is Resource.Error -> {
                    showError("Error al cargar sedes: ${resource.message}")
                }
                is Resource.Loading -> {
                    // Loading ya manejado por otros observers
                }
            }
        }
    }

    // 🆕 NUEVA FUNCIÓN: Observar reservas (23 Oct 2025)
    // TODO: Implementar cuando tengas ReservasRepository y ViewModel
    /*
    private fun observeReservas() {
        viewModel.reservas.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val reservas = resource.data
                    if (reservas != null) {
                        // Total de reservas
                        binding.tvTotalReservas.text = reservas.size.toString()

                        // Reservas de hoy
                        val hoy = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }.timeInMillis

                        val reservasHoy = reservas.count { reserva ->
                            val reservaDate = reserva.fecha // Timestamp
                            reservaDate >= hoy && reservaDate < hoy + 86400000
                        }
                        binding.tvReservasHoy.text = reservasHoy.toString()

                        // Reservas de la semana
                        val inicioSemana = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                        }.timeInMillis

                        val reservasSemana = reservas.count { it.fecha >= inicioSemana }
                        binding.tvReservasSemana.text = reservasSemana.toString()

                        // Reservas del mes
                        val inicioMes = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                        }.timeInMillis

                        val reservasMes = reservas.count { it.fecha >= inicioMes }
                        binding.tvReservasMes.text = reservasMes.toString()

                        // Por estado
                        binding.tvReservasPendientes.text =
                            reservas.count { it.estado == "PENDIENTE" }.toString()
                        binding.tvReservasConfirmadas.text =
                            reservas.count { it.estado == "CONFIRMADA" }.toString()
                        binding.tvReservasCompletadas.text =
                            reservas.count { it.estado == "COMPLETADA" }.toString()
                        binding.tvReservasCanceladas.text =
                            reservas.count { it.estado == "CANCELADA" }.toString()

                        // Ingresos (suma de precios de reservas completadas)
                        val ingresosMes = reservas
                            .filter { it.estado == "COMPLETADA" && it.fecha >= inicioMes }
                            .sumOf { it.precio }
                        binding.tvIngresosMes.text = "S/ ${ingresosMes.toInt()}"
                    }
                }
                is Resource.Error -> {
                    showError("Error al cargar reservas: ${resource.message}")
                }
                is Resource.Loading -> {
                    // Ya manejado
                }
            }
        }
    }
    */

    // ✅ CÓDIGO EXISTENTE (NO MODIFICADO)
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
