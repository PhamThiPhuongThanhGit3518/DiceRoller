package com.phuongthanh.diceroller.features.shakeroll.ui

import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentSelectGameModeBinding

class SelectGameModeFragment : BaseFragment<FragmentSelectGameModeBinding>(FragmentSelectGameModeBinding::inflate) {

    override fun initViews() {
        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        binding.cardMultiplePeople.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_selectGameModeFragment_to_classicPlayerFragment)
        }

        binding.cardAlone.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_selectGameModeFragment_to_freeRollFragment)
        }
    }
}
