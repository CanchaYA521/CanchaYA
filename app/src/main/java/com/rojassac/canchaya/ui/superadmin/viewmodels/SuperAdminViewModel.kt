package com.rojassac.canchaya.ui.superadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.data.repository.SuperAdminRepository
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.launch

class SuperAdminViewModel : ViewModel() {

    private val repository = SuperAdminRepository()

    // ========== USUARIOS ==========
    private val _usuarios = MutableLiveData<Resource<List<User>>>()
    val usuarios: LiveData<Resource<List<User>>> = _usuarios

    private val _updateUserResult = MutableLiveData<Resource<Unit>>()
    val updateUserResult: LiveData<Resource<Unit>> = _updateUserResult

    // ========== CANCHAS ==========
    private val _canchasGlobales = MutableLiveData<Resource<List<Cancha>>>()
    val canchasGlobales: LiveData<Resource<List<Cancha>>> = _canchasGlobales

    private val _canchas = MutableLiveData<Resource<List<Cancha>>>()
    val canchas: LiveData<Resource<List<Cancha>>> = _canchas

    private val _updateCanchaResult = MutableLiveData<Resource<Unit>>()
    val updateCanchaResult: LiveData<Resource<Unit>> = _updateCanchaResult

    // ========== ESTADÍSTICAS ==========
    private val _userStats = MutableLiveData<Resource<Map<String, Int>>>()
    val userStats: LiveData<Resource<Map<String, Int>>> = _userStats

    private val _canchaStats = MutableLiveData<Resource<Map<String, Int>>>()
    val canchaStats: LiveData<Resource<Map<String, Int>>> = _canchaStats

    // ✅ AGREGADO: Inicializar carga automática de datos
    init {
        loadAllUsers()
        loadAllCanchas()
    }

    // ========== FUNCIONES - USUARIOS ==========
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

    // ✅ AGREGADO: Alias para compatibilidad con EstadisticasFragment
    fun loadUsuarios() = loadAllUsers()

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            _updateUserResult.value = Resource.Loading()
            val result = repository.updateUserRole(userId, newRole)
            if (result.isSuccess) {
                _updateUserResult.value = Resource.Success(Unit)
                loadAllUsers() // Recargar lista
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
                loadAllUsers() // Recargar lista
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

    // ========== FUNCIONES - CANCHAS GLOBALES ==========
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

    // ✅ AGREGADO: Alias para compatibilidad con EstadisticasFragment
    fun loadCanchas() = loadAllCanchas()

    fun toggleCanchaStatus(cancha: Cancha) {
        viewModelScope.launch {
            _updateCanchaResult.value = Resource.Loading()
            val result = repository.toggleCanchaApproval(cancha.id, !cancha.activo)
            if (result.isSuccess) {
                _updateCanchaResult.value = Resource.Success(Unit)
                cargarCanchasGlobales() // Recargar lista
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
                cargarCanchasGlobales() // Recargar lista
            } else {
                _updateCanchaResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "Error al eliminar cancha")
            }
        }
    }

    // ========== FUNCIONES - ESTADÍSTICAS ==========
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
}
