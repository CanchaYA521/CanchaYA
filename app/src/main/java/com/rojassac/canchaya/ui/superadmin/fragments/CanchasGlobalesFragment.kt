package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.databinding.FragmentCanchasGlobalesBinding
import com.rojassac.canchaya.ui.superadmin.adapters.CanchasGlobalesAdapter
import com.rojassac.canchaya.utils.FirestoreConverter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CanchasGlobalesFragment : Fragment() {

    private var _binding: FragmentCanchasGlobalesBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: CanchasGlobalesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCanchasGlobalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        loadCanchas()
    }

    private fun setupRecyclerView() {
        adapter = CanchasGlobalesAdapter(
            canchas = emptyList(),
            onToggleStatus = { cancha -> toggleCanchaStatus(cancha.id, !cancha.activa) },
            onDelete = { cancha -> deleteCancha(cancha.id) },
            onAssignAdmin = { cancha ->
                Snackbar.make(binding.root, "Función asignar admin - Implementar", Snackbar.LENGTH_SHORT).show()
            }
        )

        binding.rvCanchasGlobales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CanchasGlobalesFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAgregarCancha.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, CrearCanchaFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadCanchas() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvCanchasGlobales.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val snapshot = db.collection("canchas")
                    .get()
                    .await()

                // ✅ Usar el conversor centralizado
                val canchas = FirestoreConverter.documentsToCanchas(snapshot.documents)

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE

                if (canchas.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvCanchasGlobales.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvCanchasGlobales.visibility = View.VISIBLE

                    adapter = CanchasGlobalesAdapter(
                        canchas = canchas,
                        onToggleStatus = { cancha -> toggleCanchaStatus(cancha.id, !cancha.activa) },
                        onDelete = { cancha -> deleteCancha(cancha.id) },
                        onAssignAdmin = { cancha ->
                            Snackbar.make(binding.root, "Asignar admin", Snackbar.LENGTH_SHORT).show()
                        }
                    )
                    binding.rvCanchasGlobales.adapter = adapter
                }

            } catch (e: Exception) {
                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleCanchaStatus(canchaId: String, newStatus: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                db.collection("canchas")
                    .document(canchaId)
                    .update("activa", newStatus)
                    .await()

                Snackbar.make(binding.root, "Estado actualizado", Snackbar.LENGTH_SHORT).show()
                loadCanchas()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCancha(canchaId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                db.collection("canchas")
                    .document(canchaId)
                    .delete()
                    .await()

                Snackbar.make(binding.root, "Cancha eliminada", Snackbar.LENGTH_SHORT).show()
                loadCanchas()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
