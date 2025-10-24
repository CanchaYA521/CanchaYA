package com.rojassac.canchaya.ui.superadmin

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.rojassac.canchaya.data.model.ParametrosGlobales
import com.google.firebase.auth.FirebaseAuth
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.data.model.Promocion // ✅ AGREGADO (23 Oct 2025)
import com.rojassac.canchaya.data.model.Sede
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.data.repository.SuperAdminRepository
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.launch

class SuperAdminViewModel : ViewModel() {

    private val repository = SuperAdminRepository()
    private val auth = FirebaseAuth.getInstance()

    // ========== USUARIOS ==========

    private val _usuarios = MutableLiveData<Resource<List<User>>>()
    val usuarios: LiveData<Resource<List<User>>> = _usuarios

    private val _updateUserResult = MutableLiveData<Resource<Unit>>()
    val updateUserResult: LiveData<Resource<Unit>> = _updateUserResult

    // ========== CANCHAS ==========

    private val _canchasGlobales = MutableLiveData<Resource<List<Cancha>>>()
    val canchasGlobales: LiveData<Resource<List<Cancha>>> = _canchasGlobales

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

    // ========== PLANES ==========

    private val _planes = MutableLiveData<Resource<List<Plan>>>()
    val planes: LiveData<Resource<List<Plan>>> = _planes

    private val _updatePlanResult = MutableLiveData<Resource<Unit>>()
    val updatePlanResult: LiveData<Resource<Unit>> = _updatePlanResult

    private val _suscriptoresPorPlan = MutableLiveData<Map<String, Int>>()
    val suscriptoresPorPlan: LiveData<Map<String, Int>> = _suscriptoresPorPlan

    // ✅ ========== PROMOCIONES (NUEVO - 23 Oct 2025) ==========

    private val _promociones = MutableLiveData<Resource<List<Promocion>>>()
    val promociones: LiveData<Resource<List<Promocion>>> = _promociones

    private val _createPromocionResult = MutableLiveData<Resource<String>>()
    val createPromocionResult: LiveData<Resource<String>> = _createPromocionResult

    private val _updatePromocionResult = MutableLiveData<Resource<Unit>>()
    val updatePromocionResult: LiveData<Resource<Unit>> = _updatePromocionResult

    private val _deletePromocionResult = MutableLiveData<Resource<Unit>>()
    val deletePromocionResult: LiveData<Resource<Unit>> = _deletePromocionResult

    private val _estadisticasPromocion = MutableLiveData<Resource<Map<String, Any>>>()
    val estadisticasPromocion: LiveData<Resource<Map<String, Any>>> = _estadisticasPromocion

    // ══════════════════════════════════════════════════════════════════
    // ⚙️ PARÁMETROS GLOBALES (NUEVO - 23 Oct 2025)
    // ══════════════════════════════════════════════════════════════════

    // LiveData para los parámetros globales
    private val _parametrosGlobales = MutableLiveData<Resource<ParametrosGlobales>>()
    val parametrosGlobales: LiveData<Resource<ParametrosGlobales>> = _parametrosGlobales

    // LiveData para resultado de actualización
    private val _updateParametrosResult = MutableLiveData<Resource<Unit>>()
    val updateParametrosResult: LiveData<Resource<Unit>> = _updateParametrosResult

    // LiveData para modo mantenimiento
    private val _modoMantenimiento = MutableLiveData<Boolean>()
    val modoMantenimiento: LiveData<Boolean> = _modoMantenimiento

