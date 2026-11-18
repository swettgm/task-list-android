# Meditation 1.0.16 verification

Source: `26dbcd8cbc699bb861887f3ce9095fc5f7b5edbf`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44224919813). **47 Android tests, 159 browser checks, eight audio checks and zero release lint errors passed.**

Brown noise is now lossless 16-bit FLAC, with a steady low-mid profile and no gain envelope. Decoded half-second RMS spread is 0.31 dB and opening-versus-rest difference is -0.02 dB. Rain retains the CC0 garden recording by barkenov, removing isolated recording ticks with a nine-sample median and gentle smoothing before quieter level adjustment. Loop seams, levels, brightness and headroom pass checks. All eight exact audio hashes were found in the signed APK; the five bells and white noise are unchanged. The timer, UI, choices and saved settings are unchanged. The original rain recording and CC0 provenance remain in audio-source.

APK: `Meditation-v1.0.16.apk`, 2798600 bytes; SHA-256 `ddbcb7056f3be42f24537ff22a7e325c4a1bd2c3d10b533e970dc4b662947450`. Package `com.personal.meditation`, version code 17. Artifact digests, ZIP integrity, packaged UI/audio, manifest, alignment, retained signer and non-signature equality to CI passed. Robolectric and Chromium were used; no physical phone listening test was performed. Install over the current app to retain settings.

# Meditation 1.0.15 verification

Source: `3bb7d1076c09769508836188147d9ae00c8ad52d`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44223561863). **47 Android tests, 159 browser checks, eight audio checks and zero release lint errors passed.**

Rain replaces synthetic wash/droplets with Soft rain.WAV, a garden field recording by barkenov under CC0: https://freesound.org/people/barkenov/sounds/640655/. The original high-quality preview, license, origin URL and SHA-256 are retained in audio-source. Equal-power overlap makes a continuous loop; isolated peaks are rounded and the heavy high-frequency rolloff is removed to preserve natural rain patter. Brown noise reduces inaudible sub-bass and increases audible warm low-mid energy while retaining smooth circular level stabilization. Half-second level spread is 0.34 dB and opening-versus-rest difference is -0.02 dB. Playback keeps fixed gain; checks verify loop continuity and level consistency. All five bells and white noise are byte-for-byte unchanged, verified in the signed APK against the protected hashes. Timer and UI are unchanged. CI uses pinned imageio-ffmpeg when FFmpeg is absent, avoiding stalled package mirrors.

APK: `Meditation-v1.0.15.apk`, 2462727 bytes; SHA-256 `610e68fddf153b60e10f4d6e480cd55e21d33444f6248edad15b36302123823b`. Package `com.personal.meditation`, version code 16. Artifact digests, ZIP integrity, packaged UI/audio, manifest, alignment, retained signing certificate and non-signature equality to CI passed. Install over the current app to retain settings. Robolectric and Chromium were used; no physical phone listening test was performed.

# Meditation 1.0.14 verification

Source: `2f1647e8a1916a84f953521ac21108bebab6219f`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/44220576688). **47 native Android tests, 159 browser checks, eight audio checks and zero release lint errors passed.**

Rain adds rounded water droplets to a soft stereo wash while retaining bounded brightness and peaks. Brown noise uses circular, smooth local-energy stabilization, with no fade envelope or volume automation. Decoded half-second RMS spread is 0.55 dB and opening-versus-rest difference is -0.03 dB; regression limits are 1 dB and 0.5 dB respectively. Seam continuity, headroom and audible level checks pass. All five bells and white noise are protected by their existing SHA-256 hashes and byte-for-byte unchanged. Packaged APK verification finds the exact encoded hashes for all eight sounds, including optimized resource names. UI, timer, cues and playback gain behavior are unchanged. CI now reuses installed FFmpeg rather than always updating packages.

APK: `Meditation-v1.0.14.apk`, 2143239 bytes, package `com.personal.meditation`, version code 15. SHA-256 `154933b3a2940dcf7b999a0b72019b088f203670f7938e6d1cbe05cb11e5ef1a`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signer and exact non-signature equality to CI passed. Install over the current app to retain settings. Native checks use Robolectric and browser checks use Chromium. No physical phone listening test was performed.

# Meditation 1.0.13 verification

Source: `05502a8310e9d412f9a67527adf32b45327acaf7`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/44075102111). **47 native Android tests, 159 browser checks, eight audio checks and zero release lint errors passed.**

