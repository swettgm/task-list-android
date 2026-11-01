# Task List — Android

A native Android app for personal tasks and checklists, with a customizable interactive home-screen widget. Requires Android 8.0 (API 26) or newer.

## Use the app

1. Open **Task List** and select **Add task**.
2. Enter a title and choose or type a checklist name. A due date is optional.
3. Tick the checkbox to complete a task. Turn on **Done** to see completed tasks and reopen them.
4. Tap a task or **Edit** to change it. Each task belongs to one checklist.

New installations start with an empty checklist. Task data and widget settings are stored locally in Android SharedPreferences. The app does not require an account or request internet access.

## Add and customize the widget

Touch and hold an empty area on your Android home screen, choose **Widgets**, and add **Task List checklist**. The app's **Home widget** action can also request placement on supported launchers.

Each widget independently supports:

- A custom title and choice of checklist, including all lists.
- All open tasks or tasks due today and overdue.
- Light or dark colors and background opacity.
- Resizing in both directions. Taller widgets show more rows; **Open app** shows the rest.

Tap a task's circle to complete it. Tapping the title opens the app, **+** adds a task to that widget's checklist, and the settings icon opens customization. Changes in the app update all widgets. Repeated taps on an already completed task leave it completed.

## Build

A signed debug APK is ready. [Download the APK artifact](https://github.com/swettgm/task-list-android/actions/runs/41267502984/artifacts/13068340692), unzip it, and install `app-debug.apk` on your Android phone. See [APK_BUILD.md](APK_BUILD.md) for the phone installation steps and rebuild options.

The [verified CI run](https://github.com/swettgm/task-list-android/actions/runs/41267502984) passed the Android build, all 14 Robolectric tests, and Android lint with zero errors. APK signature verification also passed. Physical device and launcher checks remain pending.

Open this folder in Android Studio, or use JDK 21 with Android SDK platform 35 and build tools 35.0.0:

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
```

Set `ANDROID_HOME` or create a local `local.properties` with your SDK path. A successful debug build produces `app/build/outputs/apk/debug/app-debug.apk`. Install it on an Android device, then add the widget through the launcher. Store publication requires a release signing configuration.

The included `gradlew` and Windows launcher are bootstrap scripts. On first use they download the pinned Gradle 8.11.1 distribution and verify its SHA-256 against Gradle's published checksum. An official Gradle Wrapper JAR is not bundled. Android Studio can also use the pinned distribution in `gradle/wrapper/gradle-wrapper.properties`.

Production code uses the Android framework and Java, with no production dependencies.

## Layout

- `MainActivity.java`: task lists and task editor.
- `TaskStore.java`: shared persistence and atomic completion.
- `TaskWidgetProvider.java`: interactive RemoteViews widget and resize handling.
- `WidgetConfigActivity.java`: independent widget configuration and preview.
- `app/src/test/java/`: fourteen passing Robolectric behavior tests.

## Validation

Current results and remaining checks are recorded in [VALIDATION.md](VALIDATION.md). Run `python3 tools/validate_source.py` for the offline Java grammar, XML/resource, and manifest checks. This requires a JDK and Python and does not compile the Android app. Device and launcher behavior still need verification on Android.
