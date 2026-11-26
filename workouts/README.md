# Exercise

Each rest timer starts a little lower on the right side, clear of the dismissal zone. Dragging changes its position for that rest. Clearing a set cancels its rest timer. Drag the floating timer onto the X to hide it for the current set. The X appears when you drag the timer into the bottom third of the screen. Its drop zone expands and gives haptic feedback when you enter it. Use End timer in the rest notification to dismiss the current timer. The larger Spotify bar keeps the controls visible and opens Spotify when tapped while idle.

[Download Exercise 1.0.27](https://github.com/swettgm/task-list-android/raw/main/downloads/Exercise-v1.0.27.apk) for Android 8 or later. This updates Workouts and keeps its saved workouts and history.

An offline workout logger in the blue-grey gradient and white-control theme shared with Tasks, RM and Meditation. New installations start with an empty workout and history.

- Create and rename workouts, add exercises, and hold an exercise to reorder it.
- Set weight, lb/kg, rest time, and separate rep targets for each set.
- Tap a circle to log target reps; each further tap reduces reps through zero. One more tap clears the set, and the next tap returns to target reps. Drag below zero to clear a circle. Each saved change restarts the full rest timer.
- Use a + target such as 5+ for an AMRAP set. Hold any rep circle and drag up or down to select actual reps with haptics, then release to save and start rest. The circle displays the rep count as you drag. Regular sets cap reps at their target. AMRAP can go higher. Taps reduce recorded AMRAP reps in the same cycle.
- Working-weight edits preserve logged sets and update future sets. Extra sets can be added to the active workout.
- Unfinished sessions survive reopening. Finish saves independent history with actual sets, weights, notes and duration. History displays blank sets as zero reps and charts the heaviest completed weight per exercise.
- Routine and exercise fields explicitly focus on touch and request the Android keyboard. The Exercise header and Today and History tabs stay visible while the content scrolls. Today lists every workout as a card. Tap a card to see its exercises and Start workout, and only the active workout shows rep circles. The plus beside Settings adds workouts. Use each pencil to rename its workout or add, edit and reorder its exercises.
- Settings is a full screen with a borderless gear and visible Save/Cancel actions.
- Rotate workouts by selected weekdays or a recurring interval in Settings. The weekday schedule previews at least one full week of selected training days, repeating workouts as needed.
- Rest counts into negative time in red after the deadline. Countdown pulses can run for the last three or five seconds with an adjustable pulse length. Workout cues require a verified headphone route and Android audio focus. The countdown uses short arcade beeps and a longer, higher finish beep. Adjust Rest beep volume independently from music, or set it to 0% to mute the cues. The explicit Settings sound test may use the phone speaker. Bluetooth outputs with ambiguous names are skipped. Physical headphone routing is unverified.
- A draggable in-app rest timer has a shrinking progress ring, saved position, +30 seconds and Skip controls. Turn it off with Floating rest timer in Settings.
- Enable Show rest timer over other apps in Settings and grant Android overlay permission. One timer window stays in place inside and outside Exercise and remembers its position.
- Enable Rest notifications in Settings for a one-line countdown notification with progress. The timer popup has +30 seconds and Skip controls. Android notification permission is required. Precise-alarm access improves completion timing while the device sleeps.
- The compact Spotify bar fits around the album art and has solid controls, bold text and haptics. Tap Connect and enable Exercise under Android notification access, then start Spotify. Tapping the track opens Spotify. Swipe left or right on the bar to skip forward or back. The title and artist slide while artwork updates in place. Full Spotify playlist browsing is omitted.
- Only Spotify media sessions are used. Notification contents are never read or stored. No Spotify OAuth credentials are required.
- Appearance, default units/rest and stepped vibration-length controls are available in Settings.
- Today and History support swiping with delayed tab selection and haptics. Dialogs dismiss on outside tap and Android Back.
- Adaptive icons use the full visible app gradient and padded, bold white artwork.

The app stores workouts privately on the device. It has no cloud account or desktop client.

## Validation

The signed release passed 34 Android tests, Android lint and browser and touch checks. These cover AMRAP persistence, real touch rep cycling/selection/cancellation, rest restart deadlines, worker-thread native timer scheduling, storage revisions, history, set logging, dragging, Spotify media commands/artwork, ongoing rest notifications, floating timer position/ring, separate Settings screen, narrow-screen/editor geometry, actual touch typing in every editor field, and real touch swipes across exercise/history cards. No physical phone or live Spotify playback was tested.

## Build

From the parent repository, run the Gradle wrapper with this directory as the project.

```sh
./gradlew --project-dir workouts :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
cd workouts
node tools/browser-smoke.mjs
node tools/touch-input-smoke.mjs
node tools/set-reps-smoke.mjs
```

For an update signed with the existing certificate, set WORKOUT_DEBUG_KEYSTORE and supply WORKOUT_KEYSTORE_PASSWORD, WORKOUT_KEY_ALIAS and WORKOUT_KEY_PASSWORD locally.
