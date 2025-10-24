package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Promocion
import com.rojassac.canchaya.data.model.TipoDescuento
import com.rojassac.canchaya.databinding.DialogCrearPromocionBinding
import com.rojassac.canchaya.databinding.FragmentGestionPromocionesBinding
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.ui.superadmin.adapters.PromocionesAdapter
import com.rojassac.canchaya.utils.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ✅ NUEVO ARCHIVO (23 Oct 2025)
 * Fragment para gestionar promociones y cupones de descuento
 */
class GestionPromocionesFragment : Fragment() {

    private var _binding: FragmentGestionPromocionesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SuperAdminViewModel
    private lateinit var adapter: PromocionesAdapter

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionPromocionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SuperAdminViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupObservers()

        viewModel.loadPromociones()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = PromocionesAdapter(
            onEditClick = { promocion ->
                mostrarDialogEditarPromocion(promocion)
            },
            onDeleteClick = { promocion ->
                confirmarEliminarPromocion(promocion)
            },
            onToggleClick = { promocion, activo ->
                viewModel.togglePromocionStatus(promocion.id, activo)
            },
            onStatsClick = { promocion ->
                mostrarEstadisticasPromocion(promocion)
            }
        )

        binding.recyclerPromociones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GestionPromocionesFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabNuevaPromocion.setOnClickListener {
            mostrarDialogCrearPromocion()
        }
    }

    private fun setupObservers() {
        // Observar lista de promociones
        viewModel.promociones.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val promociones = resource.data ?: emptyList()

                    if (promociones.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        adapter.submitList(promociones)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Observar creación de promoción
        viewModel.createPromocionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Loading manejado en el diálogo
                }
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Promoción creada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadPromociones()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Observar actualización de promoción
        viewModel.updatePromocionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Loading
                }
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Promoción actualizada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadPromociones()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Observar eliminación de promoción
        viewModel.deletePromocionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Loading
                }
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Promoción eliminada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadPromociones()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun mostrarDialogCrearPromocion() {
        val dialogBinding = DialogCrearPromocionBinding.inflate(layoutInflater)

        var fechaInicio = 0L
        var fechaFin = 0L

        // DatePicker para fecha de inicio
        dialogBinding.btnSeleccionarFechaInicio.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Fecha de inicio")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                fechaInicio = selection
                dialogBinding.tvFechaInicio.text = dateFormat.format(Date(selection))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER_INICIO")
        }

        // DatePicker para fecha de fin
        dialogBinding.btnSeleccionarFechaFin.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Fecha de fin")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                fechaFin = selection
                dialogBinding.tvFechaFin.text = dateFormat.format(Date(selection))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER_FIN")
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnCrear.setOnClickListener {
            val codigo = dialogBinding.etCodigo.text.toString().trim().uppercase()
            val nombre = dialogBinding.etNombre.text.toString().trim()
            val descripcion = dialogBinding.etDescripcion.text.toString().trim()
            val valorDescuento = dialogBinding.etValorDescuento.text.toString().toDoubleOrNull()
            val usosMaximos = dialogBinding.etUsosMaximos.text.toString().toIntOrNull() ?: -1
            val usosMaximosPorUsuario = dialogBinding.etUsosMaximosPorUsuario.text.toString().toIntOrNull() ?: 1

            // Validaciones
            if (codigo.isEmpty()) {
                dialogBinding.tilCodigo.error = "Ingresa un código"
                return@setOnClickListener
            }

            if (nombre.isEmpty()) {
                dialogBinding.tilNombre.error = "Ingresa un nombre"
                return@setOnClickListener
            }

            if (valorDescuento == null || valorDescuento <= 0) {
                dialogBinding.tilValorDescuento.error = "Ingresa un valor válido"
                return@setOnClickListener
            }

            if (fechaInicio == 0L) {
                Toast.makeText(requireContext(), "Selecciona fecha de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaFin == 0L) {
                Toast.makeText(requireContext(), "Selecciona fecha de fin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaFin < fechaInicio) {
                Toast.makeText(requireContext(), "La fecha de fin debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipoDescuento = if (dialogBinding.radioGrupoPorcentaje.isChecked) {
                TipoDescuento.PORCENTAJE
            } else {
                TipoDescuento.MONTO_FIJO
            }

            // Validar porcentaje
            if (tipoDescuento == TipoDescuento.PORCENTAJE && valorDescuento > 100) {
                dialogBinding.tilValorDescuento.error = "El porcentaje no puede ser mayor a 100"
                return@setOnClickListener
            }

            val promocion = Promocion(
                codigo = codigo,
                nombre = nombre,
                descripcion = descripcion,
                tipoDescuento = tipoDescuento,
                valorDescuento = valorDescuento,
                aplicaATodos = dialogBinding.switchAplicaTodos.isChecked,
                usosMaximos = usosMaximos,
                usosMaximosPorUsuario = usosMaximosPorUsuario,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                activo = true,
                creadoPor = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            )

            viewModel.crearPromocion(promocion)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogEditarPromocion(promocion: Promocion) {
        val dialogBinding = DialogCrearPromocionBinding.inflate(layoutInflater)

        var fechaInicio = promocion.fechaInicio
        var fechaFin = promocion.fechaFin

        // Prellenar datos
        dialogBinding.apply {
            etCodigo.setText(promocion.codigo)
            etCodigo.isEnabled = false // No se puede cambiar el código
            etNombre.setText(promocion.nombre)
            etDescripcion.setText(promocion.descripcion)
            etValorDescuento.setText(promocion.valorDescuento.toString())
            etUsosMaximos.setText(if (promocion.usosMaximos == -1) "" else promocion.usosMaximos.toString())
            etUsosMaximosPorUsuario.setText(promocion.usosMaximosPorUsuario.toString())
            switchAplicaTodos.isChecked = promocion.aplicaATodos
            tvFechaInicio.text = dateFormat.format(Date(promocion.fechaInicio))
            tvFechaFin.text = dateFormat.format(Date(promocion.fechaFin))

            if (promocion.tipoDescuento == TipoDescuento.PORCENTAJE) {
                radioGrupoPorcentaje.isChecked = true
            } else {
                radioGrupoMontoFijo.isChecked = true
            }

            btnCrear.text = "Actualizar"
        }

        // DatePicker para fecha de inicio
        dialogBinding.btnSeleccionarFechaInicio.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Fecha de inicio")
                .setSelection(fechaInicio)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                fechaInicio = selection
                dialogBinding.tvFechaInicio.text = dateFormat.format(Date(selection))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER_INICIO")
        }

        // DatePicker para fecha de fin
        dialogBinding.btnSeleccionarFechaFin.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Fecha de fin")
                .setSelection(fechaFin)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                fechaFin = selection
                dialogBinding.tvFechaFin.text = dateFormat.format(Date(selection))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER_FIN")
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnCrear.setOnClickListener {
            val nombre = dialogBinding.etNombre.text.toString().trim()
            val descripcion = dialogBinding.etDescripcion.text.toString().trim()
            val valorDescuento = dialogBinding.etValorDescuento.text.toString().toDoubleOrNull()
            val usosMaximos = dialogBinding.etUsosMaximos.text.toString().toIntOrNull() ?: -1
            val usosMaximosPorUsuario = dialogBinding.etUsosMaximosPorUsuario.text.toString().toIntOrNull() ?: 1

            // Validaciones
            if (nombre.isEmpty()) {
                dialogBinding.tilNombre.error = "Ingresa un nombre"
                return@setOnClickListener
            }

            if (valorDescuento == null || valorDescuento <= 0) {
                dialogBinding.tilValorDescuento.error = "Ingresa un valor válido"
                return@setOnClickListener
            }

            if (fechaFin < fechaInicio) {
                Toast.makeText(requireContext(), "La fecha de fin debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipoDescuento = if (dialogBinding.radioGrupoPorcentaje.isChecked) {
                TipoDescuento.PORCENTAJE
            } else {
                TipoDescuento.MONTO_FIJO
            }

            if (tipoDescuento == TipoDescuento.PORCENTAJE && valorDescuento > 100) {
                dialogBinding.tilValorDescuento.error = "El porcentaje no puede ser mayor a 100"
                return@setOnClickListener
            }

            val promocionActualizada = promocion.copy(
                nombre = nombre,
                descripcion = descripcion,
                tipoDescuento = tipoDescuento,
                valorDescuento = valorDescuento,
                aplicaATodos = dialogBinding.switchAplicaTodos.isChecked,
                usosMaximos = usosMaximos,
                usosMaximosPorUsuario = usosMaximosPorUsuario,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin
            )

            viewModel.actualizarPromocion(promocionActualizada)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmarEliminarPromocion(promocion: Promocion) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar promoción")
            .setMessage("¿Estás seguro de eliminar la promoción '${promocion.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarPromocion(promocion.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarEstadisticasPromocion(promocion: Promocion) {
        viewModel.loadEstadisticasPromocion(promocion.id)

        viewModel.estadisticasPromocion.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val stats = resource.data ?: emptyMap()
                    val totalUsos = stats["totalUsos"] as? Int ?: 0
                    val usuariosUnicos = stats["usuariosUnicos"] as? Int ?: 0
                    val montoDescontado = stats["montoTotalDescontado"] as? Double ?: 0.0

                    val mensaje = """
                        Estadísticas de '${promocion.nombre}':
                        
                        • Total de usos: $totalUsos
                        • Usuarios únicos: $usuariosUnicos
                        • Monto total descontado: S/. ${String.format("%.2f", montoDescontado)}
                        • Usos restantes: ${if (promocion.usosMaximos == -1) "Ilimitados" else (promocion.usosMaximos - promocion.usosActuales)}
                    """.trimIndent()

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Estadísticas")
                        .setMessage(mensaje)
                        .setPositiveButton("Cerrar", null)
                        .show()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar estadísticas: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
