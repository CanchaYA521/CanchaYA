package com.rojassac.canchaya.ui.user.mapa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rojassac.canchaya.R
import com.rojassac.canchaya.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configurar mapa
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
        }

        // Ubicación inicial (Lima, Perú)
        val lima = LatLng(-12.0464, -77.0428)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 12f))

        // TODO: Cargar marcadores de canchas desde ViewModel
        loadCanchaMarkers()
    }

    private fun loadCanchaMarkers() {
        // Ejemplo de marcador
        val canchaEjemplo = LatLng(-12.0897, -77.0282)
        googleMap.addMarker(
            MarkerOptions()
                .position(canchaEjemplo)
                .title("Cancha El Gol")
                .snippet("S/ 60.00 por hora")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
