# Meditation

A separate lightweight Android app matching the navy-to-gray theme of Tasks and Route Manager.

- The main screen has only a centered timer and Start button, plus a small Settings icon. A small Settings button opens duration, sounds, vibration controls and the changelog on a separate screen.
- Choose 1–240 minutes in Settings. A separate 30-second preparation countdown comes first.
- At three, two and one seconds remaining: two short vibrations, then a long vibration. The start bell follows at zero.
- Quarter cues at 25%, 50% and 75%; the shared start/end bell plays at 100%. Ten minutes has quarter cues at 2:30, 5:00 and 7:30, then the end bell at 10:00.
- Choose independent start/end and quarter sounds: four meditation bell/bowl presets, silence or your own audio file. Preview sounds before starting.
- Enable and use sliders to set vibration lengths separately for the first two preparation pulses, final preparation pulse, start, quarters and end. Preparation pulses allow 0.05–0.9 seconds; other cues allow 0.05–5 seconds.
- Pause, resume or end from the app or ongoing session notification. The foreground service uses a monotonic clock and a partial wake lock while counting so cues continue with the screen off. Pausing releases the wake lock; ending releases all session resources.
- Settings stay saved. No guidance, text-to-speech, account, widget, statistics or Internet permission.

The phone's media volume controls the sounds. Custom audio is chosen with Android's document picker and retained read access. Use short audio clips; each sound cue is capped at ten seconds.

The timer continues when the activity is closed or the screen is off. Android force-stop, device shutdown or process termination ends the session; it is not reconstructed after a reboot.

Build with Gradle 8.11.1, Java 21 and Android SDK 35. Run `gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. The independent Build Meditation workflow also runs Chromium UI checks. Keep all signing keys outside the repository.

Version 1.0.1 uses live native timer snapshots and second-boundary display scheduling, with no cached 200 ms timer sampling. Audio presets are original synthesized bell/bowl sounds with inharmonic modes, gentle beating and longer decays; they are not field recordings.

Version 1.0.3 keeps timer geometry stable across session phases, uses equal session-button sizes and crossfades between timer modes without delaying cues. Reduced-motion preferences skip animation.

Vibration sliders play one preview at the chosen length when released. Previews stop when leaving Settings or the app and are disabled during a session.
