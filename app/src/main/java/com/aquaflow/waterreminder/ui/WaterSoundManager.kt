package com.aquaflow.waterreminder.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.aquaflow.waterreminder.R

object WaterSoundManager {
    private const val PREFS_NAME = "aquaflow_prefs"
    private const val KEY_POUR_SOUND_ENABLED = "water_pour_sound_enabled"

    private var soundPool: SoundPool? = null
    private var pourSoundId: Int = 0
    private var currentStreamId: Int = 0
    private var isLoaded: Boolean = false

    fun isPourSoundEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_POUR_SOUND_ENABLED, true)
    }

    fun setPourSoundEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_POUR_SOUND_ENABLED, enabled)
            .apply()
    }

    fun init(context: Context) {
        if (soundPool == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(1) // Single stream to prevent overlapping audio on rapid taps
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    setOnLoadCompleteListener { _, sampleId, status ->
                        if (status == 0 && sampleId == pourSoundId) {
                            isLoaded = true
                        }
                    }
                }

            pourSoundId = soundPool?.load(context.applicationContext, R.raw.water_pour, 1) ?: 0
        }
    }

    fun playPourSound(context: Context) {
        if (!isPourSoundEnabled(context)) return

        if (soundPool == null) {
            init(context)
        }

        val pool = soundPool ?: return
        if (isLoaded && pourSoundId != 0) {
            if (currentStreamId != 0) {
                try {
                    pool.stop(currentStreamId)
                } catch (_: Exception) {
                }
            }
            currentStreamId = pool.play(pourSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
        }
    }

    fun release() {
        try {
            soundPool?.release()
        } catch (_: Exception) {
        }
        soundPool = null
        pourSoundId = 0
        currentStreamId = 0
        isLoaded = false
    }
}
