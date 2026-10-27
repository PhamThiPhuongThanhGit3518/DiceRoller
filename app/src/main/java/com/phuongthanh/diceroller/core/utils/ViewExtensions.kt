package com.phuongthanh.diceroller.core.utils

import android.os.SystemClock
import android.view.View

fun View.setOnDebouncedClickListener(debounceTime: Long = 600L, onClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { v ->
        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            onClick(v)
        }
    }
}
