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
  let state=JSON.parse(localStorage.getItem('qa-state')||'{}');
  window.qa={haptics:0,fail:false,overlayVisible:false,restReads:0,dismissOverlay(){state.restDismissed=true;qa.overlayVisible=false;}};
  window.WorkoutNative={readState:()=>JSON.stringify(state),writeState:raw=>{if(qa.fail)return null;state=JSON.parse(raw);state.revision=(state.revision||0)+1;localStorage.setItem('qa-state',JSON.stringify(state));return JSON.stringify(state);},
   floatingRestVisible:()=>qa.overlayVisible,
   restState:()=>{qa.restReads++;return JSON.stringify({end:state.restEndAt,duration:state.restDurationMs,started:state.restStartedAt,dismissed:state.restDismissed,position:state.restPosition});},ready(){},setThemeColors(){},setModalOpen(){},hideKeyboard(){},showKeyboard(){},haptic(){qa.haptics++;},restComplete(){},spotifyState:()=>JSON.stringify({connected:false}),preciseTimers:()=>true};
 });
 await page.goto(fixture.url);await page.waitForFunction(()=>!!window.Workouts);
 const data=()=>page.evaluate(()=>Workouts.debug()),sets=()=>page.locator('.set:not(.set-add)'),dialog=()=>page.locator('dialog[open]');
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).tap();
 await page.getByRole('button',{name:'Add exercise',exact:true}).tap();
 await page.getByRole('textbox',{name:'Exercise',exact:true}).fill('Squat');
 await page.getByRole('textbox',{name:'Sets/Reps'}).fill('5, 5+, 5');
 await page.getByRole('spinbutton',{name:'Rest in seconds'}).fill('90');
 await dialog().getByRole('button',{name:'Add',exact:true}).tap();
 check((await data()).routines[0].exercises[0].sets[1].amrap,'A plus marks an AMRAP set in the routine editor');
 await page.getByRole('button',{name:'Workout A',exact:true}).tap();
 await page.getByRole('button',{name:'Start workout',exact:true}).tap();
 check((await sets().first().boundingBox()).width===54,'Rep circles are 54 pixels wide on a standard phone');
 check(await sets().nth(1).textContent()==='5+','AMRAP has the same circle with a plus suffix');
 let deadline=0;
 for(const reps of [5,4,3,2,1,0,null,5]){
  await sets().first().tap();const d=await data();
  check(d.active.exercises[0].sets[0].reps===reps,'Tap cycle records '+reps);
  check(reps===null?d.restEndAt===0&&d.restDurationMs===0:d.restEndAt>deadline&&d.restDurationMs===90000,'Logging reps restarts rest and clearing cancels it');deadline=d.restEndAt;
 }
 check(await dialog().count()===0&&await page.getByLabel('Rest timer', {exact:true}).isVisible(),'Rep taps keep the rest timer visible without an editor');
 await page.waitForTimeout(1100);
 check((await data()).restEndAt===deadline,'Native timer polling keeps the saved rest deadline');
 const rest=page.getByLabel('Rest timer',{exact:true});
 const box=await rest.boundingBox(),x=box.x+box.width/2,y=box.y+box.height/2;
 await page.mouse.move(x,y);await page.mouse.down();await page.mouse.move(x-12,y-12);
 const target=page.locator('#timer-dismiss');
 check(!(await target.isVisible()),'The X stays hidden while the timer is far away');
 const {cx,cy}=await target.evaluate(element=>{
  const box=element.getBoundingClientRect();
  return {cx:box.x+box.width/2,cy:box.y+box.height/2};
 });
 const beforeHover=await page.evaluate(()=>qa.haptics);
 const boundary=await page.evaluate(()=>innerHeight*2/3);
 await page.mouse.move(x,boundary+2,{steps:5});
 check(await target.isVisible()&&await page.evaluate(()=>qa.haptics)===beforeHover,'The X appears as soon as the drag enters the bottom third, even at the side of the screen');
 await page.mouse.move(x,boundary-2,{steps:4});
 check(!(await target.isVisible()),'The X disappears again above the bottom third');
 await page.mouse.move(cx,cy,{steps:5});
 check(await page.locator('#timer-dismiss').evaluate(target=>target.classList.contains('over')&&getComputedStyle(target,'::before').content!=='none'),'Entering the X expands its drop zone');
 check(await page.evaluate(()=>qa.haptics)===beforeHover+1,'The X gives one haptic on entry');
 await page.mouse.move(cx+44,cy,{steps:4});
 check(await page.locator('#timer-dismiss').evaluate(target=>target.classList.contains('over'))&&
       await page.evaluate(()=>qa.haptics)===beforeHover+1,'The expanded zone retains hover without repeated haptics');
 await page.mouse.up();
 check(!(await rest.isVisible())&&(await data()).restEndAt===deadline,'Dropping on the X hides only the current floating timer');
 await sets().nth(2).tap();
 check(await rest.isVisible()&&!(await data()).restDismissed,'Logging another set restores the floating timer');
 await page.evaluate(()=>qa.overlayVisible=true);
 await rest.waitFor({state:'hidden'});
 const restReads=await page.evaluate(()=>qa.restReads);
 await page.waitForFunction(reads=>qa.restReads>reads,restReads);
 await page.evaluate(()=>{
  const timer=document.querySelector('#rest');
  qa.flashed=false;
  qa.timerObserver=new MutationObserver(records=>{
   if(!timer.hidden||records.some(record=>record.oldValue===null))qa.flashed=true;
  });
  qa.timerObserver.observe(timer,{attributeFilter:['hidden'],attributeOldValue:true});
  qa.dismissOverlay();
 });
 await page.waitForFunction(()=>Workouts.debug().restDismissed);
 check(await page.evaluate(()=>{qa.timerObserver.disconnect();return !qa.flashed;})&&!(await rest.isVisible()),
   'Dismissing the Android overlay never flashes the in-app timer');
 await sets().nth(2).tap();
 check(await rest.isVisible()&&!(await data()).restDismissed,'The in-app timer returns for the next set without the Android overlay');
 deadline=(await data()).restEndAt;
 const touch=await page.context().newCDPSession(page);
 check(await sets().nth(1).evaluate(circle=>{
  const box=circle.getBoundingClientRect();
  return document.elementFromPoint(box.x+box.width/2,box.y+box.height/2)===circle;
 }),'A touch on the centered label hits the rep button itself');
 await page.evaluate(()=>document.addEventListener('pointerdown',event=>{
  const circle=event.target.closest('.set');
  if(!circle)return;
  // An event bubbling from the label must never cancel the button's capture.
  setTimeout(()=>circle.querySelector('.set-reps').dispatchEvent(new PointerEvent('lostpointercapture',{
   bubbles:true,pointerId:event.pointerId,pointerType:'touch'
  })),300);
 },{once:true}));
 async function holdAndDrag(delta,cancel=false,index=1){
  const box=await sets().nth(index).boundingBox(),x=box.x+box.width/2,y=box.y+box.height/2;
  const before=await data(),initialSet=before.active.exercises[0].sets[index];
  const initialReps=initialSet.reps??initialSet.target;
  const offset=initialReps+Math.round(delta/12);
  const previewReps=offset<0?null:Math.min(initialSet.amrap?100:initialSet.target,offset);
  const labelFor=reps=>reps==null?String(initialSet.target)+(initialSet.amrap?'+':''):String(reps);
  const haptics=await page.evaluate(()=>qa.haptics);
  await touch.send('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x,y}]});await page.waitForTimeout(330);
  check(await page.locator('.sets input[type=range]').count()===0,'Holding a rep circle keeps the set row free of sliders');
  for(let i=1;i<=6;i++)await touch.send('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x,y:y-delta*i/6}]});
  check(await sets().nth(index).textContent()===labelFor(previewReps),'The circle previews the rep count while dragging');
  check((await data()).restEndAt===before.restEndAt,'Dragging does not start rest before release');
  check(await page.evaluate(()=>qa.haptics)>haptics,'Rep selection provides haptic feedback');
  await touch.send('Input.dispatchTouchEvent',{type:cancel?'touchCancel':'touchEnd',touchPoints:[]});
  await page.waitForTimeout(500);
  check(await sets().nth(index).textContent()===labelFor(cancel?initialSet.reps:previewReps),'The circle shows the saved count after release or cancel');
 }
 await holdAndDrag(48);
 let saved=await data();
 check(saved.active.exercises[0].sets[1].reps===9,'Holding and dragging upward logs the chosen AMRAP reps on release');
 check(saved.restEndAt>deadline&&saved.restDurationMs===90000,'Releasing AMRAP restarts the full timer');
 check(await page.locator('#today-tab').getAttribute('aria-selected')==='true','AMRAP dragging does not switch pages');
 const before=JSON.stringify(saved.active.exercises[0].sets[1]);deadline=saved.restEndAt;
 await holdAndDrag(24,true);
 check(JSON.stringify((await data()).active.exercises[0].sets[1])===before&&(await data()).restEndAt===deadline,'Cancelled AMRAP gestures preserve reps and rest');
 for(const reps of [8,7,6,5,4,3,2,1,0,null]){await sets().nth(1).tap();check((await data()).active.exercises[0].sets[1].reps===reps,'AMRAP taps count down to '+reps);}
 check(await sets().nth(1).textContent()==='5+','Clearing restores the AMRAP target and plus');
 await sets().nth(1).tap();check((await data()).active.exercises[0].sets[1].reps===5,'A cleared AMRAP circle cycles back to its target');
 await holdAndDrag(-24,false,0);
 check((await data()).active.exercises[0].sets[0].reps===3,'Dragging down on a regular set logs fewer reps');
 await holdAndDrag(48,false,0);
 check((await data()).active.exercises[0].sets[0].reps===5,'Dragging up on a regular set stops at the target');
 await page.evaluate(()=>qa.fail=true);const snapshot=await data();await sets().first().tap();
 check(JSON.stringify(await data())===JSON.stringify(snapshot),'A failed save does not alter reps or restart rest');await page.evaluate(()=>qa.fail=false);
 await holdAndDrag(-60,false,0);
 check((await data()).active.exercises[0].sets[0].reps===0&&await sets().first().getAttribute('aria-pressed')==='true','Dragging to zero logs zero as a selected count');
 await holdAndDrag(-12,false,0);
 check((await data()).active.exercises[0].sets[0].reps===null&&await sets().first().getAttribute('aria-pressed')==='false','Dragging below zero clears the set');
 await holdAndDrag(-60,false,0);
 await page.reload();await page.waitForFunction(()=>!!window.Workouts);
 check(await sets().first().textContent()==='0','An explicit zero survives reopening');
 check((await data()).active.exercises[0].sets[1].amrap&&(await data()).active.exercises[0].sets[1].reps===5,'AMRAP and recorded reps survive reopening');
 await holdAndDrag(-72,false,2);
 await page.getByRole('button',{name:'Finish',exact:true}).tap();await page.getByRole('button',{name:'Save workout',exact:true}).tap();
 check((await data()).history[0].exercises[0].sets[1].reps===5&&(await data()).restEndAt===0,'Finishing archives AMRAP reps and stops rest');
 await page.getByRole('tab',{name:'History'}).tap();
 await page.waitForFunction(()=>document.querySelector('#history-tab').getAttribute('aria-selected')==='true');
 check(await page.getByRole('img',{name:'Heaviest completed weight by workout: 0 lb'}).isVisible(),'History shows a trend graph next to the exercise');
 await page.locator('.history-card').tap();
 await dialog().waitFor({state:'visible'});
 check(await dialog().getByText('1. 0 reps · 0 lb',{exact:true}).isVisible(),'History stores an explicit zero with its weight');
 check(await dialog().getByText('3. 0 reps',{exact:true}).isVisible(),'History displays cleared sets as zero reps');
 await dialog().getByRole('button',{name:'Cancel',exact:true}).tap();
 await page.getByRole('tab',{name:'Today'}).tap();
 await page.waitForFunction(()=>document.querySelector('#today-tab').getAttribute('aria-selected')==='true');
 await page.getByRole('button',{name:'Workout A',exact:true}).tap();
 await page.getByRole('button',{name:'Start workout'}).tap();
 const trash=page.locator('#discard');
 await trash.tap();
 check((await data()).active&&await dialog().count()===0&&await trash.evaluate(el=>el.classList.contains('danger')),'The first trash tap arms confirmation in place');
 await page.waitForFunction(()=>!document.querySelector('#discard').classList.contains('danger'));
 check((await data()).active,'The trash confirmation expires without discarding');
 await trash.tap();
 await trash.tap();
 check((await data()).active===null&&(await data()).history.length===1,'A second trash tap discards only the unfinished workout');
 const startWorkout=page.getByRole('button',{name:'Start workout',exact:true});
 const finishWorkout=page.getByRole('button',{name:'Finish',exact:true});
 await startWorkout.tap();
 const blankId=(await data()).active.id;
 await page.setViewportSize({width:320,height:650});
 await finishWorkout.tap();
 await dialog().waitFor({state:'visible'});
 check(await dialog().getByRole('heading',{name:'Finish blank workout?'}).isVisible(),
  'Finishing with no logged sets opens the blank workout prompt');
 check(await dialog().locator('.actions').evaluate(actions=>{
  const buttons=[...actions.querySelectorAll('button')].map(button=>button.getBoundingClientRect());
  return buttons.length===3&&buttons.every(button=>button.top===buttons[0].top&&button.height===buttons[0].height)&&
   actions.scrollWidth<=actions.clientWidth;
 }),'The three blank workout actions stay on one row at 320px');
 await dialog().getByRole('button',{name:'Cancel',exact:true}).tap();
 check((await data()).active.id===blankId,'Cancel returns to the same unfinished workout');
 await finishWorkout.tap();
 await dialog().getByRole('button',{name:'Discard',exact:true}).tap();
 check((await data()).active===null&&(await data()).history.length===1,'Discard closes a blank workout without recording history');
 await startWorkout.tap();
 const loggedBlankId=(await data()).active.id;
 await finishWorkout.tap();
 await dialog().getByRole('button',{name:'Log blank',exact:true}).tap();
 const blankHistory=await data();
 check(blankHistory.active===null&&blankHistory.history.length===2&&blankHistory.history[0].id===loggedBlankId&&
  blankHistory.history[0].exercises.every(exercise=>exercise.sets.every(set=>set.reps===null)),
 'Log blank archives the workout with its planned sets and no recorded reps');
 check(errors.length===0,'No errors during real touch rep editing');
 console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();await fixture.close();}
