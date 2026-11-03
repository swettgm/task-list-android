# Tasks 0.37.0 verification

APK source: `38bafb8307cbc7f90c7b71ff281f455552183b00`. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41622863595): 114 Android tests, 514 browser checks including 15 widget editor checks, 26 calendar checks, and zero lint errors.

Widget task titles now send an edit action containing the stable occurrence ID. The provider checks that the widget is registered and the task is still visible before opening the unexported TaskEditorActivity. Completion and in-progress actions retain their existing separate targets.

TaskEditorActivity reuses MainActivity’s offline document, storage bridge and actual Edit task form. A transparent window hides the underlying app list and retains the editor’s rounded shape and saved gradient. The WebView is hidden until the selected task editor is ready. Missing or completed occurrences cannot create new tasks. The keyboard is not focused automatically.

Save persists through the existing definition and recurrence logic, publishes the updated widget snapshot synchronously, then closes the overlay. Failed saves leave the editor open with an error. Cancel, Back and outside taps close without saving. The close bridge only finishes widget editors, never the normal app.

Native regression checks cover widget launch validation, exact occurrence forwarding, transparent rendering and editor-only close behavior. Chromium checks cover the shared populated form, focus, repeat/label retention, immediate snapshot refresh, cancellation, failed saves, stale IDs, Back and outside dismissal. The full existing task, widget, reminder, touch and recurrence suites also run.

`Tasks-v0.37.0.apk`: version 0.37.0 / code 37; 220,294 bytes. SHA-256: `d65d7dcbacd32852d43b60c1ee8412978c0727a84df413892384139a3de6b26d`.

The retained signing certificate matches the prior update. Artifact digests, ZIP integrity, package/version, exact packaged assets and all non-signature APK entries passed verification.

No physical Android launcher or phone was used. Native tests use Robolectric and browser tests use Chromium. Windows work remains deferred.
