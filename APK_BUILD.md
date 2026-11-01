# Install or rebuild Task List

A signed debug APK is ready from the [successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41267502984). The Android build, all 14 Robolectric tests, lint, and APK signature verification passed. Device and launcher checks remain pending.

## Install on your Android phone

1. Open the [Task-List-Android-Debug-APK download](https://github.com/swettgm/task-list-android/actions/runs/41267502984/artifacts/13068340692). Sign in to GitHub if asked.
2. Download the artifact ZIP and extract it with your phone's Files app. It contains `app-debug.apk`, `app-debug.apk.sha256`, and `signature-verification.txt`.
3. Open `app-debug.apk` to install **Task List**. If Android asks, allow installation from the app opening the APK.
4. Open Task List, add a task, then touch and hold an empty home-screen area. Choose **Widgets → Task List checklist**, place the widget, and customize it using its settings icon.

Requires Android 8.0 or newer. This is a debug build for personal testing.

The verified APK's SHA-256 is:

```text
124cd6b93608d40b39fd9508916f13243f26cc85af5045b18e27ad10505b6d56
```

GitHub retains the artifact for 14 days. The [source repository](https://github.com/swettgm/task-list-android) can produce a fresh build afterward.

## Rebuild with GitHub Actions

Open [Actions → Build Android APK](https://github.com/swettgm/task-list-android/actions/workflows/build-apk.yml) and select **Run workflow**. The workflow also runs automatically when relevant app, build, or workflow source files are pushed.

After the run succeeds, open it and download **Task-List-Android-Debug-APK** under **Artifacts**. Test and lint reports are available separately as **Android-test-and-lint-reports**.

GitHub's runner supplies Java and the Android SDK. The workflow installs SDK 35/build-tools 35.0.0, compiles and packages the APK, runs unit tests and Android lint, and verifies the APK signature. It does not run an Android emulator or verify launcher behavior on a phone.

## Build with Android Studio or the command line

Open this project in Android Studio, select JDK 21 for Gradle, and install Android SDK Platform 35 and Build-Tools 35.0.0 through **SDK Manager**. Let Android Studio create `local.properties` with your SDK path, or set `ANDROID_HOME` when using the command line.

On macOS/Linux:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

On Windows:

```bat
gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

These bootstrap launchers download and checksum-verify Gradle 8.11.1 on first use; a Gradle Wrapper JAR is not bundled. The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Store publication requires a release signing configuration.

See [VALIDATION.md](VALIDATION.md) for the verified source commit, APK metadata, and remaining device checks.
