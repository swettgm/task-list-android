# Tasks 0.49.0 verification

Source: 028880a27fb70c11516803becfb2b2e1d4c90f31. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41734780370): 131 Android tests, 696 browser checks, 27 calendar checks and zero lint errors.

Task time metadata now displays a rounded white bell only when the task has both a time and its notification enabled. The bell precedes the time in the same clickable component, uses the existing 24px icon slot and 6px gap, and remains vertically centered. This reminder indicator remains visible independently of the optional decorative metadata icon preference. Untimed tasks and tasks with notifications disabled show no bell. Existing reminder scheduling is unchanged.

Browser checks cover notification on/off, missing time, all three metadata icon preferences, icon alignment and immediate removal when a reminder is disabled through the time editor. Existing wrapping, recurrence, duration and priority checks remain in place.

The widget editor signals readiness from the task interface instead of waiting for onPageFinished, skips hidden list rendering and disables activity transition animation. The main app renders Today before Upcoming, avoids rereading the already embedded bridge, and reveals the WebView only after its visual-state callback confirms the completed layout. A page-generation token prevents stale load callbacks from revealing a replacement page. Readiness is idempotent across the early signal and page-finished fallback.

Modal background dimming extends consistently over status, navigation and side insets. Normal app bars are composited against the saved theme color while a modal is open and restored after the final modal closes. Widget editor bars use the matching translucent scrim. Projects no longer has Back to tasks; its completed toggle is the final control, with Android Back and outside-tap dismissal retained.

Additional checks cover early readiness, duplicate page-finished handling, bar colors and restoration, deferred Upcoming rendering and Projects footer placement. Physical startup timing and system-bar rendering still need device confirmation.

Signed APK: 257,604 bytes. SHA-256: `6d38ef35250687186395bd41c360b35ebf40a35f2cbf25a3c16089797caa71fd`.

Artifact hashes, ZIP CRCs, CI checksum, exact packaged UI assets, all non-signature APK entries, alignment and the retained signer passed verification. The signer matches Tasks 0.48.0. Both published download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
