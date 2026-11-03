# Install Tasks

1. Download [Tasks-v0.43.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.43.0.apk).
2. Open the APK on your Android phone and install it over your current app. Existing tasks and settings remain saved.
3. The launcher label is now **Tasks**. The app always opens on Today.
4. In Tasks → Settings, tap **Allow precise reminders** if shown, then enable **Allow setting alarms and reminders** in Android. Return to Tasks to refresh its alarm plan. Allow notifications when Android prompts. Scheduled notifications require a set task time plus Notify checked; the separate daily reminder starts at 9 AM and is configurable in Settings. Android may delay alerts while idle; force-stopping the app blocks alarms until reopened.
5. Customize all timed task notification titles and messages under Settings → Notifications. Optional {task}, {time} and {date} placeholders insert each task’s details; this does not change the separate daily reminder.
6. Add a widget through Settings → Add home-screen widget, or your launcher’s Widgets menu. Background opacity can be set to 0% in the widget’s settings. Tap an individual task title to edit it in a popup over the home screen; tap Today at the top to open the app.

Requires Android 8.0 or newer. This remains a debug build for personal use. No Windows companion or pairing is included in this update.

The package remains `com.personal.tasklist.preview` so it updates the existing Task List Dark app. Keep any separate original Task List installation if it holds other data; that package is independent.

The source can be rebuilt through the [Android workflow](https://github.com/swettgm/task-list-android/actions/workflows/build-apk.yml) or locally with JDK 21 and Android SDK 35. See [VALIDATION.md](VALIDATION.md) for the exact verified source, checksum, and remaining device checks.
