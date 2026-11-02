# Tasks 0.16.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41444152194), source commit `f0acd5716d990c0540726e2a9968445d97130e99`.

81 Android tests, 288 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

The home widget now uses a native ListView and a protected RemoteViewsService. Its factory returns every selected open Today row, without the former 12-row cap. The header is always Today; task-count and checklist-name subtitles are removed. Each row keeps its Today label, including narrow widgets. The collection uses stable IDs and scoped fill-in click intents; completion stays persistent and idempotent. Context/checklist filters, colors, 0% opacity, quick add and the enlarged Guild crest remain available.

Tests cover 40 scrollable rows, excluding future/undated/completed rows, retaining completed records in storage, stable row identity after a completion, per-row Today labels, the fixed header and native list type, fresh snapshot flags, current-day recurring rows versus old daily history, old snapshot compatibility, and collection completion with stale-tap protection. Existing widget-opacity/configuration, notification, daily-edit recurrence, checklist, crest, gradient and mobile layout checks pass.

A distinct widget layout ID causes installed widget hosts to rebuild the old fixed-row layout when the update is applied. No physical Android launcher or device was used: collection behavior was tested with Robolectric and native view inflation.

[Download Tasks-v0.16.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.16.0.apk).

Package `com.personal.tasklist.preview`, version 0.16.0, code 16; minimum API 26, target 35; size 161,334 bytes.

SHA-256: `277c9e49191a0b003baa6f0d0b4478f78bda633fb252d55f1de49812071e2f9a`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

Android policies can delay notifications and widget refreshes; force-stop blocks alarms until reopening. Local data remains subject to device loss, uninstalling, clearing app data, storage failure and software defects; tests are not a 100% reliability guarantee.
