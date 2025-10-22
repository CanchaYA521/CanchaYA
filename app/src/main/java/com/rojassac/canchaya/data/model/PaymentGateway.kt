package com.rojassac.canchaya.data.model

/**
 * 🔥 INTERFAZ PRINCIPAL PARA PAGOS
 *
 * Esta interfaz permite cambiar fácilmente entre:
 * - MockPaymentGateway (desarrollo/demos)
 * - CulqiPaymentGateway (producción con RUC)
 */
interface PaymentGateway {

    /**
     * Crea un token de pago a partir de los datos de la tarjeta
     * @param datosTarjeta Información de la tarjeta del usuario
     * @return Result con el tokenId si es exitoso
     */
    suspend fun crearToken(datosTarjeta: DatosTarjeta): Result<String>

    /**
     * Crea un cargo usando el token generado
     * @param tokenId Token de la tarjeta
     * @param monto Monto a cobrar en soles
     * @param email Email del usuario
     * @param descripcion Descripción del cargo
     * @return Result con el CargoResponse si es exitoso
     */
    suspend fun crearCargo(
        tokenId: String,
        monto: Double,
        email: String,
        descripcion: String = "Reserva CanchaYA"
    ): Result<CargoResponse>

    /**
     * Reembolsa un cargo existente
     * @param chargeId ID del cargo a reembolsar
     * @return Result<Boolean> true si se reembolsó exitosamente
     */
    suspend fun reembolsar(chargeId: String): Result<Boolean>

    /**
     * Obtiene el estado de un cargo
     * @param chargeId ID del cargo
     * @return Result con el CargoResponse actualizado
     */
    suspend fun obtenerEstadoCargo(chargeId: String): Result<CargoResponse>
}
