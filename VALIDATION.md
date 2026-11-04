# Tasks 0.56.0 verification

Source: 934e3a178d98c201e9964409ff41f2eadc97ec94. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41906907210): 139 Android tests, 736 browser checks, 27 calendar checks and zero lint errors.

The detailed task editor places Urgent directly after Priority and before the Date/Time/Duration grid. It shares Priority's left edge and retains its prior checkbox size. Notify stays in the duration area. The existing save and prefill behavior, Quick add header and widget controls are unchanged.

The layout was reviewed in source and in the editor preview. Existing urgent-save, Quick add carryover and widget badge checks remain in place. Footer visibility, resizing and growing task-title checks continue to pass.

Signed APK: 270,000 bytes. SHA-256: `a0be1015ce21c2656c98aac644d51e774432b0d40729c29ecab51100ac217dc3`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.55.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
