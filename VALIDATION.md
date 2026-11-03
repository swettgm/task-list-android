# Tasks 0.34.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41553642825), source commit `447f515b43209c6be785af5f25898269f4e15253`.

108 Android tests, 483 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Quick add resets all task fields after each successful save and keeps the popup open. The native widget form supports several checked labels, Today/Tomorrow/Weekend/custom dates and an optional time. Tests cover selected-label checks on reopening, independent consecutive saves, reset priority/date/time/labels, native date/time picker callbacks, calendar/time persistence, field spacing and retained task history.

Weekend tasks appear in Today starting Friday at 6 PM by default. Settings can change the Friday cutoff time. One-off weekend dates, weekends-only weekly repeats and alternate-weekend patterns keep their original occurrence and completion keys. A Saturday/Sunday repeating task previews Saturday first, preserving its separate Sunday recurrence. Daily tasks remain on their usual dates. Explicit task deferrals take precedence. A weekly maintenance alarm refreshes the widget at the cutoff without sending a notification; the app also refreshes open lists. Normal Notify opt-in and scheduled time still control task notifications.

Progress now stores multiple active occurrences with a compatible legacy cursor. The app banner joins active titles with commas; completion or Stop removes only matching task/step keys. Native snapshots preserve all active states across stale UI refreshes. Existing single-task state remains readable. The two-second completion undo layout retains its height when another task stays active.

Low disables its down arrow and High disables its up arrow. Holds jump to endpoints; a fresh pointer click after a hold is no longer blocked by the release guard. Unset remains a separate default/editable priority. Linked-task notes use the same font size as duration/time. Upcoming headings use month/day ordinals, including tomorrow, and keep the next day expanded. Empty widgets center the app’s exact flower/rest sentence. Fresh widget layouts refresh existing hosts.

[Download Tasks-v0.34.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.34.0.apk).

Package `com.personal.tasklist.preview`, version 0.34.0, code 34; minimum API 26, target 35; size 216,110 bytes.

SHA-256: `d076c5beaf8531518e69a388d480e64532a7a4d0b52039b82d8b7a46f691ee5e`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Android alarm and background policies still govern refresh timing. Windows development and pairing are deferred.
