// Targeted regression checks for Tasks scheduling, open-task linking, and Upcoming.
import { chromium } from 'playwright';
import { readFile, mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import { pathToFileURL } from 'node:url';
const documentPath=path.resolve(process.argv[2]||'app/src/main/assets/www/index.html');
const seedPath=path.resolve(process.argv[3]||path.join(path.dirname(documentPath),'initial-state.json'));
const seed=JSON.parse(await readFile(seedPath,'utf8'));
const output=path.resolve(process.env.TASK_SCREENSHOT_DIR||'build/browser-qa');await mkdir(output,{recursive:true});
const executablePath=process.env.CHROME_BIN||['/usr/bin/google-chrome','/usr/bin/chromium','/opt/google/chrome/chrome'].find(existsSync);
const browser=await chromium.launch({headless:true,executablePath,args:['--no-sandbox','--disable-dev-shm-usage']});
const checks=[],errors=[];
function check(value,label){if(!value)throw Error(label);checks.push(label);}
async function newPage(state,fixedTime){
 const page=await browser.newPage({viewport:{width:390,height:844},deviceScaleFactor:1});page.on('pageerror',error=>errors.push(error.message));
 await page.addInitScript(({initial,fixedTime})=>{
  if(fixedTime){const RealDate=Date;class FixedDate extends RealDate{constructor(...args){super(...(args.length?args:[fixedTime]));}static now(){return fixedTime;}}window.Date=FixedDate;}
  const key='tasks-v7-state';let current=JSON.parse(sessionStorage.getItem(key)||JSON.stringify(initial));
  const setState=value=>{current=JSON.parse(JSON.stringify(value));sessionStorage.setItem(key,JSON.stringify(current));};
  window.__v7={snapshot:[],getState:()=>JSON.parse(JSON.stringify(current)),setState};
  window.TaskListNative={readState:()=>JSON.stringify(current),writeState:text=>setState(JSON.parse(text)),updateSnapshot:text=>{__v7.snapshot=JSON.parse(text);},setThemeColors(){},showWidgetSettings(){}};
 },{initial:state,fixedTime});
 await page.goto(pathToFileURL(documentPath).href);await page.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);return page;
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
 await editor.getByRole('button',{name:'Link task',exact:true}).click();
 await editor.getByRole('button',{name:'Save task',exact:true}).click();
 await page.waitForFunction(id=>{const row=[...document.querySelectorAll('#pt-list [data-task-id]')].find(r=>r.dataset.taskId===id);return row?.textContent.includes('Daily link B')&&row.textContent.includes('🔗');},ids.dailyA);
 check(await todayRow(ids.dailyA).textContent().then(t=>t.includes('Daily link B')&&t.includes('🔗')),'Newly linked repeating task shows its link icon and peer immediately');
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
 check(Math.abs((firstBox.y+firstBox.height/2)-(firstTitle.y+firstTitle.height/2))<=2.1,'Checkbox optically aligns with task title text');
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
 await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__v7.snapshot.length);
 check(await page.locator('#pt-today').isVisible()&&!await page.locator('#pt-upcoming').isVisible(),'Reopening Tasks always selects Today');
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
 check(errors.length===0,'No page errors in Tasks v7 behavior checks');
 console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();}
