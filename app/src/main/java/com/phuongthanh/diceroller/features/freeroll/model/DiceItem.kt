package com.phuongthanh.diceroller.features.freeroll.model

import com.phuongthanh.diceroller.core.model.DiceFaceStyle

data class DiceItem(
    val id: Int,
    val type: DiceType,
    val value: Int,
    val isLocked: Boolean = false,
    val rotationAngle: Float = 0f,
    val customMin: Int = 1,
    val customMax: Int = 100,
    val faceStyle: DiceFaceStyle = DiceFaceStyle.DOTS
) {
    fun rollNewValue(): Int {
        return if (type == DiceType.CUSTOM) {
            val low = minOf(customMin, customMax)
            val high = maxOf(customMin, customMax)
            kotlin.random.Random.nextInt(low, high + 1)
        } else {
            kotlin.random.Random.nextInt(type.minValue, type.maxValue + 1)
        }
    }

    val isGreaterThanD20: Boolean
        get() = type == DiceType.D100 || type == DiceType.CUSTOM

    val isTileMode: Boolean
        get() = type == DiceType.D100 || type == DiceType.CUSTOM
}
