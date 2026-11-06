# Android Tasks 0.66.0 verification

Source: `82240b6d0aa0b6182cffa1026ed49f6b9bda3441`. [Build, tests and browser checks](https://github.com/swettgm/task-list-android/actions/runs/42150732017). **185 Android tests, 835 browser checks, 33 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.66.0.apk`, 307042 bytes; SHA-256 `4f6dc4bc05ebf41e57d7338c9ad02a3084b8213932cb1548ed177fa81042644c`. The signature matches the retained preview certificate and every non-signature ZIP entry matches the tested CI APK.

The in-progress notification completes one active task directly, or opens a native chooser for multiple tasks. Tests on Android API 28 and 34 cover notification intents, cancellation, stale session actions, completion history, widget updates, checklist parents, recurring occurrence keys, fallback snapshots and preserving unrelated active tasks.

Calendar checks cover Sunday-only Groceries, Saturday–Sunday Check oil and Wash sheets, and weekly repeats anchored on Sunday without explicit weekdays. Upcoming uses actual occurrence dates and keeps hidden tasks visible. Browser checks open and save future editors, change Sunday to Monday while preserving task details, and save unchanged weekly rules without resetting them. Friday-evening availability for explicit weekend groups in Today remains separate from Upcoming date grouping.

Remembered Today/Upcoming tabs persist on the device across activity recreation and page reloads, without adding the view to synced task data. Hidden Today rows have a white outline only while manually revealed. Both rest messages keep their vertical placement and have horizontal centering checks across phone and widget widths. Widget RemoteViews use a fresh layout ID to avoid reusing stale launcher layout state.

The release APK is checked for retained signing identity, alignment, package/version, archive integrity and exact packaged assets. No physical phone was used; native checks use Robolectric and browser checks use Chromium in GitHub Actions.

# Windows taskbar fix 0.65.2

The native window and installed shortcuts passed identity and relaunch checks. See [taskbar verification](docs/WINDOWS-TASKBAR-VALIDATION.md).

# Windows installer 0.65.1

The Windows installer passed installation, live upgrade and uninstall checks. See [installer verification](docs/WINDOWS-INSTALLER-VALIDATION.md). The Android and task-sync checks below remain applicable.

# Tasks 0.65.0 and Windows companion verification

Android source: 4b3c40a2c47ea2a6b630f0352267b2549b5141fe. [Android build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/42085358547): 167 Android tests, 811 browser checks, 33 calendar/timing checks and zero lint errors.

Windows source: 9256eb8c9d8acbee9f9f188486ba916b4e9a243d. [Windows build and executable checks](https://github.com/swettgm/task-list-android/actions/runs/42085005875): 18 Python persistence/protocol tests, 19 real-browser companion checks, and a smoke test running the actual packaged Windows EXE.

The Windows screen reuses the packaged Android task UI and schedule engine. It shows Today and Upcoming, checkboxes and Do now/in-progress controls, with task editing, add buttons, settings, priority changes, rescheduling and deletion unavailable. Linked groups start and stop together. Main-list checkbox sizing and native widget layouts are unchanged.

The first PC launch waits for pairing rather than displaying historical seed data. A QR code or pairing link opens a confirmation in Android. Confirmed local-Wi-Fi sync transfers the current phone state, including colors, gradient, metadata icons, completion history and active tasks. Theme updates apply to an already-open Windows window. The EXE opens a compact, resizable Edge app window, falling back to the system browser.

Phone sync runs while the Android task app is open. Changes save locally while disconnected and merge on reconnection. Tests cover simultaneous unrelated changes, custom task merging by stable ID, linked-progress stops preserving unrelated tasks, stale compare-and-write rejection, failed saves retaining data, malformed remote data, authentication, local-only UI credential pages and pairing cancellation. PC and phone context filters stay independent.

Desktop completions, reopening and progress refresh the phone's native widget state before a deferred WebView redraw. A native regression check asserts this without running the page callback. Ongoing task notifications update through the existing reminder planner. Android WebView networking remains blocked; the native sync client accepts explicitly validated local network endpoints and uses a pairing token.

Android APK: `Tasks-v0.65.0.apk`, 302855 bytes; SHA-256 `bceb6d0dcf34fce6f5831313abd22fe6bf5cc7e7654efc46e9989e7991d93165`. The actual signature, alignment, retained signer, ZIP CRCs, packaged assets and non-signature contents match the verified CI build. The certificate matches Tasks 0.64.0.

Windows executable: `Tasks-PC.exe`, 9720841 bytes; SHA-256 `49866c17a28241a7c3bca7aff6a5b5f91e33d5c60d1aaeb9c27fe60f0b6caefe`. The artifact digest, ZIP CRCs, executable checksum and AMD64 Windows GUI PE headers were verified. The packaged EXE itself passed startup, authenticated HTTP sync, saved-state, QR generation and clean-shutdown checks on Windows. It has no commercial Authenticode publisher signature.

The public download bytes were compared to the verified local artifacts. Settings Changelog and the desktop instructions include pairing, theme transfer and the foreground/same-Wi-Fi sync limitation.

No physical phone or personal Windows PC was used. Android checks use Robolectric; Windows checks run on GitHub's Windows runner with a real browser and the packaged executable.
