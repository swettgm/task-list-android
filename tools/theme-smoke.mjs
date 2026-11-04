// Actual packaged-UI checks for the dependency-free color picker and gradient settings.
import { chromium } from 'playwright';
import { readFile, mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import {serveFixture} from './browser-fixture-server.mjs';

const documentPath = path.resolve(process.argv[2] || 'app/src/main/assets/www/index.html');
const seedPath = path.resolve(process.argv[3] || path.join(path.dirname(documentPath), 'initial-state.json'));
const seed = JSON.parse(await readFile(seedPath, 'utf8'));
const output = path.resolve(process.env.TASK_SCREENSHOT_DIR || 'build/browser-qa');
await mkdir(output, {recursive:true});
const executablePath = process.env.CHROME_BIN || ['/usr/bin/google-chrome','/usr/bin/chromium','/opt/google/chrome/chrome'].find(existsSync);
const browser = await chromium.launch({headless:true,executablePath,args:['--no-sandbox','--disable-dev-shm-usage']});
const page = await browser.newPage({viewport:{width:390,height:844},deviceScaleFactor:1});
const checks = [], errors = [];
const check = (condition,label) => { if (!condition) throw new Error(label); checks.push(label); };
page.on('pageerror', error => errors.push(error.message));
await page.addInitScript(initial => {
  const storage = 'tasks-theme-smoke-native';
  let canonical = JSON.parse(sessionStorage.getItem(storage) || JSON.stringify(initial));
  window.__themeNative = {getState(){return JSON.parse(JSON.stringify(canonical));},background:null,snapshot:[]};
  window.TaskListNative = {
    readState(){return JSON.stringify(canonical);},
    writeState(text){canonical=JSON.parse(text);sessionStorage.setItem(storage,text);},
    updateSnapshot(text){__themeNative.snapshot=JSON.parse(text);},
    setThemeColors(background){__themeNative.background=background;},
    showWidgetSettings(){},
    getSyncStatus(){return JSON.stringify({paired:false,connected:false,message:'Not paired'});},
    pairDesktop(){},unpairDesktop(){},syncNow(){}
  };
},seed);
const fixture=await serveFixture(documentPath);
const state = () => page.evaluate(() => __themeNative.getState());
async function waitReady(){await page.waitForFunction(()=>!!window.TasksTheme&&!!window.TaskListApp&&window.__themeNative.snapshot.length>0);}
async function settings(){await page.getByRole('button',{name:'Settings',exact:true}).click();await page.locator('.tasks-theme-settings').waitFor({state:'visible'});}
const accent = () => page.getByRole('textbox',{name:'Accent hex',exact:true});
const background = () => page.getByRole('textbox',{name:'Background hex',exact:true});
const end = () => page.getByRole('textbox',{name:'Gradient end hex',exact:true});
const apply = () => page.getByRole('button',{name:'Apply colors',exact:true}).click();

try {
  await page.goto(fixture.url);await waitReady();
  check((await state()).uiTheme?.gradient !== true,'Existing installs keep a solid background by default');
  const originalTheme = JSON.stringify((await state()).uiTheme || null);
  await settings();
  check(await accent().count()===1&&await background().count()===1,'Settings retains editable accent and background hex fields');
  await page.getByRole('button',{name:'Pick accent color',exact:true}).click();
  const wheel = page.getByRole('slider',{name:'Accent color wheel',exact:true});
  const originalAccent=await accent().inputValue();await wheel.focus();await wheel.press('ArrowRight');
  check(await accent().inputValue()!==originalAccent,'The color wheel changes hex using real keyboard input');
  check(await page.locator('.tasks-rgb-label').count()===0,'RGB sliders are removed');
  const wheelBox=await wheel.boundingBox();await page.mouse.click(wheelBox.x+wheelBox.width*.9,wheelBox.y+wheelBox.height*.5);
  check(/^#[a-f0-9]{6}$/.test(await accent().inputValue()),'Pointer input on the wheel selects a color');
  check(JSON.stringify((await state()).uiTheme || null)===originalTheme,'Picker previews do not change saved colors before Apply');
  await page.getByRole('button',{name:'Accent #5865f2',exact:true}).click();
  check(await accent().inputValue()==='#5865f2','Color swatches choose an exact hex color without a native picker dialog');
  check((await wheel.getAttribute('aria-valuetext')).includes('#5865f2'),'Swatches synchronize the color wheel');
  const brightness=page.getByRole('slider',{name:'Accent brightness',exact:true});await brightness.focus();await brightness.press('Home');check(await accent().inputValue()==='#000000','Brightness control reaches black');await brightness.press('End');check(await accent().inputValue()!=='#000000','Brightness can be restored from black');
  await page.getByRole('button',{name:'Accent #5865f2',exact:true}).click();
  await page.locator('.tasks-color-picker').filter({has:page.getByRole('slider',{name:'Accent color wheel',exact:true})}).getByRole('button',{name:'Done',exact:true}).click();
  await page.getByRole('button',{name:'Slate gradient',exact:true}).click();
  check(await page.getByRole('checkbox',{name:'Gradient background',exact:true}).isChecked(),'The slate preset enables an optional gradient');
  check(await background().inputValue()==='#1e2935'&&await end().inputValue()==='#47535c','Slate preset matches the dark blue-to-charcoal reference');
  check((await page.locator('.tasks-theme-preview').evaluate(element=>getComputedStyle(element).backgroundImage)).includes('linear-gradient'),'Settings shows the gradient in a live preview');
  check(JSON.stringify((await state()).uiTheme || null)===originalTheme,'Choosing a gradient preset is also a draft until Apply');
  await page.getByRole('slider',{name:'Gradient angle',exact:true}).evaluate(element=>{element.value='135';element.dispatchEvent(new Event('input',{bubbles:true}));});
  await apply();
  let saved = (await state()).uiTheme;
  check(saved.accent==='#5865f2'&&saved.background==='#1e2935'&&saved.gradient===true&&saved.gradientEnd==='#47535c'&&saved.gradientAngle===135,'Apply saves both colors, gradient endpoints, and direction');
  check((await page.evaluate(()=>getComputedStyle(document.body).backgroundImage)).includes('135deg'),'Saved gradient is applied to the app background');
  check(await page.evaluate(()=>document.body.style.backgroundAttachment==='scroll'),'Body uses a fallback beneath the independent gradient layer');
  check(await page.evaluate(()=>getComputedStyle(document.getElementById('today-task-list-20261023')).backgroundColor==='rgba(0, 0, 0, 0)'),'The task-list surface does not cover or repeat the page gradient');
  check(await page.evaluate(()=>__themeNative.background)==='#1e2935','Android receives the base background hex for its system bars');
  await page.getByRole('button',{name:'Back to tasks',exact:true}).click();
  await page.screenshot({path:path.join(output,'gradient-tasks.png'),fullPage:false});
  await page.getByRole('tab',{name:'Upcoming',exact:true}).click();await page.locator('#pt-future-list details.tm-day').first().evaluate(section=>section.open=true);
  check(await page.locator('#pt-upcoming').isVisible()&&await page.evaluate(()=>getComputedStyle(document.body).backgroundImage.includes('135deg')&&['main','#today-task-list-20261023','#pt-upcoming','#pt-future-list'].every(selector=>getComputedStyle(document.querySelector(selector)).backgroundColor==='rgba(0, 0, 0, 0)')),'Upcoming shows the same page-wide gradient through every list surface');
  await page.screenshot({path:path.join(output,'gradient-upcoming.png'),fullPage:false});
  await page.evaluate(()=>{document.querySelector('main').style.minHeight='2500px';window.scrollTo(0,document.documentElement.scrollHeight);});
  check(await page.locator('#tasks-app-backdrop').evaluate(n=>{const r=n.getBoundingClientRect();return getComputedStyle(n).position==='fixed'&&Math.abs(r.height-innerHeight)<2&&Math.abs(r.top)<1&&Math.abs(r.bottom-innerHeight)<2;}),'Gradient stays fixed to the visible screen after scrolling a long page');
  await page.screenshot({path:path.join(output,'gradient-scroll-bottom.png')});
  await page.evaluate(()=>{document.querySelector('main').style.minHeight='';window.scrollTo(0,0);});

  await page.evaluate(()=>TaskListApp.addTask());
  check(await page.locator('dialog[open],.tl-settings-screen:not(:has(~dialog[open]))').evaluate(d=>getComputedStyle(d).backgroundImage.includes('135deg')),'Task editor uses the app gradient');
  await page.getByRole('button',{name:'Cancel',exact:true}).click();
  await settings();check(await page.locator('.tl-settings-screen').isVisible()&&await page.locator('dialog').count()===0&&await page.evaluate(()=>document.querySelector('body>main').hidden&&document.body.style.position!=='fixed'),'Settings is a separate full screen without a modal or dimmed task-list overlay');const gradientBefore=await page.evaluate(()=>{const layer=document.getElementById('tasks-app-backdrop'),style=getComputedStyle(layer),box=layer.getBoundingClientRect();return{image:style.backgroundImage,position:style.position,top:box.top,width:box.width,height:box.height,viewportWidth:innerWidth,viewportHeight:innerHeight};});check(gradientBefore.image.includes('135deg')&&gradientBefore.position==='fixed'&&Math.abs(gradientBefore.width-gradientBefore.viewportWidth)<1&&Math.abs(gradientBefore.height-gradientBefore.viewportHeight)<1,'Settings uses the same viewport-sized gradient as the main pages');await page.evaluate(()=>window.scrollTo(0,document.documentElement.scrollHeight));check(await page.evaluate(before=>{const layer=document.getElementById('tasks-app-backdrop'),style=getComputedStyle(layer),box=layer.getBoundingClientRect();return scrollY>0&&style.position===before.position&&Math.abs(box.top-before.top)<1&&Math.abs(box.width-before.width)<1&&Math.abs(box.height-before.height)<1;},gradientBefore),'Scrolling the Settings screen leaves the app background fixed');await page.screenshot({path:path.join(output,'settings-gradient-scroll-bottom.png')});
  await page.getByRole('button',{name:'Back to tasks',exact:true}).click();

  check(await page.evaluate(()=>JSON.parse(sessionStorage.getItem('tasks-theme-smoke-native')).uiTheme.gradient===true),'The native-state test fixture retains saved settings before reload');
  await page.reload();await waitReady();
  check((await state()).uiTheme.gradient===true&&(await page.evaluate(()=>getComputedStyle(document.body).backgroundImage)).includes('135deg'),'Saved gradient survives a full page reload');
  await settings();
  const beforeInvalid=JSON.stringify((await state()).uiTheme);
  await background().fill('not-hex');await apply();
  check(JSON.stringify((await state()).uiTheme)===beforeInvalid&&!!(await page.locator('.tasks-theme-error').textContent()),'Invalid hex input is rejected without overwriting the saved gradient');
  await background().fill('#123');await end().fill('#456');await apply();
  check((await state()).uiTheme.background==='#112233'&&(await state()).uiTheme.gradientEnd==='#445566','Three-digit hex values expand for both gradient endpoints');
  await page.getByRole('checkbox',{name:'Gradient background',exact:true}).uncheck();await apply();
  check((await state()).uiTheme.gradient===false&&(await page.evaluate(()=>getComputedStyle(document.body).backgroundImage))==='none','The gradient can be disabled while retaining chosen colors');
  await page.getByRole('button',{name:'Reset colors',exact:true}).click();
  saved=(await state()).uiTheme;
  check(saved.accent==='#68b7ff'&&saved.background==='#10151f'&&saved.gradient===false,'Reset restores the original solid dark theme');
  // Expand every custom picker at the narrowest phone width and inspect real geometry.
  await page.setViewportSize({width:320,height:844});
  await page.getByRole('checkbox',{name:'Gradient background',exact:true}).check();
  for(const name of ['Pick accent color','Pick background color','Pick gradient end color'])await page.getByRole('button',{name,exact:true}).click();
  const metrics=await page.locator('dialog[open],.tl-settings-screen:not(:has(~dialog[open]))').evaluate(dialog=>({scroll:dialog.scrollWidth,client:dialog.clientWidth,left:dialog.getBoundingClientRect().left,right:dialog.getBoundingClientRect().right,viewport:innerWidth}));
  check(metrics.scroll<=metrics.client+1&&metrics.left>=0&&metrics.right<=metrics.viewport+1,'Expanded appearance pickers fit a 320px screen without horizontal scrolling');
  await page.screenshot({path:path.join(output,'appearance-picker.png'),fullPage:false});
  check(errors.length===0,'No browser errors occurred during picker or gradient changes');
  console.log(JSON.stringify({passed:checks.length,checks},null,2));
} finally {await browser.close();await fixture.close();}
