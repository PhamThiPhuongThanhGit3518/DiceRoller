package com.phuongthanh.diceroller.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.phuongthanh.diceroller.R

/**
 * Custom ConstraintLayout for Figma cards that clips all child views (e.g. 3D dice illustrations)
 * strictly to the card's 20dp rounded corner boundary, while overlaying a crisp 1px gradient stroke.
 */
class FigmaCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val clipPath = Path()
    private val cardRect = RectF()
    private val strokeRect = RectF()
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    var cornerRadiusPx: Float = 22f * resources.displayMetrics.density
    var insets: RectF = RectF(0f, 0f, 0f, 0f)
    var strokeWidthPx: Float = 0f
    var strokeColors: IntArray? = null

    init {
        setWillNotDraw(false)
        clipChildren = false
        clipToPadding = false

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.FigmaCardView, defStyleAttr, 0)
            cornerRadiusPx = a.getDimension(R.styleable.FigmaCardView_cardCornerRadius, cornerRadiusPx)
            a.recycle()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        cardRect.set(
            insets.left,
            insets.top,
            width - insets.right,
            height - insets.bottom
        )
        clipPath.reset()
        clipPath.addRoundRect(cardRect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)

        // Draw crisp 1px gradient stroke ON TOP of clipped children
        if (strokeWidthPx > 0f && strokeColors != null) {
            val halfStroke = strokeWidthPx / 2f
            strokeRect.set(
                cardRect.left + halfStroke,
                cardRect.top + halfStroke,
                cardRect.right - halfStroke,
                cardRect.bottom - halfStroke
            )
            strokePaint.strokeWidth = strokeWidthPx
            strokePaint.shader = LinearGradient(
                cardRect.centerX(), cardRect.top,
                cardRect.centerX(), cardRect.bottom,
                strokeColors!!,
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            val innerRadius = (cornerRadiusPx - halfStroke).coerceAtLeast(0f)
            canvas.drawRoundRect(strokeRect, innerRadius, innerRadius, strokePaint)
        }
    }
}
