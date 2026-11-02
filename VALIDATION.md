# Tasks 0.13.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41435809952), source commit `362c5939d41b6b9545c26bf0d30f52cb21500e10`.

75 Android tests, 271 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

The label emoji picker offers 24 common choices plus keyboard input. Checks verify persistence, task-editor and task-row icons, context-filter icons, unchanged label identities, reloads, updating existing labels, multi-codepoint emoji and skin tones, invalid input, matching emoji font sizing, and narrow-phone layout. Native checks verify that saved unused labels reach pickers, selected emojis reach native controls and widgets, and an icon fallback never overwrites unreadable task data. Existing checklist editing, notification, recurrence, gradient, mobile row alignment, swipe and persistence checks pass.

The custom image requested for Guild is not included: only its chat preview was available, and the original GIF/first-frame file is still needed for exact extraction.

[Download Tasks-v0.13.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.13.0.apk).

Package `com.personal.tasklist.preview`, version 0.13.0, code 13; minimum API 26, target 35; size 140,698 bytes.

SHA-256: `c7c588253a78ac27d4244eaeada0679a8534707a00d252d0bf9a7430bdd709e7`.

Archive hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the previous certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android emulator was used. Notification delivery can be delayed by Android idle policies; force-stop blocks alarms until reopening. Local data remains subject to uninstalling, clearing app data, device loss, storage failure and software defects; these checks are not a 100% reliability guarantee.
