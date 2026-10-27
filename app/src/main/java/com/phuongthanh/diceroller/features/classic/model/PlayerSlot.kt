package com.phuongthanh.diceroller.features.classic.model

import androidx.annotation.ColorRes

data class PlayerSlot(
    val id: Int,
    val name: String,
    @get:ColorRes val colorRes: Int,
    val diceValues: List<Int> = listOf(1),
    val score: Int = 1,
    val isRolling: Boolean = false,
    val isLocked: Boolean = false,
    val isWinner: Boolean = false,
    val isLoser: Boolean = false,
    val isTie: Boolean = false
)
