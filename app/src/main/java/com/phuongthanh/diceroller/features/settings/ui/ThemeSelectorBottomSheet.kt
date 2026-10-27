package com.phuongthanh.diceroller.features.settings.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetThemeSelectorBinding
import com.phuongthanh.diceroller.databinding.ItemThemeColorCircleBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ThemeSelectorBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetThemeSelectorBinding? = null
    private val binding get() = _binding!!

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private val isClassic: Boolean by lazy {
        arguments?.getBoolean(ARG_IS_CLASSIC, false) ?: false
    }

    private var selectedDiceColor: String = "#C4EA7E"
    private var selectedDotColor: String = "#F7D4D5"
    private var currentFaceStyle: DiceFaceStyle = DiceFaceStyle.DOTS
    private var isSameColorsOn: Boolean = true

    private val diceColorViews = mutableListOf<Pair<String, ItemThemeColorCircleBinding>>()
    private val dotColorViews = mutableListOf<Pair<String, ItemThemeColorCircleBinding>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetThemeSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Classic mode only shows "Same Colors on All Dice" card
        if (isClassic) {
            binding.cardLayoutNumberDisplay.visibility = View.GONE
            binding.cardLayoutDotColor.visibility = View.GONE
        } else {
            binding.cardLayoutNumberDisplay.visibility = View.VISIBLE
            binding.cardLayoutDotColor.visibility = View.VISIBLE
        }

        binding.btnDone.setOnDebouncedClickListener {
            dismiss()
        }

        loadInitialPreferences()
    }

    private fun loadInitialPreferences() {
        viewLifecycleOwner.lifecycleScope.launch {
            val prefs = appContainer.appPreferences
            isSameColorsOn = prefs.sameColorsAllDiceFlow.first()
            selectedDiceColor = prefs.diceColorFlow.first()
            selectedDotColor = prefs.dotColorFlow.first()
            currentFaceStyle = DiceFaceStyle.fromCode(prefs.diceFaceStyleFlow.first())

            setupSameColorsSwitch()
            setupDiceColorPalette()

            if (!isClassic) {
                setupNumberDisplayOptions()
                setupDotColorPalette()
            }
        }
    }

    private fun setupSameColorsSwitch() {
        binding.swSameColors.isChecked = isSameColorsOn
        binding.layoutDiceColors.alpha = if (isSameColorsOn) 1.0f else 0.5f

        binding.swSameColors.setOnCheckedChangeListener { _, isChecked ->
            isSameColorsOn = isChecked
            binding.layoutDiceColors.alpha = if (isChecked) 1.0f else 0.5f
            viewLifecycleOwner.lifecycleScope.launch {
                appContainer.appPreferences.setSameColorsAllDice(isChecked)
            }
        }
    }

    private fun setupDiceColorPalette() {
        binding.layoutDiceColors.removeAllViews()
        diceColorViews.clear()

        val inflater = LayoutInflater.from(requireContext())
        for (colorHex in DICE_COLORS) {
            val itemBinding = ItemThemeColorCircleBinding.inflate(inflater, binding.layoutDiceColors, false)
            val colorInt = Color.parseColor(colorHex)
            itemBinding.viewColorCircle.backgroundTintList = ColorStateList.valueOf(colorInt)

            val isSelected = colorHex.equals(selectedDiceColor, ignoreCase = true)
            itemBinding.viewSelectionRing.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

            itemBinding.root.setOnClickListener {
                selectedDiceColor = colorHex
                updateDiceColorRings()
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setDiceColor(colorHex)
                }
            }

            diceColorViews.add(colorHex to itemBinding)
            binding.layoutDiceColors.addView(itemBinding.root)
        }
    }

    private fun updateDiceColorRings() {
        for ((hex, itemBinding) in diceColorViews) {
            val isSelected = hex.equals(selectedDiceColor, ignoreCase = true)
            itemBinding.viewSelectionRing.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setupNumberDisplayOptions() {
        updateNumberDisplayUi(currentFaceStyle)

        binding.btnOptionDot.setOnClickListener {
            currentFaceStyle = DiceFaceStyle.DOTS
            updateNumberDisplayUi(currentFaceStyle)
            viewLifecycleOwner.lifecycleScope.launch {
                appContainer.appPreferences.setDiceFaceStyle(DiceFaceStyle.DOTS.code)
            }
        }

        binding.btnOptionNumber.setOnClickListener {
            currentFaceStyle = DiceFaceStyle.NUMBERS
            updateNumberDisplayUi(currentFaceStyle)
            viewLifecycleOwner.lifecycleScope.launch {
                appContainer.appPreferences.setDiceFaceStyle(DiceFaceStyle.NUMBERS.code)
            }
        }
    }

    private fun updateNumberDisplayUi(style: DiceFaceStyle) {
        if (style == DiceFaceStyle.DOTS) {
            binding.ivRadioDot.setImageResource(R.drawable.ic_radio_checked_green)
            binding.ivRadioNumber.setImageResource(R.drawable.ic_radio_unchecked_gray)
        } else {
            binding.ivRadioDot.setImageResource(R.drawable.ic_radio_unchecked_gray)
            binding.ivRadioNumber.setImageResource(R.drawable.ic_radio_checked_green)
        }
    }

    private fun setupDotColorPalette() {
        binding.layoutDotColors.removeAllViews()
        dotColorViews.clear()

        val inflater = LayoutInflater.from(requireContext())
        for (colorHex in DOT_COLORS) {
            val itemBinding = ItemThemeColorCircleBinding.inflate(inflater, binding.layoutDotColors, false)
            val colorInt = Color.parseColor(colorHex)
            itemBinding.viewColorCircle.backgroundTintList = ColorStateList.valueOf(colorInt)

            val isSelected = colorHex.equals(selectedDotColor, ignoreCase = true)
            itemBinding.viewSelectionRing.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

            itemBinding.root.setOnClickListener {
                selectedDotColor = colorHex
                updateDotColorRings()
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setDotColor(colorHex)
                }
            }

            dotColorViews.add(colorHex to itemBinding)
            binding.layoutDotColors.addView(itemBinding.root)
        }
    }

    private fun updateDotColorRings() {
        for ((hex, itemBinding) in dotColorViews) {
            val isSelected = hex.equals(selectedDotColor, ignoreCase = true)
            itemBinding.viewSelectionRing.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ThemeSelectorBottomSheet"
        private const val ARG_IS_CLASSIC = "arg_is_classic"

        val DICE_COLORS = listOf(
            "#F79371", // Coral Orange
            "#E9807F", // Salmon Pink
            "#E9E880", // Pastel Yellow
            "#7F8BE9", // Soft Purple/Blue
            "#C4EA7E", // Lime Green
            "#80EACE"  // Mint Teal
        )

        val DOT_COLORS = listOf(
            "#FFFFFF", // White
            "#F7D4D5", // Soft Pink
            "#EFEFA9", // Soft Yellow
            "#A9B0F0", // Soft Lavender/Periwinkle
            "#D7EFA8", // Soft Lime
            "#AAF1DE"  // Soft Mint
        )

        fun newInstance(isClassic: Boolean = false): ThemeSelectorBottomSheet {
            return ThemeSelectorBottomSheet().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_CLASSIC, isClassic)
                }
            }
        }
    }
}
