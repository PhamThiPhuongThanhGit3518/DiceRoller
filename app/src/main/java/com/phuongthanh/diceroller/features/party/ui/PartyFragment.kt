package com.phuongthanh.diceroller.features.party.ui

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentPartyBinding
import com.phuongthanh.diceroller.features.party.model.PartyChallengePack
import com.phuongthanh.diceroller.features.party.viewmodel.PartyViewModel

class PartyFragment : BaseFragment<FragmentPartyBinding>(FragmentPartyBinding::inflate) {

    private val viewModel: PartyViewModel by viewModels {
        PartyViewModel.Factory()
    }

    private lateinit var recentAdapter: PartyRecentAdapter
    private lateinit var recommendedAdapter: PartyRecommendedAdapter

    override fun initViews() {
        setupTopBar()
        setupAdapters()
        observeViewModel()
    }

    private fun setupTopBar() {
        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        binding.topBar.setRightAction1 {
            // Future VIP upgrade or perks
        }

        binding.topBar.setRightAction2 {
            findNavController().safeNavigate(R.id.action_partyFragment_to_diceSettingsFragment)
        }
    }

    private fun setupAdapters() {
        recentAdapter = PartyRecentAdapter { pack ->
            navigateToDetail(pack.id)
        }
        binding.rvRecentChallenges.adapter = recentAdapter

        recommendedAdapter = PartyRecommendedAdapter { pack ->
            navigateToDetail(pack.id)
        }
        binding.rvRecommendedChallenges.adapter = recommendedAdapter
    }

    private fun observeViewModel() {
        viewModel.uiState.collectWithLifecycle(viewLifecycleOwner) { state ->
            // Bind Featured Card
            state.featuredPack?.let { pack ->
                bindFeaturedCard(pack)
            }

            // Bind Recent List
            recentAdapter.submitList(state.recentPacks)

            // Bind Recommended List
            recommendedAdapter.submitList(state.recommendedPacks)
        }
    }

    private fun bindFeaturedCard(pack: PartyChallengePack) {
        binding.tvFeaturedTitle.setText(pack.nameRes)
        binding.tvFeaturedDesc.setText(pack.descRes)

        binding.tvFeaturedTag1.text = getString(R.string.challenges_count_format, pack.challengesCount)
        binding.tvFeaturedTag2.text = getString(R.string.players_range_format, pack.playersRange)
        binding.tvFeaturedTag3.text = when (pack.difficulty.lowercase()) {
            "easy" -> getString(R.string.difficulty_easy)
            "medium" -> getString(R.string.difficulty_medium)
            "hard" -> getString(R.string.difficulty_hard)
            else -> pack.difficulty
        }

        binding.btnFeaturedPlayNow.setOnDebouncedClickListener {
            navigateToDetail(pack.id)
        }

        binding.cardFeatured.setOnDebouncedClickListener {
            navigateToDetail(pack.id)
        }
    }

    private fun navigateToDetail(packId: String) {
        val bundle = Bundle().apply {
            putString("packId", packId)
        }
        findNavController().safeNavigate(R.id.action_partyFragment_to_partyDetailFragment, bundle)
    }
}
