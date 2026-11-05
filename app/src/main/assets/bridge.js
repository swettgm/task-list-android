/* Generic Android integration. Personal task definitions and history are supplied
   privately in the installed document; this file contains no personal task data. */
(function (global) {
  'use strict';
  const KEY = 'task-list-20261020-v1';
  const APP_CHANGELOG = [{"version":"0.60.0","date":"2026-11-04","changes":["Added a per-task Hide until before due time checkbox, with hours and minutes, in the detailed task editor.","Hidden tasks stay out of Today, Upcoming and the widget until their reveal time; Show all at the bottom of the app temporarily reveals them for editing.","Task reveal times follow moved dates and recurring occurrences, refresh automatically, and keep in-progress tasks and completion history visible.","When the list is clear but a hidden task will appear later today, the app and widget show the next reveal time in the rest message.","Both empty-list messages now end with Get some rest. 🪻"]},{"version":"0.59.0","date":"2026-11-04","changes":["Urgent immediately selects High priority; unticking Urgent restores the priority from before it was enabled, including after saving and reopening the editor.","Widget Quick add uses the same temporary High priority and remembers the underlying priority when saving urgent tasks.","Choosing Medium or Low directly clears Urgent and retains the chosen priority."]},{"version":"0.58.0","date":"2026-11-04","changes":["Moving a task to another day clears its in-progress mark, including active checklist steps.","Rescheduling through Do today, Do tomorrow, or the detailed editor updates the active-task banner, widget and persistent notification while preserving other active tasks.","The in-progress notification stays ongoing and restores itself if Android allows it to be dismissed while tasks are still active."]},{"version":"0.57.0","date":"2026-11-04","changes":["Aligned the Settings Back button with the Today/Upcoming control.","Added Urgent to widget Quick add; urgent tasks save at High priority and the checkbox resets after each add.","Kept one large resize handle on Notes and Checklist, with editor scrolling that follows the drag.","Added a persistent in-progress notification that updates for multiple active tasks, with customizable title and message in Settings.","Starting a task also starts its linked tasks, without duplicating active tasks or following link cycles indefinitely.","Kept the header date position and text size steady when switching between Today and Upcoming."]},{"version":"0.56.0","date":"2026-11-04","changes":["Moved Urgent directly beneath Priority in the detailed task editor. Notify remains beside the duration controls; Quick add and widget controls are unchanged."]},{"version":"0.55.0","date":"2026-11-04","changes":["Notes has one large corner drag handle; the duplicate browser grip is hidden. Checklist and Project resizing controls remain unchanged.","Task titles expand and shrink automatically as text wraps in the editor and in-app Quick add, without a resize handle. Quick add keeps Enter-to-add and the editor keeps its visible Save/Cancel footer.","Notifications use a rounded checkbox icon. The launcher icon is a checkmark with rounded ends and joins, retaining the existing white background and gradient colors.","Removed the visible Task label from in-app Quick add so the popup fits its contents more compactly; the title keeps its placeholder and accessible label.","Settings opens as a full app screen with a Back button and Android Back support, preserving task-list scroll position. It uses the app’s fixed background without a dimmed task list or popup frame."]},{"version":"0.54.0","date":"2026-11-04","changes":["Added an Urgent checkbox at the top right of in-app Quick add. It saves an urgent High-priority task, carries into Details and resets after a successful add.","App checkboxes use squircle corners, with rounded-square fallback on older WebViews. Widget checkboxes, native label pickers and the widget editor retain their existing appearance.","The Today summary wraps completion, remaining duration and untimed counts as whole sections. Separator dots disappear at line boundaries, matching task metadata wrapping.","Urgent widget tasks show the red Urgent bubble in place of Today. The existing status tap changes it to In progress and back to Urgent, preserving the task’s urgent flag."]},{"version":"0.53.0","date":"2026-11-04","changes":["Swapped task notification action positions: Snooze on the left and Complete on the right.","Inset the highlighted Today/Upcoming tab evenly inside its outer border, with matching inner corner rounding and unchanged outer control height."]},{"version":"0.52.0","date":"2026-11-04","changes":["Corrected Today/Upcoming alignment using the actual text tops instead of row-box centers. The Upcoming offset adapts to font changes, wrapping and screen width while leaving Today and all text sizes unchanged.","Recurring task editors keep the selected occurrence date instead of substituting the historical repeat anchor. Date changes reanchor future repeats, preserve completed history and suppress old moved or retroactively generated overdue occurrences.","Timed task notifications have Complete and Snooze actions. Complete handles the notified occurrence and its checklist, preserving future repeats. Snooze delays only that reminder and survives app restarts and alarm rescheduling.","Settings → Notifications has a global Task snooze (minutes) duration, defaulting to 10 minutes. Daily reminders have no Complete or Snooze actions. Stale notification actions are ignored after completion, disabling notifications or changing the schedule."]},{"version":"0.51.0","date":"2026-11-03","changes":["Raised the Upcoming list slightly so Tomorrow is vertically aligned with the Today summary during swipes. The Today summary, text sizes and spacing between Upcoming dates are preserved."]},{"version":"0.50.0","date":"2026-11-03","changes":["Changed the notification bell to a solid white silhouette and reduced its drawing slightly, preserving its centered alignment and spacing beside the task time."]},{"version":"0.49.0","date":"2026-11-03","changes":["Tasks with a scheduled notification show a rounded white bell immediately before their time. Tasks without notifications have no bell.","Widget task editors start as soon as the task interface is ready, without waiting for the full page load or rendering hidden task lists.","Editor background dimming now covers the Android status bar, navigation bar and side insets consistently in both the app and widget editor.","Removed Back to tasks from Projects and moved Show/Hide completed to the bottom. Android Back and outside-tap dismissal remain available.","App startup shows the first completed layout without flashing intermediate adjustments. Hidden Upcoming rows are built after Today is ready, and the native shell avoids rereading an already-embedded bridge."]},{"version":"0.48.0","date":"2026-11-03","changes":["Removed the literal Checklist size slider. Notes and Checklist now expand directly from a small corner drag handle, with touch, mouse and keyboard support.","Settings uses a viewport-sized gradient that stays fixed while its contents scroll, matching the main screens instead of stretching over the full Settings page."]},{"version":"0.47.0","date":"2026-11-03","changes":["Removed outer group frames from Priority, Repeat, Contexts and Linked tasks in editors.","Group labels and contents align with the left edge of the other fields, while individual input and choice-button outlines remain visible."]},{"version":"0.46.0","date":"2026-11-03","changes":["Vertically centered duration, recurrence, linked-task and label icons with neighboring text, including wrapped captions.","Standardized icon-to-text and label gaps at 6 pixels and removed a duplicate label margin that caused uneven spacing.","Centered native label-picker images with their text and reduced the Guild crest to a fixed 20 dp size instead of scaling it with the system font.","Extended the widget editor backdrop over the top status-bar area while retaining safe clearance below it.","Added a touch-friendly Checklist size slider in detailed task editors, while retaining the textarea resize handle and the separate visible action footer.","Checklist lines in [brackets] create section dividers. Headings do not count as tasks; existing Guild groups and completed item identities are preserved when editing or reorganizing sections."]},{"version":"0.45.0","date":"2026-11-03","changes":["Redrew the simple clock with equal-length hands and generous clearance from the circular rim.","Redrew the repeat symbol with two balanced circular arcs and large solid arrowheads so the direction is clear.","Redrew the chain as two rounded interlocking links, matching the emoji in white.","Replaced the pointed tree with a rounded canopy and refined the phone home-button cutout while reviewing the rest of the icon set.","Rounded corners and tips throughout the white icon set and standardized all symbols to a shared 24-pixel canvas with balanced visible proportions. The Guild crest and custom emojis keep their existing sizes."]},{"version":"0.44.0","date":"2026-11-03","changes":["Simple clock, repeat and chain-link symbols are larger, with white strokes on a transparent background instead of a white surrounding disk.","Settings → Notifications now lets you customize the daily reminder title and message separately from timed task notifications. Existing daily reminder wording remains the default.","The original Home, Road, Phone, Computer and Outdoors labels use solid white pictograms by default, including native label pickers. Custom emoji choices still override them, and the Guild crest is unchanged."]},{"version":"0.43.0","date":"2026-11-03","changes":["The full date and Today / Upcoming tabs stay together on one line. Slightly smaller tabs and tighter spacing preserve the text size, with proportional fitting only when the header needs more room.","Task priority labels after the separator now read Low, Med and High.","Task detail sections wrap together at their existing text size. Separator dots disappear at line boundaries instead of dangling at the end or start of a line."]},{"version":"0.42.0","date":"2026-11-03","changes":["Low is now the default for all new tasks, including widget quick-add and projects converted to tasks.","Existing tasks without a priority move into Low while keeping their details, completion history and future recurrences.","Removed the Priority not set section, header count and widget heading. Editors offer only High, Medium and Low.","Widget task editors respect the Android notification-bar inset so the window stays below it."]},{"version":"0.41.0","date":"2026-11-03","changes":["Added a separate Show trash / Hide trash area beside Show completed, with Restore and confirmed Delete forever controls.","Permanent deletion stays deleted across reloads; removing completed recurring entries preserves future dates.","Upcoming date groups now start collapsed. Saved task notes display inline and obsolete fixed instructions were removed.","Editors lock background scrolling, keep a stable thin border and reduce excess widget-editor top spacing. Widget quick-add fields have more room for vertically centered text.","Task details show duration then time, recurrence, labels and low/med/high priority, with consistent dot separators.","Settings offers emoji icons, simple clock/repeat symbols or no duration/recurrence icons; emojis remain the default.","The top summary includes the number of pending tasks with no priority and uses a dot before untimed tasks.","Priority changes and task deletion fade briefly before rows move or disappear.","Added global task notification title and message templates in Settings, with task, time and date placeholders.","Added a matching chain-link symbol for linked tasks under Task detail icons."]},{"version":"0.40.0","date":"2026-11-03","changes":["Added light haptic feedback to widget task actions and opening the app from the widget.","Left-aligned buttons, dropdowns and editable fields, with vertically centered control content and centered icon controls.","Added this complete update history to Settings.","Trash confirmation turns red with no extra text, resets after three seconds and requires a second tap to delete.","Detailed task editors keep their action buttons visible in a separate footer while fields scroll above it.","Subtask Do now buttons have the same outlined border as main task buttons."]},{"version":"0.39.0","date":"2026-11-03","changes":["Matched the red Urgent badge to the In progress pill shape and sizing.","Weekend shortcuts now choose Friday. Detailed editors open with the keyboard collapsed; Quick add keeps typing ready.","Placed editor buttons below the fields instead of overlaying content. Added a two-tap outline trash button for deleting tasks.","New tasks default to Priority not set; editors offer only High, Medium and Low.","Upcoming headings show Tomorrow or weekday names with ordinal dates. The summary shows the done fraction and hours/minutes without a pending count."]},{"version":"0.38.0","date":"2026-11-03","changes":["Placed Urgent beside task titles when space permits, matching In progress spacing and vertical alignment.","Badges move below long titles when needed without squeezing the title."]},{"version":"0.37.0","date":"2026-11-03","changes":["Tapping a widget task title opens the same full editor used in the app, over the home screen.","Saved edits immediately refresh the widget and carry forward to recurring tasks. The widget heading still opens the app."]},{"version":"0.36.0","date":"2026-11-03","changes":["Added light haptics for add controls, successful task creation and date/time choices.","Enabled Today/Upcoming swipes in blank space below short lists.","Centered the completed-widget flower message across the whole widget."]},{"version":"0.35.0","date":"2026-11-02","changes":["Fixed priority taps after touch release and after holding: taps move one level, holds jump to High or Low.","Removed Do today and Do tomorrow from repeating tasks so their schedules stay intact."]},{"version":"0.34.0","date":"2026-11-02","changes":["Quick add clears all fields after saving and keeps the popup open for another task.","Added multiple checked labels and date/time choices to widget quick add.","Added configurable Friday-evening visibility for weekend tasks without changing saved occurrence identities.","Allowed several tasks to be In progress, with comma-separated titles in the app banner.","Improved boundary priority buttons, linked-task text sizing, ordinal Upcoming dates and the centered empty-widget message."]},{"version":"0.33.0","date":"2026-11-02","changes":["Fixed manual task links so linked names and chain markers appear immediately and remain saved."]},{"version":"0.32.0","date":"2026-11-02","changes":["Moved In progress closer to the task title with consistent spacing.","Centered the subtask disclosure arrow, removed the left rule and compacted step counts such as 0/4."]},{"version":"0.31.0","date":"2026-11-02","changes":["Sorted completed tasks by most recent completion first.","Retained completion timestamps through widget actions and undo."]},{"version":"0.30.0","date":"2026-11-02","changes":["Improved reminder delivery for existing tasks and reminders delayed by Android.","Added precise-alarm permission controls and kept valid reminders armed across unrelated saves."]},{"version":"0.29.0","date":"2026-11-02","changes":["Centered In progress vertically with task titles in the app and widget.","Improved spacing around metadata separator dots."]},{"version":"0.28.0","date":"2026-11-02","changes":["Added a short widget completion delay so a mistaken checkmark can be undone.","Styled widget In progress with the green badge used in the app."]},{"version":"0.27.0","date":"2026-11-02","changes":["Placed labels beside duration and recurrence with consistent separator dots.","Made the widget Today label toggle In progress, pin active tasks to the top and restore their position when stopped.","Added padding beside the context dropdown chevron."]},{"version":"0.26.0","date":"2026-11-02","changes":["Kept quick-add windows open after saving, cleared the form and flashed the popup border to confirm success.","Added widget In progress controls synchronized with the app, slightly larger task text and a smaller bare accent plus.","Used rounded priority squares, limited priority holds to High and Low, and placed the Do today arrow before its text.","Refined the gradient checklist icon, inline label row and hidden scrollbars while retaining scrolling."]},{"version":"0.25.0","date":"2026-11-02","changes":["Added Projects, opened by holding the date: notes with optional dates and duration, separate completion history and conversion to tasks.","Added confirmed project deletion with an outline trash icon.","Enabled tab swipes starting on task controls and refined rounded, themed plus buttons."]},{"version":"0.24.0","date":"2026-11-02","changes":["Made tab swipes follow the finger and settle on release.","Added emoji-label editing shortcuts and haptic priority holds.","Restored Priority not set as a separate bottom section and renamed Show hidden to Show completed."]},{"version":"0.23.0","date":"2026-11-02","changes":["Added Do today for upcoming tasks and centered checkboxes against task titles.","Only checkbox taps complete tasks; time, duration and recurrence open focused editors.","Reduced priorities to High, Medium and Low, kept urgent work first and spaced active subtasks."]},{"version":"0.22.0","date":"2026-11-02","changes":["Smoothed tab transitions to remove vertical jumping.","Aligned the widget plus with its outer edge.","Included future daily occurrences in Upcoming even when today's occurrence is still pending."]},{"version":"0.21.0","date":"2026-11-02","changes":["Added swipe animations between Today and Upcoming."]},{"version":"0.20.0","date":"2026-11-02","changes":["Added spacing between widget popup controls without enlarging the popup.","Removed the outline around the widget plus."]},{"version":"0.19.0","date":"2026-11-02","changes":["Sorted each section by due time, then alphabetically for equal times and untimed tasks.","Reduced and centered the priority arrows to match other task controls."]},{"version":"0.18.0","date":"2026-11-02","changes":["Added quick priority up/down controls before each task's gear, with recurring edits retained.","Applied rounded fields and buttons throughout the app and a matching gradient to the widget popup."]},{"version":"0.17.0","date":"2026-11-02","changes":["Simplified the widget header and controls, moved Add to the top and kept settings under the launcher's long-press action.","Added subtle priority headings and extended widget content toward the edges.","Prevented task editing from automatically opening the keyboard and enlarged the Guild crest."]},{"version":"0.16.0","date":"2026-11-02","changes":["Added a scrolling Today-only widget that excludes completed tasks.","Simplified the header to Today and removed extra task-count and open-app text."]},{"version":"0.15.0","date":"2026-11-02","changes":["Slightly enlarged the Guild crest to better match the other labels."]},{"version":"0.14.0","date":"2026-11-02","changes":["Added the supplied Guild crest as a static image label using its first frame."]},{"version":"0.13.0","date":"2026-11-02","changes":["Added custom label emojis, including a picker and keyboard entry.","Updated existing task labels and filters when a label's emoji changes."]},{"version":"0.12.0","date":"2026-11-02","changes":["Added editable checklists to existing tasks.","Added opt-in scheduled-time notifications and a separate configurable daily reminder.","Placed Notify beside Urgent and required a task time for scheduled reminders."]},{"version":"0.11.0","date":"2026-11-01","changes":["Kept the gradient fixed to the visible viewport across both lists and editors.","Added touch swiping, improved matching Today/Upcoming layouts and refined checkbox alignment.","Made Urgent red and checkboxes follow the accent color."]},{"version":"0.10.0","date":"2026-11-01","changes":["Added native time/date pickers, Tomorrow/Weekend shortcuts and Daily/Weekends frequency choices.","Expanded hours/minutes duration fields and kept Notes and Checklist visible on separate rows.","Moved custom label creation to Settings, added color wheels and applied gradients across the app.","Added outside-tap dismissal and a longer completion undo delay."]},{"version":"0.9.0","date":"2026-11-01","changes":["Made Today and Upcoming row sizes and spacing consistent.","Removed frames and divider lines around upcoming date groups."]},{"version":"0.8.0","date":"2026-11-01","changes":["Improved title/checkbox alignment and made Upcoming groups frameless."]},{"version":"0.7.0","date":"2026-11-01","changes":["Renamed the app Tasks and made it open on Today.","Added emoji-only context labels, flexible Every X day/week/month/year schedules and persistent manual links.","Added background/accent colors and optional gradients, simpler Upcoming groups and no task numbering."]},{"version":"0.5.0","date":"2026-11-01","changes":["Added a completion pause with undo and highlighted tasks completed today.","Allowed widget backgrounds to be fully transparent at 0% opacity."]},{"version":"0.4.0","date":"2026-11-01","changes":["Added corner-plus Quick add and a more compact detailed task editor.","Added custom hex colors and a white checklist app icon.","Improved Back handling for task menus."]},{"version":"0.3.0","date":"2026-11-01","changes":["Improved phone-width layouts and retained tasks and completion history across app updates.","Added manual task links and recurring labels, gear editing, Do now and one-time rescheduling.","Removed AI suggestions, automatic linked-task suggestions, copy/backup controls and manual completion-save controls."]},{"version":"0.2.0","date":"2026-11-01","changes":["Created the offline dark-mode Android app from the supplied task-list design and existing task history.","Added Today/Upcoming lists, contexts, recurring tasks, checklists and persistent completion state.","Added the home-screen widget and floating native quick-add popup."]}];
  const DEFAULT_CONTEXTS = ['At home', 'On the road', 'On the phone', 'On the computer', 'Outdoors', 'Guild'];
  let native = global.TaskListNative || null;
  let fallback = null;
  try { fallback = global.localStorage; } catch (_) {}
  let cached = null;
  let lastCanonical = null;
  let scope = null;
  let selectedContext = '';
  let baselineRetired=new Set();
  let snapshotTimer = 0;
  const clone = value => JSON.parse(JSON.stringify(value));
  const parse = value => { try { const result = JSON.parse(value || '{}'); return result && typeof result === 'object' && !Array.isArray(result) ? result : {}; } catch (_) { return {}; } };
  function readCanonical() {
    if (cached) return cached;
    let value = null;
    try { if (native && native.readState) value = native.readState(); } catch (_) {}
    if (!value && fallback) { try { value = fallback.getItem(KEY); } catch (_) {} }
    cached = parse(value);lastCanonical=clone(cached);
    return cached;
  }
  function equalState(a,b){
    if(a===b)return true;
    if(!a||!b||typeof a!=='object'||typeof b!=='object'||Array.isArray(a)!==Array.isArray(b))return false;
    const ak=Object.keys(a).sort(),bk=Object.keys(b).sort();return ak.length===bk.length&&ak.every((key,i)=>key===bk[i]&&equalState(a[key],b[key]));
  }
  function mergeState(base,local,remote,key){
    if(equalState(base,local))return remote===undefined?undefined:clone(remote);
    if(local===undefined)return undefined;
    if(key==='linkPeers'&&Array.isArray(local)){
      const before=new Set(Array.isArray(base)?base:[]),after=new Set(local),current=new Set(Array.isArray(remote)?remote:[]);
      before.forEach(peer=>{if(!after.has(peer))current.delete(peer);});after.forEach(peer=>{if(!before.has(peer))current.add(peer);});return Array.from(current);
    }
    if(key==='customTasks'&&Array.isArray(local)){
      const index=list=>new Map((Array.isArray(list)?list:[]).filter(item=>item&&typeof item.id==='string').map(item=>[item.id,item]));
      const before=index(base),after=index(local),current=index(remote);
      new Set([...before.keys(),...after.keys()]).forEach(id=>{const merged=mergeState(before.get(id),after.get(id),current.get(id),'task');if(merged===undefined)current.delete(id);else current.set(id,merged);});return Array.from(current.values());
    }
    if(local&&typeof local==='object'&&!Array.isArray(local)){
      const before=base&&typeof base==='object'&&!Array.isArray(base)?base:{};
      const current=remote&&typeof remote==='object'&&!Array.isArray(remote)?clone(remote):{};
      new Set([...Object.keys(before),...Object.keys(local)]).forEach(name=>{const merged=mergeState(before[name],local[name],current[name],key==='manualLinks'?'linkPeers':name);if(merged===undefined)delete current[name];else current[name]=merged;});return current;
    }
    return clone(local);
  }
  function isPurgedOccurrence(task,date,state=readCanonical()) {return !!task&&!!(state.completionPurged||{})[task.id+':'+date];}
  function cleanPurgedState(state){
    const ids=Object.keys(state.taskPurged||{}).filter(id=>state.taskPurged[id]),occurrences=Object.keys(state.completionPurged||{}).filter(key=>state.completionPurged[key]);
    if(!ids.length&&!occurrences.length)return state;
    const removed=key=>ids.some(id=>key===id||key.startsWith(id+':'))||occurrences.some(prefix=>key===prefix||key.startsWith(prefix+':'));
    if(Array.isArray(state.customTasks))state.customTasks=state.customTasks.filter(task=>!ids.includes(task.id));
    if(state.taskOverrides)ids.forEach(id=>delete state.taskOverrides[id]);
    for(const field of ['completed','completedOn','completedAt','movedTo','rolloverCompletedOn'])if(state[field])Object.keys(state[field]).forEach(key=>{if(removed(key))delete state[field][key];});
    if(state.manualLinks){ids.forEach(id=>delete state.manualLinks[id]);Object.keys(state.manualLinks).forEach(id=>{if(Array.isArray(state.manualLinks[id]))state.manualLinks[id]=state.manualLinks[id].filter(peer=>!ids.includes(peer));});}
    const active=[...new Set([...(state.inProgressTasks||[]),state.inProgress].filter(Boolean))].filter(key=>!removed(key));state.inProgressTasks=active;state.inProgress=active.at(-1)||null;
    return state;
  }
  function persist(value) {
    const previous=readCanonical(),before=lastCanonical||clone(previous);
    let fresh=previous;
    try{if(native&&native.readState)fresh=parse(native.readState());}catch(_){}
    const desired=Object.assign({},previous,value);
    const next=cleanPurgedState(mergeState(before,desired,fresh,'state'));
    const normalize=task=>task&&typeof task==='object'?{...task,priority:task.priority==='HIGHEST'?'HIGH':['HIGH','MEDIUM','LOW'].includes(task.priority)?task.priority:'LOW'}:task;
    if(Array.isArray(next.customTasks))next.customTasks=next.customTasks.map(normalize);
    if(next.taskOverrides)next.taskOverrides=Object.fromEntries(Object.entries(next.taskOverrides).map(([id,task])=>[id,normalize(task)]));
    if(typeof desired.uiContext==='string')next.uiContext=desired.uiContext;
    let succeeded = false;
    if (native && native.writeState) { native.writeState(JSON.stringify(next)); succeeded = true; }
    if (fallback) { try { fallback.setItem(KEY, JSON.stringify(next)); succeeded = true; } catch (_) {} }
    if (!succeeded) throw new Error('Task storage is unavailable');
    cached = next;lastCanonical=clone(next);
    if(scope){scope.setState(next);applyDefinitions(next);}
    return next;
  }
  global.TaskListStorage = {
    getItem(key) {
      if (key === KEY) { const data = readCanonical(); return Object.keys(data).length ? JSON.stringify(data) : null; }
      try { return fallback ? fallback.getItem(key) : null; } catch (_) { return null; }
    },
    setItem(key, value) {
      if (key === KEY) { persist(parse(value)); return; }
      if (native && key === KEY + '-probe') return;
      if (fallback) { try { fallback.setItem(key, value); return; } catch (error) { if (!native) throw error; } }
      // The reference probes storage before saving. Native state is durable too.
      if (!native) throw new Error('Task storage is unavailable');
    },
    removeItem(key) { try { if (fallback) fallback.removeItem(key); } catch (_) {} }
  };
  function actionHaptic(){try{if(native&&native.actionHaptic)native.actionHaptic();}catch(_){}}
  function node(tag, className, text) {
    const result = document.createElement(tag);
    if (className) result.className = className;
    if (text !== undefined) result.textContent = text;
    return result;
  }
  function action(text, fn, className) {
    const result = node('button', className || 'btn', text);
    result.type = 'button'; result.addEventListener('click', fn); return result;
  }
  const GUILD_ICON_TOKEN='image:guild';
  const GUILD_ICON_DATA='data:image/png;base64,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';
  const contextIcon=label=>{const stored=readCanonical().contextIcons||{},defaults={'At home':'🏠','On the road':'🚗','On the phone':'📱','On the computer':'💻','Outdoors':'🌳','Guild':GUILD_ICON_TOKEN};if(Object.prototype.hasOwnProperty.call(stored,label)&&typeof stored[label]==='string'&&stored[label].trim())return stored[label];return Object.prototype.hasOwnProperty.call(defaults,label)?defaults[label]:'🏷️';};
  const DEFAULT_CONTEXT_SYMBOLS = {"At home":"M3.5 10.5L10.8 3.2Q12 2 13.2 3.2L20.5 10.5Q21.5 11.5 20.2 12H19.5V20Q19.5 21 18.5 21H14V15Q14 14 13 14H11Q10 14 10 15V21H5.5Q4.5 21 4.5 20V12H3.8Q2.5 11.5 3.5 10.5Z","On the road":"M7 3H17Q18 3 18.4 4L20.8 9.5Q21 10 21 11V18Q21 19 20 19V20Q20 21 19 21H18Q17 21 17 20V19H7V20Q7 21 6 21H5Q4 21 4 20V19Q3 19 3 18V11Q3 10 3.2 9.5L5.6 4Q6 3 7 3ZM8 5Q7.4 5 7.2 5.6L5.8 9Q5.4 10 6.5 10H17.5Q18.6 10 18.2 9L16.8 5.6Q16.6 5 16 5ZM7 12.5a1.5 1.5 0 1 0 0 3a1.5 1.5 0 1 0 0-3ZM17 12.5a1.5 1.5 0 1 0 0 3a1.5 1.5 0 1 0 0-3Z","On the phone":"M7 3H17Q19 3 19 5V19Q19 21 17 21H7Q5 21 5 19V5Q5 3 7 3ZM8.8 6H15.2Q16 6 16 6.8V15.2Q16 16 15.2 16H8.8Q8 16 8 15.2V6.8Q8 6 8.8 6ZM12 17.5a1 1 0 1 0 0 2a1 1 0 1 0 0-2Z","On the computer":"M4.5 3h15Q21 3 21 4.5v10Q21 16 19.5 16H13.5V19H17Q18 19 18 20Q18 21 17 21H7Q6 21 6 20Q6 19 7 19H10.5V16H4.5Q3 16 3 14.5V4.5Q3 3 4.5 3ZM6 5H18Q19 5 19 6V12.5Q19 14 18 14H6Q5 14 5 12.5V6Q5 5 6 5Z","Outdoors":"M7.5 16C4.8 16 3 14.2 3 11.8C3 9.4 4.7 7.5 7 7.2C7.5 4.5 9.4 3 12 3C14.6 3 16.5 4.5 17 7.2C19.3 7.5 21 9.4 21 11.8C21 14.2 19.2 16 16.5 16H14.5Q13.5 16 13.5 17V20Q13.5 21 12.5 21H11.5Q10.5 21 10.5 20V17Q10.5 16 9.5 16H7.5Z"};
  const contextSymbol=label=>{const icons=readCanonical().contextIcons||{};if(Object.prototype.hasOwnProperty.call(icons,label)&&typeof icons[label]==='string'&&icons[label].trim())return null;return Object.prototype.hasOwnProperty.call(DEFAULT_CONTEXT_SYMBOLS,label)?DEFAULT_CONTEXT_SYMBOLS[label]:null;};
  function contextGlyph(label,className='') {
    const glyph=node('span','tl-context-glyph'+(className?' '+className:'')),value=contextIcon(label);
    const symbol=contextSymbol(label);if(symbol){const ns='http://www.w3.org/2000/svg',svg=document.createElementNS(ns,'svg'),path=document.createElementNS(ns,'path');svg.setAttribute('viewBox','0 0 24 24');svg.setAttribute('fill','currentColor');svg.setAttribute('stroke','none');svg.setAttribute('stroke-width','2');svg.setAttribute('stroke-linecap','round');svg.setAttribute('stroke-linejoin','round');svg.setAttribute('aria-hidden','true');path.setAttribute('d',symbol);path.setAttribute('fill-rule','evenodd');svg.append(path);glyph.classList.add('tl-context-symbol');glyph.dataset.label=label;glyph.append(svg);return glyph;}
    if(value===GUILD_ICON_TOKEN){const img=node('img','tl-context-image');img.src=GUILD_ICON_DATA;img.alt=label;img.draggable=false;img.width=64;img.height=64;glyph.append(img);}else glyph.textContent=value;
    return glyph;
  }
  function derivedContexts(task) {
    if (Array.isArray(task._contexts)) return [...new Set(task._contexts.filter(x => typeof x === 'string' && x.trim()).map(x => x.trim()))];
    if (Array.isArray(task.labels)) return [...new Set(task.labels.filter(x => typeof x === 'string' && x.trim()).map(x => x.trim()))];
    const result = [];
    const place = String(task.place || ''), device = String(task.device || '');
    if (place.includes('🏠')) result.push('At home');
    if (/[🚗🚚🚘🛣]/u.test(place)) result.push('On the road');
    if (place.includes('🌳')) result.push('Outdoors');
    if (device.includes('📱')) result.push('On the phone');
    if (device.includes('💻')) result.push('On the computer');
    return result;
  }
  function allContexts() {
    return [...new Set([...DEFAULT_CONTEXTS, ...(readCanonical().contextOptions || []), ...(scope ? scope.tasks.flatMap(derivedContexts) : [])])];
  }
  function matches(task) { return !selectedContext || derivedContexts(task).includes(selectedContext); }
  function rebuild() {
    if (!scope) return;
    scope.renderToday(); scope.renderFuture(); updateContextOptions(); queueSnapshot();if(projectPanel?.dialog.isConnected)projectPanel.render();
  }
  function saveExtras() {
    if (!scope) return;
    const data = readCanonical();
    persist(Object.assign({}, scope.getState(), {
      taskOverrides: data.taskOverrides || {}, customTasks: data.customTasks || [],
      pairOverrides: data.pairOverrides || {}, uiContext: selectedContext
    }));
  }
  function saveDefinition(task, isNew, peers, fade=false) {
    const previousTask=scope?.tasks.find(value=>value.id===task.id),previousPriority=previousTask?.priority;
    if(task.urgent){const before=['HIGH','MEDIUM','LOW'].includes(task.priorityBeforeUrgent)?task.priorityBeforeUrgent:(previousTask?.priorityBeforeUrgent||previousTask?.priority||task.priority||'LOW');task={...task,priority:'HIGH',priorityBeforeUrgent:before};}else{task={...task};delete task.priorityBeforeUrgent;}
    const state = clone(readCanonical());
    if((state.taskPurged||{})[task.id])throw Error('This task was permanently deleted.');
    if (isNew) {
      const custom = Array.isArray(state.customTasks) ? state.customTasks.slice() : [];
      custom.push(clone(task)); state.customTasks = custom;
    } else if ((state.customTasks || []).some(item => item.id === task.id)) {
      state.customTasks = state.customTasks.map(item => item.id === task.id ? clone(task) : item);
    } else {
      state.taskOverrides = Object.assign({}, state.taskOverrides, { [task.id]: clone(task) });
    }
    const anchor=value=>value?.due||value?.repeat?.start||value?.startsOn||value?.intervalStart||value?.weekendCycleStart||'';
    if(!isNew&&anchor(previousTask)!==anchor(task)){
      const active=(state.inProgressTasks||[state.inProgress].filter(Boolean)).filter(key=>!key.startsWith(task.id+':'));
      state.inProgressTasks=active;state.inProgress=active.at(-1)||null;
    }
    if(Array.isArray(peers))state.manualLinks=manualLinksFor(state,task.id,peers);
    persist(Object.assign({}, scope.getState(), state));
    if(fade||(!isNew&&previousPriority!==task.priority))fadeRows(task.id);else rebuild();if(isNew)actionHaptic();
  }
  function applyDefinitions(state) {
    if (!scope) return;
    Object.entries(state.taskOverrides || {}).forEach(([id, value]) => {
      const index = scope.tasks.findIndex(t => t.id === id);
      if (index >= 0 && value && typeof value === 'object') scope.tasks[index] = Object.assign({}, clone(value), { id });
    });
    (Array.isArray(state.customTasks) ? state.customTasks : []).forEach(task => {
      if (!task || typeof task.id !== 'string' || typeof task.title !== 'string') return;
      const index = scope.tasks.findIndex(t => t.id === task.id);
      if (index >= 0) scope.tasks[index] = clone(task); else scope.tasks.push(clone(task));
    });
    for(let index=scope.tasks.length-1;index>=0;index--)if((state.taskPurged||{})[scope.tasks[index].id])scope.tasks.splice(index,1);
    scope.tasks.forEach(task=>{task.priority=task.urgent?'HIGH':task.priority==='HIGHEST'?'HIGH':task.priority==='LOWEST'?'LOW':['HIGH','MEDIUM','LOW'].includes(task.priority)?task.priority:'LOW';});
    // Original suggested pairings and legacy pairOverrides are no longer used.
    Object.keys(scope.pairWith).forEach(id => delete scope.pairWith[id]);
    const ids = new Set(scope.tasks.map(task => task.id));
    Object.entries(state.manualLinks || {}).forEach(([id, peers]) => {
      if (!ids.has(id) || !Array.isArray(peers)) return;
      peers.filter(peer => ids.has(peer) && peer !== id).forEach(peer => {
        scope.pairWith[id] = [...new Set([...(scope.pairWith[id] || []), peer])];
        scope.pairWith[peer] = [...new Set([...(scope.pairWith[peer] || []), id])];
      });
    });
  }
  function manualLinksFor(state,id,peers) {
    const links = {};
    Object.entries(state.manualLinks || {}).forEach(([key, values]) => {
      if (key !== id && Array.isArray(values)) links[key] = values.filter(peer => peer !== id);
    });
    links[id] = [...new Set(peers.filter(peer => peer !== id))];
    links[id].forEach(peer => { links[peer] = [...new Set([...(links[peer] || []), id])]; });
    return links;
  }
  function linkedTasksField(form, task, occurrenceDate) {
    const selected = new Set(task ? scope.pairWith[task.id] || [] : []);
    const group = node('fieldset', 'tl-context-options tl-links');
    group.append(node('legend', '', 'Linked tasks'));
    const row = node('div', 'tl-link-picker');
    const select = node('select', 'form-control');
    select.setAttribute('aria-label', 'Choose a task to link');
    const list = node('div', 'tl-linked-list');
    const atDate=scope.onDate(occurrenceDate||scope.today);
    const available=candidate=>{
      const occurrences=atDate.filter(item=>item.task.id===candidate.id);
      if(occurrences.length)return occurrences.some(item=>!scope.isDone(candidate,item.stateDate));
      if(candidate.due)return !scope.isDone(candidate,candidate.due);
      return !candidate.retired;
    };
    const candidates = scope.tasks.filter(candidate => !candidate.retired&&(!task || candidate.id !== task.id))
      .slice().sort((a, b) => a.title.localeCompare(b.title));
    function render() {
      select.replaceChildren();
      const placeholder = node('option', '', 'Choose a task…'); placeholder.value = ''; select.append(placeholder);
      candidates.filter(candidate => !selected.has(candidate.id)&&available(candidate)).forEach(candidate => {
        const option = node('option', '', candidate.title);
        option.value = candidate.id; select.append(option);
      });
      list.replaceChildren();
      selected.forEach(id => {
        const target = candidates.find(candidate => candidate.id === id); if (!target) return;
        const chip = node('div', 'tl-linked-task'); chip.append(node('span', '', target.title));
        const remove = action('×', () => { selected.delete(id); render(); }, 'btn tl-unlink');
        remove.setAttribute('aria-label', 'Unlink ' + target.title); chip.append(remove); list.append(chip);
      });
    }
    select.addEventListener('change', () => { if(select.value){selected.add(select.value);render();} });
    row.append(select); group.append(row, list); form.append(group); render();
    return () => Array.from(selected);
  }
  let settingsScreen=null;
  let modalScroll=null;
  function lockModalScroll(){if(modalScroll)return;const body=document.body,html=document.documentElement;modalScroll={x:scrollX,y:scrollY,htmlOverflow:html.style.overflow,styles:{}};for(const key of ['position','top','left','right','width','overflow'])modalScroll.styles[key]=body.style[key];html.style.overflow='hidden';if(native&&native.setModalOpen)native.setModalOpen(true);Object.assign(body.style,{position:'fixed',top:-modalScroll.y+'px',left:'0',right:'0',width:'100%',overflow:'hidden'});}
  function unlockModalScroll(){if(!modalScroll||document.querySelector('dialog[open]'))return;const saved=modalScroll;modalScroll=null;Object.assign(document.body.style,saved.styles);document.documentElement.style.overflow=saved.htmlOverflow;if(native&&native.setModalOpen)native.setModalOpen(false);global.scrollTo(saved.x,saved.y);}
  function modal(title) {
    lockModalScroll();const dialog = node('dialog', 'tl-dialog');
    const heading = node('h2', '', title); dialog.append(heading);
    const form = node('form'); form.method = 'dialog'; dialog.append(form);
    document.body.append(dialog);
    const close = () => { for(const animation of dialog.getAnimations?.()||[])animation.cancel();if (dialog.open && dialog.close) dialog.close(); dialog.remove();unlockModalScroll();if(global.TaskListWidgetEditor&&native&&native.finishTaskEditor){publishSnapshot();native.finishTaskEditor();} };
    dialog.addEventListener('cancel', event => { event.preventDefault(); close(); });
    const outside=event=>{const box=dialog.getBoundingClientRect();return event.clientX<box.left||event.clientX>box.right||event.clientY<box.top||event.clientY>box.bottom;};
    let backdropPressed=false;
    dialog.addEventListener('pointerdown',event=>{backdropPressed=event.target===dialog&&outside(event);});
    dialog.addEventListener('click',event=>{if(backdropPressed&&event.target===dialog&&outside(event))close();backdropPressed=false;});
    if (dialog.showModal) dialog.showModal(); else dialog.setAttribute('open', '');
    return { dialog, form, close };
  }
  function inputField(form, label, type, value) {
    const wrap = node('label', 'tl-field'); wrap.append(node('span', '', label));
    const field = node(type === 'textarea' ? 'textarea' : 'input', 'form-control');
    if (type !== 'textarea') field.type = type;
    field.value = value === undefined || value === null ? '' : String(value);
    if (type === 'textarea') field.rows = 2;
    if(type==='date'||type==='time')field.addEventListener('change',()=>{if(field.validity.valid)actionHaptic();});
    wrap.append(field); form.append(wrap); return field;
  }
  function taskTitleField(form,value){
    const field=inputField(form,'Task','textarea',value);field.rows=1;field.classList.add('tl-task-title');field.placeholder='What needs doing?';field.setAttribute('aria-label','Task');
    const resize=()=>{if(!field.isConnected)return;const style=getComputedStyle(field),border=(parseFloat(style.borderTopWidth)||0)+(parseFloat(style.borderBottomWidth)||0),minimum=parseFloat(style.minHeight)||38;field.style.height='auto';field.style.height=Math.max(minimum,field.scrollHeight+border)+'px';};
    field.addEventListener('input',resize);field.addEventListener('beforeinput',event=>{if((event.inputType==='insertLineBreak'||event.inputType==='insertParagraph')&&!event.isComposing)event.preventDefault();});field.addEventListener('keydown',event=>{if(event.key==='Enter'&&!event.isComposing)event.preventDefault();});
    const dialog=field.closest('dialog');if(global.ResizeObserver){let width=-1;const observer=new ResizeObserver(entries=>{const next=entries[0]?.contentRect.width;if(next!==width){width=next;resize();}});observer.observe(field);dialog?.addEventListener('close',()=>observer.disconnect(),{once:true});}else{global.addEventListener('resize',resize);dialog?.addEventListener('close',()=>global.removeEventListener('resize',resize),{once:true});}
    resize();requestAnimationFrame(resize);return field;
  }
  function enableTextareaResize(field,label){
    const wrap=node('div','tl-textarea-resize-wrap'),handle=node('button','tl-textarea-resize-handle');field.before(wrap);wrap.append(field);handle.type='button';handle.setAttribute('aria-label','Resize '+label);handle.title='Drag to resize '+label;const ns='http://www.w3.org/2000/svg',svg=document.createElementNS(ns,'svg'),path=document.createElementNS(ns,'path');svg.setAttribute('viewBox','0 0 24 24');svg.setAttribute('aria-hidden','true');path.setAttribute('d','M7 17L17 7M13 17L17 13');path.setAttribute('fill','none');path.setAttribute('stroke','currentColor');path.setAttribute('stroke-width','1.5');path.setAttribute('stroke-linecap','round');svg.append(path);handle.append(svg);wrap.append(handle);
    let drag=null,frame=0;const resize=value=>{field.style.height=Math.max(80,Math.min(800,value))+'px';};
    const apply=()=>{if(drag)resize(drag.height+drag.lastY-drag.y+(drag.scroller?drag.scroller.scrollTop-drag.scroll:0));};
    const follow=time=>{frame=0;if(!drag||!field.isConnected)return;const scroller=drag.scroller;if(!scroller)return;const bounds=scroller.getBoundingClientRect(),elapsed=Math.min(32,time-(drag.time||time));drag.time=time;let speed=0;
      if(drag.direction>0&&drag.lastY>bounds.bottom-44&&field.getBoundingClientRect().height<800)speed=Math.min(1,(drag.lastY-bounds.bottom+44)/44)*.6;
      if(drag.direction<0&&drag.lastY<bounds.top+44&&field.getBoundingClientRect().height>80)speed=-Math.min(1,(bounds.top+44-drag.lastY)/44)*.6;
      if(speed){scroller.scrollTop+=speed*elapsed;apply();}frame=requestAnimationFrame(follow);
    };
    const clear=()=>{if(frame)cancelAnimationFrame(frame);frame=0;if(drag?.scroller)drag.scroller.style.overflowAnchor=drag.anchor;drag=null;};
    handle.addEventListener('pointerdown',event=>{if(event.button!==0)return;event.preventDefault();event.stopPropagation();clear();const scroller=field.closest('.tl-task-editor-fields');drag={id:event.pointerId,y:event.clientY,lastY:event.clientY,direction:0,height:field.getBoundingClientRect().height,scroller,scroll:scroller?.scrollTop||0,anchor:scroller?.style.overflowAnchor||''};if(scroller)scroller.style.overflowAnchor='none';handle.setPointerCapture(event.pointerId);frame=requestAnimationFrame(follow);});
    handle.addEventListener('pointermove',event=>{if(drag&&event.pointerId===drag.id){event.preventDefault();const delta=event.clientY-drag.lastY;if(delta)drag.direction=Math.sign(delta);drag.lastY=event.clientY;apply();}});
    const stop=event=>{if(drag&&event.pointerId===drag.id){clear();if(handle.hasPointerCapture(event.pointerId))handle.releasePointerCapture(event.pointerId);}};handle.addEventListener('pointerup',stop);handle.addEventListener('pointercancel',stop);handle.addEventListener('lostpointercapture',clear);field.closest('dialog')?.addEventListener('close',clear,{once:true});handle.addEventListener('click',event=>{event.preventDefault();event.stopPropagation();});
    handle.addEventListener('keydown',event=>{const current=field.getBoundingClientRect().height;if(event.key==='ArrowDown'){event.preventDefault();resize(current+20);}else if(event.key==='ArrowUp'){event.preventDefault();resize(current-20);}else if(event.key==='Home'){event.preventDefault();resize(80);}else if(event.key==='End'){event.preventDefault();resize(800);}});
  }
  function choiceField(form, label, choices, selected) {
    const group = node('fieldset', 'tl-choice-options'); group.dataset.field = label.toLowerCase();
    group.append(node('legend', '', label));
    const grid = node('div', 'tl-choice-grid');
    const name = 'tl-' + label.toLowerCase() + '-' + Math.random().toString(36).slice(2);
    const inputs = choices.map(([value, text]) => {
      const row = node('label', 'tl-choice'); const input = node('input');
      input.type = 'radio'; input.name = name; input.value = value; input.checked = value === selected;
      row.append(input, node('span', '', text)); grid.append(row); return input;
    });
    Object.defineProperty(group, 'value', {
      get() { return inputs.find(input => input.checked)?.value || ''; },
      set(value) { inputs.forEach(input => { input.checked = input.value === value; }); }
    });
    group.append(grid); form.append(group); return group;
  }
  function contextsField(form, selected) {
    const group = node('fieldset', 'tl-context-options tl-context-grid'); group.append(node('legend', '', 'Contexts'));
    const choices = [...new Set([...allContexts(), ...(selected || [])])];
    const grid = node('div', 'tl-choice-grid');
    const inputs = choices.map(label => {
      const row = node('label', 'tl-context-choice'); const check = node('input'); check.type = 'checkbox';
      check.checked = (selected || []).includes(label); row.setAttribute('aria-label',label);row.title=label;row.append(check, contextGlyph(label)); grid.append(row);
      return { label, check };
    });
    group.append(grid);
    form.append(group);
    return () => inputs.filter(x => x.check.checked).map(x => x.label);
  }

  function frequencyFields(form,original,initialDate,date,keepOccurrenceDate=false){
    const oldSpec=global.TasksSchedule.fromTask(original,initialDate);
    const oldMode=oldSpec.mode==='repeat'&&oldSpec.unit==='day'&&oldSpec.amount===1?'daily':(oldSpec.mode==='weekdays'&&JSON.stringify(oldSpec.weekdays)==='[0,6]'?'weekends':oldSpec.mode);
    if(oldSpec.mode==='repeat'&&!keepOccurrenceDate)date.value=oldSpec.start;
    const recurrence = choiceField(form, 'Repeat', [['once','Once'],['daily','Daily'],['weekdays','Weekdays'],['weekends','Weekends'],['repeat','Every…']], oldMode);
    const repeatRow=node('div','tl-repeat-row');
    repeatRow.append(node('span','','Every'));
    const amount=node('input','form-control tl-repeat-amount');amount.type='number';amount.min='1';amount.step='1';amount.value=oldSpec.amount||1;amount.setAttribute('aria-label','Repeat amount');
    const unit=node('select','form-control tl-repeat-unit');unit.setAttribute('aria-label','Repeat unit');
    [['day','Day'],['week','Week'],['month','Month'],['year','Year']].forEach(([value,label])=>{const option=node('option','',label);option.value=value;unit.append(option);});unit.value=oldSpec.unit||'day';
    repeatRow.append(amount,unit);recurrence.append(repeatRow);
    const week = node('div', 'tl-weekdays');
    const weekChecks = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'].map((name, index) => {
      const label = node('label', 'tl-context-choice'), check = node('input'); check.type = 'checkbox'; check.checked = (oldSpec.weekdays || []).includes(index);
      check.setAttribute('aria-label', name); label.append(check, node('span', '', name.slice(0, 3))); week.append(label); return check;
    }); recurrence.append(week);
    const toggleRepeat = () => {repeatRow.hidden=recurrence.value!=='repeat';week.hidden=recurrence.value!=='weekdays'&&!(recurrence.value==='repeat'&&unit.value==='week');};
    recurrence.addEventListener('change',toggleRepeat);unit.addEventListener('change',toggleRepeat);toggleRepeat();
    return {oldSpec,oldMode,recurrence,amount,unit,weekChecks};
  }
  function editComponent(task,item,component){
    const original=clone(task),names={time:'Time',frequency:'Frequency',duration:'Duration',labels:'Labels'};
    if(!names[component])return;
    const {dialog,form,close}=modal(names[component]);form.classList.add('tl-task-form','tl-component-form');
    form.append(node('p','tl-component-title',task.title));
    let readChange;
    if(component==='labels'){
      const readLabels=contextsField(form,derivedContexts(original));readChange=next=>{next._contexts=readLabels();};
    }else if(component==='time'){
      const match=/^(\d{1,2}):(\d{2})(?:\s*(AM|PM))?$/i.exec(original.time||'');let value='';if(match){let h=Number(match[1]);if(match[3])h=h%12+(match[3].toUpperCase()==='PM'?12:0);value=String(h).padStart(2,'0')+':'+match[2];}
      const time=inputField(form,'Time','time',value);time.setAttribute('aria-label','Time');
      const notifyLabel=node('label','tl-context-choice'),notify=node('input');notify.type='checkbox';notify.checked=!!original.notify;notify.setAttribute('aria-label','Notify at scheduled time');notifyLabel.append(notify,node('span','','Notify'));form.append(notifyLabel);
      const sync=()=>{notify.disabled=!time.value;if(!time.value)notify.checked=false;};time.addEventListener('input',sync);time.addEventListener('change',sync);sync();
      form.append(action('Clear',()=>{time.value='';sync();actionHaptic();},'btn tl-clear-time'));
      readChange=next=>{if(time.value){const [h,m]=time.value.split(':').map(Number);next.time=(h%12||12)+':'+String(m).padStart(2,'0')+(h>=12?' PM':' AM');}else{delete next.time;delete next.showBeforeMinutes;}if(time.value&&notify.checked)next.notify=true;else delete next.notify;};
    }else if(component==='duration'){
      const row=node('div','tl-duration-row');form.append(row);const hours=inputField(row,'Hours','number',original.minutes===undefined?'':Math.floor(original.minutes/60)),minutes=inputField(row,'Minutes','number',original.minutes===undefined?'':original.minutes%60);hours.min=minutes.min='0';hours.step=minutes.step='1';minutes.max='59';hours.setAttribute('aria-label','Hours');minutes.setAttribute('aria-label','Minutes');
      readChange=next=>{if((hours.value&&!/^\d+$/.test(hours.value))||(minutes.value&&!/^\d+$/.test(minutes.value))||Number(minutes.value)>59||((hours.value||minutes.value)&&(!Number.isSafeInteger(Number(hours.value)*60+Number(minutes.value))||Number(hours.value)*60+Number(minutes.value)<1)))throw Error('Enter hours and 0–59 minutes.');if(hours.value||minutes.value)next.minutes=Number(hours.value)*60+Number(minutes.value);else delete next.minutes;};
    }else{
      const initialDate=item?item.date:scope.today,date=inputField(form,'Starts','date',initialDate);date.setAttribute('aria-label','Repeat start date');
      const fields=frequencyFields(form,original,initialDate,date),{oldMode,recurrence,amount,unit,weekChecks}=fields;
      const signature=()=>JSON.stringify([recurrence.value,date.value,recurrence.value==='repeat'?[amount.value,unit.value]:null,recurrence.value==='weekdays'||recurrence.value==='repeat'&&unit.value==='week'?weekChecks.map(c=>c.checked):null]),initialSignature=signature();
      readChange=next=>{
        const parsed=new Date(date.value+'T12:00:00Z');if(!/^\d{4}-\d{2}-\d{2}$/.test(date.value)||!Number.isFinite(parsed.getTime())||parsed.toISOString().slice(0,10)!==date.value)throw Error('Choose a valid date.');
        const chosenDays=weekChecks.flatMap((check,index)=>check.checked?[index]:[]);
        if(recurrence.value==='weekdays'&&!chosenDays.length)throw Error('Select at least one weekday.');
        if(recurrence.value==='repeat'&&(!/^\d+$/.test(amount.value)||!Number.isSafeInteger(Number(amount.value))||Number(amount.value)<1))throw Error('Repeat amount must be at least 1.');
        if(signature()===initialSignature)return;
        if(oldMode!=='once'||recurrence.value!=='once'){next.recurrenceResetOn=scope.today;next.reminderScheduleVersion=Date.now().toString(36)+'-'+Math.random().toString(36).slice(2);}
        ['due','days','intervalDays','intervalStart','weekendCycleStart','recurrence','repeat','startsOn'].forEach(key=>delete next[key]);
        if(recurrence.value==='once')next.due=date.value;
        if(recurrence.value==='daily'){next.repeat={amount:1,unit:'day',start:date.value};next.recurrence='Daily';}
        if(recurrence.value==='weekends'){next.days=[0,6];next.startsOn=date.value;next.recurrence='Weekends';}
        if(recurrence.value==='weekdays'){next.days=chosenDays;next.startsOn=date.value;next.recurrence=global.TasksSchedule.weekdaysLabel(chosenDays);}
        if(recurrence.value==='repeat'){next.repeat={amount:Number(amount.value),unit:unit.value,start:date.value};if(unit.value==='week'&&chosenDays.length)next.repeat.weekdays=chosenDays;next.recurrence=global.TasksSchedule.describe(next);}
      };
    }
    const error=node('p','tl-form-error');error.setAttribute('role','alert');form.append(error);const buttons=node('div','tl-dialog-actions');
    const save=()=>{try{const next=clone(original);readChange(next);saveDefinition(next,false);close();}catch(e){error.textContent=e.message||'Couldn’t save. Please try again.';}};
    buttons.append(action('Cancel',close),action('Save',save,'btn btn-primary'));form.append(buttons);form.addEventListener('submit',e=>{e.preventDefault();save();});dialog.focus({preventScroll:true});
  }
  let rowFadeTimer=null;
  function fadeRows(taskId,date){
    const duration=global.matchMedia('(prefers-reduced-motion: reduce)').matches?0:180;
    document.querySelectorAll('.tm-task-item').forEach(row=>{if((row.dataset.taskId===taskId&&(!date||row.dataset.stateDate===date))||row.dataset.trashId===taskId){row.style.pointerEvents='none';if(duration&&row.animate)row.animate([{opacity:1},{opacity:0}],{duration,fill:'forwards'});}});
    queueSnapshot();if(!duration){rebuild();return;}clearTimeout(rowFadeTimer);rowFadeTimer=setTimeout(()=>{rowFadeTimer=null;rebuild();},duration);
  }
  function deleteTaskDefinition(task) {
    const state=clone(readCanonical()),retired={...clone(task),retired:true,trashedAt:new Date().toISOString()};
    if((state.customTasks||[]).some(value=>value.id===task.id))state.customTasks=state.customTasks.map(value=>value.id===task.id?retired:value);
    else state.taskOverrides={...(state.taskOverrides||{}),[task.id]:retired};
    const current=scope.getState(),active=(current.inProgressTasks||[current.inProgress].filter(Boolean)).filter(key=>!key.startsWith(task.id+':'));
    state.manualLinks=manualLinksFor(state,task.id,[]);
    persist({...current,...state,inProgressTasks:active,inProgress:active.at(-1)||null});
    fadeRows(task.id);
  }
  function restoreTask(task){saveDefinition({...clone(task),retired:false,trashedAt:undefined},false);}
  function purgeTask(task){const state=clone(readCanonical());state.taskPurged={...(state.taskPurged||{}),[task.id]:new Date().toISOString()};persist({...scope.getState(),...state});fadeRows(task.id);}
  function purgeCompletion(task,item){const state=clone(readCanonical()),key=scope.progressKey(task,item.stateDate);state.completionPurged={...(state.completionPurged||{}),[key]:new Date().toISOString()};persist({...scope.getState(),...state});fadeRows(task.id,item.stateDate);}
  function permanentDeleteButton(task,item,wrapper,error){
    const button=deleteControl('task',()=>{try{if(item)purgeCompletion(task,item);else purgeTask(task);}catch(e){error.textContent=e.message||'Couldn’t delete permanently. Please try again.';}},wrapper,null,{label:'Delete forever '+task.title,confirmLabel:'Confirm delete forever '+task.title});
    button.classList.add('tl-permanent-delete');button.title='Delete forever';return button;
  }
  function renderTrash(){
    if(!scope)return;const panel=document.getElementById('pt-trash'),list=document.getElementById('pt-trash-list'),toggle=document.getElementById('pt-toggle-trash');if(!panel||!list||!toggle)return;
    toggle.textContent=panel.hidden?'Show trash':'Hide trash';toggle.setAttribute('aria-expanded',String(!panel.hidden));list.replaceChildren();
    const tasks=scope.tasks.filter(task=>task.retired&&!baselineRetired.has(task.id)&&matches(task)).sort((a,b)=>(Date.parse(b.trashedAt)||0)-(Date.parse(a.trashedAt)||0)||a.title.localeCompare(b.title));
    if(!tasks.length)list.append(node('p','text-muted','Trash is empty.'));
    tasks.forEach(task=>{const row=node('div','tm-task-item tl-trash-row');row.dataset.trashId=task.id;row.append(node('span','tm-title',task.title));const controls=node('div','tm-item-actions'),error=node('p','tl-form-error');error.setAttribute('role','alert');controls.append(action('Restore',()=>{try{restoreTask(task);}catch(e){error.textContent=e.message||'Couldn’t restore this task.';}},'tm-move-control tl-restore-task'),permanentDeleteButton(task,null,row,error));row.append(controls,error);list.append(row);});
  }
  function editTask(task, item, prefill) {
    if (!scope) return;
    const isNew = !task; const original = task ? clone(task) : {};
    const { dialog, form, close } = modal(isNew ? 'Add task' : 'Edit task');
    form.classList.add('tl-task-form');
    const title = taskTitleField(form, original.title || (prefill && prefill.title) || ''); title.required = true;
    const priority = choiceField(form, 'Priority', scope.groups.map(([key,label])=>[key,label.replace(' priority','')]), original.priority || 'LOW');
    const schedule = node('div', 'tl-schedule-grid'); form.append(schedule);
    const initialDate = item ? item.date : (original.due || original.intervalStart || original.weekendCycleStart || (prefill && (prefill.dueDate || prefill.plannedDate)) || scope.today);
    const date = inputField(schedule, 'Date', 'date', initialDate); date.required = true;
    const shortcuts=node('div','tl-date-shortcuts');date.parentElement.append(shortcuts);
    shortcuts.append(action('Tomorrow',()=>{date.value=scope.addDays(scope.today,1);actionHaptic();}),action('Weekend',()=>{date.value=global.TasksSchedule.nextWeekend(scope.today);date.dataset.weekend='true';actionHaptic();}));
    const toPicker=value=>{const match=String(value||'').match(/^(\d{1,2}):(\d{2})(?:\s*(AM|PM))?$/i);if(!match)return '';let h=Number(match[1]);if(match[3])h=h%12+(/pm/i.test(match[3])?12:0);return h<24?String(h).padStart(2,'0')+':'+match[2]:'';};
    const time = inputField(schedule, 'Time', 'time', toPicker(original.time)); time.placeholder = 'Any time';time.setAttribute('aria-label','Time');
    time.parentElement.append(action('Clear',()=>{time.value='';time.dispatchEvent(new Event('input'));actionHaptic();},'btn tl-clear-time'));
    const durationGroup=node('div','tl-duration-group');durationGroup.append(node('span','tl-duration-caption','Duration'));
    const durationRow=node('div','tl-duration-row');durationGroup.append(durationRow);schedule.append(durationGroup);
    const hours=inputField(durationRow,'Hours','number',original.minutes===undefined?'':Math.floor(original.minutes/60));hours.placeholder='Hours';hours.min='0';hours.step='1';hours.setAttribute('aria-label','Hours');
    const minutes=inputField(durationRow,'Minutes','number',original.minutes===undefined?'':original.minutes%60);minutes.placeholder='Minutes';minutes.min='0';minutes.max='59';minutes.step='1';minutes.setAttribute('aria-label','Minutes');
    const urgentWrap = node('label', 'tl-context-choice tl-urgent'), urgent = node('input'); urgent.type = 'checkbox'; urgent.checked = task ? !!original.urgent : !!(prefill&&prefill.urgent);
    urgentWrap.append(urgent, node('span', '', 'Urgent'));urgentWrap.classList.add('tl-priority-urgent');priority.after(urgentWrap);
    let priorityBeforeUrgent=['HIGH','MEDIUM','LOW'].includes(original.priorityBeforeUrgent)?original.priorityBeforeUrgent:(prefill?.priorityBeforeUrgent||priority.value||'LOW');
    if(urgent.checked)priority.value='HIGH';
    urgent.addEventListener('change',()=>{if(urgent.checked){priorityBeforeUrgent=priority.value||'LOW';priority.value='HIGH';}else priority.value=priorityBeforeUrgent;});
    priority.addEventListener('change',()=>{if(urgent.checked&&priority.value!=='HIGH')urgent.checked=false;if(!urgent.checked)priorityBeforeUrgent=priority.value||'LOW';});
    const flags=node('div','tl-task-flags'),notifyWrap=node('label','tl-context-choice tl-notify'),notify=node('input');notify.type='checkbox';notify.checked=original.notify===true;notify.setAttribute('aria-label','Notify at scheduled time');notifyWrap.append(notify,node('span','','Notify'));flags.append(notifyWrap);schedule.append(flags);
    const syncNotify=()=>{notify.disabled=!time.value;if(!time.value)notify.checked=false;notifyWrap.title=time.value?'Send a reminder at this time':'Set a time to enable notifications';};time.addEventListener('input',syncNotify);time.addEventListener('change',syncNotify);syncNotify();
    const visibility=node('section','tl-visibility-settings'),visibilityLabel=node('label','tl-context-choice'),hideUntil=node('input');hideUntil.type='checkbox';hideUntil.checked=Number.isInteger(original.showBeforeMinutes)&&original.showBeforeMinutes>=0;hideUntil.setAttribute('aria-label','Hide until before due time');visibilityLabel.append(hideUntil,node('span','','Hide until before due time'));visibility.append(visibilityLabel);
    const visibilityFields=node('div','tl-duration-row');const showHours=inputField(visibilityFields,'Hours before','number',Math.floor((original.showBeforeMinutes||0)/60)),showMinutes=inputField(visibilityFields,'Minutes before','number',(original.showBeforeMinutes||0)%60);showHours.min=showMinutes.min='0';showHours.step=showMinutes.step='1';showMinutes.max='59';showHours.setAttribute('aria-label','Hours before');showMinutes.setAttribute('aria-label','Minutes before');visibility.append(visibilityFields,node('p','tl-checklist-help','Set a task time first. 0 hours and 0 minutes shows it when due. In-progress tasks stay visible.'));schedule.append(visibility);
    const syncVisibility=()=>{hideUntil.disabled=!time.value;if(!time.value)hideUntil.checked=false;visibilityFields.hidden=!hideUntil.checked;showHours.disabled=showMinutes.disabled=!hideUntil.checked;};hideUntil.addEventListener('change',syncVisibility);time.addEventListener('input',syncVisibility);time.addEventListener('change',syncVisibility);syncVisibility();
    const {oldSpec,oldMode,recurrence,amount,unit,weekChecks}=frequencyFields(form,original,initialDate,date,true);
    const selected = task ? derivedContexts(task) : (prefill && (prefill.labels || [prefill.label || prefill.widget_label].filter(Boolean))) || (selectedContext ? [selectedContext] : []);
    const readContexts = contextsField(form, selected);
    const readLinks = linkedTasksField(form, task, item?item.date:scope.today);
    const extra = node('section', 'tl-extra-fields');
    const extraGrid = node('div', 'tl-notes-grid'); extra.append(extraGrid); form.append(extra);
    const notes = inputField(extraGrid, 'Notes', 'textarea', original.notes || ''); notes.placeholder = 'Add notes';
    const checklistDate=item?item.stateDate:scope.today;
    const originalSteps=isNew?[]:scope.checklistEntries(original,checklistDate);
    let section='';const checklistLines=[];for(const step of originalSteps){const heading=step.section||'';if(heading!==section){checklistLines.push('['+heading+']');section=heading;}checklistLines.push(step.title);}const initialChecklistText=checklistLines.join('\n');
    const steps = inputField(extraGrid, 'Checklist', 'textarea', initialChecklistText);
    if (steps) steps.placeholder = 'One step per line';
    steps.parentElement.append(node('span','tl-checklist-help','One item per line. Use [Raid] or [Discord] for headings; [] returns to ungrouped items.'));
    enableTextareaResize(notes,'Notes');enableTextareaResize(steps,'Checklist');


    const error = node('p', 'tl-form-error'); error.setAttribute('role', 'alert'); form.append(error);
    const buttons = node('div', 'tl-dialog-actions');
    if(!isNew)buttons.append(deleteControl('task',()=>{try{deleteTaskDefinition(task);close();}catch(e){error.textContent=e.message||'Couldn’t delete this task. Please try again.';}},form,dialog));
    buttons.append(action('Cancel', close), action('Save task', () => {
      if (!title.value.trim()) { error.textContent = 'Enter a task title.'; title.focus(); return; }
      if (!/^\d{4}-\d{2}-\d{2}$/.test(date.value)) { error.textContent = 'Choose a valid date.'; return; }
      if((hours.value&&!/^\d+$/.test(hours.value))||(minutes.value&&!/^\d+$/.test(minutes.value))||Number(minutes.value)>59||((hours.value||minutes.value)&&Number(hours.value)*60+Number(minutes.value)<1)){error.textContent='Enter hours and 0–59 minutes.';return;}
      if (recurrence.value === 'weekdays' && !weekChecks.some(check => check.checked)) { error.textContent = 'Select at least one weekday.'; return; }
      if(recurrence.value==='repeat'&&(!/^\d+$/.test(amount.value)||Number(amount.value)<1)){error.textContent='Repeat amount must be at least 1.';return;}
      const showBefore=Number(showHours.value)*60+Number(showMinutes.value);if(hideUntil.checked&&((showHours.value&&!/^\d+$/.test(showHours.value))||(showMinutes.value&&!/^\d+$/.test(showMinutes.value))||Number(showMinutes.value)>59||!Number.isSafeInteger(showBefore)||showBefore<0||showBefore>2147483647)){error.textContent='Enter valid hours and 0–59 minutes before the due time.';return;}
      const next = Object.assign({}, original, { id: original.id || 'app-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2,9), title: title.value.trim(), priority: urgent.checked ? 'HIGH' : (priority.value || 'LOW'), _contexts: readContexts() });
      if(hours.value||minutes.value)next.minutes=Number(hours.value)*60+Number(minutes.value);else delete next.minutes;
      if(time.value){const [h,m]=time.value.split(':').map(Number);next.time=(h%12||12)+':'+String(m).padStart(2,'0')+(h>=12?' PM':' AM');}else delete next.time;
      if(date.dataset.weekend==='true')next.weekend=true;else if(next.due&&date.value!==original.due)delete next.weekend;
      if (notes.value.trim()) next.notes = notes.value.trim(); else delete next.notes;
      if(hideUntil.checked&&time.value)next.showBeforeMinutes=showBefore;else delete next.showBeforeMinutes;
      delete next.aiHelp;
      if (urgent.checked){next.urgent=true;next.priorityBeforeUrgent=priorityBeforeUrgent;}else{delete next.urgent;delete next.priorityBeforeUrgent;}
      if(notify.checked&&time.value)next.notify=true;else delete next.notify;
      const chosenDays=weekChecks.flatMap((check,index)=>check.checked?[index]:[]);
      const changedSchedule=recurrence.value!==oldMode||(recurrence.value!=='once'&&date.value!==initialDate)||(recurrence.value==='weekdays'&&JSON.stringify(chosenDays)!==JSON.stringify(oldSpec.weekdays||[]))||(recurrence.value==='repeat'&&(Number(amount.value)!==oldSpec.amount||unit.value!==oldSpec.unit||date.value!==initialDate||(unit.value==='week'&&JSON.stringify(chosenDays)!==JSON.stringify(oldSpec.weekdays||[]))));
      if(isNew||changedSchedule){
        ['due','days','intervalDays','intervalStart','weekendCycleStart','recurrence','repeat','startsOn'].forEach(key=>delete next[key]);
        if(recurrence.value==='once')next.due=date.value;
        if(recurrence.value==='daily'){next.repeat={amount:1,unit:'day',start:date.value};next.recurrence='Daily';}
        if(recurrence.value==='weekends'){next.days=[0,6];next.startsOn=date.value;next.recurrence='Weekends';}
        if(recurrence.value==='weekdays'){next.days=chosenDays;next.startsOn=date.value;next.recurrence=global.TasksSchedule.weekdaysLabel(chosenDays);}
        if(recurrence.value==='repeat'){next.repeat={amount:Number(amount.value),unit:unit.value,start:date.value};if(unit.value==='week'&&chosenDays.length)next.repeat.weekdays=chosenDays;next.recurrence=global.TasksSchedule.describe(next);}
      }else if(item&&recurrence.value==='once'&&date.value!==initialDate){
        scope.moveOccurrence(task,item.stateDate,date.value);
      }
      if(!isNew&&changedSchedule&&(oldMode!=='once'||recurrence.value!=='once')){next.recurrenceResetOn=scope.today;next.reminderScheduleVersion=Date.now().toString(36)+'-'+Math.random().toString(36).slice(2);}
      let currentSection='';const entries=[];for(const raw of steps.value.split('\n')){const title=raw.trim();if(!title)continue;const heading=title.match(/^\[([^\[\]]*)\]$/);if(heading){currentSection=heading[1].trim();continue;}entries.push({...currentSection?{section:currentSection}:{},title});}const names=entries.map(step=>step.title);
      if(isNew){if(names.length){next.steps=names;if(entries.some(step=>step.section))next.checklistRevisions=[{from:date.value,items:entries.map((step,index)=>({...step,key:':step:'+index}))}];}}
      else if(steps.value!==initialChecklistText&&JSON.stringify(entries.map(step=>[step.title,step.section||'']))!==JSON.stringify(originalSteps.map(step=>[step.title,step.section||'']))){
        const used=new Set(),items=entries.map(entry=>{let index=originalSteps.findIndex((step,i)=>!used.has(i)&&step.title===entry.title&&(step.section||'')===(entry.section||''));if(index<0)index=originalSteps.findIndex((step,i)=>!used.has(i)&&step.title===entry.title);if(index<0)return{...entry,key:null};used.add(index);return{...entry,key:originalSteps[index].key};});
        items.forEach((step,index)=>{if(step.key)return;const old=originalSteps[index];if(old&&!used.has(index)){used.add(index);step.key=old.key;}else step.key=':step:added-'+Date.now().toString(36)+'-'+Math.random().toString(36).slice(2,9);});
        next.checklistRevisions=[...(original.checklistRevisions||[]).filter(revision=>revision.from!==checklistDate),{from:checklistDate,items}].sort((a,b)=>a.from.localeCompare(b.from));
        const current=Object.assign({},readCanonical(),scope.getState()),prefix=original.id+':';let stateChanged=false;
        if(!items.length){
          current.completed={...current.completed};current.completedOn={...current.completedOn};
          const dates=new Set([checklistDate,...Object.keys(current.completed).filter(key=>key.startsWith(prefix)).map(key=>key.slice(prefix.length,prefix.length+10)).filter(day=>/^\d{4}-\d{2}-\d{2}$/.test(day)&&day>=checklistDate)]);
          dates.forEach(day=>{if(!scope.hasSubtasks(original,day))return;const key=scope.occurrenceKey(original,day),done=scope.isDone(original,day,current.completed);current.completed[key]=done;const finished=Array.from({length:scope.subtaskCount(original,day)},(_,i)=>current.completedOn[scope.childKey(original,day,i)]).filter(Boolean).sort().pop();if(done&&finished)current.completedOn[key]=finished;else if(!done)delete current.completedOn[key];stateChanged=true;});
        }
        const active=current.inProgressTasks||[current.inProgress].filter(Boolean),retained=active.filter(key=>{if(!key.startsWith(prefix)||key.slice(prefix.length,prefix.length+10)<checklistDate)return true;const suffix=key.slice(prefix.length+10);return !suffix||items.some(step=>step.key===suffix);});if(retained.length!==active.length){current.inProgressTasks=retained;current.inProgress=retained.at(-1)||null;stateChanged=true;}
        if(stateChanged)persist(current);

      }
      try{saveDefinition(next,isNew,readLinks());close();}catch(e){error.textContent=e.message||"Couldn’t save. Please try again.";}
    }, 'btn btn-primary'));
    const fields=node('div','tl-task-editor-fields');Array.from(form.children).forEach(child=>{if(child!==error)fields.append(child);});
    form.append(fields,error,buttons);dialog.classList.add('tl-task-editor');dialog.focus({preventScroll:true});try{if(native&&native.hideKeyboard)native.hideKeyboard();}catch(_){}
  }
  function updateContextOptions() {
    const select = document.getElementById('tl-context-filter'); if (!select) return;
    const wanted = ['', ...allContexts()]; const existing = Array.from(select.options).map(o => o.value);
    if (JSON.stringify(wanted) !== JSON.stringify(existing)) {
      select.replaceChildren(); wanted.forEach(label => { const option = node('option', '', label?(contextIcon(label)===GUILD_ICON_TOKEN||contextSymbol(label)?label:contextIcon(label)):'All contexts'); option.value = label; select.append(option); });
    }
    select.value = selectedContext;
    const preview=document.getElementById('tl-context-filter-preview');if(preview){const image=selectedContext&&(contextIcon(selectedContext)===GUILD_ICON_TOKEN||contextSymbol(selectedContext));preview.replaceChildren();preview.hidden=!image;select.classList.toggle('tl-image-filter',!!image);if(image)preview.append(contextGlyph(selectedContext));}
  }
  function changePriority(task,offset) {
    const levels=scope.groups.map(([key])=>key);const found=levels.indexOf(task.priority||'LOW');const current=found<0?levels.length-1:found;const target=Math.max(0,Math.min(levels.length-1,current+offset));if(target===current)return false;
    const next={...clone(task),priority:levels[target]};if(next.priority!=='HIGH'){delete next.urgent;delete next.priorityBeforeUrgent;}
    const status=document.getElementById('pt-status');
    try{saveDefinition(next,false,undefined,true);if(status)status.textContent='';return true;}
    catch(error){cached=null;lastCanonical=null;const state=readCanonical();scope.setState(state);applyDefinitions(state);rebuild();if(status)status.textContent='Couldn’t change priority. Please try again.';return false;}
  }
  let priorityHoldActive=false,priorityHoldUntil=0,priorityHoldCancel=null;
  const releasePriorityHold=()=>{if(priorityHoldCancel)priorityHoldCancel();if(priorityHoldActive){priorityHoldActive=false;priorityHoldUntil=performance.now()+450;}};
  document.addEventListener('pointerup',releasePriorityHold,true);document.addEventListener('pointercancel',releasePriorityHold,true);global.addEventListener('blur',releasePriorityHold);
  document.addEventListener('visibilitychange',()=>{if(document.hidden)releasePriorityHold();});
  document.addEventListener('click',event=>{if(event.target.closest?.('.tl-priority-button')&&(priorityHoldActive||performance.now()<priorityHoldUntil)){event.preventDefault();event.stopImmediatePropagation();}},true);
  function priorityButton(task,direction,offset,label,rank,levels){
    let timer=null,start=null,held=false,cancelled=false,pressed=false;
    const clear=()=>{if(timer!==null){clearTimeout(timer);timer=null;}if(priorityHoldCancel===clear)priorityHoldCancel=null;};
    const button=action('',event=>{if(held||cancelled||priorityHoldActive||performance.now()<priorityHoldUntil){event.preventDefault();return;}clear();changePriority(task,offset);},'tm-move-control tl-priority-button tl-priority-'+direction);
    const arrow=node('span','tl-priority-arrow','➜');arrow.setAttribute('aria-hidden','true');button.append(arrow);button.setAttribute('aria-label',label+' priority for '+task.title);button.title=label+' priority · Hold for '+(offset<0?'High':'Low');button.disabled=direction==='up'?rank===0:rank>=levels.indexOf('LOW');
    button.addEventListener('pointerdown',event=>{
      if(button.disabled||event.button!==0||event.isPrimary===false)return;priorityHoldUntil=0;priorityHoldActive=false;
      if(priorityHoldCancel)priorityHoldCancel();held=false;cancelled=false;pressed=true;start={x:event.clientX,y:event.clientY};priorityHoldCancel=clear;
      timer=setTimeout(()=>{timer=null;if(!button.isConnected||button.disabled)return;held=true;priorityHoldActive=true;priorityHoldUntil=performance.now()+450;
        if(changePriority(task,(offset<0?0:levels.indexOf('LOW'))-rank)){try{if(native&&native.priorityHaptic)native.priorityHaptic();}catch(_){}}
      },450);
    },{passive:true});
    button.addEventListener('pointermove',event=>{if(start&&Math.hypot(event.clientX-start.x,event.clientY-start.y)>10){cancelled=true;clear();}},{passive:true});
    button.addEventListener('pointerleave',()=>{if(pressed){cancelled=true;clear();}});button.addEventListener('pointercancel',()=>{pressed=false;cancelled=true;clear();});button.addEventListener('pointerup',()=>{pressed=false;clear();});
    button.addEventListener('contextmenu',event=>event.preventDefault());return button;
  }
  let progressLayoutFrame=0;
  const progressRows=new Set(),progressWidths=new WeakMap();
  const progressResize=global.ResizeObserver?new ResizeObserver(entries=>{let changed=false;for(const entry of entries){const size=entry.contentRect.width+':'+entry.contentRect.height;if(progressWidths.get(entry.target)!==size){progressWidths.set(entry.target,size);changed=true;}}if(changed)queueProgressLayout();}):null;
  function queueProgressLayout(){if(progressLayoutFrame)return;progressLayoutFrame=requestAnimationFrame(()=>{progressLayoutFrame=0;
    for(const form of progressRows)if(!form.isConnected){progressResize?.unobserve(form);progressRows.delete(form);}
    document.querySelectorAll('.tm-task-item').forEach(row=>{
      const form=row.querySelector(':scope>.form-check'),home=row.querySelector(':scope>.tm-task-details>.tm-task-badges');if(!form||!home)return;
      const badges=['tm-urgent-badge','tm-in-progress-badge'].map(name=>form.querySelector(':scope>.'+name)||home.querySelector(':scope>.'+name)).filter(Boolean);if(!badges.length)return;
      if(!progressRows.has(form)){progressRows.add(form);progressResize?.observe(form);}home.prepend(...badges);form.classList.remove('tm-inline-progress');
      const label=form.querySelector(':scope>.form-check-label'),title=label?.querySelector('.tm-title');if(!title||!form.clientWidth)return;
      const range=document.createRange();range.selectNodeContents(title);const lines=Array.from(range.getClientRects());let used=range.getBoundingClientRect().width;const available=label.getBoundingClientRect().width;
      for(const badge of badges){const width=badge.getBoundingClientRect().width;if(lines.length===1&&used+width+4<=available){used+=width+4;form.classList.add('tm-inline-progress');form.append(badge);}}
    });
  });}
  document.addEventListener('toggle',queueProgressLayout,true);
  global.addEventListener('resize',queueProgressLayout);if(document.fonts){document.fonts.ready.then(queueProgressLayout);document.fonts.addEventListener?.('loadingdone',queueProgressLayout);}
  function decorateRow(wrapper, task, item, completed) {
    wrapper.dataset.taskId = task.id; wrapper.dataset.stateDate = item.stateDate; wrapper.dataset.displayDate = item.date;
    const text = wrapper.querySelector('.tm-task-details') || wrapper.querySelector('.form-check-label') || wrapper.querySelector('.tm-future-content');
    const labels = derivedContexts(task);
    if (text && labels.length) { const badges = node('span', 'tl-context-badges tl-meta-group'); labels.forEach(label => {const icon=action('',()=>editComponent(task,item,'labels'),'tl-context-icon tl-label-edit');icon.append(contextGlyph(label));icon.setAttribute('aria-label',label);icon.setAttribute('aria-haspopup','dialog');icon.title='Edit labels · '+label;badges.append(icon);}); const separator=node('span','tl-label-separator','·');separator.setAttribute('aria-hidden','true');badges.prepend(separator);(text.querySelector('.tm-meta')||text).append(badges); }
    const meta=text?.querySelector('.tm-meta'),priority={HIGH:'High',MEDIUM:'Med',LOW:'Low'}[task.priority];if(meta&&priority){const value=node('span','tl-meta-priority',priority);value.setAttribute('aria-label','Priority '+priority);const group=node('span','tl-meta-group');const separator=node('span','tl-meta-separator','·');separator.setAttribute('aria-hidden','true');group.append(separator,value);meta.append(group);}
    const controls = wrapper.querySelector('.tm-item-actions');
    if (controls) {
      const edit = action('⚙', () => editTask(task, item), 'tm-move-control tl-edit-button');
      edit.setAttribute('aria-label', 'Edit ' + task.title); edit.title = 'Edit task';
      const tools=node('span','tl-priority-tools'),levels=scope.groups.map(([key])=>key),rank=levels.indexOf(task.priority||'LOW');
      [['up',-1,'Raise'],['down',1,'Lower']].forEach(([direction,offset,label])=>{
        tools.append(priorityButton(task,direction,offset,label,rank,levels));
      });tools.append(edit);controls.append(tools);
    }
    if(completed){const error=node('p','tl-form-error');error.setAttribute('role','alert');const button=permanentDeleteButton(task,item,wrapper,error);wrapper.querySelector(':scope>.form-check')?.append(button);wrapper.append(error);}
    queueSnapshot();queueProgressLayout();
  }
  function queueSnapshot() {
    if (!scope || snapshotTimer) return;
    snapshotTimer = setTimeout(() => { snapshotTimer = 0; publishSnapshot(); }, 0);
  }
  function publishSnapshot() {
    if (!scope) return;
    const rows = new Map(); const state=scope.getState(),active=state.inProgressTasks||[state.inProgress].filter(Boolean);
    const push = (item, onToday) => {
      const key = scope.progressKey(item.task, item.stateDate); if (rows.has(key)) return;
      const done = scope.isDone(item.task, item.stateDate); const task = item.task;
      const completionKeys = scope.hasSubtasks(task,item.stateDate) ? Array.from({ length: scope.subtaskCount(task, item.stateDate) }, (_, i) => scope.childKey(task, item.stateDate, i)) : [scope.occurrenceKey(task, item.stateDate)];
      rows.set(key, {
        id: 'html:' + key, title: task.title, listName: 'My tasks',
        dueDate: task.due || '', plannedDate: item.date || scope.today,
        done, status: done ? 'Done' : active.some(value=>value===key||completionKeys.includes(value)) ? 'In progress' : 'Next',
        showBeforeMinutes:task.showBeforeMinutes,visibilityDate:state.movedTo?.[key]||item.stateDate,
        time:task.time||'', urgent:!!task.urgent, labels: derivedContexts(task), priority: task.priority || 'LOW', minutes: Number(task.minutes) || 0,
        createdAt: Date.parse(item.stateDate + 'T12:00:00Z') || 0,
        completionKeys, progressKey: key, stateDate: item.stateDate, onToday: !!onToday,
        weekendOpening:global.TasksSchedule.weekendOpening(task,item.stateDate),recurrence: task.recurrence || '', source: 'html', snapshotDate:scope.today, recurringDaily:task.repeat?(task.repeat.unit==='day'&&Number(task.repeat.amount)===1&&!task.repeat.weekdays?.length):(task.recurrence==='Daily'||!!(task.days&&task.days.length===7)), skipCarryDays:task.skipCarryDays||[]
      });
    };
    scope.dueToday().forEach(item => push(item, true)); scope.overdue().forEach(item => push(item, false));
    for (let day = 1; day <= 14; day++) scope.onDate(scope.addDays(scope.today, day)).forEach(item => push(item, false));
    const snapshot = Array.from(rows.values()); global.TaskListBridge.lastSnapshot = snapshot;
    if(native&&native.updateReminders){try{native.updateReminders(JSON.stringify(scope.tasks));}catch(error){console.error('Could not update task reminders',error);}}
    if (native && native.updateSnapshot) { try { native.updateSnapshot(JSON.stringify(snapshot)); } catch (error) { console.error('Could not update the home widget', error); } }
  }
  function applyTheme(){return global.TasksTheme.apply(readCanonical(),native);}
  function quickAddTask(prefill = {}) {
    const { dialog, form, close } = modal('Quick add'); form.classList.add('tl-quick-form');
    dialog.classList.add('tl-app-quick-add');const heading=dialog.querySelector('h2'),header=node('div','tl-quick-heading'),urgentWrap=node('label','tl-context-choice tl-quick-urgent'),urgent=node('input');urgent.type='checkbox';urgent.checked=!!prefill.urgent;urgent.setAttribute('aria-label','Urgent');urgentWrap.append(urgent,node('span','','Urgent'));heading.replaceWith(header);header.append(heading,urgentWrap);
    const title = taskTitleField(form, prefill.title || '');title.setAttribute('enterkeyhint','done');title.parentElement.querySelector('span')?.remove();
    const error = node('p', 'tl-form-error'); error.setAttribute('role', 'alert'); form.append(error);
    let labels = prefill.labels || (selectedContext ? [selectedContext] : []);let flash=null;
    const savedFlash=()=>{if(flash)flash.cancel();if(!dialog.animate)return;const shadow=getComputedStyle(dialog).boxShadow,base=shadow==='none'?'':shadow+', ',reduced=global.matchMedia('(prefers-reduced-motion: reduce)').matches;const widths=reduced?[1,0]:[0,1,3,1,3,0];flash=dialog.animate(widths.map(width=>({boxShadow:base+'inset 0 0 0 '+width+'px rgba(255,255,255,'+(width?1:0)+')'})),{duration:reduced?180:800,easing:'linear'});flash.finished.catch(()=>{});};
    const save = () => {
      if (!title.value.trim()) { error.textContent = 'Enter a task title.'; title.focus(); return; }
      const next = { id:'app-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2,9),
        title:title.value.trim(), priority:urgent.checked?'HIGH':'LOW', due:prefill.dueDate || scope.today, _contexts:labels };if(urgent.checked){next.urgent=true;next.priorityBeforeUrgent=prefill.priorityBeforeUrgent||'LOW';}
      try{saveDefinition(next,true);title.value='';title.dispatchEvent(new Event('input',{bubbles:true}));urgent.checked=false;labels=[];prefill={};error.textContent='';title.focus();savedFlash();}catch(e){if(flash)flash.cancel();cached=null;lastCanonical=null;const state=readCanonical();scope.setState(state);applyDefinitions(state);rebuild();error.textContent='Couldn’t add this task. Please try again.';title.focus();}
    };
    const actions = node('div', 'tl-dialog-actions');
    actions.append(action('Cancel', close), action('Details', () => {
      const value = title.value; close(); editTask(null, null, { ...prefill, title:value, labels, urgent:urgent.checked,priorityBeforeUrgent:prefill.priorityBeforeUrgent||'LOW' });
    }), action('Add', save, 'btn btn-primary'));
    form.append(actions); form.addEventListener('submit', event => { event.preventDefault(); save(); });title.addEventListener('beforeinput',event=>{if((event.inputType==='insertLineBreak'||event.inputType==='insertParagraph')&&!event.isComposing){event.preventDefault();save();}});title.addEventListener('keydown',event=>{if(event.key==='Enter'&&!event.isComposing){event.preventDefault();save();}});title.focus();
  }
  function handleBack() {
    const dialogs = Array.from(document.querySelectorAll('dialog[open]'));
    const active = dialogs[dialogs.length - 1]; if (!active){if(settingsScreen){settingsScreen.close();return true;}return false;}
    active.dispatchEvent(new Event('cancel', { cancelable:true }));
    if (active.isConnected) { if (active.close) active.close(); active.remove(); }
    return true;
  }
  function scheduleIcon(kind){
    const mode=kind==='notification'?'symbols':readCanonical().metadataIcons||'emoji',span=node('span',mode==='symbols'?'tl-schedule-symbol':'tl-schedule-emoji');span.setAttribute('aria-hidden','true');if(mode==='off')return document.createDocumentFragment();
    if(mode==='emoji'){span.textContent=kind==='duration'?'⏰':kind==='linked'?'🔗':'🔁';return span;}
    const ns='http://www.w3.org/2000/svg',svg=document.createElementNS(ns,'svg');svg.setAttribute('viewBox','0 0 24 24');svg.setAttribute('fill','none');svg.setAttribute('stroke','currentColor');svg.setAttribute('stroke-width','2');svg.setAttribute('stroke-linecap','round');svg.setAttribute('stroke-linejoin','round');
    const path=document.createElementNS(ns,'path');
    if(kind==='notification'){path.setAttribute('d','M12 1a1 1 0 0 0-1 1v1.08A6 6 0 0 0 6 9v3.5c0 1.36-.48 2.68-1.35 3.73l-.42.5A.8.8 0 0 0 4.85 18h14.3a.8.8 0 0 0 .62-1.27l-.42-.5A5.86 5.86 0 0 1 18 12.5V9a6 6 0 0 0-5-5.92V2a1 1 0 0 0-1-1ZM9.2 20a2.8 2.8 0 0 0 5.6 0Z');path.setAttribute('fill','currentColor');path.setAttribute('stroke','none');path.setAttribute('transform','translate(12 12) scale(.94) translate(-12 -12)');svg.append(path);
    }else if(kind==='recurrence'){
      path.setAttribute('d','M4.25 10A8 8 0 0 1 18 6.7M19.75 14A8 8 0 0 1 6 17.3');
      const heads=document.createElementNS(ns,'path');heads.setAttribute('d','M21.212 9.665Q21.300 10.460 20.523 10.271L16.152 9.212Q15.375 9.023 15.975 8.494L20.025 4.923Q20.625 4.394 20.713 5.189ZM2.788 14.335Q2.700 13.540 3.477 13.729L7.848 14.788Q8.625 14.977 8.025 15.506L3.975 19.077Q3.375 19.606 3.287 18.811Z');heads.setAttribute('fill','currentColor');heads.setAttribute('stroke','none');svg.append(path,heads);
    }else{
      path.setAttribute('d',kind==='linked'?'M10 13a5 5 0 0 0 7.54 .54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71':'M12 4a8 8 0 1 0 0 16a8 8 0 0 0 0-16M12 8.5V12h3.5');if(kind==='linked'){path.setAttribute('transform','translate(12 12) scale(.9) translate(-12 -12)');path.setAttribute('stroke-width','2.5');}svg.append(path);
    }
    span.append(svg);return span;
  }
  function showChangelog(){
    const {dialog,form,close}=modal('Changelog');form.classList.add('tl-changelog-form');
    form.append(node('p','tl-changelog-intro','Tasks '+APP_CHANGELOG[0].version+' · Update history'));
    APP_CHANGELOG.forEach((release,index)=>{const entry=node('details','tl-release-note');entry.dataset.version=release.version;entry.open=index===0;const date=new Date(release.date+'T12:00:00Z').toLocaleDateString('en-US',{year:'numeric',month:'short',day:'numeric',timeZone:'UTC'});entry.append(node('summary','',release.version+' · '+date));const changes=node('ul');release.changes.forEach(change=>changes.append(node('li','',change)));entry.append(changes);form.append(entry);});
    const actions=node('div','tl-dialog-actions');actions.append(action('Close',close));form.append(actions);dialog.focus({preventScroll:true});
  }
  function settingsPage(){
    const main=document.querySelector('body>main'),previousHidden=main?.hidden,position={x:scrollX,y:scrollY},screen=node('section','tl-dialog tl-settings-screen'),header=node('header','tl-settings-heading'),heading=node('h2','','Settings'),form=node('form');heading.id='tl-settings-title';screen.setAttribute('role','main');screen.setAttribute('aria-labelledby',heading.id);screen.tabIndex=-1;form.addEventListener('submit',event=>event.preventDefault());
    const close=()=>{if(!screen.isConnected)return;screen.remove();settingsScreen=null;document.body.classList.remove('tl-settings-open');if(main)main.hidden=previousHidden;document.removeEventListener('keydown',escape);global.scrollTo(position.x,position.y);try{if(native&&native.hideKeyboard)native.hideKeyboard();}catch(_){}};
    const escape=event=>{if(event.key==='Escape'&&!document.querySelector('dialog[open]')){event.preventDefault();close();}};
    const back=action('',close,'btn tl-settings-back');back.setAttribute('aria-label','Back to tasks');back.title='Back to tasks';const ns='http://www.w3.org/2000/svg',icon=document.createElementNS(ns,'svg'),path=document.createElementNS(ns,'path');icon.setAttribute('viewBox','0 0 24 24');icon.setAttribute('aria-hidden','true');path.setAttribute('d','M19 12H5M12 5l-7 7 7 7');path.setAttribute('fill','none');path.setAttribute('stroke','currentColor');path.setAttribute('stroke-width','2.5');path.setAttribute('stroke-linecap','round');path.setAttribute('stroke-linejoin','round');icon.append(path);back.append(icon);header.append(back,heading);screen.append(header,form);
    if(main)main.hidden=true;document.body.classList.add('tl-settings-open');document.body.append(screen);settingsScreen={element:screen,close};document.addEventListener('keydown',escape);global.scrollTo(0,0);screen.focus({preventScroll:true});return{dialog:screen,form,close};
  }
  function showSettings() {
    if(settingsScreen){settingsScreen.element.focus({preventScroll:true});return;}
    const { dialog, form, close } = settingsPage();dialog.classList.add('tl-settings-dialog');form.classList.add('tl-settings-form');
    const reminders=node('section','tl-notification-settings');reminders.append(node('h3','','Notifications'));
    const reminderSettings=readCanonical().reminders||{};
    const dailyLabel=node('label','tl-context-choice'),daily=node('input');daily.type='checkbox';daily.checked=reminderSettings.daily!==false;daily.setAttribute('aria-label','Daily reminder');dailyLabel.append(daily,node('span','','Daily reminder'));reminders.append(dailyLabel);
    const weekendTime=inputField(reminders,'Weekend starts Friday','time',readCanonical().weekendStartTime||'18:00');weekendTime.setAttribute('aria-label','Weekend starts Friday');
    const dailyTime=inputField(reminders,'Daily reminder time','time',reminderSettings.dailyTime||'09:00');dailyTime.setAttribute('aria-label','Daily reminder time');
    const dailyTitle=inputField(reminders,'Daily reminder title','text',reminderSettings.dailyTitle??'Your daily task reminder');dailyTitle.setAttribute('aria-label','Daily reminder title');dailyTitle.maxLength=200;
    const dailyBody=inputField(reminders,'Daily reminder message','text',reminderSettings.dailyBody??"Open Tasks to review today's list.");dailyBody.setAttribute('aria-label','Daily reminder message');dailyBody.maxLength=1000;
    const timedLabel=node('label','tl-context-choice'),timed=node('input');timed.type='checkbox';timed.checked=reminderSettings.scheduled!==false;timed.setAttribute('aria-label','Scheduled task reminders');timedLabel.append(timed,node('span','','Scheduled task reminders'));reminders.append(timedLabel);
    const notificationTitle=inputField(reminders,'Task notification title','text',reminderSettings.taskTitle??'{task}');notificationTitle.setAttribute('aria-label','Task notification title');notificationTitle.maxLength=200;
    const notificationBody=inputField(reminders,'Task notification message','text',reminderSettings.taskBody??"It's time for this task.");notificationBody.setAttribute('aria-label','Task notification message');notificationBody.maxLength=1000;
    const progressTitle=inputField(reminders,'In-progress notification title','text',reminderSettings.progressTitle??'In progress');progressTitle.setAttribute('aria-label','In-progress notification title');progressTitle.maxLength=200;
    const progressBody=inputField(reminders,'In-progress notification message','text',reminderSettings.progressBody??'{tasks}');progressBody.setAttribute('aria-label','In-progress notification message');progressBody.maxLength=1000;
    reminders.append(node('p','text-muted','The persistent notification updates with your active tasks. Use {tasks} for their names and {count} for their number.'));
    const snooze=inputField(reminders,'Task snooze (minutes)','number',reminderSettings.snoozeMinutes??10);snooze.min='1';snooze.max='10080';snooze.step='1';snooze.setAttribute('aria-label','Task snooze (minutes)');
    reminders.append(node('p','text-muted','Applies to all timed task reminders. Use {task}, {time}, and {date} to include task details. Daily reminders stay separate.'));
    reminders.append(node('p','text-muted','Only tasks with a set time and Notify checked send scheduled reminders. Allow precise reminders for delivery at the scheduled time.'));
    if(native&&native.exactAlarmStatus)reminders.append(node('p','text-muted',native.exactAlarmStatus()));
    if(native&&native.exactAlarmsNeedPermission&&native.exactAlarmsNeedPermission())reminders.append(action('Allow precise reminders',()=>native.openExactAlarmSettings()));
    const reminderMessage=node('p','text-muted');if(native&&native.notificationStatus)reminderMessage.textContent=native.notificationStatus();
    reminders.append(action('Save reminders',()=>{if(!/^\d+$/.test(snooze.value)||Number(snooze.value)<1||Number(snooze.value)>10080){reminderMessage.textContent='Choose a snooze duration from 1 to 10080 minutes.';return;}if(daily.checked&&!dailyTime.value){reminderMessage.textContent='Choose a daily reminder time.';return;}persist({weekendStartTime:weekendTime.value||'18:00',reminders:{daily:daily.checked,dailyTime:dailyTime.value||'09:00',dailyTitle:dailyTitle.value,dailyBody:dailyBody.value,scheduled:timed.checked,taskTitle:notificationTitle.value,taskBody:notificationBody.value,progressTitle:progressTitle.value,progressBody:progressBody.value,snoozeMinutes:Number(snooze.value)}});queueSnapshot();reminderMessage.textContent='Reminder settings saved';if((daily.checked||timed.checked)&&native&&native.requestNotificationPermission)native.requestNotificationPermission();}));
    if(native&&native.openNotificationSettings)reminders.append(action('Android notification settings',()=>native.openNotificationSettings()));reminders.append(reminderMessage);form.append(reminders);

    global.TasksTheme.renderSettings(form,{readState:readCanonical,persistState:persist,native});
    const iconLabel=node('label','tl-field');iconLabel.append(node('span','','Task detail icons'));const icons=node('select','form-control');icons.setAttribute('aria-label','Task detail icons');for(const [value,label]of [['emoji','Emojis'],['symbols','Simple symbols'],['off','Off']]){const option=node('option','',label);option.value=value;icons.append(option);}icons.value=readCanonical().metadataIcons||'emoji';icons.addEventListener('change',()=>{persist({metadataIcons:icons.value});rebuild();});iconLabel.append(icons);form.append(iconLabel);
    const contexts=node('section','tl-context-settings');contexts.append(node('h3','','Contexts'));
    const list=node('p','text-muted');const refresh=()=>{list.replaceChildren();allContexts().forEach((label,index)=>{if(index)list.append(document.createTextNode(' · '));const item=node('span','tl-context-summary-item');item.append(contextGlyph(label),node('span','',label));list.append(item);});};refresh();contexts.append(list);
    const custom=inputField(contexts,'New context','text','');custom.placeholder='Context name';custom.setAttribute('aria-label','New context');
    const emoji=inputField(contexts,'Emoji','text','🏷️');emoji.placeholder='Choose an emoji';emoji.setAttribute('aria-label','Label emoji');emoji.classList.add('tl-emoji-input');
    let selectedImage=false;
    const emojiChoices=node('div','tl-emoji-choices');emojiChoices.setAttribute('aria-label','Choose a label emoji');
    const emojiButtons=['🏷️','🏠','🚗','📱','💻','🌳','⚔️','🎮','🛡️','👑','🏰','🧰','💼','📚','💪','🐾','🛒','🧹','🍽️','📝','📞','❤️','⭐','🎯'].map(value=>{const pick=action(value,()=>{selectedImage=false;emoji.value=value;refreshEmoji();},'btn tl-emoji-choice');pick.setAttribute('aria-label','Choose emoji '+value);pick.dataset.emoji=value;emojiChoices.append(pick);return pick;});
    const guildChoice=action('',()=>{selectedImage=true;emoji.value='';refreshEmoji();},'btn tl-emoji-choice');const guildImage=node('img','tl-context-image');guildImage.src=GUILD_ICON_DATA;guildImage.alt='Guild';guildImage.width=64;guildImage.height=64;guildChoice.append(guildImage);guildChoice.setAttribute('aria-label','Choose Guild image');guildChoice.dataset.emoji=GUILD_ICON_TOKEN;emojiChoices.append(guildChoice);
    const refreshEmoji=()=>{const selected=selectedImage?GUILD_ICON_TOKEN:emoji.value.trim();[...emojiButtons,guildChoice].forEach(pick=>pick.setAttribute('aria-pressed',String(pick.dataset.emoji===selected)));emoji.style.backgroundImage=selectedImage?'url('+GUILD_ICON_DATA+')':'';emoji.style.paddingLeft=selectedImage?'38px':'';emoji.placeholder=selectedImage?'Guild':'Choose an emoji';};emoji.addEventListener('input',()=>{selectedImage=false;refreshEmoji();});refreshEmoji();contexts.append(emojiChoices);
    contexts.append(node('p','text-muted','Choose an emoji above or enter one from your keyboard. Enter an existing label name to change its emoji.'));
    const contextError=node('p','tl-context-error');contextError.setAttribute('role','alert');contexts.append(contextError);
    const validEmoji=value=>{if(!value||value.length>64||!/[\p{Extended_Pictographic}\p{Regional_Indicator}\u20e3]/u.test(value))return false;if(global.Intl&&Intl.Segmenter)return Array.from(new Intl.Segmenter(undefined,{granularity:'grapheme'}).segment(value)).length===1;return !/\s/.test(value);};
    contexts.append(action('Add context',()=>{const labels=custom.value.split(',').map(x=>x.trim()).filter(Boolean);if(!labels.length){contextError.textContent='Enter a label name.';return;}const icon=selectedImage?GUILD_ICON_TOKEN:emoji.value.trim();if(icon!==GUILD_ICON_TOKEN&&!validEmoji(icon)){contextError.textContent='Choose one emoji for this label.';return;}const icons={...(readCanonical().contextIcons||{})};labels.forEach(label=>{Object.defineProperty(icons,label,{value:icon,enumerable:true,writable:true,configurable:true});});persist({contextOptions:[...new Set([...(readCanonical().contextOptions||[]),...labels])],contextIcons:icons});custom.value='';contextError.textContent='';refresh();rebuild();}));form.append(contexts);

    if (native && native.showWidgetSettings) {
      form.append(action('Add home-screen widget', () => { close(); native.showWidgetSettings(); }, 'btn tl-widget-settings'));
    }
    form.append(action('Changelog',showChangelog,'btn tl-show-changelog'));
  }
  let projectPanel=null,projectsCompletedOpen=false;
  function projectMap(state=readCanonical()){
    const value=state.projects;if(value===undefined)return{};
    if(!value||typeof value!=='object'||Array.isArray(value))throw Error('Couldn’t read saved projects. Nothing has been replaced.');
    for(const [id,project]of Object.entries(value))if(!project||typeof project!=='object'||project.id!==id||typeof project.title!=='string')throw Error('Couldn’t read a saved project. Nothing has been replaced.');
    return value;
  }
  function projectDeletions(state=readCanonical()){
    const value=state.projectDeleted;if(value===undefined)return{};if(!value||typeof value!=='object'||Array.isArray(value))throw Error('Couldn’t read project history.');return value;
  }
  function freshProjectState(){
    if(!native||!native.readState)return readCanonical();
    const value=JSON.parse(native.readState());if(!value||typeof value!=='object'||Array.isArray(value))throw Error('Couldn’t read saved projects.');return value;
  }
  function projectDuration(value){const hours=Math.floor(value/60),minutes=value%60;return[hours?hours+' hr':'',minutes?minutes+' min':''].filter(Boolean).join(' ');}
  function plusButton(fn,label,className='btn'){
    const button=action('',event=>{actionHaptic();fn(event);},className),glyph=node('span','tl-plus-glyph');glyph.setAttribute('aria-hidden','true');button.append(glyph);button.setAttribute('aria-label',label);button.title=label;return button;
  }
  function saveProjectRecord(record,isNew,mode){
    const latest=freshProjectState(),fresh=projectMap(latest),removed=projectDeletions(latest);
    if(removed[record.id]||(!isNew&&!fresh[record.id]))throw Error('This project was deleted. Reopen Projects to refresh.');
    if(isNew&&fresh[record.id])throw Error('A project with this identity already exists. Please reopen the editor.');
    const projects={...projectMap()},stamp=new Date().toISOString();
    record.updatedAt=stamp;
    if(mode==='delete'){
      delete projects[record.id];persist({projects,projectDeleted:{...projectDeletions(),[record.id]:stamp}});
    }else{
      if(mode==='complete'){record.completed=true;record.completedAt=stamp;}
      if(mode==='reopen'){record.completed=false;delete record.completedAt;}
      if(mode==='task'){
        if(fresh[record.id]?.convertedTaskId)throw Error('This project already has a task.');
        const task={id:'app-'+Date.now().toString(36)+'-'+Math.random().toString(36).slice(2,9),title:record.title,priority:'LOW',due:record.date&&record.date>scope.today?record.date:scope.today,_contexts:[]};
        if(record.notes)task.notes=record.notes;if(record.minutes!==undefined)task.minutes=record.minutes;
        record.completed=true;record.completedAt=stamp;record.convertedTaskId=task.id;projects[record.id]=record;
        persist({projects,customTasks:[...(readCanonical().customTasks||[]),task]});
      }else{projects[record.id]=record;persist({projects});}
    }
    rebuild();if(isNew||mode==='task')actionHaptic();
  }
  function projectTrashIcon(){
    const ns='http://www.w3.org/2000/svg',icon=document.createElementNS(ns,'svg'),path=document.createElementNS(ns,'path');icon.setAttribute('viewBox','0 0 24 24');icon.setAttribute('width','18');icon.setAttribute('height','18');icon.setAttribute('fill','none');icon.setAttribute('stroke','currentColor');icon.setAttribute('stroke-width','1.8');icon.setAttribute('stroke-linecap','round');icon.setAttribute('stroke-linejoin','round');icon.setAttribute('aria-hidden','true');icon.setAttribute('focusable','false');path.setAttribute('d','M3 6h18M9 6V4h6v2M5 6l1 14h12l1-14M10 10v6M14 10v6');icon.append(path);return icon;
  }
  function deleteControl(kind,onConfirm,form,dialog,options={}){
    let armedUntil=0,timer=null;const name=options.label||'Delete '+kind;
    const reset=()=>{clearTimeout(timer);timer=null;armedUntil=0;button.classList.remove('tl-project-delete-armed');button.setAttribute('aria-label',name);};
    const button=action('',()=>{if(!armedUntil||Date.now()>=armedUntil){reset();armedUntil=Date.now()+3000;button.classList.add('tl-project-delete-armed');button.setAttribute('aria-label',options.confirmLabel||'Confirm delete '+kind);timer=setTimeout(reset,3000);return;}reset();onConfirm();},'btn tl-project-delete'+(kind==='task'?' tl-task-delete':''));
    button.append(projectTrashIcon());button.setAttribute('aria-label',name);form.addEventListener('input',reset);dialog?.addEventListener('close',reset,{once:true});return button;
  }
  function editProject(project=null){
    const isNew=!project,original=project?clone(project):{id:'project-'+Date.now().toString(36)+'-'+Math.random().toString(36).slice(2,9),createdAt:new Date().toISOString(),completed:false};
    const {dialog,form,close}=modal(isNew?'Add project':'Edit project');dialog.classList.add('tl-project-editor');form.classList.add('tl-task-form');
    const title=inputField(form,'Project','text',original.title||'');title.placeholder='Project or idea';title.required=true;title.setAttribute('aria-label','Project title');
    const notes=inputField(form,'Notes','textarea',original.notes||'');notes.rows=4;notes.placeholder='Ideas to assess later';notes.setAttribute('aria-label','Project notes');
    const date=inputField(form,'Date','date',original.date||'');date.setAttribute('aria-label','Project date');
    const duration=node('div','tl-duration-row');form.append(duration);const hours=inputField(duration,'Hours','number',original.minutes===undefined?'':Math.floor(original.minutes/60)),minutes=inputField(duration,'Minutes','number',original.minutes===undefined?'':original.minutes%60);hours.min=minutes.min='0';hours.step=minutes.step='1';minutes.max='59';hours.setAttribute('aria-label','Project hours');minutes.setAttribute('aria-label','Project minutes');
    const error=node('p','tl-form-error');error.setAttribute('role','alert');form.append(error);const actions=node('div','tl-dialog-actions tl-project-actions');
    const read=()=>{const next=clone(original);if(!title.value.trim())throw Error('Enter a project title.');next.title=title.value.trim();if(notes.value.trim())next.notes=notes.value.trim();else delete next.notes;
      if(date.value){const parsed=new Date(date.value+'T12:00:00Z');if(!Number.isFinite(parsed.getTime())||parsed.toISOString().slice(0,10)!==date.value)throw Error('Choose a valid date.');next.date=date.value;}else delete next.date;
      const total=Number(hours.value)*60+Number(minutes.value);if((hours.value&&!/^\d+$/.test(hours.value))||(minutes.value&&!/^\d+$/.test(minutes.value))||Number(minutes.value)>59||((hours.value||minutes.value)&&(!Number.isSafeInteger(total)||total<1)))throw Error('Enter hours and 0–59 minutes.');if(hours.value||minutes.value)next.minutes=total;else delete next.minutes;return next;};
    const save=mode=>{try{const next=mode==='delete'?clone(original):read();saveProjectRecord(next,isNew,mode);close();}catch(e){error.textContent=e.message||'Couldn’t save this project. Please try again.';}};
    if(!isNew){actions.append(deleteControl('project',()=>save('delete'),form,dialog));
      actions.append(action(original.completed?'Reopen':'Complete',()=>save(original.completed?'reopen':'complete'),'btn'));
      const make=action(original.convertedTaskId?'Task already created':'Make task',()=>save('task'),'btn');make.disabled=!!original.convertedTaskId;actions.append(make);
    }
    actions.append(action('Cancel',close),action('Save',()=>save('save'),'btn btn-primary'));form.append(actions);form.addEventListener('submit',event=>{event.preventDefault();save('save');});if(isNew)title.focus();else dialog.focus({preventScroll:true});
  }
  function showProjects(){
    if(projectPanel?.dialog.isConnected){projectPanel.dialog.focus({preventScroll:true});return;}
    const {dialog,form,close}=modal('Projects');dialog.classList.add('tl-projects');const heading=dialog.querySelector('h2'),header=node('div','tl-projects-header');heading.replaceWith(header);header.append(heading,plusButton(()=>editProject(),'Add project','btn tl-project-add'));
    const active=node('div','tl-project-list'),completed=node('div','tl-project-list tl-project-completed'),toggle=action('Show completed',()=>{projectsCompletedOpen=!projectsCompletedOpen;render();},'btn tl-project-toggle');toggle.setAttribute('aria-controls','tl-project-completed');completed.id='tl-project-completed';
    const error=node('p','tl-form-error');error.setAttribute('role','alert');form.append(active,completed,error,toggle);
    const row=project=>{const button=action('',()=>editProject(project),'tl-project-row');button.dataset.projectId=project.id;button.append(node('strong','tl-project-title',project.title));
      const parts=[project.date?new Date(project.date+'T12:00:00Z').toLocaleDateString(undefined,{month:'short',day:'numeric',year:'numeric',timeZone:'UTC'}):'Any date'];if(project.minutes!==undefined)parts.push(projectDuration(project.minutes));if(project.convertedTaskId)parts.push('Added to task list');button.append(node('span','tl-project-meta',parts.join(' · ')));if(project.notes)button.append(node('span','tl-project-note',project.notes));return button;};
    const render=()=>{try{const deleted=projectDeletions(),projects=Object.values(projectMap()).filter(p=>!deleted[p.id]),open=projects.filter(p=>!p.completed).sort((a,b)=>(a.date||'9999').localeCompare(b.date||'9999')||a.title.localeCompare(b.title,undefined,{sensitivity:'base',numeric:true})),done=projects.filter(p=>p.completed).sort((a,b)=>(b.completedAt||'').localeCompare(a.completedAt||'')||a.title.localeCompare(b.title));
        active.replaceChildren(...open.map(row));if(!open.length)active.append(node('p','text-muted','No projects yet.'));completed.replaceChildren(...done.map(row));if(!done.length)completed.append(node('p','text-muted','No completed projects.'));completed.hidden=!projectsCompletedOpen;toggle.textContent=projectsCompletedOpen?'Hide completed':'Show completed';toggle.setAttribute('aria-expanded',String(projectsCompletedOpen));error.textContent='';
      }catch(e){error.textContent=e.message;}};
    projectPanel={dialog,render};render();dialog.focus({preventScroll:true});
  }
  function bindHeaderFit(){
    const header=document.querySelector('.tm-top'),date=document.getElementById('pt-date'),tabs=header?.querySelector('.nav'),buttons=[...header?.querySelectorAll('.nav-link')||[]];if(!header||!date||!tabs||buttons.length!==2)return;
    let frame=0,lastWidth=-1;
    const width=element=>{const previous=element.style.fontWeight;if(element!==date)element.style.fontWeight='650';const range=document.createRange();range.selectNodeContents(element);const measured=range.getBoundingClientRect().width;element.style.fontWeight=previous;return measured;};
    const fit=()=>{frame=0;if(!header.clientWidth)return;const elements=[date,...buttons];elements.forEach(element=>element.style.removeProperty('font-size'));const sizes=elements.map(element=>parseFloat(getComputedStyle(element).fontSize));
      for(let attempt=0;attempt<3;attempt++){const gap=parseFloat(getComputedStyle(header).columnGap)||0,textWidth=elements.reduce((total,element)=>total+width(element),0),fixed=buttons.reduce((total,button)=>{const style=getComputedStyle(button);return total+parseFloat(style.paddingLeft)+parseFloat(style.paddingRight)+parseFloat(style.borderLeftWidth)+parseFloat(style.borderRightWidth);},0)+parseFloat(getComputedStyle(tabs).paddingLeft)+parseFloat(getComputedStyle(tabs).paddingRight)+parseFloat(getComputedStyle(tabs).borderLeftWidth)+parseFloat(getComputedStyle(tabs).borderRightWidth)+gap,available=header.clientWidth-fixed-1;if(textWidth<=available+.25)break;const scale=Math.max(.1,Math.min(1,available/textWidth));elements.forEach((element,index)=>{sizes[index]*=scale;element.style.fontSize=sizes[index]+'px';});}
    };
    const queue=()=>{if(!frame)frame=requestAnimationFrame(fit);};
    if(global.ResizeObserver){const observer=new ResizeObserver(entries=>{const next=entries[0]?.contentRect.width;if(next!==lastWidth){lastWidth=next;queue();}});observer.observe(header);}
    const dates=new MutationObserver(queue);dates.observe(date,{childList:true,characterData:true,subtree:true});const tabChanges=new MutationObserver(queue);tabChanges.observe(tabs,{attributes:true,subtree:true,attributeFilter:['class']});global.addEventListener('resize',queue);if(document.fonts){document.fonts.ready.then(queue);document.fonts.addEventListener?.('loadingdone',queue);}queue();
  }
  function bindProjectDate(){
    const date=document.getElementById('pt-date');if(!date)return;date.setAttribute('role','button');date.tabIndex=0;date.title='Hold to open projects';date.setAttribute('aria-haspopup','dialog');
    let timer=null,start=null;const clear=()=>{if(timer!==null)clearTimeout(timer);timer=null;};
    date.addEventListener('pointerdown',event=>{clear();if(event.button!==0||event.isPrimary===false)return;start={x:event.clientX,y:event.clientY};timer=setTimeout(()=>{timer=null;if(!document.querySelector('dialog[open]')){showProjects();try{if(native&&native.priorityHaptic)native.priorityHaptic();}catch(_){}}},450);});
    date.addEventListener('pointermove',event=>{if(start&&Math.hypot(event.clientX-start.x,event.clientY-start.y)>10)clear();});for(const name of ['pointerup','pointercancel','pointerleave'])date.addEventListener(name,clear);global.addEventListener('blur',clear);document.addEventListener('visibilitychange',()=>{if(document.hidden)clear();});date.addEventListener('contextmenu',event=>event.preventDefault());date.addEventListener('keydown',event=>{if(event.key==='Enter'||event.key===' '){event.preventDefault();clear();showProjects();}});date.addEventListener('click',event=>{if(event.detail===0&&!document.querySelector('dialog[open]'))showProjects();});
  }

  function addToolbar() {
    const root = scope.root; if (root.querySelector('#tl-tools')) return;
    const toolbar = node('div', 'tl-tools'); toolbar.id = 'tl-tools';
    const select = node('select', 'form-control'); select.id = 'tl-context-filter'; select.setAttribute('aria-label', 'Filter tasks by context');
    select.addEventListener('change', () => global.TaskListApp.setContextFilter(select.value)); const filterWrap=node('div','tl-context-filter-wrap'),preview=node('span','tl-context-filter-preview');preview.id='tl-context-filter-preview';preview.hidden=true;preview.setAttribute('aria-hidden','true');filterWrap.append(select,preview);toolbar.append(filterWrap);
    const settings = action('⚙', showSettings, 'btn tl-settings'); settings.setAttribute('aria-label', 'Settings'); settings.title = 'Settings'; toolbar.append(settings);
    const add = plusButton(() => quickAddTask(), 'Add task', 'btn tl-add-task'); add.setAttribute('aria-label', 'Add task'); add.title = 'Add task';
    root.querySelector('.tm-top').after(toolbar); root.append(add); updateContextOptions();bindProjectDate();bindHeaderFit();
  }
  global.TaskListBridge = {
    editComponent,
    renderTrash,isPurgedOccurrence,scheduleIcon,
    weekendStartTime:()=>readCanonical().weekendStartTime||'18:00',contextsFor: derivedContexts, contextMatches: matches, decorateRow, queueSnapshot, lastSnapshot: [],
    attach(context) {
      scope = context;baselineRetired=new Set(scope.tasks.filter(task=>task.retired).map(task=>task.id));
      if(readCanonical().guildIconVersion!==1){const current=readCanonical(),guild=(current.taskOverrides||{}).guild||scope.tasks.find(task=>task.id==='guild'),overrides={...(current.taskOverrides||{})};if(guild)overrides.guild={...guild,_contexts:[...new Set([...derivedContexts(guild),'Guild'])]};persist({...scope.getState(),guildIconVersion:1,taskOverrides:overrides,contextIcons:{...(current.contextIcons||{}),Guild:GUILD_ICON_TOKEN}});}
      if(!readCanonical().notesMigrationV1){const state=clone(readCanonical()),groom=state.taskOverrides?.groom;if(groom){if(groom.instruction==='Between haircuts, trim hair with a #1.5 guard fully closed.')delete groom.instruction;if(groom.notes==='Hairline / Nails / Mustache / Body / Armpits / Teeth / Jewelry / Nose / Ears / Haircut: #1.5 guard, fully closed / Pluck eyebrows / Trim beard: #0.5 guard, fully closed.')delete groom.notes;}persist({...scope.getState(),...state,notesMigrationV1:true});}
      if(!readCanonical().lowPriorityDefaultV1)persist({...scope.getState(),...readCanonical(),lowPriorityDefaultV1:true});
      const state = readCanonical(); selectedContext = typeof state.uiContext === 'string' ? state.uiContext : '';
      applyDefinitions(state); applyTheme(); addToolbar();
      if(global.TaskListWidgetEditor){document.documentElement.classList.add('tl-widget-editor');
        document.documentElement.style.setProperty('background','transparent','important');
        document.body.style.setProperty('background','transparent','important');
      }
      global.TaskListApp = {
        quickAddTask, handleBack,showProjects,
        editWidgetTask(rowId) {
          if(typeof rowId!=='string'||!rowId.startsWith('html:'))return false;
          const key=rowId.slice(5),match=key.match(/^(.*):(\d{4}-\d{2}-\d{2})$/);
          if(!match)return false;
          const task=scope.tasks.find(value=>value.id===match[1]);if(!task)return false;
          const item=[...scope.dueToday(),...scope.overdue(),...scope.onDate(match[2])].find(value=>scope.progressKey(value.task,value.stateDate)===key);
          if(!item||scope.isDone(task,item.stateDate))return false;
          editTask(task,item);return true;
        },
        showToday(){scope.showView('today');},
        isInteracting(){return scope.isInteracting()||!!settingsScreen||!!document.querySelector('dialog[open]');},
        addTask(prefill) { try { if (typeof prefill === 'string') prefill = JSON.parse(prefill || '{}'); } catch (_) { prefill = {}; } editTask(null, null, prefill || {}); },
        setContextFilter(label) { selectedContext = typeof label === 'string' ? label : ''; saveExtras(); rebuild(); },
        reloadFromNative() { cached = null;lastCanonical=null; const latest = readCanonical(); scope.setState(latest); applyDefinitions(latest); selectedContext = typeof latest.uiContext === 'string' ? latest.uiContext : ''; applyTheme(); rebuild(); },
        debugSummary() { return { definitions: scope.tasks.length, savedKeys: Object.keys(scope.getState().completed).length, moves: Object.keys(scope.getState().movedTo).length, active: scope.getState().inProgress, today: scope.today, contexts: allContexts(), snapshot: global.TaskListBridge.lastSnapshot.length }; }
      };
      if(global.TaskListWidgetEditor&&native&&native.editorReady)native.editorReady();
      queueSnapshot();
    }
  };
})(window);
