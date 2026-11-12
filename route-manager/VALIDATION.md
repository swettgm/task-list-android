# Route Notes 1.0.19 validation

Source: `82f10d64ce394335719f8c17f054c4883a57b79a`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43254178231). **18 Android tests, 155 Chromium checks and zero lint errors passed.**

Added red Delegate. All existing notes across routes switch to Delegate once on upgrade, as requested; a persisted marker prevents reapplying this after later edits or clears. New notes default to Delegate. Double tap cycles Delegate, In progress, For me and no status. Tapping any pill clears it. Green and yellow notes remain above delegated and unmarked notes. Tests cover one-time migration across routes and previous statuses, clearing and reopening, new-note defaults, the complete cycle, persistence, editing, account numbers and exports. Native validation rejects malformed and conflicting flags. The red-pill screenshot was reviewed. The earlier edit-check failure was resolved by clicking a stop label rather than the note center, which could now contain a link.

Signed APK: `Route-Notes-v1.0.19.apk`, 41553 bytes. Package `com.personal.routemanager`, version 1.0.19 (20), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `18d29953a0fe0543c747b3bd2a6499fdd96930fa3f118c5957a83ca44c673cee`. The retained certificate matches previous releases. Robolectric and Chromium were used; no physical phone was used.

# Route Notes 1.0.18 validation

Source: `138f0cc16154867bbc9ea475062d8077f3e92468`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43249745572). **17 Android tests, 150 Chromium checks and zero lint errors passed.**

Double tap cycles from no status to green In progress, then yellow For me, then no status. Both marked states remain above inactive notes on their own route page and survive reopening and editing. Tapping either pill clears it directly. Shift+Enter follows the same cycle. Optional boolean flags retain compatibility with existing progress data; native validation rejects malformed and conflicting states. Browser checks cover the full cycle, yellow appearance and top position, reload/edit persistence, restoring ordinary order, failed saves and existing account/export workflows. The yellow-pill screenshot was reviewed.

Signed APK: `Route-Notes-v1.0.18.apk`, 41553 bytes. Package `com.personal.routemanager`, version 1.0.18 (19), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `51f2b53e76ecab9c0a8ef54373c5fe48f525c0bb54ab76f7515ab09501086ec0`. The retained certificate matches previous releases. Robolectric and Chromium were used; no physical phone was used.

# Route Notes 1.0.17 validation

Source: `3041fa17a0189aea1132fcfdd5c59567032ee9c1`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43244135561). **16 Android tests, 146 Chromium checks and zero lint errors passed.**

In-progress notes sort above inactive notes on their own route page. Each group retains the existing creation-date ordering. Stopping a note restores its original position without rewriting its creation time. Browser workflows verify an older note moves above a newer inactive note and returns below it after stopping; progress persistence, editing, double-tap interaction and export checks also passed.

Signed APK: `Route-Notes-v1.0.17.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.17 (18), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `c1ab478d0da2fc93470912a3afeb916660686aa688fce561aa82bcd87f658fba`. The retained certificate matches previous releases. Robolectric and Chromium were used; no physical phone was used.

# Route Notes 1.0.16 validation

Source: `e4f25ed534d04a9b06937ca8658e914a33b49c45`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43240706805). **16 Android tests, 143 Chromium checks and zero lint errors passed.**

Double tapping a route note toggles its persisted green in-progress pill with haptic feedback. Tapping the pill stops progress. Single tap still edits after the double-tap recognition window; keyboard users may use Shift+Enter to toggle. Editing preserves status. Native validation accepts optional boolean status and rejects malformed flags. Browser checks cover double-tap on/off, single-tap editing, persistence across reload and edits, pill stop, rejected writes and keyboard access.

Weekly, Biweekly and Monthly labels retain their width without wrapping and center vertically against multi-line stop names. Geometry passed at 320px and 390px; the in-progress screenshot was reviewed. Deferred editor autofocus now respects another already-focused field; this resolved the earlier automated account-entry failures. Account associations, leading zeros, exports and existing workflows passed in the complete rerun.

Signed APK: `Route-Notes-v1.0.16.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.16 (17), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `72e68079e6443388f2f717fdc5d848cd9af6540a7e51a9d04da2fe450e06a18d`. The retained certificate matches previous releases. Robolectric and Chromium were used; no physical phone was used.

# Route Notes 1.0.15 validation

Source: `06f4717979a63d1f7c27a7e0ac2b95a29101619f`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43199246767). **15 Android tests, 134 Chromium checks and zero lint errors passed.**

All account labels now use `Acct #number`, including route note tags, stop management, autocomplete, selected stop chips, export choices and copied/shared note and stop lists. Both account entry fields request `inputmode=numeric` while retaining text storage and leading zeros. Checks verify numeric input mode in the stop and note editors, separate accounts with matching names, leading-zero persistence, exact export content and matching Copy/Share text. The account export screenshot was reviewed. Physical keyboard appearance remains controlled by the installed Android keyboard.

