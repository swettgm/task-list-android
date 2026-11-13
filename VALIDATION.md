# Habits 1.0.5 verification

Source: `3db9e576da17c6d6566d6cf663545165af736c4d`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43497236509). **33 native Android tests and zero lint errors passed.**

Habit names are always visible on the widget. Removed reveal preferences, tap-to-hide actions and auto-hide timers. Obsolete name-toggle intents from an older widget view are ignored and trigger a refresh. Restored the earlier 42dp day-heading row, 6dp top margin, 4dp vertical name padding and 10dp row bottom padding. Today remains a dot only; other days keep weekday abbreviations. The app layout is unchanged.

Future dates are read-only in both the widget and app. Future cells are disabled and have read-only accessible descriptions. The shared store rejects edits after the phone’s local today date, so stale pending intents and direct calls cannot bypass the restriction. Today/past marks remain editable. Existing future marks are preserved unchanged and become editable when their date arrives.

Removed obsolete hiding tests and added coverage for always-visible text after six seconds, ignored old hidden flags, restored spacing, past/today edits, disabled future app/widget cells and rejected stale future taps even when a future mark already exists. The complete persistence, rollover, widget-interaction and resizing suite passed. The native widget screenshot was reviewed. Robolectric SDK 28 was used; no physical launcher was tested.

APK: `Habits-v1.0.5.apk`, 37694 bytes. SHA-256 `93767d0c06d7ab82dacdb36f5251cababc07a2c7bff85ddf569b0b7d035fdfe5`. Package `com.personal.habits`, version 1.0.5 (6), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signer matches previous Habits releases.

# Habits 1.0.4 verification

Source: `5d4eba17b86a41247172095e51f7529e0ba9d624`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43492008137). **37 native Android tests and zero lint errors passed.**

Widget names no longer restore persisted reveal flags. Each reveal uses an in-process monotonic five-second deadline; tapping again hides immediately. Widget initialization and opening the app clear reveals, and process restart naturally starts hidden. A short asynchronous receiver callback refreshes the widgets after five seconds. Deadline tokens prevent an older callback from ending a later reveal. No extra permissions, foreground service or exact-alarm access is required. Daily marks remain independently persisted.

The widget heading is now a single 24dp row instead of 42dp. Removed the 6dp top margin and 8dp vertical name padding, and reduced row bottom padding from 10dp to 4dp. Habit rows sit about 32dp closer to the weekday row, with less space between habits. The invisible name tap area remains stable when revealing. App layout and typography are unchanged.

Four new native checks cover ignoring legacy saved visibility, resetting on app opening, hiding at five seconds while preserving daily marks, rapid hide/reveal timing and compact heading/row geometry. Existing persistence, widget tapping, rollover, font and width checks passed. The native widget preview was reviewed. Robolectric SDK 28 was used; no physical launcher was tested.

APK: `Habits-v1.0.4.apk`, 37694 bytes. SHA-256 `82ea1335663ad85b4369afb328731f99432ebe89ea7d455870c48e5c6d0230d7`. Package `com.personal.habits`, version 1.0.4 (5), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signer matches previous Habits releases.

# Habits 1.0.3 verification

Source: `1932a1208ce984f2b59c43adb3d31f157a089723`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43489431933). **33 native Android tests and zero lint errors passed.**

Today’s widget heading is only a solid dot, aligned with the other weekday abbreviations. Removed the habit-name outline and its unused drawable. Hidden names leave no border or placeholder; their transparent text preserves the invisible tap target and row dimensions. Tap to reveal/hide remains independent for each habit and retains its saved visibility state. In-app habit names and daily marks are unchanged.

Updated existing native checks assert today’s dot-only header and absent name backgrounds in both hidden/revealed states. The full persistence, rollover, mark/name-tap independence, font-size and resize checks passed. The native widget preview was reviewed. Robolectric SDK 28 was used; no physical launcher was tested.

APK: `Habits-v1.0.3.apk`, 37694 bytes. SHA-256 `c281ee59bb159aed50632c288665d7b2962eb8c29347d059bdbd5fcb66fa649d`. Package `com.personal.habits`, version 1.0.3 (4), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signer matches previous Habits releases.

# Habits 1.0.2 verification

Source: `70c0a35e37954349a8ebf54143eaac5a64729c91`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43486961270). **33 native Android tests and zero lint errors passed.**

Widget habit names start visually hidden inside 12dp rounded white outlines that fit their text plus 8dp horizontal and 4dp vertical padding. Tapping each outline reveals/hides only that habit’s name. Transparent text retains the measured dimensions, so revealing does not shift the row. The visibility preference is stored per stable habit ID, shared across widget instances and preserved through refresh/reopening, separately from daily marks. Hidden rows expose generic accessible descriptions. This is a visual concealment feature, not encrypted storage.

