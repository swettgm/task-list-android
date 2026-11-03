# Tasks 0.38.0 verification

Source: `b288f3f889faccf4cf8e03e21c65aefab96317dd`. [Successful build and checks](https://github.com/swettgm/task-list-android/actions/runs/41624813708): 114 Android tests, 525 browser checks including 78 layout checks, 26 calendar checks and zero lint errors.

Urgent and In progress use the same title-space layout pass. Each badge is measured against the title’s natural width before being placed beside it, with a 4px gap and centered vertical alignment. Urgent is considered first when both badges are present. Badges that do not fit stay in the details area; multiline titles retain their full width. The same pass runs for Today and Upcoming, viewport changes and font updates. Completion, urgency ordering, widget actions and storage behavior are unchanged.

Layout regression checks cover Urgent on Today and Upcoming at 320px, 390px and 600px, both badges together, repeated resizing, long-title wrapping, checkbox alignment and horizontal overflow. The existing native, browser and calendar suites remain enabled.

`Tasks-v0.38.0.apk`: version 0.38.0 / code 38; 220,294 bytes. SHA-256: `d6146bb1b1f7016384b20259942ce5e544d584da8fec2405b045c33b6715c6d7`.

Artifact digests, ZIP integrity, exact packaged assets, package/version, retained signing certificate and all non-signature APK entries passed verification. Install over the current app to retain tasks and settings.

No physical phone was used. Browser checks run in Chromium and native checks in Robolectric. Windows work remains deferred.
