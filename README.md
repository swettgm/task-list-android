# Tasks — Android

A personal Android checklist based on the supplied task list. The app includes the original task definitions and saved history, works offline, and keeps existing installed data when updated.

- Opens on Today, with a date-only header and corner plus for Quick add. Tap outside either task editor to dismiss it without saving.
- Task checkboxes use the chosen accent color, with optical alignment against task titles and mobile text autosizing disabled. Swipe horizontally between Today and Upcoming; vertical scrolling and interactive controls retain their usual behavior. Both screens share task-row markup, fonts and spacing; duration and other details sit below the title. Completion saves immediately, pauses for two seconds for undo, then hides the task without an extra undo note. Urgent badges are red. Show hidden lists highlighted completions from today first.
- Contexts use emojis: 🏠 🚗 📱 💻 🌳. Choose existing contexts on tasks; add custom contexts in Settings.
- Use the native time picker and Tomorrow / Weekend shortcuts alongside the date picker. Edit duration with larger, full-width Hours and Minutes boxes. Notes and Checklist stay expanded on separate rows. Lists show durations without numbering or the word total.
- Repeat with Every X days, weeks, months, or years, or choose Daily, Weekdays, or Weekends. Clean out truck shows Sunday–Thursday.
- Upcoming excludes work currently pending on Today. Tomorrow is expanded with a simple Tomorrow heading; later dates have full-date headings in separate frameless dropdowns without divider lines. Upcoming tasks have checkboxes and visible Do now state.
- Link unfinished tasks by title in the editor. Links display immediately and remain attached to repeating task definitions.
- Settings includes hex fields, swatches/color wheels, optional gradients, and a slate gradient preset. A separate viewport-sized background layer keeps the gradient visible while scrolling long lists in either tab; controls use translucent surfaces.
- Customize the interactive home widget, including a fully transparent 0% background. Its plus opens a small home-screen form.

The app is called Tasks and uses the white icon with black checklist lines. Windows development and pairing are deferred; this Android update requests no internet permission.

## Install

See [APK_BUILD.md](APK_BUILD.md) for the latest APK and [VALIDATION.md](VALIDATION.md) for verification. Requires Android 8.0 or newer. Install over the existing app; do not uninstall or clear its data.

## Build

Use JDK 21 and Android SDK Platform 35 / Build Tools 35.0.0:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

GitHub Actions also checks the real packaged UI in Chromium. Release signing keys remain outside the repository; the downloadable personal-test APK uses the retained preview signing key for in-place updates.
