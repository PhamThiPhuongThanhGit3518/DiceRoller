package com.phuongthanh.diceroller.features.settings.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetHowToRollBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HowToRollBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHowToRollBinding? = null
    private val binding get() = _binding!!

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private var isClassic: Boolean = false
    private var currentSensitivity: Int = 1
    private var isProgrammaticUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isClassic = arguments?.getBoolean(ARG_IS_CLASSIC) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHowToRollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isClassic) {
            binding.cardTotal.visibility = View.GONE
        } else {
            binding.cardTotal.visibility = View.VISIBLE
        }

        loadInitialPreferences()
        setupListeners()
    }

    private fun loadInitialPreferences() {
        viewLifecycleOwner.lifecycleScope.launch {
            val prefs = appContainer.appPreferences
            val shake = prefs.shakeEnabledFlow.first()
            currentSensitivity = prefs.shakeSensitivityFlow.first()
            val sound = prefs.soundEnabledFlow.first()
            val vibrate = prefs.hapticEnabledFlow.first()
            val total = prefs.showTotalScoreFlow.first()

            isProgrammaticUpdate = true
            binding.swShake.isChecked = shake
            binding.layoutSensitivity.visibility = if (shake) View.VISIBLE else View.GONE
            updateSensitivityUi(currentSensitivity)
            binding.swSound.isChecked = sound
            binding.swVibrate.isChecked = vibrate
            binding.swTotal.isChecked = total
            isProgrammaticUpdate = false
        }
    }

    private fun setupListeners() {
        binding.swShake.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutSensitivity.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isProgrammaticUpdate) {
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setShakeEnabled(isChecked)
                }
            }
        }

        binding.btnSensLow.setOnDebouncedClickListener {
            setSensitivity(0)
        }

        binding.btnSensMed.setOnDebouncedClickListener {
            setSensitivity(1)
        }

        binding.btnSensHigh.setOnDebouncedClickListener {
            setSensitivity(2)
        }

        binding.swSound.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticUpdate) {
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setSoundEnabled(isChecked)
                }
            }
        }

        binding.swVibrate.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticUpdate) {
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setHapticEnabled(isChecked)
                }
            }
        }

        binding.swTotal.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticUpdate) {
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.appPreferences.setShowTotalScore(isChecked)
                }
            }
        }

        binding.btnDone.setOnDebouncedClickListener {
            dismiss()
        }
    }

    private fun setSensitivity(level: Int) {
        currentSensitivity = level
        updateSensitivityUi(level)
        viewLifecycleOwner.lifecycleScope.launch {
            appContainer.appPreferences.setShakeSensitivity(level)
        }
    }

    private fun updateSensitivityUi(selected: Int) {
        val activeBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_create_game)
        val inactiveBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_white_pill_card)
        val activeTextColor = Color.WHITE
        val inactiveTextColor = Color.parseColor("#0F172A")

        binding.btnSensLow.background = if (selected == 0) activeBg else inactiveBg
        binding.btnSensLow.setTextColor(if (selected == 0) activeTextColor else inactiveTextColor)

        binding.btnSensMed.background = if (selected == 1) activeBg else inactiveBg
        binding.btnSensMed.setTextColor(if (selected == 1) activeTextColor else inactiveTextColor)

        binding.btnSensHigh.background = if (selected == 2) activeBg else inactiveBg
        binding.btnSensHigh.setTextColor(if (selected == 2) activeTextColor else inactiveTextColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HowToRollBottomSheet"
        private const val ARG_IS_CLASSIC = "arg_is_classic"

        fun newInstance(isClassic: Boolean = false): HowToRollBottomSheet {
            return HowToRollBottomSheet().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_CLASSIC, isClassic)
                }
            }
        }
    }
}
