package com.phuongthanh.diceroller.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.phuongthanh.diceroller.data.local.datastore.AppPreferences
import com.phuongthanh.diceroller.features.settings.model.RollHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class HistoryRepository(private val appPreferences: AppPreferences) {

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<RollHistoryItem>>() {}.type

    val historyListFlow: Flow<List<RollHistoryItem>> = appPreferences.rollHistoryJsonFlow.map { json ->
        if (json.isBlank()) {
            emptyList()
        } else {
            try {
                gson.fromJson<List<RollHistoryItem>>(json, typeToken) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun addHistoryEntry(item: RollHistoryItem) {
        val currentJson = appPreferences.rollHistoryJsonFlow.first()
        val currentList = if (currentJson.isBlank()) {
            mutableListOf()
        } else {
            try {
                gson.fromJson<List<RollHistoryItem>>(currentJson, typeToken)?.toMutableList() ?: mutableListOf()
            } catch (_: Exception) {
                mutableListOf()
            }
        }

        currentList.add(0, item) // Insert latest first
        val trimmed = if (currentList.size > 50) currentList.take(50) else currentList
        appPreferences.setRollHistoryJson(gson.toJson(trimmed))
    }

    suspend fun clearHistory() {
        appPreferences.setRollHistoryJson("")
    }
}
