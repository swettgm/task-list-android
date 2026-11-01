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
  window.__nativeQA = { snapshot: [], failWrites:false, widgetOpened:false,
    setState(value) { canonical = JSON.parse(JSON.stringify(value)); },
    getState() { return JSON.parse(JSON.stringify(canonical)); } };
  window.TaskListNative = {
    readState() { return JSON.stringify(canonical); },
    writeState(text) { if (__nativeQA.failWrites) throw Error('Simulated storage failure'); canonical = JSON.parse(text); },
    updateSnapshot(text) { window.__nativeQA.snapshot = JSON.parse(text); },
    showWidgetSettings() { window.__nativeQA.widgetOpened=true; }
  };
}, seed);
await page.goto(pathToFileURL(documentPath).href);
await page.waitForFunction(() => !!window.TaskListApp && window.__nativeQA.snapshot.length > 0);
await page.screenshot({ path: path.join(output, 'today-dark.png'), fullPage: false });
await page.getByRole('button',{name:'Add task',exact:true}).click();
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
  check(add.textContent==='+'&&getComputedStyle(add).position==='fixed'&&add.getBoundingClientRect().height===56,'Add task is a large corner plus button');
  document.querySelector('.tl-settings').click();
  check(document.querySelector('dialog h2').textContent==='Settings','Settings opens from the gear');
  clickText(document.querySelector('dialog'),'Add home-screen widget');
  check(__nativeQA.widgetOpened&&!document.querySelector('dialog'),'Widget setup is available inside Settings');
  check(original.definitions === 102 && original.savedKeys >= 304 && original.moves >= 26, 'Original tasks, saved history, and moves retained');
  check(!!document.querySelector('#pt-date').textContent.trim() && !document.querySelector('h1'), 'Date-only heading');
  check(getComputedStyle(document.body).backgroundColor === 'rgb(16, 21, 31)', 'Dark page background');
  check(document.querySelector('#pt-upcoming').hidden && document.querySelector('#pt-completed').hidden, 'Today opens with completed tasks hidden');
  check(!document.querySelector('#pt-save,#pt-copy,#pt-backup,#pt-copy-full,#pt-copy-box,.tl-export'), 'Manual completion, copy, and backup controls removed');
  check(!document.querySelector('.tm-ai-note,.tm-pair-note,.tm-linked-chain'), 'AI tips and original suggested links are absent');
  check([...document.querySelectorAll('.tm-children')].every(item => !item.open), 'Subtask checklists start collapsed');
  check(rows().every(row=>[...row.querySelector(':scope > .tm-item-actions').children].every(b=>['Do now','Do tomorrow ➜','⚙'].includes(b.textContent))), 'Task rows only show Do now, Do tomorrow arrow, and gear');
  check(rows().every(row=>row.querySelector('.tl-edit-button')?.getAttribute('aria-label').startsWith('Edit ')), 'Gear buttons have accessible task names');
  TaskListApp.setContextFilter('At home'); await tick();
  check(rows().every(row => [...row.querySelectorAll('.tl-context-badge')].some(badge => badge.textContent === 'At home')), 'Context filter works');
  TaskListApp.setContextFilter(''); await tick();
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
  check(!rowFor(parentId,parentDate),'Checking a parent saves and hides it immediately');
  check(__nativeQA.snapshot.some(row=>row.id==='html:'+parentId+':'+parentDate&&row.done),'Automatic completion reaches the widget snapshot');
  check(TaskListApp.debugSummary().active===null,'Completing the active parent clears Do now');
  TaskListApp.reloadFromNative();await tick();
  check(!rowFor(parentId,parentDate),'Completion survives reload');
  document.querySelector('#pt-toggle-hidden').click();await tick();
  const hiddenParent=()=>[...document.querySelectorAll('#pt-completed-list [data-task-id]')].find(row=>row.dataset.taskId===parentId&&row.dataset.stateDate===parentDate);
  check(!document.querySelector('#pt-completed').hidden&&!!hiddenParent(),'Show hidden reveals completed tasks');
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
  const state=__nativeQA.getState();state.customTasks=[...(state.customTasks||[]),{id:'custom-native-qa',title:'Native popup task',priority:'UNSET',labels:['On the phone'],due:original.today,when:'Not set'}];
  __nativeQA.setState(state);TaskListApp.setContextFilter('');TaskListApp.reloadFromNative();await tick();
  check(rows().some(row=>row.dataset.taskId==='custom-native-qa'),'Native popup task is retained and loaded');
  // Create with a manual link; the picker offers every existing task.
  TaskListApp.addTask({labels:['On the computer']});
  let dialog=document.querySelector('dialog');check(dialog.open,'Creation editor opens');
  check(!dialog.textContent.includes('Useful help note'),'AI recommendation field is removed');
  check(dialog.querySelectorAll('[data-field=priority] input[type=radio]').length===6&&dialog.querySelectorAll('[data-field=repeat] input[type=radio]').length===5,'Priority and repeat use compact single-choice rows');
  check(![...dialog.querySelectorAll('legend,label>span')].some(label=>/[()]/.test(label.textContent)),'Editor labels contain no parenthetical instructions');
  check(dialog.querySelector('input[placeholder="Any time"]')&&dialog.querySelector('input[placeholder="Minutes"]'),'Time and duration use short placeholders');
  dialog.querySelector('input[type=text]').value='Browser-created task';
  dialog.querySelector('[data-field=priority] input[value=HIGH]').click();
  dialog.querySelector('input[placeholder="Minutes"]').value='17';
  dialog.querySelector('input[placeholder="Any time"]').value='9:00 AM';
  dialog.querySelector('.tl-extra-fields').open=true;
  dialog.querySelector('textarea[placeholder="Add notes"]').value='Preserve these notes';
  dialog.querySelector('textarea[placeholder="One step per line"]').value='First step\nSecond step';
  check(dialog.querySelectorAll('.tl-schedule-grid .tl-field').length===3,'Date, time, and duration remain available together');
  const picker=dialog.querySelector('[aria-label="Choose a task to link"]');
  check(picker.options.length===104,'Creation dropdown lists every existing task');
  picker.value='custom-native-qa';picker.dispatchEvent(new Event('change'));clickText(dialog,'Link task');
  clickText(dialog,'Save task');await tick();
  let created=()=>rows().find(row=>row.querySelector('.tm-title').textContent==='Browser-created task');
  check(!!created()&&created().querySelector('.tm-pair-note')?.textContent.includes('Native popup task'),'Manually linked task appears on the saved row');
  const createdId=created().dataset.taskId;
  const stored=__nativeQA.getState().customTasks.find(task=>task.id===createdId);
  check(stored.priority==='HIGH'&&stored.minutes===17&&stored.time==='9:00 AM'&&stored.notes==='Preserve these notes'&&stored.steps.length===2,'Compact editor preserves priority, duration, time, notes, and checklist steps');
  check(__nativeQA.getState().manualLinks['custom-native-qa'].includes(createdId),'Manual linking is bidirectional');
  TaskListApp.reloadFromNative();await tick();check(created().querySelector('.tm-pair-note')?.textContent.includes('Native popup task'),'Manual links survive reload');
  created().querySelector('.tl-edit-button').click();dialog=document.querySelector('dialog');
  check(![...dialog.querySelectorAll('option')].some(o=>o.value===createdId),'Task cannot link to itself');
  check(dialog.querySelector('[data-field=priority] input[value=HIGH]').checked&&dialog.querySelector('input[placeholder="Minutes"]').value==='17'&&dialog.querySelector('input[placeholder="Any time"]').value==='9:00 AM','Saved values repopulate the compact editor');
  [...dialog.querySelectorAll('label.tl-context-choice')].find(l=>l.textContent==='On the road').querySelector('input').click();
  dialog.querySelector('.tl-unlink').click();clickText(dialog,'Save task');await tick();TaskListApp.reloadFromNative();await tick();
  check([...created().querySelectorAll('.tl-context-badge')].some(b=>b.textContent==='On the road'),'Contexts can be edited through the gear');
  check(!created().querySelector('.tm-pair-note')&&!(__nativeQA.getState().manualLinks['custom-native-qa']||[]).includes(createdId),'Unlink removes both sides and survives reload');
  created().querySelector(':scope > .form-check > input').click();await tick();
  check(!created()&&!!rowFor('custom-native-qa'),'Completion affects only the checked task');
  document.querySelector('#pt-upcoming-tab').click();check(!document.querySelector('#pt-upcoming').hidden&&document.querySelector('#pt-today').hidden,'Upcoming tab works');
  check(document.querySelectorAll('#pt-future-list .tm-day').length>0,'Upcoming remains grouped by date');
  document.querySelector('#pt-today-tab').click();
  TaskListApp.addTask();dialog=document.querySelector('dialog');
  dialog.querySelector('input[type=text]').value='Repeat choice QA';
  dialog.querySelector('[data-field=repeat] input[value=weekdays]').click();
  check(!dialog.querySelector('.tl-weekdays').hidden,'Weekday checkboxes appear only for weekday repeating');
  dialog.querySelector('input[aria-label=Monday]').click();dialog.querySelector('input[aria-label=Friday]').click();
  clickText(dialog,'Save task');await tick();
  let repeat=__nativeQA.getState().customTasks.find(task=>task.title==='Repeat choice QA');
  check(JSON.stringify(repeat.days)==='[1,5]','Weekday choices save the chosen repeat days');
  TaskListApp.addTask();dialog=document.querySelector('dialog');dialog.querySelector('input[type=text]').value='Interval choice QA';
  dialog.querySelector('[data-field=repeat] input[value=interval]').click();
  const every=dialog.querySelector('input[placeholder=Days]');check(!every.closest('label').hidden&&dialog.querySelector('.tl-weekdays').hidden,'Interval input replaces weekday choices');every.value='4';
  clickText(dialog,'Save task');await tick();
  repeat=__nativeQA.getState().customTasks.find(task=>task.title==='Interval choice QA');
  check(repeat.intervalDays===4&&!!repeat.intervalStart,'Every N days remains available');
  TaskListApp.addTask();dialog=document.querySelector('dialog');dialog.querySelector('input[type=text]').value='Alternate weekends QA';
  dialog.querySelector('[data-field=repeat] input[value=weekends]').click();dialog.querySelector('.tl-urgent input').click();
  clickText(dialog,'Save task');await tick();repeat=__nativeQA.getState().customTasks.find(task=>task.title==='Alternate weekends QA');
  check(!!repeat.weekendCycleStart&&repeat.urgent&&repeat.priority==='HIGHEST','Alternate weekends and urgent options remain available');
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
  await page.getByRole('button',{name:'Cancel',exact:true}).click();
}
if(failures.length)throw Error('Page errors: '+failures.join('; '));
console.log(JSON.stringify({passed:result.checks.length,checks:result.checks,layoutChecks:layouts},null,2));
await browser.close();
