package com.phuongthanh.diceroller.features.party.model

data class ScoreboardPlayer(
    val id: String,
    val name: String,
    val penalties: Int = 0,
    val isTopPenalized: Boolean = false
)
