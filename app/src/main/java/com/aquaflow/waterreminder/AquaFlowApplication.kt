package com.aquaflow.waterreminder

import android.app.Application
import com.aquaflow.waterreminder.data.AquaFlowDatabase
import com.aquaflow.waterreminder.data.HydrationRepository
import com.aquaflow.waterreminder.reminder.ReminderScheduler

class AquaFlowApplication : Application() {
    val database by lazy { AquaFlowDatabase.getDatabase(this) }
    val repository by lazy { HydrationRepository(database.waterIntakeDao(), database.userSettingsDao()) }

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.createNotificationChannel(this)
    }
}
