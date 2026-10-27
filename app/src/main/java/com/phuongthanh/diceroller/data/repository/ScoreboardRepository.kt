package com.phuongthanh.diceroller.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.phuongthanh.diceroller.data.local.datastore.AppPreferences
import com.phuongthanh.diceroller.features.party.model.ScoreboardPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ScoreboardRepository(private val appPreferences: AppPreferences) {

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<ScoreboardPlayer>>() {}.type

    private val defaultPlayers = listOf(
        ScoreboardPlayer("1", "Player 1", 0),
        ScoreboardPlayer("2", "Player 2", 0),
        ScoreboardPlayer("3", "Player 3", 0)
    )

    val playersFlow: Flow<List<ScoreboardPlayer>> = appPreferences.scoreboardPlayersJsonFlow.map { json ->
        if (json.isBlank()) {
            defaultPlayers
        } else {
            try {
                gson.fromJson<List<ScoreboardPlayer>>(json, typeToken) ?: defaultPlayers
            } catch (_: Exception) {
                defaultPlayers
            }
        }
    }

    suspend fun savePlayers(players: List<ScoreboardPlayer>) {
        appPreferences.setScoreboardPlayersJson(gson.toJson(players))
    }
}
