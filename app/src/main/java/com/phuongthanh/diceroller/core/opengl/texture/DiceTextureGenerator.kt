package com.phuongthanh.diceroller.core.opengl.texture

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.phuongthanh.diceroller.core.opengl.geometry.MeshBuilderUtils
import kotlin.math.min

object DiceTextureGenerator {

    private const val ATLAS_SIZE = 1024
    private const val COLS = MeshBuilderUtils.ATLAS_COLS // 6
    private const val ROWS = MeshBuilderUtils.ATLAS_ROWS // 4

    // Cache generated atlas bitmaps so Canvas rendering happens only once per color combination
    private val bitmapCache = mutableMapOf<Long, Bitmap>()

    fun getAtlasBitmap(diceColor: Int, dotColor: Int): Bitmap {
        val key = (diceColor.toLong() shl 32) or (dotColor.toLong() and 0xFFFFFFFFL)
        synchronized(bitmapCache) {
            val existing = bitmapCache[key]
            if (existing != null && !existing.isRecycled) {
                return existing
            }
            val newBitmap = createAtlasBitmap(diceColor, dotColor)
            bitmapCache[key] = newBitmap
            return newBitmap
        }
    }

    fun clearCache() {
        synchronized(bitmapCache) {
            for (bitmap in bitmapCache.values) {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            bitmapCache.clear()
        }
    }

    private fun createAtlasBitmap(diceColor: Int, dotColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(ATLAS_SIZE, ATLAS_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cellW = ATLAS_SIZE.toFloat() / COLS.toFloat()
        val cellH = ATLAS_SIZE.toFloat() / ROWS.toFloat()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = diceColor
        }

        val textShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#40000000") // Soft engraved drop shadow
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dotColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val underlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dotColor
            style = Paint.Style.STROKE
            strokeWidth = 4f
            strokeCap = Paint.Cap.ROUND
        }

        val totalCells = COLS * ROWS
        for (idx in 0 until totalCells) {
            val faceNum = idx + 1
            val col = idx % COLS
            val row = idx / COLS

            val left = col * cellW
            val top = row * cellH
            val right = left + cellW
            val bottom = top + cellH
            val rect = RectF(left, top, right, bottom)

            // Fill cell background with base dice color
            canvas.drawRect(rect, bgPaint)

            val cx = rect.centerX()
            val cy = rect.centerY()
            val minDim = min(cellW, cellH)

            // Dynamic font sizing based on single or double digit
            val text = faceNum.toString()
            val scaleFactor = if (faceNum >= 10) 0.32f else 0.38f
            val textSize = minDim * scaleFactor

            textPaint.textSize = textSize
            textShadowPaint.textSize = textSize

            val fontMetrics = textPaint.fontMetrics
            val baseline = cy - (fontMetrics.ascent + fontMetrics.descent) / 2f

            // 1. Engraved subtle shadow slightly offset downward
            canvas.drawText(text, cx, baseline + 3f, textShadowPaint)

            // 2. Crisp main number
            canvas.drawText(text, cx, baseline, textPaint)

            // 3. Clear underline for 6 and 9 to eliminate ambiguity
            if (faceNum == 6 || faceNum == 9) {
                val underlineY = baseline + fontMetrics.descent + 3f
                val underlineHalfW = textSize * 0.18f
                canvas.drawLine(cx - underlineHalfW, underlineY, cx + underlineHalfW, underlineY, underlinePaint)
            }
        }

        return bitmap
    }
}
