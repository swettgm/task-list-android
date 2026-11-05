# Tasks 0.64.0 verification

Source: e89797e917adf514a137632ebd7b8912224aaec3. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/42023527497): 151 Android tests, 811 browser checks, 33 calendar/timing checks and zero lint errors.

Linked task information follows the priority label inside the existing metadata flow. The same dot separator spacing and line-boundary clipping apply. The chain icon and linked task name remain one unit when wrapping; a name wider than the entire available row is ellipsized rather than split. Linked details retain the duration/time text size, vertical alignment and icon spacing. Standalone active-task banner links retain their layout.

Browser checks verify ordering after priority, the dot separator, atomic wrapping, vertical centering and available-width containment at 360, 390 and 650 pixels. Existing linking, hidden annotations, app/widget progress, task editing, completion and full workflow checks pass. Widget front layouts and main task-list checkbox sizing are unchanged.

APK: `Tasks-v0.64.0.apk`, 290567 bytes. SHA-256: `18fe5f8d9b95561e6536bcce32609675a63ee80757d31e86fede838d5300badd`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.63.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
