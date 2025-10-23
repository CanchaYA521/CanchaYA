package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.FragmentUsuariosManagementBinding
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.ui.superadmin.adapters.UsuariosAdapter
import com.rojassac.canchaya.utils.Resource

/**
 * ✅ CÓDIGO EXISTENTE MANTENIDO
 * 🔧 CORREGIDO: Botón filtro agregado (23 Oct 2025)
 */
class UsuariosManagementFragment : Fragment() {

    private var _binding: FragmentUsuariosManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SuperAdminViewModel by activityViewModels()
    private lateinit var adapter: UsuariosAdapter

    private var allUsers = listOf<User>()
    private var filteredUsers = listOf<User>()
    private var allCanchas = listOf<Cancha>()
    private var currentFilterRole: UserRole? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsuariosManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupFilterButton() // 🆕 NUEVO
        setupRefreshLayout() // 🆕 NUEVO
        setupObservers()

        // Cargar datos
        viewModel.loadAllUsers()
        viewModel.loadAllCanchas()
    }

    // 🔧 MODIFICADO: Adapter con click
    private fun setupRecyclerView() {
        adapter = UsuariosAdapter(
            onUserClick = { user -> showUserOptionsDialog(user) }
        )

        binding.rvUsuarios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsuarios.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters(newText ?: "")
                return true
            }
        })
    }

    // 🆕 NUEVO: Botón de filtro
    private fun setupFilterButton() {
        binding.btnFiltro.setOnClickListener {
            showFilterDialog()
        }
    }

    // 🆕 NUEVO: SwipeRefresh
    private fun setupRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadAllUsers()
            viewModel.loadAllCanchas()
        }
    }

    // 🆕 NUEVO: Diálogo de filtro
    private fun showFilterDialog() {
        val options = arrayOf("Todos", "Usuarios", "Admins", "SuperAdmins")
        val selectedIndex = when (currentFilterRole) {
            null -> 0
            UserRole.USUARIO -> 1
            UserRole.ADMIN -> 2
            UserRole.SUPERADMIN -> 3
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar por rol")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                currentFilterRole = when (which) {
                    0 -> null
                    1 -> UserRole.USUARIO
                    2 -> UserRole.ADMIN
                    3 -> UserRole.SUPERADMIN
                    else -> null
                }
                applyFilters()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🆕 NUEVO: Diálogo de opciones
    private fun showUserOptionsDialog(user: User) {
        val options = mutableListOf("Editar Rol")

        options.add(if (user.activo) "Desactivar" else "Activar")

        if (user.rol == UserRole.ADMIN) {
            options.add("Asignar Cancha")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(user.nombre)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Editar Rol" -> showEditUserDialog(user)
                    "Desactivar", "Activar" -> toggleUserStatus(user)
                    "Asignar Cancha" -> showAssignCanchaDialog(user)
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun applyFilters(searchQuery: String = binding.searchView.query.toString()) {
        filteredUsers = allUsers.filter { user ->
            val matchesSearch = searchQuery.isEmpty() ||
                    user.nombre.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true) ||
                    user.celular?.contains(searchQuery) == true

            val matchesRole = currentFilterRole == null || user.rol == currentFilterRole

            matchesSearch && matchesRole
        }

        adapter.submitList(filteredUsers)
        updateEmptyState()
        updateCounter()
    }

    // 🆕 NUEVO: Contador
    private fun updateCounter() {
        val filterText = when (currentFilterRole) {
            null -> "Mostrando ${filteredUsers.size} de ${allUsers.size} usuarios"
            UserRole.USUARIO -> "Mostrando ${filteredUsers.size} usuarios"
            UserRole.ADMIN -> "Mostrando ${filteredUsers.size} admins"
            UserRole.SUPERADMIN -> "Mostrando ${filteredUsers.size} superadmins"
        }
        binding.tvContador.text = filterText
    }

    private fun updateEmptyState() {
        if (filteredUsers.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvUsuarios.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvUsuarios.visibility = View.VISIBLE
        }
    }

    private fun setupObservers() {
        viewModel.usuarios.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    allUsers = resource.data ?: emptyList()
                    applyFilters()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    showError("Error: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        viewModel.canchas.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    allCanchas = resource.data ?: emptyList()
                }
                is Resource.Error -> {
                    showError("Error canchas: ${resource.message}")
                }
                is Resource.Loading -> {}
            }
        }

        viewModel.updateUserResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showSuccess("Operación exitosa")
                    viewModel.loadAllUsers()
                }
                is Resource.Error -> {
                    showError("Error: ${resource.message}")
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun showEditUserDialog(user: User) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_user, null)

        val radioUsuario = dialogView.findViewById<RadioButton>(R.id.radioUsuario)
        val radioAdmin = dialogView.findViewById<RadioButton>(R.id.radioAdmin)
        val radioSuperadmin = dialogView.findViewById<RadioButton>(R.id.radioSuperadmin)

        when (user.rol) {
            UserRole.USUARIO -> radioUsuario.isChecked = true
            UserRole.ADMIN -> radioAdmin.isChecked = true
            UserRole.SUPERADMIN -> radioSuperadmin.isChecked = true
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar: ${user.nombre}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newRole = when {
                    radioUsuario.isChecked -> UserRole.USUARIO
                    radioAdmin.isChecked -> UserRole.ADMIN
                    radioSuperadmin.isChecked -> UserRole.SUPERADMIN
                    else -> user.rol
                }

                if (newRole != user.rol) {
                    viewModel.updateUserRole(user.uid, newRole)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toggleUserStatus(user: User) {
        val newStatus = !user.activo
        val message = if (newStatus) "activar" else "desactivar"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar")
            .setMessage("¿$message a ${user.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.toggleUserStatus(user.uid, newStatus)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showAssignCanchaDialog(user: User) {
        if (allCanchas.isEmpty()) {
            showError("No hay canchas disponibles")
            return
        }

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_assign_cancha, null)

        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerCanchas)

        val canchasDisponibles = allCanchas.filter {
            it.adminId == null || it.adminId == user.uid
        }

        if (canchasDisponibles.isEmpty()) {
            showError("No hay canchas disponibles")
            return
        }

        val canchaNames = canchasDisponibles.map { it.nombre }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, canchaNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Asignar Cancha: ${user.nombre}")
            .setView(dialogView)
            .setPositiveButton("Asignar") { _, _ ->
                val selectedCancha = canchasDisponibles[spinner.selectedItemPosition]
                viewModel.assignAdminToCancha(user.uid, selectedCancha.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
