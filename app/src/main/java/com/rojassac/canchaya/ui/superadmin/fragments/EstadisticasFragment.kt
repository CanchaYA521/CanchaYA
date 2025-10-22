package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.FragmentEstadisticasBinding
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.utils.Resource

class EstadisticasFragment : Fragment() {

    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SuperAdminViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadisticasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupRefreshLayout()
    }

    private fun setupRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadUsuarios()
            viewModel.loadCanchas()
        }
    }

    private fun setupObservers() {
        // Observar usuarios
        viewModel.usuarios.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val users = resource.data
                    if (users != null) {
                        // Estadísticas básicas
                        binding.tvTotalUsuarios.text = users.size.toString()
                        binding.tvUsuariosActivos.text = users.count { it.activo }.toString()

                        // Solo Admins (tvTotalSuperAdmins y tvTotalClientes NO existen en tu XML)
                        val admins = users.count { it.rol == UserRole.ADMIN }
                        binding.tvTotalAdmins.text = admins.toString()
                    }
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    showError("Error al cargar usuarios: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
                }
            }
        }

        // Observar canchas
        viewModel.canchas.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val canchas = resource.data
                    if (canchas != null) {
                        binding.tvTotalCanchas.text = canchas.size.toString()
                        binding.tvCanchasActivas.text = canchas.count { it.activo }.toString()
                        binding.tvCanchasInactivas.text = canchas.count { !it.activo }.toString()
                    }
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    showError("Error al cargar canchas: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
