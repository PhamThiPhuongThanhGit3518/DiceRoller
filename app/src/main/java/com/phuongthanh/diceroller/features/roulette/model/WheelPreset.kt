package com.phuongthanh.diceroller.features.roulette.model

data class WheelPreset(
    val id: String,
    val title: String,
    val sectors: List<WheelSector>
)
