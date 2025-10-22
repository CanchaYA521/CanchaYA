package com.rojassac.canchaya.ui.admin.horarios

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.rojassac.canchaya.databinding.FragmentAdminHorariosBinding
import com.rojassac.canchaya.data.model.HorarioSlot  // ✅ IMPORTA DESDE data.model
import com.rojassac.canchaya.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminHorariosFragment : Fragment() {

    private var _binding: FragmentAdminHorariosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HorariosViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: HorariosAdapter

    private var currentDate: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHorariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        setupUI()
        setupRecyclerView()
        observeViewModel()

        // ✅ CARGAR HORARIOS AL INICIO
        loadHorarios()
    }

    private fun setupUI() {
        // Mostrar fecha actual
        updateDateDisplay()

        // Botón fecha anterior
        binding.btnPreviousDay.setOnClickListener {
            currentDate.add(Calendar.DAY_OF_MONTH, -1)
            updateDateDisplay()
            loadHorarios()
        }

        // Botón fecha siguiente
        binding.btnNextDay.setOnClickListener {
            currentDate.add(Calendar.DAY_OF_MONTH, 1)
            updateDateDisplay()
            loadHorarios()
        }
    }

    private fun updateDateDisplay() {
        binding.tvSelectedDate.text = dateFormat.format(currentDate.time)
    }

    private fun setupRecyclerView() {
        adapter = HorariosAdapter { horario ->
            toggleHorarioEstado(horario)
        }

        binding.rvHorarios.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvHorarios.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.horarios.observe(viewLifecycleOwner) { horarios ->
            adapter.submitList(horarios)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHorarios() {
        lifecycleScope.launch {
            try {
                // ✅ 1. RECARGAR SESIÓN DESDE FIRESTORE
                Log.d("AdminHorarios", "=== RECARGANDO SESIÓN ===")
                sessionManager.reloadUserData()

                // ✅ 2. OBTENER CANCHAS DESPUÉS DE RECARGAR
                val canchaId = sessionManager.getPrimeraCanchaAsignada()

                // ✅ 3. LOGS DE DEBUG
                Log.d("AdminHorarios", "Usuario ID: ${sessionManager.getUserId()}")
                Log.d("AdminHorarios", "Canchas asignadas: ${sessionManager.getCanchasAsignadas()}")
                Log.d("AdminHorarios", "Primera cancha: $canchaId")

                if (canchaId != null) {
                    val fechaSeleccionada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(currentDate.time)

                    Log.d("AdminHorarios", "Cargando horarios para: $canchaId, fecha: $fechaSeleccionada")
                    viewModel.cargarHorarios(canchaId, fechaSeleccionada)

                } else {
                    Log.e("AdminHorarios", "No hay canchas asignadas")
                    Toast.makeText(
                        requireContext(),
                        "No tienes canchas asignadas",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("AdminHorarios", "Error al cargar horarios", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun toggleHorarioEstado(horario: HorarioSlot) {
        val canchaId = sessionManager.getPrimeraCanchaAsignada()
        if (canchaId != null) {
            val fechaSeleccionada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(currentDate.time)

            viewModel.actualizarEstadoHorario(canchaId, fechaSeleccionada, horario)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ❌ ELIMINA ESTA data class - YA EXISTE EN data/model/Horario.kt
