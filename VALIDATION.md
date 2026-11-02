# Tasks 0.14.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41439120875), source commit `597bfe5f95dc6a6444029e34d2efaa884da5544e`.

77 Android tests, 286 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

The Guild image is extracted from frame zero of the user's TTF-Jester-Original-Speed-Smooth-1024-v8.gif from Google Drive, with the pure-green corner background made transparent and the result resampled to a static 64px PNG. No animation runs in the app. Web and native controls use identical PNG bytes.

Checks cover Guild migration while retaining previous task edits, other labels and other emoji choices; loaded/static image identity; task-editor and row icons; matching label-box sizes; the full-width image filter; reloads; reusing the image for another label; narrow phone layouts; native ImageSpan rendering; and widget resources. Existing notification, checklist editing, recurrence, mobile checkbox alignment, swipe, gradient and persistence checks pass. Final task-row screenshots were inspected after fixing the image-filter width.

[Download Tasks-v0.14.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.14.0.apk).

Package `com.personal.tasklist.preview`, version 0.14.0, code 14; minimum API 26, target 35; size 161,261 bytes.

SHA-256: `fee7f2a638fe5570e8706e9f46d62eb819a891b48bd5ce17bf11e3d9a87bb70e`.

Archive hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the previous certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android emulator was used. Android policies can delay notification delivery; force-stop blocks alarms until reopening. Local data remains subject to device loss, uninstalling, clearing app data, storage failures and software defects; tests are not a 100% reliability guarantee.
