/* Shared, dependency-free appearance controls for Android WebView and the PC app. */
(function (global) {
  'use strict';
  const DEFAULTS = Object.freeze({ accent:'#68b7ff', background:'#10151f', gradient:false, gradientEnd:'#46525d', gradientAngle:180 });
  const PALETTE = ['#68b7ff','#5865f2','#a78bfa','#ec4899','#fb7185','#f97316','#facc15','#4ade80','#2dd4bf','#ffffff','#47535c','#10151f'];
  const node = (tag, className, text) => { const item = document.createElement(tag); if (className) item.className = className; if (text !== undefined) item.textContent = text; return item; };
  const button = (text, action, className) => { const item = node('button', className || 'btn', text); item.type = 'button'; item.addEventListener('click', action); return item; };
  function hex(value) {
    const text = String(value || '').trim().replace(/^#/, '');
    if (/^[0-9a-f]{3}$/i.test(text)) return '#' + [...text].map(c => c + c).join('').toLowerCase();
    return /^[0-9a-f]{6}$/i.test(text) ? '#' + text.toLowerCase() : null;
  }
  const channels = value => [1,3,5].map(i => parseInt(value.slice(i, i + 2), 16));
  const fromChannels = values => '#' + values.map(value => Math.max(0, Math.min(255, Math.round(value))).toString(16).padStart(2, '0')).join('');
  function mix(a, b, amount) { const left = channels(a), right = channels(b); return fromChannels(left.map((value, i) => value * (1 - amount) + right[i] * amount)); }
  function luminance(color) {
    const values = channels(color).map(value => value / 255).map(value => value <= .04045 ? value / 12.92 : Math.pow((value + .055) / 1.055, 2.4));
    return values[0] * .2126 + values[1] * .7152 + values[2] * .0722;
  }
  function contrast(a, b) { const x = luminance(a), y = luminance(b); return (Math.max(x, y) + .05) / (Math.min(x, y) + .05); }
  function foreground(colors) {
    const dark = '#10151f', light = '#f3f6fb';
    const darkScore = Math.min(...colors.map(color => contrast(color, dark)));
    const lightScore = Math.min(...colors.map(color => contrast(color, light)));
    return darkScore > lightScore ? dark : light;
  }
  function normalize(value) {
    const stored = value && typeof value === 'object' ? value : {};
    const angle = Number(stored.gradientAngle);
    return { accent:hex(stored.accent) || DEFAULTS.accent, background:hex(stored.background) || DEFAULTS.background,
      gradient:stored.gradient === true, gradientEnd:hex(stored.gradientEnd) || DEFAULTS.gradientEnd,
      gradientAngle:Number.isFinite(angle) ? Math.max(0, Math.min(360, Math.round(angle))) : DEFAULTS.gradientAngle };
  }
  function paint(theme) { return theme.gradient ? 'linear-gradient(' + theme.gradientAngle + 'deg, ' + theme.background + ', ' + theme.gradientEnd + ')' : theme.background; }
  function ensureStyles() {
    if (document.getElementById('tasks-theme-controls-style')) return;
    const style = node('style'); style.id = 'tasks-theme-controls-style';
    style.textContent = `
      .tasks-theme-settings{min-width:0;margin:0 0 14px;border:1px solid var(--tl-border);border-radius:8px;padding:8px}
      .tasks-theme-settings>summary{font-size:14px;font-weight:650;cursor:pointer;min-height:30px;color:var(--tl-text)}
      .tasks-theme-settings *{box-sizing:border-box}
      .tasks-color-fields{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px;margin-top:6px}
      .tasks-color-field{min-width:0;max-width:100%}
      .tasks-color-field>label{display:block;font-size:12px;color:var(--tl-muted);margin-bottom:4px}
      .tasks-color-entry{display:flex;gap:5px;min-width:0;align-items:stretch}
      .tasks-color-entry input{flex:1;width:0;min-width:0;max-width:100%;height:38px;padding:6px;font-family:ui-monospace,monospace;font-size:13px}
      .tasks-color-entry .tasks-color-open{width:38px;height:38px;min-width:38px;min-height:38px;flex:none;border:2px solid var(--tl-border);border-radius:6px;padding:0;box-shadow:inset 0 0 0 2px #ffffff33;cursor:pointer}
      .tasks-color-picker{margin-top:6px;padding:8px;border:1px solid var(--tl-border);border-radius:7px;background:var(--tl-surface);min-width:0}
      .tasks-color-picker[hidden],.tasks-gradient-options[hidden]{display:none!important}
      .tasks-color-palette{display:grid;grid-template-columns:repeat(6,minmax(0,1fr));gap:5px;margin-bottom:8px}
      .tasks-color-palette button{padding:0;min-width:0;width:100%;min-height:25px;height:25px;border:1px solid #ffffff66;border-radius:4px;cursor:pointer}
      .tasks-color-palette button[aria-pressed=true]{outline:2px solid var(--tl-accent);outline-offset:1px}
      .tasks-rgb-label{display:grid;grid-template-columns:14px minmax(0,1fr) 25px;gap:4px;align-items:center;font-size:11px;min-width:0;color:var(--tl-text);margin:3px 0}
      .tasks-rgb-label input{width:100%;min-width:0;max-width:100%;height:25px;accent-color:var(--tl-accent);padding:0}
      .tasks-rgb-label output{text-align:right}
      .tasks-color-picker .tasks-color-done{display:block;margin:5px 0 0 auto;min-height:30px;height:30px;padding:3px 8px;font-size:12px}
      .tasks-gradient-toggle{display:flex;align-items:center;gap:7px;margin:12px 0 7px;font-size:13px;color:var(--tl-text)}
      .tasks-gradient-toggle input{width:18px;height:18px;flex:none;margin:0;accent-color:var(--tl-accent)}
      .tasks-gradient-options{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1fr);gap:8px;align-items:end;min-width:0}
      .tasks-gradient-angle{display:block;margin:0;font-size:12px;min-width:0;color:var(--tl-muted)}
      .tasks-gradient-angle input{display:block;width:100%;min-width:0;max-width:100%;height:38px;accent-color:var(--tl-accent);margin:0}
      .tasks-theme-preview{min-height:88px;border:1px solid var(--tl-border);border-radius:8px;padding:12px;margin:10px 0;overflow-wrap:anywhere}
      .tasks-theme-preview strong{display:block;font-size:18px;margin-bottom:8px}
      .tasks-theme-preview .tasks-preview-chip{display:inline-block;border-radius:5px;font-size:12px;padding:5px 9px}
      .tasks-theme-actions{display:flex;gap:6px;flex-wrap:wrap;margin-top:8px}
      .tasks-theme-actions .btn{font-size:12px;padding:5px 8px;min-height:36px}
      .tasks-theme-settings .tasks-theme-error{color:#ff9388;font-size:12px;margin:8px 0}
      .tasks-theme-error:empty{display:none}
      @media(max-width:350px){.tasks-color-fields,.tasks-gradient-options{gap:6px}.tasks-theme-settings{padding:7px}.tasks-color-entry input{font-size:12px;padding:5px}.tasks-color-entry .tasks-color-open{width:32px;min-width:32px}}
    `;
    document.head.append(style);
  }
  function apply(state, native) {
    ensureStyles();
    const theme = normalize(state && state.uiTheme);
    const colors = theme.gradient ? [theme.background, theme.gradientEnd] : [theme.background];
    const text = foreground(colors), middle = theme.gradient ? mix(theme.background, theme.gradientEnd, .5) : theme.background;
    const surface = theme.background === DEFAULTS.background && !theme.gradient ? '#172131' : mix(middle, text, .07);
    const variables = { '--tl-accent':theme.accent, '--tl-background':theme.background, '--tl-surface':surface,
      '--tl-ink':foreground([theme.accent]), '--tl-text':text, '--tl-muted':mix(middle, text, .7),
      '--tl-border':mix(middle, text, .25), '--tl-selected':mix(middle, theme.accent, .18) };
    Object.entries(variables).forEach(([key, value]) => document.documentElement.style.setProperty(key, value));
    document.documentElement.style.colorScheme = text === '#10151f' ? 'light' : 'dark';
    document.documentElement.style.setProperty('background', theme.background, 'important');
    if (document.body) {
      document.body.style.setProperty('background', paint(theme), 'important');
      document.body.style.backgroundAttachment = theme.gradient ? 'fixed' : '';
      document.body.style.backgroundRepeat = 'no-repeat';
      document.body.style.backgroundSize = theme.gradient ? '100% 100vh' : '';
      document.body.style.minHeight = '100vh';
    }
    const root = document.getElementById('today-task-list-20261023');
    if (root) root.style.setProperty('background', 'transparent', 'important');
    if (native && typeof native.setThemeColors === 'function') native.setThemeColors(theme.background);
    return theme;
  }
  function renderSettings(form, options) {
    ensureStyles();
    const readState = options.readState, persistState = options.persistState, native = options.native;
    const current = normalize(readState().uiTheme);
    const appearance = node('details', 'tasks-theme-settings'); appearance.open = true;
    appearance.append(node('summary', '', 'Appearance')); form.append(appearance);
    const fields = node('div', 'tasks-color-fields'); appearance.append(fields);
    const pickers = [];
    let updatePreview = () => {};
    function colorField(parent, name, value, placeholder) {
      const box = node('div', 'tasks-color-field');
      const label = node('label', '', name + ' hex');
      const input = node('input', 'form-control'); input.type = 'text'; input.value = value;
      input.placeholder = placeholder; input.spellcheck = false; input.autocapitalize = 'off'; input.setAttribute('aria-label', name + ' hex');
      const id = 'tasks-color-' + name.toLowerCase().replace(/\W+/g, '-'); input.id = id; label.htmlFor = id;
      const entry = node('div', 'tasks-color-entry');
      const picker = node('div', 'tasks-color-picker'); picker.hidden = true; picker.id = id + '-picker';
      const open = button('', () => { picker.hidden = !picker.hidden; open.setAttribute('aria-expanded', String(!picker.hidden)); }, 'tasks-color-open');
      open.style.background = value; open.title = 'Pick ' + name.toLowerCase() + ' color'; open.setAttribute('aria-label', open.title); open.setAttribute('aria-expanded', 'false'); open.setAttribute('aria-controls', picker.id);
      entry.append(input, open); box.append(label, entry, picker); parent.append(box);
      const palette = node('div', 'tasks-color-palette'); picker.append(palette);
      const swatches = PALETTE.map(color => {
        const swatch = button('', () => setValue(color), 'tasks-color-swatch'); swatch.style.background = color; swatch.dataset.color = color;
        swatch.setAttribute('aria-label', name + ' ' + color); palette.append(swatch); return swatch;
      });
      const sliders = ['Red','Green','Blue'].map((channel, i) => {
        const row = node('label', 'tasks-rgb-label'); row.append(node('span', '', channel[0]));
        const slider = node('input'); slider.type = 'range'; slider.min = '0'; slider.max = '255'; slider.step = '1'; slider.setAttribute('aria-label', name + ' ' + channel.toLowerCase());
        const output = node('output'); row.append(slider, output); picker.append(row);
        slider.addEventListener('input', () => { setValue(fromChannels(sliders.map(item => Number(item.slider.value)))); });
        return {slider,output};
      });
      picker.append(button('Done', () => { picker.hidden = true; open.setAttribute('aria-expanded','false'); open.focus(); }, 'btn tasks-color-done'));
      function sync() {
        const parsed = hex(input.value); if (!parsed) return;
        open.style.background = parsed;
        const values = channels(parsed); sliders.forEach((item, i) => { item.slider.value = values[i]; item.output.textContent = String(values[i]); });
        swatches.forEach(item => item.setAttribute('aria-pressed', String(item.dataset.color === parsed)));
      }
      function setValue(value) { input.value = value; sync(); updatePreview(); }
      input.addEventListener('input', () => { sync(); updatePreview(); }); sync();
      const result = {input,setValue,sync}; pickers.push(result); return result;
    }
    const accent = colorField(fields, 'Accent', current.accent, '#68B7FF');
    const background = colorField(fields, 'Background', current.background, '#10151F');
    const gradientLabel = node('label', 'tasks-gradient-toggle'), gradient = node('input'); gradient.type = 'checkbox'; gradient.checked = current.gradient; gradient.setAttribute('aria-label','Gradient background');
    gradientLabel.append(gradient, node('span', '', 'Gradient background')); appearance.append(gradientLabel);
    const gradientOptions = node('div', 'tasks-gradient-options'); gradientOptions.hidden = !gradient.checked; appearance.append(gradientOptions);
    const end = colorField(gradientOptions, 'Gradient end', current.gradientEnd, '#46525D');
    const angleLabel = node('label', 'tasks-gradient-angle'); angleLabel.append(node('span', '', 'Angle '));
    const angleOutput = node('output'), angle = node('input'); angle.type = 'range'; angle.min = '0'; angle.max = '360'; angle.step = '1'; angle.value = String(current.gradientAngle); angle.setAttribute('aria-label','Gradient angle'); angleLabel.append(angleOutput, angle); gradientOptions.append(angleLabel);
    const preview = node('div', 'tasks-theme-preview'); preview.setAttribute('aria-label','Theme preview'); preview.append(node('strong', '', 'Tasks')); const chip = node('span', 'tasks-preview-chip', 'Your colors'); preview.append(chip); appearance.append(preview);
    const error = node('p', 'tasks-theme-error'); error.setAttribute('role','alert'); appearance.append(error);
    function draft() {
      const a = hex(accent.input.value), b = hex(background.input.value), e = hex(end.input.value);
      if (!a || !b || (gradient.checked && !e)) return null;
      return {accent:a, background:b, gradient:gradient.checked, gradientEnd:e || DEFAULTS.gradientEnd, gradientAngle:Number(angle.value)};
    }
    updatePreview = () => {
      gradientOptions.hidden = !gradient.checked; angleOutput.textContent = angle.value + '°';
      const theme = draft(); if (!theme) return;
      preview.style.background = paint(theme); preview.style.color = foreground(theme.gradient ? [theme.background,theme.gradientEnd] : [theme.background]); preview.style.borderColor = theme.accent;
      chip.style.background = theme.accent; chip.style.color = foreground([theme.accent]);
    };
    gradient.addEventListener('change', updatePreview); angle.addEventListener('input', updatePreview);
    function save() {
      const theme = draft();
      if (!theme) { error.textContent = 'Use a hex color such as #68B7FF.'; return; }
      try {
        const state = readState();
        persistState(Object.assign({}, state, {uiTheme:Object.assign({}, state.uiTheme || {}, theme)}));
        apply(readState(), native); error.textContent = '';
        accent.setValue(theme.accent); background.setValue(theme.background); end.setValue(theme.gradientEnd);
      } catch (_) { error.textContent = 'Couldn’t save colors. Please try again.'; }
    }
    const actions = node('div', 'tasks-theme-actions');
    actions.append(button('Slate gradient', () => {
      background.setValue('#1e2935'); end.setValue('#47535c'); gradient.checked = true; angle.value = '180'; updatePreview();
    }), button('Apply colors', save, 'btn btn-primary'), button('Reset colors', () => {
      accent.setValue(DEFAULTS.accent); background.setValue(DEFAULTS.background); end.setValue(DEFAULTS.gradientEnd); gradient.checked = false; angle.value = String(DEFAULTS.gradientAngle); updatePreview(); save();
    }));
    appearance.append(actions); updatePreview();
    return { element:appearance, apply:save, getDraft:draft };
  }
  global.TasksTheme = Object.freeze({ apply, renderSettings, normalize, defaults:DEFAULTS });
})(window);
