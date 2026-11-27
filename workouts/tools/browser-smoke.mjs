import {chromium} from 'playwright';
import {existsSync} from 'node:fs';
import {mkdir} from 'node:fs/promises';
import assert from 'node:assert/strict';
import {serveFixture} from './browser-fixture-server.mjs';
const fixture=await serveFixture('app/src/main/assets/index.html');
const browser=await chromium.launch({executablePath:'/usr/bin/chromium',headless:true,args:['--no-sandbox','--disable-dev-shm-usage']});
const checks=[],errors=[];const check=(value,label)=>{assert.ok(value,label);checks.push(label);};
try{
 const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true});
 page.on('pageerror',e=>errors.push(e.message));
 await page.addInitScript(()=>{
  let state=JSON.parse(localStorage.getItem('native-workouts')||'{}');
  window.qa={fail:false,rest:0,haptics:0,keyboard:0,keyboardHides:0,music:{connected:false,available:false,title:'Spotify',artist:'',playing:false},commands:[]};
  window.WorkoutNative={
   readState:()=>JSON.stringify(state),
   writeState:raw=>{if(qa.fail)return null;const next=JSON.parse(raw);if((next.revision||0)!==(state.revision||0))return null;next.revision=(state.revision||0)+1;state=next;localStorage.setItem('native-workouts',JSON.stringify(state));return JSON.stringify(state);},
   ready(){},setThemeColors(){},setModalOpen(){},hideKeyboard(){qa.keyboardHides++;},showKeyboard(){qa.keyboard++;},haptic(){qa.haptics++;},
   spotifyState:()=>JSON.stringify(qa.music),spotifyCommand:command=>qa.commands.push(command),openSpotify:()=>qa.openedSpotify=true,spotifyAccess:()=>qa.connectedSpotify=true,preciseTimers:()=>true,restComplete(){qa.rest++;},previewVibration(){},requestNotifications(){},overlayAllowed(){return qa.overlayGranted===true;},allowOverlay(){qa.overlayRequests=(qa.overlayRequests||0)+1;},previewRestSequence(...args){qa.preview=args;},stopRestSequence(){qa.previewStopped=true;}
  };
 });
 await page.goto(fixture.url);await page.waitForFunction(()=>!!window.Workouts);
 check(await page.title()==='Exercise'&&await page.getByRole('heading',{name:'Exercise',exact:true}).count()===1,'The app is named Exercise');
 check(await page.locator('#settings').evaluate(e=>parseFloat(getComputedStyle(e).borderWidth)===0),'The settings gear has no border');
 const state=()=>page.evaluate(()=>Workouts.debug()),dialog=()=>page.locator('dialog[open]');
 check((await state()).routines[0].exercises.length===0&&(await state()).history.length===0,'Fresh install starts with an empty routine and no workout history');
 check(await page.getByLabel('Spotify player').isVisible(),'Compact Spotify player uses the app layout');
 check(await page.locator('#spotify').evaluate(bar=>
  [bar,...bar.querySelectorAll('button, span')].every(part=>getComputedStyle(part).webkitTapHighlightColor==='rgba(0, 0, 0, 0)')&&
  getComputedStyle(bar.querySelector('#spotify-title')).userSelect==='none'
 ),'The Spotify bar, controls, and track text have no touch selection highlight');
 await page.waitForFunction(()=>document.querySelector('#spotify-title').textContent==='Open Spotify');
 for(const id of ['spotify-art','spotify-prev','spotify-play','spotify-next']){
  await page.evaluate(()=>qa.openedSpotify=false);
  await page.locator('#'+id).click();
  check(await page.evaluate(()=>qa.openedSpotify&&qa.commands.length===0),'The idle '+id+' remains visible and opens Spotify');
 }
 await page.evaluate(()=>qa.openedSpotify=false);
 check(await page.locator('#spotify').evaluate(bar=>{
  const box=bar.getBoundingClientRect(),scale=new DOMMatrix(getComputedStyle(bar).transform).a;
  return Math.abs(scale-1.1)<.001&&box.left>=8&&box.right<=innerWidth-8;
 }),'The entire player is scaled 10% and stays inside the screen');
 const playerGeometry=()=>page.locator('#spotify').evaluate(player=>{
  const bar=player.getBoundingClientRect();
  const art=player.querySelector('#spotify-art').getBoundingClientRect();
  const title=player.querySelector('#spotify-title').getBoundingClientRect();
  return {height:bar.height,gaps:[art.left-bar.left,art.top-bar.top,bar.bottom-art.bottom],
          titleOffset:(title.top+title.bottom-bar.top-bar.bottom)/2};
 });
 const idle=await playerGeometry();
 check(Math.abs(idle.titleOffset)<.6,'Open Spotify is centered vertically with no artist');
 await page.evaluate(()=>{qa.music={connected:true,available:true,title:'Training track',artist:'Example artist',playing:true,duration:180000,position:45000,speed:1,art:'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/a9sAAAAASUVORK5CYII='};});
 await page.waitForFunction(()=>document.querySelector('#spotify-title').textContent==='Training track');
 check(await page.locator('#spotify-progress').isVisible()&&await page.locator('#spotify-progress span').evaluate(bar=>bar.style.transform.startsWith('scaleX(0.25')),'Playback shows the track progress on the bottom edge');
 await page.getByRole('button',{name:'Pause Spotify',exact:true}).click();
 await page.getByRole('button',{name:'Next Spotify track',exact:true}).click();
 check(await page.evaluate(()=>qa.haptics>=3),'Spotify buttons provide haptic feedback');
 check(await page.evaluate(()=>qa.commands.join()==='play,next'),'Player forwards pause and next without changing workout state');
 const playing=await playerGeometry();
 check(playing.height===idle.height&&playing.gaps.every(gap=>Math.abs(gap-playing.gaps[0])<.6),'Playback retains identical player height and even artwork padding');
 const spotifyTouch=await page.context().newCDPSession(page);
 const spotifyBox=await page.locator('#spotify-open').boundingBox();
 const x=spotifyBox.x+spotifyBox.width/2,y=spotifyBox.y+spotifyBox.height/2;
 for(const dx of [-64,64]){
  await spotifyTouch.send('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x,y}]});
  for(const fraction of [.3,.6,1])await spotifyTouch.send('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:x+dx*fraction,y}]});
  await spotifyTouch.send('Input.dispatchTouchEvent',{type:'touchEnd',touchPoints:[]});
 }
 check(await page.evaluate(()=>qa.commands.join()==='play,next,next,prev'&&!qa.openedSpotify),'Swiping the Spotify bar skips tracks without opening Spotify');
 await page.evaluate(()=>{qa.music.title='Previous track';qa.music.artist='Another artist';qa.music.art='';});
 await page.waitForFunction(()=>document.querySelector('#spotify-title').textContent==='Previous track');
 check(await page.locator('#spotify-copy').evaluate(el=>el.getAnimations().length>0)&&
       await page.locator('#spotify-art').evaluate(el=>el.getAnimations().length===0),
       'Track text slides independently of the artwork');
 for(const [name,reps,weight] of [['Barbell Curl','8, 8, 8',45],['Front/Lateral Raise','10, 10, 10',10],['Deadlift','5, 5, 5',275],['Bench Press','5, 5, 5',165],['Leg Extension','6, 8, 10',50],['Leg Curl','6, 8, 10',65]]){
  await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
  await dialog().getByRole('button',{name:'Add exercise',exact:true}).click();
  if(name==='Barbell Curl'){
   const weight=await dialog().locator('.weight-entry').boundingBox();
   const reps=await dialog().getByRole('textbox',{name:'Sets/Reps',exact:true}).boundingBox();
   check(weight.y===reps.y&&weight.height===reps.height&&weight.x<reps.x,'Weight and Sets/Reps are aligned on one row');
   check(await dialog().locator('#exercise-weight-unit').textContent()==='lbs','Pound weights show an inline lbs suffix');
   check(await dialog().getByRole('textbox',{name:'Sets/Reps',exact:true}).inputValue()==='5, 5, 5','A fresh exercise starts at three sets of five');
   check(await dialog().locator('#exercise-rep-hint').textContent()==='+ ➜ AMRAP | 5, 5, 5+ ➜ 2 regular sets + 1 AMRAP.','The Sets/Reps hint uses the Tasks arrows and explains AMRAP');
  }
  await dialog().getByRole('textbox',{name:'Exercise',exact:true}).fill(name);
  await dialog().getByRole('textbox',{name:'Sets/Reps'}).fill(reps);
  await dialog().getByRole('spinbutton',{name:'Weight',exact:true}).fill(String(weight));
  await dialog().getByRole('button',{name:'Add',exact:true}).click();
 }
 check((await state()).settings.notifications===false,'Notifications are disabled by default');
 const workoutA=page.getByRole('button',{name:'Workout A',exact:true});
 check(await workoutA.getAttribute('aria-expanded')==='false','Routine cards begin collapsed');
 const arrow=page.getByRole('button',{name:'Expand Workout A',exact:true});
 const pencil=page.getByRole('button',{name:'Edit Workout A',exact:true});
 check((await arrow.boundingBox()).x>(await pencil.boundingBox()).x&&await arrow.locator('svg').evaluate(svg=>svg.getBoundingClientRect().height===26),'The larger chevron sits to the right of the pencil');
 check(await page.locator('#today-date').count()===0,'Today opens straight into the routine cards');
 await arrow.click();
 check(await workoutA.getAttribute('aria-expanded')==='true','The right chevron opens the card');
 check(await page.locator('.exercise:visible').count()===6&&(await state()).active===null,'Opening a card reveals exercise rows without starting a workout');
 check(await page.getByRole('region',{name:'Workout A',exact:true}).evaluate(card=>{
  const outer=card.getBoundingClientRect();
  const left=card.querySelector('.routine-name').getBoundingClientRect().left;
  const elements=card.querySelectorAll('.exercise-name');
  const matchingLeft=[...elements].every(item=>Math.abs(item.getBoundingClientRect().left-left)<.25);
  const matchingRight=[...card.querySelectorAll('.routine-chevron,.exercise-plan,.routine-start')]
    .every(item=>Math.abs(outer.right-item.getBoundingClientRect().right-(left-outer.left))<.25);
  return matchingLeft&&matchingRight;
 }),'Routine text and trailing actions share matching card insets');
 check(await page.locator('.exercise').first().evaluate(row=>parseFloat(getComputedStyle(row).borderBottomWidth)===0),'The routine cards separate exercises without divider lines');
 for(const width of [320,390,600]){
  await page.setViewportSize({width,height:844});
  check(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'Home fits at '+width+'px');
 }
 await page.setViewportSize({width:390,height:360});
 const headerPosition=()=>page.evaluate(()=>[...document.querySelectorAll('#app>header,#app>.tabs')].map(e=>e.getBoundingClientRect().top));
 const fixedHeader=await headerPosition();
 const workoutPane=page.locator('#today');
 await workoutPane.hover();
 await page.mouse.wheel(0,450);
 await page.waitForFunction(()=>document.querySelector('#today').scrollTop>0);
 check(JSON.stringify(await headerPosition())===JSON.stringify(fixedHeader)&&await page.evaluate(()=>window.scrollY===0),
       'Long workouts scroll within the pane while the header and tabs stay pinned');
 await page.getByRole('tab',{name:'History',exact:true}).click();
 await page.waitForFunction(()=>document.querySelector('#history-tab').getAttribute('aria-selected')==='true');
 check(await page.locator('#history').evaluate(pane=>pane.scrollHeight===pane.clientHeight&&pane.scrollTop===0),
       'Empty History has no overflow even after scrolling Today');
 await page.getByRole('tab',{name:'Today',exact:true}).click();
 await page.waitForFunction(()=>document.querySelector('#today-tab').getAttribute('aria-selected')==='true');
 await page.setViewportSize({width:390,height:844});
 await workoutPane.evaluate(pane=>pane.scrollTop=0);
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
 await dialog().getByRole('button',{name:'Edit Leg Extension',exact:true}).click();
 check(await dialog().getByRole('textbox',{name:'Sets/Reps'}).inputValue()==='6, 8, 10','Variable rep targets are editable');
 await dialog().getByRole('button',{name:'Cancel',exact:true}).click();
 const beforePencil=await pencil.boundingBox();
 const beforeChevron=await page.getByRole('button',{name:'Collapse Workout A',exact:true}).boundingBox();
 await page.getByRole('region',{name:'Workout A',exact:true}).getByRole('button',{name:'Start workout',exact:true}).click();
 const afterPencil=await pencil.boundingBox(),afterTrash=await page.locator('#discard').boundingBox();
 check(afterPencil.x===beforePencil.x&&afterPencil.y===beforePencil.y&&afterTrash.x===beforeChevron.x&&afterTrash.y===beforeChevron.y,'During the workout the trash can takes the chevron position and the pencil stays put');
 check((await state()).active?.exercises.length===6&&await page.locator('.set[aria-pressed=false]').count()===18,'Start creates a saved session with one circle per set');
 await page.getByRole('button',{name:'Log set 1 of Barbell Curl',exact:true}).click();
 check((await state()).active.exercises[0].sets[0].reps===8,'One tap records the target reps');
 await page.waitForFunction(()=>document.querySelector('#elapsed')?.textContent.endsWith(' · 6%'));
 check(/^0:\d{2} · 6%$/.test(await page.locator('#elapsed').textContent()),'Elapsed workout time uses unpadded minutes and rounds completion to a whole percent');
 check((await state()).restEndAt>Date.now()&&await page.getByLabel('Rest timer').isVisible(),'A completed set starts the rest timer');
 const calculator=page.getByRole('button',{name:'Plate calculator',exact:true});
 check(await calculator.evaluate(control=>{
  const finish=document.querySelector('#finish'),icon=control.querySelector('svg');
  const square=control.getBoundingClientRect(),finishBox=finish.getBoundingClientRect();
  const style=getComputedStyle(control),finishStyle=getComputedStyle(finish);
  return control.nextElementSibling===finish&&square.right<finishBox.left&&square.width===square.height&&
   square.height===finishBox.height&&square.top===finishBox.top&&style.backgroundColor===finishStyle.backgroundColor&&
   getComputedStyle(icon).stroke===finishStyle.color&&icon.getBoundingClientRect().width>=20&&
   parseFloat(getComputedStyle(icon).strokeWidth)>2;
 }),'The larger, heavier calculator icon sits in a square button matching Finish');
 await calculator.click();
 const equipmentButton=()=>dialog().getByRole('button',{name:'Edit plate equipment',exact:true});
 check(await equipmentButton().evaluate(control=>{
  const title=control.parentElement.querySelector('h2'),icon=control.querySelector('svg');
  const center=element=>{const box=element.getBoundingClientRect();return box.top+box.height/2;};
  return control.textContent===''&&Math.abs(center(control)-center(title))<.6&&
   Math.abs(center(icon)-center(title)+1)<.6&&
   icon.getBoundingClientRect().height===parseFloat(getComputedStyle(title).fontSize)&&
   parseFloat(getComputedStyle(icon).strokeWidth)>2;
 })&&(await dialog().getByText('Match this load on both sides.').count())===0,
 'A bold, icon-only pencil matches the calculator title size and alignment');
 const curlLoad=()=>dialog().getByRole('region',{name:'Barbell Curl',exact:true});
 check(await dialog().locator('.plate-card').count()===6&&
  (await curlLoad().locator('.plate-summary').textContent())==='45 lb ➜ 45 lb bar + 0 lb in plates'&&
  (await curlLoad().locator('.plate-load-summary').textContent())==='0 lb each side · Bar only'&&
  (await dialog().getByRole('region',{name:'Front/Lateral Raise',exact:true}).textContent()).includes('Target is lighter than the bar.'),
 'The calculator lists every exercise, shows zero plate weight for the 45 lb bar alone, and identifies targets below it');
 const perSide=name=>dialog().getByRole('region',{name,exact:true}).locator('.plate-load-summary').textContent();
 check(await perSide('Deadlift')==='115 lb each side · 2×45 lb + 1×25 lb',
  'The 275 lb deadlift uses two 45s and a 25 on each side');
 check(await perSide('Bench Press')==='60 lb each side · 1×45 lb + 1×10 lb + 1×5 lb',
  'The calculator uses the largest plate for a matching load even when that takes more plates');
 const keyboardBeforeEdit=await page.evaluate(()=>[qa.keyboard,qa.keyboardHides]);
 await dialog().getByRole('button',{name:'Edit load for Barbell Curl'}).click();
 const loadInput=dialog().getByRole('spinbutton',{name:'Weight',exact:true});
 await page.waitForFunction(count=>qa.keyboard>count,keyboardBeforeEdit[0]);
 check(await loadInput.evaluate(input=>document.activeElement===input)&&
  await page.evaluate(([shows,hides])=>qa.keyboard===shows+1&&qa.keyboardHides===hides,keyboardBeforeEdit),
 'The calculator opens the weight editor focused with one keyboard request');
 const editorTop=await dialog().evaluate(editor=>editor.getBoundingClientRect().top);
 for(const height of [690,540,390]){
  await page.setViewportSize({width:390,height});
  await page.waitForFunction(height=>Math.abs(parseFloat(document.documentElement.style.getPropertyValue('--height'))-height)<1,height);
  check(await dialog().evaluate((editor,{top,height})=>{
   const rect=editor.getBoundingClientRect();
   return Math.abs(rect.top-top)<.6&&rect.bottom<=height&&document.activeElement?.getAttribute('aria-label')==='Weight';
  },{top:editorTop,height}),'The editor stays anchored as the keyboard reduces the viewport to '+height+'px');
 }
 await page.setViewportSize({width:390,height:844});
 await loadInput.fill('140');
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 const plateDimensions=()=>curlLoad().locator('.plate-group').evaluateAll(groups=>Promise.all(groups.map(async group=>{
  const image=new Image();
  image.src=group.style.backgroundImage.slice(5,-2);
  await image.decode();
  const canvas=document.createElement('canvas');
  canvas.width=image.width;canvas.height=image.height;
  const context=canvas.getContext('2d');
  context.drawImage(image,0,0);
  const {data}=context.getImageData(image.width/2,0,1,image.height);
  const axis=2*parseFloat(getComputedStyle(group).getPropertyValue('--sleeve-axis'));
  let top=axis,bottom=axis;
  while(top>0&&data[(top-1)*4+3]>128)top--;
  while(bottom<image.height&&data[bottom*4+3]>128)bottom++;
  const {data:row}=context.getImageData(0,axis,image.width,1);
  let depth=0;
  for(let pixel=3;pixel<row.length;pixel+=4)if(row[pixel]>128)depth+=.5;
  return {height:(bottom-top)/2,depth};
 })));
 check((await curlLoad().locator('.plate-summary').textContent())==='140 lb ➜ 45 lb bar + 95 lb in plates'&&
  (await curlLoad().locator('.plate-summary strong').textContent())==='140 lb ➜'&&
  (await curlLoad().locator('.plate-load-summary').textContent())==='47.5 lb each side · 1×45 lb + 1×2.5 lb'&&
  await curlLoad().locator('.plate-load-summary').evaluate(summary=>getComputedStyle(summary).fontWeight==='700'),
 'The bold target and per-side line use the approved breakdown format');
 const [large,small]=await plateDimensions();
 check(large.height===94&&large.depth===13&&small.height===20&&small.depth===7,
 'The smallest plates gain height and the largest retain their proportions');
 check(await curlLoad().locator('.plate-bar').evaluate(bar=>{
  const inner=bar.querySelector('.plate-shaft').getBoundingClientRect();
  const collar=bar.querySelector('.plate-collar').getBoundingClientRect();
  const outer=bar.querySelector('.plate-tip').getBoundingClientRect();
  return collar.height===26&&outer.height>inner.height&&
   Math.abs(collar.top+collar.height/2-inner.top-inner.height/2)<.5&&
   Math.abs(outer.top+outer.height/2-inner.top-inner.height/2)<.5;
 }),'The shorter stopper sits between aligned inner and thicker outer bars');
 await equipmentButton().click();
 const bar=dialog().getByRole('spinbutton',{name:'Bar weight (lb)',exact:true});
 await bar.fill('40');
 await dialog().getByRole('spinbutton',{name:'Count for 45 lb plates',exact:true}).fill('1');
 await dialog().getByRole('spinbutton',{name:'Count for 25 lb plates',exact:true}).fill('4');
 for(const size of [10,5,2.5])await dialog().getByRole('checkbox',{name:size+' lb',exact:true}).uncheck();
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 check((await curlLoad().textContent()).includes('2×25 lb')&&await curlLoad().locator('.plate-group').evaluate(group=>{
  const style=getComputedStyle(group);
  return style.backgroundRepeat==='repeat-x'&&group.getBoundingClientRect().width===2*parseFloat(style.backgroundSize);
 }),'Two plates of the same weight are drawn as two individual plates');
 await equipmentButton().click();
 await dialog().getByRole('spinbutton',{name:'Count for 25 lb plates',exact:true}).fill('3');
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 const lighterLoad=await curlLoad().textContent();
 check(lighterLoad.includes('110 lb loaded · 30 lb short')&&lighterLoad.includes('1×35 lb')&&
  lighterLoad.includes('70 lb in plates')&&lighterLoad.includes('35 lb each side'),
 'The closest lighter load reports the plate weight actually shown on the bar');
 const [{height}]=await plateDimensions();
 check(height===76,'Intermediate plates get taller while scaling against the biggest available plate');
 await equipmentButton().click();
 check(await dialog().getByRole('checkbox',{name:'0.5 lb',exact:true}).count()===0&&
  await dialog().getByRole('checkbox',{name:'0.25 lb',exact:true}).count()===0,'The two smallest sizes are no longer presets');
 const custom=dialog().getByRole('spinbutton',{name:'Custom plate (lb)',exact:true});
 const addPlate=dialog().getByRole('button',{name:'Add plate size',exact:true});
 await custom.fill('45');await addPlate.click();
 check(await dialog().getByText('That plate size is already listed.').isVisible(),'Custom sizes cannot duplicate presets');
 await custom.fill('50.37');await addPlate.click();
 const customCount=dialog().getByRole('spinbutton',{name:'Count for 50.37 lb plates',exact:true});
 check(await customCount.inputValue()===''&&await dialog().getByRole('checkbox',{name:'50.37 lb',exact:true}).isChecked(),
  'A newly added size is enabled with an unlimited count');
 await customCount.fill('2');
 await bar.fill('39.26');
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 check((await curlLoad().locator('.plate-summary').textContent())==='140 lb ➜ 39.26 lb bar + 100.74 lb in plates'&&
  (await curlLoad().locator('.plate-load-summary').textContent())==='50.37 lb each side · 1×50.37 lb',
  'The calculator honors the count and exact weight of a custom plate');
 await equipmentButton().click();
 await dialog().getByRole('checkbox',{name:'50.37 lb',exact:true}).uncheck();
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 await equipmentButton().click();
 check(!await dialog().getByRole('checkbox',{name:'50.37 lb',exact:true}).isChecked(),'Disabled custom plate sizes stay listed');
 await dialog().getByRole('button',{name:'Remove custom 50.37 lb plate',exact:true}).click();
 await bar.fill('40');
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 check((await state()).settings.plateEquipment.lb.customSizes.length===0,'Removing a custom plate clears its saved entry');
 await dialog().getByRole('button',{name:'Done',exact:true}).click();
 await page.getByRole('button',{name:'Edit sets/reps for Barbell Curl',exact:true}).click();
 const quickTargets=dialog().getByRole('textbox',{name:'Sets/Reps',exact:true});
 check(await quickTargets.getAttribute('placeholder')==='8, 8, 8','Tapping the workout targets opens the popup focused on Sets/Reps');
 await quickTargets.fill('8, 7, 6+');
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 await page.getByRole('button',{name:'Edit weight for Barbell Curl',exact:true}).click();
 check(await dialog().getByRole('spinbutton',{name:'Weight',exact:true}).getAttribute('placeholder')==='140','Tapping the workout weight focuses Weight');
 await dialog().getByRole('button',{name:'Cancel',exact:true}).click();
 const quickSaved=await state();
 check(quickSaved.active.exercises[0].sets[0].reps===8&&quickSaved.active.exercises[0].sets[0].weight===45&&
  quickSaved.active.exercises[0].sets[1].weight===140&&quickSaved.active.exercises[0].sets[2].amrap&&
  quickSaved.routines[0].exercises[0].sets[1].target===7,'Quick edits update the workout and its template while preserving logged weights and reps');
 await page.getByRole('button',{name:'Edit set 1 of Barbell Curl',exact:true}).click();
 await page.getByRole('button',{name:'Edit set 1 of Barbell Curl',exact:true}).click();
 check((await state()).active.exercises[0].sets[0].reps===6,'Repeated taps reduce actual reps without opening an editor');
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
 await dialog().getByRole('button',{name:'Edit Barbell Curl',exact:true}).click();
 await dialog().getByRole('spinbutton',{name:'Weight',exact:true}).fill('50');
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 let saved=await state();
 check(saved.active.exercises[0].sets[0].weight===45&&saved.active.exercises[0].sets[1].weight===50&&saved.routines[0].exercises[0].weight===50,'Changing working weight preserves logged sets and updates future sets and routine');
 await page.reload();await page.waitForFunction(()=>!!window.Workouts);
 check((await state()).active.exercises[0].sets[0].reps===6,'Unfinished workouts survive reopening');
 await page.evaluate(()=>{
  const realNow=Date.now;
  const end=Workouts.debug().restEndAt;
  Date.now=()=>Math.max(realNow(),end+3001);
  setTimeout(()=>Date.now=realNow,900);
 });
 await page.waitForFunction(()=>document.querySelector('#rest-time').textContent==='-0:03');
 check(await page.locator('#rest-time.overdue').textContent()==='-0:03','The rest circle counts negative time after the deadline');
 await page.getByRole('button',{name:'Rest controls',exact:true}).click();
 await dialog().getByRole('button',{name:'Skip rest',exact:true}).click();
 check((await state()).restEndAt===0,'Rest can be skipped');
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
 await dialog().getByRole('button',{name:'Add set to Barbell Curl'}).click();
 await dialog().getByRole('button',{name:'Done'}).click();
 check((await state()).active.exercises[0].sets.length===4,'Extra sets can be added to the active workout');
 await page.getByRole('button',{name:'Finish',exact:true}).click();
 await dialog().getByRole('textbox',{name:'Workout notes'}).fill('First recorded session');
 await dialog().getByRole('button',{name:'Save workout',exact:true}).click();
 saved=await state();
 check(saved.active===null&&saved.history.length===1&&saved.restEndAt===0,'Finish archives one session and cancels rest');
 check(saved.history[0].exercises[0].sets[0].reps===6&&saved.history[0].notes==='First recorded session','Saved history keeps actual reps, weights and notes');
 await page.getByRole('tab',{name:'History',exact:true}).click();
 check(await page.getByRole('tab',{name:'Today'}).getAttribute('aria-selected')==='true','Tab highlight waits while the page slides');
 await page.waitForFunction(()=>document.querySelector('#history-tab').getAttribute('aria-selected')==='true');
 check(await page.locator('.history-card').count()===1,'History lists the completed workout');
 await page.locator('.history-card').click();
 check(await dialog().textContent().then(s=>s.includes('6 reps · 45 lb')),'Workout details display actual logged sets');
 await dialog().getByRole('button',{name:'Cancel',exact:true}).click();
 check(await page.locator('.progress-row').textContent().then(s=>s.includes('45 lb × 6')),'Progress uses completed sets');
 const progress=page.locator('.progress summary');
 await progress.click();
 check(await progress.evaluate(summary=>{
  const style=getComputedStyle(summary),label=summary.querySelector('span').getBoundingClientRect();
  const title=document.querySelector('.history-card strong').getBoundingClientRect();
  const gap=title.top-label.bottom;
  return !summary.parentElement.open&&style.fontSize==='16px'&&style.fontWeight==='700'&&
   style.borderBottomWidth==='1px'&&style.webkitTapHighlightColor==='rgba(0, 0, 0, 0)'&&gap>0&&gap<=22;
 }),'The larger, bold Progress heading has a divider and a tighter gap to workout history');
 await progress.click();
 await page.getByRole('tab',{name:'Today'}).click();
 await page.waitForFunction(()=>document.querySelector('#today-tab').getAttribute('aria-selected')==='true');
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
 await dialog().getByRole('button',{name:'Edit Barbell Curl',exact:true}).click();
 await dialog().getByRole('spinbutton',{name:'Weight',exact:true}).fill('55');
 await page.evaluate(()=>qa.fail=true);
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 check(await dialog().isVisible()&&await dialog().locator('.error').textContent().then(s=>s.includes('Couldn’t save')),'Save failures keep the editor and draft');
 check((await state()).routines[0].exercises[0].weight===50,'Save failures leave saved data intact');
 await page.evaluate(()=>qa.fail=false);
 await dialog().getByRole('button',{name:'Cancel',exact:true}).click();
 await page.getByRole('button',{name:'Settings',exact:true}).click();
 check(await page.locator('dialog[open]').count()===0&&await page.locator('#app').isVisible()===false,'Settings opens as a separate screen');
 check(await page.locator('#settings-screen').getByRole('checkbox',{name:'Rest notifications'}).isChecked()===false,'Notification controls reflect opt-in defaults');
 const defaults=page.getByRole('region',{name:'Defaults',exact:true});
 check(await defaults.evaluate(section=>{
  const [weight,reps,unit,rest]=[...section.querySelectorAll('input,select')].map(e=>e.getBoundingClientRect());
  return weight.top===reps.top&&unit.top===rest.top&&weight.left===unit.left&&reps.left===rest.left&&unit.width===rest.width;
 }),'The four Defaults controls align in two equal columns');
 check(await page.getByRole('spinbutton',{name:'Bar weight (lb)',exact:true}).inputValue()==='40','Settings and the calculator share bar weight');
 await defaults.getByRole('combobox',{name:'Unit',exact:true}).selectOption('kg');
 check(await page.getByRole('spinbutton',{name:'Bar weight (kg)',exact:true}).inputValue()==='20','Changing unit uses a separate metric bar and inventory');
 await page.locator('#settings-screen').getByRole('button',{name:'Save',exact:true}).click();
 check((await state()).settings.unit==='kg'&&(await state()).history[0].exercises[0].unit==='lb','Changing default units preserves historical units');
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
 await dialog().getByRole('button',{name:'Edit Barbell Curl',exact:true}).click();
 check(await dialog().locator('#exercise-weight-unit').textContent()==='lbs','Editing an existing exercise preserves its original weight unit');
 const savedWeight=dialog().getByRole('spinbutton',{name:'Weight',exact:true});
 const savedValue=await savedWeight.inputValue();
 await savedWeight.focus();
 check(await savedWeight.inputValue()===''&&await savedWeight.getAttribute('placeholder')===savedValue,'Existing weights show their saved value as a hint');
 await dialog().getByRole('button',{name:'Cancel',exact:true}).click();
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
  await dialog().getByRole('button',{name:'Add exercise',exact:true}).click();
 await dialog().getByRole('textbox',{name:'Exercise',exact:true}).fill('New exercise');
 await dialog().getByRole('textbox',{name:'Sets/Reps'}).fill('0, 8');
 await dialog().getByRole('button',{name:'Add',exact:true}).click();
 check(await dialog().locator('.error').textContent().then(s=>s.includes('1–100')),'Invalid rep targets are rejected');
 await dialog().getByRole('button',{name:'Cancel',exact:true}).click();
 const old=await state();
 await page.getByRole('button',{name:'Add workout',exact:true}).click();
 await dialog().getByRole('textbox',{name:'Name',exact:true}).fill('Workout B');
 await dialog().getByRole('button',{name:'Add',exact:true}).click();
 check((await state()).routines.length===old.routines.length+1&&await page.locator('.routine:visible').count()===2,'Multiple routines are visible as cards');
 await page.getByRole('button',{name:'Edit Workout B',exact:true}).click();
 await dialog().getByRole('button',{name:'Rename workout',exact:true}).click();
 await dialog().getByRole('textbox',{name:'Name',exact:true}).fill('Workout B renamed');
 await dialog().getByRole('button',{name:'Save',exact:true}).click();
 check((await state()).routines.find(r=>r.id===(old.routines[0].id))?.name==='Workout A'&&await page.getByRole('region',{name:'Workout B renamed',exact:true}).isVisible(),'The pencil renames the selected workout');
 await page.getByRole('button',{name:'Edit Workout B renamed',exact:true}).click();
 await dialog().getByRole('button',{name:'Add exercise',exact:true}).click();
 await dialog().getByRole('textbox',{name:'Exercise',exact:true}).fill('B-only exercise');
 await dialog().getByRole('button',{name:'Add',exact:true}).click();
 check(await page.getByRole('button',{name:'Add exercise',exact:true}).count()===0&&
  await page.locator('.exercise-head button').evaluateAll(controls=>controls.every(control=>control.closest('.exercise-plan'))),
  'Today offers quick plan edits while keeping exercise management behind the pencil');
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
 const cards=dialog().locator('.workout-edit-row');
 const a=await cards.nth(0).boundingBox(),b=await cards.nth(1).boundingBox();
 await page.mouse.move(a.x+20,a.y+a.height/2);await page.mouse.down();await page.waitForTimeout(430);await page.mouse.move(b.x+20,b.y+b.height-2,{steps:12});await page.waitForTimeout(220);await page.mouse.up();await page.waitForTimeout(480);
 check((await state()).routines[0].exercises[0].name==='Front/Lateral Raise','Hold and drag reorders exercises');
 await dialog().getByRole('button',{name:'Done'}).click();
 await page.reload();await page.waitForFunction(()=>!!window.Workouts);
 check((await state()).routines[0].exercises[0].name==='Front/Lateral Raise','Exercise order persists after reopening');
 check((await state()).history[0].exercises[0].name==='Barbell Curl','Reordering routines preserves workout history');
 await mkdir('build/browser-qa',{recursive:true});
 await page.screenshot({path:'build/browser-qa/workouts-home.png',fullPage:true});
 await workoutA.click();
 await page.getByRole('region',{name:'Workout A',exact:true}).getByRole('button',{name:'Start workout',exact:true}).click();
 const other=page.getByRole('region',{name:'Workout B renamed',exact:true});
 await other.getByRole('button',{name:'Workout B renamed',exact:true}).click();
 check(await other.getByText('B-only exercise',{exact:true}).isVisible()&&await other.locator('.set').count()===0&&await other.getByRole('button',{name:'Start workout'}).isDisabled(),'Inactive routines stay visible with their rep circles collapsed');
 await page.getByRole('button',{name:'Log set 1 of Front/Lateral Raise'}).click();
 await page.screenshot({path:'build/browser-qa/workouts-active.png',fullPage:true});
 await page.evaluate(()=>{qa.music={connected:true,available:true,title:'Training track',artist:'Example artist',playing:true,duration:180000,position:45000,speed:1,art:'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/a9sAAAAASUVORK5CYII='};});
 await page.waitForFunction(()=>document.querySelector('#spotify-art img'));
 const aligned=await page.locator('#spotify').evaluate(player=>{
  const centers=[...player.querySelectorAll('button')].filter(e=>!e.hidden).map(e=>{const r=e.getBoundingClientRect();return r.top+r.height/2;});
  return Math.max(...centers)-Math.min(...centers)<.6;
 });
 check(aligned,'Spotify controls and track text share a centered row');
 const before=await page.locator('#rest').boundingBox();
 check(before.x>250&&before.y>240&&before.y<330,'A new rest starts lower on the right, clear of the drop zone');
 await page.mouse.move(before.x+38,before.y+38);await page.mouse.down();await page.mouse.move(90,220,{steps:12});await page.mouse.up();
 check(await page.evaluate(()=>{const data=Workouts.debug();return data.restPosition?.started===data.restStartedAt;}),'Dragging saves the position for the current rest');
 const after=await page.locator('#rest').boundingBox();
 check(Math.abs(after.x-before.x)>10||Math.abs(after.y-before.y)>10,'The floating timer moves independently of the workout');
 const ringStart=await page.locator('#rest-ring').evaluate(e=>Number(e.style.strokeDashoffset));
 await page.waitForTimeout(500);
 check(await page.locator('#rest-ring').evaluate((e,start)=>Number(e.style.strokeDashoffset)>start,ringStart),'The timer ring shrinks as time passes');
 await page.getByRole('button',{name:'Log set 2 of Front/Lateral Raise'}).click();
 const nextRest=await page.locator('#rest').boundingBox();
 check(Math.abs(nextRest.x-after.x)<1&&Math.abs(nextRest.y-after.y)<1,'The next rest keeps the last dragged position');
 check(await page.locator('#spotify-art img').count()===1,'Album art is shown when Spotify provides it');
 check(await page.locator('#spotify-title').evaluate(e=>Number(getComputedStyle(e).fontWeight)>=700),'Spotify track text is bold');
 check(await page.locator('#spotify').evaluate(player=>{
  const bar=player.getBoundingClientRect(),art=player.querySelector('#spotify-art').getBoundingClientRect();
  return Math.abs(art.top-bar.top-(art.left-bar.left))<.6&&Math.abs(bar.bottom-art.bottom-(art.left-bar.left))<.6;
 }),'Spotify artwork has equal left, top and bottom padding');
 await page.setViewportSize({width:320,height:500});
 await page.getByRole('button',{name:'Edit Workout A',exact:true}).click();
 await dialog().getByRole('button',{name:'Edit Front/Lateral Raise',exact:true}).click();
 check(await dialog().evaluate(d=>d.scrollWidth<=d.clientWidth+1),'Exercise editor fits a narrow phone');
 check(await dialog().getByRole('button',{name:'Save',exact:true}).evaluate(b=>{const r=b.getBoundingClientRect();return r.bottom<=innerHeight&&r.top>=0;}),'Editor actions remain visible with limited keyboard space');
 await dialog().getByRole('button',{name:'Cancel',exact:true}).click();
 await page.getByRole('button',{name:'Settings',exact:true}).click();
 check(await page.locator('#settings-screen').evaluate(d=>d.scrollWidth<=d.clientWidth+1),'Settings fit a narrow phone');
 check(await page.locator('#settings-screen').getByRole('button',{name:'Save',exact:true}).evaluate(b=>b.getBoundingClientRect().bottom<=innerHeight),'Settings actions remain visible while fields scroll');
 await page.locator('#settings-screen').getByRole('checkbox',{name:'Floating rest timer'}).uncheck();
 await page.locator('#settings-screen').getByRole('button',{name:'Save',exact:true}).click();
 check(await page.getByLabel('Rest timer',{exact:true}).isVisible()===false,'The floating timer can be disabled');
 check(await page.locator('.fab').count()===0,'There is no floating action button');
 await page.setViewportSize({width:390,height:844});
 await page.getByRole('button',{name:'Settings',exact:true}).click();
 const settings=page.locator('#settings-screen');
 await settings.getByRole('combobox',{name:'Rest countdown'}).selectOption('Last 5 seconds');
 await settings.getByRole('slider',{name:'Countdown pulse length'}).fill('0.12');
 const deadlineBeforePreview=(await state()).restEndAt;
 const beepVolume=settings.getByRole('slider',{name:/Rest beep volume/});
 check(await beepVolume.inputValue()==='40','Rest beeps default to 40 percent gain');
 await beepVolume.fill('24');
 await settings.getByRole('button',{name:'Play test',exact:true}).click();
 check(await page.evaluate(()=>qa.preview.join() === '5,120,350,true,24')&&(await state()).restEndAt===deadlineBeforePreview,'Play test uses the staged settings without changing the rest timer');
 await settings.getByRole('button',{name:'Stop test',exact:true}).click();
 check(await page.evaluate(()=>qa.previewStopped),'The settings preview can be stopped');

 await settings.getByRole('combobox',{name:'Schedule',exact:true}).selectOption('Selected weekdays');
 await settings.getByRole('checkbox',{name:'Friday',exact:true}).uncheck();
 await settings.getByRole('button',{name:'Save',exact:true}).click();
 const preferences=(await state()).settings;
 check(preferences.restCueVolume===24,'Rest beep volume saves independently of the vibration settings');
 check(preferences.countdownSeconds===5&&preferences.countdownVibrationMs===120,'Rest countdown duration and pulse length save independently');
 check(preferences.schedule.days.join() === '1,3'&&await page.locator('.routine-date').count()===2,'The weekday schedule labels both routines');
 check(await page.locator('.routine').first().getAttribute('aria-label')==='Workout B renamed','The next routine follows the latest completed workout');
 const tuesday=new Date(2040,0,1,12);
 tuesday.setDate(tuesday.getDate()+(2-tuesday.getDay()+7)%7);
 await page.clock.setFixedTime(tuesday);
 await page.getByRole('button',{name:'Settings',exact:true}).click();
 await settings.getByRole('checkbox',{name:'Friday',exact:true}).check();
 await settings.getByRole('button',{name:'Save',exact:true}).click();
 const labels=await page.locator('.routine-date').allTextContents();
 check(labels.length===3&&['Wednesday','Friday','Monday'].every((day,i)=>labels[i].startsWith(day)&&labels[i].includes('January')),'Three selected weekdays preview Wednesday, Friday and Monday');
 const scheduledNames=await page.locator('.routine').evaluateAll(cards=>cards.map(card=>card.getAttribute('aria-label')));
 check(scheduledNames.join('|')==='Workout B renamed|Workout A|Workout B renamed','Two routines rotate across all three upcoming days');

 await page.getByRole('button',{name:'Settings',exact:true}).click();
 await settings.getByRole('combobox',{name:'Schedule',exact:true}).selectOption('Every few days');
 await settings.getByRole('spinbutton',{name:'Train every (days)'}).fill('3');
 await settings.getByRole('button',{name:'Save',exact:true}).click();
 check((await state()).settings.schedule.every===3&&await page.locator('.routine-date').count()===2,'An every-three-days schedule saves and dates each workout');
 await page.getByRole('button',{name:'Settings',exact:true}).click();
 await settings.getByRole('checkbox',{name:'Show rest timer over other apps',exact:true}).check();
 await settings.getByRole('button',{name:'Save',exact:true}).click();
 check((await state()).settings.floatOverApps&&await page.evaluate(()=>qa.overlayRequests)===1,'Enabling the overlay opens Android permission settings');
 await page.evaluate(()=>qa.overlayGranted=true);
 await page.getByRole('button',{name:'Settings',exact:true}).click();
 check(await settings.getByRole('checkbox',{name:'Show rest timer over other apps',exact:true}).isChecked(),'The overlay choice persists');
 await settings.getByRole('button',{name:'Save',exact:true}).click();
 check(await page.evaluate(()=>qa.overlayRequests)===1,'Saving with permission already granted leaves Android settings closed');
 check(errors.length===0,'No browser errors');
 console.log(JSON.stringify({passed:checks.length,checks},null,2));
}finally{await browser.close();await fixture.close();}
