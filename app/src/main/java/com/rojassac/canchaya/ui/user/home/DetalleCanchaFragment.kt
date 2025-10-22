package com.rojassac.canchaya.ui.user.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.databinding.FragmentDetalleCanchaBinding
import com.rojassac.canchaya.ui.user.resenas.ResenasCanchaFragment
import com.rojassac.canchaya.ui.user.reservas.ReservaFragment
import com.rojassac.canchaya.utils.toast

class DetalleCanchaFragment : Fragment() {

    private var _binding: FragmentDetalleCanchaBinding? = null
    private val binding get() = _binding!!

    private var cancha: Cancha? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cancha = it.getParcelable("cancha")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleCanchaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancha?.let { setupUI(it) } ?: run {
            requireContext().toast("Error al cargar cancha")
            requireActivity().onBackPressed()
        }
    }

    private fun setupUI(cancha: Cancha) {
        binding.apply {
            // Toolbar
            toolbar.title = cancha.nombre
            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            // Datos de la cancha
            tvNombre.text = cancha.nombre
            tvDireccion.text = "${cancha.direccion}, ${cancha.distrito}"
            tvPrecio.text = "S/ ${cancha.precioHora}/hora"
            tvHorario.text = "Horario: ${cancha.horarioApertura} - ${cancha.horarioCierre}"
            tvCalificacion.text = "${cancha.calificacionPromedio} ⭐ (${cancha.totalResenas} reseñas)"

            // Imagen de portada
            if (cancha.imagenes.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(cancha.imagenes[0])
                    .placeholder(R.mipmap.ic_launcher)
                    .into(ivPortada)
            } else {
                ivPortada.setImageResource(R.mipmap.ic_launcher)
            }

            // Servicios
            if (cancha.servicios.isNotEmpty()) {
                tvServicios.text = cancha.servicios.joinToString(", ")
            } else {
                tvServicios.text = "Sin servicios adicionales"
            }

            // Botón Reservar
            btnReservar.setOnClickListener {
                navigateToReserva(cancha)
            }

            // Botón Ver Mapa
            btnVerMapa.setOnClickListener {
                requireContext().toast("Ubicación: ${cancha.latitud}, ${cancha.longitud}")
            }

            // ✅ NUEVO: Botón Ver Reseñas
            btnVerResenas.setOnClickListener {
                navigateToResenas(cancha)
            }
        }
    }

    private fun navigateToReserva(cancha: Cancha) {
        val reservaFragment = ReservaFragment()
        val bundle = Bundle().apply {
            putParcelable("cancha", cancha)
        }
        reservaFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, reservaFragment)
            .addToBackStack(null)
            .commit()
    }

    // ✅ NUEVO: Navegar a Reseñas
    private fun navigateToResenas(cancha: Cancha) {
        val resenasFragment = ResenasCanchaFragment()
        val bundle = Bundle().apply {
            putParcelable("cancha", cancha)
        }
        resenasFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, resenasFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
