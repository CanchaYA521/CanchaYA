package com.rojassac.canchaya.ui.admin.ingresos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rojassac.canchaya.databinding.FragmentAdminPlaceholderBinding

class AdminIngresosFragment : Fragment() {

    private var _binding: FragmentAdminPlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvPlaceholder.text = "💰 Gestión de Ingresos\n\nReportes financieros y pagos\n\n(Próximamente...)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
