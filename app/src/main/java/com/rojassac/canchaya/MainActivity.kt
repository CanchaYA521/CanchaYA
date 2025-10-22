package com.rojassac.canchaya

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.databinding.ActivityMainBinding
import com.rojassac.canchaya.ui.admin.horarios.AdminHorariosFragment
import com.rojassac.canchaya.ui.admin.ingresos.AdminIngresosFragment
import com.rojassac.canchaya.ui.admin.perfil.AdminPerfilFragment
import com.rojassac.canchaya.ui.admin.reservas.AdminReservasFragment
import com.rojassac.canchaya.ui.auth.LoginActivity
import com.rojassac.canchaya.ui.superadmin.SuperAdminActivity
import com.rojassac.canchaya.ui.user.home.HomeFragment
import com.rojassac.canchaya.ui.user.mapa.MapFragment
import com.rojassac.canchaya.ui.user.perfil.PerfilFragment
import com.rojassac.canchaya.ui.user.reservas.ReservasFragment
import com.rojassac.canchaya.utils.SessionManager
import com.rojassac.canchaya.utils.toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // ✅ Ocultar navegación hasta verificar rol
        binding.bottomNavigation.visibility = View.GONE
        checkUserRole()
    }

    private fun checkUserRole() {
        val savedRole = sessionManager.getUserRole()
        if (savedRole != null) {
            // ✅ Hay rol guardado, configurar navegación
            setupNavigationByRole(savedRole)
            binding.bottomNavigation.visibility = View.VISIBLE
        } else {
            // ✅ No hay sesión guardada, cerrar sesión y volver a login
            toast("Sesión no válida. Inicia sesión nuevamente.")
            logout()
        }
    }

    private fun setupNavigationByRole(role: UserRole) {
        when (role) {
            UserRole.USUARIO -> setupUserNavigation()
            UserRole.ADMIN -> setupAdminNavigation()
            UserRole.SUPERADMIN -> {
                // ✅ Redirigir a SuperAdminActivity (interfaz separada)
                startActivity(Intent(this, SuperAdminActivity::class.java))
                finish()
            }
        }
    }

    // ============================
    // NAVEGACIÓN PARA USUARIOS
    // ============================
    private fun setupUserNavigation() {
        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_user)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_map -> {
                    loadFragment(MapFragment())
                    true
                }
                R.id.nav_reservas -> {
                    loadFragment(ReservasFragment())
                    true
                }
                R.id.nav_perfil -> {
                    loadFragment(PerfilFragment())
                    true
                }
                else -> false
            }
        }

        // ✅ Cargar Home por defecto PARA USUARIOS
        loadFragment(HomeFragment())
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    // ============================
    // NAVEGACIÓN PARA ADMIN (Dueño) - SOLO 4 TABS (SIN SUSCRIPCIÓN)
    // ============================
    private fun setupAdminNavigation() {
        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_admin)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_horarios -> {
                    loadFragment(AdminHorariosFragment())
                    true
                }
                R.id.nav_admin_reservas -> {
                    loadFragment(AdminReservasFragment())
                    true
                }
                R.id.nav_admin_ingresos -> {
                    loadFragment(AdminIngresosFragment())
                    true
                }
                R.id.nav_admin_perfil -> {
                    loadFragment(AdminPerfilFragment())
                    true
                }
                else -> false
            }
        }

        // ✅ Cargar Horarios por defecto PARA ADMINS
        loadFragment(AdminHorariosFragment())
        binding.bottomNavigation.selectedItemId = R.id.nav_horarios
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .commit()
    }

    private fun logout() {
        sessionManager.clearSession()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    // ✅ Función pública para cerrar sesión desde fragments
    fun cerrarSesion() {
        logout()
    }
}
