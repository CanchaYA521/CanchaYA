package com.rojassac.canchaya.ui.admin.resenas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class AdminResenasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val textView = TextView(requireContext())
        textView.text = "📝 Módulo de Reseñas\n\nPróximamente..."
        textView.textSize = 20f
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.setPadding(32, 32, 32, 32)
        return textView
    }
}
