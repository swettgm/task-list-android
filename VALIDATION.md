# Tasks 0.51.0 verification

Source: ae033205f485ee47d3535d0a3e93e9f8f4752da2. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41738114336): 131 Android tests, 698 browser checks, 27 calendar checks and zero lint errors.

The first Upcoming date now has a smaller top margin, calculated from the existing Today summary margin and line height and the centered 42px Upcoming heading. At the default text size this raises the entire Upcoming list by about 8px while leaving Today, font sizes and the spacing between later dates unchanged. Both-direction swipe checks compare the actual header centers and retain the existing checks against vertical shifting before, during and after transitions.

Includes the solid white, slightly smaller notification bell from Tasks 0.50.0. Notification visibility, alignment and reminder editing checks remain in place.

Signed APK: 257,604 bytes. SHA-256: `e139ce32c97e4826bff71a9f1a5465eded0a2ce06006817b5da699200f787fed`.

Artifact hashes, ZIP CRCs, CI checksum, exact packaged UI assets, all non-signature APK entries, alignment and the retained signer passed verification. The signer matches Tasks 0.50.0. Both published download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
