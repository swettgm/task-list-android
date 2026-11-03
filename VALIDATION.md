# Tasks 0.42.0 verification

Source: fd23f3558a242205080fbc80ed10ec677582df34. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41677744192): 125 Android tests, 626 browser checks, 27 calendar checks and zero lint errors.

Only High, Medium and Low priorities remain. App quick-add, detailed creation, widget quick-add, native default creation and project-to-task conversion all default to Low. Legacy unset, missing and Lowest priorities normalize to Low; Highest normalizes to High. Existing custom definitions and overrides are normalized when written, including the initial upgrade migration. Task identities, notes, labels, links, completion records and recurring definitions remain intact. The removed section and header count are absent from Today and Upcoming, and the widget groups former unset tasks under Low. Native and browser checks cover legacy records, saved canonical priorities, default creation/reset, widget headings, sorting, metadata preservation and priority-arrow endpoints.

The widget editor now respects Android's top system inset, reducing its usable height by the notification-bar area while retaining the small dialog margin and reserved bottom action row. An Android inset test verifies top and bottom padding, alongside the existing editor scroll and footer geometry checks. The thin border and background scroll lock remain in place.

The complete workflow also checks Trash and permanent-deletion safeguards, recurring task history, global notification templates, detail icons, task ordering, app gestures, widget editing and haptics. Settings Changelog contains 40 published releases including this update.

`Tasks-v0.42.0.apk`: version 0.42.0 / code 42; 240,841 bytes. SHA-256: `da4f2445878a1b38cadf4732545c61832ce6b2a3dc6e17edfd723a723c403531`.

Artifact hashes, ZIP CRCs, CI checksum, package/version, exact packaged UI assets including changelog data, all non-signature APK entries, alignment and the retained preview signer passed verification. The signer matches Tasks 0.41.0, and the original seed state is unchanged. Both download paths contain the same signed APK.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
