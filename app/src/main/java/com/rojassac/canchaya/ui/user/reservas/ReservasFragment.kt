package com.rojassac.canchaya.ui.user.reservas

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.data.repository.ReservaRepository
import com.rojassac.canchaya.databinding.FragmentReservasBinding
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.toast
import kotlinx.coroutines.launch

class ReservasFragment : Fragment() {

    private var _binding: FragmentReservasBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var reservaAdapter: ReservaAdapter? = null
    private lateinit var reservaRepository: ReservaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reservaRepository = ReservaRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadReservas()
    }

    private fun setupRecyclerView() {
        // ✅ CORREGIDO: DOS CALLBACKS
        reservaAdapter = ReservaAdapter(
            onReservaClick = { reserva ->
                // Navegar al detalle de la reserva
                val fragment = DetalleReservaFragment.newInstance(reserva)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.navHostFragment, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onCancelarClick = { reserva ->
                mostrarDialogoCancelar(reserva)
            }
        )

        binding.rvReservas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reservaAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadReservas() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            requireContext().toast("Debes iniciar sesión")
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        firestore.collection(Constants.RESERVAS_COLLECTION)
            .whereEqualTo("usuarioId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                binding.progressBar.visibility = View.GONE

                if (snapshot.isEmpty) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvReservas.visibility = View.GONE
                    binding.tvEmpty.text = "No tienes reservas aún.\n¡Explora y reserva tu cancha!"
                } else {
                    val reservas = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Reserva::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.fechaCreacion }

                    binding.tvEmpty.visibility = View.GONE
                    binding.rvReservas.visibility = View.VISIBLE
                    reservaAdapter?.submitList(reservas)
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                requireContext().toast("Error: ${e.message}")
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text = "Error al cargar reservas"
            }
    }

    private fun mostrarDialogoCancelar(reserva: Reserva) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar Reserva")
            .setMessage("¿Estás seguro de cancelar la reserva en ${reserva.canchaNombre}?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                cancelarReserva(reserva)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelarReserva(reserva: Reserva) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resultado = reservaRepository.cancelarReserva(
                    reservaId = reserva.id,
                    canchaNombre = reserva.canchaNombre
                )

                if (resultado.isSuccess) {
                    requireContext().toast("Reserva cancelada")
                    loadReservas()
                } else {
                    requireContext().toast("Error al cancelar")
                }
            } catch (e: Exception) {
                requireContext().toast("Error: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadReservas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        reservaAdapter = null
    }
}
