# Route Manager 1.0.1 validation

Source: `5cc4d3a1faf7e085b325f09a67b12f22b21a2eff`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/42524680754). Nine Android tests, 60 Chromium workflow checks and zero lint errors passed.

The Add/Edit note field starts on one line and grows with text. Editor headings and fields do not shrink, and the inner fields scroll separately from the fixed action row. The top-anchored dialog uses both the dynamic viewport and visual viewport height to stay inside the keyboard area. Field focus avoids scrolling the page; keyboard resize keeps the first stop field and its caption intact. Tests cover 390×420, 320×360 and 600×480 keyboard-sized viewports, dynamic grow/shrink, larger text, long notes and a visible separate footer.

The settings control now uses the same gear character as Tasks. The main page no longer has a stop dropdown or Stops button. Manage stops is available inside the separate Settings page; Back from stop management returns there. Existing stop names, cadence labels, note associations, routes and appearance remain saved.

The editor screenshot was reviewed. The signed APK's non-signature entries match the tested CI artifact, including exact packaged HTML. Package `com.personal.routemanager`, version 1.0.1 (2), 33361 bytes. APK signatures, retained certificate match to 1.0.0, alignment, manifest and ZIP integrity passed. SHA-256 `bf60c26f412968d940794511cfdb06e613120a6dea0a7fcc666f6a2d81ad2277`.

Validation used Robolectric and Chromium; no physical phone was used.

# Route Manager 1.0.0 validation

Source: `4bfa360a17d5253e5444f9fb441b7641b96d68ed`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/42512153493).

Nine Android tests, 53 Chromium workflow checks and zero lint errors passed. Tests cover native offline storage and rejection of invalid writes; safe external links; automatic weekday selection; real touch swipes looping in both directions; saved-stop autocomplete and selection; multiple stops on a note; explicit disk save and automatic save; note/stop editing and deletion; cancellation; storage failure recovery; persistence; appearance settings; narrow screens and visible editor actions.

Screenshots of the route page, note editor and stop page were reviewed. Testing used Robolectric and Chromium; no physical phone was used.

The signed APK has the same non-signature contents as the tested build. Package `com.personal.routemanager`, version 1.0.0 (1), Android 8.0 or newer, 33,361 bytes. APK v2/v3 signatures, alignment, manifest, ZIP integrity and packaged HTML were verified. A separate retained private signing key supports future updates; it is not in the repository.

SHA-256: `403c1bf550a243a00e9aec28105571a1ac7b61323f133093ee2f7922a620eb4e`.

This app stores its data separately from Tasks and has no widget, notification components, account or Internet permission. A fresh install starts without any sample stops or notes.
