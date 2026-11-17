# CRM

[Download CRM 1.0.0](https://github.com/swettgm/task-list-android/raw/refs/heads/main/downloads/CRM-v1.0.0.apk). Android 8 or later.

A separate Android app for staying in touch, with Friends and Dating pages. Link selected phone contacts (including contacts synced to the phone by Google), add notes, choose a repeat interval and reminder date/time, and record when you last contacted someone. Dating entries have In progress, Hiatus and DTF labels.

Linked names, phone numbers refresh on launch/resume. CRM does not write to the phone contacts database or send messages automatically. Removing a person from CRM leaves the phone contact intact. Notes and schedules are saved in app-private storage. Notifications repeat on a calendar cadence; Contacted today resets that cadence. Delivery uses Android’s battery-aware alarms and may be delayed by battery restrictions.

Build: `gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` (JDK 21, Gradle 8.11.1, Android SDK 35). Browser checks: `node tools/browser-smoke.mjs` with Playwright installed.

Friends and Dating are separate pages; entries are added on the current page. Snapchat, Discord and In person tags can be combined. Editors use compact single-line actions and a red trash icon with a second-tap confirmation. Existing Interested/Dating entries display In progress; Paused displays Hiatus. Existing notes and reminders are preserved. Email is no longer shown or read from linked contacts.
