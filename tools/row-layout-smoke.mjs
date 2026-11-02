import {chromium} from 'playwright';
import {readFile,mkdir} from 'node:fs/promises';
import {existsSync} from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
const file=path.resolve('app/src/main/assets/www/index.html');
const seed=JSON.parse(await readFile(path.join(path.dirname(file),'initial-state.json'),'utf8'));
const executablePath=['/usr/bin/google-chrome','/usr/bin/chromium'].find(existsSync);
const browser=await chromium.launch({headless:true,executablePath,args:['--no-sandbox','--disable-dev-shm-usage']});
const page=await browser.newPage({viewport:{width:390,height:844}});const checks=[];const errors=[];
page.on('pageerror',e=>errors.push(e.message));const check=(v,s)=>{if(!v)throw Error(s);checks.push(s);};
await page.addInitScript(initial=>{let state=initial;window.__layout={get:()=>state,set:s=>state=s};window.TaskListNative={readState:()=>JSON.stringify(state),writeState:s=>state=JSON.parse(s),updateSnapshot(){},setThemeColors(){},showWidgetSettings(){}};},seed);
try{
 await page.goto(pathToFileURL(file).href);await page.waitForFunction(()=>!!window.TaskListApp);
 await page.evaluate(()=>{
  const state=__layout.get(),date=TaskListApp.debugSummary().today,d=new Date(date+'T12:00:00Z');d.setUTCDate(d.getUTCDate()+1);
  const task=(id,due)=>({id,title:'A task with matching layout',due,priority:'HIGH',minutes:15,labels:['Layout check']});
  state.customTasks=[...(state.customTasks||[]),task('layout-today',date),task('layout-future',d.toISOString().slice(0,10))];state.inProgress=null;
  __layout.set(state);TaskListApp.reloadFromNative();TaskListApp.setContextFilter('Layout check');
 });
 const metric=async selector=>page.locator(selector).first().evaluate(row=>{
  const box=row.querySelector(':scope>.form-check>input').getBoundingClientRect(),title=row.querySelector('.tm-title').getBoundingClientRect();
  const css=n=>{const s=getComputedStyle(n);return[s.fontSize,s.lineHeight,s.paddingLeft,s.paddingRight];};
  return {offset:Math.abs(box.y+box.height/2-title.y-title.height/2),width:row.getBoundingClientRect().width,title:css(row.querySelector('.tm-title')),meta:css(row.querySelector('.tm-meta')),actions:css(row.querySelector('.tm-item-actions')),checkbox:[box.width,box.height],height:row.getBoundingClientRect().height};
 });
 for(const width of [320,390,600]){
  await page.setViewportSize({width,height:844});
  for(const size of [15,20,25]){
   await page.evaluate(size=>{let s=document.getElementById('layout-test-font');if(!s){s=document.createElement('style');s.id='layout-test-font';document.head.append(s);}s.textContent='#today-task-list-20261023 .form-check-label{font-size:'+size+'px!important}';},size);
   await page.locator('#pt-today-tab').click();const today=await metric('#pt-list [data-task-id="layout-today"]');
   await page.locator('#pt-upcoming-tab').click();const upcoming=await metric('#pt-future-list [data-task-id="layout-future"]');
   check(today.offset<1&&upcoming.offset<1,'Checkbox centers on title at '+width+'px / '+size+'px text');
   check(JSON.stringify(today.title)===JSON.stringify(upcoming.title)&&JSON.stringify(today.meta)===JSON.stringify(upcoming.meta)&&JSON.stringify(today.actions)===JSON.stringify(upcoming.actions)&&Math.abs(today.width-upcoming.width)<1&&Math.abs(today.height-upcoming.height)<1,'Today and Upcoming row sizing matches at '+width+'px / '+size+'px text');
  }
 }
 check(await page.locator('#pt-future-list .tm-day-heading').evaluateAll(nodes=>nodes.every(n=>getComputedStyle(n).borderBottomWidth==='0px')),'No dividers between Upcoming dropdowns');
 await page.setViewportSize({width:390,height:844});await page.evaluate(()=>document.getElementById('layout-test-font').remove());
 await mkdir('build/browser-qa',{recursive:true});await page.screenshot({path:'build/browser-qa/equal-upcoming.png'});
 await page.locator('#pt-today-tab').click();await page.screenshot({path:'build/browser-qa/equal-today.png'});
 check(!errors.length,'Layout interactions have no page errors');console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();}
