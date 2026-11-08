# Android Tasks 0.77.0 verification

Source: `1c8026fceb36cfd7d909ad253e4ab55611580c1b`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42519398165). **193 Android tests, 887 browser checks, 37 calendar checks and zero lint errors passed.**

Settings → Task ordering now offers Hold and drag as an alternative to priority arrows. Long press picks up a solid preview using the task's actual styling, with a white insertion line marking the landing position. Empty priority sections remain available while there are tasks to drag. Moving before the hold keeps native scrolling; active drags scroll at the screen edges. Pickup and successful drop provide haptic feedback. Order and priority save together using the existing task definition/order model; task dates, note content and completion history are preserved. Urgent tasks remain first and lowering an urgent task clears Urgent, matching arrows. Completed/trash items and interactive row controls cannot initiate a drag. The widget front is unchanged.

Browser regressions verify real touch pickup and cross-priority dropping, within-priority insertion, opaque correctly styled preview, insertion target, no unintended editor/completion/page swipe, Escape cancellation, scroll-before-hold cancellation, rejected-write recovery, persistence and restoring arrows. Screenshots were reviewed. Existing arrow, calendar, overdue, editor, widget and other browser suites remain passing.

The Weekend date shortcut now picks the next Friday strictly after today, including Friday (+7 days), Saturday and Sunday. JS and native implementations are covered across weekdays and year boundaries, with browser checks of the detailed form and native widget Quick add coverage. Recurring weekday selections are unchanged.

APK: `Tasks-v0.77.0.apk`, 327613 bytes. SHA-256 `9ce0315873af543dafa01898570ca11150804004a4f399a5d71c8021fa75b94a`. Package/version, retained update signer, alignment, ZIP integrity and exact packaged assets were verified. Every non-signature entry matches the tested CI artifact. Tests use Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.76.0 verification

Source: `31c3fb0229eb61834a06174742f36f10684f1327`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42385687191). **193 Android tests, 867 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.76.0.apk`, 323517 bytes; SHA-256 `6ea9a33248562aa9c23750c0cd3b17bccad43d7634b1d1fb4ba6be0c9e4ee6ef`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

Removed the Repeat starts caption and its display-only event listeners. Repeat scheduling and saved anchors remain unchanged. Hide until before due time now occupies a full-width grid row below both Time/Notify and Date/shortcut buttons, with its lead-time inputs using that same width when enabled. Notify stays under Time. The shared Edit and detailed Add forms use the same layout.

Existing geometry regressions verify that Hide until begins below both columns, reaches the right edge of the date column, and remains above Duration, with Notify under Time and no repeat caption. The full scheduling, gesture, editor, completion and native suites pass.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.75.0 verification

Source: `fe58b4f41011e80f8e9d7c67423910160f41e88e`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42384748918). **193 Android tests, 867 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.75.0.apk`, 323517 bytes; SHA-256 `d899a4e70ae0ee6ffdb69de6c1f12a2d206e6be78f5c1d03bff446501142cd68`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

The detailed task forms now place Notify above Hide until due time under Time. The same shared form is used by Edit task and detailed Add task. Existing geometry checks verify Time/Date columns, Contexts placement and the new checkbox order in both forms. Arrow fades are lengthened to 350 ms, with reduced-motion support retained. Existing gesture tests assert the animation duration and final placement, and native sync defers reloads until the fade completes. Field sizes are unchanged.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.74.0 verification

