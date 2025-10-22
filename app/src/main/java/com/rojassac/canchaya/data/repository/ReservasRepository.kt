package com.rojassac.canchaya.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rojassac.canchaya.data.model.EstadoReserva
import com.rojassac.canchaya.data.model.Reserva
import kotlinx.coroutines.tasks.await

class ReservasRepository {

    private val db = FirebaseFirestore.getInstance()
    private val reservasRef = db.collection("reservas")

    /**
     * Obtener todas las reservas de una cancha específica
     */
    suspend fun obtenerReservasPorCancha(canchaId: String): List<Reserva> {
        return try {
            val snapshot = reservasRef
                .whereEqualTo("canchaId", canchaId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reserva::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ReservasRepository", "Error al obtener reservas", e)
            emptyList()
        }
    }

    /**
     * Obtener reservas por cancha y fecha
     */
    suspend fun obtenerReservasPorCanchaYFecha(
        canchaId: String,
        fecha: String
    ): List<Reserva> {
        return try {
            val snapshot = reservasRef
                .whereEqualTo("canchaId", canchaId)
                .whereEqualTo("fecha", fecha)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reserva::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ReservasRepository", "Error al obtener reservas por fecha", e)
            emptyList()
        }
    }

    /**
     * Obtener reservas por estado
     */
    suspend fun obtenerReservasPorEstado(
        canchaId: String,
        estado: EstadoReserva
    ): List<Reserva> {
        return try {
            val snapshot = reservasRef
                .whereEqualTo("canchaId", canchaId)
                .whereEqualTo("estado", estado.name)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reserva::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ReservasRepository", "Error al obtener reservas por estado", e)
            emptyList()
        }
    }

    /**
     * Actualizar estado de una reserva
     */
    suspend fun actualizarEstadoReserva(
        reservaId: String,
        nuevoEstado: EstadoReserva
    ): Boolean {
        return try {
            reservasRef.document(reservaId)
                .update("estado", nuevoEstado.name)
                .await()
            true
        } catch (e: Exception) {
            Log.e("ReservasRepository", "Error al actualizar estado", e)
            false
        }
    }

    /**
     * Obtener detalles de una reserva específica
     */
    suspend fun obtenerReservaPorId(reservaId: String): Reserva? {
        return try {
            val doc = reservasRef.document(reservaId).get().await()
            doc.toObject(Reserva::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ReservasRepository", "Error al obtener reserva", e)
            null
        }
    }
}
