package com.phuongthanh.diceroller.core.opengl.math

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Quaternion(
    var w: Float = 1f,
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {
    fun normalize(): Quaternion {
        val len = sqrt(w * w + x * x + y * y + z * z)
        return if (len > 0.00001f) {
            Quaternion(w / len, x / len, y / len, z / len)
        } else {
            IDENTITY
        }
    }

    operator fun times(q: Quaternion): Quaternion {
        val nw = w * q.w - x * q.x - y * q.y - z * q.z
        val nx = w * q.x + x * q.w + y * q.z - z * q.y
        val ny = w * q.y - x * q.z + y * q.w + z * q.x
        val nz = w * q.z + x * q.y - y * q.x + z * q.w
        return Quaternion(nw, nx, ny, nz)
    }

    fun rotate(v: Vector3): Vector3 {
        // v' = q * (0, v) * q^-1
        val qv = Quaternion(0f, v.x, v.y, v.z)
        val qInv = Quaternion(w, -x, -y, -z)
        val res = (this * qv) * qInv
        return Vector3(res.x, res.y, res.z)
    }

    fun toMatrix(m: FloatArray, offset: Int = 0) {
        val x2 = x * x
        val y2 = y * y
        val z2 = z * z
        val xy = x * y
        val xz = x * z
        val yz = y * z
        val wx = w * x
        val wy = w * y
        val wz = w * z

        // Column 0
        m[offset + 0] = 1f - 2f * (y2 + z2)
        m[offset + 1] = 2f * (xy + wz)
        m[offset + 2] = 2f * (xz - wy)
        m[offset + 3] = 0f

        // Column 1
        m[offset + 4] = 2f * (xy - wz)
        m[offset + 5] = 1f - 2f * (x2 + z2)
        m[offset + 6] = 2f * (yz + wx)
        m[offset + 7] = 0f

        // Column 2
        m[offset + 8] = 2f * (xz + wy)
        m[offset + 9] = 2f * (yz - wx)
        m[offset + 10] = 1f - 2f * (x2 + y2)
        m[offset + 11] = 0f

        // Column 3
        m[offset + 12] = 0f
        m[offset + 13] = 0f
        m[offset + 14] = 0f
        m[offset + 15] = 1f
    }

    companion object {
        val IDENTITY = Quaternion(1f, 0f, 0f, 0f)

        fun fromAxisAngle(axis: Vector3, angleRadians: Float): Quaternion {
            val halfAngle = angleRadians * 0.5f
            val s = sin(halfAngle)
            val normAxis = axis.normalize()
            return Quaternion(
                cos(halfAngle),
                normAxis.x * s,
                normAxis.y * s,
                normAxis.z * s
            ).normalize()
        }

        fun rotationBetween(from: Vector3, to: Vector3): Quaternion {
            val uFrom = from.normalize()
            val uTo = to.normalize()
            val dot = uFrom.dot(uTo)

            if (dot >= 0.99999f) {
                return IDENTITY
            }
            if (dot <= -0.99999f) {
                // 180 degree rotation around arbitrary orthogonal axis
                var ortho = Vector3.UNIT_X.cross(uFrom)
                if (ortho.length() < 0.01f) {
                    ortho = Vector3.UNIT_Y.cross(uFrom)
                }
                return fromAxisAngle(ortho, Math.PI.toFloat())
            }

            val cross = uFrom.cross(uTo)
            val w = 1f + dot
            return Quaternion(w, cross.x, cross.y, cross.z).normalize()
        }

        fun slerp(q1: Quaternion, q2: Quaternion, t: Float): Quaternion {
            var cosHalfTheta = q1.w * q2.w + q1.x * q2.x + q1.y * q2.y + q1.z * q2.z

            var targetQ = q2
            if (cosHalfTheta < 0f) {
                targetQ = Quaternion(-q2.w, -q2.x, -q2.y, -q2.z)
                cosHalfTheta = -cosHalfTheta
            }

            if (cosHalfTheta > 0.9995f) {
                // Very close: linearly interpolate to avoid division by zero
                val nw = q1.w + t * (targetQ.w - q1.w)
                val nx = q1.x + t * (targetQ.x - q1.x)
                val ny = q1.y + t * (targetQ.y - q1.y)
                val nz = q1.z + t * (targetQ.z - q1.z)
                return Quaternion(nw, nx, ny, nz).normalize()
            }

            val halfTheta = acos(cosHalfTheta)
            val sinHalfTheta = sqrt(1f - cosHalfTheta * cosHalfTheta)

            val ratioA = sin((1f - t) * halfTheta) / sinHalfTheta
            val ratioB = sin(t * halfTheta) / sinHalfTheta

            val nw = q1.w * ratioA + targetQ.w * ratioB
            val nx = q1.x * ratioA + targetQ.x * ratioB
            val ny = q1.y * ratioA + targetQ.y * ratioB
            val nz = q1.z * ratioA + targetQ.z * ratioB
            return Quaternion(nw, nx, ny, nz).normalize()
        }
    }
}
