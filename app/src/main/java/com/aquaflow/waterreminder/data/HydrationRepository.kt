package com.aquaflow.waterreminder.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HydrationRepository(
    private val waterIntakeDao: WaterIntakeDao,
    private val userSettingsDao: UserSettingsDao
) {
    fun getTodayConsumedFlow(todayDate: String): Flow<Int> {
        return waterIntakeDao.getTotalConsumedForDate(todayDate).map { it ?: 0 }
    }

    fun getTodayIntakesFlow(todayDate: String): Flow<List<WaterIntakeEntity>> {
        return waterIntakeDao.getIntakesForDate(todayDate)
    }

    fun getUserSettingsFlow(): Flow<UserSettingsEntity> {
        return userSettingsDao.getUserSettings().map {
            it ?: UserSettingsEntity()
        }
    }

    fun getDailyTotalsFlow(): Flow<List<DateTotalResult>> {
        return waterIntakeDao.getDailyTotals()
    }

    suspend fun addWaterIntake(amountMl: Int, timestamp: Long, dateString: String): Long {
        val entity = WaterIntakeEntity(
            amountMl = amountMl,
            timestamp = timestamp,
            dateString = dateString
        )
        return waterIntakeDao.insertIntake(entity)
    }

    suspend fun deleteIntake(id: Long) {
        waterIntakeDao.deleteIntakeById(id)
    }

    suspend fun updateDailyGoal(newGoalMl: Int) {
        val current = userSettingsDao.getUserSettingsSync() ?: UserSettingsEntity()
        userSettingsDao.saveUserSettings(current.copy(dailyGoalMl = newGoalMl.coerceAtLeast(100)))
    }

    suspend fun updateReminderSettings(
        enabled: Boolean,
        intervalMinutes: Int,
        startHour: Int = 8,
        endHour: Int = 22
    ) {
        val current = userSettingsDao.getUserSettingsSync() ?: UserSettingsEntity()
        userSettingsDao.saveUserSettings(
            current.copy(
                reminderEnabled = enabled,
                reminderIntervalMinutes = intervalMinutes,
                reminderStartHour = startHour,
                reminderEndHour = endHour
            )
        )
    }

    suspend fun getTodayTotalSync(todayDate: String): Int {
        return waterIntakeDao.getTodayTotalSync(todayDate) ?: 0
    }

    suspend fun getUserSettingsSync(): UserSettingsEntity {
        return userSettingsDao.getUserSettingsSync() ?: UserSettingsEntity()
    }

    suspend fun deleteAllIntakesForDate(dateString: String) {
        waterIntakeDao.deleteAllIntakesForDate(dateString)
    }

    suspend fun clearAllHistory() {
        waterIntakeDao.clearAllHistory()
    }
}
