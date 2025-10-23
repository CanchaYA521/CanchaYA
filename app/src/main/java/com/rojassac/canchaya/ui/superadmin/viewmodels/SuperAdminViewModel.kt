package com.rojassac.canchaya.ui.superadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.Sede
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.data.repository.SuperAdminRepository
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.launch

class SuperAdminViewModel : ViewModel() {

    private val repository = SuperAdminRepository()

    // ========== USUARIOS (CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    private val _usuarios = MutableLiveData<Resource<List<User>>>()
    val usuarios: LiveData<Resource<List<User>>> = _usuarios

    private val _updateUserResult = MutableLiveData<Resource<Unit>>()
    val updateUserResult: LiveData<Resource<Unit>> = _updateUserResult

    // ========== CANCHAS (CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    private val _canchasGlobales = MutableLiveData<Resource<List<Cancha>>>()
    val canchasGlobales: LiveData<Resource<List<Cancha>>> = _canchasGlobales

    private val _canchas = MutableLiveData<Resource<List<Cancha>>>()
    val canchas: LiveData<Resource<List<Cancha>>> = _canchas

    private val _updateCanchaResult = MutableLiveData<Resource<Unit>>()
    val updateCanchaResult: LiveData<Resource<Unit>> = _updateCanchaResult

    // ========== ESTADÍSTICAS (CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    private val _userStats = MutableLiveData<Resource<Map<String, Int>>>()
    val userStats: LiveData<Resource<Map<String, Int>>> = _userStats

    private val _canchaStats = MutableLiveData<Resource<Map<String, Int>>>()
    val canchaStats: LiveData<Resource<Map<String, Int>>> = _canchaStats

    // 🆕 ========== SEDES (NUEVO - 22 Oct 2025) ==========

    private val _sedes = MutableLiveData<Resource<List<Sede>>>()
    val sedes: LiveData<Resource<List<Sede>>> = _sedes

    private val _updateSedeResult = MutableLiveData<Resource<Unit>>()
    val updateSedeResult: LiveData<Resource<Unit>> = _updateSedeResult

    init {
        loadAllUsers()
        loadAllCanchas()
        cargarSedes() // ✅ AGREGADO (23 Oct 2025)
    }

