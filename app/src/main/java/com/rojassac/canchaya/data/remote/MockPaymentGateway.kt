package com.rojassac.canchaya.data.remote

import android.util.Log
import com.rojassac.canchaya.data.model.CargoResponse
import com.rojassac.canchaya.data.model.DatosTarjeta
import com.rojassac.canchaya.data.model.PaymentGateway
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

/**
 * 🎭 IMPLEMENTACIÓN MOCK DEL GATEWAY DE PAGOS
 *
 * Simula completamente el comportamiento de Culqi:
 * - Validaciones reales de tarjeta (Algoritmo de Luhn)
 * - Delays para simular latencia de red
 * - 90% de éxito, 10% de fallos aleatorios
 * - Genera IDs realistas
 *
 * ✅ PERFECTO PARA:
 * - Desarrollo sin RUC
 * - Demos a inversionistas
 * - Testing completo de UI
 * - Pruebas ilimitadas gratis
 */
class MockPaymentGateway : PaymentGateway {

    companion object {
        private const val TAG = "MockPaymentGateway"

        // Tarjetas de prueba que SIEMPRE funcionan
        private val TARJETAS_PRUEBA = listOf(
            "4111111111111111", // Visa
            "5555555555554444", // Mastercard
            "378282246310005"   // Amex
        )

        // Tarjetas que SIEMPRE fallan (para testing)
        private val TARJETAS_ERROR = listOf(
            "4000000000000002", // Tarjeta rechazada
            "4000000000000069"  // Tarjeta expirada
        )
    }

    /**
     * Crea un token de tarjeta (simulado)
     */
    override suspend fun crearToken(datosTarjeta: DatosTarjeta): Result<String> {
        // Simular latencia de red (1-2 segundos)
        delay((1000..2000).random().toLong())

        Log.d(TAG, "🎭 Creando token mock para: ${datosTarjeta.email}")

        // Validar datos de tarjeta
        val validacion = datosTarjeta.validar()
        if (validacion.isFailure) {
            Log.e(TAG, "❌ Validación fallida: ${validacion.exceptionOrNull()?.message}")
            return Result.failure(validacion.exceptionOrNull()!!)
        }

        // Validar con algoritmo de Luhn
        val numeroLimpio = datosTarjeta.numeroTarjeta.replace(" ", "")

        // Verificar si es tarjeta de error
        if (numeroLimpio in TARJETAS_ERROR) {
            Log.e(TAG, "❌ Tarjeta rechazada (test)")
            return Result.failure(Exception("Tarjeta rechazada"))
        }

        // Validación adicional (Luhn)
        if (!validarLuhn(numeroLimpio) && numeroLimpio !in TARJETAS_PRUEBA) {
            Log.e(TAG, "❌ Número de tarjeta inválido (Luhn)")
            return Result.failure(Exception("Número de tarjeta inválido"))
        }

        // Generar token simulado
        val token = "tok_test_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        Log.d(TAG, "✅ Token generado: $token")

        return Result.success(token)
    }

    /**
     * Crea un cargo usando el token (simulado)
     */
    override suspend fun crearCargo(
        tokenId: String,
        monto: Double,
        email: String,
        descripcion: String
    ): Result<CargoResponse> {
        // Simular procesamiento (2-3 segundos)
        delay((2000..3000).random().toLong())

        Log.d(TAG, "🎭 Procesando cargo mock: S/ $monto para $email")

        // Validar monto
        if (monto <= 0) {
            Log.e(TAG, "❌ Monto inválido: $monto")
            return Result.failure(Exception("Monto debe ser mayor a 0"))
        }

        // Validar token
        if (!tokenId.startsWith("tok_test_")) {
            Log.e(TAG, "❌ Token inválido")
            return Result.failure(Exception("Token inválido"))
        }

        // Simular éxito/fallo (90% éxito, 10% fallo)
        val exitoso = Random.nextDouble() < 0.9

        if (exitoso) {
            // ✅ CARGO EXITOSO
            val charge = CargoResponse(
                id = "chr_test_${UUID.randomUUID().toString().replace("-", "").take(16)}",
                monto = monto,
                moneda = "PEN",
                estado = "exitosa",
                fechaCreacion = System.currentTimeMillis(),
                descripcion = descripcion,
                email = email,
                ultimos4Digitos = generarUltimos4Digitos(),
                metadata = mapOf(
                    "modo" to "mock",
                    "tokenId" to tokenId
                )
            )

            Log.d(TAG, "✅ Cargo exitoso: ${charge.id}")
            return Result.success(charge)

        } else {
            // ❌ CARGO FALLIDO (para testing)
            val errores = listOf(
                "Fondos insuficientes",
                "Tarjeta rechazada por el banco",
                "CVV incorrecto",
                "Tarjeta expirada"
            )

            val error = errores.random()
            Log.e(TAG, "❌ Cargo fallido (simulado): $error")
            return Result.failure(Exception(error))
        }
    }

    /**
     * Reembolsa un cargo (simulado)
     */
    override suspend fun reembolsar(chargeId: String): Result<Boolean> {
        delay(1000)

        Log.d(TAG, "🎭 Procesando reembolso mock: $chargeId")

        // Validar ID
        if (!chargeId.startsWith("chr_test_")) {
            Log.e(TAG, "❌ ID de cargo inválido")
            return Result.failure(Exception("ID de cargo inválido"))
        }

        // Simular éxito (95% de éxito en reembolsos)
        val exitoso = Random.nextDouble() < 0.95

        if (exitoso) {
            Log.d(TAG, "✅ Reembolso exitoso")
            return Result.success(true)
        } else {
            Log.e(TAG, "❌ Error al procesar reembolso")
            return Result.failure(Exception("No se pudo procesar el reembolso"))
        }
    }

    /**
     * Obtiene el estado de un cargo (simulado)
     */
    override suspend fun obtenerEstadoCargo(chargeId: String): Result<CargoResponse> {
        delay(500)

        Log.d(TAG, "🎭 Consultando estado de cargo: $chargeId")

        // Validar ID
        if (!chargeId.startsWith("chr_test_")) {
            return Result.failure(Exception("ID de cargo inválido"))
        }

        // Simular respuesta
        val cargo = CargoResponse(
            id = chargeId,
            monto = 0.0, // No tenemos el monto original en mock
            moneda = "PEN",
            estado = "exitosa",
            fechaCreacion = System.currentTimeMillis(),
            descripcion = "Cargo de prueba",
            email = "mock@test.com",
            ultimos4Digitos = "****",
            metadata = mapOf("modo" to "mock")
        )

        Log.d(TAG, "✅ Estado obtenido: ${cargo.estado}")
        return Result.success(cargo)
    }

    // ========================
    // MÉTODOS AUXILIARES
    // ========================

    /**
     * Algoritmo de Luhn para validar números de tarjeta
     */
    private fun validarLuhn(numero: String): Boolean {
        var suma = 0
        var alternar = false

        for (i in numero.length - 1 downTo 0) {
            var digito = numero[i].toString().toIntOrNull() ?: return false

            if (alternar) {
                digito *= 2
                if (digito > 9) {
                    digito -= 9
                }
            }

            suma += digito
            alternar = !alternar
        }

        return suma % 10 == 0
    }

    /**
     * Genera últimos 4 dígitos aleatorios para el mock
     */
    private fun generarUltimos4Digitos(): String {
        return (1000..9999).random().toString()
    }
}
