// Behavior checks against the exact offline APK document with Android persistence mocked.
import { chromium } from 'playwright';
import { readFile, mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import { pathToFileURL } from 'node:url';
const documentPath = path.resolve(process.argv[2] || 'app/src/main/assets/www/index.html');
const seedPath = path.resolve(process.argv[3] || path.join(path.dirname(documentPath), 'initial-state.json'));
const seed = JSON.parse(await readFile(seedPath, 'utf8'));
const output = path.resolve(process.env.TASK_SCREENSHOT_DIR || 'build/browser-qa');
await mkdir(output, { recursive: true });
const executablePath = process.env.CHROME_BIN || ['/usr/bin/google-chrome', '/usr/bin/chromium', '/opt/google/chrome/chrome'].find(existsSync);
const browser = await chromium.launch({ headless: true, executablePath, args: ['--no-sandbox', '--disable-dev-shm-usage'] });
const page = await browser.newPage({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 1 });
const failures = [];
page.on('pageerror', error => failures.push(error.message));
await page.addInitScript(initial => {
  let canonical = JSON.parse(JSON.stringify(initial));
  // Simulate an upgrade with old suggested pairs still in saved data.
  canonical.pairOverrides = { credit:['fix-credit'] };
  window.__nativeQA = { snapshot: [], failWrites:false, widgetOpened:false, haptics:0,
    setState(value) { canonical = JSON.parse(JSON.stringify(value)); },
    getState() { return JSON.parse(JSON.stringify(canonical)); } };
  window.TaskListNative = {
    hideKeyboard() { __nativeQA.keyboardDismissals=(__nativeQA.keyboardDismissals||0)+1; },
    actionHaptic() { __nativeQA.haptics++; },
    readState() { return JSON.stringify(canonical); },
    writeState(text) { if (__nativeQA.failWrites) throw Error('Simulated storage failure'); canonical = JSON.parse(text); },
    updateSnapshot(text) { window.__nativeQA.snapshot = JSON.parse(text); },
    setThemeColors(color) { window.__nativeQA.background=color; },
    showWidgetSettings() { window.__nativeQA.widgetOpened=true; }
  };
}, seed);
await page.goto(pathToFileURL(documentPath).href);
await page.waitForFunction(() => !!window.TaskListApp && window.__nativeQA.snapshot.length > 0);
await page.screenshot({ path: path.join(output, 'today-dark.png'), fullPage: false });
await page.getByRole('button',{name:'Add task',exact:true}).click();
await page.getByRole('button',{name:'Details',exact:true}).click();
await page.screenshot({path:path.join(output,'compact-editor.png'),fullPage:false});
await page.getByRole('button',{name:'Cancel',exact:true}).click();
const result = await page.evaluate(async () => {
  const checks = [];
  function check(condition, label) { if (!condition) throw new Error(label); checks.push(label); }
  const tick = () => new Promise(resolve => setTimeout(resolve, 40));
  const rows = () => [...document.querySelectorAll('#pt-list .tm-task-item[data-task-id]')];
  const rowFor = (id, date) => rows().find(row => row.dataset.taskId === id && (!date || row.dataset.stateDate === date));
  const clickText = (root, text) => { const button=[...root.querySelectorAll('button')].find(b=>b.textContent===text);check(!!button,'Control exists: '+text);button.click(); };
  const original = TaskListApp.debugSummary();
  const topControls=[document.querySelector('#tl-context-filter'),document.querySelector('.tl-settings'),...document.querySelectorAll('.nav-link')];
  check(topControls.every(control=>Math.abs(control.getBoundingClientRect().height-48)<1),'Header dropdown and buttons have matching 48px heights');
  check(document.querySelector('#tl-tools').children.length===2&&!document.querySelector('.tl-widget'),'Top bar contains only the larger context dropdown and Settings');
  const add=document.querySelector('.tl-add-task');
  check(!!add.querySelector('.tl-plus-glyph')&&getComputedStyle(add).position==='fixed'&&add.getBoundingClientRect().height===56,'Add task is a large corner plus button');
  document.querySelector('.tl-settings').click();
  check(document.querySelector('dialog h2').textContent==='Settings','Settings opens from the gear');
  clickText(document.querySelector('dialog'),'Add home-screen widget');
  check(__nativeQA.widgetOpened&&!document.querySelector('dialog'),'Widget setup is available inside Settings');
  check(original.definitions === 102 && original.savedKeys >= 304 && original.moves >= 26, 'Original tasks, saved history, and moves retained');
  check(!!document.querySelector('#pt-date').textContent.trim() && !document.querySelector('h1'), 'Date-only heading');
  check(getComputedStyle(document.body).backgroundColor === 'rgb(16, 21, 31)', 'Dark page background');
  check(document.querySelector('#pt-upcoming').hidden && document.querySelector('#pt-completed').hidden&&document.querySelector('#pt-toggle-hidden').textContent==='Show completed', 'Today opens with a Show completed button without a count');
  check(!document.querySelector('#pt-save,#pt-copy,#pt-backup,#pt-copy-full,#pt-copy-box,.tl-export'), 'Manual completion, copy, and backup controls removed');
  check(!document.querySelector('.tm-ai-note,.tm-pair-note,.tm-linked-chain'), 'AI tips and original suggested links are absent');
  check([...document.querySelectorAll('.tm-children')].every(item => !item.open), 'Subtask checklists start collapsed');
  check(rows().every(row=>[...row.querySelectorAll(':scope > .tm-item-actions button')].every(b=>['Do now','Do tomorrow ➜','➜','⚙'].includes(b.textContent))), 'Task rows show only scheduling, priority arrows, and gear controls');
  check(rows().every(row=>row.querySelector('.tl-edit-button')?.getAttribute('aria-label').startsWith('Edit ')), 'Gear buttons have accessible task names');
  TaskListApp.setContextFilter('At home'); await tick();
  check(rows().every(row => [...row.querySelectorAll('.tl-context-icon')].some(badge => badge.getAttribute('aria-label') === 'At home')), 'Context filter works');
  TaskListApp.setContextFilter(''); await tick();
  {const isolated=__nativeQA.getState();isolated.inProgress=null;isolated.inProgressTasks=[];__nativeQA.setState(isolated);TaskListApp.reloadFromNative();await tick();}
  const first = rows().find(row => !row.querySelector('.tm-progress-control').disabled);
  const firstId=first.dataset.taskId,firstDate=first.dataset.stateDate;
  first.querySelector('.tm-progress-control').click(); await tick();
  check(TaskListApp.debugSummary().active===firstId+':'+firstDate,'Do now selects the task');
  rowFor(firstId,firstDate).querySelector('.tm-progress-control').click();await tick();
  TaskListApp.reloadFromNative();await tick();
  check(TaskListApp.debugSummary().active===null,'Do now toggles off and stays off');
  const repeating=rows().filter(row=>row.querySelector('.tm-meta').textContent.includes('🔁'));
  check(repeating.length>0&&repeating.every(row=>![...row.querySelectorAll('button')].some(b=>b.textContent==='Do tomorrow ➜')),'Recurring tasks retain their schedule');
  const movable=rows().find(row=>[...row.querySelectorAll('button')].some(b=>b.textContent==='Do tomorrow ➜'));
  const movedId=movable.dataset.taskId,movedDate=movable.dataset.stateDate;
  clickText(movable,'Do tomorrow ➜');await tick();
  check(!!__nativeQA.getState().movedTo[movedId+':'+movedDate]&&!rowFor(movedId,movedDate),'Do tomorrow saves the move and removes the current row');
  const parent=rows().find(row=>row.querySelector('.tm-children'));
  check(!!parent,'A task with a checklist is available');
  const parentId=parent.dataset.taskId,parentDate=parent.dataset.stateDate;
  parent.querySelector('.tm-progress-control').click();await tick();
  rowFor(parentId,parentDate).querySelector(':scope > .form-check > input').click();await tick();
  check(!!rowFor(parentId,parentDate)&&rowFor(parentId,parentDate).classList.contains('tm-completing'),'Checking a parent leaves a short completion confirmation before hiding');
  check(__nativeQA.snapshot.some(row=>row.id==='html:'+parentId+':'+parentDate&&row.done),'Automatic completion reaches the widget snapshot');
  check(TaskListApp.debugSummary().active===null,'Completing the active parent clears Do now');
  await new Promise(resolve=>setTimeout(resolve,2200));
  check(!rowFor(parentId,parentDate),'Completed parent disappears after the short delay');
  TaskListApp.reloadFromNative();await tick();
  check(!rowFor(parentId,parentDate),'Completion survives reload');
  document.querySelector('#pt-toggle-hidden').click();await tick();
  const hiddenParent=()=>[...document.querySelectorAll('#pt-completed-list [data-task-id]')].find(row=>row.dataset.taskId===parentId&&row.dataset.stateDate===parentDate);
  check(!document.querySelector('#pt-completed').hidden&&!!hiddenParent()&&document.querySelector('#pt-toggle-hidden').textContent==='Hide completed','Show completed reveals history and the button has no count');
  hiddenParent().querySelector(':scope > .form-check > input').click();await tick();
  check(!!rowFor(parentId,parentDate)&&!hiddenParent(),'Unchecking a hidden task saves and restores it');
  TaskListApp.reloadFromNative();await tick();check(!!rowFor(parentId,parentDate),'Reopened task survives reload');
  document.querySelector('#pt-toggle-hidden').click();await tick();
  const details=rowFor(parentId,parentDate).querySelector('.tm-children');details.open=true;await tick();
  details.querySelector('.tm-child-item input').click();await tick();
  check(rowFor(parentId,parentDate).querySelector('.tm-children').open,'Checklist stays open after checking a step');
  check(rowFor(parentId,parentDate).querySelector('.tm-child-item input').checked,'Individual step saves immediately');
  TaskListApp.reloadFromNative();await tick();
  check(rowFor(parentId,parentDate).querySelector('.tm-child-item input').checked,'Individual step survives reload');
  // A failed native save must keep the task available for retry.
  const before=JSON.stringify(__nativeQA.getState().completed);
  __nativeQA.failWrites=true;rowFor(parentId,parentDate).querySelector(':scope > .form-check > input').click();await tick();
  check(!!rowFor(parentId,parentDate)&&JSON.stringify(__nativeQA.getState().completed)===before,'Failed persistence does not lose or hide the task');
  check(document.querySelector('#pt-status').textContent.includes('Couldn’t save'),'Save failure is shown');__nativeQA.failWrites=false;
  // The native home-screen popup can add a task while the page is paused.
  const state=__nativeQA.getState();state.customTasks=[...(state.customTasks||[]),{id:'custom-native-qa',title:'Native popup task',priority:'UNSET',labels:['On the phone'],due:original.today,when:'Not set'},{id:'custom-completed-link-qa',title:'Completed picker fixture',priority:'UNSET',labels:[],due:original.today}];state.completed={...state.completed,['custom-completed-link-qa:'+original.today]:true};
  __nativeQA.setState(state);TaskListApp.setContextFilter('');TaskListApp.reloadFromNative();await tick();
  check(rows().some(row=>row.dataset.taskId==='custom-native-qa'),'Native popup task is retained and loaded');
  // Create with a manual link; the picker offers every existing task.
  TaskListApp.addTask({labels:['On the computer']});
  let dialog=document.querySelector('dialog');check(dialog.open,'Creation editor opens');check(document.activeElement===dialog&&__nativeQA.keyboardDismissals>0,'Detailed task creation keeps the keyboard collapsed until a field is selected');
  check(!dialog.textContent.includes('Useful help note'),'AI recommendation field is removed');
  check(JSON.stringify([...dialog.querySelectorAll('[data-field=priority] input[type=radio]')].map(n=>n.value))==='["HIGH","MEDIUM","LOW"]'&&!dialog.querySelector('[data-field=priority] input:checked')&&dialog.querySelectorAll('[data-field=repeat] input[type=radio]').length===5,'Priority offers only High, Medium and Low with no default selection; repeat remains compact');
  check(![...dialog.querySelectorAll('legend,label>span')].some(label=>/[()]/.test(label.textContent)),'Editor labels contain no parenthetical instructions');
  check(dialog.querySelector('input[placeholder="Any time"]')&&dialog.querySelector('input[aria-label=Hours]')&&dialog.querySelector('input[aria-label=Minutes]'),'Time and split duration use short placeholders');
  check(dialog.querySelector('input[type=time]')&&dialog.querySelector('input[type=date]'),'Native time and date pickers are available');
  check(!dialog.querySelector('details.tl-extra-fields')&&!dialog.querySelector('.tl-other-contexts'),'Notes stay expanded and custom context creation is outside the editor');
  const duration=dialog.querySelector('.tl-duration-group').getBoundingClientRect(),scheduleBox=dialog.querySelector('.tl-schedule-grid').getBoundingClientRect();
  check(Math.abs(duration.width-scheduleBox.width)<2,'Duration uses the full available editor width');
  const notesBox=dialog.querySelector('textarea[placeholder="Add notes"]').getBoundingClientRect(),stepsBox=dialog.querySelector('textarea[placeholder="One step per line"]').getBoundingClientRect();
  check(stepsBox.top>=notesBox.bottom,'Notes and checklist occupy separate full-width rows');
  clickText(dialog,'Tomorrow');check(dialog.querySelector('input[type=date]').value===new Date(Date.parse(original.today+'T12:00:00Z')+86400000).toISOString().slice(0,10),'Tomorrow shortcut chooses the next date');
  clickText(dialog,'Weekend');check(new Date(dialog.querySelector('input[type=date]').value+'T12:00:00Z').getUTCDay()===5,'Weekend shortcut chooses Friday');
  dialog.querySelector('input[type=date]').value=original.today;
  dialog.querySelector('input[type=text]').value='Browser-created task';
  dialog.querySelector('[data-field=priority] input[value=HIGH]').click();
  dialog.querySelector('input[aria-label=Hours]').value='0';dialog.querySelector('input[aria-label=Minutes]').value='17';
  dialog.querySelector('input[placeholder="Any time"]').value='09:00';
  dialog.querySelector('.tl-extra-fields').open=true;
  dialog.querySelector('textarea[placeholder="Add notes"]').value='Preserve these notes';
  dialog.querySelector('textarea[placeholder="One step per line"]').value='First step\nSecond step';
  check(dialog.querySelector('input[type=date]')&&dialog.querySelector('input[placeholder="Any time"]')&&dialog.querySelector('input[aria-label=Hours]')&&dialog.querySelector('input[aria-label=Minutes]'),'Date, time, hours, and minutes remain available together');
  const picker=dialog.querySelector('[aria-label="Choose a task to link"]');
  check([...picker.options].some(option=>option.value==='custom-native-qa'&&option.textContent==='Native popup task')&&![...picker.options].some(option=>option.value==='custom-completed-link-qa'),'Link dropdown offers open tasks by title and excludes completed tasks');
  picker.value='custom-native-qa';picker.dispatchEvent(new Event('change'));check(dialog.querySelector('.tl-linked-list').textContent.includes('Native popup task'),'Choosing a linked task immediately adds it to the selection without another button');
  clickText(dialog,'Save task');await tick();
  let created=()=>rows().find(row=>row.querySelector('.tm-title').textContent==='Browser-created task');
  check(!!created()&&created().querySelector('.tm-pair-note')?.textContent.includes('Native popup task'),'Manually linked task appears on the saved row');
  const createdId=created().dataset.taskId;
  const stored=__nativeQA.getState().customTasks.find(task=>task.id===createdId);
  check(stored.priority==='HIGH'&&stored.minutes===17&&stored.time==='9:00 AM'&&stored.notes==='Preserve these notes'&&stored.steps.length===2,'Compact editor preserves priority, duration, time, notes, and checklist steps');
  check(__nativeQA.getState().manualLinks['custom-native-qa'].includes(createdId),'Manual linking is bidirectional');
  TaskListApp.reloadFromNative();await tick();check(created().querySelector('.tm-pair-note')?.textContent.includes('Native popup task'),'Manual links survive reload');
  created().querySelector('.tl-edit-button').click();dialog=document.querySelector('dialog');
  check(document.activeElement===dialog,'Editing an existing task focuses the dialog instead of opening a text field');dialog.querySelector('input[type=text]').focus();check(document.activeElement===dialog.querySelector('input[type=text]'),'Existing task fields can still be focused intentionally');
  check(![...dialog.querySelectorAll('option')].some(o=>o.value===createdId),'Task cannot link to itself');
  check(dialog.querySelector('[data-field=priority] input[value=HIGH]').checked&&dialog.querySelector('input[aria-label=Minutes]').value==='17'&&dialog.querySelector('input[placeholder="Any time"]').value==='09:00','Saved values repopulate the compact editor');
  [...dialog.querySelectorAll('label.tl-context-choice')].find(l=>l.getAttribute('aria-label')==='On the road').querySelector('input').click();
  dialog.querySelector('.tl-unlink').click();clickText(dialog,'Save task');await tick();TaskListApp.reloadFromNative();await tick();
  check([...created().querySelectorAll('.tl-context-icon')].some(b=>b.getAttribute('aria-label')==='On the road'),'Contexts can be edited through the gear');
  check(!created().querySelector('.tm-pair-note')&&!(__nativeQA.getState().manualLinks['custom-native-qa']||[]).includes(createdId),'Unlink removes both sides and survives reload');
  created().querySelector(':scope > .form-check > input').click();await new Promise(resolve=>setTimeout(resolve,2200));
  check(!created()&&!!rowFor('custom-native-qa'),'Completion affects only the checked task');
  document.querySelector('#pt-upcoming-tab').click();check(!document.querySelector('#pt-upcoming').hidden&&document.querySelector('#pt-today').hidden,'Upcoming tab works');
  check(document.querySelectorAll('#pt-future-list .tm-day').length>0,'Upcoming remains grouped by date');
  document.querySelector('#pt-today-tab').click();
  TaskListApp.addTask();dialog=document.querySelector('dialog');
  dialog.querySelector('input[type=text]').value='Repeat choice QA';
  dialog.querySelector('[data-field=repeat] input[value=weekdays]').click();
  check(!dialog.querySelector('.tl-weekdays').hidden,'Weekday checkboxes appear only for weekday repeating');
  dialog.querySelectorAll('.tl-weekdays input').forEach(input=>input.checked=false);dialog.querySelector('input[aria-label=Monday]').click();dialog.querySelector('input[aria-label=Friday]').click();
  clickText(dialog,'Save task');await tick();
  let repeat=__nativeQA.getState().customTasks.find(task=>task.title==='Repeat choice QA');
  check(JSON.stringify(repeat.days)==='[1,5]','Weekday choices save the chosen repeat days');
  check(repeat.priority==='UNSET','Saving without a priority choice keeps the task in Priority not set');
  TaskListApp.addTask();dialog=document.querySelector('dialog');dialog.querySelector('input[type=text]').value='Interval choice QA';
  dialog.querySelector('[data-field=repeat] input[value=repeat]').click();
  const every=dialog.querySelector('.tl-repeat-amount'),unit=dialog.querySelector('.tl-repeat-unit');unit.value='day';unit.dispatchEvent(new Event('change'));check(every&&!every.closest('[hidden]')&&dialog.querySelector('.tl-weekdays').hidden,'Every amount and unit replace weekday choices');every.value='4';
  clickText(dialog,'Save task');await tick();
  repeat=__nativeQA.getState().customTasks.find(task=>task.title==='Interval choice QA');
  check(repeat.repeat?.amount===4&&repeat.repeat?.unit==='day'&&!!repeat.repeat.start,'Every N days remains available');
  TaskListApp.addTask();dialog=document.querySelector('dialog');dialog.querySelector('input[type=text]').value='Alternate weekends QA';
  dialog.querySelector('[data-field=repeat] input[value=repeat]').click();dialog.querySelector('.tl-repeat-amount').value='2';const weekly=dialog.querySelector('.tl-repeat-unit');weekly.value='week';weekly.dispatchEvent(new Event('change'));dialog.querySelectorAll('.tl-weekdays input').forEach(input=>input.checked=false);dialog.querySelector('input[aria-label=Sunday]').click();dialog.querySelector('input[aria-label=Saturday]').click();dialog.querySelector('.tl-urgent input').click();
  clickText(dialog,'Save task');await tick();repeat=__nativeQA.getState().customTasks.find(task=>task.title==='Alternate weekends QA');
  check(repeat.repeat?.amount===2&&repeat.repeat?.unit==='week'&&JSON.stringify(repeat.repeat.weekdays?.slice().sort())==='[0,6]'&&repeat.urgent&&repeat.priority==='HIGH','Every two weeks on selected weekend days and urgent options remain available');
  // Quick entry saves with the current context; Back dismisses without saving.
  TaskListApp.setContextFilter('On the phone');await tick();
  const quickHaptics=__nativeQA.haptics;document.querySelector('.tl-add-task').click();dialog=document.querySelector('dialog');check(__nativeQA.haptics===quickHaptics+1,'Add-task plus emits one light haptic');
  clickText(dialog,'Add');check(__nativeQA.haptics===quickHaptics+1,'An empty invalid add does not emit a saved-task haptic');
  check(dialog.querySelector('h2').textContent==='Quick add'&&dialog.querySelectorAll('input').length===1,'Corner plus opens the one-field quick-add form');
  dialog.querySelector('input').value='Quick entry QA';
  dialog.querySelector('form').dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));await tick();
  check(__nativeQA.haptics===quickHaptics+2,'Successful quick add emits exactly one additional light haptic');
  let quick=__nativeQA.getState().customTasks.find(task=>task.title==='Quick entry QA');
  check(quick&&quick.due===original.today&&quick._contexts.includes('On the phone')&&dialog.open&&dialog.querySelector('input').value===''&&document.activeElement===dialog.querySelector('input'),'Quick add saves and clears its title while keeping the popup and keyboard focus');
  const firstQuickBox=dialog.getBoundingClientRect();check(dialog.getAnimations().length>0,'Successful quick add flashes the popup border');
  dialog.querySelector('input').value='Second quick entry QA';clickText(dialog,'Add');await tick();check(__nativeQA.getState().customTasks.filter(task=>['Quick entry QA','Second quick entry QA'].includes(task.title)).length===2&&dialog.open&&dialog.querySelector('input').value==='','The same quick-add popup can save another distinct task');
  check(__nativeQA.getState().customTasks.find(task=>task.title==='Second quick entry QA')._contexts.length===0,'Batch quick add clears inherited labels after a successful save');
  const nextQuickBox=dialog.getBoundingClientRect();check(Math.abs(firstQuickBox.width-nextQuickBox.width)<1&&Math.abs(firstQuickBox.height-nextQuickBox.height)<1,'Border pulses do not resize the quick-add popup');clickText(dialog,'Cancel');
  document.querySelector('.tl-add-task').click();dialog=document.querySelector('dialog');dialog.querySelector('input').value='Keep this title';
  clickText(dialog,'Details');dialog=document.querySelector('dialog');
  check(dialog.querySelector('input[type=text]').value==='Keep this title','Quick add can open full details without losing the title');
  check(document.activeElement===dialog,'Switching from Quick add to Details removes text focus and collapses the keyboard');
  const dateHaptics=__nativeQA.haptics;const pickerDate=dialog.querySelector('input[type=date]');pickerDate.dispatchEvent(new Event('change'));check(__nativeQA.haptics===dateHaptics+1,'Confirming a date selection emits one light haptic');
  const countBeforeBack=TaskListApp.debugSummary().definitions;
  check(TaskListApp.handleBack()&&!document.querySelector('dialog')&&TaskListApp.debugSummary().definitions===countBeforeBack,'Android Back closes the full editor without saving a draft');
  document.querySelector('.tl-add-task').click();check(TaskListApp.handleBack()&&!document.querySelector('dialog'),'Android Back closes quick add');
  check(!TaskListApp.handleBack(),'Back reports when there is no menu to close');
  TaskListApp.setContextFilter('');await tick();
  // Hex colors persist and invalid input cannot replace saved colors.
  document.querySelector('.tl-settings').click();dialog=document.querySelector('dialog');
  let accent=dialog.querySelector('input[placeholder="#68B7FF"]'),background=dialog.querySelector('input[placeholder="#10151F"]');
  accent.value='#ff9933';background.value='#18232f';clickText(dialog,'Apply colors');
  check(__nativeQA.getState().uiTheme.accent==='#ff9933'&&__nativeQA.background==='#18232f','Hex settings save the accent and native app background');
  check(getComputedStyle(document.querySelector('.tl-add-task')).backgroundColor==='rgb(255, 153, 51)','Chosen accent colors the app buttons');
  check(getComputedStyle(document.body).backgroundColor==='rgb(24, 35, 47)','Chosen background colors the task screen');
  const selectionHaptics=__nativeQA.haptics;const pickerTime=dialog.querySelector('input[type=time]');pickerTime.value='10:15';pickerTime.dispatchEvent(new Event('change'));check(__nativeQA.haptics===selectionHaptics+1,'Confirming a time selection emits one light haptic');
  check(TaskListApp.handleBack(),'Android Back closes Settings');TaskListApp.reloadFromNative();await tick();
  check(document.documentElement.style.getPropertyValue('--tl-accent')==='#ff9933','Hex theme survives reload');
  document.querySelector('.tl-settings').click();dialog=document.querySelector('dialog');accent=dialog.querySelector('input[placeholder="#68B7FF"]');background=dialog.querySelector('input[placeholder="#10151F"]');
  accent.value='invalid';clickText(dialog,'Apply colors');
  check(!!dialog.querySelector('[role=alert]').textContent&&__nativeQA.getState().uiTheme.accent==='#ff9933','Invalid hex input is rejected without changing saved colors');
  accent.value='#123';background.value='#fff';clickText(dialog,'Apply colors');
  check(__nativeQA.getState().uiTheme.accent==='#112233'&&getComputedStyle(document.body).backgroundColor==='rgb(255, 255, 255)','Short hex input expands and light backgrounds are supported');
  clickText(dialog,'Reset colors');check(__nativeQA.getState().uiTheme.accent==='#68b7ff'&&__nativeQA.getState().uiTheme.background==='#10151f','Default colors can be restored');
  clickText(dialog,'Close');
  // Rapid completions and undo have independent presentation timers, while saving immediately.
  for(const title of ['Delay task A','Delay task B']){
    TaskListApp.quickAddTask();const quickDialog=document.querySelector('dialog');quickDialog.querySelector('input').value=title;clickText(quickDialog,'Add');await tick();clickText(quickDialog,'Cancel');
  }
  const byTitle=title=>rows().find(row=>row.querySelector('.tm-title').textContent===title);
  byTitle('Delay task A').querySelector(':scope > .form-check > input').click();await tick();
  byTitle('Delay task B').querySelector(':scope > .form-check > input').click();await tick();
  check(byTitle('Delay task A')?.classList.contains('tm-completing')&&byTitle('Delay task B')?.classList.contains('tm-completing'),'Rapidly completed tasks both remain briefly visible');
  const delayBId=byTitle('Delay task B').dataset.taskId;
  check(__nativeQA.snapshot.some(row=>row.id.startsWith('html:'+delayBId+':')&&row.done),'Delayed disappearance does not delay widget completion');
  byTitle('Delay task A').querySelector(':scope > .form-check > input').click();await tick();
  check(byTitle('Delay task A')&&!byTitle('Delay task A').classList.contains('tm-completing'),'Unchecking during the delay cancels disappearance');
  await new Promise(resolve=>setTimeout(resolve,2200));
  check(!!byTitle('Delay task A')&&!byTitle('Delay task B'),'Undo stays visible while the other completed task disappears');
  TaskListApp.addTask();dialog=document.querySelector('dialog');dialog.querySelector('input[type=text]').value='Delay checklist QA';
  dialog.querySelector('textarea[placeholder="One step per line"]').value='One\nTwo';clickText(dialog,'Save task');await tick();
  byTitle('Delay checklist QA').querySelectorAll('.tm-child-item input')[0].click();await tick();
  byTitle('Delay checklist QA').querySelectorAll('.tm-child-item input')[1].click();await tick();
  check(byTitle('Delay checklist QA')?.classList.contains('tm-completing'),'Finishing the last checklist step also delays parent disappearance');
  await new Promise(resolve=>setTimeout(resolve,2200));check(!byTitle('Delay checklist QA'),'Finished checklist disappears after the confirmation delay');
  {const state=__nativeQA.getState();state.customTasks.push({id:'summary-duration-a',title:'Summary duration A',due:original.today,priority:'UNSET',minutes:90,_contexts:['Summary duration QA']},{id:'summary-duration-b',title:'Summary duration B',due:original.today,priority:'UNSET',minutes:45,_contexts:['Summary duration QA']});__nativeQA.setState(state);TaskListApp.reloadFromNative();TaskListApp.setContextFilter('Summary duration QA');
   for(const [a,b,expected] of [[90,45,'2h 15m'],[15,0,'15m'],[60,0,'1h'],[135,0,'2h 15m'],[0,0,'0m']]){const state=__nativeQA.getState();state.customTasks.find(t=>t.id==='summary-duration-a').minutes=a;state.customTasks.find(t=>t.id==='summary-duration-b').minutes=b;__nativeQA.setState(state);TaskListApp.reloadFromNative();await tick();check(document.getElementById('pt-progress').textContent==='0/2 done · '+expected+' remaining','Summary retains the completion fraction and formats '+a+' + '+b+' minutes without a pending count');}
   const state=__nativeQA.getState();state.customTasks.push({id:'summary-untimed',title:'Summary untimed',due:original.today,priority:'UNSET',_contexts:['Summary duration QA']});__nativeQA.setState(state);TaskListApp.reloadFromNative();await tick();check(document.getElementById('pt-progress').textContent==='0/3 done · 0m remaining + 1 untimed','Untimed tasks remain visible in the compact summary');TaskListApp.setContextFilter('');
  }
  return {checks};
});
// Check real geometry, including expanded content and long task text, without masking overflow.
const layouts=[];
for(const width of [320,360,390,412,600,844]){
  await page.setViewportSize({width,height:844});
  await page.evaluate(()=>{if(document.querySelector('#pt-completed').hidden)document.querySelector('#pt-toggle-hidden').click();});
  for(const view of ['today','upcoming']){
    await page.locator('#pt-'+view+'-tab').click();
    await page.evaluate(()=>{document.querySelectorAll('.tm-children,#pt-overdue details').forEach(d=>d.open=true);});
    const metrics=await page.evaluate(()=>({viewport:document.documentElement.clientWidth,scroll:document.documentElement.scrollWidth,body:document.body.scrollWidth}));
    if(metrics.scroll>metrics.viewport+1||metrics.body>metrics.viewport+1)throw Error('Horizontal overflow '+width+' '+view+': '+JSON.stringify(metrics));
    layouts.push({width,view,...metrics});
  }
  await page.evaluate(()=>TaskListApp.addTask());
  const metrics=await page.evaluate(()=>{const d=document.querySelector('dialog'),r=d.getBoundingClientRect();return{left:r.left,right:r.right,viewport:innerWidth,scroll:d.scrollWidth,client:d.clientWidth};});
  if(metrics.left<0||metrics.right>width+1||metrics.scroll>metrics.client+1)throw Error('Editor overflow '+width+': '+JSON.stringify(metrics));
  await page.locator('dialog').evaluate(dialog=>{dialog.scrollTop=dialog.scrollHeight;});
  const actions=await page.locator('dialog').evaluate(dialog=>{const row=dialog.querySelector('.tl-dialog-actions'),last=dialog.querySelector('.tl-extra-fields'),a=row.getBoundingClientRect(),b=last.getBoundingClientRect();return {position:getComputedStyle(row).position,gap:a.top-b.bottom,height:a.height,inside:a.bottom<=dialog.getBoundingClientRect().bottom};});
  if(actions.position!=='static'||actions.gap<8||!actions.inside)throw Error('Editor action row overlaps fields at '+width+': '+JSON.stringify(actions));
  await page.getByRole('button',{name:'Cancel',exact:true}).click();
}
if(failures.length)throw Error('Page errors: '+failures.join('; '));
console.log(JSON.stringify({passed:result.checks.length,checks:result.checks,layoutChecks:layouts},null,2));
await browser.close();
