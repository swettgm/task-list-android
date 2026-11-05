# Tasks 0.58.0 verification

Source: 1611fc0e267156b3f6e7b9f5394130c5fb55462a. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41948387591): 144 Android tests, 749 browser checks, 27 calendar checks and zero lint errors.

Moving a task through Do tomorrow, Do today, or a saved editor date change clears that task's in-progress status. Active checklist steps under the moved occurrence are cleared too. Changing a recurring task's start date clears its active occurrences. Other active tasks, including linked tasks, keep their independent status. Completion history and task links remain intact.

Regression checks cover both move buttons, detailed date edits, recurring start-date changes, checklist completion preservation and reload persistence. Saving without changing the date and cancelling a date change preserve the in-progress mark. Existing native ongoing-notification checks verify that the notification follows canonical active-task changes and disappears when no active tasks remain.

The active-task notification is ongoing and flagged against Clear all. Where Android permits an individual dismissal, its private delete intent restores it using fresh canonical state. API 28 and 34 tests simulate dismissal and verify a single restored notification, plus a stale-dismissal callback after the last active task stops. Android controls notification dismissal policy; this app restores the notification rather than claiming a platform-level non-dismissible exemption.

APK: `Tasks-v0.58.0.apk`, 278192 bytes. SHA-256: `b89e1570df47f070b1d2b0ec68ffc037f562cb5d5495e4759442291309404226`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.57.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
