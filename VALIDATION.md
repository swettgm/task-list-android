# Tasks 0.39.0 verification — signing pending

Source: `d0b396a7efa01b70f791b32da27180fadc47e45e`. [Successful complete build and checks](https://github.com/swettgm/task-list-android/actions/runs/41626541458): Android unit tests and lint passed, 526 browser checks including 79 layout checks, and 26 calendar checks.

Urgent now shares the In progress badge's pill radius, padding, typography, line height and alignment. Its background stays red. Existing title-space placement and wrap behavior are retained.

The added Chromium geometry check compares the badges' computed shape, spacing, type and rendered height, verifies their separate red/green colors, and runs alongside the existing title wrapping and resize checks.

The personal preview signing workspace was offline when these checks completed. No signed 0.39.0 update has been published. The existing download links remain on 0.38.0. The CI artifact uses a temporary signing key and must be re-signed with the retained preview key before being distributed as an in-place update.

Before publication: synchronize the tested HTML into the private signing copy, verify artifact digests, ZIP integrity, packaged assets and package/version; sign with the retained key; compare every non-signature APK entry to the tested artifact; publish both versioned and legacy download paths with checksums.

No physical phone was used. Chromium and Robolectric checks do not replace device testing. Windows work remains deferred.
