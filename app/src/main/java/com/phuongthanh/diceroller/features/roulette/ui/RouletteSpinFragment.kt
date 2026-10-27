package com.phuongthanh.diceroller.features.roulette.ui

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentRouletteSpinBinding
import com.phuongthanh.diceroller.features.roulette.viewmodel.RouletteIntent
import com.phuongthanh.diceroller.features.roulette.viewmodel.RouletteUiEffect
import com.phuongthanh.diceroller.features.roulette.viewmodel.RouletteViewModel

class RouletteSpinFragment : BaseFragment<FragmentRouletteSpinBinding>(FragmentRouletteSpinBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private val viewModel: RouletteViewModel by activityViewModels {
        RouletteViewModel.Factory(appContainer.rouletteRepository)
    }

    private var activePresetId: String = ""

    override fun initViews() {
        activePresetId = arguments?.getString(ARG_PRESET_ID).orEmpty()
        if (activePresetId.isNotEmpty()) {
            viewModel.processIntent(RouletteIntent.SelectPreset(activePresetId))
        }

        binding.btnBack.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        binding.btnEdit.setOnDebouncedClickListener {
            val bundle = Bundle().apply { putString("presetId", activePresetId) }
            findNavController().safeNavigate(R.id.action_rouletteSpinFragment_to_editWheelFragment, bundle)
        }

        binding.btnSpin.setOnDebouncedClickListener {
            viewModel.processIntent(RouletteIntent.Spin)
        }

        binding.wheelView.onCenterSpinClickListener = {
            viewModel.processIntent(RouletteIntent.Spin)
        }

        binding.wheelView.onSectorTickListener = {
            viewModel.processIntent(RouletteIntent.PlayTick)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.collectWithLifecycle(viewLifecycleOwner) { state ->
            val preset = state.currentPreset
            if (preset != null) {
                activePresetId = preset.id
                binding.tvQuestionTitle.text = preset.title
                binding.wheelView.setSectors(preset.sectors)
            }

            binding.btnSpin.isEnabled = !state.isSpinning
            binding.btnSpin.text = if (state.isSpinning) {
                getString(R.string.spinning_status)
            } else {
                getString(R.string.spin_action)
            }
        }

        viewModel.uiEffect.collectWithLifecycle(viewLifecycleOwner) { effect ->
            when (effect) {
                is RouletteUiEffect.SpinToAngle -> {
                    binding.wheelView.spinTo(effect.targetAngle) {
                        viewModel.processIntent(RouletteIntent.OnSpinCompleted(effect.winner))
                    }
                }
                is RouletteUiEffect.PlayTickSoundAndHaptic -> {
                    appContainer.soundManager.playWheelTick()
                    appContainer.hapticManager.performWheelTick()
                }
                is RouletteUiEffect.PlayWinnerFanfare -> {
                    appContainer.soundManager.playVictoryFanfare()
                    appContainer.hapticManager.performVictory()
                    val winner = viewModel.uiState.value.winnerSector
                    if (winner != null) {
                        RouletteResultDialog.newInstance(winner.text)
                            .show(childFragmentManager, RouletteResultDialog.TAG)
                    }
                }
                else -> Unit
            }
        }
    }

    companion object {
        const val ARG_PRESET_ID = "presetId"

        fun newBundle(presetId: String): Bundle {
            return Bundle().apply {
                putString(ARG_PRESET_ID, presetId)
            }
        }
    }
}
