package com.rojassac.canchaya.ui.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.FirestoreConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _canchas = MutableLiveData<List<Cancha>>()
    val canchas: LiveData<List<Cancha>> = _canchas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var allCanchas = listOf<Cancha>()

    companion object {
        private const val TAG = "UserViewModel"
    }

    fun loadCanchas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                Log.d(TAG, "Iniciando carga de canchas...")

                val snapshot = firestore.collection(Constants.CANCHAS_COLLECTION)
                    .whereEqualTo("activa", true)  // ✅ CAMBIADO DE "activo" A "activa"
                    .get()
                    .await()

                Log.d(TAG, "Firestore respondió con ${snapshot.documents.size} documentos")

                // ✅ USA FirestoreConverter.documentsToCanchas() para conversión robusta
                val canchasList = FirestoreConverter.documentsToCanchas(snapshot.documents)

                Log.d(TAG, "Se convirtieron ${canchasList.size} canchas correctamente")

                withContext(Dispatchers.Main) {
                    allCanchas = canchasList
                    _canchas.value = canchasList
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar canchas: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _canchas.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }

    fun searchCanchas(query: String) {
        if (query.isEmpty()) {
            _canchas.value = allCanchas
        } else {
            _canchas.value = allCanchas.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.distrito.contains(query, ignoreCase = true) ||
                        it.ciudad.contains(query, ignoreCase = true)
            }
        }
    }
}
