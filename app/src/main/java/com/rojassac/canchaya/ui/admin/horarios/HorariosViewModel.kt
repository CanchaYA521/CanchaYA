package com.rojassac.canchaya.ui.admin.horarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojassac.canchaya.data.model.EstadoHorario
import com.rojassac.canchaya.data.model.HorarioSlot
import com.rojassac.canchaya.data.repository.HorariosRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HorariosViewModel : ViewModel() {

    private val repository = HorariosRepository()

    private val _horarios = MutableLiveData<List<HorarioSlot>>()
    val horarios: LiveData<List<HorarioSlot>> = _horarios

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Horarios por defecto (08:00 - 22:00)
    private val horariosDefault = listOf(
        "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
        "14:00", "15:00", "16:00", "17:00", "18:00", "19:00",
        "20:00", "21:00", "22:00"
    )

    fun cargarHorarios(canchaId: String, fecha: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val horariosOcupados = repository.obtenerHorariosOcupados(canchaId, fecha)

                // ✅ Calcular estado de cada horario
                val listaHorarios = horariosDefault.map { hora ->
                    val estado = calcularEstado(hora, fecha, horariosOcupados.contains(hora))

                    HorarioSlot(
                        hora = hora,
                        disponible = !horariosOcupados.contains(hora),
                        estado = estado
                    )
                }

                _horarios.value = listaHorarios
                _loading.value = false

            } catch (e: Exception) {
                _error.value = "Error al cargar horarios: ${e.message}"
                _loading.value = false
            }
        }
    }

    /**
     * ✅ Calcula el estado del horario según la hora actual
     */
    private fun calcularEstado(hora: String, fecha: String, estaOcupado: Boolean): EstadoHorario {
        try {
            // Combinar fecha + hora
            val formatoCompleto = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val fechaHora = formatoCompleto.parse("$fecha $hora")
            val ahora = Calendar.getInstance().time

            return when {
                // ⚫ Pasado: Ya transcurrió
                fechaHora != null && fechaHora.before(ahora) -> EstadoHorario.PASADO

                // 🔴 Ocupado: Tiene reserva
                estaOcupado -> EstadoHorario.OCUPADO

                // 🟢 Disponible: Futuro y sin reserva
                else -> EstadoHorario.DISPONIBLE
            }
        } catch (e: Exception) {
            return if (estaOcupado) EstadoHorario.OCUPADO else EstadoHorario.DISPONIBLE
        }
    }

    fun actualizarEstadoHorario(canchaId: String, fecha: String, horario: HorarioSlot) {
        // ❌ No permitir cambiar horarios pasados
        if (horario.estado == EstadoHorario.PASADO) {
            _error.value = "No puedes modificar horarios pasados"
            return
        }

        viewModelScope.launch {
            try {
                // Cambiar estado en Firestore
                if (horario.disponible) {
                    // Marcar como ocupado
                    repository.marcarHorarioOcupado(canchaId, fecha, horario.hora)
                } else {
                    // Marcar como disponible
                    repository.marcarHorarioDisponible(canchaId, fecha, horario.hora)
                }

                // Recargar horarios
                cargarHorarios(canchaId, fecha)

            } catch (e: Exception) {
                _error.value = "Error al actualizar horario: ${e.message}"
            }
        }
    }
}
