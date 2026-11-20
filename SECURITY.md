# AquaFlow — Security Policy & Data Privacy

AquaFlow prioritizes user privacy and device security above all else. This application is architected from the ground up as a zero-cost, offline-first application with zero telemetry, zero analytics tracking, and zero data transmission.

---

## 1. Data Privacy Principles

- **Local Storage Only**: All user information (water intake amounts, timestamps, daily goals, and reminder preferences) is stored exclusively in the private app sandbox via Android SQLite / Room.
- **Zero Cloud Transmission**: AquaFlow makes zero network calls during normal operation. No backend servers, analytics platforms, or ad networks are integrated.
- **No Personal Identifiable Information (PII)**: The application does not collect names, email addresses, phone numbers, location data, or device identifiers.

---

## 2. Secrets Management & Repository Protection

To ensure no machine-specific credentials or sensitive configuration files ever enter the Git repository, the following rules are strictly enforced:

- `local.properties` is explicitly ignored by `.gitignore`.
- Private keystores (`*.jks`, `*.keystore`) and temporary certificates (`*.cer`, `cacerts`) are explicitly ignored by `.gitignore`.
- CI/CD workflows only access necessary build tools without exposing tokens or keys.

---

## 3. Android Security Standards

- **Permissions**: AquaFlow requests only `POST_NOTIFICATIONS` (for water reminders) and `RECEIVE_BOOT_COMPLETED` (for restoring reminder schedules after reboot). No camera, microphone, contacts, storage, or location permissions are requested.
- **Component Exporting**: `MainActivity` is marked `android:exported="true"` solely to allow the system launcher to open the app. All other internal components and workers are unexported and unexposed.
- **PendingIntent Security**: All `PendingIntent` instances specify `FLAG_IMMUTABLE` to prevent intent spoofing and hijacking on modern Android versions (API 31+).
