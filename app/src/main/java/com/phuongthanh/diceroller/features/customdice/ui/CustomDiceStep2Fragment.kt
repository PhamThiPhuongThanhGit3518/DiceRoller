package com.phuongthanh.diceroller.features.customdice.ui

import android.net.Uri
import android.text.InputFilter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentCustomDiceStep2Binding
import com.phuongthanh.diceroller.features.customdice.model.CustomDice
import com.phuongthanh.diceroller.features.customdice.model.CustomDiceFace
import com.phuongthanh.diceroller.features.customdice.utils.CustomDiceImageHelper
import kotlinx.coroutines.launch
import java.util.UUID

class CustomDiceStep2Fragment : BaseFragment<FragmentCustomDiceStep2Binding>(FragmentCustomDiceStep2Binding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private var diceCount = 3
    private var facesCount = 6
    private var currentFaceIndex = 0
    private val faces = mutableListOf<CustomDiceFace>()

    private val pickFaceImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && currentFaceIndex < faces.size) {
            val localPath = CustomDiceImageHelper.saveUriToInternalStorage(requireContext(), uri)
            if (!localPath.isNullOrBlank()) {
                val currentFace = faces[currentFaceIndex]
                faces[currentFaceIndex] = currentFace.copy(
                    formatType = "image",
                    text = "",
                    iconOrEmoji = localPath
                )
                updateFaceControls()
            }
        }
    }

    override fun initViews() {
        diceCount = arguments?.getInt("diceCount", 3) ?: 3

        initializeFaces()
        updateFaceControls()

        binding.btnBack.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        binding.btnMinusFaces.setOnDebouncedClickListener {
            if (facesCount > 2) {
                facesCount--
                if (currentFaceIndex >= facesCount) {
                    currentFaceIndex = facesCount - 1
                }
                while (faces.size > facesCount) {
                    faces.removeLastOrNull()
                }
                updateFaceControls()
            }
        }

        binding.btnPlusFaces.setOnDebouncedClickListener {
            if (facesCount < 12) {
                facesCount++
                faces.add(
                    CustomDiceFace(
                        faceIndex = facesCount,
                        text = getString(R.string.face_default_name, facesCount),
                        colorHex = "#344054",
                        formatType = "text"
                    )
                )
                updateFaceControls()
            }
        }

        binding.btnPrevFace.setOnDebouncedClickListener {
            if (currentFaceIndex > 0) {
                currentFaceIndex--
                updateFaceControls()
            }
        }

        binding.btnNextFace.setOnDebouncedClickListener {
            if (currentFaceIndex < facesCount - 1) {
                currentFaceIndex++
                updateFaceControls()
            }
        }

        binding.cardCustomizeCurrentFace.setOnDebouncedClickListener {
            openFormatSelection()
        }

        binding.btnBottomAux.setOnDebouncedClickListener {
            openFormatSelection()
        }

        binding.btnDone.setOnDebouncedClickListener {
            saveCustomGame()
        }
    }

    private fun openFormatSelection() {
        val sheet = CustomFaceFormatBottomSheet.newInstance().apply {
            onFormatSelected = { format ->
                if (currentFaceIndex < faces.size) {
                    val currentFace = faces[currentFaceIndex]
                    when (format) {
                        "text" -> openTextDialog(currentFace)
                        "emoji" -> openEmojiPicker(currentFace)
                        "image" -> pickFaceImageLauncher.launch("image/*")
                        "image_text" -> openImageTextPicker(currentFace)
                    }
                }
            }
        }
        sheet.show(childFragmentManager, CustomFaceFormatBottomSheet.TAG)
    }

    private fun initializeFaces() {
        faces.clear()
        for (i in 1..facesCount) {
            faces.add(
                CustomDiceFace(
                    faceIndex = i,
                    text = getString(R.string.face_default_name, i),
                    colorHex = "#344054",
                    formatType = "text"
                )
            )
        }
    }

    private fun openTextDialog(currentFace: CustomDiceFace) {
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.picker_text_hint)
            val defaultName = getString(R.string.face_default_name, currentFaceIndex + 1)
            if (currentFace.formatType == "text" && currentFace.text != defaultName) {
                setText(currentFace.text)
                setSelection(text.length)
            }
            filters = arrayOf(InputFilter.LengthFilter(30))
            setSingleLine(true)
        }
        val density = resources.displayMetrics.density
        val pad = (20 * density).toInt()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.picker_text_title)
            .setView(FrameLayout(requireContext()).apply {
                setPadding(pad, pad / 2, pad, pad / 2)
                addView(input)
            })
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotBlank()) {
                    faces[currentFaceIndex] = currentFace.copy(
                        formatType = "text",
                        text = text,
                        iconOrEmoji = ""
                    )
                    updateFaceControls()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun openEmojiPicker(currentFace: CustomDiceFace) {
        val initialEmoji = if (currentFace.formatType == "emoji") currentFace.iconOrEmoji else ""
        val sheet = FaceEmojiPickerBottomSheet.newInstance(initialEmoji).apply {
            onEmojiSelected = { emoji ->
                faces[currentFaceIndex] = currentFace.copy(
                    formatType = "emoji",
                    text = emoji,
                    iconOrEmoji = emoji
                )
                updateFaceControls()
            }
        }
        sheet.show(childFragmentManager, FaceEmojiPickerBottomSheet.TAG)
    }

    private fun openImageTextPicker(currentFace: CustomDiceFace) {
        val defaultName = getString(R.string.face_default_name, currentFaceIndex + 1)
        val initialText = if (currentFace.formatType == "image_text") {
            currentFace.text
        } else if (currentFace.formatType == "text" && currentFace.text != defaultName) {
            currentFace.text
        } else {
            ""
        }
        val initialImage = if (currentFace.formatType == "image_text" || currentFace.formatType == "image") {
            currentFace.iconOrEmoji
        } else {
            ""
        }

        val sheet = FaceImageTextBottomSheet.newInstance(initialText, initialImage).apply {
            onConfigured = { text, imageSource ->
                faces[currentFaceIndex] = currentFace.copy(
                    formatType = "image_text",
                    text = text,
                    iconOrEmoji = imageSource
                )
                updateFaceControls()
            }
        }
        sheet.show(childFragmentManager, FaceImageTextBottomSheet.TAG)
    }

    private fun updateFaceControls() {
        binding.tvFacesCount.text = facesCount.toString()
        binding.tvCurrentFaceIndicator.text = (currentFaceIndex + 1).toString()

        if (currentFaceIndex < faces.size) {
            val face = faces[currentFaceIndex]
            val defaultName = getString(R.string.face_default_name, currentFaceIndex + 1)
            val isPlaceholder = face.text == defaultName && face.iconOrEmoji.isEmpty() && face.formatType == "text"

            if (isPlaceholder) {
                binding.layoutEmptyFace.visibility = android.view.View.VISIBLE
                binding.layoutCustomizedFace.visibility = android.view.View.GONE
            } else {
                binding.layoutEmptyFace.visibility = android.view.View.GONE
                binding.layoutCustomizedFace.visibility = android.view.View.VISIBLE
                CustomDiceImageHelper.renderFace(
                    face = face,
                    imageView = binding.ivFaceIconOrAdd,
                    textView = binding.tvFaceContentPreview,
                    isPlaceholder = false
                )
            }
        }
    }

    private fun saveCustomGame() {
        val newDice = CustomDice(
            id = UUID.randomUUID().toString(),
            title = "Bộ Xúc Xắc $diceCount Viên ($facesCount Mặt)",
            diceCount = diceCount,
            faceCount = facesCount,
            faces = faces.toList(),
            isPreset = false
        )

        viewLifecycleOwner.lifecycleScope.launch {
            appContainer.customDiceRepository.saveCustomDice(newDice)
            Toast.makeText(requireContext(), R.string.custom_game_created_success, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack(R.id.customRollHomeFragment, false)
        }
    }
}
