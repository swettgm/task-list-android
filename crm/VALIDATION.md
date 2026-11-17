# CRM 1.0.0 verification

Source: `9cf4a1ee62b931ed01f542c04e8330ce8ada8d81`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44095884711). **17 native Android tests, 22 browser checks and zero lint errors passed.**

A separate Android app with People and Dating pages, selected phone-contact linking, contact details refreshed on launch/resume, notes, per-person daily/weekly/monthly reminders, and Contacted today to reset the schedule. Dating stages are Interested, Dating and Paused. Import is explicit, skips existing links and enables reminders only when the user configures them. Removing a CRM entry never deletes a phone contact. Linked identity fields are read-only until unlinked.

Checks cover calendar month boundaries and daylight saving time, earliest-reminder scheduling, notifications advancing once, reset on contact, cancelled schedules, denied notification permission, corrupt-storage preservation, stale snapshot rejection, app-private storage, WebView network isolation, cross-page editing and persistence, contact selection, failed writes and editor layout at 320/390px. The boot/package/time-change receiver reschedules saved reminders. Alarms are battery-aware and Android may delay delivery. Native checks use Robolectric and browser checks use Chromium; real contact-provider synchronization and notification delivery on a physical phone remain device checks.

APK: `CRM-v1.0.0.apk`, 37535 bytes. SHA-256 `2e80ac94de7728319c58ef3d6bb302f5e3188d8c6f2e7d8be9ce97139587c446`. Package `com.personal.crm`, version code 1, min SDK 26, target SDK 35. Artifact hashes, ZIP integrity, manifest metadata, alignment, APK signing and exact non-signature content equality to CI passed. Retained local signing certificate SHA-256 `9ce9de5ae8d5cf8315b32d2c8e65cfe7f45e99c4562066af6a2bd2acb92f0310`. No signing material or actual contact records are published.

