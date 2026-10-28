package com.phuongthanh.diceroller.features.party.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentPartyRollPlayBinding
import com.phuongthanh.diceroller.features.freeroll.model.DiceItem
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import com.phuongthanh.diceroller.features.party.model.PartyChallenge
import com.phuongthanh.diceroller.features.party.model.PartyChallengePack
import com.phuongthanh.diceroller.features.party.model.PartyPacksBank
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.random.Random

class PartyRollPlayFragment : BaseFragment<FragmentPartyRollPlayBinding>(FragmentPartyRollPlayBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private lateinit var currentPack: PartyChallengePack
    private var isRolling = false

    override fun initViews() {
        val packId = arguments?.getString("packId") ?: "funny_dare"
        currentPack = PartyPacksBank.getPackById(packId)

        // 1. Pack Title
        binding.tvPackTitle.setText(currentPack.nameRes)

        // 2. Setup Top Bar Actions
        binding.btnBack.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        binding.btnHelp.setOnDebouncedClickListener {
            showHelpDialog()
        }

        binding.btnSettings.setOnDebouncedClickListener {
            val args = Bundle().apply { putBoolean("isClassic", false) }
            findNavController().safeNavigate(
                R.id.action_partyRollPlayFragment_to_diceSettingsFragment,
                args
            )
        }

        // 3. Dice Mode / Roll Button
        binding.btnDiceMode.setOnDebouncedClickListener {
            if (!isRolling) {
                startRollAnimation()
            }
        }

        // 4. Tap Table to Roll
        binding.diceCanvas.onCanvasClickListener = {
            if (!isRolling && binding.layoutResultContainer.visibility != View.VISIBLE) {
                startRollAnimation()
            }
        }

        // 5. Result Screen Actions (Image 2)
        binding.btnCloseResult.setOnDebouncedClickListener {
            binding.layoutResultContainer.visibility = View.GONE
        }

        binding.btnHomeResult.setOnDebouncedClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

        binding.btnRollAgain.setOnDebouncedClickListener {
            startRollAnimation()
        }

        // Initialize 3D dice
        val initialDie = DiceItem(1, DiceType.D6, 4, false)
        binding.diceCanvas.setDiceList(listOf(initialDie))
        binding.tvCenterNumber.text = "4"

        // Observe Theme Preferences
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
            PartyTheme(sameColors, diceColor, dotColor, faceStyle)
        }.collectWithLifecycle(viewLifecycleOwner) { theme ->
            binding.diceCanvas.setThemeColors(
                sameColors = theme.sameColors,
                diceColor = theme.diceColor,
                dotColor = theme.dotColor,
                faceStyle = theme.faceStyle
            )
        }

        // Auto roll once on entering from Detail
        viewLifecycleOwner.lifecycleScope.launch {
            delay(300)
            startRollAnimation()
        }
    }

    private fun startRollAnimation() {
        if (isRolling) return
        isRolling = true

        // Hide result overlay if visible
        binding.layoutResultContainer.visibility = View.GONE

        // Sound & Haptics
        appContainer.soundManager.playDiceRoll()
        appContainer.hapticManager.performDiceBounce()

        val finalValue = Random.nextInt(1, 21) // 1 to 20
        val dieTarget = ((finalValue - 1) % 6) + 1
        binding.diceCanvas.setDiceList(listOf(DiceItem(1, DiceType.D6, dieTarget, false)))
        binding.diceCanvas.rollDice(1100L)

        viewLifecycleOwner.lifecycleScope.launch {
            // Suspense countdown / rapid number cycle
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 1100L) {
                binding.tvCenterNumber.text = Random.nextInt(1, 21).toString()
                delay(60)
            }
            binding.tvCenterNumber.text = finalValue.toString()

            delay(200)

            // Select random challenge from current pack
            val challenge = PartyPacksBank.getRandomChallenge(currentPack.id)

            // Show Result Screen (Image 2)
            showResultOverlay(finalValue, challenge)

            appContainer.soundManager.playVictoryFanfare()
            appContainer.hapticManager.performVictory()
            isRolling = false
        }
    }

    private fun showResultOverlay(rolledNumber: Int, challenge: PartyChallenge) {
        binding.tvResultNumber.text = rolledNumber.toString()
        binding.tvChallengeText.text = "\"${getString(challenge.textRes)}\""

        binding.layoutResultContainer.alpha = 0f
        binding.layoutResultContainer.visibility = View.VISIBLE
        binding.layoutResultContainer.animate()
            .alpha(1f)
            .setDuration(220)
            .start()
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(currentPack.nameRes)
            .setMessage(getString(R.string.custom_play_help_message))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        binding.diceCanvas.onResume()
    }

    override fun onPause() {
        binding.diceCanvas.onPause()
        super.onPause()
    }
}

private data class PartyTheme(
    val sameColors: Boolean,
    val diceColor: Int,
    val dotColor: Int,
    val faceStyle: DiceFaceStyle
)
