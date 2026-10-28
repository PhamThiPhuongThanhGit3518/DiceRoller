package com.phuongthanh.diceroller.core.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.features.roulette.model.WheelSector
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class LuckyWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onSectorTickListener: (() -> Unit)? = null
    var onCenterSpinClickListener: (() -> Unit)? = null

    private var sectors: List<WheelSector> = emptyList()
    private var currentAngle: Float = 0f
    private var lastCrossedSectorIndex: Int = -1
    private var pointerBounceAngle: Float = 0f
    private var isSpinning: Boolean = false

    private var spinAnimator: ValueAnimator? = null

    // Paints
    private val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val outerRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val centerHubBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val sectorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    fun setSectors(items: List<WheelSector>) {
        this.sectors = items
        invalidate()
    }

    fun spinTo(targetAngle: Float, onFinished: () -> Unit) {
        if (isSpinning || sectors.isEmpty()) return
        isSpinning = true
        spinAnimator?.cancel()

        lastCrossedSectorIndex = (currentAngle / (360f / sectors.size)).toInt()

        spinAnimator = ValueAnimator.ofFloat(currentAngle, targetAngle).apply {
            duration = 3800L
            interpolator = DecelerateInterpolator(2.5f)
            addUpdateListener { animator ->
                currentAngle = animator.animatedValue as Float
                val sweep = 360f / sectors.size
                val crossedIndex = (currentAngle / sweep).toInt()
                if (crossedIndex != lastCrossedSectorIndex) {
                    lastCrossedSectorIndex = crossedIndex
                    pointerBounceAngle = -14f
                    onSectorTickListener?.invoke()
                } else {
                    pointerBounceAngle *= 0.85f
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isSpinning = false
                    pointerBounceAngle = 0f
                    invalidate()
                    onFinished()
                }
            })
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isSpinning) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val cx = width / 2f
                val cy = height / 2f
                val dx = event.x - cx
                val dy = event.y - cy
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                val hubRadius = min(width, height) * 0.16f
                if (dist <= hubRadius) {
                    performClick()
                    onCenterSpinClickListener?.invoke()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (sectors.isEmpty() || width <= 0 || height <= 0) return

        val count = sectors.size
        val sweepAngle = 360f / count
        val cx = width / 2f
        val cy = height / 2f
        val diameter = min(width, height) * 0.88f
        val radius = diameter / 2f
        val wheelRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // Draw sectors with rotation
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(currentAngle)

        val localRect = RectF(-radius, -radius, radius, radius)
        for (i in 0 until count) {
            val sector = sectors[i]
            val startAngle = i * sweepAngle

            try {
                sectorPaint.color = Color.parseColor(sector.colorHex)
            } catch (_: Exception) {
                sectorPaint.color = Color.parseColor("#3B82F6")
            }

            canvas.drawArc(localRect, startAngle, sweepAngle, true, sectorPaint)

            // Draw white divider line
            val rad = Math.toRadians(startAngle.toDouble())
            canvas.drawLine(0f, 0f, (radius * cos(rad)).toFloat(), (radius * sin(rad)).toFloat(), dividerPaint)

            // Draw Sector Text
            canvas.save()
            canvas.rotate(startAngle + sweepAngle / 2f)

            sectorTextPaint.textSize = (radius * 0.11f).coerceIn(24f, 40f)
            val displayText = if (sector.text.length > 10) sector.text.take(9) + "…" else sector.text
            val textX = radius * 0.82f
            val fontMetrics = sectorTextPaint.fontMetrics
            val textY = -(fontMetrics.ascent + fontMetrics.descent) / 2f
            canvas.drawText(displayText, textX, textY, sectorTextPaint)

            canvas.restore()
        }

        // Draw Outer Rim
        canvas.drawCircle(0f, 0f, radius, outerRimPaint)

        canvas.restore()

        // Draw Center Hub (Solid white circle matching Figma)
        val hubRadius = radius * 0.22f
        canvas.drawCircle(cx, cy, hubRadius, centerHubBgPaint)

        // Draw Pointer at Top (12 o'clock pointing downwards)
        drawTopPointer(canvas, cx, cy - radius - 2f)
    }

    private fun drawTopPointer(canvas: Canvas, px: Float, py: Float) {
        canvas.save()
        canvas.translate(px, py)
        canvas.rotate(pointerBounceAngle)

        val path = Path()
        val pointerW = 38f
        val pointerH = 30f

        // Upside-down white triangle pointing down into the wheel
        path.moveTo(0f, pointerH) // tip pointing down
        path.lineTo(-pointerW / 2f, 0f) // top left
        path.lineTo(pointerW / 2f, 0f) // top right
        path.close()

        canvas.drawPath(path, pointerPaint)
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        spinAnimator?.cancel()
    }
}
