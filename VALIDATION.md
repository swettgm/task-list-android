# Tasks 0.20.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41458489773), source commit `7900df7748fcbb9be450595f018f9fcfabd9ff4f`.

87 Android tests, 322 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

The widget header plus has a transparent background without an outline, retaining its existing tap area and add-task action. Widget tests check the transparent background and routing.

Widget Quick Add uses compact 40dp fields and 42dp buttons, with 8dp gaps between each row, the two side-by-side menus and the action buttons. Title and menu text use 16sp. Its window width remains 92% of the screen, with the existing outer padding, gradient and rounded panel. Native layout checks measure the actual field heights and horizontal/vertical gaps, then compare total height with the previous layout at the same width and font scale to verify that the popup does not grow. Saving, validation, cancellation and retained-state checks remain passing.

[Download Tasks-v0.20.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.20.0.apk).

Package `com.personal.tasklist.preview`, version 0.20.0, code 20; minimum API 26, target 35; size 173,949 bytes.

SHA-256: `2c43d683b41865e8127bfa76356c451a1fe3cbfe1ea040937e62813e34da58f1`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Android policies can delay background updates and notifications. These tests do not guarantee against every software, storage or device failure.
