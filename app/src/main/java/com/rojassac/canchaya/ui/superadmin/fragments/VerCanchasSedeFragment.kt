package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.Sede
import com.rojassac.canchaya.databinding.FragmentVerCanchasSedeBinding
import com.rojassac.canchaya.ui.superadmin.adapters.CanchasSedeAdapter

/**
 * ✨ NUEVO: Fragment para ver canchas de una sede (23 Oct 2025)
 */
class VerCanchasSedeFragment : Fragment() {

    private var _binding: FragmentVerCanchasSedeBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: CanchasSedeAdapter
    private var sede: Sede? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerCanchasSedeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar sede de argumentos
        sede = arguments?.getParcelable(ARG_SEDE)

        if (sede == null) {
            Toast.makeText(requireContext(), "Error: Sede no encontrada", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        setupUI()
        setupRecyclerView()
        setupListeners()
        cargarCanchas()
    }

    private fun setupUI() {
        sede?.let {
            binding.tvNombreSede.text = it.nombre
            binding.tvDireccionSede.text = "${it.direccion} • ${it.getHorarioDisplay()}"
        }
    }

    private fun setupRecyclerView() {
        adapter = CanchasSedeAdapter(
            canchas = emptyList(),
            onEditar = { cancha ->
                // TODO: Navegar a editar cancha
                Toast.makeText(requireContext(), "Editar: ${cancha.nombre}", Toast.LENGTH_SHORT).show()
            },
            onEliminar = { cancha ->
                eliminarCancha(cancha)
            }
        )

        binding.recyclerViewCanchas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@VerCanchasSedeFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAgregarCancha.setOnClickListener {
            sede?.let { sedeActual ->
                navigateToCrearCancha(sedeActual)
            }
        }
    }

    private fun cargarCanchas() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE

        sede?.let { sedeActual ->
            // Consultar canchas donde sedeId = sede.id
            db.collection("canchas")
                .whereEqualTo("sedeId", sedeActual.id)
                .get()
                .addOnSuccessListener { documents ->
                    binding.progressBar.visibility = View.GONE

                    val canchas = documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Cancha::class.java).copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (canchas.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        adapter.updateList(canchas)
                    }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar canchas: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun eliminarCancha(cancha: Cancha) {
        // Confirmar eliminación
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Cancha")
            .setMessage("¿Estás seguro de eliminar '${cancha.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCanchaConfirmado(cancha)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarCanchaConfirmado(cancha: Cancha) {
        binding.progressBar.visibility = View.VISIBLE

        // Eliminar de Firestore
        db.collection("canchas")
            .document(cancha.id)
            .delete()
            .addOnSuccessListener {
                // Eliminar del array canchaIds de la sede
                sede?.let { sedeActual ->
                    db.collection("sedes")
                        .document(sedeActual.id)
                        .update("canchaIds", com.google.firebase.firestore.FieldValue.arrayRemove(cancha.id))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Cancha eliminada", Toast.LENGTH_SHORT).show()
                            cargarCanchas() // Recargar lista
                        }
                        .addOnFailureListener { e ->
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Error al actualizar sede: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error al eliminar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun navigateToCrearCancha(sede: Sede) {
        val fragment = CrearCanchaFragment.newInstanceForSede(
            sedeId = sede.id,
            sedeNombre = sede.nombre,
            horaApertura = sede.horaApertura,
            horaCierre = sede.horaCierre
        )
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SEDE = "sede"

        fun newInstance(sede: Sede): VerCanchasSedeFragment {
            return VerCanchasSedeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SEDE, sede)
                }
            }
        }
    }
}
