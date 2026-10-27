package com.phuongthanh.diceroller.core.opengl.view

import android.graphics.RectF
import java.util.ArrayList

object DicePartitionHelper {

    /**
     * Tùy chỉnh khoảng cách và bố cục cho 3 xúc xắc (tam giác: 2 viên trên, 1 viên dưới):
     * - SPACING_3_DICE_HORIZONTAL: Độ lệch ngang từ tâm sang trái/phải cho 2 viên trên.
     *   (Mặc định 0.17f -> 2 viên cách nhau ~34% bề ngang thẻ thay vì 50% như trước).
     * - SPACING_3_DICE_VERTICAL: Độ lệch dọc từ tâm lên/xuống.
     *   (Mặc định 0.15f -> hàng trên và viên dưới cách nhau ~30% bề dọc thẻ thay vì 50% như trước).
     * - CELL_SIZE_3_DICE_RATIO: Kích thước vùng hiển thị của mỗi xúc xắc (mặc định 0.50f).
     */
    const val SPACING_3_DICE_HORIZONTAL = 0.17f
    const val SPACING_3_DICE_VERTICAL = 0.15f
    const val CELL_SIZE_3_DICE_RATIO = 0.50f

    fun calculatePartitionRects(count: Int, totalW: Float, totalH: Float): List<RectF> {
        if (count <= 0 || totalW <= 0f || totalH <= 0f) return emptyList()
        val clampedCount = count.coerceIn(1, 9)
        val rects = ArrayList<RectF>(clampedCount)

        val isCardRatio = (totalH / totalW) < 1.35f

        when (clampedCount) {
            1 -> {
                rects.add(RectF(0f, 0f, totalW, totalH))
            }
            2 -> {
                if (isCardRatio) {
                    val w2 = totalW / 2f
                    rects.add(RectF(0f, 0f, w2, totalH))
                    rects.add(RectF(w2, 0f, totalW, totalH))
                } else {
                    val h2 = totalH / 2f
                    rects.add(RectF(0f, 0f, totalW, h2))
                    rects.add(RectF(0f, h2, totalW, totalH))
                }
            }
            3 -> {
                if (isCardRatio) {
                    val cx = totalW / 2f
                    val cy = totalH / 2f
                    val dx = totalW * SPACING_3_DICE_HORIZONTAL
                    val dy = totalH * SPACING_3_DICE_VERTICAL
                    val cellW = totalW * CELL_SIZE_3_DICE_RATIO
                    val cellH = totalH * CELL_SIZE_3_DICE_RATIO

                    // Top-Left die (viên trên bên trái)
                    val c0x = cx - dx
                    val c0y = cy - dy
                    rects.add(RectF(c0x - cellW / 2f, c0y - cellH / 2f, c0x + cellW / 2f, c0y + cellH / 2f))

                    // Top-Right die (viên trên bên phải)
                    val c1x = cx + dx
                    val c1y = cy - dy
                    rects.add(RectF(c1x - cellW / 2f, c1y - cellH / 2f, c1x + cellW / 2f, c1y + cellH / 2f))

                    // Bottom-Center die (viên dưới ở giữa)
                    val c2x = cx
                    val c2y = cy + dy
                    rects.add(RectF(c2x - cellW / 2f, c2y - cellH / 2f, c2x + cellW / 2f, c2y + cellH / 2f))
                } else {
                    val h3 = totalH / 3f
                    rects.add(RectF(0f, 0f, totalW, h3))
                    rects.add(RectF(0f, h3, totalW, 2f * h3))
                    rects.add(RectF(0f, 2f * h3, totalW, totalH))
                }
            }
            4 -> {
                val w2 = totalW / 2f
                val h2 = totalH / 2f
                rects.add(RectF(0f, 0f, w2, h2))
                rects.add(RectF(w2, 0f, totalW, h2))
                rects.add(RectF(0f, h2, w2, totalH))
                rects.add(RectF(w2, h2, totalW, totalH))
            }
            5 -> {
                val h2 = totalH / 2f
                val w2 = totalW / 2f
                val w3 = totalW / 3f
                rects.add(RectF(0f, 0f, w2, h2))
                rects.add(RectF(w2, 0f, totalW, h2))
                rects.add(RectF(0f, h2, w3, totalH))
                rects.add(RectF(w3, h2, 2f * w3, totalH))
                rects.add(RectF(2f * w3, h2, totalW, totalH))
            }
            6 -> {
                val h3 = totalH / 3f
                val w2 = totalW / 2f
                for (r in 0..2) {
                    for (c in 0..1) {
                        rects.add(RectF(c * w2, r * h3, (c + 1) * w2, (r + 1) * h3))
                    }
                }
            }
            7 -> {
                val h3 = totalH / 3f
                val w2 = totalW / 2f
                val w3 = totalW / 3f
                rects.add(RectF(0f, 0f, w2, h3))
                rects.add(RectF(w2, 0f, totalW, h3))
                rects.add(RectF(0f, h3, w2, 2f * h3))
                rects.add(RectF(w2, h3, totalW, 2f * h3))
                rects.add(RectF(0f, 2f * h3, w3, totalH))
                rects.add(RectF(w3, 2f * h3, 2f * w3, totalH))
                rects.add(RectF(2f * w3, 2f * h3, totalW, totalH))
            }
            8 -> {
                val h4 = totalH / 4f
                val w2 = totalW / 2f
                for (r in 0..3) {
                    for (c in 0..1) {
                        rects.add(RectF(c * w2, r * h4, (c + 1) * w2, (r + 1) * h4))
                    }
                }
            }
            9 -> {
                val h3 = totalH / 3f
                val w3 = totalW / 3f
                for (r in 0..2) {
                    for (c in 0..2) {
                        rects.add(RectF(c * w3, r * h3, (c + 1) * w3, (r + 1) * h3))
                    }
                }
            }
        }
        return rects
    }
}
