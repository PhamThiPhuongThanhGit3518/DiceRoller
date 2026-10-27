package com.phuongthanh.diceroller.features.intro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class IntroSlide(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    @param:DrawableRes val illustrationRes: Int
)
