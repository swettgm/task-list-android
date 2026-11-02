// Real pointer/touch regressions for delayed completion undo and completion-day history.
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
const page = await browser.newPage({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 1, hasTouch: true, isMobile: true });
const checks = [], errors = [];
function check(condition, label) { if (!condition) throw new Error(label); checks.push(label); }
page.on('pageerror', error => errors.push(error.message));

await page.addInitScript(initial => {
  const storageKey = 'completion-undo-native-state';
  let canonical = JSON.parse(sessionStorage.getItem(storageKey) || JSON.stringify(initial));
  function save(value) { canonical = JSON.parse(JSON.stringify(value)); sessionStorage.setItem(storageKey, JSON.stringify(canonical)); }
  window.__undoNative = { snapshot: [], getState() { return JSON.parse(JSON.stringify(canonical)); }, setState: save };
  window.TaskListNative = {
    readState() { return JSON.stringify(canonical); },
    writeState(text) { save(JSON.parse(text)); },
    updateSnapshot(text) { window.__undoNative.snapshot = JSON.parse(text); },
    setThemeColors() {}, showWidgetSettings() {}
  };
}, seed);

const row = (id, area = '#pt-list') => page.locator(`${area} .tm-task-item[data-task-id="${id}"]`).first();
const checkbox = (id, area = '#pt-list') => row(id, area).locator(':scope > .form-check > input');
const point = async locator => {
  await locator.scrollIntoViewIfNeeded();
  const box = await locator.boundingBox();
  if (!box) throw new Error('Checkbox has no pointer target');
  return { x: box.x + box.width / 2, y: box.y + box.height / 2, width: box.width, height: box.height };
};
const state = () => page.evaluate(() => __undoNative.getState());
async function reload() {
  await page.reload();
  await page.waitForFunction(() => !!window.TaskListApp && window.__undoNative.snapshot.length > 0);
}
async function showCompleted() {
  if (await page.locator('#pt-completed').evaluate(element => element.hidden)) await page.locator('#pt-toggle-hidden').click();
}

