# Tasks 0.24.0 verification

[Successful CI run](https://github.com/swettgm/task-list-android/actions/runs/41485496780), source commit `4abf95c5d7125320150a60728c0a444cfc290da3`.

89 Android tests, 394 browser checks and 26 calendar recurrence checks passed; lint reported zero errors.

Label emojis are accessible buttons opening a label-only popup. Existing selections load automatically; Save updates task definitions and widget snapshots while preserving other details and completion history. Cancel retains the previous labels. Emoji sizes and the original Guild crest remain unchanged. Inline metadata and emoji shortcuts suppress blue touch highlights and text selection; keyboard focus uses a neutral outline.

Holding a priority arrow for 450ms jumps to the top or bottom section: High for up, Priority not set for down. A successful change requests one native LONG_PRESS haptic, using View.performHapticFeedback without adding vibration permission. Normal taps still move one section. Moving away, cancellation, blur and visibility changes clear pending holds. Release clicks cannot cause an extra change. Browser checks exercise endpoint jumps, haptic call counts, release suppression, ordinary taps afterward, cancellation and Upcoming. Native checks verify the haptic constant, unchanged task state and safety after activity destruction.

High, Medium and Low remain the set priorities, with Priority not set restored as a separate bottom section in the app and widget. New tasks default to unset in quick-add and full editors. Highest maps to High and Lowest to Low; existing unset values stay unset. Tests verify bottom ordering, default editor selection, native legacy records, widget headings, and preservation of every saved task identity through priority changes. Urgent stays first.

The completed-list button says Show completed or Hide completed, with no count. Duration and repeat frequency share a non-wrapping horizontal group; frequency text can wrap within its own column, and time can wrap separately. Layout checks cover Sunday–Tuesday beside duration on Today and Upcoming at 320, 390 and 600px without sideways scrolling.

Both tab panels now follow the finger during horizontal dragging. Release settles from the current position; short drags return to the starting tab, and held drags can last over a second. Existing vertical positions remain stable. Checks measure both panels before release, direction and settling, cancellation, vertical scrolling, cleanup and reduced-motion behavior. The white app icon retains its black checklist design with rounded line ends.

The theme-reload browser fixture uses a stable local HTTP origin because file-URL sessionStorage is unreliable across browser process changes. It explicitly checks saved fixture state before and after reload; Android persistence continues to use the existing native private store.

[Download Tasks-v0.24.0.apk](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Tasks-v0.24.0.apk).

Package `com.personal.tasklist.preview`, version 0.24.0, code 24; minimum API 26, target 35; size 178,123 bytes.

SHA-256: `832e8d1d88300eba6f8eedd57f2381b1047f0dd0cead2d4bbaf505671b5496d9`.

Artifact hashes, ZIP CRCs and CI checksum passed. UI assets match source. The signed APK retains the prior certificate and passed signature, ZIP alignment and package/version checks. Every non-signature entry matches the tested CI APK. Install over the existing app to retain tasks and settings.

No physical phone, software keyboard, vibrator or Android launcher was used. Browser checks run in Chromium; native controls and widget behavior use Robolectric and view inflation. Device settings control physical haptics, and Android policies can delay background updates and notifications. These tests do not guarantee against every software, storage or device failure.
