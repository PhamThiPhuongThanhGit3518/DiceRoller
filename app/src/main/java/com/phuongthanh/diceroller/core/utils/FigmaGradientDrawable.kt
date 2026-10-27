package com.phuongthanh.diceroller.core.utils

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Custom Drawable to accurately render Figma Linear Gradient fills, glowing neon halo borders,
 * and realistic Gaussian blurred drop shadows, respecting rectangular aspect ratio and arbitrary color stops.
 */
class FigmaGradientDrawable(
    private val cornerRadius: Float,
    private val bgColors: IntArray,
    private val bgPositions: FloatArray? = null,
    private val bgStartNorm: PointF = PointF(0f, 0f),
    private val bgEndNorm: PointF = PointF(1f, 1f),
    private val bgHandle2Norm: PointF? = null,
    private val strokeWidth: Float = 0f,
    private val strokeColors: IntArray? = null,
    private val strokePositions: FloatArray? = null,
    private val strokeStartNorm: PointF = PointF(0.5f, 0f),
    private val strokeEndNorm: PointF = PointF(0.5f, 1f),
    // Glow & Shadow parameters
    private val insets: RectF = RectF(0f, 0f, 0f, 0f),
    private val shadowRadius: Float = 0f,
    private val shadowColor: Int = 0,
    private val shadowOffsetY: Float = 0f,
    private val haloWidth: Float = 0f,
    private val haloBlurRadius: Float = 0f,
    private val haloColors: IntArray? = null
) : Drawable() {

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val cardRect = RectF()
    private val shadowRect = RectF()
    private val strokeRect = RectF()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        if (bounds.width() <= 0 || bounds.height() <= 0) return

        cardRect.set(
            bounds.left + insets.left,
            bounds.top + insets.top,
            bounds.right - insets.right,
            bounds.bottom - insets.bottom
        )

        val cardW = cardRect.width()
        val cardH = cardRect.height()
        if (cardW <= 0f || cardH <= 0f) return

        // 1. Drop shadow glow
        if (shadowRadius > 0f && shadowColor != 0) {
            shadowPaint.color = shadowColor
            shadowPaint.maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL)
            shadowRect.set(cardRect)
            shadowRect.offset(0f, shadowOffsetY)
        }

        // 2. Halo glow around border
        if (haloBlurRadius > 0f && haloColors != null) {
            haloPaint.strokeWidth = haloWidth
            haloPaint.maskFilter = BlurMaskFilter(haloBlurRadius, BlurMaskFilter.Blur.NORMAL)
            haloPaint.shader = LinearGradient(
                cardRect.centerX(), cardRect.top,
                cardRect.centerX(), cardRect.bottom,
                haloColors,
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        // 3. Card fill
        if (bgHandle2Norm != null) {
            val p0x = cardRect.left + bgStartNorm.x * cardW
            val p0y = cardRect.top + bgStartNorm.y * cardH
            val p1x = cardRect.left + bgEndNorm.x * cardW
            val p1y = cardRect.top + bgEndNorm.y * cardH
            val p2x = cardRect.left + bgHandle2Norm.x * cardW
            val p2y = cardRect.top + bgHandle2Norm.y * cardH

            val v1x = p1x - p0x
            val v1y = p1y - p0y
            val v2x = p2x - p0x
            val v2y = p2y - p0y

            val shader = LinearGradient(
                0f, 0f, 1f, 0f,
                bgColors,
                bgPositions,
                Shader.TileMode.CLAMP
            )
            val matrix = Matrix().apply {
                setValues(floatArrayOf(
                    v1x, v2x, p0x,
                    v1y, v2y, p0y,
                    0f,  0f,  1f
                ))
            }
            shader.setLocalMatrix(matrix)
            bgPaint.shader = shader
        } else {
            val x0 = cardRect.left + bgStartNorm.x * cardW
            val y0 = cardRect.top + bgStartNorm.y * cardH
            val x1 = cardRect.left + bgEndNorm.x * cardW
            val y1 = cardRect.top + bgEndNorm.y * cardH

            bgPaint.shader = LinearGradient(
                x0, y0, x1, y1,
                bgColors,
                bgPositions,
                Shader.TileMode.CLAMP
            )
        }

        // 4. Crisp stroke
        if (strokeWidth > 0f && strokeColors != null) {
            strokePaint.strokeWidth = strokeWidth
            val halfStroke = strokeWidth / 2f
            strokeRect.set(
                cardRect.left + halfStroke,
                cardRect.top + halfStroke,
                cardRect.right - halfStroke,
                cardRect.bottom - halfStroke
            )

            val sx0 = cardRect.left + strokeStartNorm.x * cardW
            val sy0 = cardRect.top + strokeStartNorm.y * cardH
            val sx1 = cardRect.left + strokeEndNorm.x * cardW
            val sy1 = cardRect.top + strokeEndNorm.y * cardH

            strokePaint.shader = LinearGradient(
                sx0, sy0, sx1, sy1,
                strokeColors,
                strokePositions,
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun draw(canvas: Canvas) {
        if (cardRect.isEmpty) return

        // Step 1: Draw drop shadow glow
        if (shadowRadius > 0f && shadowColor != 0) {
            canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)
        }

        // Step 2: Draw glowing neon halo around border
        if (haloBlurRadius > 0f && haloColors != null) {
            canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, haloPaint)
        }

        // Step 3: Draw card body fill
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)

        // Step 4: Draw crisp border line
        if (strokeWidth > 0f && strokeColors != null) {
            val innerRadius = (cornerRadius - strokeWidth / 2f).coerceAtLeast(0f)
            canvas.drawRoundRect(strokeRect, innerRadius, innerRadius, strokePaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        shadowPaint.alpha = alpha
        haloPaint.alpha = alpha
        bgPaint.alpha = alpha
        strokePaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        bgPaint.colorFilter = colorFilter
        strokePaint.colorFilter = colorFilter
        haloPaint.colorFilter = colorFilter
        shadowPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getOutline(outline: Outline) {
        if (!cardRect.isEmpty) {
            outline.setRoundRect(
                cardRect.left.toInt(),
                cardRect.top.toInt(),
                cardRect.right.toInt(),
                cardRect.bottom.toInt(),
                cornerRadius
            )
        } else {
            outline.setRoundRect(bounds, cornerRadius)
        }
    }
}
