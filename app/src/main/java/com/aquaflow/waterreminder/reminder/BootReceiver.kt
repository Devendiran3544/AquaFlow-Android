package com.aquaflow.waterreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aquaflow.waterreminder.data.AquaFlowDatabase
import com.aquaflow.waterreminder.data.HydrationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AquaFlowDatabase.getDatabase(context)
                val repository = HydrationRepository(db.waterIntakeDao(), db.userSettingsDao())
                val settings = repository.getUserSettingsSync()

                if (settings.reminderEnabled) {
                    ReminderScheduler.scheduleReminders(context, settings.reminderIntervalMinutes)
                }
            } catch (e: Exception) {
                // Log and swallow safely to prevent boot crash
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
