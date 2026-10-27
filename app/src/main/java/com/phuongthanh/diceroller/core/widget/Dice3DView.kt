package com.phuongthanh.diceroller.core.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.opengl.model.Dice3DState
import com.phuongthanh.diceroller.core.opengl.view.Dice3DRenderer
import com.phuongthanh.diceroller.core.opengl.view.DicePartitionHelper
import com.phuongthanh.diceroller.core.opengl.view.GLTextureView
import com.phuongthanh.diceroller.features.freeroll.model.DiceItem
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import kotlin.math.min
import kotlin.random.Random

class Dice3DView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onDiceClickListener: ((Int) -> Unit)? = null
    var onDiceLongClickListener: ((Int) -> Unit)? = null
    var onCanvasClickListener: (() -> Unit)? = null
    var onRollFinishedListener: (() -> Unit)? = null

    private val renderer = Dice3DRenderer {
        onRollFinishedListener?.invoke()
    }

    private val glTextureView: GLTextureView = GLTextureView(context).apply {
        setRenderer(renderer)
        renderMode = GLTextureView.RENDERMODE_CONTINUOUSLY
    }

    private val overlayView = OverlayView(context)

    private var diceItems: List<DiceItem> = emptyList()
    private val diceStates = mutableListOf<Dice3DState>()
    private val diceBoundsMap = mutableMapOf<Int, RectF>()

    private var isTileMode: Boolean = false
    private var tileRollAnimator: ValueAnimator? = null
    private val tileRollingValues = mutableMapOf<Int, Int>()
    private var isRollingTiles: Boolean = false

    init {
        addView(glTextureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(overlayView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun setThemeColors(sameColors: Boolean, diceColor: Int, dotColor: Int) {
        renderer.sameColorsAllDice = sameColors
        renderer.diceColor = diceColor
        renderer.dotColor = dotColor
        overlayView.setDotColor(dotColor)
        overlayView.invalidate()
    }

    fun setTableColors(bgColor: Int, separatorColor: Int) {
        renderer.tableBgColor = bgColor
        renderer.tableSeparatorColor = separatorColor
        glTextureView.requestRender()
    }

    fun setShowDividers(show: Boolean) {
        renderer.showDividers = show
        overlayView.setShowDividers(show)
        glTextureView.requestRender()
    }

    fun setDiceScale(
        scale1Die: Float = Dice3DRenderer.SCALE_1_DICE,
        scale2Dice: Float = Dice3DRenderer.SCALE_2_DICE,
        scale3Dice: Float = Dice3DRenderer.SCALE_3_DICE
    ) {
        renderer.customScale1Die = scale1Die
        renderer.customScale2Dice = scale2Dice
        renderer.customScale3Dice = scale3Dice
        glTextureView.requestRender()
    }

    fun setDiceList(list: List<DiceItem>) {
        val newTileMode = list.isNotEmpty() && list.any { it.isGreaterThanD20 }
        if (newTileMode != isTileMode) {
            isTileMode = newTileMode
            glTextureView.visibility = if (isTileMode) View.GONE else View.VISIBLE
        }

        val oldMap = diceStates.associateBy { it.id }
        val newStates = mutableListOf<Dice3DState>()

        for (item in list) {
            val existing = oldMap[item.id]
            if (existing != null && existing.type == item.type) {
                existing.isLocked = item.isLocked
                if (existing.value != item.value && !existing.isRolling) {
                    existing.snapToValue(item.value)
                }
                newStates.add(existing)
            } else {
                val newState = Dice3DState(item.id, item.type, item.value, item.isLocked)
                newStates.add(newState)
            }
        }

        this.diceItems = list
        this.diceStates.clear()
        this.diceStates.addAll(newStates)
        renderer.diceStates = newStates

        updatePartitions()
        overlayView.invalidate()
    }

    fun rollDice(durationMs: Long = 1200L) {
        if (isTileMode) {
            rollTiles(durationMs)
        } else {
            for (state in diceStates) {
                if (!state.isLocked) {
                    val item = diceItems.find { it.id == state.id }
                    val targetValue = item?.value ?: state.value
                    state.startRoll(targetValue, durationMs)
                }
            }
            overlayView.invalidate()
        }
    }

    private fun rollTiles(durationMs: Long) {
        tileRollAnimator?.cancel()
        isRollingTiles = true

        var lastUpdateTime = 0L
        val updateIntervalMs = 50L

        tileRollAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                val currentTime = (fraction * durationMs).toLong()

                if (fraction < 0.90f) {
                    if (currentTime - lastUpdateTime >= updateIntervalMs) {
                        lastUpdateTime = currentTime
                        for (die in diceItems) {
                            if (!die.isLocked) {
                                val minVal = if (die.type == DiceType.CUSTOM) minOf(die.customMin, die.customMax) else die.type.minValue
                                val maxVal = if (die.type == DiceType.CUSTOM) maxOf(die.customMin, die.customMax) else die.type.maxValue
                                tileRollingValues[die.id] = Random.nextInt(minVal, maxVal + 1)
                            }
                        }
                        overlayView.invalidate()
                    }
                } else {
                    for (die in diceItems) {
                        tileRollingValues[die.id] = die.value
                    }
                    overlayView.invalidate()
                }
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isRollingTiles = false
                    tileRollingValues.clear()
                    overlayView.invalidate()
                    onRollFinishedListener?.invoke()
                }

                override fun onAnimationCancel(animation: Animator) {
                    isRollingTiles = false
                    tileRollingValues.clear()
                    overlayView.invalidate()
                }
            })
            start()
        }
    }

    private fun updatePartitions() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f || diceItems.isEmpty()) return

        val count = diceItems.size.coerceIn(1, 9)
        val rects = DicePartitionHelper.calculatePartitionRects(count, w, h)
        renderer.partitionRects = rects

        diceBoundsMap.clear()
        for (i in 0 until minOf(diceItems.size, rects.size)) {
            diceBoundsMap[diceItems[i].id] = rects[i]
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePartitions()
    }

    fun onResume() {
        if (!isTileMode) {
            glTextureView.onResume()
        }
    }

    fun onPause() {
        tileRollAnimator?.cancel()
        glTextureView.onPause()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tileRollAnimator?.cancel()
    }

    private inner class OverlayView(ctx: Context) : View(ctx) {

        private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        private val separatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F000000")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F7D4D5")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 2f, 4f, Color.parseColor("#4D000000"))
        }

        private val lockedGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(ctx, R.color.accent_gold)
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        private val lockBadgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(ctx, R.color.accent_gold)
            style = Paint.Style.FILL
        }

        private val lockIconDrawable = ContextCompat.getDrawable(ctx, R.drawable.ic_lock)

        // Palette matching user reference:
        // Tile 0: Soft Yellow (#E9E880), Tile 1: Coral (#E9807F)
        // Tile 2: Peach (#F79371), Tile 3: Cyan (#80EACE)
        private val tilePalette = listOf(
            Color.parseColor("#E9E880"), // Soft Yellow
            Color.parseColor("#E9807F"), // Coral / Red
            Color.parseColor("#F79371"), // Peach / Salmon
            Color.parseColor("#80EACE"), // Cyan / Turquoise
            Color.parseColor("#7F8BE9"), // Light Purple
            Color.parseColor("#C4EA7E"), // Lime / Green
            Color.parseColor("#FFB085"), // Warm Orange
            Color.parseColor("#90CAF9"), // Soft Blue
            Color.parseColor("#FFAB91")  // Soft Peach
        )

        private var showDividers: Boolean = false

        fun setShowDividers(show: Boolean) {
            this.showDividers = show
            invalidate()
        }

        fun setDotColor(color: Int) {
            numberPaint.color = color
        }

        private var isLongPressed = false

        private val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                isLongPressed = false
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                isLongPressed = true
                val dieId = findDieAt(e.x, e.y)
                if (dieId != null) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onDiceLongClickListener?.invoke(dieId)
                }
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (!isLongPressed) {
                    performClick()
                    val dieId = findDieAt(e.x, e.y)
                    if (dieId != null) {
                        onDiceClickListener?.invoke(dieId)
                    }
                    onCanvasClickListener?.invoke()
                }
                return true
            }
        })

        private fun findDieAt(x: Float, y: Float): Int? {
            for ((id, rect) in diceBoundsMap) {
                if (rect.contains(x, y)) {
                    return id
                }
            }
            return null
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val handled = gestureDetector.onTouchEvent(event)
            return handled || super.onTouchEvent(event)
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (diceItems.isEmpty()) return

            if (isTileMode) {
                val count = minOf(diceItems.size, diceBoundsMap.size)
                for (i in 0 until count) {
                    val die = diceItems[i]
                    val rect = diceBoundsMap[die.id] ?: continue

                    // 1. Draw Tile Background Color ("mỗi ô sẽ là mỗi màu khác nhau")
                    val tileColor = tilePalette[i % tilePalette.size]
                    tilePaint.color = tileColor
                    canvas.drawRect(rect, tilePaint)

                    // 2. Draw Subtle Tile Separator Line
                    if (showDividers) {
                        canvas.drawRect(rect, separatorPaint)
                    }

                    // 3. Draw Center Random / Final Number ("hiển thị số ở giữa và random đi thoi")
                    val displayValue = if (isRollingTiles && !die.isLocked) {
                        tileRollingValues[die.id] ?: die.value
                    } else {
                        die.value
                    }

                    val cx = rect.centerX()
                    val cy = rect.centerY()
                    val minDim = min(rect.width(), rect.height())
                    val text = displayValue.toString()

                    val scale = when {
                        text.length >= 4 -> 0.24f
                        text.length >= 3 -> 0.30f
                        else -> 0.38f
                    }
                    numberPaint.textSize = minDim * scale

                    val fontMetrics = numberPaint.fontMetrics
                    val textY = cy - (fontMetrics.ascent + fontMetrics.descent) / 2f
                    canvas.drawText(text, cx, textY, numberPaint)

                    // 4. Draw Locked State if die is locked
                    if (die.isLocked) {
                        drawLockState(canvas, rect)
                    }
                }
            } else {
                // 3D mode: Overlay only renders lock indicator on top of 3D OpenGL view
                for (die in diceItems) {
                    if (!die.isLocked) continue
                    val rect = diceBoundsMap[die.id] ?: continue
                    drawLockState(canvas, rect)
                }
            }
        }

        private fun drawLockState(canvas: Canvas, rect: RectF) {
            // 1. Locked Border Glow
            val lockBorderRect = RectF(rect)
            lockBorderRect.inset(4f, 4f)
            canvas.drawRect(lockBorderRect, lockedGlowPaint)

            // 2. Lock Icon Badge
            val badgeRadius = min(rect.width(), rect.height()) * 0.12f
            val badgeX = rect.right - badgeRadius * 1.5f
            val badgeY = rect.top + badgeRadius * 1.5f
            canvas.drawCircle(badgeX, badgeY, badgeRadius, lockBadgeBgPaint)

            lockIconDrawable?.let { icon ->
                val iconHalf = (badgeRadius * 0.65f).toInt()
                icon.setBounds(
                    (badgeX - iconHalf).toInt(),
                    (badgeY - iconHalf).toInt(),
                    (badgeX + iconHalf).toInt(),
                    (badgeY + iconHalf).toInt()
                )
                icon.draw(canvas)
            }
        }
    }
}
