package com.rojassac.canchaya.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.data.repository.AuthRepository
import com.rojassac.canchaya.utils.Resource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<Resource<User>>()
    val authState: LiveData<Resource<User>> = _authState

    private val _registerState = MutableLiveData<Resource<FirebaseUser>>()
    val registerState: LiveData<Resource<FirebaseUser>> = _registerState

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    private val _userRole = MutableLiveData<UserRole?>()
    val userRole: LiveData<UserRole?> = _userRole

    // ========== REGISTRO ==========
    fun registerWithEmail(email: String, password: String): LiveData<Resource<FirebaseUser>> {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    _registerState.value = Resource.Success(firebaseUser)
                } ?: run {
                    _registerState.value = Resource.Error("Error al crear usuario")
                }
            } catch (e: Exception) {
                _registerState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
        return registerState
    }

    // ========== LOGIN ==========
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            repository.loginWithEmail(email, password).collect { result ->
                _authState.value = result
                if (result is Resource.Success) {
                    verificarRolAdmin()
                }
            }
        }
    }

    // ✨ NUEVO: Login/Registro con Google
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            repository.signInWithGoogle(idToken).collect { result ->
                _authState.value = result
                if (result is Resource.Success) {
                    verificarRolAdmin()
                }
            }
        }
    }

    // ========== USUARIO ACTUAL ==========
    fun getCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUser().collect { result ->
                _authState.value = result
                if (result is Resource.Success) {
                    verificarRolAdmin()
                }
            }
        }
    }

    // ========== VERIFICACIÓN DE ROL ==========
    fun verificarRolAdmin() {
        viewModelScope.launch {
            val esAdmin = repository.esAdministrador()
            _isAdmin.value = esAdmin
            val rol = repository.obtenerRolActual()
            _userRole.value = rol
        }
    }

    fun obtenerRol() {
        viewModelScope.launch {
            val rol = repository.obtenerRolActual()
            _userRole.value = rol
        }
    }

    // ========== SESIÓN ==========
    fun logout() {
        repository.logout()
        _isAdmin.value = false
        _userRole.value = null
    }

    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
}
