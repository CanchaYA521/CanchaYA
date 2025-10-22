package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rojassac.canchaya.databinding.FragmentListaSedesBinding
import com.rojassac.canchaya.ui.superadmin.adapters.SedesAdapter
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.utils.Resource

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
        setupObservers()
        setupListeners()
        viewModel.cargarSedes()
    }

    private fun setupRecyclerView() {
        sedesAdapter = SedesAdapter(
            sedes = emptyList(),
            onVerCanchas = { sede ->
                Toast.makeText(requireContext(), "Ver canchas de: ${sede.nombre}", Toast.LENGTH_SHORT).show()
            },
            onEditar = { sede ->
                val fragment = CrearSedeFragment.newInstance(sede)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onEliminar = { sede ->
                viewModel.eliminarSede(sede.id)
                Toast.makeText(requireContext(), "Sede eliminada: ${sede.nombre}", Toast.LENGTH_SHORT).show()
            },
            onAgregarCancha = { sede ->
                Toast.makeText(requireContext(), "Agregar cancha a: ${sede.nombre}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewSedes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sedesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.sedes.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerViewSedes.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val sedes = resource.data ?: emptyList()
                    if (sedes.isEmpty()) {
                        binding.recyclerViewSedes.visibility = View.GONE
                        Toast.makeText(requireContext(), "No hay sedes registradas", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.recyclerViewSedes.visibility = View.VISIBLE
                        sedesAdapter.updateList(sedes)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerViewSedes.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabCrearSede.setOnClickListener {
            val fragment = CrearSedeFragment.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ListaSedesFragment()
    }
}
