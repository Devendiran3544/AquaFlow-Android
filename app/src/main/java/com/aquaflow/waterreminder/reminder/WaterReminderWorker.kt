package com.aquaflow.waterreminder.reminder

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aquaflow.waterreminder.MainActivity
import com.aquaflow.waterreminder.R
import com.aquaflow.waterreminder.data.AquaFlowDatabase
import com.aquaflow.waterreminder.data.HydrationRepository
import com.aquaflow.waterreminder.domain.HydrationCalculator
import java.time.LocalTime

class WaterReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AquaFlowDatabase.getDatabase(context)
        val repository = HydrationRepository(db.waterIntakeDao(), db.userSettingsDao())
        val settings = repository.getUserSettingsSync()

        if (!settings.reminderEnabled) {
            return Result.success()
        }

        val currentHour = LocalTime.now().hour
        if (currentHour < settings.reminderStartHour || currentHour >= settings.reminderEndHour) {
            // Outside user-configured reminder hours (e.g. night time)
            return Result.success()
        }

        val todayDate = HydrationCalculator.getTodayDateString()
        val totalConsumed = repository.getTodayTotalSync(todayDate)

        if (totalConsumed >= settings.dailyGoalMl) {
            // Daily goal already achieved
            return Result.success()
        }

        showReminderNotification(totalConsumed, settings.dailyGoalMl)
        return Result.success()
    }

    private fun showReminderNotification(consumed: Int, goal: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundEnabled = ReminderScheduler.isSoundEnabled(context)
        val channelId = ReminderScheduler.getNotificationChannelId(context)
        val remaining = (goal - consumed).coerceAtLeast(0)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_water_drop)
            .setContentTitle("💧 Time to hydrate!")
            .setContentText("You've had $consumed ml of $goal ml. Drink $remaining ml more today!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Every sip matters! You have $remaining ml remaining to hit your daily goal of $goal ml.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundEnabled) {
            notificationBuilder.setSound(ReminderScheduler.getSoundUri(context))
        } else {
            notificationBuilder.setSilent(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
