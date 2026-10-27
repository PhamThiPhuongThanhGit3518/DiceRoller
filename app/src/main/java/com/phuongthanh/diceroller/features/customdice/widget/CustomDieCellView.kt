package com.phuongthanh.diceroller.features.customdice.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.widget.TextViewCompat
import com.phuongthanh.diceroller.databinding.ItemCustomDieCellBinding
import com.phuongthanh.diceroller.features.customdice.model.CustomDiceFace
import com.phuongthanh.diceroller.features.customdice.utils.CustomDiceImageHelper
import kotlin.math.min

class CustomDieCellView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ItemCustomDieCellBinding.inflate(LayoutInflater.from(context), this, true)
    private var currentFace: CustomDiceFace? = null

    init {
        isClickable = true
        isFocusable = true
    }

    fun bindFace(
        face: CustomDiceFace,
        cellColor: Int,
        isTopRow: Boolean = false,
        isBottomRow: Boolean = false
    ) {
        currentFace = face
        setBackgroundColor(cellColor)
        applyEdgePadding(isTopRow, isBottomRow)
        applyFaceData(face)
    }

    private fun applyEdgePadding(isTopRow: Boolean, isBottomRow: Boolean) {
        val density = resources.displayMetrics.density
        val startPad = (16 * density).toInt()
        val endPad = (16 * density).toInt()
        // Top buttons (back & help) occupy 48dp + 16dp = 64dp
        val topPad = if (isTopRow) (64 * density).toInt() else (12 * density).toInt()
        // Bottom controls (stepper & buttons) occupy 52dp + 16dp = 68dp
        val bottomPad = if (isBottomRow) (72 * density).toInt() else (12 * density).toInt()

        binding.layoutDieContent.setPadding(startPad, topPad, endPad, bottomPad)
    }

    private fun applyFaceData(face: CustomDiceFace) {
        val density = resources.displayMetrics.density
        val textLp = binding.tvFaceText.layoutParams as LinearLayout.LayoutParams
        val cardLp = binding.cardImageContainer.layoutParams as LinearLayout.LayoutParams

        when (face.formatType) {
            "emoji" -> {
                binding.cardImageContainer.visibility = View.GONE
                binding.tvFaceEmoji.visibility = View.VISIBLE
                binding.tvFaceText.visibility = View.GONE
                binding.tvFaceEmoji.text = face.iconOrEmoji.ifEmpty { face.text }
            }
            "image" -> {
                cardLp.bottomMargin = 0
                binding.cardImageContainer.layoutParams = cardLp
                binding.cardImageContainer.visibility = View.VISIBLE
                binding.tvFaceEmoji.visibility = View.GONE
                binding.tvFaceText.visibility = View.GONE
                CustomDiceImageHelper.loadFaceImage(binding.ivFaceImage, face.iconOrEmoji)
            }
            "image_text" -> {
                cardLp.bottomMargin = (8 * density).toInt()
                binding.cardImageContainer.layoutParams = cardLp
                binding.cardImageContainer.visibility = View.VISIBLE
                binding.tvFaceEmoji.visibility = View.GONE
                binding.tvFaceText.visibility = View.VISIBLE

                textLp.height = LinearLayout.LayoutParams.WRAP_CONTENT
                textLp.weight = 0f
                binding.tvFaceText.layoutParams = textLp

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    binding.tvFaceText,
                    11, 20, 1, TypedValue.COMPLEX_UNIT_SP
                )
                binding.tvFaceText.text = face.text
                CustomDiceImageHelper.loadFaceImage(binding.ivFaceImage, face.iconOrEmoji)
            }
            else -> { // "text"
                binding.cardImageContainer.visibility = View.GONE
                binding.tvFaceEmoji.visibility = View.GONE
                binding.tvFaceText.visibility = View.VISIBLE

                textLp.height = 0
                textLp.weight = 1f
                binding.tvFaceText.layoutParams = textLp

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    binding.tvFaceText,
                    12, 40, 1, TypedValue.COMPLEX_UNIT_SP
                )
                binding.tvFaceText.text = face.text
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        if (w > 0 && h > 0) {
            val minDim = min(w, h)
            val density = resources.displayMetrics.density
            val face = currentFace
            val isImageText = face?.formatType == "image_text"
            val cardRatio = if (isImageText) 0.38f else 0.55f
            val cardSize = (minDim * cardRatio).toInt().coerceIn(
                (44 * density).toInt(),
                (140 * density).toInt()
            )

            if (binding.cardImageContainer.layoutParams.width != cardSize ||
                binding.cardImageContainer.layoutParams.height != cardSize
            ) {
                binding.cardImageContainer.layoutParams.width = cardSize
                binding.cardImageContainer.layoutParams.height = cardSize
                binding.cardImageContainer.radius = cardSize / 2f
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun animateTumble() {
        val angle = (-15..15).random().toFloat()
        val scale = 0.93f
        binding.layoutDieContent.animate()
            .rotation(angle)
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(80)
            .start()
    }

    fun animateFinalBounce() {
        binding.layoutDieContent.rotation = 0f
        binding.layoutDieContent.scaleX = 1.15f
        binding.layoutDieContent.scaleY = 1.15f
        binding.layoutDieContent.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(220)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}