Signed APK: `Route-Notes-v1.0.15.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.15 (16), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `9ea7125cd20086b681269f82bcb7d3a85ca1cdb406e7171a8eba0edb08f1ec68`. The retained certificate matches previous releases. Robolectric and Chromium were used; no physical phone was used.

# Route Notes 1.0.14 validation

Source: `2f3e7ee19ab6d369ee6dc5a20b57382648e3f41d`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43196371455). **15 Android tests, 131 Chromium checks and zero lint errors passed.**

Export choices alone now show slightly smaller gray headings, with plain parenthesized route/day labels such as `Circle K (G200 Monday)`. Note instructions remain white and are bold. Regular route pages and note editors are unchanged. Checkbox centering, wrapping, full-height instructions, selected Copy/Share output and persistence workflows passed. The compact export screenshot was reviewed. One earlier browser run hit an editor-focus timing failure; the automated note helper now waits for scheduled focus before typing.

Signed APK: `Route-Notes-v1.0.14.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.14 (15), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `63d9d63c116de914045dd0fcc9e9438abcadba6afac5183cb71e8f9d1bf29926`. The retained certificate matches previous releases. Robolectric and Chromium were used; no physical phone was used.

# Route Notes 1.0.13 validation

Source: `dba7749b16b1946ef69ef1d497172ea9a70d5ac4`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43193750930). **15 Android tests, 131 Chromium checks and zero lint errors passed.**

Export choices show the route and weekday beside the stop name as `Circle K · (G200 · Monday)`, with natural wrapping. Heading and note text are white and the same size. Checkboxes remain centered against the stop name alone. Note exports use the smaller literal Unicode U+2022 circle (•). Full-height note selection, unchecked defaults, collapsed preview, route grouping, account numbers, exact Copy/Share content and persistence checks passed. Compact and full-height export screenshots were reviewed.

Signed APK: `Route-Notes-v1.0.13.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.13 (14), Android 8.0+. ZIP integrity, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `620ca280327cc9cca887d5c4036836a3feb04dc65d943c3b531b61b627ae467e`. The retained certificate matches Route Notes 1.0.12, so this installs as an update. Robolectric and Chromium were used; no physical phone was used.

# Route Notes 1.0.12 validation

Source: `a8f352b446eed25de4c2a0201f8cc7a238c97b4f`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43191077231). **15 Android tests, 131 Chromium checks and zero lint errors passed.**

Renamed the launcher application label, page title, visible app/version text, error wording and clipboard label to Route Notes. The installed identity remains `com.personal.routemanager`; storage keys, native bridge and shared-preference formats are unchanged. Existing route notes, stops, account numbers, settings and offsets therefore use the same saved state. All prior export, checkbox, layout and persistence checks passed. The compiled resources contain the new launcher label.

This update includes full-height unchecked export choices across all routes, no route dropdown, a collapsed Preview, literal Unicode ● note markers, grouped route/day output, a content-sized window with safe scrolling and fixed Close/Copy/Share actions, and checkboxes centered against the stop name.

Signed APK: `Route-Notes-v1.0.12.apk`, 37457 bytes. Version 1.0.12 (13), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest, compiled name and v2/v3 signatures passed. SHA-256 `f0541b030f9f58f79b302d4efe294a434cff1c682dd56e5bc163355d8700a5f7`. The retained signer matches Route Manager 1.0.11, so this installs as an update. Robolectric and Chromium were used; no physical phone was used.

# Route Manager 1.0.11 validation

Source: `f45f7295fe738cc54d02581373351dd89055bf88`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43189304207). **15 Android tests, 131 Chromium checks and zero lint errors passed.**

All export choices start unchecked, with no route dropdown. Entries show their full note text and each checkbox centers vertically against only the stop name. The dialog grows with content up to the safe viewport height, then its fields scroll above the fixed Close/Copy/Share footer. Preview starts collapsed and opens on demand.

Notes export with literal Unicode U+25CF circles (●), grouped by route number and weekday. Tests cover exact selected Copy/Share text, saved numbers and multiline content, unchecked defaults on reopen, preview toggling, long note heights, stop-title checkbox alignment, all-route choices, unchanged saved data, compact/empty states and footer fit at 320x420, 390x844 and 600x844. Stop-only lists retain numbering. Long and compact export screenshots were reviewed.

Signed APK: `Route-Manager-v1.0.11.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.11 (12), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `3f9c6363705c4695797c75c982fdd20e49a4e2aad338bce111c717bfaa7505dc`. The retained certificate matches prior updates. Robolectric and Chromium were used; no physical phone was used.

# Route Manager 1.0.10 validation

Source: `1861a19871bfd178d8e669cde3da2ab83dff69d8`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43132372230). **15 Android tests, 135 Chromium checks and zero lint errors passed.**

Export opens All routes from every route page. Checkbox entries identify their route number and weekday; selected notes export beneath matching route/day headings with consecutive numbering across groups. Optional route filtering remains available. Tests verify all-routes defaults, route labels, cross-route selections, numbering and exact Copy/Share equality. Saved stop names, numbers and note bodies remain covered.

