# Validation — Task List Dark 0.4.0

[GitHub Actions run 41323491743](https://github.com/swettgm/task-list-android/actions/runs/41323491743) passed for source commit `2693da00619ba1bd2be2358215181786ad37843e`.

## Passed

- **53 Android Robolectric tests**, zero failures/errors; Android lint: zero errors. New coverage checks Android Back routing to the open task menu and safe native system-bar color updates from hex input.
- **84 browser behavior checks** using the actual packaged document. These cover all prior task completion/reopen/history/linking behaviors, the cleaned header and equal control heights, Settings-only widget placement, compact priority/repeat choices, every task field saving and repopulating, weekday/interval/weekend repeats, one-field Quick add, retaining the quick title when opening Details, Back dismissal without saving drafts, valid/invalid/short hex input, theme persistence, light backgrounds, reset colors, and native background updates.
- Real geometry checks at **320, 360, 390, 412, 600, and 844 CSS pixels** found no horizontal overflow in Today, Upcoming, expanded checklists, hidden completed tasks, or the full task editor.
- The supplied 102 original task definitions, 304 saved completion records, 26 moves, and 13 rollover records remain included. The saved-state identity and package remain unchanged.
- Artifact ZIP hashes/CRCs passed; the downloaded CI APK checksum matched its verification file, and packaged task assets match source byte-for-byte.

## Installable update

[Task-List-Dark-v0.4.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Task-List-Dark-v0.4.0.apk)

- Package: `com.personal.tasklist.preview`; version `0.4.0`, version code `4`.
- Minimum Android API 26 (Android 8.0); target API 35.
- Size: 103,631 bytes.
- SHA-256: `934defe2a19ef9710eae2d5ff667eb40e0833163c61fecd46b9c08aef97c117d`.
- Signer certificate SHA-256: `c608cec387ed404701648b82b426777de0c2bc038d4fe44d3dd0237f3d0171c2`.

The downloadable APK was re-signed with the retained preview key. Signature verification and ZIP alignment passed; its signing certificate matches version 0.3.0. All compiled code/resources and task assets match the tested CI APK. It updates the existing Task List Dark installation in place, retaining saved tasks. Its launcher icon uses a white background and black checklist lines.

## Device checks remaining

No physical Android device or Android emulator was used. Browser layout and Robolectric checks do not establish actual phone keyboard, launcher, or gesture behavior. Check the in-place update, system Back with the keyboard open, hex appearance with device font settings, quick entry, widget synchronization, and the floating home-screen popup on a phone. This remains a debug build for personal use.
