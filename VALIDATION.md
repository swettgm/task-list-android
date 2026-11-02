# Tasks 0.27.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41504601696), source commit `88eda5fdeb527218806b36d155c3ddf4a3745412`.

95 Android tests, the full Chromium browser suite and calendar recurrence checks passed; lint reported zero errors.

One spaced separator dot appears between the schedule and label sections in Today and Upcoming. No separator is inserted between individual label emojis. The context dropdown uses an inset chevron with a 16px right gap; the native select remains accessible and clickable.

Tap a task’s Today badge in the widget to start progress. Its badge becomes In progress and the row moves above the priority sections. Tap that badge again to stop and restore its priority/time/title order. There are no play/stop buttons or title prefixes. Only widget presentation moves; saved task priority and app ordering are unchanged. A new row layout resource refreshes existing collection views. The 48dp badge touch area, title size, completion control, Today-only filtering and scrollbar-free scrolling remain intact.

Native checks verify shared app progress, repeated/stale taps and completed-task guards, active-row pinning, stable row identity, restored ordering and unchanged task priority. Canonical state, completion history and projects remain preserved. Browser checks cover the packaged task screens, labels, recurrence, completion undo, Projects, inline editors, narrow layouts, gestures and theme controls.

[Download Tasks-v0.27.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.27.0.apk).

Package `com.personal.tasklist.preview`, version 0.27.0, code 27; minimum API 26, target 35; size 195,211 bytes.

SHA-256: `249f1502ab4762cbaca413594f56cfbaae24495aaa51ffcf893ff1b3e70fa9cc`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Android policies can delay background updates and notifications.
