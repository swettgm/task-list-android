import assert from 'node:assert/strict';
import {chromium} from 'playwright';
import {serveFixture} from './browser-fixture-server.mjs';
const fixture=await serveFixture('app/src/main/assets/index.html');
const browser=await chromium.launch({executablePath:'/usr/bin/chromium',args:['--no-sandbox']});
const checks=[],check=(value,label)=>{assert.ok(value,label);checks.push(label);};
try{
 const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.addInitScript(()=>{
  let state=JSON.parse(localStorage.getItem('qa-state')||'{}');window.qa={haptics:0,fail:false};
  window.WorkoutNative={readState:()=>JSON.stringify(state),writeState:raw=>{if(qa.fail)return null;state=JSON.parse(raw);state.revision=(state.revision||0)+1;localStorage.setItem('qa-state',JSON.stringify(state));return JSON.stringify(state);},
   restState:()=>JSON.stringify({end:state.restEndAt,duration:state.restDurationMs,started:state.restStartedAt}),ready(){},setThemeColors(){},setModalOpen(){},hideKeyboard(){},showKeyboard(){},haptic(){qa.haptics++;},restComplete(){},spotifyState:()=>JSON.stringify({connected:false}),preciseTimers:()=>true};
 });
 await page.goto(fixture.url);await page.waitForFunction(()=>!!window.Workouts);
 const data=()=>page.evaluate(()=>Workouts.debug()),sets=()=>page.locator('.set:not(.set-add)'),dialog=()=>page.locator('dialog[open]');
 await page.getByRole('button',{name:'Edit workout',exact:true}).tap();
 await page.getByRole('button',{name:'Add exercise',exact:true}).tap();
 await page.getByRole('textbox',{name:'Exercise',exact:true}).fill('Squat');
 await page.getByRole('textbox',{name:'Reps per set'}).fill('5, 5+');
 await page.getByRole('spinbutton',{name:'Rest in seconds'}).fill('90');
 await dialog().getByRole('button',{name:'Add',exact:true}).tap();
 check((await data()).routines[0].exercises[0].sets[1].amrap,'A plus marks an AMRAP set in the routine editor');
 await page.getByRole('button',{name:'Start workout',exact:true}).tap();
 check(await sets().nth(1).textContent()==='5+','AMRAP has the same circle with a plus suffix');
 let deadline=0;
 for(const reps of [5,4,3,2,1,null,5]){
  await sets().first().tap();const d=await data();
  check(d.active.exercises[0].sets[0].reps===reps,'Tap cycle records '+reps);
  check(d.restEndAt>deadline&&d.restDurationMs===90000,'Each final tap restarts the full rest duration');deadline=d.restEndAt;
 }
 check(await dialog().count()===0&&await page.getByLabel('Rest timer', {exact:true}).isVisible(),'Rep taps keep the rest timer visible without an editor');
 await page.waitForTimeout(1100);
 check((await data()).restEndAt===deadline,'Native timer polling keeps the saved rest deadline');
 const touch=await page.context().newCDPSession(page);
 async function holdAndDrag(delta,cancel=false,index=1){
  const box=await sets().nth(index).boundingBox(),x=box.x+box.width/2,y=box.y+box.height/2;
  const before=await data(),initialSet=before.active.exercises[0].sets[index];
  const initialReps=initialSet.reps??initialSet.target;
  const previewReps=initialReps+Math.round(delta/12);
  const haptics=await page.evaluate(()=>qa.haptics);
  await touch.send('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x,y}]});await page.waitForTimeout(330);
  check(await page.locator('.sets input[type=range]').count()===0,'Holding a rep circle keeps the set row free of sliders');
  for(let i=1;i<=6;i++)await touch.send('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x,y:y-delta*i/6}]});
  check(await sets().nth(index).textContent()===String(previewReps),'The circle previews the rep count while dragging');
  check((await data()).restEndAt===before.restEndAt,'Dragging does not start rest before release');
  check(await page.evaluate(()=>qa.haptics)>haptics,'Rep selection provides haptic feedback');
  await touch.send('Input.dispatchTouchEvent',{type:cancel?'touchCancel':'touchEnd',touchPoints:[]});
  await page.waitForTimeout(500);
  check(await sets().nth(index).textContent()===String(cancel?initialReps:previewReps||initialSet.target),'The circle shows the saved count after release or cancel');
 }
 await holdAndDrag(48);
 let saved=await data();
 check(saved.active.exercises[0].sets[1].reps===9,'Holding and dragging upward logs the chosen AMRAP reps on release');
 check(saved.restEndAt>deadline&&saved.restDurationMs===90000,'Releasing AMRAP restarts the full timer');
 check(await page.locator('#today-tab').getAttribute('aria-selected')==='true','AMRAP dragging does not switch pages');
 const before=JSON.stringify(saved.active.exercises[0].sets[1]);deadline=saved.restEndAt;
 await holdAndDrag(24,true);
 check(JSON.stringify((await data()).active.exercises[0].sets[1])===before&&(await data()).restEndAt===deadline,'Cancelled AMRAP gestures preserve reps and rest');
 for(const reps of [8,7,6,5,4,3,2,1,null]){await sets().nth(1).tap();check((await data()).active.exercises[0].sets[1].reps===reps,'AMRAP taps count down to '+reps);}
 check(await sets().nth(1).textContent()==='5+','Clearing restores the AMRAP target and plus');
 await sets().nth(1).tap();check((await data()).active.exercises[0].sets[1].reps===5,'A cleared AMRAP circle cycles back to its target');
 await holdAndDrag(-24,false,0);
 check((await data()).active.exercises[0].sets[0].reps===3,'Dragging down on a regular set logs fewer reps');
 await holdAndDrag(48,false,0);
 check((await data()).active.exercises[0].sets[0].reps===7,'Dragging up on a regular set can exceed the target');
 await page.evaluate(()=>qa.fail=true);const snapshot=await data();await sets().first().tap();
 check(JSON.stringify(await data())===JSON.stringify(snapshot),'A failed save does not alter reps or restart rest');await page.evaluate(()=>qa.fail=false);
 await holdAndDrag(-84,false,0);
 check((await data()).active.exercises[0].sets[0].reps===null,'Dragging a regular set to zero clears it');
 await page.reload();await page.waitForFunction(()=>!!window.Workouts);
 check((await data()).active.exercises[0].sets[1].amrap&&(await data()).active.exercises[0].sets[1].reps===5,'AMRAP and recorded reps survive reopening');
 await page.getByRole('button',{name:'Finish',exact:true}).tap();await page.getByRole('button',{name:'Save workout',exact:true}).tap();
 check((await data()).history[0].exercises[0].sets[1].reps===5&&(await data()).restEndAt===0,'Finishing archives AMRAP reps and stops rest');
 await page.getByRole('tab',{name:'History'}).tap();
 await page.waitForFunction(()=>document.querySelector('#history-tab').getAttribute('aria-selected')==='true');
 check(await page.getByRole('img',{name:'Heaviest completed weight by workout: 0 lb'}).isVisible(),'History shows a trend graph next to the exercise');
 await page.locator('.history-card').tap();
 await dialog().waitFor({state:'visible'});
 check(await dialog().getByText('1. 0 reps',{exact:true}).isVisible(),'History displays cleared sets as zero reps');
 check(errors.length===0,'No errors during real touch rep editing');
 console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();await fixture.close();}
