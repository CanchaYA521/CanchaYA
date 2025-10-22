package com.rojassac.canchaya.ui.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojassac.canchaya.data.model.EstadoSuscripcion
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.data.model.Subscription
import com.rojassac.canchaya.data.repository.CodigoRepository // ✅ NUEVO IMPORT
import com.rojassac.canchaya.data.repository.SubscriptionRepository
import com.rojassac.canchaya.data.repository.ValidacionCodigoResult // ✅ NUEVO IMPORT
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val subscriptionRepository = SubscriptionRepository()
    private val codigoRepository = CodigoRepository() // ✅ NUEVO: Instancia del repositorio

    companion object {
        private const val TAG = "AdminViewModel"
    }

    // Planes disponibles
    private val _planes = MutableLiveData<List<Plan>>()
    val planes: LiveData<List<Plan>> = _planes

    // Suscripción actual
    private val _suscripcionActual = MutableLiveData<Subscription?>()
    val suscripcionActual: LiveData<Subscription?> = _suscripcionActual

    // Plan actual
    private val _planActual = MutableLiveData<Plan?>()
    val planActual: LiveData<Plan?> = _planActual

    // ✅ NUEVO: Estado del cambio de plan
    private val _cambioPlanExitoso = MutableLiveData<Boolean>()
    val cambioPlanExitoso: LiveData<Boolean> = _cambioPlanExitoso

    // ========== ✅ NUEVO: VALIDACIÓN DE CÓDIGO DE INVITACIÓN ==========
    private val _validacionCodigo = MutableLiveData<ValidacionCodigoResult>()
    val validacionCodigo: LiveData<ValidacionCodigoResult> = _validacionCodigo

    private val _isValidatingCodigo = MutableLiveData<Boolean>()
    val isValidatingCodigo: LiveData<Boolean> = _isValidatingCodigo
    // ========== FIN NUEVO ==========

    /**
     * Carga todos los planes disponibles
     */
    fun cargarPlanes() {
        viewModelScope.launch {
            try {
                val resultado = subscriptionRepository.obtenerPlanes()
                if (resultado.isSuccess) {
                    _planes.postValue(resultado.getOrNull() ?: emptyList())
                    Log.d(TAG, "✅ Planes cargados: ${resultado.getOrNull()?.size}")
                } else {
                    Log.e(TAG, "❌ Error al cargar planes", resultado.exceptionOrNull())
                    _planes.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado al cargar planes", e)
                _planes.postValue(emptyList())
            }
        }
    }

    /**
     * Carga la suscripción activa del usuario
     */
    fun cargarSuscripcionActual(userId: String) {
        viewModelScope.launch {
            try {
                val resultado = subscriptionRepository.obtenerSuscripcionActiva(userId)
                if (resultado.isSuccess) {
                    val suscripcion = resultado.getOrNull()
                    _suscripcionActual.postValue(suscripcion)
                    // Cargar el plan asociado
                    suscripcion?.let {
                        cargarPlan(it.planId)
                    }
                } else {
                    Log.e(TAG, "❌ Error al obtener suscripción", resultado.exceptionOrNull())
                    _suscripcionActual.postValue(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado al cargar suscripción", e)
                _suscripcionActual.postValue(null)
            }
        }
    }

    /**
     * Carga un plan específico por ID
     */
    private fun cargarPlan(planId: String) {
        viewModelScope.launch {
            try {
                val resultado = subscriptionRepository.obtenerPlan(planId)
                if (resultado.isSuccess) {
                    _planActual.postValue(resultado.getOrNull())
                } else {
                    Log.e(TAG, "❌ Error al obtener plan", resultado.exceptionOrNull())
                    _planActual.postValue(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado al cargar plan", e)
                _planActual.postValue(null)
            }
        }
    }

    /**
     * ✅ MEJORADO: Cambia el plan de suscripción con feedback
     */
    fun cambiarPlan(userId: String, nuevoPlanId: String) {
        viewModelScope.launch {
            try {
                val suscripcionActual = subscriptionRepository.obtenerSuscripcionActiva(userId)
                    .getOrNull()

                if (suscripcionActual != null) {
                    // Actualizar suscripción existente
                    val resultado = subscriptionRepository.cambiarPlan(
                        suscripcionActual.id,
                        nuevoPlanId
                    )

                    if (resultado.isSuccess) {
                        Log.d(TAG, "✅ Plan cambiado exitosamente")
                        _cambioPlanExitoso.postValue(true)
                        cargarSuscripcionActual(userId)
                    } else {
                        Log.e(TAG, "❌ Error al cambiar plan", resultado.exceptionOrNull())
                        _cambioPlanExitoso.postValue(false)
                    }
                } else {
                    // Crear nueva suscripción
                    val nuevaSuscripcion = Subscription(
                        userId = userId,
                        planId = nuevoPlanId,
                        estado = EstadoSuscripcion.ACTIVA,
                        fechaInicio = System.currentTimeMillis(),
                        fechaVencimiento = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                        autoRenovacion = true
                    )

                    val resultado = subscriptionRepository.crearSuscripcion(nuevaSuscripcion)
                    if (resultado.isSuccess) {
                        Log.d(TAG, "✅ Suscripción creada exitosamente")
                        _cambioPlanExitoso.postValue(true)
                        cargarSuscripcionActual(userId)
                    } else {
                        Log.e(TAG, "❌ Error al crear suscripción", resultado.exceptionOrNull())
                        _cambioPlanExitoso.postValue(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado al cambiar plan", e)
                _cambioPlanExitoso.postValue(false)
            }
        }
    }

    /**
     * Cancela la suscripción actual
     */
    fun cancelarSuscripcion(userId: String, motivo: String) {
        viewModelScope.launch {
            try {
                val suscripcion = subscriptionRepository.obtenerSuscripcionActiva(userId)
                    .getOrNull()

                if (suscripcion != null) {
                    val resultado = subscriptionRepository.cancelarSuscripcion(
                        suscripcion.id,
                        motivo
                    )

                    if (resultado.isSuccess) {
                        Log.d(TAG, "✅ Suscripción cancelada")
                        cargarSuscripcionActual(userId)
                    } else {
                        Log.e(TAG, "❌ Error al cancelar suscripción", resultado.exceptionOrNull())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado al cancelar suscripción", e)
            }
        }
    }

    // ========== ✅ NUEVO: FUNCIONES DE VALIDACIÓN DE CÓDIGO ==========

    /**
     * Valida un código de invitación
     * @param codigo El código a validar (será convertido a uppercase)
     */
    fun validarCodigoInvitacion(codigo: String) {
        viewModelScope.launch {
            try {
                _isValidatingCodigo.postValue(true)
                Log.d(TAG, "🔄 Iniciando validación de código: $codigo")

                val resultado = codigoRepository.validarCodigoInvitacion(codigo.trim().uppercase())
                _validacionCodigo.postValue(resultado)

                Log.d(TAG, if (resultado.exito) {
                    "✅ Código validado exitosamente"
                } else {
                    "❌ Error en validación: ${resultado.mensaje}"
                })
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado al validar código", e)
                _validacionCodigo.postValue(
                    ValidacionCodigoResult(
                        exito = false,
                        mensaje = "Error inesperado: ${e.message}"
                    )
                )
            } finally {
                _isValidatingCodigo.postValue(false)
            }
        }
    }

    /**
     * Resetea el estado de validación de código
     * Llamar después de procesar el resultado
     */
    fun resetValidacionCodigo() {
        _validacionCodigo.postValue(null)
    }

    // ========== FIN NUEVO ==========
}
