# Tasks 0.50.0 verification

Source: 6c267fa1b1c2173d3ffe9aae5808f09af715ca4d. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41737053772): 131 Android tests, 696 browser checks, 27 calendar checks and zero lint errors.

The notification bell now uses a filled white silhouette with no outline stroke. The drawing is scaled to 94% around its center within the existing 24px icon slot, preserving vertical alignment, time spacing and the clickable time component. It remains conditional on a scheduled time with notifications enabled. Existing browser checks cover notification visibility, all metadata icon preferences, alignment and reminder edits.

Signed APK: 257,604 bytes. SHA-256: `e89e07df8c1e60a0be147096bfe927a2985a5f719273910a99ab6a42b7fdeb3a`.

Artifact hashes, ZIP CRCs, CI checksum, exact packaged UI assets, all non-signature APK entries, alignment and the retained signer passed verification. The signer matches Tasks 0.49.0. Both published download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
