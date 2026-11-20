package com.aquaflow.waterreminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aquaflow.waterreminder.AquaFlowApplication
import com.aquaflow.waterreminder.data.HydrationRepository
import com.aquaflow.waterreminder.domain.HydrationCalculator
import com.aquaflow.waterreminder.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HydrationViewModel(
    application: Application,
    private val repository: HydrationRepository
) : AndroidViewModel(application) {

    private val currentDateString = MutableStateFlow(HydrationCalculator.getTodayDateString())
    private val _uiState = MutableStateFlow(HydrationUiState(todayDateString = currentDateString.value))
    val uiState: StateFlow<HydrationUiState> = _uiState.asStateFlow()

    init {
        val soundEnabled = ReminderScheduler.isSoundEnabled(application)
        val pourEnabled = WaterSoundManager.isPourSoundEnabled(application)
        WaterSoundManager.init(application)
        _uiState.update {
            it.copy(
                reminderSoundEnabled = soundEnabled,
                pourSoundEnabled = pourEnabled
            )
        }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getTodayConsumedFlow(currentDateString.value),
                repository.getTodayIntakesFlow(currentDateString.value),
                repository.getUserSettingsFlow(),
                repository.getDailyTotalsFlow()
            ) { consumed, intakes, settings, dailyTotals ->
                val progress = HydrationCalculator.calculateProgress(consumed, settings.dailyGoalMl)
                val percentage = HydrationCalculator.calculatePercentage(consumed, settings.dailyGoalMl)
                val totalsMap = dailyTotals.associate { it.dateString to it.total }
                val stats = HydrationCalculator.aggregateStatsForPastDays(
                    intakesByDate = totalsMap,
                    dailyGoalMl = settings.dailyGoalMl,
                    days = 7
                )
                val streak = HydrationCalculator.calculateStreak(
                    intakesByDate = totalsMap,
                    dailyGoalMl = settings.dailyGoalMl
                )

                _uiState.value.copy(
                    consumedMl = consumed,
                    dailyGoalMl = settings.dailyGoalMl,
                    progress = progress,
                    percentage = percentage,
                    todayDateString = currentDateString.value,
                    todayIntakes = intakes,
                    weeklyStats = stats,
                    streakDays = streak,
                    reminderEnabled = settings.reminderEnabled,
                    reminderIntervalMinutes = settings.reminderIntervalMinutes,
                    reminderStartHour = settings.reminderStartHour,
                    reminderEndHour = settings.reminderEndHour
                )
            }.collect { updatedState ->
                _uiState.update { current ->
                    updatedState.copy(
                        showGoalDialog = current.showGoalDialog,
                        showReminderDialog = current.showReminderDialog,
                        showStatsDialog = current.showStatsDialog,
                        showHistoryDialog = current.showHistoryDialog,
                        showCustomIntakeDialog = current.showCustomIntakeDialog,
                        lastAddedIntakeId = current.lastAddedIntakeId,
                        lastAddedAmount = current.lastAddedAmount
                    )
                }
            }
        }
    }

    fun checkAndResetDay() {
        val today = HydrationCalculator.getTodayDateString()
        if (today != currentDateString.value) {
            currentDateString.value = today
            observeData()
        }
    }

    fun addWater(amountMl: Int) {
        checkAndResetDay()
        val sanitized = HydrationCalculator.sanitizeIntakeAmount(amountMl)
        viewModelScope.launch {
            val insertedId = repository.addWaterIntake(
                amountMl = sanitized,
                timestamp = System.currentTimeMillis(),
                dateString = currentDateString.value
            )
            _uiState.update {
                it.copy(
                    lastAddedIntakeId = insertedId,
                    lastAddedAmount = sanitized
                )
            }
        }
    }

    fun undoLastIntake() {
        val lastId = _uiState.value.lastAddedIntakeId ?: return
        viewModelScope.launch {
            repository.deleteIntake(lastId)
            _uiState.update {
                it.copy(
                    lastAddedIntakeId = null,
                    lastAddedAmount = null
                )
            }
        }
    }

    fun clearLastAdded() {
        _uiState.update {
            it.copy(
                lastAddedIntakeId = null,
                lastAddedAmount = null
            )
        }
    }

    fun deleteIntake(id: Long) {
        viewModelScope.launch {
            repository.deleteIntake(id)
        }
    }

    fun clearTodayIntakes() {
        viewModelScope.launch {
            repository.deleteAllIntakesForDate(currentDateString.value)
            _uiState.update {
                it.copy(
                    lastAddedIntakeId = null,
                    lastAddedAmount = null
                )
            }
        }
    }

    fun updateDailyGoal(newGoalMl: Int) {
        viewModelScope.launch {
            repository.updateDailyGoal(newGoalMl)
        }
    }

    fun updateReminderSettings(
        enabled: Boolean,
        intervalMinutes: Int,
        soundEnabled: Boolean = ReminderScheduler.isSoundEnabled(getApplication())
    ) {
        ReminderScheduler.setSoundEnabled(getApplication(), soundEnabled)
        _uiState.update { it.copy(reminderSoundEnabled = soundEnabled) }
        viewModelScope.launch {
            repository.updateReminderSettings(enabled, intervalMinutes)
            if (enabled) {
                ReminderScheduler.scheduleReminders(getApplication(), intervalMinutes)
            } else {
                ReminderScheduler.cancelReminders(getApplication())
            }
        }
    }

    fun setGoalDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showGoalDialog = visible) }
    }

    fun setReminderDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showReminderDialog = visible) }
    }

    fun setStatsDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showStatsDialog = visible) }
    }

    fun setHistoryDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showHistoryDialog = visible) }
    }

    fun setCustomIntakeDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showCustomIntakeDialog = visible) }
    }

    fun updatePourSoundEnabled(enabled: Boolean) {
        WaterSoundManager.setPourSoundEnabled(getApplication(), enabled)
        _uiState.update { it.copy(pourSoundEnabled = enabled) }
    }

    override fun onCleared() {
        super.onCleared()
        WaterSoundManager.release()
    }

    companion object {
        fun provideFactory(application: AquaFlowApplication): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HydrationViewModel(application, application.repository) as T
                }
            }
    }
}
