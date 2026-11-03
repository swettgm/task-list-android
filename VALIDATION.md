# Tasks 0.48.0 verification

Source: 728482cc9cae6bd0a53c18ef6294e41e51d7f0a0. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41728704203): 128 Android tests, 685 browser checks, 27 calendar checks and zero lint errors.

Removed the separate Checklist size range control. Notes and Checklist have matching corner drag grips that resize the field directly with pointer capture for touch or mouse. Arrow keys also resize a focused grip. Resize operations preserve the textarea value, stay within the existing popup width and retain the visible, separate Save/Cancel footer. The textarea's vertical browser resize behavior remains available. Checks verify the absence of the old slider, both grips, direct field expansion, actual pointer dragging, preserved content and footer separation across normal and small viewports.

The Settings dialog has a dedicated gradient rule with fixed attachment, viewport-width/height sizing and a stable viewport position. Its long contents scroll over the same screen-sized gradient used by the main pages. Other popup painting and saved theme values remain unchanged. Checks verify Settings uses the saved gradient, matches the viewport dimensions and retains its gradient size/position when scrolled to the bottom.

Signed APK: 257,604 bytes. SHA-256: `74217d2041b180a3ba709eedbcaa741a1b6a8422dd63d264f923b1599489cc67`.

Artifact hashes, ZIP CRCs, CI checksum, package/version, exact packaged UI assets including changelog data, all non-signature APK entries, alignment and the retained preview signer passed verification. The signer matches Tasks 0.47.0, and the original seed state is unchanged. Both download paths contain the same signed APK. Settings Changelog contains 46 published releases including this update. Existing checklist headings, completion identities, native picker alignment and widget editor clearance remain covered.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
