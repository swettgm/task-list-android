# Tasks 0.39.0 verification

Source: `bc235b8cfc16708b1939bcff184a66ed6afac598`. [Successful complete build and checks](https://github.com/swettgm/task-list-android/actions/runs/41628791968): 116 Android unit tests and lint passed, 561 browser checks including 79 layout, 20 Friday-weekend, 16 widget-editor and 17 deletion checks, plus 27 calendar checks.

Changes verified:
- Urgent shares In progress's pill shape, padding, typography and height, retaining its red color.
- Weekend shortcuts choose Friday in the detailed editor and native widget quick add. Saturday and Sunday use that weekend's Friday. Existing saved occurrence identities remain unchanged.
- Detailed task editors focus the dialog and explicitly dismiss the keyboard. Quick add retains title focus.
- Action buttons occupy their own row below the fields, with geometry checks across responsive widths and the widget editor.
- Priority choices are High, Medium and Low. No choice saves UNSET; widget quick add uses a disabled Priority placeholder.
- Upcoming uses Tomorrow plus date for the next day, and full weekday names plus ordinal dates afterward.
- The summary shows the completion fraction, combined hours/minutes and untimed tasks, without a pending count.
- Existing task editors offer an outline trash icon with gray first-tap confirmation and red second-press deletion. Input changes reset confirmation. Deletion removes current and future occurrences, links and matching active state while retaining saved completion history. Tests cover failed writes, built-in legacy carryovers, recurring tasks, other active tasks and immediate widget refresh.

## Published APK

Version 0.39.0 / code 39, 220,294 bytes. SHA-256: `015fa5c9952ba49b911096d701cf7786d5415c5137e193e83cb4e5e5d0708b41`.

Artifact digests, ZIP CRCs, CI checksum and exact packaged assets passed verification. The retained signing certificate matches the prior update, and real APK signature, alignment, package and version checks passed. Every non-signature APK entry matches the tested CI artifact exactly. The original seed state is unchanged. Both versioned and legacy download paths contain the same signed APK.

No physical phone was used. Chromium and Robolectric checks do not replace device testing. Windows work remains deferred.
