# Tasks 0.59.0 verification

Source: 488f3e8fdd7c04c135cf58517b2d2428df0dae35. [Successful build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/41950572412): 145 Android tests, 764 browser checks, 27 calendar checks and zero lint errors.

Urgent selects High immediately and stores the underlying normal priority separately. Unticking it restores that priority in the form, including after saving and reopening. Saving a normal task removes the temporary priority metadata. Choosing Medium or Low explicitly clears Urgent and respects the user's choice; arrow priority changes also remove obsolete urgency metadata.

Widget Quick add follows the same temporary High behavior and persists the selected underlying priority into canonical task definitions. Its next-entry reset still clears Urgent and returns to Low. App Quick add saves Low as the underlying priority for new urgent tasks, and passes it through Details.

Browser regression checks exercise Low, Medium and High round trips, reloads, cancellation, direct priority choices, arrows and Quick add. Native checks cover immediate spinner changes, restored Medium, the saved underlying priority and batch reset. Previously saved urgent tasks without historical priority metadata retain High because their former priority cannot be recovered reliably; selecting a normal priority establishes the next remembered value.

The ongoing notification retains the previous release's ongoing/Clear-all protection and dismissal restoration. Official Android documentation confirms that Android 14 allows users to dismiss ordinary ongoing notifications: https://developer.android.com/about/versions/14/behavior-changes-all#non-dismissable-notifications . No unsupported non-dismissible exemption is claimed.

APK: `Tasks-v0.59.0.apk`, 278192 bytes. SHA-256: `429a7c9ce563224da8fc5557a85444c60919a05309d18acf8670d512ae58b699`.

Artifact digests, ZIP CRCs, CI checksum, exact packaged assets, non-signature APK contents, alignment and the retained signing certificate were verified. The signer matches Tasks 0.58.0. Both public download paths contain the same signed APK. Settings Changelog includes this update.

No physical phone was used. Android checks use Robolectric; browser checks use Chromium. Windows work remains deferred.
