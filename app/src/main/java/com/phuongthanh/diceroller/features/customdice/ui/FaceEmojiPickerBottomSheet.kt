package com.phuongthanh.diceroller.features.customdice.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetFaceEmojiPickerBinding

class FaceEmojiPickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFaceEmojiPickerBinding? = null
    private val binding get() = _binding!!

    private var initialEmoji: String = ""
    private var currentEmoji: String = ""

    var onEmojiSelected: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialEmoji = arguments?.getString(ARG_INITIAL_EMOJI).orEmpty()
        currentEmoji = initialEmoji
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFaceEmojiPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (currentEmoji.isNotEmpty()) {
            binding.tvSelectedEmojiPreview.text = currentEmoji
        } else {
            binding.tvSelectedEmojiPreview.text = "🎲"
            currentEmoji = "🎲"
        }

        binding.emojiPickerView.setOnEmojiPickedListener { emojiViewItem ->
            val emoji = emojiViewItem.emoji
            currentEmoji = emoji
            binding.tvSelectedEmojiPreview.text = emoji
        }

        binding.btnCancel.setOnDebouncedClickListener {
            dismiss()
        }

        binding.btnApply.setOnDebouncedClickListener {
            if (currentEmoji.isNotBlank()) {
                onEmojiSelected?.invoke(currentEmoji)
                dismiss()
            } else {
                Toast.makeText(requireContext(), R.string.picker_please_select_emoji, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FaceEmojiPickerBottomSheet"
        private const val ARG_INITIAL_EMOJI = "arg_initial_emoji"

        fun newInstance(initialEmoji: String = ""): FaceEmojiPickerBottomSheet {
            return FaceEmojiPickerBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_INITIAL_EMOJI, initialEmoji)
                }
            }
        }
    }
}
