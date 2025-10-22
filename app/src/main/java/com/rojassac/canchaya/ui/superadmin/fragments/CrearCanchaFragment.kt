package com.rojassac.canchaya.ui.superadmin.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rojassac.canchaya.databinding.FragmentCrearCanchaBinding
import java.util.*

class CrearCanchaFragment : Fragment() {

    private var _binding: FragmentCrearCanchaBinding? = null
    private val binding get() = _binding!!
    private var imagenUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imagenUri = result.data?.data
            imagenUri?.let { uri ->
                binding.ivPreviewCancha.setImageURI(uri)
                binding.tvNoImagen.visibility = View.GONE
                binding.ivPreviewCancha.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearCanchaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupRadioGroupListener()
    }

    private fun setupClickListeners() {
        binding.btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        binding.btnGuardar.setOnClickListener {
            guardarNegocio()
        }

        binding.btnCancelar.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRadioGroupListener() {
        // Listener para cambio de tipo de negocio
        binding.radioGroupTipo.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbSede.id -> {
                    // Ocultar campos específicos de Cancha Individual
                    binding.tilPrecio.visibility = View.GONE
                    binding.cardImagen.visibility = View.GONE
                    // Limpiar campos no necesarios
                    binding.etPrecio.text?.clear()
                    imagenUri = null
                }
                binding.rbCanchaIndividual.id -> {
                    // Mostrar campos específicos de Cancha Individual
                    binding.tilPrecio.visibility = View.VISIBLE
                    binding.cardImagen.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun guardarNegocio() {
        val nombre = binding.etNombre.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val precio = binding.etPrecio.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        // Obtener el tipo de negocio seleccionado (Sede o Cancha Individual)
        val tipoNegocio = when (binding.radioGroupTipo.checkedRadioButtonId) {
            binding.rbSede.id -> "sede"
            binding.rbCanchaIndividual.id -> "cancha_individual"
            else -> {
                Toast.makeText(requireContext(), "Selecciona el tipo de negocio", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Validar según tipo de negocio
        if (tipoNegocio == "sede") {
            if (!validateSedeInputs(nombre, direccion)) {
                Toast.makeText(requireContext(), "Por favor completa los campos requeridos", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            if (!validateCanchaInputs(nombre, direccion, precio)) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Mostrar loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        // Generar código único de invitación
        val codigoInvitacion = generarCodigoUnico(tipoNegocio)

        if (tipoNegocio == "sede") {
            crearSede(nombre, direccion, descripcion, codigoInvitacion)
        } else {
            crearCanchaIndividual(nombre, direccion, precio, descripcion, codigoInvitacion)
        }
    }

    private fun crearSede(
        nombre: String,
        direccion: String,
        descripcion: String,
        codigoInvitacion: String
    ) {
        val sedeData = hashMapOf<String, Any?>(
            "nombre" to nombre,
            "direccion" to direccion,
            "descripcion" to descripcion,
            "codigoInvitacion" to codigoInvitacion,
            "tipoNegocio" to "sede",
            "activa" to true,
            "adminAsignado" to null,
            "totalCanchas" to 0,
            "fechaCreacion" to FieldValue.serverTimestamp(),
            "creadoPor" to auth.currentUser?.uid
        )

        db.collection("sedes")
            .add(sedeData)
            .addOnSuccessListener { documentRef ->
                // Crear código de invitación
                crearCodigoInvitacion(
                    codigo = codigoInvitacion,
                    tipo = "sede",
                    sedeId = documentRef.id,
                    canchaId = null,
                    nombreNegocio = nombre
                )

                // Registrar en logs
                registrarEnLogs(codigoInvitacion, "sede", nombre)

                // Mostrar código generado
                mostrarCodigoGenerado(codigoInvitacion)

                Toast.makeText(requireContext(),
                    "Sede creada exitosamente\nCódigo: $codigoInvitacion",
                    Toast.LENGTH_LONG).show()

                clearForm()
                binding.progressBar.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(),
                    "Error al crear sede: ${e.message}",
                    Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
            }
    }

    private fun crearCanchaIndividual(
        nombre: String,
        direccion: String,
        precio: String,
        descripcion: String,
        codigoInvitacion: String
    ) {
        val canchaData = hashMapOf<String, Any?>(
            "nombre" to nombre,
            "direccion" to direccion,
            "descripcion" to descripcion,

            // ✅ COORDENADAS POR DEFECTO (puedes implementar selector de ubicación después)
            "latitud" to -6.7779,  // Nueva Cajamarca
            "longitud" to -77.5095,

            "precioHora" to precio.toDouble(),
            "codigoInvitacion" to codigoInvitacion,
            "tipoNegocio" to "cancha_individual",
            "sedeId" to null,
            "activa" to true,
            "adminAsignado" to null,
            "fechaCreacion" to System.currentTimeMillis(), // ✅ Long en lugar de FieldValue
            "creadoPor" to auth.currentUser?.uid,
            "totalResenas" to 0,
            "calificacionPromedio" to 0.0,
            "horarioApertura" to "00:00",
            "horarioCierre" to "23:00"
        )

        // Si hay imagen, subirla primero
        if (imagenUri != null) {
            subirImagenYCrearCancha(canchaData, codigoInvitacion, nombre)
        } else {
            // Crear sin imagen
            canchaData["imagenes"] = arrayListOf<String>()
            guardarCanchaEnFirestore(canchaData, codigoInvitacion, nombre)
        }
    }

    private fun subirImagenYCrearCancha(
        canchaData: HashMap<String, Any?>,
        codigoInvitacion: String,
        nombreCancha: String
    ) {
        val imagenRef = storage.reference
            .child("canchas/${UUID.randomUUID()}.jpg")

        imagenUri?.let { uri ->
            imagenRef.putFile(uri)
                .addOnSuccessListener {
                    imagenRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        canchaData["imagenes"] = arrayListOf(downloadUrl.toString())
                        guardarCanchaEnFirestore(canchaData, codigoInvitacion, nombreCancha)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(),
                        "Error al subir imagen: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                    // Guardar sin imagen
                    canchaData["imagenes"] = arrayListOf<String>()
                    guardarCanchaEnFirestore(canchaData, codigoInvitacion, nombreCancha)
                }
        }
    }

    private fun guardarCanchaEnFirestore(
        canchaData: HashMap<String, Any?>,
        codigoInvitacion: String,
        nombreCancha: String
    ) {
        db.collection("canchas")
            .add(canchaData)
            .addOnSuccessListener { documentRef ->
                // Crear código de invitación
                crearCodigoInvitacion(
                    codigo = codigoInvitacion,
                    tipo = "cancha_individual",
                    sedeId = null,
                    canchaId = documentRef.id,
                    nombreNegocio = nombreCancha
                )

                // Registrar en logs
                registrarEnLogs(codigoInvitacion, "cancha_individual", nombreCancha)

                // Mostrar código generado
                mostrarCodigoGenerado(codigoInvitacion)

                Toast.makeText(requireContext(),
                    "Cancha creada exitosamente\nCódigo: $codigoInvitacion",
                    Toast.LENGTH_LONG).show()

                clearForm()
                binding.progressBar.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(),
                    "Error al crear cancha: ${e.message}",
                    Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
            }
    }

    private fun crearCodigoInvitacion(
        codigo: String,
        tipo: String,
        sedeId: String?,
        canchaId: String?,
        nombreNegocio: String
    ) {
        val codigoData = hashMapOf<String, Any?>(
            "codigo" to codigo,
            "tipo" to tipo,
            "sedeId" to sedeId,
            "canchaId" to canchaId,
            "usado" to false,
            "activo" to true,
            "fechaCreacion" to FieldValue.serverTimestamp(),
            "creadoPor" to auth.currentUser?.uid,
            "adminAsignado" to null,
            "fechaUso" to null,
            "metadata" to hashMapOf<String, Any>(
                "nombreNegocio" to nombreNegocio
            )
        )

        db.collection("codigos_invitacion")
            .document(codigo)
            .set(codigoData)
            .addOnSuccessListener {
                // Código creado exitosamente
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(),
                    "Advertencia: Error al crear código de invitación",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun registrarEnLogs(codigo: String, tipo: String, nombre: String) {
        val logData = hashMapOf<String, Any?>(
            "codigo" to codigo,
            "tipo" to tipo,
            "nombre" to nombre,
            "accion" to "codigo_creado",
            "timestamp" to FieldValue.serverTimestamp(),
            "userId" to auth.currentUser?.uid
        )

        db.collection("codigo_logs")
            .add(logData)
    }

    private fun mostrarCodigoGenerado(codigo: String) {
        // Mostrar el card con el código generado si existe en el layout
        try {
            binding.cardCodigoGenerado.visibility = View.VISIBLE
            binding.tvCodigoGenerado.text = codigo
        } catch (e: Exception) {
            // Si no existe el card en el layout, ignorar
        }
    }

    private fun generarCodigoUnico(tipo: String): String {
        val prefijo = if (tipo == "sede") "SEDE" else "CANCHA"
        val random = UUID.randomUUID().toString().substring(0, 8).uppercase()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return "$prefijo-$random-$year"
    }

    private fun validateSedeInputs(
        nombre: String,
        direccion: String
    ): Boolean {
        return nombre.isNotEmpty() && direccion.isNotEmpty()
    }

    private fun validateCanchaInputs(
        nombre: String,
        direccion: String,
        precio: String
    ): Boolean {
        return nombre.isNotEmpty() &&
                direccion.isNotEmpty() &&
                precio.isNotEmpty() &&
                precio.toDoubleOrNull() != null &&
                precio.toDouble() > 0
    }

    private fun clearForm() {
        binding.etNombre.text?.clear()
        binding.etDireccion.text?.clear()
        binding.etPrecio.text?.clear()
        binding.etDescripcion.text?.clear()
        binding.radioGroupTipo.clearCheck()
        imagenUri = null
        binding.tvNoImagen.visibility = View.VISIBLE
        binding.ivPreviewCancha.visibility = View.GONE

        // Ocultar card de código si está visible
        try {
            binding.cardCodigoGenerado.visibility = View.GONE
        } catch (e: Exception) {
            // Ignorar si no existe
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
