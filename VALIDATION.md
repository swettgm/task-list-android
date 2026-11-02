# Tasks 0.23.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41479651446), source commit `ea6ebda73bf661ab85f8005a214831b47c5345ef`.

88 Android tests, 364 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Upcoming tasks have a Do today button with the same styling as Do tomorrow and a left-facing thick arrow. It moves the selected occurrence, retains its completion identity and original definition, updates Today and the widget, and removes that occurrence from Upcoming. Repeating definitions remain unchanged and later daily occurrences remain available. Checks cover one-time moves, legacy Daily and Every 1 day schedules, widget snapshots, independent occurrence completion and persistence after reload.

Checkbox centers now align with the vertical center of task titles without an optical offset. Checks cover Today and Upcoming at multiple widths and text sizes, wrapped parent titles and checklist steps, including active subtasks. Active subtask badges sit below the title with 6dp top and 4dp bottom padding.

Only checkbox interaction changes completion. Parent titles, checklist titles, labels and metadata do not. Time, repeat frequency and duration are individual accessible buttons styled as inline metadata. They open focused rounded popups using the existing theme; the full gear editor remains available. Frequency controls are shared with the full editor. Tests cover cancellation, time-only edits, notification clearing when time is removed, duration edits, Every 2 days, reload persistence, Upcoming edits, and retention of notes, links, checklist progress and completion history.

App, widget, quick-add controls and priority arrows now use High, Medium and Low. Medium is the default. Legacy Highest maps to High, Lowest to Low and no priority to Medium; unknown saved task metadata and task identities remain intact. Urgent tasks are High and sort first, before time and alphabetical order, on Today, Upcoming and the widget. Lowering an urgent task clears Urgent. Tests cover legacy priority records, the three editor choices, boundaries, daily persistence, later timed Urgent tasks above earlier High tasks, and widget ordering and headings.

[Download Tasks-v0.23.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.23.0.apk).

Package `com.personal.tasklist.preview`, version 0.23.0, code 23; minimum API 26, target 35; size 178,123 bytes.

SHA-256: `c2d57495ae3414df307a3c245914c25faff45d79f027b57e725ded673488e42d`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Android policies can delay background updates and notifications. These tests do not guarantee against every software, storage or device failure.
