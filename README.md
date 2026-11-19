# AquaFlow – Smart Water Reminder

AquaFlow is an offline-first Android hydration reminder app.

## Current milestone
Phase 1 foundation scaffold:
- Kotlin + Jetpack Compose + Material 3
- Minimum SDK 26; compile/target SDK 36
- Basic hydration dashboard and quick-add buttons
- GitHub Actions workflow for debug APK and unit tests

## Build
Requires JDK 17, Android SDK 36, and Gradle 8.13-compatible tooling.

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

## Cloud build
Push to `main` or run **Actions → Android CI → Run workflow**.
Download `AquaFlow-debug-apk` from the completed workflow run's Artifacts section.

## Roadmap
1. Foundation and architecture
2. Premium UI and animations
3. Water tracking and local persistence
4. Smart reminders, sounds and voice
5. Statistics and settings
6. Device testing and release
