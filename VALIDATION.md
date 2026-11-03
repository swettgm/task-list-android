# Tasks 0.46.0 verification

Source: cb1d471b1bd2463b25356c4eddaf95dfb0d15f55. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41724118536): 128 Android tests, 680 browser checks, 27 calendar checks and zero lint errors.

Duration, repeat and linked-task icons now use separate text spans and centered flex layouts with 6 px caption gaps. Context glyphs use centered wrappers, uniform 24 px button heights and 6 px label gaps. An old extra 7 px margin is removed. Checks compare actual icon/caption vertical centers and gaps, including wrapped captions, symbols, custom emojis and the Guild image. Native label images use a centered ImageSpan that preserves the font's vertical center across text sizes. The Guild crest uses fixed 20 dp native bounds and a 20 px web inline size, avoiding system-font enlargement. Native images share a 24 dp text-advance slot, keeping label captions aligned despite the smaller crest.

The widget editor retains top and bottom safe-area insets. Its status-bar area uses the same 72% black dimming as the dialog backdrop, with a foreground scrim limited to the top inset and a matching status-bar color on supported Android versions. Tests verify retained clearance, status-bar tint, foreground presence and unchanged editor state.

Detailed editors have a touch-friendly Checklist size slider, retaining the native textarea resize handle. Increasing the field does not change its content, move the fixed action row over the fields or write task state before Save. Bracketed lines such as [Raid] create checklist section dividers; [] resumes ungrouped items. Dividers are section metadata on real items and never count as checkboxes. Existing Guild sections round-trip into the editor. Tests cover new sections, renamed headings, moving completed items, preserved completion keys, future daily occurrences and item-only totals. Native reminders continue to consume actual checklist item keys.

`Tasks-v0.46.0.apk`: version 0.46.0 / code 46; 253,508 bytes. SHA-256: `276e1dd23c4ba65ec68a095135c34a6da9e857914ef1a8fb14ceca68fcb8d45a`.

Artifact hashes, ZIP CRCs, CI checksum, package/version, exact packaged UI assets including changelog data, all non-signature APK entries, alignment and the retained preview signer passed verification. The signer matches Tasks 0.45.0, and the original seed state is unchanged. Both download paths contain the same signed APK. Settings Changelog contains 44 published releases including this update. Existing task, widget, notification, icon and checklist workflows remain covered.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
