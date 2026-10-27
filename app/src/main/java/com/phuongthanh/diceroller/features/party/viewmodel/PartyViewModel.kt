package com.phuongthanh.diceroller.features.party.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phuongthanh.diceroller.features.party.model.PartyChallenge
import com.phuongthanh.diceroller.features.party.model.PartyChallengePack
import com.phuongthanh.diceroller.features.party.model.PartyPacksBank
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PartyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        PartyUiState(
            featuredPack = PartyPacksBank.getFeaturedPack(),
            recentPacks = PartyPacksBank.getRecentPacks(),
            recommendedPacks = PartyPacksBank.getRecommendedPacks(),
            selectedPack = PartyPacksBank.getFeaturedPack()
        )
    )
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<PartyUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun processIntent(intent: PartyIntent) {
        when (intent) {
            is PartyIntent.SelectPack -> selectPack(intent.packId)
            is PartyIntent.RollChallenge -> rollChallenge(intent.packId)
        }
    }

    private fun selectPack(packId: String) {
        val pack = PartyPacksBank.getPackById(packId)
        _uiState.update { it.copy(selectedPack = pack) }
    }

    fun rollChallenge(packId: String) {
        if (_uiState.value.isRolling) return
        val pack = PartyPacksBank.getPackById(packId)

        viewModelScope.launch {
            _uiState.update { it.copy(isRolling = true, drawnChallenge = null) }
            _uiEffect.send(PartyUiEffect.PlayRollSoundAndHaptic)

            delay(350)

            val challenge = PartyPacksBank.getRandomChallenge(packId)
            _uiState.update {
                it.copy(
                    isRolling = false,
                    drawnChallenge = challenge
                )
            }
            _uiEffect.send(PartyUiEffect.ShowChallenge(pack.name, challenge))
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PartyViewModel() as T
        }
    }
}
