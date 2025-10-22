package com.rojassac.canchaya.data.model

/**
 * Modelo para los datos de la tarjeta del usuario
 */
data class DatosTarjeta(
    val numeroTarjeta: String, // Sin espacios: "4111111111111111"
    val cvv: String, // 3-4 dígitos
    val mesExpiracion: String, // Formato: "12"
    val anioExpiracion: String, // Formato: "2025"
    val nombreTitular: String,
    val email: String
) {

    /**
     * Valida que los datos sean correctos antes de enviar
     */
    fun validar(): Result<Boolean> {
        // Validar número de tarjeta
        val numeroLimpio = numeroTarjeta.replace(" ", "")
        if (numeroLimpio.length !in 13..19) {
            return Result.failure(Exception("Número de tarjeta inválido"))
        }

        // Validar CVV
        if (cvv.length !in 3..4 || !cvv.all { it.isDigit() }) {
            return Result.failure(Exception("CVV inválido"))
        }

        // Validar mes
        val mes = mesExpiracion.toIntOrNull()
        if (mes == null || mes !in 1..12) {
            return Result.failure(Exception("Mes de expiración inválido"))
        }

        // Validar año
        val anio = anioExpiracion.toIntOrNull()
        if (anio == null || anio < 2025) {
            return Result.failure(Exception("Año de expiración inválido"))
        }

        // Validar nombre
        if (nombreTitular.isBlank()) {
            return Result.failure(Exception("Nombre del titular requerido"))
        }

        // Validar email
        if (!email.contains("@")) {
            return Result.failure(Exception("Email inválido"))
        }

        return Result.success(true)
    }

    /**
     * Obtiene los últimos 4 dígitos para mostrar en UI
     */
    fun ultimos4Digitos(): String {
        val numeroLimpio = numeroTarjeta.replace(" ", "")
        return if (numeroLimpio.length >= 4) {
            "**** **** **** ${numeroLimpio.takeLast(4)}"
        } else {
            numeroTarjeta
        }
    }
}
