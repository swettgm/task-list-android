import {chromium} from 'playwright';
import {existsSync} from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
const browser=await chromium.launch({executablePath:process.env.CHROME_BIN||['/usr/bin/google-chrome','/usr/bin/chromium'].find(existsSync),args:['--no-sandbox','--disable-dev-shm-usage']});
const page=await browser.newPage({viewport:{width:390,height:844}}),errors=[];page.on('pageerror',e=>errors.push(e.message));
await page.addInitScript(()=>{
 let state={customTasks:[{id:'delete-series',title:'Delete repeating task',repeat:{amount:1,unit:'day',start:'2020-01-01'},priority:'LOW',notify:true,time:'6:00 PM'},{id:'delete-keeper',title:'Keep this task',repeat:{amount:1,unit:'day',start:'2020-01-01'},priority:'HIGH'}],completed:{'delete-series:2026-01-01':true},manualLinks:{'delete-series':['delete-keeper'],'delete-keeper':['delete-series']}};
 window.__del={get:()=>structuredClone(state),set:value=>state=value,snapshot:[],definitions:[],fail:false};
 window.TaskListNative={readState:()=>JSON.stringify(state),writeState:value=>{if(__del.fail)throw Error('Save failed');state=JSON.parse(value);},updateSnapshot:value=>__del.snapshot=JSON.parse(value),updateReminders:value=>__del.definitions=JSON.parse(value),setThemeColors(){},hideKeyboard(){}};
});
let passed=0;function check(value,message){if(!value)throw Error(message);passed++;}
const open=()=>page.evaluate(()=>TaskListApp.editWidgetTask(__del.snapshot.find(row=>row.id.startsWith('html:delete-series:')&&row.onToday).id));
const editor=()=>page.locator('dialog');
try{
 await page.goto(pathToFileURL(path.resolve('app/src/main/assets/www/index.html')).href);await page.waitForFunction(()=>window.TaskListApp&&__del.snapshot.some(row=>row.id.startsWith('html:delete-series:')&&row.onToday));
 await page.evaluate(()=>{const state=__del.get(),day=TaskListApp.debugSummary().today;state.inProgressTasks=['delete-series:'+day,'delete-keeper:'+day];state.inProgress='delete-keeper:'+day;state.progressStateInitialized=true;__del.set(state);TaskListApp.reloadFromNative();});await page.waitForTimeout(50);
 await open();check(await editor().locator('.tl-task-delete svg path').count()===1&&await editor().getByRole('button',{name:'Delete task',exact:true}).textContent()==='','Delete uses an outline trash icon, not an emoji');
 check(await editor().getByRole('button',{name:'Delete task',exact:true}).evaluate(button=>getComputedStyle(button).backgroundColor==='rgb(75, 80, 88)'),'Delete starts gray');
 await editor().getByRole('button',{name:'Delete task',exact:true}).click();check(await editor().getByRole('button',{name:'Confirm delete task',exact:true}).textContent()==='Are you sure?'&&await page.evaluate(()=>!__del.get().customTasks.find(t=>t.id==='delete-series').retired),'First tap asks without deleting');
 await editor().getByRole('button',{name:'Cancel',exact:true}).click();check(await page.evaluate(()=>!__del.get().customTasks.find(t=>t.id==='delete-series').retired),'Cancel retains task');
 await open();await editor().getByRole('button',{name:'Delete task',exact:true}).click();await editor().locator('input[type=text]').first().fill('Unsaved rename');check(await editor().getByRole('button',{name:'Delete task',exact:true}).count()===1,'Editing disarms delete confirmation');
 await editor().getByRole('button',{name:'Delete task',exact:true}).click();await page.evaluate(()=>__del.fail=true);await editor().getByRole('button',{name:'Confirm delete task',exact:true}).click();check(await editor().locator('[role=alert]').textContent().then(text=>text.includes('Save failed'))&&await page.evaluate(()=>!__del.get().customTasks.find(t=>t.id==='delete-series').retired),'Failed deletion keeps the task and editor');
 await page.evaluate(()=>__del.fail=false);await editor().locator('input[type=text]').first().fill('Delete repeating task');await editor().getByRole('button',{name:'Delete task',exact:true}).click();
 const confirm=editor().getByRole('button',{name:'Confirm delete task',exact:true}),box=await confirm.boundingBox();await page.mouse.move(box.x+box.width/2,box.y+box.height/2);await page.mouse.down();check(await confirm.evaluate(button=>getComputedStyle(button).backgroundColor==='rgb(180, 35, 54)'),'Second confirmation press turns red');await page.mouse.up();await page.waitForTimeout(100);
 check(await page.locator('dialog').count()===0&&await page.evaluate(()=>__del.get().customTasks.find(t=>t.id==='delete-series').retired===true),'Second tap deletes the repeating definition and closes editor');
 check(await page.evaluate(()=>__del.get().completed['delete-series:2026-01-01']===true),'Deletion retains saved completion history');
 check(await page.evaluate(()=>{const state=__del.get();return state.inProgressTasks.length===1&&state.inProgressTasks[0].startsWith('delete-keeper:')&&state.inProgress===state.inProgressTasks[0];}),'Deletion clears only the matching active task');
 check(await page.evaluate(()=>{const state=__del.get();return !(state.manualLinks['delete-series']||[]).length&&!(state.manualLinks['delete-keeper']||[]).includes('delete-series');}),'Deletion removes links on both sides');
 check(await page.evaluate(()=>!__del.snapshot.some(row=>row.id.startsWith('html:delete-series:'))&&__del.snapshot.some(row=>row.id.startsWith('html:delete-keeper:'))),'Widget immediately drops deleted occurrences and retains other tasks');
 check(await page.evaluate(()=>__del.definitions.find(task=>task.id==='delete-series').retired===true),'Reminder planner receives retired definition');
 await page.getByRole('tab',{name:'Upcoming',exact:true}).click();check(await page.locator('#pt-future-list [data-task-id="delete-series"]').count()===0,'Future daily recurrences no longer appear');
 await page.evaluate(()=>TaskListApp.reloadFromNative());await page.waitForTimeout(50);check(await page.locator('[data-task-id="delete-series"]').count()===0,'Deleted task stays deleted after reload');
 check(errors.length===0,'No deletion browser errors');console.log(JSON.stringify({passed,suite:'task deletion confirmation'}));
}finally{await browser.close();}
