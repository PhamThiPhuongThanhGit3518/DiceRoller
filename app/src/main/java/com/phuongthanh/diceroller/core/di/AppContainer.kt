package com.phuongthanh.diceroller.core.di

import android.content.Context
import com.phuongthanh.diceroller.core.audio.SoundManager
import com.phuongthanh.diceroller.core.haptic.HapticManager
import com.phuongthanh.diceroller.core.sensor.ShakeDetector
import com.phuongthanh.diceroller.data.local.datastore.AppPreferences

class AppContainer(val context: Context) {

    val appPreferences by lazy { AppPreferences(context) }
    val soundManager by lazy { SoundManager(context) }
    val hapticManager by lazy { HapticManager(context) }
    val shakeDetector by lazy { ShakeDetector(context) }
    val customDiceRepository by lazy { com.phuongthanh.diceroller.data.repository.CustomDiceRepository(appPreferences) }
    val scoreboardRepository by lazy { com.phuongthanh.diceroller.data.repository.ScoreboardRepository(appPreferences) }
    val historyRepository by lazy { com.phuongthanh.diceroller.data.repository.HistoryRepository(appPreferences) }
    val database by lazy { com.phuongthanh.diceroller.data.local.room.AppDatabase.getInstance(context) }
    val partyQuestionRepository by lazy { com.phuongthanh.diceroller.data.repository.PartyQuestionRepository(database.partyQuestionDao(), context) }
    val drinkingRuleRepository by lazy { com.phuongthanh.diceroller.data.repository.DrinkingRuleRepository(database.drinkingRuleDao()) }
    val rouletteRepository by lazy { com.phuongthanh.diceroller.data.repository.RouletteRepository(appPreferences) }
    val billingManager by lazy { com.phuongthanh.diceroller.core.billing.BillingManager(appPreferences) }
}
