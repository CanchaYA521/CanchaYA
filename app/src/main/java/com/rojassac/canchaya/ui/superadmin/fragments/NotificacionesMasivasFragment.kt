package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.EstadoNotificacion
import com.rojassac.canchaya.data.model.NotificacionMasiva
import com.rojassac.canchaya.data.repository.NotificacionRepository
import com.rojassac.canchaya.databinding.FragmentNotificacionesMasivasBinding
import com.rojassac.canchaya.ui.superadmin.NotificacionViewModel
import com.rojassac.canchaya.ui.superadmin.adapters.NotificacionAdapter
import com.rojassac.canchaya.utils.Resource

/**
 * ✅ NUEVO (24 Oct 2025)
 * Fragment para gestionar notificaciones masivas
 * SIN HILT - Inyección manual
 */
class NotificacionesMasivasFragment : Fragment() {

    private var _binding: FragmentNotificacionesMasivasBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificacionViewModel
    private lateinit var notificacionAdapter: NotificacionAdapter
    private var filtroActual: EstadoNotificacion? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificacionesMasivasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModel manualmente
        val repository = NotificacionRepository(FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return NotificacionViewModel(repository) as T
                }
            }
        )[NotificacionViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupListeners()
        setupObservers()

        // Cargar notificaciones
        viewModel.cargarNotificaciones()
        viewModel.cargarEstadisticasGenerales()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        notificacionAdapter = NotificacionAdapter(
            onItemClick = { notificacion ->
                mostrarDetalleNotificacion(notificacion)
            },
            onCancelarClick = { notificacion ->
                confirmarCancelacion(notificacion)
            },
            onDuplicarClick = { notificacion ->
                duplicarNotificacion(notificacion)
            }
        )

        binding.recyclerView.apply {
            adapter = notificacionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Todas"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Enviadas"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Programadas"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Pendientes"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Canceladas"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        filtroActual = null
                        viewModel.cargarNotificaciones()
                    }
                    1 -> {
                        filtroActual = EstadoNotificacion.ENVIADA
                        viewModel.cargarNotificacionesPorEstado(EstadoNotificacion.ENVIADA)
                    }
                    2 -> {
                        filtroActual = EstadoNotificacion.PROGRAMADA
                        viewModel.cargarNotificacionesProgramadas()
                    }
                    3 -> {
                        filtroActual = EstadoNotificacion.PENDIENTE
                        viewModel.cargarNotificacionesPorEstado(EstadoNotificacion.PENDIENTE)
                    }
                    4 -> {
                        filtroActual = EstadoNotificacion.CANCELADA
                        viewModel.cargarNotificacionesPorEstado(EstadoNotificacion.CANCELADA)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        // Botón crear nueva notificación
        binding.fabCrear.setOnClickListener {
            navegarACrearNotificacion()
        }

        // Búsqueda
        binding.searchView.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        if (it.isNotEmpty()) {
                            viewModel.buscarNotificaciones(it)
                        } else {
                            viewModel.cargarNotificaciones()
                        }
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        viewModel.cargarNotificaciones()
                    }
                    return true
                }
            }
        )

        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            if (filtroActual != null) {
                viewModel.cargarNotificacionesPorEstado(filtroActual!!)
            } else {
                viewModel.cargarNotificaciones()
            }
            viewModel.cargarEstadisticasGenerales()
        }
    }

    private fun setupObservers() {
        // Observar lista de notificaciones
        viewModel.notificaciones.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    resource.data?.let { notificaciones ->
                        if (notificaciones.isEmpty()) {
                            mostrarEstadoVacio()
                        } else {
                            ocultarEstadoVacio()
                            notificacionAdapter.submitList(notificaciones)
                        }
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    showError(resource.message ?: "Error al cargar notificaciones")
                }
            }
        }

        // Observar estadísticas generales
        viewModel.estadisticasGenerales.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { stats ->
                        mostrarEstadisticas(stats)
                    }
                }
                is Resource.Error -> {
                    // Opcional: mostrar error de estadísticas
                }
                else -> {}
            }
        }

        // Observar resultado de cancelación
        viewModel.cancelarNotificacionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Mostrar loading
                }
                is Resource.Success -> {
                    showSuccess("Notificación cancelada correctamente")
                }
                is Resource.Error -> {
                    showError(resource.message ?: "Error al cancelar notificación")
                }
            }
        }
    }

    private fun mostrarEstadisticas(stats: Map<String, Any>) {
        binding.apply {
            tvTotalNotificaciones.text = stats["totalNotificaciones"]?.toString() ?: "0"
            tvTotalEnviadas.text = stats["totalEnviadas"]?.toString() ?: "0"
            tvTotalProgramadas.text = stats["totalProgramadas"]?.toString() ?: "0"
            tvTotalDestinatarios.text = stats["totalDestinatarios"]?.toString() ?: "0"

            // Calcular tasa de apertura si hay envíos
            val totalEnvios = (stats["totalEnvios"] as? Number)?.toInt() ?: 0
            val totalVistas = (stats["totalVistas"] as? Number)?.toInt() ?: 0

            if (totalEnvios > 0) {
                val tasaApertura = (totalVistas.toDouble() / totalEnvios * 100).toInt()
                tvTasaApertura.text = "$tasaApertura%"
            } else {
                tvTasaApertura.text = "0%"
            }
        }
    }

    private fun mostrarDetalleNotificacion(notificacion: NotificacionMasiva) {
        // TODO: Crear DetalleNotificacionFragment más adelante
        Toast.makeText(
            requireContext(),
            "Ver detalle: ${notificacion.titulo}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun confirmarCancelacion(notificacion: NotificacionMasiva) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancelar notificación")
            .setMessage("¿Estás seguro de que deseas cancelar esta notificación programada?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                viewModel.cancelarNotificacion(notificacion.id)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun duplicarNotificacion(notificacion: NotificacionMasiva) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Duplicar notificación")
            .setMessage("Se creará una copia de esta notificación que podrás editar y enviar.")
            .setPositiveButton("Duplicar") { _, _ ->
                viewModel.duplicarNotificacion(notificacion.id)
                showSuccess("Notificación duplicada correctamente")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navegarACrearNotificacion() {
        val fragment = CrearNotificacionFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun mostrarEstadoVacio() {
        binding.recyclerView.visibility = View.GONE
        binding.layoutVacio.visibility = View.VISIBLE
    }

    private fun ocultarEstadoVacio() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.layoutVacio.visibility = View.GONE
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
