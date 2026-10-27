package com.phuongthanh.diceroller.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.phuongthanh.diceroller.data.local.datastore.AppPreferences
import com.phuongthanh.diceroller.features.customdice.model.CustomDice
import com.phuongthanh.diceroller.features.customdice.model.CustomDicePresetsBank
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CustomDiceRepository(private val appPreferences: AppPreferences) {

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<CustomDice>>() {}.type

    val customDiceListFlow: Flow<List<CustomDice>> = appPreferences.customDiceJsonFlow.map { json ->
        if (json.isBlank()) {
            emptyList()
        } else {
            try {
                gson.fromJson<List<CustomDice>>(json, typeToken) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun getCurrentList(): List<CustomDice> {
        val json = appPreferences.customDiceJsonFlow.first()
        return if (json.isBlank()) {
            emptyList()
        } else {
            try {
                gson.fromJson<List<CustomDice>>(json, typeToken) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getCustomDice(id: String): CustomDice? {
        return getCurrentList().firstOrNull { it.id == id }
    }

    suspend fun saveCustomDice(dice: CustomDice) {
        val currentList = getCurrentList().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == dice.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = dice
        } else {
            currentList.add(dice)
        }
        appPreferences.setCustomDiceJson(gson.toJson(currentList))
    }

    suspend fun deleteCustomDice(id: String) {
        val currentList = getCurrentList().toMutableList()
        currentList.removeAll { it.id == id } // Allows deleting presets as requested
        appPreferences.setCustomDiceJson(gson.toJson(currentList))
    }

    suspend fun resetToPresets() {
        appPreferences.setCustomDiceJson(gson.toJson(CustomDicePresetsBank.presets))
    }
}
