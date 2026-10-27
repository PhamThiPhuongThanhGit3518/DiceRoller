package com.phuongthanh.diceroller.core.utils

import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import android.view.View
import androidx.core.content.ContextCompat
import com.phuongthanh.diceroller.R

fun View.setOnDebouncedClickListener(debounceTime: Long = 600L, onClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { v ->
        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            onClick(v)
        }
    }
}

/**
 * Dynamically applies a linear gradient background and optional gradient stroke
 * to any View, matching the true rectangular aspect ratio and arbitrary color stops.
 */
fun View.setFigmaCardBackground(
    colors: IntArray,
    positions: FloatArray? = null,
    cornerRadiusDp: Float = 20f,
    strokeWidthDp: Float = 1f,
    strokeColors: IntArray? = null,
    strokePositions: FloatArray? = null,
    startNorm: PointF = PointF(0f, 0f),
    endNorm: PointF = PointF(1f, 1f),
    strokeStartNorm: PointF = PointF(0.5f, 0f),
    strokeEndNorm: PointF = PointF(0.5f, 1f)
) {
    val density = resources.displayMetrics.density
    val radiusPx = cornerRadiusDp * density
    val strokePx = strokeWidthDp * density

    background = FigmaGradientDrawable(
        cornerRadius = radiusPx,
        bgColors = colors,
        bgPositions = positions,
        bgStartNorm = startNorm,
        bgEndNorm = endNorm,
        strokeWidth = strokePx,
        strokeColors = strokeColors,
        strokePositions = strokePositions,
        strokeStartNorm = strokeStartNorm,
        strokeEndNorm = strokeEndNorm
    )
}

/**
 * Specifically configures the Premium Settings Hero banner matching Figma node 154:1590
 * with precise normalized diagonal coordinates (-0.1186, -0.1755) -> (0.8314, 0.8302),
 * color stops at [0.0, 0.3224, 1.0], vertical gradient stroke #6742F8 -> #2337CC,
 * glowing neon halo around the border, and realistic 18dp Gaussian blurred drop shadow.
 */
fun View.setFigmaSettingsBannerBackground() {
    val density = resources.displayMetrics.density
    val radiusPx = 20f * density
    val strokePx = 1f * density
    val bannerInsets = RectF(12f * density, 8f * density, 12f * density, 22f * density)

    val bgColors = intArrayOf(
        ContextCompat.getColor(context, R.color.ui_banner_start),
        ContextCompat.getColor(context, R.color.ui_banner_center),
        ContextCompat.getColor(context, R.color.ui_banner_end)
    )
    val strokeColors = intArrayOf(
        ContextCompat.getColor(context, R.color.ui_banner_stroke_top),
        ContextCompat.getColor(context, R.color.ui_banner_stroke_bottom)
    )

    if (this is com.phuongthanh.diceroller.core.widget.FigmaCardView) {
        this.cornerRadiusPx = radiusPx
        this.insets = bannerInsets
        this.strokeWidthPx = strokePx
        this.strokeColors = strokeColors
    }

    setLayerType(View.LAYER_TYPE_SOFTWARE, null)

    background = FigmaGradientDrawable(
        cornerRadius = radiusPx,
        bgColors = bgColors,
        bgPositions = floatArrayOf(0.0f, 0.3224f, 1.0f),
        bgStartNorm = PointF(-0.1186f, -0.1755f),
        bgEndNorm = PointF(0.8314f, 0.8302f),
        bgHandle2Norm = PointF(-0.5224f, 0.2500f),
        strokeWidth = if (this is com.phuongthanh.diceroller.core.widget.FigmaCardView) 0f else strokePx,
        strokeColors = strokeColors,
        strokePositions = floatArrayOf(0.0f, 1.0f),
        strokeStartNorm = PointF(0.5f, 0f),
        strokeEndNorm = PointF(0.5f, 1f),
        insets = bannerInsets,
        shadowRadius = 12f * density,
        shadowColor = ContextCompat.getColor(context, R.color.ui_banner_shadow),
        shadowOffsetY = 6f * density,
        haloWidth = 0f,
        haloBlurRadius = 0f,
        haloColors = null
    )
}

