package com.phuongthanh.diceroller.core.opengl.model

import com.phuongthanh.diceroller.core.opengl.geometry.PolyhedralGeometryFactory
import com.phuongthanh.diceroller.core.opengl.math.Quaternion
import com.phuongthanh.diceroller.core.opengl.math.Vector3
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

class Dice3DState(
    val id: Int,
    var type: DiceType = DiceType.D6,
    var value: Int = 1,
    var isLocked: Boolean = false
) {
    var currentRotation: Quaternion = Quaternion.IDENTITY
    var isRolling: Boolean = false

    private var startRotation: Quaternion = Quaternion.IDENTITY
    private var targetRotation: Quaternion = Quaternion.IDENTITY
    private var rollAxis: Vector3 = Vector3.UNIT_X
    private var wobbleAxis: Vector3 = Vector3.UNIT_Y
    private var totalSpinAngle: Float = 0f
    private var rollDurationSeconds: Float = 1.2f
    private var rollElapsedSeconds: Float = 0f

    init {
        snapToValue(value)
    }

    fun snapToValue(newValue: Int) {
        this.value = newValue
        val mesh = PolyhedralGeometryFactory.getMesh(type)
        val target = mesh.targetRotations[newValue] ?: Quaternion.IDENTITY
        currentRotation = target
        targetRotation = target
        startRotation = target
        isRolling = false
    }

    fun startRoll(newValue: Int, durationMs: Long = 1200L) {
        if (isLocked) return
        this.value = newValue
        this.rollDurationSeconds = (durationMs.toFloat() / 1000f).coerceAtLeast(0.5f)
        this.rollElapsedSeconds = 0f
        this.isRolling = true

        val mesh = PolyhedralGeometryFactory.getMesh(type)
        targetRotation = mesh.targetRotations[newValue] ?: Quaternion.IDENTITY
        startRotation = currentRotation

        // Relative quaternion from start to target: deltaQ = target * inv(start)
        val invStart = Quaternion(startRotation.w, -startRotation.x, -startRotation.y, -startRotation.z)
        var deltaQ = targetRotation * invStart

        // Ensure shortest geodesic path in quaternion sphere
        if (deltaQ.w < 0f) {
            deltaQ = Quaternion(-deltaQ.w, -deltaQ.x, -deltaQ.y, -deltaQ.z)
        }

        // Extract angle and axis of deltaQ
        val sinHalfAngle = sqrt(deltaQ.x * deltaQ.x + deltaQ.y * deltaQ.y + deltaQ.z * deltaQ.z)
        val baseAngle = 2f * atan2(sinHalfAngle, deltaQ.w.coerceIn(-1f, 1f))

        // Choose consistent forward roll axis
        val extractedAxis = if (sinHalfAngle > 0.001f) {
            Vector3(deltaQ.x / sinHalfAngle, deltaQ.y / sinHalfAngle, deltaQ.z / sinHalfAngle)
        } else {
            Vector3(1f, 0.45f, 0.25f).normalize()
        }

        rollAxis = extractedAxis

        // Complete 3 full revolutions (3 * 360 deg) plus the baseAngle to land on target face
        val extraSpins = 3
        totalSpinAngle = baseAngle + extraSpins * (2f * PI.toFloat())

        // Choose wobble axis orthogonal to rollAxis for realistic 3D tumbling in the air
        var tempOrtho = Vector3.UNIT_Y.cross(rollAxis)
        if (tempOrtho.length() < 0.1f) {
            tempOrtho = Vector3.UNIT_X.cross(rollAxis)
        }
        wobbleAxis = tempOrtho.normalize()
    }

    fun update(deltaSeconds: Float): Boolean {
        if (!isRolling) return false

        rollElapsedSeconds += deltaSeconds
        val rawProgress = (rollElapsedSeconds / rollDurationSeconds).coerceIn(0f, 1f)

        if (rawProgress >= 1f) {
            currentRotation = targetRotation
            isRolling = false
            return false
        }

        // Realistic friction deceleration: Ease-Out Cubic
        val p = rawProgress
        val easedP = 1f - (1f - p) * (1f - p) * (1f - p)

        // Monotonically forward angle in ONE continuous direction: NEVER reverses, NEVER jerks
        val currentSpinAngle = totalSpinAngle * easedP
        val qSpin = Quaternion.fromAxisAngle(rollAxis, currentSpinAngle)

        // Subtle 3D precession wobble that decays to 0 as the die lands on the table
        val wobbleAmplitude = (1f - p) * (1f - p) * 0.40f
        val wobbleAngle = sin(p * PI.toFloat() * 6f) * wobbleAmplitude
        val qWobble = Quaternion.fromAxisAngle(wobbleAxis, wobbleAngle)

        // Final orientation at time t
        currentRotation = ((qWobble * qSpin) * startRotation).normalize()

        return true
    }
}
