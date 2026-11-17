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

