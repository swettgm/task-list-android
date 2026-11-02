# Tasks 0.11.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41387636667), source commit `806de6432ba3d8b6bf8063fc94451d69cbcaa965`.

59 Android tests, 223 browser checks and 26 calendar recurrence tests passed; lint reported zero errors.

Mobile Chromium checks enable a coarse touch pointer and the phone viewport. They reproduce the old 44px title-span minimum height, verify its removal, compare Today and Upcoming row dimensions at 320/390/600px and 15/20/25px text, and check the 2px optical checkbox adjustment. Real touch events verify left/right navigation and vertical-scroll isolation. Screenshots of both tabs were inspected.

A separate fixed layer keeps the gradient viewport-sized after scrolling a 2500px page. Transparent list surfaces and translucent controls expose it in both tabs. Settings and task editors retain the chosen gradient. Urgent markers are red; checkbox borders and checked backgrounds use the accent color. The completion hint is removed while completion undo remains tested. Tomorrow has a simple heading and later groups show full dates.

[Download Tasks-v0.11.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.11.0.apk).

Package `com.personal.tasklist.preview`, version 0.11.0, code 11; minimum API 26, target 35; size 124,236 bytes.

SHA-256: `137dc8f1b9a6212cfbfea5d7a5cf4092d16f44c615c3ba9d8515ae579f3a737c`.

Downloaded archive hashes, ZIP CRCs and CI APK checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. All non-signature entries match the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android emulator was used. Android WebView and launcher behavior remain device checks. This is a debug build for personal use.
