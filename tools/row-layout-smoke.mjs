import {chromium} from 'playwright';
import {readFile,mkdir} from 'node:fs/promises';
import {existsSync} from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
const file=path.resolve('app/src/main/assets/www/index.html');
const seed=JSON.parse(await readFile(path.join(path.dirname(file),'initial-state.json'),'utf8'));
const executablePath=['/usr/bin/google-chrome','/usr/bin/chromium'].find(existsSync);
const browser=await chromium.launch({headless:true,executablePath,args:['--no-sandbox','--disable-dev-shm-usage']});
const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true});const checks=[];const errors=[];
page.on('pageerror',e=>errors.push(e.message));const check=(v,s)=>{if(!v)throw Error(s);checks.push(s);};
await page.addInitScript(initial=>{let state=initial;window.__layout={get:()=>state,set:s=>state=s};window.TaskListNative={readState:()=>JSON.stringify(state),writeState:s=>state=JSON.parse(s),updateSnapshot(){},setThemeColors(){},showWidgetSettings(){}};},seed);
try{
 await page.goto(pathToFileURL(file).href);await page.waitForFunction(()=>!!window.TaskListApp);
 await page.evaluate(()=>{
  const state=__layout.get(),date=TaskListApp.debugSummary().today,d=new Date(date+'T12:00:00Z');d.setUTCDate(d.getUTCDate()+1);
  const task=(id,due)=>({id,title:'A task with matching layout',due,priority:'HIGH',minutes:15,urgent:true,labels:['Layout check']});
  state.customTasks=[...(state.customTasks||[]),task('layout-today',date),task('layout-future',d.toISOString().slice(0,10))];state.inProgress=null;
  __layout.set(state);TaskListApp.reloadFromNative();TaskListApp.setContextFilter('Layout check');
 });
 const metric=async selector=>page.locator(selector).first().evaluate(row=>{
  const box=row.querySelector(':scope>.form-check>input').getBoundingClientRect(),title=row.querySelector('.tm-title').getBoundingClientRect();
  const css=n=>{const s=getComputedStyle(n);return[s.fontSize,s.lineHeight,s.paddingLeft,s.paddingRight];};
  return {offset:box.y+box.height/2-title.y-title.height/2,width:row.getBoundingClientRect().width,title:css(row.querySelector('.tm-title')),meta:css(row.querySelector('.tm-meta')),actions:css(row.querySelector('.tm-item-actions')),checkbox:[box.width,box.height],height:row.getBoundingClientRect().height};
 });
 for(const width of [320,390,600]){
  await page.setViewportSize({width,height:844});
  for(const size of [15,20,25]){
   await page.evaluate(size=>{let s=document.getElementById('layout-test-font');if(!s){s=document.createElement('style');s.id='layout-test-font';document.head.append(s);}s.textContent='#today-task-list-20261023 .form-check-label{font-size:'+size+'px!important}';},size);
   await page.locator('#pt-today-tab').click();const today=await metric('#pt-list [data-task-id="layout-today"]');
   await page.locator('#pt-upcoming-tab').click();const upcoming=await metric('#pt-future-list [data-task-id="layout-future"]');
   check(Math.abs(today.offset)<.5&&Math.abs(upcoming.offset)<.5,'Checkbox center matches the vertical center of its title at '+width+'px / '+size+'px text '+JSON.stringify({today,upcoming}));
   check(JSON.stringify(today.title)===JSON.stringify(upcoming.title)&&JSON.stringify(today.meta)===JSON.stringify(upcoming.meta)&&JSON.stringify(today.actions)===JSON.stringify(upcoming.actions)&&Math.abs(today.width-upcoming.width)<1&&Math.abs(today.height-upcoming.height)<1,'Today and Upcoming row sizing matches at '+width+'px / '+size+'px text');
  }
 }
 await page.evaluate(()=>{const state=__layout.get();state.customTasks.filter(t=>t.id.startsWith('layout-')).forEach(t=>{t.title='A deliberately long task title that wraps over multiple lines on narrow screens';t.steps=['A checklist step with a long title that also wraps across multiple lines','Second step'];});__layout.set(state);TaskListApp.reloadFromNative();});
 for(const width of [320,390]){
  await page.setViewportSize({width,height:844});
  for(const [tab,panel,id]of [['Today','#pt-list','layout-today'],['Upcoming','#pt-future-list','layout-future']]){
   await page.getByRole('tab',{name:tab,exact:true}).click();const task=page.locator(panel+' [data-task-id="'+id+'"]');await task.locator('.tm-children summary').click();await task.locator('.tm-child-item .tm-progress-control').first().click();
   const centered=await task.locator('.form-check').evaluateAll(labels=>labels.every(label=>{const box=label.querySelector('input').getBoundingClientRect(),title=label.querySelector('.tm-title').getBoundingClientRect();return Math.abs(box.y+box.height/2-title.y-title.height/2)<.5;}));check(await task.locator('.tm-child-item .tm-task-details').first().evaluate(n=>parseFloat(getComputedStyle(n).paddingTop)>=6),'Active checklist badge has space below its title in '+tab+' at '+width+'px');check(centered,'Wrapped parent and checklist titles keep centered checkboxes, including an active child, in '+tab+' at '+width+'px');
   await task.locator('.tm-children summary').click();
  }
 }
 check(await page.locator('#pt-future-list .tm-day-heading').evaluateAll(nodes=>nodes.every(n=>getComputedStyle(n).borderBottomWidth==='0px')),'No dividers between Upcoming dropdowns');
 await page.setViewportSize({width:390,height:844});await page.evaluate(()=>document.getElementById('layout-test-font').remove());
 await mkdir('build/browser-qa',{recursive:true});await page.screenshot({path:'build/browser-qa/equal-upcoming.png'});
 await page.locator('#pt-today-tab').click();await page.screenshot({path:'build/browser-qa/equal-today.png'});
 check(await page.locator('.tm-urgent-badge').first().evaluate(n=>getComputedStyle(n).backgroundColor==='rgb(165, 29, 45)'&&n.textContent.includes('🔴')),'Urgent marker is red');
 check(await page.locator('.form-check-input').first().evaluate(n=>getComputedStyle(n).borderTopColor===getComputedStyle(document.documentElement).getPropertyValue('--tl-accent').trim().replace('#68b7ff','rgb(104, 183, 255)')),'Unchecked checkboxes use the accent color');
 const cdp=await page.context().newCDPSession(page);
 const panelOffsets=()=>page.evaluate(()=>{const stack=document.querySelector('.tm-view-stack').getBoundingClientRect();return Object.fromEntries([...document.querySelectorAll('.tm-view-stack>[role=tabpanel]:not([hidden])')].map(panel=>[panel.id,panel.querySelector('.tm-summary,.tm-day-heading').getBoundingClientRect().top-stack.top]));});
 async function swipe(from,to,vertical=false,holdMs=0){
  const before=await panelOffsets();
  const box=await page.locator('[role=tabpanel]:not([hidden]) .tm-task-item .tm-title').first().boundingBox(),y=box.y+box.height/2;
  await cdp.send('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x:from,y}]});
  const held=[];for(let step=1;step<=5;step++){await cdp.send('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:vertical?from:from+(to-from)*step/5,y:vertical?y+step*20:y}]});held.push(await page.evaluate(()=>{const stack=document.querySelector('.tm-view-stack').getBoundingClientRect();return{width:stack.width,panels:Object.fromEntries([...document.querySelectorAll('.tm-view-stack>[role=tabpanel]:not([hidden])')].map(n=>[n.id,n.getBoundingClientRect().left-stack.left]))};}));}
  if(holdMs)await page.waitForTimeout(holdMs);
  await cdp.send('Input.dispatchTouchEvent',{type:'touchEnd',touchPoints:[]});
  const motion=await page.evaluate(()=>[...document.querySelectorAll('.tm-view-stack>[role=tabpanel]')].flatMap(n=>n.getAnimations().map(a=>({id:n.id,frames:a.effect.getKeyframes().map(k=>k.transform)}))));
  const during=await panelOffsets();await page.waitForTimeout(30);const middle=await panelOffsets();
  const overflow=await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1);
  await page.waitForTimeout(450);const after=await panelOffsets();return{motion,overflow,before,during,middle,after,held};
 }
 const left=await swipe(290,70);check(left.motion.some(a=>a.id==='pt-upcoming'&&Number(a.frames[0].match(/-?[\d.]+/)[0])>0&&Number(a.frames[0].match(/-?[\d.]+/)[0])<100)&&left.motion.some(a=>a.id==='pt-today'&&a.frames.at(-1)==='translateX(-100%)')&&!left.overflow,'Swipe left slides both panels in the matching direction without horizontal overflow');check(await page.locator('#pt-upcoming').isVisible(),'Swipe left opens Upcoming');
 const right=await swipe(70,290);check(right.motion.some(a=>a.id==='pt-today'&&Number(a.frames[0].match(/-?[\d.]+/)[0])<0&&Number(a.frames[0].match(/-?[\d.]+/)[0])>-100)&&right.motion.some(a=>a.id==='pt-upcoming'&&a.frames.at(-1)==='translateX(100%)')&&!right.overflow,'Swipe right reverses the slide direction');check(await page.locator('#pt-today').isVisible(),'Swipe right opens Today');
 const leftHeld=left.held.at(-1),rightHeld=right.held.at(-1);check(Math.abs(leftHeld.panels['pt-today']+220)<1&&Math.abs(leftHeld.panels['pt-upcoming']-(leftHeld.width-220))<1,'Both panels follow the finger before release during a left drag');check(Math.abs(rightHeld.panels['pt-upcoming']-220)<1&&Math.abs(rightHeld.panels['pt-today']+rightHeld.width-220)<1,'Both panels follow the finger before release during a right drag');
 for(const [name,result,incoming,outgoing]of [['left',left,'pt-upcoming','pt-today'],['right',right,'pt-today','pt-upcoming']]){
  check(Math.abs(result.during[incoming]-result.after[incoming])<.5&&Math.abs(result.middle[incoming]-result.after[incoming])<.5,'Incoming panel holds its final vertical position throughout the '+name+' slide');
  check(Math.abs(result.before[outgoing]-result.during[outgoing])<.5&&Math.abs(result.before[outgoing]-result.middle[outgoing])<.5,'Outgoing panel stays at its resting vertical position during the '+name+' slide');
 }
 await swipe(240,240,true);check(await page.locator('#pt-today').isVisible(),'Vertical scroll does not change tabs');
 await swipe(290,260);check(await page.locator('#pt-today').isVisible()&&await page.locator('#pt-upcoming').isHidden(),'A short drag returns smoothly to its starting tab');
 await swipe(290,70,false,1100);check(await page.locator('#pt-upcoming').isVisible(),'Holding a drag longer than a second still switches when released');await swipe(70,290);
 check(await page.locator('.tm-view-animating,.tm-view-leaving,[inert]').count()===0&&await page.locator('[role=tabpanel]:not([hidden])').count()===1,'Finished transitions clean up animation state and leave one interactive panel');
 await page.emulateMedia({reducedMotion:'reduce'});const reduced=await swipe(290,70);check(reduced.motion.length===0&&await page.locator('#pt-upcoming').isVisible(),'Reduced motion skips the slide while retaining swipe navigation');
 await page.evaluate(()=>{const state=__layout.get(),today=TaskListApp.debugSummary().today,d=new Date(today+'T12:00:00Z');d.setUTCDate(d.getUTCDate()+1);for(const [id,due]of [['layout-frequency',today],['layout-frequency-future',d.toISOString().slice(0,10)]])state.customTasks.push({id,title:'Duration and weekdays alignment',due,startsOn:due,days:[0,1,2],minutes:15,time:'9:00 AM',priority:'HIGH',labels:['Layout check']});__layout.set(state);TaskListApp.reloadFromNative();});
 for(const width of [320,390,600]){await page.setViewportSize({width,height:844});for(const [tab,panel,id]of [['Today','#pt-list','layout-frequency'],['Upcoming','#pt-future-list','layout-frequency-future']]){await page.getByRole('tab',{name:tab,exact:true}).click();const aligned=await page.locator(panel+' [data-task-id="'+id+'"] .tl-meta-schedule').first().evaluate(n=>{const d=n.querySelector('.tl-meta-duration').getBoundingClientRect(),f=n.querySelector('.tl-meta-frequency').getBoundingClientRect();return Math.abs(d.top-f.top)<.5&&f.left>=d.right&&document.documentElement.scrollWidth<=innerWidth+1;});check(aligned,'Duration and Sunday–Tuesday stay side by side without sideways scrolling in '+tab+' at '+width+'px');}}
 check(!errors.length,'Layout interactions have no page errors');console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();}
