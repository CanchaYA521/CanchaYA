package com.rojassac.canchaya.ui.admin.perfil

/**
 * Modelo de datos para los items del menú de perfil de administrador
 *
 * @property id Identificador único del item
 * @property icon Recurso drawable del icono
 * @property titulo Título del item
 * @property descripcion Descripción breve del item
 */
data class AdminPerfilMenuItem(
    val id: Int,
    val icon: Int,
    val titulo: String,
    val descripcion: String
)
