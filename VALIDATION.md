# Tasks 0.31.0 verification

[Successful CI checks](https://github.com/swettgm/task-list-android/actions/runs/41515882639), source commit `88193ac0d34eaea8a35fc2a19d278d241a8e8764`.

102 Android tests, 457 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Completed tasks now sort newest first within their existing completion-day sections. App and widget completions record durable per-occurrence timestamps, including parent and checklist keys. Strict ordering handles multiple completions in the same millisecond. Duplicate widget completion taps preserve the original time. Undo clears the timestamp, and recompletion receives a new one and returns to the top.

No task identities or historical completion dates are replaced. Old records without exact times retain their completion-day grouping and use saved completion-key order as a fallback; precise historical times cannot be reconstructed. Checklist completion uses the most recent parent/step timestamp. Other saves preserve these timestamps. Failed app completion saves restore the prior timestamp map alongside completion state.

Browser regressions complete Alpha then Zulu and verify Zulu is first despite title/priority, reload persistence, timestamp removal on undo, and moving Alpha to the top when completed again. Native regressions verify ordered widget timestamps and idempotent duplicate taps. Existing undo, history, checklist, native-state, reminder, widget and layout checks remain enabled. Reload checks use a stable HTTP fixture origin for their mock native storage.

[Download Tasks-v0.31.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.31.0.apk).

Package `com.personal.tasklist.preview`, version 0.31.0, code 31; minimum API 26, target 35; size 207,837 bytes.

SHA-256: `0d673589bce5c696f5106a01a2143256769a5a4f2014d0ba307041614d8a89c0`.

Artifact hashes, ZIP CRCs and CI checksum passed. Packaged UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation.
