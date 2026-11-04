# Tasks 0.53.0 verification

Source: 97162a9a743a7d600c575416d25f7814b12bbf40. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41820574136): 138 Android tests, 719 browser checks, 27 calendar checks and zero lint errors.

Task notification action order is Snooze first (left) and Complete second (right). Updated existing notification checks verify both positions and exercise completion, snoozing, redelivery and stale-action handling through their new positions. Action sizes and behavior are unchanged. Daily reminders still have neither action.

The Today/Upcoming control retains its 46px outer height. Two pixels of inner padding plus the one-pixel border give the 40px active button an even three-pixel inset; its five-pixel corner radius matches the eight-pixel outer radius. Existing large-type and narrow-width header checks remain in place, and both selected tabs are checked for inset bounds and corner radii.

Signed APK: 265,968 bytes. SHA-256: `1283b3394b6d45feb474d1afff7af386aa033e841108f5103bc9b06f8dbfd90d`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.52.0. Both public download paths contain the same signed APK. Settings Changelog includes the new action order.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
