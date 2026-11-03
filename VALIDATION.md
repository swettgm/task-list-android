# Tasks 0.36.0 verification

Source commit: `8ca5b7cbb936de65f3fec6a099b002cb94194d03`. [Successful build and checks](https://github.com/swettgm/task-list-android/actions/runs/41574960805): 111 Android unit tests, 499 browser checks, 26 calendar checks, and zero lint errors.

The app exposes a small native actionHaptic bridge using CLOCK_TICK on the UI thread. Plus buttons, successful task creation and valid date/time change events request a light tick. Date shortcuts and Clear time actions also provide feedback. Projects plus/create/Make task actions use the same bridge. Existing priority long-press feedback stays separate.

Widget quick add gives one tick when a widget-add launch gains window focus, including reuse through a new intent. Ordinary focus returns do not repeat the entrance tick. Successful saves and keyboard Done use the same feedback path. Confirming native date/time pickers and changing Today/Tomorrow/Weekend gives feedback; cancellation and form resets do not. No vibration permission or ignore-setting flags are introduced.

Native checks verify light feedback without changing saved tasks, widget entrance and picker callbacks. Browser checks count plus feedback, successful creation, date/time confirmation and absence of saved-task feedback on an invalid add. The full task, touch priority, recurrence, widget, reminder and history suites remain enabled.

Tab gestures now listen across the document instead of only the task root. This includes blank viewport space below short lists. Dialogs still block tab swiping, and vertical gestures remain scrolling. Real touch checks start outside the task-root bounds and navigate both directions without switching on a vertical gesture.

The empty-widget message is a wrap-content overlay centered against the full widget bounds. Equal side margins, centered text alignment and disabled extra font padding avoid header and side-padding offsets. A fresh layout refreshes existing hosts. Native geometry checks cover three widget widths and three heights.

Signed APK: `Tasks-v0.36.0.apk`, version 0.36.0 / code 36, 216,198 bytes. SHA-256: `704b9015639a7ebc85dd30448985f786aabf3267f3b2328d997aa06ff7114bf9`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or vibrator was used. Browser checks run in Chromium; native controls use Robolectric. Physical feedback depends on device support and Android’s touch-feedback settings. Windows work remains deferred.
