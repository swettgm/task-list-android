# RM 1.0.13 verification

Source: `d15d6db3262c136b22e9027a38bc84614d56a75d`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44218583318). **27 native Android tests, 127 browser checks and zero lint errors passed.**

Location and step details now sit alongside the label icons, vertically centered with them and inheriting the shared Friends/Family metadata size, line height and muted color. The location pin is removed. Details continue to wrap naturally, with an arrow only when both location and steps are present, and each field remains separately editable. Browser assertions verify the absence of the pin, shared typography/opacity and placement beside the icons.

APK: `Relationship-Manager-v1.0.13.apk`, 41631 bytes, package `com.personal.crm`, version code 14. SHA-256 `1a4ef0d20d0ab633fcbf1adfe580916c27baabaaf17be5a0370ced96dbe7bfd5`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over the current app to retain data. Native checks use Robolectric/native graphics and browser checks use Chromium. No physical phone was tested.

# RM 1.0.12 verification

Source: `b6ae9e17e0ef6ec0a9d90416e316a6c85ef12b92`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44216299498). **27 native Android tests, 127 browser checks and zero lint errors passed.**

Cards combine location and next steps into one naturally wrapping line above tag icons. The Next step caption is removed. A single filled arrow divides location from steps only when both are present; steps without location have no arrow, and location alone has no trailing arrow. Multiple saved steps remain visible, separated by semicolons. Location and step text retain matching compact typography and muted color. Separate accessible inline controls preserve focused editing and swipe navigation.

APK: `Relationship-Manager-v1.0.12.apk`, 41631 bytes, package `com.personal.crm`, version code 13. SHA-256 `e127dffb5573a4f2581fa9b3d0940af25e0cdd91e291b4030c87cbe0a871288b`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over the current app to retain data. Native checks use Robolectric/native graphics and browser checks use Chromium. No physical phone was tested.

# RM 1.0.11 verification

Source: `c8453a11c8bde0014a2f087c0b8ba10c2966f9e5`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44200979195). **27 native Android tests, 127 browser checks and zero lint errors passed.**

The launcher label is RM; the in-app title remains Relationship Manager. Location and next-step caption/text/arrow share the same color, effective opacity, normal weight and compact text size. The smaller arrow aligns with the text baseline instead of its top edge. Location and next steps remain above tag icons. Browser checks cover matching typography, color/opacity, arrow size/alignment and row order. The native manifest-label test verifies RM.

APK: `Relationship-Manager-v1.0.11.apk`, 41631 bytes, package `com.personal.crm`, version code 12. SHA-256 `27619faad45203db478901981f14544cdc2a74f49a72b05b6e9d645f73ec13dc`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over the current app to retain data. Native checks use Robolectric/native graphics and browser checks use Chromium. No physical phone was tested.

# Relationship Manager 1.0.10 verification

Source: `8c97f97d61d0cde18dfe63e2975ff81cac753957`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44198133715). **27 native Android tests, 127 browser checks and zero lint errors passed.**

Location, the Next step caption and step text share the same compact size. Location pins and solid step arrows are proportionally smaller. Both rows appear above the tag icons on main cards. Browser checks verify equal text sizes, arrow dimensions and row ordering, alongside existing editing, persistence and dialog behavior.

APK: `Relationship-Manager-v1.0.10.apk`, 41631 bytes, package `com.personal.crm`, version code 11. SHA-256 `11846cf16b8066edffedd82fddc2daf2132ebd01745f64bbdebd4c6e287e45b5`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over the current app to retain data. Native checks use Robolectric/native graphics and browser checks use Chromium. No physical phone was tested.

# Relationship Manager 1.0.9 verification

Source: `0516afeff2dfa7195d53a1a12aaa8b43fae94aa2`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44196072579). **27 native Android tests, 126 browser checks and zero lint errors passed.**

Profiles in both tabs support an optional manually entered Location describing where the user knows someone from. Filled locations appear below the name with a pin and can be tapped for focused editing. Older records remain valid; the optional string is limited to 500 characters and is preserved during unrelated profile/reminder updates. No device location permission or geocoding is added.

Status pills follow the Tasks app styling with colored circle markers, bold uppercase text, rounded ends, matching padding, weight and letter spacing. Existing yellow In progress, red Hiatus and green DTF meanings are retained. The smaller app heading and reduced gear remain on one line at 320/390px. The shared dialog component now closes on a deliberate outside tap, discarding unsaved edits like Cancel. Inside/padding taps and drags crossing the edge do not dismiss it.

Checks cover location save/reload/quick edit/cancel/failed write, both groups, native contact-update preservation and invalid-data rejection; task-style pill rendering; header bounds; outside dismissal for Add, full editor, quick editors, status, Settings and contacts, plus inside/drag protection. Existing notes, next steps, tags, reminders and appearance are retained.

APK: `Relationship-Manager-v1.0.9.apk`, 41631 bytes, package `com.personal.crm`, version code 10. SHA-256 `2d4fb88ed6ece2faff31a4cbade2fd9623fb5c064238aa4afb7f88afaa653e55`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over the existing app to retain data. Native checks use Robolectric/native graphics and browser checks use Chromium. No physical phone was tested.

# Relationship Manager 1.0.8 verification

Source: `6dfd1498f8cf14195332e3e82ba6910e639d856d`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44184702911). **25 native Android tests, 107 browser checks and zero lint errors passed.**

Interests have an optional Next steps field in Add/Edit person, with one step per line. Non-empty steps display on the main card with a solid right-pointing arrow for each line, beside a Next step/Next steps caption. Tapping the section opens a focused editor. Text wraps in full on small screens; blank lines do not create arrows. Clearing the field hides the card section. Friends/Family do not show this field, and notifications remain off by default for new entries.

The additive nextSteps string is optional for older records, bounded to 4000 characters and retained by unrelated profile/reminder updates. Native checks cover save/reload/contact-update preservation and rejection of invalid/oversized values without replacing stored data. Browser checks cover full-editor creation, card arrows, multiline wrapping, quick save/cancel/failed writes, reopening, unrelated edits, swipe navigation, clearing and Interests-only scope. Existing notes remain separate and are retained.

APK: `Relationship-Manager-v1.0.8.apk`, 41631 bytes, package `com.personal.crm`, version code 9. SHA-256 `2dbd8d16a6cbbbc865b3d456dfec9beef106f37b5307de6f033264235aa4a9cd`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over Relationship Manager to retain data. Native checks use Robolectric/native graphics and browser checks use Chromium. No physical phone was tested.

# Relationship Manager 1.0.7 verification

Source: `ca03de8ab353aff3f318d8cc95b601daca104158`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44146953177). **23 native Android tests, 90 browser checks and zero lint errors passed.**

CRM is now Relationship Manager in the Android application label, home heading, page title and user-facing app-name messages. The longer heading wraps without overlapping the settings icon at 320/390px. Native checks verify the actual application label. The existing package com.personal.crm, private preference store, WebView origin, bridge callbacks and signing identity are retained so this installs over CRM without losing people, notes, tags, reminders or appearance settings. Existing behavior and icon artwork are retained.

APK: `Relationship-Manager-v1.0.7.apk`, 41631 bytes, version code 8. SHA-256 `27cc4014eea146672af518fed10859e2dea9fe643c8d05fb120f73e7556f42f0`. New stable alias Relationship-Manager.apk and legacy CRM.apk point to the same signed bytes. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Native checks use Robolectric/native graphics and browser checks use Chromium. No physical phone was tested.

# CRM 1.0.6 verification

Source: `5f84084357c79bd1fb9a59458499bdf22875641d`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44135479634). **23 native Android tests, 90 browser checks and zero lint errors passed.**

With the user's approval, CRM copies the VectorDrawable launcher setup and exact rounded white background path from Route Notes/Meditation. Raster launcher resources and vendor container overrides are removed. Native graphics tests compare CRM's rendered alpha boundary with Android-rendered background paths read from both actual companion sources, pixel-for-pixel. The bold single-person foreground is inset inside that shared boundary. Rounded outer corners remain transparent. No physical launcher was tested.

Status is immediately beside the name and vertically centered: Hiatus is red (#a61b32), In progress yellow (#e7bc35) and DTF green (#176b42). Status, tags, notes, last-contacted dates and reminder text open focused quick editors. Quick edits update only their own fields; save/cancel/failure and swipe behavior are checked. Clicking the name opens the full editor. Editor scroll areas and footers leave space for complete focus outlines, including both sides of the Status selector.

New entries in both tabs default to notifications off. Tests verify that saving a default entry retains reminders=false and does not request notification permission. Reminder opt-in and existing reminder settings are retained. Existing entries are not reset. Compact cards, icon-only tags, full notes and contact links remain available.

APK: `CRM-v1.0.6.apk`, 41631 bytes, package `com.personal.crm`, version code 7. SHA-256 `2a73b09d0bc626d3f9baf6b908aa991126bf9fcd8fbf424d5f7b60913252992f`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over CRM to retain data. Native tests use Robolectric/native graphics and browser checks use Chromium. No physical phone was tested.

# CRM 1.0.5 verification

Source: `3b0014923040a0ebe72e6d9c6d4f937dcfd7cdc7`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44122778755). **23 native Android tests, 67 browser checks and zero lint errors passed.**

The launcher uses a new transparent density-specific PNG resource rather than relying on the previous internal vector scale. Native Android graphics tests load the actual manifest icon and measure its visible bounds: 52x54 pixels versus the previous vector's 60x62 on the same 108x108 canvas. Tests verify a BitmapDrawable with alpha, centered smaller bounds and empty pixels around the symbol without an opaque background plate. The manifest includes Samsung original-icon compatibility metadata; its packaged value is tested. PNG sources are in crm/artwork. No physical launcher or Samsung firmware was tested.

The interface retains the navy-to-gray theme, rounded controls and white selected borders. App text is reduced to 16px, fields and spacing are tighter, and cards align title/status, tag/detail and action rows vertically. Tags are larger icons only, with accessible names retained. Interests omit Contacted today and unused contact-history wording; saved phone numbers still offer Call/Text. Friends/Family retain Contacted today. Full notes and wrapping names remain visible. Representative cards measured about 149px for Friends/Family with notes/tags/three actions and 117px for Interests. Browser checks cover compact geometry, tag icons, aligned wrapping titles, full multi-line notes, selection, persistence, legacy records, imports and modal footer sizing.

APK: `CRM-v1.0.5.apk`, 54355 bytes, package `com.personal.crm`, version code 6. SHA-256 `76e14ae2707e78f8f0f043e6ea5cdc990592cf958a216d19c620f634506437bd`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over CRM to retain data. Native tests use Robolectric with native graphics for the icon checks; browser checks use Chromium. No physical phone was tested.

