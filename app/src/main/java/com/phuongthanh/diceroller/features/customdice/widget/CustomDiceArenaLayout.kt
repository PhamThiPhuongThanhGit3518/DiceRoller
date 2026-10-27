package com.phuongthanh.diceroller.features.customdice.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.phuongthanh.diceroller.features.customdice.model.CustomDiceFace

class CustomDiceArenaLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    var onArenaClickListener: (() -> Unit)? = null
    var onDieClickListener: ((Int) -> Unit)? = null

    private var activeDiceCount = 8
    private var currentFaces: List<CustomDiceFace> = emptyList()

    // 8-Dice color pattern matching media_1792740216216.png
    private val colorsFor8Dice = listOf(
        Color.parseColor("#BFE87A"), // 0: green
        Color.parseColor("#BFE87A"), // 1: green
        Color.parseColor("#7F8BE9"), // 2: purple
        Color.parseColor("#F28C74"), // 3: salmon
        Color.parseColor("#E9E880"), // 4: yellow
        Color.parseColor("#F28C74"), // 5: salmon
        Color.parseColor("#E9E880"), // 6: yellow
        Color.parseColor("#7F8BE9")  // 7: purple
    )

    private val generalPalette = listOf(
        Color.parseColor("#BFE87A"), // Lime Green
        Color.parseColor("#7F8BE9"), // Light Purple
        Color.parseColor("#F28C74"), // Salmon / Coral
        Color.parseColor("#E9E880"), // Soft Yellow
        Color.parseColor("#80EACE"), // Cyan / Mint
        Color.parseColor("#F79371")  // Peach
    )

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val index = findDieIndexAt(e.x.toInt(), e.y.toInt())
            if (index != null) {
                onDieClickListener?.invoke(index)
            }
            onArenaClickListener?.invoke()
            return true
        }
    })

    init {
        // Pre-create 9 CustomDieCellView children to avoid allocation during roll / stepper
        for (i in 0 until 9) {
            val cell = CustomDieCellView(context)
            cell.visibility = View.GONE
            addView(cell)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun findDieIndexAt(x: Int, y: Int): Int? {
        val rects = calculatePartitionRects(activeDiceCount, width, height)
        for (i in 0 until minOf(activeDiceCount, rects.size)) {
            if (rects[i].contains(x, y)) {
                return i
            }
        }
        return null
    }

    fun setDiceCount(count: Int, faces: List<CustomDiceFace>) {
        this.activeDiceCount = count.coerceIn(1, 9)
        this.currentFaces = faces
        bindAllCells()
        requestLayout()
    }

    fun updateDiceFaces(faces: List<CustomDiceFace>) {
        this.currentFaces = faces
        bindAllCells()
    }

    private fun getCellColor(index: Int, count: Int): Int {
        if (count == 8 && index in colorsFor8Dice.indices) {
            return colorsFor8Dice[index]
        }
        return generalPalette[index % generalPalette.size]
    }

    private fun isCellInTopRow(index: Int, count: Int): Boolean = when (count) {
        1 -> true
        2 -> index == 0
        3 -> index == 0
        4 -> index in 0..1
        5 -> index in 0..1
        6 -> index in 0..1
        7 -> index in 0..1
        8 -> index in 0..2
        9 -> index in 0..2
        else -> false
    }

    private fun isCellInBottomRow(index: Int, count: Int): Boolean = when (count) {
        1 -> true
        2 -> index == 1
        3 -> index == 2
        4 -> index in 2..3
        5 -> index in 2..4
        6 -> index in 4..5
        7 -> index in 4..6
        8 -> index in 6..7
        9 -> index in 6..8
        else -> false
    }

    private fun bindAllCells() {
        for (i in 0 until childCount) {
            val cell = getChildAt(i) as? CustomDieCellView ?: continue
            if (i < activeDiceCount && i < currentFaces.size) {
                cell.visibility = View.VISIBLE
                val face = currentFaces[i]
                val color = getCellColor(i, activeDiceCount)
                val isTop = isCellInTopRow(i, activeDiceCount)
                val isBottom = isCellInBottomRow(i, activeDiceCount)
                cell.bindFace(face, color, isTop, isBottom)
            } else if (i >= activeDiceCount) {
                cell.visibility = View.GONE
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalW = MeasureSpec.getSize(widthMeasureSpec)
        val totalH = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(totalW, totalH)

        val count = activeDiceCount.coerceIn(1, 9)
        val rects = calculatePartitionRects(count, totalW, totalH)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (i < count && i < rects.size) {
                val rect = rects[i]
                val childWSpec = MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY)
                val childHSpec = MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY)
                child.measure(childWSpec, childHSpec)
            } else {
                child.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
                )
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val totalW = r - l
        val totalH = b - t
        val count = activeDiceCount.coerceIn(1, 9)
        val rects = calculatePartitionRects(count, totalW, totalH)

        for (i in 0 until minOf(count, childCount, rects.size)) {
            val child = getChildAt(i)
            val rect = rects[i]
            child.layout(rect.left, rect.top, rect.right, rect.bottom)
        }
    }

    fun animateTumble() {
        for (i in 0 until activeDiceCount) {
            val cell = getChildAt(i) as? CustomDieCellView ?: continue
            cell.animateTumble()
        }
    }

    fun animateFinalBounce() {
        for (i in 0 until activeDiceCount) {
            val cell = getChildAt(i) as? CustomDieCellView ?: continue
            cell.animateFinalBounce()
        }
    }

    private fun calculatePartitionRects(count: Int, totalW: Int, totalH: Int): List<Rect> {
        if (count <= 0 || totalW <= 0 || totalH <= 0) return emptyList()
        val clamped = count.coerceIn(1, 9)
        val rects = ArrayList<Rect>(clamped)

        when (clamped) {
            1 -> {
                rects.add(Rect(0, 0, totalW, totalH))
            }
            2 -> {
                val h2 = totalH / 2
                rects.add(Rect(0, 0, totalW, h2))
                rects.add(Rect(0, h2, totalW, totalH))
            }
            3 -> {
                val h3 = totalH / 3
                rects.add(Rect(0, 0, totalW, h3))
                rects.add(Rect(0, h3, totalW, 2 * h3))
                rects.add(Rect(0, 2 * h3, totalW, totalH))
            }
            4 -> {
                val w2 = totalW / 2
                val h2 = totalH / 2
                rects.add(Rect(0, 0, w2, h2))
                rects.add(Rect(w2, 0, totalW, h2))
                rects.add(Rect(0, h2, w2, totalH))
                rects.add(Rect(w2, h2, totalW, totalH))
            }
            5 -> {
                val h2 = totalH / 2
                val w2 = totalW / 2
                val w3 = totalW / 3
                // Row 0: 2 cols
                rects.add(Rect(0, 0, w2, h2))
                rects.add(Rect(w2, 0, totalW, h2))
                // Row 1: 3 cols
                rects.add(Rect(0, h2, w3, totalH))
                rects.add(Rect(w3, h2, 2 * w3, totalH))
                rects.add(Rect(2 * w3, h2, totalW, totalH))
            }
            6 -> {
                val h3 = totalH / 3
                val w2 = totalW / 2
                for (r in 0..2) {
                    val top = r * h3
                    val bottom = if (r == 2) totalH else (r + 1) * h3
                    rects.add(Rect(0, top, w2, bottom))
                    rects.add(Rect(w2, top, totalW, bottom))
                }
            }
            7 -> {
                val h3 = totalH / 3
                val w2 = totalW / 2
                val w3 = totalW / 3
                val r0Bottom = h3
                val r1Bottom = 2 * h3
                // Row 0: 2 cols
                rects.add(Rect(0, 0, w2, r0Bottom))
                rects.add(Rect(w2, 0, totalW, r0Bottom))
                // Row 1: 2 cols
                rects.add(Rect(0, r0Bottom, w2, r1Bottom))
                rects.add(Rect(w2, r0Bottom, totalW, r1Bottom))
                // Row 2: 3 cols
                rects.add(Rect(0, r1Bottom, w3, totalH))
                rects.add(Rect(w3, r1Bottom, 2 * w3, totalH))
                rects.add(Rect(2 * w3, r1Bottom, totalW, totalH))
            }
            8 -> {
                val h3 = totalH / 3
                val w3 = totalW / 3
                val w2 = totalW / 2
                val r0Bottom = h3
                val r1Bottom = 2 * h3
                // Row 0: 3 cols
                rects.add(Rect(0, 0, w3, r0Bottom))
                rects.add(Rect(w3, 0, 2 * w3, r0Bottom))
                rects.add(Rect(2 * w3, 0, totalW, r0Bottom))
                // Row 1: 3 cols
                rects.add(Rect(0, r0Bottom, w3, r1Bottom))
                rects.add(Rect(w3, r0Bottom, 2 * w3, r1Bottom))
                rects.add(Rect(2 * w3, r0Bottom, totalW, r1Bottom))
                // Row 2: 2 cols
                rects.add(Rect(0, r1Bottom, w2, totalH))
                rects.add(Rect(w2, r1Bottom, totalW, totalH))
            }
            9 -> {
                val h3 = totalH / 3
                val w3 = totalW / 3
                for (r in 0..2) {
                    val top = r * h3
                    val bottom = if (r == 2) totalH else (r + 1) * h3
                    for (c in 0..2) {
                        val left = c * w3
                        val right = if (c == 2) totalW else (c + 1) * w3
                        rects.add(Rect(left, top, right, bottom))
                    }
                }
            }
        }
        return rects
    }
}
