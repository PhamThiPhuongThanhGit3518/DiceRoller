package com.phuongthanh.diceroller.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "drinking_rules")
data class DrinkingRuleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val iconEmoji: String,
    val colorHex: String,
    val isCustom: Boolean = false,
    val orderIndex: Int = 0
)
