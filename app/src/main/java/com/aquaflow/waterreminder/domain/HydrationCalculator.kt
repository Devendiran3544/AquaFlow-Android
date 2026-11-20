package com.aquaflow.waterreminder.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DayHydrationStat(
    val dateString: String,
    val dayLabel: String,
    val totalConsumedMl: Int,
    val goalMl: Int,
    val percentage: Int
)

object HydrationCalculator {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dayLabelFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    fun calculateProgress(consumedMl: Int, goalMl: Int): Float {
        if (goalMl <= 0) return 0f
        return (consumedMl.toFloat() / goalMl).coerceIn(0f, 1f)
    }

    fun calculatePercentage(consumedMl: Int, goalMl: Int): Int {
        return (calculateProgress(consumedMl, goalMl) * 100).toInt()
    }

    fun getTodayDateString(zoneId: ZoneId = ZoneId.systemDefault()): String {
        return LocalDate.now(zoneId).format(dateFormatter)
    }

    fun calculateRemaining(consumedMl: Int, goalMl: Int): Int {
        return (goalMl - consumedMl).coerceAtLeast(0)
    }

    fun isValidGoal(goalMl: Int): Boolean {
        return goalMl in 500..10000
    }

    fun isValidIntakeAmount(amountMl: Int): Boolean {
        return amountMl in 10..5000
    }

    fun sanitizeGoal(goalMl: Int, fallback: Int = 2000): Int {
        return if (isValidGoal(goalMl)) goalMl else fallback.coerceIn(500, 10000)
    }

    fun sanitizeIntakeAmount(amountMl: Int): Int {
        return amountMl.coerceIn(10, 5000)
    }

    fun isNewDay(lastDateString: String, currentDateString: String): Boolean {
        if (lastDateString.isBlank() || currentDateString.isBlank()) return false
        return lastDateString != currentDateString
    }

    fun formatTime(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(zoneId)
            .format(timeFormatter)
    }

    fun aggregateStatsForPastDays(
        intakesByDate: Map<String, Int>,
        dailyGoalMl: Int,
        days: Int = 7,
        today: LocalDate = LocalDate.now()
    ): List<DayHydrationStat> {
        val result = mutableListOf<DayHydrationStat>()
        for (i in (days - 1) downTo 0) {
            val date = today.minusDays(i.toLong())
            val dateStr = date.format(dateFormatter)
            val dayLabel = if (i == 0) "Today" else date.format(dayLabelFormatter)
            val consumed = intakesByDate[dateStr] ?: 0
            val percentage = calculatePercentage(consumed, dailyGoalMl)
            result.add(
                DayHydrationStat(
                    dateString = dateStr,
                    dayLabel = dayLabel,
                    totalConsumedMl = consumed,
                    goalMl = dailyGoalMl,
                    percentage = percentage
                )
            )
        }
        return result
    }

    fun calculateStreak(
        intakesByDate: Map<String, Int>,
        dailyGoalMl: Int,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (dailyGoalMl <= 0) return 0
        var streak = 0
        val todayStr = today.format(dateFormatter)
        val todayConsumed = intakesByDate[todayStr] ?: 0

        val startDate = if (todayConsumed >= dailyGoalMl) {
            streak++
            today.minusDays(1)
        } else {
            today.minusDays(1)
        }

        var checkDate = startDate
        while (true) {
            val dateStr = checkDate.format(dateFormatter)
            val consumed = intakesByDate[dateStr] ?: 0
            if (consumed >= dailyGoalMl) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }
}
