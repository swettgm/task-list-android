import {chromium} from 'playwright';
import {existsSync} from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
const browser=await chromium.launch({executablePath:process.env.CHROME_BIN||['/usr/bin/google-chrome','/usr/bin/chromium'].find(existsSync),args:['--no-sandbox','--disable-dev-shm-usage']});
const page=await browser.newPage({viewport:{width:390,height:844}}),errors=[];page.on('pageerror',error=>errors.push(error.message));
await page.addInitScript(()=>{let state={customTasks:[{id:'footer-outline',title:'Subtask outline fixture',repeat:{amount:1,unit:'day',start:'2020-01-01'},priority:'LOW',steps:['First step','Second step']}]};window.__footer={get:()=>structuredClone(state)};window.TaskListNative={readState:()=>JSON.stringify(state),writeState:value=>state=JSON.parse(value),updateSnapshot(){},setThemeColors(){},hideKeyboard(){}};});
let passed=0;function check(value,message){if(!value)throw Error(message);passed++;}
const layout=()=>page.locator('dialog').evaluate(dialog=>{const fields=dialog.querySelector('.tl-task-editor-fields'),footer=dialog.querySelector('.tl-dialog-actions'),f=fields.getBoundingClientRect(),b=footer.getBoundingClientRect(),d=dialog.getBoundingClientRect(),buttons=[...footer.querySelectorAll('button')].map(button=>{const r=button.getBoundingClientRect();return {top:r.top,bottom:r.bottom,left:r.left,right:r.right,hit:!!document.elementFromPoint(r.x+r.width/2,r.y+r.height/2)?.closest('.tl-dialog-actions')};});return {top:b.top,bottom:b.bottom,width:innerWidth,height:innerHeight,valid:fields.clientHeight>0&&getComputedStyle(fields).overflowY==='auto'&&f.bottom<=b.top+1&&d.top>=0&&d.bottom<=innerHeight+1&&d.left>=0&&d.right<=innerWidth+1&&buttons.every(button=>button.top-f.bottom>=8&&button.bottom<=d.bottom&&button.hit)&&dialog.scrollWidth<=dialog.clientWidth+1};});
try{
 await page.goto(pathToFileURL(path.resolve('app/src/main/assets/www/index.html')).href);await page.waitForFunction(()=>window.TaskListApp);
 const before=await page.evaluate(()=>JSON.stringify(__footer.get()));await page.evaluate(()=>TaskListApp.addTask({title:'Footer checks'}));
 check(await page.locator('dialog').evaluate(dialog=>{const left=dialog.querySelector('.tl-task-editor-fields>.tl-field').getBoundingClientRect().left;return [...dialog.querySelectorAll('fieldset.tl-choice-options,fieldset.tl-context-options')].every(group=>{const legend=group.querySelector('legend'),content=group.querySelector('.tl-choice-grid,.tl-link-picker'),style=getComputedStyle(group);return style.borderTopWidth==='0px'&&style.paddingLeft==='0px'&&Math.abs(legend.getBoundingClientRect().left-left)<.5&&Math.abs(content.getBoundingClientRect().left-left)<.5;});}),'Editor groups have no outer frames and share the other fields’ left edge');
 const checklist=page.locator('textarea[placeholder="One step per line"]');await checklist.fill('Keep this checklist text');const oldHeight=await checklist.evaluate(node=>node.getBoundingClientRect().height);await page.getByRole('slider',{name:'Checklist size',exact:true}).evaluate(slider=>{slider.value='320';slider.dispatchEvent(new Event('input',{bubbles:true}));});check(await checklist.evaluate(node=>node.getBoundingClientRect().height)>oldHeight+200&&await checklist.inputValue()==='Keep this checklist text'&&(await layout()).valid,'Checklist size slider expands the field without changing text or overlapping the footer');
 for(const [width,height] of [[320,844],[390,844],[600,844],[390,480],[320,320]]){
  await page.setViewportSize({width,height});await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve))));
  let previous=null;
  for(const fraction of [0,.5,1]){
   await page.locator('.tl-task-editor-fields').evaluate((fields,fraction)=>fields.scrollTop=(fields.scrollHeight-fields.clientHeight)*fraction,fraction);
   const metrics=await layout();check(metrics.valid&&(!previous||Math.abs(metrics.top-previous.top)<1),'Footer stays visible in its own space at '+width+'x'+height+' / scroll '+fraction+' '+JSON.stringify(metrics));previous=metrics;
  }
 }
 await page.getByRole('button',{name:'Cancel',exact:true}).click();check(await page.evaluate(()=>JSON.stringify(__footer.get()))===before,'Footer scrolling and viewport resizing do not alter task state');
 await page.setViewportSize({width:390,height:844});await page.evaluate(()=>TaskListApp.showToday());
 const task=page.locator('#pt-list [data-task-id="footer-outline"]');await task.locator(':scope>.tm-children').evaluate(details=>details.open=true);
 const styles=()=>task.evaluate(row=>{const parent=getComputedStyle(row.querySelector(':scope>.tm-item-actions>.tm-progress-control')),child=getComputedStyle(row.querySelector('.tm-child-item .tm-progress-control'));return {parent:{width:parent.borderTopWidth,style:parent.borderTopStyle,color:parent.borderTopColor,background:parent.backgroundColor},child:{width:child.borderTopWidth,style:child.borderTopStyle,color:child.borderTopColor,background:child.backgroundColor}};});
 let border=await styles();check(border.parent.width==='1px'&&border.child.width===border.parent.width&&border.child.style===border.parent.style&&border.child.color===border.parent.color,'Subtask Do now has the same visible outline as its parent');
 await task.locator(':scope>.tm-item-actions>.tm-progress-control').click();await task.locator('.tm-child-item .tm-progress-control').first().click();border=await styles();check(border.child.color===border.parent.color&&border.child.background===border.parent.background,'Active subtask and parent buttons retain matching green styling');
 check(errors.length===0,'Footer and subtask styling have no browser errors');console.log(JSON.stringify({passed,suite:'reserved editor footer and subtask button outlines'}));
}finally{await browser.close();}
