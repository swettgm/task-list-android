# Tasks 0.10.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41386126759), source commit `68c807d7529ffabb322fc2b8b8686f1a3164b310`.

59 Android tests, 217 browser checks, and 26 calendar recurrence tests passed; lint reported zero errors. Checks cover native time/date inputs, Tomorrow and Weekend shortcuts, Daily and Weekends recurrence, wider duration fields, expanded stacked notes/checklist, contexts saved through Settings, backdrop dismissal, completion undo and history, color-wheel pointer/keyboard input, and a shared gradient across Today, Upcoming, task editors and Settings. Existing row alignment, recurrence, linking, persistence and widget checks pass.

[Download Tasks-v0.10.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.10.0.apk).

Package `com.personal.tasklist.preview`, version 0.10.0, code 10; minimum API 26, target 35; size 124,236 bytes.

SHA-256: `4bf50d7c9344eace47eba1bf211b08e39829b9294cce4f95100a74305abb424e`.

Downloaded artifact hashes, ZIP CRCs and CI APK checksum passed. Packaged UI assets match source. The published APK retains the prior signing certificate and passed signature, ZIP alignment, and package/version checks. All non-signature APK entries match the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android emulator was used; phone time-picker, keyboard and launcher behavior remain device checks. This is a debug build for personal use.
