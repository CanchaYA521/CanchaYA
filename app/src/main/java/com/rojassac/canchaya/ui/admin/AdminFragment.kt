package com.rojassac.canchaya.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.FragmentAdminBinding
import com.rojassac.canchaya.ui.admin.canchas.AdminCanchasFragment
import com.rojassac.canchaya.ui.admin.dashboard.AdminDashboardFragment
import com.rojassac.canchaya.ui.admin.horarios.AdminHorariosFragment
import com.rojassac.canchaya.ui.admin.ingresos.AdminIngresosFragment
import com.rojassac.canchaya.ui.admin.menu.AdminMenuAdapter
import com.rojassac.canchaya.ui.admin.resenas.AdminResenasFragment
import com.rojassac.canchaya.ui.admin.reservas.AdminReservasFragment

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminMenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val menuItems = listOf(
            AdminMenuItem(
                id = 1,
                titulo = "Mis Canchas",
                descripcion = "Gestiona tus canchas deportivas",
                icono = R.drawable.ic_cancha
            ),
            AdminMenuItem(
                id = 2,
                titulo = "Reservas",
                descripcion = "Ver y gestionar reservas",
                icono = R.drawable.ic_calendar
            ),
            AdminMenuItem(
                id = 3,
                titulo = "Horarios",
                descripcion = "Configurar disponibilidad",
                icono = R.drawable.ic_time
            ),
            AdminMenuItem(
                id = 4,
                titulo = "Ingresos",
                descripcion = "Reportes financieros",
                icono = R.drawable.ic_money
            ),
            AdminMenuItem(
                id = 5,
                titulo = "Dashboard",
                descripcion = "Estadísticas y métricas",
                icono = R.drawable.ic_home
            ),
            AdminMenuItem(
                id = 6,
                titulo = "Reseñas",
                descripcion = "Ver y responder reseñas",
                icono = R.drawable.ic_star
            )
        )

        adapter = AdminMenuAdapter(menuItems) { menuItem ->
            onMenuItemClick(menuItem)
        }

        binding.rvAdminMenu.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@AdminFragment.adapter
        }
    }

    private fun onMenuItemClick(menuItem: AdminMenuItem) {
        when (menuItem.id) {
            1 -> navigateToMisCanchas()
            2 -> navigateToReservas()
            3 -> navigateToHorarios()
            4 -> navigateToIngresos()
            5 -> navigateToDashboard()
            6 -> navigateToResenas()
        }
    }

    private fun navigateToMisCanchas() {
        val fragment = AdminCanchasFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToReservas() {
        val fragment = AdminReservasFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToHorarios() {
        val fragment = AdminHorariosFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToIngresos() {
        val fragment = AdminIngresosFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToDashboard() {
        val fragment = AdminDashboardFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToResenas() {
        val fragment = AdminResenasFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data class para items del menú
data class AdminMenuItem(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val icono: Int
)
