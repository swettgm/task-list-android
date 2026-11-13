## 0.83.0

- Reduced task action buttons’ horizontal padding by 1px per side and made them 2px taller (34px), keeping matching controls aligned.
- Preserved their styling and text size. Detailed editor and native widget quick-add are unchanged.

## 0.82.0

- Restored original main-screen control dimensions and padding while retaining the current rounded style and thick border appearance.
- Preserved the approved detailed editor and native quick-add dimensions and styling.
- Removed visible Repeat radio circles; selected Repeat uses a white outline like Priority. Date toggles remain.

## 0.81.0

- Unified app buttons, fields and choice tiles with the label buttons: 12px corners and 2px borders. Native popup controls use matching corners and borders.
- Tomorrow and Weekend now toggle back to Today when unselected, with mutually exclusive selected outlines. Manual date changes update the outlines; unselecting clears the weekend flag.

## 0.80.0

- Made editor label tiles icon-only squares, six per row, at the usual button height when space allows.
- Kept accessible names, white selected outlines, and existing task/widget-front appearance.
- Applied the compact layout to the native label picker too.

## 0.79.0

- Rounded square label tiles in task editors, including widget Quick add, with icon/name labels and white selected borders.
- Priority buttons use white selected outlines and glow without visible radio circles.

# Changelog

## Android 0.78.0 — 2026-11-08

- Aligned clock/repeat artwork with the task title, priority text, action buttons and checklist disclosure column while preserving icon/text sizes and wrapping. The checkbox keeps its separate column.

## 0.77.0 — 2026-11-08

- Optional Hold and drag task ordering in Settings replaces priority arrows with direct reordering within and between priority groups.
- Solid moving task preview, insertion line, haptic pickup/drop feedback, edge scrolling and cancellation without changing saved order.
- Weekend now always selects the next Friday after today in detailed forms and widget Quick add.
- Normal scrolling remains available until a hold starts. Existing arrows, task dates, completion history and widget front remain unchanged.


## Android 0.76.0 — 2026-11-07

- Removed the Repeat starts date note from the detailed editor. Saved repeat schedules are unchanged.
- Hide until before due time and its hour/minute fields now span the full form width below Time, Date and their shortcut buttons. Notify stays under Time, above Hide until. This applies to both Edit task and detailed Add task.

## Android 0.75.0 — 2026-11-07

- Moved Notify above Hide until due time beneath the Time field in the detailed Edit task and Add task forms.

- Lengthened task-arrow fades to 350 ms so movement is easier to notice. Reduced-motion preferences remain respected.

## Android 0.74.0 — 2026-11-07

- In-progress tasks now move as full task cards to the top of Today, replacing the separate title-only progress banner. Multiple active tasks appear there together, with their checkbox, details, checklist and actions. Stopping a task returns it to its normal priority section without changing its saved order.
- Active future occurrences and tasks with an active checklist step can also appear as full cards at the top. Completing an active task retains the card during the existing undo pause. The Android in-progress notification and widget presentation are unchanged.
- Web addresses in displayed task notes are now tappable and open in the default browser. Notes remain plain text when editing.
- Swapped the detailed task form columns so Time is on the left and Date is on the right, in both Edit task and Add task.
- Kept Contexts beneath Priority and Urgent. Under the Time field, Hide until due time appears above Notify. Removed the explanatory Hide until sentence and slightly tightened the detailed Add/Edit form spacing while preserving field and button sizes.
- Task arrow moves fade out briefly before the task appears in its new position, including within-priority reordering and endpoint holds.
- The widget keeps its plain Overdue status, but an active task’s green pill shows only IN PROGRESS.

## Android 0.73.0 — 2026-11-07

- Removed the separate Overdue badge. Carried tasks show one simple line such as Overdue from November 4th, always using the original due date rather than a carry-over or move date. Carry-over, completion and task ordering are unchanged.

- A single down-click from the bottom of a priority section now puts the task at the top of the next lower priority. Double taps and holds still enter a different priority at its bottom.

- Added slightly more space between task action buttons while keeping their size unchanged.

## Android 0.72.0 — 2026-11-06