Rebuilt Mindfulness bell, Tingsha bells and Temple gong from clean inharmonic struck modes, without random strike noise, time stretching or lossy encoding. The three files now use lossless FLAC and preserve distinct attacks with twenty-second fades. Replaced rain with softer, warm stereo wash and rounded droplets, lower peaks and less high-frequency energy. Rain checks bound brightness, peaks, audible level and loop continuity. The approved Zen tone and Singing bowl and the existing brown/white noise are locked by their original SHA-256 hashes. Verification compared all eight encoded audio file hashes inside the actual APK, accounting for optimized resource names; all protected bytes are exactly unchanged. Timer/UI code and sound identifiers are unchanged.

APK: `Meditation-v1.0.13.apk`, 2118663 bytes. SHA-256 `aecba26d28a2c105f23d62a6b5fa4b4cff5d2710e1b461a56f5c98b4b235e2f8`. Package `com.personal.meditation`. Artifact hashes, ZIP integrity, package/version, alignment, retained signer and exact non-signature content equality to CI passed. Updates retain existing app data. Native checks use Robolectric; browser checks use Chromium. No physical phone was tested.

# Meditation 1.0.12 verification

Source: `9c58f86f923bb743b33cce22daf1ada82ace75f9`. [Build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/43777874138). **47 native Android tests, 159 Chromium checks, eight audio checks and zero release lint errors passed.**

Removed the home Settings gear border/background in all accent modes while preserving its 44px touch target and focus outline. The Settings heading is reduced from 26px to 22px. Browser checks confirm the border does not return with gradient accents.

The clarified interval limit remains five bells before the end cue, at each sixth of the session when the slider is at maximum. Spacing is calculated from duration: eight minutes gives 1 minute 20 seconds, and one minute gives 10 seconds. Tests verify changing duration retains the five-bell cap and updates the shown gap. Existing timer scheduling and saved counts remain unchanged.

Artifact hashes, archive integrity, exact packaged UI, retained signing identity, package/version, alignment, non-signature equality with CI and v2/v3 signatures passed. Tests use Robolectric and Chromium; no physical phone was tested.

APK: `Meditation-v1.0.12.apk`, 1496068 bytes. SHA-256 `0d719d29a3c912289c3939b585b9a05a775fb7bfcf9337cadacc760d8ce455e3`. Package `com.personal.meditation`, version 1.0.12 (13), API 26/35. Installs over previous Meditation releases and retains settings.

# Meditation 1.0.11 verification

Source: `8df9406ae194e23826d09daf485981e3823663c9`. [Build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/43775571184). **47 native Android tests, 154 Chromium checks, eight audio checks and zero release lint errors passed.**

Settings dropdowns use natural height and proportional line height instead of a fixed-height text box. Their preview buttons stretch to the adjacent dropdown. Checks measure actual font ink against the available content height and equal preview/dropdown bounds at 14px, 24px and 32px text sizes, in addition to the existing narrow/wide layout checks.

The Interval bells control is a whole-number slider from Off to 5 bells, matching the vibration sliders. Saved counts and timing rules are unchanged. Its output shows the count; the line below says One bell every 2 minutes, One bell every 1 minute 45 seconds, or One bell every 10 seconds, with no dot separator. Off still suppresses interval sound and vibration. Visible Style captions are removed from Appearance, preserving accessible names for its dropdowns.

Collapsible section summaries have equal 20px top and bottom padding. Removed the extra margin between collapsed sections and normalized expanded-content bottom padding. Chromium verifies equal header spacing and adjoining divider positions. Home-screen styling and the existing image, theme, background audio, timer and vibration behavior are unchanged.

Artifact hashes, ZIP CRCs, exact packaged UI, non-signature content equality with CI, retained signing identity, alignment, package/version and v2/v3 signatures passed. Tested with Robolectric and Chromium, including enlarged text; no physical phone was tested.

APK: `Meditation-v1.0.11.apk`, 1496068 bytes. SHA-256 `22f8cd2efd8c2ea9e3b520dec7bbba2fdb286625684afa4893854b60de3d2a61`. Package `com.personal.meditation`, version 1.0.11 (12), API 26/35. Installs over previous Meditation releases and retains settings.

# Meditation 1.0.10 verification

Source: `ccd151d37142901667a94eb29d454f84631e7911`. [Build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/43768997941). **47 native Android tests, 143 Chromium checks, eight audio-file checks and zero release lint errors passed.**

Settings now uses centered, uniform dropdown chevrons with equal visible/text insets, aligned preview buttons and centered checkbox ticks. Labels say Duration in minutes, Countdown in seconds and clear vibration stages; the Vibrations seconds suffix and in-app changelog are removed. The launcher icon stroke is slightly thicker. Home layout and control dimensions remain unchanged.

