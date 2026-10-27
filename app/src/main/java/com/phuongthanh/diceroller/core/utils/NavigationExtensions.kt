package com.phuongthanh.diceroller.core.utils

import androidx.navigation.NavController
import androidx.navigation.NavDirections

fun NavController.safeNavigate(directions: NavDirections) {
    val currentDest = currentDestination ?: return
    val action = currentDest.getAction(directions.actionId)
    if (action != null) {
        navigate(directions)
    }
}

fun NavController.safeNavigate(resId: Int) {
    val currentDest = currentDestination ?: return
    val action = currentDest.getAction(resId)
    if (action != null || currentDest.id != resId) {
        navigate(resId)
    }
}

fun NavController.safeNavigate(resId: Int, args: android.os.Bundle) {
    val currentDest = currentDestination ?: return
    val action = currentDest.getAction(resId)
    if (action != null || currentDest.id != resId) {
        navigate(resId, args)
    }
}
