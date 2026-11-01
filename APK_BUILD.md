# Install Task List Dark

Requires Android 8.0 or newer. This is a debug build for personal use.

1. Download [Task-List-Dark-v0.4.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Task-List-Dark-v0.4.0.apk) on your phone.
2. Open the download. If Android asks, allow installation from the browser or Files app opening the APK.
3. Open **Task List Dark**. Your supplied tasks and saved history are included on the first launch. Use the corner **+** for Quick add and **Details** for the full editor. Custom hex colors are in **Settings ⚙**.
4. To add the checklist widget, use **Settings ⚙ → Add home-screen widget** in the app, or touch and hold an empty area on the home screen and choose **Widgets → Task List Dark checklist**, and place it. Use its settings icon to change the context filter, title, colors, and transparency.
5. Tap the widget’s **+** to add a task in a small floating form over the home screen. Saving closes the form. Ticking a widget task also saves it in the full task list.

Version 0.4 updates the existing Task List Dark installation in place, retaining its tasks and saved history. The original Task List app can also remain installed. Keep the old app if you have added tasks there; its data is separate. This version contains the supplied HTML/PDF history, rather than importing changes made inside the older Android app.

## Build again

The [Build Android APK workflow](https://github.com/swettgm/task-list-android/actions/workflows/build-apk.yml) runs automatically for app/build changes and can be run manually from GitHub Actions. It produces **Task-List-Android-Debug-APK**, plus test/lint/browser reports. Artifacts expire after 14 days; the APK committed under `downloads/` remains available.

CI compiles against SDK 35 with JDK 21 and Gradle 8.11.1. It runs Android behavior tests, lint, Chromium workflow checks, and signature verification. It does not run a phone or Android emulator.

Local build:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The output is `app/build/outputs/apk/debug/app-debug.apk`. A fresh CI runner creates a fresh debug signing key; use the retained original preview key when preparing an installable update for the published APK. The CI key is encrypted to the public certificate in `.github/`; the private decryption key and retained signing key stay outside this repository.

See [VALIDATION.md](VALIDATION.md) for the verified build and APK checksum.
