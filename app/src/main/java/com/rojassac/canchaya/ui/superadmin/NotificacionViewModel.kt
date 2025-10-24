package com.rojassac.canchaya.ui.superadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rojassac.canchaya.data.model.*
import com.rojassac.canchaya.data.repository.NotificacionRepository
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ✅ NUEVO (24 Oct 2025)
 * ViewModel para gestionar notificaciones masivas
 * SIN HILT - Inyección manual
 */
class NotificacionViewModel(
    private val repository: NotificacionRepository
) : ViewModel() {

    // ═══════════════════════════════════════════════════════════════
    // 📋 LISTADOS DE NOTIFICACIONES
    // ═══════════════════════════════════════════════════════════════

    private val _notificaciones = MutableLiveData<Resource<List<NotificacionMasiva>>>()
    val notificaciones: LiveData<Resource<List<NotificacionMasiva>>> = _notificaciones

    private val _notificacionesProgramadas = MutableLiveData<Resource<List<NotificacionMasiva>>>()
    val notificacionesProgramadas: LiveData<Resource<List<NotificacionMasiva>>> = _notificacionesProgramadas

    private val _notificacionSeleccionada = MutableLiveData<Resource<NotificacionMasiva>>()
    val notificacionSeleccionada: LiveData<Resource<NotificacionMasiva>> = _notificacionSeleccionada

    // ═══════════════════════════════════════════════════════════════
    // ✍️ OPERACIONES DE CREACIÓN Y ENVÍO
    // ═══════════════════════════════════════════════════════════════

    private val _crearNotificacionResult = MutableLiveData<Resource<String>>()
    val crearNotificacionResult: LiveData<Resource<String>> = _crearNotificacionResult

    private val _enviarNotificacionResult = MutableLiveData<Resource<Int>>()
    val enviarNotificacionResult: LiveData<Resource<Int>> = _enviarNotificacionResult

    private val _cancelarNotificacionResult = MutableLiveData<Resource<Unit>>()
    val cancelarNotificacionResult: LiveData<Resource<Unit>> = _cancelarNotificacionResult

    // ═══════════════════════════════════════════════════════════════
    // 📊 ESTADÍSTICAS
    // ═══════════════════════════════════════════════════════════════

    private val _estadisticasGenerales = MutableLiveData<Resource<Map<String, Any>>>()
    val estadisticasGenerales: LiveData<Resource<Map<String, Any>>> = _estadisticasGenerales

    private val _tasaAperturaPromedio = MutableLiveData<Resource<Double>>()
    val tasaAperturaPromedio: LiveData<Resource<Double>> = _tasaAperturaPromedio

    // ═══════════════════════════════════════════════════════════════
    // 📝 FUNCIONES DE CARGA DE DATOS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Cargar todas las notificaciones
     */
    fun cargarNotificaciones() {
        viewModelScope.launch {
            _notificaciones.value = Resource.Loading()
            val resultado = repository.obtenerTodasNotificaciones()
            _notificaciones.value = resultado
        }
    }

    /**
     * Cargar notificaciones por estado
     */
    fun cargarNotificacionesPorEstado(estado: EstadoNotificacion) {
        viewModelScope.launch {
            _notificaciones.value = Resource.Loading()
            val resultado = repository.obtenerNotificacionesPorEstado(estado)
            _notificaciones.value = resultado
        }
    }

    /**
     * Cargar notificaciones programadas
     */
    fun cargarNotificacionesProgramadas() {
        viewModelScope.launch {
            _notificacionesProgramadas.value = Resource.Loading()
            val resultado = repository.obtenerNotificacionesProgramadas()
            _notificacionesProgramadas.value = resultado
        }
    }

    /**
     * Cargar una notificación específica
     */
    fun cargarNotificacion(notificacionId: String) {
        viewModelScope.launch {
            _notificacionSeleccionada.value = Resource.Loading()
            val resultado = repository.obtenerNotificacionPorId(notificacionId)
            _notificacionSeleccionada.value = resultado
        }
    }

    /**
     * Buscar notificaciones por texto
     */
    fun buscarNotificaciones(query: String) {
        viewModelScope.launch {
            _notificaciones.value = Resource.Loading()
            val resultado = repository.buscarNotificaciones(query)
            _notificaciones.value = resultado
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ✍️ FUNCIONES DE CREACIÓN Y ENVÍO
    // ═══════════════════════════════════════════════════════════════

    /**
     * Crear y enviar notificación inmediatamente
     */
    fun crearYEnviarNotificacion(
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion,
        destinatarios: DestinatariosType,
        urlDestino: String? = null,
        imagenUrl: String? = null,
        sedeId: String? = null,
        prioridad: PrioridadNotificacion = PrioridadNotificacion.NORMAL
    ) {
        viewModelScope.launch {
            _crearNotificacionResult.value = Resource.Loading()

            // 1. Validar datos
            val error = validarDatosNotificacion(titulo, mensaje, destinatarios, sedeId)
            if (error != null) {
                _crearNotificacionResult.value = Resource.Error(error)
                return@launch
            }

            // 2. Crear notificación
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "SuperAdmin"

            val notificacion = NotificacionMasiva(
                titulo = titulo,
                mensaje = mensaje,
                tipo = tipo.name,
                urlDestino = urlDestino,
                imagenUrl = imagenUrl,
                destinatarios = destinatarios.name,
                sedeId = sedeId,
                envioInmediato = true,
                fechaProgramada = null,
                estado = EstadoNotificacion.PENDIENTE.name,
                creadoPor = userId,
                nombreCreador = userName,
                prioridad = prioridad.name
            )

            // 3. Guardar y enviar
            val resultCrear = repository.crearNotificacion(notificacion)
            if (resultCrear is Resource.Success) {
                val notifId = resultCrear.data ?: ""

                // Enviar inmediatamente
                val resultEnviar = repository.enviarNotificacionInmediata(notifId)
                if (resultEnviar is Resource.Success) {
                    _crearNotificacionResult.value = Resource.Success(notifId)
                    _enviarNotificacionResult.value = resultEnviar
                    cargarNotificaciones()
                } else if (resultEnviar is Resource.Error) {
                    _enviarNotificacionResult.value = resultEnviar
                }
            } else if (resultCrear is Resource.Error) {
                _crearNotificacionResult.value = resultCrear
            }
        }
    }

    /**
     * Crear notificación programada
     */
    fun crearNotificacionProgramada(
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion,
        destinatarios: DestinatariosType,
        fechaProgramada: Date,
        urlDestino: String? = null,
        imagenUrl: String? = null,
        sedeId: String? = null,
        prioridad: PrioridadNotificacion = PrioridadNotificacion.NORMAL
    ) {
        viewModelScope.launch {
            _crearNotificacionResult.value = Resource.Loading()

            // 1. Validar datos
            val error = validarDatosNotificacion(titulo, mensaje, destinatarios, sedeId)
            if (error != null) {
                _crearNotificacionResult.value = Resource.Error(error)
                return@launch
            }

            // Validar fecha futura
            if (fechaProgramada.before(Date())) {
                _crearNotificacionResult.value = Resource.Error("La fecha programada debe ser futura")
                return@launch
            }

            // 2. Crear notificación
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "SuperAdmin"

            val notificacion = NotificacionMasiva(
                titulo = titulo,
                mensaje = mensaje,
                tipo = tipo.name,
                urlDestino = urlDestino,
                imagenUrl = imagenUrl,
                destinatarios = destinatarios.name,
                sedeId = sedeId,
                envioInmediato = false,
                fechaProgramada = fechaProgramada,
                estado = EstadoNotificacion.PROGRAMADA.name,
                creadoPor = userId,
                nombreCreador = userName,
                prioridad = prioridad.name
            )

            // 3. Guardar
            val result = repository.crearNotificacion(notificacion)
            _crearNotificacionResult.value = result

            if (result is Resource.Success) {
                cargarNotificaciones()
            }
        }
    }

    /**
     * Cancelar notificación programada
     */
    fun cancelarNotificacion(notificacionId: String) {
        viewModelScope.launch {
            _cancelarNotificacionResult.value = Resource.Loading()
            val resultado = repository.cancelarNotificacion(notificacionId)
            _cancelarNotificacionResult.value = resultado

            if (resultado is Resource.Success) {
                cargarNotificaciones()
            }
        }
    }

    /**
     * Duplicar notificación
     */
    fun duplicarNotificacion(notificacionId: String) {
        viewModelScope.launch {
            _crearNotificacionResult.value = Resource.Loading()
            val resultado = repository.duplicarNotificacion(notificacionId)
            _crearNotificacionResult.value = resultado

            if (resultado is Resource.Success) {
                cargarNotificaciones()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 📊 FUNCIONES DE ESTADÍSTICAS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Cargar estadísticas generales
     */
    fun cargarEstadisticasGenerales() {
        viewModelScope.launch {
            _estadisticasGenerales.value = Resource.Loading()
            val resultado = repository.obtenerEstadisticasGenerales()
            _estadisticasGenerales.value = resultado
        }
    }

    /**
     * Cargar tasa de apertura promedio
     */
    fun cargarTasaAperturaPromedio() {
        viewModelScope.launch {
            _tasaAperturaPromedio.value = Resource.Loading()
            val resultado = repository.obtenerTasaAperturaPromedio()
            _tasaAperturaPromedio.value = resultado
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔍 FUNCIONES DE VALIDACIÓN
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validar datos de notificación
     */
    private fun validarDatosNotificacion(
        titulo: String,
        mensaje: String,
        destinatarios: DestinatariosType,
        sedeId: String?
    ): String? {
        return when {
            titulo.isEmpty() ->
                "El título es obligatorio"
            titulo.length < 3 ->
                "El título debe tener al menos 3 caracteres"
            titulo.length > 50 ->
                "El título no puede tener más de 50 caracteres"
            mensaje.isEmpty() ->
                "El mensaje es obligatorio"
            mensaje.length < 10 ->
                "El mensaje debe tener al menos 10 caracteres"
            mensaje.length > 200 ->
                "El mensaje no puede tener más de 200 caracteres"
            destinatarios == DestinatariosType.SEDE_ESPECIFICA && sedeId.isNullOrEmpty() ->
                "Debe seleccionar una sede"
            else -> null
        }
    }

    /**
     * Obtener texto descriptivo del tipo de notificación
     */
    fun obtenerDescripcionTipo(tipo: TipoNotificacion): String {
        return when (tipo) {
            TipoNotificacion.INFO -> "Información general"
            TipoNotificacion.ALERTA -> "Alerta importante"
            TipoNotificacion.PROMOCION -> "Promoción o descuento"
            TipoNotificacion.SISTEMA -> "Actualización del sistema"
            TipoNotificacion.EVENTO -> "Evento especial"
        }
    }

    /**
     * Obtener color del estado
     */
    fun obtenerColorEstado(estado: EstadoNotificacion): String {
        return estado.color
    }
}
