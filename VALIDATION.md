# Tasks 0.15.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41441232629), source commit `46650b5b395874fddaee3a3a8a6686972614815a`.

77 Android tests, 288 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

The Guild crest is about 12% larger in the web interface (1.25em to 1.4em), with comparable increases in native pickers (18sp to 20sp) and widget summary (14dp to 16dp). Its original GIF first-frame PNG is unchanged. Shared label boxes, row spacing, checkbox alignment and filter width remain intact; the final screenshot was inspected.

A next-day recurrence test assigns an additional label and edits notes, duration, scheduled time and Notify, then verifies them on the following day's task. It also verifies that edited checklist steps carry forward with fresh per-day completion, while occurrences before the checklist edit retain their original steps. Labels, ordinary task fields and manual links belong to the task definition; completing or rescheduling an occurrence does not rewrite its recurrence definition.

[Download Tasks-v0.15.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.15.0.apk).

Package `com.personal.tasklist.preview`, version 0.15.0, code 15; minimum API 26, target 35; size 161,261 bytes.

SHA-256: `99026aaa50a8508d44acd4ff347d4a1afcd9e53a5365e2eae8e02014f6c2a4c4`.

Archive hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android emulator was used. Android policies can delay notifications; force-stop blocks alarms until reopening. Local data is still subject to device loss, uninstalling, clearing app data, storage failures and software defects; tests are not a 100% reliability guarantee.
