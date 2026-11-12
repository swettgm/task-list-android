# Route Notes

A separate, offline Android app for route notes. Installs alongside Tasks with its own data and no widget.

- Persistent 1-day/2-day-ahead checkboxes shift the route opened for the weekday, wrapping Friday to Monday. Turn them off to restore the current day.
- Optional account numbers keep same-named stops separate; account numbers retain leading zeros.
- Manage stops → Export list previews a clean numbered list for all routes or one route. Copy or Share it into a text message.
- Route tabs highlight and show a dot when they contain saved notes.
- Stop saves capitalize word starts while preserving existing internal capitalization, such as UPS or UniFirst.
- Each route header shows the total number of notes across all five pages. A note with several stops counts once.
- G200 Monday, G300 Tuesday, G400 Wednesday, G500 Thursday, G600 Friday.
- Opens the current weekday's page; weekends open Monday. Swipe between routes; both ends loop around.
- Save stop names with the disk button, or save a typed new stop when adding a note. Autocomplete saved names and select multiple stops per note.
- Weekly, Biweekly and Monthly labels are informational: notes always remain visible.
- Add, edit and delete notes; maintain stops for all five routes from Settings, grouped by route and weekday, with a route choice when adding a stop; adjust colors, gradient and text size. The main route page shows all notes without a filter.
- The compact note field starts on one line and grows with text; the keyboard keeps editor headings and action buttons visible.
- Delete requires two taps within three seconds. Removing a stop keeps its notes.
- Local Android storage only; no account, network permission, widget or reminders.

Build: Gradle 8.11.1, Java 21, Android SDK 35. Run `gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.
Browser checks: `npm install --no-save --package-lock=false playwright@1.56.1`, then `node tools/browser-smoke.mjs` with Chrome installed.

The independent Build Route Notes workflow builds and checks this project. Signing keys must stay private; never commit a keystore.

The header’s Export notes button opens notes from all five routes. Choose entries across the week, then Copy or Share the list grouped by route number and day. Entries include stop names, saved account numbers (including leading zeros), and full notes. Shared notes appear under each associated stop; general route notes are kept. Saved stops without notes are omitted. The separate managed-stop list export remains available.

Export previews, copied lists and shared lists align all lines to the left, including multiline notes.

Choose entries in either export using checkboxes. Only checked entries appear in the preview, Copy and Share, with literal smaller Unicode circle markers (•) for notes; stop lists retain consecutive numbering. Entries start unchecked each time the export dialog opens.

Export dialogs show individual entry checkboxes and Close/Copy/Share actions. The optional Preview opens in a disclosure; there is no route dropdown or selection count. Full note text is shown, and the window grows to fit content up to the screen height.

Export checkboxes align vertically with the stop name; route and note text sit beneath the title independently.

Export choices show slightly smaller gray headings with route/day beside the stop name, for example Circle K (G200 Monday), wrapping when needed. Note instructions are larger, bold and white.

Account numbers use `Acct #number` consistently in labels and exports. All account fields request the numeric keypad and retain leading zeros.

Double tap a note to toggle its saved green in-progress pill; tap the pill to stop. Single tap edits, and Shift+Enter toggles progress for keyboard users. Visit frequency labels remain centered and unbroken beside wrapping stop names.

In-progress notes appear first within their route. Stopped notes return to their normal creation-date order.
