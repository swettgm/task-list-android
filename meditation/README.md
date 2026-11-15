# Meditation

A separate lightweight Android app matching the navy-to-gray theme of Tasks and Route Manager.

- The main screen has only a centered timer and Start button, plus a small Settings icon. A small Settings button opens duration, intermediate bells, sounds and vibration controls on a separate screen.
- Choose 1–240 minutes in Settings. A separate preparation countdown comes first; choose 0–300 seconds in Settings, defaulting to 30. Zero starts meditation immediately.
- At three, two and one seconds remaining: two short vibrations, then a long vibration. The start bell follows at zero.
- Choose Off or 1–5 intermediate bells, evenly spaced during the session. The default is three, preserving the previous timing. Eight minutes with three intermediate bells cues at 2:00, 4:00 and 6:00; the end cue remains at 8:00. Off disables both intermediate sound and vibration.
- Choose independent start/end and intermediate sounds: five meditation bell/bowl presets, including Zen tone, silence or your own audio file. Preview sounds before starting.
- Enable and use sliders to set vibration lengths separately for the first two preparation pulses, final preparation pulse, start, intermediate cues and end. Preparation pulses allow 0.05–0.9 seconds; other cues allow 0.05–5 seconds. Vibration sliders snap to 0.05-second steps.
- Pause, resume or end from the app or ongoing session notification. The foreground service uses a monotonic clock and a partial wake lock while counting so cues continue with the screen off. Pausing releases the wake lock; ending releases all session resources.
- Settings stay saved. No guidance, text-to-speech, account, widget, statistics or Internet permission.

The phone's media volume controls the sounds. Custom audio is chosen with Android's document picker and retained read access. Built-in sounds ring for about twenty seconds and fade smoothly to silence. Their original attack and pitch are retained while the ringing tail is extended. User-selected custom audio retains its ten-second playback limit.

The timer continues when the activity is closed or the screen is off. Android force-stop, device shutdown or process termination ends the session; it is not reconstructed after a reboot.

Build with Gradle 8.11.1, Java 21 and Android SDK 35. Run `gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. The independent Build Meditation workflow also runs Chromium UI checks. Keep all signing keys outside the repository.

Version 1.0.1 uses live native timer snapshots and second-boundary display scheduling, with no cached 200 ms timer sampling. The four original audio presets are synthesized bell/bowl sounds with inharmonic modes, gentle beating and longer decays; they are not field recordings.

Version 1.0.3 keeps timer geometry stable across session phases, uses equal session-button sizes and crossfades between timer modes without delaying cues. Reduced-motion preferences skip animation.

Vibration sliders play one preview at the chosen length when released. Previews stop when leaving Settings or the app and are disabled during a session.

Zen tone is an extended/faded version of the supplied `alex_jauk-zen-tone-mid-high-202557.mp3`. Original sources and processing details are in `audio-source/`; `tools/extend-sounds.py` rebuilds the bundled Ogg Vorbis audio using FFmpeg with Rubber Band. Start/end and intermediate choices remain independent, and session deadlines do not wait for audio playback.

Zen tone retains the original recording’s real-time bell strike, with only the quiet pre-strike noise silenced and a short 10ms entry fade. Its late ringing modes are extended without time stretching the attack. Singing bowl retains its natural attack with an added short, clean mallet strike and a phase-aligned extended tail. Both use lossless FLAC, last twenty seconds and fade to silence. Other presets are unchanged. Previously selected custom copies named `alex_jauk-zen-tone-mid-high-202557.mp3` (including numbered downloads) resolve to the bundled Zen tone; unrelated custom files retain their existing behavior.

At completion, the chosen end sound/vibration plays once and the home display returns to the configured duration with Start available. It never starts the next session automatically. For vibration-only intermediate cues, select Silent and enable Intermediate bells vibration. For sound-only cues, uncheck that vibration; both are also supported. Sound choices and vibration lengths remain saved when intermediate bells are Off.

Settings uses uniform centered dropdown chevrons, aligned preview buttons, centered checkbox ticks and slider rows. The in-app changelog is removed. The home layout and timer fades are unchanged. Distribution uses a non-debuggable release APK, signed locally with the retained Meditation key for in-place upgrades.

Appearance in Settings includes independent solid/gradient background and text/button accents. Each color opens a touch/keyboard color wheel with brightness and hex entry; Use color saves, Cancel and Back discard the picker draft. Colors persist privately, system bars follow the background, and Reset colors restores the original palette without changing the timer or cues. Default home-screen pixels and control dimensions remain unchanged.

Short countdowns only play the final pulses that fit: a two-second preparation has one short pulse and one long pulse, a one-second preparation has the long pulse, and zero skips preparation pulses entirely. Cue deadlines still use the monotonic clock.
