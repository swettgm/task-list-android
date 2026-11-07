# Install Tasks

1. Download [Tasks-v0.75.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.75.0.apk).
2. Open the APK on your Android phone and install it over your current app. Existing tasks and settings remain saved.
3. The launcher label is now **Tasks**. The app reopens on your last selected Today or Upcoming tab.
4. In Tasks → Settings, tap **Allow precise reminders** if shown, then enable **Allow setting alarms and reminders** in Android. Return to Tasks to refresh its alarm plan. Allow notifications when Android prompts. Scheduled notifications require a set task time plus Notify checked; the separate daily reminder starts at 9 AM and is configurable in Settings. Android may delay alerts while idle; force-stopping the app blocks alarms until reopened.
5. Customize all timed task notification titles and messages under Settings → Notifications. Optional {task}, {time} and {date} placeholders insert each task’s details; the separate daily reminder has its own title and message fields in the same section.
6. Customize the persistent in-progress notification under Settings → Notifications. Use {tasks} for active task names and {count} for their number. It updates as tasks start, stop, move or complete. Tap Complete to finish the active task, or choose which task to finish if several are active. The notification is ongoing; if Android permits a swipe dismissal, it restores itself while any task remains in progress.
7. New widgets request a wider, shorter default where the launcher supports it. For an existing widget, long-press it and drag the launcher’s resize handles to adjust its outer bounds. The widget message stays centered within those bounds. Add a widget through Settings → Add home-screen widget, or your launcher’s Widgets menu. Background opacity can be set to 0% in the widget’s settings. Tap an individual task title to edit it in a popup over the home screen; tap Today at the top to open the app.

8. In a task’s full editor, set a Time, enable Hide until before due time, and enter Hours before and Minutes before. Use Hidden at the bottom of Today to temporarily reveal hidden tasks for editing; those rows have a white outline. Upcoming always shows future scheduled tasks, including hidden ones. A zero interval reveals the task when due.

Requires Android 8.0 or newer. This remains a debug build for personal use. The separately installed Windows companion pairs through the QR code or Settings → Windows companion. See [Windows instructions](desktop/USING-TASKS-PC.txt).

The package remains `com.personal.tasklist.preview` so it updates the existing Task List Dark app. Keep any separate original Task List installation if it holds other data; that package is independent.

The source can be rebuilt through the [Android workflow](https://github.com/swettgm/task-list-android/actions/workflows/build-apk.yml) or locally with JDK 21 and Android SDK 35. See [VALIDATION.md](VALIDATION.md) for the exact verified source, checksum, and remaining device checks.

Tasks follow their chosen dates strictly. Past unfinished tasks stay saved without appearing on later days automatically. Use the refresh icon beside Settings to reload state, refresh widgets/reminders and request companion sync. The calendar also refreshes at midnight and when the app regains focus.
