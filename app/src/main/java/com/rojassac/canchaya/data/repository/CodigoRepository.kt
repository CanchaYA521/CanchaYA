package com.rojassac.canchaya.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ValidacionCodigoResult(
    val exito: Boolean,
    val mensaje: String,
    val sedeId: String? = null,
    val canchaId: String? = null,
    val nombreNegocio: String? = null,
    val tipo: String? = null
)

class CodigoRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "CodigoRepository"
    }

    /**
     * Valida un código de invitación y asigna la sede/cancha al usuario admin
     */
    suspend fun validarCodigoInvitacion(codigo: String): ValidacionCodigoResult {
        return try {
            val userId = auth.currentUser?.uid
                ?: return ValidacionCodigoResult(false, "Usuario no autenticado")

            Log.d(TAG, "🔍 Validando código: $codigo para usuario: $userId")

            // 1. Buscar el código en codigos_invitacion
            val codigoDoc = firestore.collection("codigos_invitacion")
                .document(codigo)
                .get()
                .await()

            if (!codigoDoc.exists()) {
                Log.e(TAG, "❌ Código no existe: $codigo")
                return ValidacionCodigoResult(false, "Código inválido")
            }

            // 2. Verificar si ya fue usado
            val usado = codigoDoc.getBoolean("usado") ?: false
            if (usado) {
                Log.e(TAG, "❌ Código ya usado: $codigo")
                return ValidacionCodigoResult(false, "Este código ya fue utilizado")
            }

            // 3. Verificar si está activo
            val activo = codigoDoc.getBoolean("activo") ?: true
            if (!activo) {
                Log.e(TAG, "❌ Código inactivo: $codigo")
                return ValidacionCodigoResult(false, "Este código ha sido desactivado")
            }

            // 4. Obtener datos del código
            val tipo = codigoDoc.getString("tipo") ?: ""
            val sedeId = codigoDoc.getString("sedeId")
            val canchaId = codigoDoc.getString("canchaId")
            val nombreNegocio = codigoDoc.getString("nombre")

            Log.d(TAG, "📋 Código válido - Tipo: $tipo, SedeId: $sedeId, CanchaId: $canchaId")

            // 5. Realizar la asignación según el tipo
            when (tipo) {
                "sede" -> {
                    if (sedeId == null) {
                        return ValidacionCodigoResult(false, "Código inválido: sin sede asociada")
                    }
                    asignarSede(userId, sedeId, codigo)
                }
                "cancha_individual" -> {
                    if (canchaId == null) {
                        return ValidacionCodigoResult(false, "Código inválido: sin cancha asociada")
                    }
                    asignarCanchaIndividual(userId, canchaId, codigo)
                }
                else -> {
                    return ValidacionCodigoResult(false, "Tipo de código no válido")
                }
            }

            // 6. Registrar en logs
            registrarValidacionExitosa(codigo, userId, tipo, sedeId, canchaId)

            Log.d(TAG, "✅ Código validado exitosamente")
            ValidacionCodigoResult(
                exito = true,
                mensaje = "¡Código validado exitosamente!",
                sedeId = sedeId,
                canchaId = canchaId,
                nombreNegocio = nombreNegocio,
                tipo = tipo
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al validar código: ${e.message}", e)
            ValidacionCodigoResult(false, "Error: ${e.message}")
        }
    }

    /**
     * 🔵 MODIFICADO: Asigna una SEDE al usuario admin
     */
    private suspend fun asignarSede(userId: String, sedeId: String, codigo: String) {
        Log.d(TAG, "🏢 Asignando sede $sedeId a usuario $userId")

        // Actualizar en batch para atomicidad
        val batch = firestore.batch()

        // 1. ✅ MODIFICADO: Actualizar usuario con los campos correctos
        val userRef = firestore.collection("users").document(userId)
        batch.update(userRef, mapOf(
            "sedeId" to sedeId,
            "sedeId" to null,  // ✅ AGREGAR esta línea
            "tipoAdministracion" to "sede",
            "canchasAsignadas" to FieldValue.arrayUnion(sedeId) // También agregar a la lista
        ))

        // 2. Actualizar sede: agregar adminAsignado
        val sedeRef = firestore.collection("sedes").document(sedeId)
        batch.update(sedeRef, mapOf(
            "adminAsignado" to userId,
            "fechaAsignacion" to FieldValue.serverTimestamp()
        ))

        // 3. Marcar código como usado
        val codigoRef = firestore.collection("codigos_invitacion").document(codigo)
        batch.update(codigoRef, mapOf(
            "usado" to true,
            "adminAsignado" to userId,
            "fechaUso" to FieldValue.serverTimestamp()
        ))

        // Ejecutar batch
        batch.commit().await()
        Log.d(TAG, "✅ Sede asignada exitosamente")
    }

    /**
     * 🔵 MODIFICADO: Asigna una CANCHA INDIVIDUAL al usuario admin
     */
    private suspend fun asignarCanchaIndividual(userId: String, canchaId: String, codigo: String) {
        Log.d(TAG, "🏟️ Asignando cancha individual $canchaId a usuario $userId")

        val batch = firestore.batch()

        // 1. ✅ MODIFICADO: Actualizar usuario con los campos correctos
        val userRef = firestore.collection("users").document(userId)
        batch.update(userRef, mapOf(
            "canchasAsignadas" to FieldValue.arrayUnion(canchaId),
            "tipoAdministracion" to "cancha_individual"
        ))

        // 2. Actualizar cancha (si existe la colección)
        val canchaRef = firestore.collection("canchas").document(canchaId)
        batch.update(canchaRef, mapOf(
            "adminAsignado" to userId,
            "fechaAsignacion" to FieldValue.serverTimestamp()
        ))

        // 3. Marcar código como usado
        val codigoRef = firestore.collection("codigos_invitacion").document(codigo)
        batch.update(codigoRef, mapOf(
            "usado" to true,
            "adminAsignado" to userId,
            "fechaUso" to FieldValue.serverTimestamp()
        ))

        batch.commit().await()
        Log.d(TAG, "✅ Cancha individual asignada exitosamente")
    }

    /**
     * Registra la validación exitosa en logs
     */
    private suspend fun registrarValidacionExitosa(
        codigo: String,
        userId: String,
        tipo: String,
        sedeId: String?,
        canchaId: String?
    ) {
        val logData = hashMapOf(
            "codigo" to codigo,
            "accion" to "codigo_validado",
            "tipo" to tipo,
            "userId" to userId,
            "sedeId" to sedeId,
            "canchaId" to canchaId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("codigo_logs")
            .add(logData)
            .await()
    }
}
