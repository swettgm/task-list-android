import assert from 'node:assert/strict';
import {chromium} from 'playwright';
import {serveFixture} from './browser-fixture-server.mjs';
const fixture=await serveFixture('app/src/main/assets/index.html');
const browser=await chromium.launch({executablePath:'/usr/bin/chromium',args:['--no-sandbox']});
try {
 const page=await browser.newPage({viewport:{width:390,height:844}});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.addInitScript(()=>{
  const RealDate=Date;window.__now=RealDate.parse('2026-11-20T15:00:00Z');
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
 await panel.getByRole('button',{name:'Add items'}).click();
 await page.getByRole('textbox',{name:'Morning routine items'}).fill('Keys\nDelivery forms\nWater bottle');
 await page.getByRole('button',{name:'Save',exact:true}).click();
 assert.equal(await panel.getByRole('checkbox').count(),3);
 await panel.getByRole('checkbox',{name:'Keys',exact:true}).check();
 await page.reload();await page.waitForFunction(()=>!!window.RouteManager);
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).isChecked(),true);
 await panel.locator('summary').click();
 await page.reload();await page.waitForFunction(()=>!!window.RouteManager);
 assert.equal(await panel.locator('details').getAttribute('open'),null);
 await panel.locator('summary').click();
 await panel.getByRole('button',{name:'Edit items'}).click();
 await page.getByRole('textbox',{name:'Morning routine items'}).fill('Keys\nWater bottle\nPhone');
 await page.getByRole('button',{name:'Save',exact:true}).click();
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).isChecked(),true);
 await panel.getByRole('button',{name:'Done for today'}).click();
 assert.equal(await panel.isVisible(),false);
 await page.getByRole('tab',{name:'G200 Monday',exact:true}).click();
 assert.equal(await panel.isVisible(),false);
 await page.reload();await page.waitForFunction(()=>!!window.RouteManager);
 assert.equal(await panel.isVisible(),false);
 await page.evaluate(()=>{window.__now+=86400000;window.dispatchEvent(new Event('focus'));});
 await panel.waitFor({state:'visible'});
 assert.equal(await panel.getByRole('checkbox',{name:'Keys',exact:true}).isChecked(),false);
 assert.equal(await panel.locator('details').evaluate(e=>e.open),true);
 await panel.getByRole('button',{name:'Edit items'}).click();
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
 assert.deepEqual(errors,[]);
 console.log('Passed daily reset, saved checks, collapse, editing, dismissal across routes/restarts, save failures and For me ordering');
} finally {await browser.close();await fixture.close();}
