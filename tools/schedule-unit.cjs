// Pure calendar regression tests; no browser, Android SDK, or network required.
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const context = { Date, Intl, Map, Set };
vm.createContext(context);
vm.runInContext(fs.readFileSync(path.join(__dirname, '../app/src/main/assets/schedule.js'), 'utf8'), context);
const S = context.TasksSchedule;
let passed = 0;
function test(name, run) { run(); passed++; console.log('PASS ' + name); }
function task(amount, unit, start, weekdays) { return { repeat: { amount, unit, start, ...(weekdays ? { weekdays } : {}) } }; }
function plain(value) { return JSON.parse(JSON.stringify(value)); }

test('day recurrence keeps the anchor and excludes earlier dates', () => {
  const t = task(3, 'day', '2026-11-01');
  assert.equal(S.matches(t, '2026-10-29'), false); assert.equal(S.matches(t, '2026-11-01'), true);
  assert.equal(S.matches(t, '2026-11-03'), false); assert.equal(S.matches(t, '2026-11-04'), true);
});
test('day intervals cross year boundaries', () => {
  assert.equal(S.matches(task(2, 'day', '2026-12-31'), '2027-01-02'), true);
  assert.equal(S.matches(task(2, 'day', '2026-12-31'), '2027-01-01'), false);
});
test('day intervals ignore DST spring changes', () => {
  const t = task(2, 'day', '2026-03-07');
  assert.equal(S.matches(t, '2026-03-09'), true); assert.equal(S.matches(t, '2026-03-08'), false);
});
test('day intervals ignore DST fall changes', () => {
  assert.equal(S.matches(task(2, 'day', '2026-10-31'), '2026-11-02'), true);
});
test('weekly recurrence defaults to the anchor weekday', () => {
  const t = task(2, 'week', '2026-11-02');
  assert.equal(S.matches(t, '2026-11-02'), true); assert.equal(S.matches(t, '2026-11-09'), false);
  assert.equal(S.matches(t, '2026-11-16'), true); assert.equal(S.matches(t, '2026-11-17'), false);
});
test('selected weekly days share an anchored calendar week', () => {
  const t = task(2, 'week', '2026-11-02', [1, 3, 5]);
  assert.equal(S.matches(t, '2026-11-04'), true); assert.equal(S.matches(t, '2026-11-06'), true);
  assert.equal(S.matches(t, '2026-11-11'), false); assert.equal(S.matches(t, '2026-11-18'), true);
});
test('weekly choices never schedule before a midweek start', () => {
  const t = task(2, 'week', '2026-11-05', [1, 5]);
  assert.equal(S.matches(t, '2026-11-02'), false); assert.equal(S.matches(t, '2026-11-06'), true);
  assert.equal(S.matches(t, '2026-11-09'), false); assert.equal(S.matches(t, '2026-11-16'), true);
});
test('weekly intervals cross calendar years', () => {
  const t = task(2, 'week', '2026-12-28', [1, 5]);
  assert.equal(S.matches(t, '2027-01-01'), true); assert.equal(S.matches(t, '2027-01-08'), false);
  assert.equal(S.matches(t, '2027-01-11'), true);
});
test('legacy Sunday weekend anchor keeps its old phase', () => {
  const t = { repeat: S.fromTask({ weekendCycleStart: '2026-10-25' }, '2026-11-01') };
  assert.equal(S.matches(t, '2026-10-24'), false); assert.equal(S.matches(t, '2026-10-25'), true);
  assert.equal(S.matches(t, '2026-10-31'), false); assert.equal(S.matches(t, '2026-11-01'), false);
  assert.equal(S.matches(t, '2026-11-07'), true); assert.equal(S.matches(t, '2026-11-08'), true);
});
test('monthly recurrence clamps short months without drifting', () => {
  const t = task(1, 'month', '2026-01-31');
  assert.equal(S.matches(t, '2026-02-28'), true); assert.equal(S.matches(t, '2026-03-28'), false);
  assert.equal(S.matches(t, '2026-03-31'), true); assert.equal(S.matches(t, '2026-04-30'), true);
});
test('monthly recurrence uses February 29 in leap years', () => {
  assert.equal(S.matches(task(1, 'month', '2028-01-31'), '2028-02-29'), true);
  assert.equal(S.matches(task(1, 'month', '2028-01-31'), '2028-02-28'), false);
});
test('monthly interval amount is honored across years', () => {
  const t = task(3, 'month', '2026-11-30');
  assert.equal(S.matches(t, '2027-02-28'), true); assert.equal(S.matches(t, '2027-01-30'), false);
  assert.equal(S.matches(t, '2027-05-30'), true);
});
test('month start dates are excluded before the anchor', () => {
  assert.equal(S.matches(task(1, 'month', '2026-05-31'), '2026-04-30'), false);
});
test('yearly leap-day recurrence clamps non-leap years', () => {
  const t = task(1, 'year', '2024-02-29');
  assert.equal(S.matches(t, '2025-02-28'), true); assert.equal(S.matches(t, '2025-03-01'), false);
  assert.equal(S.matches(t, '2028-02-29'), true); assert.equal(S.matches(t, '2028-02-28'), false);
});
test('yearly interval amount does not change the anchor month', () => {
  const t = task(2, 'year', '2024-02-29');
  assert.equal(S.matches(t, '2025-02-28'), false); assert.equal(S.matches(t, '2026-02-28'), true);
  assert.equal(S.matches(t, '2026-03-28'), false);
});
test('Gregorian century leap rules are respected', () => {
  const t = task(1, 'year', '2096-02-29');
  assert.equal(S.matches(t, '2100-02-28'), true); assert.equal(S.matches(t, '2100-02-29'), false);
});
test('invalid dates and amounts never schedule', () => {
  for (const amount of [0, -1, 1.5, true, false, {}, NaN, Infinity, Number.MAX_SAFE_INTEGER + 1]) assert.equal(S.matches(task(amount, 'day', '2026-11-01'), '2026-11-01'), false);
  for (const day of ['2026-02-30', '2026-13-01', '2026-1-01', '', null]) assert.equal(S.matches(task(1, 'day', '2026-11-01'), day), false);
  assert.equal(S.matches(task(1, 'day', '2026-02-30'), '2026-11-01'), false);
});
test('invalid units and weekday indexes are rejected', () => {
  assert.equal(S.matches(task(1, 'hour', '2026-11-01'), '2026-11-01'), false);
  assert.equal(S.matches(task(1, 'week', '2026-11-01', [7]), '2026-11-01'), false);
  assert.equal(S.matches({ repeat: { amount:1, unit:'week', start:'2026-11-01', weekdays:'Sunday' } }, '2026-11-01'), false);
});
test('plural units and duplicate weekday choices normalize', () => {
  assert.deepEqual(plain(S.normalize({ amount:'2', unit:'weeks', start:'2026-11-01', weekdays:[6,0,6] })), { amount:2, unit:'week', start:'2026-11-01', weekdays:[0,6] });
});
test('no repeat does not hijack the legacy engine', () => {
  assert.equal(S.matches({ days:[0,1,2,3,4] }, '2026-11-01'), false);
  assert.equal(S.matches(null, '2026-11-01'), false);
});
test('weekday descriptions remove incorrect Daily prefix', () => {
  assert.equal(S.describe({ days:[0,1,2,3,4], recurrence:'Daily, Sunday through Thursday' }), 'Sunday–Thursday');
  assert.equal(S.describe({ days:[1,2,3,4,5] }), 'Monday–Friday');
  assert.equal(S.describe({ days:[0,1,2,3,4,5,6] }), 'Daily');
});
test('weekend and split-day labels remain clear', () => {
  assert.equal(S.weekdaysLabel([0,6]), 'Saturday–Sunday');
  assert.equal(S.weekdaysLabel([1,3,5]), 'Monday, Wednesday, Friday');
  assert.equal(S.describe(task(2, 'week', '2026-11-01', [0,6])), 'Every 2 weeks · Saturday–Sunday');
});
test('generalized units have compact descriptions', () => {
  assert.equal(S.describe(task(1, 'month', '2026-11-01')), 'Every month');
  assert.equal(S.describe(task(3, 'year', '2026-11-01')), 'Every 3 years');
  assert.equal(S.describe(task(1, 'week', '2026-11-01', [0,1,2,3,4])), 'Sunday–Thursday');
});
test('existing task modes map without discarding anchors', () => {
  assert.deepEqual(plain(S.fromTask({ intervalDays:4, intervalStart:'2026-10-01' }, '2026-11-01')), { mode:'repeat', amount:4, unit:'day', start:'2026-10-01', weekdays:[] });
  assert.deepEqual(plain(S.fromTask({ days:[0,1,2,3,4] }, '2026-11-01')), { mode:'weekdays', amount:1, unit:'week', start:'2026-11-01', weekdays:[0,1,2,3,4] });
  assert.equal(S.fromTask({ days:[0,1,2,3,4,5,6] }, '2026-11-01').mode, 'repeat');
  assert.equal(S.fromTask({ due:'2026-12-01' }, '2026-11-01').start, '2026-12-01');
});
test('fromTask retains new repeats and does not mutate the input', () => {
  const t = task(2, 'month', '2026-01-31'); const before = JSON.stringify(t);
  assert.deepEqual(plain(S.fromTask(t, '2026-11-01')), { mode:'repeat', amount:2, unit:'month', start:'2026-01-31', weekdays:[] });
  assert.equal(JSON.stringify(t), before);
});
test('calendar years below 100 are not accidentally interpreted as 1900s', () => {
  assert.equal(S.matches(task(1, 'month', '0004-01-31'), '0004-02-29'), true);
  assert.equal(S.matches(task(1, 'year', '0096-02-29'), '0100-02-28'), true);
});

test('Weekend shortcut targets Friday including the current weekend',()=>{
  for(const day of ['2026-11-02','2026-11-03','2026-11-04','2026-11-05','2026-11-06','2026-11-07','2026-11-08'])assert.equal(S.nextWeekend(day),'2026-11-06');
  assert.equal(S.nextWeekend('2026-11-30'),'2026-12-04');
});
console.log(JSON.stringify({ passed, suite:'calendar recurrence' }));
