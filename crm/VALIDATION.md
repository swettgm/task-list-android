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

