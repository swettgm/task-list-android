# Exercise

[Download Exercise 1.0.3](https://github.com/swettgm/task-list-android/raw/main/downloads/Exercise-v1.0.3.apk) for Android 8 or later. This updates Workouts and keeps its saved routines and history.

An offline workout logger in the blue-grey gradient and white-control theme shared with Tasks, RM and Meditation. New installations start with an empty routine and history.

- Create and rename routines, add exercises, and hold an exercise to reorder it.
- Set weight, lb/kg, rest time, and separate rep targets for each set.
- Tap a circle to log target reps; each further tap reduces reps down to 0, then clears the set, then returns to target reps. Each saved change restarts the full rest timer.
- Use a + target such as 5+ for an AMRAP set. Hold its circle and drag up/down to select actual reps with haptics, then release to save and start rest. The temporary selector disappears on release; a cancelled gesture changes nothing. Taps reduce recorded AMRAP reps in the same cycle.
- Working-weight edits preserve logged sets and update future sets. Extra sets can be added to the active workout.
- Unfinished sessions survive reopening. Finish saves independent history with actual sets, weights, notes and duration.
- Routine and exercise fields explicitly focus on touch and request the Android keyboard. Tap the workout name, then Add routine to create another workout.
- Settings is a full screen with a borderless gear and visible Save/Cancel actions. Add exercise is an inline button.
- A draggable in-app rest timer has a shrinking progress ring, saved position, +30 seconds and Skip controls. Turn it off with Floating rest timer in Settings.
- Enable Rest notifications in Settings for an ongoing countdown notification with progress, +30 seconds and Skip actions. Android notification permission is required. Precise-alarm access improves completion timing while the device sleeps.
- The compact Spotify bar fits around the album art and has solid controls, bold text and haptics. Tap Connect and enable Exercise under Android notification access, then start Spotify. Tapping the track opens Spotify. Full Spotify playlist browsing is omitted.
- Only Spotify media sessions are used. Notification contents are never read or stored. No Spotify OAuth credentials are required.
- Appearance, default units/rest and stepped vibration-length controls are available in Settings.
- Today and History support swiping with delayed tab selection and haptics. Dialogs dismiss on outside tap and Android Back.
- Adaptive icons use the full visible app gradient and padded, bold white artwork.

The app stores workouts privately on the device. It has no cloud account or desktop client.

## Validation

The signed release passed 33 Android tests, Android lint and 120 browser/touch checks. These cover AMRAP persistence, real touch rep cycling/selection/cancellation, rest restart deadlines, worker-thread native timer scheduling, storage revisions, history, set logging, dragging, Spotify media commands/artwork, ongoing rest notifications/actions, floating timer position/ring, separate Settings screen, narrow-screen/editor geometry, actual touch typing in every editor field, and real touch swipes across exercise/history cards. No physical phone or live Spotify playback was tested.

## Build

From the parent repository, run the Gradle wrapper with this directory as the project.

```sh
./gradlew --project-dir workouts :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
cd workouts
node tools/browser-smoke.mjs
node tools/touch-input-smoke.mjs
node tools/set-reps-smoke.mjs
```

Set WORKOUT_DEBUG_KEYSTORE to the retained private signing key for updates. Never publish the key.
