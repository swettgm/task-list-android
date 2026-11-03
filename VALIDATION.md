# Tasks 0.44.0 verification

Source: da10663bb5d7fdbf831e39c68d36b38be9447055. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41689274061): 127 Android tests, 669 browser checks, 27 calendar checks and zero lint errors.

Simple clock, repeat-arrow and chain-link symbols now use a 1.6 em container with a full-size SVG and slightly stronger white strokes. The old white disk and dark strokes are removed; backgrounds are transparent. This increases the drawn glyph size while retaining the existing task text size, label emoji sizes and Emoji / Simple symbols / Off choices. Browser checks verify the rendered size, white stroke color, transparent background and existing icon selection behavior, alongside header and wrapping geometry.

The five original label defaults (Home, Road, Phone, Computer and Outdoors) use solid white pictograms with transparent cutouts. The app and native label pickers honor saved emoji overrides, and the Guild crest is unchanged. Label identities, filters and task assignments are retained. Native checks cover each default and custom overrides; browser checks cover all five symbols and changing an original label to an emoji.

Settings → Notifications has separate Daily reminder title and Daily reminder message fields. The existing daily enable switch and reminder time remain available. Saving restores the custom wording on reopening Settings and preserves the independent timed-task title/message templates. Native delivery uses the saved daily text, retaining the original title and message when unset and honoring the existing opt-out. Tests inspect actual notification extras, literal punctuation and braces, defaults, scheduling, opt-in and timed-task independence.

`Tasks-v0.44.0.apk`: version 0.44.0 / code 44; 249,412 bytes. SHA-256: `fbcf156f363e175dc002fbc503e6ed03634d2f06b0fc85b1a5a26c281b4bf774`.

Artifact hashes, ZIP CRCs, CI checksum, package/version, exact packaged UI assets including changelog data, all non-signature APK entries, alignment and the retained preview signer passed verification. The signer matches Tasks 0.43.0, and the original seed state is unchanged. Both download paths contain the same signed APK. Settings Changelog contains 42 published releases including this update.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
