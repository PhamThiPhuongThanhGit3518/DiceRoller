package com.phuongthanh.diceroller.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phuongthanh.diceroller.core.audio.SoundManager
import com.phuongthanh.diceroller.core.haptic.HapticManager
import com.phuongthanh.diceroller.core.sensor.ShakeDetector
import com.phuongthanh.diceroller.data.local.datastore.AppPreferences
import com.phuongthanh.diceroller.data.repository.HistoryRepository
import com.phuongthanh.diceroller.features.settings.model.RollHistoryItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val hapticIntensity: Int = 1,
    val shakeEnabled: Boolean = true,
    val shakeSensitivity: Int = 1,
    val keepScreenOn: Boolean = true,
    val diceSkin: String = "classic_white",
    val tableTheme: String = "felt_green",
    val historyList: List<RollHistoryItem> = emptyList()
)

class SettingsViewModel(
    private val appPreferences: AppPreferences,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager,
    private val shakeDetector: ShakeDetector,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            appPreferences.soundEnabledFlow,
            appPreferences.hapticEnabledFlow,
            appPreferences.hapticIntensityFlow,
            appPreferences.shakeEnabledFlow
        ) { sound, haptic, hIntensity, shake ->
            Tuple4(sound, haptic, hIntensity, shake)
        },
        combine(
            appPreferences.shakeSensitivityFlow,
            appPreferences.keepScreenOnFlow,
            appPreferences.diceSkinFlow,
            appPreferences.tableThemeFlow
        ) { sSensitivity, keepScreen, skin, table ->
            Tuple4(sSensitivity, keepScreen, skin, table)
        },
        historyRepository.historyListFlow
    ) { part1, part2, history ->
        SettingsUiState(
            soundEnabled = part1.v1,
            hapticEnabled = part1.v2,
            hapticIntensity = part1.v3,
            shakeEnabled = part1.v4,
            shakeSensitivity = part2.v1,
            keepScreenOn = part2.v2,
            diceSkin = part2.v3,
            tableTheme = part2.v4,
            historyList = history
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setSoundEnabled(enabled)
            soundManager.setMuted(!enabled)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setHapticEnabled(enabled)
            hapticManager.setEnabled(enabled)
        }
    }

    fun setHapticIntensity(level: Int) {
        viewModelScope.launch {
            appPreferences.setHapticIntensity(level)
            hapticManager.setIntensityLevel(level)
        }
    }

    fun setShakeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setShakeEnabled(enabled)
        }
    }

    fun setShakeSensitivity(level: Int) {
        viewModelScope.launch {
            appPreferences.setShakeSensitivity(level)
            shakeDetector.setSensitivityLevel(level)
        }
    }

    fun setKeepScreenOn(keep: Boolean) {
        viewModelScope.launch {
            appPreferences.setKeepScreenOn(keep)
        }
    }

    fun setDiceSkin(skin: String) {
        viewModelScope.launch {
            appPreferences.setDiceSkin(skin)
        }
    }

    fun setTableTheme(theme: String) {
        viewModelScope.launch {
            appPreferences.setTableTheme(theme)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    private data class Tuple4<A, B, C, D>(val v1: A, val v2: B, val v3: C, val v4: D)

    class Factory(
        private val appPreferences: AppPreferences,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager,
        private val shakeDetector: ShakeDetector,
        private val historyRepository: HistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                appPreferences,
                soundManager,
                hapticManager,
                shakeDetector,
                historyRepository
            ) as T
        }
    }
}
