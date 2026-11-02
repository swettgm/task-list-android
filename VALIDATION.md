# Tasks 0.22.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41473854807), source commit `5314c15048084ee4502d822b1d0c07f89bcb10d8`.

87 Android tests, 333 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Swipe panels now contain their top margins in a consistent layout before, during and after the horizontal transition. The clipping container keeps the same formatting behavior at rest and in motion. Browser checks compare the incoming panel's starting and middle vertical offsets with its final offset, and the outgoing panel's resting offset with its offsets during movement, in both swipe directions. Existing horizontal motion, overflow, reduced-motion and equal-row-layout checks remain passing.

The widget plus is aligned to the right edge in XML, with zero trailing content padding and its 48dp tap area retained. A fresh widget layout resource causes installed widgets to receive the updated layout. Native tests inflate and measure the updated widget, checking the plus bounds against the outer width, end gravity, tap width, transparent background and add-task routing.

Upcoming now shows every future daily occurrence even when that task is pending on Today. Each of the next fourteen dates retains its own task occurrence and completion key. Checks cover all fourteen daily occurrences, unique future dates beginning tomorrow, continued exclusion of nonrecurring Today work, and future daily completion leaving today's task unchecked.

[Download Tasks-v0.22.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.22.0.apk).

Package `com.personal.tasklist.preview`, version 0.22.0, code 22; minimum API 26, target 35; size 174,027 bytes.

SHA-256: `d36a0f81057b49ef94d2c48e775170f0faa572b5cec9f5b4d08df251af1b8a98`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Android policies can delay background updates and notifications. These tests do not guarantee against every software, storage or device failure.