The app uses a separate, unchanged plain row layout: names remain visible, editable and their original size. Widget names retain the 2sp reduction from 1.0.1. The widget keeps weekday-only labels, today’s dot, no date numbers, no title and no plus/settings buttons.

Four additional native tests verify default hidden state, persistent/independent reveal preferences, deleted-row handling, exact fitted-box dimensions before/after reveal, no daily-mark mutation from widget name taps and unchanged in-app names. Existing persistence, date-window, widget-interaction and resizing checks passed. Native main-screen and static widget previews using the production RemoteViews row layouts were reviewed. Robolectric SDK 28 was used; no physical launcher was tested.

APK: `Habits-v1.0.2.apk`, 37774 bytes. SHA-256 `e28f2236fdfa0a458e065475ecfe9ec7004ef5684ecfa327d4238f9dd43e0543`. Package `com.personal.habits`, version 1.0.2 (3), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signer matches Habits 1.0.0 and existing data is retained on update.

# Habits 1.0.1 verification

Source: `9b0da89d8d5588cc9c6d412ff28f5ac0e72a8683`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43486189828). **29 native Android tests and zero lint errors passed.**

Removed the widget’s entire 48dp title/action bar and its pending intents. Seven weekday abbreviations remain, with no date numbers and a solid dot beneath today’s weekday. Today keeps its full accessible date description. The widget empty message points to the app instead of a removed + button. Habit names are 2sp smaller on the widget (14sp by default); their in-app size remains unchanged. The settings slider displays the actual widget size while preserving existing saved sizing preferences and habit history.

Updated native checks verify header structure, weekday/dot text, absence of visible date numbers, widget font size and unchanged in-app font size, alongside the existing persistence, rollover, widget interaction and resize checks. A static native preview built with production RemoteViews/row layouts was reviewed. Verification used Robolectric SDK 28; no physical launcher was tested.

APK: `Habits-v1.0.1.apk`, 37620 bytes. SHA-256 `877818cbd3d37a53fc2ffb721681a2876d5f2e701910b969a63329b9e62723b9`. Package `com.personal.habits`, version 1.0.1 (2), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signing certificate matches Habits 1.0.0, so this installs as an update retaining habits and settings.

# Habits 1.0.0 verification

Source: `9250ac45fa0f86fc1307c33edac6e1c2ed454571`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43479475173). **29 native Android tests and zero lint errors passed.**

A separate native Android app (`com.personal.habits`) with no runtime libraries or network permission. Its scrollable, resizable RemoteViews widget displays a seven-day calendar window from today minus two through today plus four. Each habit/date cycles empty circle → green check → red X → empty. Stable habit IDs and ISO calendar keys retain marks through renaming, reopening and window rollover. Deletion is confirmed. Stale row taps carry their rendered date, and each collection adapter’s snapshot date also anchors its headings. Invalid/deleted rows and unregistered widget taps cannot create marks.

Widget settings cover text/background/gradient colors, opacity, text size and tap vibration. Defaults are transparent with white text/icons. All instances share data and appearance. Midnight inexact alarms, periodic updates and date/time/time-zone/boot broadcasts refresh the window; Android may defer background alarms while asleep. No exact-alarm permission or persistent notification is required. Widget minimum height leaves room for a complete habit row. Existing apps and their APKs remain unchanged.

Tests cover date-window boundaries, leap years, 23/25-hour DST days, persistent checks/Xs, past/future independence, rename/delete/stale taps, corruption retention, appearance validation, background alpha, distinct artwork, real RemoteViews inflation, collection date anchoring, headers and columns at 250/320/480px, registered widget taps, main-screen marking and editor/settings flows. The native screen screenshot was reviewed. Robolectric SDK 28 was used; no physical phone/launcher was tested.

APK: `Habits-v1.0.0.apk`, 37620 bytes. SHA-256 `4211cd8cda23775b94ab4b8d62b869e1695a7991e28b100dca07f0360a9a58e0`. Package/version/minimum/target: `com.personal.habits`, 1.0.0 (1), API 26/35. Artifact digests, ZIP CRCs, signed APK non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. Retained Habits certificate SHA-256: `1caa94727f98bf0aafc631dca39a03902fa3600c47417028d266bcfc28c35e18`.

# Android Tasks 0.85.0 verification

Source: `5dd0d438dbcca48288c8732fe86a856bff542ad9`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43431898876). **193 Android tests, 912 browser checks, 37 calendar checks and zero lint errors passed.**

