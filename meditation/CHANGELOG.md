## 1.0.13

- Rebuilt Mindfulness bell, Tingsha bells and Temple gong with clean bell strikes, smooth long decays and lossless audio. Approved Zen tone and Singing bowl remain byte-for-byte unchanged.
- Replaced the tinny rain with a softer, warmer stereo rain loop. Brown and white noise are unchanged.

## 1.0.12

- Removed the Settings gear border, including under gradient accents, while preserving its 44px tap target.
- Reduced the Settings heading to 22px.
- Confirmed the interval slider caps at five bells, with spacing recalculated from duration so the maximum is one bell at each sixth of the session.

## 1.0.11

- Let dropdowns grow with their text to prevent clipped labels at larger font sizes. Sound preview buttons match the dropdown height.
- Renamed controls to Interval bells and Interval cues. Timing text says One bell every 2 minutes, using minutes and seconds as needed without a dot separator.
- Equalized section heading padding and removed the extra gap between collapsed divider lines.

## 1.0.10

- Added private image backgrounds with choose/remove controls.
- Added looping Brown noise, White noise, Rain and custom background audio, independent volume, preview and session pause/resume/end handling.

- Added a 0–300 second preparation countdown, including instant start at zero, and 0.05-second vibration-slider steps.
- Slightly thickened the launcher icon while preserving its shape and colors.

- Added independent solid/gradient backgrounds and text/button accents with color wheels, brightness, hex input and a color reset. Saved themes also update the system bars.
- Clarified vibration-stage labels, changed the top caption to Duration in minutes, and matched visible dropdown-arrow insets to the text padding.

- Polished Settings dropdown arrows, preview-button alignment and checkbox ticks without changing home-page styling. Removed the in-app changelog.
- Added Off and 1–5 evenly spaced intermediate bells, with matching spacing text and independent sound/vibration controls. Existing settings default to three intermediate bells.
- Completion plays its configured ending cue and restores the chosen duration with Start ready for another session.
- Build a non-debuggable release APK for distribution, preserving the existing signing identity.

# 1.0.7

- Smooth isolated high-frequency spikes in Zen tone’s first half-second and blend back into the existing audio by 800ms.
- Added full-rate stereo checks for sharp sample jumps that downsampled checks could miss.
- The existing twenty-second tail and other four sounds are unchanged.

# 1.0.6

- Silence Zen tone’s quiet leading noise and use a gentle 100ms onset plus higher-quality encoding to soften the brief crackly attack. Its existing twenty-second ring and fade are retained.
- Existing custom selections of the supplied MP3, including numbered download copies, now use the extended bundled Zen tone. Other custom audio is unchanged.
- The other four built-in bells are unchanged.

# 1.0.5

- Extended all four built-in bells to twenty seconds with smooth fades to silence, preserving their original strike and pitch.
- Added the supplied recording as Zen tone, with the same longer ringing tail and fade.
- Bundled compressed Ogg audio and removed the ten-second cutoff for built-in presets. Timer deadlines, custom audio and vibration behavior remain unchanged.

# 1.0.4

- Preview the chosen vibration length once when releasing a settings slider.
- Previews stop when leaving Settings or the app and cannot interfere with an active meditation.

# Changelog

## 1.0.3

- Timer stays at the same vertical position before, during and after preparation.
- Start, Pause, Resume and End share the same button dimensions.
- Timer modes crossfade without delaying the native clock; reduced-motion preferences skip the fade.

## 1.0.2

- Minimal centered timer and Start button, with a small Settings button.
- Duration, sounds, vibrations and changelog live on a separate Settings screen.
- Removed the main heading, readiness/preparation wording and countdown vibration instructions.

## 1.0.1

- Inverted the launcher icon to navy artwork on a white background.

- Added vibration-length sliders with live seconds readouts and saved values.
- Reworked sounds into a Mindfulness bell, Singing bowl and Tingsha bells, plus a new Temple gong. Existing sound selections retain their saved IDs.
- The screen now reads the live monotonic clock and schedules display changes at second boundaries. Native cues schedule against their actual deadlines instead of fixed polling.

## 1.0.0

- Separate themed meditation app with adjustable session length and a 30-second preparation countdown.
- Two short preparation vibrations followed by a long one, then the start bell.
- Quarter chimes, shared start/end sound, bundled bells and custom audio file selection.
- Configurable vibration timing and lengths, pause/resume, and a foreground timer for screen-off sessions.
- No guided voice or text-to-speech.
