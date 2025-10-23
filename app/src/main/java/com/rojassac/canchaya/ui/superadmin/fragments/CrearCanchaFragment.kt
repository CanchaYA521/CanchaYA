package com.rojassac.canchaya.ui.superadmin.fragments

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.FranjaHoraria
import com.rojassac.canchaya.databinding.FragmentCrearCanchaBinding
import java.util.*

/**
 * ✅ CÓDIGO EXISTENTE MANTENIDO
 * ✨ ACTUALIZADO: Agregado soporte para franjas horarias y sedeId (23 Oct 2025)
 */
class CrearCanchaFragment : Fragment() {

    private var _binding: FragmentCrearCanchaBinding? = null
    private val binding get() = _binding!!
    private var imagenUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ✨ NUEVO: Variables para manejo de sede (23 Oct 2025)
    private var sedeId: String? = null
    private var sedeNombre: String? = null
    private var horaAperturaSede: String = "06:00"
    private var horaCierreSede: String = "23:00"

    // ✨ NUEVO: Lista de franjas horarias (23 Oct 2025)
    private val franjasHorarias = mutableListOf<FranjaHorariaView>()
    private var contadorFranjas = 0

    // ✅ CÓDIGO EXISTENTE: Launcher para seleccionar imagen
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imagenUri = uri
                binding.ivImagenPreview.setImageURI(uri)
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

