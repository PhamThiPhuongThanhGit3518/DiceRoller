package com.phuongthanh.diceroller.core.opengl.math

import kotlin.math.acos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {
    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vector3 {
        val len = length()
        return if (len > 0.00001f) {
            Vector3(x / len, y / len, z / len)
        } else {
            Vector3(0f, 0f, 1f)
        }
    }

    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)

    operator fun times(scalar: Float): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun slerp(other: Vector3, t: Float): Vector3 {
        val dotVal = dot(other).coerceIn(-1f, 1f)
        val theta = acos(dotVal)
        if (theta < 0.0001f) {
            return (this * (1f - t) + other * t).normalize()
        }
        val sinTheta = sin(theta)
        val w0 = sin((1f - t) * theta) / sinTheta
        val w1 = sin(t * theta) / sinTheta
        return this * w0 + other * w1
    }

    companion object {
        val ZERO = Vector3(0f, 0f, 0f)
        val UNIT_X = Vector3(1f, 0f, 0f)
        val UNIT_Y = Vector3(0f, 1f, 0f)
        val UNIT_Z = Vector3(0f, 0f, 1f)
    }
}
