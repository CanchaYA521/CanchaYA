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
        setupFilterChips()
        setupObservers()

        // Cargar datos
        viewModel.loadAllUsers()
        viewModel.loadAllCanchas()
    }

    private fun setupRecyclerView() {
        adapter = UsuariosAdapter(
            onEditClick = { user -> showEditUserDialog(user) },
            onToggleStatusClick = { user -> toggleUserStatus(user) },
            onDeleteClick = { user -> confirmDeleteUser(user) },
            onAssignCanchaClick = { user -> showAssignCanchaDialog(user) }
        )

        binding.rvUsuarios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsuarios.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText ?: "")
                return true
            }
        })
    }

    private fun setupFilterChips() {
        binding.chipTodos.setOnClickListener { filterByRole(null) }
        binding.chipUsuarios.setOnClickListener { filterByRole(UserRole.USUARIO) }
        binding.chipAdmins.setOnClickListener { filterByRole(UserRole.ADMIN) }
        binding.chipSuperadmins.setOnClickListener { filterByRole(UserRole.SUPERADMIN) }
    }

    private fun filterByRole(role: UserRole?) {
        currentFilterRole = role

        // Actualizar estado de chips
        binding.chipTodos.isChecked = role == null
        binding.chipUsuarios.isChecked = role == UserRole.USUARIO
        binding.chipAdmins.isChecked = role == UserRole.ADMIN
        binding.chipSuperadmins.isChecked = role == UserRole.SUPERADMIN

        applyFilters()
    }

    private fun filterUsers(query: String) {
        applyFilters(query)
    }

    private fun applyFilters(searchQuery: String = binding.searchView.query.toString()) {
        filteredUsers = allUsers.filter { user ->
            val matchesSearch = searchQuery.isEmpty() ||
                    user.nombre.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true)

            val matchesRole = currentFilterRole == null || user.rol == currentFilterRole

            matchesSearch && matchesRole
        }

        adapter.submitList(filteredUsers)
        updateEmptyState()
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
        // Observar usuarios
        viewModel.usuarios.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    allUsers = resource.data ?: emptyList()
                    applyFilters()
                    updateStats()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showError("Error al cargar usuarios: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        // Observar canchas
        viewModel.canchas.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    allCanchas = resource.data ?: emptyList()
                }
                is Resource.Error -> {
                    showError("Error al cargar canchas: ${resource.message}")
                }
                is Resource.Loading -> {
                    // Loading
                }
            }
        }

        // Observar resultado de operaciones
        viewModel.updateUserResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showSuccess("Operación exitosa")
                }
                is Resource.Error -> {
                    showError("Error: ${resource.message}")
                }
                is Resource.Loading -> {
                    // Loading
                }
            }
        }
    }

    private fun updateStats() {
        val total = allUsers.size
        val usuarios = allUsers.count { it.rol == UserRole.USUARIO }
        val admins = allUsers.count { it.rol == UserRole.ADMIN }
        val superadmins = allUsers.count { it.rol == UserRole.SUPERADMIN }

        binding.chipTodos.text = "Todos ($total)"
        binding.chipUsuarios.text = "Usuarios ($usuarios)"
        binding.chipAdmins.text = "Admins ($admins)"
        binding.chipSuperadmins.text = "SuperAdmins ($superadmins)"
    }

    private fun showEditUserDialog(user: User) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_user, null)

        val radioUsuario = dialogView.findViewById<RadioButton>(R.id.radioUsuario)
        val radioAdmin = dialogView.findViewById<RadioButton>(R.id.radioAdmin)
        val radioSuperadmin = dialogView.findViewById<RadioButton>(R.id.radioSuperadmin)

        // Marcar rol actual
        when (user.rol) {
            UserRole.USUARIO -> radioUsuario.isChecked = true
            UserRole.ADMIN -> radioAdmin.isChecked = true
            UserRole.SUPERADMIN -> radioSuperadmin.isChecked = true
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar Usuario: ${user.nombre}")
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
            .setTitle("Confirmar acción")
            .setMessage("¿Estás seguro de $message a ${user.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.toggleUserStatus(user.uid, newStatus)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun confirmDeleteUser(user: User) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de eliminar a ${user.nombre}? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                showError("Función de eliminar no implementada en el Repository original")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAssignCanchaDialog(user: User) {
        if (allCanchas.isEmpty()) {
            showError("No hay canchas disponibles para asignar")
            return
        }

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_assign_cancha, null)

        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerCanchas)

        // Filtrar canchas sin admin o del mismo admin
        val canchasDisponibles = allCanchas.filter {
            it.adminId == null || it.adminId == user.uid
        }

        if (canchasDisponibles.isEmpty()) {
            showError("No hay canchas disponibles para asignar")
            return
        }

        val canchaNames = canchasDisponibles.map { it.nombre }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, canchaNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Asignar Cancha a ${user.nombre}")
            .setView(dialogView)
            .setPositiveButton("Asignar") { _, _ ->
                val selectedPosition = spinner.selectedItemPosition
                val selectedCancha = canchasDisponibles[selectedPosition]
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
