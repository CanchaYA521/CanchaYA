package com.rojassac.canchaya.ui.admin.reservas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojassac.canchaya.data.model.EstadoReserva
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.data.repository.ReservasRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReservasViewModel : ViewModel() {

    private val repository = ReservasRepository()

    private val _reservas = MutableLiveData<List<Reserva>>()
    val reservas: LiveData<List<Reserva>> = _reservas

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _filtroEstado = MutableLiveData<EstadoReserva?>(null)
    val filtroEstado: LiveData<EstadoReserva?> = _filtroEstado

    /**
     * Cargar todas las reservas de una cancha
     */
    fun cargarReservas(canchaId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val todasReservas = repository.obtenerReservasPorCancha(canchaId)
                aplicarFiltros(todasReservas)
                _loading.value = false
            } catch (e: Exception) {
                _error.value = "Error al cargar reservas: ${e.message}"
                _loading.value = false
            }
        }
    }

    /**
     * Cargar reservas por fecha específica
     */
    fun cargarReservasPorFecha(canchaId: String, fecha: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val reservasFecha = repository.obtenerReservasPorCanchaYFecha(canchaId, fecha)
                aplicarFiltros(reservasFecha)
                _loading.value = false
            } catch (e: Exception) {
                _error.value = "Error al cargar reservas: ${e.message}"
                _loading.value = false
            }
        }
    }

    /**
     * Filtrar por estado
     */
    fun filtrarPorEstado(estado: EstadoReserva?) {
        _filtroEstado.value = estado
        _reservas.value?.let { aplicarFiltros(it) }
    }

    /**
     * Aplicar filtros a la lista
     */
    private fun aplicarFiltros(reservas: List<Reserva>) {
        val filtradas = when (_filtroEstado.value) {
            null -> reservas
            else -> reservas.filter { it.estado == _filtroEstado.value }
        }
        _reservas.value = filtradas.sortedByDescending { it.fechaCreacion }
    }

    /**
     * Actualizar estado de una reserva
     */
    fun actualizarEstado(reservaId: String, nuevoEstado: EstadoReserva, canchaId: String) {
        viewModelScope.launch {
            try {
                val exito = repository.actualizarEstadoReserva(reservaId, nuevoEstado)
                if (exito) {
                    cargarReservas(canchaId)
                } else {
                    _error.value = "No se pudo actualizar el estado"
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            }
        }
    }

    /**
     * Contar reservas por estado
     */
    fun contarPorEstado(estado: EstadoReserva): Int {
        return _reservas.value?.count { it.estado == estado } ?: 0
    }
}