Task-row metadata now compensates for the invisible space inside each SVG viewBox. The visible clock, repeat, notification, linked-task and default context symbols align to their containing column; context badges start at the title/action column even when the metadata wraps. The correction is scoped to task metadata across Today, Upcoming and other task-list sections. Icon sizes, task-action dimensions, editor fields and native widget are preserved.

Added six browser geometry checks across Today/Upcoming at 320, 390 and 600px, verifying the visible edges of all five default context symbols and the shared title/action column on wrapped rows. Existing clock/action/checklist layout checks passed. The wrapped task screenshot was reviewed.

APK: `Tasks-v0.85.0.apk`, 335702 bytes. SHA-256 `393cd4d689c545800c866de4994fe5b49f1713a4e3b708585fffd07f9b365932`. Package `com.personal.tasklist.preview`, version 0.85.0 (85). Archive hashes, ZIP integrity, exact packaged assets, non-signature entry equality to CI, manifest, alignment and v2/v3 signatures passed. The retained certificate matches prior Tasks updates. Verification used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.84.0 verification

Source: `d68a9c8f4c901cb0410fdb191d16b58589e6117f`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43417762755). **193 Android tests, 906 browser checks, 37 calendar checks and zero lint errors passed.**

The inline High/Med/Low label is a keyboard-accessible button opening a focused Priority picker, reusing the component editor and existing High/Medium/Low choices. Save updates priority through the existing definition-save path and preserves other task details, history, links and progress. Medium/Low clear Urgent consistently with existing priority controls. Cancel and rejected writes preserve saved state. Today and Upcoming are supported without changing task dates. The inline appearance and layout remain unchanged; the Today screenshot was reviewed.

Added browser checks for keyboard activation and focused controls, Cancel, saving and label update, unrelated-field preservation, rejected writes, Urgent clearing, reopening and Upcoming edits. Existing layout, metadata, schedule and widget tests passed.

APK: `Tasks-v0.84.0.apk`, 335702 bytes. SHA-256 `e1e9da92909927e520dbec0770fc563e61e373fd030691f82f5d9f1ab748bc30`. Package `com.personal.tasklist.preview`, version 0.84.0 (84). ZIP integrity, exact packaged assets, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.83.0 verification

Source: `82d49f70a9143ffe0188774aff262ea670a2a036`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43410898785). **193 Android tests, 899 browser checks, 37 calendar checks and zero lint errors passed.**

Task-row action buttons have 1px less horizontal padding per side and are 2px taller, increasing from 32px to 34px. Matching controls, including arrows and gear, keep aligned heights; icon arrows retain their widths. Child task buttons receive the same height increase and a proportional padding reduction. Rounding, border appearance and fonts are unchanged. Detailed editor, native widget quick-add and widget-front styling are unchanged. Browser checks verify uniform rendered row-control heights and arrow/gear alignment at 320, 390 and 600px. The Today screenshot was reviewed.

APK: `Tasks-v0.83.0.apk`, 331606 bytes. SHA-256 `12ea09f02f72610d6dd5ccf78057741dd992e7917124ec5e8b24cb292d9bdc1d`. Package `com.personal.tasklist.preview`, version 0.83.0 (83). ZIP integrity, exact packaged assets, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.82.0 verification

Source: `d1ee984168551587e681e8ff52b70392b73d0c25`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43361986193). **193 Android tests, 899 browser checks, 37 calendar checks and zero lint errors passed.**

Main-screen controls retain the previous dimensions, padding and font sizes while keeping their rounded appearance and thick visual border. Additional border thickness is drawn as an inset stroke without consuming layout space. A browser comparison disables/re-enables the unified styling and checks the rendered widths, heights and font sizes of visible main controls against the pre-style geometry. Existing arrow/gear uniformity, header fit and row alignment checks passed.

The approved detailed editor and native widget quick-add dimensions and styling are retained. Repeat radio inputs remain semantic controls but their circles are hidden; the selected option has a white outline like Priority. The main-screen and detailed editor screenshots were reviewed. Date toggles, persistence and existing task workflows passed. Native popup and widget-front code are unchanged by this release.

APK: `Tasks-v0.82.0.apk`, 331606 bytes. SHA-256 `384b049cdb40c5aaede44c228d71f1044248d58e2dfce31cf4212fd784483afb`. Package `com.personal.tasklist.preview`, version 0.82.0 (82). ZIP integrity, exact packaged assets, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.81.0 verification

Source: `8752ce5f6dcb34531d02e34be5d97da67977a8bc`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43347416430). **193 Android tests, 899 browser checks, 37 calendar checks and zero lint errors passed.**

