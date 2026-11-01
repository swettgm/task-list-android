# Validation — Task List Dark 0.5.0

[GitHub Actions run 41325730953](https://github.com/swettgm/task-list-android/actions/runs/41325730953) passed for source commit `6f9d7be47fd975ce1d30d16b572ea2322a4b0d4d`.

## Passed

- **55 Android Robolectric tests**, zero failures/errors; Android lint: zero errors. Widget tests verify 0%, 50%, and 100% background opacity with text/checkbox alpha unchanged. Configuration tests verify preserving existing opacity, selecting zero, activity recreation, and saving zero.
- **94 browser checks** using the actual packaged UI. New checks verify immediate saved/widget completion while the checked row stays visible for 450 ms, delayed parent/checklist disappearance, independent rapid completions, and cancelling disappearance by unchecking during the pause. Prior task, history, context, linking, quick-entry, Back, and hex-theme checks continue to pass.
- Geometry checks at 320, 360, 390, 412, 600, and 844 CSS pixels passed for Today, Upcoming, expanded checklists, hidden tasks, and the editor.
- The supplied 102 task definitions, 304 completion records, 26 moves, and 13 rollover records remain included; the package and saved-state identity remain unchanged.
- Artifact ZIP hashes and CRCs passed. The CI APK checksum matched its verification file, and task assets matched source byte-for-byte.

## Installable update

[Task-List-Dark-v0.5.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Task-List-Dark-v0.5.0.apk)

- Package: `com.personal.tasklist.preview`; version `0.5.0`, version code `5`.
- Android API minimum 26, target 35; size 103,631 bytes.
- SHA-256: `b3cf7ce24633e999fc52b8888df8fe9454c0734c5f29162cccd8bf621ff9e531`.
- Signer certificate SHA-256: `c608cec387ed404701648b82b426777de0c2bc038d4fe44d3dd0237f3d0171c2`.

The APK is signed with the retained preview key; its signing certificate matches 0.4.0. Signature and ZIP alignment checks passed. All compiled code/resources and task assets match the tested CI APK. Install it over Task List Dark to retain saved tasks and settings.

## Device checks remaining

No physical Android device or emulator was used. Browser/Robolectric checks do not establish actual launcher, keyboard, or gesture behavior. Check completion timing and the fully transparent widget against the phone wallpaper on a device. This remains a debug build for personal use.
