# Tasks 0.57.0 verification

Source: 5af909186515622581817f309eda9f4deb4b09f5. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41941285087): 142 Android tests, 741 browser checks, 27 calendar checks and zero lint errors.

Notes and Checklist retain one large app resize grip with the browser grip disabled. Continuous resize dragging scrolls only the editor fields, with the footer reserved and task text preserved. Title fields still grow automatically, and Projects retains its existing single native grip.

The Settings Back control matches the main tabs' height and top position. Header fitting measures a stable tab font weight, so the date's text size and vertical position remain constant when the active tab changes. Visible active/inactive tab styling is preserved.

Widget Quick add includes Urgent at the top right, saves High priority and urgent metadata to the app and widget, and clears the checkbox after each successful add. Starting a task in progress also starts linked tasks with cycle/duplicate protection; stopping an individual task remains independent. Completed and retired peers are skipped.

A separate low-importance ongoing notification shows active task names, updates as tasks start, stop or complete, and disappears when none remain. It stays separate from daily and timed reminders. Settings supports custom title/message templates with {tasks} and {count}; inserted task names are not recursively interpreted as template tokens.

APK: `Tasks-v0.57.0.apk`, 278192 bytes. SHA-256: `367c72b40df07136e3de10f3c8ed6ed662946e0acda1a29168d88925056e66e7`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.56.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
