# Route Manager 1.0.5 validation

Source: `ca8ba4869c9e774d59d34166e71c1a160b2be6be`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43035007369). **15 Android tests, 107 Chromium checks and zero lint errors passed.**

This release includes the pending 1.0.4 tab indicators and stop-name capitalization: populated routes have a subtle highlight and dot independently of the selected route; the indicator clears after the last note is deleted. Stop saves capitalize word starts while preserving internal brand/acronym capitalization.

Optional account numbers are stored as text, preserving leading zeros. Name/account pairs identify duplicate stops within each route while stable stop IDs preserve note associations. Accounts appear in suggestions, chips, note labels, managed stops and deletion labels. Validation rejects malformed account data and duplicate pairs without replacing saved data.

Manage stops → Export list previews a clean numbered plain-text list with route headings, names, account numbers and frequencies, for all routes or one route. Copy uses Android ClipboardManager; Share opens an ACTION_SEND text/plain chooser. No message is sent automatically. Tests verify exact copied/shared text, valid chooser intent, independent accounts and rejected duplicate edits.

Saved 1 day / 2 day checkboxes are mutually exclusive. The offset determines the initial/foreground weekday route and survives reopening. Monday +1 opens G300, Monday +2 opens G400; Friday +1 opens G200 and Friday +2 opens G300. Turning the offset off returns to the current weekday. Tests cover saved offsets, launch/foreground behavior, weekend wrapping and failed-write rollback.

The note editor remains compact with its optional account field collapsed initially. Export, tab-indicator and editor screenshots were reviewed. Geometry verifies the Add note button stays in the visible phone viewport with offset controls. The complete project/build workflow files were restored from the prior verified repository tree, retaining current app sources and archived downloads.

APK package `com.personal.routemanager`, version 1.0.5 (6), 37457 bytes. Exact HTML, ZIP integrity, all non-signature CI entries, alignment, APK signatures and retained certificate match to 1.0.3 passed. SHA-256 `8cebf840cadd430e5f822bfed907bd7a04d05ad81c9bd455593299767ac2bc63`. Testing used Robolectric and Chromium; no physical phone was used.

# Route Manager 1.0.3 validation

Source: `d35ebc3d84536e0fc08e4d6b78079bbd6da53d7f`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/42531214614). **11 Android tests, 80 Chromium checks and zero lint errors passed.**

Every route header now shows the number of notes across all five routes in the existing count area, such as 4 notes total. This replaces the route-specific stop/note count and preserves the weekday label. The count is derived from note records, updates on save/delete and counts a note with multiple stop associations once. Browser checks cover zero, singular wording, multiple-stop notes, changes after deletion and the same count on all five pages including empty routes.

Settings now has a general Manage stops button. Its page shows all five route groups with route and weekday headings and route labels on each stop. Editors use the clicked stop's route instead of the main page's selected route. Add stop offers a route selector. Tests open Monday editing from Tuesday, add a Monthly stop to Friday from Tuesday, and verify deleting a same-named Tuesday stop preserves Monday. Existing stops and notes keep their route associations.

Header and all-route management screenshots were reviewed. The signed APK matches tested CI entries except signatures. Manifest, exact HTML, ZIP integrity, alignment, APK signatures and the retained certificate match to 1.0.2 passed. Package `com.personal.routemanager`, version 1.0.3 (4), 33361 bytes. SHA-256 `cea092661ee10b61a8bd5d7f00811dc09454f785732e984852057c3bde89c263`. Testing used Robolectric and Chromium; no physical phone was used.

# Route Manager 1.0.2 validation

Source: `a9ff3a0cb2378208a713d078c75e419307b13187`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/42525642527). **11 Android tests, 68 Chromium checks and zero lint errors passed.**

Monthly is accepted by the native store, selectable in Add/Edit stop, preserved on save/reopen and displayed on note tags and managed stops. All notes remain visible. Invalid frequencies reject saves without replacing valid data.

Manage stops shows the route and weekday in its page caption and each stop row. Settings identifies the route on its Manage stops button; Add/Edit stop also shows the route and weekday. Native and browser tests create the same customer name on Monday and Tuesday with separate frequencies, edit Monday and verify Tuesday remains unchanged. The route-specific stop-list screenshot was reviewed.

The signed APK matches the tested CI artifact except its signing entries. ZIP integrity, manifest, exact HTML, alignment, signatures and retained certificate match to 1.0.1 passed. Package `com.personal.routemanager`, version 1.0.2 (3), 33361 bytes. SHA-256 `890ca5e8f6ef5946a6866ede13ce8be78f486240ed388849e006beb1b2f99851`. Validation uses Robolectric and Chromium; no physical phone was used.

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
