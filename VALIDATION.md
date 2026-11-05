# Tasks 0.62.0 verification

Source: e268daa1cf2424824d89df1cdcd7e8faa2e3883e. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/42014022026): 151 Android tests, 793 browser checks, 33 calendar/timing checks and zero lint errors.

The Hidden footer button now includes the scheduled-hidden count in parentheses, including Hidden (0). Today reuses the count shown in its summary. Upcoming counts distinct eligible hidden occurrences in its existing fourteen-day list, excluding Today duplicates, completed work, active tasks and tasks outside the selected context.

Temporary revelation keeps the underlying scheduled-hidden count visible. Both buttons update through the existing reveal, edit, completion and context refresh paths. Smaller text-only styling and bold open-state borders are preserved. Accessible names include the count.

Existing task visibility, header-total, reveal-boundary, completion, editor, widget and complete workflow checks passed.

A linked task name receives “(hidden)” while its relevant occurrence is hidden by schedule. The occurrence lookup respects moved dates, recurring dates, completed work and active-task visibility overrides. Link annotations use the same text style as before. Reveal boundaries from displayed links also participate in refresh scheduling. Regression checks verify the annotation before reveal and its removal when the linked task becomes visible.

Detailed task forms use Priority → Urgent → Contexts. Checkbox dimensions are normalized to Urgent’s 18-pixel size only inside `.tl-dialog .tl-task-form`, including component editors and the full editor opened from the widget. Main-list checkbox rules and native widget front layouts are unchanged.

APK: `Tasks-v0.62.0.apk`, 286384 bytes. SHA-256: `7658e0dafdb16cbab44d64f198a609c2107270afe8a9b2acf045ccca110b149c`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.61.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