# CRM 1.0.4 verification

Source: `403b3ed1df10e9baf1c3ee8b413a56ed36c2c590`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44113389891). **20 native Android tests, 58 browser checks and zero lint errors passed.**

The launcher symbol is scaled to 85% around the center of the existing 108dp canvas, making it 15% smaller to align with the companion app symbols. The transparent background and bold single-person design are retained. The VectorDrawable has a centered 0.85 scale group; other app icons use the same 108dp canvas. No application data or UI behavior changes.

APK: `CRM-v1.0.4.apk`, 41631 bytes, package `com.personal.crm`, version code 5. SHA-256 `af0bbde75f371216dd2090cb771c70e1addf793c430fb5ecc1c1635ca41fe41d`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over CRM to retain data. Native tests use Robolectric and browser checks use Chromium; no physical phone was tested.

# CRM 1.0.3 verification

Source: `076aedd62107cb92822f9c19733cdcd431025aed`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44109083788). **20 native Android tests, 58 browser checks and zero lint errors passed.**

The launcher icon is now a single person with a 7-unit stroke instead of 5. Its opaque background rectangle is removed; the retained dark outline has a transparent background. An SVG render of the vector confirms alpha-zero corners and visible opaque strokes. Snapchat tags now use the standard Simple Icons v16 ghost silhouette in yellow, replacing the approximate drawing in both selectors and saved chips. Icon attribution is in crm/ASSETS.md. Existing app data and other behavior are retained.

APK: `CRM-v1.0.3.apk`, 41631 bytes, package `com.personal.crm`, version code 4. SHA-256 `aa71ee1d901d68c37fc89e40b0970e4b68b12c811a275439c6b8a60497ef93e4`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over CRM to preserve existing data. Native tests use Robolectric; browser checks use Chromium. No physical phone was tested.

# CRM 1.0.2 verification

Source: `2b81c10de7aad8b5fa16a3f235376634f6eb8534`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44106037653). **20 native Android tests, 58 browser checks and zero lint errors passed.**

Friends/Family and Interests replace the previous tab labels. Friends/Family has more width, while both labels stay on one line. Swipe left to Interests and right to Friends/Family, including over person titles; vertical scrolling, short gestures, edge swipes and editor interactions do not change pages accidentally. The home search and separate contact-import button are removed. Floating + offers Add manually or Add from contacts.

Dialogs now size the scrolling content around their actual heading and fixed action footer, keeping Cancel/Add contacts visible with long contact lists, wrapped headings, larger text and shorter viewports. Checkboxes use the same 19px rounded square, 2px border and drawn checkmark as Route Notes. Tag selectors and saved chips show a yellow Snapchat ghost, purple Discord mask and white In person silhouette. The launcher icon is inverted to a light background with dark people outlines. Existing notes, groups, tags, contact links and reminders are retained; internal storage group keys are unchanged.

Browser checks use actual touch gestures, verify tab geometry at 320/390px, check the modal footer with 62 contacts at 320x700, 390x700 with 24px text and 390x480, and cover the add menu, contact import, icon colors, checkbox tick, saved tags, editing, persistence and legacy entries. Native checks use Robolectric; browser checks use Chromium. No physical phone was tested.

APK: `CRM-v1.0.2.apk`, 37535 bytes, package `com.personal.crm`, version code 3. SHA-256 `cffb205a9333eff3c2ec2235043d3bdf6c44d705ed7ea85a2e1d05573cc73342`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over CRM to preserve existing data.

# CRM 1.0.1 verification

Source: `248d683083bfdeb66cd98398d606a08244d94de9`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44100173331). **20 native Android tests, 40 browser checks and zero lint errors passed.**

People is now Friends. Friends and Dating editors stay on their respective page without a Page selector. Dating statuses are In progress, Hiatus and DTF; old Interested/Dating values display In progress and Paused displays Hiatus. Existing entries, notes, contact links and reminders are retained. Snapchat, Discord and In person tags support multiple selections, persist through editing/reopening and appear on cards and in search. Email fields/actions are removed, and linked contacts no longer read email addresses. The settings gear matches Route Notes and has no border/background. Editor actions are shorter and remain on one line; Delete is a red trash icon with second-tap confirmation.

Browser checks cover real button geometry at 320/390px, the shared gear, absent selectors/email, all new statuses, independent tags, persistence, legacy records, imports, reminders, failed writes and safe note rendering. Native tests cover new categories/tags, invalid/duplicate tag rejection and backward-compatible reads alongside reminder/storage checks. Native tests use Robolectric and browser checks use Chromium; no physical phone was tested.

APK: `CRM-v1.0.1.apk`, 37535 bytes, package `com.personal.crm`, version code 2. SHA-256 `b8f7cff5ff52c3e612e0dea147ff182139bbd7da44385f299baf272e449ae800`. Artifact hashes, ZIP integrity, manifest metadata, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over CRM to keep saved data.

# CRM 1.0.0 verification

Source: `9cf4a1ee62b931ed01f542c04e8330ce8ada8d81`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44095884711). **17 native Android tests, 22 browser checks and zero lint errors passed.**

A separate Android app with People and Dating pages, selected phone-contact linking, contact details refreshed on launch/resume, notes, per-person daily/weekly/monthly reminders, and Contacted today to reset the schedule. Dating stages are Interested, Dating and Paused. Import is explicit, skips existing links and enables reminders only when the user configures them. Removing a CRM entry never deletes a phone contact. Linked identity fields are read-only until unlinked.

Checks cover calendar month boundaries and daylight saving time, earliest-reminder scheduling, notifications advancing once, reset on contact, cancelled schedules, denied notification permission, corrupt-storage preservation, stale snapshot rejection, app-private storage, WebView network isolation, cross-page editing and persistence, contact selection, failed writes and editor layout at 320/390px. The boot/package/time-change receiver reschedules saved reminders. Alarms are battery-aware and Android may delay delivery. Native checks use Robolectric and browser checks use Chromium; real contact-provider synchronization and notification delivery on a physical phone remain device checks.

APK: `CRM-v1.0.0.apk`, 37535 bytes. SHA-256 `2e80ac94de7728319c58ef3d6bb302f5e3188d8c6f2e7d8be9ce97139587c446`. Package `com.personal.crm`, version code 1, min SDK 26, target SDK 35. Artifact hashes, ZIP integrity, manifest metadata, alignment, APK signing and exact non-signature content equality to CI passed. Retained local signing certificate SHA-256 `9ce9de5ae8d5cf8315b32d2c8e65cfe7f45e99c4562066af6a2bd2acb92f0310`. No signing material or actual contact records are published.

# Route Notes 1.0.21 verification

Source: `117a4bf175dff380cbdcadb2c9fe8457c29c372f`. [Build and checks](https://github.com/swettgm/task-list-android/actions/runs/44095568370). **18 native Android tests, 166 browser checks and zero lint errors passed.**

Each note export title has a pencil to edit its original note. A stop with several notes offers a note chooser. Saving, cancelling, deleting or using Back returns to refreshed export choices with selections, scroll position and Preview expansion preserved. Cross-route editing keeps the home route unchanged. Pencil actions do not toggle selection checkboxes; status labels stay out of the exported text.

APK: `Route-Notes-v1.0.21.apk`, 41553 bytes. SHA-256 `e361ec65ad8a14a5adaf901e95816a385af5ac1470e496779a32e83ac3395586`. Package `com.personal.routemanager`, version code 22. Artifact hash, ZIP integrity, package/version, alignment, retained signing certificate and exact non-signature contents compared to CI passed. Install over the current app to retain data. Native checks use Robolectric; browser checks use Chromium. No physical phone was tested.

# Route Notes 1.0.20 verification

Source: `a1e4d6cce7222a14b911920ae64037ac17b107b8`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/44074121754). **18 native Android tests, 157 browser checks and zero lint errors passed.**

Export selection renders each note’s saved status beside its own body. Notes sharing a stop retain distinct per-note pills. Route-only notes also show their status. The entry text passed to Preview, Copy and Share remains unchanged. Browser checks include mixed-status grouped entries and exact output comparisons. Checkboxes remain centered against the stop title, and exports do not write to saved notes.

APK: `Route-Notes-v1.0.20.apk`, 41553 bytes. SHA-256 `ddc6d4a79ee19a0094db2fc9415206d80eb8667ef9e2165cda3f95d3234922d4`. Package `com.personal.routemanager`. Artifact hashes, ZIP integrity, package/version, alignment, retained signer and exact non-signature content equality to CI passed. Updates retain existing app data. Native checks use Robolectric; browser checks use Chromium. No physical phone was tested.

# Habits 1.0.7 verification

Source: `a1e4d6cce7222a14b911920ae64037ac17b107b8`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/44074121766). **37 native Android tests and zero lint errors passed.**

Widget double taps cycle empty → X (state 2) → check (state 1) → empty. The in-app single-tap sequence is unchanged. No persisted state meanings are remapped, so existing saved checks and X marks retain their meanings. The two-tap gate, per-cell/date pairing, future-day locks and haptics remain. Accessible widget descriptions announce the actual next state. Tests cover the reversed sequence, single/slow taps, saved-history meaning and stale/future taps.

APK: `Habits-v1.0.7.apk`, 37694 bytes. SHA-256 `a836f7ce24505fcb5fb6e7fccef3eb9edd0e6ef55243b6811517d644d0b69dee`. Package `com.personal.habits`. Artifact hashes, ZIP integrity, package/version, alignment, retained signer and exact non-signature content equality to CI passed. Updates retain existing app data. Native checks use Robolectric; browser checks use Chromium. No physical phone was tested.

# Meditation 1.0.13 verification

Source: `05502a8310e9d412f9a67527adf32b45327acaf7`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/44075102111). **47 native Android tests, 159 browser checks, eight audio checks and zero release lint errors passed.**

Rebuilt Mindfulness bell, Tingsha bells and Temple gong from clean inharmonic struck modes, without random strike noise, time stretching or lossy encoding. The three files now use lossless FLAC and preserve distinct attacks with twenty-second fades. Replaced rain with softer, warm stereo wash and rounded droplets, lower peaks and less high-frequency energy. Rain checks bound brightness, peaks, audible level and loop continuity. The approved Zen tone and Singing bowl and the existing brown/white noise are locked by their original SHA-256 hashes. Verification compared all eight encoded audio file hashes inside the actual APK, accounting for optimized resource names; all protected bytes are exactly unchanged. Timer/UI code and sound identifiers are unchanged.

