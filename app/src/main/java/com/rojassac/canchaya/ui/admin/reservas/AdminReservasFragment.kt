package com.rojassac.canchaya.ui.admin.reservas

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.EstadoReserva
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.databinding.FragmentAdminReservasBinding
import com.rojassac.canchaya.utils.SessionManager

class AdminReservasFragment : Fragment() {

    private var _binding: FragmentAdminReservasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReservasViewModel by viewModels()
    private lateinit var adapter: ReservasAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminReservasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        setupRecyclerView()
        setupChipFilters()
        setupObservers()

        cargarReservas()
    }

    private fun setupRecyclerView() {
        adapter = ReservasAdapter { reserva ->
            mostrarDetallesReserva(reserva)
        }

        binding.rvReservas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminReservasFragment.adapter
        }
    }

    private fun setupChipFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { group, _ ->
            val selectedChipId = group.checkedChipId

            val filtro = when (selectedChipId) {
                R.id.chipPendientes -> EstadoReserva.PENDIENTE
                R.id.chipConfirmadas -> EstadoReserva.CONFIRMADA
                R.id.chipCompletadas -> EstadoReserva.COMPLETADA
                R.id.chipCanceladas -> EstadoReserva.CANCELADA
                else -> null // Todas
            }

            viewModel.filtrarPorEstado(filtro)
        }
    }

    private fun setupObservers() {
        viewModel.reservas.observe(viewLifecycleOwner) { reservas ->
            adapter.submitList(reservas)

            // Mostrar/Ocultar empty state
            if (reservas.isEmpty()) {
                binding.rvReservas.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvReservas.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
            }

            actualizarContadores()
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

    private fun cargarReservas() {
        val canchaId = sessionManager.getPrimeraCanchaAsignada()  // ✅ CORRECCIÓN AQUÍ
        if (canchaId != null) {
            viewModel.cargarReservas(canchaId)
        } else {
            Toast.makeText(requireContext(), "No hay cancha asignada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarContadores() {
        // Actualizar badges en chips
        actualizarBadge(R.id.chipPendientes, viewModel.contarPorEstado(EstadoReserva.PENDIENTE))
        actualizarBadge(R.id.chipConfirmadas, viewModel.contarPorEstado(EstadoReserva.CONFIRMADA))
        actualizarBadge(R.id.chipCompletadas, viewModel.contarPorEstado(EstadoReserva.COMPLETADA))
        actualizarBadge(R.id.chipCanceladas, viewModel.contarPorEstado(EstadoReserva.CANCELADA))
    }

    private fun actualizarBadge(chipId: Int, count: Int) {
        val chip = binding.chipGroupFiltros.findViewById<Chip>(chipId)
        chip?.text = when (chipId) {
            R.id.chipPendientes -> "Pendientes ($count)"
            R.id.chipConfirmadas -> "Confirmadas ($count)"
            R.id.chipCompletadas -> "Completadas ($count)"
            R.id.chipCanceladas -> "Canceladas ($count)"
            else -> chip.text
        }
    }

    private fun mostrarDetallesReserva(reserva: Reserva) {
        val opciones = when (reserva.estado) {
            EstadoReserva.PENDIENTE -> arrayOf("Confirmar", "Cancelar", "Ver detalles")
            EstadoReserva.CONFIRMADA -> arrayOf("Completar", "Cancelar", "Ver detalles")
            EstadoReserva.COMPLETADA, EstadoReserva.CANCELADA -> arrayOf("Ver detalles")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Reserva de ${reserva.usuarioNombre}")
            .setItems(opciones) { _, which ->
                when (opciones[which]) {
                    "Confirmar" -> confirmarReserva(reserva)
                    "Completar" -> completarReserva(reserva)
                    "Cancelar" -> cancelarReserva(reserva)
                    "Ver detalles" -> verDetallesCompletos(reserva)
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun confirmarReserva(reserva: Reserva) {
        sessionManager.getPrimeraCanchaAsignada()?.let { canchaId ->  // ✅ CORRECCIÓN AQUÍ
            viewModel.actualizarEstado(reserva.id, EstadoReserva.CONFIRMADA, canchaId)
            Toast.makeText(requireContext(), "Reserva confirmada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun completarReserva(reserva: Reserva) {
        sessionManager.getPrimeraCanchaAsignada()?.let { canchaId ->  // ✅ CORRECCIÓN AQUÍ
            viewModel.actualizarEstado(reserva.id, EstadoReserva.COMPLETADA, canchaId)
            Toast.makeText(requireContext(), "Reserva completada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelarReserva(reserva: Reserva) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar reserva")
            .setMessage("¿Estás seguro de cancelar esta reserva?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                sessionManager.getPrimeraCanchaAsignada()?.let { canchaId ->  // ✅ CORRECCIÓN AQUÍ
                    viewModel.actualizarEstado(reserva.id, EstadoReserva.CANCELADA, canchaId)
                    Toast.makeText(requireContext(), "Reserva cancelada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun verDetallesCompletos(reserva: Reserva) {
        val detalles = """
            Cliente: ${reserva.usuarioNombre}
            Teléfono: ${reserva.usuarioCelular}
            
            Fecha: ${reserva.fecha}
            Horario: ${reserva.horaInicio} - ${reserva.horaFin}
            
            Precio: S/ ${String.format("%.2f", reserva.precio)}
            Método de pago: ${reserva.metodoPago}
            
            Estado: ${reserva.estado.name}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Detalles de la reserva")
            .setMessage(detalles)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
