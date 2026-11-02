# Tasks0.8.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41382877326), source commit `6064ded1eb0c62a6151f919fb4e3745270828d9a`.

59 Android tests,177 browser checks, and26 calendar recurrence checks passed. Android lint reported zero errors. The browser checks include title-only checkbox labels, geometric alignment, no Upcoming section frames, touch undo, history highlighting, task persistence, recurrence, links, color pickers and gradients. Layouts from320–844 CSS pixels passed overflow checks. Today and Upcoming screenshots were visually inspected.

The APK’s packaged assets match checked source; artifact ZIP hashes, CRCs, and APK checksum passed. Re-signing retained the existing preview certificate and every compiled code/resource/task entry. Signature and ZIP alignment verified. It updates the installed app in place.

Package `com.personal.tasklist.preview`; version0.8.0, code8; minimum API26, target35.

SHA-256: `a861a4a7898ed5eb05af030e7e3f4ba3fc524c8d523745013987cf5a5641b498`.

[Download Tasks-v0.8.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.8.0.apk).

No physical Android device or emulator was used. Actual phone font settings, keyboard, and launcher behavior remain device checks. This is a personal-use debug build.
