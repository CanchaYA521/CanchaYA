package com.rojassac.canchaya.ui.user.resenas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.rojassac.canchaya.data.model.Resena
import com.rojassac.canchaya.data.repository.ResenaRepository
import com.rojassac.canchaya.databinding.FragmentEscribirResenaBinding
import com.rojassac.canchaya.utils.toast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EscribirResenaFragment : Fragment() {

    private var _binding: FragmentEscribirResenaBinding? = null
    private val binding get() = _binding!!

    private val resenaRepository = ResenaRepository()
    private val auth = FirebaseAuth.getInstance()

    private var canchaId = ""
    private var canchaNombre = ""
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            canchaId = it.getString("canchaId", "")
            canchaNombre = it.getString("canchaNombre", "")
            userId = it.getString("userId", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEscribirResenaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.tvCanchaNombre.text = canchaNombre

        binding.btnPublicar.setOnClickListener {
            publicarResena()
        }
    }

    private fun publicarResena() {
        val calificacion = binding.ratingBar.rating
        val comentario = binding.etComentario.text.toString().trim()

        // Validaciones
        if (calificacion == 0f) {
            requireContext().toast("Por favor, selecciona una calificación")
            return
        }

        if (comentario.isEmpty()) {
            requireContext().toast("Por favor, escribe un comentario")
            return
        }

        val currentUser = auth.currentUser ?: return
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val resena = Resena(
            canchaId = canchaId,
            canchaNombre = canchaNombre,
            userId = userId,
            userName = currentUser.displayName ?: "Usuario",
            userPhotoUrl = currentUser.photoUrl?.toString() ?: "",
            calificacion = calificacion,
            comentario = comentario,
            fecha = sdf.format(Date()),
            timestamp = System.currentTimeMillis()
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnPublicar.isEnabled = false

        lifecycleScope.launch {
            val result = resenaRepository.crearResena(resena)

            binding.progressBar.visibility = View.GONE
            binding.btnPublicar.isEnabled = true

            result.onSuccess {
                requireContext().toast("¡Reseña publicada exitosamente!")
                requireActivity().onBackPressed()
            }

            result.onFailure { error ->
                requireContext().toast("Error al publicar reseña: ${error.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
