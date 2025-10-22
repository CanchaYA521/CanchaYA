package com.rojassac.canchaya.ui.user.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.rojassac.canchaya.R
import com.rojassac.canchaya.data.model.Cancha
import com.rojassac.canchaya.databinding.FragmentHomeBinding
import com.rojassac.canchaya.ui.user.UserViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var canchaAdapter: CanchaAdapter
    private lateinit var viewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        setupRecyclerView()
        setupSearch()
        observeCanchas()

        // Cargar canchas
        viewModel.loadCanchas()
    }

    private fun setupRecyclerView() {
        canchaAdapter = CanchaAdapter { cancha ->
            // Navegar al detalle de la cancha
            navigateToDetalle(cancha)
        }

        binding.rvCanchas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = canchaAdapter
            setHasFixedSize(true)
        }
    }

    private fun navigateToDetalle(cancha: Cancha) {
        val fragment = DetalleCanchaFragment().apply {
            arguments = Bundle().apply {
                putParcelable("cancha", cancha)
            }
        }

        // Navegar al detalle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchCanchas(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchCanchas(it) }
                return true
            }
        })
    }

    private fun observeCanchas() {
        viewModel.canchas.observe(viewLifecycleOwner) { canchas ->
            canchaAdapter.submitList(canchas)
            binding.progressBar.visibility = View.GONE

            if (canchas.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvCanchas.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvCanchas.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
