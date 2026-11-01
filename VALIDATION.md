# Validation status

A signed debug APK was built successfully in [GitHub Actions run 41267502984](https://github.com/swettgm/task-list-android/actions/runs/41267502984), from source commit `bd3327d6f5d9901b49adab6bd00cda41042b0806` in [swettgm/task-list-android](https://github.com/swettgm/task-list-android).

## Passed in CI

- `:app:assembleDebug`: Android Java/resources compiled and the debug APK was packaged.
- `:app:testDebugUnitTest`: **14 tests passed**, with zero failures, errors, or skipped tests.
- `:app:lintDebug`: zero errors and 21 warnings. Remaining warnings include hardcoded English strings, text sizing, newer widget attributes, target-SDK guidance, and a compatibility constant.
- `apksigner verify`: signature verification passed using APK Signature Scheme v2, with one signer.
- Downloaded test/lint reports were independently inspected, and the downloaded APK's checksum matched the CI artifact.

The tests cover task creation/editing/persistence, list selection, date/title validation, damaged-data preservation, repeated completion, widget date/list filtering, per-widget preferences and deletion, invalid configuration, and the task editor through the app and widget shortcuts, including empty checklists.

## APK

- Package: `com.personal.tasklist`
- Version: `0.1.0` (version code `1`)
- Minimum Android API: `26` (Android 8.0); target API: `35`
- File size: `40,380` bytes
- SHA-256: `124cd6b93608d40b39fd9508916f13243f26cc85af5045b18e27ad10505b6d56`

The APK and verification files are in the [Task-List-Android-Debug-APK artifact](https://github.com/swettgm/task-list-android/actions/runs/41267502984/artifacts/13068340692). GitHub retains this artifact for 14 days. See [APK_BUILD.md](APK_BUILD.md) for installation and rebuild steps.

## Passed offline

- All seven production/test Java source files parse with the JDK Java 17 grammar: four production classes and three test classes.
- All 11 Android XML files parse, and all Java/XML resource references resolve to the project's 29 resource definitions.
- All manifest components have corresponding Java source files.
- The Unix Gradle bootstrap passes shell syntax validation. Its cache/argument forwarding and invalid-checksum rejection were exercised using local fixtures.

Repeat the offline source checks with:

```sh
python3 tools/validate_source.py
sh -n gradlew
```

## Device checks remaining

Robolectric tests run on the host JVM. No physical phone or Android emulator was used in this validation.

On an Android device, install the APK, create a task, and add the widget. Change its checklist, theme, and opacity; complete a task from the widget and confirm that the app updates. Check a second independently configured widget, live resizing, and the **+** shortcut after its selected checklist's final task is deleted.

This APK is a debug build for personal testing. A store release needs a release signing configuration.
