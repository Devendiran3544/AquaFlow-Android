# AquaFlow v0.1.0 — Initial Release

AquaFlow is a modern, privacy-focused, zero-cost Android hydration tracking and water reminder application built natively with Kotlin and Jetpack Compose. Designed with a calming light-blue aesthetic, tactile haptic feedback, and fluid water-pouring animations, AquaFlow helps you stay consistently hydrated throughout the day without ads, tracking, or accounts.

---

## ✨ Features & Highlights

### 💧 Water Intake Tracking
- **Quick-Add Logging**: Instant one-tap logging buttons for **+150 ml**, **+200 ml**, **+250 ml**, and **+500 ml**.
- **Custom Intake Logging**: Easily log custom drink volumes with quick presets (100 ml, 250 ml, 330 ml can, 750 ml bottle) or exact manual milliliter inputs.
- **Intake History**: View a timestamped breakdown of all drinks logged today with single-entry deletion and a "Clear All" option.
- **Instant Undo Action**: Non-intrusive Snackbar allows you to immediately revert any accidental water addition with one tap.

### 🌊 Satisfying Water-Pour Experience
- **Live Glass & Pour Animation**: A modern glass vessel illustration dynamically simulates fluid levels, continuous ambient wave motion, and an animated water stream with radiating impact ripples when drinks are added.
- **Smooth Number & Progress Counters**: Watch your consumed milliliter count and completion percentage roll smoothly upward.
- **Tactile Haptic Feedback**: Immediate, subtle tactile clicks upon tapping intake buttons, powered by a dual-path mechanism with system vibration fallback.
- **Water-Pouring Audio Effects**: Realistic 0.95-second acoustic water-pour sound synthesized with Helmholtz air-cavity resonance and fluid bubble acoustics, with rapid-tap audio overlap protection.
- **Independent Audio Controls**: Separate in-app toggles for intake water-pouring sounds and notification alert chimes.

### 🎯 Daily Goals & Progress
- **Customizable Hydration Target**: Set your personal daily target (presets from 1,500 ml to 3,000 ml, or any custom volume).
- **Streak & Statistics**: Track your 7-day completion averages, daily breakdown bars, and consecutive daily hydration streaks (`🔥 Streak`).
- **Midnight Date Rollover**: Real-time broadcast receivers automatically reset today's intake on-screen across midnight without requiring an app restart.

### ⏰ Smart Water Reminders
- **Battery-Friendly Background Scheduling**: Uses AndroidX WorkManager for periodic reminder delivery.
- **Quiet Hours & Goal Suppression**: Automatically pauses alerts between 10:00 PM and 8:00 AM and suppresses reminders once your daily goal has been reached.
- **Modern Notification Channels**: Android 8.0+ to Android 14/15/16 notification channel integration (`aquaflow_reminders_v3`) with soothing chime, vibration, and Android 13+ runtime permission handling.
- **Boot & Package Update Restorer**: Reschedules reminders automatically upon device reboot or app update.

### 🔒 100% Offline Privacy
- **Zero Network Permissions**: AquaFlow requests **no `android.permission.INTERNET`** permission. Your data never leaves your device.
- **Local Room Storage**: All hydration records and user preferences are persisted safely on-device in an SQLite database via AndroidX Room.

---

## 📱 Installation Instructions (Android APK Sideloading)

AquaFlow is distributed directly as an APK binary. Because it is installed outside of the Google Play Store, Android requires manual installation permission.

### How to Install:
1. **Download the APK**: Download `AquaFlow-v0.1.0.apk` from the **Assets** section below.
2. **Open the Downloaded File**: Tap the downloaded `.apk` file from your browser downloads or file manager.
3. **Allow Installation from Unknown Sources**:
   - If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings**.
   - Toggle **Allow from this source** to ON, then return to the installer.
4. **Complete Installation**: Tap **Install**. Once installed, tap **Open** to launch AquaFlow.
5. **Notification Permission (Android 13+)**: When prompted, allow notification permissions so AquaFlow can send scheduled hydration reminders.

---

## ⚠️ Known Limitations & Compatibility

- **Direct APK Distribution**: This release is distributed as an independent APK package and is not currently published on Google Play.
- **Play Protect Notice**: On some Android versions, Google Play Protect may display an *"Unrecognized app developer"* prompt for sideloaded APKs. Tap *"Install anyway"* or scan with Play Protect to verify package safety.
- **Minimum Requirements**: Requires **Android 8.0 (API level 26)** or higher. Target SDK is **Android 16 (API level 36)**.

---

## 📦 Package Information & Integrity
- **App Name**: AquaFlow
- **Package Name**: `com.aquaflow.waterreminder`
- **Version Name**: `0.1.0`
- **Version Code**: `1`
- **File Name**: `AquaFlow-v0.1.0.apk`
- **File Size**: `19,154,599 bytes` (~18.2 MB)
- **SHA-256 Hash**: `01ABE2668892FB134B859185245445E3B135F381DDFF4C33B9317E5DBE0902A4`
