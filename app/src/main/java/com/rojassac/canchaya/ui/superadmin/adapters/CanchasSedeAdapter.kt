package com.rojassac.canchaya.ui.superadmin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.databinding.ItemCanchaSedeBinding

/**
 * ✨ NUEVO: Adaptador para mostrar canchas de una sede (23 Oct 2025)
 */
class CanchasSedeAdapter(
    private var canchas: List<Cancha>,
    private val onEditar: (Cancha) -> Unit,
    private val onEliminar: (Cancha) -> Unit
) : RecyclerView.Adapter<CanchasSedeAdapter.CanchaViewHolder>() {

    inner class CanchaViewHolder(private val binding: ItemCanchaSedeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cancha: Cancha) {
            binding.apply {
                // Datos básicos
                tvNombreCancha.text = cancha.nombre
                tvDireccion.text = "${cancha.direccion}, ${cancha.distrito}"
                tvHorario.text = "${cancha.horarioApertura} - ${cancha.horarioCierre}"

                // ✨ NUEVO: Mostrar precio según si tiene franjas o no (23 Oct 2025)
                if (cancha.preciosPorFranja.isNotEmpty()) {
                    tvPrecio.text = cancha.getResumenPrecios()
                    rvFranjasHorarias.visibility = View.VISIBLE

                    // Mostrar franjas en un RecyclerView simple
                    val franjasText = cancha.preciosPorFranja.joinToString("\n") { franja ->
                        "⏰ ${franja.horaInicio} - ${franja.horaFin}: S/ ${franja.precio}/hora"
                    }
                    // Por simplicidad, usar TextView en lugar de RecyclerView anidado
                    // (puedes mejorar esto después con un adapter anidado)
                    rvFranjasHorarias.visibility = View.GONE
                } else {
                    tvPrecio.text = "S/ ${cancha.precioHora}/hora"
                    rvFranjasHorarias.visibility = View.GONE
                }

                // Botones
                btnEditar.setOnClickListener {
                    onEditar(cancha)
                }

                btnEliminar.setOnClickListener {
                    onEliminar(cancha)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanchaViewHolder {
        val binding = ItemCanchaSedeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CanchaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CanchaViewHolder, position: Int) {
        holder.bind(canchas[position])
    }

    override fun getItemCount(): Int = canchas.size

    fun updateList(newCanchas: List<Cancha>) {
        canchas = newCanchas
        notifyDataSetChanged()
    }
}
