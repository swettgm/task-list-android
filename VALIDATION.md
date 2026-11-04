# Tasks 0.52.0 verification

Source: 6dc31594030334ddb0af9ae2e9b8b013aad4a731. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41770613797): 138 Android tests, 717 browser checks, 27 calendar checks and zero lint errors.

Replaced the fixed Upcoming margin from 0.51.0 with measurement of the actual first-line text rectangles. The first date's top margin is adjusted until its text top matches the Today summary's text top, relative to their own panels. A hidden panel is measured off-flow and invisible within the same script turn; its original hidden state and inline styles are restored before painting. Today is not moved and text sizes are unchanged.

Alignment refreshes after Upcoming rendering, before tab switches or swipe starts, after document/font readiness, summary size changes and container-width changes. Both-direction swipe tests now compare text tops instead of row-box centers. Additional tests use 320, 390 and 600px widths with larger summary/heading fonts and different line heights. Existing checks verify neither page shifts vertically during the transition. A held-swipe screenshot was inspected.

Includes the solid white, slightly smaller notification bell. Existing editing, recurrence, widget, persistence and notification checks remain in place.

Recurring editors distinguish the selected occurrence date from the historical repeat anchor. An unchanged save retains the anchor and creates no moved occurrence. Explicit date or schedule changes set a cutover and revision marker; previous completion keys remain in history while invalid old moved occurrences and retroactive pending dates are excluded. Native reminder planning applies the same cutover and validates moved sources against the current schedule.

Timed notifications have immutable Complete and Snooze PendingIntents scoped to a task occurrence and a one-use notification token. Completion derives canonical checklist keys even without a widget snapshot and preserves future recurrence. Snoozes use a separately persisted alarm queue, survive rescheduling and reboot, validate the source schedule before redelivery and do not alter task dates or completion. Disabling notifications, completing a task or changing its source schedule invalidates pending snoozes/actions. Daily notifications have neither action. Settings validates a global 1–10080 minute snooze duration, defaulting to 10 minutes.

Native tests cover action labels and daily separation, checklist completion without a snapshot, repeat preservation, replay rejection, snooze duration, rescheduling, redelivery, opt-out cancellation, stale schedule actions and recurrence cutover. Browser checks cover unchanged recurrence saves, date changes for several recurrence representations, history retention, Upcoming visibility and snooze setting persistence. Android notification actions were exercised with Robolectric, not a physical notification shade.

Signed APK: 265,968 bytes. SHA-256: `f98b1ad9341677781f7d83360e552fa2e2cd657a502f1e78949bd1b2f47db648`.

Artifact hashes, ZIP CRCs, CI checksum, exact packaged UI assets, all non-signature APK entries, alignment and the retained signer passed verification. The signer matches Tasks 0.51.0. Both published download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
