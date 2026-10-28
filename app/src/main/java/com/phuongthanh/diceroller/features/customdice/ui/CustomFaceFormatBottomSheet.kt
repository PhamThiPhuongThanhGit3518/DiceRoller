package com.phuongthanh.diceroller.features.customdice.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetCustomFaceFormatBinding

class CustomFaceFormatBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCustomFaceFormatBinding? = null
    private val binding get() = _binding!!

    var onFormatSelected: ((format: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCustomFaceFormatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.optImage.setOnDebouncedClickListener {
            val callback = onFormatSelected
            dismiss()
            callback?.invoke("image")
        }

        binding.optText.setOnDebouncedClickListener {
            val callback = onFormatSelected
            dismiss()
            callback?.invoke("text")
        }

        binding.optImageText.setOnDebouncedClickListener {
            val callback = onFormatSelected
            dismiss()
            callback?.invoke("image_text")
        }

        binding.optEmoji.setOnDebouncedClickListener {
            val callback = onFormatSelected
            dismiss()
            callback?.invoke("emoji")
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
            sheet.setBackgroundResource(android.R.color.transparent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CustomFaceFormatBottomSheet"
        fun newInstance(): CustomFaceFormatBottomSheet = CustomFaceFormatBottomSheet()
    }
}
