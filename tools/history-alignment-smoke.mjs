import {chromium} from 'playwright';
import {readFile} from 'node:fs/promises';
import {existsSync} from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
const file=path.resolve('app/src/main/assets/www/index.html'),seed=JSON.parse(await readFile(path.join(path.dirname(file),'initial-state.json'),'utf8')),history=JSON.parse(await readFile('app/src/main/assets/changelog.json','utf8'));
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
 check(await page.locator('.tl-priority-button').first().evaluate(button=>{const glyph=button.querySelector('.tl-priority-arrow').getBoundingClientRect(),box=button.getBoundingClientRect();return Math.abs(glyph.x+glyph.width/2-box.x-box.width/2)<1&&Math.abs(glyph.y+glyph.height/2-box.y-box.height/2)<1;}),'Icon-only priority arrows remain perfectly centered');
 await page.locator('.tl-settings').click();
 check(await aligned(page.locator('dialog button').filter({hasText:/^(Save reminders|Add context|Changelog|Close)$/})),'Settings text buttons remain left aligned');
 const settings=page.locator('dialog').last();await settings.getByRole('button',{name:'Changelog',exact:true}).click();
 const dialog=page.locator('dialog').last();check(await dialog.locator('h2').textContent()==='Changelog','Changelog opens directly from Settings');
 check(history[0].version==='0.40.0'&&history.at(-1).version==='0.2.0'&&history.length===38,'History covers current release through the original published app');
 check(JSON.stringify(await dialog.locator('.tl-release-note').evaluateAll(nodes=>nodes.map(node=>({version:node.dataset.version,changes:[...node.querySelectorAll('li')].map(li=>li.textContent)}))))===JSON.stringify(history.map(({version,changes})=>({version,changes}))),'Every saved release and change appears in the app');
 check(await dialog.locator('.tl-release-note').first().evaluate(node=>node.open)&&await dialog.locator('.tl-release-note').last().evaluate(node=>!node.open),'Latest release is expanded and older versions are available separately');
 const oldest=dialog.locator('[data-version="0.2.0"]');await oldest.locator('summary').click();check(await oldest.locator('ul').isVisible(),'Earlier release notes can be expanded');
 for(const width of [320,390,600]){await page.setViewportSize({width,height:844});check(await dialog.evaluate(node=>node.scrollWidth<=node.clientWidth+1&&node.getBoundingClientRect().left>=0&&node.getBoundingClientRect().right<=innerWidth+1),'Changelog fits without sideways scrolling at '+width+'px');}
 await page.evaluate(()=>TaskListApp.handleBack());check(await page.locator('dialog').count()===1&&await page.locator('dialog h2').textContent()==='Settings','Back returns from Changelog to Settings');
 await page.locator('dialog').getByRole('button',{name:'Close',exact:true}).click();check(await page.evaluate(()=>JSON.stringify(__history.get()))===before,'Viewing history and alignment changes do not modify task state');
 check(errors.length===0,'No history or alignment browser errors');console.log(JSON.stringify({passed,suite:'settings history and left alignment'}));
}finally{await browser.close();}
