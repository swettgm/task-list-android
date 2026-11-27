/* Desktop persistence/sync adapter, injected before the shared offline Tasks UI. */
(function (global) {
  'use strict';
  const boot = global.__TASKS_DESKTOP_BOOT__;
  if (!boot || typeof boot !== 'object') throw new Error('Tasks desktop startup data is missing.');
  const MAX_BYTES = 2 * 1024 * 1024;
  const FORBIDDEN_KEYS = new Set(['__proto__', 'prototype', 'constructor']);
  const clone = value => JSON.parse(JSON.stringify(value));
  const own = (value, key) => Object.prototype.hasOwnProperty.call(value, key);
  const object = value => value !== null && typeof value === 'object' && !Array.isArray(value);
  function validate(value) {
    if (!object(value)) throw new Error('Tasks state must be an object.');
    const text = JSON.stringify(value);
    if (new TextEncoder().encode(text).byteLength > MAX_BYTES) throw new Error('Tasks state is too large.');
    const visit = item => {
      if (item && typeof item === 'object') Object.keys(item).forEach(key => {
        if (FORBIDDEN_KEYS.has(key)) throw new Error('Tasks state contains an unsupported key.');
        visit(item[key]);
      });
    };
    visit(value); return clone(value);
  }
  function equal(left, right) {
    if (left === right) return true;
    if (Array.isArray(left) || Array.isArray(right)) return Array.isArray(left) && Array.isArray(right)
      && left.length === right.length && left.every((value, i) => equal(value, right[i]));
    if (!object(left) || !object(right)) return false;
    const keys = Object.keys(left);
    return keys.length === Object.keys(right).length && keys.every(key => own(right, key) && equal(left[key], right[key]));
  }
  function byId(items) {
    const result = new Map();
    for (const item of items) {
      if (!object(item) || typeof item.id !== 'string' || !item.id || result.has(item.id)) return null;
      result.set(item.id, item);
    }
    return result;
  }
  // Apply only edits made relative to base. False and missing are distinct values.
  function merge(base, local, remote, path) {
    if (equal(base, local)) return clone(remote);
    if ((path === 'inProgressTasks' || path && path.startsWith('manualLinks.')) && Array.isArray(base) && Array.isArray(local) && Array.isArray(remote)
        && [...base, ...local, ...remote].every(value => typeof value === 'string')) {
      const before = new Set(base), after = new Set(local);
      const result = remote.filter(value => !(before.has(value) && !after.has(value)));
      for (const value of local) if (!before.has(value) && !result.includes(value)) result.push(value);
      return [...new Set(result)];
    }
    if (path === 'customTasks' && Array.isArray(base) && Array.isArray(local) && Array.isArray(remote)) {
      const before = byId(base), after = byId(local), current = byId(remote);
      if (before && after && current) {
        const result = new Map(current);
        for (const [id, value] of before) {
          if (!after.has(id)) result.delete(id);
          else if (!equal(value, after.get(id))) result.set(id, merge(value, after.get(id), current.get(id) || {}, 'task'));
        }
        for (const [id, value] of after) if (!before.has(id)) {
          result.set(id, current.has(id) ? merge({}, value, current.get(id), 'task') : clone(value));
        }
        return Array.from(result.values());
      }
    }
    if (object(base) && object(local) && object(remote)) {
      const result = clone(remote);
      for (const key of new Set([...Object.keys(base), ...Object.keys(local)])) {
        const childPath = path ? path + '.' + key : key;
        if (!own(local, key)) { if (own(base, key)) delete result[key]; continue; }
        if (!own(base, key)) {
          if ((childPath === 'customTasks' || childPath === 'inProgressTasks' || childPath.startsWith('manualLinks.')) && Array.isArray(local[key]) && Array.isArray(remote[key])) result[key] = merge([], local[key], remote[key], childPath);
          else if (object(local[key]) && object(remote[key])) result[key] = merge({}, local[key], remote[key], childPath);
          else result[key] = clone(local[key]);
          continue;
        }
        if (equal(base[key], local[key])) continue;
        result[key] = merge(base[key], local[key], own(remote, key) ? remote[key] : {}, childPath);
      }
      if(!path&&Array.isArray(result.inProgressTasks))result.inProgress=result.inProgressTasks.at(-1)||null;
      return result;
    }
    return clone(local);
  }
  function shared(value) {
    const result = clone(value); delete result.uiContext; delete result.updatedAt; if(Array.isArray(result.inProgressTasks))result.inProgress=result.inProgressTasks.at(-1)||null;return result;
  }
  function hash(value) {
    let result = 2166136261;
    for (let i = 0; i < value.length; i++) result = Math.imul(result ^ value.charCodeAt(i), 16777619);
    return (result >>> 0).toString(16);
  }
  const origin = new URL(boot.origin || global.location.origin);
  if (!['http:', 'https:'].includes(origin.protocol)) throw new Error('Tasks desktop address is invalid.');
  if (typeof boot.token !== 'string' || boot.token.length < 32) throw new Error('Tasks desktop pairing token is missing.');
  const cacheKey = 'tasks-desktop-pending-v1:' + hash(boot.token);
  const contextKey = cacheKey + ':context';
  let base = shared(validate(boot.state)), canonical = clone(base),cloudSession=boot.cloudSession||'';
  let localContext = '', cacheBlocked = false, userEditedPending = false;
  try {
    localContext = global.localStorage.getItem(contextKey) || '';
    const text = global.localStorage.getItem(cacheKey);
    if (text) {
      const pending = JSON.parse(text);
      if(cloudSession&&(pending.cloudSession||'')!==cloudSession){global.localStorage.setItem(cacheKey+':before-cloud-restore:'+Date.now(),text);}
      else canonical = merge(shared(validate(pending.base)), shared(validate(pending.state)), base, '');
      if (typeof pending.context === 'string') localContext = pending.context;
      userEditedPending=!!pending.userEdited;
    }
  } catch (_) {
    cacheBlocked = true;
  }
  canonical.uiContext = localContext;
  let uiState = clone(canonical);
  let revision = Number.isSafeInteger(boot.revision) ? boot.revision : 0;
  let cacheTimer=null;
  let windowView='today';
  let inFlight = false, timer = null, reloadTimer = null, needsReload = false, pointers = 0;
  let status = cacheBlocked ? 'Storage unavailable' : 'Ready', lastSyncedAt = null, error = cacheBlocked ? 'A saved pending update could not be read.' : '';
  let connected = !cacheBlocked, stopped = false;
  const dirty = () => !equal(base, shared(canonical));
  function persistCache() {
    if (cacheBlocked) throw new Error('A saved pending update could not be read.');
    global.localStorage.setItem(cacheKey, JSON.stringify({base, state:shared(canonical), context:localContext, userEdited:userEditedPending,cloudSession}));
  }
  function queueCacheWrite() {
    if (cacheTimer !== null) return;
    cacheTimer = setTimeout(() => {
      cacheTimer = null;
      try { persistCache(); }
      catch (failure) {
        status = 'Couldn’t save changes';
        error = failure instanceof Error ? failure.message : 'PC storage is unavailable.';
        updateDesktop();
      }
    }, 0);
  }
  function notifyUI() {
    needsReload = true;
    if (reloadTimer) return;
    const attempt = () => {
      reloadTimer = null;
      if (!needsReload || stopped) return;
      if (!global.TaskListApp || typeof global.TaskListApp.reloadFromNative !== 'function'
          || document.querySelector('dialog[open],.tm-completing') || pointers) {
        reloadTimer = setTimeout(attempt, 200); return;
      }
      needsReload = false;
      uiState = clone(canonical);
      global.TaskListApp.reloadFromNative();updateDesktop();
    };
    reloadTimer = setTimeout(attempt, 0);
  }
  document.addEventListener('pointerdown', () => { pointers++; }, true);
  const release = () => { pointers = Math.max(0, pointers - 1); };
  document.addEventListener('pointerup', release, true);
  document.addEventListener('pointercancel', release, true);
  global.addEventListener('blur', () => { pointers = 0; });
  function schedule(delay) {
    if (stopped || cacheBlocked) return;
    if(timer&&delay>=1000)return;
    if(timer)clearTimeout(timer);
    timer = setTimeout(() => { timer = null; synchronize(); }, delay);
  }
  async function request(endpoint, options) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 8000);
    try {
      const response = await fetch(new URL(endpoint, origin), {
        ...options, cache:'no-store', credentials:'omit', redirect:'error', signal:controller.signal,
        headers:{'Authorization':'Bearer ' + boot.token, 'Content-Type':'application/json', ...(options?.headers || {})}
      });
      if (!response.ok) throw new Error(response.status === 401 ? 'Desktop pairing was rejected.' : 'Desktop sync is unavailable.');
      const text = await response.text();
      if (new TextEncoder().encode(text).byteLength > MAX_BYTES + 4096) throw new Error('Desktop sync response is too large.');
      const data = JSON.parse(text);
      if (!object(data) || !Number.isSafeInteger(data.revision) || data.revision < 0) throw new Error('Desktop sync returned invalid data.');
      return {state:shared(validate(data.state)), revision:data.revision, phoneLinked:!!data.phoneLinked, phoneLastSeenAt:Number(data.phoneLastSeenAt)||0,cloudSession:data.cloudSession||''};
    } finally { clearTimeout(timeout); }
  }
  async function synchronize() {
    if (stopped || cacheBlocked || inFlight) { if (!stopped && !cacheBlocked) schedule(500); return; }
    inFlight = true; status = 'Syncing';
    const sent = shared(canonical), previousBase = clone(base), isWrite = dirty(), sentUserEdit = userEditedPending;
    try {
      const reply = await request(isWrite ? '/api/sync' : '/api/state', isWrite
        ? {method:'POST', body:JSON.stringify({base:previousBase, state:sent, userEdited:sentUserEdit,cloudSession})} : {method:'GET'});
      if (reply.revision < revision) throw new Error('Desktop state is older than this window. Your pending changes were kept.');
      // A task may have been edited again while this request was in flight.
      const current = shared(canonical);
      const reset=reply.cloudSession!==cloudSession;
      if(reset&&dirty())global.localStorage.setItem(cacheKey+':before-cloud-restore:'+Date.now(),JSON.stringify({base,state:current}));
      const merged = merge(reset?sent:(isWrite ? sent : previousBase), current, reply.state, '');
      cloudSession=reply.cloudSession;
      const changed = !equal(current, merged);
      phoneLinked=reply.phoneLinked;phoneLastSeenAt=reply.phoneLastSeenAt;
      if(isWrite&&sentUserEdit)userEditedPending=false;
      base = reply.state;
      canonical = Object.assign(merged, {uiContext:localContext});
      revision = reply.revision; connected = true; status = 'Connected'; error = ''; lastSyncedAt = new Date().toISOString();
      persistCache();
      if (changed) notifyUI();else updateDesktop();
    } catch (failure) {
      connected = false; status = 'Offline';
      error = failure && failure.name === 'AbortError' ? 'Desktop sync timed out. Your pending changes are kept on this PC.'
        : (failure instanceof Error ? failure.message : 'Desktop sync is unavailable.');
    } finally {
      inFlight = false; schedule(dirty() && connected ? 150 : 3000);
    }
  }
  let phoneLinked=!!boot.phoneLinked,phoneLastSeenAt=Number(boot.phoneLastSeenAt)||0;
  document.documentElement.classList.add('tl-desktop');
  const style=document.createElement('style');style.textContent=`
    html.tl-desktop body>main{max-width:960px;margin:auto}
    .tl-pc-bar,.tl-pc-welcome{position:relative;z-index:1;max-width:934px;margin:0 auto 18px;font:14px/1.5 system-ui;color:var(--tl-muted,#b9c7d9)}
    .tl-pc-bar{display:flex;align-items:center;gap:12px;justify-content:space-between}
    .tl-pc-bar button,.tl-pc-welcome button{font:inherit;border:1px solid var(--tl-border,#536478);border-radius:9px;padding:7px 12px;background:var(--tl-surface,#243649);color:var(--tl-text,#eef3fb);cursor:pointer}
    .tl-pc-welcome{padding:48px 18px;max-width:500px}.tl-pc-welcome h1{color:var(--tl-text,#eef3fb);font-size:30px;margin:0 0 12px}
    html.tl-desktop body{padding-bottom:30px!important}
  `;document.head.append(style);
  const staleGoogleSyncMs = 60 * 60 * 1000;
  let bar,connectionLabel,googleSyncSummary,lastGoogleSync,googleSyncWarning;
  let googleStatus={signedIn:false,message:'Not signed in'};
  async function googleRequest(path){
    const response=await fetch(new URL('/api/google/'+path,origin),{method:path==='status'?'GET':'POST',credentials:'omit',cache:'no-store',redirect:'error',headers:{Authorization:'Bearer '+boot.token}});
    if(!response.ok)throw Error('The Google connection is unavailable.');
    googleStatus=await response.json();updateDesktop();return googleStatus;
  }
  function updateDesktop(){
    if(!document.body)return;
    document.documentElement.classList.toggle('tl-phone-linked',phoneLinked);
    if(!bar){
      bar=document.createElement('div');bar.className='tl-pc-bar';
      connectionLabel=document.createElement('span');connectionLabel.setAttribute('role','status');
      const link=document.createElement('button');link.textContent='Connect';link.onclick=()=>global.TaskListApp?.showConnections();
      googleSyncSummary=document.createElement('span');
      lastGoogleSync=document.createElement('time');
      googleSyncWarning=document.createElement('span');googleSyncWarning.setAttribute('role','img');
      googleSyncWarning.textContent='⚠️';
      googleSyncSummary.append(googleSyncWarning,' ',lastGoogleSync);
      bar.append(connectionLabel,googleSyncSummary,link);document.body.prepend(bar);
    }
    connectionLabel.textContent=!connected?'PC connection unavailable — changes kept locally':googleStatus.signedIn?googleStatus.email+' · '+googleStatus.message:Date.now()/1000-phoneLastSeenAt<20?'Phone connected':'Saved on this PC';
    const synced=Number(googleStatus.lastSyncAt);
    const stamp=new Date(synced*1000);
    const hasSync=Number.isFinite(stamp.getTime())&&synced>0;
    const warning=googleStatus.lastSyncFailed?'Latest Google sync failed':
      hasSync&&Date.now()-stamp.getTime()>=staleGoogleSyncMs?'Last successful Google sync was over an hour ago':'';
    googleSyncSummary.hidden=!googleStatus.signedIn||(!hasSync&&!warning);
    googleSyncWarning.hidden=!warning;
    if(warning){
      googleSyncWarning.setAttribute('aria-label',warning);
      googleSyncSummary.title=warning;
    }else{
      googleSyncWarning.removeAttribute('aria-label');
      googleSyncSummary.removeAttribute('title');
    }
    lastGoogleSync.hidden=!hasSync;
    if(hasSync){
      lastGoogleSync.dateTime=stamp.toISOString();
      lastGoogleSync.textContent='Last synced at '+stamp.toLocaleTimeString([], {hour:'numeric',minute:'2-digit'});
    }
  }
  document.addEventListener('DOMContentLoaded',()=>{updateDesktop();googleRequest('status').catch(()=>{});setInterval(()=>{updateDesktop();googleRequest('status').catch(()=>{});},5000);});
  const facade = {
    isDesktop:true,
    get hasLinkedPhone(){return phoneLinked;},
    readState() { return JSON.stringify(canonical); },
    writeState(text) {
      if(global.TaskListApp)userEditedPending=true;
      const incoming = validate(JSON.parse(text));
      const nextContext = typeof incoming.uiContext === 'string' ? incoming.uiContext : localContext;
      const next = merge(shared(uiState), shared(incoming), shared(canonical), '');
      canonical = Object.assign(next, {uiContext:nextContext}); localContext = nextContext;
      queueCacheWrite();
      uiState = clone(incoming);
      if (dirty()) schedule(200);
    },
    queueStateWrite(_base, text) { facade.writeState(text); },
    updateReminders(text) {
      const tasks=JSON.parse(text),definitions={};
      if(!Array.isArray(tasks))throw Error('Invalid task definitions');
      for(const task of tasks)if(object(task)&&typeof task.id==='string'&&typeof task.title==='string'&&!FORBIDDEN_KEYS.has(task.id))definitions[task.id]=clone(task);
      if(!equal(canonical.sharedDefinitions,definitions)){canonical.sharedDefinitions=definitions;queueCacheWrite();schedule(250);}
    },
    updateSnapshot() {},
    getGoogleStatus(){return JSON.stringify(googleStatus);},
    configureGoogle(){googleRequest('configure').catch(error=>{googleStatus.message=error.message;});},
    connectGoogle(){googleRequest('sign-in').catch(error=>{googleStatus.message=error.message;});},
    disconnectGoogle(){googleRequest('sign-out').catch(error=>{googleStatus.message=error.message;});},
    async syncGoogle(){
      try{
        // Save this window's edits to the native host before asking it to upload.
        while(inFlight)await new Promise(resolve=>setTimeout(resolve,25));
        await synchronize();
        if(!connected||dirty())throw Error('Local edits are still pending. Try syncing again when the PC connection is available.');
        await googleRequest('sync');
      }catch(error){googleStatus.message=error.message;updateDesktop();}
    },
    getLastView(){return windowView;},
    setLastView(value){if(['today','upcoming'].includes(value))windowView=value;},
    setThemeColors(background) { document.documentElement.style.backgroundColor = background; },
    showPairing() { global.open(boot.pairingUrl, '_blank', 'noopener'); },
    connectPC() { facade.showPairing(); },
    getSyncStatus() { return JSON.stringify({desktop:true, paired:true, connected, status, revision, lastSyncedAt, error, pending:dirty()}); },
    syncNow() { if (timer) { clearTimeout(timer); timer = null; } synchronize(); },
    disconnectPC() { /* The PC is the local sync host; pairing is managed in its pairing window. */ }
  };
  global.TaskListNative = facade;
  document.addEventListener('visibilitychange', () => { if (!document.hidden) facade.syncNow(); });
  global.addEventListener('pagehide', () => {
    stopped = true; if (cacheTimer !== null) clearTimeout(cacheTimer); if (timer) clearTimeout(timer); if (reloadTimer) clearTimeout(reloadTimer);
    try { persistCache(); } catch (_) { /* The existing cached copy remains available. */ }
  });
  schedule(dirty() ? 50 : 1000);
})(window);
