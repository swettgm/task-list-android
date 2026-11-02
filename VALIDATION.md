# Tasks 0.26.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41503359795), source commit `09e938c07b5d3fbc2d454f56faae23949d598aab`.

94 Android tests, 448 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Priority long-press stops at High or Low; it cannot put a task in Priority not set. Regular taps retain access to unset. Priority arrows use centered, rounded 32px squares; Do today has its arrow on the left. Projects use an outline trash icon that stays gray through the first confirmation and turns red during the second press. Editing disarms confirmation.

The widget has a smaller bare accent-colored plus, slightly larger 13.5sp task titles, and a Do now/Stop action that updates the same in-progress task in the app. Native tests cover idempotent repeated taps, stale stops, completed-task guards, child progress and stale snapshot reconciliation. Canonical state, completion history and projects are preserved. The launcher icon uses slate-gradient checklist shapes and rounded line ends; app and Projects add glyphs follow their background/gradient.

Both quick-add popups stay open after successful saves, clear the title and retain keyboard focus for another task. A white inset border pulses twice without resizing the popup. Tests cover two distinct consecutive saves, native IME submission, cancellation without saving a draft, retained completion history, and animation cleanup. Reduced-motion or disabled animation settings are respected.

Editable label emojis sit in the duration/repeat schedule row and wrap within the screen. App and widget scrollbars are hidden; scrolling stays enabled. The widget uses a new layout resource so existing widgets refresh on update. Browser checks cover narrow task layouts, reactive tab gestures, inline editors, Projects, recurring tasks, completion undo, notifications configuration and shared native state. Projects remain separate from task counts and widget rows. Existing task identities and completion history are unchanged.

[Download Tasks-v0.26.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.26.0.apk).

Package `com.personal.tasklist.preview`, version 0.26.0, code 26; minimum API 26, target 35; size 195,129 bytes.

SHA-256: `69f8e57c3623a248eeb52dbe2262f929b8648dfa569901dccb62583f0548cfba`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard, vibrator or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Device settings control physical haptics, and Android policies can delay background updates and notifications.
