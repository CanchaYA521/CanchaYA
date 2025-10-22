package com.rojassac.canchaya.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.data.model.Cancha

object TestDataGenerator {

    private val firestore = FirebaseFirestore.getInstance()

    fun createTestCanchas() {
        val testCanchas = listOf(
            Cancha(
                id = "",
                nombre = "Cancha La Bombonera",
                direccion = "Av. Javier Prado 123",
                distrito = "San Isidro",
                ciudad = "Lima",
                latitud = -12.0897,
                longitud = -77.0282,
                precioHora = 70.0,
                imagenes = emptyList(),
                servicios = listOf("Estacionamiento", "Vestuarios", "Iluminación"),
                horarioApertura = "08:00",
                horarioCierre = "23:00",
                activo = true,
                calificacionPromedio = 4.5,
                totalResenas = 12
            ),
            Cancha(
                id = "",
                nombre = "Complejo Deportivo El Crack",
                direccion = "Jr. Los Olivos 456",
                distrito = "Miraflores",
                ciudad = "Lima",
                latitud = -12.1212,
                longitud = -77.0333,
                precioHora = 80.0,
                imagenes = emptyList(),
                servicios = listOf("Cafetería", "Vestuarios", "Duchas"),
                horarioApertura = "07:00",
                horarioCierre = "22:00",
                activo = true,
                calificacionPromedio = 4.8,
                totalResenas = 25
            ),
            Cancha(
                id = "",
                nombre = "Estadio Monumental",
                direccion = "Av. Colonial 789",
                distrito = "Surco",
                ciudad = "Lima",
                latitud = -12.1500,
                longitud = -77.0100,
                precioHora = 60.0,
                imagenes = emptyList(),
                servicios = listOf("Estacionamiento", "Iluminación"),
                horarioApertura = "09:00",
                horarioCierre = "21:00",
                activo = true,
                calificacionPromedio = 4.2,
                totalResenas = 8
            )
        )

        testCanchas.forEach { cancha ->
            firestore.collection(Constants.CANCHAS_COLLECTION)
                .add(cancha)
                .addOnSuccessListener {
                    android.util.Log.d("TestData", "Cancha creada: ${cancha.nombre}")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("TestData", "Error: ${e.message}")
                }
        }
       }
}
