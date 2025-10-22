package com.rojassac.canchaya.ui.user.reservas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.EstadoReserva
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.databinding.FragmentDetalleReservaBinding
import com.rojassac.canchaya.ui.user.pagos.PagoFragment
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.toast
import java.text.SimpleDateFormat
import java.util.Locale
import com.rojassac.canchaya.utils.NotificationHelper

class DetalleReservaFragment : Fragment() {

    private var _binding: FragmentDetalleReservaBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private var reserva: Reserva? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reserva = it.getParcelable("reserva")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleReservaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reserva?.let { setupUI(it) } ?: run {
            requireContext().toast("Error al cargar reserva")
            requireActivity().onBackPressed()
        }
    }

    private fun setupUI(reserva: Reserva) {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            // Estado
            tvEstadoBadge.text = reserva.estado.toString().uppercase()
            tvEstadoBadge.setBackgroundResource(
                when (reserva.estado) {
                    EstadoReserva.PENDIENTE -> R.drawable.bg_badge_pendiente
                    EstadoReserva.CONFIRMADA -> R.drawable.bg_badge_confirmada
                    EstadoReserva.CANCELADA -> R.drawable.bg_badge_cancelada
                    EstadoReserva.COMPLETADA -> R.drawable.bg_badge_completada
                }
            )

            // Código
            tvCodigoReserva.text = "Código: #${reserva.id.take(8).uppercase()}"

            // Información
            tvCanchaNombre.text = reserva.canchaNombre
            tvFecha.text = formatearFecha(reserva.fecha)
            tvHorario.text = "${reserva.horaInicio} - ${reserva.horaFin}"

            // Pago
            tvPrecio.text = "S/ ${reserva.precio}"
            tvMetodoPago.text = reserva.metodoPago
            tvTotal.text = "S/ ${reserva.precio}"

            // Botones
            when (reserva.estado) {
                EstadoReserva.PENDIENTE -> {
                    btnPagar.visibility = View.VISIBLE
                    btnCancelar.visibility = View.VISIBLE
                }
                EstadoReserva.CONFIRMADA -> {
                    btnPagar.visibility = View.GONE
                    btnCancelar.visibility = View.VISIBLE
                }
                else -> {
                    btnPagar.visibility = View.GONE
                    btnCancelar.visibility = View.GONE
                }
            }

            btnPagar.setOnClickListener {
                mostrarPantallaPago(reserva)
            }

            btnCancelar.setOnClickListener {
                mostrarDialogoCancelar(reserva)
            }
        }
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "PE"))
            val date = inputFormat.parse(fecha)
            date?.let { outputFormat.format(it) } ?: fecha
        } catch (e: Exception) {
            fecha
        }
    }

    private fun mostrarPantallaPago(reserva: Reserva) {
        val pagoFragment = PagoFragment()
        val bundle = Bundle().apply {
            putParcelable("reserva", reserva)
        }
        pagoFragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, pagoFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmarPago(reserva: Reserva) {
        binding.progressBar.visibility = View.VISIBLE
        firestore.collection(Constants.RESERVAS_COLLECTION)
            .document(reserva.id)
            .update("estado", EstadoReserva.CONFIRMADA)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                requireContext().toast("¡Pago confirmado!")
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                requireContext().toast("Error: ${e.message}")
            }
    }

    private fun mostrarDialogoCancelar(reserva: Reserva) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar Reserva")
            .setMessage("¿Estás seguro de que deseas cancelar esta reserva? Esta acción no se puede deshacer.")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                cancelarReserva(reserva)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelarReserva(reserva: Reserva) {
        binding.progressBar.visibility = View.VISIBLE
        firestore.collection(Constants.RESERVAS_COLLECTION)
            .document(reserva.id)
            .update("estado", EstadoReserva.CANCELADA)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                // Enviar notificación
                NotificationHelper.showReservaCancelada(
                    requireContext(),
                    reserva.canchaNombre
                )
                requireContext().toast("Reserva cancelada")
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                requireContext().toast("Error: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(reserva: Reserva): DetalleReservaFragment {
            return DetalleReservaFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("reserva", reserva)
                }
            }
        }
    }
}
