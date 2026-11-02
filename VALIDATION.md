# Tasks 0.19.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41456829763), source commit `11b99bcbfd619fc6a4023ffd0a685a2dc5a5130c`.

86 Android tests, 322 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Today and Upcoming sort each priority section by scheduled time, then alphabetically when times match. Untimed tasks follow alphabetically. The widget follows the same ordering within its priority headings. Sorting affects presentation only and retains task identities, schedules, completion history and priority groups. Checks cover midnight, noon, AM/PM and 24-hour inputs, alphabetical time ties and untimed rows on both tabs and the native widget.

Priority arrow buttons retain their position before the gear, rectangular shape and existing button dimensions. The arrow glyph now uses the same font size and weight as Do tomorrow, with its box centered on both axes. Browser checks compare computed font sizes and weights, verify both center coordinates within half a pixel, and check narrow layouts. Existing daily-priority persistence tests remain passing.

[Download Tasks-v0.19.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.19.0.apk).

Package `com.personal.tasklist.preview`, version 0.19.0, code 19; minimum API 26, target 35; size 173,949 bytes.

SHA-256: `7cddad6dbc81fa6ac881b720d9d82875bc995f98c4edf6e5a5efa8074b632ab8`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Android policies can delay background updates and notifications. These tests do not guarantee against every software, storage or device failure.
