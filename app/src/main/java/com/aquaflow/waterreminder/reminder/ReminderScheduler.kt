package com.aquaflow.waterreminder.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aquaflow.waterreminder.R
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val CHANNEL_ID_SOUND = "aquaflow_reminders_v3"
    const val CHANNEL_ID_SILENT = "aquaflow_reminders_silent_v3"
    private const val LEGACY_CHANNEL_ID = "aquaflow_reminders_channel"
    private const val V2_CHANNEL_SOUND_ID = "aquaflow_reminders_v2"
    private const val V2_CHANNEL_SILENT_ID = "aquaflow_reminders_silent_v2"
    private const val WORK_NAME = "aquaflow_periodic_reminder"
    private const val PREFS_NAME = "aquaflow_prefs"
    private const val KEY_SOUND_ENABLED = "reminder_sound_enabled"

    fun getSoundUri(context: Context): Uri {
        return Uri.parse("android.resource://${context.packageName}/${R.raw.water_pour}")
    }

    fun isSoundEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SOUND_ENABLED, enabled)
            .apply()
    }

    fun getNotificationChannelId(context: Context): String {
        return if (isSoundEnabled(context)) CHANNEL_ID_SOUND else CHANNEL_ID_SILENT
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Safe migration: clean up obsolete channels so channel properties update cleanly
            try {
                notificationManager.deleteNotificationChannel(LEGACY_CHANNEL_ID)
                notificationManager.deleteNotificationChannel(V2_CHANNEL_SOUND_ID)
                notificationManager.deleteNotificationChannel(V2_CHANNEL_SILENT_ID)
            } catch (_: Exception) {
            }

            val soundUri = getSoundUri(context)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // Sound channel with soothing water-pour chime
            val soundChannel = NotificationChannel(
                CHANNEL_ID_SOUND,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Water reminders with soothing water-pour chime and vibration"
                enableVibration(true)
                enableLights(true)
                setSound(soundUri, audioAttributes)
            }

            // Silent channel for users preferring vibration only
            val silentChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                "Hydration Reminders (Silent)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Water reminders with vibration only"
                enableVibration(true)
                enableLights(true)
                setSound(null, null)
            }

            notificationManager.createNotificationChannel(soundChannel)
            notificationManager.createNotificationChannel(silentChannel)
        }
    }

    fun scheduleReminders(context: Context, intervalMinutes: Int) {
        val sanitizedInterval = intervalMinutes.coerceAtLeast(15).toLong()
        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            sanitizedInterval,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