- Arrow moves into another priority now always put the task at the bottom of that section, whether using a single tap at a section edge, a double tap, or a hold. Creation-time sorting remains the default until tasks are manually moved.
- Holding Up on a task already in High moves it to the top; holding Down on a task already in Low moves it to the bottom. Urgent tasks stay first. Successful holds give haptic feedback, and saved ordering is shared by Today, Upcoming and the widget.

- Unfinished tasks now carry into following days in their normal priority lists with an Overdue tag, including missed recurring occurrences. Completing an overdue occurrence preserves later repeats. The widget also shows unfinished past tasks and labels them Overdue. Future tasks keep their chosen dates.

## Android 0.71.0 — 2026-11-06

- A single up-tap on the first task in a priority section now moves it to the bottom of the next higher priority. A single down-tap on the last task moves it to the top of the next lower priority. Position and priority are saved together across Today, Upcoming and the widget.
- Single taps within a section still move one task, double taps change priority one level, and holding jumps to High or Low. Successful moves keep their haptic feedback, Urgent tasks stay first, and default sorting remains creation time.

## Android 0.70.0 — 2026-11-06

- Task arrows now use a single tap to move one task within its priority section, a double tap to change priority one level, and a hold to jump to High or Low. Successful actions give haptic feedback, and drags cancel arrow gestures.
- Tasks default to creation time, oldest first. Manual order is saved across Today, Upcoming and the widget, including future recurring occurrences. Older tasks without recorded creation times retain their original definition order. Urgent tasks remain first and completed history remains newest-completed first.
- Replaced the refresh icon with a centered rounded two-arrow design. It reloads saved tasks, updates the widget and reminders, and requests companion sync without changing dates.
- Interval repeat editors now show the actual repeat start date beneath Date, distinguishing the repeat anchor from the occurrence being edited. Existing repeat schedules remain unchanged.

## Android 0.69.0 — 2026-11-06

- Tasks now appear only on their scheduled date or an explicitly chosen move date. Removed Friday weekend previews and automatic carry-over into later days in both Today and the widget. Past unfinished tasks and completion history remain saved.
- Upcoming shows each scheduled repeat even if an earlier occurrence is unfinished. Chosen weekdays override stale one-time due fields, and every-other-week cycles keep their original anchor. Recurring schedules ignore obsolete day-move entries.
- Added a refresh button beside Settings. It reloads saved tasks, updates the widget and reminders, and requests companion sync while preserving your tab and context filter. The calendar also refreshes automatically at midnight and when the app regains focus.
- Removed the old Friday weekend-opening setting. The Weekend date shortcut still explicitly selects Friday, while Saturday/Sunday recurring choices stay on those selected days.

## Android 0.68.0 — 2026-11-06

- The task editor shows a subtle creation date in its top-right corner, formatted as Created Monday, Nov 4. New tasks record their creation time, edits and rescheduling preserve it, and older app tasks recover it from their original IDs where available. Older tasks without a recorded date show Created date unavailable.
- Revealed hidden tasks have a slightly wider white border with space around their checkbox, preserving task alignment and row spacing.
- Moved Hide until before due time directly below Date and Time, above Duration, in detailed task forms.

## Android 0.67.0 — 2026-11-05

- The widget centers the visible text and flower together, including wrapped messages, using its current launcher-provided width. Resizing redraws the message without changing its font size.
- New widgets request a wider, shorter layout (five columns by two rows where supported) and can resize down to 144 dp high. Existing widgets keep their launcher placement and can be resized.
- The app’s empty-list messages are left-aligned. The widget keeps its centered message.

## Android 0.66.0 — 2026-11-05

- Added Complete to the persistent in-progress notification. A single active task completes directly; with several active tasks, Complete… opens a task picker so you choose which one to finish.
- Notification completion uses the same saved completion history and widget state as the app, handles complete checklists and recurring occurrences, and leaves other tasks in progress.
- Stopped, moved, deleted and already-completed tasks reject outdated actions. The app refreshes when you return from the notification shade or picker.
- Upcoming groups now use actual scheduled dates instead of Friday weekend previews. Single-weekday repeats such as Sunday Groceries keep their chosen weekday, and future task editors show the correct occurrence date and selected days.
- The app reopens on the last selected Today or Upcoming tab, saved only on that device.
- Hidden tasks get a white outline only while temporarily revealed in Today. Upcoming always includes future tasks before their hidden-until time.
- Horizontally centered both rest messages in the app and reinforced widget alignment on refresh, with a fresh widget layout to clear stale launcher styling. Vertical placement stays unchanged.