APK: `Meditation-v1.0.13.apk`, 2118663 bytes. SHA-256 `aecba26d28a2c105f23d62a6b5fa4b4cff5d2710e1b461a56f5c98b4b235e2f8`. Package `com.personal.meditation`. Artifact hashes, ZIP integrity, package/version, alignment, retained signer and exact non-signature content equality to CI passed. Updates retain existing app data. Native checks use Robolectric; browser checks use Chromium. No physical phone was tested.

# Meditation 1.0.12 verification

Source: `9c58f86f923bb743b33cce22daf1ada82ace75f9`. [Build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/43777874138). **47 native Android tests, 159 Chromium checks, eight audio checks and zero release lint errors passed.**

Removed the home Settings gear border/background in all accent modes while preserving its 44px touch target and focus outline. The Settings heading is reduced from 26px to 22px. Browser checks confirm the border does not return with gradient accents.

The clarified interval limit remains five bells before the end cue, at each sixth of the session when the slider is at maximum. Spacing is calculated from duration: eight minutes gives 1 minute 20 seconds, and one minute gives 10 seconds. Tests verify changing duration retains the five-bell cap and updates the shown gap. Existing timer scheduling and saved counts remain unchanged.

Artifact hashes, archive integrity, exact packaged UI, retained signing identity, package/version, alignment, non-signature equality with CI and v2/v3 signatures passed. Tests use Robolectric and Chromium; no physical phone was tested.

APK: `Meditation-v1.0.12.apk`, 1496068 bytes. SHA-256 `0d719d29a3c912289c3939b585b9a05a775fb7bfcf9337cadacc760d8ce455e3`. Package `com.personal.meditation`, version 1.0.12 (13), API 26/35. Installs over previous Meditation releases and retains settings.

# Meditation 1.0.11 verification

Source: `8df9406ae194e23826d09daf485981e3823663c9`. [Build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/43775571184). **47 native Android tests, 154 Chromium checks, eight audio checks and zero release lint errors passed.**

Settings dropdowns use natural height and proportional line height instead of a fixed-height text box. Their preview buttons stretch to the adjacent dropdown. Checks measure actual font ink against the available content height and equal preview/dropdown bounds at 14px, 24px and 32px text sizes, in addition to the existing narrow/wide layout checks.

The Interval bells control is a whole-number slider from Off to 5 bells, matching the vibration sliders. Saved counts and timing rules are unchanged. Its output shows the count; the line below says One bell every 2 minutes, One bell every 1 minute 45 seconds, or One bell every 10 seconds, with no dot separator. Off still suppresses interval sound and vibration. Visible Style captions are removed from Appearance, preserving accessible names for its dropdowns.

Collapsible section summaries have equal 20px top and bottom padding. Removed the extra margin between collapsed sections and normalized expanded-content bottom padding. Chromium verifies equal header spacing and adjoining divider positions. Home-screen styling and the existing image, theme, background audio, timer and vibration behavior are unchanged.

Artifact hashes, ZIP CRCs, exact packaged UI, non-signature content equality with CI, retained signing identity, alignment, package/version and v2/v3 signatures passed. Tested with Robolectric and Chromium, including enlarged text; no physical phone was tested.

APK: `Meditation-v1.0.11.apk`, 1496068 bytes. SHA-256 `22f8cd2efd8c2ea9e3b520dec7bbba2fdb286625684afa4893854b60de3d2a61`. Package `com.personal.meditation`, version 1.0.11 (12), API 26/35. Installs over previous Meditation releases and retains settings.

# Meditation 1.0.10 verification

Source: `ccd151d37142901667a94eb29d454f84631e7911`. [Build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/43768997941). **47 native Android tests, 143 Chromium checks, eight audio-file checks and zero release lint errors passed.**

Settings now uses centered, uniform dropdown chevrons with equal visible/text insets, aligned preview buttons and centered checkbox ticks. Labels say Duration in minutes, Countdown in seconds and clear vibration stages; the Vibrations seconds suffix and in-app changelog are removed. The launcher icon stroke is slightly thicker. Home layout and control dimensions remain unchanged.

Choose 0–300 preparation seconds, default 30. Zero starts immediately; one/two-second preparations only emit pulses that fit. Choose Off or 1–5 evenly spaced intermediate bells; Off suppresses both interval sound and vibration. Intermediate sound and vibration remain independent. Vibration sliders snap to 0.05-second increments and preview the saved snapped duration only on release. Session completion plays the configured end cue once and restores the selected duration, ready for Start without automatically restarting.

Background and text/button accents each support solid or two-color gradients, with touch/keyboard color wheels, brightness, validated hex entry, cancel/Back handling and reset. Native system bars follow the background. Image backgrounds use Android's document picker, decode/orient/downsample into a bounded private JPEG, survive reopening and can be removed. No network upload or broad storage permission is introduced.

Background sound supports Off, original synthesized Brown noise, White noise and Rain, or retained custom audio, with independent volume. A separate MediaPlayer loops throughout meditation, follows Pause/Resume and stops at End. Bell cues never replace the background player. Ten-second previews stop when leaving Settings or starting a session; actual custom loop playback has no ten-second limit. Native tests cover repeated loops, custom playback, pause during preparation, stop/timeout, cue coexistence and service integration. The three 24-second presets pass level, clipping, non-silence and loop-boundary checks; existing five bell files are unchanged.

The release APK is non-debuggable and signed locally with the retained Meditation certificate. Its assets match the tested CI artifact, and all non-signature archive content matches before/after signing. Verified artifact SHA-256, ZIP CRCs, package/version, alignment and v2/v3 signatures. Tests use Robolectric SDK 28 and Chromium; no physical phone or personal photo/audio was used.

APK: `Meditation-v1.0.10.apk`, 1496068 bytes. SHA-256 `8c9ae7c8ce8bc26b0e53709bdeb25be06412d19b0759efcbefbbad75893b5fcf`. Package `com.personal.meditation`, version 1.0.10 (11), API 26/35. Installs over existing Meditation releases while retaining settings.

# Tasks 0.88.0 verification

Source: `568cdf2f77b4ac1014732eb977b7e35d0c6d9cb6`. [Android build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43723414185); [Windows build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43723414190). **205 Android tests, 912 browser checks, 37 calendar checks and zero lint errors passed. Windows passed 41 unit tests, 35 browser/HTTP checks, nine native-window checks and eleven installer checks.**

Home-screen Refresh now invokes the same Google sync action as Settings on both clients. Android retains widget/reminder and local-pairing refresh without requesting a second cloud sync. Desktop saves pending window edits to its native host before the cloud request. Refresh reports the Google status after its timestamp or message changes, and reports pending status after thirty seconds rather than claiming success early. Without a connected account, local refresh stays available and the status points to Google sign-in in Settings.

Targeted Chromium checks cover the Google request, waiting for a fresh success, connection errors and signed-out local refresh. The desktop HTTP test clicks the real home button and verifies exactly one Google sync POST after the pending note edit has reached the local host. Google authorization/cloud operations are mocked in these interaction tests; no personal Google credentials or live account were used. Appearance and widget layout are unchanged.

Android: `Tasks-v0.88.0.apk`, 2880589 bytes; SHA-256 `6143b49c13894e1c2cdb00892c06334cfde67e56cab73ca7497d427a3d74e9b7`. Windows portable: 13790923 bytes; SHA-256 `65b14fcbce7677ffdfbab55299f7e12169a0a73b581381d450b91cf3765c37de`. Installer: 17338345 bytes; SHA-256 `7568f8fc09e27ca470b5ce8819b93ebc4239649948555f96d19cda3dcec0560d`. CI artifact digests, ZIP CRCs, package/version, exact Android non-signature content, retained Android signer and alignment passed. The updater installs over existing apps and preserves their saved data. Tested in CI and Chromium, without a physical phone.

# Tasks 0.87.0 verification

