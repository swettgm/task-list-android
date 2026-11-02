// Actual packaged-UI checks for the dependency-free color picker and gradient settings.
import { chromium } from 'playwright';
import { readFile, mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import { pathToFileURL } from 'node:url';

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
const state = () => page.evaluate(() => __themeNative.getState());
async function waitReady(){await page.waitForFunction(()=>!!window.TasksTheme&&!!window.TaskListApp&&window.__themeNative.snapshot.length>0);}
async function settings(){await page.getByRole('button',{name:'Settings',exact:true}).click();await page.locator('.tasks-theme-settings').waitFor({state:'visible'});}
const accent = () => page.getByRole('textbox',{name:'Accent hex',exact:true});
const background = () => page.getByRole('textbox',{name:'Background hex',exact:true});
const end = () => page.getByRole('textbox',{name:'Gradient end hex',exact:true});
const apply = () => page.getByRole('button',{name:'Apply colors',exact:true}).click();

try {
  await page.goto(pathToFileURL(documentPath).href);await waitReady();
  check((await state()).uiTheme?.gradient !== true,'Existing installs keep a solid background by default');
  const originalTheme = JSON.stringify((await state()).uiTheme || null);
  await settings();
  check(await accent().count()===1&&await background().count()===1,'Settings retains editable accent and background hex fields');
  await page.getByRole('button',{name:'Pick accent color',exact:true}).click();
  const red = page.getByRole('slider',{name:'Accent red',exact:true});
  await red.focus();await red.press('Home');await red.press('ArrowRight');
  check(await accent().inputValue()==='#01b7ff','The built-in RGB picker changes the hex field using real keyboard input');
  check(JSON.stringify((await state()).uiTheme || null)===originalTheme,'Picker previews do not change saved colors before Apply');
  await page.getByRole('button',{name:'Accent #5865f2',exact:true}).click();
  check(await accent().inputValue()==='#5865f2','Color swatches choose an exact hex color without a native picker dialog');
  check(await red.inputValue()==='88','Selecting a swatch synchronizes its RGB slider values');
  await page.locator('.tasks-color-picker').filter({has:page.getByRole('slider',{name:'Accent red',exact:true})}).getByRole('button',{name:'Done',exact:true}).click();
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
  check(await page.evaluate(()=>document.body.style.backgroundAttachment==='fixed'),'The app gradient stays attached to the screen while the task list scrolls');
  check(await page.evaluate(()=>getComputedStyle(document.getElementById('today-task-list-20261023')).backgroundColor==='rgba(0, 0, 0, 0)'),'The task-list surface does not cover or repeat the page gradient');
  check(await page.evaluate(()=>__themeNative.background)==='#1e2935','Android receives the base background hex for its system bars');
  await page.getByRole('button',{name:'Close',exact:true}).click();
  await page.screenshot({path:path.join(output,'gradient-tasks.png'),fullPage:false});
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
  const metrics=await page.locator('dialog').evaluate(dialog=>({scroll:dialog.scrollWidth,client:dialog.clientWidth,left:dialog.getBoundingClientRect().left,right:dialog.getBoundingClientRect().right,viewport:innerWidth}));
  check(metrics.scroll<=metrics.client+1&&metrics.left>=0&&metrics.right<=metrics.viewport+1,'Expanded appearance pickers fit a 320px screen without horizontal scrolling');
  await page.screenshot({path:path.join(output,'appearance-picker.png'),fullPage:false});
  check(errors.length===0,'No browser errors occurred during picker or gradient changes');
  console.log(JSON.stringify({passed:checks.length,checks},null,2));
} finally {await browser.close();}
