package com.phuongthanh.diceroller.core.model

enum class DiceFaceStyle(val code: String) {
    DOTS("dots"),
    NUMBERS("numbers");

    companion object {
        fun fromCode(code: String?): DiceFaceStyle {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: DOTS
        }
    }
}
