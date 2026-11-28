import {chromium} from 'playwright';
import assert from 'node:assert/strict';
import {serveFixture} from './browser-fixture-server.mjs';

const fixture=await serveFixture('app/src/main/assets/index.html');
const browser=await chromium.launch({executablePath:'/usr/bin/chromium',headless:true,args:['--no-sandbox','--disable-dev-shm-usage']});
try{
 const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true,timezoneId:'UTC'});
 const day=new Date('2026-11-27T12:00:00Z');
 await page.clock.setFixedTime(day);
 await page.addInitScript(()=>{
  if(localStorage.getItem('workouts-v1'))return;
  const exercise=(id,name,weight,sets)=>({id,name,weight,unit:'lb',rest:0,notes:'',
   sets:sets.map(amrap=>({target:5,amrap,weight,reps:null}))});
  const press={...exercise('press','Press',100,[false,true]),progressionIncrement:3.5};
  const squat={...exercise('squat','Squat',150,[false]),rest:60};
  localStorage.setItem('workouts-v1',JSON.stringify({version:1,revision:0,
   routines:[{id:'routine',name:'Workout A',exercises:[press,squat]}],
   selected:'routine',active:null,history:[],restEndAt:0,
   settings:{top:'#203747',bottom:'#5a5c61',text:'#f4f7fa',gradient:true,unit:'lb',rest:90,
    vibrationMs:350,notifications:false,spotify:false,floatingTimer:true}}));
 });
 await page.goto(fixture.url);
 const data=()=>page.evaluate(()=>Workouts.debug());
 const dialog=()=>page.locator('dialog[open]');
 const button=(name,root=page)=>root.getByRole('button',{name,exact:true});
 const number=(name,root=page)=>root.getByRole('spinbutton',{name,exact:true});
 const dismiss=()=>button('Cancel',dialog()).click();
 async function drag(control,dx,dy){
  await control.scrollIntoViewIfNeeded();
  const box=await control.boundingBox(),x=box.x+box.width/2,y=box.y+box.height/2;
  await page.mouse.move(x,y);await page.mouse.down();
  await page.mouse.move(x+dx,y+dy,{steps:5});await page.mouse.up();
 }
 await button('Workout A').click();
 await button('Edit sets/reps for Press').click();
 const targets=dialog().getByRole('textbox',{name:'Sets/Reps',exact:true});
 for(const name of ['Weight','Sets/Reps']){
  assert.equal(await button('Drag to adjust '+name,dialog()).evaluate(grip=>{
   const control=grip.getBoundingClientRect(),field=grip.parentElement.getBoundingClientRect();
   return parseFloat(getComputedStyle(grip.querySelector('svg')).strokeWidth)>3.2&&
    control.right<=field.right&&field.right-control.right<=8&&
    Math.abs(control.top+control.height/2-field.top-field.height/2)<.6;
  }),true);
 }
 await drag(button('Drag to adjust Sets/Reps',dialog()),0,-24);
 assert.equal(await targets.inputValue(),'7, 7+');
 await drag(button('Drag to adjust Weight',dialog()),0,-24);
 assert.equal(await number('Weight',dialog()).inputValue(),'102');
 await dismiss();

 await button('Edit Workout A').click();
 await button('Edit Press',dialog()).click();
 const note=dialog().getByRole('textbox',{name:'Notes',exact:true});
 const grip=button('Resize Notes',dialog());
 const before=await note.boundingBox();
 await drag(grip,-30,37);
 const after=await note.boundingBox();
 assert.equal(after.width,before.width-30);
 assert.equal(after.height,before.height+37);
 await drag(grip,20,16);
 const expanded=await note.boundingBox();
 assert.equal(expanded.width,before.width-10);
 assert.equal(expanded.height,before.height+53);
 await dismiss();

 await button('Settings').click();
 const settings=page.locator('#settings-screen');
 assert.equal(await settings.locator('.number-grip:visible').count(),0);
 await settings.locator('.settings-changelog summary').click();
 assert.equal(await settings.getByRole('heading',{name:'Exercise 1.0.49',exact:true}).isVisible(),true);
 await page.getByRole('combobox',{name:'Number controls'}).selectOption('− and + buttons');
 assert.equal(await settings.locator('.number-step:visible, .number-grip:visible').count(),0);
 await number('Number adjustment step').fill('2');
 await page.getByRole('checkbox',{name:'Prompt for workout notes'}).uncheck();
 await button('Save').click();
 await button('Edit sets/reps for Press').click();
 await button('Increase Sets/Reps',dialog()).click();
 assert.equal(await targets.inputValue(),'7, 7+');
 await button('Decrease Sets/Reps',dialog()).click();
 assert.equal(await targets.inputValue(),'5, 5+');
 assert.equal(await button('Drag to adjust Weight',dialog()).isVisible(),false);
 await dismiss();

 const start=()=>button('Start workout').click();
 const finish=()=>button('Finish').click();
 const pressWeight=async()=>(await data()).routines[0].exercises[0].weight;
 await start();
 await button('Log set 1 of Press').click();
 await drag(button('Log set 2 of Press'),0,-60);
 assert.equal((await data()).active.exercises[0].sets[1].reps,10);
 await finish();
 assert.equal(await dialog().count(),0);
 assert.equal(await pressWeight(),103.5);
 assert.equal((await data()).history[0].exercises[0].weight,100);
 assert.equal((await data()).routines[0].exercises[1].weight,150);
 await start();
 await dialog().getByRole('checkbox',{name:'Press · 107 lb'}).check();
 await button('Start workout',dialog()).click();
 assert.equal((await data()).active.exercises[0].weight,107);
 await page.waitForTimeout(450);
 for(const nextWeight of [107,96.3,96.3]){
  await button('Log set 1 of Press').click();
  await button('Edit set 1 of Press').click();
  await finish();
  assert.equal(await pressWeight(),nextWeight);
  await start();
 }
 await button('Finish').click();
 await button('Discard',dialog()).click();
 await button('Settings').click();
 await page.getByRole('combobox',{name:'Schedule',exact:true}).selectOption('Every few days');
 await number('Train every (days)').fill('1');
 await button('Save').click();
 await page.clock.setFixedTime(new Date(day.getTime()+4*86400000));
 await page.reload();
 const scheduled=button('Workout A');
 assert.equal(await scheduled.getAttribute('aria-expanded'),'true');
 assert.equal(await button('Start workout').isVisible(),true);
 assert.equal((await data()).active,null);
 await scheduled.click();
 await page.evaluate(()=>Workouts.refresh());
 assert.equal(await scheduled.getAttribute('aria-expanded'),'false');
 await scheduled.click();
 await start();
 const percent=dialog().getByRole('slider',{name:'Deload percentage'});
 assert.equal(await percent.inputValue(),'10');
 assert.equal(await dialog().getByText('You missed 3 scheduled workouts in a row.').count(),1);
 await percent.fill('20');
 await dialog().getByRole('checkbox',{name:'Deload all workouts'}).check();
 await button('Start workout',dialog()).click();
 const active=(await data()).active;
 assert.equal(active.exercises[0].weight,77.04);
 assert.equal(active.exercises[1].weight,120);

 await button('Log set 1 of Squat').click();
 const timer=button('Rest controls');
 await drag(timer,-250,-125);
 const moved=await timer.boundingBox(),savedPosition=(await data()).restPosition;
 assert.ok(savedPosition);
 await page.reload();
 await timer.waitFor();
 const reopened=await timer.boundingBox();
 assert.ok(Math.abs(moved.x-reopened.x)<1&&Math.abs(moved.y-reopened.y)<1);
 await drag(timer,195-(reopened.x+reopened.width/2),794-(reopened.y+reopened.height/2));
 assert.equal((await data()).restDismissed,true);
 await button('Edit set 1 of Squat').click();
 await timer.waitFor();
 const restored=await timer.boundingBox();
 assert.ok(Math.abs(moved.x-restored.x)<1&&Math.abs(moved.y-restored.y)<1);
 console.log('Number controls, note resize, progression, failures, AMRAP, missed-workout deload and persistent timer position passed.');

 const previewPage=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true,timezoneId:'UTC'});
 await previewPage.clock.setFixedTime(day);
 await previewPage.addInitScript(()=>{
  if(localStorage.getItem('workouts-v1'))return;
  const exercise=(id,name,weight)=>({id,name,weight,unit:'lb',rest:0,notes:'',
   sets:[{target:5,amrap:false,weight,reps:null}]});
  const bench={...exercise('bench','Bench Press',135),progressionIncrement:2.5};
  const squat=exercise('squat','Squat',225);
  const press=exercise('press','Overhead Press',95);
  localStorage.setItem('workouts-v1',JSON.stringify({version:1,revision:0,
   routines:[{id:'bench-squat',name:'Bench & Squat',exercises:[bench,squat]},
    {id:'press',name:'Press & Deadlift',exercises:[press]}],
   selected:'bench-squat',active:null,history:[],restEndAt:0,
   settings:{top:'#203747',bottom:'#5a5c61',text:'#f4f7fa',gradient:true,spotify:false,notifications:false,
    promptWorkoutNotes:false,schedule:{mode:'weekdays',days:[1,3,5]}}}));
 });
 await previewPage.goto(fixture.url);
 const previewData=()=>previewPage.evaluate(()=>Workouts.debug());
 const repeated=previewPage.getByRole('region',{name:'Bench & Squat',exact:true});
 const shownWeights=card=>card.locator('button[aria-label^="Edit weight for "]').allTextContents();
 assert.deepEqual(await shownWeights(repeated.nth(0)),['135 lb','225 lb']);
 const later=repeated.nth(1);
 await button('Bench & Squat',later).click();
 assert.equal(await later.locator('.routine-date').textContent(),'Wednesday, December 2');
 assert.deepEqual(await shownWeights(later),['137.5 lb','230 lb']);
 assert.equal((await previewData()).revision,0);

 await button('Start workout',later).click();
 assert.deepEqual((await previewData()).active.exercises.map(exercise=>exercise.weight),[135,225]);
 await button('Finish',previewPage).click();
 await button('Discard',previewPage.locator('dialog[open]')).click();
 await button('Start workout',repeated.first()).click();
 await button('Log set 1 of Bench Press',previewPage).click();
 await button('Edit set 1 of Bench Press',previewPage).click();
 await button('Log set 1 of Squat',previewPage).click();
 await button('Finish',previewPage).click();
 assert.deepEqual((await previewData()).routines[0].exercises.map(exercise=>exercise.weight),[135,230]);
 assert.deepEqual(await shownWeights(repeated.first()),['135 lb','230 lb']);
 console.log('Scheduled repeats preview progression without saving it and reflect the actual result after logging.');
}finally{
 await browser.close();
 await fixture.close();
}
