# Tasks 0.60.0 verification

Source: cf3af57a22a2cd6c5ee23700de58210a8126aeeb. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/42005068471): 151 Android tests, 786 browser checks, 33 calendar/timing checks and zero lint errors.

The detailed task editor includes Hide until before due time, with separate Hours before and Minutes before fields. A valid task time is required. The total lead interval is stored on the task; zero means reveal at the due time. Clearing time removes the hiding interval to avoid an untimed task remaining hidden.

Today, Upcoming and the widget suppress pending tasks until their reveal times. In-progress tasks stay visible, and completion history remains accessible. The app's Show all button sits beside Show completed and Show trash; Upcoming has its own matching Show all control. This temporary display toggle reveals hidden tasks for editing without changing their schedules or the widget's filtering.

Reveal times follow the original occurrence date, or the moved date when rescheduled, rather than a carryover display date. JavaScript and native calculations agree on New York time, zero lead, midnight crossings, and daylight-saving gaps/overlaps. The app schedules a boundary refresh and updates the current date at midnight. Hidden records remain in widget storage, with a separate visibility alarm that requires no Notify setting or notification permission. Android exact-alarm capability is respected; the existing inexact fallback can delay a widget refresh when precise reminders are not allowed.

When Today is clear but a hidden task will appear later that day, both app and widget show the earliest reveal time: “You’re done until [time]! Get some rest. 🪻”. Otherwise they show “You’re all done! Get some rest. 🪻”. Existing widget centering is preserved.

Regression coverage includes interval editing, saved values in the widget editor entry point, invalid minutes, both Show all controls, automatic reveal without reopening, moved dates, active-task overrides, completed history, the next reveal message and midnight rollover. Native coverage verifies hidden-row retention, exact reveal boundaries, alarm scheduling independent of task notifications, completion/filter handling, stale snapshots, moved/repeated occurrences and invalid settings.

APK: `Tasks-v0.60.0.apk`, 282288 bytes. SHA-256: `e1e8ad213ef7c81f6679b7c158fcbdc0d42368b6e0ea504623a9f8db5973af6f`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.59.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
