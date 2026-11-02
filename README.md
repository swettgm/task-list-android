# Tasks — Android

A personal Android checklist based on the supplied task list. The app includes the original task definitions and saved history, works offline, and keeps existing installed data when updated.

- Opens on Today, with a date-only header and corner plus for Quick add.
- Task checkboxes share a title-only row; duration, recurrence, and emoji details sit underneath. Completion saves immediately, pauses briefly for undo, then hides the task. Show hidden lists highlighted completions from today first.
- Contexts use emojis: 🏠 🚗 📱 💻 🌳. Task settings retain the original context identities.
- Edit duration with separate Hours and Minutes boxes. Lists show durations without numbering or the word total.
- Repeat with Every X days, weeks, months, or years, or choose weekdays. Clean out truck shows Sunday–Thursday.
- Upcoming excludes work currently pending on Today. Tomorrow is expanded; later dates have separate frameless dropdowns with thin date dividers. Upcoming tasks have checkboxes and visible Do now state.
- Link unfinished tasks by title in the editor. Links display immediately and remain attached to repeating task definitions.
- Settings includes hex fields, swatches/RGB color pickers, optional gradients, and a slate gradient preset.
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
