package com.aquaflow.waterreminder.ui

import com.aquaflow.waterreminder.data.WaterIntakeEntity
import com.aquaflow.waterreminder.domain.DayHydrationStat

data class HydrationUiState(
    val consumedMl: Int = 0,
    val dailyGoalMl: Int = 2000,
    val progress: Float = 0f,
    val percentage: Int = 0,
    val todayDateString: String = "",
    val todayIntakes: List<WaterIntakeEntity> = emptyList(),
    val weeklyStats: List<DayHydrationStat> = emptyList(),
    val streakDays: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderSoundEnabled: Boolean = true,
    val reminderIntervalMinutes: Int = 60,
    val reminderStartHour: Int = 8,
    val reminderEndHour: Int = 22,
    val showGoalDialog: Boolean = false,
    val showReminderDialog: Boolean = false,
    val showStatsDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val showCustomIntakeDialog: Boolean = false,
    val lastAddedIntakeId: Long? = null,
    val lastAddedAmount: Int? = null
)
