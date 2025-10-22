package com.rojassac.canchaya.ui.superadmin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.ActivitySuperadminBinding
import com.rojassac.canchaya.ui.superadmin.fragments.ConfiguracionFragment
import com.rojassac.canchaya.ui.superadmin.fragments.EstadisticasFragment
import com.rojassac.canchaya.ui.superadmin.fragments.ListaSedesFragment // 🆕 NUEVO IMPORT
import com.rojassac.canchaya.ui.superadmin.fragments.UsuariosManagementFragment

/**
 * 🔄 ACTUALIZADO: SuperAdminActivity con BottomNavigationView (22 Oct 2025)
 * ANTES: TabLayout con 5 tabs (Canchas Globales, Crear Cancha, Usuarios, Estadísticas, Configuración)
 * AHORA: BottomNavigationView con 4 tabs (Sedes, Dashboard, Usuarios, Configuración)
 */
class SuperAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperadminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperadminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🆕 NUEVO: Configurar Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "SuperAdmin Panel"

        // 🆕 NUEVO: Configurar BottomNavigationView (reemplaza setupTabs)
        setupBottomNavigation()

        // Cargar fragment inicial (Sedes)
        if (savedInstanceState == null) {
            loadFragment(ListaSedesFragment()) // 🆕 CAMBIADO: antes era CanchasGlobalesFragment
        }
    }

    /**
     * 🆕 NUEVA FUNCIÓN: Configurar BottomNavigationView
     * Reemplaza la función setupTabs() anterior
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_sedes -> {
                    loadFragment(ListaSedesFragment()) // 🆕 NUEVO
                    true
                }
                R.id.nav_dashboard -> {
                    loadFragment(EstadisticasFragment()) // ✅ EXISTENTE (ahora se llama Dashboard)
                    true
                }
                R.id.nav_usuarios -> {
                    loadFragment(UsuariosManagementFragment()) // ✅ EXISTENTE
                    true
                }
                R.id.nav_configuracion -> {
                    loadFragment(ConfiguracionFragment()) // ✅ EXISTENTE
                    true
                }
                else -> false
            }
        }
    }

    /**
     * ✅ FUNCIÓN EXISTENTE (NO MODIFICADA)
     * Carga un fragment en el contenedor
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
