package com.aquaflow.waterreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val dailyGoalMl: Int = 2000,
    val reminderEnabled: Boolean = false,
    val reminderIntervalMinutes: Int = 60,
    val reminderStartHour: Int = 8,
    val reminderEndHour: Int = 22
)
