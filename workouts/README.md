# Workouts

[Download Workouts 1.0.0](https://github.com/swettgm/task-list-android/raw/main/downloads/Workouts-v1.0.0.apk) for Android 8 or later.

An offline workout logger in the same blue-grey gradient and white-control theme as Tasks, RM and Meditation. The initial routine and history are empty.

- Create and rename routines, add exercises, and hold an exercise to reorder it.
- Set weight, lb/kg, rest time, and separate rep targets for each set.
- Start a workout and tap a circle to log its target reps. Tap a logged set to correct reps or weight.
- Working-weight edits preserve logged sets and update future sets. Extra sets can be added to the active workout.
- Unfinished sessions survive reopening. Finish saves an independent history record with actual sets, weights, notes and duration.
- Rest uses a saved deadline, with skip and +30 seconds controls. Notifications start disabled. Android precise-alarm access is optional and enables precise background rest alerts.
- The compact Spotify player shows the active track and supports previous, play/pause and next. Tap Connect and enable Workouts under Android notification access, then start Spotify. Only Spotify media sessions are used. Notification contents are never read or stored. No Spotify OAuth credentials are required.
- Settings include default units and rest, optional alerts, a stepped vibration-length slider with release preview, and background/text colors.
- Today and History support swiping with delayed tab selection and haptics. Dialogs dismiss on outside tap and Android Back.
- Adaptive icons use the full visible app gradient and padded white artwork.

The app stores workouts privately on the device. It has no cloud account or desktop client.

## Validation

The signed release passed 21 Android tests, Android lint and 40 browser checks covering set logging, independent history, interrupted sessions, save failures, exercise dragging, Spotify controls, narrow screens and editor geometry. Media controls were tested with Android media-session fixtures. No physical phone or live Spotify playback was tested.

## Build

From the parent repository, run the existing Gradle wrapper with this directory as the project.

```sh
./gradlew --project-dir workouts :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
cd workouts
node tools/browser-smoke.mjs
```

Set WORKOUT_DEBUG_KEYSTORE to the retained private signing key when preparing an update. Never publish the key.
