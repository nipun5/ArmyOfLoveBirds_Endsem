package com.vipul.doco.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.vipul.doco.R
import com.vipul.doco.databinding.FragmentFilterBottomSheetBinding


class FilterBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFilterBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_filter_bottom_sheet,
            container,
            false
        )

        binding.filterApplyBtn.setOnClickListener {

            // tags
            val tagList = binding.filterTags.children.toList().filter { (it as Chip).isChecked }
                .joinToString(", ") { (it as Chip).text }

            // breeds
            val breed = binding.filterBreeds.children.toList().filter { (it as Chip).isChecked }
                .joinToString(", ") { (it as Chip).text }

            // gender
            val gender = binding.filterGender.children.toList().filter { (it as Chip).isChecked }
                .joinToString(", ") { (it as Chip).text }

            val action =
                FilterBottomSheetFragmentDirections.actionFilterBottomSheetFragmentToHomeFragment(
                    tagList = tagList,
                    breed = breed,
                    gender = gender
                )
            findNavController().navigate(action)
        }

        binding.filterClearBtn.setOnClickListener {
            binding.filterTags.clearCheck()
            binding.filterBreeds.clearCheck()
            binding.filterGender.clearCheck()
        }
        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetTheme
}