Visible export title, Choose entries, Select all, selection count and Numbered list caption are removed. Individual checkboxes, route selector, preview and action buttons remain. The dialog and preview retain accessible names. Checkbox margins are reset and checkbox rows center their controls vertically throughout Route Manager, including multi-line export entries. Export checkbox geometry, small-screen action visibility and removed-caption checks passed. The cleaned all-routes export screenshot was reviewed.

Earlier browser attempts hit different existing save/account assertions. The diagnostic full rerun passed all workflows, including save rejection, account preservation and exports; note-save logic was unchanged by this release.

Signed APK: `Route-Manager-v1.0.10.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.10 (11), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `eb326d2ceaf119eff9145ae06c6b40fb229fbfab32b327d5e54beae9fca69c2a`. The retained certificate matches prior updates. Robolectric and Chromium were used; no physical phone was used.

# Route Manager 1.0.9 validation

Source: `cc5f1e855579842bc4520eb4ecf93f78bd153533`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43128168435). **15 Android tests, 130 Chromium checks and zero lint errors passed.**

Both export dialogs show checkbox choices, Select all and a selection count. All entries start selected; unchecked entries are excluded from preview, Copy and Share. Numbering is generated after filtering and runs consecutively from 1, including across route headings in stop-list exports. Saved account numbers remain unchanged. Choices are scoped to the open dialog, survive route/day switching and reset on reopening. Empty selections disable Copy and Share. Notes/stops remain untouched.

Tests cover skipped earlier/middle entries, 1-to-total renumbering, exact selected Copy/Share payloads, mixed/none/all selections, per-route retention, reset on reopen, saved-data equality and visible export footers at 320x420 and 390x844. Existing full-list exports remain covered. The selected-notes preview screenshot was reviewed.

Signed APK: `Route-Manager-v1.0.9.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.9 (10), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `35cd5c46b85ac4d2b2220c42c95892e99590f2f57342a8434146f7203559f8ab`. The retained certificate matches prior updates. Robolectric and Chromium were used; no physical phone was used.

# Route Manager 1.0.8 validation

Source: `a56ae8d6a602a6d19775cee88ec617f588ecb187`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43125611020). **15 Android tests, 116 Chromium checks and zero lint errors passed.**

Export previews explicitly align text left. Note export removes generated indentation and leading whitespace on each note line, preserving note text, line breaks, stop names and saved numbers. The existing exact-output and Copy/Share equality checks pass with the left-aligned format. Saved notes are unchanged. The note-export screenshot was reviewed.

Signed APK: `Route-Manager-v1.0.8.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.8 (9), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. SHA-256 `b229bc08d2b1a23c320775c9847ae3e93236298d0583b31bba741e7af8cccbbd`. The retained certificate matches prior updates. Robolectric and Chromium were used; no physical phone was used.

# Route Manager 1.0.7 validation

Source: `2b21b66d65e099856bbea560c7ea4c1fc193bc2c`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43123478569). **15 Android tests, 116 Chromium checks and zero lint errors passed.**

The header exports the currently viewed day’s notes. A five-day selector changes the preview; Copy and Share receive the exact previewed plain text. Entries group complete notes under numbered stop names and saved account numbers, retaining leading zeros and separating same-named accounts. Shared notes appear under each assigned stop. Unassigned notes and notes whose saved stop was removed remain included as route notes. Stops without notes and other days’ notes are excluded. Empty-day exports disable Copy and Share. Exporting never writes saved route state. The existing managed-stop export remains covered.

Checks cover exact multiline formatting, multiple notes, missing numbers, same names across accounts and days, defaulting to the viewed day, clipboard/share payload equality and read-only export. The export screenshot was reviewed.

Signed APK: `Route-Manager-v1.0.7.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.7 (8), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment and v2/v3 signatures passed. SHA-256 `c3adc2faf00bdbead46cf748f228177c59bb5a66984b5d76880743ffe1d16e06`. The retained signing certificate matches prior updates. Robolectric and Chromium were used; no physical phone was used.

# Route Manager 1.0.6 validation

Source: `cabf049cf0fcaae117e0acf47689690652ad1aad`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/43121212431). **15 Android tests, 107 Chromium checks and zero lint errors passed.**

Launcher artwork is now navy on white. The in-app theme, saved routes, notes, stops, accounts and day-ahead settings are unchanged. Existing workflow coverage passed with the updated version.

Signed APK: `Route-Manager-v1.0.6.apk`, 37457 bytes. Package `com.personal.routemanager`, version 1.0.6 (7), Android 8.0+. ZIP integrity, exact packaged HTML, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches 1.0.5. SHA-256 `2d66a6d31933890bc1d6c3919d853f9a452444663f51114e996d36c19ea43f4b`. Tests used Robolectric and Chromium; no physical phone was used.

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
