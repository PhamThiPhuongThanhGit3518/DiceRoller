package com.phuongthanh.diceroller.features.freeroll.model

import androidx.annotation.StringRes
import com.phuongthanh.diceroller.R

enum class DiceType(
    val code: String,
    val minValue: Int,
    val maxValue: Int,
    @param:StringRes val displayNameRes: Int
) {
    D4("D4", 1, 4, R.string.dice_d4),
    D6("D6", 1, 6, R.string.dice_d6),
    D8("D8", 1, 8, R.string.dice_d8),
    D10("D10", 1, 10, R.string.dice_d10),
    D12("D12", 1, 12, R.string.dice_d12),
    D20("D20", 1, 20, R.string.dice_d20),
    D100("D100", 1, 100, R.string.dice_d100),
    CUSTOM("CUSTOM", 1, 100, R.string.dice_custom);

    val isGreaterThanD20: Boolean
        get() = this == D100 || this == CUSTOM

    val isTileMode: Boolean
        get() = this == D100 || this == CUSTOM
}