try {
  await page.goto(pathToFileURL(documentPath).href);
  await page.waitForFunction(() => !!window.TaskListApp && window.__undoNative.snapshot.length > 0);
  const dates = await page.evaluate(() => {
    const today = TaskListApp.debugSummary().today;
    const ago = n => { const d = new Date(today + 'T12:00:00Z'); d.setUTCDate(d.getUTCDate() - n); return d.toISOString().slice(0, 10); };
    return { today, yesterday: ago(1), earlier: ago(2) };
  });
  const ids = { touch:'undo-touch-qa', hold:'undo-hold-qa', overdue:'undo-overdue-qa', older:'undo-older-qa' };
  const keys = { touch:ids.touch + ':' + dates.today, hold:ids.hold + ':' + dates.today,
    overdue:ids.overdue + ':' + dates.yesterday, older:ids.older + ':' + dates.yesterday };
  await page.evaluate(({ ids, keys, dates }) => {
    const canonical = __undoNative.getState();
    const task = (id, title, due) => ({ id, title, due, priority:'HIGHEST', minutes:1, labels:['Undo QA'], when:'Not set' });
    canonical.customTasks = [...(canonical.customTasks || []),
      task(ids.touch, 'Touch undo QA', dates.today), task(ids.hold, 'Held-pointer undo QA', dates.today),
      task(ids.overdue, 'Z finished today QA', dates.yesterday), task(ids.older, 'A completed earlier QA', dates.yesterday)];
    canonical.completed = { ...(canonical.completed || {}), [keys.touch]:false, [keys.hold]:false, [keys.overdue]:false, [keys.older]:true };
    canonical.completedOn = { ...(canonical.completedOn || {}), [keys.older]:dates.earlier };
    canonical.inProgress = null; canonical.progressStateInitialized = true;
    __undoNative.setState(canonical); TaskListApp.reloadFromNative(); TaskListApp.setContextFilter('Undo QA');
  }, { ids, keys, dates });
  await row(ids.touch).waitFor({ state:'visible' });

  // Touch the same screen coordinates twice while the active-task banner is retained.
  await row(ids.touch).locator(':scope > .tm-item-actions .tm-progress-control').click();
  await page.waitForFunction(() => !document.querySelector('#pt-current').hidden);
  const firstPoint = await point(checkbox(ids.touch));
  check(firstPoint.width >= 24 && firstPoint.height >= 24, 'Completion checkbox has a touch-sized target');
  await page.touchscreen.tap(firstPoint.x, firstPoint.y);
  const afterFirst = await row(ids.touch).evaluate(element => {
    const input = element.querySelector(':scope > .form-check > input'), box = input.getBoundingClientRect();
    return { checked:input.checked, disabled:input.disabled, x:box.x + box.width / 2, y:box.y + box.height / 2,
      completing:element.classList.contains('tm-completing'), hint:element.textContent.includes('Uncheck to undo') };
  });
  await page.touchscreen.tap(firstPoint.x, firstPoint.y);
  check(afterFirst.checked && !afterFirst.disabled && afterFirst.completing, 'First real touch saves completion while leaving the checkbox enabled');
  check(afterFirst.hint, 'Completing task tells the user it can be unchecked to undo');
  check(Math.abs(afterFirst.x - firstPoint.x) < 2 && Math.abs(afterFirst.y - firstPoint.y) < 2,
    'Completing an active task keeps the checkbox at the same touch coordinates');
  await page.waitForTimeout(2200);
  let saved = await state();
  check(saved.completed[keys.touch] === false && await row(ids.touch).isVisible(), 'Second real touch at the same coordinates cancels disappearance');
  check(!saved.completedOn?.[keys.touch], 'Touch undo clears its completion-day marker');
  await reload();
  check((await state()).completed[keys.touch] === false && await row(ids.touch).isVisible(), 'Touch undo survives a full document reload');

  // Hold the undo pointer across the original removal deadline, then release it.
  await row(ids.hold).locator(':scope > .tm-item-actions .tm-progress-control').click();
  const heldPoint = await point(checkbox(ids.hold));
  await page.mouse.click(heldPoint.x, heldPoint.y);
  await page.mouse.move(heldPoint.x, heldPoint.y);
  await page.mouse.down();
  await page.waitForTimeout(2200);
  check(await row(ids.hold).isVisible(), 'A checkbox is not removed while an undo pointer is held across the deadline');
  const held = await row(ids.hold).evaluate(element => {
    const input = element.querySelector(':scope > .form-check > input');
    let effectiveOpacity = 1;
    for (let ancestor = input; ancestor; ancestor = ancestor.parentElement) effectiveOpacity *= Number(getComputedStyle(ancestor).opacity);
    return { enabled:!input.disabled, opacity:effectiveOpacity };
  });
  check(held.enabled && held.opacity >= .95, 'Undo checkbox remains enabled and fully visible during the pause');
  await page.mouse.up();
  await page.waitForTimeout(2200);
  saved = await state();
  check(saved.completed[keys.hold] === false && await row(ids.hold).isVisible(), 'Releasing the held pointer completes undo instead of losing the task');
  await reload();
  check((await state()).completed[keys.hold] === false && await row(ids.hold).isVisible(), 'Held-pointer undo survives a full reload');

  // An overdue task finished now must be highlighted by actual completion day, not its due date.
  const overduePoint = await point(checkbox(ids.overdue));
  await page.touchscreen.tap(overduePoint.x, overduePoint.y);
  saved = await state();
  check(saved.completed[keys.overdue] === true && saved.completedOn[keys.overdue] === dates.today,
    'Overdue task records the actual New York completion date immediately');
  await page.waitForTimeout(2200);
  check(!await row(ids.overdue).isVisible(), 'Completed overdue task leaves the pending list after the pause');
  await showCompleted();
  const highlighted = row(ids.overdue, '#pt-completed-list');
  const earlier = row(ids.older, '#pt-completed-list');
  check(await highlighted.evaluate(element => element.classList.contains('tm-completed-today')), 'Completed-today history row has the highlight class');
  check(await highlighted.locator('.tm-today-badge').textContent() === 'Completed today', 'Today highlight includes an explicit text badge');
  check(await earlier.evaluate(element => !element.classList.contains('tm-completed-today')) && await earlier.locator('.tm-today-badge').count() === 0,
    'A task completed on an earlier day is not highlighted');
  const firstHistoryId = await page.locator('#pt-completed-list .tm-task-item[data-task-id]').first().getAttribute('data-task-id');
  check(firstHistoryId === ids.overdue, 'Tasks completed today appear before older completed tasks');
  await page.locator('#pt-completed').scrollIntoViewIfNeeded();
  await page.screenshot({ path:path.join(output, 'completion-history.png'), fullPage:false });
  await reload();
  await showCompleted();
  check(await row(ids.overdue, '#pt-completed-list').evaluate(element => element.classList.contains('tm-completed-today')), 'Today highlight survives a full reload');
  check((await state()).completedOn[keys.older] === dates.earlier, 'Adding a current highlight preserves the older completion date');
  const reopenPoint = await point(checkbox(ids.overdue, '#pt-completed-list'));
  await page.touchscreen.tap(reopenPoint.x, reopenPoint.y);
  saved = await state();
  check(saved.completed[keys.overdue] === false && !saved.completedOn?.[keys.overdue], 'Unchecking a highlighted history row clears completion and its day marker');
  check(await row(ids.overdue).isVisible() && !await row(ids.overdue, '#pt-completed-list').isVisible(), 'Reopening a highlighted row restores it to pending work');
  await reload();
  check((await state()).completed[keys.overdue] === false && !(await state()).completedOn?.[keys.overdue], 'History undo remains cleared after reload');
  check(errors.length === 0, 'No browser errors occurred during pointer undo or history highlighting');
  console.log(JSON.stringify({ passed:checks.length, checks }, null, 2));
} finally {
  await browser.close();
}
