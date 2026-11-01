# Validation — Task List Dark 0.2.0

[GitHub Actions run 41314538789](https://github.com/swettgm/task-list-android/actions/runs/41314538789) passed for source commit `fb6a6e88cc245ea4f1306c111e2ed256e0b86d6e`.

## Passed

- Android APK compilation and packaging.
- **51 Robolectric tests**, with zero failures or errors. Coverage includes persistent task state, damaged data preservation, widget filters and independent settings, canonical widget completion, clipboard integration, and popup save/cancel/validation. Popup tests confirm that saving does not launch MainActivity.
- Android lint: **zero errors**, 23 warnings. Warnings concern hardcoded English text, layout/accessibility guidance, compatibility attributes, and target-SDK guidance.
- **38 Chromium workflow checks** using the packaged HTML and a narrow mock of the Android bridge: date-only dark UI; original definitions/history; contexts; Do now toggling; one-time Do tomorrow; staged parent completion; save; clipboard exports; full backup; native-popup task reload; editor saves; persistent labels; and Upcoming grouping.
- Exact data comparison with the original HTML: all 102 task definitions and their fields, pairing maps, child history indexes, 304 completion records, 26 moves, and 13 rollover records retained. The original HTML contains all 78 PDF task IDs and all 217 PDF completion records, plus newer updates; those newer updates take precedence.
- All downloaded artifact ZIP digests matched GitHub’s reported SHA-256; archive CRC checks passed. Packaged HTML, bridge, and first-launch state match the checked source byte-for-byte.
- APK checksum matched CI. APK Signature Scheme v2 verified in CI and independently with the downloaded apksigner; exactly one signer. The retained private preview signing key’s certificate matches the APK.

## Download

[Task-List-Dark.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Task-List-Dark.apk)

- Package: `com.personal.tasklist.preview`
- Version: `0.2.0`, version code `2`
- Android: minimum API 26 (Android 8.0), target API 35
- Size: 105,024 bytes
- SHA-256: `84a9af5fc37fc354514153977ade86a1eae3b7a8380dde4132b9cb344752c61e`
- Signer certificate SHA-256: `c608cec387ed404701648b82b426777de0c2bc038d4fe44d3dd0237f3d0171c2`

The APK in `downloads/` is the exact verified CI APK. The previous Task List app uses a separate package and can stay installed; its local data is not imported automatically into this version.

## Device checks remaining

No physical Android phone or Android emulator was available. Chromium and Robolectric checks do not establish actual launcher/window behavior. On a phone, check initial installation, task completion and reopen, widget placement/resizing, independent widget settings, widget completion reflected in the app, and the widget’s floating + popup with the keyboard open. Try cancel and save, including a Tomorrow task and a selected context.

This is a debug build for personal testing. Store publication needs a release signing configuration.
