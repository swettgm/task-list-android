# Tasks — Android

A personal Android checklist based on the supplied task list. The app includes the original task definitions and saved history, works offline, and keeps existing installed data when updated.

- Opens on Today, with a date-only header and corner plus for Quick add. Tap outside either task editor to dismiss it without saving.
- Task checkboxes use the chosen accent color, with optical alignment against task titles and mobile text autosizing disabled. Swipe horizontally between Today and Upcoming; vertical scrolling and interactive controls retain their usual behavior. Both screens share task-row markup, fonts and spacing; duration and other details sit below the title. Completion saves immediately, pauses for two seconds for undo, then hides the task without an extra undo note. Urgent badges are red. Show hidden lists highlighted completions from today first.
- Guild has a static image label extracted from frame zero of the supplied v8 GIF, with transparent corners. The crest is slightly larger than the standard emoji glyphs, about 12% larger than its original display size, in task rows, choices, the selected filter, native pickers and the widget summary. The existing Guild task gains the label while retaining its other edits and contexts. The image is also available in the label icon picker.
- Contexts use emojis: 🏠 🚗 📱 💻 🌳. Choose existing contexts on tasks; add custom contexts and choose their emoji in Settings. Use a palette or an emoji from your keyboard. Changing a label emoji updates existing tasks, the filter and native widget/quick-add pickers while retaining the label identity.
- Use the native time picker and Tomorrow / Weekend shortcuts alongside the date picker. Edit duration with larger, full-width Hours and Minutes boxes. Notes and Checklist stay expanded on separate rows. Lists show durations without numbering or the word total.
- Labels and normal edits are saved to a task definition and carry forward to its future daily recurrences. Checklist changes apply from the edited occurrence onward; completion is stored per occurrence.
- Repeat with Every X days, weeks, months, or years, or choose Daily, Weekdays, or Weekends. Clean out truck shows Sunday–Thursday.
- Upcoming excludes work currently pending on Today. Tomorrow is expanded with a simple Tomorrow heading; later dates have full-date headings in separate frameless dropdowns without divider lines. Upcoming tasks have checkboxes and visible Do now state.
- Add, rename, reorder or remove checklist steps in Edit task. Stable step identities preserve checkmarks and completion dates; occurrences before the edit retain their original checklist.
- Set a task time and check Notify beside Urgent for a scheduled notification. Settings has a separate daily reminder, initially 9 AM, and independent controls for both reminder types. Android notification permission is required. Alarms resume after restart and app updates; power-saving mode or force-stop can delay/block delivery.
- Saves check commit results and reject unreadable state rather than resetting it. Local storage cannot guarantee against uninstalling, clearing data or device loss.
- Link unfinished tasks by title in the editor. Links display immediately and remain attached to repeating task definitions.
- Settings includes hex fields, swatches/color wheels, optional gradients, and a slate gradient preset. A separate viewport-sized background layer keeps the gradient visible while scrolling long lists in either tab; controls use translucent surfaces.
- The interactive home widget is a native scrollable Today list with all open Today rows, no fixed row cap, no completed/history or upcoming rows, and a simple Today header without counts or list-name subtitles. Every row keeps its Today badge. Context/checklist filters, colors and fully transparent 0% backgrounds remain configurable; its plus opens a small home-screen form. Existing widgets switch to the new layout on update.

The app is called Tasks and uses the white icon with black checklist lines. Windows development and pairing are deferred; this Android update requests no internet permission.

## Install

See [APK_BUILD.md](APK_BUILD.md) for the latest APK and [VALIDATION.md](VALIDATION.md) for verification. Requires Android 8.0 or newer. Install over the existing app; do not uninstall or clear its data.

## Build

Use JDK 21 and Android SDK Platform 35 / Build Tools 35.0.0:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

GitHub Actions also checks the real packaged UI in Chromium. Release signing keys remain outside the repository; the downloadable personal-test APK uses the retained preview signing key for in-place updates.
