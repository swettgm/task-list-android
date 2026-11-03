# Changelog

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
