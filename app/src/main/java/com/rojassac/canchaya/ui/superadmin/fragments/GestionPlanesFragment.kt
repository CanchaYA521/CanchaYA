package com.rojassac.canchaya.ui.superadmin.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.databinding.DialogEditarPlanBinding
import com.rojassac.canchaya.databinding.FragmentGestionPlanesBinding
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.ui.superadmin.adapters.PlanesAdapter
import com.rojassac.canchaya.utils.Resource

/**
 * ✅ NUEVO ARCHIVO (23 Oct 2025)
 * Fragment para gestionar los planes de suscripción
 */
class GestionPlanesFragment : Fragment() {

    private var _binding: FragmentGestionPlanesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SuperAdminViewModel
    private lateinit var adapter: PlanesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionPlanesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SuperAdminViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        // Cargar planes
        viewModel.loadPlanes()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = PlanesAdapter(
            onEditClick = { plan ->
                mostrarDialogEditarPlan(plan)
            }
        )

        binding.recyclerPlanes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GestionPlanesFragment.adapter
        }
    }

    private fun setupObservers() {
        // Observar planes
        viewModel.planes.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val planes = resource.data ?: emptyList()

                    if (planes.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        adapter.submitList(planes)

                        // Cargar suscriptores para cada plan
                        val planIds = planes.map { it.id }
                        viewModel.loadSuscriptoresPorPlan(planIds)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Observar suscriptores por plan
        viewModel.suscriptoresPorPlan.observe(viewLifecycleOwner) { suscriptoresMap ->
            adapter.updateSuscriptores(suscriptoresMap)
        }

        // Observar resultado de actualización
        viewModel.updatePlanResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Mostrar loading si es necesario
                }
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Plan actualizado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Recargar planes
                    viewModel.loadPlanes()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun mostrarDialogEditarPlan(plan: Plan) {
        val dialogBinding = DialogEditarPlanBinding.inflate(layoutInflater)

        // Prellenar datos
        dialogBinding.apply {
            tvNombrePlan.text = "Plan ${plan.nombre}"
            etPrecio.setText(plan.precio.toString())
            etComision.setText((plan.comision * 100).toInt().toString()) // Convertir 0.4 a 40
            etMaxCanchas.setText(plan.maxCanchas.toString())
            etDescripcion.setText(plan.descripcion)
            switchActivo.isChecked = plan.activo
            switchDestacado.isChecked = plan.destacado
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnGuardar.setOnClickListener {
            // Validar campos
            val precio = dialogBinding.etPrecio.text.toString().toDoubleOrNull()
            val comision = dialogBinding.etComision.text.toString().toDoubleOrNull()
            val maxCanchas = dialogBinding.etMaxCanchas.text.toString().toIntOrNull()
            val descripcion = dialogBinding.etDescripcion.text.toString()

            if (precio == null) {
                dialogBinding.tilPrecio.error = "Ingresa un precio válido"
                return@setOnClickListener
            }

            if (comision == null || comision < 0 || comision > 100) {
                dialogBinding.tilComision.error = "Ingresa una comisión entre 0 y 100"
                return@setOnClickListener
            }

            if (maxCanchas == null || maxCanchas < 1) {
                dialogBinding.tilMaxCanchas.error = "Ingresa un número válido"
                return@setOnClickListener
            }

            // Actualizar plan
            val planActualizado = plan.copy(
                precio = precio,
                comision = comision / 100.0, // Convertir 40 a 0.4
                maxCanchas = maxCanchas,
                descripcion = descripcion,
                activo = dialogBinding.switchActivo.isChecked,
                destacado = dialogBinding.switchDestacado.isChecked
            )

            viewModel.updatePlan(planActualizado)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
