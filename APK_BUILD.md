# Get an installable Android APK

The app source is ready for a build. **An APK has not been produced or tested yet.** The workflow below has been prepared but has not run remotely. It needs no repository secrets and publishes nothing.

## Build in your browser with GitHub

1. Create a private GitHub repository and upload the **contents of this project folder** to its root. `app/`, `gradle/`, `build.gradle`, and `settings.gradle` must be at the repository root.
2. Include `.github/workflows/build-apk.yml`. If your upload skips the hidden `.github` folder, use **Add file → Create new file**, enter `.github/workflows/build-apk.yml`, paste the supplied workflow, and commit it to the default branch.
3. Open **Actions → Build Android APK → Run workflow → Run workflow**. The build runs only when you start it manually.
4. After it succeeds, open the run and download **Task-List-Android-Debug-APK** under **Artifacts**. Unzip it to obtain `app-debug.apk`, its SHA-256 checksum, and signature-verification results. Test and lint reports appear as a separate artifact, including when the build fails.
5. Transfer `app-debug.apk` to your Android phone and open it to install. Android may ask you to allow installs from the app opening the file. This is a debug build for trying the app; it is not a Play Store release.

GitHub's runner supplies Java and the Android SDK. The workflow installs SDK 35/build-tools 35.0.0, builds the APK, runs the included unit tests and Android lint, and verifies the APK signature. It does not run an Android emulator or verify launcher/widget behavior on a device.

## Build with Android Studio instead

Open this folder in Android Studio, select a complete JDK 21 for Gradle, and install Android SDK Platform 35 and Build-Tools 35.0.0 through **SDK Manager**. Let Android Studio set `local.properties` to your computer's existing SDK path; replace the supplied `/workspace/android-sdk` path if present. Sync the project and use the APK build action under **Build → Generate App Bundles or APKs → Generate APKs** (wording varies by Android Studio version). The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

For command-line builds with an existing SDK/JDK, run `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` on macOS/Linux or `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` on Windows. These bootstrap launchers download and checksum-verify Gradle 8.11.1 on first use; a Gradle wrapper JAR is not bundled.
