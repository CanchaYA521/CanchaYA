package com.rojassac.canchaya.ui.admin.canchas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.FragmentAdminCanchasBinding
import com.rojassac.canchaya.utils.Resource
import com.rojassac.canchaya.utils.toast

class AdminCanchasFragment : Fragment() {

    private var _binding: FragmentAdminCanchasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminCanchasViewModel by viewModels()
    private lateinit var adapter: AdminCanchasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCanchasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Cargar canchas
        viewModel.cargarTodasLasCanchas()
    }

    private fun setupRecyclerView() {
        adapter = AdminCanchasAdapter(
            onEditClick = { cancha ->
                // TODO: Navegar a pantalla de edición
                requireContext().toast("Editar: ${cancha.nombre}")
            },
            onDeleteClick = { cancha ->
                confirmarEliminar(cancha.id, cancha.nombre)
            },
            onToggleActivoClick = { cancha, activo ->
                viewModel.toggleActivoCancha(cancha.id, activo)
            }
        )

        binding.rvCanchas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminCanchasFragment.adapter
        }
    }

    private fun setupObservers() {
        // Observar lista de canchas
        viewModel.canchasState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvCanchas.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val canchas = resource.data ?: emptyList()

                    if (canchas.isEmpty()) {
                        binding.rvCanchas.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvCanchas.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        adapter.submitList(canchas)
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    requireContext().toast("Error: ${resource.message}")
                }
            }
        }

        // Observar operaciones (crear, editar, eliminar)
        viewModel.operacionState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    requireContext().toast(resource.data ?: "Operación exitosa")
                }

                is Resource.Error -> {
                    requireContext().toast("Error: ${resource.message}")
                }

                else -> {}
            }
        }
    }

    private fun setupListeners() {
        binding.fabAgregarCancha.setOnClickListener {
            // TODO: Navegar a pantalla de crear cancha
            requireContext().toast("Próximamente: Crear cancha")
        }
    }

    private fun confirmarEliminar(canchaId: String, nombre: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar cancha")
            .setMessage("¿Estás seguro de que deseas eliminar '$nombre'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarCancha(canchaId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
