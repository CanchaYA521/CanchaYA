package com.rojassac.canchaya.ui.admin.canchas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.repository.CanchaRepository
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.launch

class CanchaViewModel : ViewModel() {

    private val repository = CanchaRepository()

    private val _canchasState = MutableLiveData<Resource<List<Cancha>>>()
    val canchasState: LiveData<Resource<List<Cancha>>> = _canchasState

    private val _operacionState = MutableLiveData<Resource<String>>()
    val operacionState: LiveData<Resource<String>> = _operacionState

    private val _codigoValidacionState = MutableLiveData<Resource<Cancha>>()
    val codigoValidacionState: LiveData<Resource<Cancha>> = _codigoValidacionState

    // ========== OPERACIONES DE LECTURA ==========

    // Obtener canchas del admin actual
    fun obtenerCanchasPorAdmin(adminId: String) {
        viewModelScope.launch {
            _canchasState.value = Resource.Loading()
            val result = repository.obtenerCanchasPorAdmin(adminId)
            _canchasState.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull()!!)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al obtener canchas")
            }
        }
    }

    // Obtener todas las canchas (SuperAdmin)
    fun obtenerTodasLasCanchas() {
        viewModelScope.launch {
            _canchasState.value = Resource.Loading()
            val result = repository.obtenerTodasLasCanchas()
            _canchasState.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull()!!)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al obtener canchas")
            }
        }
    }

    // ========== OPERACIONES DE ESCRITURA ==========

    // Crear cancha
    fun crearCancha(cancha: Cancha) {
        viewModelScope.launch {
            _operacionState.value = Resource.Loading()
            val result = repository.crearCancha(cancha)
            _operacionState.value = if (result.isSuccess) {
                Resource.Success("Cancha creada exitosamente")
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al crear cancha")
            }
        }
    }

    // Actualizar cancha
    fun actualizarCancha(canchaId: String, cancha: Cancha) {
        viewModelScope.launch {
            _operacionState.value = Resource.Loading()
            val result = repository.actualizarCancha(canchaId, cancha)
            _operacionState.value = if (result.isSuccess) {
                Resource.Success("Cancha actualizada exitosamente")
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al actualizar")
            }
        }
    }

    // Eliminar cancha (soft delete)
    fun eliminarCancha(canchaId: String) {
        viewModelScope.launch {
            _operacionState.value = Resource.Loading()
            val result = repository.eliminarCancha(canchaId)
            _operacionState.value = if (result.isSuccess) {
                Resource.Success("Cancha eliminada")
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al eliminar")
            }
        }
    }

    // Activar/Desactivar cancha
    fun toggleActivoCancha(canchaId: String, activo: Boolean) {
        viewModelScope.launch {
            _operacionState.value = Resource.Loading()
            val result = repository.toggleActivoCancha(canchaId, activo)
            _operacionState.value = if (result.isSuccess) {
                Resource.Success("Estado actualizado")
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Error al actualizar estado")
            }
        }
    }

    // ========== MÉTODOS PARA CÓDIGO DE VINCULACIÓN ==========

    // Generar código único
    fun generarCodigoUnico(callback: (String) -> Unit) {
        viewModelScope.launch {
            var codigo: String
            var existe: Boolean

            // Generar código único (reintentar si ya existe)
            do {
                codigo = repository.generarCodigoVinculacion()
                existe = repository.codigoExiste(codigo)
            } while (existe)

            callback(codigo)
        }
    }

    // Validar código de vinculación (para registro de admin)
    fun validarCodigoVinculacion(codigo: String) {
        viewModelScope.launch {
            _codigoValidacionState.value = Resource.Loading()
            val result = repository.validarCodigoVinculacion(codigo)
            _codigoValidacionState.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull()!!)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Código no válido")
            }
        }
    }

    // Vincular cancha con admin (después del registro)
    fun vincularCanchaConAdmin(canchaId: String, adminId: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.vincularCanchaConAdmin(canchaId, adminId)
            if (result.isSuccess) {
                callback(true, "Cancha vinculada exitosamente")
            } else {
                callback(false, result.exceptionOrNull()?.message ?: "Error al vincular")
            }
        }
    }
}
