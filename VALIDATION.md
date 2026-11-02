# Tasks 0.28.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41507043236), source commit `36166bd60374cd1cb0ec4e3f42b834a46e5e6510`.

96 Android tests, the full Chromium browser suite and calendar recurrence checks passed; lint reported zero errors.

The widget’s active task uses the app’s green rounded pill: #176B42 background, white bold IN PROGRESS text and the green circle emoji. The compact badge is vertically centered inside a 48dp touch area. Tapping it still stops progress and restores Today with its plain background and normal widget priority/time/title position. Active tasks remain pinned at the top of the widget, sharing the same in-progress state with the app. A fresh row resource refreshes existing widget views.

Native checks verify the badge text, green fill, white text, rounded corners and minimum touch height, plus restoration to the plain Today label. Existing progress, canonical-state, completion-history, recurrence and widget-ordering checks remain enabled. Widget completion has a durable two-second confirmation window: the row remains checked and can be cancelled by tapping its checkbox again. Canonical completion is applied after the window; expired requests are recovered on widget refresh or app resume. Tokens keep stale taps from cancelling a later completion. Tests cover cancellation, stale undo, timed completion and refresh recovery.

Metadata dots use a shared bold style with exactly 6px of padding on each side. Labels retain no dots between individual emojis. The full packaged browser UI checks also passed.

[Download Tasks-v0.28.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.28.0.apk).

Package `com.personal.tasklist.preview`, version 0.28.0, code 28; minimum API 26, target 35; size 199,560 bytes.

SHA-256: `ca15b3da8ec8c62cc40e21191e73b7e0e0bf27a8b3d62df087c45d8eca42c186`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation.
