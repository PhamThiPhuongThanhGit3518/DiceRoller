package com.phuongthanh.diceroller.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View

/**
 * A custom [Drawable] that renders a gradient border (stroke) with a transparent or customizable fill.
 * Supports rounded corners and customizable gradient directions.
 */
class GradientBorderDrawable(
    private var strokeWidthPx: Float,
    private var cornerRadiusPx: Float,
    private var startColor: Int = Color.parseColor("#00CAFB"),
    private var endColor: Int = Color.parseColor("#0075FB"),
    private var fillColor: Int = Color.TRANSPARENT,
    private var orientation: Orientation = Orientation.LEFT_RIGHT
) : Drawable() {

    enum class Orientation {
        LEFT_RIGHT,
        TOP_BOTTOM,
        TOP_LEFT_BOTTOM_RIGHT
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = fillColor
    }

    private val strokeRect = RectF()
    private val fillRect = RectF()

    fun update(
        strokeWidthPx: Float = this.strokeWidthPx,
        cornerRadiusPx: Float = this.cornerRadiusPx,
        startColor: Int = this.startColor,
        endColor: Int = this.endColor,
        fillColor: Int = this.fillColor,
        orientation: Orientation = this.orientation
    ) {
        this.strokeWidthPx = strokeWidthPx
        this.cornerRadiusPx = cornerRadiusPx
        this.startColor = startColor
        this.endColor = endColor
        this.fillColor = fillColor
        this.orientation = orientation

        strokePaint.strokeWidth = strokeWidthPx
        fillPaint.color = fillColor

        updateBounds(bounds)
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateBounds(bounds)
    }

    private fun updateBounds(bounds: Rect) {
        if (bounds.isEmpty) return

        val halfStroke = strokeWidthPx / 2f
        strokeRect.set(
            bounds.left + halfStroke,
            bounds.top + halfStroke,
            bounds.right - halfStroke,
            bounds.bottom - halfStroke
        )

        fillRect.set(bounds)

        val (x0, y0, x1, y1) = when (orientation) {
            Orientation.LEFT_RIGHT -> listOf(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.top.toFloat())
            Orientation.TOP_BOTTOM -> listOf(bounds.left.toFloat(), bounds.top.toFloat(), bounds.left.toFloat(), bounds.bottom.toFloat())
            Orientation.TOP_LEFT_BOTTOM_RIGHT -> listOf(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat())
        }

        strokePaint.shader = LinearGradient(
            x0, y0, x1, y1,
            startColor, endColor,
            Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas) {
        if (fillColor != Color.TRANSPARENT && Color.alpha(fillColor) > 0) {
            canvas.drawRoundRect(fillRect, cornerRadiusPx, cornerRadiusPx, fillPaint)
        }
        if (strokeWidthPx > 0f) {
            canvas.drawRoundRect(strokeRect, cornerRadiusPx, cornerRadiusPx, strokePaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        strokePaint.alpha = alpha
        fillPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        strokePaint.colorFilter = colorFilter
        fillPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    companion object {
        fun create(
            context: Context,
            strokeWidthDp: Float = 1.5f,
            cornerRadiusDp: Float = 12f,
            startColor: Int = Color.parseColor("#00CAFB"),
            endColor: Int = Color.parseColor("#0075FB"),
            fillColor: Int = Color.TRANSPARENT,
            orientation: Orientation = Orientation.LEFT_RIGHT
        ): GradientBorderDrawable {
            val density = context.resources.displayMetrics.density
            return GradientBorderDrawable(
                strokeWidthPx = strokeWidthDp * density,
                cornerRadiusPx = cornerRadiusDp * density,
                startColor = startColor,
                endColor = endColor,
                fillColor = fillColor,
                orientation = orientation
            )
        }
    }
}

/**
 * Extension helper to set a gradient border with transparent/custom background on any View.
 */
fun View.setGradientBorder(
    strokeWidthDp: Float = 1.5f,
    cornerRadiusDp: Float = 12f,
    startColor: Int = Color.parseColor("#00CAFB"),
    endColor: Int = Color.parseColor("#0075FB"),
    fillColor: Int = Color.TRANSPARENT,
    orientation: GradientBorderDrawable.Orientation = GradientBorderDrawable.Orientation.LEFT_RIGHT
) {
    background = GradientBorderDrawable.create(
        context = context,
        strokeWidthDp = strokeWidthDp,
        cornerRadiusDp = cornerRadiusDp,
        startColor = startColor,
        endColor = endColor,
        fillColor = fillColor,
        orientation = orientation
    )
}
