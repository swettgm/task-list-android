/* Shared calendar rules for Tasks on Android and PC. Dates are calendar days,
   evaluated in UTC so daylight-saving changes cannot move an occurrence. */
(function (global) {
  'use strict';
  const DAY = 86400000;
  const names = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const dates = new Map();

  function parseDate(value) {
    if (typeof value !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return null;
    if (dates.has(value)) return dates.get(value);
    const date = new Date(value + 'T12:00:00Z');
    if (!Number.isFinite(date.getTime()) || date.toISOString().slice(0, 10) !== value) return null;
    const result = { year: date.getUTCFullYear(), month: date.getUTCMonth() + 1,
      day: date.getUTCDate(), weekday: date.getUTCDay(), ordinal: Math.floor(date.getTime() / DAY) };
    if (dates.size >= 512) dates.delete(dates.keys().next().value);
    dates.set(value, result);
    return result;
  }

  function validAmount(value) {
    if (typeof value !== 'number' && (typeof value !== 'string' || !/^\d+$/.test(value.trim()))) return null;
    const number = Number(value);
    return Number.isSafeInteger(number) && number > 0 ? number : null;
  }

  function weekdays(value) {
    if (value == null) return [];
    if (!Array.isArray(value) || value.some(day => !Number.isInteger(day) || day < 0 || day > 6)) return null;
    return [...new Set(value)].sort((a, b) => a - b);
  }

  function normalize(value) {
    if (!value || typeof value !== 'object' || Array.isArray(value)) return null;
    const amount = validAmount(value.amount);
    const unit = typeof value.unit === 'string' ? value.unit.trim().toLowerCase().replace(/s$/, '') : '';
    const days = weekdays(value.weekdays);
    if (!amount || !['day', 'week', 'month', 'year'].includes(unit) || !parseDate(value.start) || !days) return null;
    return { amount, unit, start: value.start, weekdays: days };
  }

  function monthLength(year, month) {
    const date = new Date(0);
    date.setUTCHours(12, 0, 0, 0);
    date.setUTCFullYear(year, month, 0);
    return date.getUTCDate();
  }

  function matches(task, day) {
    const repeat = normalize(task && task.repeat), target = parseDate(day);
    if (!repeat || !target) return false;
    const start = parseDate(repeat.start);
    if (target.ordinal < start.ordinal) return false;
    if (repeat.unit === 'day') {
      return (target.ordinal - start.ordinal) % repeat.amount === 0
        && (!repeat.weekdays.length || repeat.weekdays.includes(target.weekday));
    }
    if (repeat.unit === 'week') {
      // Monday-based calendar weeks preserve the existing Sunday-anchored
      // alternate-weekend cycle: Sunday 0, Saturday 13, Sunday 14.
      const startMonday = start.ordinal - (start.weekday + 6) % 7;
      const targetMonday = target.ordinal - (target.weekday + 6) % 7;
      const weekDifference = (targetMonday - startMonday) / 7;
      const selected = repeat.weekdays.length ? repeat.weekdays : [start.weekday];
      return weekDifference % repeat.amount === 0 && selected.includes(target.weekday);
    }
    if (repeat.unit === 'month') {
      const difference = (target.year - start.year) * 12 + target.month - start.month;
      return difference % repeat.amount === 0 && target.day === Math.min(start.day, monthLength(target.year, target.month));
    }
    const difference = target.year - start.year;
    return difference % repeat.amount === 0 && target.month === start.month
      && target.day === Math.min(start.day, monthLength(target.year, start.month));
  }

  function weekdaysLabel(value) {
    const days = weekdays(value);
    if (!days || !days.length) return '';
    if (days.length === 7) return 'Daily';
    const selected = new Set(days);
    const starts = days.filter(day => !selected.has((day + 6) % 7));
    const runs = starts.map(first => {
      const run = [first];
      while (selected.has((run[run.length - 1] + 1) % 7)) run.push((run[run.length - 1] + 1) % 7);
      return run.length > 1 ? names[first] + '–' + names[run[run.length - 1]] : names[first];
    });
    return runs.join(', ');
  }

  function describe(task) {
    if (!task || typeof task !== 'object') return '';
    const repeat = normalize(task.repeat);
    if (repeat) {
      const selected = repeat.weekdays;
      if (repeat.unit === 'day' && repeat.amount === 1) return selected.length ? weekdaysLabel(selected) : 'Daily';
      if (repeat.unit === 'week' && repeat.amount === 1 && selected.length) return weekdaysLabel(selected);
      const label = 'Every ' + (repeat.amount === 1 ? '' : repeat.amount + ' ') + repeat.unit + (repeat.amount === 1 ? '' : 's');
      return label + (repeat.unit === 'week' && selected.length ? ' · ' + weekdaysLabel(selected) : '');
    }
    if (task.weekendCycleStart) return 'Every 2 weeks · Saturday–Sunday';
    const interval = validAmount(task.intervalDays);
    if (interval) return interval === 1 ? 'Daily' : 'Every ' + interval + ' days';
    const days = weekdays(task.days);
    if (days && days.length) return weekdaysLabel(days);
    return typeof task.recurrence === 'string'
      ? task.recurrence.trim().replace(/^Daily\s*[,·:]\s*(?=\S)/i, '') : '';
  }

  function today() {
    return new Intl.DateTimeFormat('en-CA', { timeZone: 'America/New_York', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date());
  }

  function fromTask(task, day) {
    task = task && typeof task === 'object' ? task : {};
    const fallback = parseDate(day) ? day : today();
    const repeat = normalize(task.repeat);
    if (repeat) return { mode: repeat.unit === 'week' && repeat.amount === 1 && repeat.weekdays.length ? 'weekdays' : 'repeat', ...repeat };
    if (parseDate(task.weekendCycleStart)) return { mode: 'repeat', amount: 2, unit: 'week', start: task.weekendCycleStart, weekdays: [0, 6] };
    const interval = validAmount(task.intervalDays);
    if (interval) return { mode: 'repeat', amount: interval, unit: 'day', start: parseDate(task.intervalStart) ? task.intervalStart : fallback, weekdays: [] };
    const days = weekdays(task.days);
    if (days && days.length === 7) return { mode: 'repeat', amount: 1, unit: 'day', start: fallback, weekdays: [] };
    if (days && days.length) return { mode: 'weekdays', amount: 1, unit: 'week', start: fallback, weekdays: days };
    return { mode: 'once', amount: 1, unit: 'day', start: parseDate(task.due) ? task.due : fallback, weekdays: [] };
  }

  function addDay(day,n){const date=new Date(day+'T12:00:00Z');date.setUTCDate(date.getUTCDate()+n);return date.toISOString().slice(0,10);}
  function occurs(task,day){
    const date=parseDate(day);if(!date||!task||task.retired||task.startsOn&&day<task.startsOn)return false;
    if(task.repeat)return matches(task,day);
    const weekend=parseDate(task.weekendCycleStart);
    if(weekend){const diff=date.ordinal-weekend.ordinal;return diff===0?date.weekday===0:diff>=13&&(diff%14===0||diff%14===13);}
    const start=parseDate(task.intervalStart),amount=validAmount(task.intervalDays);
    if(start&&amount)return date.ordinal>=start.ordinal&&(date.ordinal-start.ordinal)%amount===0;
    if(Array.isArray(task.days)&&task.days.length)return task.days.includes(date.weekday);
    if(Array.isArray(task.dates)&&task.dates.length)return task.dates.includes(day);
    return task.due===day;
  }
  // Kept for older widget snapshots; strict schedules have no early opening.
  function weekendOpening(){return '';}
  function repeating(task){return !!(task.repeat||task.recurrence||task.days?.length||task.intervalDays||task.weekendCycleStart||task.recurringDaily);}
  function chosenDate(task,day,moves){return repeating(task)?day:moves?.[task.id+':'+day]||day;}
  function nextWeekend(day){const date=parseDate(day);return addDay(day,date.weekday===0?-2:date.weekday===6?-1:(5-date.weekday+7)%7);}
  const taskClock=new Intl.DateTimeFormat('en-CA',{timeZone:'America/New_York',year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',second:'2-digit',hourCycle:'h23'});
  function clockWall(epoch){const parts=Object.fromEntries(taskClock.formatToParts(new Date(epoch)).filter(part=>part.type!=='literal').map(part=>[part.type,Number(part.value)]));const date=new Date(0);date.setUTCFullYear(parts.year,parts.month-1,parts.day);date.setUTCHours(parts.hour,parts.minute,parts.second,0);return date.getTime();}
  function showAt(task,day){
    if(!task||!Number.isInteger(task.showBeforeMinutes)||task.showBeforeMinutes<0||task.showBeforeMinutes>2147483647||!parseDate(day))return null;
    const match=/^(\d{1,2}):(\d{2})(?:\s*(AM|PM))?$/i.exec(String(task.time||'').trim());if(!match)return null;let hour=Number(match[1]),minute=Number(match[2]);if(minute>59||hour>23||match[3]&&(hour<1||hour>12))return null;if(match[3])hour=hour%12+(/pm/i.test(match[3])?12:0);
    const wallDate=new Date(day+'T00:00:00Z');wallDate.setUTCHours(hour,minute,0,0);const wall=wallDate.getTime(),offsets=new Set([wall-86400000,wall+86400000].map(epoch=>clockWall(epoch)-epoch)),candidates=[...offsets].map(offset=>wall-offset),exact=candidates.filter(epoch=>clockWall(epoch)===wall);
    // Match java.time: earlier offset during a repeated hour; shift forward over a missing hour.
    const due=exact.length?Math.min(...exact):Math.max(...candidates);return due-task.showBeforeMinutes*60000;
  }
  function isVisible(task,day,now=Date.now()){const at=showAt(task,day);return at===null||now>=at;}
  global.TasksSchedule = Object.freeze({ matches, describe, fromTask, normalize, weekdaysLabel,occurs,weekendOpening,nextWeekend,showAt,isVisible,repeating,chosenDate });
})(typeof window === 'object' ? window : globalThis);
