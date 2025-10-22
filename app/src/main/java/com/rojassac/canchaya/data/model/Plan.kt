package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Plan(
    val id: String = "",
    val nombre: String = "", // "BÁSICO", "PRO", "PREMIUM", "ENTERPRISE"
    val precio: Double = 0.0, // Precio mensual en soles
    val comision: Double = 0.0, // Porcentaje de comisión (ejemplo: 0.40 = 40%)
    val plazoRetiro: Int = 0, // Días hasta que puede retirar dinero
    val destacado: Boolean = false, // Si aparece destacado en búsquedas
    val posicionPrioritaria: Boolean = false, // Si aparece primero
    val marketingIncluido: Boolean = false, // Si tiene marketing incluido
    val maxCanchas: Int = 1, // Máximo de canchas que puede administrar
    val whiteLabel: Boolean = false, // Si puede personalizar marca
    val soporte: String = "email", // Tipo de soporte: "email", "chat", "prioritario"
    val color: String = "#4CAF50", // Color del tema para este plan
    val descripcion: String = "",
    val caracteristicas: List<String> = listOf(),
    val activo: Boolean = true,
    val orden: Int = 0 // Para ordenar los planes en la UI
) : Parcelable
