package com.phuongthanh.diceroller.core.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.phuongthanh.diceroller.R

/**
 * A [LinearLayout] container with a customizable gradient border and transparent background.
 */
class GradientBorderLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val gradientDrawable: GradientBorderDrawable

    init {
        val density = context.resources.displayMetrics.density
        var strokeWidthPx = 1.5f * density
        var cornerRadiusPx = 12f * density
        var startColor = Color.parseColor("#00CAFB")
        var endColor = Color.parseColor("#0075FB")
        var fillColor = Color.TRANSPARENT
        var shadowColor = Color.TRANSPARENT
        var shadowDxPx = 0f
        var shadowDyPx = 0f
        var shadowBlurPx = 0f

        attrs?.let { attributeSet ->
            context.withStyledAttributes(attributeSet, R.styleable.GradientBorderLayout, defStyleAttr, 0) {
                strokeWidthPx = getDimension(R.styleable.GradientBorderLayout_gbStrokeWidth, strokeWidthPx)
                cornerRadiusPx = getDimension(R.styleable.GradientBorderLayout_gbCornerRadius, cornerRadiusPx)
                startColor = getColor(R.styleable.GradientBorderLayout_gbStartColor, startColor)
                endColor = getColor(R.styleable.GradientBorderLayout_gbEndColor, endColor)
                fillColor = getColor(R.styleable.GradientBorderLayout_gbFillColor, fillColor)
                shadowColor = getColor(R.styleable.GradientBorderLayout_gbInnerShadowColor, shadowColor)
                shadowDxPx = getDimension(R.styleable.GradientBorderLayout_gbInnerShadowDx, shadowDxPx)
                shadowDyPx = getDimension(R.styleable.GradientBorderLayout_gbInnerShadowDy, shadowDyPx)
                shadowBlurPx = getDimension(R.styleable.GradientBorderLayout_gbInnerShadowBlur, shadowBlurPx)
            }
        }

        gradientDrawable = GradientBorderDrawable(
            strokeWidthPx = strokeWidthPx,
            cornerRadiusPx = cornerRadiusPx,
            startColor = startColor,
            endColor = endColor,
            fillColor = fillColor,
            shadowColor = shadowColor,
            shadowDxPx = shadowDxPx,
            shadowDyPx = shadowDyPx,
            shadowBlurPx = shadowBlurPx
        )
        background = gradientDrawable
    }

    fun setGradientBorder(
        startColor: Int,
        endColor: Int,
        strokeWidthDp: Float = 1.5f,
        cornerRadiusDp: Float = 12f,
        fillColor: Int = Color.TRANSPARENT,
        orientation: GradientBorderDrawable.Orientation = GradientBorderDrawable.Orientation.LEFT_RIGHT,
        shadowColor: Int = Color.TRANSPARENT,
        shadowDxDp: Float = 0f,
        shadowDyDp: Float = 0f,
        shadowBlurDp: Float = 0f
    ) {
        val density = context.resources.displayMetrics.density
        gradientDrawable.update(
            strokeWidthPx = strokeWidthDp * density,
            cornerRadiusPx = cornerRadiusDp * density,
            startColor = startColor,
            endColor = endColor,
            fillColor = fillColor,
            orientation = orientation,
            gradientColors = null,
            gradientPositions = null,
            shadowColor = shadowColor,
            shadowDxPx = shadowDxDp * density,
            shadowDyPx = shadowDyDp * density,
            shadowBlurPx = shadowBlurDp * density
        )
    }

    fun setGradientBorder(
        colors: IntArray,
        positions: FloatArray? = null,
        strokeWidthDp: Float = 2f,
        cornerRadiusDp: Float = 20f,
        fillColor: Int = Color.TRANSPARENT,
        orientation: GradientBorderDrawable.Orientation = GradientBorderDrawable.Orientation.TOP_LEFT_BOTTOM_RIGHT,
        shadowColor: Int = Color.TRANSPARENT,
        shadowDxDp: Float = 0f,
        shadowDyDp: Float = 0f,
        shadowBlurDp: Float = 0f
    ) {
        val density = context.resources.displayMetrics.density
        gradientDrawable.update(
            strokeWidthPx = strokeWidthDp * density,
            cornerRadiusPx = cornerRadiusDp * density,
            fillColor = fillColor,
            orientation = orientation,
            gradientColors = colors,
            gradientPositions = positions,
            shadowColor = shadowColor,
            shadowDxPx = shadowDxDp * density,
            shadowDyPx = shadowDyDp * density,
            shadowBlurPx = shadowBlurDp * density
        )
    }
}
