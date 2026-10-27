package com.phuongthanh.diceroller.features.freeroll.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.features.freeroll.model.DiceItem
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class FreeRollViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FreeRollUiState())
    val uiState: StateFlow<FreeRollUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<FreeRollUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        val initialDice = createDiceList(
            count = _uiState.value.diceCount,
            type = _uiState.value.diceType,
            customMin = _uiState.value.customMin,
            customMax = _uiState.value.customMax
        )
        val total = initialDice.sumOf { it.value }
        val breakdown = initialDice.joinToString(" + ") { it.value.toString() }
        _uiState.update {
            it.copy(
                diceList = initialDice,
                totalScore = total,
                scoreBreakdown = breakdown
            )
        }
    }

    private var animationSpeed: Int = 1

    fun processIntent(intent: FreeRollIntent) {
        when (intent) {
            is FreeRollIntent.Roll -> rollDice()
            is FreeRollIntent.ToggleLock -> toggleDiceLock(intent.diceId)
            is FreeRollIntent.ChangeDiceCount -> changeDiceCount(intent.count)
            is FreeRollIntent.ChangeDiceType -> changeDiceType(intent.type)
            is FreeRollIntent.SetCustomRange -> setCustomRange(intent.min, intent.max)
            is FreeRollIntent.SetFaceStyle -> setFaceStyle(intent.style)
            is FreeRollIntent.SetAnimationSpeed -> animationSpeed = intent.speed
            is FreeRollIntent.SyncConfiguration -> syncConfiguration(
                intent.type,
                intent.faceStyle,
                intent.customMin,
                intent.customMax
            )
        }
    }

    private fun rollDice() {
        if (_uiState.value.isRolling) return

        viewModelScope.launch {
            val durationMs = when (animationSpeed) {
                0 -> 750L   // Fast
                2 -> 1600L  // Slow
                else -> 1100L // Normal
            }

            _uiState.update { it.copy(isRolling = true) }
            _uiEffect.send(FreeRollUiEffect.PlayRollAudioAndHaptic)

            // Determine final settled roll
            val settledList = _uiState.value.diceList.map { die ->
                if (die.isLocked) die
                else die.copy(value = die.rollNewValue())
            }
            val total = settledList.sumOf { it.value }
            val breakdown = settledList.joinToString(" + ") { it.value.toString() }

            _uiState.update { state ->
                state.copy(diceList = settledList)
            }
            _uiEffect.send(FreeRollUiEffect.Animate3DRoll(durationMs))

            delay(durationMs)

            _uiState.update { state ->
                state.copy(
                    totalScore = total,
                    scoreBreakdown = breakdown,
                    isRolling = false
                )
            }
        }
    }

    private fun toggleDiceLock(diceId: Int) {
        if (_uiState.value.isRolling) return

        _uiState.update { state ->
            val updatedList = state.diceList.map { die ->
                if (die.id == diceId) die.copy(isLocked = !die.isLocked) else die
            }
            state.copy(diceList = updatedList)
        }
    }

    private fun changeDiceCount(count: Int) {
        if (_uiState.value.isRolling) return
        val clampedCount = count.coerceIn(1, 9)
        if (clampedCount == _uiState.value.diceCount) return

        _uiState.update { state ->
            val currentList = state.diceList
            val newList = if (clampedCount < currentList.size) {
                currentList.take(clampedCount)
            } else {
                val added = (currentList.size until clampedCount).map { index ->
                    val type = state.diceType
                    val initialValue = when (type) {
                        DiceType.CUSTOM -> Random.nextInt(
                            minOf(state.customMin, state.customMax),
                            maxOf(state.customMin, state.customMax) + 1
                        )
                        else -> Random.nextInt(type.minValue, type.maxValue + 1)
                    }
                    DiceItem(
                        id = index,
                        type = type,
                        value = initialValue,
                        customMin = state.customMin,
                        customMax = state.customMax,
                        faceStyle = state.faceStyle
                    )
                }
                currentList + added
            }
            val total = newList.sumOf { it.value }
            val breakdown = newList.joinToString(" + ") { it.value.toString() }
            state.copy(
                diceCount = clampedCount,
                diceList = newList,
                totalScore = total,
                scoreBreakdown = breakdown
            )
        }
    }

    private fun changeDiceType(type: DiceType) {
        if (_uiState.value.isRolling) return

        _uiState.update { state ->
            val updatedList = state.diceList.map { die ->
                if (die.isLocked) {
                    die.copy(type = type)
                } else {
                    val newValue = when (type) {
                        DiceType.CUSTOM -> Random.nextInt(
                            minOf(state.customMin, state.customMax),
                            maxOf(state.customMin, state.customMax) + 1
                        )
                        else -> Random.nextInt(type.minValue, type.maxValue + 1)
                    }
                    die.copy(
                        type = type,
                        value = newValue,
                        rotationAngle = (Random.nextFloat() * 16f) - 8f
                    )
                }
            }
            val total = updatedList.sumOf { it.value }
            val breakdown = updatedList.joinToString(" + ") { it.value.toString() }
            state.copy(
                diceType = type,
                diceList = updatedList,
                totalScore = total,
                scoreBreakdown = breakdown
            )
        }
    }

    private fun setCustomRange(min: Int, max: Int) {
        if (_uiState.value.isRolling) return
        val low = minOf(min, max)
        val high = maxOf(min, max)

        _uiState.update { state ->
            val updatedList = state.diceList.map { die ->
                if (die.isLocked) {
                    die.copy(type = DiceType.CUSTOM, customMin = low, customMax = high)
                } else {
                    val newValue = Random.nextInt(low, high + 1)
                    die.copy(
                        type = DiceType.CUSTOM,
                        value = newValue,
                        customMin = low,
                        customMax = high,
                        rotationAngle = (Random.nextFloat() * 16f) - 8f
                    )
                }
            }
            val total = updatedList.sumOf { it.value }
            val breakdown = updatedList.joinToString(" + ") { it.value.toString() }
            state.copy(
                diceType = DiceType.CUSTOM,
                customMin = low,
                customMax = high,
                diceList = updatedList,
                totalScore = total,
                scoreBreakdown = breakdown
            )
        }
    }

    private fun setFaceStyle(style: DiceFaceStyle) {
        _uiState.update { state ->
            val updated = state.diceList.map { it.copy(faceStyle = style) }
            state.copy(faceStyle = style, diceList = updated)
        }
    }

    private fun syncConfiguration(
        type: DiceType,
        faceStyle: DiceFaceStyle,
        customMin: Int,
        customMax: Int
    ) {
        val low = minOf(customMin, customMax)
        val high = maxOf(customMin, customMax)

        _uiState.update { state ->
            val isTypeChanged = state.diceType != type
            val isStyleChanged = state.faceStyle != faceStyle
            val isCustomChanged = type == DiceType.CUSTOM && (state.customMin != low || state.customMax != high)

            if (!isTypeChanged && !isStyleChanged && !isCustomChanged) {
                return@update state
            }

            val updatedList = state.diceList.map { die ->
                val newValue = if (isTypeChanged || isCustomChanged) {
                    val maxAllowed = if (type == DiceType.CUSTOM) high else type.maxValue
                    val minAllowed = if (type == DiceType.CUSTOM) low else type.minValue
                    if (die.isLocked && die.value in minAllowed..maxAllowed) {
                        die.value
                    } else {
                        when (type) {
                            DiceType.CUSTOM -> Random.nextInt(low, high + 1)
                            else -> Random.nextInt(type.minValue, type.maxValue + 1)
                        }
                    }
                } else {
                    die.value
                }

                die.copy(
                    type = type,
                    faceStyle = faceStyle,
                    customMin = low,
                    customMax = high,
                    value = newValue
                )
            }
            val total = updatedList.sumOf { it.value }
            val breakdown = updatedList.joinToString(" + ") { it.value.toString() }
            state.copy(
                diceType = type,
                faceStyle = faceStyle,
                customMin = low,
                customMax = high,
                diceList = updatedList,
                totalScore = total,
                scoreBreakdown = breakdown
            )
        }
    }

    private fun createDiceList(
        count: Int,
        type: DiceType,
        customMin: Int,
        customMax: Int,
        faceStyle: DiceFaceStyle = DiceFaceStyle.DOTS
    ): List<DiceItem> {
        return (0 until count).map { index ->
            val initialValue = when (type) {
                DiceType.CUSTOM -> Random.nextInt(minOf(customMin, customMax), maxOf(customMin, customMax) + 1)
                else -> Random.nextInt(type.minValue, type.maxValue + 1)
            }
            DiceItem(
                id = index,
                type = type,
                value = initialValue,
                customMin = customMin,
                customMax = customMax,
                rotationAngle = (Random.nextFloat() * 16f) - 8f,
                faceStyle = faceStyle
            )
        }
    }
}
