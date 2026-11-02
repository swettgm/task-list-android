# Tasks 0.25.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41490957652), source commit `8ecfbe2d4ad8688406f7b0094506b47af0b49d78`.

90 Android tests, 434 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Holding the date opens Projects; Today and Upcoming remain the only tabs. Keyboard and assistive activation are also supported. Projects have notes, an optional date and duration, their own plus and completed list, and no labels, priority, time or checkboxes. Clicking a project opens its editor. Complete and Reopen move it between project sections. The trash button first turns red and asks Are you sure?; only the second tap deletes. Editing a field disarms confirmation.

Projects are stored separately by identity in the canonical private state. Deletion removes project content and retains a deletion marker so stale editors cannot resurrect records. Native tests verify projects and completion flags survive native task writes without entering task storage. Browser checks cover new/existing focus, save/cancel, optional fields, completion/reopening, two-step deletion, write failures, stale editors, reload persistence and narrow layouts. Task identities, completion history and manual links remain intact. Open dialogs count as active editing for native refresh handling.

Make task atomically creates one unset task with the project's notes and duration, and moves the project into its own completed list. Future dates are retained; an undated or past project starts Today. A conversion reference prevents duplicate task creation. Deleting the completed project preserves the new task. Projects themselves stay out of task counts and widget snapshots.

Horizontal drags can begin on task-panel controls, including time, repeat frequency, labels, checkboxes and the gear. Dragging does not activate their tap actions; ordinary taps retain editing and completion behavior. Checks use touch events to verify time/repeat/emoji navigation, checkbox drags without completion and gear drags without opening an editor, along with finger tracking, settling, cancellation, vertical position and reduced motion.

App, widget and Projects add controls have rounded plus strokes. The button uses the accent; the glyph uses the app background color or gradient. Web glyphs use a vector mask and shared theme variables. The native widget uses an antialiased bitmap drawn from the saved theme, with a 48dp tap area aligned to the outer edge and no outline. A fresh widget layout resource refreshes installed widget views. Native tests verify bitmap inflation, layout bounds and add-task routing; browser checks verify rounded masks and theme colors. Projects and editor screenshots were visually reviewed.

[Download Tasks-v0.25.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.25.0.apk).

Package `com.personal.tasklist.preview`, version 0.25.0, code 25; minimum API 26, target 35; size 186,554 bytes.

SHA-256: `05f98d1050ada8b63b293ac82a07d755263208ff17c1e480dcc192447be4b626`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard, vibrator or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Device settings control physical haptics, and Android policies can delay background updates and notifications. These tests do not guarantee against every software, storage or device failure.
