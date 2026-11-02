# Tasks 0.30.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41512812383), source commit `fe484e34dce3977a51eae466b64e53ba9d6ae020`.

101 Android tests, 452 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

A reminder already armed for 2 PM could be cancelled by an unrelated save after 2 PM when Android had delayed delivery. Rescheduling now preserves the earliest still-valid undelivered alarm for up to 24 hours and requests prompt catch-up delivery. Due records are acknowledged after delivery so they do not repeat. Notify off, changed time/date/recurrence, completed tasks and expired reminders are excluded. Canonical task identities and completion history remain unchanged.

On Android 12 and newer Settings offers Allow precise reminders when permission is needed. The app uses exact alarms when permitted and safely falls back to inexact alarms if unavailable or revoked. It replans on permission-grant broadcasts and app resume. Notifications permission remains separate. Settings also detects a blocked Scheduled task reminders channel, even if general notifications are enabled.

Native regressions reproduce a delayed alarm followed by an unrelated state save, verify delivery and acknowledgement, check an existing Use hair serum override at 2 PM, reject cancelled/edited/completed/expired reminders, and exercise precise-alarm permission and the Android settings intent. Existing notification, recurrence, widget, progress, completion and layout checks remain enabled.

[Download Tasks-v0.30.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.30.0.apk).

Package `com.personal.tasklist.preview`, version 0.30.0, code 30; minimum API 26, target 35; size 203,741 bytes.

SHA-256: `06eba1278f270c352b3c7182a7cffb7c5bc115e4c977a2067915f893298a8e45`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone was used. Checks establish the scheduling regression and its fix, not the cause of a specific missed notification on the user’s device. Android permission settings, channel settings, Do Not Disturb, force-stop and device policies can affect delivery; exact alarms require user permission on Android 12 and newer.
