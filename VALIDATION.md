# Tasks 0.47.0 verification

Source: d6b17f3111198905239f6aa59a1a554f2f1368ec. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41725644114): 128 Android tests, 681 browser checks, 27 calendar checks and zero lint errors.

Removed the outer fieldset frames, padding and side margins from Priority, Repeat, Contexts and Linked tasks across detailed and component editors. Legends and control groups share the other fields' left edge. Input borders, individual option outlines, selected states and the rounded popup frame remain intact. A geometry check verifies zero group borders and equal label/control left edges. Existing editor footer, scrolling, viewport and input checks remain in the complete workflow.

This update also includes the centered icon captions and consistent spacing, compact Guild crest and centered native label images, dimmed widget-editor status-bar area, Checklist size slider and bracketed Checklist section headings introduced in 0.46.0. Checklist editing checks preserve completion keys, item-only counts and future recurrences.

`Tasks-v0.47.0.apk`: version 0.47.0 / code 47; 253,508 bytes. SHA-256: `dc51d5cd681770960804d0b4c61b32f64f5519a0c212bfd2e12f68c9bdcc8e43`.

Artifact hashes, ZIP CRCs, CI checksum, package/version, exact packaged UI assets including changelog data, all non-signature APK entries, alignment and the retained preview signer passed verification. The signer matches Tasks 0.46.0, and the original seed state is unchanged. Both download paths contain the same signed APK. Settings Changelog contains 45 published releases including this update.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
