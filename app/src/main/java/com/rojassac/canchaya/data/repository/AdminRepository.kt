package com.rojassac.canchaya.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.CodigoLog
import com.rojassac.canchaya.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val db = FirebaseFirestore.getInstance()

    // ==================== MÉTODOS EXISTENTES (SIN CAMBIOS) ====================

    // ✅ Validar código de invitación y obtener info de la cancha
    suspend fun validarCodigoInvitacion(codigo: String): Result<Cancha> {
        return try {
            // ✅ 1. Buscar el código en la colección codigos_invitacion POR CAMPO
            val querySnapshot = db.collection("codigos_invitacion")
                .whereEqualTo("codigoInvitacion", codigo)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Código no válido"))
            }

            val codigoDoc = querySnapshot.documents[0]

            // 2. Validar que el código esté activo
            val activo = codigoDoc.getBoolean("activa") ?: false // ✅ CORREGIDO: "activa" no "activo"
            if (!activo) {
                return Result.failure(Exception("Código inactivo o ya usado"))
            }

            // 3. Sin validación de fecha de expiración (no expira por tiempo)

            // 4. Validar usos disponibles (USO ÚNICO)
            val usosMaximos = codigoDoc.getLong("usosMaximos")?.toInt() ?: 1
            val usosActuales = codigoDoc.getLong("usosActuales")?.toInt() ?: 0

            if (usosActuales >= usosMaximos) {
                return Result.failure(Exception("Código ya fue utilizado"))
            }

            // 5. Obtener el ID de la cancha vinculada
            val canchaId = codigoDoc.getString("canchaId")
                ?: return Result.failure(Exception("Código sin cancha asociada"))

            // 6. Obtener información de la cancha
            val canchaDoc = db.collection("canchas")
                .document(canchaId)
                .get()
                .await()

            if (!canchaDoc.exists()) {
                return Result.failure(Exception("Cancha no encontrada"))
            }

            val cancha = canchaDoc.toObject(Cancha::class.java)
                ?: return Result.failure(Exception("Error al cargar cancha"))

            Result.success(cancha)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Asignar cancha a admin y MARCAR CÓDIGO COMO USADO
    suspend fun asignarCanchaAAdmin(
        adminId: String,
        canchaId: String,
        codigo: String
    ): Result<Unit> {
        return try {
            // ✅ 1. Buscar el documento del código por campo
            val querySnapshot = db.collection("codigos_invitacion")
                .whereEqualTo("codigoInvitacion", codigo)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Código no encontrado"))
            }

            val codigoDocId = querySnapshot.documents[0].id
            val batch = db.batch()

            // 2. Actualizar usuario: añadir cancha a canchasAsignadas
            val userRef = db.collection("users").document(adminId)
            batch.update(userRef, "canchasAsignadas", FieldValue.arrayUnion(canchaId))

            // 3. Actualizar cancha: marcar como asignada a este admin
            val canchaRef = db.collection("canchas").document(canchaId)
            batch.update(canchaRef, mapOf(
                "adminAsignado" to adminId,
                "adminId" to adminId
            ))

            // ✅ 4. INCREMENTAR USO DEL CÓDIGO (usosActuales + 1)
            val codigoRef = db.collection("codigos_invitacion").document(codigoDocId)
            batch.update(codigoRef, "usosActuales", FieldValue.increment(1))

            // ✅ 5. DESACTIVAR EL CÓDIGO después del primer uso
            batch.update(codigoRef, "activa", false) // ✅ CORREGIDO: "activa" no "activo"

            // 6. Obtener nombre de la cancha para el log
            val canchaDoc = db.collection("canchas").document(canchaId).get().await()
            val canchaNombre = canchaDoc.getString("nombre") ?: "Cancha"

            // 7. Registrar log del uso del código
            val logRef = db.collection("codigo_logs").document()
            val log = CodigoLog(
                id = logRef.id,
                canchaId = canchaId,
                canchaNombre = canchaNombre,
                codigo = codigo,
                accion = "USADO",
                adminAnterior = null,
                adminNuevo = adminId,
                superadminId = "",
                superadminNombre = "",
                timestamp = System.currentTimeMillis(),
                detalles = "Cancha asignada mediante código de invitación (uso único)"
            )

            batch.set(logRef, log)

            // Ejecutar todas las operaciones
            batch.commit().await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Obtener canchas asignadas a un admin
    fun obtenerCanchasAsignadas(adminId: String): Flow<Result<List<Cancha>>> = flow {
        try {
            val userDoc = db.collection("users")
                .document(adminId)
                .get()
                .await()

            if (!userDoc.exists()) {
                emit(Result.failure(Exception("Usuario no encontrado")))
                return@flow
            }

            val canchasAsignadas = (userDoc.get("canchasAsignadas") as? List<*>)
                ?.filterIsInstance<String>() ?: listOf()

            if (canchasAsignadas.isEmpty()) {
                emit(Result.success(listOf()))
                return@flow
            }

            val canchas = mutableListOf<Cancha>()
            for (canchaId in canchasAsignadas) {
                val canchaDoc = db.collection("canchas")
                    .document(canchaId)
                    .get()
                    .await()

                if (canchaDoc.exists()) {
                    canchaDoc.toObject(Cancha::class.java)?.let {
                        canchas.add(it)
                    }
                }
            }

            emit(Result.success(canchas))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // ✅ Verificar si el admin ya tiene asignada esta cancha
    suspend fun adminTieneCanchaAsignada(adminId: String, canchaId: String): Boolean {
        return try {
            val userDoc = db.collection("users")
                .document(adminId)
                .get()
                .await()

            val canchasAsignadas = (userDoc.get("canchasAsignadas") as? List<*>)
                ?.filterIsInstance<String>() ?: listOf()

            canchasAsignadas.contains(canchaId)

        } catch (e: Exception) {
            false
        }
    }

    // ==================== NUEVOS MÉTODOS DE SUSCRIPCIÓN ====================

    /**
     * 🆕 Obtener información de suscripción del admin actual
     */
    suspend fun obtenerSuscripcionAdmin(adminId: String): Result<User> {
        return try {
            val userDoc = db.collection("users")
                .document(adminId)
                .get()
                .await()

            if (!userDoc.exists()) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(Exception("Error al cargar usuario"))

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🆕 Actualizar plan de suscripción del admin
     */
    suspend fun actualizarSuscripcion(
        adminId: String,
        planId: String,
        precioMensual: Double,
        comision: Double,
        pagoId: String,
        metodoPago: String
    ): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val vencimiento = timestamp + (30L * 24 * 60 * 60 * 1000) // 30 días

            val updates = mapOf(
                "planActual" to planId,
                "fechaSuscripcion" to timestamp,
                "fechaVencimiento" to vencimiento,
                "pagoId" to pagoId,
                "metodoPago" to metodoPago,
                "comisionActual" to comision,
                "suscripcionActiva" to true
            )

            db.collection("users")
                .document(adminId)
                .update(updates)
                .await()

            // Registrar el pago en historial
            registrarHistorialPago(
                adminId = adminId,
                planId = planId,
                monto = precioMensual,
                pagoId = pagoId,
                metodoPago = metodoPago,
                timestamp = timestamp
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🆕 Registrar historial de pagos
     */
    private suspend fun registrarHistorialPago(
        adminId: String,
        planId: String,
        monto: Double,
        pagoId: String,
        metodoPago: String,
        timestamp: Long
    ) {
        try {
            val pagoDoc = hashMapOf(
                "adminId" to adminId,
                "planId" to planId,
                "monto" to monto,
                "pagoId" to pagoId,
                "metodoPago" to metodoPago,
                "timestamp" to timestamp,
                "estado" to "COMPLETADO"
            )

            db.collection("historial_pagos")
                .document(pagoId)
                .set(pagoDoc)
                .await()

        } catch (e: Exception) {
            // Log error pero no fallar la transacción principal
            android.util.Log.e("AdminRepository", "Error al registrar historial: ${e.message}")
        }
    }

    /**
     * 🆕 Verificar si la suscripción está vencida
     */
    suspend fun verificarVencimientoSuscripcion(adminId: String): Result<Boolean> {
        return try {
            val userDoc = db.collection("users")
                .document(adminId)
                .get()
                .await()

            val fechaVencimiento = userDoc.getLong("fechaVencimiento")
            val planActual = userDoc.getString("planActual") ?: "BASICO"

            // Plan básico nunca vence
            if (planActual == "BASICO" || fechaVencimiento == null) {
                return Result.success(false)
            }

            val ahora = System.currentTimeMillis()
            val vencido = ahora > fechaVencimiento

            // Si está vencido, revertir a plan básico
            if (vencido) {
                db.collection("users")
                    .document(adminId)
                    .update(
                        mapOf(
                            "planActual" to "BASICO",
                            "comisionActual" to 40.0,
                            "suscripcionActiva" to false
                        )
                    )
                    .await()
            }

            Result.success(vencido)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🆕 Obtener historial de pagos del admin
     */
    fun obtenerHistorialPagos(adminId: String): Flow<Result<List<Map<String, Any>>>> = flow {
        try {
            val pagos = db.collection("historial_pagos")
                .whereEqualTo("adminId", adminId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val listaPagos = pagos.documents.mapNotNull { it.data }
            emit(Result.success(listaPagos))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
