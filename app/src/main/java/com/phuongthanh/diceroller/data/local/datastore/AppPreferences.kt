package com.phuongthanh.diceroller.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dice_roller_preferences")

class AppPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEY_HAPTIC_INTENSITY = intPreferencesKey("haptic_intensity") // 0: subtle, 1: medium, 2: strong
        val KEY_SHAKE_ENABLED = booleanPreferencesKey("shake_enabled")
        val KEY_SHAKE_SENSITIVITY = intPreferencesKey("shake_sensitivity") // 0: low, 1: medium, 2: high
        val KEY_DICE_SKIN = stringPreferencesKey("dice_skin")
        val KEY_TABLE_THEME = stringPreferencesKey("table_theme")
        val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val KEY_HISTORY_ENABLED = booleanPreferencesKey("history_enabled")
        val KEY_FIRST_OPEN = booleanPreferencesKey("is_first_open_app")
        val KEY_IS_VIP = booleanPreferencesKey("is_vip_user")
        val KEY_CUSTOM_DICE_JSON = stringPreferencesKey("custom_dice_json")
        val KEY_SCOREBOARD_PLAYERS_JSON = stringPreferencesKey("scoreboard_players_json")
        val KEY_ROLL_HISTORY_JSON = stringPreferencesKey("roll_history_json")
        val KEY_DICE_FACE_STYLE = stringPreferencesKey("dice_face_style")
        val KEY_ROULETTE_PRESETS_JSON = stringPreferencesKey("roulette_presets_json")
        val KEY_SHOW_TOTAL_SCORE = booleanPreferencesKey("show_total_score")
        val KEY_ANIMATION_SPEED = intPreferencesKey("animation_speed") // 0: fast, 1: normal, 2: slow
        val KEY_ANIMATION_TYPE = stringPreferencesKey("animation_type") // "full_left", "full_right", "none"
        val KEY_DICE_TYPE = stringPreferencesKey("dice_type")
        val KEY_CUSTOM_MIN = intPreferencesKey("custom_min")
        val KEY_CUSTOM_MAX = intPreferencesKey("custom_max")
        val KEY_SAME_COLORS_ALL_DICE = booleanPreferencesKey("same_colors_all_dice")
        val KEY_DICE_COLOR = stringPreferencesKey("dice_color")
        val KEY_DOT_COLOR = stringPreferencesKey("dot_color")
    }

    val soundEnabledFlow: Flow<Boolean> = dataStore.data.map { it[KEY_SOUND_ENABLED] ?: true }
    val hapticEnabledFlow: Flow<Boolean> = dataStore.data.map { it[KEY_HAPTIC_ENABLED] ?: true }
    val hapticIntensityFlow: Flow<Int> = dataStore.data.map { it[KEY_HAPTIC_INTENSITY] ?: 1 }
    val shakeEnabledFlow: Flow<Boolean> = dataStore.data.map { it[KEY_SHAKE_ENABLED] ?: true }
    val shakeSensitivityFlow: Flow<Int> = dataStore.data.map { it[KEY_SHAKE_SENSITIVITY] ?: 1 }
    val diceSkinFlow: Flow<String> = dataStore.data.map { it[KEY_DICE_SKIN] ?: "classic_white" }
    val tableThemeFlow: Flow<String> = dataStore.data.map { it[KEY_TABLE_THEME] ?: "felt_green" }
    val keepScreenOnFlow: Flow<Boolean> = dataStore.data.map { it[KEY_KEEP_SCREEN_ON] ?: true }
    val historyEnabledFlow: Flow<Boolean> = dataStore.data.map { it[KEY_HISTORY_ENABLED] ?: true }
    val isFirstOpenFlow: Flow<Boolean> = dataStore.data.map { it[KEY_FIRST_OPEN] ?: true }
    val isVipFlow: Flow<Boolean> = dataStore.data.map { it[KEY_IS_VIP] ?: false }
    val customDiceJsonFlow: Flow<String> = dataStore.data.map { it[KEY_CUSTOM_DICE_JSON] ?: "" }
    val scoreboardPlayersJsonFlow: Flow<String> = dataStore.data.map { it[KEY_SCOREBOARD_PLAYERS_JSON] ?: "" }
    val rollHistoryJsonFlow: Flow<String> = dataStore.data.map { it[KEY_ROLL_HISTORY_JSON] ?: "" }
    val diceFaceStyleFlow: Flow<String> = dataStore.data.map { it[KEY_DICE_FACE_STYLE] ?: com.phuongthanh.diceroller.core.model.DiceFaceStyle.DOTS.code }
    val roulettePresetsJsonFlow: Flow<String> = dataStore.data.map { it[KEY_ROULETTE_PRESETS_JSON] ?: "" }
    val showTotalScoreFlow: Flow<Boolean> = dataStore.data.map { it[KEY_SHOW_TOTAL_SCORE] ?: true }
    val animationSpeedFlow: Flow<Int> = dataStore.data.map { it[KEY_ANIMATION_SPEED] ?: 0 }
    val animationTypeFlow: Flow<String> = dataStore.data.map { it[KEY_ANIMATION_TYPE] ?: "none" }
    val diceTypeFlow: Flow<String> = dataStore.data.map { it[KEY_DICE_TYPE] ?: "D6" }
    val customMinFlow: Flow<Int> = dataStore.data.map { it[KEY_CUSTOM_MIN] ?: 1 }
    val customMaxFlow: Flow<Int> = dataStore.data.map { it[KEY_CUSTOM_MAX] ?: 100 }
    val sameColorsAllDiceFlow: Flow<Boolean> = dataStore.data.map { it[KEY_SAME_COLORS_ALL_DICE] ?: true }
    val diceColorFlow: Flow<String> = dataStore.data.map { it[KEY_DICE_COLOR] ?: "#C4EA7E" }
    val dotColorFlow: Flow<String> = dataStore.data.map { it[KEY_DOT_COLOR] ?: "#F7D4D5" }

    suspend fun setSoundEnabled(enabled: Boolean) = dataStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    suspend fun setHapticEnabled(enabled: Boolean) = dataStore.edit { it[KEY_HAPTIC_ENABLED] = enabled }
    suspend fun setHapticIntensity(level: Int) = dataStore.edit { it[KEY_HAPTIC_INTENSITY] = level }
    suspend fun setShakeEnabled(enabled: Boolean) = dataStore.edit { it[KEY_SHAKE_ENABLED] = enabled }
    suspend fun setShakeSensitivity(level: Int) = dataStore.edit { it[KEY_SHAKE_SENSITIVITY] = level }
    suspend fun setDiceSkin(skin: String) = dataStore.edit { it[KEY_DICE_SKIN] = skin }
    suspend fun setTableTheme(theme: String) = dataStore.edit { it[KEY_TABLE_THEME] = theme }
    suspend fun setKeepScreenOn(keep: Boolean) = dataStore.edit { it[KEY_KEEP_SCREEN_ON] = keep }
    suspend fun setHistoryEnabled(enabled: Boolean) = dataStore.edit { it[KEY_HISTORY_ENABLED] = enabled }
    suspend fun setFirstOpen(first: Boolean) = dataStore.edit { it[KEY_FIRST_OPEN] = first }
    suspend fun setVip(vip: Boolean) = dataStore.edit { it[KEY_IS_VIP] = vip }
    suspend fun setCustomDiceJson(json: String) = dataStore.edit { it[KEY_CUSTOM_DICE_JSON] = json }
    suspend fun setScoreboardPlayersJson(json: String) = dataStore.edit { it[KEY_SCOREBOARD_PLAYERS_JSON] = json }
    suspend fun setRollHistoryJson(json: String) = dataStore.edit { it[KEY_ROLL_HISTORY_JSON] = json }
    suspend fun setDiceFaceStyle(style: String) = dataStore.edit { it[KEY_DICE_FACE_STYLE] = style }
    suspend fun setRoulettePresetsJson(json: String) = dataStore.edit { it[KEY_ROULETTE_PRESETS_JSON] = json }
    suspend fun setShowTotalScore(show: Boolean) = dataStore.edit { it[KEY_SHOW_TOTAL_SCORE] = show }
    suspend fun setAnimationSpeed(speed: Int) = dataStore.edit { it[KEY_ANIMATION_SPEED] = speed }
    suspend fun setAnimationType(type: String) = dataStore.edit { it[KEY_ANIMATION_TYPE] = type }
    suspend fun setDiceType(type: String) = dataStore.edit { it[KEY_DICE_TYPE] = type }
    suspend fun setSameColorsAllDice(enabled: Boolean) = dataStore.edit { it[KEY_SAME_COLORS_ALL_DICE] = enabled }
    suspend fun setDiceColor(color: String) = dataStore.edit { it[KEY_DICE_COLOR] = color }
    suspend fun setDotColor(color: String) = dataStore.edit { it[KEY_DOT_COLOR] = color }
    suspend fun setCustomRange(min: Int, max: Int) = dataStore.edit {
        it[KEY_CUSTOM_MIN] = min
        it[KEY_CUSTOM_MAX] = max
    }
}
