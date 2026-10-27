package com.phuongthanh.diceroller.features.settings.model

data class RollHistoryItem(
    val id: String,
    val mode: String,
    val totalScore: Int,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
