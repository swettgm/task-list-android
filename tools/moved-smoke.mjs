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
    let now=Date.parse('2026-11-27T15:00:00Z');
    const RealDate=Date;
    window.advanceMovedFixture=()=>{now+=86400000;window.dispatchEvent(new Event('focus'));};
    window.Date=class extends RealDate {
      constructor(...args){super(...(args.length?args:[now]));}
      static now(){return now;}
    };
    const initial={
      version:2026103022,
      customTasks:[
        {id:'today',title:'Today task',due:'2026-11-27',priority:'HIGH'},
        {id:'wrapped',title:'Wrapped task',days:[0,1,2,3,4,5],startsOn:'2026-11-27',priority:'HIGH',
          labels:['At home','On the computer','Outdoors','On the road','On the phone']},
        {id:'future',title:'Future task',due:'2026-11-28',priority:'LOW'},
        {id:'prev',title:'Moved yesterday',due:'2026-11-26',priority:'LOW'},
        {id:'legacy',title:'Move without a saved day',due:'2026-11-25',priority:'LOW'}
      ],
      movedTo:{'prev:2026-11-26':'2026-11-29','legacy:2026-11-25':'2026-11-27'},
      moveHistory:{'prev:2026-11-26':{day:'2026-11-26',from:'2026-11-26',to:'2026-11-29'}}
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
  await page.setViewportSize({width:320,height:844});
  const footer=page.locator('#pt-today .tl-history-toggles');
  assert.deepEqual(await footer.getByRole('button').allTextContents(),['Done','Moved','Hidden','Trash']);
  assert.equal(await footer.getByRole('button').evaluateAll(buttons=>{
    const boxes=buttons.map(button=>button.getBoundingClientRect());
    return boxes.every(box=>box.top===boxes[0].top&&box.right<=innerWidth);
  }),true);
  const detailsGap=task=>task.evaluate(row=>{
    const details=row.querySelector(':scope > .tm-task-details');
    const title=row.querySelector(':scope > .form-check');
    const action=row.querySelector(':scope > .tm-item-actions > button');
    const meta=details.querySelector('.tl-meta-flow');
    return {
      height:meta.getBoundingClientRect().height,
      rowGap:parseFloat(getComputedStyle(meta).rowGap),
      gap:action.getBoundingClientRect().top-details.getBoundingClientRect().bottom,
      titleGap:details.getBoundingClientRect().top-title.getBoundingClientRect().bottom
    };
  });
  const singleLine=await detailsGap(row);
  const wrapped=await detailsGap(page.locator('#pt-list [data-task-id="wrapped"]'));
  assert.ok(wrapped.height>singleLine.height,'The recurring fixture wraps to additional lines');
  assert.ok(singleLine.titleGap>0,'The title has space below it');
  for(const spacing of [singleLine,wrapped]){
    assert.equal(spacing.gap,singleLine.titleGap,'Task actions match the gap below the title');
    assert.equal(spacing.rowGap,singleLine.titleGap,'Wrapped detail lines match the gap below the title');
    assert.equal(spacing.titleGap,singleLine.titleGap,'The title keeps the same gap above single and wrapped details');
  }
  await page.setViewportSize({width:390,height:844});
  await moved.tap();
  assert.equal(await page.getByText('No tasks moved today.',{exact:true}).isVisible(),true);
  await moved.tap();

  await time.tap();
  assert.equal(await dialog.getByRole('heading',{name:'Time',exact:true}).isVisible(),true);
  await dialog.getByRole('button',{name:'Cancel',exact:true}).tap();
  const timeBox=await time.boundingBox(),tomorrowBox=await tomorrow.boundingBox();
  assert.ok(tomorrowBox.y-timeBox.y-timeBox.height>=singleLine.titleGap);
  await tomorrow.tap({position:{x:tomorrowBox.width/2,y:2}});
  assert.equal(await dialog.count(),0);
  assert.equal(await row.count(),0);
  assert.equal(await moved.textContent(),'Moved (1)');

  await moved.tap();
  assert.equal(await moved.getAttribute('aria-expanded'),'true');
  assert.deepEqual(await moveDates('today'),['2026-11-27','2026-11-28']);
  assert.deepEqual(await moveDates('prev'),[]);
  assert.deepEqual(await moveDates('legacy'),[]);

  await page.getByRole('tab',{name:'Upcoming',exact:true}).tap();
  await page.locator('#pt-future-list [data-date="2026-11-28"] summary').tap();
  await page.getByRole('button',{name:'Move Today task to today',exact:true}).tap();
  await page.getByRole('button',{name:'Move Future task to today',exact:true}).tap();
  await page.getByRole('tab',{name:'Today',exact:true}).tap();
  const roundTrip=['2026-11-28','2026-11-27','2026-11-27','2026-11-28'];
  assert.deepEqual(await moveDates('today'),roundTrip);
  assert.deepEqual(await moveDates('future'),['2026-11-28','2026-11-27']);
  assert.equal(await moved.textContent(),'Moved (3)');
  await page.locator('#pt-list [data-task-id="future"] .tl-edit-button').tap();
  await dialog.locator('input[type=date]').fill('2026-11-30');
  await dialog.getByRole('button',{name:'Save task',exact:true}).tap();
  const futureMoves=['2026-11-27','2026-11-30','2026-11-28','2026-11-27'];
  assert.deepEqual(await moveDates('future'),futureMoves);
  assert.equal(await moved.textContent(),'Moved (4)');

  await page.reload();
  await page.waitForFunction(()=>!!window.TaskListApp);
  assert.equal(await moved.getAttribute('aria-expanded'),'false');
  await moved.tap();
  assert.deepEqual(await moveDates('today'),roundTrip);
  assert.deepEqual(await moveDates('future'),futureMoves);
  await page.evaluate(()=>advanceMovedFixture());
  assert.equal(await page.getByText('No tasks moved today.',{exact:true}).isVisible(),true);
  assert.equal(await moved.textContent(),'Moved');
  assert.deepEqual(errors,[]);
  console.log('Passed one-line footer, touch targets, daily move filtering, date edits, reopening and midnight rollover');
} finally {
  await browser.close();
  await fixture.close();
}
