package com.rojassac.canchaya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Resena(
    val id: String = "",
    val canchaId: String = "",
    val canchaNombre: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val calificacion: Float = 0f,
    val comentario: String = "",
    val fecha: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList()
) : Parcelable
