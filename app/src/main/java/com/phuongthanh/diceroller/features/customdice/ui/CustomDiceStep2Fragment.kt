package com.phuongthanh.diceroller.features.customdice.ui

import android.net.Uri
import android.text.InputFilter
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
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
    private var backPressedCallback: OnBackPressedCallback? = null

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

        val callback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (isFormatSheetVisible()) {
                    hideFormatBottomSheet()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        backPressedCallback = callback

        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        binding.viewGradientOverlay.setOnClickListener {
            hideFormatBottomSheet()
        }

        binding.layoutFormatBottomSheet.optImage.setOnDebouncedClickListener {
            hideFormatBottomSheet {
                pickFaceImageLauncher.launch("image/*")
            }
        }

        binding.layoutFormatBottomSheet.optText.setOnDebouncedClickListener {
            hideFormatBottomSheet {
                if (currentFaceIndex < faces.size) {
                    openTextDialog(faces[currentFaceIndex])
                }
            }
        }

        binding.layoutFormatBottomSheet.optEmoji.setOnDebouncedClickListener {
            hideFormatBottomSheet {
                if (currentFaceIndex < faces.size) {
                    openEmojiPicker(faces[currentFaceIndex])
                }
            }
        }

        binding.layoutFormatBottomSheet.optImageText.setOnDebouncedClickListener {
            hideFormatBottomSheet {
                if (currentFaceIndex < faces.size) {
                    openImageTextPicker(faces[currentFaceIndex])
                }
            }
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
            showFormatBottomSheet()
        }

        binding.btnBottomClose.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        binding.btnDone.setOnDebouncedClickListener {
            saveCustomGame()
        }
    }

    private fun isFormatSheetVisible(): Boolean {
        return binding.layoutFormatBottomSheet.root.visibility == View.VISIBLE
    }

    private fun showFormatBottomSheet() {
        backPressedCallback?.isEnabled = true

        binding.viewGradientOverlay.apply {
            animate().cancel()
            alpha = 0f
            scaleX = 0.96f
            scaleY = 0.96f
            visibility = View.VISIBLE
            doOnPreDraw { overlay ->
                overlay.pivotX = overlay.width / 2f
                overlay.pivotY = overlay.height / 2f
                overlay.animate()
                    .alpha(0.85f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }

        binding.layoutFormatBottomSheet.root.apply {
            animate().cancel()
            visibility = View.VISIBLE
            alpha = 1f
            scaleX = 1f
            scaleY = 1f

            doOnPreDraw { sheet ->
                val startY = if (sheet.height > 0) sheet.height.toFloat() else 1200f
                sheet.translationY = startY
                sheet.animate()
                    .translationY(0f)
                    .setDuration(320)
                    .setInterpolator(DecelerateInterpolator(2.0f))
                    .start()
            }
        }
    }

    private fun hideFormatBottomSheet(onEnd: (() -> Unit)? = null) {
        backPressedCallback?.isEnabled = false

        binding.viewGradientOverlay.apply {
            animate().cancel()
            animate()
                .alpha(0f)
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(220)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    visibility = View.GONE
                }
                .start()
        }

        binding.layoutFormatBottomSheet.root.apply {
            animate().cancel()
            val targetY = if (height > 0) height.toFloat() else 1200f
            animate()
                .translationY(targetY)
                .setDuration(240)
                .setInterpolator(AccelerateInterpolator(1.8f))
                .withEndAction {
                    visibility = View.GONE
                    onEnd?.invoke()
                }
                .start()
        }
    }

    private fun openFormatSelection() {
        showFormatBottomSheet()
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
        binding.tvCurrentFaceIndicator.text = "${currentFaceIndex + 1}/$facesCount"

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
