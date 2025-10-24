package com.rojassac.canchaya.ui.superadmin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.ParametrosGlobales
import com.rojassac.canchaya.databinding.FragmentParametrosGlobalesBinding
import com.rojassac.canchaya.ui.superadmin.SuperAdminViewModel
import com.rojassac.canchaya.utils.Resource

/**
 * ✅ NUEVO (23 Oct 2025)
 * Fragment para gestionar parámetros globales de la aplicación
 */
class ParametrosGlobalesFragment : Fragment() {

    private var _binding: FragmentParametrosGlobalesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SuperAdminViewModel by activityViewModels()

    private var parametrosActuales: ParametrosGlobales? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParametrosGlobalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupObservers()
        setupListeners()

        // Cargar parámetros
        viewModel.loadParametrosGlobales()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupObservers() {
        // Observar parámetros globales
        viewModel.parametrosGlobales.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    // ✅ CORRECCIÓN: Manejar nullable con let
                    resource.data?.let { parametros ->
                        parametrosActuales = parametros
                        cargarDatos(parametros)
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: "Error al cargar parámetros")
                }
            }
        }

        // Observar resultado de actualización
        viewModel.updateParametrosResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnGuardar.isEnabled = false
                }
                is Resource.Success -> {
                    binding.btnGuardar.isEnabled = true
                    showSuccess("Parámetros actualizados correctamente")
                }
                is Resource.Error -> {
                    binding.btnGuardar.isEnabled = true
                    showError(resource.message ?: "Error al guardar")
                }
            }
        }
    }

    private fun setupListeners() {
        // Botón guardar
        binding.btnGuardar.setOnClickListener {
            guardarParametros()
        }

        // Botón resetear
        binding.btnResetear.setOnClickListener {
            confirmarReseteo()
        }

        // Switch modo mantenimiento
        binding.switchMantenimiento.setOnCheckedChangeListener { _, isChecked ->
            // Actualizar inmediatamente
            viewModel.toggleModoMantenimiento(isChecked)
        }
    }

    private fun cargarDatos(parametros: ParametrosGlobales) {
        binding.apply {
            // ⏰ RESERVAS
            etAnticipacionMinima.setText(parametros.anticipacionMinima.toString())
            etAnticipacionMaxima.setText(parametros.anticipacionMaxima.toString())
            etDuracionMinima.setText(parametros.duracionMinima.toString())
            etDuracionMaxima.setText(parametros.duracionMaxima.toString())
            etTiempoGraciaCancelacion.setText(parametros.tiempoGraciaCancelacion.toString())

            // 💰 PAGOS
            etPorcentajeAnticipo.setText(parametros.porcentajeAnticipo.toString())
            etComisionPlataforma.setText(parametros.comisionPlataforma.toString())
            etMontoMinimo.setText(parametros.montoMinimo.toString())

            // 🚫 CANCELACIONES
            when (parametros.politicaReembolso) {
                "TOTAL" -> radioReembolsoTotal.isChecked = true
                "PARCIAL" -> radioReembolsoParcial.isChecked = true
                "NINGUNO" -> radioReembolsoNinguno.isChecked = true
            }
            etPorcentajeReembolso.setText(parametros.porcentajeReembolso.toString())
            switchPenalizacionNoShow.isChecked = parametros.penalizacionNoShow
            etMaxCancelacionesMes.setText(parametros.maxCancelacionesMes.toString())

            // 🏆 PUNTOS
            switchPuntosHabilitados.isChecked = parametros.puntosHabilitados
            etPuntosPorCienSoles.setText(parametros.puntosPorCienSoles.toString())
            etPuntosPorReferido.setText(parametros.puntosPorReferido.toString())
            etConversionPuntos.setText(parametros.conversionPuntos.toString())
            etDescuentoPorPuntos.setText(parametros.descuentoPorPuntos.toString())

            // 📱 NOTIFICACIONES
            etRecordatorioReserva.setText(parametros.recordatorioReserva.toString())
            switchConfirmacionAutomatica.isChecked = parametros.confirmacionAutomatica
            switchNotificacionesPush.isChecked = parametros.notificacionesPushHabilitadas

            // 🔒 LÍMITES
            etMaxReservasActivas.setText(parametros.maxReservasActivas.toString())
            etDiasBloqueo.setText(parametros.diasBloqueo.toString())
            etMaxCambiosHorario.setText(parametros.maxCambiosHorario.toString())

            // 🎨 PERSONALIZACIÓN
            switchMantenimiento.isChecked = parametros.modoMantenimiento
            etMensajeMantenimiento.setText(parametros.mensajeMantenimiento)
            etVersionMinima.setText(parametros.versionMinimaRequerida)
            etMensajeBienvenida.setText(parametros.mensajeBienvenida)

            // Habilitar/deshabilitar campos de puntos
            layoutPuntos.visibility = if (parametros.puntosHabilitados) View.VISIBLE else View.GONE
        }

        // Listener para mostrar/ocultar campos de puntos
        binding.switchPuntosHabilitados.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutPuntos.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun guardarParametros() {
        try {
            val parametros = ParametrosGlobales(
                // ⏰ RESERVAS
                anticipacionMinima = binding.etAnticipacionMinima.text.toString().toIntOrNull() ?: 2,
                anticipacionMaxima = binding.etAnticipacionMaxima.text.toString().toIntOrNull() ?: 30,
                duracionMinima = binding.etDuracionMinima.text.toString().toIntOrNull() ?: 1,
                duracionMaxima = binding.etDuracionMaxima.text.toString().toIntOrNull() ?: 4,
                tiempoGraciaCancelacion = binding.etTiempoGraciaCancelacion.text.toString().toIntOrNull() ?: 24,

                // 💰 PAGOS
                porcentajeAnticipo = binding.etPorcentajeAnticipo.text.toString().toIntOrNull() ?: 50,
                comisionPlataforma = binding.etComisionPlataforma.text.toString().toDoubleOrNull() ?: 10.0,
                montoMinimo = binding.etMontoMinimo.text.toString().toDoubleOrNull() ?: 20.0,
                metodosPagoHabilitados = listOf("YAPE", "PLIN", "EFECTIVO", "TARJETA"),

                // 🚫 CANCELACIONES
                politicaReembolso = when {
                    binding.radioReembolsoTotal.isChecked -> "TOTAL"
                    binding.radioReembolsoParcial.isChecked -> "PARCIAL"
                    else -> "NINGUNO"
                },
                porcentajeReembolso = binding.etPorcentajeReembolso.text.toString().toIntOrNull() ?: 80,
                penalizacionNoShow = binding.switchPenalizacionNoShow.isChecked,
                maxCancelacionesMes = binding.etMaxCancelacionesMes.text.toString().toIntOrNull() ?: 3,

                // 🏆 PUNTOS
                puntosHabilitados = binding.switchPuntosHabilitados.isChecked,
                puntosPorCienSoles = binding.etPuntosPorCienSoles.text.toString().toIntOrNull() ?: 10,
                puntosPorReferido = binding.etPuntosPorReferido.text.toString().toIntOrNull() ?: 50,
                conversionPuntos = binding.etConversionPuntos.text.toString().toIntOrNull() ?: 100,
                descuentoPorPuntos = binding.etDescuentoPorPuntos.text.toString().toDoubleOrNull() ?: 5.0,

                // 📱 NOTIFICACIONES
                recordatorioReserva = binding.etRecordatorioReserva.text.toString().toIntOrNull() ?: 2,
                confirmacionAutomatica = binding.switchConfirmacionAutomatica.isChecked,
                notificacionesPushHabilitadas = binding.switchNotificacionesPush.isChecked,

                // 🔒 LÍMITES
                maxReservasActivas = binding.etMaxReservasActivas.text.toString().toIntOrNull() ?: 3,
                diasBloqueo = binding.etDiasBloqueo.text.toString().toIntOrNull() ?: 7,
                maxCambiosHorario = binding.etMaxCambiosHorario.text.toString().toIntOrNull() ?: 1,

                // 🎨 PERSONALIZACIÓN
                modoMantenimiento = binding.switchMantenimiento.isChecked,
                mensajeMantenimiento = binding.etMensajeMantenimiento.text.toString(),
                versionMinimaRequerida = binding.etVersionMinima.text.toString(),
                mensajeBienvenida = binding.etMensajeBienvenida.text.toString()
            )

            // Validar parámetros
            val error = viewModel.validarParametros(parametros)
            if (error != null) {
                showError(error)
                return
            }

            // Guardar
            viewModel.actualizarParametrosGlobales(parametros)

        } catch (e: Exception) {
            showError("Error al procesar datos: ${e.message}")
        }
    }

    private fun confirmarReseteo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Resetear parámetros")
            .setMessage("¿Estás seguro de que deseas resetear todos los parámetros a sus valores por defecto?")
            .setPositiveButton("Sí, resetear") { _, _ ->
                viewModel.resetearParametros()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollView.visibility = if (show) View.GONE else View.VISIBLE
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
