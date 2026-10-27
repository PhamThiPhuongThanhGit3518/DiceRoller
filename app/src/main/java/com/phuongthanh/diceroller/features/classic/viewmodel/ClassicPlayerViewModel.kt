package com.phuongthanh.diceroller.features.classic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.features.classic.model.PlayerMode
import com.phuongthanh.diceroller.features.classic.model.PlayerSlot
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class ClassicPlayerViewModel : ViewModel() {

    private val playerColors = listOf(
        R.color.player_red,
        R.color.player_blue,
        R.color.player_yellow,
        R.color.player_green
    )

    private val _uiState = MutableStateFlow(ClassicPlayerUiState())
    val uiState: StateFlow<ClassicPlayerUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ClassicPlayerUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        initPlayersForMode(PlayerMode.FOUR_PLAYERS, diceCount = 1)
    }

    fun processIntent(intent: ClassicPlayerIntent) {
        when (intent) {
            is ClassicPlayerIntent.SetMode -> initPlayersForMode(intent.mode, _uiState.value.diceCountPerPlayer)
            is ClassicPlayerIntent.ChangeDiceCount -> changeDiceCount(intent.count)
            is ClassicPlayerIntent.RollForPlayer -> rollForPlayer(intent.playerId)
            is ClassicPlayerIntent.RollAll -> rollAll()
            is ClassicPlayerIntent.SetFaceStyle -> setFaceStyle(intent.style)
            is ClassicPlayerIntent.SetTheme -> setTheme(intent.diceColor, intent.dotColor)
            is ClassicPlayerIntent.SyncConfiguration -> syncConfiguration(
                intent.diceType,
                intent.customMin,
                intent.customMax,
                intent.faceStyle
            )
            is ClassicPlayerIntent.ResetRound -> resetRound()
        }
    }

    private fun setFaceStyle(style: DiceFaceStyle) {
        _uiState.update { it.copy(faceStyle = style) }
    }

    private fun setTheme(diceColor: String, dotColor: String) {
        _uiState.update { it.copy(diceColor = diceColor, dotColor = dotColor) }
    }

    private fun syncConfiguration(
        diceType: DiceType,
        customMin: Int,
        customMax: Int,
        faceStyle: DiceFaceStyle?
    ) {
        val current = _uiState.value
        val style = faceStyle ?: current.faceStyle
        val typeChanged = current.diceType != diceType || current.customMin != customMin || current.customMax != customMax
        _uiState.update {
            it.copy(
                diceType = diceType,
                customMin = customMin,
                customMax = customMax,
                faceStyle = style
            )
        }
        if (typeChanged) {
            initPlayersForMode(current.mode, current.diceCountPerPlayer)
        }
    }

    private fun initPlayersForMode(mode: PlayerMode, diceCount: Int) {
        val type = _uiState.value.diceType
        val cMin = _uiState.value.customMin
        val cMax = _uiState.value.customMax
        val defaultVal = if (type == DiceType.CUSTOM) minOf(cMin, cMax) else type.minValue

        val players = (1..mode.count).map { id ->
            val initialDice = List(diceCount) { defaultVal }
            PlayerSlot(
                id = id,
                name = "P$id",
                colorRes = playerColors[(id - 1) % playerColors.size],
                diceValues = initialDice,
                score = initialDice.sum()
            )
        }
        _uiState.update {
            it.copy(
                mode = mode,
                diceCountPerPlayer = diceCount,
                players = players,
                isRoundOver = false,
                winnerId = null,
                loserId = null
            )
        }
    }

    private fun changeDiceCount(count: Int) {
        val coerced = count.coerceIn(1, 3)
        if (coerced == _uiState.value.diceCountPerPlayer) return
        initPlayersForMode(_uiState.value.mode, coerced)
    }

    private fun rollSingleDie(diceType: DiceType, customMin: Int, customMax: Int): Int {
        return if (diceType == DiceType.CUSTOM) {
            val low = minOf(customMin, customMax)
            val high = maxOf(customMin, customMax)
            Random.nextInt(low, high + 1)
        } else {
            Random.nextInt(diceType.minValue, diceType.maxValue + 1)
        }
    }

    private fun rollForPlayer(playerId: Int) {
        val state = _uiState.value
        val player = state.players.find { it.id == playerId } ?: return

        // Không cho phép roll nếu chính player này đang trong animation roll
        if (player.isRolling) return

        // Ai roll rồi mà chưa đến lượt mới (chưa kết thúc vòng hiện tại) thì không cho roll lại
        if (!state.isRoundOver && player.isLocked) return

        viewModelScope.launch {
            val durationMs = 1200L
            val isStartingNewRound = _uiState.value.isRoundOver

            // Cập nhật trạng thái bắt đầu roll:
            // Nếu là bắt đầu vòng mới: reset người chơi khác về trạng thái sẵn sàng (chưa roll, không còn badge thắng/thua)
            _uiState.update { current ->
                val updated = current.players.map { p ->
                    if (p.id == playerId) {
                        p.copy(
                            isRolling = true,
                            isLocked = false,
                            isWinner = false,
                            isLoser = false,
                            isTie = false
                        )
                    } else if (isStartingNewRound && !p.isRolling) {
                        p.copy(
                            isLocked = false,
                            isWinner = false,
                            isLoser = false,
                            isTie = false
                        )
                    } else {
                        p
                    }
                }
                current.copy(
                    players = updated,
                    isRoundOver = false,
                    winnerId = null,
                    loserId = null
                )
            }
            _uiEffect.send(ClassicPlayerUiEffect.PlayDiceRollSound(playerId))

            val diceCount = _uiState.value.diceCountPerPlayer
            val diceType = _uiState.value.diceType
            val cMin = _uiState.value.customMin
            val cMax = _uiState.value.customMax

            val finalDice = List(diceCount) { rollSingleDie(diceType, cMin, cMax) }
            val finalScore = finalDice.sum()

            _uiState.update { current ->
                val updated = current.players.map {
                    if (it.id == playerId) {
                        it.copy(diceValues = finalDice, score = finalScore)
                    } else it
                }
                current.copy(players = updated)
            }
            _uiEffect.send(ClassicPlayerUiEffect.AnimateDiceRoll(playerId, durationMs))

            delay(durationMs)

            _uiState.update { current ->
                val updated = current.players.map {
                    if (it.id == playerId) {
                        it.copy(isRolling = false, isLocked = true)
                    } else it
                }
                current.copy(players = updated)
            }

            evaluateRoundCompletion()
        }
    }

    private fun rollAll() {
        val state = _uiState.value
        if (state.players.any { it.isRolling }) return

        viewModelScope.launch {
            val durationMs = 1200L
            _uiState.update { current ->
                val updated = current.players.map {
                    it.copy(
                        isRolling = true,
                        isLocked = false,
                        isWinner = false,
                        isLoser = false,
                        isTie = false
                    )
                }
                current.copy(
                    players = updated,
                    isRoundOver = false,
                    winnerId = null,
                    loserId = null
                )
            }
            _uiEffect.send(ClassicPlayerUiEffect.PlayDiceRollSound(1))

            val diceCount = _uiState.value.diceCountPerPlayer
            val diceType = _uiState.value.diceType
            val cMin = _uiState.value.customMin
            val cMax = _uiState.value.customMax

            _uiState.update { current ->
                val updated = current.players.map { player ->
                    val finalDice = List(diceCount) { rollSingleDie(diceType, cMin, cMax) }
                    player.copy(diceValues = finalDice, score = finalDice.sum())
                }
                current.copy(players = updated)
            }
            _uiEffect.send(ClassicPlayerUiEffect.AnimateAllDiceRoll(durationMs))

            delay(durationMs)

            _uiState.update { current ->
                val updated = current.players.map {
                    it.copy(isRolling = false, isLocked = true)
                }
                current.copy(players = updated)
            }

            evaluateRoundCompletion()
        }
    }

    private suspend fun evaluateRoundCompletion() {
        val currentPlayers = _uiState.value.players
        val anyStillRolling = currentPlayers.any { it.isRolling }
        val allFinished = currentPlayers.all { it.isLocked }

        if (!anyStillRolling && allFinished && currentPlayers.isNotEmpty()) {
            val maxScore = currentPlayers.maxOf { it.score }
            val minScore = currentPlayers.minOf { it.score }
            val isAllTie = maxScore == minScore

            val evaluatedPlayers = currentPlayers.map { p ->
                when {
                    isAllTie -> p.copy(isTie = true)
                    p.score == maxScore -> p.copy(isWinner = true)
                    p.score == minScore -> p.copy(isLoser = true)
                    else -> p
                }
            }

            _uiState.update { state ->
                state.copy(
                    players = evaluatedPlayers,
                    isRoundOver = true,
                    winnerId = evaluatedPlayers.firstOrNull { it.isWinner }?.id,
                    loserId = evaluatedPlayers.firstOrNull { it.isLoser }?.id
                )
            }

            _uiEffect.send(ClassicPlayerUiEffect.PlayVictoryFanfare)
        }
    }

    private fun resetRound() {
        _uiState.update { state ->
            val diceCount = state.diceCountPerPlayer
            val diceType = state.diceType
            val cMin = state.customMin
            val cMax = state.customMax
            val initialVal = if (diceType == DiceType.CUSTOM) minOf(cMin, cMax) else diceType.minValue

            val resetPlayers = state.players.map {
                val initDice = List(diceCount) { initialVal }
                it.copy(
                    diceValues = initDice,
                    score = initDice.sum(),
                    isRolling = false,
                    isLocked = false,
                    isWinner = false,
                    isLoser = false,
                    isTie = false
                )
            }
            state.copy(
                players = resetPlayers,
                isRoundOver = false,
                winnerId = null,
                loserId = null
            )
        }
    }
}
