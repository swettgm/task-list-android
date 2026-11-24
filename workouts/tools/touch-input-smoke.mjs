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
  let state={};window.qa={haptics:0,keyboard:0};
  window.WorkoutNative={
   readState:()=>JSON.stringify(state),writeState:raw=>{state=JSON.parse(raw);state.revision=(state.revision||0)+1;return JSON.stringify(state);},
   ready(){},setThemeColors(){},setModalOpen(){},hideKeyboard(){},showKeyboard(){qa.keyboard++;},haptic(){qa.haptics++;},
   spotifyState:()=>JSON.stringify({connected:false,available:false,title:'Spotify'}),preciseTimers:()=>true
  };
 });
 await page.goto(fixture.url);await page.waitForFunction(()=>!!window.Workouts);
 const touch=await page.context().newCDPSession(page);
 async function swipe(from,to,cancel=false,y=400){
  await touch.send('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x:from,y}]});
  for(let i=1;i<=8;i++){
   await touch.send('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:from+(to-from)*i/8,y}]});
   await page.waitForTimeout(10);
  }
  check(await page.locator('#today-tab').getAttribute('aria-selected')===(from>to?'true':'false'),'Selection waits for swipe completion');
  await touch.send('Input.dispatchTouchEvent',{type:cancel?'touchCancel':'touchEnd',touchPoints:[]});
 }
 await swipe(320,70);
 await page.waitForFunction(()=>document.querySelector('#history-tab').getAttribute('aria-selected')==='true');
 check(await page.locator('#history').isVisible(),'A real left touch swipe opens History');
 await swipe(70,320);
 await page.waitForFunction(()=>document.querySelector('#today-tab').getAttribute('aria-selected')==='true');
 check(await page.locator('#today').isVisible(),'A real right touch swipe returns to Today');
 const haptics=await page.evaluate(()=>qa.haptics);
 await swipe(320,290,true);await page.waitForTimeout(260);
 check(await page.locator('#today-tab').getAttribute('aria-selected')==='true'&&await page.evaluate(()=>qa.haptics)===haptics,'Cancelled touch swipes keep the selected tab without a success haptic');
 await page.getByRole('button',{name:'Edit workout',exact:true}).tap();
 await page.getByRole('button',{name:'Add routine',exact:true}).tap();
 const name=page.getByRole('textbox',{name:'Name',exact:true});
 await name.tap();await page.keyboard.type('Workout B',{delay:10});
 check(await name.inputValue()==='Workout B','Routine names accept touch focus and keyboard typing');
 check(await page.evaluate(()=>qa.keyboard)>0,'The routine editor requests the Android keyboard');
 await page.getByRole('button',{name:'Add',exact:true}).tap();
 check(await page.locator('#routine-picker').textContent().then(t=>t.includes('Workout B')),'The new routine saves and becomes selected');
 await page.getByRole('button',{name:'Edit workout',exact:true}).tap();
 await page.getByRole('button',{name:'Add exercise',exact:true}).tap();
 const exercise=page.getByRole('textbox',{name:'Exercise',exact:true});
 await exercise.tap();await page.keyboard.type('Deadlift',{delay:10});
 const weight=page.getByRole('spinbutton',{name:'Weight',exact:true});
 await weight.tap();await weight.press('ControlOrMeta+A');await page.keyboard.type('135');
 const reps=page.getByRole('textbox',{name:'Reps per set'});
 await reps.tap();await reps.press('ControlOrMeta+A');await page.keyboard.type('5, 5, 5');
 const rest=page.getByRole('spinbutton',{name:'Rest in seconds'});
 await rest.tap();
 await rest.press('ControlOrMeta+A');
 await page.keyboard.type('120');
 const notes=page.getByRole('textbox',{name:'Notes',exact:true});
 await notes.tap();
 await page.keyboard.type('Controlled reps');
 check(await exercise.inputValue()==='Deadlift'&&await weight.inputValue()==='135'&&await reps.inputValue()==='5, 5, 5'&&await rest.inputValue()==='120'&&await notes.inputValue()==='Controlled reps','Text, numeric, reps and notes fields accept touch and typing');
 await page.getByRole('combobox',{name:'Unit'}).selectOption('kg');
 await page.getByRole('button',{name:'Add',exact:true}).tap();
 const saved=await page.evaluate(()=>Workouts.debug());
 const row=saved.routines.find(r=>r.name==='Workout B').exercises[0];
 check(row.name==='Deadlift'&&row.weight===135&&row.unit==='kg'&&row.sets.length===3&&row.notes==='Controlled reps','The completed exercise editor saves all typed fields');
 await page.getByRole('button',{name:'Start workout',exact:true}).tap();
 await page.getByRole('button',{name:'Log set 1 of Deadlift',exact:true}).tap();
 const setRow=await page.locator('.sets').boundingBox();
 await swipe(150,30,false,setRow.y+setRow.height/2);
 await page.waitForFunction(()=>document.querySelector('#history-tab').getAttribute('aria-selected')==='true');
 check(await page.evaluate(()=>Workouts.debug().active.exercises[0].sets.filter(s=>s.reps!==null).length)===1,'Swiping across sets changes pages without logging another set');
 await page.getByRole('tab',{name:'Today',exact:true}).tap();
 await page.waitForFunction(()=>document.querySelector('#today-tab').getAttribute('aria-selected')==='true');
 await page.getByRole('button',{name:'Finish',exact:true}).tap();
 await page.getByRole('button',{name:'Save workout',exact:true}).tap();
 const exerciseCard=await page.locator('.exercise').boundingBox();
 await swipe(320,70,false,exerciseCard.y+exerciseCard.height/2);
 await page.waitForFunction(()=>document.querySelector('#history-tab').getAttribute('aria-selected')==='true');
 check(await page.locator('dialog').count()===0,'Swiping across an exercise does not open its editor');
 const historyCard=await page.locator('.history-card').boundingBox();
 await swipe(70,320,false,historyCard.y+historyCard.height/2);
 await page.waitForFunction(()=>document.querySelector('#today-tab').getAttribute('aria-selected')==='true');
 check(await page.locator('dialog').count()===0,'Swiping across a history card returns to Today without opening it');
 check(await page.locator('#settings').textContent()==='⚙','The settings glyph matches Tasks');
 check(await page.locator('#spotify-play svg').evaluate(e=>getComputedStyle(e).stroke==='none'&&getComputedStyle(e).fill!=='none'),'Spotify transport icons use solid shapes');
 const sizes=await page.locator('#spotify').evaluate(e=>({bar:e.getBoundingClientRect().height,art:document.querySelector('#spotify-art').getBoundingClientRect().height}));
 check(sizes.bar-sizes.art<=12&&sizes.bar>sizes.art,'The Spotify bar fits closely around the artwork');
 check(errors.length===0,'No browser errors during touch editing and swipes');
 console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();await fixture.close();}
