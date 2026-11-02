# Tasks 0.18.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41455650346), source commit `012122624cc21a971d79c5b43a4acb2df763aa0a`.

85 Android tests, 319 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Up/down priority controls use the same thick arrow glyph as Do tomorrow, match the other task buttons and stay together before the gear. Browser checks cover all priority levels, disabled limits, Today and Upcoming, persisted reload and next-day recurring tasks. Changes preserve notes, labels, links, checklist progress, schedule and completion history. Lowering an urgent task clears Urgent so future edits retain its new priority.

Buttons, inputs, dropdowns and native widget forms use rounded rectangular corners. The main plus is a rounded square. Widget Quick Add reads the saved app theme, including gradient colors and direction, clips the panel to rounded corners, and uses matching fields and buttons. Android checks verify saved theme colors, gradient direction, rounded panel clipping, field backgrounds and button colors without default Android tints overriding them. Browser checks also cover rounded control styles and narrow layouts.

[Download Tasks-v0.18.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.18.0.apk).

Package `com.personal.tasklist.preview`, version 0.18.0, code 18; minimum API 26, target 35; size 173,949 bytes.

SHA-256: `f03b32d643d70897013aac5f84c4b30a2d4f0352e5a0ce08f21f8bee2a0c94bd`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Android policies can delay background updates and notifications. These tests do not guarantee against every software, storage or device failure.