Source: `47dd1871dc58d72e20d4c33eb03ea6861042a70d`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42382991287). **193 Android tests, 867 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.74.0.apk`, 323517 bytes; SHA-256 `0ef5790a1743fd575aa7ceec5603e2aab3b6f6e104a3c0b555fe031bfed90a57`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

Today renders active occurrences as full task cards above the completion progress bar and normal priority sections. The former title-only progress banner and its styling are removed. Active cards retain their checkbox, metadata, notes, checklist, progress action, priority arrows and editor. They are removed from the normal priority rows, deduplicated by occurrence, respect the context filter and retain saved priority/manual ordering. Stopping a task returns it to its normal section without mutating that order.

Active future occurrences keep their due-date caption and Do today control. A task with an active checklist step can pin its parent card. During completion, the active card and its checked undo checkbox retain their position for the existing two-second pause. Completing and undoing one of multiple active tasks preserves the others.

Browser checks cover multiple full cards, absence of a separate banner, no duplicate normal rows, retained notes/checklists/editor controls, stopping and reload persistence, future cards, linked progress, real-touch completion undo and pointer holds. The full calendar, priority, layout, settings and editor suites also pass. The widget retains its plain Overdue label but suppresses the Overdue suffix in the green active pill; native widget tests verify both states. The native in-progress notification is unchanged. Arrow reordering uses the existing fade path before rebuilding, with a browser animation assertion. Web URLs in displayed notes are linked using text nodes and HTTP(S) anchors; literal HTML stays text. Only user-gesture main-frame HTTP(S) requests open an external browser, and all WebView navigation/network blocking stays in place. Tests cover browser intents, blocked schemes and background/frame requests, preserved note text, punctuation and task-click isolation. Detailed Edit and Add forms place Time to the left of Date, keep Contexts below Priority/Urgent, and put Hide until above Notify under Time. The helper sentence is removed and vertical gaps are reduced slightly without resizing controls.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.73.0 verification

Source: `8c28c2c77065c51a2268c43149cafaf57a9a8c74`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42344189633). **192 Android tests, 855 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.73.0.apk`, 319421 bytes; SHA-256 `d88a9678395e799eb3dcf4b68ff1bb56b922f3826e84a94f6532fba3729c5b5c`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

Removed the extra app Overdue badge and its styling. Carried tasks retain one line such as Overdue from November 4th, always using the original occurrence date, including explicitly moved tasks. A single down-click at a priority-section edge enters the top of the next lower priority. Double taps and holds still append to their destination priority. Task action and arrow/gear gaps increase by 2 px while button sizes stay unchanged. Carry-over, scheduling, completion keys and widget presentation are unchanged. The existing calendar browser regression checks the exact date text and absence of a separate badge, along with recurring completion and future-date preservation.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.72.0 verification

Source: `26f1006da171ae50278c3673bcdcc7895753aa99`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42342694863). **192 Android tests, 855 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.72.0.apk`, 319421 bytes; SHA-256 `5b745f6fd27031bcf0c7ba76d07bd87e91c88d7974fcb7a73f439129abc074df`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

Every arrow transfer to a different priority appends the task at the bottom, including section-edge single taps, double taps and holds. Holding Up while already High moves to the top of its urgency group; holding Down while already Low moves to the bottom. Urgent tasks remain first. Priority and destination order save together; creation time remains the default for unmoved tasks. Browser tests include real touch gestures, haptics, hold-release suppression, cancelled drags, rejected writes and retries, reload persistence and Upcoming/Today agreement.

Unfinished occurrences carry into following days in the normal priority lists, with an Overdue tag and original due date. Their original completion keys remain unchanged. Recurring carry-over starts at the task's creation/schedule boundary, skips deleted, completed and superseded occurrences, respects explicit moves, and leaves future dates intact. Multiple pending occurrences of one task share task order without trapping single-tap reordering. The editor shows the original chosen due date for an overdue occurrence.

Browser checks exercise one-time and recurring overdue rows, visible tags, completion of only the original occurrence, future recurrence preservation, alternate-weekend schedules, explicit moves, automatic midnight refresh and rescheduling. Native checks confirm that widget snapshots carry unfinished past rows, reject future and invalid weekday rows, show an Overdue label, complete only the selected occurrence, and remain carried over several days later. New-task test fixtures record creation timestamps so layout-only repeat anchors do not invent pre-creation history.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.71.0 verification

Source: `c009b1dc5f26c3f704ddd926725322904a062672`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42320716295). **191 Android tests, 870 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.71.0.apk`, 319421 bytes; SHA-256 `5acaaab5e906aee1e87d8a1bb1c35c28f9426991f72fb5957f64a9aa7eaa6d03`. The retained preview certificate matches the previous release, and every non-signature APK entry matches the tested CI build.

