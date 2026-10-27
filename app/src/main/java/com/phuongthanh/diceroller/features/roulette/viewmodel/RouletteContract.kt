package com.phuongthanh.diceroller.features.roulette.viewmodel

import com.phuongthanh.diceroller.features.roulette.model.WheelPreset
import com.phuongthanh.diceroller.features.roulette.model.WheelSector

data class RouletteUiState(
    val presets: List<WheelPreset> = emptyList(),
    val currentPreset: WheelPreset? = null,
    val isSpinning: Boolean = false,
    val winnerSector: WheelSector? = null
)

sealed interface RouletteUiEffect {
    data class SpinToAngle(val targetAngle: Float, val winner: WheelSector) : RouletteUiEffect
    object PlayTickSoundAndHaptic : RouletteUiEffect
    object PlayWinnerFanfare : RouletteUiEffect
    data class ShowToast(val messageRes: Int, val formatArg: String? = null) : RouletteUiEffect
    data class OpenSpinScreen(val presetId: String) : RouletteUiEffect
}

sealed interface RouletteIntent {
    data class SelectPreset(val presetId: String) : RouletteIntent
    data class SavePreset(val preset: WheelPreset) : RouletteIntent
    data class DuplicatePreset(val presetId: String) : RouletteIntent
    data class DeletePreset(val presetId: String) : RouletteIntent
    data class UpdateSectors(val newSectors: List<WheelSector>) : RouletteIntent
    object Spin : RouletteIntent
    data class OnSpinCompleted(val winner: WheelSector) : RouletteIntent
    object PlayTick : RouletteIntent
}
