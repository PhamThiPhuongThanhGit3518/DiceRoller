package com.phuongthanh.diceroller.features.customdice.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetFaceImageTextBinding
import com.phuongthanh.diceroller.features.customdice.utils.CustomDiceImageHelper

class FaceImageTextBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFaceImageTextBinding? = null
    private val binding get() = _binding!!

    private var currentImageSource: String = ""
    private var initialText: String = ""

    var onConfigured: ((text: String, imageSource: String) -> Unit)? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val localPath = CustomDiceImageHelper.saveUriToInternalStorage(requireContext(), uri)
            if (!localPath.isNullOrBlank()) {
                currentImageSource = localPath
                updateImagePreview()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialText = arguments?.getString(ARG_TEXT).orEmpty()
        currentImageSource = arguments?.getString(ARG_IMAGE).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFaceImageTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etFaceCaption.setText(initialText)
        if (initialText.isNotEmpty()) {
            binding.etFaceCaption.setSelection(initialText.length)
        }

        updateImagePreview()

        binding.layoutImagePreview.setOnDebouncedClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnPickFromGallery.setOnDebouncedClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCancel.setOnDebouncedClickListener {
            dismiss()
        }

        binding.btnApply.setOnDebouncedClickListener {
            val text = binding.etFaceCaption.text?.toString()?.trim().orEmpty()
            if (text.isEmpty() && currentImageSource.isEmpty()) {
                Toast.makeText(requireContext(), R.string.picker_please_enter_content, Toast.LENGTH_SHORT).show()
                return@setOnDebouncedClickListener
            }

            onConfigured?.invoke(text, currentImageSource)
            dismiss()
        }
    }

    private fun updateImagePreview() {
        if (currentImageSource.isNotEmpty()) {
            CustomDiceImageHelper.loadFaceImage(binding.ivSelectedImage, currentImageSource)
        } else {
            binding.ivSelectedImage.setImageResource(R.drawable.ic_theme)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FaceImageTextBottomSheet"
        private const val ARG_TEXT = "arg_text"
        private const val ARG_IMAGE = "arg_image"

        fun newInstance(initialText: String, initialImage: String): FaceImageTextBottomSheet {
            return FaceImageTextBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TEXT, initialText)
                    putString(ARG_IMAGE, initialImage)
                }
            }
        }
    }
}
