// Edit existing checklists without moving completion state between steps.
import { chromium } from 'playwright';
import { readFile,mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import { pathToFileURL } from 'node:url';
const file=path.resolve('app/src/main/assets/www/index.html'),seed=JSON.parse(await readFile(path.join(path.dirname(file),'initial-state.json'),'utf8'));
const browser=await chromium.launch({headless:true,executablePath:['/usr/bin/google-chrome','/usr/bin/chromium'].find(existsSync),args:['--no-sandbox','--disable-dev-shm-usage']});
const checks=[],errors=[];const check=(v,s)=>{if(!v)throw Error(s);checks.push(s);};
async function openPage(initial,fixedTime){
 const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true});page.on('pageerror',e=>errors.push(e.message));
 await page.addInitScript(({initial,fixedTime})=>{
  if(fixedTime){const RealDate=Date;window.Date=class extends RealDate{constructor(...args){super(...(args.length?args:[fixedTime]));}static now(){return fixedTime;}};}
  let state=JSON.parse(sessionStorage.getItem('checklist-qa')||JSON.stringify(initial));window.__checklist={get:()=>JSON.parse(JSON.stringify(state)),set:s=>{state=JSON.parse(JSON.stringify(s));sessionStorage.setItem('checklist-qa',JSON.stringify(state));},snapshot:[]};
  window.TaskListNative={readState:()=>JSON.stringify(state),writeState:s=>__checklist.set(JSON.parse(s)),updateSnapshot:s=>__checklist.snapshot=JSON.parse(s),setThemeColors(){},showWidgetSettings(){}};
 },{initial,fixedTime});
 await page.goto(pathToFileURL(file).href);await page.waitForFunction(()=>window.TaskListApp&&__checklist.snapshot.length);return page;
}
const page=await openPage(seed);
try{
 const dates=await page.evaluate(()=>{const today=TaskListApp.debugSummary().today,add=n=>{const d=new Date(today+'T12:00:00Z');d.setUTCDate(d.getUTCDate()+n);return d.toISOString().slice(0,10);};return{today,yesterday:add(-1),tomorrow:add(1)};});
 await page.evaluate(d=>{
  const s=__checklist.get(),task=(id,title,extra={})=>({id,title,priority:'HIGH',labels:['Checklist QA'],days:[0,1,2,3,4,5,6],...extra});
  s.customTasks=[...(s.customTasks||[]),task('edit-plain','Plain editable task'),task('edit-steps','Existing checklist',{steps:['Alpha','Beta']}),task('edit-groups','Grouped checklist',{children:true})];
  s.completed={...s.completed,['edit-steps:'+d.today+':step:0']:true,['edit-steps:'+d.yesterday+':step:0']:true,['edit-groups:'+d.today+':child:0']:true};s.completedOn={...s.completedOn,['edit-steps:'+d.today+':step:0']:d.today};
  __checklist.set(s);TaskListApp.reloadFromNative();TaskListApp.setContextFilter('Checklist QA');
 },dates);
 const row=id=>page.locator('#pt-list > .tm-task-item[data-task-id="'+id+'"]');
 const editor=page.locator('dialog'),field=()=>editor.locator('textarea[placeholder="One step per line"]');
 const edit=async id=>{await row(id).locator('.tl-edit-button').click();await field().waitFor({state:'visible'});};
 const save=()=>editor.getByRole('button',{name:'Save task',exact:true}).click();
 const state=()=>page.evaluate(()=>__checklist.get());
 const boxes=id=>row(id).locator('.tm-children .form-check-input');
 const openSteps=async id=>{await row(id).locator('.tm-children').evaluate(n=>n.open=true);};
 await edit('edit-plain');check(await field().inputValue()==='','Editing a plain task offers an empty checklist field');
 await field().fill('One\n\nTwo');await save();await openSteps('edit-plain');
 check(await boxes('edit-plain').count()===2,'Subtasks can be added to an existing plain task');
 await boxes('edit-plain').first().check();
 let snapshot=await page.evaluate(()=>__checklist.snapshot.find(t=>t.title==='Plain editable task'));const firstKey=snapshot.completionKeys[0];
 await edit('edit-plain');check(await field().inputValue()==='One\nTwo','Existing edited steps repopulate the editor');
 await field().fill('Two\nOne\nThree');await save();await openSteps('edit-plain');
 check(!await boxes('edit-plain').nth(0).isChecked()&&await boxes('edit-plain').nth(1).isChecked()&&!await boxes('edit-plain').nth(2).isChecked(),'Reordering keeps completion attached to the matching step');
 check((await state()).completed[firstKey]===true,'Reordering keeps the original stable completion key');
 await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__checklist.snapshot.length);await openSteps('edit-plain');
 check(await boxes('edit-plain').nth(1).isChecked(),'Edited checklist and completion survive reload');
 await edit('edit-steps');check(await field().inputValue()==='Alpha\nBeta','Original checklist steps are editable');
 await field().fill('Beta\nAlpha\nGamma');await save();await openSteps('edit-steps');
 check(!await boxes('edit-steps').nth(0).isChecked()&&await boxes('edit-steps').nth(1).isChecked(),'Original numeric step keys retain the right completed step');
 check((await state()).completedOn['edit-steps:'+dates.today+':step:0']===dates.today,'Editing preserves the actual completion date');
 await edit('edit-steps');await field().fill('Alpha\nGamma');await save();await openSteps('edit-steps');
 check(await boxes('edit-steps').nth(0).isChecked()&&await boxes('edit-steps').count()===2,'Removing another step preserves retained progress');
 const beforeRename=(await state()).customTasks.find(t=>t.id==='edit-steps').checklistRevisions.at(-1).items[1].key;
 await edit('edit-steps');await field().fill('Alpha\nDelta');await save();
 check((await state()).customTasks.find(t=>t.id==='edit-steps').checklistRevisions.at(-1).items[1].key===beforeRename,'Renaming a step retains its identity');
 const saved=await state();
 const tomorrow=await openPage(saved,Date.parse(dates.tomorrow+'T17:00:00Z'));
 await tomorrow.locator('#pt-list > [data-task-id="edit-steps"] .tm-children').evaluate(n=>n.open=true);
 const tomorrowSteps=tomorrow.locator('#pt-list > [data-task-id="edit-steps"] .tm-children .form-check-input');
 check(await tomorrowSteps.count()===2&&!await tomorrowSteps.nth(0).isChecked(),'Repeating tasks keep edited steps with fresh progress on the next day');await tomorrow.close();
 const yesterday=await openPage(saved,Date.parse(dates.yesterday+'T17:00:00Z'));
 await yesterday.locator('#pt-list > [data-task-id="edit-steps"] .tl-edit-button').click();
 check(await yesterday.locator('dialog textarea[placeholder="One step per line"]').inputValue()==='Alpha\nBeta','Occurrences before the edit keep their original checklist');await yesterday.close();
 await edit('edit-steps');await field().fill('');await save();
 check(await row('edit-steps').locator('.tm-children').count()===0,'Clearing the field removes the checklist');
 await edit('edit-steps');await field().fill('Fresh step');await save();await openSteps('edit-steps');
 check(!await boxes('edit-steps').first().isChecked(),'Adding after removal does not reuse a deleted completed step');
 await edit('edit-groups');const grouped=await field().inputValue();check(grouped.includes('Recruiting needs')&&grouped.includes('Leadership requests'),'Original grouped subtasks populate the same editor field');
 await field().fill(grouped+'\nNew grouped step');await save();await openSteps('edit-groups');
 check(await boxes('edit-groups').first().isChecked()&&!await boxes('edit-groups').last().isChecked(),'Grouped checklist edits preserve child completion and start added steps unchecked');
 check((await state()).completed['edit-groups:'+dates.today+':child:0']===true,'Legacy grouped child keys remain intact');
 await mkdir('build/browser-qa',{recursive:true});await edit('edit-groups');await field().scrollIntoViewIfNeeded();await page.screenshot({path:'build/browser-qa/edit-checklist.png'});
 check(!errors.length,'Checklist edits have no browser errors');console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();}
