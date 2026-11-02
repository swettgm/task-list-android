import {chromium} from 'playwright';
import {readFile,mkdir} from 'node:fs/promises';
import {existsSync} from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
const file=path.resolve('app/src/main/assets/www/index.html'),seed=JSON.parse(await readFile(path.join(path.dirname(file),'initial-state.json'),'utf8'));
seed.contextIcons={Guild:'👑',Raid:'⚔️'};seed.taskOverrides={...(seed.taskOverrides||{}),guild:{id:'guild',title:'Weekly Guild Checklist',priority:'MEDIUM',minutes:20,days:[2],recurrence:'Tuesdays',children:true,childrenChangedOn:'2026-11-03',notes:'Keep this note',_contexts:['At home']}};
const browser=await chromium.launch({headless:true,executablePath:['/usr/bin/google-chrome','/usr/bin/chromium'].find(existsSync),args:['--no-sandbox','--disable-dev-shm-usage']});
const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true}),checks=[],errors=[];const check=(v,s)=>{if(!v)throw Error(s);checks.push(s);};page.on('pageerror',e=>errors.push(e.message));
await page.addInitScript(initial=>{let state=JSON.parse(sessionStorage.getItem('guild-qa')||JSON.stringify(initial));window.__guild={get:()=>JSON.parse(JSON.stringify(state)),snapshot:[]};window.TaskListNative={readState:()=>JSON.stringify(state),writeState:s=>{state=JSON.parse(s);sessionStorage.setItem('guild-qa',s);},updateSnapshot:s=>__guild.snapshot=JSON.parse(s),setThemeColors(){},showWidgetSettings(){}};},seed);
try{
 await page.goto(pathToFileURL(file).href);await page.waitForFunction(()=>window.TaskListApp&&__guild.snapshot.length);
 const state=await page.evaluate(()=>__guild.get());check(state.guildIconVersion===1&&state.contextIcons.Guild==='image:guild','Guild label adopts the supplied image once on upgrade');check(state.contextIcons.Raid==='⚔️','Existing icons for other labels are preserved');check(state.taskOverrides.guild.notes==='Keep this note'&&JSON.stringify(state.taskOverrides.guild._contexts)==='["At home","Guild"]','Existing Guild task keeps its edits and contexts while gaining the Guild label');
 await page.evaluate(()=>TaskListApp.addTask({title:'Guild image QA',labels:['Guild','At home']}));const editor=page.locator('dialog');
 const imageChoice=editor.locator('label[aria-label=Guild] img');check(await imageChoice.count()===1,'Task editor offers Guild with the actual image');check(await imageChoice.evaluate(n=>n.complete&&n.naturalWidth===64&&n.naturalHeight===64),'Guild image is a loaded static 64px PNG');
 const expected=(await readFile('app/src/main/res/drawable-nodpi/guild_label.png')).toString('base64');check((await imageChoice.getAttribute('src'))==='data:image/png;base64,'+expected,'Web and native Guild icons use exactly the same PNG asset');
 await editor.getByRole('button',{name:'Save task',exact:true}).click();await page.waitForFunction(()=>__guild.snapshot.some(t=>t.title==='Guild image QA'));
 const id=await page.evaluate(()=>__guild.snapshot.find(t=>t.title==='Guild image QA').id.slice(5).split(':')[0]),row=()=>page.locator('#pt-list [data-task-id="'+id+'"]');
 check(await row().locator('.tl-context-icon[aria-label=Guild] img').count()===1,'Task row renders the Guild image label');
 const boxes=await row().locator('.tl-context-icon').evaluateAll(nodes=>nodes.map(n=>{const r=n.getBoundingClientRect();return{width:r.width,height:r.height,font:getComputedStyle(n).fontSize};}));check(boxes.length===2&&Math.abs(boxes[0].width-boxes[1].width)<1&&Math.abs(boxes[0].height-boxes[1].height)<1&&boxes[0].font===boxes[1].font,'Guild and normal emoji labels occupy identical row boxes');
 await page.evaluate(()=>TaskListApp.setContextFilter('Guild'));check(await page.locator('#tl-context-filter-preview img').isVisible()&&await row().isVisible(),'Selected Guild filter displays the image and filters correctly');
 await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__guild.snapshot.length);check(await row().locator('.tl-context-icon[aria-label=Guild] img').count()===1,'Guild image persists across reload');
 await page.getByRole('button',{name:'Settings',exact:true}).click();await editor.locator('[aria-label="New context"]').fill('Guild projects');await editor.getByRole('button',{name:'Choose Guild image',exact:true}).click();
 check(await editor.locator('[aria-label="Label emoji"]').inputValue()==='','Choosing a label image does not expose an internal token');await editor.getByRole('button',{name:'Add context',exact:true}).click();check(await page.evaluate(()=>__guild.get().contextIcons['Guild projects']==='image:guild'),'The same image can be chosen for a new label');
 await page.setViewportSize({width:320,height:844});await editor.getByRole('button',{name:'Choose Guild image',exact:true}).scrollIntoViewIfNeeded();check(await editor.evaluate(n=>n.scrollWidth<=n.clientWidth+1),'Guild image picker fits a narrow phone');await mkdir('build/browser-qa',{recursive:true});await page.screenshot({path:'build/browser-qa/guild-label-picker.png'});
 await editor.getByRole('button',{name:'Close',exact:true}).click();await page.setViewportSize({width:390,height:844});await page.screenshot({path:'build/browser-qa/guild-task-label.png'});
 check(!errors.length,'Guild image labels have no browser errors');console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();}
