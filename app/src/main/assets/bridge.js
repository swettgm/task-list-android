/* Generic Android integration. Personal task definitions and history are supplied
   privately in the installed document; this file contains no personal task data. */
(function (global) {
  'use strict';
  const KEY = 'task-list-20261020-v1';
  const DEFAULT_CONTEXTS = ['At home', 'On the road', 'On the phone', 'On the computer', 'Outdoors', 'Guild'];
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
  const GUILD_ICON_TOKEN='image:guild';
  const GUILD_ICON_DATA='data:image/png;base64,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';
  const contextIcon=label=>{const stored=readCanonical().contextIcons||{},defaults={'At home':'🏠','On the road':'🚗','On the phone':'📱','On the computer':'💻','Outdoors':'🌳','Guild':GUILD_ICON_TOKEN};if(Object.prototype.hasOwnProperty.call(stored,label)&&typeof stored[label]==='string'&&stored[label].trim())return stored[label];return Object.prototype.hasOwnProperty.call(defaults,label)?defaults[label]:'🏷️';};
  function contextGlyph(label,className='') {
    const glyph=node('span',className),value=contextIcon(label);
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
  function saveDefinition(task, isNew) {
    const state = clone(readCanonical());
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
    scope.tasks.forEach(task=>{task.priority=task.urgent?'HIGH':task.priority==='HIGHEST'?'HIGH':task.priority==='LOWEST'?'LOW':['HIGH','MEDIUM','LOW','UNSET'].includes(task.priority)?task.priority:'UNSET';});
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
    const close = () => { for(const animation of dialog.getAnimations?.()||[])animation.cancel();if (dialog.open && dialog.close) dialog.close(); dialog.remove(); };
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
      check.checked = (selected || []).includes(label); row.setAttribute('aria-label',label);row.title=label;row.append(check, contextGlyph(label)); grid.append(row);
      return { label, check };
    });
    group.append(grid);
    form.append(group);
    return () => inputs.filter(x => x.check.checked).map(x => x.label);
  }

  function frequencyFields(form,original,initialDate,date){
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
      form.append(action('Clear',()=>{time.value='';sync();},'btn tl-clear-time'));
      readChange=next=>{if(time.value){const [h,m]=time.value.split(':').map(Number);next.time=(h%12||12)+':'+String(m).padStart(2,'0')+(h>=12?' PM':' AM');}else delete next.time;if(time.value&&notify.checked)next.notify=true;else delete next.notify;};
    }else if(component==='duration'){
      const row=node('div','tl-duration-row');form.append(row);const hours=inputField(row,'Hours','number',original.minutes===undefined?'':Math.floor(original.minutes/60)),minutes=inputField(row,'Minutes','number',original.minutes===undefined?'':original.minutes%60);hours.min=minutes.min='0';hours.step=minutes.step='1';minutes.max='59';hours.setAttribute('aria-label','Hours');minutes.setAttribute('aria-label','Minutes');
      readChange=next=>{if((hours.value&&!/^\d+$/.test(hours.value))||(minutes.value&&!/^\d+$/.test(minutes.value))||Number(minutes.value)>59||((hours.value||minutes.value)&&(!Number.isSafeInteger(Number(hours.value)*60+Number(minutes.value))||Number(hours.value)*60+Number(minutes.value)<1)))throw Error('Enter hours and 0–59 minutes.');if(hours.value||minutes.value)next.minutes=Number(hours.value)*60+Number(minutes.value);else delete next.minutes;};
    }else{
      const initialDate=item?item.date:scope.today,date=inputField(form,'Starts','date',initialDate);date.setAttribute('aria-label','Repeat start date');
      const fields=frequencyFields(form,original,initialDate,date),{recurrence,amount,unit,weekChecks}=fields;
      const signature=()=>JSON.stringify([recurrence.value,date.value,recurrence.value==='repeat'?[amount.value,unit.value]:null,recurrence.value==='weekdays'||recurrence.value==='repeat'&&unit.value==='week'?weekChecks.map(c=>c.checked):null]),initialSignature=signature();
      readChange=next=>{
        const parsed=new Date(date.value+'T12:00:00Z');if(!/^\d{4}-\d{2}-\d{2}$/.test(date.value)||!Number.isFinite(parsed.getTime())||parsed.toISOString().slice(0,10)!==date.value)throw Error('Choose a valid date.');
        const chosenDays=weekChecks.flatMap((check,index)=>check.checked?[index]:[]);
        if(recurrence.value==='weekdays'&&!chosenDays.length)throw Error('Select at least one weekday.');
        if(recurrence.value==='repeat'&&(!/^\d+$/.test(amount.value)||!Number.isSafeInteger(Number(amount.value))||Number(amount.value)<1))throw Error('Repeat amount must be at least 1.');
        if(signature()===initialSignature)return;
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
  function editTask(task, item, prefill) {
    if (!scope) return;
    const isNew = !task; const original = task ? clone(task) : {};
    const { dialog, form, close } = modal(isNew ? 'Add task' : 'Edit task');
    form.classList.add('tl-task-form');
    const title = inputField(form, 'Task', 'text', original.title || (prefill && prefill.title) || ''); title.required = true; title.placeholder = 'What needs doing?';
    const priority = choiceField(form, 'Priority', scope.groups.map(([key, label]) => [key, key==='UNSET'?'Not set':label.replace(' priority', '')]), original.priority || 'UNSET');
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
    const {oldSpec,oldMode,recurrence,amount,unit,weekChecks}=frequencyFields(form,original,initialDate,date);
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
      const next = Object.assign({}, original, { id: original.id || 'app-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2,9), title: title.value.trim(), priority: urgent.checked ? 'HIGH' : priority.value, _contexts: readContexts() });
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
    form.append(buttons); if(isNew)title.focus();else dialog.focus({preventScroll:true});
  }
  function updateContextOptions() {
    const select = document.getElementById('tl-context-filter'); if (!select) return;
    const wanted = ['', ...allContexts()]; const existing = Array.from(select.options).map(o => o.value);
    if (JSON.stringify(wanted) !== JSON.stringify(existing)) {
      select.replaceChildren(); wanted.forEach(label => { const option = node('option', '', label?(contextIcon(label)===GUILD_ICON_TOKEN?label:contextIcon(label)):'All contexts'); option.value = label; select.append(option); });
    }
    select.value = selectedContext;
    const preview=document.getElementById('tl-context-filter-preview');if(preview){const image=selectedContext&&contextIcon(selectedContext)===GUILD_ICON_TOKEN;preview.replaceChildren();preview.hidden=!image;select.classList.toggle('tl-image-filter',!!image);if(image)preview.append(contextGlyph(selectedContext));}
  }
  function changePriority(task,offset) {
    const levels=scope.groups.map(([key])=>key);const found=levels.indexOf(task.priority||'UNSET');const current=found<0?levels.length-1:found;const target=Math.max(0,Math.min(levels.length-1,current+offset));if(target===current)return false;
    const next={...clone(task),priority:levels[target]};if(next.priority!=='HIGH')delete next.urgent;
    const status=document.getElementById('pt-status');
    try{saveDefinition(next,false);if(status)status.textContent='';return true;}
    catch(error){cached=null;lastCanonical=null;const state=readCanonical();scope.setState(state);applyDefinitions(state);rebuild();if(status)status.textContent='Couldn’t change priority. Please try again.';return false;}
  }
  let priorityHoldActive=false,priorityHoldUntil=0,priorityHoldCancel=null;
  const releasePriorityHold=()=>{if(priorityHoldCancel)priorityHoldCancel();if(priorityHoldActive){priorityHoldActive=false;priorityHoldUntil=performance.now()+450;}};
  document.addEventListener('pointerup',releasePriorityHold,true);document.addEventListener('pointercancel',releasePriorityHold,true);global.addEventListener('blur',releasePriorityHold);
  document.addEventListener('visibilitychange',()=>{if(document.hidden)releasePriorityHold();});
  document.addEventListener('click',event=>{if(event.target.closest?.('.tl-priority-button')&&(priorityHoldActive||performance.now()<priorityHoldUntil)){event.preventDefault();event.stopImmediatePropagation();}},true);
  function priorityButton(task,direction,offset,label,rank,levels){
    let timer=null,start=null,held=false,cancelled=false;
    const clear=()=>{if(timer!==null){clearTimeout(timer);timer=null;}if(priorityHoldCancel===clear)priorityHoldCancel=null;};
    const button=action('',event=>{if(held||cancelled||priorityHoldActive||performance.now()<priorityHoldUntil){event.preventDefault();return;}clear();changePriority(task,offset);},'tm-move-control tl-priority-button tl-priority-'+direction);
    const arrow=node('span','tl-priority-arrow','➜');arrow.setAttribute('aria-hidden','true');button.append(arrow);button.setAttribute('aria-label',label+' priority for '+task.title);button.title=label+' priority · Hold for '+(offset<0?'High':'Low');button.disabled=direction==='up'?rank===0:rank===levels.length-1;
    button.addEventListener('pointerdown',event=>{
      if(button.disabled||event.button!==0||event.isPrimary===false)return;
      if(priorityHoldCancel)priorityHoldCancel();held=false;cancelled=false;start={x:event.clientX,y:event.clientY};priorityHoldCancel=clear;
      timer=setTimeout(()=>{timer=null;if(!button.isConnected||button.disabled)return;held=true;priorityHoldActive=true;priorityHoldUntil=performance.now()+450;
        if(changePriority(task,(offset<0?0:levels.indexOf('LOW'))-rank)){try{if(native&&native.priorityHaptic)native.priorityHaptic();}catch(_){}}
      },450);
    },{passive:true});
    button.addEventListener('pointermove',event=>{if(start&&Math.hypot(event.clientX-start.x,event.clientY-start.y)>10){cancelled=true;clear();}},{passive:true});
    button.addEventListener('pointerleave',()=>{cancelled=true;clear();});button.addEventListener('pointercancel',()=>{cancelled=true;clear();});button.addEventListener('pointerup',clear);
    button.addEventListener('contextmenu',event=>event.preventDefault());return button;
  }
  function decorateRow(wrapper, task, item, completed) {
    wrapper.dataset.taskId = task.id; wrapper.dataset.stateDate = item.stateDate; wrapper.dataset.displayDate = item.date;
    const text = wrapper.querySelector('.tm-task-details') || wrapper.querySelector('.form-check-label') || wrapper.querySelector('.tm-future-content');
    const labels = derivedContexts(task);
    if (text && labels.length) { const badges = node('span', 'tl-context-badges'); labels.forEach(label => {const icon=action('',()=>editComponent(task,item,'labels'),'tl-context-icon tl-label-edit');icon.append(...contextGlyph(label).childNodes);icon.setAttribute('aria-label',label);icon.setAttribute('aria-haspopup','dialog');icon.title='Edit labels · '+label;badges.append(icon);}); const separator=node('span','tl-label-separator','\u00a0·\u00a0');separator.setAttribute('aria-hidden','true');(text.querySelector('.tm-meta')||text).append(separator,badges); }
    const controls = wrapper.querySelector('.tm-item-actions');
    if (controls) {
      const edit = action('⚙', () => editTask(task, item), 'tm-move-control tl-edit-button');
      edit.setAttribute('aria-label', 'Edit ' + task.title); edit.title = 'Edit task';
      const tools=node('span','tl-priority-tools'),levels=scope.groups.map(([key])=>key),rank=levels.indexOf(task.priority||'UNSET');
      [['up',-1,'Raise'],['down',1,'Lower']].forEach(([direction,offset,label])=>{
        tools.append(priorityButton(task,direction,offset,label,rank,levels));
      });tools.append(edit);controls.append(tools);
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
        time:task.time||'', urgent:!!task.urgent, labels: derivedContexts(task), priority: task.priority || 'UNSET', minutes: Number(task.minutes) || 0,
        createdAt: Date.parse(item.stateDate + 'T12:00:00Z') || 0,
        completionKeys, progressKey: key, stateDate: item.stateDate, onToday: !!onToday,
        recurrence: task.recurrence || '', source: 'html', snapshotDate:scope.today, recurringDaily:task.repeat?(task.repeat.unit==='day'&&Number(task.repeat.amount)===1&&!task.repeat.weekdays?.length):(task.recurrence==='Daily'||!!(task.days&&task.days.length===7)), skipCarryDays:task.skipCarryDays||[]
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
    const title = inputField(form, 'Task', 'text', prefill.title || ''); title.placeholder = 'What needs doing?';
    const error = node('p', 'tl-form-error'); error.setAttribute('role', 'alert'); form.append(error);
    const labels = prefill.labels || (selectedContext ? [selectedContext] : []);let flash=null;
    const savedFlash=()=>{if(flash)flash.cancel();if(!dialog.animate)return;const shadow=getComputedStyle(dialog).boxShadow,base=shadow==='none'?'':shadow+', ',reduced=global.matchMedia('(prefers-reduced-motion: reduce)').matches;const widths=reduced?[1,0]:[0,1,3,1,3,0];flash=dialog.animate(widths.map(width=>({boxShadow:base+'inset 0 0 0 '+width+'px rgba(255,255,255,'+(width?1:0)+')'})),{duration:reduced?180:800,easing:'linear'});flash.finished.catch(()=>{});};
    const save = () => {
      if (!title.value.trim()) { error.textContent = 'Enter a task title.'; title.focus(); return; }
      const next = { id:'app-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2,9),
        title:title.value.trim(), priority:'UNSET', due:prefill.dueDate || scope.today, _contexts:labels };
      try{saveDefinition(next,true);title.value='';error.textContent='';title.focus();savedFlash();}catch(e){if(flash)flash.cancel();cached=null;lastCanonical=null;const state=readCanonical();scope.setState(state);applyDefinitions(state);rebuild();error.textContent='Couldn’t add this task. Please try again.';title.focus();}
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
    const list=node('p','text-muted');const refresh=()=>{list.replaceChildren();allContexts().forEach((label,index)=>{if(index)list.append(document.createTextNode(' · '));list.append(contextGlyph(label),document.createTextNode(' '+label));});};refresh();contexts.append(list);
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
    const actions = node('div', 'tl-dialog-actions'); actions.append(action('Close', close, 'btn')); form.append(actions);
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
    const button=action('',fn,className),glyph=node('span','tl-plus-glyph');glyph.setAttribute('aria-hidden','true');button.append(glyph);button.setAttribute('aria-label',label);button.title=label;return button;
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
        const task={id:'app-'+Date.now().toString(36)+'-'+Math.random().toString(36).slice(2,9),title:record.title,priority:'UNSET',due:record.date&&record.date>scope.today?record.date:scope.today,_contexts:[]};
        if(record.notes)task.notes=record.notes;if(record.minutes!==undefined)task.minutes=record.minutes;
        record.completed=true;record.completedAt=stamp;record.convertedTaskId=task.id;projects[record.id]=record;
        persist({projects,customTasks:[...(readCanonical().customTasks||[]),task]});
      }else{projects[record.id]=record;persist({projects});}
    }
    rebuild();
  }
  function projectTrashIcon(){
    const ns='http://www.w3.org/2000/svg',icon=document.createElementNS(ns,'svg'),path=document.createElementNS(ns,'path');icon.setAttribute('viewBox','0 0 24 24');icon.setAttribute('width','18');icon.setAttribute('height','18');icon.setAttribute('fill','none');icon.setAttribute('stroke','currentColor');icon.setAttribute('stroke-width','1.8');icon.setAttribute('stroke-linecap','round');icon.setAttribute('stroke-linejoin','round');icon.setAttribute('aria-hidden','true');icon.setAttribute('focusable','false');path.setAttribute('d','M3 6h18M9 6V4h6v2M5 6l1 14h12l1-14M10 10v6M14 10v6');icon.append(path);return icon;
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
    if(!isNew){let armed=false;const trash=action('',()=>{if(!armed){armed=true;trash.append(node('span','','Are you sure?'));trash.classList.add('tl-project-delete-armed');trash.setAttribute('aria-label','Confirm delete project');return;}save('delete');},'btn tl-project-delete');trash.append(projectTrashIcon());trash.setAttribute('aria-label','Delete project');form.addEventListener('input',()=>{armed=false;trash.replaceChildren(projectTrashIcon());trash.classList.remove('tl-project-delete-armed');trash.setAttribute('aria-label','Delete project');});actions.append(trash);
      actions.append(action(original.completed?'Reopen':'Complete',()=>save(original.completed?'reopen':'complete'),'btn'));
      const make=action(original.convertedTaskId?'Task already created':'Make task',()=>save('task'),'btn');make.disabled=!!original.convertedTaskId;actions.append(make);
    }
    actions.append(action('Cancel',close),action('Save',()=>save('save'),'btn btn-primary'));form.append(actions);form.addEventListener('submit',event=>{event.preventDefault();save('save');});if(isNew)title.focus();else dialog.focus({preventScroll:true});
  }
  function showProjects(){
    if(projectPanel?.dialog.isConnected){projectPanel.dialog.focus({preventScroll:true});return;}
    const {dialog,form,close}=modal('Projects');dialog.classList.add('tl-projects');const heading=dialog.querySelector('h2'),header=node('div','tl-projects-header');heading.replaceWith(header);header.append(heading,plusButton(()=>editProject(),'Add project','btn tl-project-add'));
    const active=node('div','tl-project-list'),completed=node('div','tl-project-list tl-project-completed'),toggle=action('Show completed',()=>{projectsCompletedOpen=!projectsCompletedOpen;render();},'btn tl-project-toggle');toggle.setAttribute('aria-controls','tl-project-completed');completed.id='tl-project-completed';
    const error=node('p','tl-form-error');error.setAttribute('role','alert');form.append(active,toggle,completed,error,action('Back to tasks',close,'btn'));
    const row=project=>{const button=action('',()=>editProject(project),'tl-project-row');button.dataset.projectId=project.id;button.append(node('strong','tl-project-title',project.title));
      const parts=[project.date?new Date(project.date+'T12:00:00Z').toLocaleDateString(undefined,{month:'short',day:'numeric',year:'numeric',timeZone:'UTC'}):'Any date'];if(project.minutes!==undefined)parts.push(projectDuration(project.minutes));if(project.convertedTaskId)parts.push('Added to task list');button.append(node('span','tl-project-meta',parts.join(' · ')));if(project.notes)button.append(node('span','tl-project-note',project.notes));return button;};
    const render=()=>{try{const deleted=projectDeletions(),projects=Object.values(projectMap()).filter(p=>!deleted[p.id]),open=projects.filter(p=>!p.completed).sort((a,b)=>(a.date||'9999').localeCompare(b.date||'9999')||a.title.localeCompare(b.title,undefined,{sensitivity:'base',numeric:true})),done=projects.filter(p=>p.completed).sort((a,b)=>(b.completedAt||'').localeCompare(a.completedAt||'')||a.title.localeCompare(b.title));
        active.replaceChildren(...open.map(row));if(!open.length)active.append(node('p','text-muted','No projects yet.'));completed.replaceChildren(...done.map(row));if(!done.length)completed.append(node('p','text-muted','No completed projects.'));completed.hidden=!projectsCompletedOpen;toggle.textContent=projectsCompletedOpen?'Hide completed':'Show completed';toggle.setAttribute('aria-expanded',String(projectsCompletedOpen));error.textContent='';
      }catch(e){error.textContent=e.message;}};
    projectPanel={dialog,render};render();dialog.focus({preventScroll:true});
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
    root.querySelector('.tm-top').after(toolbar); root.append(add); updateContextOptions();bindProjectDate();
  }
  global.TaskListBridge = {
    editComponent,
    contextsFor: derivedContexts, contextMatches: matches, decorateRow, queueSnapshot, lastSnapshot: [],
    attach(context) {
      scope = context;
      if(readCanonical().guildIconVersion!==1){const current=readCanonical(),guild=(current.taskOverrides||{}).guild||scope.tasks.find(task=>task.id==='guild'),overrides={...(current.taskOverrides||{})};if(guild)overrides.guild={...guild,_contexts:[...new Set([...derivedContexts(guild),'Guild'])]};persist({...scope.getState(),guildIconVersion:1,taskOverrides:overrides,contextIcons:{...(current.contextIcons||{}),Guild:GUILD_ICON_TOKEN}});}
      const state = readCanonical(); selectedContext = typeof state.uiContext === 'string' ? state.uiContext : '';
      applyDefinitions(state); applyTheme(); addToolbar();
      global.TaskListApp = {
        quickAddTask, handleBack,showProjects,
        showToday(){scope.showView('today');},
        isInteracting(){return scope.isInteracting()||!!document.querySelector('dialog[open]');},
        addTask(prefill) { try { if (typeof prefill === 'string') prefill = JSON.parse(prefill || '{}'); } catch (_) { prefill = {}; } editTask(null, null, prefill || {}); },
        setContextFilter(label) { selectedContext = typeof label === 'string' ? label : ''; saveExtras(); rebuild(); },
        reloadFromNative() { cached = null;lastCanonical=null; const latest = readCanonical(); scope.setState(latest); applyDefinitions(latest); selectedContext = typeof latest.uiContext === 'string' ? latest.uiContext : ''; applyTheme(); rebuild(); },
        debugSummary() { return { definitions: scope.tasks.length, savedKeys: Object.keys(scope.getState().completed).length, moves: Object.keys(scope.getState().movedTo).length, active: scope.getState().inProgress, today: scope.today, contexts: allContexts(), snapshot: global.TaskListBridge.lastSnapshot.length }; }
      };
      queueSnapshot();
    }
  };
})(window);
