package com.phuongthanh.diceroller.features.intro

import android.animation.ValueAnimator
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.MainActivity
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseActivity
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.ActivityIntroBinding
import kotlinx.coroutines.launch

class IntroActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {

    private val slides = listOf(
        IntroSlide(R.string.intro_title_1, R.string.intro_desc_1, R.drawable.ic_onboarding_dice),
        IntroSlide(R.string.intro_title_2, R.string.intro_desc_2, R.drawable.ic_onboarding_party),
        IntroSlide(R.string.intro_title_3, R.string.intro_desc_3, R.drawable.ic_dice_d6),
        IntroSlide(R.string.intro_title_4, R.string.intro_desc_4, R.drawable.ic_onboarding_wheel)
    )

    private val indicatorViews = mutableListOf<View>()
    private var currentPosition = 0

    override fun initViews() {
        val adapter = IntroSlideAdapter(slides)
        binding.viewPager.adapter = adapter

        setupIndicators()
        updateContent(0, animate = false)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != currentPosition) {
                    updateIndicators(position)
                    updateContent(position, animate = true)
                    currentPosition = position
                }
            }
        })

        binding.btnNext.setOnDebouncedClickListener {
            if (currentPosition < slides.lastIndex) {
                binding.viewPager.currentItem = currentPosition + 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun setupIndicators() {
        binding.layoutIndicators.removeAllViews()
        indicatorViews.clear()

        val density = resources.displayMetrics.density
        val size9dp = (9 * density).toInt()
        val size26dp = (26 * density).toInt()
        val margin4dp = (4 * density).toInt()

        for (i in slides.indices) {
            val view = View(this).apply {
                val initialWidth = if (i == 0) size26dp else size9dp
                layoutParams = LinearLayout.LayoutParams(initialWidth, size9dp).apply {
                    setMargins(margin4dp, 0, margin4dp, 0)
                }
                setBackgroundResource(
                    if (i == 0) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive
                )
            }
            binding.layoutIndicators.addView(view)
            indicatorViews.add(view)
        }
    }

    private fun updateIndicators(newPos: Int) {
        val density = resources.displayMetrics.density
        val size9dp = (9 * density).toInt()
        val size26dp = (26 * density).toInt()

        for (i in indicatorViews.indices) {
            val view = indicatorViews[i]
            val isCurrent = (i == newPos)
            val targetWidth = if (isCurrent) size26dp else size9dp
            val startWidth = view.layoutParams.width

            if (startWidth != targetWidth) {
                ValueAnimator.ofInt(startWidth, targetWidth).apply {
                    duration = 200L
                    addUpdateListener { animator ->
                        view.layoutParams = view.layoutParams.apply {
                            width = animator.animatedValue as Int
                        }
                    }
                    start()
                }
            }
            view.setBackgroundResource(
                if (isCurrent) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive
            )
        }
    }

    private fun updateContent(position: Int, animate: Boolean) {
        val slide = slides[position]
        val isLast = (position == slides.lastIndex)
        binding.btnNext.text = getString(if (isLast) R.string.btn_start else R.string.btn_next)

        if (animate) {
            binding.tvTitle.animate().alpha(0f).setDuration(120L).withEndAction {
                binding.tvTitle.setText(slide.titleRes)
                binding.tvTitle.animate().alpha(1f).setDuration(120L).start()
            }.start()

            binding.tvDescription.animate().alpha(0f).setDuration(120L).withEndAction {
                binding.tvDescription.setText(slide.descRes)
                binding.tvDescription.animate().alpha(1f).setDuration(120L).start()
            }.start()
        } else {
            binding.tvTitle.setText(slide.titleRes)
            binding.tvDescription.setText(slide.descRes)
        }
    }

    private fun finishOnboarding() {
        lifecycleScope.launch {
            val appPreferences = (application as DiceRollerApplication).appContainer.appPreferences
            appPreferences.setFirstOpen(false)

            val intent = Intent(this@IntroActivity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }
    }
}
