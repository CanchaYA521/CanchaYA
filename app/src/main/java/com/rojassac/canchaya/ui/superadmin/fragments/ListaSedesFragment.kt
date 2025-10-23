package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Sede
import com.rojassac.canchaya.databinding.FragmentListaSedesBinding
import com.rojassac.canchaya.ui.superadmin.adapters.SedesAdapter
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.utils.Resource

/**
 * ✅ CÓDIGO EXISTENTE MANTENIDO
 * 🔧 CORREGIDO: Agregado listener al FAB para crear nueva sede (23 Oct 2025)
 * 🔧 CORREGIDO: ID correcto del FAB es fabCrearSede (no fabAgregarSede)
 */
class ListaSedesFragment : Fragment() {

    private var _binding: FragmentListaSedesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SuperAdminViewModel by activityViewModels()
    private lateinit var sedesAdapter: SedesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaSedesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners() // 🔧 CORREGIDO: Ya no está vacío
        setupObservers()

        // Cargar sedes
        viewModel.cargarSedes()
    }

    // ✅ CÓDIGO EXISTENTE (NO MODIFICADO)
    private fun setupRecyclerView() {
        sedesAdapter = SedesAdapter(
            sedes = emptyList(),
            onVerCanchas = { sede ->
                navigateToVerCanchasSede(sede)
            },
            onEditar = { sede ->
                navigateToCrearSedeFragment(sede)
            },
            onEliminar = { sede ->
                // TODO: Confirmar eliminación
                viewModel.eliminarSede(sede.id)
            },
            onAgregarCancha = { sede ->
                navigateToCrearCanchaFragment(sede)
            }
        )

        binding.recyclerViewSedes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sedesAdapter
        }
    }

    // 🔧 CORREGIDO: ID correcto fabCrearSede (23 Oct 2025)
    private fun setupListeners() {
        // ✨ NUEVO: FAB para crear nueva sede
        binding.fabCrearSede.setOnClickListener {
            navigateToCrearSedeFragment(null) // null = crear nueva sede
        }
    }

    // ✅ CÓDIGO EXISTENTE (NO MODIFICADO)
    private fun setupObservers() {
        viewModel.sedes.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    sedesAdapter.updateList(resource.data ?: emptyList())
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ✅ CÓDIGO EXISTENTE (NO MODIFICADO): Navegar a crear/editar sede
    private fun navigateToCrearSedeFragment(sede: Sede?) {
        val fragment = CrearSedeFragment.newInstance(sede)
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    // ✅ CÓDIGO EXISTENTE (NO MODIFICADO): Navegar a crear cancha
    private fun navigateToCrearCanchaFragment(sede: Sede) {
        val fragment = CrearCanchaFragment.newInstanceForSede(
            sedeId = sede.id,
            sedeNombre = sede.nombre,
            horaApertura = sede.horaApertura,
            horaCierre = sede.horaCierre
        )

        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    // ✅ CÓDIGO EXISTENTE (NO MODIFICADO): Navegar a ver canchas
    private fun navigateToVerCanchasSede(sede: Sede) {
        val fragment = VerCanchasSedeFragment.newInstance(sede)
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
