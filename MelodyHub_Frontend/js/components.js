/* Reusable UI: icons, toast, modal, confirm, header/footer, portal shell, cards, form fields and validation, cart */
(function (MH) {
  var esc = MH.esc;

  // ---------------- icons (24px stroke set) ----------------
  var ICONS = {
    home: 'M3 11l9-8 9 8M5 10v10h14V10',
    box: 'M21 8l-9-5-9 5 9 5 9-5zM3 8v8l9 5 9-5V8M12 13v8',
    tag: 'M3 12V3h9l9 9-9 9-9-9zM8 8h.01',
    users: 'M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2M9 11a4 4 0 100-8 4 4 0 000 8zM22 21v-2a4 4 0 00-3-3.9M16 3.1a4 4 0 010 7.8',
    cart: 'M3 4h2l2.4 11.4a2 2 0 002 1.6h7.7a2 2 0 002-1.5L21 8H6M10 21h.01M18 21h.01',
    card: 'M2 6h20v12H2zM2 10h20',
    shield: 'M12 2l8 3v6c0 5-3.4 9-8 11-4.6-2-8-6-8-11V5l8-3z',
    music: 'M9 18V5l12-2v13M9 18a3 3 0 11-6 0 3 3 0 016 0zM21 16a3 3 0 11-6 0 3 3 0 016 0z',
    disc: 'M12 22a10 10 0 100-20 10 10 0 000 20zM12 15a3 3 0 100-6 3 3 0 000 6z',
    mic: 'M12 2a3 3 0 00-3 3v7a3 3 0 006 0V5a3 3 0 00-3-3zM19 11a7 7 0 01-14 0M12 18v4',
    warehouse: 'M3 21V8l9-5 9 5v13M7 21v-8h10v8M7 17h10',
    truck: 'M1 5h13v11H1zM14 9h4l3 3v4h-7M5.5 19a1.5 1.5 0 100-3 1.5 1.5 0 000 3zM17.5 19a1.5 1.5 0 100-3 1.5 1.5 0 000 3z',
    file: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6zM14 2v6h6M8 13h8M8 17h8',
    megaphone: 'M3 11v2a1 1 0 001 1h2l5 4V6L6 10H4a1 1 0 00-1 1zM15 8a5 5 0 010 8M18 5a9 9 0 010 14',
    star: 'M12 2l3 6.5 7 .9-5.2 4.8 1.4 7L12 17.8 5.8 21.2l1.4-7L2 9.4l7-.9L12 2z',
    gear: 'M12 15a3 3 0 100-6 3 3 0 000 6zM19.4 15a1.7 1.7 0 00.3 1.8l.1.1a2 2 0 11-2.8 2.8l-.1-.1a1.7 1.7 0 00-1.8-.3 1.7 1.7 0 00-1 1.5V21a2 2 0 01-4 0v-.1a1.7 1.7 0 00-1.1-1.5 1.7 1.7 0 00-1.8.3l-.1.1a2 2 0 11-2.8-2.8l.1-.1a1.7 1.7 0 00.3-1.8 1.7 1.7 0 00-1.5-1H3a2 2 0 010-4h.1a1.7 1.7 0 001.5-1.1 1.7 1.7 0 00-.3-1.8l-.1-.1a2 2 0 112.8-2.8l.1.1a1.7 1.7 0 001.8.3H9a1.7 1.7 0 001-1.5V3a2 2 0 014 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.8-.3l.1-.1a2 2 0 112.8 2.8l-.1.1a1.7 1.7 0 00-.3 1.8V9a1.7 1.7 0 001.5 1H21a2 2 0 010 4h-.1a1.7 1.7 0 00-1.5 1z',
    logout: 'M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9',
    menu: 'M3 6h18M3 12h18M3 18h18',
    x: 'M18 6L6 18M6 6l12 12',
    plus: 'M12 5v14M5 12h14',
    search: 'M11 19a8 8 0 100-16 8 8 0 000 16zM21 21l-4.3-4.3',
    eye: 'M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12zM12 15a3 3 0 100-6 3 3 0 000 6z',
    edit: 'M12 20h9M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4 12.5-12.5z',
    trash: 'M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6M10 11v6M14 11v6',
    check: 'M20 6L9 17l-5-5',
    alert: 'M12 9v4M12 17h.01M10.3 3.9L1.8 18a2 2 0 001.7 3h17a2 2 0 001.7-3L13.7 3.9a2 2 0 00-3.4 0z',
    upload: 'M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M17 8l-5-5-5 5M12 3v12',
    list: 'M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01',
    user: 'M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z',
    chart: 'M3 3v18h18M7 14l4-4 3 3 5-6',
    layers: 'M12 2l10 5-10 5L2 7l10-5zM2 17l10 5 10-5M2 12l10 5 10-5',
    handshake: 'M11 17l2 2a1 1 0 003-3M14 14l2.5 2.5a1 1 0 003-3L15 8.5M7 15l3 3M3 12l5-5 4 1 3-3 6 6',
    clipboard: 'M9 2h6a1 1 0 011 1v2H8V3a1 1 0 011-1zM8 4H6a2 2 0 00-2 2v14a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-2M9 13l2 2 4-4',
    download: 'M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3',
    image: 'M3 5h18v14H3zM3 16l5-5 4 4 3-3 6 6M15.5 9.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z',
    percent: 'M19 5L5 19M6.5 9a2.5 2.5 0 100-5 2.5 2.5 0 000 5zM17.5 20a2.5 2.5 0 100-5 2.5 2.5 0 000 5z',
    returns: 'M9 14L4 9l5-5M4 9h11a5 5 0 010 10h-3',
    message: 'M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z',
    store: 'M3 9l1.5-5h15L21 9M3 9v11h18V9M3 9h18M9 20v-6h6v6',
    chevron: 'M9 18l6-6-6-6',
    wallet: 'M3 7a2 2 0 012-2h13v4M3 7v11a2 2 0 002 2h15V9H5a2 2 0 01-2-2zM16 14h.01',
    clock: 'M12 22a10 10 0 100-20 10 10 0 000 20zM12 6v6l4 2',
    mail: 'M3 5h18v14H3zM3 6l9 7 9-7',
    phone: 'M22 16.9v3a2 2 0 01-2.2 2 19.8 19.8 0 01-8.6-3.1 19.5 19.5 0 01-6-6A19.8 19.8 0 012.1 4.2 2 2 0 014.1 2h3a2 2 0 012 1.7c.1.9.4 1.8.7 2.7a2 2 0 01-.5 2.1L8 9.8a16 16 0 006 6l1.3-1.3a2 2 0 012.1-.4c.9.3 1.8.6 2.7.7a2 2 0 011.7 2z',
    pin: 'M12 22s-8-6.5-8-13a8 8 0 0116 0c0 6.5-8 13-8 13zM12 12a3 3 0 100-6 3 3 0 000 6z',
    info: 'M12 22a10 10 0 100-20 10 10 0 000 20zM12 16v-4M12 8h.01'
  };
  MH.icon = function (name, size) {
    return '<svg viewBox="0 0 24 24" width="' + (size || 18) + '" height="' + (size || 18) + '" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="' + (ICONS[name] || ICONS.box) + '"/></svg>';
  };

  // ---------------- brand ----------------
  // The MelodyHub mark: five equalizer bars on an ink tile. The bars are the brand's one signature.
  MH.logoSvg = '<svg viewBox="0 0 36 36" aria-hidden="true"><rect width="36" height="36" rx="10" fill="#15161a"/><g fill="#f7b500"><rect x="8" y="15.5" width="3" height="7" rx="1.5"/><rect x="12.75" y="11" width="3" height="16" rx="1.5"/><rect x="17.5" y="7.5" width="3" height="21" rx="1.5"/><rect x="22.25" y="12.5" width="3" height="13" rx="1.5"/><rect x="27" y="16" width="3" height="6" rx="1.5" opacity=".55"/></g></svg>';
  MH.logo = function () { return '<a class="logo" href="' + MH.url('index.html') + '" aria-label="MelodyHub home">' + MH.logoSvg + '<span>MelodyHub</span></a>'; };
  // decorative equalizer used on the hero stage and the login page (heights are fixed, so it never jumps)
  var EQ = [22, 38, 30, 54, 46, 70, 58, 84, 66, 92, 74, 60, 80, 52, 68, 44, 58, 36, 48, 30, 40, 26, 34, 20];
  MH.eqBars = function (count, hot) {
    var out = '';
    for (var i = 0; i < count; i++) {
      var h = EQ[i % EQ.length];
      out += '<i style="height:' + h + '%;animation-delay:' + (i * 28) + 'ms"' + (hot && hot.indexOf(i) >= 0 ? ' class="hot"' : '') + '></i>';
    }
    return '<div class="stage-eq" aria-hidden="true">' + out + '</div>';
  };
  MH.eqMark = '<span class="eq" aria-hidden="true"><i></i><i></i><i></i></span>';

  // ---------------- toast ----------------
  MH.toast = function (message, type) {
    type = type || 'info';
    var host = document.querySelector('.toasts');
    if (!host) { host = document.createElement('div'); host.className = 'toasts'; host.setAttribute('role', 'status'); host.setAttribute('aria-live', 'polite'); document.body.appendChild(host); }
    var t = document.createElement('div');
    t.className = 'toast ' + type;
    var ic = type === 'ok' ? 'check' : type === 'error' ? 'alert' : 'info';
    t.innerHTML = '<span class="t-icon">' + MH.icon(ic, 13) + '</span><span>' + esc(message) + '</span><button type="button" aria-label="Dismiss">' + MH.icon('x', 15) + '</button>';
    function close() { t.classList.add('leaving'); setTimeout(function () { t.remove(); }, 200); }
    t.querySelector('button').onclick = close;
    host.appendChild(t);
    while (host.children.length > 4) host.firstChild.remove();
    setTimeout(close, type === 'error' ? 7000 : 4000);
  };
  MH.ok = function (m) { MH.toast(m, 'ok'); };
  MH.fail = function (e) { MH.toast(e && e.message ? e.message : String(e), 'error'); };

  // ---------------- modal / confirm ----------------
  var openModals = 0;
  MH.modal = function (o) {
    var back = document.createElement('div');
    back.className = 'modal-backdrop';
    var footer = (o.footer || []).map(function (b, i) {
      return '<button type="button" class="btn ' + (b.cls || '') + '" data-i="' + i + '">' + esc(b.label) + '</button>';
    }).join('');
    var titleId = 'm' + Date.now() + Math.floor(Math.random() * 1000);
    back.innerHTML = '<div class="modal ' + (o.wide ? 'wide' : '') + '" role="dialog" aria-modal="true" aria-labelledby="' + titleId + '">' +
      '<div class="modal-head"><h3 id="' + titleId + '">' + esc(o.title) + '</h3><button type="button" class="icon-btn" data-close aria-label="Close">' + MH.icon('x', 20) + '</button></div>' +
      '<div class="modal-body"></div>' + (footer ? '<div class="modal-foot">' + footer + '</div>' : '') + '</div>';
    var body = back.querySelector('.modal-body');
    if (typeof o.body === 'string') body.innerHTML = o.body; else if (o.body) body.appendChild(o.body);
    var previousFocus = document.activeElement;
    document.body.appendChild(back);
    openModals++; document.body.classList.add('modal-open');

    var closed = false;
    var ctl = {
      el: back, body: body,
      close: function () {
        if (closed) return; closed = true;
        back.remove(); document.removeEventListener('keydown', onKey);
        if (--openModals <= 0) { openModals = 0; document.body.classList.remove('modal-open'); }
        if (previousFocus && previousFocus.focus) try { previousFocus.focus(); } catch (e) { /* element gone */ }
        if (o.onClose) o.onClose();
      },
      btn: function (i) { return back.querySelector('[data-i="' + i + '"]'); }
    };
    function onKey(e) {
      if (e.key === 'Escape') ctl.close();
      if (e.key === 'Tab') { // keep keyboard focus inside the dialog
        var f = back.querySelectorAll('button:not([disabled]),[href],input:not([type="hidden"]):not([disabled]),select:not([disabled]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"])');
        f = Array.prototype.filter.call(f, function (x) { return x.offsetParent !== null && x.getAttribute('tabindex') !== '-1'; });
        if (!f.length) return;
        if (e.shiftKey && document.activeElement === f[0]) { e.preventDefault(); f[f.length - 1].focus(); }
        else if (!e.shiftKey && document.activeElement === f[f.length - 1]) { e.preventDefault(); f[0].focus(); }
      }
    }
    document.addEventListener('keydown', onKey);
    if (!o.keepOpen) back.addEventListener('mousedown', function (e) { if (e.target === back) ctl.close(); });
    back.querySelector('[data-close]').onclick = ctl.close;
    (o.footer || []).forEach(function (b, i) {
      ctl.btn(i).onclick = function () {
        if (b.onClick) b.onClick(ctl, ctl.btn(i)); else ctl.close();
      };
    });
    var first = body.querySelector('input:not([type="file"]):not([type="hidden"]),select,textarea') || back.querySelector('.modal-foot .btn-primary, .modal-foot .btn');
    setTimeout(function () { if (first) first.focus(); }, 30);
    return ctl;
  };

  MH.confirm = function (o) {
    return new Promise(function (resolve) {
      var done = false;
      MH.modal({
        title: o.title || 'Are you sure?',
        body: '<p style="margin:0">' + (o.html || esc(o.message || '')) + '</p>',
        onClose: function () { if (!done) resolve(false); },
        footer: [
          { label: o.cancelText || 'Cancel', onClick: function (c) { c.close(); } },
          { label: o.confirmText || 'Confirm', cls: o.danger ? 'btn-danger solid' : 'btn-primary', onClick: function (c) { done = true; c.close(); resolve(true); } }
        ]
      });
    });
  };

  // ---------------- button loading ----------------
  MH.busy = function (btn, on, label) {
    if (!btn) return;
    if (on) {
      if (!btn.classList.contains('loading')) btn.dataset.label = btn.innerHTML;
      btn.classList.add('loading'); btn.disabled = true; btn.setAttribute('aria-busy', 'true');
      if (label) btn.textContent = label;
    } else {
      btn.classList.remove('loading'); btn.disabled = false; btn.removeAttribute('aria-busy');
      if (btn.dataset.label != null) { btn.innerHTML = btn.dataset.label; delete btn.dataset.label; }
    }
  };

  // ---------------- states ----------------
  MH.skeletonCards = function (n) { return new Array(n || 8).fill('<div class="skeleton sk-card"></div>').join(''); };
  MH.skeletonRows = function (n) { return '<div class="card card-flush" style="padding:6px 0">' + new Array(n || 6).fill('<div class="skeleton sk-row"></div>').join('') + '</div>'; };
  MH.empty = function (o) {
    return '<div class="empty"><div class="glyph">' + MH.icon(o.icon || 'disc', 26) + '</div><h3>' + esc(o.title) + '</h3>' + (o.text ? '<p>' + esc(o.text) + '</p>' : '') + (o.actionHtml || '') + '</div>';
  };
  MH.errorState = function (msg, retryId) {
    return '<div class="empty"><div class="glyph" style="color:var(--bad);background:var(--bad-bg)">' + MH.icon('alert', 26) + '</div><h3>This could not be loaded</h3><p>' + esc(msg) + '</p><button type="button" class="btn" id="' + (retryId || 'retry') + '">Try again</button></div>';
  };
  MH.breadcrumbs = function (items) {
    return '<nav class="breadcrumbs" aria-label="Breadcrumb">' + items.map(function (it, i) {
      var last = i === items.length - 1;
      return (i ? '<span class="sep" aria-hidden="true">/</span>' : '') + (it[1] && !last ? '<a href="' + it[1] + '">' + esc(it[0]) + '</a>' : '<span' + (last ? ' aria-current="page"' : '') + '>' + esc(it[0]) + '</span>');
    }).join('') + '</nav>';
  };

  // ---------------- form validation ----------------
  var NUMERIC = { money: 1, decimal: 1, int: 1, percent: 1 };
  var KIND_BY_TYPE = { select: 'select', textarea: 'longtext', file: 'file', email: 'email', tel: 'phone', url: 'url', date: 'date', password: 'password' };

  // Turns a form-field description (used by MH.field) into a validation spec (see js/validate.js).
  MH.specOf = function (f) {
    var kind = f.kind || KIND_BY_TYPE[f.type] || (f.type === 'number' ? (String(f.step) === '1' ? 'int' : 'decimal') : 'text');
    var spec = { label: f.errorLabel || f.label, kind: kind, required: !!f.required };
    ['min', 'max', 'minlength', 'maxlength', 'decimals', 'positive', 'minDate', 'maxDate', 'allowZero', 'what', 'exts', 'mimes', 'maxMB', 'requiredMsg', 'typeMsg', 'blankMsg', 'minMsg', 'maxMsg'].forEach(function (k) { if (f[k] != null) spec[k] = f[k]; });
    if (kind === 'file' && !spec.what) spec.what = 'image';
    if (kind === 'longtext' && spec.maxlength == null) spec.maxlength = 1000;
    if (kind === 'email' && spec.maxlength == null) spec.maxlength = 254;
    if (kind === 'password' && spec.maxlength == null) spec.maxlength = 72;
    return spec;
  };

  MH.field = function (f, value) {
    var spec = MH.specOf(f), kind = spec.kind;
    var id = 'f_' + f.name, v = value == null ? '' : value, req = f.required ? ' required aria-required="true"' : '';
    var dis = f.disabled ? ' disabled' : '';
    var specAttr = ' data-spec="' + esc(JSON.stringify(spec)) + '"';
    var attrs = '', type = f.type || (NUMERIC[kind] ? 'number' : 'text');
    if (NUMERIC[kind]) {
      attrs += ' step="' + (f.step || (kind === 'int' ? '1' : kind === 'decimal' ? 'any' : '0.01')) + '" inputmode="' + (kind === 'int' ? 'numeric' : 'decimal') + '"';
      if (f.min != null) attrs += ' min="' + f.min + '"';
      if (f.max != null) attrs += ' max="' + f.max + '"';
    } else if (kind === 'date') {
      attrs += ' min="' + (MH.v.resolveDate(spec.minDate) || '1900-01-01') + '" max="' + (MH.v.resolveDate(spec.maxDate) || '2100-12-31') + '"';
    } else if (kind !== 'select' && kind !== 'file') {
      attrs += ' maxlength="' + (spec.maxlength || (f.type === 'textarea' ? 1000 : 255)) + '"';
    }
    if (f.placeholder) attrs += ' placeholder="' + esc(f.placeholder) + '"';
    var control;
    if (f.type === 'select') {
      control = '<select class="select" id="' + id + '" name="' + f.name + '"' + req + dis + specAttr + '><option value="">' + esc(f.placeholder || 'Select…') + '</option>' +
        (f.options || []).map(function (o) {
          var val = typeof o === 'object' ? o.value : o, lab = typeof o === 'object' ? o.label : MH.humanize(o);
          return '<option value="' + esc(val) + '"' + (String(val) === String(v) ? ' selected' : '') + '>' + esc(lab) + '</option>';
        }).join('') + '</select>';
    } else if (f.type === 'textarea') {
      control = '<textarea class="textarea" id="' + id + '" name="' + f.name + '"' + req + dis + attrs + specAttr + '>' + esc(v) + '</textarea>';
    } else if (f.type === 'file') {
      var def = MH.v.FILE_KINDS[spec.what], maxMB = spec.maxMB || def.maxMB;
      var accept = ' accept="' + esc(f.accept || (def.exts.map(function (e) { return '.' + e; }).concat(def.mimes)).join(',')) + '"';
      var help = f.fileHelp || ((spec.what === 'audio' ? 'MP3 only, up to ' : 'JPG, JPEG, PNG or WEBP, up to ') + maxMB + ' MB.' + (f.fileHint ? ' ' + f.fileHint : ''));
      var helpId = id + '_help';
      // the real <input type="file"> is hidden; the visible button opens it (keyboard friendly)
      var input = '<input id="' + id + '" name="' + f.name + '" type="file" class="sr-only-file" tabindex="-1" aria-describedby="' + helpId + '"' + accept + req + dis + specAttr + '>';
      var choose = function (text) {
        return '<button type="button" class="btn btn-sm btn-upload" data-upload-choose aria-describedby="' + helpId + '"' + dis + '>' + MH.icon('upload', 15) + '<span data-upload-choose-text>' + text + '</span></button>';
      };
      if (spec.what === 'audio') {
        control = '<div class="file-pick" data-upload-wrap><span class="file-icon">' + MH.icon('music', 20) + '</span><div class="image-upload-info">' +
          '<span class="image-upload-name" data-upload-name>No file chosen</span><span class="image-upload-status" data-upload-status hidden></span>' +
          '<div class="image-upload-row">' + choose('Choose MP3') + '</div>' +
          '<div class="field-help" id="' + helpId + '">' + esc(help) + '</div></div>' + input + '</div>';
      } else {
        var emptyIcon = f.emptyIcon || 'image';
        control = '<div class="image-upload' + (f.round ? ' round' : '') + '" data-upload-wrap data-empty-icon="' + esc(emptyIcon) + '">' +
          '<div class="image-upload-preview-wrap" data-upload-preview>' + (v ? '<img class="upload-preview-image" src="' + esc(MH.assetUrl(v)) + '" alt="Current image" data-upload-preview-img>' : MH.uploadEmpty(emptyIcon)) + '</div>' +
          '<div class="image-upload-info">' +
          '<span class="image-upload-name" data-upload-name>' + (v ? 'Current image' : 'No image chosen') + '</span>' +
          '<span class="image-upload-status" data-upload-status hidden></span>' +
          '<div class="image-upload-row">' + choose(v ? 'Replace Image' : 'Choose Image') +
          '<button type="button" class="btn btn-sm btn-ghost btn-remove" data-upload-remove' + (v ? '' : ' hidden') + dis + '>' + MH.icon('trash', 15) + 'Remove</button>' +
          '<button type="button" class="btn btn-sm btn-ghost" data-upload-undo hidden>Undo</button></div>' +
          '<div class="field-help" id="' + helpId + '">' + esc(help) + '</div></div>' + input + '</div>';
      }
    } else {
      control = '<input class="input" id="' + id + '" name="' + f.name + '" type="' + type + '" value="' + esc(v) + '"' + req + dis + attrs + specAttr + ' autocomplete="' + (f.autocomplete || 'off') + '">';
    }
    return '<div class="field' + (f.full ? ' full' : '') + '" data-field="' + f.name + '"><label for="' + id + '">' + esc(f.label) + (f.required ? '<span class="req" aria-hidden="true">*</span>' : '') + '</label>' + control +
      (f.help ? '<div class="field-help">' + esc(f.help) + '</div>' : '') + '<div class="field-error" role="alert"></div></div>';
  };

  // ---- preview, replace, remove, undo and drag-and-drop for file fields ----
  MH.uploadEmpty = function (icon) {
    return '<div class="image-upload-preview empty" data-upload-empty>' + MH.icon(icon || 'image', 26) + '</div>';
  };
  MH.bindFileFields = function (form) {
    form.querySelectorAll('input[type="file"]').forEach(function (input) {
      if (input.dataset.boundFile) return;
      input.dataset.boundFile = '1';
      var wrap = input.closest('[data-upload-wrap]');
      if (!wrap) return;
      var spec = JSON.parse(input.dataset.spec || '{}'), what = spec.what || 'image';
      var nameEl = wrap.querySelector('[data-upload-name]'), statusEl = wrap.querySelector('[data-upload-status]');
      var pw = wrap.querySelector('[data-upload-preview]');
      var chooseBtn = wrap.querySelector('[data-upload-choose]'), chooseText = wrap.querySelector('[data-upload-choose-text]');
      var removeBtn = wrap.querySelector('[data-upload-remove]'), undoBtn = wrap.querySelector('[data-upload-undo]');
      var initialHtml = pw ? pw.innerHTML : '', initialName = nameEl ? nameEl.textContent : '';
      var hadFile = !!(pw && pw.querySelector('img'));   // editing a record that already has an image
      var objectUrl = null, good = null;                  // good = the last valid file picked

      function set(o) {
        if (nameEl) nameEl.textContent = o.name;
        if (statusEl) { statusEl.textContent = o.status || ''; statusEl.hidden = !o.status; }
        if (chooseText) chooseText.textContent = what === 'audio' ? (o.showing ? 'Choose another MP3' : 'Choose MP3') : (o.showing ? 'Replace Image' : 'Choose Image');
        if (removeBtn) removeBtn.hidden = !o.showing;
        if (undoBtn) undoBtn.hidden = !(o.changed && hadFile);
      }
      function forget() { if (objectUrl) { URL.revokeObjectURL(objectUrl); objectUrl = null; } }
      // back to what the form showed when it opened (the current image is kept)
      function restore() {
        forget(); good = null; input.value = ''; wrap.dataset.cleared = '';
        if (pw) pw.innerHTML = initialHtml;
        set({ name: initialName, showing: hadFile, changed: false });
      }
      // nothing chosen; a stored image is removed when the form is saved
      function clearAll() {
        forget(); good = null; input.value = '';
        wrap.dataset.cleared = hadFile ? '1' : '';
        if (pw) pw.innerHTML = MH.uploadEmpty(wrap.dataset.emptyIcon);
        set({ name: what === 'audio' ? 'No file chosen' : 'No image chosen', status: hadFile ? 'The current image will be removed when you save.' : '', showing: false, changed: hadFile });
      }
      function show(file) {
        good = file; wrap.dataset.cleared = '';
        set({ name: file.name, status: MH.fileSize(file.size) + (what === 'audio' ? '' : (hadFile ? ', replaces the current image when you save' : ', uploaded when you save')), showing: true, changed: true });
        if (!pw) return;
        forget();
        objectUrl = URL.createObjectURL(file);
        pw.innerHTML = '<img class="upload-preview-image" data-upload-preview-img alt="">';
        var img = pw.querySelector('img'); img.alt = 'Preview of ' + file.name; img.src = objectUrl;
      }
      // a rejected file never replaces what was there: the last valid choice (or nothing) goes back into the input
      function reject(msg) {
        try { var dt = new DataTransfer(); if (good) dt.items.add(good); input.files = dt.files; }
        catch (err) { if (good) restore(); else input.value = ''; }
        MH.setFieldError(input, msg);
      }
      async function accept(file) {
        var msg = MH.v.checkFile(spec, file);
        if (!msg && !(await MH.v.sniff(file, what))) msg = spec.typeMsg || MH.v.FILE_KINDS[what].type;
        if (msg) { reject(msg); return false; }
        MH.setFieldError(input, '');
        show(file);
        return true;
      }

      if (chooseBtn) chooseBtn.addEventListener('click', function () { input.click(); });
      if (removeBtn) removeBtn.addEventListener('click', function () { clearAll(); MH.setFieldError(input, ''); if (chooseBtn) chooseBtn.focus(); });
      if (undoBtn) undoBtn.addEventListener('click', function () { restore(); MH.setFieldError(input, ''); if (chooseBtn) chooseBtn.focus(); });
      input.addEventListener('change', function () {
        var file = input.files && input.files[0];
        if (file) accept(file);
      });
      // drag a file onto the box
      ['dragenter', 'dragover'].forEach(function (t) { wrap.addEventListener(t, function (e) { e.preventDefault(); wrap.classList.add('dragover'); }); });
      ['dragleave', 'drop'].forEach(function (t) { wrap.addEventListener(t, function () { wrap.classList.remove('dragover'); }); });
      wrap.addEventListener('drop', function (e) {
        e.preventDefault();
        var file = e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files[0];
        if (!file) return;
        try { var dt = new DataTransfer(); dt.items.add(file); input.files = dt.files; } catch (err) { /* old browsers: use the button instead */ }
        accept(file);
      });
    });
  };

  // ---- validation ----
  function msgFor(el) {
    var v = el.validity;
    if (v.valueMissing) return 'This field is required.';
    if (v.typeMismatch) return el.type === 'email' ? 'Please enter a valid email address.' : el.type === 'url' ? 'Enter a valid URL (including https://).' : 'Enter a valid value.';
    if (v.rangeUnderflow) return 'Must be at least ' + el.min + '.';
    if (v.rangeOverflow) return 'Must be at most ' + el.max + '.';
    if (v.tooLong) return 'Too long.';
    if (v.tooShort) return 'Must be at least ' + el.minLength + ' characters.';
    if (v.patternMismatch) return el.title || 'Invalid format.';
    return 'Invalid value.';
  }
  MH.setFieldError = function (el, msg) {
    var wrap = el.closest('.field'); if (!wrap) return;
    wrap.classList.toggle('invalid', !!msg);
    if (msg) el.setAttribute('aria-invalid', 'true'); else el.removeAttribute('aria-invalid');
    var e = wrap.querySelector('.field-error'); if (e) e.textContent = msg || '';
  };
  function specFrom(el) {
    if (el._spec) return el._spec;
    if (!el.dataset || !el.dataset.spec) return null;
    try { el._spec = JSON.parse(el.dataset.spec); } catch (e) { el._spec = null; }
    return el._spec;
  }
  // Gives hand-written forms (register, checkout, ...) the same rules as MH.field forms.
  MH.attachSpecs = function (form, specs) {
    Object.keys(specs).forEach(function (name) {
      var el = form.elements[name]; if (!el || el.length) return;
      el._spec = specs[name]; el.dataset.spec = JSON.stringify(specs[name]);
    });
  };
  // message for one control ('' when it is fine)
  MH.checkControl = function (el) {
    if (!el || el.disabled || el.type === 'hidden' || el.type === 'radio' || el.type === 'checkbox' || el.type === 'submit' || el.type === 'button') return '';
    var spec = specFrom(el);
    if (spec) {
      if (el.type === 'file') return MH.v.check(spec, '', { file: el.files && el.files[0] });
      return MH.v.check(spec, el.value, { badInput: !!(el.validity && el.validity.badInput) });
    }
    // controls without a spec: required + browser type checks
    if (el.type !== 'file' && el.required && el.value.trim() === '') return 'This field is required.';
    el.setCustomValidity('');
    return el.checkValidity() ? '' : msgFor(el);
  };
  function controlsOf(form) {
    return Array.prototype.filter.call(form.querySelectorAll('input,select,textarea'), function (el) { return el.name; });
  }
  /**
   * Validates every control of the form and shows the messages next to the fields.
   * opts.cross(values) may return { fieldName: message } for rules that involve several fields
   * (end date after start date, passwords match, ...).
   * Returns true when the form may be submitted.
   */
  MH.validate = function (form, opts) {
    opts = opts || {};
    if (opts.cross) form._mhCross = opts.cross;
    var ok = true, first = null;
    controlsOf(form).forEach(function (el) {
      var msg = MH.checkControl(el);
      MH.setFieldError(el, msg);
      if (msg) { ok = false; first = first || el; }
    });
    var cross = opts.cross || form._mhCross;
    if (cross) {
      var errs = cross(MH.readForm(form)) || {};
      Object.keys(errs).forEach(function (name) {
        var el = form.elements[name];
        if (!el || el.length || !errs[name]) return;
        var wrap = el.closest('.field');
        if (wrap && wrap.classList.contains('invalid')) return; // its own error is more basic
        MH.setFieldError(el, errs[name]); ok = false; first = first || el;
      });
    }
    if (!ok) {
      if (!opts.silent) MH.toast('Please correct the highlighted fields.', 'error');
      if (first) {
        var pick = first.type === 'file' && first.closest('[data-upload-wrap]');
        ((pick && pick.querySelector('[data-upload-choose]')) || first).focus();
      }
    }
    return ok;
  };
  // Same as validate, then also checks the real content of chosen files (a renamed file keeps a fake extension).
  MH.validateAsync = async function (form, opts) {
    if (!MH.validate(form, opts)) return false;
    var files = form.querySelectorAll('input[type="file"]');
    for (var i = 0; i < files.length; i++) {
      var el = files[i], spec = specFrom(el), file = el.files && el.files[0];
      if (!spec || !file || el.disabled) continue;
      var what = spec.what || 'image';
      if (!(await MH.v.sniff(file, what))) {
        MH.setFieldError(el, spec.typeMsg || MH.v.FILE_KINDS[what].type);
        MH.toast('Please correct the highlighted fields.', 'error');
        return false;
      }
    }
    return true;
  };
  MH.readForm = function (form) {
    var out = {};
    form.querySelectorAll('input,select,textarea').forEach(function (el) {
      if (!el.name || el.type === 'file') return;
      if (el.type === 'radio') { if (el.checked) out[el.name] = el.value; return; }
      out[el.name] = el.type === 'checkbox' ? el.checked : el.value.trim();
    });
    return out;
  };

  // An error disappears as soon as the user corrects the value (and stays visible while it is still wrong).
  function liveRevalidate(e) {
    var el = e.target;
    if (!el || !el.closest || el.type === 'file') return; // file inputs report their own errors (bindFileFields)
    var form = el.closest('form');
    if (!form || !form.querySelector('.field.invalid')) return;
    var cross = form._mhCross, errs = cross ? (cross(MH.readForm(form)) || {}) : {};
    form.querySelectorAll('.field.invalid').forEach(function (wrap) {
      var c = wrap.querySelector('input,select,textarea');
      if (!c || !c.name || c.type === 'file') return;
      var msg = specFrom(c) ? (MH.checkControl(c) || errs[c.name] || '') : (c === el ? MH.checkControl(c) : null);
      if (msg !== null) MH.setFieldError(c, msg);
    });
  }
  document.addEventListener('input', liveRevalidate);
  document.addEventListener('change', liveRevalidate);

  // ---------------- cart (customer) ----------------
  MH.cart = {
    refresh: async function () {
      if (MH.auth.getRole() !== 'CUSTOMER') return 0;
      try {
        var c = await MH.api.get('/cart');
        var n = (c.items || []).reduce(function (s, i) { return s + i.quantity; }, 0);
        document.querySelectorAll('.cart-count').forEach(function (b) { b.textContent = n > 99 ? '99+' : n; b.classList.toggle('hidden', n === 0); });
        return n;
      } catch (e) { return 0; }
    },
    add: async function (productId, qty, btn) {
      if (!MH.auth.isLoggedIn()) { location.href = MH.auth.loginUrl(); return false; }
      if (MH.auth.getRole() !== 'CUSTOMER') { MH.toast('Staff accounts cannot shop. Log in with a customer account to order.', 'info'); return false; }
      MH.busy(btn, true);
      try {
        await MH.api.post('/cart/items', { productId: Number(productId), quantity: qty || 1 });
        MH.ok('Added to your cart.');
        MH.cart.refresh();
        return true;
      } catch (e) { MH.fail(e); return false; }
      finally { MH.busy(btn, false); }
    }
  };

  // ---------------- promotions, ratings, product card ----------------
  // map productId -> best running promotion
  MH.loadPromoMap = async function () {
    var map = {};
    try {
      (await MH.api.get('/promotions/active', { auth: false })).forEach(function (p) {
        if (!map[p.productId] || p.discountedPrice < map[p.productId].discountedPrice) map[p.productId] = p;
      });
    } catch (e) { /* promotions are optional on the store front */ }
    return map;
  };
  // map productId -> { averageRating, reviewCount }
  MH.loadRatingMap = async function () {
    var map = {};
    try { (await MH.api.get('/reviews/summary', { auth: false })).forEach(function (r) { map[r.productId] = r; }); } catch (e) { /* ratings are optional */ }
    return map;
  };
  // "-25%" for a percentage promotion, "-LKR 500.00" for a fixed amount
  MH.promoTag = function (promo) {
    return promo.discountAmount > 0 ? '−' + MH.money(promo.discountAmount) : '−' + Math.round(promo.discountPercentage) + '%';
  };
  MH.priceHtml = function (p, promo) {
    if (promo) return '<div class="price"><strong>' + MH.money(promo.discountedPrice) + '</strong><s>' + MH.money(p.price) + '</s></div>';
    return '<div class="price"><strong>' + MH.money(p.price) + '</strong></div>';
  };
  // artist card used on the home page, the artists list and artist profiles; links to the profile page
  MH.artistCard = function (a, opts) {
    opts = opts || {};
    var meta = [a.genre, a.country].filter(Boolean).join(', ');
    var bio = opts.bio && a.bio ? '<p class="artist-card-bio">' + esc(a.bio.length > 110 ? a.bio.slice(0, 110).trim() + '…' : a.bio) + '</p>' : '';
    return '<a class="artist-card" href="' + MH.url('artists/profile/') + '?id=' + encodeURIComponent(a.artistId) + '" aria-label="' + esc(a.artistName) + ', view profile">' +
      '<div class="artist-photo">' + MH.img(a.imageUrl, a.artistName, '', 'artist') + '</div>' +
      '<div class="body"><h3>' + esc(a.artistName) + '</h3>' + (meta ? '<div class="meta">' + esc(meta) + '</div>' : '') + bio +
      (opts.albums != null ? '<div class="artist-card-foot">' + MH.icon('disc', 15) + MH.plural(opts.albums, 'album') + '</div>' : '') + '</div></a>';
  };

  MH.productCard = function (p, promo, rating) {
    var av = MH.availability(p), href = MH.url('products/details/') + '?id=' + p.productId;
    return '<article class="product-card" data-id="' + p.productId + '">' +
      '<a class="product-media" href="' + href + '" tabindex="-1" aria-hidden="true">' + MH.img(p.imageUrl, p.name, '', p.categoryName) +
      (promo ? '<span class="deal-tag">' + MH.promoTag(promo) + '</span>' : '') +
      (av.tone !== 'ok' ? '<span class="tag badge badge-' + av.tone + '">' + esc(av.text) + '</span>' : '') + '</a>' +
      '<div class="product-body"><span class="product-cat">' + esc(p.categoryName || '') + '</span>' +
      '<a class="product-name" href="' + href + '">' + esc(p.name) + '</a>' +
      (rating && rating.reviewCount ? MH.stars(rating.averageRating, rating.reviewCount) : '') +
      MH.priceHtml(p, promo) +
      '<div class="product-actions"><button type="button" class="btn btn-sm btn-primary" data-act="add"' + (av.buyable ? '' : ' disabled') + '>' + (av.buyable ? MH.icon('cart', 15) + ' Add to cart' : esc(av.text)) + '</button></div></div></article>';
  };
  MH.bindProductCards = function (root) {
    root.addEventListener('click', function (e) {
      var b = e.target.closest('[data-act="add"]'); if (!b) return;
      var card = b.closest('[data-id]'); MH.cart.add(card.dataset.id, 1, b);
    });
  };

  // ---------------- storefront header / footer ----------------
  MH.renderHeader = function (active) {
    var user = MH.auth.getCurrentUser(), isCustomer = user && user.role === 'CUSTOMER';
    var links = [['home', 'Home', 'index.html'], ['products', 'Shop', 'products/'], ['categories', 'Categories', 'categories/'], ['artists', 'Artists', 'artists/'], ['about', 'About', 'about/'], ['contact', 'Contact', 'contact/']];
    var nav = links.map(function (l) { return '<a href="' + MH.url(l[2]) + '"' + (active === l[0] ? ' class="active" aria-current="page"' : '') + '>' + l[1] + '</a>'; }).join('');
    var right;
    if (!user) {
      right = '<a class="btn btn-sm btn-ghost" href="' + MH.url('auth/login/') + '">Log in</a><a class="btn btn-sm btn-primary" href="' + MH.url('auth/register/') + '">Create account</a>';
    } else {
      var link = function (icon, label, path) { return '<a href="' + MH.url(path) + '">' + MH.icon(icon, 16) + esc(label) + '</a>'; };
      var menu = isCustomer
        ? link('home', 'My account', 'customer/dashboard/') + link('clipboard', 'My orders', 'customer/orders/') + link('music', 'Digital library', 'customer/music/') + link('star', 'My reviews', 'customer/reviews/') + link('shield', 'Warranty claims', 'customer/warranty-claims/') + link('user', 'Profile', 'customer/profile/')
        : '<a href="' + MH.auth.homeFor(user.role) + '">' + MH.icon('chart', 16) + 'Open dashboard</a>' + link('user', 'Profile', 'admin/profile/');
      right = (isCustomer ? '<a class="icon-btn cart-link" href="' + MH.url('customer/cart/') + '" aria-label="Shopping cart">' + MH.icon('cart', 22) + '<span class="cart-count hidden">0</span></a>' : '') +
        '<div class="user-menu"><button type="button" class="user-trigger" aria-haspopup="true" aria-expanded="false"><span class="avatar">' + esc(MH.initials(user.name)) + '</span><span class="uname">' + esc(user.name.split(' ')[0]) + '</span></button>' +
        '<div class="dropdown" role="menu"><div class="who"><strong>' + esc(user.name) + '</strong><small>' + esc(user.email) + '</small></div>' + menu +
        '<div class="sep"></div><button type="button" data-logout>' + MH.icon('logout', 16) + 'Log out</button></div></div>';
    }
    var host = document.getElementById('site-header');
    host.className = 'site-header';
    host.innerHTML = '<div class="container bar">' + MH.logo() + '<nav class="main-nav" id="main-nav" aria-label="Main">' + nav + '</nav>' +
      '<form class="header-search" role="search" action="' + MH.url('products/') + '"><label class="sr-only" for="hs">Search products</label>' + MH.icon('search', 16) + '<input class="input" id="hs" name="q" type="search" placeholder="Search guitars, vinyl, keyboards…" autocomplete="off"></form>' +
      '<div class="header-actions">' + right + '<button type="button" class="icon-btn menu-toggle" aria-label="Menu" aria-controls="main-nav" aria-expanded="false">' + MH.icon('menu', 22) + '</button></div></div>';
    var toggle = host.querySelector('.menu-toggle');
    toggle.onclick = function () { var o = host.querySelector('#main-nav').classList.toggle('open'); toggle.setAttribute('aria-expanded', o); };
    var hs = host.querySelector('#hs'); if (hs && MH.qs('q') && active === 'products') hs.value = MH.qs('q');
    var trig = host.querySelector('.user-trigger');
    if (trig) {
      var dd = host.querySelector('.dropdown');
      trig.onclick = function (e) { e.stopPropagation(); var o = dd.classList.toggle('open'); trig.setAttribute('aria-expanded', o); };
      document.addEventListener('click', function (e) { if (!dd.contains(e.target)) { dd.classList.remove('open'); trig.setAttribute('aria-expanded', 'false'); } });
      document.addEventListener('keydown', function (e) { if (e.key === 'Escape') { dd.classList.remove('open'); trig.setAttribute('aria-expanded', 'false'); } });
      host.querySelector('[data-logout]').onclick = function () { MH.auth.logout(); };
    }
    if (isCustomer) MH.cart.refresh();
  };
  MH.renderFooter = function () {
    var u = MH.url;
    var host = document.getElementById('site-footer'); host.className = 'site-footer';
    host.innerHTML = '<div class="container"><div class="footer-grid">' +
      '<div>' + MH.logo() + '<p class="muted" style="margin-top:16px;max-width:36ch">Instruments, records and digital music, checked before they ship and delivered across Sri Lanka.</p></div>' +
      '<div><h4>Shop</h4><ul><li><a href="' + u('products/') + '">All products</a></li><li><a href="' + u('products/') + '?deals=1">Offers</a></li><li><a href="' + u('categories/') + '">Categories</a></li><li><a href="' + u('artists/') + '">Artists</a></li></ul></div>' +
      '<div><h4>Help</h4><ul><li><a href="' + u('customer/orders/') + '">Track an order</a></li><li><a href="' + u('customer/warranty-claims/') + '">Warranty and returns</a></li><li><a href="' + u('about/') + '">About us</a></li><li><a href="' + u('contact/') + '">Contact</a></li></ul></div>' +
      '<div><h4>Visit or call</h4><ul class="muted"><li>Colombo, Sri Lanka</li><li>+94 11 000 0000</li><li>support@melodyhub.example</li><li>Monday to Saturday, 9:00 to 18:00</li></ul></div>' +
      '</div><div class="footer-bottom"><span>© ' + new Date().getFullYear() + ' MelodyHub</span><span>Prices in Sri Lankan rupees (LKR)</span></div></div>';
  };

  // ---------------- portal shell (customer + staff) ----------------
  // id, label, icon, path, roles that can open the page, sidebar group
  var A = 'admin/', C = 'customer/';
  var STAFF = ['ADMIN', 'INVENTORY_MANAGER', 'ARTIST', 'MARKETING_MANAGER', 'SALES_MANAGER', 'SUPPLIER'];
  MH.MODULES = [
    { id: 'dashboard', label: 'Dashboard', icon: 'chart', path: A + 'dashboard/', roles: STAFF, group: 'Overview' },
    // Sales
    { id: 'orders', label: 'Orders', icon: 'clipboard', path: A + 'orders/', roles: ['ADMIN', 'SALES_MANAGER'], group: 'Sales' },
    { id: 'order-items', label: 'Order items', icon: 'list', path: A + 'order-items/', roles: ['ADMIN', 'SALES_MANAGER'], group: 'Sales' },
    { id: 'payments', label: 'Payments', icon: 'card', path: A + 'payments/', roles: ['ADMIN', 'SALES_MANAGER'], group: 'Sales' },
    // Catalog
    { id: 'products', label: 'Products', icon: 'box', path: A + 'products/', roles: ['ADMIN', 'INVENTORY_MANAGER', 'MARKETING_MANAGER', 'SALES_MANAGER'], group: 'Catalog' },
    { id: 'categories', label: 'Categories', icon: 'layers', path: A + 'categories/', roles: ['ADMIN', 'INVENTORY_MANAGER', 'MARKETING_MANAGER'], group: 'Catalog' },
    { id: 'inventory', label: 'Inventory', icon: 'warehouse', path: A + 'inventory/', roles: ['ADMIN', 'INVENTORY_MANAGER'], group: 'Catalog' },
    { id: 'physical-products', label: 'Shipping details', icon: 'truck', path: A + 'physical-products/', roles: ['ADMIN', 'INVENTORY_MANAGER'], group: 'Catalog' },
    // Music
    { id: 'songs', label: 'Songs', icon: 'music', path: A + 'songs/', roles: ['ADMIN', 'ARTIST'], group: 'Music' },
    { id: 'digital-music', label: 'Digital music', icon: 'download', path: A + 'digital-music/', roles: ['ADMIN', 'ARTIST'], group: 'Music' },
    { id: 'artists', label: 'Artists', icon: 'mic', path: A + 'artists/', roles: ['ADMIN', 'ARTIST'], group: 'Music' },
    { id: 'albums', label: 'Albums', icon: 'disc', path: A + 'albums/', roles: ['ADMIN', 'ARTIST'], group: 'Music' },
    // Marketing
    { id: 'advertisements', label: 'Advertisements', icon: 'image', path: A + 'advertisements/', roles: ['ADMIN', 'MARKETING_MANAGER'], group: 'Marketing' },
    { id: 'campaigns', label: 'Campaigns', icon: 'megaphone', path: A + 'campaigns/', roles: ['ADMIN', 'MARKETING_MANAGER'], group: 'Marketing' },
    { id: 'promotions', label: 'Promotions', icon: 'tag', path: A + 'promotions/', roles: ['ADMIN', 'MARKETING_MANAGER'], group: 'Marketing' },
    { id: 'discounts', label: 'Discounts', icon: 'percent', path: A + 'discounts/', roles: ['ADMIN', 'MARKETING_MANAGER'], group: 'Marketing' },
    { id: 'featured-products', label: 'Featured products', icon: 'star', path: A + 'featured-products/', roles: ['ADMIN', 'MARKETING_MANAGER'], group: 'Marketing' },
    // Customers
    { id: 'users', label: 'Users', icon: 'users', path: A + 'users/', roles: ['ADMIN'], group: 'Customers' },
    { id: 'reviews', label: 'Feedback / reviews', icon: 'message', path: A + 'reviews/', roles: ['ADMIN', 'MARKETING_MANAGER'], group: 'Customers' },
    // Suppliers
    { id: 'suppliers', label: 'Suppliers', icon: 'handshake', path: A + 'suppliers/', roles: ['ADMIN'], group: 'Suppliers' },
    { id: 'purchase-orders', label: 'Purchase orders', icon: 'file', path: A + 'purchase-orders/', roles: ['ADMIN'], group: 'Suppliers' },
    { id: 'supplier-deliveries', label: 'Supplier deliveries', icon: 'truck', path: A + 'supplier-deliveries/', roles: ['ADMIN'], group: 'Suppliers' },
    { id: 'supplier-contracts', label: 'Supplier contracts', icon: 'file', path: A + 'supplier-contracts/', roles: ['ADMIN'], group: 'Suppliers' },
    // After sales
    { id: 'warranties', label: 'Warranty', icon: 'shield', path: A + 'warranties/', roles: ['ADMIN', 'SALES_MANAGER'], group: 'After sales' },
    { id: 'warranty-claims', label: 'Warranty claims', icon: 'clipboard', path: A + 'warranty-claims/', roles: ['ADMIN', 'SALES_MANAGER'], group: 'After sales' },
    { id: 'returns', label: 'Returns', icon: 'returns', path: A + 'returns/', roles: ['ADMIN', 'SALES_MANAGER'], group: 'After sales' },
    // Account
    { id: 'profile', label: 'My profile', icon: 'user', path: A + 'profile/', roles: STAFF, group: 'Account' },
    // customer area
    { id: 'c-dashboard', label: 'Overview', icon: 'home', path: C + 'dashboard/', roles: ['CUSTOMER'], group: 'My account' },
    { id: 'c-cart', label: 'Cart', icon: 'cart', path: C + 'cart/', roles: ['CUSTOMER'], group: 'Shopping' },
    { id: 'c-orders', label: 'My orders', icon: 'clipboard', path: C + 'orders/', roles: ['CUSTOMER'], group: 'Shopping' },
    { id: 'c-payments', label: 'Payments', icon: 'card', path: C + 'payments/', roles: ['CUSTOMER'], group: 'Shopping' },
    { id: 'c-music', label: 'Digital library', icon: 'music', path: C + 'music/', roles: ['CUSTOMER'], group: 'Music' },
    { id: 'c-reviews', label: 'My reviews', icon: 'star', path: C + 'reviews/', roles: ['CUSTOMER'], group: 'Support' },
    { id: 'c-claims', label: 'Warranty claims', icon: 'shield', path: C + 'warranty-claims/', roles: ['CUSTOMER'], group: 'Support' },
    { id: 'c-profile', label: 'Profile', icon: 'user', path: C + 'profile/', roles: ['CUSTOMER'], group: 'My account' }
  ];
  MH.canOpen = function (id) {
    var m = MH.MODULES.filter(function (x) { return x.id === id; })[0];
    return !!m && m.roles.indexOf(MH.auth.getRole()) >= 0;
  };

  /**
   * Builds the sidebar + top bar around #portal-content.
   * opts: { active, title, subtitle, actionsHtml }
   */
  MH.mountPortal = function (opts) {
    var user = MH.auth.getCurrentUser(), role = user.role;
    var groups = {}, order = [], activeModule = null;
    MH.MODULES.forEach(function (m) {
      if (m.roles.indexOf(role) < 0) return;
      if (!groups[m.group]) { groups[m.group] = []; order.push(m.group); }
      groups[m.group].push(m);
      if (m.id === opts.active) activeModule = m;
    });
    var side = order.map(function (g) {
      var single = groups[g].length === 1 && (g === 'Overview');
      return '<div class="side-group">' + (single ? '' : '<h5>' + esc(g) + '</h5>') + groups[g].map(function (m) {
        var on = m.id === opts.active;
        return '<a class="side-link' + (on ? ' active' : '') + '" href="' + MH.url(m.path) + '"' + (on ? ' aria-current="page"' : '') + '>' + MH.icon(m.icon) + '<span>' + esc(m.label) + '</span>' + (on ? MH.eqMark : '') + '</a>';
      }).join('') + '</div>';
    }).join('');
    var home = role === 'CUSTOMER' ? ['My account', MH.url('customer/dashboard/')] : ['Dashboard', MH.url('admin/dashboard/')];
    var crumbs = opts.active === 'dashboard' || opts.active === 'c-dashboard' ? null
      : [home].concat(activeModule && activeModule.group !== 'My account' && activeModule.group !== 'Overview' ? [[activeModule.group]] : []).concat([[opts.title]]);

    var root = document.getElementById('portal');
    var content = root.innerHTML;
    root.className = 'portal';
    root.innerHTML = '<aside class="sidebar" id="sidebar" aria-label="Sections">' + MH.logo() + '<nav>' + side + '</nav>' +
      '<div class="side-foot"><div class="side-user"><span class="avatar">' + esc(MH.initials(user.name)) + '</span><div><strong>' + esc(user.name) + '</strong><small>' + esc(MH.humanize(role)) + '</small></div></div>' +
      '<div class="side-actions"><a class="btn btn-sm" href="' + MH.url('index.html') + '">' + MH.icon('store', 15) + ' Store</a>' +
      '<button type="button" class="btn btn-sm" data-logout>' + MH.icon('logout', 15) + ' Log out</button></div></div></aside>' +
      '<div class="scrim" id="scrim"></div>' +
      '<main class="portal-main"><header class="topbar"><button type="button" class="icon-btn drawer-btn" aria-label="Open menu" aria-controls="sidebar" aria-expanded="false">' + MH.icon('menu', 22) + '</button>' +
      '<div class="titles">' + (crumbs ? MH.breadcrumbs(crumbs) : '') + '<h1>' + esc(opts.title) + '</h1>' + (opts.subtitle ? '<p>' + esc(opts.subtitle) + '</p>' : '') + '</div><div class="row" id="portal-actions">' + (opts.actionsHtml || '') + '</div></header>' +
      '<div class="portal-content" id="portal-content">' + content + '</div></main>';
    var sb = document.getElementById('sidebar'), sc = document.getElementById('scrim'), db = root.querySelector('.drawer-btn');
    function toggle(o) { sb.classList.toggle('open', o); sc.classList.toggle('open', o); db.setAttribute('aria-expanded', o); }
    db.onclick = function () { toggle(true); };
    sc.onclick = function () { toggle(false); };
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') toggle(false); });
    root.querySelector('[data-logout]').onclick = function () { MH.auth.logout(); };
    var tb = root.querySelector('.topbar');
    window.addEventListener('scroll', function () { tb.classList.toggle('scrolled', window.scrollY > 4); }, { passive: true });
    var activeLink = sb.querySelector('.side-link.active');
    if (activeLink && activeLink.scrollIntoView) activeLink.scrollIntoView({ block: 'nearest' });
    document.title = opts.title + ' · MelodyHub';
    if (role === 'CUSTOMER') MH.cart.refresh();
    return document.getElementById('portal-content');
  };
})(window.MH);