        // ✨ NUEVO: Recuperar argumentos de sede (23 Oct 2025)
        arguments?.let {
            sedeId = it.getString(ARG_SEDE_ID)
            sedeNombre = it.getString(ARG_SEDE_NOMBRE)
            horaAperturaSede = it.getString(ARG_HORA_APERTURA) ?: "06:00"
            horaCierreSede = it.getString(ARG_HORA_CIERRE) ?: "23:00"
        }

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // ✨ NUEVO: Si viene de una sede, ocultar RadioGroup y pre-llenar info (23 Oct 2025)
        if (sedeId != null) {
            binding.rgTipo.visibility = View.GONE
            binding.tvInfoSedeHorarios.text =
                "📍 Sede: $sedeNombre\n⏰ Horarios heredados: $horaAperturaSede - $horaCierreSede"

            // Pre-llenar horarios de la sede
            binding.etHorarioApertura.setText(horaAperturaSede)
            binding.etHorarioCierre.setText(horaCierreSede)
        } else {
            // ✅ CÓDIGO EXISTENTE: Modo normal (con RadioGroup)
            binding.tvInfoSedeHorarios.text = "Define precios diferentes según la hora del día."
        }
    }

    private fun setupListeners() {
        // ✅ CÓDIGO EXISTENTE: RadioGroup
        binding.rgTipo.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbSede -> {
                    // Lógica para sede
                }
                R.id.rbCancha -> {
                    // Lógica para cancha
                }
            }
        }

        // ✅ CÓDIGO EXISTENTE: Seleccionar imagen
        binding.btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectImageLauncher.launch(intent)
        }

        // ✅ CÓDIGO EXISTENTE: TimePickerDialog para horarios
        binding.etHorarioApertura.setOnClickListener {
            mostrarTimePickerDialog { hora, minuto ->
                val horaFormateada = String.format("%02d:%02d", hora, minuto)
                binding.etHorarioApertura.setText(horaFormateada)
            }
        }

        binding.etHorarioCierre.setOnClickListener {
            mostrarTimePickerDialog { hora, minuto ->
                val horaFormateada = String.format("%02d:%02d", hora, minuto)
                binding.etHorarioCierre.setText(horaFormateada)
            }
        }

        // ✨ NUEVO: Botón agregar franja horaria (23 Oct 2025)
        binding.btnAgregarFranja.setOnClickListener {
            agregarFranjaHoraria()
        }

        // ✅ CÓDIGO EXISTENTE: Guardar cancha
        binding.btnGuardarCancha.setOnClickListener {
            if (validarCampos()) {
                guardarCancha()
            }
        }
    }

    // ✅ CÓDIGO EXISTENTE: Mostrar TimePicker
    private fun mostrarTimePickerDialog(onTimeSelected: (Int, Int) -> Unit) {
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

    // ✨ NUEVO: Agregar franja horaria dinámica (23 Oct 2025)
    private fun agregarFranjaHoraria() {
        contadorFranjas++

        // Inflar el layout de item_franja_horaria
        val franjaView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_franja_horaria, binding.containerFranjas, false)

        // Obtener referencias a los campos
        val tvTitulo = franjaView.findViewById<android.widget.TextView>(R.id.tvTituloFranja)
        val tilHoraInicio = franjaView.findViewById<TextInputLayout>(R.id.tilHoraInicio)
        val etHoraInicio = franjaView.findViewById<TextInputEditText>(R.id.etHoraInicio)
        val tilHoraFin = franjaView.findViewById<TextInputLayout>(R.id.tilHoraFin)
        val etHoraFin = franjaView.findViewById<TextInputEditText>(R.id.etHoraFin)
        val tilPrecio = franjaView.findViewById<TextInputLayout>(R.id.tilPrecio)
        val etPrecio = franjaView.findViewById<TextInputEditText>(R.id.etPrecio)
        val btnEliminar = franjaView.findViewById<android.widget.ImageButton>(R.id.btnEliminarFranja)

        // Configurar título
        tvTitulo.text = "Franja $contadorFranjas"

        // ✨ Pre-llenar con valores sugeridos según número de franja
        when (contadorFranjas) {
            1 -> {
                etHoraInicio.setText(horaAperturaSede)
                etHoraFin.setText("12:00")
                etPrecio.setText("50")
            }
            2 -> {
                etHoraInicio.setText("12:00")
                etHoraFin.setText("18:00")
                etPrecio.setText("60")
            }
            3 -> {
                etHoraInicio.setText("18:00")
                etHoraFin.setText(horaCierreSede)
                etPrecio.setText("70")
            }
        }

        // Configurar TimePickerDialog para hora inicio
        etHoraInicio.setOnClickListener {
            mostrarTimePickerDialog { hora, minuto ->
                val horaFormateada = String.format("%02d:%02d", hora, minuto)
                etHoraInicio.setText(horaFormateada)
            }
        }

        // Configurar TimePickerDialog para hora fin
        etHoraFin.setOnClickListener {
            mostrarTimePickerDialog { hora, minuto ->
                val horaFormateada = String.format("%02d:%02d", hora, minuto)
                etHoraFin.setText(horaFormateada)
            }
        }

        // Crear objeto FranjaHorariaView
        val franjaHorariaView = FranjaHorariaView(
            view = franjaView,
            tilHoraInicio = tilHoraInicio,
            etHoraInicio = etHoraInicio,
            tilHoraFin = tilHoraFin,
            etHoraFin = etHoraFin,
            tilPrecio = tilPrecio,
            etPrecio = etPrecio
        )

        // Agregar a la lista
        franjasHorarias.add(franjaHorariaView)

        // Botón eliminar
        btnEliminar.setOnClickListener {
            eliminarFranjaHoraria(franjaHorariaView)
        }

        // Agregar al contenedor
        binding.containerFranjas.addView(franjaView)

        // Ocultar mensaje de "no hay franjas"
        binding.tvNoFranjas.visibility = View.GONE
    }

    // ✨ NUEVO: Eliminar franja horaria (23 Oct 2025)
    private fun eliminarFranjaHoraria(franja: FranjaHorariaView) {
        binding.containerFranjas.removeView(franja.view)
        franjasHorarias.remove(franja)

        // Mostrar mensaje si no quedan franjas
        if (franjasHorarias.isEmpty()) {
            binding.tvNoFranjas.visibility = View.VISIBLE
        }
    }

    // ✨ ACTUALIZADO: Validar campos (23 Oct 2025)
    private fun validarCampos(): Boolean {
        var isValid = true

        binding.apply {
            // Validar nombre
            if (etNombre.text.toString().trim().isEmpty()) {
                tilNombre.error = "Ingrese el nombre de la cancha"
                isValid = false
            } else {
                tilNombre.error = null
            }

            // Validar dirección
            if (etDireccion.text.toString().trim().isEmpty()) {
                tilDireccion.error = "Ingrese la dirección"
                isValid = false
            } else {
                tilDireccion.error = null
            }

            // Validar precio base O franjas
            val precioBase = etPrecioHora.text.toString().toDoubleOrNull()
            if (franjasHorarias.isEmpty() && (precioBase == null || precioBase <= 0)) {
                tilPrecioHora.error = "Ingrese un precio base o configure franjas horarias"
                isValid = false
            } else {
                tilPrecioHora.error = null
            }

            // ✨ NUEVO: Validar franjas horarias (23 Oct 2025)
            for ((index, franja) in franjasHorarias.withIndex()) {
                val horaInicio = franja.etHoraInicio.text.toString()
                val horaFin = franja.etHoraFin.text.toString()
                val precio = franja.etPrecio.text.toString().toDoubleOrNull()

                if (horaInicio.isEmpty()) {
                    franja.tilHoraInicio.error = "Requerido"
                    isValid = false
                } else {
                    franja.tilHoraInicio.error = null
                }

                if (horaFin.isEmpty()) {
                    franja.tilHoraFin.error = "Requerido"
                    isValid = false
                } else {
                    franja.tilHoraFin.error = null
                }

                if (precio == null || precio <= 0) {
                    franja.tilPrecio.error = "Precio inválido"
                    isValid = false
                } else {
                    franja.tilPrecio.error = null
                }
            }
        }

        return isValid
    }

    // ✨ ACTUALIZADO: Guardar cancha con franjas horarias (23 Oct 2025)
    private fun guardarCancha() {
        binding.btnGuardarCancha.isEnabled = false

        val nombre = binding.etNombre.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val distrito = binding.etDistrito.text.toString().trim()
        val ciudad = binding.etCiudad.text.toString().trim()
        val precioHora = binding.etPrecioHora.text.toString().toDoubleOrNull() ?: 0.0
        val horarioApertura = binding.etHorarioApertura.text.toString()
        val horarioCierre = binding.etHorarioCierre.text.toString()

        // ✅ CÓDIGO EXISTENTE: Obtener servicios seleccionados
        val servicios = mutableListOf<String>()
        if (binding.chipEstacionamiento.isChecked) servicios.add("Estacionamiento")
        if (binding.chipVestuarios.isChecked) servicios.add("Vestuarios")
        if (binding.chipDuchas.isChecked) servicios.add("Duchas")
        if (binding.chipIluminacion.isChecked) servicios.add("Iluminación")
        if (binding.chipCafeteria.isChecked) servicios.add("Cafetería")

        // ✨ NUEVO: Obtener franjas horarias (23 Oct 2025)
        val preciosPorFranja = franjasHorarias.map { franja ->
            FranjaHoraria(
                horaInicio = franja.etHoraInicio.text.toString(),
                horaFin = franja.etHoraFin.text.toString(),
                precio = franja.etPrecio.text.toString().toDouble()
            )
        }

        if (imagenUri != null) {
            // Subir imagen primero
            subirImagenYGuardarCancha(
                nombre, direccion, descripcion, distrito, ciudad,
                precioHora, servicios, horarioApertura, horarioCierre,
                preciosPorFranja
            )
        } else {
            // Guardar sin imagen
            guardarCanchaEnFirestore(
                nombre, direccion, descripcion, distrito, ciudad,
                precioHora, servicios, horarioApertura, horarioCierre,
                "", preciosPorFranja
            )
        }
    }

    // ✨ ACTUALIZADO: Subir imagen y guardar (23 Oct 2025)
    private fun subirImagenYGuardarCancha(
        nombre: String, direccion: String, descripcion: String,
        distrito: String, ciudad: String, precioHora: Double,
        servicios: List<String>, horarioApertura: String, horarioCierre: String,
        preciosPorFranja: List<FranjaHoraria>
    ) {
        val fileName = "canchas/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(imagenUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    guardarCanchaEnFirestore(
                        nombre, direccion, descripcion, distrito, ciudad,
                        precioHora, servicios, horarioApertura, horarioCierre,
                        uri.toString(), preciosPorFranja
                    )
                }
            }
            .addOnFailureListener { e ->
                binding.btnGuardarCancha.isEnabled = true
                Toast.makeText(requireContext(), "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ✨ ACTUALIZADO: Guardar cancha en Firestore con franjas (23 Oct 2025)
    private fun guardarCanchaEnFirestore(
        nombre: String, direccion: String, descripcion: String,
        distrito: String, ciudad: String, precioHora: Double,
        servicios: List<String>, horarioApertura: String, horarioCierre: String,
        imagenUrl: String, preciosPorFranja: List<FranjaHoraria>
    ) {
        val cancha = Cancha(
            nombre = nombre,
            direccion = direccion,
            descripcion = descripcion,
            distrito = distrito,
            ciudad = ciudad,
            precioHora = precioHora,
            imagenUrl = imagenUrl,
            servicios = servicios,
            horarioApertura = horarioApertura,
            horarioCierre = horarioCierre,
            adminId = auth.currentUser?.uid,
            activo = true,
            aprobado = false,
            sedeId = sedeId, // ✨ NUEVO: Asociar a sede
            preciosPorFranja = preciosPorFranja, // ✨ NUEVO: Franjas horarias
            fechaCreacion = FieldValue.serverTimestamp(),
            fechaActualizacion = FieldValue.serverTimestamp()
        )

        db.collection("canchas")
            .add(cancha)
            .addOnSuccessListener { documentReference ->
                val canchaId = documentReference.id

                // ✨ NUEVO: Si está asociada a una sede, actualizar el array canchaIds (23 Oct 2025)
                if (sedeId != null) {
                    db.collection("sedes").document(sedeId!!)
                        .update("canchaIds", FieldValue.arrayUnion(canchaId))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Cancha creada y asociada a la sede", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Cancha creada pero error al asociar: ${e.message}", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        }
                } else {
                    Toast.makeText(requireContext(), "Cancha creada correctamente", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }

                binding.btnGuardarCancha.isEnabled = true
            }
            .addOnFailureListener { e ->
                binding.btnGuardarCancha.isEnabled = true
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ✨ NUEVO: Data class para manejar vistas de franjas (23 Oct 2025)
    private data class FranjaHorariaView(
        val view: View,
        val tilHoraInicio: TextInputLayout,
        val etHoraInicio: TextInputEditText,
        val tilHoraFin: TextInputLayout,
        val etHoraFin: TextInputEditText,
        val tilPrecio: TextInputLayout,
        val etPrecio: TextInputEditText
    )

    companion object {
        // ✨ NUEVO: Argumentos para recibir sedeId (23 Oct 2025)
        private const val ARG_SEDE_ID = "sedeId"
        private const val ARG_SEDE_NOMBRE = "sedeNombre"
        private const val ARG_HORA_APERTURA = "horaApertura"
        private const val ARG_HORA_CIERRE = "horaCierre"

        // ✨ NUEVO: Factory method para crear cancha desde sede (23 Oct 2025)
        fun newInstanceForSede(
            sedeId: String,
            sedeNombre: String,
            horaApertura: String,
            horaCierre: String
        ): CrearCanchaFragment {
            return CrearCanchaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SEDE_ID, sedeId)
                    putString(ARG_SEDE_NOMBRE, sedeNombre)
                    putString(ARG_HORA_APERTURA, horaApertura)
                    putString(ARG_HORA_CIERRE, horaCierre)
                }
            }
        }

        // ✅ CÓDIGO EXISTENTE: Factory method normal (sin sede)
        fun newInstance(): CrearCanchaFragment {
            return CrearCanchaFragment()
        }
    }
}
