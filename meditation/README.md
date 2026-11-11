# Meditation

A separate lightweight Android app matching the navy-to-gray theme of Tasks and Route Manager.

- Choose 1–240 minutes. A separate 30-second preparation countdown comes first.
- At three, two and one seconds remaining: two short vibrations, then a long vibration. The start bell follows at zero.
- Quarter cues at 25%, 50% and 75%; the shared start/end bell plays at 100%. Ten minutes has quarter cues at 2:30, 5:00 and 7:30, then the end bell at 10:00.
- Choose independent start/end and quarter sounds: three bundled tones, silence or your own audio file. Preview sounds before starting.
- Enable and set vibration lengths separately for the first two preparation pulses, final preparation pulse, start, quarters and end. Preparation pulses allow 0.05–0.9 seconds; other cues allow 0.05–5 seconds.
- Pause, resume or end from the app or ongoing session notification. The foreground service uses a monotonic clock and a partial wake lock while counting so cues continue with the screen off. Pausing releases the wake lock; ending releases all session resources.
- Settings stay saved. No guidance, text-to-speech, account, widget, statistics or Internet permission.

The phone's media volume controls the sounds. Custom audio is chosen with Android's document picker and retained read access. Use short audio clips; each sound cue is capped at ten seconds.

The timer continues when the activity is closed or the screen is off. Android force-stop, device shutdown or process termination ends the session; it is not reconstructed after a reboot.

Build with Gradle 8.11.1, Java 21 and Android SDK 35. Run `gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. The independent Build Meditation workflow also runs Chromium UI checks. Keep all signing keys outside the repository.
