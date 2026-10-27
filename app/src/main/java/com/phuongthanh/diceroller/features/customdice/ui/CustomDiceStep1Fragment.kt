package com.phuongthanh.diceroller.features.customdice.ui

import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentCustomDiceStep1Binding

class CustomDiceStep1Fragment : BaseFragment<FragmentCustomDiceStep1Binding>(FragmentCustomDiceStep1Binding::inflate) {

    private var diceCount = 3

    override fun initViews() {
        updateDicePreview()

        binding.btnBack.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        binding.btnMinusDice.setOnDebouncedClickListener {
            if (diceCount > 1) {
                diceCount--
                updateDicePreview()
            }
        }

        binding.btnPlusDice.setOnDebouncedClickListener {
            if (diceCount < 6) {
                diceCount++
                updateDicePreview()
            }
        }

        binding.btnContinue.setOnDebouncedClickListener {
            val bundle = Bundle().apply {
                putInt("diceCount", diceCount)
            }
            findNavController().safeNavigate(
                R.id.action_customDiceStep1Fragment_to_customDiceStep2Fragment,
                bundle
            )
        }
    }

    private fun updateDicePreview() {
        binding.tvDiceCount.text = diceCount.toString()
        binding.layoutDicePreview.removeAllViews()

        val density = resources.displayMetrics.density
        val size64dp = (64 * density).toInt()
        val margin8dp = (8 * density).toInt()

        for (i in 1..diceCount) {
            val diceCard = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(size64dp, size64dp).apply {
                    setMargins(margin8dp, 0, margin8dp, 0)
                }
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_nav_pill)
                elevation = 2 * density
            }

            val icon = ImageView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams((32 * density).toInt(), (32 * density).toInt(), Gravity.CENTER)
                setImageResource(R.drawable.ic_dice_d6)
                setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
            }

            diceCard.addView(icon)
            binding.layoutDicePreview.addView(diceCard)
        }
    }
}
