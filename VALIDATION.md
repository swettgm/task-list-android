# Tasks 0.55.0 verification

Source: 58c5522748289691f91bab2c6a027382e5758305. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41901693035): 139 Android tests, 736 browser checks, 27 calendar checks and zero lint errors.

Notes suppresses its native browser resizer while retaining the existing large custom corner grip. Its pointer/keyboard resize behavior is unchanged. Checklist and Project grips are untouched.

Task titles use one-row textareas that grow and shrink to their content on input and width changes. They have no resize handle. Detailed editors retain initial dialog focus and their reserved Save/Cancel footer. Quick add retains keyboard and mobile Enter-to-add behavior, clears and shrinks after adding, and removes the visible Task caption while preserving its accessible name. Checks cover growth, shrinkage, full content visibility, footer separation and compact popup sizing.

Settings is a full in-app screen with Back navigation rather than an HTML modal. It hides the task page, keeps normal page scrolling and the shared fixed background, restores task-list scroll position on exit and participates in native Back handling. Changelog remains a child dialog. Checks cover page navigation, background sizing, scrolling, nested Back behavior, settings editing and unchanged task state.

The notification vector is a rounded white checkbox and tick on transparency. The launcher vector is a centered checkmark with rounded caps and joins, retaining its white background and original gradient palette. Android compilation verifies both resources.

Signed APK: 270,000 bytes. SHA-256: `862fc8d8e7b50eedef4bcbdf07953b375eb90011a3124520cd6063aca6f85ec6`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.54.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
