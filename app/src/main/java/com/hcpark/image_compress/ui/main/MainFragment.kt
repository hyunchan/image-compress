package com.hcpark.image_compress.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.hcpark.image_compress.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding
    private val pickLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let { viewModel.put(requireContext(), it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.set()
        return binding.root
    }

    private fun FragmentMainBinding.set() {
        lifecycleOwner = viewLifecycleOwner
        vm = viewModel

        selectImage.setOnClickListener {
            pickLauncher.launch("image/*")
        }
        compress.setOnClickListener {
            val uri = viewModel.choose.value ?: return@setOnClickListener
            val quality = viewModel.quality.value ?: return@setOnClickListener
            viewModel.compress(requireContext(), uri, quality)
            val inputManager = requireContext().getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputManager.hideSoftInputFromWindow(binding.quality.windowToken, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.choose.observe(viewLifecycleOwner) {
            Glide.with(this).load(it).into(binding.preview)
        }
    }

}