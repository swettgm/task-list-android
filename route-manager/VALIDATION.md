# Route Manager 1.0.0 validation

Source: `4bfa360a17d5253e5444f9fb441b7641b96d68ed`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/42512153493).

Nine Android tests, 53 Chromium workflow checks and zero lint errors passed. Tests cover native offline storage and rejection of invalid writes; safe external links; automatic weekday selection; real touch swipes looping in both directions; saved-stop autocomplete and selection; multiple stops on a note; explicit disk save and automatic save; note/stop editing and deletion; cancellation; storage failure recovery; persistence; appearance settings; narrow screens and visible editor actions.

Screenshots of the route page, note editor and stop page were reviewed. Testing used Robolectric and Chromium; no physical phone was used.

The signed APK has the same non-signature contents as the tested build. Package `com.personal.routemanager`, version 1.0.0 (1), Android 8.0 or newer, 33,361 bytes. APK v2/v3 signatures, alignment, manifest, ZIP integrity and packaged HTML were verified. A separate retained private signing key supports future updates; it is not in the repository.

SHA-256: `403c1bf550a243a00e9aec28105571a1ac7b61323f133093ee2f7922a620eb4e`.

This app stores its data separately from Tasks and has no widget, notification components, account or Internet permission. A fresh install starts without any sample stops or notes.
