# Tasks 0.35.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41570381754), source commit `03f3d604c6c124bc3d7be9d4987b9f25a43d8005`.

108 Android tests, 489 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

A touch contact can generate pointer-leave after pointer-up and before its compatibility click. The old handler marked that release as cancelled, discarding ordinary taps. Priority buttons now track whether a contact is still pressed; pointer-leave cancels only an ongoing press. Real cancellation and moving away still cancel pending holds. Taps retain one-level changes, holds jump to endpoints with haptics, release clicks do not double-apply changes, and a fresh tap after holding remains usable.

Upcoming’s Do today factory now uses the same recurrence guard already used by Today’s Do tomorrow factory. Daily, weekday and general repeat definitions have no day-move controls. Existing saved task definitions, occurrences, completion history and earlier explicit moves are preserved. One-time task movement remains available.

Browser regressions use touchscreen taps and CDP touch start/end holds, in addition to mouse input. They verify tap up/down, hold up/down, tap immediately after hold, endpoint limits, recurrence controls on both tabs, and one-time movement. The full native, calendar and browser suites remain enabled.

[Download Tasks-v0.35.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.35.0.apk).

Package `com.personal.tasklist.preview`, version 0.35.0, code 35; minimum API 26, target 35; size 216,110 bytes.

SHA-256: `5035df2dfe53f44d7a7bc3d0f2a8358f788384a8b24e80d821358dc9ab5c6de3`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android launcher was used. Browser touch checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Windows work remains deferred.