Choose 0–300 preparation seconds, default 30. Zero starts immediately; one/two-second preparations only emit pulses that fit. Choose Off or 1–5 evenly spaced intermediate bells; Off suppresses both interval sound and vibration. Intermediate sound and vibration remain independent. Vibration sliders snap to 0.05-second increments and preview the saved snapped duration only on release. Session completion plays the configured end cue once and restores the selected duration, ready for Start without automatically restarting.

Background and text/button accents each support solid or two-color gradients, with touch/keyboard color wheels, brightness, validated hex entry, cancel/Back handling and reset. Native system bars follow the background. Image backgrounds use Android's document picker, decode/orient/downsample into a bounded private JPEG, survive reopening and can be removed. No network upload or broad storage permission is introduced.

Background sound supports Off, original synthesized Brown noise, White noise and Rain, or retained custom audio, with independent volume. A separate MediaPlayer loops throughout meditation, follows Pause/Resume and stops at End. Bell cues never replace the background player. Ten-second previews stop when leaving Settings or starting a session; actual custom loop playback has no ten-second limit. Native tests cover repeated loops, custom playback, pause during preparation, stop/timeout, cue coexistence and service integration. The three 24-second presets pass level, clipping, non-silence and loop-boundary checks; existing five bell files are unchanged.

The release APK is non-debuggable and signed locally with the retained Meditation certificate. Its assets match the tested CI artifact, and all non-signature archive content matches before/after signing. Verified artifact SHA-256, ZIP CRCs, package/version, alignment and v2/v3 signatures. Tests use Robolectric SDK 28 and Chromium; no physical phone or personal photo/audio was used.

APK: `Meditation-v1.0.10.apk`, 1496068 bytes. SHA-256 `8c9ae7c8ce8bc26b0e53709bdeb25be06412d19b0759efcbefbbad75893b5fcf`. Package `com.personal.meditation`, version 1.0.10 (11), API 26/35. Installs over existing Meditation releases while retaining settings.

# Meditation 1.0.9 verification

Source: `a1a04e51c4244004453e1e5fa681fec7523dbd6d`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43651288722). **24 native tests, 74 browser checks, five decoded-audio checks and zero lint errors passed.**

Restored Zen tone’s original real-time strike through 1.4 seconds, with only the recording’s quiet pre-strike lead silenced and a 10ms entry fade. Its late ringing modes are fitted from the recording and extended, avoiding time-stretch processing on the attack. Singing bowl retains its real-time attack, gains a brief mallet strike made from deterministic damped inharmonic modes without random noise, and joins a phase-aligned extended ring. Both use seekable lossless FLAC and retain twenty-second decays that reach silence at 19.8 seconds. The other three preset files are unchanged.

Audio checks now compare the opening waveform against the original source so the previous whole-file stretch and long fade-in fail verification. Relative waveform residuals from 350–700ms are below 0.00004 for both sounds. The bowl tail’s normalized join correlation is above 0.99. Full-rate stereo sample steps, clipping, sustained decay, duration and silent endings are checked. Native tests cover full cue playback, stop/replacement, custom-file recognition and timer behavior. Verification used decoded samples, Robolectric and Chromium; no physical-phone listening test was performed.

APK: `Meditation-v1.0.9.apk`, 1123316 bytes. SHA-256 `3d6d536eea752627df1ff4eed9281810131b94195018bf9bd6cb6fee2b97d7c1`. Package `com.personal.meditation`, version 1.0.9 (10), API 26/35. Artifact hashes, ZIP integrity, exact uncompressed bell resources, non-signature content equality to CI, alignment and v2/v3 signatures passed. The retained signer matches prior Meditation releases.

# Meditation 1.0.8 verification

Source: `2b74d50aab0d796d22dde1aa30a53d72cdb3636f`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43647608028). **24 native tests, 74 browser checks, five decoded-audio checks and zero lint errors passed.**

Rebuilt Zen tone as one continuous pitch-preserving stretch instead of joining an unstretched strike to a stretched ring. The old crossfade caused a brief phase-cancelling loudness dip around 400ms that sample-spike cleanup did not fix. Source cleanup runs before stretching; 140ms of output silence is followed by a 650ms raised-cosine onset. The sound remains twenty seconds long with its gradual fade ending at 19.8 seconds. Zen tone uses lossless 16-bit stereo FLAC, packaged uncompressed so Android MediaPlayer can seek the resource. The other four presets are byte-identical to 1.0.7. Saved sound choices and original-MP3 recognition remain compatible.

