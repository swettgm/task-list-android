# Tasks 0.33.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41519283721), source commit `45bbe895e2da972d078ccaf9264d3270cff79eb8`.

102 Android tests, 463 browser checks and 26 calendar recurrence checks passed; lint reported zero errors. The manual-link screenshot was visually reviewed.

The link dropdown previously required a separate Link task press after choosing a task. Selecting a peer now adds it immediately to the editor’s selected chips; the redundant button is removed. Save task persists the definition and reciprocal manualLinks in the same state write. New/existing task writes update the engine from persisted state, and save errors remain visible in the editor.

Linked open task rows show a small 12px muted line such as 🔗 Daily link B underneath their metadata. Rendering uses text nodes, preserving emoji and arbitrary task titles safely. Existing manually saved links continue to render; original automatic suggestions remain disabled. Task identity links remain bidirectional and apply to future recurring instances. The picker still excludes completed tasks, duplicates and self-links. Unlink and Cancel retain their expected behavior.

Browser regressions verify selecting a peer without another button, immediate chips, cancellation without state changes, atomic reciprocal-link writes, immediate chain/title rendering, reload persistence, next-day recurrence and unlinking. Creation and editing paths are both covered. The link screenshot is visually reviewed. The full existing native and browser checks remain enabled.

[Download Tasks-v0.33.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.33.0.apk).

Package `com.personal.tasklist.preview`, version 0.33.0, code 33; minimum API 26, target 35; size 207,837 bytes.

SHA-256: `acef26490573825e0128cacd07fc9f1832433479c6de4a853c544d94d1f4864b`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation.
