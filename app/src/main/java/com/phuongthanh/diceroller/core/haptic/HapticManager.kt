package com.phuongthanh.diceroller.core.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var isEnabled: Boolean = true
    private var intensityLevel: Int = 1 // 0: subtle, 1: medium, 2: strong

    fun setEnabled(enabled: Boolean) {
        this.isEnabled = enabled
    }

    fun setIntensityLevel(level: Int) {
        this.intensityLevel = level.coerceIn(0, 2)
    }

    fun performDiceBounce() {
        if (!isEnabled || vibrator == null || !vibrator.hasVibrator()) return
        val (duration, amplitude) = when (intensityLevel) {
            0 -> 15L to 40
            1 -> 25L to 100
            else -> 40L to 180
        }
        vibrate(duration, amplitude)
    }

    fun performWheelTick() {
        if (!isEnabled || vibrator == null || !vibrator.hasVibrator()) return
        val (duration, amplitude) = when (intensityLevel) {
            0 -> 8L to 30
            1 -> 15L to 70
            else -> 25L to 140
        }
        vibrate(duration, amplitude)
    }

    fun performVictory() {
        if (!isEnabled || vibrator == null || !vibrator.hasVibrator()) return
        vibrate(80L, 220)
    }

    private fun vibrate(duration: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val clampedAmplitude = amplitude.coerceIn(1, 255)
            val effect = VibrationEffect.createOneShot(duration, clampedAmplitude)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
}
