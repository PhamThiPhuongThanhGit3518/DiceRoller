package com.phuongthanh.diceroller.features.roulette.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.data.repository.RouletteRepository
import com.phuongthanh.diceroller.features.roulette.model.WheelPreset
import com.phuongthanh.diceroller.features.roulette.model.WheelSector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class RouletteViewModel(
    private val repository: RouletteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouletteUiState())
    val uiState: StateFlow<RouletteUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<RouletteUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentAccumulatedAngle = 0f

    init {
        viewModelScope.launch {
            repository.presetsFlow.collectLatest { list ->
                _uiState.update { state ->
                    val currentId = state.currentPreset?.id
                    val updatedCurrent = list.find { it.id == currentId } ?: list.firstOrNull()
                    state.copy(
                        presets = list,
                        currentPreset = updatedCurrent
                    )
                }
            }
        }
    }

    fun processIntent(intent: RouletteIntent) {
        when (intent) {
            is RouletteIntent.SelectPreset -> selectPreset(intent.presetId)
            is RouletteIntent.SavePreset -> savePreset(intent.preset)
            is RouletteIntent.DuplicatePreset -> duplicatePreset(intent.presetId)
            is RouletteIntent.DeletePreset -> deletePreset(intent.presetId)
            is RouletteIntent.UpdateSectors -> updateSectors(intent.newSectors)
            is RouletteIntent.Spin -> spinWheel()
            is RouletteIntent.OnSpinCompleted -> onSpinCompleted(intent.winner)
            is RouletteIntent.PlayTick -> {
                viewModelScope.launch {
                    _uiEffect.send(RouletteUiEffect.PlayTickSoundAndHaptic)
                }
            }
        }
    }

    private fun selectPreset(presetId: String) {
        val target = _uiState.value.presets.find { it.id == presetId } ?: return
        _uiState.update {
            it.copy(currentPreset = target, winnerSector = null)
        }
    }

    private fun savePreset(preset: WheelPreset) {
        viewModelScope.launch {
            repository.savePreset(preset)
        }
    }

    private fun duplicatePreset(presetId: String) {
        viewModelScope.launch {
            val duplicate = repository.duplicatePreset(presetId)
            if (duplicate != null) {
                _uiEffect.send(RouletteUiEffect.ShowToast(R.string.roulette_duplicated_suffix, duplicate.title))
            }
        }
    }

    private fun deletePreset(presetId: String) {
        viewModelScope.launch {
            val success = repository.deletePreset(presetId)
            if (!success) {
                _uiEffect.send(RouletteUiEffect.ShowToast(R.string.roulette_min_preset_error))
            }
        }
    }

    private fun updateSectors(newSectors: List<WheelSector>) {
        val current = _uiState.value.currentPreset ?: return
        val updated = current.copy(sectors = newSectors)
        savePreset(updated)
    }

    private fun spinWheel() {
        if (_uiState.value.isSpinning) return
        val sectors = _uiState.value.currentPreset?.sectors.orEmpty()
        if (sectors.isEmpty()) return

        val count = sectors.size
        val sweep = 360f / count

        val winningIndex = Random.nextInt(count)
        val winner = sectors[winningIndex]

        val sectorCenter = (winningIndex + 0.5f) * sweep
        val neededNormAngle = ((270f - sectorCenter) % 360f + 360f) % 360f
        val currentNorm = ((currentAccumulatedAngle % 360f) + 360f) % 360f
        var diff = neededNormAngle - currentNorm
        if (diff <= 0) diff += 360f

        val jitter = (Random.nextFloat() * 0.5f - 0.25f) * sweep
        val fullSpins = Random.nextInt(5, 8) * 360f
        val targetAngle = currentAccumulatedAngle + fullSpins + diff + jitter
        currentAccumulatedAngle = targetAngle

        _uiState.update { it.copy(isSpinning = true, winnerSector = null) }

        viewModelScope.launch {
            _uiEffect.send(RouletteUiEffect.SpinToAngle(targetAngle, winner))
        }
    }

    private fun onSpinCompleted(winner: WheelSector) {
        _uiState.update { it.copy(isSpinning = false, winnerSector = winner) }
        viewModelScope.launch {
            _uiEffect.send(RouletteUiEffect.PlayWinnerFanfare)
        }
    }

    class Factory(private val repository: RouletteRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RouletteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RouletteViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
