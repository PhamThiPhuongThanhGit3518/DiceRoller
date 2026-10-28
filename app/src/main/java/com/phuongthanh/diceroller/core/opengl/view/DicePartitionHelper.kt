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

    /**
     * Khoảng cách mặc định (gap) giữa các ô chứa xúc xắc (in pixels).
     * Mặc định là 8f để các xúc xắc không dính sát viền hay chạm vào nhau.
     */
    const val DEFAULT_DICE_GAP = 4f
    const val COLUMN_CLUSTER_RATIO = 0.82f

    fun calculatePartitionRects(
        count: Int,
        totalW: Float,
        totalH: Float,
        gap: Float = DEFAULT_DICE_GAP
    ): List<RectF> {
        if (count <= 0 || totalW <= 0f || totalH <= 0f) return emptyList()
        val clampedCount = count.coerceIn(1, 9)
        val rects = ArrayList<RectF>(clampedCount)

        // Tính số hàng: mỗi hàng tối đa 3 xúc xắc
        val numRows = when {
            clampedCount <= 3 -> 1
            clampedCount <= 6 -> 2
            else -> 3
        }

        // Số cột tối đa của layout (1 cột nếu 1 viên, 3 cột nếu >= 2 viên để căn giữa và thu hẹp khoảng cách)
        val maxCols = if (clampedCount == 1) 1 else 3

        val clusterWidth = if (clampedCount == 1) totalW else totalW * COLUMN_CLUSTER_RATIO
        val cellW = clusterWidth / maxCols.toFloat()
        // Chiều cao mỗi ô: vừa vặn với chiều dọc tổng chia cho số hàng, nhưng không quá dài
        val cellH = minOf(totalH / numRows.toFloat(), cellW * 0.8f)

        // Căn giữa toàn bộ cụm hàng theo chiều dọc (Y)
        val totalClusterH = numRows * cellH
        val startY = (totalH - totalClusterH) / 2f

        var remainingDice = clampedCount
        for (r in 0 until numRows) {
            val itemsInThisRow = minOf(3, remainingDice)
            val rowTop = startY + r * cellH
            val rowBottom = rowTop + cellH

            // Căn giữa các viên trên hàng này theo chiều ngang (X)
            val startX = (totalW - itemsInThisRow * cellW) / 2f
            for (c in 0 until itemsInThisRow) {
                val left = startX + c * cellW
                val right = left + cellW
                rects.add(RectF(left, rowTop, right, rowBottom))
            }
            remainingDice -= itemsInThisRow
        }

        return if (gap > 0f && clampedCount > 1) {
            rects.map { rect ->
                val halfW = (rect.right - rect.left) / 2f
                val halfH = (rect.bottom - rect.top) / 2f
                val clampedGapX = minOf(gap, halfW * 0.5f)
                val clampedGapY = minOf(gap, halfH * 0.5f)
                RectF(
                    rect.left + clampedGapX,
                    rect.top + clampedGapY,
                    rect.right - clampedGapX,
                    rect.bottom - clampedGapY
                )
            }
        } else {
            rects
        }
    }
}
