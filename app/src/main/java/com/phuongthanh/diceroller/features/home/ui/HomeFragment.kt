package com.phuongthanh.diceroller.features.home.ui

import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentHomeBinding
import com.phuongthanh.diceroller.features.customdice.ui.CustomDicePickerBottomSheet
import com.phuongthanh.diceroller.features.settings.ui.PremiumBottomSheet

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun initViews() {
        binding.btnCrown.setOnDebouncedClickListener {
            PremiumBottomSheet().show(childFragmentManager, "PremiumBottomSheet")
        }

        binding.btnSettings.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_settingsFragment)
        }

        binding.btnCtaPlay.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_selectGameModeFragment)
        }

        binding.cardCategoryCustomDice.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_customRollHomeFragment)
        }

        binding.cardCategoryPlayWithFriends.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_classicPlayerFragment)
        }

        binding.cardCategoryPartyChallenge.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_partyFragment)
        }

        binding.cardRecDrinkingNight.setOnDebouncedClickListener {
            val bundle = android.os.Bundle().apply { putString("packId", "drinking_night") }
            findNavController().safeNavigate(R.id.action_homeFragment_to_partyDetailFragment, bundle)
        }

        binding.cardRecClassicParty.setOnDebouncedClickListener {
            val bundle = android.os.Bundle().apply { putString("packId", "classic_party") }
            findNavController().safeNavigate(R.id.action_homeFragment_to_partyDetailFragment, bundle)
        }
    }
}
