package com.phuongthanh.diceroller.features.freeroll.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetCustomRangeBinding

class CustomRangeBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCustomRangeBinding? = null
    private val binding get() = _binding!!

    var onRangeApplied: ((min: Int, max: Int) -> Unit)? = null
    var initialMin: Int = 1
    var initialMax: Int = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCustomRangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etMin.setText(initialMin.toString())
        binding.etMax.setText(initialMax.toString())

        setupPresetChips()

        binding.btnCancel.setOnDebouncedClickListener {
            dismiss()
        }

        binding.btnApply.setOnDebouncedClickListener {
            applyRange()
        }
    }

    private fun setupPresetChips() {
        binding.chip1to10.setOnClickListener { setRangeValues(1, 10) }
        binding.chip1to20.setOnClickListener { setRangeValues(1, 20) }
        binding.chip1to50.setOnClickListener { setRangeValues(1, 50) }
        binding.chip1to100.setOnClickListener { setRangeValues(1, 100) }
        binding.chip1to500.setOnClickListener { setRangeValues(1, 500) }
        binding.chip1to1000.setOnClickListener { setRangeValues(1, 1000) }
    }

    private fun setRangeValues(min: Int, max: Int) {
        binding.etMin.setText(min.toString())
        binding.etMax.setText(max.toString())
        binding.tvError.visibility = View.GONE
    }

    private fun applyRange() {
        val minStr = binding.etMin.text?.toString()?.trim().orEmpty()
        val maxStr = binding.etMax.text?.toString()?.trim().orEmpty()

        val minVal = minStr.toIntOrNull()
        val maxVal = maxStr.toIntOrNull()

        if (minVal == null || maxVal == null || minVal >= maxVal) {
            binding.tvError.text = getString(R.string.custom_range_invalid)
            binding.tvError.visibility = View.VISIBLE
            return
        }

        binding.tvError.visibility = View.GONE
        onRangeApplied?.invoke(minVal, maxVal)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CustomRangeBottomSheet"

        fun newInstance(min: Int, max: Int): CustomRangeBottomSheet {
            return CustomRangeBottomSheet().apply {
                initialMin = min
                initialMax = max
            }
        }
    }
}
