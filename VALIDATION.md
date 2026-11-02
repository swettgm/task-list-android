# Tasks 0.32.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41517808566), source commit `b62ddacc8256be42219d1eb6250d962f967c602e`.

102 Android tests, 459 browser checks and 26 calendar recurrence checks passed; lint reported zero errors. Updated progress and subtask screenshots were visually reviewed.

An inline In progress badge now follows the task title with a 4px gap. Its title label sizes to its contents instead of filling spare row width, while the badge and checkbox retain vertical centering. The existing fit check still moves the badge below long titles rather than compressing them.

Subtask disclosure summaries use separate decorative arrow and count elements in a centered flex row. The arrow rotates on expansion and is hidden from accessibility text; native summary keyboard/click behavior remains intact. Native disclosure markers are hidden to prevent duplication. The faint left rule is removed and counts read 0/4 steps without spaces around the slash.

Browser geometry checks verify the exact title-to-badge gap, vertical centers, long-title fallback, and centered summary arrow with no border in both collapsed and expanded states. The full existing task, widget, history, reminder and layout checks remain enabled. Screenshots capture compact subtask summaries and inline progress spacing.

[Download Tasks-v0.32.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.32.0.apk).

Package `com.personal.tasklist.preview`, version 0.32.0, code 32; minimum API 26, target 35; size 207,837 bytes.

SHA-256: `f683accc2910cf4deedd4ef84cc4540f050990eed7d0ea8e21f1bf8a596309f0`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation.
