/* Orders management for ADMIN and SALES_MANAGER */
(function () {
  var esc = MH.esc;
  // same lifecycle the backend enforces
  var NEXT = { PENDING: ['CONFIRMED', 'PROCESSING', 'CANCELLED'], CONFIRMED: ['PROCESSING', 'SHIPPED', 'CANCELLED'], PROCESSING: ['SHIPPED', 'CANCELLED'], SHIPPED: ['DELIVERED'], DELIVERED: [], CANCELLED: [] };

  async function details(order, ctx) {
    var ctl = MH.modal({ title: 'Order #' + order.orderId, wide: true, body: '<div class="spinner"></div>', footer: [{ label: 'Close' }] });
    try {
      var results = await Promise.all([
        MH.api.get('/order-items/order/' + order.orderId),
        MH.api.get('/payments/order/' + order.orderId).catch(function (e) { if (e.status === 404) return null; throw e; })
      ]);
      var items = results[0], pay = results[1];
      ctl.body.innerHTML =
        '<dl class="kv" style="margin-bottom:20px"><dt>Customer</dt><dd>' + esc(order.userName) + ' <span class="muted">(' + esc(order.userEmail) + ')</span></dd>' +
        '<dt>Placed</dt><dd>' + MH.dateTime(order.orderDate) + '</dd><dt>Ship to</dt><dd>' + esc(order.shippingAddress) + '</dd>' +
        '<dt>Order status</dt><dd>' + MH.badge(order.orderStatus) + '</dd><dt>Payment</dt><dd>' + MH.badge(order.paymentStatus) +
        (pay ? ' <span class="muted">' + esc(MH.humanize(pay.paymentMethod)) + ', ref ' + esc(pay.transactionId) + '</span>' : '') + '</dd></dl>' +
        '<div class="table-wrap"><table class="table"><thead><tr><th>Product</th><th class="num">Qty</th><th class="num">Unit price</th><th class="num">Subtotal</th></tr></thead><tbody>' +
        items.map(function (i) { return '<tr><td>' + esc(i.productName) + '</td><td class="num">' + i.quantity + '</td><td class="num">' + MH.money(i.unitPrice) + '</td><td class="num">' + MH.money(i.subtotal) + '</td></tr>'; }).join('') +
        '</tbody><tfoot><tr><td colspan="3" class="num"><strong>Total</strong></td><td class="num"><strong>' + MH.money(order.totalAmount) + '</strong></td></tr></tfoot></table></div>' +
        '<div class="row" id="order-acts" style="margin-top:18px"></div>';
      // every order action lives here, so the table row stays compact
      var acts = ctl.body.querySelector('#order-acts');
      function act(label, cls, fn) { var b = document.createElement('button'); b.type = 'button'; b.className = 'btn btn-sm ' + (cls || ''); b.textContent = label; b.onclick = function () { ctl.close(); fn(order, Object.assign({ button: b }, ctx || {})); }; acts.appendChild(b); }
      if (ctx && ctx.reload) {
        if ((NEXT[order.orderStatus] || []).length) act('Update status', 'btn-primary', changeStatus);
        if (isAdmin) act('Update payment status', '', changePayment);
        if (isAdmin && (order.orderStatus === 'PENDING' || order.orderStatus === 'CONFIRMED')) act('Edit shipping address', '', editAddress);
        if (['PENDING', 'CONFIRMED', 'PROCESSING'].indexOf(order.orderStatus) >= 0) act('Cancel order', 'btn-danger', cancelOrder);
        if (isAdmin) act('Delete order', 'btn-danger', deleteOrder);
      }
    } catch (e) { ctl.body.innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; }
  }

  function changeStatus(order, ctx) {
    var opts = NEXT[order.orderStatus] || [];
    var form = document.createElement('form');
    form.noValidate = true;
    form.innerHTML = '<div id="se"></div>' + MH.field({ name: 'status', label: 'New status', type: 'select', required: true, options: opts, full: true }, '') +
      '<p class="muted" id="hint" style="margin:0"></p>';
    var sel = form.querySelector('select');
    sel.onchange = function () {
      form.querySelector('#hint').textContent = sel.value === 'CANCELLED' ? 'Cancelling returns the reserved stock to inventory and marks a paid order as refunded.' : '';
    };
    var ctl = MH.modal({
      title: 'Update order #' + order.orderId, body: form,
      footer: [{ label: 'Cancel' }, { label: 'Update status', cls: 'btn-primary', onClick: async function (c, btn) {
        if (!MH.validate(form)) return;
        MH.busy(btn, true);
        try {
          await MH.api.put('/orders/' + order.orderId + '/status', undefined, { params: { status: sel.value } });
          c.close(); MH.ok('Order #' + order.orderId + ' is now ' + MH.humanize(sel.value) + '.'); ctx.reload();
        } catch (e) { form.querySelector('#se').innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; MH.busy(btn, false); }
      } }]
    });
  }


  var PAYMENT = ['PENDING', 'PAID', 'FAILED', 'REFUNDED'];
  var isAdmin = MH.auth.getRole() === 'ADMIN';
  function itemsOf(order, ctx) { return (ctx && ctx.itemsByOrder && ctx.itemsByOrder[order.orderId]) || []; }

  // ---- UPDATE: payment status (ADMIN only, PUT /orders/{id}/payment-status?status=) ----
  function changePayment(order, ctx) {
    var opts = PAYMENT.filter(function (p) { return p !== order.paymentStatus; });
    var form = document.createElement('form'); form.noValidate = true;
    form.innerHTML = '<div id="pe"></div><p style="margin-top:0">Current payment status: ' + MH.badge(order.paymentStatus) + '</p>' +
      MH.field({ name: 'status', label: 'New payment status', errorLabel: 'Payment status', type: 'select', required: true, options: opts, full: true }, '') +
      '<p class="muted" style="margin:0">This only changes the recorded status. It does not move any money.</p>' +
      '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Save</button>';
    var busy = false;
    async function go(c, btn) {
      if (busy || !MH.validate(form)) return;
      var v = form.querySelector('select').value; busy = true; MH.busy(btn, true);
      try {
        await MH.api.put('/orders/' + order.orderId + '/payment-status', undefined, { params: { status: v } });
        c.close(); MH.ok('Payment for order #' + order.orderId + ' is now ' + MH.humanize(v) + '.'); ctx.reload();
      } catch (e) { form.querySelector('#pe').innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; }
      finally { busy = false; MH.busy(btn, false); }
    }
    var ctl = MH.modal({ title: 'Update payment for order #' + order.orderId, body: form, keepOpen: true, footer: [{ label: 'Cancel' }, { label: 'Update payment', cls: 'btn-primary', onClick: function (c, b) { go(c, b); } }] });
    form.onsubmit = function (e) { e.preventDefault(); go(ctl, ctl.btn(1)); };
  }

  // ---- UPDATE: shipping address (PUT /orders/{id}/shipping-address) ----
  function editAddress(order, ctx) {
    var form = document.createElement('form'); form.noValidate = true;
    form.innerHTML = '<div id="ae"></div>' +
      MH.field({ name: 'shippingAddress', label: 'Shipping address', errorLabel: 'Shipping address', type: 'textarea', kind: 'address', required: true, minlength: 5, maxlength: 300, full: true, help: 'Can be changed until the order starts processing.' }, order.shippingAddress || '') +
      '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Save</button>';
    var busy = false;
    async function go(c, btn) {
      if (busy || !MH.validate(form)) return;
      var v = MH.readForm(form); busy = true; MH.busy(btn, true);
      try {
        await MH.api.put('/orders/' + order.orderId + '/shipping-address', { shippingAddress: v.shippingAddress.trim() });
        c.close(); MH.ok('Shipping address for order #' + order.orderId + ' updated.'); ctx.reload();
      } catch (e) { form.querySelector('#ae').innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; }
      finally { busy = false; MH.busy(btn, false); }
    }
    var ctl = MH.modal({ title: 'Edit shipping address for order #' + order.orderId, body: form, keepOpen: true, footer: [{ label: 'Cancel' }, { label: 'Save address', cls: 'btn-primary', onClick: function (c, b) { go(c, b); } }] });
    form.onsubmit = function (e) { e.preventDefault(); go(ctl, ctl.btn(1)); };
  }

  // ---- DELETE/CANCEL: cancel (PUT /orders/{id}/cancel) ----
  async function cancelOrder(order, ctx) {
    var yes = await MH.confirm({ title: 'Cancel order?', message: 'Cancel order #' + order.orderId + '? The reserved stock goes back to inventory and a paid order is marked refunded.', confirmText: 'Cancel order', cancelText: 'Keep order', danger: true });
    if (!yes) return;
    MH.busy(ctx.button, true);
    try { await MH.api.put('/orders/' + order.orderId + '/cancel'); MH.ok('Order #' + order.orderId + ' cancelled.'); ctx.reload(); }
    catch (e) { MH.fail(e); MH.busy(ctx.button, false); }
  }

  async function deleteOrder(order, ctx) {
    var yes = await MH.confirm({ title: 'Delete order?', html: 'This permanently deletes order <strong>#' + order.orderId + '</strong>, its items and its payment record. Reserved stock goes back to inventory. This cannot be undone.', confirmText: 'Delete order', danger: true });
    if (!yes) return;
    try { await MH.api.del('/orders/' + order.orderId); MH.ok('Order #' + order.orderId + ' deleted.'); ctx.reload(); }
    catch (e) { MH.fail(e); }
  }

  // ---- CREATE: order on behalf of a customer (POST /orders, ADMIN only) ----
  async function createOrder(ctx) {
    var ctl = MH.modal({ title: 'Create order', wide: true, body: '<div class="spinner"></div>', footer: [{ label: 'Close' }] });
    var users, products;
    try {
      var r = await Promise.all([MH.api.get('/auth/users'), MH.api.get('/products')]);
      users = r[0].filter(function (u) { return u.role === 'CUSTOMER'; });
      products = r[1].filter(function (p) { return String(p.status || 'ACTIVE').toUpperCase() === 'ACTIVE'; });
    } catch (e) { ctl.body.innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; return; }
    ctl.close();
    if (!users.length || !products.length) { MH.toast(!users.length ? 'There are no customers yet, so an order cannot be created.' : 'There are no active products to order.', 'error'); return; }
    var byId = {}; products.forEach(function (p) { byId[p.productId] = p; });
    var stockOf = function (p) { return p.quantityAvailable == null ? 0 : Number(p.quantityAvailable); };

    var form = document.createElement('form'); form.noValidate = true;
    form.innerHTML = '<div id="ce"></div>' +
      MH.field({ name: 'userId', label: 'Customer', errorLabel: 'Customer', type: 'select', required: true, full: true, requiredMsg: 'Please select a customer.',
        options: users.map(function (u) { return { value: u.id, label: u.name + ' (' + u.email + ')' }; }) }, '') +
      '<div class="field" id="lines-wrap" style="margin-top:14px"><label class="label">Products<span class="req" aria-hidden="true"> *</span></label><div id="lines"></div><div class="field-error" id="lines-err" role="alert"></div>' +
      '<button type="button" class="btn btn-sm" id="add-line" style="margin-top:8px">' + MH.icon('plus', 14) + ' Add product</button></div>' +
      MH.field({ name: 'shippingAddress', label: 'Shipping address', errorLabel: 'Shipping address', type: 'textarea', kind: 'address', required: true, minlength: 8, maxlength: 300, full: true, help: 'Full name, street, city and postal code.' }, '') +
      '<div class="card" style="margin-top:14px;background:var(--surface-2,#faf8f2)"><strong>Order review</strong><div id="review" class="muted" style="margin-top:6px"></div></div>' +
      '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Create</button>';
    var lines = form.querySelector('#lines');

    function optionsHtml(sel) {
      return '<option value="">Select a product…</option>' + products.map(function (p) {
        var out = stockOf(p) <= 0;
        return '<option value="' + p.productId + '"' + (out ? ' disabled' : '') + (String(sel) === String(p.productId) ? ' selected' : '') + '>' + esc(p.name) + ' — ' + MH.money(p.price) + ' (' + (out ? 'out of stock' : stockOf(p) + ' in stock') + ')</option>';
      }).join('');
    }
    function addLine() {
      var d = document.createElement('div'); d.className = 'order-line'; d.style.cssText = 'display:grid;grid-template-columns:1fr 110px auto;gap:8px;align-items:start;margin-bottom:8px';
      d.innerHTML = '<div><select class="select" name="productId" aria-label="Product">' + optionsHtml('') + '</select><div class="field-error" data-e="p" role="alert"></div></div>' +
        '<div><input class="input" name="quantity" type="text" inputmode="numeric" value="1" aria-label="Quantity" maxlength="6"><div class="field-error" data-e="q" role="alert"></div></div>' +
        '<button type="button" class="btn btn-sm btn-danger" data-rm title="Remove product" aria-label="Remove product">' + MH.icon('trash', 14) + '</button>';
      lines.appendChild(d);
      d.querySelector('[data-rm]').onclick = function () { if (lines.children.length > 1) { d.remove(); } else { d.querySelector('select').value = ''; d.querySelector('input').value = '1'; } review(); };
      d.querySelector('select').onchange = d.querySelector('input').oninput = function () { review(); };
    }
    function lineData() {
      return [].map.call(lines.children, function (d) {
        return { el: d, pid: d.querySelector('select').value, qtyText: d.querySelector('input').value.trim() };
      });
    }
    function setErr(el, sel, msg) { var e = el.querySelector('[data-e=' + sel + ']'); e.textContent = msg || ''; }
    function review() {
      var total = 0, parts = [];
      lineData().forEach(function (l) {
        var p = byId[l.pid], q = /^\d+$/.test(l.qtyText) ? Number(l.qtyText) : 0;
        if (p && q > 0) { total += Number(p.price) * q; parts.push(esc(p.name) + ' × ' + q + ' = ' + MH.money(Number(p.price) * q)); }
      });
      form.querySelector('#review').innerHTML = parts.length ? parts.join('<br>') + '<br><strong style="color:var(--ink)">Estimated total: ' + MH.money(total) + '</strong> <span class="faint">(the server calculates the final total)</span>' : 'Add at least one product to see the order summary.';
    }
    function validateAll() {
      var ok = true, seen = {}, any = false;
      form.querySelector('#lines-err').textContent = '';
      lineData().forEach(function (l) {
        setErr(l.el, 'p', ''); setErr(l.el, 'q', '');
        var p = byId[l.pid];
        if (!l.pid || !p) { setErr(l.el, 'p', 'Please select a product.'); ok = false; return; }
        any = true;
        if (seen[l.pid]) { setErr(l.el, 'p', 'This product is already in the order. Increase its quantity instead.'); ok = false; }
        seen[l.pid] = true;
        if (l.qtyText === '') { setErr(l.el, 'q', 'Quantity is required.'); ok = false; }
        else if (!/^\d+$/.test(l.qtyText)) { setErr(l.el, 'q', /^-/.test(l.qtyText) || /^0*$/.test(l.qtyText) ? 'Quantity must be greater than 0.' : 'Quantity must be a whole number.'); ok = false; }
        else if (Number(l.qtyText) <= 0) { setErr(l.el, 'q', 'Quantity must be greater than 0.'); ok = false; }
        else if (Number(l.qtyText) > stockOf(p)) { setErr(l.el, 'q', 'Quantity cannot exceed available stock (' + stockOf(p) + ' available).'); ok = false; }
      });
      if (!any && ok) { form.querySelector('#lines-err').textContent = 'Please add at least one product.'; ok = false; }
      return ok;
    }
    addLine(); review();
    form.querySelector('#add-line').onclick = function () { addLine(); review(); };

    var busy = false;
    async function go(c, btn) {
      if (busy) return;
      var okFields = MH.validate(form), okLines = validateAll();
      if (!okFields || !okLines) { MH.shake && MH.shake(form); return; }
      var v = MH.readForm(form);
      var body = { userId: Number(v.userId), shippingAddress: v.shippingAddress.trim(), items: lineData().map(function (l) { return { productId: Number(l.pid), quantity: Number(l.qtyText) }; }) };
      busy = true; MH.busy(btn, true); form.querySelector('#ce').innerHTML = '';
      try {
        var created = await MH.api.post('/orders', body);
        c.close(); MH.ok('Order #' + created.orderId + ' created.'); Promise.resolve(ctx.reload()).then(function () { details(created, ctx); });
      } catch (e) { form.querySelector('#ce').innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; }
      finally { busy = false; MH.busy(btn, false); }
    }
    var ctl2 = MH.modal({ title: 'Create order', wide: true, body: form, keepOpen: true, footer: [{ label: 'Cancel' }, { label: 'Create order', cls: 'btn-primary', onClick: function (c, b) { go(c, b); } }] });
    form.onsubmit = function (e) { e.preventDefault(); go(ctl2, ctl2.btn(1)); };
  }

  MH.crud({
    module: 'orders', title: 'Orders', subtitle: 'Follow every order from payment to delivery. Open an order to change its payment, address or to cancel it.', strongColumn: 1,
    singular: 'Order', icon: 'clipboard', noCreate: true,
    extraActionsHtml: isAdmin ? '<button class="btn btn-primary" id="order-add">' + MH.icon('plus', 16) + ' Create order</button>' : '',
    onMounted: function (api) {
      var b = document.getElementById('order-add');
      if (b) b.onclick = function () { createOrder(api); };
    },
    endpoint: '/orders', idKey: 'orderId', nameKey: 'orderId',
    viewRoles: ['ADMIN', 'SALES_MANAGER'], writeRoles: ['ADMIN', 'SALES_MANAGER'], deleteRoles: ['ADMIN'],
    canEdit: function () { return false; }, noDelete: true, defaultSort: 'orderId',
    // READ: orders (GET /orders) plus their items (GET /order-items) so products and quantities show in the table
    preload: async function () {
      var items = await MH.api.get('/order-items').catch(function () { return []; });
      var map = {};
      items.forEach(function (i) { (map[i.orderId] = map[i.orderId] || []).push(i); });
      return { itemsByOrder: map };
    },
    searchKeys: ['orderId', 'userName', 'userEmail', 'orderStatus', 'paymentStatus', 'shippingAddress'],
    filters: [{ key: 'orderStatus', label: 'Status', options: Object.keys(NEXT) }, { key: 'paymentStatus', label: 'Payment', options: PAYMENT }],
    columns: [
      { key: 'orderId', label: 'Order', render: function (r) { return '<strong>#' + r.orderId + '</strong>'; } }, { key: 'userName', label: 'Customer' },
      { key: 'orderDate', label: 'Placed', type: 'date' },
      { key: 'products', label: 'Products', low: true, render: function (r, ctx) {
        var it = itemsOf(r, ctx); if (!it.length) return '<span class="faint">—</span>';
        var t = it.map(function (i) { return i.productName + ' × ' + i.quantity; }).join(', ');
        return '<span class="clip" style="max-width:170px" title="' + esc(t) + '">' + esc(t) + '</span>'; } },
      { key: 'totalAmount', label: 'Total', type: 'money' },
      { key: 'orderStatus', label: 'Status', type: 'badge' }, { key: 'paymentStatus', label: 'Payment', type: 'badge' }
    ],
    onView: function (r, api) { details(r, api); },
    rowActions: [
      { label: 'Change status', cls: 'btn-primary', show: function (r) { return (NEXT[r.orderStatus] || []).length > 0; }, run: changeStatus }
    ],
    empty: { title: 'No orders yet', text: 'Orders show up here as soon as customers check out.' }
  });
})();
