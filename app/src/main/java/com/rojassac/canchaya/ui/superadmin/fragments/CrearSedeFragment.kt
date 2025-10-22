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
 * 🔄 ACTUALIZADO: Usar ViewModel en vez de Firestore directo (22 Oct 2025)
 */
class CrearSedeFragment : Fragment() {

    private var _binding: FragmentCrearSedeBinding? = null
    private val binding get() = _binding!!

    // 🔄 CAMBIADO: Usar ViewModel en vez de Firestore directo
    private val viewModel: SuperAdminViewModel by activityViewModels()

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var imageUri: Uri? = null
    private var sedeToEdit: Sede? = null
    private var currentLatitud: Double = 0.0
    private var currentLongitud: Double = 0.0

    // Launcher para seleccionar imagen
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.imgPreview.setImageURI(it)
            binding.imgPreview.visibility = View.VISIBLE
        }
    }

    // Launcher para permisos de ubicación
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
        setupObservers() // 🆕 NUEVO: Observar resultados del ViewModel

        // Si hay una sede para editar, cargar datos
        sedeToEdit?.let { cargarDatosSede(it) }
    }

    private fun setupListeners() {
        // Botón seleccionar imagen
        binding.btnSeleccionarImagen.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        // Botón obtener ubicación actual
        binding.btnObtenerUbicacion.setOnClickListener {
            solicitarPermisoUbicacion()
        }

        // Selector de hora de apertura
        binding.etHoraApertura.setOnClickListener {
            mostrarTimePicker { hora, minuto ->
                val horaFormateada = String.format("%02d:%02d", hora, minuto)
                binding.etHoraApertura.setText(horaFormateada)
            }
        }

        // Selector de hora de cierre
        binding.etHoraCierre.setOnClickListener {
            mostrarTimePicker { hora, minuto ->
                val horaFormateada = String.format("%02d:%02d", hora, minuto)
                binding.etHoraCierre.setText(horaFormateada)
            }
        }

        // Botón guardar
        binding.btnGuardar.setOnClickListener {
            if (validarCampos()) {
                guardarSede()
            }
        }

        // Botón cancelar
        binding.btnCancelar.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    // 🆕 NUEVO: Observar resultados del ViewModel
    private fun setupObservers() {
        viewModel.updateSedeResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnGuardar.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGuardar.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        if (sedeToEdit != null) "Sede actualizada correctamente" else "Sede creada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().onBackPressed()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGuardar.isEnabled = true
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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

    private fun obtenerUbicacionActual() {
        try {
            binding.progressBar.visibility = View.VISIBLE
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    binding.progressBar.visibility = View.GONE
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
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun validarCampos(): Boolean {
        var isValid = true

        if (binding.etNombre.text.toString().trim().isEmpty()) {
            binding.tilNombre.error = "Ingrese el nombre de la sede"
            isValid = false
        } else {
            binding.tilNombre.error = null
        }

        if (binding.etDireccion.text.toString().trim().isEmpty()) {
            binding.tilDireccion.error = "Ingrese la dirección"
            isValid = false
        } else {
            binding.tilDireccion.error = null
        }

        val latitud = binding.etLatitud.text.toString().toDoubleOrNull()
        val longitud = binding.etLongitud.text.toString().toDoubleOrNull()

        if (latitud == null || latitud == 0.0) {
            binding.tilLatitud.error = "Ingrese latitud válida"
            isValid = false
        } else {
            binding.tilLatitud.error = null
        }

        if (longitud == null || longitud == 0.0) {
            binding.tilLongitud.error = "Ingrese longitud válida"
            isValid = false
        } else {
            binding.tilLongitud.error = null
        }

        if (binding.etHoraApertura.text.toString().trim().isEmpty()) {
            binding.tilHoraApertura.error = "Seleccione hora de apertura"
            isValid = false
        } else {
            binding.tilHoraApertura.error = null
        }

        if (binding.etHoraCierre.text.toString().trim().isEmpty()) {
            binding.tilHoraCierre.error = "Seleccione hora de cierre"
            isValid = false
        } else {
            binding.tilHoraCierre.error = null
        }

        return isValid
    }

    private fun guardarSede() {
        val nombre = binding.etNombre.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val latitud = binding.etLatitud.text.toString().toDouble()
        val longitud = binding.etLongitud.text.toString().toDouble()
        val telefono = binding.etTelefono.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val horaApertura = binding.etHoraApertura.text.toString().trim()
        val horaCierre = binding.etHoraCierre.text.toString().trim()

        if (imageUri != null) {
            // Subir imagen primero
            subirImagenYGuardarSede(
                nombre, direccion, descripcion, latitud, longitud,
                telefono, email, horaApertura, horaCierre
            )
        } else {
            // Guardar sin imagen o con la URL existente
            val imageUrl = sedeToEdit?.imageUrl ?: ""
            guardarSedeEnViewModel(
                nombre, direccion, descripcion, latitud, longitud,
                telefono, email, horaApertura, horaCierre, imageUrl
            )
        }
    }

    private fun subirImagenYGuardarSede(
        nombre: String, direccion: String, descripcion: String,
        latitud: Double, longitud: Double, telefono: String,
        email: String, horaApertura: String, horaCierre: String
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        val fileName = "sedes/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    guardarSedeEnViewModel(
                        nombre, direccion, descripcion, latitud, longitud,
                        telefono, email, horaApertura, horaCierre, uri.toString()
                    )
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
                Toast.makeText(requireContext(), "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔄 CAMBIADO: Usar ViewModel en vez de Firestore directo
    private fun guardarSedeEnViewModel(
        nombre: String, direccion: String, descripcion: String,
        latitud: Double, longitud: Double, telefono: String,
        email: String, horaApertura: String, horaCierre: String,
        imageUrl: String
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
            canchasIds = sedeToEdit?.canchasIds ?: emptyList(),
            adminId = auth.currentUser?.uid.orEmpty()
        )

        // 🆕 NUEVO: Guardar a través del ViewModel
        viewModel.guardarSede(sede)
    }

    private fun cargarDatosSede(sede: Sede) {
        binding.apply {
            etNombre.setText(sede.nombre)
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

            if (sede.imageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(sede.imageUrl)
                    .into(imgPreview)
                imgPreview.visibility = View.VISIBLE
            }

            btnGuardar.text = "Actualizar Sede"
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
