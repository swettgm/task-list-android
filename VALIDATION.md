# Tasks 0.21.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41464410541), source commit `60fde4d5a8684839de73f86d3336d86b35dbf3ba`.

87 Android tests, 326 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Swiping left from Today slides Upcoming into view from the right while Today exits left. Swiping right reverses the direction. The transition lasts 240ms; header controls and viewport gradient remain stationary. Tab taps and app startup still switch immediately. Reduced-motion preferences skip animation while preserving swipe navigation.

The outgoing panel temporarily leaves normal flow, remains inaccessible during transition, and is hidden afterward. Animation transforms and inert flags are cleaned up on completion or interruption; task identities and stored data are unchanged. Browser checks verify both directional keyframes, absence of horizontal overflow during movement, clean final state with one interactive panel, vertical scrolling and reduced-motion navigation. Existing narrow-layout, completion, editor, priority, theme and widget checks remain passing.

[Download Tasks-v0.21.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.21.0.apk).

Package `com.personal.tasklist.preview`, version 0.21.0, code 21; minimum API 26, target 35; size 173,949 bytes.

SHA-256: `527be72c0cd7bd54dd941fa3e3ced1e42327ccca9eded9e71072921d4f5ae767`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Android policies can delay background updates and notifications. These tests do not guarantee against every software, storage or device failure.
