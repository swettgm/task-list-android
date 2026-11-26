import assert from 'node:assert/strict';
import {chromium} from 'playwright';
import {serveFixture} from './browser-fixture-server.mjs';
const fixture = await serveFixture('app/src/main/assets/www/index.html');
const browser = await chromium.launch({executablePath:'/usr/bin/chromium',args:['--no-sandbox']});
try {
  const page = await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true});
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.addInitScript(() => {
    let state = {};
    window.TaskListNative = {
      readState:()=>JSON.stringify(state),writeState:text=>state=JSON.parse(text),
      viewHaptic:()=>{},actionHaptic:()=>{}
    };
    window.__haptics = 0;
  });
  await page.goto(fixture.url);
  await page.waitForFunction(()=>!!window.TaskListApp);
  await page.evaluate(()=>TaskListBridge.viewHaptic=()=>window.__haptics++);
  const selected = () => page.locator('#pt-today-tab').getAttribute('aria-selected');
  const finish = () => page.evaluate(()=>document.getAnimations().forEach(a=>a.finish()));
  const moving = () => page.waitForFunction(()=>document.querySelector('.tm-view-animating'));
  await page.evaluate(()=>document.querySelector('#pt-upcoming-tab').click());
  await moving();
  assert.equal(await selected(),'true');
  await finish();
  await page.waitForFunction(()=>document.querySelector('#pt-upcoming-tab').getAttribute('aria-selected')==='true');
  assert.equal(await page.evaluate(()=>__haptics),1);
  await page.evaluate(()=>document.querySelector('#pt-today-tab').click());
  await moving();
  assert.equal(await selected(),'false');
  await finish();
  await page.waitForFunction(()=>document.querySelector('#pt-today-tab').getAttribute('aria-selected')==='true');
  const swipe = async (distance, cancel=false) => page.evaluate(({distance,cancel})=>{
    const fire=(type,x)=>document.body.dispatchEvent(new TouchEvent(type,{
      bubbles:true,cancelable:true,touches:type==='touchstart'||type==='touchmove'
        ?[new Touch({identifier:1,target:document.body,clientX:x,clientY:400})]:[]
    }));
    fire('touchstart',300);fire('touchmove',300-distance);fire(cancel?'touchcancel':'touchend',300-distance);
  },{distance,cancel});
  const panelsOpen=()=>page.evaluate(()=>[
    document.querySelector('#pt-toggle-all').getAttribute('aria-pressed')==='true',
    !document.querySelector('#pt-completed').hidden,
    !document.querySelector('#pt-trash').hidden
  ]);
  await page.evaluate(()=>{
    for(const id of ['pt-toggle-all','pt-toggle-hidden','pt-toggle-trash'])document.getElementById(id).click();
  });
  assert.deepEqual(await panelsOpen(),[true,true,true]);
  await swipe(100,true);
  await finish();
  await page.waitForFunction(()=>!document.querySelector('.tm-view-animating'));
  assert.deepEqual(await panelsOpen(),[true,true,true]);
  await swipe(100);
  await moving();
  assert.equal(await selected(),'true');
  await finish();
  await page.waitForFunction(()=>document.querySelector('#pt-upcoming-tab').getAttribute('aria-selected')==='true');
  assert.deepEqual(await panelsOpen(),[false,false,false]);
  await swipe(-100,true);
  await moving();
  assert.equal(await selected(),'false');
  await finish();
  await page.waitForFunction(()=>!document.querySelector('.tm-view-animating'));
  assert.equal(await selected(),'false');
  assert.equal(await page.evaluate(()=>__haptics),3);
  await page.waitForTimeout(450);
  await page.emulateMedia({reducedMotion:'reduce'});
  await page.evaluate(()=>document.querySelector('#pt-today-tab').click());
  assert.equal(await selected(),'true');
  assert.equal(await page.locator('.tm-view-animating').count(),0);
  assert.deepEqual(errors,[]);
  console.log('Passed tab selection after slide, both directions, swipe completion, Today panel collapse, cancellation, reduced motion and haptics');
} finally {
  await browser.close();
  await fixture.close();
}
