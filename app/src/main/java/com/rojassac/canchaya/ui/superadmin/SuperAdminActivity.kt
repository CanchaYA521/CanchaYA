package com.rojassac.canchaya.ui.superadmin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.ActivitySuperadminBinding
import com.rojassac.canchaya.ui.superadmin.fragments.CanchasGlobalesFragment
import com.rojassac.canchaya.ui.superadmin.fragments.ConfiguracionFragment
import com.rojassac.canchaya.ui.superadmin.fragments.CrearCanchaFragment
import com.rojassac.canchaya.ui.superadmin.fragments.EstadisticasFragment
import com.rojassac.canchaya.ui.superadmin.fragments.UsuariosManagementFragment

class SuperAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperadminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperadminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()

        // Cargar fragment inicial
        if (savedInstanceState == null) {
            loadFragment(CanchasGlobalesFragment())
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Canchas Globales"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Crear Cancha"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Usuarios"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Estadísticas"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Configuración"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadFragment(CanchasGlobalesFragment())
                    1 -> loadFragment(CrearCanchaFragment())
                    2 -> loadFragment(UsuariosManagementFragment())
                    3 -> loadFragment(EstadisticasFragment())
                    4 -> loadFragment(ConfiguracionFragment())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
