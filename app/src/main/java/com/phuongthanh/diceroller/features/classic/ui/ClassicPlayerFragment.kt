package com.phuongthanh.diceroller.features.classic.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentClassicPlayerBinding
import com.phuongthanh.diceroller.databinding.ItemManyPlayersCardBinding
import com.phuongthanh.diceroller.features.classic.model.PlayerMode
import com.phuongthanh.diceroller.features.classic.model.PlayerSlot
import com.phuongthanh.diceroller.features.classic.viewmodel.ClassicPlayerIntent
import com.phuongthanh.diceroller.features.classic.viewmodel.ClassicPlayerUiEffect
import com.phuongthanh.diceroller.features.classic.viewmodel.ClassicPlayerUiState
import com.phuongthanh.diceroller.features.classic.viewmodel.ClassicPlayerViewModel
import com.phuongthanh.diceroller.features.freeroll.model.DiceItem
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import kotlinx.coroutines.flow.combine

import androidx.core.content.ContextCompat

class ClassicPlayerFragment : BaseFragment<FragmentClassicPlayerBinding>(FragmentClassicPlayerBinding::inflate) {

    private val viewModel: ClassicPlayerViewModel by viewModels()

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private var isShakeEnabled = true
    private var sameColorsAllDice = true
    private var selectedDiceColor = Color.parseColor("#C4EA7E")
    private var selectedDotColor = Color.parseColor("#F7D4D5")
    private var selectedFaceStyle = DiceFaceStyle.DOTS

    private val dicePalette = listOf(
        Color.parseColor("#80EACE"), // Cyan / Turquoise
        Color.parseColor("#E9807F"), // Coral / Red
        Color.parseColor("#E9E880"), // Soft Yellow
        Color.parseColor("#F79371"), // Peach / Salmon
        Color.parseColor("#7F8BE9"), // Light Purple
        Color.parseColor("#C4EA7E")  // Lime / Green
    )

    override fun initViews() {
        // 1. Top Bar
        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        binding.topBar.setRightAction1 {
            findNavController().safeNavigate(R.id.action_classicPlayerFragment_to_premiumFragment)
        }

        binding.topBar.setRightAction2 {
            val args = Bundle().apply { putBoolean("isClassic", false) }
            findNavController().safeNavigate(
                R.id.action_classicPlayerFragment_to_diceSettingsFragment,
                args
            )
        }

        // 2. Mode Toggle
        setupModeToggle()

        // 3. Dice Stepper
        setupDiceStepper()

        // 4. Card Click Listeners
        setupCardClicks()

        // 5. Observers
        observeViewModel()
        observePreferences()
    }

    private fun setupModeToggle() {
        binding.btnMode2P.setOnDebouncedClickListener {
            viewModel.processIntent(ClassicPlayerIntent.SetMode(PlayerMode.TWO_PLAYERS))
        }
        binding.btnMode3P.setOnDebouncedClickListener {
            viewModel.processIntent(ClassicPlayerIntent.SetMode(PlayerMode.THREE_PLAYERS))
        }
        binding.btnMode4P.setOnDebouncedClickListener {
            viewModel.processIntent(ClassicPlayerIntent.SetMode(PlayerMode.FOUR_PLAYERS))
        }
    }

    private fun updateSegmentedMode(mode: PlayerMode) {
        val activeBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_segmented_active_item)
        val inactiveText = ContextCompat.getColor(requireContext(), R.color.segmented_ctrl_inactive_text)
        val activeText = Color.WHITE

        binding.btnMode2P.background = if (mode == PlayerMode.TWO_PLAYERS) activeBg else null
        binding.btnMode2P.setTextColor(if (mode == PlayerMode.TWO_PLAYERS) activeText else inactiveText)

        binding.btnMode3P.background = if (mode == PlayerMode.THREE_PLAYERS) activeBg else null
        binding.btnMode3P.setTextColor(if (mode == PlayerMode.THREE_PLAYERS) activeText else inactiveText)

