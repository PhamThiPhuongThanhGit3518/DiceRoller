package com.phuongthanh.diceroller.features.freeroll.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentFreeRollBinding
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import com.phuongthanh.diceroller.features.freeroll.viewmodel.FreeRollIntent
import com.phuongthanh.diceroller.features.freeroll.viewmodel.FreeRollUiEffect
import com.phuongthanh.diceroller.features.freeroll.viewmodel.FreeRollViewModel
import kotlinx.coroutines.flow.combine

import com.phuongthanh.diceroller.core.utils.safeNavigate

class FreeRollFragment : BaseFragment<FragmentFreeRollBinding>(FragmentFreeRollBinding::inflate) {

    private val viewModel: FreeRollViewModel by viewModels()

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private var isShakeEnabled = true

    override fun initViews() {
        // Back Navigation
        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        // Top Bar: VIP Crown Navigation
        binding.topBar.setRightAction1 {
            findNavController().safeNavigate(R.id.action_freeRollFragment_to_premiumFragment)
        }

        // Dice Settings Navigation
        binding.topBar.setRightAction2 {
            val args = Bundle().apply { putBoolean("isClassic", false) }
            findNavController().safeNavigate(
                R.id.action_freeRollFragment_to_diceSettingsFragment,
                args
            )
        }

        // Configure Canvas for Midnight background
        binding.diceCanvas.setShowDividers(false)
        binding.diceCanvas.setTableColors(
            bgColor = android.graphics.Color.TRANSPARENT,
            separatorColor = android.graphics.Color.TRANSPARENT
        )

        // Roll Button
        binding.btnRoll.setOnDebouncedClickListener {
            viewModel.processIntent(FreeRollIntent.Roll)
        }

        // Action Button Right: Unlock all or Roll
        binding.btnActionRight.setOnDebouncedClickListener {
            val diceList = viewModel.uiState.value.diceList
            val lockedDice = diceList.filter { it.isLocked }
            if (lockedDice.isNotEmpty()) {
                lockedDice.forEach { die ->
                    viewModel.processIntent(FreeRollIntent.ToggleLock(die.id))
                }
            } else {
                viewModel.processIntent(FreeRollIntent.Roll)
            }
        }

        // Stepper: Minus & Plus (1 to 9 dice)
        binding.btnMinus.setOnDebouncedClickListener {
            val current = viewModel.uiState.value.diceCount
            if (current > 1) {
                viewModel.processIntent(FreeRollIntent.ChangeDiceCount(current - 1))
            }
        }

        binding.btnPlus.setOnDebouncedClickListener {
            val current = viewModel.uiState.value.diceCount
            if (current < 9) {
                viewModel.processIntent(FreeRollIntent.ChangeDiceCount(current + 1))
            }
        }

        // Canvas Click: Tap anywhere on grid to Roll; Long press to toggle lock
        binding.diceCanvas.onCanvasClickListener = {
            viewModel.processIntent(FreeRollIntent.Roll)
        }

        binding.diceCanvas.onDiceLongClickListener = { diceId ->
            viewModel.processIntent(FreeRollIntent.ToggleLock(diceId))
        }

        // Observe ViewModel & Preferences
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.collectWithLifecycle(viewLifecycleOwner) { state ->
            // Canvas updates
            binding.diceCanvas.setDiceList(state.diceList)

            // Stepper count display & button states
            val count = state.diceCount
            binding.tvDiceCount.text = count.toString()

            val canDecrease = count > 1 && !state.isRolling
            binding.btnMinus.isEnabled = canDecrease
            binding.btnMinus.alpha = if (canDecrease) 1.0f else 0.4f

            val canIncrease = count < 9 && !state.isRolling
            binding.btnPlus.isEnabled = canIncrease
            binding.btnPlus.alpha = if (canIncrease) 1.0f else 0.4f

            // Total Score Pill
            if (state.isRolling) {
                binding.tvTotalScore.text = getString(R.string.rolling_status)
                binding.btnRoll.isEnabled = false
            } else {
                binding.tvTotalScore.text = getString(R.string.total_score_display, state.totalScore)
                binding.btnRoll.isEnabled = true
            }
        }

        viewModel.uiEffect.collectWithLifecycle(viewLifecycleOwner) { effect ->
            when (effect) {
                is FreeRollUiEffect.PlayRollAudioAndHaptic -> {
                    appContainer.soundManager.playDiceRoll()
                    appContainer.hapticManager.performDiceBounce()
                }
                is FreeRollUiEffect.Animate3DRoll -> {
                    binding.diceCanvas.rollDice(effect.durationMs)
                }
            }
        }

        // Observe Preferences from Dice Settings
        appContainer.appPreferences.showTotalScoreFlow.collectWithLifecycle(viewLifecycleOwner) { show ->
            binding.cardTotal.visibility = if (show) View.VISIBLE else View.GONE
        }

        appContainer.appPreferences.animationSpeedFlow.collectWithLifecycle(viewLifecycleOwner) { speed ->
            viewModel.processIntent(FreeRollIntent.SetAnimationSpeed(speed))
        }

        combine(
            appContainer.appPreferences.diceTypeFlow,
            appContainer.appPreferences.diceFaceStyleFlow,
            appContainer.appPreferences.customMinFlow,
            appContainer.appPreferences.customMaxFlow
        ) { typeCode, styleCode, min, max ->
            val type = DiceType.entries.find { it.code == typeCode } ?: DiceType.D6
            val style = DiceFaceStyle.fromCode(styleCode)
            viewModel.processIntent(FreeRollIntent.SyncConfiguration(type, style, min, max))
        }.collectWithLifecycle(viewLifecycleOwner) {}

        // Theme colors (Same Colors on All Dice, Dice Color, Dot Color, Face Style)
        combine(
            appContainer.appPreferences.sameColorsAllDiceFlow,
            appContainer.appPreferences.diceColorFlow,
            appContainer.appPreferences.dotColorFlow,
            appContainer.appPreferences.diceFaceStyleFlow
        ) { sameColors, diceColorHex, dotColorHex, styleCode ->
            val diceColor = try {
                android.graphics.Color.parseColor(diceColorHex)
            } catch (_: Exception) {
                android.graphics.Color.parseColor("#C4EA7E")
            }
            val dotColor = try {
                android.graphics.Color.parseColor(dotColorHex)
            } catch (_: Exception) {
                android.graphics.Color.parseColor("#F7D4D5")
            }
            val faceStyle = DiceFaceStyle.fromCode(styleCode)
            FreeRollTheme(sameColors, diceColor, dotColor, faceStyle)
        }.collectWithLifecycle(viewLifecycleOwner) { theme ->
            binding.diceCanvas.setThemeColors(
                sameColors = theme.sameColors,
                diceColor = theme.diceColor,
                dotColor = theme.dotColor,
                faceStyle = theme.faceStyle
            )
        }

        appContainer.appPreferences.shakeEnabledFlow.collectWithLifecycle(viewLifecycleOwner) { enabled ->
            isShakeEnabled = enabled
            updateShakeListener()
        }
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.free_roll_help_title)
            .setMessage(R.string.free_roll_help_message)
            .setPositiveButton(R.string.btn_got_it, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateShakeListener()
        binding.diceCanvas.onResume()
    }

    override fun onPause() {
        super.onPause()
        appContainer.shakeDetector.stopListening()
        binding.diceCanvas.onPause()
    }

    private fun updateShakeListener() {
        if (!isResumed) return
        if (isShakeEnabled) {
            appContainer.shakeDetector.startListening {
                viewModel.processIntent(FreeRollIntent.Roll)
            }
        } else {
            appContainer.shakeDetector.stopListening()
        }
    }
}

private data class FreeRollTheme(
    val sameColors: Boolean,
    val diceColor: Int,
    val dotColor: Int,
    val faceStyle: DiceFaceStyle
)
