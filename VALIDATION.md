# Tasks 0.12.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41432323349), source commit `11ff29a08600d256f52e2426aa321a2e5eaf4645`.

73 Android tests, 255 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Checklist checks cover adding steps in Edit task, repopulating original and grouped checklists, reordering without transferring completion, renaming, removal, completion dates, recurring future occurrences, dated history, clearing completed checklists and removing active steps. Browser notification checks verify that Notify is disabled without a time, never enabled automatically by setting a time, persists when chosen, clears with the time, and is independent of daily reminder settings.

Native reminder tests cover opt-in plus time requirements, 12/24-hour input, completed-task suppression, moved dates with original completion keys, repeated tasks, monthly clamping, weekly phase, stable checklist keys, simultaneous tasks, canonical overrides, separate daily enablement, and boot rescheduling without state changes.

The app requests Android notification permission on Android 13+. Alarms use setAndAllowWhileIdle without exact-alarm access; Android power-saving policies can delay them and force-stop blocks them until reopening. Notification delivery, permission dialogs and reboot behavior were tested with Robolectric; no physical phone or Android emulator was used. The app uses its existing America/New_York task calendar for reminder times.

Saves check commit results, preserve unknown extension fields, and reject unreadable stored state rather than replacing it with an empty list. These protections and tests are not a 100% reliability guarantee: local data can still be lost through uninstalling, clearing app data, device loss, storage failure or software defects. Android system backup availability depends on the phone's backup settings.

[Download Tasks-v0.12.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.12.0.apk).

Package `com.personal.tasklist.preview`, version 0.12.0, code 12; minimum API 26, target 35; size 136,602 bytes.

SHA-256: `8022d6976ed675fe554677a0df2c38b7e229d4bc983d3fb0ca2d46bf56e53c48`.

Downloaded archive hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.
