package com.phuongthanh.diceroller.features.classic.model

import androidx.annotation.StringRes
import com.phuongthanh.diceroller.R

enum class PlayerMode(val count: Int, @param:StringRes val labelRes: Int) {
    ONE_PLAYER(1, R.string.mode_1p),
    TWO_PLAYERS(2, R.string.mode_2p),
    THREE_PLAYERS(3, R.string.mode_3p),
    FOUR_PLAYERS(4, R.string.mode_4p)
}
