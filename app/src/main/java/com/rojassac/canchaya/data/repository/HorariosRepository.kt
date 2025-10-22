package com.rojassac.canchaya.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HorariosRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Obtiene los horarios ocupados de una cancha en una fecha específica
     */
    suspend fun obtenerHorariosOcupados(canchaId: String, fecha: String): List<String> {
        return try {
            val doc = db.collection("canchas")
                .document(canchaId)
                .collection("horarios")
                .document(fecha)
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            doc.get("horasOcupadas") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Marca un horario como ocupado
     */
    suspend fun marcarHorarioOcupado(canchaId: String, fecha: String, hora: String) {
        try {
            db.collection("canchas")
                .document(canchaId)
                .collection("horarios")
                .document(fecha)
                .set(
                    mapOf("horasOcupadas" to FieldValue.arrayUnion(hora)),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Error al marcar horario ocupado: ${e.message}")
        }
    }

    /**
     * Marca un horario como disponible
     */
    suspend fun marcarHorarioDisponible(canchaId: String, fecha: String, hora: String) {
        try {
            db.collection("canchas")
                .document(canchaId)
                .collection("horarios")
                .document(fecha)
                .update("horasOcupadas", FieldValue.arrayRemove(hora))
                .await()
        } catch (e: Exception) {
            throw Exception("Error al marcar horario disponible: ${e.message}")
        }
    }
}