Source: `6a864b5d98a5d083fc1f98d270a56710be9e67f1`. [Android checks](https://github.com/swettgm/task-list-android/actions/runs/43666000859) and [Windows checks](https://github.com/swettgm/task-list-android/actions/runs/43666000901) succeeded. **205 Android unit tests, 912 browser checks, 37 calendar checks and zero lint errors passed. Windows passed 41 unit tests, 34 browser/HTTP checks, nine native-window/taskbar checks and eleven installer/upgrade/uninstall checks.**

A live, account-free diagnostic against Google’s desktop token endpoint returned `invalid_request: client_secret is missing`, establishing that the previous expired-authorization message was incorrect. Windows now imports a downloaded Desktop app OAuth JSON with a native file picker, validates its project/client type and protects the client secret with DPAPI CurrentUser. Authorization-code and refresh exchanges include it alongside PKCE and state. Setup errors are distinguished from an expired grant. Tests verify the token request, wrong-state rejection, config type/project validation, credential encryption and absence of secrets from status/browser URLs. The pywebview 6.1 file-dialog API was checked against its released package.

The current, already connected phone initializes the corrected cloud format from its current task state when upgraded. A fresh client restores the cloud list before generating its own changes; an old PC cannot initialize it from a stale local list. Previous private cloud v1 files are left untouched and ignored. Restores save local backups, reset stale browser caches/session state, clear old widget rows and reload task definitions from cloud data. Connected account controls show email, status, Sync now and an ×, hiding redundant setup/sign-in controls. Reconnect/initialization controls appear only when needed.

The cloud now has an immutable phone seed and compact per-installation change documents. No metadata ETags are applied to media reads/writes, eliminating the prior conditional-version loop. Logical clocks and deletion tombstones provide idempotent convergence. Shared Java/Python fixtures cover simultaneous independent edits, escaped IDs and a deletion racing a stale child edit. Deleted tasks/projects cannot reappear as incomplete objects; trashed tasks remain in Trash after reopening instead of becoming baseline tasks. Tests cover offline retries without reclocking, first restore, stale browser writes, pending per-account journals, account switching and sign-out retaining local tasks. UI filters, volatile timestamps, account credentials and tokens remain outside task payloads.

Google storage and identity are mocked in automated tests. Real account consent, the imported Desktop client JSON and cross-device service access require the user’s live account after installation. Phone sync remains foreground-only; widget edits sync on the next app open. Native task-row styling, widget presentation and the existing borderless plus button are unchanged.

Android APK: `Tasks-v0.87.0.apk`, 2880589 bytes, SHA-256 `ece0916a571f077ed8c735fdbc978d416528f2e36a33419727e4449e7686ba96`. Package `com.personal.tasklist.preview`, version 0.87.0 (87), API 26/35. Artifact checksums, ZIP integrity, exact packaged assets, non-signature equality to CI, alignment and v2/v3 signatures passed. The retained certificate SHA-256 is `c608cec387ed404701648b82b426777de0c2bc038d4fe44d3dd0237f3d0171c2`; its SHA-1 matches the registered Android OAuth client.

Portable EXE: 13789163 bytes, SHA-256 `046b9700e67cc20d55fa5cf44b0c94c8363e367b0615c59fe37b4a90940e6ae0`. Installer: 17335515 bytes, SHA-256 `4766522c99ebfeeb3796a4e53a43094e74f81c139772ffada83255f9502e9c3c`. The actual frozen and installed executable, taskbar identity, relaunch, running upgrade and uninstall were exercised on Windows. Publication verifies both successful source commits, immutable build artifacts and all binary hashes. No app signing key is uploaded to the publishing runner.

# Tasks 0.86.0 verification

Source: `aec67775bde7312414225f07e4ea2da778472def`. [Android build and checks](https://github.com/swettgm/task-list-android/actions/runs/43653938329) and [Windows build and checks](https://github.com/swettgm/task-list-android/actions/runs/43653938352) succeeded. **202 Android unit tests, 912 browser checks, 37 calendar checks and zero lint errors passed. Windows passed 39 unit tests (one platform-specific skip), 29 real-HTTP/browser checks, nine native-window/taskbar checks and eleven installer/upgrade/uninstall checks.**

Windows uses the shared full task UI instead of the previous completion-only view. Adding/editing tasks, inline metadata, repeat schedules, projects, notes, checklist steps, task ordering, Today/Upcoming, history/trash and themes are available. A new PC starts without bundled phone tasks, historical completions, moves or active progress. Full task definitions from the phone are exported alongside custom tasks. Manual PC edits are preserved during first phone pairing. Core Android behavior and widget presentation remain covered by the full regression suites. The home-screen Add task button loses only its inset outline; size, fill, glyph and rounding stay intact.

Google authorization uses system-browser PKCE/state on Windows and Google’s native authorization client on Android. Windows protects the refresh token with DPAPI CurrentUser (round-trip tested on Windows); Android keeps short-lived access tokens in memory. Tokens are absent from WebView state and HTTP status responses. Account-control endpoints require both local host access and bearer authentication. Task state and theme use private Drive app-data storage with ETag-guarded reads/writes and three-way merging. Tests cover conflict retries, simultaneous edits, offline persistence, in-flight local changes, account switching/sign-out, stale token refreshes, malformed documents and missing ETags. Local view choices and volatile timestamps are excluded from cloud payloads. OAuth loopback tests reject bad state and verify PKCE exchange.

Google transport and account identity are mocked in automated tests; no real Google account consent or physical-phone sync test was performed. Use the configured project’s test account after installing both apps to check live authorization and cross-device sync. Phone sync is foreground-only; widget changes sync on the next app open. Android reminders and widget remain phone features.

The signed Android APK is 2872397 bytes, package `com.personal.tasklist.preview`, version 0.86.0 (86), API 26/35. SHA-256 `de318992396a11e6def7010533b24ec90a10a2df31558a09199ca4ca3d0473e2`. Its retained signer is `c608cec387ed404701648b82b426777de0c2bc038d4fe44d3dd0237f3d0171c2`, with the registered Android OAuth SHA-1 `2B:E6:14:0F:A0:50:9D:13:6E:18:D5:25:06:2C:85:10:5D:57:9F:9C`. ZIP integrity, all packaged shared assets, CI artifact hashes, non-signature entry equality to CI, alignment and v2/v3 signatures passed.

Windows EXE SHA-256 `a44b907d46749ae94dd0971f67f78ccd631f5d1c8fa76955baf84906df2e00f5` (13778104 bytes). Installer SHA-256 `0220e99ef9c357b03d2cb4ec4e02011108e349c8119f8294a7b64b5996f76925` (17323002 bytes). The actual executable, visible native window, shortcut identity, relaunch, close/reopen, installed copy, running upgrade and uninstall were exercised on Windows. Publication verifies the exact successful run/source, immutable artifact identity and all binary hashes before one download commit. No app signing key is uploaded to the publishing runner.

# Meditation 1.0.9 verification

Source: `a1a04e51c4244004453e1e5fa681fec7523dbd6d`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43651288722). **24 native tests, 74 browser checks, five decoded-audio checks and zero lint errors passed.**

Restored Zen tone’s original real-time strike through 1.4 seconds, with only the recording’s quiet pre-strike lead silenced and a 10ms entry fade. Its late ringing modes are fitted from the recording and extended, avoiding time-stretch processing on the attack. Singing bowl retains its real-time attack, gains a brief mallet strike made from deterministic damped inharmonic modes without random noise, and joins a phase-aligned extended ring. Both use seekable lossless FLAC and retain twenty-second decays that reach silence at 19.8 seconds. The other three preset files are unchanged.

Audio checks now compare the opening waveform against the original source so the previous whole-file stretch and long fade-in fail verification. Relative waveform residuals from 350–700ms are below 0.00004 for both sounds. The bowl tail’s normalized join correlation is above 0.99. Full-rate stereo sample steps, clipping, sustained decay, duration and silent endings are checked. Native tests cover full cue playback, stop/replacement, custom-file recognition and timer behavior. Verification used decoded samples, Robolectric and Chromium; no physical-phone listening test was performed.

APK: `Meditation-v1.0.9.apk`, 1123316 bytes. SHA-256 `3d6d536eea752627df1ff4eed9281810131b94195018bf9bd6cb6fee2b97d7c1`. Package `com.personal.meditation`, version 1.0.9 (10), API 26/35. Artifact hashes, ZIP integrity, exact uncompressed bell resources, non-signature content equality to CI, alignment and v2/v3 signatures passed. The retained signer matches prior Meditation releases.

# Meditation 1.0.8 verification

Source: `2b74d50aab0d796d22dde1aa30a53d72cdb3636f`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43647608028). **24 native tests, 74 browser checks, five decoded-audio checks and zero lint errors passed.**

Rebuilt Zen tone as one continuous pitch-preserving stretch instead of joining an unstretched strike to a stretched ring. The old crossfade caused a brief phase-cancelling loudness dip around 400ms that sample-spike cleanup did not fix. Source cleanup runs before stretching; 140ms of output silence is followed by a 650ms raised-cosine onset. The sound remains twenty seconds long with its gradual fade ending at 19.8 seconds. Zen tone uses lossless 16-bit stereo FLAC, packaged uncompressed so Android MediaPlayer can seek the resource. The other four presets are byte-identical to 1.0.7. Saved sound choices and original-MP3 recognition remain compatible.

Decoded-audio QA now checks the full-rate stereo loudness envelope as well as adjacent-sample steps, headroom, duration and final silence. The opening envelope check rejects the published 1.0.7 dip (successive 50ms RMS ratio 0.8015) and passes the rebuilt opening (minimum 1.0268 in the checked rising interval). Additional analysis verifies the complete first two seconds independently in left, right and mono, an unclipped peak of 0.5, and exact silent leading/trailing samples. Native tests verify the complete twenty-second Zen cue, replacement/cancellation, custom audio and timer behavior. Verification used decoded samples, Robolectric and Chromium; no physical-phone listening test was performed.

APK: `Meditation-v1.0.8.apk`, 967667 bytes. SHA-256 `5ff5f882aad06f4dba011aed66fbb08d29da7b779598d6c9b497cefb1668dd4c`. Package `com.personal.meditation`, version 1.0.8 (9), API 26/35. Artifact hashes, ZIP integrity, exact seekable packaged audio, non-signature entry equality to CI, alignment and v2/v3 signatures passed. The retained signer matches previous Meditation releases.

# Meditation 1.0.7 verification

Source: `d9dcee73d3b0cc042c6512a166d638efc3f69aa2`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43645894005). **24 native tests, 74 browser checks, five decoded-audio checks and zero lint errors passed.**

Removed isolated sharp high-frequency spikes in Zen tone’s opening using a 129-tap zero-phase 3kHz FIR on the first 600ms, blending back into the original signal by 800ms. Cleanup runs after the original gain calculation, preserving the ringing body’s volume and twenty-second fade. The other four packaged sounds remain byte-identical. The original-file routing fix from 1.0.6 remains available.

The earlier 24kHz mono check could mask these short spikes. The new QA also decodes 48kHz stereo and asserts adjacent same-channel sample jumps stay below 0.06 in the opening. The measured maximum fell from about 0.540 to 0.034, with first-second RMS effectively unchanged. Full native/UI/audio checks passed, including custom-file recognition, duration, stop/replacement and final silence. Verification used sample analysis, Robolectric and Chromium; no physical phone listening test was performed.

APK: `Meditation-v1.0.7.apk`, 672754 bytes. SHA-256 `ee84865101ad39b18b04299abec932dfedad42fa99941cfbaa56edb2abed636a`. Package `com.personal.meditation`, version 1.0.7 (8), API 26/35. Artifact hashes, ZIP integrity, exact packaged UI/audio, four unchanged presets, non-signature content equality to CI, alignment, manifest and v2/v3 signatures passed. The retained signer matches previous Meditation releases.

# Meditation 1.0.6 verification

Source: `3e457c92cb3aa9c3f142e6b4380c1117a4654ac4`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43644361427). **24 native tests, 74 browser checks, five decoded-audio checks and zero lint errors passed.**

Kept Zen tone’s existing twenty-second ringing tail and fade. Silenced its first 140ms of low-level lead-in noise and applied a gentle 100ms fade into the actual tone, using Ogg quality 8. The first 130ms decoded peak fell from about 0.00144 to 0.00000073 while the later ringing level and fade remain effectively unchanged. The other four packaged sounds are byte-for-byte identical to 1.0.5.

An original MP3 already selected through the custom-file picker previously continued to use its three-second source. Existing and newly selected copies named `alex_jauk-zen-tone-mid-high-202557.mp3`, including numbered downloads, now resolve to the extended bundled Zen tone. Unrelated custom files retain their existing URI, choice and playback behavior. Added native checks for both paths.

Decoded-audio checks now verify the quiet lead-in and gradual actual onset as well as full duration, tail level, headroom and final silence. Full playback, timer, vibration and UI checks passed. Verification used decoded samples, Robolectric and Chromium; no physical phone listening test was performed.

APK: `Meditation-v1.0.6.apk`, 680946 bytes. SHA-256 `44c6bd3f732704470464ffbaa785c460ebb749a7b78a954ddbae30b95e67231a`. Package `com.personal.meditation`, version 1.0.6 (7), API 26/35. Archive hashes, ZIP integrity, exact packaged audio/UI, four unchanged preset assets, non-signature content equality to CI, alignment, manifest and v2/v3 signatures passed. The retained signer matches previous Meditation updates.

# Meditation 1.0.5 verification