## Windows 0.65.2 — 2026-11-05

- Tasks now owns its native Windows window instead of launching an Edge app window. The window and shortcuts share a stable taskbar identity and relaunch command.
- Launching Tasks again restores the same window, including when minimized or closed to the background. Setup and uninstall close the native window and sync host together.
- Tasks, pairing, QR theme transfer and the task interface are retained. Existing taskbar pins from the browser-based version should be unpinned and recreated from Start menu Tasks once.

## Windows 0.65.1 — 2026-11-05

- Added a per-user Windows installer with Start menu and optional desktop shortcuts, plus a standard Windows uninstaller.
- Setup safely stops a running companion before upgrading. Updates and uninstall preserve tasks, theme and phone pairing.
- Android 0.65.0 remains compatible; the task interface, QR theme transfer and Google sign-in setup status are unchanged.

## 0.65.0 — 2026-11-05

- QR pairing transfers phone colors, gradient and icon choices, and keeps later theme changes in sync.

- Added a simple Windows companion for Today and Upcoming, completing tasks and toggling in progress, without Settings or task editing.
- Pair the PC once by QR code or pairing link under Android Settings → Windows companion. Tasks sync locally while both devices are open on the same Wi-Fi; offline changes are retained.
- The PC starts with your current phone list instead of the bundled historical snapshot. Linked progress starts and stops together, and unrelated simultaneous device changes are merged.

## 0.64.0 — 2026-11-05

- Linked task details now follow the priority label in the same metadata row, separated by a dot.
- The chain icon and linked task name move to the next line together using the existing metadata wrapping and separator rules. Very long linked names are ellipsized within the available width.

## 0.63.0 — 2026-11-05

- The Projects plus now opens a title-only Quick add popup with Cancel, Details and Add.
- Quick project saves keep the popup open, clear the title, provide save feedback and support Enter/Done for batch adding.
- Details carries the typed title into the full project form for notes, date and duration without saving a draft. Existing projects still open their full editor directly.

- Revealing hidden tasks expands above the Hidden button and keeps that button in view; Completed and Trash still open below.

- Matched linked-task text size and line height to the duration and time details.
- Turning off in progress for a linked task stops its linked group together in the app and widget, while preserving unrelated active tasks.

- Persistent in-progress notifications show 🟢 before In progress, preserving custom titles, and use a rounded warning-triangle status icon.

## 0.62.0 — 2026-11-05

- The Hidden footer button now includes the current scheduled-hidden count in parentheses, such as Hidden (3).
- Today and Upcoming show counts for their own lists, respecting the selected context and updating as tasks reveal.

- Linked task names now include (hidden) while their scheduled visibility is hiding them, and clear that note at reveal time.

- Kept Urgent directly beneath Priority, followed by Contexts, and matched task-editor checkbox sizes to Urgent without changing main-list checkboxes or the widget front.

## 0.61.0 — 2026-11-05

- Shortened task-list footer buttons to Completed, Trash and Hidden, with smaller text-only buttons on one row.
- Open sections use a bold border while button labels remain unchanged.
- Today’s completed/total count, remaining duration and progress bar include scheduled-hidden tasks; a parenthetical count shows how many remain hidden.
- Revealing hidden tasks leaves the daily totals and scheduled-hidden count unchanged.

- Moved Contexts directly below Priority in detailed task forms, and Labels below Priority in widget Quick add.

- Added spacing between the Urgent checkbox and its label in widget Quick add.

- Made app Quick add more compact with slightly larger text, matching widget typography while retaining growing task titles.

## 0.60.0 — 2026-11-04

- Added a per-task Hide until before due time checkbox, with hours and minutes, in the detailed task editor.
- Hidden tasks stay out of Today, Upcoming and the widget until their reveal time; Show all at the bottom of the app temporarily reveals them for editing.
- Task reveal times follow moved dates and recurring occurrences, refresh automatically, and keep in-progress tasks and completion history visible.
- When the list is clear but a hidden task will appear later today, the app and widget show the next reveal time in the rest message.
- Both empty-list messages now end with Get some rest. 🪻

## 0.59.0 — 2026-11-04

