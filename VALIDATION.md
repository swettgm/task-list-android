# Tasks 0.9.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41384179956), source commit `2d7c2aa44935663ad134074a772dfbd355c8191c`.

59 Android tests,197 browser checks, and26 calendar recurrence tests passed; lint reported zero errors. New layout checks compare Today and Upcoming at320,390,600 CSS pixels and15,20,25px title text. They verify checkbox centers against actual title bounds, identical row dimensions/fonts/spacing, and no date-heading dividers. Identical task fixtures were also visually inspected in both screens. Existing undo, history, recurrence, color, gradient, widget, and persistence checks continue to pass.

[Download Tasks-v0.9.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.9.0.apk).

Package `com.personal.tasklist.preview`, version0.9.0, code9; minimum API26, target35; size120,140 bytes.

SHA-256: `388f23aeda5b17da60f66578d5ad45025c55506263bf93d2808d67b70ed72fb0`.

The downloaded artifact hashes, ZIP CRCs and CI APK checksum passed. Packaged UI assets match source. The published APK retains the previous signing certificate and passed signature, ZIP alignment, and package/version checks. All code/resources/task entries match the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android emulator was used; actual phone font, keyboard, and launcher behavior remain device checks. This is a debug build for personal use.
