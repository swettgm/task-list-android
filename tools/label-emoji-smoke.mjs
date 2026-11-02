import {chromium} from 'playwright';
import {readFile,mkdir} from 'node:fs/promises';
import {existsSync} from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
const file=path.resolve('app/src/main/assets/www/index.html'),seed=JSON.parse(await readFile(path.join(path.dirname(file),'initial-state.json'),'utf8'));
const browser=await chromium.launch({headless:true,executablePath:['/usr/bin/google-chrome','/usr/bin/chromium'].find(existsSync),args:['--no-sandbox','--disable-dev-shm-usage']});
const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true}),checks=[],errors=[];
const check=(v,s)=>{if(!v)throw Error(s);checks.push(s);};page.on('pageerror',e=>errors.push(e.message));
await page.addInitScript(initial=>{let state=JSON.parse(sessionStorage.getItem('emoji-qa')||JSON.stringify(initial));window.__emoji={get:()=>JSON.parse(JSON.stringify(state)),snapshot:[]};window.TaskListNative={readState:()=>JSON.stringify(state),writeState:s=>{state=JSON.parse(s);sessionStorage.setItem('emoji-qa',s);},updateSnapshot:s=>__emoji.snapshot=JSON.parse(s),setThemeColors(){},showWidgetSettings(){}};},seed);
const dialog=page.locator('dialog'),settings=()=>page.getByRole('button',{name:'Settings',exact:true}).click(),label=()=>dialog.locator('[aria-label="New context"]'),emoji=()=>dialog.locator('[aria-label="Label emoji"]'),add=()=>dialog.getByRole('button',{name:'Add context',exact:true}).click();
try{
 await page.goto(pathToFileURL(file).href);await page.waitForFunction(()=>window.TaskListApp&&__emoji.snapshot.length);
 await settings();check(await dialog.locator('.tl-emoji-choice').count()>=20,'New-label settings offer common emoji choices');
 await label().fill('Guild');await dialog.getByRole('button',{name:'Choose emoji 👑',exact:true}).click();check(await emoji().inputValue()==='👑','Picking an emoji fills the label emoji field');await add();
 check(await page.evaluate(()=>__emoji.get().contextIcons.Guild==='👑'&&__emoji.get().contextOptions.includes('Guild')),'Label name and selected emoji persist together');
 await dialog.getByRole('button',{name:'Close',exact:true}).click();
 await page.evaluate(()=>TaskListApp.addTask({title:'Guild emoji QA',labels:['Guild','At home']}));
 check(await dialog.locator('label[aria-label=Guild]').textContent().then(t=>t.includes('👑')),'Task editor displays the chosen emoji for the label');
 await dialog.getByRole('button',{name:'Save task',exact:true}).click();
 await page.waitForFunction(()=>__emoji.snapshot.some(t=>t.title==='Guild emoji QA'));
 const id=await page.evaluate(()=>__emoji.snapshot.find(t=>t.title==='Guild emoji QA').id.slice(5).split(':')[0]);
 const row=()=>page.locator('#pt-list [data-task-id="'+id+'"]');
 check(await row().locator('.tl-context-icon[aria-label=Guild]').textContent()==='👑','Task rows show the selected emoji');
 check(await page.locator('#tl-context-filter option[value=Guild]').textContent()==='👑','Context filter uses the selected emoji');
 const glyphs=await row().locator('.tl-context-icon').evaluateAll(nodes=>nodes.map(n=>({font:getComputedStyle(n).fontSize,line:getComputedStyle(n).lineHeight})));check(glyphs.length===2&&glyphs[0].font===glyphs[1].font&&glyphs[0].line===glyphs[1].line,'Custom emojis use the same font size and line height as built-in emojis');
 await page.evaluate(()=>TaskListApp.setContextFilter('Guild'));check(await row().isVisible(),'Changing the emoji preserves filtering by label identity');
 await page.reload();await page.waitForFunction(()=>window.TaskListApp&&__emoji.snapshot.length);check(await row().locator('.tl-context-icon[aria-label=Guild]').textContent()==='👑','Chosen label emoji survives reload');
 await settings();await label().fill('Guild');await emoji().fill('⚔️');await add();
 check(await page.evaluate(()=>__emoji.get().contextOptions.filter(x=>x==='Guild').length===1&&__emoji.get().contextIcons.Guild==='⚔️'),'Existing labels can change emoji without duplicate labels');
 check(await row().locator('.tl-context-icon[aria-label=Guild]').textContent()==='⚔️','Changing a label emoji updates existing tasks immediately');
 await label().fill('Computer work');await emoji().fill('👩🏽‍💻');await add();check(await page.evaluate(()=>__emoji.get().contextIcons['Computer work']==='👩🏽‍💻'),'A keyboard emoji with skin tone and joined characters is accepted');
 for(const invalid of ['abc','👑⚔️']){await label().fill('Invalid emoji');await emoji().fill(invalid);await add();check(await page.evaluate(()=>!__emoji.get().contextOptions.includes('Invalid emoji'))&&!!await dialog.locator('.tl-context-error').textContent(),'Reject invalid or multiple emoji: '+invalid);}
 await page.setViewportSize({width:320,height:844});await emoji().scrollIntoViewIfNeeded();check(await dialog.evaluate(n=>n.scrollWidth<=n.clientWidth+1),'Emoji picker fits a narrow phone without horizontal scrolling');
 await mkdir('build/browser-qa',{recursive:true});await page.screenshot({path:'build/browser-qa/label-emoji-picker.png'});check(!errors.length,'Emoji settings and label changes have no browser errors');console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();}
