package com.phuongthanh.diceroller.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.phuongthanh.diceroller.data.local.datastore.AppPreferences
import com.phuongthanh.diceroller.features.roulette.model.WheelPreset
import com.phuongthanh.diceroller.features.roulette.model.WheelSector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class RouletteRepository(private val appPreferences: AppPreferences) {

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<WheelPreset>>() {}.type

    companion object {
        val defaultPresets = listOf(
            WheelPreset(
                id = "where_go",
                title = "Where Should You Go Today?",
                sectors = listOf(
                    WheelSector("1", "Cinema", "#10B981"),
                    WheelSector("2", "Coffee Shop", "#3B82F6"),
                    WheelSector("3", "Shopping Mall", "#F59E0B"),
                    WheelSector("4", "Park", "#EF4444"),
                    WheelSector("5", "Beach", "#06B6D4"),
                    WheelSector("6", "Stay Home", "#8B5CF6"),
                    WheelSector("7", "Gym", "#EC4899"),
                    WheelSector("8", "Library", "#14B8A6")
                )
            ),
            WheelPreset(
                id = "yes_no",
                title = "Yes or No",
                sectors = listOf(
                    WheelSector("1", "YES", "#10B981"),
                    WheelSector("2", "NO", "#EF4444"),
                    WheelSector("3", "MAYBE", "#F59E0B"),
                    WheelSector("4", "RE-SPIN", "#3B82F6")
                )
            ),
            WheelPreset(
                id = "what_eat",
                title = "What Should You Eat Today?",
                sectors = listOf(
                    WheelSector("1", "Pizza", "#EF4444"),
                    WheelSector("2", "Phở", "#3B82F6"),
                    WheelSector("3", "Cơm tấm", "#10B981"),
                    WheelSector("4", "Bún bò", "#F59E0B"),
                    WheelSector("5", "Gà rán", "#8B5CF6"),
                    WheelSector("6", "Lẩu", "#EC4899"),
                    WheelSector("7", "Trà sữa", "#06B6D4"),
                    WheelSector("8", "Nhịn ăn", "#64748B")
                )
            )
        )
    }

    val presetsFlow: Flow<List<WheelPreset>> = appPreferences.roulettePresetsJsonFlow.map { json ->
        if (json.isBlank()) {
            defaultPresets
        } else {
            try {
                gson.fromJson<List<WheelPreset>>(json, typeToken) ?: defaultPresets
            } catch (_: Exception) {
                defaultPresets
            }
        }
    }

    suspend fun getPresets(): List<WheelPreset> {
        val json = appPreferences.roulettePresetsJsonFlow.first()
        return if (json.isBlank()) {
            defaultPresets
        } else {
            try {
                gson.fromJson<List<WheelPreset>>(json, typeToken) ?: defaultPresets
            } catch (_: Exception) {
                defaultPresets
            }
        }
    }

    suspend fun getPresetById(id: String): WheelPreset? {
        return getPresets().find { it.id == id }
    }

    suspend fun savePreset(preset: WheelPreset) {
        val current = getPresets().toMutableList()
        val index = current.indexOfFirst { it.id == preset.id }
        if (index >= 0) {
            current[index] = preset
        } else {
            current.add(preset)
        }
        appPreferences.setRoulettePresetsJson(gson.toJson(current))
    }

    suspend fun duplicatePreset(presetId: String): WheelPreset? {
        val current = getPresets().toMutableList()
        val index = current.indexOfFirst { it.id == presetId }
        if (index < 0) return null

        val original = current[index]
        val duplicate = original.copy(
            id = UUID.randomUUID().toString(),
            title = "${original.title} (Copy)",
            sectors = original.sectors.map { it.copy(id = UUID.randomUUID().toString()) }
        )
        current.add(index + 1, duplicate)
        appPreferences.setRoulettePresetsJson(gson.toJson(current))
        return duplicate
    }

    suspend fun deletePreset(presetId: String): Boolean {
        val current = getPresets().toMutableList()
        if (current.size <= 1) return false
        val removed = current.removeAll { it.id == presetId }
        if (removed) {
            appPreferences.setRoulettePresetsJson(gson.toJson(current))
        }
        return removed
    }

    suspend fun resetToDefaults() {
        appPreferences.setRoulettePresetsJson(gson.toJson(defaultPresets))
    }
}
