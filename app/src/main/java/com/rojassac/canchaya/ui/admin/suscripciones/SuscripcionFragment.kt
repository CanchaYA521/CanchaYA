package com.rojassac.canchaya.ui.admin.suscripciones

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.databinding.FragmentSuscripcionBinding
import com.rojassac.canchaya.ui.admin.AdminViewModel
import com.rojassac.canchaya.ui.admin.perfil.PlanesAdapter
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class SuscripcionFragment : Fragment() {

    private var _binding: FragmentSuscripcionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var planesAdapter: PlanesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuscripcionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupUI()
        setupRecyclerView()
        observeViewModel()
        loadData()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.plan_selected_bounce)
            it.startAnimation(anim)
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        planesAdapter = PlanesAdapter(
            planes = emptyList(),
            planActualId = null,
            onPlanSelected = { plan ->
                animateCardSelection(plan)
            }
        )

        binding.rvPlanes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = planesAdapter
            setHasFixedSize(true)
        }
    }

    private fun animateCardSelection(plan: Plan) {
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.plan_selected_bounce)
        binding.rvPlanes.startAnimation(anim)

        binding.rvPlanes.postDelayed({
            showConfirmacionCambioPlan(plan, sessionManager.getUserId() ?: "")
        }, 200)
    }

    private fun observeViewModel() {
        val userId = sessionManager.getUserId() ?: return

        // Observar planes
        viewModel.planes.observe(viewLifecycleOwner) { listaPlanes ->
            if (listaPlanes.isNotEmpty()) {
                binding.progressBar.visibility = View.GONE
                binding.rvPlanes.visibility = View.VISIBLE

                val planActualId = viewModel.suscripcionActual.value?.planId ?: Constants.PLAN_BASICO

                planesAdapter = PlanesAdapter(
                    planes = listaPlanes,
                    planActualId = planActualId,
                    onPlanSelected = { plan ->
                        animateCardSelection(plan)
                    }
                )
                binding.rvPlanes.adapter = planesAdapter
            }
        }

        // Observar suscripción actual
        viewModel.suscripcionActual.observe(viewLifecycleOwner) { suscripcion ->
            if (suscripcion != null) {
                binding.cardPlanActual.visibility = View.VISIBLE

                when (suscripcion.estado.name) {
                    "ACTIVA" -> {
                        binding.tvEstado.text = "✅ ACTIVA"
                        binding.tvEstado.setBackgroundColor(requireContext().getColor(R.color.success_light))
                        binding.tvEstado.setTextColor(requireContext().getColor(R.color.success_dark))
                    }
                    "VENCIDA" -> {
                        binding.tvEstado.text = "⚠️ VENCIDA"
                        binding.tvEstado.setBackgroundColor(requireContext().getColor(R.color.yellow))
                    }
                    "CANCELADA" -> {
                        binding.tvEstado.text = "❌ CANCELADA"
                        binding.tvEstado.setBackgroundColor(requireContext().getColor(R.color.red_error))
                    }
                }

                if (suscripcion.fechaVencimiento > 0) {
                    val fecha = SimpleDateFormat("dd 'de' MMM, yyyy", Locale("es"))
                        .format(Date(suscripcion.fechaVencimiento))
                    binding.tvVencimiento.text = "Vence: $fecha"
                    binding.tvVencimiento.visibility = View.VISIBLE
                } else {
                    binding.tvVencimiento.visibility = View.GONE
                }

                binding.btnCancelarSuscripcion.visibility =
                    if (suscripcion.planId != Constants.PLAN_BASICO) View.VISIBLE else View.GONE

            } else {
                binding.cardPlanActual.visibility = View.VISIBLE
                binding.tvNombrePlanActual.text = "Básico"
                binding.tvPrecioPlanActual.text = "GRATIS"
                binding.tvComisionActual.text = "40% de comisión"
                binding.tvEstado.text = "GRATIS"
                binding.tvVencimiento.visibility = View.GONE
                binding.btnCancelarSuscripcion.visibility = View.GONE
            }
        }

        // Observar plan actual
        viewModel.planActual.observe(viewLifecycleOwner) { plan ->
            plan?.let {
                binding.tvNombrePlanActual.text = it.nombre

                if (it.precio == 0.0) {
                    binding.tvPrecioPlanActual.text = "GRATIS"
                } else {
                    binding.tvPrecioPlanActual.text = "S/ ${it.precio.toInt()} / mes"
                }

                binding.tvComisionActual.text = "${(it.comision * 100).toInt()}% de comisión"
            }
        }

        // ✅ NUEVO: Observar resultado del cambio de plan
        viewModel.cambioPlanExitoso.observe(viewLifecycleOwner) { exitoso ->
            binding.progressBar.visibility = View.GONE

            if (exitoso) {
                Toast.makeText(
                    requireContext(),
                    "✅ Plan actualizado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                // Recargar datos
                viewModel.cargarPlanes()
                viewModel.cargarSuscripcionActual(userId)
            } else {
                Toast.makeText(
                    requireContext(),
                    "❌ Error al cambiar de plan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Botón cancelar suscripción
        binding.btnCancelarSuscripcion.setOnClickListener {
            showConfirmacionCancelarSuscripcion(userId)
        }
    }

    private fun showConfirmacionCambioPlan(plan: Plan, userId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmacion_plan_mejorado, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val vHeaderColor = dialogView.findViewById<View>(R.id.vHeaderColor)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTitulo)
        val tvPrecio = dialogView.findViewById<TextView>(R.id.tvPrecio)
        val llBeneficios = dialogView.findViewById<LinearLayout>(R.id.llBeneficios)
        val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<MaterialButton>(R.id.btnConfirmar)
        val progressBarDialog = dialogView.findViewById<ProgressBar>(R.id.progressBarDialog)

        val headerColor: Int
        val buttonColor: Int
        when (plan.nombre) {
            "Pro" -> {
                headerColor = R.color.blue_500
                buttonColor = R.color.blue_600
            }
            "Premium" -> {
                headerColor = R.color.orange_500
                buttonColor = R.color.orange_600
            }
            "Enterprise" -> {
                headerColor = R.color.purple_500
                buttonColor = R.color.purple_600
            }
            else -> {
                headerColor = R.color.grey_500
                buttonColor = R.color.grey_600
            }
        }

        vHeaderColor.setBackgroundColor(requireContext().getColor(headerColor))
        btnConfirmar.backgroundTintList = requireContext().getColorStateList(buttonColor)

        tvTitulo.text = "¡Cambiar a ${plan.nombre}!"
        tvPrecio.text = if (plan.precio == 0.0) {
            "GRATIS"
        } else {
            "S/ ${plan.precio.toInt()}"
        }

        plan.caracteristicas.forEachIndexed { index, beneficio ->
            val tvBeneficio = TextView(requireContext()).apply {
                text = "✓ $beneficio"
                textSize = 15f
                setTextColor(requireContext().getColor(R.color.text_primary))
                setPadding(0, 12, 0, 12)
                alpha = 0f
            }
            llBeneficios.addView(tvBeneficio)

            tvBeneficio.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay((index * 100).toLong())
                .start()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            ObjectAnimator.ofFloat(btnConfirmar, "scaleX", 1f, 0.9f, 1f).setDuration(200).start()
            ObjectAnimator.ofFloat(btnConfirmar, "scaleY", 1f, 0.9f, 1f).setDuration(200).start()

            // Mostrar loading en el diálogo
            progressBarDialog?.visibility = View.VISIBLE
            btnConfirmar.isEnabled = false
            btnCancelar.isEnabled = false

            viewModel.cambiarPlan(userId, plan.id)

            // Cerrar diálogo cuando termine
            viewModel.cambioPlanExitoso.observe(viewLifecycleOwner) {
                dialog.dismiss()
            }
        }

        // ✅ Mostrar sin animación de zoom
        dialog.show()
    }

    private fun showConfirmacionCancelarSuscripcion(userId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("⚠️ Cancelar Suscripción")
            .setMessage("¿Estás seguro? Perderás todos los beneficios de tu plan actual y volverás al plan Básico.")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                viewModel.cancelarSuscripcion(userId, "Usuario canceló")
            }
            .setNegativeButton("No, mantener plan", null)
            .show()
    }

    private fun loadData() {
        val userId = sessionManager.getUserId() ?: return
        viewModel.cargarPlanes()
        viewModel.cargarSuscripcionActual(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
