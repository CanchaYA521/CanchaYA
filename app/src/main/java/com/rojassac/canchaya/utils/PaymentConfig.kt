package com.rojassac.canchaya.utils

import com.rojassac.canchaya.data.model.PaymentGateway
import com.rojassac.canchaya.data.remote.CulqiPaymentGateway
import com.rojassac.canchaya.data.remote.MockPaymentGateway

/**
 * 🔥 CONFIGURACIÓN CENTRALIZADA DE PAGOS
 *
 * Aquí controlas si usas pagos reales o simulados.
 * Cambia USE_REAL_PAYMENTS a true cuando tengas Culqi configurado.
 */
object PaymentConfig {

    // ⚠️ CAMBIAR A TRUE CUANDO TENGAS RUC Y CULQI CONFIGURADO
    private const val USE_REAL_PAYMENTS = false

    // Claves de Culqi (vacías por ahora)
    private const val CULQI_PUBLIC_KEY = "pk_test_xxxxxxxxxxxxxxxx"
    private const val CULQI_SECRET_KEY = "sk_test_xxxxxxxxxxxxxxxx"

    /**
     * Obtiene el gateway de pagos según configuración
     */
    fun getPaymentGateway(): PaymentGateway {
        return if (USE_REAL_PAYMENTS) {
            // 🟢 PRODUCCIÓN - Pagos reales con Culqi
            CulqiPaymentGateway(
                publicKey = CULQI_PUBLIC_KEY,
                secretKey = CULQI_SECRET_KEY
            )
        } else {
            // 🟡 DESARROLLO/DEMO - Pagos simulados
            MockPaymentGateway()
        }
    }

    /**
     * Verifica si estamos en modo mock
     */
    fun isMockMode(): Boolean = !USE_REAL_PAYMENTS

    /**
     * Obtiene mensaje para mostrar en UI
     */
    fun getModoActual(): String {
        return if (USE_REAL_PAYMENTS) {
            "Modo: Pagos Reales (Culqi)"
        } else {
            "Modo: Pagos Simulados (Demo)"
        }
    }
}
