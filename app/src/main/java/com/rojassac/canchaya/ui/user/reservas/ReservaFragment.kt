package com.rojassac.canchaya.ui.user.reservas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.data.model.EstadoReserva
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.data.repository.ReservaRepository
import com.rojassac.canchaya.databinding.FragmentReservaBinding
import com.rojassac.canchaya.utils.toast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HorarioItem(
    val hora: String,
    val disponible: Boolean
)

class ReservaFragment : Fragment() {

    private var _binding: FragmentReservaBinding? = null
    private val binding get() = _binding!!

    private var cancha: Cancha? = null

    // ✅ CORREGIDO: Inicializar directamente en lugar de nullable
    private lateinit var horarioAdapter: HorarioAdapter

    private var selectedDate: String = ""
    private var selectedHora: String = ""

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var reservaRepository: ReservaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cancha = it.getParcelable("cancha")
        }
        reservaRepository = ReservaRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancha?.let { setupUI(it) } ?: run {
            requireContext().toast("Error: Cancha no encontrada")
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI(cancha: Cancha) {
        binding.apply {
            toolbar.title = "Reservar: ${cancha.nombre}"
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }

            tvCanchaNombre.text = cancha.nombre
            tvPrecio.text = "S/ ${cancha.precioHora}/hora"

            // ✅ IMPORTANTE: Inicializar adapter PRIMERO
            setupHorariosGrid()

            // ✅ LUEGO configurar el selector de fecha
            setupDateSelector()

            btnConfirmar.setOnClickListener {
                confirmarReserva()
            }
        }
    }

    private fun setupHorariosGrid() {
        // ✅ INICIALIZAR adapter AQUÍ
        horarioAdapter = HorarioAdapter { horario ->
            if (horario.disponible) {
                selectedHora = horario.hora
                requireContext().toast("Hora seleccionada: $selectedHora")
                binding.btnConfirmar.isEnabled = true
            } else {
                requireContext().toast("Horario no disponible")
            }
        }

        binding.rvHorarios.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = horarioAdapter
        }
    }

    private fun setupDateSelector() {
        val today = Calendar.getInstance()
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)
        val fechaMostrar = SimpleDateFormat("dd 'de' MMMM", Locale("es", "PE")).format(today.time)

        val tvFecha = binding.root.findViewById<TextView>(R.id.tvFechaSeleccionada)
        tvFecha?.text = fechaMostrar

        binding.root.findViewById<View>(R.id.btnPrevDay)?.setOnClickListener {
            cambiarFecha(-1, tvFecha)
        }

        binding.root.findViewById<View>(R.id.btnNextDay)?.setOnClickListener {
            cambiarFecha(1, tvFecha)
        }

        // ✅ Ahora es seguro llamar loadHorarios() porque el adapter ya está inicializado
        loadHorarios()
    }

    private fun cambiarFecha(dias: Int, tvFecha: TextView?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(selectedDate) ?: return
        calendar.add(Calendar.DAY_OF_MONTH, dias)

        val hoy = Calendar.getInstance()
        hoy.set(Calendar.HOUR_OF_DAY, 0)
        hoy.set(Calendar.MINUTE, 0)
        hoy.set(Calendar.SECOND, 0)

        if (calendar.before(hoy)) {
            requireContext().toast("No puedes reservar en fechas pasadas")
            return
        }

        selectedDate = sdf.format(calendar.time)
        val fechaMostrar = SimpleDateFormat("dd 'de' MMMM", Locale("es", "PE")).format(calendar.time)
        tvFecha?.text = fechaMostrar

        loadHorarios()
    }

    private fun loadHorarios() {
        val canchaData = cancha ?: return

        val horaInicio = canchaData.horarioApertura.split(":")[0].toInt()
        val horaFin = canchaData.horarioCierre.split(":")[0].toInt()

        val horarios = mutableListOf<HorarioItem>()
        val now = Calendar.getInstance()
        val selectedCal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedCal.time = sdf.parse(selectedDate) ?: now.time

        val esHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time) == selectedDate
        val horaActual = now.get(Calendar.HOUR_OF_DAY)

        for (hora in horaInicio until horaFin) {
            val horaStr = String.format("%02d:00", hora)
            val disponible = if (esHoy) {
                hora > horaActual
            } else {
                true
            }

            horarios.add(HorarioItem(hora = horaStr, disponible = disponible))
        }

        // ✅ SEGURO: adapter ya está inicializado
        horarioAdapter.submitList(horarios)
    }

    private fun confirmarReserva() {
        if (selectedHora.isEmpty()) {
            requireContext().toast("Selecciona un horario")
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            requireContext().toast("Debes iniciar sesión")
            return
        }

        val canchaData = cancha ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmar.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val disponibilidadResult = reservaRepository.verificarDisponibilidad(
                    canchaId = canchaData.id,
                    fecha = selectedDate,
                    hora = selectedHora
                )

                if (disponibilidadResult.isSuccess && disponibilidadResult.getOrNull() == true) {
                    val horaInicio = selectedHora
                    val horaFin = calcularHoraFin(selectedHora)

                    val reserva = Reserva(
                        id = "",
                        canchaId = canchaData.id,
                        canchaNombre = canchaData.nombre,
                        usuarioId = currentUser.uid,
                        usuarioNombre = currentUser.displayName ?: "Usuario",
                        usuarioCelular = "987654321",
                        fecha = selectedDate,
                        horaInicio = horaInicio,
                        horaFin = horaFin,
                        precio = canchaData.precioHora,
                        estado = EstadoReserva.CONFIRMADA,
                        metodoPago = "Efectivo",
                        fechaCreacion = System.currentTimeMillis()
                    )

                    val resultado = reservaRepository.crearReserva(
                        reserva = reserva,
                        canchaNombre = canchaData.nombre
                    )

                    if (resultado.isSuccess) {
                        binding.progressBar.visibility = View.GONE
                        requireContext().toast("¡Reserva confirmada! 🎉")
                        parentFragmentManager.popBackStack()
                    } else {
                        throw Exception("Error al crear reserva")
                    }

                } else {
                    requireContext().toast("Horario no disponible")
                    binding.progressBar.visibility = View.GONE
                    binding.btnConfirmar.isEnabled = true
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnConfirmar.isEnabled = true
                requireContext().toast("Error: ${e.message}")
            }
        }
    }

    private fun calcularHoraFin(horaInicio: String): String {
        val hora = horaInicio.split(":")[0].toInt() + 1
        return String.format("%02d:00", hora)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
