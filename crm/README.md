# Relationship Manager

[Download RM 1.0.18](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/Relationship-Manager-v1.0.18.apk). Android 8 or later.

The tab selector matches Tasks with a filled selected tab, 40 px controls, 12 px corners and an inset inside a 2 px outer border. Holding the title opens Personal notes with the browser touch highlight disabled. Verified with 32 Android tests, lint and 157 browser checks.

A separate Android app for staying in touch, with Friends/Family and Interests pages. Link selected phone contacts (including contacts synced to the phone by Google), add notes, choose a repeat interval and reminder date/time, and record when you last contacted someone. Interests entries have In progress, Hiatus and DTF labels.

Linked names and phone numbers refresh on launch/resume. Relationship Manager does not write to the phone contacts database or send messages automatically. Removing a person from Relationship Manager leaves the phone contact intact. Notes and schedules are saved in app-private storage. Notifications repeat on a calendar cadence; Contacted today resets that cadence. Delivery uses Android’s battery-aware alarms and may be delayed by battery restrictions.

Build: `gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` (JDK 21, Gradle 8.11.1, Android SDK 35). Browser checks: `node tools/browser-smoke.mjs` with Playwright installed.

Friends/Family and Interests are separate pages; entries are added on the current page. Snapchat, Discord and In person tags can be combined. Editors use compact single-line actions and a red trash icon with a second-tap confirmation. Existing Interested/Dating entries display In progress; Paused displays Hiatus. Existing notes and reminders are preserved. Email is no longer shown or read from linked contacts.

Swipe left from Friends/Family to Interests, and right to return. Vertical scrolling, short gestures and editor interactions do not change categories. The Friends/Family tab is wider, and both tab labels stay on one line.

The home screen has no search bar or separate contact-import button. Use the floating + button to choose Add manually or Add from contacts. Import dialogs keep their footer visible with long lists, wrapped headings, larger text or a reduced viewport. Tag buttons and saved tags use yellow Snapchat, purple Discord and white In person icons. The launcher icon uses a rounded white square with a bold dark single-person outline.

The Snapchat tag uses the standard Simple Icons ghost silhouette in yellow. See [icon sources](ASSETS.md).

The launcher now copies the VectorDrawable setup and rounded white background path used by Route Notes and Meditation. Native graphics tests compare the rendered alpha boundary against both actual companion rounded-square background paths, pixel-for-pixel. The single-person symbol remains bold and inset inside that shared boundary.

The manifest uses the same icon and roundIcon resource method as the companion apps, without vendor-specific container overrides.

Cards use smaller app text, tighter spacing and aligned title/status, tag/detail and action rows. Tags show larger icons without visible text, with accessible names retained. Interests omit Contacted today and unused contact-history wording; Call/Text remain available for saved phone numbers. Notes and long names wrap in full instead of being clipped.

Status appears immediately beside the name: Hiatus red, In progress yellow and DTF green. Click status, tags, notes, last-contacted dates or reminder text for a focused quick editor; clicking the name opens the full person editor. Editor scroll areas include space for focus rings so selected fields do not clip at the edges.

New entries start with reach-out notifications off. Enable Reach-out reminders per person when needed; existing reminder settings are preserved.

Relationship Manager updates the existing CRM app in place. The package name, private data store and signing identity are retained.

Interests can include Next steps, with one step per line. Non-empty steps appear on the main card beside solid right-pointing arrows and can be tapped for focused editing. The full editor also supports adding and clearing them; Friends/Family does not show this field. Existing entries without next steps remain unchanged.

Profiles in either tab can include a manually entered Location describing where you know someone from. It appears below the name with a pin icon when filled, and tapping it opens focused editing. No device location or geocoding is used. Existing profiles without this optional field remain unchanged.

Status markers follow the Tasks app pill styling: bold uppercase labels, colored circle markers, rounded ends, matching padding and letter spacing, with the existing red/yellow/green status meanings. The app heading and gear are smaller and remain on one line.

Tap the dimmed area outside any app dialog to close it with the same discard behavior as Cancel. Inside taps and drags crossing the panel edge do not dismiss it.

Location and next-step text use the same compact caption size, with proportionally smaller arrows and pins. Location and next steps appear above the tag icons on main cards.

The launcher label is RM; the in-app title remains Relationship Manager. Location and next-step details share one muted color and text size, with a baseline-aligned arrow.

Cards combine location and next steps on a single wrapping line above tags: location → steps. The arrow appears only when both are present; no Next step caption appears on cards. Each field remains separately editable.

Location and step details sit beside the tag icons, vertically centered and using the same typography and muted color as Friends/Family metadata. Locations do not display a pin icon.

Hold a card briefly, then drag it up or down to reorder Friends/Family or Interests. Cards move aside and an opaque lifted copy follows the finger. Each tab saves its own order; new entries appear after manually ordered cards. Short gestures still scroll or switch tabs.

Friends/Family and Interests follow the finger during horizontal swipes and finish with the Tasks app easing and timing. Short or cancelled swipes return to the original page. Arrow placement follows the rendered text ink metrics rather than a fixed baseline offset.

Arrow centering uses capital-letter height, so descenders in names or step text cannot pull the arrow downward.

Hold the Relationship Manager title to open Personal notes, separate from Friends/Family and Interests. Add a name, choose Past partner or Shown interest, and write notes. Entries can be edited or deleted with confirmation and are kept in app-private storage. Keyboard Enter/Space on the title opens the same pane.