- Urgent immediately selects High priority; unticking Urgent restores the priority from before it was enabled, including after saving and reopening the editor.
- Widget Quick add uses the same temporary High priority and remembers the underlying priority when saving urgent tasks.
- Choosing Medium or Low directly clears Urgent and retains the chosen priority.

## 0.58.0 — 2026-11-04

- Moving a task to another day clears its in-progress mark, including active checklist steps.
- Rescheduling through Do today, Do tomorrow, or the detailed editor updates the active-task banner, widget and persistent notification while preserving other active tasks.

- The in-progress notification stays ongoing and restores itself if Android allows it to be dismissed while tasks are still active.

## 0.57.0 — 2026-11-04

- Aligned the Settings Back button with the Today/Upcoming control.
- Added Urgent to widget Quick add; urgent tasks save at High priority and the checkbox resets after each add.
- Kept one large resize handle on Notes and Checklist, with editor scrolling that follows the drag.
- Added a persistent in-progress notification that updates for multiple active tasks, with customizable title and message in Settings.
- Starting a task also starts its linked tasks, without duplicating active tasks or following link cycles indefinitely.
- Kept the header date position and text size steady when switching between Today and Upcoming.

## 0.56.0 — 2026-11-04

- Moved Urgent directly beneath Priority in the detailed task editor. Notify remains beside the duration controls; Quick add and widget controls are unchanged.

## 0.55.0 — 2026-11-04

- Notes has one large corner drag handle; the duplicate browser grip is hidden. Checklist and Project resizing controls remain unchanged.
- Task titles expand and shrink automatically as text wraps in the editor and in-app Quick add, without a resize handle. Quick add keeps Enter-to-add and the editor keeps its visible Save/Cancel footer.
- Notifications use a rounded checkbox icon. The launcher icon is a checkmark with rounded ends and joins, retaining the existing white background and gradient colors.

- Removed the visible Task label from in-app Quick add so the popup fits its contents more compactly; the title keeps its placeholder and accessible label.

- Settings opens as a full app screen with a Back button and Android Back support, preserving task-list scroll position. It uses the app’s fixed background without a dimmed task list or popup frame.

## 0.54.0 — 2026-11-04

- Added an Urgent checkbox at the top right of in-app Quick add. It saves an urgent High-priority task, carries into Details and resets after a successful add.
- App checkboxes use squircle corners, with rounded-square fallback on older WebViews. Widget checkboxes, native label pickers and the widget editor retain their existing appearance.

- The Today summary wraps completion, remaining duration and untimed counts as whole sections. Separator dots disappear at line boundaries, matching task metadata wrapping.

- Urgent widget tasks show the red Urgent bubble in place of Today. The existing status tap changes it to In progress and back to Urgent, preserving the task’s urgent flag.

## 0.53.0 — 2026-11-04

- Swapped task notification action positions: Snooze on the left and Complete on the right.

- Inset the highlighted Today/Upcoming tab evenly inside its outer border, with matching inner corner rounding and unchanged outer control height.

## 0.52.0 — 2026-11-04

- Corrected Today/Upcoming alignment using the actual text tops instead of row-box centers. The Upcoming offset adapts to font changes, wrapping and screen width while leaving Today and all text sizes unchanged.
- Recurring task editors keep the selected occurrence date instead of substituting the historical repeat anchor. Date changes reanchor future repeats, preserve completed history and suppress old moved or retroactively generated overdue occurrences.
- Timed task notifications have Complete and Snooze actions. Complete handles the notified occurrence and its checklist, preserving future repeats. Snooze delays only that reminder and survives app restarts and alarm rescheduling.
- Settings → Notifications has a global Task snooze (minutes) duration, defaulting to 10 minutes. Daily reminders have no Complete or Snooze actions. Stale notification actions are ignored after completion, disabling notifications or changing the schedule.

## 0.51.0 — 2026-11-03

- Raised the Upcoming list slightly so Tomorrow is vertically aligned with the Today summary during swipes. The Today summary, text sizes and spacing between Upcoming dates are preserved.

## 0.50.0 — 2026-11-03

- Changed the notification bell to a solid white silhouette and reduced its drawing slightly, preserving its centered alignment and spacing beside the task time.

## 0.49.0 — 2026-11-03

