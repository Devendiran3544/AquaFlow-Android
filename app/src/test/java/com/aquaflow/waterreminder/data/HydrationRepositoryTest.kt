package com.aquaflow.waterreminder.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeWaterIntakeDao : WaterIntakeDao {
    private val intakes = mutableListOf<WaterIntakeEntity>()
    private val intakesFlow = MutableStateFlow<List<WaterIntakeEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insertIntake(intake: WaterIntakeEntity): Long {
        val id = nextId++
        val entity = intake.copy(id = id)
        intakes.add(entity)
        intakesFlow.value = intakes.toList()
        return id
    }

    override suspend fun deleteIntakeById(id: Long) {
        intakes.removeAll { it.id == id }
        intakesFlow.value = intakes.toList()
    }

    override fun getIntakesForDate(dateString: String): Flow<List<WaterIntakeEntity>> {
        return intakesFlow.map { list -> list.filter { it.dateString == dateString } }
    }

    override fun getTotalConsumedForDate(dateString: String): Flow<Int?> {
        return intakesFlow.map { list ->
            val total = list.filter { it.dateString == dateString }.sumOf { it.amountMl }
            if (total == 0 && list.none { it.dateString == dateString }) null else total
        }
    }

    override fun getDailyTotals(): Flow<List<DateTotalResult>> {
        return intakesFlow.map { list ->
            list.groupBy { it.dateString }.map { (date, items) ->
                DateTotalResult(dateString = date, total = items.sumOf { it.amountMl })
            }
        }
    }

    override fun getRecentIntakes(limit: Int): Flow<List<WaterIntakeEntity>> {
        return intakesFlow.map { list -> list.takeLast(limit) }
    }

    override suspend fun getTodayTotalSync(dateString: String): Int? {
        val total = intakes.filter { it.dateString == dateString }.sumOf { it.amountMl }
        return if (total == 0 && intakes.none { it.dateString == dateString }) null else total
    }

    override suspend fun deleteAllIntakesForDate(dateString: String) {
        intakes.removeAll { it.dateString == dateString }
        intakesFlow.value = intakes.toList()
    }

    override suspend fun clearAllHistory() {
        intakes.clear()
        intakesFlow.value = emptyList()
    }
}

private class FakeUserSettingsDao : UserSettingsDao {
    private var settings: UserSettingsEntity? = null
    private val settingsFlow = MutableStateFlow<UserSettingsEntity?>(null)

    override fun getUserSettings(): Flow<UserSettingsEntity?> = settingsFlow

    override suspend fun saveUserSettings(settings: UserSettingsEntity) {
        this.settings = settings
        settingsFlow.value = settings
    }

    override suspend fun getUserSettingsSync(): UserSettingsEntity? = settings
}

class HydrationRepositoryTest {

    private lateinit var fakeWaterIntakeDao: FakeWaterIntakeDao
    private lateinit var fakeUserSettingsDao: FakeUserSettingsDao
    private lateinit var repository: HydrationRepository

    @Before
    fun setUp() {
        fakeWaterIntakeDao = FakeWaterIntakeDao()
        fakeUserSettingsDao = FakeUserSettingsDao()
        repository = HydrationRepository(fakeWaterIntakeDao, fakeUserSettingsDao)
    }

    @Test
    fun initialConsumedIsZeroWhenNoRecordsExist() = runBlocking {
        val consumed = repository.getTodayConsumedFlow("2026-11-20").first()
        assertEquals(0, consumed)
    }

    @Test
    fun addingIntakesEmitsAggregatedTotal() = runBlocking {
        repository.addWaterIntake(250, 1000L, "2026-11-20")
        repository.addWaterIntake(500, 2000L, "2026-11-20")

        val total = repository.getTodayConsumedFlow("2026-11-20").first()
        assertEquals(750, total)

        val syncTotal = repository.getTodayTotalSync("2026-11-20")
        assertEquals(750, syncTotal)
    }

    @Test
    fun deletingIntakeUpdatesEmittedTotal() = runBlocking {
        val id1 = repository.addWaterIntake(200, 1000L, "2026-11-20")
        val id2 = repository.addWaterIntake(300, 2000L, "2026-11-20")

        assertEquals(500, repository.getTodayConsumedFlow("2026-11-20").first())

        repository.deleteIntake(id1)
        assertEquals(300, repository.getTodayConsumedFlow("2026-11-20").first())

        repository.deleteIntake(id2)
        assertEquals(0, repository.getTodayConsumedFlow("2026-11-20").first())
    }

    @Test
    fun deleteAllIntakesForTodayClearsOnlyTargetDate() = runBlocking {
        repository.addWaterIntake(250, 1000L, "2026-11-19")
        repository.addWaterIntake(500, 2000L, "2026-11-20")
        repository.addWaterIntake(150, 3000L, "2026-11-20")

        assertEquals(650, repository.getTodayConsumedFlow("2026-11-20").first())
        assertEquals(250, repository.getTodayConsumedFlow("2026-11-19").first())

        repository.deleteAllIntakesForDate("2026-11-20")

        assertEquals(0, repository.getTodayConsumedFlow("2026-11-20").first())
        assertEquals(250, repository.getTodayConsumedFlow("2026-11-19").first())
    }

    @Test
    fun clearAllHistoryClearsEntireDatabase() = runBlocking {
        repository.addWaterIntake(250, 1000L, "2026-11-19")
        repository.addWaterIntake(500, 2000L, "2026-11-20")

        repository.clearAllHistory()

        assertEquals(0, repository.getTodayConsumedFlow("2026-11-19").first())
        assertEquals(0, repository.getTodayConsumedFlow("2026-11-20").first())
    }

    @Test
    fun userSettingsFlowEmitsDefaultWhenUnset() = runBlocking {
        val settings = repository.getUserSettingsFlow().first()
        assertEquals(2000, settings.dailyGoalMl)
        assertFalse(settings.reminderEnabled)
        assertEquals(60, settings.reminderIntervalMinutes)
    }

    @Test
    fun updateDailyGoalPersistsAndClampsToMinimum() = runBlocking {
        repository.updateDailyGoal(3000)
        assertEquals(3000, repository.getUserSettingsFlow().first().dailyGoalMl)

        repository.updateDailyGoal(50)
        assertEquals(100, repository.getUserSettingsFlow().first().dailyGoalMl)
    }

    @Test
    fun updateReminderSettingsPersistsConfiguration() = runBlocking {
        repository.updateReminderSettings(enabled = true, intervalMinutes = 45, startHour = 9, endHour = 21)

        val settings = repository.getUserSettingsFlow().first()
        assertTrue(settings.reminderEnabled)
        assertEquals(45, settings.reminderIntervalMinutes)
        assertEquals(9, settings.reminderStartHour)
        assertEquals(21, settings.reminderEndHour)
    }

    @Test
    fun dailyTotalsFlowGroupsIntakesByDate() = runBlocking {
        repository.addWaterIntake(200, 1000L, "2026-11-18")
        repository.addWaterIntake(300, 2000L, "2026-11-18")
        repository.addWaterIntake(500, 3000L, "2026-11-19")

        val totals = repository.getDailyTotalsFlow().first()
        val totalsMap = totals.associate { it.dateString to it.total }

        assertEquals(500, totalsMap["2026-11-18"])
        assertEquals(500, totalsMap["2026-11-19"])
    }
}
