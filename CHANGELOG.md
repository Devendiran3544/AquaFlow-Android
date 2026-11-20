# AquaFlow — Changelog

All notable changes to the AquaFlow Android project are documented in this file.

---

## [0.1.0] - Phase 3 Completion & Quality Audit (2026-11-20)

### Added
- **Hydration Streak Tracking**: Implemented `HydrationCalculator.calculateStreak()` calculating daily consecutive goals achieved, preserved across in-progress days, and displayed in `StatsDialog` as `🔥 Streak`.
- **Database Deletion APIs**: Added `deleteAllIntakesForDate()` and `clearAllHistory()` in `WaterIntakeDao` and `HydrationRepository` for user data control.
- **Clear All Intake Option**: Added "Clear All" action in `HistoryDialog` to reset today's entries with one tap.
- **Scrollable Responsive Layout**: Added `verticalScroll(rememberScrollState())` to `MainActivity` Column ensuring full usability on small screens and with high accessibility font scaling.
- **Accessibility & TalkBack Support**: Added `onClickLabel` semantics to interactive icons (💧 history, stats, reminders, custom add, goal card) for screen readers.
- **App Update Rescheduling**: Added `ACTION_MY_PACKAGE_REPLACED` filter to `BootReceiver` so reminders are rescheduled automatically after app updates without needing device reboot.
- **Notification Permission Guard**: Added explicit runtime `POST_NOTIFICATIONS` check in `WaterReminderWorker` on Android 13+.
- **Instantaneous Rescheduling**: Configured WorkManager to use `ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE` for immediate interval updates.
- **Expanded Test Suite (30 Tests)**:
  - Added 6 hydration streak calculation tests in `WaterProgressTest`.
  - Created `HydrationRepositoryTest` with 9 in-memory tests verifying reactive Flow emissions, intake additions, deletions, daily clearing, and goal/reminder settings persistence.
- **Acceptance Test Suite**: Created `DEVICE_TESTING.md` covering all 25 physical device acceptance scenarios.

### Changed
- Explicitly pinned OpenJDK 21 toolchain (`org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr`) in `gradle.properties` to avoid host JDK 25 incompatibilities.
- Updated `.github/workflows/android-build.yml` to use `./gradlew` wrapper with execution permissions.

---

## [0.1.0] - Phase 2 Core Features (2026-11-20)

### Added
- **Persistent Local Storage**: AndroidX Room database (`AquaFlowDatabase`) with `WaterIntakeEntity` and `UserSettingsEntity`.
- **Reactive Architecture**: Kotlin Coroutines `StateFlow` and Flow combination in `HydrationViewModel` and `HydrationRepository`.
- **Quick-Add Buttons**: Functional intake buttons for +150 ml, +200 ml, +250 ml, and +500 ml.
- **Dynamic Hydration Calculations**: `HydrationCalculator` for real-time progress ratios, percentage clamping, and 7-day stats aggregation.
- **Daily Intake History**: `HistoryDialog` showing timestamped entries for today with individual deletion.
- **Customizable Goals**: `GoalDialog` supporting 1500, 2000, 2500, 3000 ml presets and custom volume inputs.
- **Configurable Water Reminders**: `ReminderScheduler` and `WaterReminderWorker` with quiet hours and goal-completion suppression.
- **Automated Unit Tests**: 15 unit tests covering mathematical formulas, boundary conditions, and date transitions.

---

## [0.1.0] - Phase 1 Foundation & Repository Recovery (2026-11-20)

### Added
- Reconstructed canonical Android Gradle project layout from starter repository.
- Generated Gradle 8.13 wrapper (`gradlew.bat`, `gradlew`, wrapper jar).
- Configured Android Gradle Plugin 8.13.2, Kotlin 2.2.21, Compile/Target SDK 36, Min SDK 26, Java 17 bytecode.
- Implemented core branding: AquaBlue (`#087EBC`), DeepBlue (`#075985`), PaleBlue (`#E7F6FF`), gradient central card.