- Tasks with a scheduled notification show a rounded white bell immediately before their time. Tasks without notifications have no bell.
- Widget task editors start as soon as the task interface is ready, without waiting for the full page load or rendering hidden task lists.
- Editor background dimming now covers the Android status bar, navigation bar and side insets consistently in both the app and widget editor.
- Removed Back to tasks from Projects and moved Show/Hide completed to the bottom. Android Back and outside-tap dismissal remain available.

- App startup shows the first completed layout without flashing intermediate adjustments. Hidden Upcoming rows are built after Today is ready, and the native shell avoids rereading an already-embedded bridge.

## 0.48.0 — 2026-11-03

- Removed the literal Checklist size slider. Notes and Checklist now expand directly from a small corner drag handle, with touch, mouse and keyboard support.
- Settings uses a viewport-sized gradient that stays fixed while its contents scroll, matching the main screens instead of stretching over the full Settings page.

## 0.47.0 — 2026-11-03

- Removed outer group frames from Priority, Repeat, Contexts and Linked tasks in editors.
- Group labels and contents align with the left edge of the other fields, while individual input and choice-button outlines remain visible.

## 0.46.0 — 2026-11-03

- Vertically centered duration, recurrence, linked-task and label icons with neighboring text, including wrapped captions.
- Standardized icon-to-text and label gaps at 6 pixels and removed a duplicate label margin that caused uneven spacing.
- Centered native label-picker images with their text and reduced the Guild crest to a fixed 20 dp size instead of scaling it with the system font.
- Extended the widget editor backdrop over the top status-bar area while retaining safe clearance below it.

- Added a Checklist size slider for expanding its field on touch screens.

- Bracketed Checklist lines create section dividers while retaining item identities and completion history.

## 0.45.0 — 2026-11-03

- Redrew the simple clock with equal-length hands and generous clearance from the circular rim.
- Redrew the repeat symbol with two balanced circular arcs and large solid arrowheads so the direction is clear.

- Redrew the chain as rounded interlocking links and the tree as a rounded canopy. Refined the phone cutout after reviewing the remaining icons.

- Rounded the white icon set and standardized its size and visible proportions. Guild and custom emoji sizing stay unchanged.

## 0.44.0 — 2026-11-03

- Simple clock, repeat and chain-link symbols are larger, with white strokes on a transparent background instead of a white surrounding disk.
- Settings → Notifications now lets you customize the daily reminder title and message separately from timed task notifications. Existing daily reminder wording remains the default.

- Original label defaults use solid white pictograms, with custom emoji overrides and the Guild crest preserved.

## 0.43.0 — 2026-11-03

- The full date and Today / Upcoming tabs stay together on one line. Slightly smaller tabs and tighter spacing preserve the text size, with proportional fitting only when the header needs more room.
- Task priority labels after the separator now read Low, Med and High.

- Task details wrap at their existing size, with separator dots hidden at line boundaries.

## 0.42.0 — 2026-11-03

- Low is now the default for all new tasks, including widget quick-add and projects converted to tasks.
- Existing tasks without a priority move into Low while keeping their details, completion history and future recurrences.
- Removed the Priority not set section, header count and widget heading. Editors offer only High, Medium and Low.
- Widget task editors respect the Android notification-bar inset so the window stays below it.

## 0.41.0 — 2026-11-03

- Added a separate Show trash / Hide trash area beside Show completed, with Restore and confirmed Delete forever controls.
- Permanent deletion stays deleted across reloads; removing completed recurring entries preserves future dates.
- Upcoming date groups now start collapsed. Saved task notes display inline and obsolete fixed instructions were removed.
- Editors lock background scrolling, keep a stable thin border and reduce excess widget-editor top spacing. Widget quick-add fields have more room for vertically centered text.
- Task details show duration then time, recurrence, labels and low/med/high priority, with consistent dot separators.
- Settings offers emoji icons, simple clock/repeat symbols or no duration/recurrence icons; emojis remain the default.
- The top summary includes the number of pending tasks with no priority and uses a dot before untimed tasks.
- Priority changes and task deletion fade briefly before rows move or disappear.
- Added global task notification text templates and matching linked-task symbols.

## 0.40.0 — 2026-11-03

