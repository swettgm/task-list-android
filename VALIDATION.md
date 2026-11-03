# Tasks 0.40.0 verification

Source: `6979102018d1fee0386a73ec5597a80bff8d6b44`. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41651979638): 122 Android tests, 601 browser checks including 19 history/alignment and 19 reserved-footer checks, 27 calendar checks and zero lint errors.

Widget feedback uses a short touch vibration after registered, visible task actions and on widget-header launches. Existing quick-add haptics remain separate, with no duplicate launch tick. Android’s touch-feedback setting and missing hardware are respected. Invalid or invisible rows do not trigger feedback. API 33 uses touch vibration attributes; older APIs use the sonification usage and a light fallback. Feedback failure cannot block task actions. The normal VIBRATE permission is declared.

Text button and dropdown contents are start-aligned in the widget quick-add form, configuration screen and native picker confirmation buttons. The task-title field uses the same alignment. Web text controls, editable fields, date/time values and dropdowns are left-aligned, with centered button content. Centered icon-only controls, wrapped task-title checkbox geometry, badge layout, widget bounds and the centered empty message are retained. Button sizes, field spacing and gradients are retained. Task scheduling, completion and stored data formats are unchanged.

The detailed task editor has a flex layout with a separate scrollable field area and a reserved, always-visible footer. Short-viewport geometry checks verify separation, usable button hit targets and no overlap during scrolling and resizing. Task and project trash controls are icon-only, arm in red, expire after three seconds and require a second tap. Editing a field or closing the dialog resets confirmation. Subtask Do now buttons share the parent outline, retaining green active styling.

Settings now includes Changelog with 38 published release entries, 0.2.0 through 0.40.0. Notes were backfilled from recorded release commits and validation documents. No nonexistent 0.6.0 release was invented. The newest version is expanded; previous versions can be opened individually. Viewing history does not modify task state. A regression check requires the first entry to match the app version and compares rendered notes with the packaged JSON history.

`Tasks-v0.40.0.apk`: version 0.40.0 / code 40; 232,649 bytes. SHA-256: `59c81e53f352ffb604653c3e7b61dbc8e7cf7b7bec2ace63d238b7f1cadf5103`.

Artifact hashes, ZIP CRCs, CI checksum, package/version, exact packaged UI assets including changelog data, the retained preview signer and all non-signature APK entries passed verification. Signature and alignment checks passed, and the signer matches the prior 0.39.0 update. The original seed state is unchanged. Both download paths contain the same signed APK.

No physical phone or launcher was used. Native checks use Robolectric; browser checks use Chromium. Physical vibration depends on phone hardware and settings. Windows work remains deferred.
