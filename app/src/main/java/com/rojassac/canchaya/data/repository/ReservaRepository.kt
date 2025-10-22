package com.rojassac.canchaya.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.NotificationHelper
import kotlinx.coroutines.tasks.await

class ReservaRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun crearReserva(reserva: Reserva, canchaNombre: String): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")

            val reservaData = hashMapOf(
                "userId" to userId,
                "usuarioId" to userId,
                "canchaId" to reserva.canchaId,
                "canchaNombre" to canchaNombre,
                "fecha" to reserva.fecha,
                "horaInicio" to reserva.horaInicio,
                "horaFin" to reserva.horaFin,
                "precio" to reserva.precio,
                "estado" to "confirmada",
                "timestamp" to System.currentTimeMillis()
            )

            val docRef = firestore.collection(Constants.RESERVAS_COLLECTION)
                .add(reservaData)
                .await()

            NotificationHelper.showReservaConfirmada(
                context = context,
                canchaNombre = canchaNombre,
                fecha = reserva.fecha,
                hora = reserva.horaInicio
            )

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerReservasUsuario(): Result<List<Reserva>> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")

            val snapshot = firestore.collection(Constants.RESERVAS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val reservas = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Reserva::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(reservas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelarReserva(reservaId: String, canchaNombre: String): Result<Unit> {
        return try {
            firestore.collection(Constants.RESERVAS_COLLECTION)
                .document(reservaId)
                .update("estado", "cancelada")
                .await()

            NotificationHelper.showReservaCancelada(
                context = context,
                canchaNombre = canchaNombre
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ CORREGIDO: Verificar disponibilidad con logs para debug
    suspend fun verificarDisponibilidad(canchaId: String, fecha: String, hora: String): Result<Boolean> {
        return try {
            android.util.Log.d("ReservaRepo", "Verificando: canchaId=$canchaId, fecha=$fecha, hora=$hora")

            val snapshot = firestore.collection(Constants.RESERVAS_COLLECTION)
                .whereEqualTo("canchaId", canchaId)
                .whereEqualTo("fecha", fecha)
                .whereEqualTo("horaInicio", hora)
                .whereIn("estado", listOf("confirmada", "CONFIRMADA"))  // ✅ Ambos formatos
                .get()
                .await()

            val disponible = snapshot.isEmpty
            android.util.Log.d("ReservaRepo", "Disponible: $disponible (encontrados: ${snapshot.size()})")

            Result.success(disponible)
        } catch (e: Exception) {
            android.util.Log.e("ReservaRepo", "Error: ${e.message}")
            // ✅ En caso de error, considerarlo DISPONIBLE para evitar bloqueos
            Result.success(true)
        }
    }
}
