# AquaFlow — Product & Engineering Roadmap

This roadmap governs the ongoing autonomous software engineering of AquaFlow. Development proceeds incrementally with rigorous testing and verification at each stage.

---

## Phase Overview

```
Phase 1: Foundation Scaffold & Repository Recovery (COMPLETED)
Phase 2: Local Persistence, MVVM Architecture, Reminders & Statistics (COMPLETED)
Phase 3: Core UX Enhancements, Animation & Midnight Rollover (COMPLETED)
Phase 4: Custom Intake Amounts, Haptic Feedback & Accessibility (COMPLETED)
Phase 5: Boot Recovery, Reliability Audit & Data Management (COMPLETED)
Phase 6: Production Hardening, CI/CD & Release Engineering (CURRENT)
```

---

## Phase Details

### Phase 1: Foundation Scaffold & Repository Recovery ✅
- [x] Recover repository from GitHub, maintaining commit history.
- [x] Reconstruct canonical Android directory hierarchy (`app/`, `gradle/`, `.github/`).
- [x] Configure Gradle 8.13 wrapper, AGP 8.13.2, and OpenJDK 21.
- [x] Fix version catalog dependencies (`libs.versions.toml`).
- [x] Build initial debug APK and run baseline unit tests.

### Phase 2: Persistence, MVVM, Reminders & Statistics ✅
- [x] Room SQLite persistence (`WaterIntakeEntity`, `UserSettingsEntity`).
- [x] Reactive MVVM architecture with `HydrationViewModel` and `StateFlow`.
- [x] Real-time hydration calculations (`HydrationCalculator`).
- [x] Configurable daily goal dialog with presets.
- [x] WorkManager scheduled water reminders with active waking hour suppression.
- [x] Android 13+ runtime notification permissions.
- [x] 7-day hydration statistics calculation and dialog.
- [x] Today's intake history log with deletion/undo support.
- [x] Home screen UI design preserved exactly.

### Phase 3: Core UX Enhancements, Animation & Midnight Rollover ✅
- [x] **Smooth Hydration Animation**: Compose `animateFloatAsState` on the hydration percentage and progress bar for liquid-smooth visual feedback on drinks added.
- [x] **Real-Time Midnight Rollover**: Registered system `Intent.ACTION_DATE_CHANGED` and `Intent.ACTION_TIME_TICK` broadcast receiver so the dashboard automatically resets to 0 ml at midnight even if the app remains in memory.
- [x] **Custom Intake Logging Dialog**: Allows users to log custom amounts (100ml, 250ml, 330ml can, 750ml sports bottle, or manual ml input) in addition to quick-add buttons.
- [x] **Recent Drink Undo SnackBar**: Non-intrusive snackbar with an "Undo" action upon adding water to prevent accidental double-taps.

### Phase 4: Haptic Feedback, Sound & Accessibility ✅
- [x] **Tactile Haptic Feedback**: Integrated `LocalHapticFeedback` on button clicks to provide physical feedback for each logged sip.
- [x] **Accessibility (A11y)**: Added explicit `onClickLabel` semantics to all interactive elements, ensuring full compatibility with TalkBack and screen readers.
- [x] **Dynamic Font & Display Scaling**: Integrated `verticalScroll` ensuring card layout and typography adapt cleanly to large font sizes and small screens without clipping.
- [x] **Hydration Streak Motivation**: Added daily consecutive streak calculation (`🔥 Streak`) surfaced in `StatsDialog`.

### Phase 5: Resilience, Boot Recovery & Data Management ✅
- [x] **Boot & Package Update Receiver**: Automatically reschedules WorkManager reminders after device reboots (`RECEIVE_BOOT_COMPLETED`) and updates (`MY_PACKAGE_REPLACED`).
- [x] **Notification Permission Guard**: Checked `POST_NOTIFICATIONS` runtime permission explicitly on Android 13+ before notifying.
- [x] **High-Priority Notification Channel**: Configured high importance with vibration and lights for reliable delivery.
- [x] **Data Privacy & Reset**: One-tap local data clear option for today or entire history.
- [x] **Physical Acceptance Plan**: Created `DEVICE_TESTING.md` covering all 25 core real-device acceptance scenarios.

### Phase 6: Production Hardening, CI/CD & Release Engineering (CURRENT)
- [x] **Comprehensive Test Coverage**: Expanded automated tests to 30 tests (100% pass rate) covering calculation edge cases, streak logic, and in-memory repository reactive flows.
- [x] **CI/CD Pipeline Hardening**: Modernized `.github/workflows/android-build.yml` to use `./gradlew` wrapper and execute tests.
- [ ] **Proguard / R8 Optimization**: Validate minification and resource shrinking for release builds.
- [ ] **Play Store Release Metadata**: Prepare store assets, privacy disclosure, and release notes.
