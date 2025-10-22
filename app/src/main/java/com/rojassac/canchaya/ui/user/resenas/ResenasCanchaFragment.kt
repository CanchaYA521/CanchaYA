package com.rojassac.canchaya.ui.user.resenas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.repository.ResenaRepository
import com.rojassac.canchaya.databinding.FragmentResenasCanchaBinding
import com.rojassac.canchaya.utils.toast
import kotlinx.coroutines.launch

class ResenasCanchaFragment : Fragment() {

    private var _binding: FragmentResenasCanchaBinding? = null
    private val binding get() = _binding!!

    private lateinit var resenaAdapter: ResenaAdapter
    private val resenaRepository = ResenaRepository()
    private val auth = FirebaseAuth.getInstance()

    private var cancha: Cancha? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cancha = it.getParcelable("cancha")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResenasCanchaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        cargarResenas()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // RecyclerView
        resenaAdapter = ResenaAdapter { resena ->
            darLike(resena)
        }

        binding.rvResenas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resenaAdapter
        }

        // FAB para escribir reseña
        binding.fabEscribirResena.setOnClickListener {
            mostrarDialogEscribirResena()
        }
    }

    private fun cargarResenas() {
        val canchaId = cancha?.id ?: return

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = resenaRepository.obtenerResenasPorCancha(canchaId)

            binding.progressBar.visibility = View.GONE

            result.onSuccess { resenas ->
                resenaAdapter.submitList(resenas)

                // Actualizar estadísticas
                if (resenas.isNotEmpty()) {
                    val promedio = resenas.map { it.calificacion }.average().toFloat()
                    binding.tvCalificacionPromedio.text = String.format("%.1f", promedio)
                    binding.ratingBar.rating = promedio
                    binding.tvTotalResenas.text = "${resenas.size} reseñas"

                    // Calcular distribución de estrellas
                    calcularDistribucionEstrellas(resenas)
                } else {
                    binding.tvCalificacionPromedio.text = "0.0"
                    binding.ratingBar.rating = 0f
                    binding.tvTotalResenas.text = "Sin reseñas"
                }
            }

            result.onFailure { error ->
                requireContext().toast("Error al cargar reseñas: ${error.message}")
            }
        }
    }

    private fun calcularDistribucionEstrellas(resenas: List<com.rojassac.canchaya.data.model.Resena>) {
        val total = resenas.size.toFloat()
        val distribucion = mutableMapOf(
            5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0
        )

        resenas.forEach { resena ->
            val estrellas = resena.calificacion.toInt()
            distribucion[estrellas] = distribucion[estrellas]!! + 1
        }

        // Actualizar progreso
        binding.progress5.progress = ((distribucion[5]!! / total) * 100).toInt()
        binding.tvCount5.text = distribucion[5].toString()

        // Puedes repetir para 4, 3, 2, 1 si agregaste esos views
    }

    private fun darLike(resena: com.rojassac.canchaya.data.model.Resena) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            val result = resenaRepository.darLike(resena.id, userId)

            result.onSuccess {
                cargarResenas()
            }

            result.onFailure { error ->
                requireContext().toast("Error: ${error.message}")
            }
        }
    }

    private fun mostrarDialogEscribirResena() {
        val canchaData = cancha ?: return
        val userId = auth.currentUser?.uid ?: return

        // Navegar al fragment de escribir reseña
        val fragment = EscribirResenaFragment().apply {
            arguments = Bundle().apply {
                putString("canchaId", canchaData.id)
                putString("canchaNombre", canchaData.nombre)
                putString("userId", userId)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
