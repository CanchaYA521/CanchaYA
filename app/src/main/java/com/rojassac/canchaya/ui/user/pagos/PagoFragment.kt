package com.rojassac.canchaya.ui.user.pagos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.EstadoReserva
import com.rojassac.canchaya.data.model.Reserva
import com.rojassac.canchaya.databinding.FragmentPagoBinding
import com.rojassac.canchaya.utils.Constants
import com.rojassac.canchaya.utils.toast
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import com.rojassac.canchaya.utils.NotificationHelper

class PagoFragment : Fragment() {

    private var _binding: FragmentPagoBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var reserva: Reserva? = null
    private var comprobanteUri: Uri? = null
    private var metodoPagoSeleccionado = "Yape"

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            comprobanteUri = result.data?.data
            comprobanteUri?.let { uri ->
                binding.ivComprobante.setImageURI(uri)
                binding.ivComprobante.visibility = View.VISIBLE
                binding.btnConfirmarPago.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reserva = it.getParcelable("reserva")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reserva?.let { setupUI(it) } ?: run {
            requireContext().toast("Error al cargar información de pago")
            requireActivity().onBackPressed()
        }
    }

    private fun setupUI(reserva: Reserva) {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            // Información de la reserva
            tvCanchaNombre.text = reserva.canchaNombre
            tvFechaHora.text = "${formatearFecha(reserva.fecha)} • ${reserva.horaInicio}-${reserva.horaFin}"
            tvTotal.text = "S/ ${reserva.precio}"

            // Generar QR de Yape
            generarQRYape()

            // Método de pago
            rgMetodoPago.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rbYape -> {
                        metodoPagoSeleccionado = "Yape"
                        cardQR.visibility = View.VISIBLE
                        tvNumeroCelular.text = "987 654 321"
                    }
                    R.id.rbPlin -> {
                        metodoPagoSeleccionado = "Plin"
                        cardQR.visibility = View.VISIBLE
                        tvNumeroCelular.text = "987 654 321"
                    }
                    R.id.rbTransferencia -> {
                        metodoPagoSeleccionado = "Transferencia"
                        cardQR.visibility = View.GONE
                        requireContext().toast("CCI: 0011-0222-0333-0444-55")
                    }
                }
            }

            // Subir comprobante
            btnSubirComprobante.setOnClickListener {
                abrirGaleria()
            }

            // Confirmar pago
            btnConfirmarPago.setOnClickListener {
                confirmarPago()
            }
        }
    }

    private fun generarQRYape() {
        try {
            val contenidoQR = "yape://transfer?amount=${reserva?.precio}&number=987654321&name=CanchaYA"

            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                contenidoQR,
                BarcodeFormat.QR_CODE,
                500,
                500
            )

            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)

            binding.ivQR.setImageBitmap(bitmap)
        } catch (e: Exception) {
            requireContext().toast("Error al generar QR: ${e.message}")
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun confirmarPago() {
        val reservaData = reserva ?: return

        if (comprobanteUri == null) {
            requireContext().toast("Por favor, sube el comprobante de pago")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmarPago.isEnabled = false

        // Subir comprobante a Firebase Storage
        subirComprobante(reservaData)
    }

    private fun subirComprobante(reserva: Reserva) {
        val uri = comprobanteUri ?: return

        val fileName = "comprobantes/${reserva.id}_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    actualizarReserva(reserva, downloadUri.toString())
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnConfirmarPago.isEnabled = true
                requireContext().toast("Error al subir comprobante: ${e.message}")
            }
    }

    private fun actualizarReserva(reserva: Reserva, comprobanteUrl: String) {
        val updates = hashMapOf<String, Any>(
            "estado" to EstadoReserva.CONFIRMADA,
            "comprobantePago" to comprobanteUrl,
            "metodoPago" to metodoPagoSeleccionado
        )

        firestore.collection(Constants.RESERVAS_COLLECTION)
            .document(reserva.id)
            .update(updates)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE

                // Enviar notificación
                NotificationHelper.showReservaConfirmada(
                    requireContext(),
                    reserva.canchaNombre,
                    reserva.fecha,
                    "${reserva.horaInicio}-${reserva.horaFin}"
                )

                requireContext().toast("¡Pago confirmado exitosamente!")

                // Volver al detalle de reserva actualizado
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnConfirmarPago.isEnabled = true
                requireContext().toast("Error al confirmar pago: ${e.message}")
            }
    }


    private fun formatearFecha(fecha: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "PE"))
            val date = inputFormat.parse(fecha)
            date?.let { outputFormat.format(it) } ?: fecha
        } catch (e: Exception) {
            fecha
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
