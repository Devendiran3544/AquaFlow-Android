# AquaFlow — Changelog

All notable changes to the AquaFlow Android project are documented in this file.

---

## [0.1.0] - Water-Pour Experience & Haptics Enhancement (2026-11-20)

### Added
- **Tactile Haptic Feedback Fix (`HapticFeedbackHelper`)**:
  - Investigated and resolved haptic feedback suppression caused by Compose default `AndroidHapticFeedback` failing when system touch feedback is disabled or views lack explicit flags.
  - Added `<uses-permission android:name="android.permission.VIBRATE" />` to `AndroidManifest.xml`.
  - Implemented dual-path haptics: primary `View.performHapticFeedback` with `FLAG_IGNORE_VIEW_SETTING`, with automatic fallback to system `Vibrator` / `VibratorManager` executing a crisp, subtle 20 ms click (`VibrationEffect.EFFECT_CLICK`).
  - Haptics trigger immediately upon pressing quick-add buttons (+150, +200, +250, +500) and custom intake confirmation, functioning independently of sound settings.
- **Realistic Water-Pour Audio (`WaterSoundManager`)**:
  - Replaced generic single-drop sound with a realistic, pleasant, 0.95-second water-pouring sound (`app/src/main/res/raw/water_pour.wav`, ~83 KB), synthesizing Helmholtz air column frequency rise (480 Hz -> 980 Hz), fluid turbulence, and stochastic Minnaert air-bubble dynamics.
  - Built `WaterSoundManager` using Android `SoundPool` with `maxStreams = 1` and stream cutoff, preventing audio overlap during rapid repeated taps.
  - Added independent in-app "Water Pouring Sound" toggle in `ReminderDialog`.
  - Migrated reminder notification channel to `aquaflow_reminders_v3` (soothing pour chime) with clean deletion of legacy channels.
- **Water-Pouring Glass UI Animation (`WaterGlassVessel`)**:
  - Replaced static droplet emoji with an interactive modern glass vessel illustration rendered via hardware-accelerated Compose `Canvas`.
  - Displays dynamic water level with continuous ambient wave motion and animated agitation during pouring.
  - Visual water stream arches gracefully into the glass with radiating impact ripples during intake.
  - Smoothly animates consumed ml (`animateIntAsState`) and percentage counter alongside floating "+XXX ml" badge.
  - Preserved existing Undo Snackbar, light-blue aesthetic, and zero-network privacy architecture.

---

## [0.1.0] - Focused Enhancements (2026-11-20)

### Added
- **Custom Water Drop Notification Sound**:
  - Synthesized a pleasant, short, high-fidelity 16-bit PCM water-drop chime asset (`app/src/main/res/raw/water_drop.wav`, ~28 KB) without paid assets or external dependencies.
  - Implemented dual notification channel architecture in `ReminderScheduler` (`aquaflow_reminders_v2` with custom sound, and `aquaflow_reminders_silent_v2` for vibration only).
  - Added safe migration strategy deleting obsolete legacy channel `aquaflow_reminders_channel`.
  - Added in-app "Water Drop Sound" toggle switch in `ReminderDialog`, persisted via `SharedPreferences`.
  - Configured status bar silhouette icon (`ic_stat_water_drop.xml`) for clean reminder display.
- **Water Intake Animation**:
  - Added organic water-drop pulse and bounce animation on the central emoji droplet (`dropScale` spring animation) when a drink is logged.
  - Added temporary floating "+XXX ml" badge visual feedback with smooth upward float and fade-out inside the hydration card.
  - Preserved existing haptic feedback (`HapticFeedbackType.LongPress`) on all button touches.
  - Maintained complete separation from Undo Snackbar, allowing instant undo without interference.
- **Professional Adaptive App Icon**:
  - Designed modern, minimalist Android adaptive launcher icon (`ic_launcher.xml` and `ic_launcher_round.xml`).
  - Created high-contrast gradient background (`ic_launcher_background.xml`) using AquaBlue (`#087EBC`) to DeepBlue (`#05537D`).
  - Created crisp, multi-layered water droplet foreground (`ic_launcher_foreground.xml`) with specular highlight arc and inner liquid depth accent, adhering to Android 72dp safe zone.
  - Updated `AndroidManifest.xml` with `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`.

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
