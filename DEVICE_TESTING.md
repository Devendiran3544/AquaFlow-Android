# AquaFlow — Real Device Acceptance Test Plan

**Version**: 0.1.0  
**Package**: `com.aquaflow.waterreminder`  
**Target Hardware**: Physical Android Devices (Android 8.0 Oreo / API 26 through Android 16 / API 36)  
**Execution Type**: Manual & Automated ADB Acceptance Testing  

---

## 1. Overview & Testing Philosophy

AquaFlow is designed to be an offline-first, battery-efficient, and zero-cloud hydration companion. Because Android background execution and notification policies vary significantly across OEM distributions (Google Pixel, Samsung One UI, Xiaomi HyperOS/MIUI, OnePlus OxygenOS), physical device acceptance testing is mandatory to ensure reliability.

This test plan defines explicit, reproducible procedures for all 25 critical acceptance scenarios.

---

## 2. Test Environment Setup & ADB Preconditions

Before commencing physical device testing:
1. Enable **Developer Options** and **USB Debugging** on the target device.
2. Connect the device via USB (`adb devices`).
3. Verify device time and timezone match local time.
4. Install debug APK:
   ```powershell
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 3. Acceptance Test Matrix (25 Core Scenarios)

### Test Case 1: First Installation
- **Objective**: Verify clean installation without crashes or unexpected permissions.
- **Preconditions**: Device does not have AquaFlow installed.
- **Procedure**: Run `adb install app/build/outputs/apk/debug/app-debug.apk`.
- **Expected Result**: Installation completes with `Success`. Application icon appears in the launcher. Zero background network requests or dangerous permissions requested on install.

### Test Case 2: Initial App Launch
- **Objective**: Verify default state initialization.
- **Preconditions**: App freshly installed.
- **Procedure**: Tap the AquaFlow launcher icon.
- **Expected Result**: Home screen renders instantly (< 500ms).
  - AquaFlow branding and "Every sip matters" header visible.
  - Hydration card displays `0 ml` of `2000 ml daily goal ✎` and `0% completed`.
  - Four quick-add buttons (+150, +200, +250, +500) rendered with AquaBlue theme.
  - Subheader displays "Today's hydration", "📊 Stats", and "⏰ Reminders".

### Test Case 3: Water Intake Button Functionality
- **Objective**: Verify single tap logging and tactile response.
- **Preconditions**: App is open at 0 ml.
- **Procedure**: Tap `+250`.
- **Expected Result**:
  - Haptic feedback vibrates lightly.
  - Progress card animates smoothly from 0 ml to `250 ml` (`12% completed`).
  - Snackbar appears at the bottom: `"Added 250 ml of water"` with `"Undo"` action.

### Test Case 4: Multiple Water Additions
- **Objective**: Verify arithmetic accumulation.
- **Preconditions**: App at 250 ml.
- **Procedure**: Sequentially tap `+150`, then `+200`, then `+500`.
- **Expected Result**: Total progresses:
  - After +150: `400 ml` (20%)
  - After +200: `600 ml` (30%)
  - After +500: `1100 ml` (55%)
  - All additions animate smoothly without UI stutter.

### Test Case 5: Custom Water Intake
- **Objective**: Verify logging arbitrary volumes not covered by quick presets.
- **Preconditions**: App open on home screen.
- **Procedure**:
  1. Tap `+ Custom` next to "Add a drink".
  2. In the dialog, verify presets (100ml, 250ml, 330ml, 750ml).
  3. Enter `330` in the text field (standard soda/can volume).
  4. Tap "Add Intake".
- **Expected Result**:
  - Dialog dismisses.
  - Consumption increases by exactly 330 ml.
  - Snackbar confirms `"Added 330 ml of water"`.

### Test Case 6: Immediate Undo
- **Objective**: Verify instant cancellation of accidental taps.
- **Preconditions**: App open on home screen.
- **Procedure**:
  1. Note current intake (e.g. 1100 ml).
  2. Tap `+500` (intake becomes 1600 ml).
  3. Immediately tap "Undo" on the snackbar.
- **Expected Result**:
  - The +500 ml entry is removed from Room database.
  - Progress card animates back down to 1100 ml.
  - History log no longer contains the 500 ml entry.

### Test Case 7: App Restart and Persistence
- **Objective**: Verify persistent SQLite storage across process restarts.
- **Preconditions**: Logged 1100 ml in the app.
- **Procedure**:
  1. Swipe the app away from the Android Recents overview (force stop process).
  2. Re-open AquaFlow from the launcher.
- **Expected Result**:
  - App displays `1100 ml` of `2000 ml` immediately without data loss.

### Test Case 8: Daily Goal Customization
- **Objective**: Verify custom goal persistence and dynamic progress percentage recalculation.
- **Preconditions**: App open at 1000 ml consumed.
- **Procedure**:
  1. Tap the central card (`of 2000 ml daily goal ✎`).
  2. In Goal Dialog, select preset `2500ml` (or type `3000`).
  3. Tap "Save Goal".
- **Expected Result**:
  - Goal updates to 2500 ml.
  - Progress recalculates: 1000 / 2500 = `40% completed` (smoothly animated).
  - New goal persists across app restarts.

### Test Case 9: Intake History & Deletion
- **Objective**: Verify timestamped drink history and selective record removal.
- **Preconditions**: Multiple drinks logged today.
- **Procedure**:
  1. Tap the 💧 droplet icon in the top header.
  2. Verify all today's drinks are listed chronologically with volume and formatted time (e.g. `+250 ml — 10:15 AM`).
  3. Tap "Remove" on one entry.
- **Expected Result**:
  - Selected entry is deleted.
  - Header updates and home screen progress immediately drops by that volume.
  - Tapping "Clear All" clears today's intake after confirmation.

### Test Case 10: 7-Day Statistics & Hydration Streak
- **Objective**: Verify past 7 days calculation and streak counting.
- **Preconditions**: Drinks logged today.
- **Procedure**: Tap `📊 Stats` in the subheader.
- **Expected Result**:
  - Summary row displays `🔥 Streak`, `Avg Daily`, `7-Day Total`, and `Goal Met`.
  - Past 7 days bar charts displayed with days labels (Mon, Tue, ... Today).
  - Green bars for days reaching goal; blue bars for in-progress days.

### Test Case 11: Reminder Configuration
- **Objective**: Verify reminder settings persistence in Room.
- **Preconditions**: Reminders dialog opened.
- **Procedure**:
  1. Tap `⏰ Reminders`.
  2. Toggle "Enable Reminders" switch to ON.
  3. Select interval chip `45m`.
  4. Tap "Save".
- **Expected Result**:
  - Dialog dismisses; subheader updates to `⏰ On`.
  - WorkManager schedules periodic work `aquaflow_periodic_reminder`.

### Test Case 12: Notification Permissions (Android 13+)
- **Objective**: Verify runtime `POST_NOTIFICATIONS` permission prompt.
- **Preconditions**: Android 13, 14, 15, or 16 device; permission not yet granted.
- **Procedure**:
  1. Toggle "Enable Reminders" ON in Reminder Dialog.
- **Expected Result**:
  - System dialog appears: *"Allow AquaFlow to send you notifications?"*.
  - If "Allow" is clicked: Switch stays ON and schedule is saved.
  - If "Don't allow" is clicked: Switch turns OFF gracefully without crash.

### Test Case 13: Notification Delivery
- **Objective**: Verify visual rendering, vibration, and tap intent.
- **Preconditions**: Reminders enabled, intake below daily goal.
- **Procedure**: Trigger worker via ADB:
  ```powershell
  adb shell cmd jobscheduler run -f com.aquaflow.waterreminder 1001
  ```
- **Expected Result**:
  - Heads-up notification appears:
    - Title: `💧 Time to hydrate!`
    - Body: Remaining ml to hit goal.
  - Device vibrates.
  - Tapping notification opens `MainActivity`.

### Test Case 14: Reminder Interval Behavior
- **Objective**: Verify interval updates cancel and reschedule cleanly.
- **Preconditions**: Reminders set to 60m.
- **Procedure**:
  1. Change interval to `30m`.
  2. Save settings.
- **Expected Result**: Existing work cancelled and rescheduled with 30m interval without duplicate worker jobs.

### Test Case 15: Reminder Time Windows (Quiet Hours)
- **Objective**: Verify suppression during sleep hours (10:00 PM – 8:00 AM).
- **Preconditions**: Reminders enabled.
- **Procedure**:
  1. Temporarily change device clock to 11:30 PM:
     ```powershell
     adb shell "su 0 date 112023302026.00"
     ```
  2. Trigger reminder worker.
- **Expected Result**: Worker returns `Result.success()`. Zero notifications displayed. User sleep is never disturbed.

### Test Case 16: Goal-Completion Reminder Suppression
- **Objective**: Verify reminders pause once today's goal is met.
- **Preconditions**: Daily goal is 2000 ml. Log 2000 ml.
- **Procedure**: Trigger reminder worker.
- **Expected Result**: Worker checks `totalConsumed >= settings.dailyGoalMl`, returns `Result.success()`, and suppresses notification.

### Test Case 17: Date Rollover (Midnight Reset)
- **Objective**: Verify daily intake resets to 0 ml at 00:00 without app restart.
- **Preconditions**: App is open in foreground with 1500 ml logged on 2026-11-20.
- **Procedure**: Advance system date across midnight:
  ```powershell
  adb shell am broadcast -a android.intent.action.DATE_CHANGED
  ```
- **Expected Result**:
  - BroadcastReceiver in `MainActivity` catches `ACTION_DATE_CHANGED`.
  - Progress card resets smoothly to `0 ml of 2000 ml daily goal (0%)`.
  - Historical data for 2026-11-20 is preserved in Stats.

### Test Case 18: App Backgrounding
- **Objective**: Verify app state preservation when user navigates away.
- **Preconditions**: App open.
- **Procedure**: Press Home button, open other apps, return to AquaFlow 10 minutes later.
- **Expected Result**: App resumes immediately in exact same state.

### Test Case 19: App Process Termination
- **Objective**: Verify database integrity when Android OS kills the process under memory pressure.
- **Preconditions**: App logged 800 ml.
- **Procedure**:
  ```powershell
  adb shell am kill com.aquaflow.waterreminder
  ```
  Relaunch app from launcher.
- **Expected Result**: No crash; Room database reads saved 800 ml cleanly.

### Test Case 20: Device Restart (BootReceiver)
- **Objective**: Verify reminders reschedule automatically upon system boot.
- **Preconditions**: Reminders enabled in settings.
- **Procedure**:
  ```powershell
  adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p com.aquaflow.waterreminder
  ```
- **Expected Result**:
  - `BootReceiver` triggers `goAsync()`.
  - Reads `reminderEnabled = true` from Room.
  - Re-enqueues periodic work in WorkManager.

### Test Case 21: Offline Functionality
- **Objective**: Verify full app functionality without internet connectivity.
- **Preconditions**: Put device into Airplane Mode (Wi-Fi OFF, Cellular OFF, Bluetooth OFF).
- **Procedure**: Perform all app actions (logging, goal change, history deletion, stats review).
- **Expected Result**: 100% of features function identically. Zero errors, zero connection warnings.

### Test Case 22: UI Responsiveness & Smooth Animation
- **Objective**: Verify 60/120 fps fluid rendering on device display.
- **Preconditions**: Device with 90Hz or 120Hz display.
- **Procedure**: Tap intake buttons and scroll dialogs while monitoring GPU profiling (`Profile HWUI rendering` in Developer Options).
- **Expected Result**: Frame rendering stays consistently under 16ms (60fps) or 8ms (120fps) without frame drops.

### Test Case 23: Accessibility (TalkBack & Font Scaling)
- **Objective**: Verify TalkBack screen reader and large text compatibility.
- **Preconditions**: Enable TalkBack and set Font Size to Largest (130%-200%).
- **Procedure**: Navigate the screen using TalkBack swipe gestures.
- **Expected Result**:
  - All interactive elements have descriptive TalkBack announcements ("View today's drink history", "Add custom volume drink").
  - `verticalScroll` allows reading entire screen without text clipping.
  - Touch targets meet Android 48dp minimum standard.

### Test Case 24: Data Consistency
- **Objective**: Verify rapid concurrent tapping does not lose records or produce inconsistent totals.
- **Preconditions**: App at 0 ml.
- **Procedure**: Tap `+250` five times in under 1 second.
- **Expected Result**:
  - Five entries inserted into SQLite database.
  - Total is exactly 1250 ml.
  - History log shows all 5 entries.

### Test Case 25: Error Scenarios & Input Validation
- **Objective**: Verify robust handling of unusual or extreme user inputs.
- **Preconditions**: Goal and Custom Intake dialogs.
- **Procedure**:
  1. In Goal Dialog, type `0`, negative numbers, or empty string.
  2. In Custom Intake, type `999999` or non-numeric characters.
- **Expected Result**:
  - Goal dialog automatically clamps values between 500 ml and 10000 ml.
  - Custom intake clamps values between 10 ml and 5000 ml.
  - App never crashes, freezes, or produces SQLite syntax exceptions.
