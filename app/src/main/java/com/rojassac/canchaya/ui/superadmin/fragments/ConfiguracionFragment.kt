package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.FragmentConfiguracionBinding
import com.rojassac.canchaya.ui.superadmin.adapters.ConfigOpcionesAdapter

/**
 * ✅ ACTUALIZADO (23 Oct 2025)
 * Fragment de configuración del SuperAdmin con navegación a submódulos
 */
class ConfiguracionFragment : Fragment() {

    private var _binding: FragmentConfiguracionBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ConfigOpcionesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupOpciones()
    }

    private fun setupRecyclerView() {
        adapter = ConfigOpcionesAdapter { opcion ->
            navegarAOpcion(opcion)
        }

        binding.recyclerConfigOpciones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ConfiguracionFragment.adapter
        }
    }

    private fun setupOpciones() {
        val opciones = listOf(
            ConfigOpcion(
                id = 1,
                titulo = "Gestión de Planes",
                descripcion = "Administrar precios y características",
                icono = R.drawable.ic_subscription
            ),
            ConfigOpcion(
                id = 2,
                titulo = "Promociones y Cupones",
                descripcion = "Crear y gestionar descuentos",
                icono = R.drawable.ic_gift
            ),
            ConfigOpcion(
                id = 3,
                titulo = "Parámetros Globales",
                descripcion = "Configurar reglas de la aplicación",
                icono = R.drawable.ic_settings
            ),
            ConfigOpcion(
                id = 4,
                titulo = "Notificaciones Masivas",
                descripcion = "Enviar mensajes a usuarios",
                icono = R.drawable.ic_notifications
            ),
            ConfigOpcion(
                id = 5,
                titulo = "Información del Sistema",
                descripcion = "Versión y estadísticas generales",
                icono = R.drawable.ic_info
            )
        )

        adapter.submitList(opciones)
    }

    private fun navegarAOpcion(opcion: ConfigOpcion) {
        when (opcion.id) {
            1 -> navegarAGestionPlanes()
            2 -> navegarAPromociones()
            3 -> navegarAParametrosGlobales()
            4 -> navegarANotificaciones()
            5 -> navegarAInfoSistema()
        }
    }

    private fun navegarAGestionPlanes() {
        val fragment = GestionPlanesFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navegarAPromociones() {
        // ✅ CORREGIDO: Navegar al fragment de promociones
        val fragment = GestionPromocionesFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navegarAParametrosGlobales() {
        val fragment = ParametrosGlobalesFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navegarANotificaciones() {
        showToast("Próximamente: Notificaciones Masivas")
    }

    private fun navegarAInfoSistema() {
        showToast("Próximamente: Información del Sistema")
    }

    private fun showToast(mensaje: String) {
        android.widget.Toast.makeText(requireContext(), mensaje, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * ✅ DATA CLASS: Opción de configuración
 */
data class ConfigOpcion(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val icono: Int
)