Outlined app buttons, editable fields and choice tiles use the label buttons' 12px corners and 2px borders. Native popup controls use matching corners and strokes. Small checkboxes retain proportional squircle corners, inline task details retain their unframed wrapping, and compact arrow/gear controls keep their 32px dimensions. The composite Today/Upcoming frame retains an even inset. The final Today and detailed editor screenshots were reviewed.

Tomorrow and Weekend are mutually exclusive selected toggles in detailed Add/Edit. Tapping the selected shortcut sets Today and explicitly clears the weekend flag. Manual date input updates shortcut selection. Checks cover toggling each shortcut on/off, selected states, manual dates, persistence without a stale weekend flag, strict scheduling and existing edit workflows. An initial run exposed row alignment changes from thicker borders; these were corrected. Screenshot review also caught unwanted frames on inline metadata, which were removed before the final full rerun.

APK: `Tasks-v0.81.0.apk`, 331606 bytes. SHA-256 `1174cdc0ec0d6afc7f6823e616376bc8cd95cc6bf339c5a4b5a84ca502171392`. Package `com.personal.tasklist.preview`, version 0.81.0 (81). ZIP integrity, exact packaged assets, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.80.0 verification

Source: `f02958474a1e2cf2600210c8d28877aec2cc86f4`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43317595410). **193 Android tests, 893 browser checks, 37 calendar checks and zero lint errors passed.**

Editor label tiles are icon-only squares, six per row. They use the usual 48px button size where space permits and shrink evenly on narrow screens so all six fit. Accessible names, tooltips, icon customization and selected white outlines remain. Geometry checks passed at 320, 390 and 600px; the detailed editor screenshot was reviewed. The native label picker uses six columns with square icon-only buttons and retained selection behavior. Task rows and the widget front are unchanged.

The first CI attempt passed layout and interaction checks but caught a missing version entry in the packaged changelog; the corrected full rerun passed. Packaged HTML, bridge, history and initial-state assets exactly match the source. Native picker selection and existing task workflows passed.

APK: `Tasks-v0.80.0.apk`, 331606 bytes. SHA-256 `54bc7b1c49d68987efed90c0f345d630f8136f321c86e4aaf3dab617b2851659`. Package `com.personal.tasklist.preview`, version 0.80.0 (80). ZIP integrity, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.79.0 verification

Source: `ae7906dce7eb4ed522a77361fa6998c556d490a2`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43189796687). **193 Android tests, 893 browser checks, 37 calendar checks and zero lint errors passed.**

Task-editor context labels use rounded square tiles with icons and names, generous full-tile tap targets, keyboard focus and white selected borders/glow. The shared field covers detailed Add/Edit, label-only popups and widget-launched full editors. Priority choices retain native radio semantics while their visible circles are hidden and selected buttons use the same white outline/glow. Main task rows and widget-front sources are unchanged.

Native widget Quick add uses a three-column label-tile picker with square sizing, preserved custom symbols/images, selected borders and saved multi-label state. Existing Android multi-label/reset tests were updated to use the tiles. Existing browser label-edit checks now tap whole tiles and verify saved associations and cancellation; all other editor, widget, calendar, completion, ordering and settings suites passed. The detailed editor screenshot was reviewed.

APK: `Tasks-v0.79.0.apk`, 331606 bytes. SHA-256 `137d458204bb539766d1170b88d418eaaf341921d12eafd52f0b9b4912250c41`. Package `com.personal.tasklist.preview`, version 0.79.0 (79). ZIP integrity, exact packaged assets, all non-signature entries matching CI, alignment and v2/v3 signatures passed. The retained certificate matches Tasks 0.78.0. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.78.0 verification

Source: `20869ca39612d329177a89011f1d13470bb730e2`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42524394530). **193 Android tests, 893 browser checks, 37 calendar checks and zero lint errors passed.**

Clock and repeat artwork now begins at the same content column as the task title, priority text, action buttons and checklist controls. The checkbox keeps its own column. The fixed icon boxes and SVG shapes retain their sizes; only their internal horizontal inset is compensated within metadata flow. This preserves icon/text spacing, dot-separator wrapping, fonts and task ordering. The adjustment excludes the widget editor, and the widget front is unchanged.

Geometry checks compare the clock's painted left edge, first task button and checklist disclosure against the title at 320, 390 and 600 px in Today and Upcoming. The prior state is restored before the existing layout tests. The full drag, calendar, completion, editor, notification, widget and other suites pass.

APK: `Tasks-v0.78.0.apk`, 331709 bytes. SHA-256 `c38474a81f48c59d77ee2ce72c6a62f7b6fd214c96a388a3c9fb77c5be69dc60`. Package/version, retained update signer, alignment, ZIP integrity and exact packaged assets were verified. Every non-signature entry matches the tested CI artifact. Tests use Robolectric and Chromium; no physical phone was used.

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
