package com.phuongthanh.diceroller.features.settings.ui

import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentDiceSettingsBinding
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import com.phuongthanh.diceroller.features.freeroll.ui.CustomRangeBottomSheet
import com.phuongthanh.diceroller.features.freeroll.ui.DiceTypeBottomSheet
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DiceSettingsFragment : BaseFragment<FragmentDiceSettingsBinding>(FragmentDiceSettingsBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private val isClassic: Boolean by lazy {
        if (arguments?.containsKey("isClassic") == true) {
            arguments?.getBoolean("isClassic") == true
        } else {
            findNavController().previousBackStackEntry?.destination?.id == R.id.classicPlayerFragment
        }
    }

    override fun initViews() {
        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        binding.topBar.setRightAction1 {
            findNavController().safeNavigate(R.id.action_diceSettingsFragment_to_premiumFragment)
        }

        binding.topBar.setRightAction2 {
            findNavController().safeNavigate(R.id.action_diceSettingsFragment_to_settingsFragment)
        }

        if (isClassic) {
            binding.cardRange.visibility = View.GONE
        } else {
            binding.cardRange.visibility = View.VISIBLE
        }

        setupClickListeners()
        observePreferences()
    }

    private fun setupClickListeners() {
        // 1. Phạm vi xúc xắc (Dice Range)
        if (!isClassic) {
            binding.cardRange.setOnDebouncedClickListener {
                showDiceTypeSelector()
            }
        }

        // 2. Giao diện (Theme / Skin)
        binding.cardTheme.setOnDebouncedClickListener {
            showThemeSelector()
        }

        // 3. Hoạt ảnh (Animation)
        binding.cardAnim.setOnDebouncedClickListener {
            showAnimationBottomSheet()
        }

        // 4. Cách gieo xúc xắc (How to Roll)
        binding.cardHowToRoll.setOnDebouncedClickListener {
            showHowToRollBottomSheet()
        }

        // Bottom CTA: Create new game
        binding.btnCreateNewGame.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_diceSettingsFragment_to_customRollHomeFragment)
        }
    }

    private fun observePreferences() {
        val prefs = appContainer.appPreferences

        // Animation type & speed summary
        combine(
            prefs.animationTypeFlow,
            prefs.animationSpeedFlow
        ) { typeCode, speedCode ->
            val typeName = when (typeCode) {
                "full_left" -> getString(R.string.anim_type_full_left)
                "full_right" -> getString(R.string.anim_type_full_right)
                else -> getString(R.string.anim_type_none)
            }
            val speedName = when (speedCode) {
                0 -> getString(R.string.anim_speed_fast)
                2 -> getString(R.string.anim_speed_slow)
                else -> getString(R.string.anim_speed_normal)
            }
            "$typeName • $speedName"
        }.collectWithLifecycle(viewLifecycleOwner) { summary ->
            binding.tvAnimSubtitle.text = summary
        }

        // Theme summary
        combine(
            prefs.sameColorsAllDiceFlow,
            prefs.diceColorFlow,
            prefs.diceFaceStyleFlow
        ) { sameColors, colorHex, styleCode ->
            val colorName = when (colorHex.uppercase()) {
                "#F79371" -> "Coral"
                "#E9807F" -> "Salmon"
                "#E9E880" -> "Yellow"
                "#7F8BE9" -> "Purple"
                "#C4EA7E" -> "Lime"
                "#80EACE" -> "Teal"
                else -> "Lime"
            }
            val modeName = if (sameColors) getString(R.string.theme_same_colors_title) else getString(R.string.theme_separate_colors)
            if (isClassic) {
                "$modeName • $colorName"
            } else {
                val style = DiceFaceStyle.fromCode(styleCode)
                val styleName = if (style == DiceFaceStyle.NUMBERS) getString(R.string.theme_display_number) else getString(R.string.theme_display_dot)
                "$modeName • $styleName"
            }
        }.collectWithLifecycle(viewLifecycleOwner) { summary ->
            binding.tvThemeSubtitle.text = summary
        }

        // Range & Style summary
        if (!isClassic) {
            combine(
                prefs.diceTypeFlow,
                prefs.diceFaceStyleFlow,
                prefs.customMinFlow,
                prefs.customMaxFlow
            ) { typeCode, styleCode, min, max ->
                val style = DiceFaceStyle.fromCode(styleCode)
                val styleName = if (style == DiceFaceStyle.NUMBERS) {
                    getString(R.string.dice_style_numbers)
                } else {
                    getString(R.string.dice_style_dots)
                }
                val typeName = if (typeCode == DiceType.CUSTOM.code) {
                    "$min - $max"
                } else {
                    typeCode
                }
                "$typeName • $styleName"
            }.collectWithLifecycle(viewLifecycleOwner) { summary ->
                binding.tvRangeSubtitle.text = summary
            }
        }

        // How to roll summary (Shake, Sound, Haptic)
        combine(
            prefs.shakeEnabledFlow,
            prefs.soundEnabledFlow,
            prefs.hapticEnabledFlow
        ) { shake, sound, haptic ->
            val shakeStatus = if (shake) "Shake: ON" else "Shake: OFF"
            val soundStatus = if (sound) "Sound" else ""
            val hapticStatus = if (haptic) "Vibrate" else ""
            val extras = listOf(soundStatus, hapticStatus).filter { it.isNotEmpty() }.joinToString(", ")
            if (extras.isNotEmpty()) {
                "$shakeStatus • $extras"
            } else {
                shakeStatus
            }
        }.collectWithLifecycle(viewLifecycleOwner) { summary ->
            binding.tvRollSubtitle.text = summary
        }
    }

    private fun showDiceTypeSelector() {
        viewLifecycleOwner.lifecycleScope.launch {
            val styleCode = appContainer.appPreferences.diceFaceStyleFlow.first()
            val style = DiceFaceStyle.fromCode(styleCode)
            val currentTypeCode = appContainer.appPreferences.diceTypeFlow.first()
            val currentType = DiceType.entries.find { it.code == currentTypeCode } ?: DiceType.D6
            val sheet = DiceTypeBottomSheet.newInstance(currentType, style)
            sheet.onDiceTypeSelected = { newType ->
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setDiceType(newType.code)
                }
            }
            sheet.onFaceStyleSelected = { newStyle ->
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setDiceFaceStyle(newStyle.code)
                }
            }
            sheet.onCustomRangeRequested = {
                showCustomRangeSelector()
            }
            sheet.show(childFragmentManager, DiceTypeBottomSheet.TAG)
        }
    }

    private fun showCustomRangeSelector() {
        viewLifecycleOwner.lifecycleScope.launch {
            val min = appContainer.appPreferences.customMinFlow.first()
            val max = appContainer.appPreferences.customMaxFlow.first()
            val sheet = CustomRangeBottomSheet.newInstance(min, max)
            sheet.onRangeApplied = { newMin, newMax ->
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setCustomRange(newMin, newMax)
                    appContainer.appPreferences.setDiceType(DiceType.CUSTOM.code)
                }
            }
            sheet.show(childFragmentManager, CustomRangeBottomSheet.TAG)
        }
    }

    private fun showThemeSelector() {
        val sheet = ThemeSelectorBottomSheet.newInstance(isClassic = isClassic)
        sheet.show(childFragmentManager, ThemeSelectorBottomSheet.TAG)
    }

    private fun showAnimationBottomSheet() {
        val sheet = AnimationBottomSheet.newInstance()
        sheet.show(childFragmentManager, AnimationBottomSheet.TAG)
    }

    private fun showHowToRollBottomSheet() {
        val sheet = HowToRollBottomSheet.newInstance(isClassic = isClassic)
        sheet.show(childFragmentManager, HowToRollBottomSheet.TAG)
    }
}