- Added light haptic feedback to widget task actions and opening the app from the widget.
- Left-aligned buttons, dropdowns and editable fields, with vertically centered control content and centered icon controls.
- Added this complete update history to Settings.
- Trash confirmation turns red with no extra text, resets after three seconds and requires a second tap to delete.
- Detailed task editors keep their action buttons visible in a separate footer while fields scroll above it.
- Subtask Do now buttons have the same outlined border as main task buttons.

## 0.39.0 — 2026-11-03

- Matched the red Urgent badge to the In progress pill shape and sizing.
- Weekend shortcuts now choose Friday. Detailed editors open with the keyboard collapsed; Quick add keeps typing ready.
- Placed editor buttons below the fields instead of overlaying content. Added a two-tap outline trash button for deleting tasks.
- New tasks default to Priority not set; editors offer only High, Medium and Low.
- Upcoming headings show Tomorrow or weekday names with ordinal dates. The summary shows the done fraction and hours/minutes without a pending count.

## 0.38.0 — 2026-11-03

- Placed Urgent beside task titles when space permits, matching In progress spacing and vertical alignment.
- Badges move below long titles when needed without squeezing the title.

## 0.37.0 — 2026-11-03

- Tapping a widget task title opens the same full editor used in the app, over the home screen.
- Saved edits immediately refresh the widget and carry forward to recurring tasks. The widget heading still opens the app.

## 0.36.0 — 2026-11-03

- Added light haptics for add controls, successful task creation and date/time choices.
- Enabled Today/Upcoming swipes in blank space below short lists.
- Centered the completed-widget flower message across the whole widget.

## 0.35.0 — 2026-11-02

- Fixed priority taps after touch release and after holding: taps move one level, holds jump to High or Low.
- Removed Do today and Do tomorrow from repeating tasks so their schedules stay intact.

## 0.34.0 — 2026-11-02

- Quick add clears all fields after saving and keeps the popup open for another task.
- Added multiple checked labels and date/time choices to widget quick add.
- Added configurable Friday-evening visibility for weekend tasks without changing saved occurrence identities.
- Allowed several tasks to be In progress, with comma-separated titles in the app banner.
- Improved boundary priority buttons, linked-task text sizing, ordinal Upcoming dates and the centered empty-widget message.

## 0.33.0 — 2026-11-02

- Fixed manual task links so linked names and chain markers appear immediately and remain saved.

## 0.32.0 — 2026-11-02

- Moved In progress closer to the task title with consistent spacing.
- Centered the subtask disclosure arrow, removed the left rule and compacted step counts such as 0/4.

## 0.31.0 — 2026-11-02

- Sorted completed tasks by most recent completion first.
- Retained completion timestamps through widget actions and undo.

## 0.30.0 — 2026-11-02

- Improved reminder delivery for existing tasks and reminders delayed by Android.
- Added precise-alarm permission controls and kept valid reminders armed across unrelated saves.

## 0.29.0 — 2026-11-02

- Centered In progress vertically with task titles in the app and widget.
- Improved spacing around metadata separator dots.

## 0.28.0 — 2026-11-02

- Added a short widget completion delay so a mistaken checkmark can be undone.
- Styled widget In progress with the green badge used in the app.

## 0.27.0 — 2026-11-02

- Placed labels beside duration and recurrence with consistent separator dots.
- Made the widget Today label toggle In progress, pin active tasks to the top and restore their position when stopped.
- Added padding beside the context dropdown chevron.

## 0.26.0 — 2026-11-02

- Kept quick-add windows open after saving, cleared the form and flashed the popup border to confirm success.
- Added widget In progress controls synchronized with the app, slightly larger task text and a smaller bare accent plus.
- Used rounded priority squares, limited priority holds to High and Low, and placed the Do today arrow before its text.
- Refined the gradient checklist icon, inline label row and hidden scrollbars while retaining scrolling.

## 0.25.0 — 2026-11-02

- Added Projects, opened by holding the date: notes with optional dates and duration, separate completion history and conversion to tasks.
- Added confirmed project deletion with an outline trash icon.
- Enabled tab swipes starting on task controls and refined rounded, themed plus buttons.

## 0.24.0 — 2026-11-02

- Made tab swipes follow the finger and settle on release.
- Added emoji-label editing shortcuts and haptic priority holds.
- Restored Priority not set as a separate bottom section and renamed Show hidden to Show completed.

