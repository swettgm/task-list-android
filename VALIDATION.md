# Tasks 0.63.0 verification

Source: 1e0c39c2fcd4f0605f8cfe3ef31d4292dfbd65ab. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/42018887587): 151 Android tests, 807 browser checks, 33 calendar/timing checks and zero lint errors.

Projects now opens a title-only Quick add popup. Add and Enter save immediately, clear the title and retain focus for more entries. Details carries the unsaved title into the full project editor for notes, date and duration. Empty and failed saves retain the draft; existing project editing, completion, deletion and task conversion remain covered.

Hidden expansion pins the footer button at its existing viewport position while revealing tasks above it, adding scroll room when needed on short pages. Today and Upcoming have viewport-position checks; Completed and Trash continue expanding below their controls. Scheduled visibility, counts, totals and reveal boundaries are retained.

Linked task names share the duration/time text size and line height. Explicitly stopping in progress removes the entire connected linked group, including active checklist steps, in both app and widget state paths. Cycles and reverse links are supported; unrelated active tasks remain. The persistent notification refreshes from the updated shared state.

The default persistent notification title is 🟢 In progress. Existing plain In progress titles gain the circle, while custom titles remain unchanged. A dedicated white warning-triangle icon uses rounded corners and a cutout exclamation mark. Other notification icons and widget front layouts are unchanged.

APK: `Tasks-v0.63.0.apk`, 290567 bytes. SHA-256: `1bf1ffe6b37bca91b67a7323170269a2a37fc5a10904dba7ff7e2ccc42763225`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.62.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