        binding.btnMode4P.background = if (mode == PlayerMode.FOUR_PLAYERS || mode == PlayerMode.ONE_PLAYER) activeBg else null
        binding.btnMode4P.setTextColor(if (mode == PlayerMode.FOUR_PLAYERS || mode == PlayerMode.ONE_PLAYER) activeText else inactiveText)
    }

    private fun setupDiceStepper() {
        binding.btnMinusDice.setOnDebouncedClickListener {
            val current = viewModel.uiState.value.diceCountPerPlayer
            if (current > 1) {
                viewModel.processIntent(ClassicPlayerIntent.ChangeDiceCount(current - 1))
            }
        }

        binding.btnPlusDice.setOnDebouncedClickListener {
            val current = viewModel.uiState.value.diceCountPerPlayer
            if (current < 3) {
                viewModel.processIntent(ClassicPlayerIntent.ChangeDiceCount(current + 1))
            }
        }
    }

    private fun setupCardClicks() {
        val slots = listOf(
            Pair(1, binding.slotPlayer1),
            Pair(2, binding.slotPlayer2),
            Pair(3, binding.slotPlayer3),
            Pair(4, binding.slotPlayer4)
        )
        for ((slotIndex, slot) in slots) {
            val onClickAction = {
                val playerId = if (viewModel.uiState.value.mode == PlayerMode.TWO_PLAYERS && slotIndex == 3) {
                    2
                } else {
                    slotIndex
                }
                viewModel.processIntent(ClassicPlayerIntent.RollForPlayer(playerId))
            }
            slot.cardPlayerRoot.setOnDebouncedClickListener(debounceTime = 300L) { onClickAction() }
            slot.diceCanvas.onCanvasClickListener = { onClickAction() }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.collectWithLifecycle(viewLifecycleOwner) { state ->
            // Update segmented toggle mode
            updateSegmentedMode(state.mode)

            // Stepper controls
            binding.tvDiceCount.text = state.diceCountPerPlayer.toString()
            val canDecrease = state.diceCountPerPlayer > 1
            binding.btnMinusDice.isEnabled = canDecrease
            binding.btnMinusDice.alpha = if (canDecrease) 1.0f else 0.4f

            val canIncrease = state.diceCountPerPlayer < 3
            binding.btnPlusDice.isEnabled = canIncrease
            binding.btnPlusDice.alpha = if (canIncrease) 1.0f else 0.4f

            // Arena Card Visibility
            applyPlayerGridVisibility(state.mode)

            // Bind Player Cards
            updatePlayerCards(state)
        }

        viewModel.uiEffect.collectWithLifecycle(viewLifecycleOwner) { effect ->
            when (effect) {
                is ClassicPlayerUiEffect.PlayDiceRollSound -> {
                    appContainer.soundManager.playDiceRoll()
                    appContainer.hapticManager.performDiceBounce()
                }
                is ClassicPlayerUiEffect.PlayVictoryFanfare -> {
                    appContainer.soundManager.playVictoryFanfare()
                    appContainer.hapticManager.performVictory()
                }
                is ClassicPlayerUiEffect.AnimateDiceRoll -> {
                    val is2P = viewModel.uiState.value.mode == PlayerMode.TWO_PLAYERS
                    val canvas = when (effect.playerId) {
                        1 -> binding.slotPlayer1.diceCanvas
                        2 -> if (is2P) binding.slotPlayer3.diceCanvas else binding.slotPlayer2.diceCanvas
                        3 -> binding.slotPlayer3.diceCanvas
                        4 -> binding.slotPlayer4.diceCanvas
                        else -> null
                    }
                    canvas?.rollDice(effect.durationMs)
                }
                is ClassicPlayerUiEffect.AnimateAllDiceRoll -> {
                    val is2P = viewModel.uiState.value.mode == PlayerMode.TWO_PLAYERS
                    if (is2P) {
                        binding.slotPlayer1.diceCanvas.rollDice(effect.durationMs)
                        binding.slotPlayer3.diceCanvas.rollDice(effect.durationMs)
                    } else {
                        binding.slotPlayer1.diceCanvas.rollDice(effect.durationMs)
                        binding.slotPlayer2.diceCanvas.rollDice(effect.durationMs)
                        binding.slotPlayer3.diceCanvas.rollDice(effect.durationMs)
                        if (viewModel.uiState.value.mode == PlayerMode.FOUR_PLAYERS) {
                            binding.slotPlayer4.diceCanvas.rollDice(effect.durationMs)
                        }
                    }
                }
            }
        }
    }

    private fun observePreferences() {
        // Preferences sync (DiceType, FaceStyle, Custom Range)
        combine(
            appContainer.appPreferences.diceTypeFlow,
            appContainer.appPreferences.diceFaceStyleFlow,
            appContainer.appPreferences.customMinFlow,
            appContainer.appPreferences.customMaxFlow
        ) { typeCode, styleCode, min, max ->
            val type = DiceType.entries.find { it.code == typeCode } ?: DiceType.D6
            val style = DiceFaceStyle.fromCode(styleCode)
            viewModel.processIntent(ClassicPlayerIntent.SyncConfiguration(type, min, max, style))
        }.collectWithLifecycle(viewLifecycleOwner) {}

        // Theme Colors
        combine(
            appContainer.appPreferences.sameColorsAllDiceFlow,
            appContainer.appPreferences.diceColorFlow,
            appContainer.appPreferences.dotColorFlow,
            appContainer.appPreferences.diceFaceStyleFlow
        ) { sameColors, diceColorHex, dotColorHex, styleCode ->
            val dColor = try {
                Color.parseColor(diceColorHex)
            } catch (_: Exception) {
                Color.parseColor("#C4EA7E")
            }
            val dtColor = try {
                Color.parseColor(dotColorHex)
            } catch (_: Exception) {
                Color.parseColor("#F7D4D5")
            }
            val style = DiceFaceStyle.fromCode(styleCode)
            ClassicPlayerTheme(sameColors, dColor, dtColor, style)
        }.collectWithLifecycle(viewLifecycleOwner) { theme ->
            sameColorsAllDice = theme.sameColors
            selectedDiceColor = theme.diceColor
            selectedDotColor = theme.dotColor
            selectedFaceStyle = theme.faceStyle

            updatePlayerCards(viewModel.uiState.value)
        }

        // Shake to Roll
        appContainer.appPreferences.shakeEnabledFlow.collectWithLifecycle(viewLifecycleOwner) { enabled ->
            isShakeEnabled = enabled
            updateShakeListener()
        }
    }

    private fun updatePlayerCards(state: ClassicPlayerUiState) {
        val playersMap = state.players.associateBy { it.id }
        if (state.mode == PlayerMode.TWO_PLAYERS) {
            bindPlayerCard(binding.slotPlayer1, playersMap[1], state)
            bindPlayerCard(binding.slotPlayer3, playersMap[2], state)
        } else {
            bindPlayerCard(binding.slotPlayer1, playersMap[1], state)
            bindPlayerCard(binding.slotPlayer2, playersMap[2], state)
            bindPlayerCard(binding.slotPlayer3, playersMap[3], state)
            bindPlayerCard(binding.slotPlayer4, playersMap[4], state)
        }
    }

    private fun applyPlayerGridVisibility(mode: PlayerMode) {
        val p1Lp = binding.framePlayer1.layoutParams as LinearLayout.LayoutParams
        val p3Lp = binding.framePlayer3.layoutParams as LinearLayout.LayoutParams

        when (mode) {
            PlayerMode.TWO_PLAYERS -> {
                p1Lp.marginEnd = 0
                p3Lp.marginEnd = 0
                binding.framePlayer1.layoutParams = p1Lp
                binding.framePlayer3.layoutParams = p3Lp

                binding.rowTop.visibility = View.VISIBLE
                binding.framePlayer1.visibility = View.VISIBLE
                binding.framePlayer2.visibility = View.GONE

                binding.rowBottom.visibility = View.VISIBLE
                binding.framePlayer3.visibility = View.VISIBLE
                binding.framePlayer4.visibility = View.GONE
            }
            PlayerMode.THREE_PLAYERS -> {
                val gap = dpToPx(6)
                p1Lp.marginEnd = gap
                p3Lp.marginEnd = gap
                binding.framePlayer1.layoutParams = p1Lp
                binding.framePlayer3.layoutParams = p3Lp

                binding.rowTop.visibility = View.VISIBLE
                binding.framePlayer1.visibility = View.VISIBLE
                binding.framePlayer2.visibility = View.VISIBLE

                binding.rowBottom.visibility = View.VISIBLE
                binding.framePlayer3.visibility = View.VISIBLE
                binding.framePlayer4.visibility = View.INVISIBLE
            }
            PlayerMode.FOUR_PLAYERS, PlayerMode.ONE_PLAYER -> {
                val gap = dpToPx(6)
                p1Lp.marginEnd = gap
                p3Lp.marginEnd = gap
                binding.framePlayer1.layoutParams = p1Lp
                binding.framePlayer3.layoutParams = p3Lp

                binding.rowTop.visibility = View.VISIBLE
                binding.framePlayer1.visibility = View.VISIBLE
                binding.framePlayer2.visibility = View.VISIBLE

                binding.rowBottom.visibility = View.VISIBLE
                binding.framePlayer3.visibility = View.VISIBLE
                binding.framePlayer4.visibility = View.VISIBLE
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun bindPlayerCard(
        slotBinding: ItemManyPlayersCardBinding,
        player: PlayerSlot?,
        state: ClassicPlayerUiState
    ) {
        if (player == null) return

        // 1. Player Label
        slotBinding.tvPlayerName.text = "P${player.id}"

        // 2. Compute Player Theme Assets (P1..P4)
        val (cardBg, avatarBg, scorePillBg, scoreColor) = when (player.id) {
            1 -> listOf(
                R.drawable.bg_card_player_p1,
                R.drawable.bg_avatar_player_p1,
                R.drawable.bg_score_pill_player_p1,
                ContextCompat.getColor(requireContext(), R.color.player_p1_pink)
            )
            2 -> listOf(
                R.drawable.bg_card_player_p2,
                R.drawable.bg_avatar_player_p2,
                R.drawable.bg_score_pill_player_p2,
                ContextCompat.getColor(requireContext(), R.color.player_p2_score)
            )
            3 -> listOf(
                R.drawable.bg_card_player_p3,
                R.drawable.bg_avatar_player_p3,
                R.drawable.bg_score_pill_player_p3,
                ContextCompat.getColor(requireContext(), R.color.player_p3_orange)
            )
            else -> listOf(
                R.drawable.bg_card_player_p4,
                R.drawable.bg_avatar_player_p4,
                R.drawable.bg_score_pill_player_p4,
                ContextCompat.getColor(requireContext(), R.color.player_p4_purple)
            )
        }

        // Winner trophy, Locked badge & Card background
        val isLockedThisRound = player.isLocked && !state.isRoundOver

        when {
            player.isWinner -> {
                slotBinding.ivWinnerTrophy.visibility = View.VISIBLE
                slotBinding.layoutLockedBadge.visibility = View.GONE
                slotBinding.cardPlayerRoot.setBackgroundResource(R.drawable.bg_player_card_winner)
            }
            isLockedThisRound -> {
                slotBinding.ivWinnerTrophy.visibility = View.GONE
                slotBinding.layoutLockedBadge.visibility = View.VISIBLE
                slotBinding.cardPlayerRoot.setBackgroundResource(R.drawable.bg_player_card_locked)
            }
            else -> {
                slotBinding.ivWinnerTrophy.visibility = View.GONE
                slotBinding.layoutLockedBadge.visibility = View.GONE
                slotBinding.cardPlayerRoot.setBackgroundResource(cardBg as Int)
            }
        }

        slotBinding.layoutPlayerPill.setBackgroundResource(avatarBg as Int)
        slotBinding.cardScorePill.setBackgroundResource(scorePillBg as Int)
        slotBinding.tvPlayerScore.setTextColor(scoreColor as Int)

        // 3. Score Pill
        slotBinding.tvPlayerScore.text = if (player.isRolling) "..." else player.score.toString()

        // 4. Compute Die Colors based on preferences
        val dieBgColor = if (sameColorsAllDice) {
            selectedDiceColor
        } else {
            dicePalette[(player.id - 1) % dicePalette.size]
        }
        val dieDotColor = selectedDotColor

        // 5. Configure Table / Card Colors and Theme Colors for 3D View
        slotBinding.diceCanvas.setShowDividers(false)
        slotBinding.diceCanvas.setTableColors(
            bgColor = Color.TRANSPARENT,
            separatorColor = Color.TRANSPARENT
        )
        slotBinding.diceCanvas.setThemeColors(
            sameColors = true,
            diceColor = dieBgColor,
            dotColor = dieDotColor,
            faceStyle = selectedFaceStyle
        )

        // 6. Map to DiceItem list and update 3D Canvas
        val diceItems = player.diceValues.mapIndexed { index, value ->
            DiceItem(
                id = index,
                type = state.diceType,
                value = value,
                isLocked = false,
                customMin = state.customMin,
                customMax = state.customMax,
                faceStyle = selectedFaceStyle
            )
        }
        slotBinding.diceCanvas.setDiceList(diceItems)
    }

    override fun onResume() {
        super.onResume()
        updateShakeListener()
        binding.slotPlayer1.diceCanvas.onResume()
        binding.slotPlayer2.diceCanvas.onResume()
        binding.slotPlayer3.diceCanvas.onResume()
        binding.slotPlayer4.diceCanvas.onResume()
    }

    override fun onPause() {
        super.onPause()
        appContainer.shakeDetector.stopListening()
        binding.slotPlayer1.diceCanvas.onPause()
        binding.slotPlayer2.diceCanvas.onPause()
        binding.slotPlayer3.diceCanvas.onPause()
        binding.slotPlayer4.diceCanvas.onPause()
    }

    private fun updateShakeListener() {
        if (!isResumed) return
        if (isShakeEnabled) {
            appContainer.shakeDetector.startListening {
                handleShakeToRoll()
            }
        } else {
            appContainer.shakeDetector.stopListening()
        }
    }

    private fun handleShakeToRoll() {
        val state = viewModel.uiState.value
        if (state.isRoundOver) {
            viewModel.processIntent(ClassicPlayerIntent.ResetRound)
        } else {
            viewModel.processIntent(ClassicPlayerIntent.RollAll)
        }
    }
}

private data class ClassicPlayerTheme(
    val sameColors: Boolean,
    val diceColor: Int,
    val dotColor: Int,
    val faceStyle: DiceFaceStyle
)
