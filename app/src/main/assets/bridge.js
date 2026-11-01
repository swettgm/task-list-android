/* Generic Android integration. Personal task definitions and history are supplied
   privately in the installed document; this file contains no personal task data. */
(function (global) {
  'use strict';
  const KEY = 'task-list-20261020-v1';
  const DEFAULT_CONTEXTS = ['At home', 'On the road', 'On the phone', 'On the computer', 'Outdoors'];
  let native = global.TaskListNative || null;
  let fallback = null;
  try { fallback = global.localStorage; } catch (_) {}
  let cached = null;
  let scope = null;
  let selectedContext = '';
  let snapshotTimer = 0;
  const clone = value => JSON.parse(JSON.stringify(value));
  const parse = value => { try { const result = JSON.parse(value || '{}'); return result && typeof result === 'object' && !Array.isArray(result) ? result : {}; } catch (_) { return {}; } };
  function readCanonical() {
    if (cached) return cached;
    let value = null;
    try { if (native && native.readState) value = native.readState(); } catch (_) {}
    if (!value && fallback) { try { value = fallback.getItem(KEY); } catch (_) {} }
    cached = parse(value);
    return cached;
  }
  function persist(value) {
    let fresh = {};
    try { if (native && native.readState) fresh = parse(native.readState()); } catch (_) {}
    const previous = readCanonical();
    const next = Object.assign({}, fresh, previous, value);
    // A launcher popup can add a task while this page is paused. Preserve its
    // new IDs when the older page later saves a checkbox or context change.
    const mergedTasks = new Map();
    [fresh.customTasks, previous.customTasks, value.customTasks].forEach(items => {
      if (Array.isArray(items)) items.forEach(task => { if (task && typeof task.id === 'string') mergedTasks.set(task.id, clone(task)); });
    });
    if (mergedTasks.size) next.customTasks = Array.from(mergedTasks.values());
    next.taskOverrides = Object.assign({}, fresh.taskOverrides, previous.taskOverrides, value.taskOverrides);
    next.pairOverrides = Object.assign({}, fresh.pairOverrides, previous.pairOverrides, value.pairOverrides);
    let succeeded = false;
    if (native && native.writeState) { native.writeState(JSON.stringify(next)); succeeded = true; }
    if (fallback) { try { fallback.setItem(KEY, JSON.stringify(next)); succeeded = true; } catch (_) {} }
    if (!succeeded) throw new Error('Task storage is unavailable');
    cached = next;
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
    return [...new Set([...DEFAULT_CONTEXTS, ...(scope ? scope.tasks.flatMap(derivedContexts) : [])])];
  }
  function matches(task) { return !selectedContext || derivedContexts(task).includes(selectedContext); }
  function rebuild() {
    if (!scope) return;
    scope.renderToday(); scope.renderFuture(); updateContextOptions(); queueSnapshot();
  }
  function saveExtras() {
    if (!scope) return;
    const data = readCanonical();
    persist(Object.assign({}, scope.getState(), {
      taskOverrides: data.taskOverrides || {}, customTasks: data.customTasks || [],
      pairOverrides: data.pairOverrides || {}, uiContext: selectedContext
    }));
  }
  function saveDefinition(task, isNew) {
    const state = readCanonical();
    if (isNew) {
      const custom = Array.isArray(state.customTasks) ? state.customTasks.slice() : [];
      custom.push(clone(task)); state.customTasks = custom;
    } else if ((state.customTasks || []).some(item => item.id === task.id)) {
      state.customTasks = state.customTasks.map(item => item.id === task.id ? clone(task) : item);
    } else {
      state.taskOverrides = Object.assign({}, state.taskOverrides, { [task.id]: clone(task) });
    }
    persist(Object.assign({}, scope.getState(), state));
    rebuild();
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
  function saveManualLinks(id, peers) {
    const state = readCanonical(), links = {};
    Object.entries(state.manualLinks || {}).forEach(([key, values]) => {
      if (key !== id && Array.isArray(values)) links[key] = values.filter(peer => peer !== id);
    });
    links[id] = [...new Set(peers.filter(peer => peer !== id))];
    links[id].forEach(peer => { links[peer] = [...new Set([...(links[peer] || []), id])]; });
    persist(Object.assign({}, state, { manualLinks: links }));
    applyDefinitions(readCanonical());
    rebuild();
  }
  function linkedTasksField(form, task) {
    const selected = new Set(task ? scope.pairWith[task.id] || [] : []);
    const group = node('fieldset', 'tl-context-options tl-links');
    group.append(node('legend', '', 'Linked tasks'));
    const row = node('div', 'tl-link-picker');
    const select = node('select', 'form-control');
    select.setAttribute('aria-label', 'Choose a task to link');
    const add = action('Link task', () => { if (select.value) { selected.add(select.value); render(); } }, 'btn tl-link-add');
    const list = node('div', 'tl-linked-list');
    const candidates = scope.tasks.filter(candidate => !task || candidate.id !== task.id)
      .slice().sort((a, b) => a.title.localeCompare(b.title));
    function render() {
      select.replaceChildren();
      const placeholder = node('option', '', 'Choose a task…'); placeholder.value = ''; select.append(placeholder);
      candidates.filter(candidate => !selected.has(candidate.id)).forEach(candidate => {
        const date = candidate.due || candidate.recurrence;
        const option = node('option', '', candidate.title + (date ? ' · ' + date : ''));
        option.value = candidate.id; select.append(option);
      });
      add.disabled = !select.value;
      list.replaceChildren();
      selected.forEach(id => {
        const target = candidates.find(candidate => candidate.id === id); if (!target) return;
        const chip = node('div', 'tl-linked-task'); chip.append(node('span', '', target.title));
        const remove = action('×', () => { selected.delete(id); render(); }, 'btn tl-unlink');
        remove.setAttribute('aria-label', 'Unlink ' + target.title); chip.append(remove); list.append(chip);
      });
    }
    select.addEventListener('change', () => { add.disabled = !select.value; });
    row.append(select, add); group.append(row, list); form.append(group); render();
    return () => Array.from(selected);
  }
  function modal(title) {
    const dialog = node('dialog', 'tl-dialog');
    const heading = node('h2', '', title); dialog.append(heading);
    const form = node('form'); form.method = 'dialog'; dialog.append(form);
    document.body.append(dialog);
    const close = () => { if (dialog.open && dialog.close) dialog.close(); dialog.remove(); };
    dialog.addEventListener('cancel', event => { event.preventDefault(); close(); });
    if (dialog.showModal) dialog.showModal(); else dialog.setAttribute('open', '');
    return { dialog, form, close };
  }
  function inputField(form, label, type, value) {
    const wrap = node('label', 'tl-field'); wrap.append(node('span', '', label));
    const field = node(type === 'textarea' ? 'textarea' : 'input', 'form-control');
    if (type !== 'textarea') field.type = type;
    field.value = value === undefined || value === null ? '' : String(value);
    if (type === 'textarea') field.rows = 2;
    wrap.append(field); form.append(wrap); return field;
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
      check.checked = (selected || []).includes(label); row.append(check, node('span', '', label)); grid.append(row);
      return { label, check };
    });
    group.append(grid);
    const custom = node('input', 'form-control tl-other-contexts'); custom.type = 'text';
    custom.placeholder = 'Other contexts, separated by commas'; custom.setAttribute('aria-label', 'Other contexts'); group.append(custom);
    form.append(group);
    return () => [...new Set([...inputs.filter(x => x.check.checked).map(x => x.label), ...custom.value.split(',').map(x => x.trim()).filter(Boolean)])];
  }
  function editTask(task, item, prefill) {
    if (!scope) return;
    const isNew = !task; const original = task ? clone(task) : {};
    const { form, close } = modal(isNew ? 'Add task' : 'Edit task');
    form.classList.add('tl-task-form');
    const title = inputField(form, 'Task', 'text', original.title || (prefill && prefill.title) || ''); title.required = true; title.placeholder = 'What needs doing?';
    const priority = choiceField(form, 'Priority', scope.groups.map(([key, label]) => [key, key === 'UNSET' ? 'None' : label.replace(' priority', '')]), original.priority || 'UNSET');
    const schedule = node('div', 'tl-schedule-grid'); form.append(schedule);
    const initialDate = item ? item.date : (original.due || original.intervalStart || original.weekendCycleStart || (prefill && (prefill.dueDate || prefill.plannedDate)) || scope.today);
    const date = inputField(schedule, 'Date', 'date', initialDate); date.required = true;
    const time = inputField(schedule, 'Time', 'text', original.time || ''); time.placeholder = 'Any time';
    const duration = inputField(schedule, 'Duration', 'number', original.minutes); duration.placeholder = 'Minutes'; duration.min = '1'; duration.step = '1';
    const urgentWrap = node('label', 'tl-context-choice tl-urgent'), urgent = node('input'); urgent.type = 'checkbox'; urgent.checked = !!original.urgent;
    urgentWrap.append(urgent, node('span', '', 'Urgent')); schedule.append(urgentWrap);
    const oldMode = original.weekendCycleStart ? 'weekends' : original.intervalDays ? 'interval' : original.days && original.days.length ? (original.days.length === 7 ? 'daily' : 'weekdays') : 'once';
    const recurrence = choiceField(form, 'Repeat', [['once','Once'],['daily','Daily'],['weekdays','Weekdays'],['interval','Every N days'],['weekends','Alternate weekends']], oldMode);
    const week = node('div', 'tl-weekdays');
    const weekChecks = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'].map((name, index) => {
      const label = node('label', 'tl-context-choice'), check = node('input'); check.type = 'checkbox'; check.checked = (original.days || []).includes(index);
      check.setAttribute('aria-label', name); label.append(check, node('span', '', name.slice(0, 3))); week.append(label); return check;
    }); recurrence.append(week);
    const interval = inputField(recurrence, 'Every', 'number', original.intervalDays || 2); interval.placeholder = 'Days'; interval.min = '1'; interval.step = '1';
    const toggleRepeat = () => { week.hidden = recurrence.value !== 'weekdays'; interval.closest('label').hidden = recurrence.value !== 'interval'; };
    recurrence.addEventListener('change', toggleRepeat); toggleRepeat();
    const selected = task ? derivedContexts(task) : (prefill && (prefill.labels || [prefill.label || prefill.widget_label].filter(Boolean))) || (selectedContext ? [selectedContext] : []);
    const readContexts = contextsField(form, selected);
    const readLinks = linkedTasksField(form, task);
    const extra = node('details', 'tl-extra-fields'); extra.append(node('summary', '', 'Notes & checklist')); extra.open = !!original.notes;
    const extraGrid = node('div', 'tl-notes-grid'); extra.append(extraGrid); form.append(extra);
    const notes = inputField(extraGrid, 'Notes', 'textarea', original.notes || ''); notes.placeholder = 'Add notes';
    const steps = isNew ? inputField(extraGrid, 'Checklist', 'textarea', '') : null;
    if (steps) steps.placeholder = 'One step per line';
    if (!isNew && scope.hasSubtasks(task)) extraGrid.append(node('p', 'text-muted', 'Check or reopen steps on the task.'));
    const error = node('p', 'tl-form-error'); error.setAttribute('role', 'alert'); form.append(error);
    const buttons = node('div', 'tl-dialog-actions');
    buttons.append(action('Cancel', close), action('Save task', () => {
      if (!title.value.trim()) { error.textContent = 'Enter a task title.'; title.focus(); return; }
      if (!/^\d{4}-\d{2}-\d{2}$/.test(date.value)) { error.textContent = 'Choose a valid date.'; return; }
      if (duration.value && (!/^\d+$/.test(duration.value) || Number(duration.value) < 1)) { error.textContent = 'Duration must be a positive number of minutes.'; return; }
      if (recurrence.value === 'weekdays' && !weekChecks.some(check => check.checked)) { error.textContent = 'Select at least one weekday.'; return; }
      if (recurrence.value === 'interval' && (!/^\d+$/.test(interval.value) || Number(interval.value) < 1)) { error.textContent = 'Repeat interval must be at least one day.'; return; }
      const next = Object.assign({}, original, { id: original.id || 'app-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2,9), title: title.value.trim(), priority: urgent.checked ? 'HIGHEST' : priority.value, _contexts: readContexts() });
      if (duration.value) next.minutes = Number(duration.value); else delete next.minutes;
      if (time.value.trim()) next.time = time.value.trim(); else delete next.time;
      if (notes.value.trim()) next.notes = notes.value.trim(); else delete next.notes;
      delete next.aiHelp;
      if (urgent.checked) next.urgent = true; else delete next.urgent;
      const changedSchedule = recurrence.value !== oldMode || (recurrence.value === 'weekdays' && JSON.stringify(weekChecks.flatMap((check, i) => check.checked ? [i] : [])) !== JSON.stringify(original.days || [])) || (recurrence.value === 'interval' && Number(interval.value) !== original.intervalDays);
      if (isNew || changedSchedule) {
        ['due','days','intervalDays','intervalStart','weekendCycleStart','recurrence'].forEach(key => delete next[key]);
        if (recurrence.value === 'once') next.due = date.value;
        if (recurrence.value === 'daily') { next.days = [0,1,2,3,4,5,6]; next.recurrence = 'Daily'; }
        if (recurrence.value === 'weekdays') { next.days = weekChecks.flatMap((check, index) => check.checked ? [index] : []); next.recurrence = next.days.map(day => ['Sundays','Mondays','Tuesdays','Wednesdays','Thursdays','Fridays','Saturdays'][day]).join(', '); }
        if (recurrence.value === 'interval') { next.intervalDays = Number(interval.value); next.intervalStart = date.value; next.recurrence = 'Every ' + interval.value + ' days'; }
        if (recurrence.value === 'weekends') { const weekday = new Date(date.value + 'T12:00:00Z').getUTCDay(); next.weekendCycleStart = scope.addDays(date.value, (7 - weekday) % 7); next.recurrence = 'Every other Saturday and Sunday'; }
      } else if (item && date.value !== initialDate) {
        // Move this occurrence; keep its original occurrence/history keys intact.
        scope.moveOccurrence(task, item.stateDate, date.value);
      }
      if (steps) { const names = steps.value.split('\n').map(x => x.trim()).filter(Boolean); if (names.length) next.steps = names; }
      if (isNew) scope.tasks.push(next); else scope.tasks[scope.tasks.findIndex(t => t.id === task.id)] = next;
      saveDefinition(next, isNew); saveManualLinks(next.id, readLinks()); close();
    }, 'btn btn-primary'));
    form.append(buttons); title.focus();
  }
  function updateContextOptions() {
    const select = document.getElementById('tl-context-filter'); if (!select) return;
    const wanted = ['', ...allContexts()]; const existing = Array.from(select.options).map(o => o.value);
    if (JSON.stringify(wanted) !== JSON.stringify(existing)) {
      select.replaceChildren(); wanted.forEach(label => { const option = node('option', '', label || 'All contexts'); option.value = label; select.append(option); });
    }
    select.value = selectedContext;
  }
  function decorateRow(wrapper, task, item, completed) {
    wrapper.dataset.taskId = task.id; wrapper.dataset.stateDate = item.stateDate; wrapper.dataset.displayDate = item.date;
    const text = wrapper.querySelector('.form-check-label') || wrapper.querySelector('.tm-future-content');
    const labels = derivedContexts(task);
    if (text && labels.length) { const badges = node('span', 'tl-context-badges'); labels.forEach(label => badges.append(node('span', 'tl-context-badge', label))); text.append(badges); }
    const controls = wrapper.querySelector('.tm-item-actions');
    if (controls) {
      const edit = action('⚙', () => editTask(task, item), 'tm-move-control tl-edit-button');
      edit.setAttribute('aria-label', 'Edit ' + task.title); edit.title = 'Edit task'; controls.append(edit);
    }
    queueSnapshot();
  }
  function queueSnapshot() {
    if (!scope || snapshotTimer) return;
    snapshotTimer = setTimeout(() => { snapshotTimer = 0; publishSnapshot(); }, 0);
  }
  function publishSnapshot() {
    if (!scope) return;
    const rows = new Map(); const active = scope.getState().inProgress;
    const push = (item, onToday) => {
      const key = scope.progressKey(item.task, item.stateDate); if (rows.has(key)) return;
      const done = scope.isDone(item.task, item.stateDate); const task = item.task;
      const completionKeys = scope.hasSubtasks(task) ? Array.from({ length: scope.subtaskCount(task, item.stateDate) }, (_, i) => scope.childKey(task, item.stateDate, i)) : [scope.occurrenceKey(task, item.stateDate)];
      rows.set(key, {
        id: 'html:' + key, title: task.title, listName: 'My tasks',
        dueDate: task.due || '', plannedDate: item.date || scope.today,
        done, status: done ? 'Done' : active && (active === key || completionKeys.includes(active)) ? 'In progress' : 'Next',
        labels: derivedContexts(task), priority: task.priority || 'UNSET', minutes: Number(task.minutes) || 0,
        createdAt: Date.parse(item.stateDate + 'T12:00:00Z') || 0,
        completionKeys, progressKey: key, stateDate: item.stateDate, onToday: !!onToday,
        recurrence: task.recurrence || '', source: 'html'
      });
    };
    scope.dueToday().forEach(item => push(item, true)); scope.overdue().forEach(item => push(item, false));
    for (let day = 1; day <= 14; day++) scope.onDate(scope.addDays(scope.today, day)).forEach(item => push(item, false));
    const snapshot = Array.from(rows.values()); global.TaskListBridge.lastSnapshot = snapshot;
    if (native && native.updateSnapshot) { try { native.updateSnapshot(JSON.stringify(snapshot)); } catch (error) { console.error('Could not update the home widget', error); } }
  }
  const DEFAULT_THEME = { accent:'#68b7ff', background:'#10151f' };
  function normalizeHex(value) {
    const text = String(value || '').trim().replace(/^#/, '');
    if (/^[0-9a-f]{3}$/i.test(text)) return '#' + text.split('').map(c => c + c).join('').toLowerCase();
    return /^[0-9a-f]{6}$/i.test(text) ? '#' + text.toLowerCase() : null;
  }
  function mix(a, b, ratio) {
    const channels = value => [1,3,5].map(i => parseInt(value.slice(i, i + 2), 16));
    const left = channels(a), right = channels(b);
    return '#' + left.map((v, i) => Math.round(v * (1 - ratio) + right[i] * ratio).toString(16).padStart(2, '0')).join('');
  }
  function ink(color) {
    const values = [1,3,5].map(i => parseInt(color.slice(i,i + 2),16) / 255)
      .map(v => v <= .04045 ? v / 12.92 : Math.pow((v + .055) / 1.055, 2.4));
    const luminance = values[0] * .2126 + values[1] * .7152 + values[2] * .0722;
    return luminance > .179 ? '#10151f' : '#f3f6fb';
  }
  function applyTheme() {
    const stored = readCanonical().uiTheme || {};
    const accent = normalizeHex(stored.accent) || DEFAULT_THEME.accent;
    const background = normalizeHex(stored.background) || DEFAULT_THEME.background;
    const text = ink(background), light = text === '#10151f';
    const surface = background === DEFAULT_THEME.background ? '#172131' : mix(background, text, .07);
    const variables = { '--tl-accent':accent, '--tl-background':background, '--tl-surface':surface,
      '--tl-ink':ink(accent), '--tl-text':text, '--tl-muted':mix(background, text, .7),
      '--tl-border':mix(background, text, .25), '--tl-selected':mix(background, accent, .18) };
    Object.entries(variables).forEach(([name, value]) => document.documentElement.style.setProperty(name, value));
    document.documentElement.style.colorScheme = light ? 'light' : 'dark';
    if (native && native.setThemeColors) native.setThemeColors(background);
    return {accent,background};
  }
  function quickAddTask(prefill = {}) {
    const { form, close } = modal('Quick add'); form.classList.add('tl-quick-form');
    const title = inputField(form, 'Task', 'text', prefill.title || ''); title.placeholder = 'What needs doing?';
    const error = node('p', 'tl-form-error'); error.setAttribute('role', 'alert'); form.append(error);
    const labels = prefill.labels || (selectedContext ? [selectedContext] : []);
    const save = () => {
      if (!title.value.trim()) { error.textContent = 'Enter a task title.'; title.focus(); return; }
      const next = { id:'app-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2,9),
        title:title.value.trim(), priority:'UNSET', due:prefill.dueDate || scope.today, _contexts:labels };
      scope.tasks.push(next); saveDefinition(next, true); close();
    };
    const actions = node('div', 'tl-dialog-actions');
    actions.append(action('Cancel', close), action('Details', () => {
      const value = title.value; close(); editTask(null, null, { ...prefill, title:value, labels });
    }), action('Add', save, 'btn btn-primary'));
    form.append(actions); form.addEventListener('submit', event => { event.preventDefault(); save(); }); title.focus();
  }
  function handleBack() {
    const dialogs = Array.from(document.querySelectorAll('dialog[open]'));
    const active = dialogs[dialogs.length - 1]; if (!active) return false;
    active.dispatchEvent(new Event('cancel', { cancelable:true }));
    if (active.isConnected) { if (active.close) active.close(); active.remove(); }
    return true;
  }
  function showSettings() {
    const { form, close } = modal('Settings'); form.classList.add('tl-settings-form');
    const theme = applyTheme();
    const colors = node('div', 'tl-schedule-grid'); form.append(colors);
    const accent = inputField(colors, 'Accent hex', 'text', theme.accent); accent.placeholder = '#68B7FF'; accent.spellcheck = false;
    const background = inputField(colors, 'Background hex', 'text', theme.background); background.placeholder = '#10151F'; background.spellcheck = false;
    const preview = node('div', 'tl-color-preview', 'Color preview'); form.append(preview);
    const previewColors = () => {
      const a = normalizeHex(accent.value), b = normalizeHex(background.value);
      if (a && b) { preview.style.background = b; preview.style.color = a; preview.style.borderColor = a; }
    };
    accent.addEventListener('input', previewColors); background.addEventListener('input', previewColors); previewColors();
    const error = node('p', 'tl-form-error'); error.setAttribute('role', 'alert'); form.append(error);
    const save = () => {
      const a = normalizeHex(accent.value), b = normalizeHex(background.value);
      if (!a || !b) { error.textContent = 'Use a hex color such as #68B7FF.'; return; }
      persist(Object.assign({}, readCanonical(), { uiTheme:{accent:a,background:b} }));
      applyTheme(); error.textContent = ''; accent.value = a; background.value = b; previewColors();
    };
    const colorsActions = node('div', 'tl-color-actions');
    colorsActions.append(action('Apply colors', save, 'btn btn-primary'), action('Reset colors', () => {
      accent.value = DEFAULT_THEME.accent; background.value = DEFAULT_THEME.background; save();
    })); form.append(colorsActions);
    if (native && native.showWidgetSettings) {
      form.append(action('Add home-screen widget', () => { close(); native.showWidgetSettings(); }, 'btn tl-widget-settings'));
    }
    const actions = node('div', 'tl-dialog-actions'); actions.append(action('Close', close, 'btn')); form.append(actions);
  }
  function addToolbar() {
    const root = scope.root; if (root.querySelector('#tl-tools')) return;
    const toolbar = node('div', 'tl-tools'); toolbar.id = 'tl-tools';
    const select = node('select', 'form-control'); select.id = 'tl-context-filter'; select.setAttribute('aria-label', 'Filter tasks by context');
    select.addEventListener('change', () => global.TaskListApp.setContextFilter(select.value)); toolbar.append(select);
    const settings = action('⚙', showSettings, 'btn tl-settings'); settings.setAttribute('aria-label', 'Settings'); settings.title = 'Settings'; toolbar.append(settings);
    const add = action('+', () => quickAddTask(), 'btn tl-add-task'); add.setAttribute('aria-label', 'Add task'); add.title = 'Add task';
    root.querySelector('.tm-top').after(toolbar); root.append(add); updateContextOptions();
  }
  global.TaskListBridge = {
    contextsFor: derivedContexts, contextMatches: matches, decorateRow, queueSnapshot, lastSnapshot: [],
    attach(context) {
      scope = context;
      const state = readCanonical(); selectedContext = typeof state.uiContext === 'string' ? state.uiContext : '';
      applyDefinitions(state); applyTheme(); addToolbar();
      global.TaskListApp = {
        quickAddTask, handleBack,
        addTask(prefill) { try { if (typeof prefill === 'string') prefill = JSON.parse(prefill || '{}'); } catch (_) { prefill = {}; } editTask(null, null, prefill || {}); },
        setContextFilter(label) { selectedContext = typeof label === 'string' ? label : ''; saveExtras(); rebuild(); },
        reloadFromNative() { cached = null; const latest = readCanonical(); scope.setState(latest); applyDefinitions(latest); selectedContext = typeof latest.uiContext === 'string' ? latest.uiContext : ''; applyTheme(); rebuild(); },
        debugSummary() { return { definitions: scope.tasks.length, savedKeys: Object.keys(scope.getState().completed).length, moves: Object.keys(scope.getState().movedTo).length, active: scope.getState().inProgress, today: scope.today, contexts: allContexts(), snapshot: global.TaskListBridge.lastSnapshot.length }; }
      };
      queueSnapshot();
    }
  };
})(window);
