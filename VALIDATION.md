# Validation — Task List Dark 0.3.0

[GitHub Actions run 41319176457](https://github.com/swettgm/task-list-android/actions/runs/41319176457) passed for source commit `ebb9e78f9b0e632bff6350a5ee950381277461d1`.

## Behavior and layout

- **51 Android Robolectric tests passed**, zero failures/errors; Android lint passed with zero errors.
- **44 browser behavior checks passed** using the actual packaged document: task/history retention; date-only dark UI; context filters; Do now and Do tomorrow; immediate parent and step completion; clearing the active marker; completion/reopen across reloads; Show hidden; native popup additions; gear editing; manual linking/unlinking in both directions; absence of AI suggestions, old suggested links, and copy/backup/save controls; and visible recovery from a failed persistence attempt.
- Real geometry checks at **320, 360, 390, 412, 600, and 844 CSS pixels** found no horizontal overflow in Today and Upcoming with checklists, overdue rows, and hidden completed tasks expanded. The task editor also fit at every width.
- The supplied 102 original task definitions, 304 saved completion records, 26 moves, and 13 rollover records remain included. Old suggested task pairings are disabled; only manually chosen links are displayed. The saved-state identity is unchanged, so an installed user's state is retained.
- Downloaded artifact ZIP SHA-256 hashes and CRCs passed. CI APK assets exactly matched source.

## Installable update

[Task-List-Dark-v0.3.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Task-List-Dark-v0.3.0.apk)

- Package: `com.personal.tasklist.preview`; version `0.3.0`, version code `3`.
- Minimum Android API 26 (Android 8.0); target API 35.
- Size: 99,535 bytes.
- SHA-256: `e17f27f93006b7cb25b5edbbd11b369a519e921f931cd372b3c03fbe2682aa7e`.
- Signer certificate SHA-256: `c608cec387ed404701648b82b426777de0c2bc038d4fe44d3dd0237f3d0171c2`.

The downloadable APK was re-signed using the retained 0.2.0 preview key. Signature verification and ZIP alignment passed, and its signing certificate matches the prior APK. Package/version metadata and every packaged task asset were independently checked. It can update the existing Task List Dark installation without uninstalling or clearing its saved tasks. The re-signing operation preserves the compiled code/resources and task assets from the checked CI APK.

## Device checks remaining

No physical Android device or Android emulator was used. Browser layout checks and Robolectric do not establish actual launcher/window behavior. On a phone, check the in-place update, checkbox persistence, screen fit with system font settings, widget synchronization, and the floating quick-add form with the keyboard open. This remains a debug build for personal use.
