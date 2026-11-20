# AquaFlow — Testing Strategy & Verification Guide

This document defines the automated testing standards, test coverage, and execution instructions for the AquaFlow Android application.

---

## 1. Testing Pyramid

AquaFlow employs a multi-tiered testing strategy:

1. **Unit Tests (JVM)**: Rapid, deterministic tests for business logic, math calculations, stats aggregation, date transitions, and formatting.
2. **ViewModel Tests**: StateFlow emissions and state transition verification using Kotlin Coroutines test dispatchers.
3. **Database Integration Tests**: Verification of Room DAOs, queries, and constraints against in-memory SQLite instances.
4. **Build Verification**: Continuous automated verification of debug APK assembly and packaging via Gradle.

---

## 2. Test Execution Commands

### Run all Unit Tests:
```powershell
.\gradlew.bat :app:testDebugUnitTest
```

### Build Debug APK:
```powershell
.\gradlew.bat :app:assembleDebug
```

### Full Verification Pipeline:
```powershell
.\gradlew.bat :app:check :app:assembleDebug
```

---

## 3. Test Suites

### `WaterProgressTest` (`app/src/test/java/com/aquaflow/waterreminder/WaterProgressTest.kt`)
- `progressIsZeroAtStart`: Verifies 0 ml consumed against a 2000 ml goal yields 0.0f ratio and 0%.
- `progressIsHalfAtHalfGoal`: Verifies 1000 ml consumed against a 2000 ml goal yields 0.5f ratio and 50%.
- `progressIsClampedAtOneWhenGoalExceeded`: Verifies consumption over the goal (e.g. 2500 ml / 2000 ml) clamps cleanly at 1.0f (100%).
- `progressHandlesZeroOrNegativeGoalGracefully`: Edge case testing against division by zero.
- `customGoalProgressCalculatesCorrectly`: Tests custom non-standard goals (3000 ml, 2500 ml).
- `isNewDayDetectsDateChangeCorrectly`: Tests date equality, rollover triggers, and empty/blank date safeguards.
- `weeklyStatsAggregationCoversAllSevenDays`: Verifies exact 7-day calendar window aggregation, ordering, and day labels.
- `hydrationRecordAdditionAggregatesCorrectly`: Tests multi-sip arithmetic, remaining volume calculations, and percentages.
