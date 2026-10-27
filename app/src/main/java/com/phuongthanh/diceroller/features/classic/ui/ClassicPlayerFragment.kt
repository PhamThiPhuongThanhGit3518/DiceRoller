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

class ClassicPlayerFragment : BaseFragment<FragmentClassicPlayerBinding>(FragmentClassicPlayerBinding::inflate) {

    private val viewModel: ClassicPlayerViewModel by viewModels()

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private var isShakeEnabled = true
    private var sameColorsAllDice = true
    private var selectedDiceColor = Color.WHITE
    private var selectedDotColor = Color.parseColor("#1E293B")

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
        binding.btnBack.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }
        binding.tvHeaderTitle.text = getString(R.string.many_players_title)
        binding.tvHeaderSubtitle.text = getString(R.string.many_players_subtitle)

        binding.btnSettings.setOnDebouncedClickListener {
            val args = Bundle().apply { putBoolean("isClassic", false) }
            findNavController().safeNavigate(
                R.id.action_classicPlayerFragment_to_diceSettingsFragment,
                args
            )
        }

        binding.btnVip.setOnDebouncedClickListener {
            // Future VIP modal / entry point
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
        binding.toggleGroupMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnMode2P -> PlayerMode.TWO_PLAYERS
                R.id.btnMode3P -> PlayerMode.THREE_PLAYERS
                R.id.btnMode4P -> PlayerMode.FOUR_PLAYERS
                else -> PlayerMode.FOUR_PLAYERS
            }
            if (viewModel.uiState.value.mode != mode) {
                viewModel.processIntent(ClassicPlayerIntent.SetMode(mode))
            }
        }
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
            // Update toggle mode button if external or init
            val checkedBtnId = when (state.mode) {
                PlayerMode.TWO_PLAYERS -> R.id.btnMode2P
                PlayerMode.THREE_PLAYERS -> R.id.btnMode3P
                PlayerMode.FOUR_PLAYERS, PlayerMode.ONE_PLAYER -> R.id.btnMode4P
            }
            if (binding.toggleGroupMode.checkedButtonId != checkedBtnId) {
                binding.toggleGroupMode.check(checkedBtnId)
            }

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
            appContainer.appPreferences.dotColorFlow
        ) { sameColors, diceColorHex, dotColorHex ->
            val dColor = try {
                Color.parseColor(diceColorHex)
            } catch (_: Exception) {
                Color.WHITE
            }
            val dtColor = try {
                Color.parseColor(dotColorHex)
            } catch (_: Exception) {
                Color.parseColor("#1E293B")
            }
            Triple(sameColors, dColor, dtColor)
        }.collectWithLifecycle(viewLifecycleOwner) { (sameColors, dColor, dtColor) ->
            sameColorsAllDice = sameColors
            selectedDiceColor = dColor
            selectedDotColor = dtColor

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

        // 2. Winner trophy, Locked badge & Card background
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
                slotBinding.cardPlayerRoot.setBackgroundResource(R.drawable.bg_player_card)
            }
        }

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
            bgColor = Color.parseColor("#F1F5F9"),
            separatorColor = Color.parseColor("#F1F5F9")
        )
        slotBinding.diceCanvas.setThemeColors(
            sameColors = true,
            diceColor = dieBgColor,
            dotColor = dieDotColor
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
                faceStyle = state.faceStyle
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
