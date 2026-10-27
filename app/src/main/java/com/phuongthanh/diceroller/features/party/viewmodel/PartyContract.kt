package com.phuongthanh.diceroller.features.party.viewmodel

import androidx.annotation.StringRes
import com.phuongthanh.diceroller.features.party.model.PartyChallenge
import com.phuongthanh.diceroller.features.party.model.PartyChallengePack

sealed interface PartyIntent {
    data class SelectPack(val packId: String) : PartyIntent
    data class RollChallenge(val packId: String) : PartyIntent
}

data class PartyUiState(
    val featuredPack: PartyChallengePack? = null,
    val recentPacks: List<PartyChallengePack> = emptyList(),
    val recommendedPacks: List<PartyChallengePack> = emptyList(),
    val selectedPack: PartyChallengePack? = null,
    val isRolling: Boolean = false,
    val drawnChallenge: PartyChallenge? = null
)

sealed interface PartyUiEffect {
    object PlayRollSoundAndHaptic : PartyUiEffect
    data class ShowChallenge(@StringRes val packNameRes: Int, val challenge: PartyChallenge) : PartyUiEffect
}
