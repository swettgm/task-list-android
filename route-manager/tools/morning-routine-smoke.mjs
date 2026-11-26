import assert from 'node:assert/strict';
import {chromium} from 'playwright';
import {serveFixture} from './browser-fixture-server.mjs';
const fixture=await serveFixture('app/src/main/assets/index.html');
const browser=await chromium.launch({executablePath:'/usr/bin/chromium',args:['--no-sandbox']});
try {
 const page=await browser.newPage({viewport:{width:390,height:844}});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.addInitScript(()=>{
  const RealDate=Date;window.__now=Number(sessionStorage.getItem('routine-test-time'))||RealDate.parse('2026-11-20T15:00:00Z');
  window.Date=class extends RealDate{constructor(...args){super(...(args.length?args:[window.__now]));}static now(){return window.__now;}};
  window.__fail=false;
  window.RouteNative={
   readState:()=>localStorage.getItem('routine-test')||'{}',
   writeState:raw=>{if(__fail)return false;localStorage.setItem('routine-test',raw);return true;},
   setThemeColors(){},ready(){},haptic(){},setModalOpen(){},hideKeyboard(){}
  };
 });
 await page.goto(fixture.url);await page.waitForFunction(()=>!!window.RouteManager);
 const panel=page.locator('#morning-routine');
 await panel.getByRole('button',{name:'Edit morning routine items'}).click();
 await page.getByRole('textbox',{name:'Morning routine items'}).fill('Keys\nDelivery forms\nWater bottle');
 await page.getByRole('button',{name:'Save',exact:true}).click();
 assert.equal(await panel.getByRole('checkbox').count(),3);
 await panel.getByRole('checkbox',{name:'Keys',exact:true}).check();
 await page.reload();await page.waitForFunction(()=>!!window.RouteManager);
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).isChecked(),true);
 await page.evaluate(()=>{
  const saved=RouteManager.debug().state;
  saved.morningRoutine.collapsed=true;
  RouteNative.writeState(JSON.stringify(saved));
 });
 await page.reload();await page.waitForFunction(()=>!!window.RouteManager);
 assert.equal(await panel.locator('details,summary').count(),0);
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).isVisible(),true);
 await panel.getByRole('button',{name:'Edit morning routine items'}).click();
 await page.getByRole('textbox',{name:'Morning routine items'}).fill('Keys\nWater bottle\nPhone');
 await page.getByRole('button',{name:'Save',exact:true}).click();
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).isChecked(),true);
 await panel.getByRole('button',{name:'Done for today'}).click();
 assert.equal(await panel.isVisible(),false);
 await page.getByRole('tab',{name:'G200 Monday',exact:true}).click();
 assert.equal(await panel.isVisible(),false);
 await page.reload();await page.waitForFunction(()=>!!window.RouteManager);
 assert.equal(await panel.isVisible(),false);
 await page.evaluate(()=>{window.__now+=86400000;sessionStorage.setItem('routine-test-time',String(window.__now));window.dispatchEvent(new Event('focus'));});
 await panel.waitFor({state:'visible'});
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).isChecked(),false);
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).isVisible(),true);
 await panel.getByRole('button',{name:'Edit morning routine items'}).click();
 await page.getByRole('textbox',{name:'Morning routine items'}).fill('Unsaved item');
 await page.evaluate(()=>window.__fail=true);
 await page.getByRole('button',{name:'Save',exact:true}).click();
 assert.equal(await page.locator('dialog').isVisible(),true);
 assert.match(await page.locator('dialog .error').textContent(),/Couldn’t save/);
 await page.evaluate(()=>window.__fail=false);
 await page.getByRole('button',{name:'Cancel',exact:true}).click();
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).count(),1);
 await page.evaluate(()=>{
  const state=RouteManager.debug().state;
  const route=RouteManager.debug().route;
  state.routes[route].notes=[
   {id:'progress',body:'Progress',stops:[],inProgress:true,createdAt:30},
   {id:'mine',body:'For me',stops:[],forMe:true,createdAt:10},
   {id:'delegate',body:'Delegate',stops:[],delegate:true,createdAt:40}
  ];
  RouteNative.writeState(JSON.stringify(state));
 });
 await page.reload();await page.waitForFunction(()=>!!window.RouteManager);
 assert.deepEqual(await page.locator('.note-body').allTextContents(),['For me','Progress','Delegate']);
 await page.evaluate(()=>{window.__now+=86400000;sessionStorage.setItem('routine-test-time',String(window.__now));window.dispatchEvent(new Event('focus'));});
 await panel.waitFor({state:'visible'});
 for (const width of [320, 390]) {
  await page.setViewportSize({width, height:844});
  for (const size of [0.92, 1, 1.14]) {
   await page.evaluate(size=>document.documentElement.style.setProperty('--size',size),size);
   const aligned=await panel.locator('.morning-header').evaluate(header=>{
    const title=header.querySelector('h2').getBoundingClientRect();
    const icons=[...header.querySelectorAll('button svg')];
    return icons.length===2&&icons.every(icon=>{
     const box=icon.getBoundingClientRect(),style=getComputedStyle(icon);
     return box.width===20&&box.height===20&&Math.abs((title.top+title.bottom-box.top-box.bottom)/2)<.5&&
       style.strokeLinecap==='round'&&parseFloat(style.strokeWidth)>2;
    });
   });
   assert.ok(aligned);
  }
 }
 assert.deepEqual(errors,[]);
 console.log('Passed daily reset, saved checks, expanded layout, editing, dismissal across routes/restarts, save failures and For me ordering');
} finally {await browser.close();await fixture.close();}
