package com.rojassac.canchaya.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.data.model.User
import com.rojassac.canchaya.data.model.UserRole
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.FirestoreConverter
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // ========== REGISTRO ==========

    suspend fun registerWithEmail(
        email: String,
        password: String,
        nombre: String,
        celular: String
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al crear usuario")

            val user = User(
                uid = firebaseUser.uid,
                nombre = nombre,
                celular = celular,
                email = email,
                rol = UserRole.USUARIO,
                fechaCreacion = System.currentTimeMillis()
            )

            firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmailAndRole(
        email: String,
        password: String,
        nombre: String,
        celular: String,
        rol: UserRole
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al crear usuario")

            val user = User(
                uid = firebaseUser.uid,
                nombre = nombre,
                celular = celular,
                email = email,
                rol = rol,
                fechaCreacion = System.currentTimeMillis()
            )

            firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== LOGIN ==========

    suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Usuario no encontrado")

            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            // ✅ USA FirestoreConverter en lugar de toObject
            val user = FirestoreConverter.documentToUser(userDoc)
                ?: throw Exception("Datos de usuario no encontrados")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✨ NUEVO: Login/Registro con Google
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al autenticar con Google")

            // Verificar si el usuario ya existe en Firestore
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if (userDoc.exists()) {
                // Usuario existente - ✅ USA FirestoreConverter
                FirestoreConverter.documentToUser(userDoc)
                    ?: throw Exception("Error al cargar usuario")
            } else {
                // Usuario nuevo - crear documento
                val newUser = User(
                    uid = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Usuario Google",
                    celular = firebaseUser.phoneNumber ?: "",
                    email = firebaseUser.email ?: "",
                    rol = UserRole.USUARIO,
                    fechaCreacion = System.currentTimeMillis()
                )

                firestore.collection(Constants.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .set(newUser)
                    .await()

                newUser
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== USUARIO ==========

    suspend fun getUserData(uid: String): Result<User> {
        return try {
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            // ✅ USA FirestoreConverter en lugar de toObject
            val user = FirestoreConverter.documentToUser(userDoc)
                ?: throw Exception("Usuario no encontrado")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== VERIFICACIÓN DE ROL ==========

    suspend fun esAdministrador(): Boolean {
        return try {
            val uid = getCurrentUser()?.uid ?: return false
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            // ✅ USA FirestoreConverter en lugar de toObject
            val user = FirestoreConverter.documentToUser(userDoc)
            user?.rol == UserRole.ADMIN || user?.rol == UserRole.SUPERADMIN
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerRolActual(): UserRole? {
        return try {
            val uid = getCurrentUser()?.uid ?: return null
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            // ✅ USA FirestoreConverter en lugar de toObject
            val user = FirestoreConverter.documentToUser(userDoc)
            user?.rol
        } catch (e: Exception) {
            null
        }
    }

    // ========== SESIÓN ==========

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}
