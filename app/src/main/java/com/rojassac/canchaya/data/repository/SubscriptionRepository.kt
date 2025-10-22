package com.rojassac.canchaya.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rojassac.canchaya.data.model.EstadoSuscripcion
import com.rojassac.canchaya.data.model.Plan
import com.rojassac.canchaya.data.model.Subscription
import com.rojassac.canchaya.utils.Constants
import kotlinx.coroutines.tasks.await

class SubscriptionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val subscriptionsCollection = db.collection("subscriptions")
    private val plansCollection = db.collection("plans")

    companion object {
        private const val TAG = "SubscriptionRepository"
    }

    // ========================
    // PLANES
    // ========================

    /**
     * Obtiene todos los planes disponibles ordenados por orden
     */
    suspend fun obtenerPlanes(): Result<List<Plan>> {
        return try {
            Log.d(TAG, "Obteniendo planes...")
            val snapshot = db.collection(Constants.PLANS_COLLECTION)
                .get()
                .await()

            val planes = snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(Plan::class.java)?.copy(id = doc.id)?.also { plan ->
                        Log.d(TAG, "Plan cargado: ${plan.nombre} (orden: ${plan.orden}, activo: ${plan.activo})")
                    }
                }
                .filter { it.activo }
                .sortedBy { it.orden }

            Log.d(TAG, "Planes obtenidos: ${planes.size}")
            Result.success(planes)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener planes", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un plan específico por ID
     * ✅ CORREGIDO: Validación mejorada
     */
    suspend fun obtenerPlan(planId: String): Result<Plan> {
        return try {
            // ✅ Validar que planId no sea inválido
            if (planId.isBlank() || planId == "plans") {
                Log.w(TAG, "⚠️ Plan ID inválido: $planId, retornando plan básico")
                return Result.success(
                    Plan(
                        id = Constants.PLAN_BASICO,
                        nombre = "Básico",
                        precio = 0.0,
                        comision = 0.40,
                        descripcion = "Plan gratuito",
                        caracteristicas = listOf("40% comisión", "Retiro en 7 días"),
                        orden = 0,
                        activo = true
                    )
                )
            }

            val doc = plansCollection.document(planId).get().await()

            if (!doc.exists()) {
                Log.e(TAG, "❌ Plan no encontrado: $planId")
                return Result.failure(Exception("Plan no encontrado"))
            }

            val plan = doc.toObject(Plan::class.java)?.copy(id = doc.id)
                ?: return Result.failure(Exception("Error al parsear plan"))

            Log.d(TAG, "✅ Plan obtenido: ${plan.nombre}")
            Result.success(plan)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener plan $planId", e)
            Result.failure(e)
        }
    }

    // ========================
    // SUSCRIPCIONES
    // ========================

    /**
     * Obtiene la suscripción activa de un usuario
     */
    suspend fun obtenerSuscripcionActiva(userId: String): Result<Subscription?> {
        return try {
            val snapshot = subscriptionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("estado", EstadoSuscripcion.ACTIVA.name)
                .limit(1)
                .get()
                .await()

            val suscripcion = snapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(Subscription::class.java)?.copy(id = doc.id)
            }

            Log.d(TAG, "✅ Suscripción activa: ${suscripcion?.planId ?: "ninguna"}")
            Result.success(suscripcion)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener suscripción activa", e)
            Result.failure(e)
        }
    }

    /**
     * Crea una nueva suscripción
     */
    suspend fun crearSuscripcion(subscription: Subscription): Result<String> {
        return try {
            // Cancelar suscripciones activas anteriores
            cancelarSuscripcionesActivas(subscription.userId)

            // Crear nueva suscripción
            val docRef = subscriptionsCollection.document()
            val nuevaSuscripcion = subscription.copy(
                id = docRef.id,
                fechaCreacion = System.currentTimeMillis(),
                fechaActualizacion = System.currentTimeMillis()
            )

            docRef.set(nuevaSuscripcion).await()
            Log.d(TAG, "✅ Suscripción creada: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear suscripción", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza una suscripción existente
     */
    suspend fun actualizarSuscripcion(subscriptionId: String, updates: Map<String, Any>): Result<Boolean> {
        return try {
            val updatesWithTimestamp = updates.toMutableMap().apply {
                put("fechaActualizacion", System.currentTimeMillis())
            }

            subscriptionsCollection.document(subscriptionId)
                .update(updatesWithTimestamp)
                .await()

            Log.d(TAG, "✅ Suscripción actualizada: $subscriptionId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar suscripción", e)
            Result.failure(e)
        }
    }

    /**
     * Cancela una suscripción
     */
    suspend fun cancelarSuscripcion(
        subscriptionId: String,
        motivo: String = "Usuario canceló"
    ): Result<Boolean> {
        return try {
            val updates = mapOf(
                "estado" to EstadoSuscripcion.CANCELADA.name,
                "canceladaFecha" to System.currentTimeMillis(),
                "motivoCancelacion" to motivo,
                "autoRenovacion" to false,
                "fechaActualizacion" to System.currentTimeMillis()
            )

            subscriptionsCollection.document(subscriptionId)
                .update(updates)
                .await()

            Log.d(TAG, "✅ Suscripción cancelada: $subscriptionId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al cancelar suscripción", e)
            Result.failure(e)
        }
    }

    /**
     * Cancela todas las suscripciones activas de un usuario
     */
    private suspend fun cancelarSuscripcionesActivas(userId: String) {
        try {
            val snapshot = subscriptionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("estado", EstadoSuscripcion.ACTIVA.name)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                cancelarSuscripcion(doc.id, "Nueva suscripción creada")
            }

            Log.d(TAG, "✅ Suscripciones anteriores canceladas")
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Error al cancelar suscripciones anteriores", e)
        }
    }

    /**
     * Cambia el plan de una suscripción (upgrade/downgrade)
     */
    suspend fun cambiarPlan(
        subscriptionId: String,
        nuevoPlanId: String
    ): Result<Boolean> {
        return try {
            val updates = mapOf(
                "planId" to nuevoPlanId,
                "fechaActualizacion" to System.currentTimeMillis()
            )

            subscriptionsCollection.document(subscriptionId)
                .update(updates)
                .await()

            Log.d(TAG, "✅ Plan cambiado a: $nuevoPlanId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al cambiar plan", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica y actualiza suscripciones vencidas
     */
    suspend fun verificarSuscripcionesVencidas() {
        try {
            val ahora = System.currentTimeMillis()
            val snapshot = subscriptionsCollection
                .whereEqualTo("estado", EstadoSuscripcion.ACTIVA.name)
                .whereLessThan("fechaVencimiento", ahora)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                actualizarSuscripcion(
                    doc.id,
                    mapOf("estado" to EstadoSuscripcion.VENCIDA.name)
                )
            }

            Log.d(TAG, "✅ Verificación de suscripciones vencidas: ${snapshot.size()} actualizadas")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al verificar suscripciones vencidas", e)
        }
    }

    /**
     * Obtiene historial de suscripciones de un usuario
     */
    suspend fun obtenerHistorialSuscripciones(userId: String): Result<List<Subscription>> {
        return try {
            val snapshot = subscriptionsCollection
                .whereEqualTo("userId", userId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val suscripciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subscription::class.java)?.copy(id = doc.id)
            }

            Log.d(TAG, "✅ Historial obtenido: ${suscripciones.size} suscripciones")
            Result.success(suscripciones)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener historial", e)
            Result.failure(e)
        }
    }
}
