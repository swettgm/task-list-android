# Tasks 0.7.0 validation

[Successful Android build](https://github.com/swettgm/task-list-android/actions/runs/41372469248), source commit `138bc96d7079ac81c28b9ad31f3274dfa858f03c`.

- 59 Android tests passed, with zero errors/failures; lint passed with zero errors.
- 175 browser checks passed:94 general workflows,23 touch-undo/history checks,36 updated task/schedule/Upcoming checks, and22 picker/gradient checks.
- 26 calendar recurrence cases passed, including leap years, month-end clamping, weekday and week intervals.
- Screens checked from320 to844 CSS pixels for horizontal overflow. Screenshots were inspected for emoji contexts, title/checkbox alignment, Upcoming dropdowns, and the slate gradient.
- Original102 task definitions and304 saved records remain included. Existing installed state is preserved.
- Repeating/checklist app definitions no longer block native widget quick-add or generate phantom widget rows.
- No Windows companion, pairing screen, or internet permission is included.

## APK

[Download Tasks-v0.7.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.7.0.apk)

Package `com.personal.tasklist.preview`, version code7, version0.7.0. Android minimumAPI26, target35. Size120,140 bytes.

SHA-256: `91c0df3a362a247a9275331c965c4cb742c4f6909bab0d82d6ec1edd73a263a8`.

Signer certificate SHA-256: `c608cec387ed404701648b82b426777de0c2bc038d4fe44d3dd0237f3d0171c2`.

Artifact archive hashes/CRCs and the CI APK checksum were independently verified. Packaged task/bridge/theme/schedule bytes match source. The downloadable APK was signed with the retained preview key; its certificate matches the previous installed version. Signature, ZIP alignment, and package/version checks passed. Compiled code/resources and task assets match the checked CI APK.

## Device checks remaining

No physical phone or Android emulator was available. Browser and framework tests do not establish actual launcher or keyboard behavior; check the update, system font sizing, widget, and touch handling on a phone. This is a debug APK for personal use.