Source: `c535e40e49e45c5ff03ee889e34a0fe8f7ddcffe`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43642594676). **22 native tests, 74 browser checks, five decoded-audio checks and zero lint errors passed.**

Extended the four existing presets and the supplied `alex_jauk-zen-tone-mid-high-202557.mp3` to twenty seconds. Zen tone is selectable independently for start/end and quarter cues. The processing preserves the original first half-second strike, stretches the ringing tail with pitch fixed at 1, crossfades the join, limits processing peaks, and applies a raised-cosine fade ending at 19.8 seconds followed by silence. The supplied recording retains stereo channels. Bundled Ogg Vorbis assets keep the APK small; original sources, hashes and the reproducible FFmpeg/Rubber Band recipe are retained outside Android resources.

CuePlayer now separates the preparation watchdog from playback duration, so built-in sounds play their complete fades instead of stopping at ten seconds. Custom audio keeps its existing ten-second playback limit. Stop/replacement cancels old completion callbacks. Timer deadlines, the minimal UI, vibration previews and saved sound identifiers are preserved.

Five new playback tests cover long built-in and Zen playback, single completion, stop/replacement, custom limits and silence. Settings/browser checks cover the added sound and independent role selection. CI decodes each real audio file and checks twenty-second duration, headroom, audible middle/late tails, decreasing RMS and final silence. The first/middle/late/last RMS levels decrease for all five files and the last 100ms is digital silence. Verification used decoded audio, Robolectric and Chromium; no physical phone was tested.

APK: `Meditation-v1.0.5.apk`, 615410 bytes. SHA-256 `cfd62d7863a598ea3def008cea9ad27ba7154417d124e2b63a96c9b325a9447d`. Package `com.personal.meditation`, version 1.0.5 (6), API 26/35. Artifact hashes, ZIP integrity, exact packaged UI/audio, non-signature content equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches Meditation 1.0.4.

# Habits 1.0.6 verification

Source: `a9a8ad172e3bf6256ade47df79cda6fc5e72a2ad`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43671797335). **36 native Android tests and zero lint errors passed.**

Widget circles now require two taps on the same habit, date and widget within 600ms. Each pair advances once through unmarked → check → X → unmarked. Single taps and slow taps leave saved marks unchanged and do not vibrate. The pending tap survives receiver recreation; completed pairs clear it so a third tap cannot advance again. Taps on different cells or widget instances cannot combine. Future dates remain locked. In-app marking and widget layout are unchanged. Accessible widget descriptions now say double tap.

Regression checks cover first/second/third taps, complete cycles, slow taps, separate cells/widgets/dates, receiver recreation, future dates and deleted habits. Existing persistence, rollover, layout and app tests also passed. Tested with Robolectric SDK 28; no physical launcher was tested.

APK: `Habits-v1.0.6.apk`, 37694 bytes. SHA-256 `945568e788a0ec39c22ba31349cce92cf54e1afbcab9fcf3134b80bd55df033a`. Package `com.personal.habits`, version 1.0.6 (7), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, alignment and v2/v3 signatures passed. The retained signing certificate matches previous Habits releases, preserving existing habits and settings on update.

# Habits 1.0.5 verification

Source: `3db9e576da17c6d6566d6cf663545165af736c4d`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43497236509). **33 native Android tests and zero lint errors passed.**

Habit names are always visible on the widget. Removed reveal preferences, tap-to-hide actions and auto-hide timers. Obsolete name-toggle intents from an older widget view are ignored and trigger a refresh. Restored the earlier 42dp day-heading row, 6dp top margin, 4dp vertical name padding and 10dp row bottom padding. Today remains a dot only; other days keep weekday abbreviations. The app layout is unchanged.

Future dates are read-only in both the widget and app. Future cells are disabled and have read-only accessible descriptions. The shared store rejects edits after the phone’s local today date, so stale pending intents and direct calls cannot bypass the restriction. Today/past marks remain editable. Existing future marks are preserved unchanged and become editable when their date arrives.

Removed obsolete hiding tests and added coverage for always-visible text after six seconds, ignored old hidden flags, restored spacing, past/today edits, disabled future app/widget cells and rejected stale future taps even when a future mark already exists. The complete persistence, rollover, widget-interaction and resizing suite passed. The native widget screenshot was reviewed. Robolectric SDK 28 was used; no physical launcher was tested.

APK: `Habits-v1.0.5.apk`, 37694 bytes. SHA-256 `93767d0c06d7ab82dacdb36f5251cababc07a2c7bff85ddf569b0b7d035fdfe5`. Package `com.personal.habits`, version 1.0.5 (6), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signer matches previous Habits releases.

# Habits 1.0.4 verification

Source: `5d4eba17b86a41247172095e51f7529e0ba9d624`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43492008137). **37 native Android tests and zero lint errors passed.**

Widget names no longer restore persisted reveal flags. Each reveal uses an in-process monotonic five-second deadline; tapping again hides immediately. Widget initialization and opening the app clear reveals, and process restart naturally starts hidden. A short asynchronous receiver callback refreshes the widgets after five seconds. Deadline tokens prevent an older callback from ending a later reveal. No extra permissions, foreground service or exact-alarm access is required. Daily marks remain independently persisted.

The widget heading is now a single 24dp row instead of 42dp. Removed the 6dp top margin and 8dp vertical name padding, and reduced row bottom padding from 10dp to 4dp. Habit rows sit about 32dp closer to the weekday row, with less space between habits. The invisible name tap area remains stable when revealing. App layout and typography are unchanged.

Four new native checks cover ignoring legacy saved visibility, resetting on app opening, hiding at five seconds while preserving daily marks, rapid hide/reveal timing and compact heading/row geometry. Existing persistence, widget tapping, rollover, font and width checks passed. The native widget preview was reviewed. Robolectric SDK 28 was used; no physical launcher was tested.

APK: `Habits-v1.0.4.apk`, 37694 bytes. SHA-256 `82ea1335663ad85b4369afb328731f99432ebe89ea7d455870c48e5c6d0230d7`. Package `com.personal.habits`, version 1.0.4 (5), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signer matches previous Habits releases.

# Habits 1.0.3 verification

Source: `1932a1208ce984f2b59c43adb3d31f157a089723`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43489431933). **33 native Android tests and zero lint errors passed.**

Today’s widget heading is only a solid dot, aligned with the other weekday abbreviations. Removed the habit-name outline and its unused drawable. Hidden names leave no border or placeholder; their transparent text preserves the invisible tap target and row dimensions. Tap to reveal/hide remains independent for each habit and retains its saved visibility state. In-app habit names and daily marks are unchanged.

Updated existing native checks assert today’s dot-only header and absent name backgrounds in both hidden/revealed states. The full persistence, rollover, mark/name-tap independence, font-size and resize checks passed. The native widget preview was reviewed. Robolectric SDK 28 was used; no physical launcher was tested.

APK: `Habits-v1.0.3.apk`, 37694 bytes. SHA-256 `c281ee59bb159aed50632c288665d7b2962eb8c29347d059bdbd5fcb66fa649d`. Package `com.personal.habits`, version 1.0.3 (4), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signer matches previous Habits releases.

# Habits 1.0.2 verification

Source: `70c0a35e37954349a8ebf54143eaac5a64729c91`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43486961270). **33 native Android tests and zero lint errors passed.**

Widget habit names start visually hidden inside 12dp rounded white outlines that fit their text plus 8dp horizontal and 4dp vertical padding. Tapping each outline reveals/hides only that habit’s name. Transparent text retains the measured dimensions, so revealing does not shift the row. The visibility preference is stored per stable habit ID, shared across widget instances and preserved through refresh/reopening, separately from daily marks. Hidden rows expose generic accessible descriptions. This is a visual concealment feature, not encrypted storage.

The app uses a separate, unchanged plain row layout: names remain visible, editable and their original size. Widget names retain the 2sp reduction from 1.0.1. The widget keeps weekday-only labels, today’s dot, no date numbers, no title and no plus/settings buttons.

Four additional native tests verify default hidden state, persistent/independent reveal preferences, deleted-row handling, exact fitted-box dimensions before/after reveal, no daily-mark mutation from widget name taps and unchanged in-app names. Existing persistence, date-window, widget-interaction and resizing checks passed. Native main-screen and static widget previews using the production RemoteViews row layouts were reviewed. Robolectric SDK 28 was used; no physical launcher was tested.

APK: `Habits-v1.0.2.apk`, 37774 bytes. SHA-256 `e28f2236fdfa0a458e065475ecfe9ec7004ef5684ecfa327d4238f9dd43e0543`. Package `com.personal.habits`, version 1.0.2 (3), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signer matches Habits 1.0.0 and existing data is retained on update.

# Habits 1.0.1 verification

Source: `9b0da89d8d5588cc9c6d412ff28f5ac0e72a8683`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43486189828). **29 native Android tests and zero lint errors passed.**

Removed the widget’s entire 48dp title/action bar and its pending intents. Seven weekday abbreviations remain, with no date numbers and a solid dot beneath today’s weekday. Today keeps its full accessible date description. The widget empty message points to the app instead of a removed + button. Habit names are 2sp smaller on the widget (14sp by default); their in-app size remains unchanged. The settings slider displays the actual widget size while preserving existing saved sizing preferences and habit history.

Updated native checks verify header structure, weekday/dot text, absence of visible date numbers, widget font size and unchanged in-app font size, alongside the existing persistence, rollover, widget interaction and resize checks. A static native preview built with production RemoteViews/row layouts was reviewed. Verification used Robolectric SDK 28; no physical launcher was tested.

APK: `Habits-v1.0.1.apk`, 37620 bytes. SHA-256 `877818cbd3d37a53fc2ffb721681a2876d5f2e701910b969a63329b9e62723b9`. Package `com.personal.habits`, version 1.0.1 (2), API 26/35. Artifact digests, ZIP integrity, non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. The signing certificate matches Habits 1.0.0, so this installs as an update retaining habits and settings.

# Habits 1.0.0 verification

Source: `9250ac45fa0f86fc1307c33edac6e1c2ed454571`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43479475173). **29 native Android tests and zero lint errors passed.**

A separate native Android app (`com.personal.habits`) with no runtime libraries or network permission. Its scrollable, resizable RemoteViews widget displays a seven-day calendar window from today minus two through today plus four. Each habit/date cycles empty circle → green check → red X → empty. Stable habit IDs and ISO calendar keys retain marks through renaming, reopening and window rollover. Deletion is confirmed. Stale row taps carry their rendered date, and each collection adapter’s snapshot date also anchors its headings. Invalid/deleted rows and unregistered widget taps cannot create marks.

