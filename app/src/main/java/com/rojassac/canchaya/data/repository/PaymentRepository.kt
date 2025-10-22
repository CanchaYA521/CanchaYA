package com.rojassac.canchaya.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rojassac.canchaya.data.model.CargoResponse
import kotlinx.coroutines.tasks.await

class PaymentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val cargosCollection = db.collection("cargos")

    companion object {
        private const val TAG = "PaymentRepository"
    }

    /**
     * Guarda un cargo exitoso en Firestore para historial
     */
    suspend fun guardarCargo(cargo: CargoResponse, userId: String, tipo: String): Result<String> {
        return try {
            val cargoData = hashMapOf(
                "id" to cargo.id,
                "userId" to userId,
                "monto" to cargo.monto,
                "moneda" to cargo.moneda,
                "estado" to cargo.estado,
                "fechaCreacion" to cargo.fechaCreacion,
                "descripcion" to cargo.descripcion,
                "email" to cargo.email,
                "ultimos4Digitos" to cargo.ultimos4Digitos,
                "tipo" to tipo, // "reserva", "suscripcion"
                "metadata" to cargo.metadata
            )

            val docRef = cargosCollection.document(cargo.id)
            docRef.set(cargoData).await()

            Log.d(TAG, "✅ Cargo guardado: ${cargo.id}")
            Result.success(cargo.id)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar cargo", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un cargo por ID
     */
    suspend fun obtenerCargo(cargoId: String): Result<CargoResponse> {
        return try {
            val doc = cargosCollection.document(cargoId).get().await()

            if (!doc.exists()) {
                return Result.failure(Exception("Cargo no encontrado"))
            }

            val cargo = CargoResponse(
                id = doc.getString("id") ?: "",
                monto = doc.getDouble("monto") ?: 0.0,
                moneda = doc.getString("moneda") ?: "PEN",
                estado = doc.getString("estado") ?: "",
                fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                descripcion = doc.getString("descripcion") ?: "",
                email = doc.getString("email") ?: "",
                ultimos4Digitos = doc.getString("ultimos4Digitos") ?: "",
                metadata = (doc.get("metadata") as? Map<String, String>) ?: mapOf()
            )

            Log.d(TAG, "✅ Cargo obtenido: $cargoId")
            Result.success(cargo)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener cargo", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene historial de cargos de un usuario
     */
    suspend fun obtenerHistorialCargos(userId: String): Result<List<CargoResponse>> {
        return try {
            val snapshot = cargosCollection
                .whereEqualTo("userId", userId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val cargos = snapshot.documents.mapNotNull { doc ->
                try {
                    CargoResponse(
                        id = doc.getString("id") ?: "",
                        monto = doc.getDouble("monto") ?: 0.0,
                        moneda = doc.getString("moneda") ?: "PEN",
                        estado = doc.getString("estado") ?: "",
                        fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                        descripcion = doc.getString("descripcion") ?: "",
                        email = doc.getString("email") ?: "",
                        ultimos4Digitos = doc.getString("ultimos4Digitos") ?: "",
                        metadata = (doc.get("metadata") as? Map<String, String>) ?: mapOf()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "⚠️ Error al parsear cargo", e)
                    null
                }
            }

            Log.d(TAG, "✅ Historial obtenido: ${cargos.size} cargos")
            Result.success(cargos)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener historial de cargos", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado de un cargo
     */
    suspend fun actualizarEstadoCargo(cargoId: String, nuevoEstado: String): Result<Boolean> {
        return try {
            cargosCollection.document(cargoId)
                .update("estado", nuevoEstado)
                .await()

            Log.d(TAG, "✅ Estado de cargo actualizado: $cargoId -> $nuevoEstado")
            Result.success(true)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar estado de cargo", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene total de ingresos de un usuario (para estadísticas admin)
     */
    suspend fun obtenerTotalIngresos(userId: String): Result<Double> {
        return try {
            val snapshot = cargosCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("estado", "exitosa")
                .get()
                .await()

            val total = snapshot.documents.sumOf { doc ->
                doc.getDouble("monto") ?: 0.0
            }

            Log.d(TAG, "✅ Total de ingresos: S/ $total")
            Result.success(total)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al calcular ingresos", e)
            Result.failure(e)
        }
    }
}
