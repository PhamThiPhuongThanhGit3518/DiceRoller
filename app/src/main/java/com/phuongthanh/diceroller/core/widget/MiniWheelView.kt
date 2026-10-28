package com.phuongthanh.diceroller.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.features.roulette.model.WheelSector
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MiniWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var customSectors: List<WheelSector> = emptyList()

    private val defaultPalette = listOf(
        "#8B5CF6", "#2F80ED", "#06B6D4"
    )

    private val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val rimStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val centerHubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val wheelBounds = RectF()
    private val pointerPath = Path()

    fun setSectors(sectors: List<WheelSector>) {
        this.customSectors = sectors
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalW = width.toFloat()
        val totalH = height.toFloat()
        val diameter = min(totalW - paddingLeft - paddingRight, totalH - paddingTop - paddingBottom)
        if (diameter <= 0f) return

        val radius = diameter / 2f
        val cx = paddingLeft + (totalW - paddingLeft - paddingRight) / 2f
        val cy = paddingTop + (totalH - paddingTop - paddingBottom) / 2f

        val wheelRadius = radius - 4f
        wheelBounds.set(cx - wheelRadius, cy - wheelRadius, cx + wheelRadius, cy + wheelRadius)

        // Draw sectors
        val sectorCount = if (customSectors.size >= 2) customSectors.size else defaultPalette.size
        val sweepAngle = 360f / sectorCount

        for (i in 0 until sectorCount) {
            val colorHex = if (customSectors.size >= 2) {
                customSectors[i].colorHex
            } else {
                defaultPalette[i % defaultPalette.size]
            }

            try {
                sectorPaint.color = Color.parseColor(colorHex)
            } catch (_: Exception) {
                sectorPaint.color = Color.parseColor("#22B07D")
            }

            val startAngle = i * sweepAngle - 90f // Start from 12 o'clock
            canvas.drawArc(wheelBounds, startAngle, sweepAngle, true, sectorPaint)

            // Dividers
            val angleRad = Math.toRadians((startAngle).toDouble())
            val endX = cx + (wheelRadius * cos(angleRad)).toFloat()
            val endY = cy + (wheelRadius * sin(angleRad)).toFloat()
            canvas.drawLine(cx, cy, endX, endY, dividerPaint)
        }

        // Outer rim
        canvas.drawCircle(cx, cy, wheelRadius, rimStrokePaint)

        // Center hub (solid white circle)
        val hubRadius = wheelRadius * 0.18f
        canvas.drawCircle(cx, cy, hubRadius, centerHubPaint)

        // Pointer triangle at top (12 o'clock pointing down into wheel)
        val pointerH = wheelRadius * 0.32f
        val pointerHalfW = pointerH * 0.5f
        val topY = cy - wheelRadius - 2f

        pointerPath.reset()
        pointerPath.moveTo(cx, cy - wheelRadius + pointerH * 0.7f) // Tip pointing downwards
        pointerPath.lineTo(cx - pointerHalfW, topY)               // Top left
        pointerPath.lineTo(cx + pointerHalfW, topY)               // Top right
        pointerPath.close()

        canvas.drawPath(pointerPath, pointerPaint)
    }
}
