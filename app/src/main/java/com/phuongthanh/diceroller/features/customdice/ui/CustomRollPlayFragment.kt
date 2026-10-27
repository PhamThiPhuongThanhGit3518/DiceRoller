package com.phuongthanh.diceroller.features.customdice.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentCustomRollPlayBinding
import com.phuongthanh.diceroller.features.customdice.model.CustomDice
import com.phuongthanh.diceroller.features.customdice.model.CustomDiceFace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomRollPlayFragment : BaseFragment<FragmentCustomRollPlayBinding>(FragmentCustomRollPlayBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private var currentDice: CustomDice? = null
    private var currentDiceCount: Int = 8
    private val diceStateList = mutableListOf<CustomDiceFace>()
    private var isRolling = false

    override fun initViews() {
        val diceId = arguments?.getString("diceId").orEmpty()

        // 1. Back Navigation
        binding.btnBack.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        // 2. Help Information Dialog
        binding.btnHelp.setOnDebouncedClickListener {
            showHelpDialog()
        }

        // 3. Dice Settings Navigation
        binding.btnSettings.setOnDebouncedClickListener {
            val args = Bundle().apply { putBoolean("isClassic", false) }
            findNavController().navigate(
                R.id.action_customRollPlayFragment_to_diceSettingsFragment,
                args
            )
        }

        // 4. Dice Mode / Roll Button
        binding.btnDiceMode.setOnDebouncedClickListener {
            rollDice()
        }

        // 5. Canvas Arena Click: Tap anywhere on grid to Roll
        binding.arenaLayout.onArenaClickListener = {
            if (!isRolling) {
                rollDice()
            }
        }

        // 6. Stepper: Minus & Plus (1 to 9 dice)
        binding.btnMinus.setOnDebouncedClickListener {
            if (currentDiceCount > 1 && !isRolling) {
                currentDiceCount--
                if (diceStateList.size > currentDiceCount) {
                    diceStateList.removeAt(diceStateList.lastIndex)
                }
                updateDiceCountDisplay()
            }
        }

        binding.btnPlus.setOnDebouncedClickListener {
            val dice = currentDice
            if (currentDiceCount < 9 && !isRolling && dice != null && dice.faces.isNotEmpty()) {
                currentDiceCount++
                val nextFace = dice.faces.random()
                diceStateList.add(nextFace)
                updateDiceCountDisplay()
            }
        }

        // Load Custom Dice from DB
        viewLifecycleOwner.lifecycleScope.launch {
            val dice = appContainer.customDiceRepository.getCustomDice(diceId)
            if (dice != null) {
                currentDice = dice
                setupDiceGame(dice)
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupDiceGame(dice: CustomDice) {
        currentDiceCount = if (dice.diceCount in 1..9) dice.diceCount else 8
        diceStateList.clear()

        val validFaces = if (dice.faces.isNotEmpty()) dice.faces else listOf(CustomDiceFace(1, "1"))
        for (i in 0 until currentDiceCount) {
            diceStateList.add(validFaces[i % validFaces.size])
        }

        updateDiceCountDisplay()
    }

    private fun updateDiceCountDisplay() {
        binding.tvDiceCount.text = currentDiceCount.toString()
        binding.arenaLayout.setDiceCount(currentDiceCount, diceStateList)

        val canDecrease = currentDiceCount > 1 && !isRolling
        binding.btnMinus.isEnabled = canDecrease
        binding.btnMinus.alpha = if (canDecrease) 1.0f else 0.4f

        val canIncrease = currentDiceCount < 9 && !isRolling
        binding.btnPlus.isEnabled = canIncrease
        binding.btnPlus.alpha = if (canIncrease) 1.0f else 0.4f
    }

    private fun rollDice() {
        val dice = currentDice ?: return
        if (isRolling || dice.faces.isEmpty()) return

        isRolling = true
        binding.btnDiceMode.isEnabled = false
        binding.btnMinus.isEnabled = false
        binding.btnMinus.alpha = 0.4f
        binding.btnPlus.isEnabled = false
        binding.btnPlus.alpha = 0.4f

        appContainer.soundManager.playDiceRoll()
        appContainer.hapticManager.performDiceBounce()

        viewLifecycleOwner.lifecycleScope.launch {
            // Animate rolling scramble for 7 frames (~630ms)
            repeat(7) {
                for (i in diceStateList.indices) {
                    diceStateList[i] = dice.faces.random()
                }
                binding.arenaLayout.updateDiceFaces(diceStateList)
                binding.arenaLayout.animateTumble()
                delay(90)
            }

            // Final stop
            for (i in diceStateList.indices) {
                diceStateList[i] = dice.faces.random()
            }
            binding.arenaLayout.updateDiceFaces(diceStateList)
            binding.arenaLayout.animateFinalBounce()

            appContainer.soundManager.playVictoryFanfare()

            isRolling = false
            binding.btnDiceMode.isEnabled = true
            updateDiceCountDisplay()
        }
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.custom_play_help_title)
            .setMessage(R.string.custom_play_help_message)
            .setPositiveButton(R.string.btn_got_it, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        appContainer.shakeDetector.startListening {
            if (!isRolling) {
                rollDice()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        appContainer.shakeDetector.stopListening()
    }
}
