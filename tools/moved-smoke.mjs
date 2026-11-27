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
      metadataIcons:'symbols',
      customTasks:[
        {id:'today',title:'Today task',due:'2026-11-27',priority:'HIGH'},
        {id:'spacing-reference',title:'Clean desk 🧼',days:[5],startsOn:'2026-11-27',priority:'HIGH',
          labels:['At home','On the computer']},
        {id:'wrapped',title:'A longer task title that wraps onto the next line',days:[0,1,2,3,4,5],startsOn:'2026-11-27',priority:'HIGH',
          notes:'First line of task notes.\nSecond line of task notes.',
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
  const wrappedRow=page.locator('#pt-list [data-task-id="wrapped"]');
  const wrapped=await detailsGap(wrappedRow);
  assert.ok(wrapped.height>singleLine.height,'The recurring fixture wraps to additional lines');
  assert.ok(singleLine.titleGap<singleLine.gap,'The title gap accounts for the surrounding font and icon space');
  for(const spacing of [singleLine,wrapped]){
    assert.equal(spacing.gap,13,'Preserve the reference gap above the task actions');
    assert.equal(spacing.titleGap,singleLine.titleGap,'Titles use the same spacing above single and wrapped details');
    assert.ok(spacing.rowGap<spacing.gap,'Wrapped details account for the whitespace inside each line');
  }
  const textGaps=await wrappedRow.evaluate(row=>['.tm-title','.tm-task-note'].map((selector,index)=>{
    const text=row.querySelector(selector),range=document.createRange();
    range.selectNodeContents(text);
    const tops=[...new Set([...range.getClientRects()].map(rect=>rect.top))];
    const base=parseFloat(getComputedStyle(text).fontSize)*(index===0?1.3:1.5);
    return tops.slice(1).map((top,i)=>top-tops[i]-base);
  }));
  for(const gaps of textGaps){
    assert.ok(gaps.length>0,'The test text spans multiple lines');
    assert.ok(gaps.every(gap=>Math.abs(gap-wrapped.rowGap)<.1),'Wrapped text keeps the same added leading on each line');
  }
  const reference=page.locator('#pt-list [data-task-id="spacing-reference"]');
  const screenshot=(await reference.screenshot()).toString('base64');
  const visibleGaps=await reference.evaluate(async (row,base64)=>{
    const image=new Image();
    image.src='data:image/png;base64,'+base64;
    await image.decode();
    const canvas=document.createElement('canvas');
    canvas.width=image.width;canvas.height=image.height;
    const ctx=canvas.getContext('2d');
    ctx.drawImage(image,0,0);
    const rect=row.getBoundingClientRect();
    const button=row.querySelector('.tm-progress-control').getBoundingClientRect();
    const left=Math.ceil(button.left-rect.left);
    const bottom=Math.floor(button.top-rect.top);
    const width=canvas.width-left;
    const {data}=ctx.getImageData(left,0,width,bottom);
    const bands=[];
    for(let y=0;y<bottom;y++){
      const start=y*width*4;
      if(!data.subarray(start,start+width*4).some((value,i)=>i%4!==3&&value>160))continue;
      const previous=bands.at(-1);
      if(previous&&y-previous.end<=2)previous.end=y;
      else bands.push({start:y,end:y});
    }
    return bands.map(({end},i)=>(bands[i+1]?.start??bottom)-end-1);
  },screenshot);
  assert.equal(visibleGaps.length,3,'The reference row has a title and two visible detail lines');
  assert.ok(visibleGaps.every(gap=>Math.abs(gap-visibleGaps.at(-1))<=4),
    'Visible gaps match the reference above the buttons: '+visibleGaps.join(', '));
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
