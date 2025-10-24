package com.rojassac.canchaya.ui.superadmin

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.data.model.Sede
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

    // ✅ ALIAS para compatibilidad con código viejo
    val canchas: LiveData<Resource<List<Cancha>>> = _canchasGlobales

    private val _deleteCanchaResult = MutableLiveData<Resource<Unit>>()
    val deleteCanchaResult: LiveData<Resource<Unit>> = _deleteCanchaResult

    // ========== SEDES ==========

    private val _sedes = MutableLiveData<Resource<List<Sede>>>()
    val sedes: LiveData<Resource<List<Sede>>> = _sedes

    private val _createSedeResult = MutableLiveData<Resource<String>>()
    val createSedeResult: LiveData<Resource<String>> = _createSedeResult

    private val _updateSedeResult = MutableLiveData<Resource<Unit>>()
    val updateSedeResult: LiveData<Resource<Unit>> = _updateSedeResult

    private val _deleteSedeResult = MutableLiveData<Resource<Unit>>()
    val deleteSedeResult: LiveData<Resource<Unit>> = _deleteSedeResult

    private val _estadisticasUsuarios = MutableLiveData<Resource<Map<String, Int>>>()
    val estadisticasUsuarios: LiveData<Resource<Map<String, Int>>> = _estadisticasUsuarios

    private val _estadisticasCanchas = MutableLiveData<Resource<Map<String, Int>>>()
    val estadisticasCanchas: LiveData<Resource<Map<String, Int>>> = _estadisticasCanchas

    private val _estadisticasSedes = MutableLiveData<Resource<Map<String, Int>>>()
    val estadisticasSedes: LiveData<Resource<Map<String, Int>>> = _estadisticasSedes

    // ✅ NUEVO: PLANES
    private val _planes = MutableLiveData<Resource<List<Plan>>>()
    val planes: LiveData<Resource<List<Plan>>> = _planes

    private val _updatePlanResult = MutableLiveData<Resource<Unit>>()
    val updatePlanResult: LiveData<Resource<Unit>> = _updatePlanResult

    private val _suscriptoresPorPlan = MutableLiveData<Map<String, Int>>()
    val suscriptoresPorPlan: LiveData<Map<String, Int>> = _suscriptoresPorPlan

    // ========== FUNCIONES DE USUARIOS ==========

    fun loadUsuarios() {
        viewModelScope.launch {
            _usuarios.value = Resource.Loading()
            val result = repository.getAllUsers()
            _usuarios.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyList())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    // ✅ ALIAS para compatibilidad
    fun loadAllUsers() = loadUsuarios()

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            _updateUserResult.value = Resource.Loading()
            val result = repository.updateUserRole(userId, newRole)
            _updateUserResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al actualizar")
            }
        }
    }

    fun toggleUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            _updateUserResult.value = Resource.Loading()
            val result = repository.toggleUserStatus(userId, isActive)
            _updateUserResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al cambiar estado")
            }
        }
    }

    fun assignAdminToCancha(adminId: String, canchaId: String) {
        viewModelScope.launch {
            _updateUserResult.value = Resource.Loading()
            val result = repository.assignAdminToCancha(adminId, canchaId)
            _updateUserResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al asignar admin")
            }
        }
    }

    // ========== FUNCIONES DE CANCHAS ==========

    fun loadCanchasGlobales() {
        viewModelScope.launch {
            _canchasGlobales.value = Resource.Loading()
            val result = repository.getAllCanchas()
            _canchasGlobales.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyList())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    // ✅ ALIAS para compatibilidad
    fun loadCanchas() = loadCanchasGlobales()
    fun loadAllCanchas() = loadCanchasGlobales()

    fun toggleCanchaApproval(canchaId: String, isActive: Boolean) {
        viewModelScope.launch {
            _deleteCanchaResult.value = Resource.Loading()
            val result = repository.toggleCanchaApproval(canchaId, isActive)
            _deleteCanchaResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al cambiar estado")
            }
        }
    }

    fun deleteCancha(canchaId: String) {
        viewModelScope.launch {
            _deleteCanchaResult.value = Resource.Loading()
            val result = repository.deleteCancha(canchaId)
            _deleteCanchaResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al eliminar")
            }
        }
    }

    // ========== FUNCIONES DE SEDES ==========

    fun loadSedes() {
        viewModelScope.launch {
            _sedes.value = Resource.Loading()
            val result = repository.getAllSedes()
            _sedes.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyList())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    // ✅ ALIAS para compatibilidad
    fun cargarSedes() = loadSedes()

    fun crearSede(sede: Sede) {
        viewModelScope.launch {
            _createSedeResult.value = Resource.Loading()
            val result = repository.crearSede(sede)
            _createSedeResult.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: "")
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al crear sede")
            }
        }
    }

    // ✅ ALIAS para compatibilidad
    fun guardarSede(sede: Sede) = crearSede(sede)

    fun actualizarSede(sede: Sede) {
        viewModelScope.launch {
            _updateSedeResult.value = Resource.Loading()
            val result = repository.actualizarSede(sede)
            _updateSedeResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al actualizar sede")
            }
        }
    }

    fun deleteSede(sedeId: String) {
        viewModelScope.launch {
            _deleteSedeResult.value = Resource.Loading()
            val result = repository.deleteSede(sedeId)
            _deleteSedeResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al eliminar sede")
            }
        }
    }

    // ✅ ALIAS para compatibilidad
    fun eliminarSede(sedeId: String) = deleteSede(sedeId)

    fun toggleSedeStatus(sedeId: String, isActive: Boolean) {
        viewModelScope.launch {
            _updateSedeResult.value = Resource.Loading()
            val result = repository.toggleSedeStatus(sedeId, isActive)
            _updateSedeResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al cambiar estado")
            }
        }
    }

    fun reactivarCodigoSede(sedeId: String) {
        viewModelScope.launch {
            _updateSedeResult.value = Resource.Loading()
            val result = repository.reactivarCodigoSede(sedeId)
            _updateSedeResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al reactivar código")
            }
        }
    }

    // ========== ESTADÍSTICAS ==========

    fun loadEstadisticasUsuarios() {
        viewModelScope.launch {
            _estadisticasUsuarios.value = Resource.Loading()
            val result = repository.getUserCountByRole()
            _estadisticasUsuarios.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyMap())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error")
            }
        }
    }

    fun loadEstadisticasCanchas() {
        viewModelScope.launch {
            _estadisticasCanchas.value = Resource.Loading()
            val result = repository.getCanchasStats()
            _estadisticasCanchas.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyMap())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error")
            }
        }
    }

    fun loadEstadisticasSedes() {
        viewModelScope.launch {
            _estadisticasSedes.value = Resource.Loading()
            val result = repository.getSedesStats()
            _estadisticasSedes.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyMap())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error")
            }
        }
    }

    // ========== FUNCIONES DE PLANES (✅ NUEVO - 23 Oct 2025) ==========

    fun loadPlanes() {
        viewModelScope.launch {
            _planes.value = Resource.Loading()
            val result = repository.getAllPlanes()
            _planes.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyList())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al cargar planes")
            }
        }
    }

    fun updatePlan(plan: Plan) {
        viewModelScope.launch {
            _updatePlanResult.value = Resource.Loading()
            val result = repository.updatePlan(plan)
            _updatePlanResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al actualizar plan")
            }
        }
    }

    fun loadSuscriptoresPorPlan(planIds: List<String>) {
        viewModelScope.launch {
            val suscriptoresMap = mutableMapOf<String, Int>()

            planIds.forEach { planId ->
                val result = repository.getSuscriptoresPorPlan(planId)
                if (result.isSuccess) {
                    suscriptoresMap[planId] = result.getOrNull() ?: 0
                }
            }

            _suscriptoresPorPlan.value = suscriptoresMap
        }
    }

    fun togglePlanStatus(planId: String, activo: Boolean) {
        viewModelScope.launch {
            _updatePlanResult.value = Resource.Loading()
            val result = repository.togglePlanStatus(planId, activo)
            _updatePlanResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al cambiar estado")
            }
        }
    }
}
