package com.aquaflow.waterreminder

import com.aquaflow.waterreminder.domain.HydrationCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WaterProgressTest {

    @Test
    fun progressIsZeroAtStart() {
        val progress = HydrationCalculator.calculateProgress(0, 2000)
        assertEquals(0f, progress, 0.001f)
        assertEquals(0, HydrationCalculator.calculatePercentage(0, 2000))
    }

    @Test
    fun progressIsHalfAtHalfGoal() {
        val progress = HydrationCalculator.calculateProgress(1000, 2000)
        assertEquals(0.5f, progress, 0.001f)
        assertEquals(50, HydrationCalculator.calculatePercentage(1000, 2000))
    }

    @Test
    fun progressIsClampedAtOneWhenGoalExceeded() {
        val progress = HydrationCalculator.calculateProgress(2500, 2000)
        assertEquals(1.0f, progress, 0.001f)
        assertEquals(100, HydrationCalculator.calculatePercentage(2500, 2000))
    }

    @Test
    fun progressHandlesZeroOrNegativeGoalGracefully() {
        val progress = HydrationCalculator.calculateProgress(500, 0)
        assertEquals(0f, progress, 0.001f)
        assertEquals(0, HydrationCalculator.calculatePercentage(500, 0))

        val progressNeg = HydrationCalculator.calculateProgress(500, -100)
        assertEquals(0f, progressNeg, 0.001f)
        assertEquals(0, HydrationCalculator.calculatePercentage(500, -100))
    }

    @Test
    fun customGoalProgressCalculatesCorrectly() {
        val progress = HydrationCalculator.calculateProgress(1500, 3000)
        assertEquals(0.5f, progress, 0.001f)
        assertEquals(50, HydrationCalculator.calculatePercentage(1500, 3000))

        val progressThreeQuarters = HydrationCalculator.calculateProgress(1875, 2500)
        assertEquals(0.75f, progressThreeQuarters, 0.001f)
        assertEquals(75, HydrationCalculator.calculatePercentage(1875, 2500))
    }

    @Test
    fun calculateRemainingHandlesEdgeCasesCorrectly() {
        assertEquals(2000, HydrationCalculator.calculateRemaining(0, 2000))
        assertEquals(1250, HydrationCalculator.calculateRemaining(750, 2000))
        assertEquals(0, HydrationCalculator.calculateRemaining(2000, 2000))
        assertEquals(0, HydrationCalculator.calculateRemaining(2500, 2000))
    }

    @Test
    fun isValidGoalValidatesRangeCorrectly() {
        assertFalse(HydrationCalculator.isValidGoal(499))
        assertTrue(HydrationCalculator.isValidGoal(500))
        assertTrue(HydrationCalculator.isValidGoal(2000))
        assertTrue(HydrationCalculator.isValidGoal(10000))
        assertFalse(HydrationCalculator.isValidGoal(10001))
        assertFalse(HydrationCalculator.isValidGoal(-500))
    }

    @Test
    fun sanitizeGoalProvidesFallbackForInvalidValues() {
        assertEquals(2000, HydrationCalculator.sanitizeGoal(2000))
        assertEquals(1500, HydrationCalculator.sanitizeGoal(1500))
        assertEquals(2000, HydrationCalculator.sanitizeGoal(100))
        assertEquals(2000, HydrationCalculator.sanitizeGoal(20000))
        assertEquals(3000, HydrationCalculator.sanitizeGoal(0, fallback = 3000))
    }

    @Test
    fun isValidIntakeAmountValidatesRangeCorrectly() {
        assertFalse(HydrationCalculator.isValidIntakeAmount(9))
        assertTrue(HydrationCalculator.isValidIntakeAmount(10))
        assertTrue(HydrationCalculator.isValidIntakeAmount(250))
        assertTrue(HydrationCalculator.isValidIntakeAmount(5000))
        assertFalse(HydrationCalculator.isValidIntakeAmount(5001))
        assertFalse(HydrationCalculator.isValidIntakeAmount(-50))
    }

    @Test
    fun sanitizeIntakeAmountClampsWithinAllowedBounds() {
        assertEquals(250, HydrationCalculator.sanitizeIntakeAmount(250))
        assertEquals(10, HydrationCalculator.sanitizeIntakeAmount(5))
        assertEquals(10, HydrationCalculator.sanitizeIntakeAmount(-100))
        assertEquals(5000, HydrationCalculator.sanitizeIntakeAmount(6000))
    }

    @Test
    fun isNewDayDetectsDateChangeCorrectly() {
        assertTrue(HydrationCalculator.isNewDay("2026-11-19", "2026-11-20"))
        assertFalse(HydrationCalculator.isNewDay("2026-11-20", "2026-11-20"))
        assertFalse(HydrationCalculator.isNewDay("", "2026-11-20"))
        assertFalse(HydrationCalculator.isNewDay("2026-11-20", ""))
    }

    @Test
    fun formatTimeFormatsInstantCorrectly() {
        // 2026-11-20 10:30:00 UTC -> 1605868200000 approx
        val instant = Instant.parse("2026-11-20T10:30:00Z")
        val zone = ZoneId.of("UTC")
        val formatted = HydrationCalculator.formatTime(instant.toEpochMilli(), zone)
        assertTrue(formatted.contains("10:30"))
    }

    @Test
    fun weeklyStatsAggregationCoversAllSevenDays() {
        val today = LocalDate.of(2026, 11, 20)
        val intakesByDate = mapOf(
            "2026-11-20" to 1500,
            "2026-11-19" to 2000,
            "2026-11-18" to 2200,
            "2026-11-17" to 1800,
            "2026-11-16" to 2000,
            "2026-11-15" to 800,
            "2026-11-14" to 0
        )

        val stats = HydrationCalculator.aggregateStatsForPastDays(
            intakesByDate = intakesByDate,
            dailyGoalMl = 2000,
            days = 7,
            today = today
        )

        assertEquals(7, stats.size)

        // Today is the last item
        val todayStat = stats.last()
        assertEquals("2026-11-20", todayStat.dateString)
        assertEquals("Today", todayStat.dayLabel)
        assertEquals(1500, todayStat.totalConsumedMl)
        assertEquals(75, todayStat.percentage)

        // 6 days ago is the first item
        val firstStat = stats.first()
        assertEquals("2026-11-14", firstStat.dateString)
        assertEquals(0, firstStat.totalConsumedMl)
        assertEquals(0, firstStat.percentage)
    }

    @Test
    fun weeklyStatsAggregationHandlesEmptyDataWithoutExceptions() {
        val today = LocalDate.of(2026, 11, 20)
        val stats = HydrationCalculator.aggregateStatsForPastDays(
            intakesByDate = emptyMap(),
            dailyGoalMl = 2000,
            days = 7,
            today = today
        )

        assertEquals(7, stats.size)
        stats.forEach { stat ->
            assertEquals(0, stat.totalConsumedMl)
            assertEquals(0, stat.percentage)
        }
    }

    @Test
    fun hydrationRecordAdditionAggregatesCorrectly() {
        val drinks = listOf(150, 200, 250, 500)
        val total = drinks.sum()
        assertEquals(1100, total)

        val goal = 2000
        val remaining = (goal - total).coerceAtLeast(0)
        assertEquals(900, remaining)
        assertEquals(55, HydrationCalculator.calculatePercentage(total, goal))
    }

    @Test
    fun streakIsZeroWhenNoGoalsMet() {
        val today = LocalDate.of(2026, 11, 20)
        val intakes = mapOf(
            "2026-11-20" to 500,
            "2026-11-19" to 800,
            "2026-11-18" to 1200
        )
        val streak = HydrationCalculator.calculateStreak(intakes, 2000, today)
        assertEquals(0, streak)
    }

    @Test
    fun streakIsOneWhenOnlyTodayGoalMet() {
        val today = LocalDate.of(2026, 11, 20)
        val intakes = mapOf(
            "2026-11-20" to 2200,
            "2026-11-19" to 1000
        )
        val streak = HydrationCalculator.calculateStreak(intakes, 2000, today)
        assertEquals(1, streak)
    }

    @Test
    fun streakCountsConsecutivePastDays() {
        val today = LocalDate.of(2026, 11, 20)
        val intakes = mapOf(
            "2026-11-20" to 2000,
            "2026-11-19" to 2500,
            "2026-11-18" to 2100,
            "2026-11-17" to 2000,
            "2026-11-16" to 1200
        )
        val streak = HydrationCalculator.calculateStreak(intakes, 2000, today)
        assertEquals(4, streak)
    }

    @Test
    fun streakPreservesPastStreakWhileTodayIsStillInProgress() {
        val today = LocalDate.of(2026, 11, 20)
        val intakes = mapOf(
            "2026-11-20" to 600,
            "2026-11-19" to 2000,
            "2026-11-18" to 2200,
            "2026-11-17" to 2000
        )
        val streak = HydrationCalculator.calculateStreak(intakes, 2000, today)
        assertEquals(3, streak)
    }

    @Test
    fun streakResetsToZeroIfYesterdayWasMissedAndTodayNotYetMet() {
        val today = LocalDate.of(2026, 11, 20)
        val intakes = mapOf(
            "2026-11-20" to 500,
            "2026-11-19" to 1200,
            "2026-11-18" to 2500
        )
        val streak = HydrationCalculator.calculateStreak(intakes, 2000, today)
        assertEquals(0, streak)
    }

    @Test
    fun streakHandlesZeroOrNegativeGoalGracefully() {
        val today = LocalDate.of(2026, 11, 20)
        assertEquals(0, HydrationCalculator.calculateStreak(mapOf("2026-11-20" to 500), 0, today))
        assertEquals(0, HydrationCalculator.calculateStreak(mapOf("2026-11-20" to 500), -100, today))
    }
}
