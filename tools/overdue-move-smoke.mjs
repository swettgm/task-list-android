import assert from 'node:assert/strict';
import {chromium} from 'playwright';
import {serveFixture} from './browser-fixture-server.mjs';

const fixture = await serveFixture(process.argv[2] || 'app/src/main/assets/www/index.html');
const browser = await chromium.launch({
  executablePath: process.env.CHROME_BIN || '/usr/bin/chromium',
  args: ['--no-sandbox', '--disable-dev-shm-usage']
});
try {
  const page = await browser.newPage({viewport:{width:390,height:844},reducedMotion:'reduce'});
  const errors=[];
  page.on('pageerror',error=>errors.push(error.message));
  await page.addInitScript(()=>{
    const RealDate=Date;
    let now=RealDate.parse('2026-11-27T15:00:00Z');
    window.Date=class extends RealDate {
      constructor(...args){super(...(args.length?args:[now]));}
      static now(){return now;}
    };
    window.advanceOverdueFixture=()=>{now+=86400000;window.dispatchEvent(new Event('focus'));};
    window.widgetRows=[];
    let state={
      version:2026103022,
      customTasks:[
        {id:'next',title:'Tomorrow task',due:'2026-11-28',priority:'HIGH'},
        {id:'later',title:'Later task',due:'2026-11-30',priority:'HIGH'},
        {id:'later-move',title:'Postponed task',due:'2026-11-26',priority:'LOW'},
        {id:'hidden',title:'Hidden until tonight',due:'2026-11-28',priority:'LOW',time:'8:00 PM',showBeforeMinutes:0}
      ],
      movedTo:{
        'next:2026-11-28':'2026-11-27',
        'later:2026-11-30':'2026-11-27',
        'later-move:2026-11-26':'2026-11-29'
      }
    };
    window.TaskListNative={
      readState:()=>JSON.stringify(state),
      writeState:value=>state=JSON.parse(value),
      updateSnapshot:raw=>window.widgetRows=JSON.parse(raw),
      setThemeColors(){}
    };
  });
  await page.goto(fixture.url);
  await page.waitForFunction(()=>!!window.TaskListApp);
  const row=id=>page.locator('#pt-list [data-task-id="'+id+'"]');
  assert.equal(await row('later').locator('.tm-overdue').textContent(),'Moved from Mon, Nov 30');
  await page.evaluate(()=>advanceOverdueFixture());

  for(const [id,original] of [['next','2026-11-28'],['later','2026-11-30']]){
    assert.equal(await row(id).count(),1);
    assert.equal(await row(id).getAttribute('data-overdue'),'true');
    assert.equal(await row(id).getAttribute('data-state-date'),original);
    assert.equal(await row(id).locator('.tm-overdue').textContent(),'Yesterday');
    await page.waitForFunction(([id,original])=>widgetRows.some(row=>
      row.progressKey===id+':'+original&&row.onToday&&row.overdue&&row.visibilityDate==='2026-11-27'
    ),[id,original]);
  }
  assert.equal(await row('later-move').count(),0,'Postponed tasks stay on their later date');
  assert.equal(await row('hidden').count(),0,'A due-today task still respects its reveal time');
  await row('later').getByRole('checkbox',{name:'Complete Later task'}).click();
  const completed=await page.evaluate(()=>JSON.parse(TaskListNative.readState()).completed);
  assert.equal(completed['later:2026-11-30'],true,'Completion uses the original occurrence identity');
  assert.equal(completed['later:2026-11-27'],undefined);
  assert.deepEqual(errors,[]);
  console.log('Passed overdue carryover for tasks moved earlier, widget parity and completion identity');
} finally {
  await browser.close();
  await fixture.close();
}