    // ========== FUNCIONES - USUARIOS (CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    fun loadAllUsers() {
        viewModelScope.launch {
            _usuarios.value = Resource.Loading()
            val result = repository.getAllUsers()
            if (result.isSuccess) {
                _usuarios.value = Resource.Success(result.getOrNull()!!)
            } else {
                _usuarios.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cargar usuarios")
            }
        }
    }

    fun loadUsuarios() = loadAllUsers()

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            _updateUserResult.value = Resource.Loading()
            val result = repository.updateUserRole(userId, newRole)
            if (result.isSuccess) {
                _updateUserResult.value = Resource.Success(Unit)
                loadAllUsers()
            } else {
                _updateUserResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al actualizar rol")
            }
        }
    }

    fun toggleUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            _updateUserResult.value = Resource.Loading()
            val result = repository.toggleUserStatus(userId, isActive)
            if (result.isSuccess) {
                _updateUserResult.value = Resource.Success(Unit)
                loadAllUsers()
            } else {
                _updateUserResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cambiar estado")
            }
        }
    }

    fun assignAdminToCancha(adminId: String, canchaId: String) {
        viewModelScope.launch {
            _updateUserResult.value = Resource.Loading()
            val result = repository.assignAdminToCancha(adminId, canchaId)
            if (result.isSuccess) {
                _updateUserResult.value = Resource.Success(Unit)
                loadAllUsers()
                cargarCanchasGlobales()
            } else {
                _updateUserResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al asignar admin")
            }
        }
    }

    // ========== FUNCIONES - CANCHAS GLOBALES (CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    fun cargarCanchasGlobales() {
        viewModelScope.launch {
            _canchasGlobales.value = Resource.Loading()
            val result = repository.getAllCanchas()
            if (result.isSuccess) {
                _canchasGlobales.value = Resource.Success(result.getOrNull()!!)
            } else {
                _canchasGlobales.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cargar canchas")
            }
        }
    }

    fun loadAllCanchas() {
        viewModelScope.launch {
            _canchas.value = Resource.Loading()
            val result = repository.getAllCanchas()
            if (result.isSuccess) {
                _canchas.value = Resource.Success(result.getOrNull()!!)
            } else {
                _canchas.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cargar canchas")
            }
        }
    }

    fun loadCanchas() = loadAllCanchas()

    fun toggleCanchaStatus(cancha: Cancha) {
        viewModelScope.launch {
            _updateCanchaResult.value = Resource.Loading()
            val result = repository.toggleCanchaApproval(cancha.id, !cancha.activo)
            if (result.isSuccess) {
                _updateCanchaResult.value = Resource.Success(Unit)
                cargarCanchasGlobales()
            } else {
                _updateCanchaResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cambiar estado")
            }
        }
    }

    fun eliminarCancha(canchaId: String) {
        viewModelScope.launch {
            _updateCanchaResult.value = Resource.Loading()
            val result = repository.deleteCancha(canchaId)
            if (result.isSuccess) {
                _updateCanchaResult.value = Resource.Success(Unit)
                cargarCanchasGlobales()
            } else {
                _updateCanchaResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al eliminar cancha")
            }
        }
    }

    // ========== FUNCIONES - ESTADÍSTICAS (CÓDIGO EXISTENTE - NO MODIFICADO) ==========

    fun loadUserStats() {
        viewModelScope.launch {
            _userStats.value = Resource.Loading()
            val result = repository.getUserCountByRole()
            if (result.isSuccess) {
                _userStats.value = Resource.Success(result.getOrNull()!!)
            } else {
                _userStats.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cargar estadísticas")
            }
        }
    }

    fun loadCanchaStats() {
        viewModelScope.launch {
            _canchaStats.value = Resource.Loading()
            val result = repository.getCanchasStats()
            if (result.isSuccess) {
                _canchaStats.value = Resource.Success(result.getOrNull()!!)
            } else {
                _canchaStats.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cargar estadísticas")
            }
        }
    }

    // 🆕 ========== FUNCIONES - SEDES (NUEVO - 22 Oct 2025) ==========

    /**
     * 🆕 NUEVA FUNCIÓN: Cargar todas las sedes
     */
    fun cargarSedes() {
        viewModelScope.launch {
            _sedes.value = Resource.Loading()
            val result = repository.getAllSedes()
            if (result.isSuccess) {
                _sedes.value = Resource.Success(result.getOrNull()!!)
            } else {
                _sedes.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cargar sedes")
            }
        }
    }

    /**
     * 🆕 NUEVA FUNCIÓN: Guardar una sede (crear o actualizar)
     */
    fun guardarSede(sede: Sede) {
        viewModelScope.launch {
            _updateSedeResult.value = Resource.Loading()

            val result = if (sede.id.isEmpty()) {
                // Crear nueva sede
                repository.crearSede(sede)
            } else {
                // Actualizar sede existente
                repository.actualizarSede(sede)
            }

            if (result.isSuccess) {
                _updateSedeResult.value = Resource.Success(Unit)
                cargarSedes() // Recargar lista de sedes
            } else {
                _updateSedeResult.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Error al guardar sede"
                )
            }
        }
    }

    /**
     * 🆕 NUEVA FUNCIÓN: Eliminar una sede
     */
    fun eliminarSede(sedeId: String) {
        viewModelScope.launch {
            _updateSedeResult.value = Resource.Loading()
            val result = repository.deleteSede(sedeId)
            if (result.isSuccess) {
                _updateSedeResult.value = Resource.Success(Unit)
                cargarSedes()
            } else {
                _updateSedeResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al eliminar sede")
            }
        }
    }

    /**
     * 🆕 NUEVA FUNCIÓN: Cambiar estado de una sede (activa/inactiva)
     */
    fun toggleSedeStatus(sedeId: String, isActive: Boolean) {
        viewModelScope.launch {
            _updateSedeResult.value = Resource.Loading()
            val result = repository.toggleSedeStatus(sedeId, isActive)
            if (result.isSuccess) {
                _updateSedeResult.value = Resource.Success(Unit)
                cargarSedes()
            } else {
                _updateSedeResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al cambiar estado de sede")
            }
        }
    }
}
