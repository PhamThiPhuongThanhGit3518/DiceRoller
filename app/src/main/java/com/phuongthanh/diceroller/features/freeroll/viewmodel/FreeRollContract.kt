package com.phuongthanh.diceroller.features.freeroll.viewmodel

import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.features.freeroll.model.DiceItem
import com.phuongthanh.diceroller.features.freeroll.model.DiceType

data class FreeRollUiState(
    val diceList: List<DiceItem> = emptyList(),
    val diceCount: Int = 2,
    val diceType: DiceType = DiceType.D6,
    val customMin: Int = 1,
    val customMax: Int = 100,
    val isRolling: Boolean = false,
    val totalScore: Int = 0,
    val scoreBreakdown: String = "",
    val faceStyle: DiceFaceStyle = DiceFaceStyle.DOTS
) {
    val canChooseFaceStyle: Boolean
        get() = diceType == DiceType.D6 || diceType == DiceType.D4 || (diceType == DiceType.CUSTOM && maxOf(customMin, customMax) <= 6 && minOf(customMin, customMax) >= 1)
}

sealed interface FreeRollUiEffect {
    object PlayRollAudioAndHaptic : FreeRollUiEffect
    data class Animate3DRoll(val durationMs: Long) : FreeRollUiEffect
}

sealed interface FreeRollIntent {
    object Roll : FreeRollIntent
    data class ToggleLock(val diceId: Int) : FreeRollIntent
    data class ChangeDiceCount(val count: Int) : FreeRollIntent
    data class ChangeDiceType(val type: DiceType) : FreeRollIntent
    data class SetCustomRange(val min: Int, val max: Int) : FreeRollIntent
    data class SetFaceStyle(val style: DiceFaceStyle) : FreeRollIntent
    data class SetAnimationSpeed(val speed: Int) : FreeRollIntent
    data class SyncConfiguration(
        val type: DiceType,
        val faceStyle: DiceFaceStyle,
        val customMin: Int,
        val customMax: Int
    ) : FreeRollIntent
}
