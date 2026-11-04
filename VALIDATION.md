# Tasks 0.54.0 verification

Source: 6b044e0464b08a26f25ed5e8dac60cbbcd14a229. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41871503095): 139 Android tests, 731 browser checks, 27 calendar checks and zero lint errors.

In-app Quick add places Urgent beside the title at the top right. It creates an urgent High-priority task, resets after a successful add and carries into Details. Keyboard focus, batch entry and border feedback remain intact. Layout checks cover 320, 390 and 600px widths.

App checkboxes use squircle corner shaping with a 50% radius where supported, producing a superellipse instead of a circle. Older WebViews use a 25% rounded-square fallback. Generic editor/settings checkboxes retain clear checked, disabled and keyboard-focus states. CSS excludes the widget-editor document. Its original checkbox appearance is explicitly checked; native widget quick-add and label controls are untouched.

The Today summary uses indivisible completion, remaining-duration and untimed sections with the same separator treatment as task metadata. Dots are clipped at new-line starts and never hang at previous-line ends. Checks cover large text on a narrow summary and first-line alignment with Upcoming after grouping.

Urgent widget rows show the red Urgent pill in place of Today. Existing start/stop actions change it to green In progress and back to Urgent, without changing the urgent flag. Tests cover the red background, rounded pill, canonical in-progress state and restoration. Other widget controls retain their existing appearance.

Signed APK: 270,146 bytes. SHA-256: `d65c56ea94c1fbe626f59eb56399fa98eebab1902da15f5661248bfd9ed4249a`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.53.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
