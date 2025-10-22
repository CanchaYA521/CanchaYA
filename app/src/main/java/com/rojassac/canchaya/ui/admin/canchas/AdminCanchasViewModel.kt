package com.rojassac.canchaya.ui.admin.canchas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.repository.CanchaRepository
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.launch

class AdminCanchasViewModel : ViewModel() {

    private val repository = CanchaRepository()

    private val _canchasState = MutableLiveData<Resource<List<Cancha>>>()
    val canchasState: LiveData<Resource<List<Cancha>>> = _canchasState

    private val _operacionState = MutableLiveData<Resource<String>>()
    val operacionState: LiveData<Resource<String>> = _operacionState

    // Cargar todas las canchas (incluye inactivas para admin)
    fun cargarTodasLasCanchas() {
        viewModelScope.launch {
            _canchasState.value = Resource.Loading()
            val result = repository.obtenerTodasLasCanchas()

            if (result.isSuccess) {
                _canchasState.value = Resource.Success(result.getOrNull()!!)
            } else {
                _canchasState.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Error al cargar canchas"
                )
            }
        }
    }

    // Crear nueva cancha
    fun crearCancha(cancha: Cancha) {
        viewModelScope.launch {
            _operacionState.value = Resource.Loading()
            val result = repository.crearCancha(cancha)

            if (result.isSuccess) {
                _operacionState.value = Resource.Success("Cancha creada exitosamente")
                cargarTodasLasCanchas() // Recargar lista
            } else {
                _operacionState.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Error al crear cancha"
                )
            }
        }
    }

    // Actualizar cancha
    fun actualizarCancha(canchaId: String, cancha: Cancha) {
        viewModelScope.launch {
            _operacionState.value = Resource.Loading()
            val result = repository.actualizarCancha(canchaId, cancha)

            if (result.isSuccess) {
                _operacionState.value = Resource.Success("Cancha actualizada exitosamente")
                cargarTodasLasCanchas() // Recargar lista
            } else {
                _operacionState.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar cancha"
                )
            }
        }
    }

    // Eliminar cancha (soft delete)
    fun eliminarCancha(canchaId: String) {
        viewModelScope.launch {
            _operacionState.value = Resource.Loading()
            val result = repository.eliminarCancha(canchaId)

            if (result.isSuccess) {
                _operacionState.value = Resource.Success("Cancha eliminada exitosamente")
                cargarTodasLasCanchas() // Recargar lista
            } else {
                _operacionState.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Error al eliminar cancha"
                )
            }
        }
    }

    // Activar/Desactivar cancha
    fun toggleActivoCancha(canchaId: String, activo: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleActivoCancha(canchaId, activo)

            if (result.isSuccess) {
                cargarTodasLasCanchas() // Recargar lista
            } else {
                _operacionState.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Error al cambiar estado"
                )
            }
        }
    }
}
