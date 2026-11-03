# Tasks 0.39.0 verification — signing pending

Source: `bc235b8cfc16708b1939bcff184a66ed6afac598`. [Successful complete build and checks](https://github.com/swettgm/task-list-android/actions/runs/41628791968): Android unit tests and lint passed, 561 browser checks including 79 layout, 20 Friday-weekend, 16 widget-editor and 17 deletion checks, plus 27 calendar checks.

Changes verified:
- Urgent shares In progress's pill shape, padding, typography and height, retaining its red color.
- Weekend shortcuts choose Friday in the detailed editor and native widget quick add. Saturday and Sunday use that weekend's Friday. Existing saved occurrence identities remain unchanged.
- Detailed task editors focus the dialog and explicitly dismiss the keyboard. Quick add retains title focus.
- Action buttons occupy their own row below the fields, with geometry checks across responsive widths and the widget editor.
- Priority choices are High, Medium and Low. No choice saves UNSET; widget quick add uses a disabled Priority placeholder.
- Upcoming uses Tomorrow plus date for the next day, and full weekday names plus ordinal dates afterward.
- The summary shows the completion fraction, combined hours/minutes and untimed tasks, without a pending count.
- Existing task editors offer an outline trash icon with gray first-tap confirmation and red second-press deletion. Input changes reset confirmation. Deletion removes current and future occurrences, links and matching active state while retaining saved completion history. Tests cover failed writes, built-in legacy carryovers, recurring tasks, other active tasks and immediate widget refresh.

## Pending distribution

The signing workspace is unavailable to this session's execution tools. No signed 0.39.0 update has been published; download links still point to 0.38.0. The CI artifact uses a temporary signing key and requires the retained preview key for an in-place update.

Reported CI artifacts:
- Task-List-Personalization-Tools — artifact 13227601077; sha256:c5de9da73531cb08fd3adc079083be2e9ba1e006d3c045cab9e294331c3f7d24
- Android-test-and-lint-reports — artifact 13226874159; sha256:aab0be0e108fb6f2a27c925dd788a343509bde60f5238f6aa3e72182f4651cd0
- Task-List-Android-Debug-APK — artifact 13226559628; sha256:88f57793e60e29fec3345ebe4f71b1d03797d41167e13e2dde7fd0fb3b90e8f9

Before publication: synchronize all tested source files and the private signing HTML from the verified commit, verify artifact digests, ZIP integrity, package/version and exact packaged assets; sign with the retained preview key; compare all non-signature entries with the tested CI APK; publish both versioned and legacy download paths with checksums. Keep the original seed state and signing key.

No physical phone was used. Chromium and Robolectric checks do not replace device testing. Windows work remains deferred.
