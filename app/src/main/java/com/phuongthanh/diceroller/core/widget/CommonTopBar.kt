package com.phuongthanh.diceroller.core.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.databinding.ViewCommonTopbarBinding

data class ActionItem(
    @param:DrawableRes val iconRes: Int,
    val onClick: (View) -> Unit
)

class CommonTopBar @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    val binding = ViewCommonTopbarBinding.inflate(LayoutInflater.from(ctx), this, true)
    private val baseHeightPx = (56 * resources.displayMetrics.density).toInt()

    init {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.layoutTopBarRoot.updateLayoutParams { height = baseHeightPx + statusBarTop }
            binding.layoutTopBarRoot.updatePadding(top = statusBarTop, bottom = 0)
            insets
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.requestApplyInsets(this)
    }

    fun setOnBackClickListener(listener: (View) -> Unit) {
        binding.btnBack.isVisible = true
        binding.btnBack.setOnClickListener(listener)
    }

    fun hideBackButton() {
        binding.btnBack.isGone = true
    }

    fun setTitle(title: String) {
        binding.tvTitle.isVisible = true
        binding.layoutEvenlySpacedActions.isGone = true
        binding.tvTitle.text = title
    }

    fun setRightAction1(@DrawableRes iconRes: Int, onClick: (View) -> Unit) {
        binding.btnAction1.apply {
            isVisible = true
            setImageResource(iconRes)
            setOnClickListener(onClick)
        }
    }

    fun setRightAction2(@DrawableRes iconRes: Int, onClick: (View) -> Unit) {
        binding.btnAction2.apply {
            isVisible = true
            setImageResource(iconRes)
            setOnClickListener(onClick)
        }
    }

    fun hideRightActions() {
        binding.btnAction1.isGone = true
        binding.btnAction2.isGone = true
    }

    fun setEvenlySpacedActions(actions: List<ActionItem>, buttonSizeDp: Int = 40) {
        binding.btnBack.isGone = true
        binding.tvTitle.isGone = true
        binding.layoutRightActions.isGone = true
        binding.layoutEvenlySpacedActions.apply {
            isVisible = true
            removeAllViews()
            val sizePx = (buttonSizeDp * resources.displayMetrics.density).toInt()
            val iconPadding = if (buttonSizeDp <= 28) 0 else (6 * resources.displayMetrics.density).toInt()
            actions.forEachIndexed { index, item ->
                val btn = ImageButton(context).apply {
                    layoutParams = LinearLayout.LayoutParams(sizePx, sizePx)
                    val outValue = TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    setImageResource(item.iconRes)
                    setColorFilter(context.getColor(R.color.white))
                    setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setOnClickListener(item.onClick)
                }
                addView(btn)
                if (index < actions.size - 1) {
                    addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(0, 0, 1f) })
                }
            }
        }
    }
}