A single arrow tap swaps with the visible neighbor inside a priority section. At its first or last row, it moves into the adjacent priority at the nearest end: up enters the bottom of the higher priority, down enters the top of the lower priority. Priority and destination order save in one state write. The order is shared across Today, Upcoming and widget sorting. The same creation-time default and Urgent-first rule remain in effect.

Browser checks cover both boundary directions, insertion despite conflicting creation timestamps, subsequent ordinary reordering, High/Low limits, reload persistence, Upcoming and Today agreement, exactly one light haptic, and an internal Urgent boundary that must not demote a task. A rejected state write leaves both priority and ordering unchanged and a retry succeeds. Notes, checklist and creation time remain intact. Existing real-touch single/double/hold and cancellation checks also passed.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Checks use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.70.0 verification

Source: `6061026c1cadeae5c7ece288a1b3f39f58601115`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42317921720). **191 Android tests, 855 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.70.0.apk`, 315325 bytes; SHA-256 `d4fbe311b5e9b692a2d2d5b70905ba2cf035849a0f9de1303c371be1325b9e33`. The certificate matches the retained preview signer and every non-signature ZIP entry matches the tested CI APK.

Default task order is actual creation time, oldest first, within priority and urgency groups. Unknown creation times use original definition order. A single arrow tap swaps the task with its visible neighbor and saves a global per-priority ID order. Double taps cancel the queued single action and change priority one level. Holding jumps to High or Low. Native light/long haptics accompany successful actions. Movement, cancellation and hold-release suppression are checked with mouse and real touch input. Shift+Enter/Space provides a keyboard priority action.

Manual order is shared by Today, Upcoming and future recurring occurrences, and carried to native widget sorting through saved task IDs and explicit creation metadata. The widget never treats an occurrence date as a task’s creation time. Urgent tasks stay first, and manual moves cannot cross the urgent boundary. Completed history retains its completion-time ordering. Tests preserve checklist history, task details, creation timestamps, links and identities.

The refresh icon uses symmetric rounded arrows centered in its existing control. Interval editors show the repeat anchor separately from the occurrence date, without changing saved schedules. The user corrected Check oil’s start date; this release retains the strict scheduling rules and does not rewrite recurrence anchors.

The release APK is verified against the CI artifact, retained signer, package/version, alignment, ZIP integrity and exact packaged assets. Checks use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.69.0 verification

Source: `eb14715f29ca3628fba765bf3029cfc5ffb473a7`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42313719512). **189 Android tests, 865 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.69.0.apk`, 311229 bytes; SHA-256 `db15b295a8c49b046ef2a0db753668d73503247e539be3fa2fde927564f074a5`. The certificate matches the retained preview signer and all non-signature contents match the tested CI APK.

The app and widget enforce scheduled dates. Friday weekend previews and automatic unfinished-task carry-over are removed. Recurring weekdays override stale one-time due fields and obsolete move entries; one-time tasks retain explicit move dates. Upcoming no longer hides future repeats because an earlier occurrence is pending. Definitions, completion keys, completion history and old unfinished records remain saved.

Native checks cover four weeks of Sunday-only, Sunday–Thursday and alternate-weekend schedules. Widget checks reject old Friday-preview/onToday/plannedDate flags, reject past carried rows, restore Saturday/Sunday occurrences despite legacy Friday moves, and retain explicitly moved one-time tasks. Reminder planning and reveal times use the same chosen-date rule.

