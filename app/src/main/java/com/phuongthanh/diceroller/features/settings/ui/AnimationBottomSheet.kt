package com.phuongthanh.diceroller.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetAnimationSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AnimationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAnimationSettingsBinding? = null
    private val binding get() = _binding!!

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private var currentType: String = "none"
    private var currentSpeed: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAnimationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadInitialPreferences()
        setupListeners()
    }

    private fun loadInitialPreferences() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentType = appContainer.appPreferences.animationTypeFlow.first()
            currentSpeed = appContainer.appPreferences.animationSpeedFlow.first()
            updateTypeUi()
            updateSpeedUi()
        }
    }

    private fun setupListeners() {
        // Animation Type Options
        binding.btnTypeFullLeft.setOnDebouncedClickListener {
            selectAnimationType("full_left")
        }

        binding.btnTypeFullRight.setOnDebouncedClickListener {
            selectAnimationType("full_right")
        }

        binding.btnTypeNone.setOnDebouncedClickListener {
            selectAnimationType("none")
        }

        // Animation Speed Options
        binding.btnSpeedFast.setOnDebouncedClickListener {
            selectAnimationSpeed(0)
        }

        binding.btnSpeedNormal.setOnDebouncedClickListener {
            selectAnimationSpeed(1)
        }

        binding.btnSpeedSlow.setOnDebouncedClickListener {
            selectAnimationSpeed(2)
        }

        // Done Button
        binding.btnDone.setOnDebouncedClickListener {
            dismiss()
        }
    }

    private fun selectAnimationType(type: String) {
        currentType = type
        updateTypeUi()
        viewLifecycleOwner.lifecycleScope.launch {
            appContainer.appPreferences.setAnimationType(type)
        }
    }

    private fun selectAnimationSpeed(speed: Int) {
        currentSpeed = speed
        updateSpeedUi()
        viewLifecycleOwner.lifecycleScope.launch {
            appContainer.appPreferences.setAnimationSpeed(speed)
        }
    }

    private fun updateTypeUi() {
        val checked = R.drawable.ic_radio_checked_green
        val unchecked = R.drawable.ic_radio_unchecked_gray

        binding.ivRadioTypeFullLeft.setImageResource(if (currentType == "full_left") checked else unchecked)
        binding.ivRadioTypeFullRight.setImageResource(if (currentType == "full_right") checked else unchecked)
        binding.ivRadioTypeNone.setImageResource(if (currentType == "none") checked else unchecked)
    }

    private fun updateSpeedUi() {
        val checked = R.drawable.ic_radio_checked_green
        val unchecked = R.drawable.ic_radio_unchecked_gray

        binding.ivRadioSpeedFast.setImageResource(if (currentSpeed == 0) checked else unchecked)
        binding.ivRadioSpeedNormal.setImageResource(if (currentSpeed == 1) checked else unchecked)
        binding.ivRadioSpeedSlow.setImageResource(if (currentSpeed == 2) checked else unchecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AnimationBottomSheet"

        fun newInstance(): AnimationBottomSheet {
            return AnimationBottomSheet()
        }
    }
}
