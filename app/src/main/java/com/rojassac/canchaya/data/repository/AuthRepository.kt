package com.rojassac.canchaya.data.repository

import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.data.remote.FirebaseAuthService
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.flow.flow

class AuthRepository {

    private val authService = FirebaseAuthService()

    // ========== REGISTRO ==========
    suspend fun registerWithEmail(
        email: String,
        password: String,
        nombre: String,
        celular: String
    ) = flow {
        emit(Resource.Loading())
        val result = authService.registerWithEmail(email, password, nombre, celular)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error(result.exceptionOrNull()?.message ?: "Error al registrar"))
        }
    }

    suspend fun registerWithEmailAndRole(
        email: String,
        password: String,
        nombre: String,
        celular: String,
        rol: UserRole
    ) = flow {
        emit(Resource.Loading())
        val result = authService.registerWithEmailAndRole(email, password, nombre, celular, rol)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error(result.exceptionOrNull()?.message ?: "Error al registrar"))
        }
    }

    // ========== LOGIN ==========
    suspend fun loginWithEmail(email: String, password: String) = flow {
        emit(Resource.Loading())
        val result = authService.loginWithEmail(email, password)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error(result.exceptionOrNull()?.message ?: "Error al iniciar sesión"))
        }
    }

    // ✨ NUEVO: Login/Registro con Google
    suspend fun signInWithGoogle(idToken: String) = flow {
        emit(Resource.Loading())
        val result = authService.signInWithGoogle(idToken)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error(result.exceptionOrNull()?.message ?: "Error al iniciar sesión con Google"))
        }
    }

    // ========== USUARIO ==========
    suspend fun getCurrentUser() = flow {
        emit(Resource.Loading())
        val firebaseUser = authService.getCurrentUser()
        if (firebaseUser != null) {
            val result = authService.getUserData(firebaseUser.uid)
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrNull()!!))
            } else {
                emit(Resource.Error(result.exceptionOrNull()?.message ?: "Error al obtener usuario"))
            }
        } else {
            emit(Resource.Error("No hay usuario autenticado"))
        }
    }

    // ========== VERIFICACIÓN DE ROL ==========
    suspend fun esAdministrador(): Boolean {
        return authService.esAdministrador()
    }

    suspend fun obtenerRolActual(): UserRole? {
        return authService.obtenerRolActual()
    }

    // ========== SESIÓN ==========
    fun logout() {
        authService.logout()
    }

    fun isLoggedIn(): Boolean {
        return authService.isLoggedIn()
    }
}