Widget settings cover text/background/gradient colors, opacity, text size and tap vibration. Defaults are transparent with white text/icons. All instances share data and appearance. Midnight inexact alarms, periodic updates and date/time/time-zone/boot broadcasts refresh the window; Android may defer background alarms while asleep. No exact-alarm permission or persistent notification is required. Widget minimum height leaves room for a complete habit row. Existing apps and their APKs remain unchanged.

Tests cover date-window boundaries, leap years, 23/25-hour DST days, persistent checks/Xs, past/future independence, rename/delete/stale taps, corruption retention, appearance validation, background alpha, distinct artwork, real RemoteViews inflation, collection date anchoring, headers and columns at 250/320/480px, registered widget taps, main-screen marking and editor/settings flows. The native screen screenshot was reviewed. Robolectric SDK 28 was used; no physical phone/launcher was tested.

APK: `Habits-v1.0.0.apk`, 37620 bytes. SHA-256 `4211cd8cda23775b94ab4b8d62b869e1695a7991e28b100dca07f0360a9a58e0`. Package/version/minimum/target: `com.personal.habits`, 1.0.0 (1), API 26/35. Artifact digests, ZIP CRCs, signed APK non-signature content equality to CI, manifest, alignment and v2/v3 signatures passed. Retained Habits certificate SHA-256: `1caa94727f98bf0aafc631dca39a03902fa3600c47417028d266bcfc28c35e18`.

# Android Tasks 0.85.0 verification

Source: `5dd0d438dbcca48288c8732fe86a856bff542ad9`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43431898876). **193 Android tests, 912 browser checks, 37 calendar checks and zero lint errors passed.**

Task-row metadata now compensates for the invisible space inside each SVG viewBox. The visible clock, repeat, notification, linked-task and default context symbols align to their containing column; context badges start at the title/action column even when the metadata wraps. The correction is scoped to task metadata across Today, Upcoming and other task-list sections. Icon sizes, task-action dimensions, editor fields and native widget are preserved.

Added six browser geometry checks across Today/Upcoming at 320, 390 and 600px, verifying the visible edges of all five default context symbols and the shared title/action column on wrapped rows. Existing clock/action/checklist layout checks passed. The wrapped task screenshot was reviewed.

APK: `Tasks-v0.85.0.apk`, 335702 bytes. SHA-256 `393cd4d689c545800c866de4994fe5b49f1713a4e3b708585fffd07f9b365932`. Package `com.personal.tasklist.preview`, version 0.85.0 (85). Archive hashes, ZIP integrity, exact packaged assets, non-signature entry equality to CI, manifest, alignment and v2/v3 signatures passed. The retained certificate matches prior Tasks updates. Verification used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.84.0 verification

Source: `d68a9c8f4c901cb0410fdb191d16b58589e6117f`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43417762755). **193 Android tests, 906 browser checks, 37 calendar checks and zero lint errors passed.**

The inline High/Med/Low label is a keyboard-accessible button opening a focused Priority picker, reusing the component editor and existing High/Medium/Low choices. Save updates priority through the existing definition-save path and preserves other task details, history, links and progress. Medium/Low clear Urgent consistently with existing priority controls. Cancel and rejected writes preserve saved state. Today and Upcoming are supported without changing task dates. The inline appearance and layout remain unchanged; the Today screenshot was reviewed.

Added browser checks for keyboard activation and focused controls, Cancel, saving and label update, unrelated-field preservation, rejected writes, Urgent clearing, reopening and Upcoming edits. Existing layout, metadata, schedule and widget tests passed.

APK: `Tasks-v0.84.0.apk`, 335702 bytes. SHA-256 `e1e9da92909927e520dbec0770fc563e61e373fd030691f82f5d9f1ab748bc30`. Package `com.personal.tasklist.preview`, version 0.84.0 (84). ZIP integrity, exact packaged assets, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.83.0 verification

Source: `82d49f70a9143ffe0188774aff262ea670a2a036`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43410898785). **193 Android tests, 899 browser checks, 37 calendar checks and zero lint errors passed.**

Task-row action buttons have 1px less horizontal padding per side and are 2px taller, increasing from 32px to 34px. Matching controls, including arrows and gear, keep aligned heights; icon arrows retain their widths. Child task buttons receive the same height increase and a proportional padding reduction. Rounding, border appearance and fonts are unchanged. Detailed editor, native widget quick-add and widget-front styling are unchanged. Browser checks verify uniform rendered row-control heights and arrow/gear alignment at 320, 390 and 600px. The Today screenshot was reviewed.

APK: `Tasks-v0.83.0.apk`, 331606 bytes. SHA-256 `12ea09f02f72610d6dd5ccf78057741dd992e7917124ec5e8b24cb292d9bdc1d`. Package `com.personal.tasklist.preview`, version 0.83.0 (83). ZIP integrity, exact packaged assets, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.82.0 verification

Source: `d1ee984168551587e681e8ff52b70392b73d0c25`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43361986193). **193 Android tests, 899 browser checks, 37 calendar checks and zero lint errors passed.**

Main-screen controls retain the previous dimensions, padding and font sizes while keeping their rounded appearance and thick visual border. Additional border thickness is drawn as an inset stroke without consuming layout space. A browser comparison disables/re-enables the unified styling and checks the rendered widths, heights and font sizes of visible main controls against the pre-style geometry. Existing arrow/gear uniformity, header fit and row alignment checks passed.

The approved detailed editor and native widget quick-add dimensions and styling are retained. Repeat radio inputs remain semantic controls but their circles are hidden; the selected option has a white outline like Priority. The main-screen and detailed editor screenshots were reviewed. Date toggles, persistence and existing task workflows passed. Native popup and widget-front code are unchanged by this release.

APK: `Tasks-v0.82.0.apk`, 331606 bytes. SHA-256 `384b049cdb40c5aaede44c228d71f1044248d58e2dfce31cf4212fd784483afb`. Package `com.personal.tasklist.preview`, version 0.82.0 (82). ZIP integrity, exact packaged assets, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.81.0 verification

Source: `8752ce5f6dcb34531d02e34be5d97da67977a8bc`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43347416430). **193 Android tests, 899 browser checks, 37 calendar checks and zero lint errors passed.**

Outlined app buttons, editable fields and choice tiles use the label buttons' 12px corners and 2px borders. Native popup controls use matching corners and strokes. Small checkboxes retain proportional squircle corners, inline task details retain their unframed wrapping, and compact arrow/gear controls keep their 32px dimensions. The composite Today/Upcoming frame retains an even inset. The final Today and detailed editor screenshots were reviewed.

Tomorrow and Weekend are mutually exclusive selected toggles in detailed Add/Edit. Tapping the selected shortcut sets Today and explicitly clears the weekend flag. Manual date input updates shortcut selection. Checks cover toggling each shortcut on/off, selected states, manual dates, persistence without a stale weekend flag, strict scheduling and existing edit workflows. An initial run exposed row alignment changes from thicker borders; these were corrected. Screenshot review also caught unwanted frames on inline metadata, which were removed before the final full rerun.

APK: `Tasks-v0.81.0.apk`, 331606 bytes. SHA-256 `1174cdc0ec0d6afc7f6823e616376bc8cd95cc6bf339c5a4b5a84ca502171392`. Package `com.personal.tasklist.preview`, version 0.81.0 (81). ZIP integrity, exact packaged assets, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.80.0 verification

Source: `f02958474a1e2cf2600210c8d28877aec2cc86f4`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43317595410). **193 Android tests, 893 browser checks, 37 calendar checks and zero lint errors passed.**

Editor label tiles are icon-only squares, six per row. They use the usual 48px button size where space permits and shrink evenly on narrow screens so all six fit. Accessible names, tooltips, icon customization and selected white outlines remain. Geometry checks passed at 320, 390 and 600px; the detailed editor screenshot was reviewed. The native label picker uses six columns with square icon-only buttons and retained selection behavior. Task rows and the widget front are unchanged.

The first CI attempt passed layout and interaction checks but caught a missing version entry in the packaged changelog; the corrected full rerun passed. Packaged HTML, bridge, history and initial-state assets exactly match the source. Native picker selection and existing task workflows passed.

APK: `Tasks-v0.80.0.apk`, 331606 bytes. SHA-256 `54bc7b1c49d68987efed90c0f345d630f8136f321c86e4aaf3dab617b2851659`. Package `com.personal.tasklist.preview`, version 0.80.0 (80). ZIP integrity, non-signature entry equality to CI, alignment, manifest and v2/v3 signatures passed. The retained certificate matches previous Tasks updates. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.79.0 verification

Source: `ae7906dce7eb4ed522a77361fa6998c556d490a2`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/43189796687). **193 Android tests, 893 browser checks, 37 calendar checks and zero lint errors passed.**

Task-editor context labels use rounded square tiles with icons and names, generous full-tile tap targets, keyboard focus and white selected borders/glow. The shared field covers detailed Add/Edit, label-only popups and widget-launched full editors. Priority choices retain native radio semantics while their visible circles are hidden and selected buttons use the same white outline/glow. Main task rows and widget-front sources are unchanged.

Native widget Quick add uses a three-column label-tile picker with square sizing, preserved custom symbols/images, selected borders and saved multi-label state. Existing Android multi-label/reset tests were updated to use the tiles. Existing browser label-edit checks now tap whole tiles and verify saved associations and cancellation; all other editor, widget, calendar, completion, ordering and settings suites passed. The detailed editor screenshot was reviewed.

APK: `Tasks-v0.79.0.apk`, 331606 bytes. SHA-256 `137d458204bb539766d1170b88d418eaaf341921d12eafd52f0b9b4912250c41`. Package `com.personal.tasklist.preview`, version 0.79.0 (79). ZIP integrity, exact packaged assets, all non-signature entries matching CI, alignment and v2/v3 signatures passed. The retained certificate matches Tasks 0.78.0. Tests used Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.78.0 verification

Source: `20869ca39612d329177a89011f1d13470bb730e2`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42524394530). **193 Android tests, 893 browser checks, 37 calendar checks and zero lint errors passed.**

Clock and repeat artwork now begins at the same content column as the task title, priority text, action buttons and checklist controls. The checkbox keeps its own column. The fixed icon boxes and SVG shapes retain their sizes; only their internal horizontal inset is compensated within metadata flow. This preserves icon/text spacing, dot-separator wrapping, fonts and task ordering. The adjustment excludes the widget editor, and the widget front is unchanged.

Geometry checks compare the clock's painted left edge, first task button and checklist disclosure against the title at 320, 390 and 600 px in Today and Upcoming. The prior state is restored before the existing layout tests. The full drag, calendar, completion, editor, notification, widget and other suites pass.

