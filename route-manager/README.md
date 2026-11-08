# Route Manager

A separate, offline Android app for route notes. Installs alongside Tasks with its own data and no widget.

- G200 Monday, G300 Tuesday, G400 Wednesday, G500 Thursday, G600 Friday.
- Opens the current weekday's page; weekends open Monday. Swipe between routes; both ends loop around.
- Save stop names with the disk button, or save a typed new stop when adding a note. Autocomplete saved names and select multiple stops per note.
- Weekly and Biweekly labels are informational: notes always remain visible.
- Add, edit and delete notes; maintain saved stops; adjust colors, gradient and text size.
- Delete requires two taps within three seconds. Removing a stop keeps its notes.
- Local Android storage only; no account, network permission, widget or reminders.

Build: Gradle 8.11.1, Java 21, Android SDK 35. Run `gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.
Browser checks: `npm install --no-save --package-lock=false playwright@1.56.1`, then `node tools/browser-smoke.mjs` with Chrome installed.

The independent Build Route Manager workflow builds and checks this project. Signing keys must stay private; never commit a keystore.
