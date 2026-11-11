# Meditation 1.0.1 validation

Source: `cabf049cf0fcaae117e0acf47689690652ad1aad`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43121212429). **15 Android tests, 33 Chromium checks and zero lint errors passed.**

Vibration lengths use sliders with live seconds output, keyboard adjustment, persisted milliseconds and independent cue switches. Countdown sliders cap at 0.9 seconds to leave the next cue clear. Existing saved settings remain compatible.

The display reads a live, coherent native monotonic-clock snapshot and refreshes at second boundaries. Native cue scheduling targets deadlines instead of accumulating polling delays. Tests verify reads between service ticks, exact cue deadlines, delayed UI refresh catching up, even displayed seconds and pause/resume behavior. Existing quarter/start/end timing remains unchanged.

Four original synthesized presets are bundled: Mindfulness bell, Singing bowl, Tingsha bells and Temple gong. Inharmonic partials, gentle beating, soft attacks and longer decays replace the previous simpler tones. WAV format, lengths, nonempty samples and unclipped peaks were checked. Existing preset IDs and custom audio selection remain compatible. Launcher artwork is navy on white; the app theme and monochrome notification icon remain unchanged. Options and timer screenshots were reviewed.

Signed APK: `Meditation-v1.0.1.apk`, 951216 bytes. Package `com.personal.meditation`, version 1.0.1 (2), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches 1.0.0. SHA-256 `6be41c4dda0f885370862afcb36ac6a4634bab5cb7e66c45ef61137c51b29c3a`.

Tests used Robolectric and Chromium; no physical phone or screen-off timing measurement was used. Force-stop, process termination and reboot still end an active session. There is no guided voice or text-to-speech option.

# Meditation 1.0.0 validation

Source: `895fcd6d1d80b96050a7c8f4074cdecf8c58ab7d`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43081262998). **12 Android tests, 24 Chromium workflow checks and zero lint errors passed.**

The monotonic session clock keeps its preparation and meditation deadlines separate. Clock tests cover two short pulses at preparation seconds 27 and 28, the long pulse at 29, the start bell at 30, exact quarter deadlines, a single end cue, pause/resume without repeated cues, delayed refresh without deadline drift or a burst of old cues, reset and duration bounds. Ten minutes has quarter cues 2:30, 5:00 and 7:30 into meditation, with its end bell at 10:00.

The user-started foreground service owns timing independently of the WebView and activity, with a partial wake lock only while counting or finishing the final cue. Pause releases the lock, and stop/destruction releases audio, vibration and wake resources. A notification offers Pause/Resume and End. Native service tests verify foreground notification and pause/resume/stop state; shell tests verify separate identity, no Internet permission and audio-only document-picker intent. Settings tests verify independent sounds, cue switches/lengths, persistence and atomic rejection of invalid settings.

The UI exposes a duration and collapsible Sounds & vibrations section, with three bundled tones, silence, per-role custom audio selection and preview. The same bell is used at start/end; the quarter sound is independent. Native document selection retains read access. Vibration controls independently cover the first two preparation pulses, last preparation pulse, start, quarter cues and end. Preparation lengths are bounded to 0.05–0.9 seconds to leave the next cue clear; other cues allow 0.05–5 seconds. There is no speech or guidance. Custom cues stop after ten seconds, and an inaccessible custom file falls back to the bundled soft bell with a message.

Chromium checks cover persistence, invalid/rejected saves, sound choices and previews, per-cue switches and lengths, preparation/meditation displays, pause/resume/end, reopening an active session, completion and 320/390/600 px layouts. Ready, options and active-timer screenshots were reviewed. Bundled WAV headers, channel/sample format and nonempty audio data were verified.

Signed APK: `Meditation-v1.0.0.apk`, 283499 bytes. Package `com.personal.meditation`, version 1.0.0 (1), Android 8.0+. ZIP integrity, exact packaged HTML, all non-signature CI entries, alignment, manifest and v2/v3 signatures passed. A separate retained private signing key supports future updates and is not in the repository. SHA-256 `49adffd345ad46994d43134c81ea61263efda8821330d716b96738d39763934f`.

Tests used Robolectric and Chromium; no physical phone, real document provider or screen-off device timing test was used. Force-stop, process termination and device reboot end an active session; settings remain saved.
