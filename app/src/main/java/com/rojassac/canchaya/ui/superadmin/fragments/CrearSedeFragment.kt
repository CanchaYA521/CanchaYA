package com.rojassac.canchaya.ui.superadmin.fragments

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.FragmentCrearSedeBinding
import com.rojassac.canchaya.data.model.Sede
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.utils.Resource
import java.util.*

/**
 * ✅ CÓDIGO EXISTENTE MANTENIDO
 * ✨ ACTUALIZADO: Agregado manejo de amenidades con checkboxes (22 Oct 2025)
 * 🔧 CORREGIDO: cbBaños -> cbBanios para evitar problemas UTF-8 (22 Oct 2025)
 */
class CrearSedeFragment : Fragment() {

    private var _binding: FragmentCrearSedeBinding? = null
    private val binding get() = _binding!!

    // ✅ CÓDIGO EXISTENTE: ViewModel y servicios
    private val viewModel: SuperAdminViewModel by activityViewModels()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var imageUri: Uri? = null
    private var sedeToEdit: Sede? = null
    private var currentLatitud: Double = 0.0
    private var currentLongitud: Double = 0.0

    // ✅ CÓDIGO EXISTENTE: Launcher para seleccionar imagen
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.ivPreviewSede.setImageURI(it)
            binding.ivPreviewSede.visibility = View.VISIBLE
        }
    }

    // ✅ CÓDIGO EXISTENTE: Launcher para permisos de ubicación
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearSedeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupListeners()
        setupObservers()

        // ✅ CÓDIGO EXISTENTE: Si hay una sede para editar, cargar datos
        sedeToEdit?.let { cargarDatosSede(it) }
    }

    private fun setupListeners() {
        binding.apply {
            // ✅ CÓDIGO EXISTENTE: Botón seleccionar imagen
            btnSeleccionarImagen.setOnClickListener {
                selectImageLauncher.launch("image/*")
            }

            // ✅ CÓDIGO EXISTENTE: Botón obtener ubicación actual
            btnObtenerUbicacion.setOnClickListener {
                solicitarPermisoUbicacion()
            }

            // ✅ CÓDIGO EXISTENTE: Selector de hora de apertura
            etHoraApertura.setOnClickListener {
                mostrarTimePicker { hora, minuto ->
                    val horaFormateada = String.format("%02d:%02d", hora, minuto)
                    etHoraApertura.setText(horaFormateada)
                }
            }

            // ✅ CÓDIGO EXISTENTE: Selector de hora de cierre
            etHoraCierre.setOnClickListener {
                mostrarTimePicker { hora, minuto ->
                    val horaFormateada = String.format("%02d:%02d", hora, minuto)
                    etHoraCierre.setText(horaFormateada)
                }
            }

            // ✅ CÓDIGO EXISTENTE: Botón guardar
            btnGuardarSede.setOnClickListener {
                if (validarCampos()) {
                    guardarSede()
                }
            }

            // ✅ CÓDIGO EXISTENTE: Botón cancelar
            btnCancelar.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }

    // ✅ CÓDIGO EXISTENTE: Observar resultados del ViewModel
    private fun setupObservers() {
        viewModel.updateSedeResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnGuardarSede.isEnabled = false
                }

                is Resource.Success -> {
                    binding.btnGuardarSede.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        if (sedeToEdit != null) "Sede actualizada correctamente" else "Sede creada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().onBackPressed()
                }

                is Resource.Error -> {
                    binding.btnGuardarSede.isEnabled = true
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ✅ CÓDIGO EXISTENTE MANTENIDO
    private fun solicitarPermisoUbicacion() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                obtenerUbicacionActual()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // ✅ CÓDIGO EXISTENTE MANTENIDO
    private fun obtenerUbicacionActual() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLatitud = location.latitude
                        currentLongitud = location.longitude
                        binding.etLatitud.setText(currentLatitud.toString())
                        binding.etLongitud.setText(currentLongitud.toString())
                        Toast.makeText(requireContext(), "Ubicación obtenida correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ CÓDIGO EXISTENTE MANTENIDO
    private fun mostrarTimePicker(onTimeSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val minuto = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                onTimeSelected(selectedHour, selectedMinute)
            },
            hora,
            minuto,
            true
        ).show()
    }

    // ✅ CÓDIGO EXISTENTE
    private fun validarCampos(): Boolean {
        var isValid = true

        binding.apply {
            // Validar nombre
            if (etNombreSede.text.toString().trim().isEmpty()) {
                tilNombreSede.error = "Ingrese el nombre de la sede"
                isValid = false
            } else {
                tilNombreSede.error = null
            }

            // Validar dirección
            if (etDireccion.text.toString().trim().isEmpty()) {
                tilDireccion.error = "Ingrese la dirección"
                isValid = false
            } else {
                tilDireccion.error = null
            }

            // Validar latitud
            val latitud = etLatitud.text.toString().toDoubleOrNull()
            if (latitud == null || latitud == 0.0) {
                tilLatitud.error = "Ingrese latitud válida"
                isValid = false
            } else {
                tilLatitud.error = null
            }

            // Validar longitud
            val longitud = etLongitud.text.toString().toDoubleOrNull()
            if (longitud == null || longitud == 0.0) {
                tilLongitud.error = "Ingrese longitud válida"
                isValid = false
            } else {
                tilLongitud.error = null
            }

            // Validar hora de apertura
            if (etHoraApertura.text.toString().trim().isEmpty()) {
                tilHoraApertura.error = "Seleccione hora de apertura"
                isValid = false
            } else {
                tilHoraApertura.error = null
            }

            // Validar hora de cierre
            if (etHoraCierre.text.toString().trim().isEmpty()) {
                tilHoraCierre.error = "Seleccione hora de cierre"
                isValid = false
            } else {
                tilHoraCierre.error = null
            }
        }

        return isValid
    }

    // 🔧 CORREGIDO: cbBaños -> cbBanios
    private fun guardarSede() {
        binding.apply {
            val nombre = etNombreSede.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val latitud = etLatitud.text.toString().toDouble()
            val longitud = etLongitud.text.toString().toDouble()
            val telefono = etTelefono.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val horaApertura = etHoraApertura.text.toString().trim()
            val horaCierre = etHoraCierre.text.toString().trim()

            // ✨ NUEVO: Obtener valores de amenidades de los checkboxes
            val tieneDucha = cbDucha.isChecked
            val tieneGaraje = cbGaraje.isChecked
            val tieneLuzNocturna = cbLuzNocturna.isChecked
            val tieneEstacionamiento = cbEstacionamiento.isChecked
            val tieneBanios = cbBanios.isChecked  // 🔧 CORREGIDO
            val tieneWifi = cbWifi.isChecked
            val tieneCafeteria = cbCafeteria.isChecked
            val tieneVestidores = cbVestidores.isChecked

            if (imageUri != null) {
                // Subir imagen primero
                subirImagenYGuardarSede(
                    nombre, direccion, descripcion, latitud, longitud,
                    telefono, email, horaApertura, horaCierre,
                    tieneDucha, tieneGaraje, tieneLuzNocturna, tieneEstacionamiento,
                    tieneBanios, tieneWifi, tieneCafeteria, tieneVestidores
                )
            } else {
                // Guardar sin imagen o con la URL existente
                val imageUrl = sedeToEdit?.imageUrl ?: ""
                guardarSedeEnViewModel(
                    nombre, direccion, descripcion, latitud, longitud,
                    telefono, email, horaApertura, horaCierre, imageUrl,
                    tieneDucha, tieneGaraje, tieneLuzNocturna, tieneEstacionamiento,
                    tieneBanios, tieneWifi, tieneCafeteria, tieneVestidores
                )
            }
        }
    }

    // ✨ ACTUALIZADO: Agregados parámetros de amenidades
    private fun subirImagenYGuardarSede(
        nombre: String, direccion: String, descripcion: String,
        latitud: Double, longitud: Double, telefono: String,
        email: String, horaApertura: String, horaCierre: String,
        tieneDucha: Boolean, tieneGaraje: Boolean, tieneLuzNocturna: Boolean,
        tieneEstacionamiento: Boolean, tieneBanios: Boolean, tieneWifi: Boolean,
        tieneCafeteria: Boolean, tieneVestidores: Boolean
    ) {
        binding.btnGuardarSede.isEnabled = false

        val fileName = "sedes/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    guardarSedeEnViewModel(
                        nombre, direccion, descripcion, latitud, longitud,
                        telefono, email, horaApertura, horaCierre, uri.toString(),
                        tieneDucha, tieneGaraje, tieneLuzNocturna, tieneEstacionamiento,
                        tieneBanios, tieneWifi, tieneCafeteria, tieneVestidores
                    )
                }
            }
            .addOnFailureListener { e ->
                binding.btnGuardarSede.isEnabled = true
                Toast.makeText(requireContext(), "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ✨ ACTUALIZADO: Agregados parámetros de amenidades al crear objeto Sede
    private fun guardarSedeEnViewModel(
        nombre: String, direccion: String, descripcion: String,
        latitud: Double, longitud: Double, telefono: String,
        email: String, horaApertura: String, horaCierre: String,
        imageUrl: String,
        tieneDucha: Boolean, tieneGaraje: Boolean, tieneLuzNocturna: Boolean,
        tieneEstacionamiento: Boolean, tieneBanios: Boolean, tieneWifi: Boolean,
        tieneCafeteria: Boolean, tieneVestidores: Boolean
    ) {
        val sede = Sede(
            id = sedeToEdit?.id ?: "",
            nombre = nombre,
            direccion = direccion,
            descripcion = descripcion,
            latitud = latitud,
            longitud = longitud,
            telefono = telefono,
            email = email,
            horaApertura = horaApertura,
            horaCierre = horaCierre,
            imageUrl = imageUrl,
            activa = true,
            canchaIds = sedeToEdit?.canchaIds,
            adminId = sedeToEdit?.adminId ?: auth.currentUser?.uid.orEmpty(),
            codigoInvitacion = sedeToEdit?.codigoInvitacion ?: "",
            codigoActivo = sedeToEdit?.codigoActivo ?: true,
            fechaCreacion = sedeToEdit?.fechaCreacion,
            fechaModificacion = sedeToEdit?.fechaModificacion,
            // ✨ NUEVO: Asignar valores de amenidades
            tieneDucha = tieneDucha,
            tieneGaraje = tieneGaraje,
            tieneLuzNocturna = tieneLuzNocturna,
            tieneEstacionamiento = tieneEstacionamiento,
            tieneBaños = tieneBanios,  // 🔧 CORREGIDO: Se lee de cbBanios pero se guarda como tieneBaños
            tieneWifi = tieneWifi,
            tieneCafeteria = tieneCafeteria,
            tieneVestidores = tieneVestidores
        )

        // ✅ CÓDIGO EXISTENTE: Guardar a través del ViewModel
        viewModel.guardarSede(sede)
    }

    // ✨ ACTUALIZADO: Cargar también las amenidades al editar
    private fun cargarDatosSede(sede: Sede) {
        binding.apply {
            // ✅ CÓDIGO EXISTENTE: Datos básicos
            etNombreSede.setText(sede.nombre)
            etDireccion.setText(sede.direccion)
            etDescripcion.setText(sede.descripcion)
            etLatitud.setText(sede.latitud.toString())
            etLongitud.setText(sede.longitud.toString())
            etTelefono.setText(sede.telefono)
            etEmail.setText(sede.email)
            etHoraApertura.setText(sede.horaApertura)
            etHoraCierre.setText(sede.horaCierre)

            currentLatitud = sede.latitud
            currentLongitud = sede.longitud

            // ✅ CÓDIGO EXISTENTE: Cargar imagen
            if (sede.imageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(sede.imageUrl)
                    .into(ivPreviewSede)
                ivPreviewSede.visibility = View.VISIBLE
            }

            // ✨ NUEVO: Cargar amenidades (🔧 CORREGIDO)
            cbDucha.isChecked = sede.tieneDucha
            cbGaraje.isChecked = sede.tieneGaraje
            cbLuzNocturna.isChecked = sede.tieneLuzNocturna
            cbEstacionamiento.isChecked = sede.tieneEstacionamiento
            cbBanios.isChecked = sede.tieneBaños  // 🔧 CORREGIDO
            cbWifi.isChecked = sede.tieneWifi
            cbCafeteria.isChecked = sede.tieneCafeteria
            cbVestidores.isChecked = sede.tieneVestidores

            btnGuardarSede.text = "Actualizar Sede"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(sede: Sede? = null): CrearSedeFragment {
            return CrearSedeFragment().apply {
                sedeToEdit = sede
            }
        }
    }
}
