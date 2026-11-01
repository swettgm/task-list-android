// Run with Playwright on a host that permits Chromium subprocess networking.
// The test uses the real offline document and a narrow mock of Android's bridge.
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
  window.__nativeQA = { snapshot: [], copied: '', exported: '', widgetOpened: false,
    setState(value) { canonical = JSON.parse(JSON.stringify(value)); }, getState() { return JSON.parse(JSON.stringify(canonical)); } };
  window.TaskListNative = {
    readState() { return JSON.stringify(canonical); },
    writeState(text) { canonical = JSON.parse(text); },
    updateSnapshot(text) { window.__nativeQA.snapshot = JSON.parse(text); },
    copyText(text) { window.__nativeQA.copied = text; },
    exportBackup(text) { window.__nativeQA.exported = text; },
    showWidgetSettings() { window.__nativeQA.widgetOpened = true; }
  };
}, seed);
await page.goto(pathToFileURL(documentPath).href);
await page.waitForFunction(() => !!window.TaskListApp && window.__nativeQA.snapshot.length > 0);
await page.screenshot({ path: path.join(output, 'today-dark.png'), fullPage: false });

const result = await page.evaluate(async () => {
  const checks = [];
  function check(condition, label) { if (!condition) throw new Error(label); checks.push(label); }
  const tick = () => new Promise(resolve => setTimeout(resolve, 25));
  const rows = () => [...document.querySelectorAll('#pt-list .tm-task-item[data-task-id]')];
  const rowFor = (id, date) => rows().find(row => row.dataset.taskId === id && (!date || row.dataset.stateDate === date));
  const original = TaskListApp.debugSummary();
  check(original.definitions === 102, 'All 102 task definitions retained');
  check(original.savedKeys >= 304, 'All 304 saved completion keys retained');
  check(original.moves >= 26, 'All 26 saved moves retained');
  check(document.querySelector('#pt-date').textContent.trim().length > 0, 'Date is visible');
  check(!document.querySelector('h1'), 'No screen heading consumes task space');
  check(getComputedStyle(document.body).backgroundColor === 'rgb(16, 21, 31)', 'Dark page background');
  check(document.querySelector('#pt-upcoming').hidden, 'Today opens first');
  check(!document.querySelector('#pt-completed').open && !document.querySelector('#pt-backup').open, 'Completed and backup start collapsed');
  check([...document.querySelectorAll('.tm-children')].every(item => !item.open), 'Subtask checklists start collapsed');
  check(document.querySelector('#tl-context-filter').options.length >= 5, 'Named contexts are available');

  TaskListApp.setContextFilter('At home'); await tick();
  check(rows().every(row => [...row.querySelectorAll('.tl-context-badge')].some(badge => badge.textContent === 'At home')), 'Context filtering limits displayed rows');
  TaskListApp.setContextFilter(''); await tick();

  const first = rows().find(row => !row.querySelector('.tm-progress-control').disabled);
  check(!!first, 'A current task can be selected');
  const firstId = first.dataset.taskId, firstDate = first.dataset.stateDate;
  first.querySelector('.tm-progress-control').click(); await tick();
  check(TaskListApp.debugSummary().active === firstId + ':' + firstDate, 'Do now selects one current occurrence');
  rowFor(firstId, firstDate).querySelector('.tm-progress-control').click(); await tick();
  check(TaskListApp.debugSummary().active === null, 'Do now toggles off');
  TaskListApp.reloadFromNative(); await tick();
  check(TaskListApp.debugSummary().active === null, 'Cleared current marker stays cleared on reload');

  const repeating = rows().filter(row => row.querySelector('.tm-meta').textContent.includes('🔁'));
  check(repeating.length > 0 && repeating.every(row => ![...row.querySelectorAll('button')].some(button => button.textContent === 'Do tomorrow ➜')), 'Recurring tasks never get Do tomorrow');
  const movable = rows().find(row => [...row.querySelectorAll('button')].some(button => button.textContent === 'Do tomorrow ➜'));
  check(!!movable, 'Unfinished one-time tasks get Do tomorrow');
  const movedId = movable.dataset.taskId, movedDate = movable.dataset.stateDate;
  [...movable.querySelectorAll('button')].find(button => button.textContent === 'Do tomorrow ➜').click(); await tick();
  const tomorrowDate = new Date(original.today + 'T12:00:00Z'); tomorrowDate.setUTCDate(tomorrowDate.getUTCDate() + 1);
  check(__nativeQA.getState().movedTo[movedId + ':' + movedDate] === tomorrowDate.toISOString().slice(0, 10), 'Do tomorrow preserves the original occurrence and records its new date');
  check(!rowFor(movedId, movedDate), 'Moved occurrence leaves Today');

  const parent = rows().find(row => row.querySelector('.tm-children') && !row.querySelector(':scope > .form-check > .form-check-input').checked);
  check(!!parent, 'An unfinished checklist is available');
  const parentId = parent.dataset.taskId, parentDate = parent.dataset.stateDate;
  const before = JSON.stringify(__nativeQA.getState().completed);
  parent.querySelector(':scope > .form-check > .form-check-input').click(); await tick();
  check(JSON.stringify(__nativeQA.getState().completed) === before, 'Checkbox completion is staged until Save Completed');
  check(!document.querySelector('#pt-save').disabled, 'Staged completion enables Save Completed');
  document.querySelector('#pt-save').click(); await tick();
  check(!rowFor(parentId, parentDate), 'Saving removes the completed parent from pending Today');
  check(__nativeQA.snapshot.some(row => row.id === 'html:' + parentId + ':' + parentDate && row.done), 'Saved parent completion reaches the native widget snapshot');
  check(!document.querySelector('#pt-completed').open, 'Saving keeps Completed today collapsed');

  document.querySelector('#pt-copy').click(); await tick();
  check(__nativeQA.copied.startsWith('Saved task updates as of'), 'Copy progress exports saved changes');
  document.querySelector('#pt-copy-full').click(); await tick();
  check(__nativeQA.copied.startsWith('Saved task states as of') && __nativeQA.copied.split('\n').length >= 304, 'Full history copy retains the original history');
  const backup = JSON.parse(TaskListApp.exportBackup());
  check(backup.tasks.length === 102 && Object.keys(backup.state.completed).length >= 304, 'Full JSON backup preserves task definitions and saved history');
  check(__nativeQA.exported.length > 0, 'Backup is sent through Android export');

  const state = __nativeQA.getState();
  state.customTasks = [...(state.customTasks || []), { id:'custom-native-qa',title:'Native popup task',priority:'UNSET',labels:['On the phone'],due:original.today,when:'Not set' }];
  __nativeQA.setState(state);
  TaskListApp.setContextFilter(''); await tick();
  check(__nativeQA.getState().customTasks.some(task => task.id === 'custom-native-qa'), 'An older open page does not drop a task added by the native popup');
  TaskListApp.reloadFromNative(); await tick();
  check(TaskListApp.debugSummary().definitions === 103 && rows().some(row => row.dataset.taskId === 'custom-native-qa'), 'Native popup task appears after state reload');
  TaskListApp.setContextFilter('On the phone'); await tick();
  check(rows().some(row => row.dataset.taskId === 'custom-native-qa'), 'Native popup labels are recognized by the context filter');
  TaskListApp.setContextFilter(''); await tick();

  TaskListApp.addTask({ labels:['On the computer'] });
  const dialog = document.querySelector('dialog'); check(!!dialog && dialog.open, 'Add task opens its editor');
  dialog.querySelector('input[type=text]').value = 'Browser-created task';
  [...dialog.querySelectorAll('button')].find(button => button.textContent === 'Save task').click(); await tick();
  check(TaskListApp.debugSummary().definitions === 104 && !document.querySelector('dialog'), 'A new task saves and closes its editor');
  check(rows().some(row => row.querySelector('.tm-title').textContent === 'Browser-created task'), 'A new task appears in Today');
  const labelsRow = rows().find(row => row.querySelector('.tm-title').textContent === 'Browser-created task');
  labelsRow.querySelector('.tl-label-button').click();
  const labelsDialog = document.querySelector('dialog');
  [...labelsDialog.querySelectorAll('label.tl-context-choice')].find(label => label.textContent === 'On the road').querySelector('input').click();
  [...labelsDialog.querySelectorAll('button')].find(button => button.textContent === 'Save contexts').click(); await tick();
  TaskListApp.reloadFromNative(); await tick();
  const restored = rows().find(row => row.querySelector('.tm-title').textContent === 'Browser-created task');
  check([...restored.querySelectorAll('.tl-context-badge')].some(badge => badge.textContent === 'On the road'), 'Edited contexts survive a reload');
  document.querySelector('#pt-upcoming-tab').click();
  check(!document.querySelector('#pt-upcoming').hidden && document.querySelector('#pt-today').hidden, 'Upcoming tab works');
  check(document.querySelectorAll('#pt-future-list .tm-day').length > 0, 'Upcoming remains grouped into dated sections');
  return { checks, definitionsAfterQA:TaskListApp.debugSummary().definitions, snapshotRows:__nativeQA.snapshot.length };
});
if (failures.length) throw new Error('Page errors: ' + failures.join('; '));
console.log(JSON.stringify({ passed: result.checks.length, ...result }, null, 2));
await browser.close();
