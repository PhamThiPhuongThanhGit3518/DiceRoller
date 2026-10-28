package com.phuongthanh.diceroller.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.ViewCommonTopbarBinding

data class ActionItem(
    @param:DrawableRes val iconRes: Int,
    val onClick: (View) -> Unit
)

/**
 * CommonTopBar: Thanh tiêu đề dùng chung cho toàn bộ ứng dụng theo Figma Midnight Casino.
 * - Nền trong suốt (Transparent background).
 * - Tùy chọn không bắt buộc:
 *   + Nút Back (ic_back hình squircle 40x40dp).
 *   + Tiêu đề chính (Title, font Be Vietnam Pro, màu trắng).
 *   + Tiêu đề phụ (Subtitle, màu xám nhạt).
 *   + Tối đa 2 Action buttons bên phải (containerAction1, containerAction2).
 */
class CommonTopBar @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    val binding = ViewCommonTopbarBinding.inflate(LayoutInflater.from(ctx), this, true)

    val btnBack: ImageButton get() = binding.btnBack
    val tvTitle: TextView get() = binding.tvTitle
    val tvSubtitle: TextView get() = binding.tvSubtitle
    val btnAction1: ImageView get() = binding.btnAction1
    val btnAction2: ImageView get() = binding.btnAction2
    val containerAction1: FrameLayout get() = binding.containerAction1
    val containerAction2: FrameLayout get() = binding.containerAction2

    init {
        // Nền trong suốt mặc định
        setBackgroundResource(android.R.color.transparent)

        attrs?.let { attributeSet ->
            ctx.withStyledAttributes(attributeSet, R.styleable.CommonTopBar, defStyleAttr, 0) {
                // Title
                getString(R.styleable.CommonTopBar_topbarTitle)?.let {
                    setTitle(it)
                }

                // Subtitle
                getString(R.styleable.CommonTopBar_topbarSubtitle)?.let {
                    setSubtitle(it)
                }

                // Show/hide Back button (mặc định hiện)
                val showBack = getBoolean(R.styleable.CommonTopBar_showBackButton, true)
                showBackButton(showBack)

                // Action 1
                val action1Res = getResourceId(R.styleable.CommonTopBar_action1Icon, 0)
                if (action1Res != 0) {
                    setRightAction1(action1Res) {}
                }

                // Action 2
                val action2Res = getResourceId(R.styleable.CommonTopBar_action2Icon, 0)
                if (action2Res != 0) {
                    setRightAction2(action2Res) {}
                }

                // Status bar insets (nếu cần tự động cộng padding statusBar)
                val applyInsets = getBoolean(R.styleable.CommonTopBar_applyStatusBarInsets, false)
                if (applyInsets) {
                    applyStatusBarPadding()
                }
            }
        }
    }

    private fun applyStatusBarPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.layoutTopBarRoot.updatePadding(
                top = statusBarTop + (12 * resources.displayMetrics.density).toInt()
            )
            insets
        }
    }

    // --- Back Button ---
    fun setOnBackClickListener(listener: (View) -> Unit) {
        binding.btnBack.isVisible = true
        binding.btnBack.setOnDebouncedClickListener { listener(it) }
    }

    fun showBackButton(show: Boolean = true) {
        binding.btnBack.isVisible = show
    }

    fun hideBackButton() {
        binding.btnBack.isGone = true
    }

    // --- Title ---
    fun setTitle(title: CharSequence?) {
        if (!title.isNullOrEmpty()) {
            binding.tvTitle.text = title
            binding.tvTitle.isVisible = true
            binding.layoutTitleGroup.isVisible = true
        } else {
            binding.tvTitle.text = ""
            binding.tvTitle.isGone = true
            checkTitleGroupVisibility()
        }
    }

    fun setTitle(@StringRes titleRes: Int) {
        setTitle(context.getString(titleRes))
    }

    // --- Subtitle ---
    fun setSubtitle(subtitle: CharSequence?) {
        if (!subtitle.isNullOrEmpty()) {
            binding.tvSubtitle.text = subtitle
            binding.tvSubtitle.isVisible = true
            binding.layoutTitleGroup.isVisible = true
        } else {
            binding.tvSubtitle.text = ""
            binding.tvSubtitle.isGone = true
            checkTitleGroupVisibility()
        }
    }

    fun setSubtitle(@StringRes subtitleRes: Int) {
        setSubtitle(context.getString(subtitleRes))
    }

    private fun checkTitleGroupVisibility() {
        val hasTitle = binding.tvTitle.isVisible && binding.tvTitle.text.isNotEmpty()
        val hasSubtitle = binding.tvSubtitle.isVisible && binding.tvSubtitle.text.isNotEmpty()
        binding.layoutTitleGroup.isVisible = hasTitle || hasSubtitle
    }

    // --- Right Actions (Tối đa 2 actions) ---
    fun setRightAction1(@DrawableRes iconRes: Int, onClick: (View) -> Unit) {
        binding.btnAction1.setImageResource(iconRes)
        binding.containerAction1.isVisible = true
        binding.containerAction1.setOnDebouncedClickListener { onClick(it) }
    }

    fun setRightAction1(onClick: (View) -> Unit) {
        binding.containerAction1.isVisible = true
        binding.containerAction1.setOnDebouncedClickListener { onClick(it) }
    }

    fun setRightAction2(@DrawableRes iconRes: Int, onClick: (View) -> Unit) {
        binding.btnAction2.setImageResource(iconRes)
        binding.containerAction2.isVisible = true
        binding.containerAction2.setOnDebouncedClickListener { onClick(it) }
    }

    fun setRightAction2(onClick: (View) -> Unit) {
        binding.containerAction2.isVisible = true
        binding.containerAction2.setOnDebouncedClickListener { onClick(it) }
    }

    fun hideRightAction1() {
        binding.containerAction1.isGone = true
    }

    fun hideRightAction2() {
        binding.containerAction2.isGone = true
    }

    fun hideRightActions() {
        binding.containerAction1.isGone = true
        binding.containerAction2.isGone = true
    }

    fun setAction1ContentDescription(description: String) {
        binding.btnAction1.contentDescription = description
    }

    fun setAction2ContentDescription(description: String) {
        binding.btnAction2.contentDescription = description
    }
}
