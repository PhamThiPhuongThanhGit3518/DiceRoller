package com.phuongthanh.diceroller.features.party.ui

import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentPartyDetailBinding
import com.phuongthanh.diceroller.features.party.model.PartyChallengePack
import com.phuongthanh.diceroller.features.party.model.PartyPacksBank

class PartyDetailFragment : BaseFragment<FragmentPartyDetailBinding>(FragmentPartyDetailBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private lateinit var currentPack: PartyChallengePack
    private lateinit var recAdapter: PartyRecentAdapter

    override fun initViews() {
        val packId = arguments?.getString("packId") ?: "funny_dare"
        currentPack = PartyPacksBank.getPackById(packId)

        setupTopBar()
        setupRecommendedList()
        bindPackDetails(currentPack)

        binding.btnStartRolling.setOnDebouncedClickListener {
            rollDiceAndShowChallenge()
        }
    }

    private fun setupTopBar() {
        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecommendedList() {
        recAdapter = PartyRecentAdapter { otherPack ->
            currentPack = otherPack
            bindPackDetails(otherPack)
            updateRecommendedList()
        }
        binding.rvDetailRecommended.adapter = recAdapter
        updateRecommendedList()
    }

    private fun updateRecommendedList() {
        val otherPacks = PartyPacksBank.getAllPacks().filter { it.id != currentPack.id }
        recAdapter.submitList(otherPacks)
    }

    private fun bindPackDetails(pack: PartyChallengePack) {
        binding.topBar.setSubtitle(getString(pack.nameRes))
        binding.tvPackTitle.setText(pack.nameRes)
        binding.tvPackDesc.setText(pack.descRes)

        binding.tvStatChallengesCount.text = pack.challengesCount.toString()
        binding.tvStatPlayersRange.text = pack.playersRange
        binding.tvStatDifficulty.text = when (pack.difficulty.lowercase()) {
            "easy" -> getString(R.string.difficulty_easy)
            "medium" -> getString(R.string.difficulty_medium)
            "hard" -> getString(R.string.difficulty_hard)
            else -> pack.difficulty
        }
    }

    private fun rollDiceAndShowChallenge() {
        val bundle = android.os.Bundle().apply {
            putString("packId", currentPack.id)
        }
        findNavController().safeNavigate(
            R.id.action_partyDetailFragment_to_partyRollPlayFragment,
            bundle
        )
    }
}
