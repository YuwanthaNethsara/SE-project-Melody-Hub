/* Customer warranty claims: only items actually purchased can be claimed (enforced by the backend too). */
(function () {
  if (!MH.guards.requireRole(['CUSTOMER'])) return;
  var esc = MH.esc, tab = 'eligible', eligible = [], mine = [];
  var content = MH.mountPortal({ active: 'c-claims', title: 'Warranty claims', subtitle: 'Claim a warranty or request a return for items you bought.' });

  async function load() {
    content.innerHTML = MH.skeletonRows(4);
    try {
      var r = await Promise.all([MH.api.get('/warranty-claims/eligible'), MH.api.get('/warranty-claims/my')]);
      eligible = r[0]; mine = r[1]; render();
    } catch (e) { content.innerHTML = MH.errorState('Unable to load warranty claims. Please try again.'); document.getElementById('retry').onclick = load; }
  }

  function render() {
    var html = '<div class="tabs"><button class="tab' + (tab === 'eligible' ? ' active' : '') + '" data-tab="eligible">My purchases (' + eligible.length + ')</button>' +
      '<button class="tab' + (tab === 'claims' ? ' active' : '') + '" data-tab="claims">My claims (' + mine.length + ')</button></div>';
    if (tab === 'eligible') {
      html += eligible.length ? '<div class="table-wrap"><table class="table stackable"><thead><tr><th>Product</th><th>Order</th><th>Purchased</th><th>Warranty</th><th>Expires</th><th>Claim status</th><th></th></tr></thead><tbody>' + eligible.map(function (i) {
        return '<tr><td data-label="Product">' + esc(i.productName) + ' × ' + i.quantity + '</td><td data-label="Order">#' + i.orderId + '</td><td data-label="Purchased">' + MH.date(i.purchaseDate) + '</td>' +
          '<td data-label="Warranty">' + MH.plural(i.warrantyPeriodMonths, 'month') + ', ' + esc(MH.humanize(i.warrantyType).toLowerCase()) + '</td><td data-label="Expires">' + MH.date(i.expiresOn) + '</td>' +
          '<td data-label="Claim status">' + (i.claimStatus ? MH.badge(i.claimStatus) : (i.claimable ? '<span class="muted" style="font-size:13px">No claim</span>' : MH.badge('EXPIRED'))) + '</td>' +
          '<td>' + (i.claimable ? '<button class="btn btn-sm btn-primary" data-claim="' + i.orderItemId + '">Claim warranty</button>' : '<span class="muted" style="font-size:13px">' + esc(blockedMessage(i)) + '</span>') + '</td></tr>';
      }).join('') + '</tbody></table></div>' : MH.empty({ icon: 'shield', title: 'No items with a warranty', text: 'Products you buy that include a warranty will appear here.', actionHtml: '<a class="btn btn-primary" href="' + MH.url('products/') + '">Browse products</a>' });
    } else {
      var expiry = {}; eligible.forEach(function (x) { expiry[x.orderItemId] = x.expiresOn; });
      html += mine.length ? '<div class="table-wrap"><table class="table stackable"><thead><tr><th>Claim ID</th><th>Product</th><th>Order</th><th>Type</th><th>Reason</th><th>Description</th><th>Submitted</th><th>Warranty expiry</th><th>Status</th><th>Actions</th></tr></thead><tbody>' + mine.slice().sort(function (a, b) { return b.claimId - a.claimId; }).map(function (c) {
        return '<tr><td data-label="Claim ID">#' + c.claimId + '</td><td data-label="Product">' + esc(c.productName) + '</td><td data-label="Order">#' + c.orderId + '</td><td data-label="Type">' + esc(MH.humanize(c.claimType)) + '</td><td data-label="Reason">' + esc(c.reason) + '</td>' +
          '<td data-label="Description"><span class="clip" title="' + esc(c.description || '') + '">' + esc(c.description || '—') + '</span></td><td data-label="Submitted">' + MH.dateTime(c.claimDate) + '</td><td data-label="Warranty expiry">' + (expiry[c.orderItemId] ? MH.date(expiry[c.orderItemId]) : '—') + '</td>' +
          '<td data-label="Status">' + MH.badge(c.status) + (c.resolution ? '<div class="muted" style="font-size:12px">' + esc(c.resolution) + '</div>' : '') + '</td>' +
          '<td data-label="Actions" style="white-space:nowrap">' + (c.status === 'PENDING' ? '<button class="btn btn-sm" data-edit-claim="' + c.claimId + '">Edit</button> <button class="btn btn-sm btn-danger" data-withdraw="' + c.claimId + '">Delete</button>' : '<span class="muted" style="font-size:13px">Locked: ' + esc(MH.humanize(c.status)) + '</span>') + '</td></tr>';
      }).join('') + '</tbody></table></div>' : MH.empty({ icon: 'shield', title: 'No warranty claims found.', text: 'Claims you submit will be tracked here.' });
    }
    content.innerHTML = html;
  }

  var REASONS = {
    WARRANTY: ['Product damaged', 'Product not working', 'Defective product', 'Wrong product', 'Other'],
    RETURN: ['Wrong item received', 'Item damaged on arrival', 'Item not as described', 'Faulty or defective', 'Other']
  };
  var DESC_SPEC = { name: 'description', label: 'Claim description', errorLabel: 'Claim description', type: 'textarea', required: true, minlength: 10, maxlength: 1000,
    requiredMsg: 'Claim description is required.', blankMsg: 'Claim description cannot contain only spaces.', minMsg: 'Claim description must be at least 10 characters.', maxMsg: 'Claim description is too long.', full: true };
  var REASON_MSG = 'Please select a claim reason.';
  function ro(label, value) { return '<div class="field"><label class="label">' + esc(label) + '</label><input class="input" type="text" readonly tabindex="-1" style="background:#f6f4ee;color:#5a564c;cursor:default" value="' + esc(value) + '" aria-readonly="true"></div>'; }
  function opts(type) { return REASONS[type].map(function (r) { return { value: r, label: r }; }); }

  // fresh copy from the server, so a stale page can never submit a duplicate or expired claim
  async function freshItem(orderItemId) {
    var list = await MH.api.get('/warranty-claims/eligible');
    eligible = list;
    return list.filter(function (x) { return String(x.orderItemId) === String(orderItemId); })[0];
  }
  function blockedMessage(item) {
    if (!item) return 'That purchase could not be found. Please refresh the page.';
    if (item.claimStatus) return 'A claim has already been submitted for this item.';
    if (!item.claimable) return /ended|expired/i.test(item.notClaimableReason || '') ? 'Warranty period has expired for this product.' : (item.notClaimableReason ? item.notClaimableReason + '.' : 'This item cannot be claimed.');
    return '';
  }

  async function claim(orderItemId) {
    if (!MH.v.isId(orderItemId)) { MH.toast('Please select a purchase to claim.', 'error'); return; }
    var item = eligible.filter(function (x) { return String(x.orderItemId) === String(orderItemId); })[0];
    var msg = blockedMessage(item);
    if (msg) { MH.toast(msg, 'error'); render(); return; }
    var form = document.createElement('form'); form.noValidate = true;
    function reasonField(type) {
      return MH.field({ name: 'reason', label: 'Reason', errorLabel: 'Reason', type: 'select', required: true, requiredMsg: REASON_MSG, options: opts(type), full: true });
    }
    form.innerHTML = '<div id="ce"></div><div class="form-grid">' +
      ro('Product', item.productName) + ro('Order number', '#' + item.orderId) + ro('Purchase date', MH.date(item.purchaseDate)) + ro('Warranty expiry', MH.date(item.expiresOn) + ' (' + MH.plural(item.warrantyPeriodMonths, 'month') + ')') +
      MH.field({ name: 'claimType', label: 'What do you need?', errorLabel: 'Claim type', type: 'select', required: true, requiredMsg: 'Please choose a claim type.', options: [{ value: 'WARRANTY', label: 'Warranty repair or replacement' }, { value: 'RETURN', label: 'Return the item' }], full: true }, 'WARRANTY') +
      '<div id="reason-slot" style="grid-column:1/-1">' + reasonField('WARRANTY') + '</div>' +
      MH.field(Object.assign({ placeholder: 'Describe the problem: what happened and when it started.', help: 'At least 10 characters.' }, DESC_SPEC)) + '</div>' +
      '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Submit</button>';
    var busy = false;
    form.querySelector('[name=claimType]').addEventListener('change', function (e) {
      var t = e.target.value; if (!REASONS[t]) return;
      document.getElementById('reason-slot').innerHTML = reasonField(t);
    });
    async function go(c, b) {
      if (busy) return;
      if (!MH.validate(form)) return;
      var v = MH.readForm(form);
      busy = true; MH.busy(b, true);
      var box = form.querySelector('#ce'); box.innerHTML = '';
      try {
        var latest = await freshItem(orderItemId), why = blockedMessage(latest);
        if (why) { c.close(); MH.toast(why, 'error'); render(); return; }
        await MH.api.post('/warranty-claims', { orderItemId: Number(orderItemId), claimType: v.claimType, reason: v.reason, description: v.description.trim() });
        c.close(); MH.ok(v.claimType === 'RETURN' ? 'Return request submitted successfully.' : 'Warranty claim submitted successfully.'); tab = 'claims'; load();
      } catch (e) {
        var m = /already (exists|been submitted)/i.test(e.message || '') ? 'A claim has already been submitted for this item.' : e.message;
        box.innerHTML = '<div class="alert alert-error">' + esc(m) + '</div>';
        if (/already|ended|expired|not active/i.test(e.message || '')) { load(); }
      }
      finally { busy = false; MH.busy(b, false); }
    }
    var ctl = MH.modal({ title: 'Warranty claim', body: form, keepOpen: true, footer: [{ label: 'Cancel' }, { label: 'Submit claim', cls: 'btn-primary', onClick: function (c, b) { go(c, b); } }] });
    form.onsubmit = function (e) { e.preventDefault(); go(ctl, ctl.btn(1)); };
  }
  // ---- edit / withdraw an own PENDING claim ----
  function editClaim(claimId) {
    var c = mine.filter(function (x) { return String(x.claimId) === String(claimId); })[0];
    if (!c) { MH.toast('That claim could not be found. Please refresh the page.', 'error'); return; }
    if (c.status !== 'PENDING') { MH.toast('Only a pending claim can be edited.', 'error'); load(); return; }
    var form = document.createElement('form'); form.noValidate = true;
    function reasonField(type, current) {
      var o = opts(type);
      if (current && !o.some(function (x) { return x.value === current; })) o.unshift({ value: current, label: current });
      return MH.field({ name: 'reason', label: 'Reason', errorLabel: 'Reason', type: 'select', required: true, requiredMsg: REASON_MSG, options: o, full: true }, current || '');
    }
    var ex = eligible.filter(function (x) { return String(x.orderItemId) === String(c.orderItemId); })[0] || {};
    form.innerHTML = '<div id="ce"></div><div class="form-grid">' +
      ro('Product', c.productName) + ro('Order number', '#' + c.orderId) + ro('Purchase date', ex.purchaseDate ? MH.date(ex.purchaseDate) : '—') + ro('Warranty expiry', ex.expiresOn ? MH.date(ex.expiresOn) : '—') + ro('Status', MH.humanize(c.status)) +
      MH.field({ name: 'claimType', label: 'What do you need?', errorLabel: 'Claim type', type: 'select', required: true, options: [{ value: 'WARRANTY', label: 'Warranty repair or replacement' }, { value: 'RETURN', label: 'Return the item' }], full: true }, c.claimType) +
      '<div id="reason-slot" style="grid-column:1/-1">' + reasonField(c.claimType, c.reason) + '</div>' +
      MH.field(Object.assign({ help: 'At least 10 characters.' }, DESC_SPEC), c.description || '') + '</div>' +
      '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Save</button>';
    form.querySelector('[name=claimType]').addEventListener('change', function (e) {
      if (REASONS[e.target.value]) document.getElementById('reason-slot').innerHTML = reasonField(e.target.value, '');
    });
    var busy = false;
    async function go(ctl, b) {
      if (busy) return;
      if (!MH.validate(form)) return;
      var v = MH.readForm(form);
      busy = true; MH.busy(b, true);
      var box = form.querySelector('#ce'); box.innerHTML = '';
      try {
        await MH.api.put('/warranty-claims/' + c.claimId, { orderItemId: c.orderItemId, claimType: v.claimType, reason: v.reason, description: v.description.trim() });
        ctl.close(); MH.ok('Warranty claim updated successfully.'); load();
      } catch (e) {
        box.innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>';
        if (/only a pending/i.test(e.message || '')) load();
      } finally { busy = false; MH.busy(b, false); }
    }
    var ctl = MH.modal({ title: 'Edit claim #' + c.claimId, body: form, keepOpen: true, footer: [{ label: 'Cancel' }, { label: 'Save changes', cls: 'btn-primary', onClick: function (k, b) { go(k, b); } }] });
    form.onsubmit = function (e) { e.preventDefault(); go(ctl, ctl.btn(1)); };
  }

  async function withdrawClaim(claimId, btn) {
    if (!await MH.confirm({ title: 'Delete claim', message: 'Are you sure you want to delete this warranty claim?', confirmText: 'Delete', cancelText: 'Cancel', danger: true })) return;
    MH.busy(btn, true);
    try { await MH.api.del('/warranty-claims/my/' + encodeURIComponent(claimId)); MH.ok('Warranty claim deleted successfully.'); load(); }
    catch (e) { MH.toast(e.message, 'error'); MH.busy(btn, false); load(); }
  }

  content.addEventListener('click', function (e) {
    var ed = e.target.closest('[data-edit-claim]'); if (ed) { editClaim(ed.dataset.editClaim); return; }
    var wd = e.target.closest('[data-withdraw]'); if (wd) { withdrawClaim(wd.dataset.withdraw, wd); return; }
    var t = e.target.closest('[data-tab]'); if (t) { tab = t.dataset.tab; render(); return; }
    var c = e.target.closest('[data-claim]'); if (c) claim(c.dataset.claim);
  });
  load();
})();
