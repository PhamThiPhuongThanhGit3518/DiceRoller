package com.phuongthanh.diceroller.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.abs
import kotlin.math.sqrt

class ShakeDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var onShakeListener: (() -> Unit)? = null
    private var isListening: Boolean = false
    private var lastShakeTimestamp: Long = 0L

    private var sensitivityLevel: Int = 1 // 0: low, 1: medium, 2: high

    fun setSensitivityLevel(level: Int) {
        this.sensitivityLevel = level.coerceIn(0, 2)
    }

    private fun getThreshold(): Float {
        return when (sensitivityLevel) {
            0 -> 10.0f // Low sensitivity: firm shake (~1.0G above earth gravity)
            1 -> 6.5f  // Medium sensitivity: natural shake (~0.65G above earth gravity)
            else -> 4.0f // High sensitivity: gentle shake (~0.4G above earth gravity)
        }
    }

    fun startListening(onShake: () -> Unit) {
        this.onShakeListener = onShake
        if (isListening || accelerometer == null || sensorManager == null) return
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        onShakeListener = null
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val totalAcc = sqrt(x * x + y * y + z * z)
        val netAcc = abs(totalAcc - SensorManager.GRAVITY_EARTH)

        if (netAcc > getThreshold()) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastShakeTimestamp > 600L) { // 600ms debounce
                lastShakeTimestamp = now
                onShakeListener?.invoke()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
