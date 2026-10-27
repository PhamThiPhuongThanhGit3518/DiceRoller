package com.phuongthanh.diceroller.features.roulette.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetRouletteActionsBinding
import com.phuongthanh.diceroller.features.roulette.model.WheelPreset

class RouletteActionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRouletteActionsBinding? = null
    private val binding get() = _binding!!

    var preset: WheelPreset? = null
    var onEditClicked: ((WheelPreset) -> Unit)? = null
    var onDuplicateClicked: ((WheelPreset) -> Unit)? = null
    var onDeleteClicked: ((WheelPreset) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRouletteActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentPreset = preset ?: return

        binding.btnEdit.setOnDebouncedClickListener {
            dismiss()
            onEditClicked?.invoke(currentPreset)
        }

        binding.btnDuplicate.setOnDebouncedClickListener {
            dismiss()
            onDuplicateClicked?.invoke(currentPreset)
        }

        binding.btnDelete.setOnDebouncedClickListener {
            dismiss()
            onDeleteClicked?.invoke(currentPreset)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RouletteActionsBottomSheet"

        fun newInstance(preset: WheelPreset): RouletteActionsBottomSheet {
            return RouletteActionsBottomSheet().apply {
                this.preset = preset
            }
        }
    }
}
