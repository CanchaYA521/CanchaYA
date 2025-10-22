package com.rojassac.canchaya.data.remote

import android.util.Log
import com.rojassac.canchaya.data.model.CargoResponse
import com.rojassac.canchaya.data.model.DatosTarjeta
import com.rojassac.canchaya.data.model.PaymentGateway

/**
 * 🔒 IMPLEMENTACIÓN REAL DE CULQI
 *
 * Este archivo es un PLACEHOLDER hasta que tengas:
 * - RUC registrado
 * - Cuenta en Culqi
 * - Claves API (Public Key y Secret Key)
 *
 * ⚠️ NO FUNCIONA AÚN - Usa MockPaymentGateway mientras tanto
 *
 * 📚 Documentación Culqi:
 * https://docs.culqi.com/
 */
class CulqiPaymentGateway(
    private val publicKey: String,
    private val secretKey: String
) : PaymentGateway {

    companion object {
        private const val TAG = "CulqiPaymentGateway"
        private const val CULQI_API_URL = "https://api.culqi.com/v2"
    }

    /**
     * TODO: Implementar cuando tengas Culqi configurado
     *
     * Endpoints de Culqi:
     * - POST /tokens (crear token)
     * - POST /charges (crear cargo)
     * - GET /charges/{id} (consultar cargo)
     */
    override suspend fun crearToken(datosTarjeta: DatosTarjeta): Result<String> {
        Log.w(TAG, "⚠️ Culqi no configurado - Usa MockPaymentGateway")
        return Result.failure(
            Exception("Culqi no está configurado. Activa el modo mock en PaymentConfig.")
        )
    }

    override suspend fun crearCargo(
        tokenId: String,
        monto: Double,
        email: String,
        descripcion: String
    ): Result<CargoResponse> {
        Log.w(TAG, "⚠️ Culqi no configurado - Usa MockPaymentGateway")
        return Result.failure(
            Exception("Culqi no está configurado. Activa el modo mock en PaymentConfig.")
        )
    }

    override suspend fun reembolsar(chargeId: String): Result<Boolean> {
        Log.w(TAG, "⚠️ Culqi no configurado - Usa MockPaymentGateway")
        return Result.failure(
            Exception("Culqi no está configurado. Activa el modo mock en PaymentConfig.")
        )
    }

    override suspend fun obtenerEstadoCargo(chargeId: String): Result<CargoResponse> {
        Log.w(TAG, "⚠️ Culqi no configurado - Usa MockPaymentGateway")
        return Result.failure(
            Exception("Culqi no está configurado. Activa el modo mock en PaymentConfig.")
        )
    }

    /**
     * 📝 NOTAS DE IMPLEMENTACIÓN FUTURA:
     *
     * 1. Agregar dependencia en build.gradle:
     *    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
     *    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
     *
     * 2. Crear interface CulqiApi con Retrofit
     *
     * 3. Implementar métodos usando las APIs de Culqi
     *
     * 4. Manejar errores específicos de Culqi
     *
     * 5. Activar en PaymentConfig:
     *    USE_REAL_PAYMENTS = true
     */
}