Browser checks exercise Friday afternoon and evening, Saturday/Sunday, the skipped alternate weekend, the next active weekend, old unfinished tasks, stale due fields and legacy move records. They recreate Waterproof shoes, edit it with Tomorrow, verify its removal from Today and immediate widget-snapshot update, then check automatic midnight refresh without hidden tasks. The toolbar refresh retains the selected tab and filter, refreshes native widgets and reminder plans, and requests companion sync. UI checks use an advancing Sunday clock for weekday-dependent gesture fixtures.

The obsolete Friday-opening setting is removed and its alarm cancelled. A native midnight refresh is always armed, and the app refreshes on midnight and focus. The Weekend date shortcut remains an explicit Friday date selection.

Release verification compares every non-signature APK entry against the tested CI artifact, validates the retained signer, alignment, package/version, archive integrity and packaged assets. Tests use Robolectric/native graphics and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.68.0 verification

Source: `00287e7fb7559edd0f46d7c64ffeeeac2913d979`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42201054068). **187 Android tests, 855 browser checks, 33 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.68.0.apk`, 311229 bytes; SHA-256 `9c36a9bed48da431139c20cb91959bb32aedf81ed62d5a0362589186d2a211f9`. The certificate matches the retained preview signer and all non-signature contents match the tested CI APK.

Creation timestamps are stored on task definitions when new tasks are saved from the app, widget Quick add or project conversion. Detailed edits and rescheduling retain the original timestamp. Earlier app task IDs encode their creation instant; the editor recovers that timestamp and makes it durable on the next save. Tasks without recorded creation history display Created date unavailable. Due dates, occurrence dates and widget snapshot timestamps are not used as creation history.

The creation date appears only beside Edit task in the full task editor, including the widget-launched editor. Browser checks cover New York date boundaries, numeric timestamps, legacy IDs, unavailable dates, editing and rescheduling, reopening, quick and detailed creation, and corner placement at 320, 390 and 650 px widths. Native checks verify widget Quick add records its creation instant independently of its future due date.

The hidden-task border extends horizontally by 6 px on each side, with no content movement or vertical expansion. It remains visible beyond the resting page clip while swipe animations retain clipping, and it ignores pointer events. Browser checks verify checkbox clearance, viewport bounds and row spacing. Detailed task forms place Hide until before due time after Date/Time and their shortcuts, before Duration.

Release checks compare all non-signature APK entries with the tested CI artifact, verify the retained signer, package/version, ZIP integrity and packaged assets. Native checks use Robolectric and browser checks use Chromium in GitHub Actions. No physical phone was used.

# Android Tasks 0.67.0 verification

Source: `4df05d54951c168fac1a1769e598805d472508c0`. [Android build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42152352238). **186 Android tests, 835 browser checks, 33 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.67.0.apk`, 311229 bytes; SHA-256 `016d6bc245daded44754b3f85dd29a2547a96332f4dec0080707840e56e37c81`. The certificate matches the retained preview signer and all non-signature contents match the tested CI APK.

The widget renders the empty message with the device’s font and emoji, centers the visible glyph bounds on each wrapped line, and exposes the full sentence to accessibility services. It uses the launcher-provided width and redraws after resizing. Native graphics checks inspect actual text/emoji pixels for both rest messages at 140, 250, 360 and 420 dp widths. RemoteViews checks verify centered placement at several widget widths and heights. The app’s rest messages use left alignment, verified in Chromium at 360, 390 and 650 px widths.

The widget requests a five-column, two-row default where supported, with a 144 dp minimum height. Its content fills the size allocated by the launcher. Android launchers control the widget’s grid position and outer size; existing widget allocations are preserved, so users resize those through launcher handles. No task data or widget preferences are reset.

The signed APK is compared with the tested CI artifact, including every non-signature archive entry. Its retained signing identity allows an in-place update. Tests use Robolectric/native graphics and Chromium in GitHub Actions; no physical Samsung launcher was available for verification.

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
