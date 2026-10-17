/*
 * MelodyHub CRUD page engine.
 * One reusable component drives every staff management page, so each module
 * only describes WHAT it manages (endpoint, columns, form fields, who may do what).
 * All data comes from the real REST API; nothing is stored in the browser.
 *
 * Image fields (type: 'file') are sent together with the record as multipart/form-data:
 *   part "data"  = the JSON record (plus removeImage: true when the picture was removed)
 *   part "image" = the chosen file (optional)
 * so a picture is only stored when the record is saved, and replaced pictures are deleted
 * by the server (no orphan files).
 */
(function (MH) {
  var esc = MH.esc, PAGE_SIZE = 10;

  function cell(col, row, ctx) {
    if (col.render) return col.render(row, ctx);
    var v = row[col.key];
    switch (col.type) {
      case 'money': return MH.money(v);
      case 'date': return MH.date(v);
      case 'datetime': return MH.dateTime(v);
      case 'badge': return MH.badge(v);
      case 'image': return MH.img(col.fallbackKey ? v || row[col.fallbackKey] : v, row[col.alt || 'name'], 'thumb' + (col.round ? ' round' : ''), col.hint || (row.artistName && !row.categoryName ? 'artist' : row.categoryName));
      // photo + name, e.g. the artist of an album, song or MP3
      case 'avatar': return v == null || v === '' ? '<span class="faint">—</span>' : MH.personChip(row[col.imageKey], v, col.hint);
      case 'number': return v == null ? '<span class="faint">—</span>' : esc(v);
      default: return v == null || v === '' ? '<span class="faint">—</span>' : '<span class="clip" title="' + esc(v) + '">' + esc(v) + '</span>';
    }
  }

  MH.crud = function (cfg) {
    var role = MH.auth.getRole();
    if (!MH.guards.requireRole(cfg.viewRoles)) return;

    var can = function (list) { return !list || list.indexOf(role) >= 0; };
    var canWrite = can(cfg.writeRoles), canDelete = cfg.deleteRoles ? can(cfg.deleteRoles) : canWrite;
    var canCreate = cfg.noCreate ? false : canWrite;

    var state = { ctx: {}, rows: [], query: MH.qs('q') || '', filters: Object.assign({}, cfg.presetFilters || {}), page: 1, sort: cfg.defaultSort || null, dir: 'desc' };
    var optionCache = {};

    var addBtn = canCreate ? '<button type="button" class="btn btn-primary" id="crud-add">' + MH.icon('plus', 16) + ' ' + esc(cfg.createLabel || 'Add ' + cfg.singular.toLowerCase()) + '</button>' : '';
    var content = MH.mountPortal({ active: cfg.module, title: cfg.title, subtitle: cfg.subtitle, actionsHtml: (cfg.extraActionsHtml || '') + addBtn });
    content.innerHTML = (!canWrite && cfg.readOnlyNote !== false ? '<div class="alert alert-info">' + MH.icon('info', 16) + '<span>You have view-only access to ' + esc(cfg.title.toLowerCase()) + '.</span></div>' : '') +
      (cfg.introHtml || '') + '<div id="crud-body"></div>';
    var body = document.getElementById('crud-body');
    if (canCreate) document.getElementById('crud-add').onclick = function () { openForm(null); };
    if (cfg.onMounted) cfg.onMounted({ reload: load });

    // ---------- loading ----------
    async function load() {
      if (!state.rows.length) body.innerHTML = MH.skeletonRows(6);
      try {
        var loaded = await Promise.all([MH.api.get(cfg.endpoint), cfg.preload ? cfg.preload() : Promise.resolve({})]);
        state.rows = loaded[0] || [];
        state.ctx = loaded[1] || {};
        if (!Array.isArray(state.rows)) state.rows = [state.rows];
        if (cfg.rowFilter) state.rows = state.rows.filter(cfg.rowFilter);
        render();
      } catch (e) {
        body.innerHTML = '<div class="card">' + MH.errorState(e.message, 'crud-retry') + '</div>';
        document.getElementById('crud-retry').onclick = load;
      }
    }

    function visibleRows() {
      var q = state.query.trim().toLowerCase();
      var rows = state.rows.filter(function (r) {
        if (q) {
          var keys = cfg.searchKeys || cfg.columns.map(function (c) { return c.key; });
          if (!keys.some(function (k) { return String(r[k] == null ? '' : r[k]).toLowerCase().indexOf(q) >= 0; })) return false;
        }
        return (cfg.filters || []).every(function (f) {
          var sel = state.filters[f.key];
          return !sel || String(r[f.key]) === sel;
        });
      });
      if (state.sort) {
        rows = rows.slice().sort(function (a, b) {
          var x = a[state.sort], y = b[state.sort];
          if (x == null && y == null) return 0; if (x == null) return 1; if (y == null) return -1;
          var r = typeof x === 'number' && typeof y === 'number' ? x - y : String(x).localeCompare(String(y), undefined, { numeric: true, sensitivity: 'base' });
          return state.dir === 'asc' ? r : -r;
        });
      }
      return rows;
    }

    function actionsFor(row) {
      var h = '';
      (cfg.rowActions || []).forEach(function (a, i) {
        if ((a.roles && !can(a.roles)) || (a.show && !a.show(row))) return;
        h += '<button type="button" class="btn btn-sm ' + (a.cls || '') + '" data-act="x' + i + '">' + esc(a.label) + '</button>';
      });
      h += '<button type="button" class="btn btn-sm btn-icon" data-act="view" title="View details" aria-label="View details">' + MH.icon('eye', 15) + '</button>';
      if (canWrite && !(cfg.canEdit && !cfg.canEdit(row))) h += '<button type="button" class="btn btn-sm btn-icon" data-act="edit" title="Edit" aria-label="Edit">' + MH.icon('edit', 15) + '</button>';
      if (canDelete && !cfg.noDelete && !(cfg.canDeleteRow && !cfg.canDeleteRow(row))) h += '<button type="button" class="btn btn-sm btn-icon btn-danger" data-act="delete" title="Delete" aria-label="Delete">' + MH.icon('trash', 15) + '</button>';
      return h;
    }

    function render() {
      if (!state.rows.length) {
        body.innerHTML = '<div class="card">' + MH.empty({
          icon: cfg.icon || 'disc',
          title: (cfg.empty && cfg.empty.title) || ('No ' + cfg.title.toLowerCase() + ' yet'),
          text: canCreate ? ((cfg.empty && cfg.empty.text) || ('Add your first ' + cfg.singular.toLowerCase() + ' to get started.')) : ((cfg.empty && cfg.empty.readOnlyText) || 'Nothing has been added yet.'),
          actionHtml: canCreate ? '<button type="button" class="btn btn-primary" id="empty-add">' + MH.icon('plus', 16) + ' ' + esc(cfg.createLabel || 'Add ' + cfg.singular.toLowerCase()) + '</button>' : ''
        }) + '</div>';
        var b = document.getElementById('empty-add'); if (b) b.onclick = function () { openForm(null); };
        return;
      }
      var rows = visibleRows(), pages = Math.max(1, Math.ceil(rows.length / PAGE_SIZE));
      if (state.page > pages) state.page = pages;
      var slice = rows.slice((state.page - 1) * PAGE_SIZE, state.page * PAGE_SIZE);

      var filters = (cfg.filters || []).map(function (f) {
        var opts = f.options || Array.from(new Set(state.rows.map(function (r) { return r[f.key]; }).filter(function (v) { return v != null && v !== ''; }))).sort();
        return '<select class="select" data-filter="' + f.key + '" aria-label="Filter by ' + esc(f.label.toLowerCase()) + '"><option value="">' + esc(f.label) + ': all</option>' +
          opts.map(function (o) { return '<option value="' + esc(o) + '"' + (state.filters[f.key] === String(o) ? ' selected' : '') + '>' + esc(f.raw ? o : MH.humanize(o)) + '</option>'; }).join('') + '</select>';
      }).join('');

      var head = cfg.columns.map(function (c) {
        var sortable = c.sortable !== false && c.type !== 'image';
        var arrow = state.sort === c.key ? '<span class="arrow" aria-hidden="true">' + (state.dir === 'asc' ? '↑' : '↓') + '</span>' : '';
        return '<th' + (c.low ? ' data-low' : '') + (sortable ? ' class="sortable" data-sort="' + c.key + '" tabindex="0" aria-sort="' + (state.sort === c.key ? (state.dir === 'asc' ? 'ascending' : 'descending') : 'none') + '"' : '') + (c.type === 'money' || c.type === 'number' ? ' style="text-align:right"' : '') + '>' + esc(c.label) + arrow + '</th>';
      }).join('') + '<th><span class="sr-only">Actions</span></th>';

      var trs = slice.map(function (r) {
        return '<tr data-id="' + esc(r[cfg.idKey]) + '">' + cfg.columns.map(function (c, i) {
          return '<td' + (c.low ? ' data-low' : '') + ' data-label="' + esc(c.label) + '" class="' + (c.type === 'money' || c.type === 'number' ? 'num' : '') + (['money', 'date', 'datetime', 'badge', 'number'].indexOf(c.type) >= 0 ? ' nowrap' : '') + (i === (cfg.strongColumn || 0) ? ' strong' : '') + '">' + cell(c, r, state.ctx) + '</td>';
        }).join('') + '<td class="actions-cell"><div class="actions">' + actionsFor(r) + '</div></td></tr>';
      }).join('');

      var pg = '';
      for (var i = 1; i <= pages; i++) {
        if (pages <= 7 || Math.abs(i - state.page) <= 1 || i === 1 || i === pages) pg += '<button type="button" class="btn btn-sm' + (i === state.page ? ' btn-dark' : '') + '" data-page="' + i + '"' + (i === state.page ? ' aria-current="page"' : '') + '>' + i + '</button>';
        else if (Math.abs(i - state.page) === 2) pg += '<span class="faint" style="padding:0 4px">…</span>';
      }

      body.innerHTML = '<div class="card card-flush"><div class="toolbar"><div class="search-wrap">' + MH.icon('search', 16) + '<input class="input search" type="search" id="crud-q" placeholder="Search ' + esc(cfg.title.toLowerCase()) + '" value="' + esc(state.query) + '" aria-label="Search ' + esc(cfg.title.toLowerCase()) + '"></div>' + filters +
        '<span class="count">' + (rows.length === state.rows.length ? rows.length + ' ' + (rows.length === 1 ? cfg.singular.toLowerCase() : cfg.title.toLowerCase()) : rows.length + ' of ' + state.rows.length) + '</span></div>' +
        (slice.length ? '<div class="table-wrap"><table class="table stackable"><thead><tr>' + head + '</tr></thead><tbody>' + trs + '</tbody></table></div>' +
          (pages > 1 ? '<div class="pagination"><span>Showing ' + ((state.page - 1) * PAGE_SIZE + 1) + ' to ' + Math.min(state.page * PAGE_SIZE, rows.length) + ' of ' + rows.length + '</span><div class="pages">' + pg + '</div></div>' : '')
          : MH.empty({ icon: 'search', title: 'No matches', text: 'Try a different search or clear the filters.' })) + '</div>';

      var q = document.getElementById('crud-q');
      q.oninput = MH.debounce(function () { state.query = q.value; state.page = 1; render(); var n = document.getElementById('crud-q'); n.focus(); n.setSelectionRange(n.value.length, n.value.length); }, 220);
      body.querySelectorAll('[data-filter]').forEach(function (s) { s.onchange = function () { state.filters[s.dataset.filter] = s.value; state.page = 1; render(); }; });
      body.querySelectorAll('[data-sort]').forEach(function (th) {
        function sort() { var k = th.dataset.sort; state.dir = state.sort === k && state.dir === 'asc' ? 'desc' : 'asc'; state.sort = k; render(); }
        th.onclick = sort; th.onkeydown = function (e) { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); sort(); } };
      });
      body.querySelectorAll('[data-page]').forEach(function (b) { b.onclick = function () { state.page = Number(b.dataset.page); render(); body.scrollIntoView({ block: 'start', behavior: 'smooth' }); }; });
      body.querySelectorAll('tbody tr').forEach(function (tr) {
        var row = state.rows.filter(function (r) { return String(r[cfg.idKey]) === tr.dataset.id; })[0];
        tr.querySelectorAll('[data-act]').forEach(function (b) {
          b.onclick = function () {
            var a = b.dataset.act;
            if (a === 'view') { if (cfg.onView) cfg.onView(row, { reload: load, ctx: state.ctx }); else openView(row); } else if (a === 'edit') openForm(row); else if (a === 'delete') remove(row);
            else cfg.rowActions[Number(a.slice(1))].run(row, { reload: load, button: b, ctx: state.ctx });
          };
        });
      });
    }

    // ---------- view ----------
    function openView(row) {
      var items = (cfg.viewFields || cfg.columns).map(function (c) {
        var v;
        if (c.type === 'image') v = MH.img(c.fallbackKey ? row[c.key] || row[c.fallbackKey] : row[c.key], row[c.alt || 'name'], 'thumb' + (c.round ? ' round' : ''), c.hint || (row.artistName && !row.categoryName ? 'artist' : row.categoryName));
        else if (c.render) v = c.render(row, state.ctx);
        else if (c.type) v = cell(c, row, state.ctx);
        else v = row[c.key] == null || row[c.key] === '' ? '<span class="faint">—</span>' : esc(row[c.key]);
        return '<dt>' + esc(c.label) + '</dt><dd>' + v + '</dd>';
      }).join('');
      MH.modal({ title: cfg.singular + ' details', wide: true, body: '<dl class="kv">' + items + '</dl>', footer: [{ label: 'Close' }] });
    }

    // ---------- create / edit ----------
    async function fieldOptions(f) {
      if (!f.optionsFrom) return f.options;
      var key = f.optionsFrom.endpoint;
      if (!optionCache[key]) {
        var data = await MH.api.get(key);
        optionCache[key] = data.map(function (d) {
          return { value: d[f.optionsFrom.value], label: typeof f.optionsFrom.label === 'function' ? f.optionsFrom.label(d) : d[f.optionsFrom.label] };
        });
      }
      return optionCache[key];
    }

    async function openForm(row) {
      if (!row && cfg.customCreate) { cfg.customCreate({ reload: load, ctx: state.ctx }); return; }
      var editing = !!row, fields;
      optionCache = {}; // pick up records created since the last form was opened
      try {
        fields = await Promise.all(cfg.fields.map(async function (f) {
          if (f.hideOnEdit && editing) return null;
          if (f.hideOnCreate && !editing) return null;
          var copy = Object.assign({}, f);
          copy.options = await fieldOptions(f);
          if (f.requiredOnCreate) copy.required = !editing;
          if (f.lockOnEdit && editing) copy.disabled = true;
          return copy;
        }));
      } catch (e) { MH.fail(e); return; }
      fields = fields.filter(Boolean);
      var values = editing ? (cfg.toForm ? cfg.toForm(row) : row) : (cfg.defaults || {});
      var hasFile = fields.some(function (f) { return f.type === 'file'; });

      var form = document.createElement('form');
      form.noValidate = true;
      form.innerHTML = '<div id="form-error"></div><div class="form-grid">' + fields.map(function (f) {
        var v = values[f.name];
        if ((v == null || v === '') && f.existingValueKey) v = values[f.existingValueKey];
        return MH.field(f, v == null ? '' : v);
      }).join('') + '</div><button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Save</button>';
      MH.bindFileFields(form);
      var saving = false;
      form.onsubmit = function (e) { e.preventDefault(); save(); };
      var cross = cfg.validate ? function (vals) { return cfg.validate(vals, row, editing, state.ctx) || {}; } : null;
      if (cross) form._mhCross = cross;

      var ctl = MH.modal({
        title: (editing ? 'Edit ' : 'Add ') + cfg.singular.toLowerCase(), wide: fields.length > 5 || hasFile, body: form, keepOpen: true,
        footer: [{ label: 'Cancel' }, { label: editing ? 'Save changes' : 'Save ' + cfg.singular.toLowerCase(), cls: 'btn-primary', onClick: function () { save(); } }]
      });
      var saveBtn = ctl.btn(1), errBox = form.querySelector('#form-error');

      async function save() {
        if (saving) return; // Enter + click, or a double click: only one request
        saving = true;
        try {
          // 1) validate everything BEFORE any request is sent; the user's input stays in the form
          if (!(await MH.validateAsync(form, { cross: cross }))) return;
          errBox.innerHTML = '';
          MH.busy(saveBtn, true, 'Saving…');

          var raw = MH.readForm(form);
          // disabled controls (locked on edit) are not submitted, so add them back
          fields.forEach(function (f) { if (f.disabled && row) raw[f.name] = row[f.name]; });
          var payload = {}, files = {};
          fields.forEach(function (f) {
            if (f.type === 'file') {
              var input = form.querySelector('[name="' + f.name + '"]');
              var file = input && input.files && input.files[0];
              if (file) files[f.formDataName || 'image'] = file;
              var wrap = input && input.closest('[data-upload-wrap]');
              if (editing && !file && wrap && wrap.dataset.cleared === '1') payload.removeImage = true;
              return;
            }
            var v = raw[f.name];
            if (f.type === 'number' || (f.type === 'select' && f.numeric)) v = v === '' || v == null ? null : Number(v);
            else if (v === '') v = f.required ? '' : null;
            payload[f.name] = v;
          });
          if (cfg.toPayload) payload = cfg.toPayload(payload, row);
          var path = editing ? cfg.endpoint + '/' + row[cfg.idKey] : (cfg.createEndpoint || cfg.endpoint);
          if (hasFile) {
            if (Object.keys(files).length) MH.busy(saveBtn, true, 'Uploading…');
            if (editing) await MH.api.putForm(path, payload, files); else await MH.api.postForm(path, payload, files);
          } else {
            if (editing) await MH.api.put(path, payload); else await MH.api.post(path, payload);
          }
          ctl.close();
          MH.ok(cfg.singular + (editing ? ' updated.' : ' created.'));
          load();
        } catch (e) {
          // the modal stays open with everything the user typed
          errBox.innerHTML = '<div class="alert alert-error">' + MH.icon('alert', 16) + '<span>' + esc(e && e.message ? e.message : 'Something went wrong. Please try again.') + '</span></div>';
          errBox.scrollIntoView({ block: 'nearest' });
        } finally {
          saving = false;
          MH.busy(saveBtn, false);
        }
      }
    }

    // ---------- delete ----------
    async function remove(row) {
      var label = row[cfg.nameKey || cfg.columns[0].key];
      var yes = await MH.confirm({
        title: 'Delete ' + cfg.singular.toLowerCase() + '?',
        html: (cfg.deleteWarning ? esc(cfg.deleteWarning(row)) + ' ' : '') + 'This permanently deletes <strong>' + esc(label == null ? 'this record' : (cfg.nameKey && /Id$/.test(cfg.nameKey) ? '#' + label : label)) + '</strong>. This cannot be undone.',
        confirmText: 'Delete', danger: true
      });
      if (!yes) return;
      try {
        await MH.api.del(cfg.endpoint + '/' + row[cfg.idKey]);
        MH.ok(cfg.singular + ' deleted.');
        load();
      } catch (e) { MH.fail(e); }
    }

    load();
  };
})(window.MH);
