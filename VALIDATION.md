# Tasks 0.29.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41509436751), source commit `44ef1c779099bfbdc5c268c5fa1a90dec69c554c`.

97 Android tests, 451 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Widget title, checkbox and progress badge now center independently of text baselines. Extra font padding is removed and a fresh row layout updates existing widget views. Native geometry checks measure badge and title text centers for single-line and wrapped titles at three widths. The green rounded badge, tap-to-stop behavior, priority restoration and two-second completion cancellation remain intact.

App task rows place the progress badge beside the title only when the complete title and badge fit. Long titles keep the badge below. The title and checkbox retain the same vertical center. A shared resize observer rechecks active rows on layout changes and tab visibility changes; removed rows are unobserved. Font loading and window resizing also recheck placement. Browser checks verify inline badge centering at two widths, fallback for long titles and unchanged checkbox alignment, alongside the full existing workflow suite.

[Download Tasks-v0.29.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.29.0.apk).

Package `com.personal.tasklist.preview`, version 0.29.0, code 29; minimum API 26, target 35; size 203,741 bytes.

SHA-256: `2bbe0209aef0fdeb4628af85fd4bddfba5162eea2bed2a3df8634760728b29e3`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Notification scheduling is unchanged by this alignment update.
