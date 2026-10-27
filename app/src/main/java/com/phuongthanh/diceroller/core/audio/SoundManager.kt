package com.phuongthanh.diceroller.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import com.phuongthanh.diceroller.R
import kotlin.random.Random

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private var diceRollSoundId: Int = 0
    private var wheelTickSoundId: Int = 0
    private var victoryFanfareSoundId: Int = 0
    private val loadedSoundIds = mutableSetOf<Int>()

    private val toneGenerator by lazy {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {
            null
        }
    }

    private var isMuted: Boolean = false
    private var volume: Float = 1.0f

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSoundIds.add(sampleId)
            }
        }

        loadSounds()
    }

    private fun loadSounds() {
        try {
            diceRollSoundId = soundPool.load(context, R.raw.dice_roll, 1)
            wheelTickSoundId = soundPool.load(context, R.raw.wheel_tick, 1)
            victoryFanfareSoundId = soundPool.load(context, R.raw.victory_fanfare, 1)
        } catch (_: Exception) {
            // Fallback to ToneGenerator remains available
        }
    }

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
    }

    fun setVolume(vol: Float) {
        this.volume = vol.coerceIn(0f, 1f)
    }

    fun playDiceRoll() {
        if (isMuted) return
        val streamId = if (diceRollSoundId != 0 && (diceRollSoundId in loadedSoundIds)) {
            val rate = 0.90f + Random.nextFloat() * 0.06f
            soundPool.play(diceRollSoundId, volume, volume, 1, 0, rate)
        } else 0

        if (streamId == 0) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        }
    }

    fun playWheelTick() {
        if (isMuted) return
        val streamId = if (wheelTickSoundId != 0 && (wheelTickSoundId in loadedSoundIds)) {
            val rate = 0.95f + Random.nextFloat() * 0.10f
            soundPool.play(wheelTickSoundId, volume * 0.8f, volume * 0.8f, 1, 0, rate)
        } else 0

        if (streamId == 0) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 30)
        }
    }

    fun playVictoryFanfare() {
        if (isMuted) return
        val streamId = if (victoryFanfareSoundId != 0 && (victoryFanfareSoundId in loadedSoundIds)) {
            soundPool.play(victoryFanfareSoundId, volume, volume, 1, 0, 1.0f)
        } else 0

        if (streamId == 0) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        }
    }

    fun release() {
        soundPool.release()
        toneGenerator?.release()
    }
}
