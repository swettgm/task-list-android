# Meditation 1.0.0 validation

Source: `895fcd6d1d80b96050a7c8f4074cdecf8c58ab7d`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43081262998). **12 Android tests, 24 Chromium workflow checks and zero lint errors passed.**

The monotonic session clock keeps its preparation and meditation deadlines separate. Clock tests cover two short pulses at preparation seconds 27 and 28, the long pulse at 29, the start bell at 30, exact quarter deadlines, a single end cue, pause/resume without repeated cues, delayed refresh without deadline drift or a burst of old cues, reset and duration bounds. Ten minutes has quarter cues 2:30, 5:00 and 7:30 into meditation, with its end bell at 10:00.

The user-started foreground service owns timing independently of the WebView and activity, with a partial wake lock only while counting or finishing the final cue. Pause releases the lock, and stop/destruction releases audio, vibration and wake resources. A notification offers Pause/Resume and End. Native service tests verify foreground notification and pause/resume/stop state; shell tests verify separate identity, no Internet permission and audio-only document-picker intent. Settings tests verify independent sounds, cue switches/lengths, persistence and atomic rejection of invalid settings.

The UI exposes a duration and collapsible Sounds & vibrations section, with three bundled tones, silence, per-role custom audio selection and preview. The same bell is used at start/end; the quarter sound is independent. Native document selection retains read access. Vibration controls independently cover the first two preparation pulses, last preparation pulse, start, quarter cues and end. Preparation lengths are bounded to 0.05–0.9 seconds to leave the next cue clear; other cues allow 0.05–5 seconds. There is no speech or guidance. Custom cues stop after ten seconds, and an inaccessible custom file falls back to the bundled soft bell with a message.

Chromium checks cover persistence, invalid/rejected saves, sound choices and previews, per-cue switches and lengths, preparation/meditation displays, pause/resume/end, reopening an active session, completion and 320/390/600 px layouts. Ready, options and active-timer screenshots were reviewed. Bundled WAV headers, channel/sample format and nonempty audio data were verified.

Signed APK: `Meditation-v1.0.0.apk`, 283499 bytes. Package `com.personal.meditation`, version 1.0.0 (1), Android 8.0+. ZIP integrity, exact packaged HTML, all non-signature CI entries, alignment, manifest and v2/v3 signatures passed. A separate retained private signing key supports future updates and is not in the repository. SHA-256 `49adffd345ad46994d43134c81ea61263efda8821330d716b96738d39763934f`.

Tests used Robolectric and Chromium; no physical phone, real document provider or screen-off device timing test was used. Force-stop, process termination and device reboot end an active session; settings remain saved.
