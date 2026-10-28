package com.phuongthanh.diceroller.core.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View

/**
 * A custom [Drawable] that renders a gradient border (stroke) with a transparent or customizable fill,
 * supporting multi-stop linear gradients and inner shadow.
 */
class GradientBorderDrawable(
    private var strokeWidthPx: Float,
    private var cornerRadiusPx: Float,
    private var startColor: Int = Color.parseColor("#00CAFB"),
    private var endColor: Int = Color.parseColor("#0075FB"),
    private var fillColor: Int = Color.TRANSPARENT,
    private var orientation: Orientation = Orientation.LEFT_RIGHT,
    private var gradientColors: IntArray? = null,
    private var gradientPositions: FloatArray? = null,
    private var shadowColor: Int = Color.TRANSPARENT,
    private var shadowDxPx: Float = 0f,
    private var shadowDyPx: Float = 0f,
    private var shadowBlurPx: Float = 0f
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
    private var shadowBitmap: Bitmap? = null

    fun update(
        strokeWidthPx: Float = this.strokeWidthPx,
        cornerRadiusPx: Float = this.cornerRadiusPx,
        startColor: Int = this.startColor,
        endColor: Int = this.endColor,
        fillColor: Int = this.fillColor,
        orientation: Orientation = this.orientation,
        gradientColors: IntArray? = this.gradientColors,
        gradientPositions: FloatArray? = this.gradientPositions,
        shadowColor: Int = this.shadowColor,
        shadowDxPx: Float = this.shadowDxPx,
        shadowDyPx: Float = this.shadowDyPx,
        shadowBlurPx: Float = this.shadowBlurPx
    ) {
        this.strokeWidthPx = strokeWidthPx
        this.cornerRadiusPx = cornerRadiusPx
        this.startColor = startColor
        this.endColor = endColor
        this.fillColor = fillColor
        this.orientation = orientation
        this.gradientColors = gradientColors
        this.gradientPositions = gradientPositions
        this.shadowColor = shadowColor
        this.shadowDxPx = shadowDxPx
        this.shadowDyPx = shadowDyPx
        this.shadowBlurPx = shadowBlurPx

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

        val colors = gradientColors
        val positions = gradientPositions
        strokePaint.shader = if (colors != null && colors.size >= 2) {
            LinearGradient(x0, y0, x1, y1, colors, positions, Shader.TileMode.CLAMP)
        } else {
            LinearGradient(x0, y0, x1, y1, startColor, endColor, Shader.TileMode.CLAMP)
        }

        generateShadowBitmap(bounds.width(), bounds.height())
    }

    private fun generateShadowBitmap(width: Int, height: Int) {
        if (width <= 0 || height <= 0 || shadowColor == Color.TRANSPARENT || Color.alpha(shadowColor) == 0 || shadowBlurPx <= 0f) {
            shadowBitmap?.recycle()
            shadowBitmap = null
            return
        }

        try {
            val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val c = Canvas(bm)

            val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            val clipPath = Path().apply {
                addRoundRect(rect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW)
            }
            c.clipPath(clipPath)

            val margin = shadowBlurPx * 2.5f
            val outerRect = RectF(-margin, -margin, width.toFloat() + margin, height.toFloat() + margin)

            val shadowCasterPath = Path().apply {
                fillType = Path.FillType.EVEN_ODD
                addRect(outerRect, Path.Direction.CW)
                addRoundRect(rect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW)
            }

            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = shadowColor
                style = Paint.Style.FILL
                maskFilter = BlurMaskFilter(shadowBlurPx, BlurMaskFilter.Blur.NORMAL)
            }

            c.save()
            c.translate(shadowDxPx, shadowDyPx)
            c.drawPath(shadowCasterPath, shadowPaint)
            c.restore()

            shadowBitmap?.recycle()
            shadowBitmap = bm
        } catch (_: Exception) {
            shadowBitmap = null
        }
    }

    override fun draw(canvas: Canvas) {
        // 1. Draw fill background
        if (fillColor != Color.TRANSPARENT && Color.alpha(fillColor) > 0) {
            canvas.drawRoundRect(fillRect, cornerRadiusPx, cornerRadiusPx, fillPaint)
        }

        // 2. Draw cached inner shadow
        shadowBitmap?.let { bm ->
            if (!bm.isRecycled) {
                canvas.drawBitmap(bm, bounds.left.toFloat(), bounds.top.toFloat(), null)
            }
        }

        // 3. Draw gradient border stroke
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
            orientation: Orientation = Orientation.LEFT_RIGHT,
            gradientColors: IntArray? = null,
            gradientPositions: FloatArray? = null,
            shadowColor: Int = Color.TRANSPARENT,
            shadowDxDp: Float = 0f,
            shadowDyDp: Float = 0f,
            shadowBlurDp: Float = 0f
        ): GradientBorderDrawable {
            val density = context.resources.displayMetrics.density
            return GradientBorderDrawable(
                strokeWidthPx = strokeWidthDp * density,
                cornerRadiusPx = cornerRadiusDp * density,
                startColor = startColor,
                endColor = endColor,
                fillColor = fillColor,
                orientation = orientation,
                gradientColors = gradientColors,
                gradientPositions = gradientPositions,
                shadowColor = shadowColor,
                shadowDxPx = shadowDxDp * density,
                shadowDyPx = shadowDyDp * density,
                shadowBlurPx = shadowBlurDp * density
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
