# Tasks 0.17.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41448519680), source commit `b40bad6e18aec2a95ee14fcdf60f6119636af9bc`.

83 Android tests, 291 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Edit task now focuses the dialog rather than a text field. New task and Quick add retain title focus for immediate entry; intentionally focusing existing fields remains supported. Browser checks cover new-task focus, existing-task dialog focus and deliberate field focus.

The widget has a top-right plus, a tappable Today title, no gear button and no footer. Its existing reconfigurable Android widget metadata provides launcher long-press configuration. Priority sections are Highest, High, Medium, Low, Lowest and No priority, with small muted labels and low-alpha 1dp dividers. The collection exposes two view types with stable identities. Tests verify priority ordering and subtle colors, add/title routing, absence of the removed controls, scrolling, Today-only filtering and persistent completion.

The original Guild crest is enlarged another small step (web 1.4em to 1.55em, native pickers 20sp to 22sp, widget header 16dp to 18dp). Widget outer padding is zero, with 4dp side and 2dp vertical internal margins. A distinct compact layout ID refreshes installed widget layouts. The launcher may still apply its own outer padding.

[Download Tasks-v0.17.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.17.0.apk).

Package `com.personal.tasklist.preview`, version 0.17.0, code 17; minimum API 26, target 35; size 165,594 bytes.

SHA-256: `276c4ade7ca135f422c1466c1d6fcd836ba38a011858dafb8acf470f6e1a19b8`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard or Android launcher was used. Keyboard checks cover DOM focus; native widget behavior was tested with Robolectric and view inflation. Android policies can delay background updates and notifications; force-stop blocks alarms until reopening. These tests do not guarantee against every software, storage or device failure.
