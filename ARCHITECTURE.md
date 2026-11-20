# AquaFlow — Architecture Documentation

This document describes the architectural principles, component structure, data flows, and design patterns used across AquaFlow.

---

## 1. Architectural Philosophy

AquaFlow follows the official Android Architecture Guidelines and modern Clean Architecture principles:

- **Offline-First**: Zero reliance on remote backends or cloud services; full functionality available offline.
- **Unidirectional Data Flow (UDF)**: UI states flow downwards from ViewModel to Composables; user events flow upwards from Composables to ViewModel.
- **Separation of Concerns**: Clear boundary between UI (Presentation), Domain (Business logic/calculations), and Data (Persistence and background workers).
- **Reactive State Management**: Single source of truth managed via Kotlin Coroutines `StateFlow` and Room `Flow` queries.
- **Resource Efficiency**: Zero polling; background reminders rely entirely on Android WorkManager.

---

## 2. Component Diagram

```
+-------------------------------------------------------------+
|                     PRESENTATION LAYER                       |
|                                                             |
|   MainActivity (Activity)                                   |
|       │                                                     |
|       ▼                                                     |
|   AquaFlowHome (Composable) ───► HydrationDialogs           |
|       │                           (Goal, Reminder, Stats,   |
|       │ StateFlow (UI State)       History, Custom Intake)  |
|       ▼                                                     |
|   HydrationViewModel                                        |
+------------------------------┬------------------------------+
                               │ Events / Method Calls
                               ▼
+-------------------------------------------------------------+
|                        DOMAIN LAYER                         |
|                                                             |
|   HydrationCalculator                                       |
|   - calculateProgress() / calculatePercentage()             |
|   - isNewDay()                                              |
|   - aggregateStatsForPastDays()                             |
+------------------------------┬------------------------------+
                               │ Calls
                               ▼
+-------------------------------------------------------------+
|                         DATA LAYER                          |
|                                                             |
|   HydrationRepository                                       |
|       │                                                     |
|       ├──────────────────────────────┐                      |
|       ▼                              ▼                      |
|   WaterIntakeDao               UserSettingsDao              |
|       │                              │                      |
|       └──────────────┬───────────────┘                      |
|                      ▼                                      |
|             AquaFlowDatabase (Room)                         |
|             - water_intakes (table)                         |
|             - user_settings (table)                         |
+-------------------------------------------------------------+
                               ▲
                               │ Work Execution
+------------------------------┴------------------------------+
|                    BACKGROUND WORKER LAYER                  |
|                                                             |
|   ReminderScheduler (WorkManager Enqueuer)                  |
|   WaterReminderWorker (CoroutineWorker)                     |
|   - NotificationManager Channel ("aquaflow_reminders")      |
+-------------------------------------------------------------+
```

---

## 3. Package Structure

```
com.aquaflow.waterreminder
├── AquaFlowApplication.kt       # Application class, DB & Channel initialization
├── MainActivity.kt              # Main Activity & Jetpack Compose Host
│
├── data/                        # Persistence & Repository
│   ├── AquaFlowDatabase.kt      # Room Database
│   ├── WaterIntakeEntity.kt     # Water intake record entity
│   ├── UserSettingsEntity.kt    # User preferences entity
│   ├── WaterIntakeDao.kt        # Intake queries & aggregation
│   ├── UserSettingsDao.kt       # Preferences queries & upserts
│   └── HydrationRepository.kt   # Clean abstraction layer
│
├── domain/                      # Pure Business Logic
│   └── HydrationCalculator.kt   # Progress, stats aggregation, date helpers
│
├── reminder/                    # WorkManager & Notifications
│   ├── ReminderScheduler.kt     # Periodic work scheduling & notification channel
│   └── WaterReminderWorker.kt   # Coroutine worker checking goals & waking hours
│
└── ui/                          # Presentation (Jetpack Compose)
    ├── HydrationUiState.kt      # Immutable UI state data class
    ├── HydrationViewModel.kt    # ViewModel managing StateFlow
    └── HydrationDialogs.kt      # Goal, Reminder, Stats, History modals
```

---

## 4. Key Data Flows

### A. Logging a Drink
1. User taps quick-add button (`+250 ml`) or custom drink button.
2. `AquaFlowHome` calls `viewModel.addWater(250)`.
3. `HydrationViewModel` delegates to `HydrationRepository.addWaterIntake()`.
4. `WaterIntakeDao` executes an `INSERT` statement into `water_intakes` table.
5. Room automatically emits updated query results through `Flow<Int?>` and `Flow<List<WaterIntakeEntity>>`.
6. `HydrationViewModel` combines the emissions, recalculates progress via `HydrationCalculator`, and updates `_uiState.value`.
7. `AquaFlowHome` collects the new state and smoothly animates the updated hydration level.

### B. Midnight Rollover
1. The app tracks the active calendar date string (`yyyy-MM-dd`).
2. On app resume or date transition, `checkAndResetDay()` updates the date query.
3. Room evaluates the new date; because no drinks exist for the new calendar day, today's total evaluates to 0 ml.
4. Historical drinks remain safely in the database and are aggregated by the 7-day statistics view.

### C. Periodic Reminders
1. `ReminderScheduler` schedules a `PeriodicWorkRequest` using WorkManager.
2. At the scheduled interval, `WaterReminderWorker` wakes up in the background.
3. The worker queries the database to check:
   - Are reminders enabled?
   - Is the current time within waking hours (8 AM – 10 PM)?
   - Has today's daily goal already been achieved?
4. If hydration is still needed, a notification is posted to the user's notification drawer with a pending intent to open `MainActivity`.
