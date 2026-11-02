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
  let lastCanonical = null;
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
  function persist(value) {
    const previous=readCanonical(),before=lastCanonical||clone(previous);
    let fresh=previous;
    try{if(native&&native.readState)fresh=parse(native.readState());}catch(_){}
    const desired=Object.assign({},previous,value);
    const next=mergeState(before,desired,fresh,'state');
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
  const contextIcon=label=>({'At home':'🏠','On the road':'🚗','On the phone':'📱','On the computer':'💻','Outdoors':'🌳'}[label]||'🏷️');
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
  function linkedTasksField(form, task, occurrenceDate) {
    const selected = new Set(task ? scope.pairWith[task.id] || [] : []);
    const group = node('fieldset', 'tl-context-options tl-links');
    group.append(node('legend', '', 'Linked tasks'));
    const row = node('div', 'tl-link-picker');
    const select = node('select', 'form-control');
    select.setAttribute('aria-label', 'Choose a task to link');
    const add = action('Link task', () => { if (select.value) { selected.add(select.value); render(); } }, 'btn tl-link-add');
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
      check.checked = (selected || []).includes(label); row.setAttribute('aria-label',label);row.title=label;row.append(check, node('span', '', contextIcon(label))); grid.append(row);
      return { label, check };
    });
    group.append(grid);
    form.append(group);
    return () => inputs.filter(x => x.check.checked).map(x => x.label);
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
    const shortcuts=node('div','tl-date-shortcuts');date.parentElement.append(shortcuts);
    shortcuts.append(action('Tomorrow',()=>{date.value=scope.addDays(scope.today,1);}),action('Weekend',()=>{const day=new Date(scope.today+'T12:00:00Z').getUTCDay();date.value=scope.addDays(scope.today,(6-day+7)%7||7);}));
    const toPicker=value=>{const match=String(value||'').match(/^(\d{1,2}):(\d{2})(?:\s*(AM|PM))?$/i);if(!match)return '';let h=Number(match[1]);if(match[3])h=h%12+(/pm/i.test(match[3])?12:0);return h<24?String(h).padStart(2,'0')+':'+match[2]:'';};
    const time = inputField(schedule, 'Time', 'time', toPicker(original.time)); time.placeholder = 'Any time';time.setAttribute('aria-label','Time');
    time.parentElement.append(action('Clear',()=>{time.value='';time.dispatchEvent(new Event('input'));},'btn tl-clear-time'));
    const durationGroup=node('div','tl-duration-group');durationGroup.append(node('span','tl-duration-caption','Duration'));
    const durationRow=node('div','tl-duration-row');durationGroup.append(durationRow);schedule.append(durationGroup);
    const hours=inputField(durationRow,'Hours','number',original.minutes===undefined?'':Math.floor(original.minutes/60));hours.placeholder='Hours';hours.min='0';hours.step='1';hours.setAttribute('aria-label','Hours');
    const minutes=inputField(durationRow,'Minutes','number',original.minutes===undefined?'':original.minutes%60);minutes.placeholder='Minutes';minutes.min='0';minutes.max='59';minutes.step='1';minutes.setAttribute('aria-label','Minutes');
    const urgentWrap = node('label', 'tl-context-choice tl-urgent'), urgent = node('input'); urgent.type = 'checkbox'; urgent.checked = !!original.urgent;
    urgentWrap.append(urgent, node('span', '', 'Urgent'));
    const flags=node('div','tl-task-flags'),notifyWrap=node('label','tl-context-choice tl-notify'),notify=node('input');notify.type='checkbox';notify.checked=original.notify===true;notify.setAttribute('aria-label','Notify at scheduled time');notifyWrap.append(notify,node('span','','Notify'));flags.append(urgentWrap,notifyWrap);schedule.append(flags);
    const syncNotify=()=>{notify.disabled=!time.value;if(!time.value)notify.checked=false;notifyWrap.title=time.value?'Send a reminder at this time':'Set a time to enable notifications';};time.addEventListener('input',syncNotify);time.addEventListener('change',syncNotify);syncNotify();
    const oldSpec=global.TasksSchedule.fromTask(original,initialDate);
    const oldMode=oldSpec.mode==='repeat'&&oldSpec.unit==='day'&&oldSpec.amount===1?'daily':(oldSpec.mode==='weekdays'&&JSON.stringify(oldSpec.weekdays)==='[0,6]'?'weekends':oldSpec.mode);
    if(oldSpec.mode==='repeat')date.value=oldSpec.start;
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
    const selected = task ? derivedContexts(task) : (prefill && (prefill.labels || [prefill.label || prefill.widget_label].filter(Boolean))) || (selectedContext ? [selectedContext] : []);
    const readContexts = contextsField(form, selected);
    const readLinks = linkedTasksField(form, task, item?item.date:scope.today);
    const extra = node('section', 'tl-extra-fields');
    const extraGrid = node('div', 'tl-notes-grid'); extra.append(extraGrid); form.append(extra);
    const notes = inputField(extraGrid, 'Notes', 'textarea', original.notes || ''); notes.placeholder = 'Add notes';
    const checklistDate=item?item.stateDate:scope.today;
    const originalSteps=isNew?[]:scope.checklistEntries(original,checklistDate);
    const steps = inputField(extraGrid, 'Checklist', 'textarea', originalSteps.map(step=>step.title).join('\n'));
    if (steps) steps.placeholder = 'One step per line';

    const error = node('p', 'tl-form-error'); error.setAttribute('role', 'alert'); form.append(error);
    const buttons = node('div', 'tl-dialog-actions');
    buttons.append(action('Cancel', close), action('Save task', () => {
      if (!title.value.trim()) { error.textContent = 'Enter a task title.'; title.focus(); return; }
      if (!/^\d{4}-\d{2}-\d{2}$/.test(date.value)) { error.textContent = 'Choose a valid date.'; return; }
      if((hours.value&&!/^\d+$/.test(hours.value))||(minutes.value&&!/^\d+$/.test(minutes.value))||Number(minutes.value)>59||((hours.value||minutes.value)&&Number(hours.value)*60+Number(minutes.value)<1)){error.textContent='Enter hours and 0–59 minutes.';return;}
      if (recurrence.value === 'weekdays' && !weekChecks.some(check => check.checked)) { error.textContent = 'Select at least one weekday.'; return; }
      if(recurrence.value==='repeat'&&(!/^\d+$/.test(amount.value)||Number(amount.value)<1)){error.textContent='Repeat amount must be at least 1.';return;}
      const next = Object.assign({}, original, { id: original.id || 'app-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2,9), title: title.value.trim(), priority: urgent.checked ? 'HIGHEST' : priority.value, _contexts: readContexts() });
      if(hours.value||minutes.value)next.minutes=Number(hours.value)*60+Number(minutes.value);else delete next.minutes;
      if(time.value){const [h,m]=time.value.split(':').map(Number);next.time=(h%12||12)+':'+String(m).padStart(2,'0')+(h>=12?' PM':' AM');}else delete next.time;
      if (notes.value.trim()) next.notes = notes.value.trim(); else delete next.notes;
      delete next.aiHelp;
      if (urgent.checked) next.urgent = true; else delete next.urgent;
      if(notify.checked&&time.value)next.notify=true;else delete next.notify;
      const chosenDays=weekChecks.flatMap((check,index)=>check.checked?[index]:[]);
      const changedSchedule=recurrence.value!==oldMode||(recurrence.value==='weekdays'&&JSON.stringify(chosenDays)!==JSON.stringify(oldSpec.weekdays||[]))||(recurrence.value==='repeat'&&(Number(amount.value)!==oldSpec.amount||unit.value!==oldSpec.unit||date.value!==oldSpec.start||(unit.value==='week'&&JSON.stringify(chosenDays)!==JSON.stringify(oldSpec.weekdays||[]))));
      if(isNew||changedSchedule){
        ['due','days','intervalDays','intervalStart','weekendCycleStart','recurrence','repeat','startsOn'].forEach(key=>delete next[key]);
        if(recurrence.value==='once')next.due=date.value;
        if(recurrence.value==='daily'){next.repeat={amount:1,unit:'day',start:date.value};next.recurrence='Daily';}
        if(recurrence.value==='weekends'){next.days=[0,6];next.startsOn=date.value;next.recurrence='Weekends';}
        if(recurrence.value==='weekdays'){next.days=chosenDays;next.startsOn=date.value;next.recurrence=global.TasksSchedule.weekdaysLabel(chosenDays);}
        if(recurrence.value==='repeat'){next.repeat={amount:Number(amount.value),unit:unit.value,start:date.value};if(unit.value==='week'&&chosenDays.length)next.repeat.weekdays=chosenDays;next.recurrence=global.TasksSchedule.describe(next);}
      }else if(item&&recurrence.value!=='repeat'&&date.value!==initialDate){
        scope.moveOccurrence(task,item.stateDate,date.value);
      }
      const names=steps.value.split('\n').map(x=>x.trim()).filter(Boolean);
      if(isNew){if(names.length)next.steps=names;}
      else if(JSON.stringify(names)!==JSON.stringify(originalSteps.map(step=>step.title))){
        const used=new Set(),items=names.map(name=>{const index=originalSteps.findIndex((step,i)=>!used.has(i)&&step.title===name);if(index<0)return{title:name,key:null};used.add(index);return{title:name,key:originalSteps[index].key};});
        items.forEach((step,index)=>{if(step.key)return;const old=originalSteps[index];if(old&&!used.has(index)){used.add(index);step.key=old.key;}else step.key=':step:added-'+Date.now().toString(36)+'-'+Math.random().toString(36).slice(2,9);});
        next.checklistRevisions=[...(original.checklistRevisions||[]).filter(revision=>revision.from!==checklistDate),{from:checklistDate,items}].sort((a,b)=>a.from.localeCompare(b.from));
        const current=Object.assign({},readCanonical(),scope.getState()),prefix=original.id+':';let stateChanged=false;
        if(!items.length){
          current.completed={...current.completed};current.completedOn={...current.completedOn};
          const dates=new Set([checklistDate,...Object.keys(current.completed).filter(key=>key.startsWith(prefix)).map(key=>key.slice(prefix.length,prefix.length+10)).filter(day=>/^\d{4}-\d{2}-\d{2}$/.test(day)&&day>=checklistDate)]);
          dates.forEach(day=>{if(!scope.hasSubtasks(original,day))return;const key=scope.occurrenceKey(original,day),done=scope.isDone(original,day,current.completed);current.completed[key]=done;const finished=Array.from({length:scope.subtaskCount(original,day)},(_,i)=>current.completedOn[scope.childKey(original,day,i)]).filter(Boolean).sort().pop();if(done&&finished)current.completedOn[key]=finished;else if(!done)delete current.completedOn[key];stateChanged=true;});
        }
        const active=current.inProgress;if(active&&active.startsWith(prefix)&&active.slice(prefix.length,prefix.length+10)>=checklistDate){const activeSuffix=active.slice(prefix.length+10);if(activeSuffix&&!items.some(step=>step.key===activeSuffix)){current.inProgress=null;stateChanged=true;}}
        if(stateChanged)persist(current);

      }
      if (isNew) scope.tasks.push(next); else scope.tasks[scope.tasks.findIndex(t => t.id === task.id)] = next;
      saveDefinition(next, isNew); saveManualLinks(next.id, readLinks()); close();
    }, 'btn btn-primary'));
    form.append(buttons); title.focus();
  }
  function updateContextOptions() {
    const select = document.getElementById('tl-context-filter'); if (!select) return;
    const wanted = ['', ...allContexts()]; const existing = Array.from(select.options).map(o => o.value);
    if (JSON.stringify(wanted) !== JSON.stringify(existing)) {
      select.replaceChildren(); wanted.forEach(label => { const option = node('option', '', label?contextIcon(label):'All contexts'); option.value = label; select.append(option); });
    }
    select.value = selectedContext;
  }
  function decorateRow(wrapper, task, item, completed) {
    wrapper.dataset.taskId = task.id; wrapper.dataset.stateDate = item.stateDate; wrapper.dataset.displayDate = item.date;
    const text = wrapper.querySelector('.tm-task-details') || wrapper.querySelector('.form-check-label') || wrapper.querySelector('.tm-future-content');
    const labels = derivedContexts(task);
    if (text && labels.length) { const badges = node('span', 'tl-context-badges'); labels.forEach(label => {const icon=node('span','tl-context-icon',contextIcon(label));icon.setAttribute('aria-label',label);icon.title=label;badges.append(icon);}); text.append(badges); }
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
      const completionKeys = scope.hasSubtasks(task,item.stateDate) ? Array.from({ length: scope.subtaskCount(task, item.stateDate) }, (_, i) => scope.childKey(task, item.stateDate, i)) : [scope.occurrenceKey(task, item.stateDate)];
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
    if(native&&native.updateReminders){try{native.updateReminders(JSON.stringify(scope.tasks));}catch(error){console.error('Could not update task reminders',error);}}
    if (native && native.updateSnapshot) { try { native.updateSnapshot(JSON.stringify(snapshot)); } catch (error) { console.error('Could not update the home widget', error); } }
  }
  function applyTheme(){return global.TasksTheme.apply(readCanonical(),native);}
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
    const reminders=node('section','tl-notification-settings');reminders.append(node('h3','','Notifications'));
    const reminderSettings=readCanonical().reminders||{};
    const dailyLabel=node('label','tl-context-choice'),daily=node('input');daily.type='checkbox';daily.checked=reminderSettings.daily!==false;daily.setAttribute('aria-label','Daily reminder');dailyLabel.append(daily,node('span','','Daily reminder'));reminders.append(dailyLabel);
    const dailyTime=inputField(reminders,'Daily reminder time','time',reminderSettings.dailyTime||'09:00');dailyTime.setAttribute('aria-label','Daily reminder time');
    const timedLabel=node('label','tl-context-choice'),timed=node('input');timed.type='checkbox';timed.checked=reminderSettings.scheduled!==false;timed.setAttribute('aria-label','Scheduled task reminders');timedLabel.append(timed,node('span','','Scheduled task reminders'));reminders.append(timedLabel);
    reminders.append(node('p','text-muted','Only tasks with a set time and Notify checked send scheduled reminders. Android may delay reminders in power-saving mode.'));
    const reminderMessage=node('p','text-muted');if(native&&native.notificationStatus)reminderMessage.textContent=native.notificationStatus();
    reminders.append(action('Save reminders',()=>{if(daily.checked&&!dailyTime.value){reminderMessage.textContent='Choose a daily reminder time.';return;}persist({reminders:{daily:daily.checked,dailyTime:dailyTime.value||'09:00',scheduled:timed.checked}});queueSnapshot();reminderMessage.textContent='Reminder settings saved';if((daily.checked||timed.checked)&&native&&native.requestNotificationPermission)native.requestNotificationPermission();}));
    if(native&&native.openNotificationSettings)reminders.append(action('Android notification settings',()=>native.openNotificationSettings()));reminders.append(reminderMessage);form.append(reminders);

    global.TasksTheme.renderSettings(form,{readState:readCanonical,persistState:persist,native});
    const contexts=node('section','tl-context-settings');contexts.append(node('h3','','Contexts'));
    const list=node('p','text-muted');const refresh=()=>{list.textContent=allContexts().map(label=>contextIcon(label)+' '+label).join(' · ');};refresh();contexts.append(list);
    const custom=inputField(contexts,'New context','text','');custom.placeholder='Context name';custom.setAttribute('aria-label','New context');
    contexts.append(action('Add context',()=>{const labels=custom.value.split(',').map(x=>x.trim()).filter(Boolean);if(!labels.length)return;persist({contextOptions:[...new Set([...(readCanonical().contextOptions||[]),...labels])]});custom.value='';refresh();updateContextOptions();}));form.append(contexts);

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
        showToday(){scope.showView('today');},
        isInteracting(){return scope.isInteracting();},
        addTask(prefill) { try { if (typeof prefill === 'string') prefill = JSON.parse(prefill || '{}'); } catch (_) { prefill = {}; } editTask(null, null, prefill || {}); },
        setContextFilter(label) { selectedContext = typeof label === 'string' ? label : ''; saveExtras(); rebuild(); },
        reloadFromNative() { cached = null;lastCanonical=null; const latest = readCanonical(); scope.setState(latest); applyDefinitions(latest); selectedContext = typeof latest.uiContext === 'string' ? latest.uiContext : ''; applyTheme(); rebuild(); },
        debugSummary() { return { definitions: scope.tasks.length, savedKeys: Object.keys(scope.getState().completed).length, moves: Object.keys(scope.getState().movedTo).length, active: scope.getState().inProgress, today: scope.today, contexts: allContexts(), snapshot: global.TaskListBridge.lastSnapshot.length }; }
      };
      queueSnapshot();
    }
  };
})(window);
