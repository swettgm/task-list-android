# Changelog

## 0.39.0 — 2026-11-03

- Existing task editors include an outline trash icon: first tap asks for confirmation while gray, second press turns red and deletes the task from current and future lists. Editing a field resets confirmation; saved completion history is retained.

- Upcoming headings show Tomorrow plus its date, followed by full weekday names and dates for later groups.
- The top summary keeps the completion fraction, omits the pending count and formats combined duration in hours and minutes.

- New tasks default to Priority not set with no selected priority. Detailed editors offer only High, Medium and Low; widget quick add uses a disabled Priority placeholder until a level is selected.

- Detailed task editors open with the keyboard collapsed, including the transition from Quick add; Quick add retains initial keyboard focus.
- Editor action buttons occupy their own row below the fields instead of a sticky overlay.

- Weekend shortcuts in the app editor and widget quick add now choose Friday instead of Saturday. On Saturday or Sunday they use that weekend’s Friday.

- Urgent now shares In progress’s rounded pill shape, padding, typography and height, while retaining its red color and title-spacing behavior.

## 0.38.0 — 2026-11-03

- Urgent now sits directly beside the task title when there is room, matching In progress spacing and vertical alignment. Badges wrap below only when needed, including when both are present.

## 0.37.0 — 2026-11-03

- Tap a widget task title to edit it in the same rounded popup used in the app, over the home screen.
- Includes all existing editor fields, saved theme and recurrence behavior; saving refreshes the widget immediately.
- Checkbox and in-progress taps retain their separate actions. Back, Cancel and an outside tap close the editor; text fields do not automatically open the keyboard.

## 0.36.0 — 2026-11-02

- Added light haptic ticks for task-add plus buttons and successful task creation, including widget quick add and keyboard submission.
- Added feedback when confirming dates/times and choosing Today, Tomorrow or Weekend in the widget popup.
- Uses Android’s haptic settings; failed saves and automatic field resets do not emit success feedback.
- Swiping between Today and Upcoming now works in the blank lower part of a short list.
- Centered the empty-widget flower message against the entire widget, with equal side margins.

## 0.35.0 — 2026-11-02

- Fixed touch priority taps being cancelled by a pointer-leave event after release. Taps move one level; holds jump to High or Low.
- Added real touch tap and hold regression checks, including tapping immediately after holding.
- Removed Do today and Do tomorrow from recurring tasks on both Today and Upcoming. One-time tasks retain these controls.
