import assert from 'node:assert/strict';
import {chromium} from 'playwright';
import {serveFixture} from './browser-fixture-server.mjs';

const fixture = await serveFixture('app/src/main/assets/www/index.html');
const browser = await chromium.launch({
  executablePath: process.env.CHROME_BIN || '/usr/bin/chromium',
  args: ['--no-sandbox', '--disable-dev-shm-usage']
});
try {
  const page = await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true,reducedMotion:'reduce'});
  const errors=[];
  page.on('pageerror',error=>errors.push(error.message));
  await page.addInitScript(()=>{
    const now=Date.parse('2026-11-27T15:00:00Z'),RealDate=Date;
    window.Date=class extends RealDate {
      constructor(...args){super(...(args.length?args:[now]));}
      static now(){return now;}
    };
    const initial={
      version:2026103022,
      customTasks:[
        {id:'today',title:'Today task',due:'2026-11-27',priority:'HIGH'},
        {id:'future',title:'Future task',due:'2026-11-28',priority:'LOW'},
        {id:'prev',title:'Already moved task',due:'2026-11-26',priority:'LOW'}
      ],
      movedTo:{'prev:2026-11-26':'2026-11-29'}
    };
    let state=JSON.parse(sessionStorage.getItem('tasks-moved-state'))||initial;
    window.TaskListNative={
      readState:()=>JSON.stringify(state),
      writeState:raw=>{state=JSON.parse(raw);sessionStorage.setItem('tasks-moved-state',raw);},
      updateSnapshot(){},
      setThemeColors(){}
    };
  });
  await page.goto(fixture.url);
  await page.waitForFunction(()=>!!window.TaskListApp);
  const dialog=page.locator('dialog[open]');
  const row=page.locator('#pt-list [data-task-id="today"]');
  const time=row.getByRole('button',{name:'Edit time for Today task',exact:true});
  const tomorrow=row.getByRole('button',{name:'Move Today task to tomorrow',exact:true});
  const moved=page.locator('#pt-toggle-moved');
  const moveDates=id=>page.locator('#pt-moved-list [data-moved-task-id="'+id+'"] time')
    .evaluateAll(nodes=>nodes.map(node=>node.dateTime));

  await time.tap();
  assert.equal(await dialog.getByRole('heading',{name:'Time',exact:true}).isVisible(),true);
  await dialog.getByRole('button',{name:'Cancel',exact:true}).tap();
  const timeBox=await time.boundingBox(),tomorrowBox=await tomorrow.boundingBox();
  assert.ok(tomorrowBox.y-timeBox.y-timeBox.height>=12);
  await tomorrow.tap({position:{x:tomorrowBox.width/2,y:2}});
  assert.equal(await dialog.count(),0);
  assert.equal(await row.count(),0);

  await moved.tap();
  assert.equal(await moved.getAttribute('aria-expanded'),'true');
  assert.deepEqual(await moveDates('today'),['2026-11-27','2026-11-28']);
  assert.deepEqual(await moveDates('prev'),['2026-11-26','2026-11-29']);

  await page.getByRole('tab',{name:'Upcoming',exact:true}).tap();
  await page.locator('#pt-future-list [data-date="2026-11-28"] summary').tap();
  await page.getByRole('button',{name:'Move Future task to today',exact:true}).tap();
  await page.getByRole('tab',{name:'Today',exact:true}).tap();
  assert.deepEqual(await moveDates('future'),['2026-11-28','2026-11-27']);

  await page.reload();
  await page.waitForFunction(()=>!!window.TaskListApp);
  assert.equal(await moved.getAttribute('aria-expanded'),'false');
  await moved.tap();
  assert.deepEqual(await moveDates('today'),['2026-11-27','2026-11-28']);
  assert.deepEqual(await moveDates('future'),['2026-11-28','2026-11-27']);
  assert.deepEqual(errors,[]);
  console.log('Passed touch targets, forward and backward moves, and saved move history after reopening');
} finally {
  await browser.close();
  await fixture.close();
}
