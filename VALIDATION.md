# Validation status

This is a source project. An APK has **not** been compiled in this workspace.

## Passed offline

- All seven production/test Java source files parse with the JDK Java 17 grammar: four production classes and three test classes. This is syntax validation, not Android type checking or compilation.
- All 10 Android XML files parse, and all Java/XML resource references resolve to the project's 28 resource definitions.
- All manifest components have corresponding Java source files.
- The Unix Gradle bootstrap passes shell syntax validation. Its cache/argument forwarding and rejection of an invalid checksum were exercised using local fixtures, without downloading or executing a real Gradle distribution.
- Manual review corrected the widget shortcut when its selected checklist is empty, handled unreadable task storage before opening the editor, and made completion idempotent.

Run the repeatable offline checks:

```sh
python3 tools/validate_source.py
sh -n gradlew
```

## Prepared, not executed

Fourteen Robolectric tests cover task creation/editing/persistence, list selection, date/title validation, damaged-data preservation, repeated completion, widget date/list filtering, per-widget preferences and deletion, invalid configuration, and the task editor through both the app and widget shortcuts, including empty checklists.

Android SDK and Gradle dependencies are unavailable in this execution environment. The networking request interface was unavailable, so downloads could not proceed. Consequently these checks have **not** run:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

There is no emulator or connected Android device available in this workspace. Widget placement, live resizing, configuration, and interactions must still be checked on Android. No passing Android build, runtime test, or device test is claimed.

See [APK_BUILD.md](APK_BUILD.md) for the prepared alternative build options and their current status.

## Finish on a machine with Android tools

1. Open the project in Android Studio and install SDK platform 35/build tools 35.0.0, or point `ANDROID_HOME` to an existing SDK.
2. Run the Gradle command above. Test dependencies are separate from production code; the app itself uses only the Android framework.
3. Install the resulting `app/build/outputs/apk/debug/app-debug.apk`.
4. Create a task, add the widget, change its checklist/theme/opacity, complete a task from the widget, and confirm it is completed in the app. Check a second independently configured widget.
5. Delete a checklist's final task and verify the widget's **+** action still creates tasks in that checklist.
