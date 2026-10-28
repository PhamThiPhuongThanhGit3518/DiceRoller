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

        binding.topBar.setOnBackClickListener {
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
    }
}
