# Tasks 0.61.0 verification

Source: 5e41add14cf1509d4e29485ac4142fa2a873babd. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/42010561273): 151 Android tests, 791 browser checks, 33 calendar/timing checks and zero lint errors.

Task-list footer buttons use stable visible labels Completed, Trash and Hidden. They are text-only, use 12-pixel labels and 32-pixel height, and stay on one flex row. Open panels and the active hidden-task override use a two-pixel foreground border. Adjusted padding reserves the same outer size and text position when that border changes; disclosure state remains available through accessible labels and expanded/pressed attributes. Projects' separate completed control is unchanged.

The daily completed/total fraction includes scheduled-hidden tasks in the selected context. The same full pending set determines remaining duration, untimed count and progress percentage. A parenthetical count such as “1/4 done (3 hidden)” identifies tasks hidden by schedule; this count stays stable when Hidden temporarily reveals them. It decreases when their reveal time arrives and is omitted when no Today tasks are hidden. Existing rest messages continue to describe currently visible work.

Contexts follows Priority directly in detailed task editors, including the full widget editor. Widget Quick add places Labels after its Priority row. Widget Urgent keeps its 18dp checkbox drawing with an additional 8dp reserved before the label. App Quick add uses a 36-pixel single-line task field with 16-pixel text, matching the widget’s title typography; its existing dynamic title expansion remains active. Slightly tighter outer padding and action spacing shorten the popup. Other editor controls and save behavior remain intact.

Regression checks cover hidden tasks in the total, remaining duration, untimed count and progress percentage, unchanged totals during temporary reveal, reveal-time count updates, completion with hidden work remaining and omission of a zero hidden count. Existing disclosure, editor, widget and complete task workflow checks continue to pass. The native popup spacing regression was updated for the requested Priority/Labels order. CI caches Robolectric Android runtimes after a Maven Central HTTP 429 interrupted a prior run.

APK: `Tasks-v0.61.0.apk`, 286384 bytes. SHA-256: `9a1b7b25251024e73ee7c4c4c2dbbda9e4e0bcd84621149fd0d5de84b8533e2e`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.60.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
