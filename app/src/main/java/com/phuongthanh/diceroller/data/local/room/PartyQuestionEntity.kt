package com.phuongthanh.diceroller.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "party_questions")
data class PartyQuestionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "TRUTH" or "DARE"
    val text: String,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
