# Tasks 0.43.0 verification

Source: cff4649de8355aaaf89ab84a9848915eae4a1e87. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41681769974): 125 Android tests, 664 browser checks, 27 calendar checks and zero lint errors.

The full date and Today / Upcoming tabs share one line. The controls are 44 px high inside their 46 px border, with slightly less horizontal padding, a 6 px gap and 2 px more header space on each side. Header text keeps its natural size when it fits. When needed, actual rendered text widths determine a proportional reduction limited to the header. Resize, date changes, active-tab changes and font loading trigger fitting. Checks cover short and long weekday/month labels, 320/390/600 px widths, larger header type, vertical centering, both tabs and preservation of full text size when space allows.

Task details retain their existing font size and wrap by duration, time, recurrence, labels and priority. Separators sit in the gap before the following piece and are clipped at the start of each visual line, preventing dots at either side of a line break. Their symmetrical spacing and weight are retained. Checks cover normal and larger detail text, no sideways overflow, consistent separator placement and capitalized Low / Med / High priority text. Editing shortcuts, label selection and horizontal gestures remain covered by the complete workflow.

`Tasks-v0.43.0.apk`: version 0.43.0 / code 43; 244,937 bytes. SHA-256: `60a8531a636803ad17814c1a1ae80804d4de5f9774672b59aa7c5dc91274259b`.

Artifact hashes, ZIP CRCs, CI checksum, package/version, exact packaged UI assets including changelog data, all non-signature APK entries, alignment and the retained preview signer passed verification. The signer matches Tasks 0.42.0, and the original seed state is unchanged. Both download paths contain the same signed APK. Settings Changelog contains 41 published releases including this update.

No physical phone or launcher was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
