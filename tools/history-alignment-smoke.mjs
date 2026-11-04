import {chromium} from 'playwright';
import {readFile} from 'node:fs/promises';
import {existsSync} from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
const file=path.resolve('app/src/main/assets/www/index.html'),seed=JSON.parse(await readFile(path.join(path.dirname(file),'initial-state.json'),'utf8')),history=JSON.parse(await readFile('app/src/main/assets/changelog.json','utf8'));
const appVersion=(await readFile('app/build.gradle','utf8')).match(/versionName ['"]([^'"]+)['"]/)[1];
const browser=await chromium.launch({executablePath:process.env.CHROME_BIN||['/usr/bin/google-chrome','/usr/bin/chromium'].find(existsSync),args:['--no-sandbox','--disable-dev-shm-usage']});
const page=await browser.newPage({viewport:{width:390,height:844}}),errors=[];page.on('pageerror',error=>errors.push(error.message));
await page.addInitScript(initial=>{let state=initial;window.__history={get:()=>structuredClone(state)};window.TaskListNative={readState:()=>JSON.stringify(state),writeState:value=>state=JSON.parse(value),updateSnapshot(){},setThemeColors(){},showWidgetSettings(){}};},seed);
let passed=0;function check(value,message){if(!value)throw Error(message);passed++;}
const aligned=async locator=>locator.evaluateAll(nodes=>nodes.length>0&&nodes.every(node=>getComputedStyle(node).textAlign==='left'));
try{
 await page.goto(pathToFileURL(file).href);await page.waitForFunction(()=>window.TaskListApp);
 const before=await page.evaluate(()=>JSON.stringify(__history.get()));
 check(await aligned(page.locator('#tl-context-filter')),'Context dropdown text is left aligned');
 check(await aligned(page.locator('#today-task-list-20261023 .nav-link')),'Tab labels are left aligned within their existing buttons');
 await page.locator('.tl-add-task').click();check(await aligned(page.locator('dialog .tl-dialog-actions button')),'Quick add action labels are left aligned');await page.getByRole('button',{name:'Details',exact:true}).click();
 check(await aligned(page.locator('dialog .tl-date-shortcuts button,dialog .tl-clear-time,dialog .tl-dialog-actions button')),'Detailed editor button labels are left aligned');
 check(await aligned(page.locator('dialog input:not([type=radio]):not([type=checkbox]),dialog textarea')),'Detailed editor fields and placeholders are left aligned');
 check(await page.locator('dialog .tl-dialog-actions button').evaluateAll(nodes=>nodes.every(node=>getComputedStyle(node).alignItems==='center')),'Action button contents remain vertically centered');
 check(await aligned(page.locator('dialog select')),'Editor dropdown text is left aligned');await page.getByRole('button',{name:'Cancel',exact:true}).click();
 await page.locator('.tl-settings').click();
 check(await page.locator('.tl-settings-screen').isVisible()&&await page.locator('dialog').count()===0&&await page.evaluate(()=>document.querySelector('body>main').hidden),'Settings hides the task page instead of overlaying it');
 check(await aligned(page.locator('.tl-settings-screen button').filter({hasText:/^(Save reminders|Add context|Changelog|Close)$/})),'Settings text buttons remain left aligned');
 const settings=page.locator('.tl-settings-screen');await settings.getByRole('button',{name:'Changelog',exact:true}).click();
 const dialog=page.locator('dialog').last();check(await dialog.locator('h2').textContent()==='Changelog','Changelog opens directly from Settings');
 check(history[0].version===appVersion&&history.at(-1).version==='0.2.0'&&history.length>=38&&new Set(history.map(entry=>entry.version)).size===history.length,'History covers current release through the original published app');
 check(JSON.stringify(await dialog.locator('.tl-release-note').evaluateAll(nodes=>nodes.map(node=>({version:node.dataset.version,changes:[...node.querySelectorAll('li')].map(li=>li.textContent)}))))===JSON.stringify(history.map(({version,changes})=>({version,changes}))),'Every saved release and change appears in the app');
 check(await dialog.locator('.tl-release-note').first().evaluate(node=>node.open)&&await dialog.locator('.tl-release-note').last().evaluate(node=>!node.open),'Latest release is expanded and older versions are available separately');
 const oldest=dialog.locator('[data-version="0.2.0"]');await oldest.locator('summary').click();check(await oldest.locator('ul').isVisible(),'Earlier release notes can be expanded');
 for(const width of [320,390,600]){await page.setViewportSize({width,height:844});check(await dialog.evaluate(node=>node.scrollWidth<=node.clientWidth+1&&node.getBoundingClientRect().left>=0&&node.getBoundingClientRect().right<=innerWidth+1),'Changelog fits without sideways scrolling at '+width+'px');}
 await page.evaluate(()=>TaskListApp.handleBack());check(await page.locator('dialog').count()===0&&await page.locator('.tl-settings-screen h2').textContent()==='Settings','Back returns from Changelog to Settings');
 await page.locator('.tl-settings-screen').getByRole('button',{name:'Back to tasks',exact:true}).click();check(await page.evaluate(()=>JSON.stringify(__history.get()))===before,'Viewing history and alignment changes do not modify task state');
await page.evaluate(()=>{window.scrollTo(0,160);window.__settingsScroll=scrollY;document.querySelector('.tl-settings').click();});check(await page.evaluate(()=>TaskListApp.isInteracting()&&!document.querySelector('dialog')&&document.querySelector('.tl-settings-screen')!==null),'Settings participates in app navigation without a dialog');await page.evaluate(()=>TaskListApp.handleBack());check(await page.evaluate(()=>!document.querySelector('.tl-settings-screen')&&!document.querySelector('body>main').hidden&&Math.abs(scrollY-window.__settingsScroll)<1),'Android Back restores the task page and its scroll position'); check(errors.length===0,'No history or alignment browser errors');console.log(JSON.stringify({passed,suite:'settings history and left alignment'}));
}finally{await browser.close();}