APK: `Tasks-v0.78.0.apk`, 331709 bytes. SHA-256 `c38474a81f48c59d77ee2ce72c6a62f7b6fd214c96a388a3c9fb77c5be69dc60`. Package/version, retained update signer, alignment, ZIP integrity and exact packaged assets were verified. Every non-signature entry matches the tested CI artifact. Tests use Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.77.0 verification

Source: `1c8026fceb36cfd7d909ad253e4ab55611580c1b`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42519398165). **193 Android tests, 887 browser checks, 37 calendar checks and zero lint errors passed.**

Settings → Task ordering now offers Hold and drag as an alternative to priority arrows. Long press picks up a solid preview using the task's actual styling, with a white insertion line marking the landing position. Empty priority sections remain available while there are tasks to drag. Moving before the hold keeps native scrolling; active drags scroll at the screen edges. Pickup and successful drop provide haptic feedback. Order and priority save together using the existing task definition/order model; task dates, note content and completion history are preserved. Urgent tasks remain first and lowering an urgent task clears Urgent, matching arrows. Completed/trash items and interactive row controls cannot initiate a drag. The widget front is unchanged.

Browser regressions verify real touch pickup and cross-priority dropping, within-priority insertion, opaque correctly styled preview, insertion target, no unintended editor/completion/page swipe, Escape cancellation, scroll-before-hold cancellation, rejected-write recovery, persistence and restoring arrows. Screenshots were reviewed. Existing arrow, calendar, overdue, editor, widget and other browser suites remain passing.

The Weekend date shortcut now picks the next Friday strictly after today, including Friday (+7 days), Saturday and Sunday. JS and native implementations are covered across weekdays and year boundaries, with browser checks of the detailed form and native widget Quick add coverage. Recurring weekday selections are unchanged.

APK: `Tasks-v0.77.0.apk`, 327613 bytes. SHA-256 `9ce0315873af543dafa01898570ca11150804004a4f399a5d71c8021fa75b94a`. Package/version, retained update signer, alignment, ZIP integrity and exact packaged assets were verified. Every non-signature entry matches the tested CI artifact. Tests use Robolectric and Chromium; no physical phone was used.

# Android Tasks 0.76.0 verification

Source: `31c3fb0229eb61834a06174742f36f10684f1327`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42385687191). **193 Android tests, 867 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.76.0.apk`, 323517 bytes; SHA-256 `6ea9a33248562aa9c23750c0cd3b17bccad43d7634b1d1fb4ba6be0c9e4ee6ef`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

Removed the Repeat starts caption and its display-only event listeners. Repeat scheduling and saved anchors remain unchanged. Hide until before due time now occupies a full-width grid row below both Time/Notify and Date/shortcut buttons, with its lead-time inputs using that same width when enabled. Notify stays under Time. The shared Edit and detailed Add forms use the same layout.

Existing geometry regressions verify that Hide until begins below both columns, reaches the right edge of the date column, and remains above Duration, with Notify under Time and no repeat caption. The full scheduling, gesture, editor, completion and native suites pass.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.75.0 verification

Source: `fe58b4f41011e80f8e9d7c67423910160f41e88e`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42384748918). **193 Android tests, 867 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.75.0.apk`, 323517 bytes; SHA-256 `d899a4e70ae0ee6ffdb69de6c1f12a2d206e6be78f5c1d03bff446501142cd68`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

The detailed task forms now place Notify above Hide until due time under Time. The same shared form is used by Edit task and detailed Add task. Existing geometry checks verify Time/Date columns, Contexts placement and the new checkbox order in both forms. Arrow fades are lengthened to 350 ms, with reduced-motion support retained. Existing gesture tests assert the animation duration and final placement, and native sync defers reloads until the fade completes. Field sizes are unchanged.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.74.0 verification

Source: `47dd1871dc58d72e20d4c33eb03ea6861042a70d`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42382991287). **193 Android tests, 867 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.74.0.apk`, 323517 bytes; SHA-256 `0ef5790a1743fd575aa7ceec5603e2aab3b6f6e104a3c0b555fe031bfed90a57`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

Today renders active occurrences as full task cards above the completion progress bar and normal priority sections. The former title-only progress banner and its styling are removed. Active cards retain their checkbox, metadata, notes, checklist, progress action, priority arrows and editor. They are removed from the normal priority rows, deduplicated by occurrence, respect the context filter and retain saved priority/manual ordering. Stopping a task returns it to its normal section without mutating that order.

Active future occurrences keep their due-date caption and Do today control. A task with an active checklist step can pin its parent card. During completion, the active card and its checked undo checkbox retain their position for the existing two-second pause. Completing and undoing one of multiple active tasks preserves the others.

Browser checks cover multiple full cards, absence of a separate banner, no duplicate normal rows, retained notes/checklists/editor controls, stopping and reload persistence, future cards, linked progress, real-touch completion undo and pointer holds. The full calendar, priority, layout, settings and editor suites also pass. The widget retains its plain Overdue label but suppresses the Overdue suffix in the green active pill; native widget tests verify both states. The native in-progress notification is unchanged. Arrow reordering uses the existing fade path before rebuilding, with a browser animation assertion. Web URLs in displayed notes are linked using text nodes and HTTP(S) anchors; literal HTML stays text. Only user-gesture main-frame HTTP(S) requests open an external browser, and all WebView navigation/network blocking stays in place. Tests cover browser intents, blocked schemes and background/frame requests, preserved note text, punctuation and task-click isolation. Detailed Edit and Add forms place Time to the left of Date, keep Contexts below Priority/Urgent, and put Hide until above Notify under Time. The helper sentence is removed and vertical gaps are reduced slightly without resizing controls.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.73.0 verification

Source: `8c28c2c77065c51a2268c43149cafaf57a9a8c74`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42344189633). **192 Android tests, 855 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.73.0.apk`, 319421 bytes; SHA-256 `d88a9678395e799eb3dcf4b68ff1bb56b922f3826e84a94f6532fba3729c5b5c`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

Removed the extra app Overdue badge and its styling. Carried tasks retain one line such as Overdue from November 4th, always using the original occurrence date, including explicitly moved tasks. A single down-click at a priority-section edge enters the top of the next lower priority. Double taps and holds still append to their destination priority. Task action and arrow/gear gaps increase by 2 px while button sizes stay unchanged. Carry-over, scheduling, completion keys and widget presentation are unchanged. The existing calendar browser regression checks the exact date text and absence of a separate badge, along with recurring completion and future-date preservation.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.72.0 verification

Source: `26f1006da171ae50278c3673bcdcc7895753aa99`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42342694863). **192 Android tests, 855 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.72.0.apk`, 319421 bytes; SHA-256 `5b745f6fd27031bcf0c7ba76d07bd87e91c88d7974fcb7a73f439129abc074df`. The retained preview certificate matches the prior release, and every non-signature APK entry matches the tested CI artifact.

Every arrow transfer to a different priority appends the task at the bottom, including section-edge single taps, double taps and holds. Holding Up while already High moves to the top of its urgency group; holding Down while already Low moves to the bottom. Urgent tasks remain first. Priority and destination order save together; creation time remains the default for unmoved tasks. Browser tests include real touch gestures, haptics, hold-release suppression, cancelled drags, rejected writes and retries, reload persistence and Upcoming/Today agreement.

Unfinished occurrences carry into following days in the normal priority lists, with an Overdue tag and original due date. Their original completion keys remain unchanged. Recurring carry-over starts at the task's creation/schedule boundary, skips deleted, completed and superseded occurrences, respects explicit moves, and leaves future dates intact. Multiple pending occurrences of one task share task order without trapping single-tap reordering. The editor shows the original chosen due date for an overdue occurrence.

Browser checks exercise one-time and recurring overdue rows, visible tags, completion of only the original occurrence, future recurrence preservation, alternate-weekend schedules, explicit moves, automatic midnight refresh and rescheduling. Native checks confirm that widget snapshots carry unfinished past rows, reject future and invalid weekday rows, show an Overdue label, complete only the selected occurrence, and remain carried over several days later. New-task test fixtures record creation timestamps so layout-only repeat anchors do not invent pre-creation history.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Tests use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.71.0 verification

Source: `c009b1dc5f26c3f704ddd926725322904a062672`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42320716295). **191 Android tests, 870 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.71.0.apk`, 319421 bytes; SHA-256 `5acaaab5e906aee1e87d8a1bb1c35c28f9426991f72fb5957f64a9aa7eaa6d03`. The retained preview certificate matches the previous release, and every non-signature APK entry matches the tested CI build.

A single arrow tap swaps with the visible neighbor inside a priority section. At its first or last row, it moves into the adjacent priority at the nearest end: up enters the bottom of the higher priority, down enters the top of the lower priority. Priority and destination order save in one state write. The order is shared across Today, Upcoming and widget sorting. The same creation-time default and Urgent-first rule remain in effect.

Browser checks cover both boundary directions, insertion despite conflicting creation timestamps, subsequent ordinary reordering, High/Low limits, reload persistence, Upcoming and Today agreement, exactly one light haptic, and an internal Urgent boundary that must not demote a task. A rejected state write leaves both priority and ordering unchanged and a retry succeeds. Notes, checklist and creation time remain intact. Existing real-touch single/double/hold and cancellation checks also passed.

The APK is verified for package/version, retained signer, alignment, ZIP integrity and exact packaged assets. Checks use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.70.0 verification

Source: `6061026c1cadeae5c7ece288a1b3f39f58601115`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42317921720). **191 Android tests, 855 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.70.0.apk`, 315325 bytes; SHA-256 `d4fbe311b5e9b692a2d2d5b70905ba2cf035849a0f9de1303c371be1325b9e33`. The certificate matches the retained preview signer and every non-signature ZIP entry matches the tested CI APK.

Default task order is actual creation time, oldest first, within priority and urgency groups. Unknown creation times use original definition order. A single arrow tap swaps the task with its visible neighbor and saves a global per-priority ID order. Double taps cancel the queued single action and change priority one level. Holding jumps to High or Low. Native light/long haptics accompany successful actions. Movement, cancellation and hold-release suppression are checked with mouse and real touch input. Shift+Enter/Space provides a keyboard priority action.

Manual order is shared by Today, Upcoming and future recurring occurrences, and carried to native widget sorting through saved task IDs and explicit creation metadata. The widget never treats an occurrence date as a task’s creation time. Urgent tasks stay first, and manual moves cannot cross the urgent boundary. Completed history retains its completion-time ordering. Tests preserve checklist history, task details, creation timestamps, links and identities.

The refresh icon uses symmetric rounded arrows centered in its existing control. Interval editors show the repeat anchor separately from the occurrence date, without changing saved schedules. The user corrected Check oil’s start date; this release retains the strict scheduling rules and does not rewrite recurrence anchors.

The release APK is verified against the CI artifact, retained signer, package/version, alignment, ZIP integrity and exact packaged assets. Checks use Robolectric and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.69.0 verification

Source: `eb14715f29ca3628fba765bf3029cfc5ffb473a7`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42313719512). **189 Android tests, 865 browser checks, 37 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.69.0.apk`, 311229 bytes; SHA-256 `db15b295a8c49b046ef2a0db753668d73503247e539be3fa2fde927564f074a5`. The certificate matches the retained preview signer and all non-signature contents match the tested CI APK.