## 0.23.0 — 2026-11-02

- Added Do today for upcoming tasks and centered checkboxes against task titles.
- Only checkbox taps complete tasks; time, duration and recurrence open focused editors.
- Reduced priorities to High, Medium and Low, kept urgent work first and spaced active subtasks.

## 0.22.0 — 2026-11-02

- Smoothed tab transitions to remove vertical jumping.
- Aligned the widget plus with its outer edge.
- Included future daily occurrences in Upcoming even when today's occurrence is still pending.

## 0.21.0 — 2026-11-02

- Added swipe animations between Today and Upcoming.

## 0.20.0 — 2026-11-02

- Added spacing between widget popup controls without enlarging the popup.
- Removed the outline around the widget plus.

## 0.19.0 — 2026-11-02

- Sorted each section by due time, then alphabetically for equal times and untimed tasks.
- Reduced and centered the priority arrows to match other task controls.

## 0.18.0 — 2026-11-02

- Added quick priority up/down controls before each task's gear, with recurring edits retained.
- Applied rounded fields and buttons throughout the app and a matching gradient to the widget popup.

## 0.17.0 — 2026-11-02

- Simplified the widget header and controls, moved Add to the top and kept settings under the launcher's long-press action.
- Added subtle priority headings and extended widget content toward the edges.
- Prevented task editing from automatically opening the keyboard and enlarged the Guild crest.

## 0.16.0 — 2026-11-02

- Added a scrolling Today-only widget that excludes completed tasks.
- Simplified the header to Today and removed extra task-count and open-app text.

## 0.15.0 — 2026-11-02

- Slightly enlarged the Guild crest to better match the other labels.

## 0.14.0 — 2026-11-02

- Added the supplied Guild crest as a static image label using its first frame.

## 0.13.0 — 2026-11-02

- Added custom label emojis, including a picker and keyboard entry.
- Updated existing task labels and filters when a label's emoji changes.

## 0.12.0 — 2026-11-02

- Added editable checklists to existing tasks.
- Added opt-in scheduled-time notifications and a separate configurable daily reminder.
- Placed Notify beside Urgent and required a task time for scheduled reminders.

## 0.11.0 — 2026-11-01

- Kept the gradient fixed to the visible viewport across both lists and editors.
- Added touch swiping, improved matching Today/Upcoming layouts and refined checkbox alignment.
- Made Urgent red and checkboxes follow the accent color.

## 0.10.0 — 2026-11-01

- Added native time/date pickers, Tomorrow/Weekend shortcuts and Daily/Weekends frequency choices.
- Expanded hours/minutes duration fields and kept Notes and Checklist visible on separate rows.
- Moved custom label creation to Settings, added color wheels and applied gradients across the app.
- Added outside-tap dismissal and a longer completion undo delay.

## 0.9.0 — 2026-11-01

- Made Today and Upcoming row sizes and spacing consistent.
- Removed frames and divider lines around upcoming date groups.

## 0.8.0 — 2026-11-01

- Improved title/checkbox alignment and made Upcoming groups frameless.

## 0.7.0 — 2026-11-01

- Renamed the app Tasks and made it open on Today.
- Added emoji-only context labels, flexible Every X day/week/month/year schedules and persistent manual links.
- Added background/accent colors and optional gradients, simpler Upcoming groups and no task numbering.

## 0.5.0 — 2026-11-01

- Added a completion pause with undo and highlighted tasks completed today.
- Allowed widget backgrounds to be fully transparent at 0% opacity.

## 0.4.0 — 2026-11-01

- Added corner-plus Quick add and a more compact detailed task editor.
- Added custom hex colors and a white checklist app icon.
- Improved Back handling for task menus.

## 0.3.0 — 2026-11-01

- Improved phone-width layouts and retained tasks and completion history across app updates.
- Added manual task links and recurring labels, gear editing, Do now and one-time rescheduling.
- Removed AI suggestions, automatic linked-task suggestions, copy/backup controls and manual completion-save controls.

## 0.2.0 — 2026-11-01

- Created the offline dark-mode Android app from the supplied task-list design and existing task history.
- Added Today/Upcoming lists, contexts, recurring tasks, checklists and persistent completion state.
- Added the home-screen widget and floating native quick-add popup.
