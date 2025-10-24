package com.rojassac.canchaya.ui.superadmin.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.*
import com.rojassac.canchaya.data.repository.NotificacionRepository
import com.rojassac.canchaya.databinding.FragmentCrearNotificacionBinding
import com.rojassac.canchaya.ui.superadmin.NotificacionViewModel
import com.rojassac.canchaya.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ NUEVO (24 Oct 2025)
 * Fragment para crear una nueva notificación masiva
 * SIN HILT - Inyección manual
 */
class CrearNotificacionFragment : Fragment() {

    private var _binding: FragmentCrearNotificacionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificacionViewModel

    private var tipoSeleccionado: TipoNotificacion = TipoNotificacion.INFO
    private var destinatariosSeleccionados: DestinatariosType = DestinatariosType.TODOS
    private var prioridadSeleccionada: PrioridadNotificacion = PrioridadNotificacion.NORMAL
    private var fechaProgramada: Date? = null
    private var envioInmediato: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearNotificacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModel manualmente
        val repository = NotificacionRepository(FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return NotificacionViewModel(repository) as T
                }
            }
        )[NotificacionViewModel::class.java]

        setupToolbar()
        setupChips()
        setupSpinners()
        setupListeners()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupChips() {
        // Chips de tipo de notificación
        binding.chipGroupTipo.removeAllViews()
        TipoNotificacion.values().forEach { tipo ->
            val chip = Chip(requireContext()).apply {
                text = "${tipo.icono} ${tipo.name}"
                isCheckable = true
                setOnClickListener {
                    tipoSeleccionado = tipo
                    // Desmarcar otros chips
                    for (i in 0 until binding.chipGroupTipo.childCount) {
                        val otherChip = binding.chipGroupTipo.getChildAt(i) as Chip
                        otherChip.isChecked = (otherChip == this)
                    }
                }
            }
            binding.chipGroupTipo.addView(chip)
            // Seleccionar el primero por defecto
            if (tipo == TipoNotificacion.INFO) {
                chip.isChecked = true
            }
        }

        // Chips de prioridad
        binding.chipGroupPrioridad.removeAllViews()
        PrioridadNotificacion.values().forEach { prioridad ->
            val chip = Chip(requireContext()).apply {
                text = prioridad.name
                isCheckable = true
                setOnClickListener {
                    prioridadSeleccionada = prioridad
                    // Desmarcar otros chips
                    for (i in 0 until binding.chipGroupPrioridad.childCount) {
                        val otherChip = binding.chipGroupPrioridad.getChildAt(i) as Chip
                        otherChip.isChecked = (otherChip == this)
                    }
                }
            }
            binding.chipGroupPrioridad.addView(chip)
            // Seleccionar NORMAL por defecto
            if (prioridad == PrioridadNotificacion.NORMAL) {
                chip.isChecked = true
            }
        }
    }

    private fun setupSpinners() {
        // Spinner de destinatarios
        val destinatarios = DestinatariosType.values().map { it.descripcion }
        val destinatariosAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            destinatarios
        )

        binding.spinnerDestinatarios.adapter = destinatariosAdapter
        binding.spinnerDestinatarios.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    destinatariosSeleccionados = DestinatariosType.values()[position]
                    // Mostrar campo de sede si es necesario
                    if (destinatariosSeleccionados == DestinatariosType.SEDE_ESPECIFICA) {
                        binding.layoutSede.visibility = View.VISIBLE
                    } else {
                        binding.layoutSede.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
    }

    private fun setupListeners() {
        // Switch de envío inmediato/programado
        binding.switchEnvioInmediato.setOnCheckedChangeListener { _, isChecked ->
            envioInmediato = isChecked
            if (isChecked) {
                binding.layoutFechaProgramada.visibility = View.GONE
                fechaProgramada = null
            } else {
                binding.layoutFechaProgramada.visibility = View.VISIBLE
            }
        }

        // Botón seleccionar fecha
        binding.btnSeleccionarFecha.setOnClickListener {
            mostrarDateTimePicker()
        }

        // Botón enviar
        binding.btnEnviar.setOnClickListener {
            validarYEnviar()
        }

        // Botón cancelar
        binding.btnCancelar.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Contador de caracteres del mensaje
        binding.etMensaje.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val length = s?.length ?: 0
                binding.tvContadorMensaje.text = "$length / 200"
                if (length > 200) {
                    binding.tvContadorMensaje.setTextColor(
                        resources.getColor(R.color.rojo, null)
                    )
                } else {
                    binding.tvContadorMensaje.setTextColor(
                        resources.getColor(R.color.gris_medio, null)
                    )
                }
            }
        })
    }

    private fun setupObservers() {
        // Observar resultado de creación
        viewModel.crearNotificacionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    mostrarLoading(true)
                }
                is Resource.Success -> {
                    mostrarLoading(false)
                    showSuccess("Notificación creada correctamente")
                    requireActivity().onBackPressed()
                }
                is Resource.Error -> {
                    mostrarLoading(false)
                    showError(resource.message ?: "Error al crear notificación")
                }
            }
        }

        // Observar resultado de envío
        viewModel.enviarNotificacionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val total = resource.data ?: 0
                    showSuccess("Notificación enviada a $total usuarios")
                }
                is Resource.Error -> {
                    showError(resource.message ?: "Error al enviar")
                }
                else -> {}
            }
        }
    }

    private fun mostrarDateTimePicker() {
        val calendar = Calendar.getInstance()

        // Primero seleccionar fecha
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Luego seleccionar hora
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        fechaProgramada = calendar.time

                        // Mostrar fecha seleccionada
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        binding.tvFechaSeleccionada.text = sdf.format(calendar.time)
                        binding.tvFechaSeleccionada.visibility = View.VISIBLE
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // No permitir fechas pasadas
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun validarYEnviar() {
        val titulo = binding.etTitulo.text.toString().trim()
        val mensaje = binding.etMensaje.text.toString().trim()
        val urlDestino = binding.etUrlDestino.text.toString().trim().takeIf { it.isNotEmpty() }

        // Validaciones básicas
        when {
            titulo.isEmpty() -> {
                showError("El título es obligatorio")
                binding.etTitulo.requestFocus()
                return
            }
            titulo.length < 3 -> {
                showError("El título debe tener al menos 3 caracteres")
                binding.etTitulo.requestFocus()
                return
            }
            titulo.length > 50 -> {
                showError("El título no puede tener más de 50 caracteres")
                binding.etTitulo.requestFocus()
                return
            }
            mensaje.isEmpty() -> {
                showError("El mensaje es obligatorio")
                binding.etMensaje.requestFocus()
                return
            }
            mensaje.length < 10 -> {
                showError("El mensaje debe tener al menos 10 caracteres")
                binding.etMensaje.requestFocus()
                return
            }
            mensaje.length > 200 -> {
                showError("El mensaje no puede tener más de 200 caracteres")
                binding.etMensaje.requestFocus()
                return
            }
            !envioInmediato && fechaProgramada == null -> {
                showError("Debe seleccionar una fecha para el envío programado")
                return
            }
            !envioInmediato && fechaProgramada?.before(Date()) == true -> {
                showError("La fecha programada debe ser futura")
                return
            }
            destinatariosSeleccionados == DestinatariosType.SEDE_ESPECIFICA &&
                    binding.spinnerSede.selectedItemPosition == 0 -> {
                showError("Debe seleccionar una sede")
                return
            }
        }

        // Obtener sedeId si es necesario
        val sedeId = if (destinatariosSeleccionados == DestinatariosType.SEDE_ESPECIFICA) {
            // TODO: Obtener ID real de la sede seleccionada
            "sede_id_ejemplo"
        } else null

        // Crear y enviar notificación
        if (envioInmediato) {
            viewModel.crearYEnviarNotificacion(
                titulo = titulo,
                mensaje = mensaje,
                tipo = tipoSeleccionado,
                destinatarios = destinatariosSeleccionados,
                urlDestino = urlDestino,
                sedeId = sedeId,
                prioridad = prioridadSeleccionada
            )
        } else {
            viewModel.crearNotificacionProgramada(
                titulo = titulo,
                mensaje = mensaje,
                tipo = tipoSeleccionado,
                destinatarios = destinatariosSeleccionados,
                fechaProgramada = fechaProgramada!!,
                urlDestino = urlDestino,
                sedeId = sedeId,
                prioridad = prioridadSeleccionada
            )
        }
    }

    private fun mostrarLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnEnviar.isEnabled = !show
        binding.btnCancelar.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