Decoded-audio QA now checks the full-rate stereo loudness envelope as well as adjacent-sample steps, headroom, duration and final silence. The opening envelope check rejects the published 1.0.7 dip (successive 50ms RMS ratio 0.8015) and passes the rebuilt opening (minimum 1.0268 in the checked rising interval). Additional analysis verifies the complete first two seconds independently in left, right and mono, an unclipped peak of 0.5, and exact silent leading/trailing samples. Native tests verify the complete twenty-second Zen cue, replacement/cancellation, custom audio and timer behavior. Verification used decoded samples, Robolectric and Chromium; no physical-phone listening test was performed.

APK: `Meditation-v1.0.8.apk`, 967667 bytes. SHA-256 `5ff5f882aad06f4dba011aed66fbb08d29da7b779598d6c9b497cefb1668dd4c`. Package `com.personal.meditation`, version 1.0.8 (9), API 26/35. Artifact hashes, ZIP integrity, exact seekable packaged audio, non-signature entry equality to CI, alignment and v2/v3 signatures passed. The retained signer matches previous Meditation releases.

# Meditation 1.0.4 validation

Source: `327b8bb56a9c9bef18db3992fe86ebf50ad22e45`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43319061544). **16 Android tests, 72 Chromium checks and zero lint errors passed.**

Releasing a vibration-length slider saves its selected duration and previews one vibration through the native vibrator. Input events during dragging do not preview repeatedly. Failed saves do not preview unsaved lengths. Native code checks the saved enabled state, foreground activity and active session before playing. Previews stop when leaving Settings, pausing/destroying the activity, or starting a session. The existing timer and cue scheduling are unchanged. Browser checks cover release-only preview, exact duration and failed saves; native checks verify exact vibration duration, cancellation, disabled/invalid keys and blocking during a session.

Signed APK: `Meditation-v1.0.4.apk`, 951216 bytes. Package `com.personal.meditation`, version 1.0.4 (5), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `c5cba7bd488e7cb6a6c4bc06fade838179f84a36f91fda8bcddb247491cfa4cc`. The retained certificate matches Meditation 1.0.3. Tests used Robolectric and Chromium; no physical phone was used.

# Meditation 1.0.3 validation

Source: `215ed899b4508b4cc76c726e6ef7df22cccf793d`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43125016206). **15 Android tests, 69 Chromium checks and zero lint errors passed.**

All session buttons use the same width and 54 px height, with a fixed-height action row. The timer retains its exact vertical position through idle, preparation, meditation, pause and end at 320/390/600 px viewport widths. Tests compare timer and button bounds within half a pixel during transitions and after settling.

Timer modes crossfade over 300 ms using a decorative, accessibility-hidden previous value in the same grid cell. The live timer updates immediately; cue deadlines and second-boundary refresh are unchanged. Ordinary second changes do not animate. Completed fades remove their temporary element; phase changes, Settings, page hiding and reduced-motion preferences clean up or skip the fade. Existing screen, Settings, saved choices, sounds and session controls remain covered. Ready and active timer screenshots were reviewed.

Signed APK: `Meditation-v1.0.3.apk`, 951216 bytes. Package `com.personal.meditation`, version 1.0.3 (4), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `1b5bf25ad7909590046190fa94151b039b9da08003148b7f7da9de99eff0f61b`. The retained certificate matches prior updates. Robolectric and Chromium were used; no physical phone was used.

# Meditation 1.0.2 validation

Source: `46cedb87efce2aa0e479b743f7fd8640aa357a4b`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43123740419). **15 Android tests, 44 Chromium checks and zero lint errors passed.**

The idle home screen shows only the centered timer, Start button and small Settings icon. Main-screen heading, readiness text, preparation helper and countdown vibration instructions are removed. Duration, independent sounds, custom audio, vibration switches/sliders and changelog live on a separate Settings screen. Back returns home; edits persist automatically, invalid duration stays in Settings with an error, and active-session settings are readable but disabled. Leaving Settings stops audio previews. The 30-second preparation countdown, cue timings, monotonic clock and ongoing-session behavior remain unchanged.

Browser checks verify centered controls and no overflow at 320/390/600 px, minimal home/countdown wording, separate Settings and changelog, settings persistence, errors, Android Back behavior, active-session protection, previews, sound choices, vibration sliders, pause/resume/reopen/end and exact second-boundary refresh/catch-up behavior. Home, Settings and timer screenshots were reviewed.

Signed APK: `Meditation-v1.0.2.apk`, 951216 bytes. Package `com.personal.meditation`, version 1.0.2 (3), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches prior updates. SHA-256 `6cbc04fc5920b074dec016f7e4678b7d7aa85aa4f7a0ab90c91492f720ab0544`. Robolectric and Chromium were used; no physical phone was used.

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
