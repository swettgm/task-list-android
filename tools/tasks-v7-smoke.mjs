// Targeted regression checks for Tasks scheduling, open-task linking, and Upcoming.
import { chromium } from 'playwright';
import { readFile, mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import { serveFixture } from './browser-fixture-server.mjs';
const documentPath=path.resolve(process.argv[2]||'app/src/main/assets/www/index.html');
const seedPath=path.resolve(process.argv[3]||path.join(path.dirname(documentPath),'initial-state.json'));
const seed=JSON.parse(await readFile(seedPath,'utf8'));
const output=path.resolve(process.env.TASK_SCREENSHOT_DIR||'build/browser-qa');await mkdir(output,{recursive:true});
const executablePath=process.env.CHROME_BIN||['/usr/bin/google-chrome','/usr/bin/chromium','/opt/google/chrome/chrome'].find(existsSync);
const fixture=await serveFixture(documentPath);
const browser=await chromium.launch({headless:true,executablePath,args:['--no-sandbox','--disable-dev-shm-usage']});
const checks=[],errors=[];
function check(value,label){if(!value)throw Error(label);checks.push(label);}
async function newPage(state,fixedTime){
 const page=await browser.newPage({viewport:{width:390,height:844},deviceScaleFactor:1});page.on('pageerror',error=>errors.push(error.message));
 await page.addInitScript(({initial,fixedTime})=>{
  if(fixedTime){const RealDate=Date;class FixedDate extends RealDate{constructor(...args){super(...(args.length?args:[fixedTime]));}static now(){return fixedTime;}}window.Date=FixedDate;}
  const key='tasks-v7-state';let current=JSON.parse(sessionStorage.getItem(key)||JSON.stringify(initial));
  const setState=value=>{current=JSON.parse(JSON.stringify(value));sessionStorage.setItem(key,JSON.stringify(current));};
  window.__v7={writes:[],snapshot:[],getState:()=>JSON.parse(JSON.stringify(current)),setState};
  window.TaskListNative={readState:()=>JSON.stringify(current),writeState:text=>{__v7.writes.push(JSON.parse(text));setState(JSON.parse(text));},updateSnapshot:text=>{__v7.snapshot=JSON.parse(text);},setThemeColors(){},showWidgetSettings(){}};
 },{initial:state,fixedTime});
 await page.goto(fixture.url);await page.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);return page;
}
const page=await newPage(seed);
try{
 check(await page.title()==='Tasks','Application window title is Tasks');
 const dates=await page.evaluate(()=>{const today=TaskListApp.debugSummary().today;const add=n=>{const d=new Date(today+'T12:00:00Z');d.setUTCDate(d.getUTCDate()+n);return d.toISOString().slice(0,10);};return{today,tomorrow:add(1),second:add(2),third:add(3),yesterday:add(-1)};});
 const truckMeta=await page.locator('[data-task-id=truck] .tm-meta').first().textContent();
 check(!/daily/i.test(truckMeta)&&/sunday.*thursday/i.test(truckMeta),'Clean out truck frequency is Sunday through Thursday without a Daily prefix');
 const ids={dailyA:'v7-daily-a',dailyB:'v7-daily-b',closed:'v7-closed',future:'v7-future-once',second:'v7-second-once',third:'v7-third-once',futureRepeat:'v7-future-repeat'};
 await page.evaluate(({ids,dates})=>{
  const current=__v7.getState(),task=(id,title,due)=>({id,title,due,priority:'HIGH',labels:['V7 QA'],minutes:15});
  current.customTasks=[...(current.customTasks||[]),
   {...task(ids.dailyA,'Daily link A'),due:undefined,days:[0,1,2,3,4,5,6],recurrence:'Daily'},
   {...task(ids.dailyB,'Daily link B'),due:undefined,days:[0,1,2,3,4,5,6],recurrence:'Daily'},
   task(ids.closed,'Already completed link option',dates.today),
   task(ids.future,'Tomorrow once task',dates.tomorrow),task(ids.second,'Second-day task',dates.second),task(ids.third,'Third-day task',dates.third),
   {id:ids.futureRepeat,title:'Future daily task',priority:'HIGH',labels:['V7 QA'],minutes:15,repeat:{amount:1,unit:'day',start:dates.tomorrow}}];
  current.completed={...current.completed,[ids.closed+':'+dates.today]:true,[ids.dailyA+':'+dates.today]:false,[ids.dailyB+':'+dates.today]:false};current.completedOn={...current.completedOn,[ids.closed+':'+dates.today]:dates.today};
  current.inProgress=null;current.progressStateInitialized=true;
  __v7.setState(current);TaskListApp.reloadFromNative();TaskListApp.setContextFilter('V7 QA');
 },{ids,dates});
 check(await page.locator('#pt-today').isVisible()&&!await page.locator('#pt-upcoming').isVisible(),'Tasks starts on Today');
 const todayRow=id=>page.locator(`#pt-list .tm-task-item[data-task-id="${id}"]`).first();
 const futureRows=id=>page.locator(`#pt-future-list .tm-task-item[data-task-id="${id}"]`);
 await todayRow(ids.dailyA).locator('.tl-edit-button').click();
 const editor=page.locator('dialog');
 const optionData=await editor.locator('[aria-label="Choose a task to link"] option').evaluateAll(options=>options.map(o=>({value:o.value,title:o.textContent})));
 check(!optionData.some(o=>o.value===ids.closed),'Completed tasks are excluded from the linked-task picker');
 check(optionData.some(o=>o.value===ids.dailyB&&o.title==='Daily link B'),'Linked-task options show titles without dates');
 await editor.locator('[aria-label="Choose a task to link"]').selectOption(ids.dailyB);
 check(await editor.locator('.tl-linked-list').textContent().then(t=>t.includes('Daily link B')),'Selecting the dropdown option immediately shows its linked-task chip');
 const beforeCancel=await page.evaluate(()=>JSON.stringify(__v7.getState().manualLinks||{}));await editor.getByRole('button',{name:'Cancel',exact:true}).click();check(await page.evaluate(()=>JSON.stringify(__v7.getState().manualLinks||{}))===beforeCancel,'Cancelling the editor does not save selected links');
 await todayRow(ids.dailyA).locator('.tl-edit-button').click();await editor.locator('[aria-label="Choose a task to link"]').selectOption(ids.dailyB);const beforeSave=await page.evaluate(()=>__v7.writes.length);
 await editor.getByRole('button',{name:'Save task',exact:true}).click();check(await page.evaluate(({a,b,start})=>__v7.writes.slice(start).length>0&&__v7.writes.slice(start).every(s=>s.manualLinks?.[a]?.includes(b)&&s.manualLinks?.[b]?.includes(a)),{a:ids.dailyA,b:ids.dailyB,start:beforeSave}),'Task changes and reciprocal links are persisted together');
 await page.waitForFunction(id=>{const row=[...document.querySelectorAll('#pt-list [data-task-id]')].find(r=>r.dataset.taskId===id);return row?.textContent.includes('Daily link B')&&row.textContent.includes('🔗');},ids.dailyA);
 check(await todayRow(ids.dailyA).textContent().then(t=>t.includes('Daily link B')&&t.includes('🔗')),'Newly linked repeating task shows its link icon and peer immediately');
 check(await todayRow(ids.dailyA).locator('.tm-pair-note').evaluate(n=>n.textContent==='🔗 Daily link B'&&getComputedStyle(n).fontSize===getComputedStyle(n.parentElement.querySelector('.tm-meta')).fontSize),'Saved link renders as a small chain-and-title line');await page.screenshot({path:path.join(output,'manual-linked-tasks.png')});
 let state=await page.evaluate(()=>__v7.getState());
 check(state.manualLinks[ids.dailyA].includes(ids.dailyB)&&state.manualLinks[ids.dailyB].includes(ids.dailyA),'Recurring links are saved bidirectionally by task identity');
 await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);
 check(await todayRow(ids.dailyA).textContent().then(t=>t.includes('Daily link B')),'Manual recurring link survives a full reload');
 // The following date uses the same saved task definitions and links, not yesterday's occurrence IDs.
 state=await page.evaluate(()=>__v7.getState());
 const tomorrowPage=await newPage(state,Date.parse(dates.tomorrow+'T17:00:00Z'));
 check(await tomorrowPage.locator(`#pt-list [data-task-id="${ids.dailyA}"]`).first().textContent().then(t=>t.includes('Daily link B')&&t.includes('🔗')),'Daily task link remains visible on the next day occurrence');
 await tomorrowPage.close();
 // Split duration remains one total in saved task data and uses no numbering or "total" label.
 await page.evaluate(()=>TaskListApp.addTask({labels:['V7 QA']}));
 await editor.locator('input[type=text]').first().fill('Duration conversion QA');
 await editor.locator('input[aria-label=Hours]').fill('1');await editor.locator('input[aria-label=Minutes]').fill('15');
 await editor.getByRole('button',{name:'Save task',exact:true}).click();
 state=await page.evaluate(()=>__v7.getState());const durationTask=state.customTasks.find(t=>t.title==='Duration conversion QA');
 check(durationTask.minutes===75,'Hours and minutes save as the correct combined duration');
 const durationRow=todayRow(durationTask.id);const durationMeta=await durationRow.locator('.tm-meta').first().textContent();
 check(!/\btotal\b|#\d+/i.test(durationMeta)&&/min/i.test(durationMeta),'Task duration has units without total or task numbers');
 await durationRow.locator('.tl-edit-button').click();
 check(await editor.locator('input[aria-label=Hours]').inputValue()==='1'&&await editor.locator('input[aria-label=Minutes]').inputValue()==='15','Split duration values repopulate when editing');
 await editor.getByRole('button',{name:'Cancel',exact:true}).click();
 check(await page.locator('#pt-list .tm-meta').evaluateAll(nodes=>nodes.every(n=>!/#\d+|\btotal\b/i.test(n.textContent))),'Task list metadata has no task numbers or total duration wording');
 const firstBox=await page.locator('#pt-list .tm-task-item .form-check-input').first().boundingBox();
 const firstTitle=await page.locator('#pt-list .tm-task-item .tm-title').first().boundingBox();
 check(Math.abs((firstBox.y+firstBox.height/2)-(firstTitle.y+firstTitle.height/2))<.5,'Checkbox is vertically centered against the task title');
 check(await page.locator('.tl-context-icon').evaluateAll(nodes=>nodes.every(n=>!['At home','On the road','On the phone','On the computer'].includes(n.textContent))),'Task context markers use emojis only');
 // General Every X Y recurrence preserves the amount, unit, and start date.
 for(const [unit,amount]of [['week',2],['month',3],['year',1]]){
  await page.evaluate(()=>TaskListApp.addTask({labels:['V7 QA']}));
  await editor.locator('input[type=text]').first().fill('General repeat '+unit);
  await editor.locator('[data-field=repeat] input[value=repeat]').check();
  await editor.locator('.tl-repeat-amount').fill(String(amount));await editor.locator('.tl-repeat-unit').selectOption(unit);
  if(unit==='week'){
   await editor.locator('.tl-weekdays input[type=checkbox]').evaluateAll(inputs=>inputs.forEach(i=>i.checked=false));
   await editor.locator('input[aria-label=Monday]').check();
  }
  await editor.getByRole('button',{name:'Save task',exact:true}).click();
  const task=await page.evaluate(title=>__v7.getState().customTasks.find(t=>t.title===title),'General repeat '+unit);
  check(task.repeat.amount===amount&&task.repeat.unit===unit&&!!task.repeat.start,'Every '+amount+' '+unit+' recurrence saves correctly');
 }
 check(await page.locator('#pt-list .form-check .tm-meta').count()===0,'Checkbox label contains the title rather than the metadata block');
 // Upcoming has its own complete controls and visible grouping.
 await page.getByRole('tab',{name:'Upcoming',exact:true}).click();
 check(await futureRows(ids.dailyA).count()===14&&await futureRows(ids.dailyB).count()===14,'Daily tasks pending on Today also show each of the next fourteen occurrences in Upcoming');
 check(await futureRows(durationTask.id).count()===0,'Nonrecurring tasks pending on Today stay excluded from Upcoming');
 const dailyDates=await futureRows(ids.dailyB).evaluateAll(nodes=>nodes.map(n=>n.dataset.stateDate));check(dailyDates[0]===dates.tomorrow&&!dailyDates.includes(dates.today)&&new Set(dailyDates).size===14,'Upcoming daily tasks start tomorrow with unique dates');
 await futureRows(ids.dailyB).first().locator(':scope > .form-check > input[type=checkbox]').click();state=await page.evaluate(()=>__v7.getState());check(state.completed[ids.dailyB+':'+dates.tomorrow]===true&&!state.completed[ids.dailyB+':'+dates.today],'Completing a future daily occurrence leaves today uncompleted');
 await futureRows(ids.dailyB).first().locator(':scope > .form-check > input[type=checkbox]').click();
 check(await futureRows(ids.future).count()===1,'An unfinished carried future occurrence is shown once');
 const repeatDates=await futureRows(ids.futureRepeat).evaluateAll(nodes=>nodes.map(n=>n.dataset.stateDate));
 check(repeatDates.length>1&&new Set(repeatDates).size===repeatDates.length,'Distinct future repeat occurrences remain available without duplicate dates');
 const tomorrowDetails=page.locator('#pt-future-list details.tm-day').filter({has:page.locator('[data-task-id="'+ids.future+'"]')}).first();
 check(await tomorrowDetails.evaluate(d=>d.open),'Tomorrow date group is expanded');
 const later=await page.locator('#pt-future-list details.tm-day').evaluateAll(groups=>groups.slice(1).every(d=>!d.open));
 check(await page.locator('#pt-future-list details.tm-day').evaluateAll(nodes=>nodes.every(n=>getComputedStyle(n).borderTopWidth==='0px'&&getComputedStyle(n).borderRadius==='0px')),'Upcoming dates have no enclosing frames');
 check(later,'Later Upcoming dates start in separate collapsed dropdowns');
 check(await tomorrowDetails.locator('summary.tm-day-heading').evaluate(summary=>parseFloat(getComputedStyle(summary).fontSize)>=16),'Upcoming date headings are visually prominent');
 const futureRow=futureRows(ids.future).first();
 check(await futureRow.locator(':scope > .form-check > input[type=checkbox]').count()===1,'Upcoming tasks have a completion checkbox');
 await futureRow.locator('.tm-progress-control').click();
 await page.waitForFunction(({id,date})=>TaskListApp.debugSummary().active===id+':'+date,{id:ids.future,date:dates.tomorrow});
 check(await futureRows(ids.future).first().evaluate(r=>r.classList.contains('tm-in-progress-item')&&r.textContent.includes('IN PROGRESS')),'Upcoming Do now visibly marks the task in progress');
 await page.evaluate(()=>TaskListApp.showToday());
 check(await page.locator('#pt-today').isVisible()&&await page.locator('#pt-current').textContent().then(t=>t.includes('Tomorrow once task')),'An upcoming active task remains visible in the Today progress banner');
 await page.getByRole('tab',{name:'Upcoming',exact:true}).click();
 await futureRows(ids.future).first().locator(':scope > .form-check > input[type=checkbox]').click();
 state=await page.evaluate(()=>__v7.getState());
 check(state.completed[ids.future+':'+dates.tomorrow]===true&&state.completedOn[ids.future+':'+dates.tomorrow]===dates.today,'Upcoming completion saves immediately with today as actual completion date');
 await page.waitForTimeout(2200);
 check(await futureRows(ids.future).count()===0,'Completed future task leaves Upcoming after its completion pause');
 const moveDay=page.locator('#pt-future-list details.tm-day[data-date="'+dates.second+'"]');await moveDay.locator(':scope > summary').click();
 const moveToday=futureRows(ids.second).first().locator('.tl-do-today');
 check(await moveToday.textContent()==='➜Do today'&&await moveToday.locator('.tl-today-arrow').evaluate(n=>getComputedStyle(n).transform==='matrix(-1, 0, 0, -1, 0, 0)'),'Upcoming has a Do today button with the matching thick arrow facing back');
 const moveStyle=await moveToday.evaluate(n=>{const gear=n.closest('.tm-item-actions').querySelector('.tl-edit-button'),a=getComputedStyle(n),b=getComputedStyle(gear);return a.height===b.height&&a.borderTopColor===b.borderTopColor&&a.color===b.color;});check(moveStyle,'Do today matches the other task controls');
 await moveToday.click();state=await page.evaluate(()=>__v7.getState());
 check(state.movedTo[ids.second+':'+dates.second]===dates.today&&state.customTasks.find(t=>t.id===ids.second).due===dates.second,'Do today saves an occurrence move without replacing the original task date');
 check(await futureRows(ids.second).count()===0&&await todayRow(ids.second).getAttribute('data-state-date')===dates.second&&await todayRow(ids.second).getAttribute('data-display-date')===dates.today,'Moved task appears on Today with its original identity and leaves Upcoming');
 await page.waitForFunction(({id,date})=>__v7.snapshot.some(t=>t.id==='html:'+id+':'+date&&t.onToday),{id:ids.second,date:dates.second});check(true,'The home widget receives the moved Today occurrence');
 // Repeating schedules cannot be manually advanced or postponed through task-row controls.
 const dailyPage=await newPage(state);await dailyPage.evaluate(({date})=>{const s=__v7.getState();for(const unit of ['week','month','year'])s.customTasks.push({id:'fixed-'+unit,title:'Fixed '+unit,priority:'HIGH',_contexts:['V7 QA'],repeat:{amount:1,unit,start:date}});__v7.setState(s);TaskListApp.reloadFromNative();TaskListApp.setContextFilter('V7 QA');},{date:dates.tomorrow});await dailyPage.getByRole('tab',{name:'Upcoming',exact:true}).click();
 for(const id of [ids.dailyA,ids.futureRepeat,'fixed-week','fixed-month','fixed-year']){
  const task=dailyPage.locator('#pt-future-list [data-task-id="'+id+'"]').first();check(await task.count()===1&&await task.locator(':scope>.tm-item-actions>button[aria-label^="Move "]').count()===0,'Repeating '+id+' has no Do today or Do tomorrow control in Upcoming');
 }
 await dailyPage.getByRole('tab',{name:'Today',exact:true}).click();check(await dailyPage.locator('#pt-list [data-task-id="'+ids.dailyA+'"]').locator(':scope>.tm-item-actions>button[aria-label^="Move "]').count()===0,'Daily tasks also have no day-move button on Today');await dailyPage.close();
 await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);
 check(await page.locator('#pt-today').isVisible()&&!await page.locator('#pt-upcoming').isVisible(),'Reopening Tasks always selects Today');
 check(await todayRow(ids.second).isVisible()&&await todayRow(ids.second).getAttribute('data-state-date')===dates.second,'Do today persists after a full reload');
 await page.screenshot({path:path.join(output,'tasks-today-v7.png'),fullPage:false});
 await page.getByRole('tab',{name:'Upcoming',exact:true}).click();
 await page.screenshot({path:path.join(output,'tasks-upcoming-v7.png'),fullPage:false});
 // A companion/widget can change native state while this document still has an older cache.
 await page.evaluate(()=>TaskListApp.showToday());
 check(await todayRow(ids.dailyA).isVisible()&&await todayRow(ids.dailyB).isVisible(),'Stale-native fixture begins with both repeating tasks open in the document');
 const remoteId='v7-native-remote-add';
 await page.evaluate(({ids,dates,remoteId})=>{
  const remote=__v7.getState();
  remote.completed={...remote.completed,[ids.dailyB+':'+dates.today]:true};
  remote.completedOn={...remote.completedOn,[ids.dailyB+':'+dates.today]:dates.today};
  remote.customTasks=[...(remote.customTasks||[]),{id:remoteId,title:'Task added by companion',due:dates.today,priority:'HIGH',labels:['V7 QA'],minutes:5}];
  __v7.setState(remote);
  // Deliberately do not call reloadFromNative: the page's task engine is stale here.
 },{ids,dates,remoteId});
 await todayRow(ids.dailyA).locator(':scope > .form-check > input[type=checkbox]').click();
 state=await page.evaluate(()=>__v7.getState());
 check(state.completed[ids.dailyA+':'+dates.today]===true&&state.completed[ids.dailyB+':'+dates.today]===true,'A local checkbox save merges its change without undoing an unseen native completion');
 check(state.completedOn[ids.dailyB+':'+dates.today]===dates.today&&state.customTasks.some(task=>task.id===remoteId),'Stale-page persistence preserves the remote completion date and newly added task');
 await page.evaluate(()=>TaskListApp.setContextFilter('V7 QA'));
 await page.waitForTimeout(2200);await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);
 state=await page.evaluate(()=>__v7.getState());
 check(state.completed[ids.dailyA+':'+dates.today]===true&&state.completed[ids.dailyB+':'+dates.today]===true,'Local and previously unseen remote completions both survive context changes and a full reload');
 check(await todayRow(remoteId).isVisible()&&!await todayRow(ids.dailyA).isVisible()&&!await todayRow(ids.dailyB).isVisible(),'Reload displays the companion task and keeps both completed tasks hidden');
 for(const mode of ['daily','weekends']){
  await page.evaluate(()=>TaskListApp.addTask());
  await editor.locator('input[type=text]').first().fill('Shortcut repeat '+mode);
  await editor.locator('[data-field=repeat] input[value='+mode+']').check();
  await editor.getByRole('button',{name:'Save task',exact:true}).click();
  const task=await page.evaluate(mode=>__v7.getState().customTasks.find(t=>t.title==='Shortcut repeat '+mode),mode);
  check(mode==='daily'?task.repeat?.amount===1&&task.repeat?.unit==='day':JSON.stringify(task.days)==='[0,6]',mode+' shortcut persists the correct schedule');
 }
 await page.getByRole('button',{name:'Settings',exact:true}).click();
 await editor.locator('[aria-label="New context"]').fill('Workshop');await editor.getByRole('button',{name:'Add context',exact:true}).click();
 await editor.getByRole('button',{name:'Close',exact:true}).click();
 await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);
 await page.evaluate(()=>TaskListApp.addTask());
 check(await editor.locator('label[aria-label="Workshop"]').count()===1,'Custom contexts added in Settings survive reload and can be assigned');
 await editor.getByRole('button',{name:'Cancel',exact:true}).click();
 for(const full of [false,true]){
  if(full)await page.evaluate(()=>TaskListApp.addTask());else await page.getByRole('button',{name:'Add task',exact:true}).click();
  const box=await editor.boundingBox();await page.mouse.click(2,Math.max(2,box.y-3));
  check(await page.locator('dialog').count()===0,(full?'Full editor':'Quick add')+' closes when tapping the backdrop');
 }
 const tapState=await page.evaluate(()=>__v7.getState());tapState.customTasks.push({id:'tap-edit',title:'Component edits',priority:'HIGH',repeat:{amount:1,unit:'day',start:dates.today},time:'2:00 PM',minutes:15,notify:true,notes:'Preserve notes',steps:['Finished step','Open step'],labels:['Tap QA']},{id:'tap-future',title:'Future component edits',priority:'HIGH',due:dates.tomorrow,time:'3:00 PM',labels:['Tap QA']});tapState.completed['tap-edit:'+dates.today+':step:0']=true;tapState.completedOn['tap-edit:'+dates.today+':step:0']=dates.today;tapState.manualLinks={...tapState.manualLinks,'tap-edit':[ids.dailyB]};
 const tapPage=await newPage(tapState);await tapPage.evaluate(()=>TaskListApp.setContextFilter('Tap QA'));const tapRow=()=>tapPage.locator('#pt-list [data-task-id="tap-edit"]'),tapDialog=()=>tapPage.locator('dialog[open]');
 let beforeTap=await tapPage.evaluate(()=>__v7.getState());await tapRow().locator(':scope > .form-check .tm-title').click();await tapRow().locator('.tl-label-edit').first().click();check(await tapDialog().getByRole('heading',{name:'Labels',exact:true}).count()===1,'Label emojis open the focused label editor');await tapDialog().getByRole('button',{name:'Cancel',exact:true}).click();await tapRow().locator('.tm-children summary').click();await tapRow().locator('.tm-child-item .tm-title').last().click();check(JSON.stringify((await tapPage.evaluate(()=>__v7.getState())).completed)===JSON.stringify(beforeTap.completed),'Tapping parent titles, checklist titles and labels does not complete tasks');
 await tapRow().locator('.tl-meta-time').click();check(await tapDialog().getByRole('heading',{name:'Time',exact:true}).count()===1&&await tapDialog().locator('input[type=time]').count()===1&&await tapDialog().locator('textarea,[data-field=priority],[data-field=repeat]').count()===0,'Tapping time opens a focused time popup rather than the full editor');
 check(await tapDialog().locator('input[type=time]').inputValue()==='14:00','Time popup loads the existing scheduled time');await tapDialog().getByRole('button',{name:'Cancel',exact:true}).click();check(JSON.stringify(await tapPage.evaluate(()=>__v7.getState()))===JSON.stringify(beforeTap),'Cancelling a component popup preserves all saved state');
 await tapRow().locator('.tl-meta-time').click();await tapDialog().locator('input[type=time]').fill('09:30');await tapDialog().getByRole('button',{name:'Save',exact:true}).click();let tapSaved=await tapPage.evaluate(()=>__v7.getState()),tapTask=tapSaved.customTasks.find(t=>t.id==='tap-edit');check(tapTask.time==='9:30 AM'&&tapTask.notify&&tapTask.minutes===15&&tapTask.repeat.amount===1,'Time-only edit updates the time and preserves notification, duration and repeat settings');
 await tapRow().locator('.tl-meta-duration').click();await tapDialog().getByRole('spinbutton',{name:'Hours',exact:true}).fill('1');await tapDialog().getByRole('spinbutton',{name:'Minutes',exact:true}).fill('5');await tapDialog().getByRole('button',{name:'Save',exact:true}).click();tapTask=(await tapPage.evaluate(()=>__v7.getState())).customTasks.find(t=>t.id==='tap-edit');check(tapTask.minutes===65&&tapTask.time==='9:30 AM','Duration popup changes just hours and minutes');
 await tapRow().locator('.tl-meta-frequency').click();check(await tapDialog().getByRole('heading',{name:'Frequency',exact:true}).count()===1&&await tapDialog().locator('input[type=time],textarea,[data-field=priority]').count()===0,'Tapping Daily opens only the frequency controls');await tapDialog().locator('[data-field=repeat] input[value=repeat]').check();await tapDialog().locator('.tl-repeat-amount').fill('2');await tapDialog().locator('.tl-repeat-unit').selectOption('day');await tapDialog().getByRole('button',{name:'Save',exact:true}).click();tapSaved=await tapPage.evaluate(()=>__v7.getState());tapTask=tapSaved.customTasks.find(t=>t.id==='tap-edit');check(tapTask.repeat.amount===2&&tapTask.repeat.unit==='day'&&tapTask.repeat.start===dates.today&&tapTask.time==='9:30 AM'&&tapTask.minutes===65,'Frequency-only editing saves Every 2 days without changing other task details');check(tapTask.notes==='Preserve notes'&&JSON.stringify(tapTask.steps)==='["Finished step","Open step"]'&&JSON.stringify(tapSaved.completed)===JSON.stringify(beforeTap.completed)&&JSON.stringify(tapSaved.completedOn)===JSON.stringify(beforeTap.completedOn)&&JSON.stringify(tapSaved.manualLinks)===JSON.stringify(beforeTap.manualLinks),'Component saves preserve checklist progress, completion history, notes and manual links');
 await tapRow().locator('.tl-meta-time').click();await tapDialog().getByRole('button',{name:'Clear',exact:true}).click();check(await tapDialog().getByRole('checkbox',{name:'Notify at scheduled time'}).isDisabled(),'Clearing the time disables scheduled notification');await tapDialog().getByRole('button',{name:'Save',exact:true}).click();tapTask=(await tapPage.evaluate(()=>__v7.getState())).customTasks.find(t=>t.id==='tap-edit');check(!tapTask.time&&!tapTask.notify&&await tapRow().locator('.tl-meta-time').textContent()==='Any time','Clearing time removes only time and its scheduled notification');
 await tapPage.reload();await tapPage.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);check(await tapRow().locator('.tl-meta-duration').textContent()==='⏰ 1 hr 5 min'&&await tapRow().locator('.tl-meta-frequency').textContent().then(t=>t.includes('2 days')),'Component edits persist after reopening');
 await tapRow().locator(':scope > .form-check > input').click();check((await tapPage.evaluate(()=>__v7.getState())).completed['tap-edit:'+dates.today+':step:1']===true,'Clicking the checkbox still completes the task and checklist');await tapRow().locator(':scope > .form-check > input').click();
 await tapPage.getByRole('tab',{name:'Upcoming',exact:true}).click();await tapPage.locator('#pt-future-list [data-task-id="tap-future"] .tl-meta-time').click();await tapDialog().locator('input[type=time]').fill('16:45');await tapDialog().getByRole('button',{name:'Save',exact:true}).click();tapTask=(await tapPage.evaluate(()=>__v7.getState())).customTasks.find(t=>t.id==='tap-future');check(tapTask.time==='4:45 PM'&&tapTask.due===dates.tomorrow,'Upcoming metadata supports direct editing without moving or completing the task');await tapPage.close();
 await page.evaluate(({id,date})=>{const remote=__v7.getState();remote.inProgress=id+':'+date;remote.progressStateInitialized=true;__v7.setState(remote);TaskListApp.reloadFromNative();TaskListApp.showToday();},{id:durationTask.id,date:dates.today});
 check(await page.locator('#pt-current').textContent().then(text=>text.includes('Duration conversion QA'))&&await todayRow(durationTask.id).getAttribute('class').then(value=>value.includes('tm-in-progress-item')),'The app reloads widget-written in-progress state and displays its active task');
 await page.evaluate(()=>{const remote=__v7.getState();remote.inProgress=null;__v7.setState(remote);TaskListApp.reloadFromNative();});check(await page.locator('#pt-current').isHidden(),'Stopping from the widget clears the app progress banner');
 await page.evaluate(()=>{const s=__v7.getState(),today=TaskListApp.debugSummary().today;s.customTasks.push({id:'multi-a',title:'First active QA',due:today,priority:'HIGH'},{id:'multi-b',title:'Second active QA',due:today,priority:'LOW'});s.inProgress=null;s.inProgressTasks=[];__v7.setState(s);TaskListApp.reloadFromNative();TaskListApp.setContextFilter('');document.getElementById('pt-today-tab').click();});
 for(const id of ['multi-a','multi-b'])await todayRow(id).locator(':scope>.tm-item-actions>.tm-progress-control').click();
 check(await page.locator('#pt-current').textContent().then(t=>t.includes('First active QA, Second active QA')),'Multiple active tasks appear together separated by commas');check((await page.evaluate(()=>__v7.getState())).inProgressTasks.length===2,'Progress stores independent active occurrence identities');
 await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);check(await todayRow('multi-a').getAttribute('class').then(t=>t.includes('tm-in-progress-item'))&&await todayRow('multi-b').getAttribute('class').then(t=>t.includes('tm-in-progress-item')),'Both active tasks retain their state after reload');await todayRow('multi-a').locator(':scope>.form-check>input').scrollIntoViewIfNeeded();const beforeBox=await todayRow('multi-a').locator(':scope>.form-check>input').boundingBox();await todayRow('multi-a').locator(':scope>.form-check>input').check();const afterBox=await todayRow('multi-a').locator(':scope>.form-check>input').boundingBox();check(Math.abs(beforeBox.y-afterBox.y)<1,'Completing one of several active tasks retains its undo checkbox position '+JSON.stringify({beforeBox,afterBox}));await todayRow('multi-a').locator(':scope>.form-check>input').click();check(await page.evaluate(()=>__v7.getState().inProgressTasks.length===1),'Undoing completion preserves the other active task');check(await page.locator('#pt-current').textContent().then(t=>!t.includes('First active QA')&&t.includes('Second active QA')),'Stopping one active task leaves the other active');
 await page.getByRole('tab',{name:'Upcoming',exact:true}).click();check(await page.locator('.tm-day-name').allTextContents().then(names=>names.length>0&&names.every(name=>/^[A-Z][a-z]{2} \d+(st|nd|rd|th)$/.test(name))),'Every Upcoming section including tomorrow uses a month, date and ordinal');
 check(errors.length===0,'No page errors in Tasks v7 behavior checks');
 console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();await fixture.close();}
