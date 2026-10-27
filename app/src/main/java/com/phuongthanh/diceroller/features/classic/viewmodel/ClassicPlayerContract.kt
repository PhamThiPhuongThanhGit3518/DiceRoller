package com.phuongthanh.diceroller.features.classic.viewmodel

import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.features.classic.model.PlayerMode
import com.phuongthanh.diceroller.features.classic.model.PlayerSlot
import com.phuongthanh.diceroller.features.freeroll.model.DiceType

data class ClassicPlayerUiState(
    val mode: PlayerMode = PlayerMode.FOUR_PLAYERS,
    val diceCountPerPlayer: Int = 1,
    val players: List<PlayerSlot> = emptyList(),
    val isRoundOver: Boolean = false,
    val winnerId: Int? = null,
    val loserId: Int? = null,
    val diceType: DiceType = DiceType.D6,
    val customMin: Int = 1,
    val customMax: Int = 100,
    val faceStyle: DiceFaceStyle = DiceFaceStyle.DOTS,
    val diceColor: String = "#FFFFFF",
    val dotColor: String = "#1E293B"
)

sealed interface ClassicPlayerUiEffect {
    data class PlayDiceRollSound(val playerId: Int) : ClassicPlayerUiEffect
    object PlayVictoryFanfare : ClassicPlayerUiEffect
    data class AnimateDiceRoll(val playerId: Int, val durationMs: Long = 1200L) : ClassicPlayerUiEffect
    data class AnimateAllDiceRoll(val durationMs: Long = 1200L) : ClassicPlayerUiEffect
}

sealed interface ClassicPlayerIntent {
    data class SetMode(val mode: PlayerMode) : ClassicPlayerIntent
    data class ChangeDiceCount(val count: Int) : ClassicPlayerIntent
    data class RollForPlayer(val playerId: Int) : ClassicPlayerIntent
    object RollAll : ClassicPlayerIntent
    data class SetFaceStyle(val style: DiceFaceStyle) : ClassicPlayerIntent
    data class SetTheme(val diceColor: String, val dotColor: String) : ClassicPlayerIntent
    data class SyncConfiguration(
        val diceType: DiceType,
        val customMin: Int,
        val customMax: Int,
        val faceStyle: DiceFaceStyle? = null
    ) : ClassicPlayerIntent
    object ResetRound : ClassicPlayerIntent
}