    /**
     * Cargar parámetros globales
     */
    fun loadParametrosGlobales() {
        viewModelScope.launch {
            _parametrosGlobales.value = Resource.Loading()

            try {
                val result = repository.getParametrosGlobales()

                if (result.isSuccess) {
                    _parametrosGlobales.value = Resource.Success(result.getOrNull()!!)
                } else {
                    _parametrosGlobales.value = Resource.Error(
                        result.exceptionOrNull()?.message ?: "Error al cargar parámetros"
                    )
                }
            } catch (e: Exception) {
                _parametrosGlobales.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Actualizar parámetros globales
     */
    fun actualizarParametrosGlobales(parametros: ParametrosGlobales) {
        viewModelScope.launch {
            _updateParametrosResult.value = Resource.Loading()

            try {
                val userId = auth.currentUser?.uid ?: ""
                val result = repository.actualizarParametrosGlobales(parametros, userId)

                if (result.isSuccess) {
                    _updateParametrosResult.value = Resource.Success(Unit)
                    // Recargar parámetros actualizados
                    loadParametrosGlobales()
                } else {
                    _updateParametrosResult.value = Resource.Error(
                        result.exceptionOrNull()?.message ?: "Error al actualizar parámetros"
                    )
                }
            } catch (e: Exception) {
                _updateParametrosResult.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Actualizar un campo específico
     */
    fun actualizarCampoParametros(campo: String, valor: Any) {
        viewModelScope.launch {
            _updateParametrosResult.value = Resource.Loading()

            try {
                val userId = auth.currentUser?.uid ?: ""
                val result = repository.actualizarCampoParametros(campo, valor, userId)

                if (result.isSuccess) {
                    _updateParametrosResult.value = Resource.Success(Unit)
                    loadParametrosGlobales()
                } else {
                    _updateParametrosResult.value = Resource.Error(
                        result.exceptionOrNull()?.message ?: "Error al actualizar campo"
                    )
                }
            } catch (e: Exception) {
                _updateParametrosResult.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Resetear parámetros a valores por defecto
     */
    fun resetearParametros() {
        viewModelScope.launch {
            _updateParametrosResult.value = Resource.Loading()

            try {
                val userId = auth.currentUser?.uid ?: ""
                val result = repository.resetearParametros(userId)

                if (result.isSuccess) {
                    _updateParametrosResult.value = Resource.Success(Unit)
                    loadParametrosGlobales()
                } else {
                    _updateParametrosResult.value = Resource.Error(
                        result.exceptionOrNull()?.message ?: "Error al resetear parámetros"
                    )
                }
            } catch (e: Exception) {
                _updateParametrosResult.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Activar/Desactivar modo mantenimiento
     */
    fun toggleModoMantenimiento(activar: Boolean) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: ""
                val result = repository.toggleModoMantenimiento(activar, userId)

                if (result.isSuccess) {
                    _modoMantenimiento.value = activar
                    loadParametrosGlobales()
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    /**
     * Observar parámetros en tiempo real
     */
    fun observeParametrosGlobales(): LiveData<ParametrosGlobales> {
        return repository.getParametrosGlobalesFlow()
            .asLiveData(viewModelScope.coroutineContext)
    }

    /**
     * Verificar si la app está en mantenimiento
     */
    fun checkModoMantenimiento() {
        viewModelScope.launch {
            try {
                val result = repository.isAppEnMantenimiento()
                if (result.isSuccess) {
                    _modoMantenimiento.value = result.getOrNull() ?: false
                }
            } catch (e: Exception) {
                _modoMantenimiento.value = false
            }
        }
    }

    /**
     * Validar cambios antes de guardar
     */
    fun validarParametros(parametros: ParametrosGlobales): String? {
        return when {
            parametros.anticipacionMinima < 0 -> "La anticipación mínima no puede ser negativa"
            parametros.anticipacionMaxima < parametros.anticipacionMinima ->
                "La anticipación máxima debe ser mayor a la mínima"
            parametros.duracionMinima < 1 -> "La duración mínima debe ser al menos 1 hora"
            parametros.duracionMaxima < parametros.duracionMinima ->
                "La duración máxima debe ser mayor a la mínima"
            parametros.porcentajeAnticipo < 0 || parametros.porcentajeAnticipo > 100 ->
                "El porcentaje de anticipo debe estar entre 0 y 100"
            parametros.comisionPlataforma < 0 || parametros.comisionPlataforma > 100 ->
                "La comisión debe estar entre 0 y 100"
            parametros.montoMinimo < 0 -> "El monto mínimo no puede ser negativo"
            parametros.porcentajeReembolso < 0 || parametros.porcentajeReembolso > 100 ->
                "El porcentaje de reembolso debe estar entre 0 y 100"
            parametros.maxReservasActivas < 1 ->
                "Debe permitirse al menos 1 reserva activa"
            parametros.tiempoGraciaCancelacion < 0 ->
                "El tiempo de gracia no puede ser negativo"
            else -> null // Todo válido
        }
    }

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

    // ========== FUNCIONES DE PLANES ==========

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

    // ✅ ========== FUNCIONES DE PROMOCIONES (NUEVO - 23 Oct 2025) ==========

    /**
     * ✅ NUEVA: Cargar todas las promociones
     */
    fun loadPromociones() {
        viewModelScope.launch {
            _promociones.value = Resource.Loading()
            val result = repository.getAllPromociones()
            _promociones.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyList())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al cargar promociones")
            }
        }
    }

    /**
     * ✅ NUEVA: Crear promoción
     */
    fun crearPromocion(promocion: Promocion) {
        viewModelScope.launch {
            _createPromocionResult.value = Resource.Loading()
            val result = repository.crearPromocion(promocion)
            _createPromocionResult.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: "")
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al crear promoción")
            }
        }
    }

    /**
     * ✅ NUEVA: Actualizar promoción
     */
    fun actualizarPromocion(promocion: Promocion) {
        viewModelScope.launch {
            _updatePromocionResult.value = Resource.Loading()
            val result = repository.actualizarPromocion(promocion)
            _updatePromocionResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al actualizar promoción")
            }
        }
    }

    /**
     * ✅ NUEVA: Eliminar promoción
     */
    fun eliminarPromocion(promocionId: String) {
        viewModelScope.launch {
            _deletePromocionResult.value = Resource.Loading()
            val result = repository.eliminarPromocion(promocionId)
            _deletePromocionResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al eliminar promoción")
            }
        }
    }

    /**
     * ✅ NUEVA: Toggle estado de promoción
     */
    fun togglePromocionStatus(promocionId: String, activo: Boolean) {
        viewModelScope.launch {
            _updatePromocionResult.value = Resource.Loading()
            val result = repository.togglePromocionStatus(promocionId, activo)
            _updatePromocionResult.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al cambiar estado")
            }
        }
    }

    /**
     * ✅ NUEVA: Obtener estadísticas de una promoción
     */
    fun loadEstadisticasPromocion(promocionId: String) {
        viewModelScope.launch {
            _estadisticasPromocion.value = Resource.Loading()
            val result = repository.getEstadisticasPromocion(promocionId)
            _estadisticasPromocion.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull() ?: emptyMap())
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error")
            }
        }
    }
}