The app and widget enforce scheduled dates. Friday weekend previews and automatic unfinished-task carry-over are removed. Recurring weekdays override stale one-time due fields and obsolete move entries; one-time tasks retain explicit move dates. Upcoming no longer hides future repeats because an earlier occurrence is pending. Definitions, completion keys, completion history and old unfinished records remain saved.

Native checks cover four weeks of Sunday-only, Sunday–Thursday and alternate-weekend schedules. Widget checks reject old Friday-preview/onToday/plannedDate flags, reject past carried rows, restore Saturday/Sunday occurrences despite legacy Friday moves, and retain explicitly moved one-time tasks. Reminder planning and reveal times use the same chosen-date rule.

Browser checks exercise Friday afternoon and evening, Saturday/Sunday, the skipped alternate weekend, the next active weekend, old unfinished tasks, stale due fields and legacy move records. They recreate Waterproof shoes, edit it with Tomorrow, verify its removal from Today and immediate widget-snapshot update, then check automatic midnight refresh without hidden tasks. The toolbar refresh retains the selected tab and filter, refreshes native widgets and reminder plans, and requests companion sync. UI checks use an advancing Sunday clock for weekday-dependent gesture fixtures.

The obsolete Friday-opening setting is removed and its alarm cancelled. A native midnight refresh is always armed, and the app refreshes on midnight and focus. The Weekend date shortcut remains an explicit Friday date selection.

Release verification compares every non-signature APK entry against the tested CI artifact, validates the retained signer, alignment, package/version, archive integrity and packaged assets. Tests use Robolectric/native graphics and Chromium in GitHub Actions; no physical phone was used.

# Android Tasks 0.68.0 verification

Source: `00287e7fb7559edd0f46d7c64ffeeeac2913d979`. [Build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42201054068). **187 Android tests, 855 browser checks, 33 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.68.0.apk`, 311229 bytes; SHA-256 `9c36a9bed48da431139c20cb91959bb32aedf81ed62d5a0362589186d2a211f9`. The certificate matches the retained preview signer and all non-signature contents match the tested CI APK.

Creation timestamps are stored on task definitions when new tasks are saved from the app, widget Quick add or project conversion. Detailed edits and rescheduling retain the original timestamp. Earlier app task IDs encode their creation instant; the editor recovers that timestamp and makes it durable on the next save. Tasks without recorded creation history display Created date unavailable. Due dates, occurrence dates and widget snapshot timestamps are not used as creation history.

The creation date appears only beside Edit task in the full task editor, including the widget-launched editor. Browser checks cover New York date boundaries, numeric timestamps, legacy IDs, unavailable dates, editing and rescheduling, reopening, quick and detailed creation, and corner placement at 320, 390 and 650 px widths. Native checks verify widget Quick add records its creation instant independently of its future due date.

The hidden-task border extends horizontally by 6 px on each side, with no content movement or vertical expansion. It remains visible beyond the resting page clip while swipe animations retain clipping, and it ignores pointer events. Browser checks verify checkbox clearance, viewport bounds and row spacing. Detailed task forms place Hide until before due time after Date/Time and their shortcuts, before Duration.

Release checks compare all non-signature APK entries with the tested CI artifact, verify the retained signer, package/version, ZIP integrity and packaged assets. Native checks use Robolectric and browser checks use Chromium in GitHub Actions. No physical phone was used.

# Android Tasks 0.67.0 verification

Source: `4df05d54951c168fac1a1769e598805d472508c0`. [Android build and full checks](https://github.com/swettgm/task-list-android/actions/runs/42152352238). **186 Android tests, 835 browser checks, 33 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.67.0.apk`, 311229 bytes; SHA-256 `016d6bc245daded44754b3f85dd29a2547a96332f4dec0080707840e56e37c81`. The certificate matches the retained preview signer and all non-signature contents match the tested CI APK.

The widget renders the empty message with the device’s font and emoji, centers the visible glyph bounds on each wrapped line, and exposes the full sentence to accessibility services. It uses the launcher-provided width and redraws after resizing. Native graphics checks inspect actual text/emoji pixels for both rest messages at 140, 250, 360 and 420 dp widths. RemoteViews checks verify centered placement at several widget widths and heights. The app’s rest messages use left alignment, verified in Chromium at 360, 390 and 650 px widths.

The widget requests a five-column, two-row default where supported, with a 144 dp minimum height. Its content fills the size allocated by the launcher. Android launchers control the widget’s grid position and outer size; existing widget allocations are preserved, so users resize those through launcher handles. No task data or widget preferences are reset.

The signed APK is compared with the tested CI artifact, including every non-signature archive entry. Its retained signing identity allows an in-place update. Tests use Robolectric/native graphics and Chromium in GitHub Actions; no physical Samsung launcher was available for verification.

# Android Tasks 0.66.0 verification

Source: `82240b6d0aa0b6182cffa1026ed49f6b9bda3441`. [Build, tests and browser checks](https://github.com/swettgm/task-list-android/actions/runs/42150732017). **185 Android tests, 835 browser checks, 33 calendar checks and zero lint errors passed.**

APK: `Tasks-v0.66.0.apk`, 307042 bytes; SHA-256 `4f6dc4bc05ebf41e57d7338c9ad02a3084b8213932cb1548ed177fa81042644c`. The signature matches the retained preview certificate and every non-signature ZIP entry matches the tested CI APK.

The in-progress notification completes one active task directly, or opens a native chooser for multiple tasks. Tests on Android API 28 and 34 cover notification intents, cancellation, stale session actions, completion history, widget updates, checklist parents, recurring occurrence keys, fallback snapshots and preserving unrelated active tasks.

Calendar checks cover Sunday-only Groceries, Saturday–Sunday Check oil and Wash sheets, and weekly repeats anchored on Sunday without explicit weekdays. Upcoming uses actual occurrence dates and keeps hidden tasks visible. Browser checks open and save future editors, change Sunday to Monday while preserving task details, and save unchanged weekly rules without resetting them. Friday-evening availability for explicit weekend groups in Today remains separate from Upcoming date grouping.

Remembered Today/Upcoming tabs persist on the device across activity recreation and page reloads, without adding the view to synced task data. Hidden Today rows have a white outline only while manually revealed. Both rest messages keep their vertical placement and have horizontal centering checks across phone and widget widths. Widget RemoteViews use a fresh layout ID to avoid reusing stale launcher layout state.

The release APK is checked for retained signing identity, alignment, package/version, archive integrity and exact packaged assets. No physical phone was used; native checks use Robolectric and browser checks use Chromium in GitHub Actions.

# Windows taskbar fix 0.65.2

The native window and installed shortcuts passed identity and relaunch checks. See [taskbar verification](docs/WINDOWS-TASKBAR-VALIDATION.md).

# Windows installer 0.65.1

The Windows installer passed installation, live upgrade and uninstall checks. See [installer verification](docs/WINDOWS-INSTALLER-VALIDATION.md). The Android and task-sync checks below remain applicable.

# Tasks 0.65.0 and Windows companion verification

Android source: 4b3c40a2c47ea2a6b630f0352267b2549b5141fe. [Android build and complete checks](https://github.com/swettgm/task-list-android/actions/runs/42085358547): 167 Android tests, 811 browser checks, 33 calendar/timing checks and zero lint errors.

Windows source: 9256eb8c9d8acbee9f9f188486ba916b4e9a243d. [Windows build and executable checks](https://github.com/swettgm/task-list-android/actions/runs/42085005875): 18 Python persistence/protocol tests, 19 real-browser companion checks, and a smoke test running the actual packaged Windows EXE.

The Windows screen reuses the packaged Android task UI and schedule engine. It shows Today and Upcoming, checkboxes and Do now/in-progress controls, with task editing, add buttons, settings, priority changes, rescheduling and deletion unavailable. Linked groups start and stop together. Main-list checkbox sizing and native widget layouts are unchanged.

The first PC launch waits for pairing rather than displaying historical seed data. A QR code or pairing link opens a confirmation in Android. Confirmed local-Wi-Fi sync transfers the current phone state, including colors, gradient, metadata icons, completion history and active tasks. Theme updates apply to an already-open Windows window. The EXE opens a compact, resizable Edge app window, falling back to the system browser.

Phone sync runs while the Android task app is open. Changes save locally while disconnected and merge on reconnection. Tests cover simultaneous unrelated changes, custom task merging by stable ID, linked-progress stops preserving unrelated tasks, stale compare-and-write rejection, failed saves retaining data, malformed remote data, authentication, local-only UI credential pages and pairing cancellation. PC and phone context filters stay independent.

Desktop completions, reopening and progress refresh the phone's native widget state before a deferred WebView redraw. A native regression check asserts this without running the page callback. Ongoing task notifications update through the existing reminder planner. Android WebView networking remains blocked; the native sync client accepts explicitly validated local network endpoints and uses a pairing token.

Android APK: `Tasks-v0.65.0.apk`, 302855 bytes; SHA-256 `bceb6d0dcf34fce6f5831313abd22fe6bf5cc7e7654efc46e9989e7991d93165`. The actual signature, alignment, retained signer, ZIP CRCs, packaged assets and non-signature contents match the verified CI build. The certificate matches Tasks 0.64.0.

Windows executable: `Tasks-PC.exe`, 9720841 bytes; SHA-256 `49866c17a28241a7c3bca7aff6a5b5f91e33d5c60d1aaeb9c27fe60f0b6caefe`. The artifact digest, ZIP CRCs, executable checksum and AMD64 Windows GUI PE headers were verified. The packaged EXE itself passed startup, authenticated HTTP sync, saved-state, QR generation and clean-shutdown checks on Windows. It has no commercial Authenticode publisher signature.

The public download bytes were compared to the verified local artifacts. Settings Changelog and the desktop instructions include pairing, theme transfer and the foreground/same-Wi-Fi sync limitation.

No physical phone or personal Windows PC was used. Android checks use Robolectric; Windows checks run on GitHub's Windows runner with a real browser and the packaged executable.
