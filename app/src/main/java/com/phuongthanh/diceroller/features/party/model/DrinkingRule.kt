package com.phuongthanh.diceroller.features.party.model

import androidx.annotation.StringRes
import com.phuongthanh.diceroller.R

data class DrinkingRule(
    val faceIndex: Int = 0,
    @param:StringRes val titleRes: Int = 0,
    val iconEmoji: String = "🍺",
    val colorHex: String = "#EF4444",
    val id: String = "",
    val title: String = ""
)

object DrinkingRulesBank {
    val rules = listOf(
        DrinkingRule(1, R.string.drinking_rule_1, "🍺", "#EF4444"),
        DrinkingRule(2, R.string.drinking_rule_2, "🍻", "#F59E0B"),
        DrinkingRule(3, R.string.drinking_rule_3, "👉", "#3B82F6"),
        DrinkingRule(4, R.string.drinking_rule_4, "🥂", "#8B5CF6"),
        DrinkingRule(5, R.string.drinking_rule_5, "🤝", "#EC4899"),
        DrinkingRule(6, R.string.drinking_rule_6, "🍀", "#10B981")
    )
}
