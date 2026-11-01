# Task List Dark — Android

An offline Android version of the supplied task-list HTML and PDF handoff, with a dark interface and date-only header. Requires Android 8.0 or newer.

## Task list

The first launch includes all 102 task definitions and 304 saved completion records from the latest HTML, including the PDF handoff’s tasks and history. The task engine preserves recurring schedules, nested checklists, priorities, durations, saved progress, historical occurrence IDs, moves, and rollover completion.

- **Today** shows pending tasks; **Upcoming** groups future work by date.
- **Do now** toggles the current task. **Do tomorrow** moves an unfinished one-time occurrence without changing its history.
- Checking a task saves it immediately and hides it. **Show hidden** reveals completed occurrences; uncheck one to reopen it. Parent checkboxes complete their checklist too, and individual steps save immediately.
- Filter by **At home**, **On the road**, **On the phone**, **On the computer**, or other contexts. Use the **⚙** on a task to change its contexts or other details.
- The large corner **+** opens a one-field **Quick add** form. Enter a task and press **Add**, or choose **Details** for the compact full editor. Priority and repeat choices appear in rows, date/time/duration share a small grid, and contexts use checkboxes. **Notes & checklist** expands to show the remaining fields. All previous task information is retained.
- Android Back closes Quick add, the full editor, and Settings before exiting the app.
- **Settings ⚙** accepts custom accent and background hex colors, with live preview, validation, and a reset option. Colors remain saved when the app reopens.
- The context dropdown and header buttons share a 48-pixel height. Open **Settings ⚙ → Add home-screen widget** for widget placement.
- The creation/edit menu has a **Linked tasks** dropdown and **Link task** button. Only links you choose are shown; you can remove them in the same menu. Linked tasks keep separate completion states.
- AI recommendation text, suggested task links, copy-progress controls, backup controls, and the bottom save button have been removed.

Dates follow America/New_York, as in the original task system. State is stored on the device; the app requests no internet permission. The user authorized including the supplied personal task definitions and initial history in this public repository.

## Home-screen widget

Touch and hold an empty area on the Android home screen and choose **Widgets → Task List Dark checklist**. Each widget has independent checklist/context filters, title, colors, transparency, and size.

Ticking a widget row immediately saves completion in the full task system. The **+** button opens a small floating quick-add form over the home screen, with context, priority, and Today/Tomorrow options. Saving closes the form without opening the full app. No “draw over other apps” permission is needed.

## Install and build

See [APK_BUILD.md](APK_BUILD.md) for installation and [VALIDATION.md](VALIDATION.md) for checks. Version 0.4 uses package `com.personal.tasklist.preview` and the label **Task List Dark**, so the earlier Task List app can remain installed with its data.

The [Android build workflow](https://github.com/swettgm/task-list-android/actions/workflows/build-apk.yml) builds the APK, runs Robolectric tests and Android lint, exercises the full HTML workflow in Chromium, and verifies the APK signature.

Build locally using JDK 21, Android SDK Platform 35, and Build-Tools 35.0.0:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The bootstrap launchers download and checksum-verify Gradle 8.11.1. The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Store publication needs a release signing configuration.

## Source

- `app/src/main/assets/www/`: full offline task document and first-launch history.
- `bridge.js`: contexts, gear editor, manual linking, and Android integration.
- `MainActivity.java`: offline WebView and native clipboard/file export.
- `ReferenceStore.java`: canonical task state and widget synchronization.
- `TaskStore.java`: native widget task mirror.
- `QuickAddActivity.java`: floating widget task form.
- `TaskWidgetProvider.java` and `WidgetConfigActivity.java`: checklist widget and customization.
- `app/src/test/` and `tools/browser-smoke.mjs`: Android and browser behavior checks.
