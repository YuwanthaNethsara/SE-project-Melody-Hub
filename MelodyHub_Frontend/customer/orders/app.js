/* Customer orders: list, detail (?id=), cancel. */
(function () {
  if (!MH.guards.requireRole(['CUSTOMER'])) return;
  var esc = MH.esc, FLOW = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];
  var rawId = MH.qs('id');
  var id = rawId !== null && MH.v.isId(rawId) ? rawId.trim() : (rawId === null ? null : 'invalid');
  var content = MH.mountPortal({ active: 'c-orders', title: id && id !== 'invalid' ? 'Order #' + id : 'My orders', subtitle: id ? '' : 'Track deliveries, pay pending orders and cancel orders that have not been processed yet.', actionsHtml: id ? '<a class="btn" href="./">All orders</a>' : '' });

  function timeline(status) {
    var cancelled = status === 'CANCELLED', idx = FLOW.indexOf(status);
    var steps = cancelled ? ['PENDING', 'CANCELLED'] : FLOW;
    return '<div class="timeline' + (cancelled ? ' cancelled' : '') + '">' + steps.map(function (s) {
      var done = cancelled ? true : FLOW.indexOf(s) <= idx;
      return '<div class="step' + (done ? ' done' : '') + '">' + esc(MH.humanize(s)) + '</div>';
    }).join('') + '</div>';
  }

  async function list() {
    content.innerHTML = MH.skeletonRows(4);
    try {
      var res = await Promise.all([MH.api.get('/orders'), MH.api.get('/order-items').catch(function () { return []; })]);
      var orders = res[0], byOrder = {};
      res[1].forEach(function (i) { (byOrder[i.orderId] = byOrder[i.orderId] || []).push(i); });
      orders.sort(function (a, b) { return b.orderId - a.orderId; });
      if (!orders.length) { content.innerHTML = MH.empty({ icon: 'clipboard', title: 'No orders yet', text: 'Your orders will show up here once you check out.', actionHtml: '<a class="btn btn-primary" href="' + MH.url('products/') + '">Start shopping</a>' }); return; }
      content.innerHTML = orders.map(function (o) {
        var its = byOrder[o.orderId] || [], qty = its.reduce(function (n, i) { return n + Number(i.quantity || 0); }, 0);
        return '<div class="order-card"><div><div class="id">Order #' + o.orderId + '</div><div class="muted">' + MH.dateTime(o.orderDate) + '</div>' +
          (its.length ? '<div style="margin-top:4px">' + its.map(function (i) { return esc(i.productName) + ' × ' + i.quantity; }).join(', ') + ' <span class="muted">(' + qty + (qty === 1 ? ' item' : ' items') + ')</span></div>' : '') +
          '<div class="muted" style="font-size:13px">Ship to: ' + esc(o.shippingAddress || '—') + '</div></div>' +
          '<div style="text-align:right"><strong>' + MH.money(o.totalAmount) + '</strong></div>' +
          '<div class="row">' + MH.badge(o.orderStatus) + MH.badge(o.paymentStatus) + '</div>' +
          '<div style="text-align:right"><a class="btn btn-sm" href="?id=' + o.orderId + '">View details</a></div></div>';
      }).join('');
    } catch (e) { content.innerHTML = MH.errorState(e.message); document.getElementById('retry').onclick = list; }
  }

  // change the shipping address (allowed by the backend until the shop starts processing the order)
  function editAddress(o) {
    var form = document.createElement('form'); form.noValidate = true;
    form.innerHTML = '<div id="ae"></div>' +
      MH.field({ name: 'shippingAddress', label: 'Shipping address', errorLabel: 'Shipping address', type: 'textarea', kind: 'address', required: true, minlength: 5, maxlength: 300, full: true, help: 'Full name, street, city and postal code.' }, o.shippingAddress || '') +
      '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Save</button>';
    var busy = false;
    async function go(ctl, b) {
      if (busy) return;
      if (!MH.validate(form)) return;
      var v = MH.readForm(form);
      busy = true; MH.busy(b, true);
      var box = form.querySelector('#ae'); box.innerHTML = '';
      try {
        await MH.api.put('/orders/' + o.orderId + '/shipping-address', { shippingAddress: v.shippingAddress.trim() });
        ctl.close(); MH.ok('Shipping address updated.'); detail();
      } catch (e) { box.innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; }
      finally { busy = false; MH.busy(b, false); }
    }
    var ctl = MH.modal({ title: 'Edit shipping address', body: form, keepOpen: true, footer: [{ label: 'Cancel' }, { label: 'Save address', cls: 'btn-primary', onClick: function (k, b) { go(k, b); } }] });
    form.onsubmit = function (e) { e.preventDefault(); go(ctl, ctl.btn(1)); };
  }

  async function detail() {
    content.innerHTML = MH.skeletonRows(5);
    try {
      var o = await MH.api.get('/orders/' + encodeURIComponent(id));
      var items = await MH.api.get('/order-items/order/' + encodeURIComponent(id)).catch(function () { return []; });
      var pay = await MH.api.get('/payments/order/' + encodeURIComponent(id)).catch(function () { return null; });
      var html = '';
      if (MH.qs('placed')) html += '<div class="alert alert-ok">' + MH.icon('check', 16) + '<span><strong>Thank you.</strong> Your order has been placed. We will email you when it ships.</span></div>';
      html += '<div class="card">' +
        '<div class="row spread"><div><h3 style="margin:0">Order #' + o.orderId + '</h3><span class="muted">Placed ' + MH.dateTime(o.orderDate) + '</span></div>' +
        '<div class="row">' + MH.badge(o.orderStatus) + MH.badge(o.paymentStatus) + '</div></div><div style="margin-top:18px">' + timeline(o.orderStatus) + '</div>' +
        '<dl class="kv"><dt>Shipping to</dt><dd>' + esc(o.shippingAddress || '—') + '</dd><dt>Total</dt><dd><strong>' + MH.money(o.totalAmount) + '</strong></dd>' +
        '<dt>Payment</dt><dd>' + (pay ? esc(MH.humanize(pay.paymentMethod)) + ', ' + esc(MH.humanize(pay.paymentStatus).toLowerCase()) + (pay.transactionId ? '<div class="faint" style="font-size:12.5px">Reference ' + esc(pay.transactionId) + '</div>' : '') : 'No payment recorded yet') + '</dd></dl></div>' +
        '<section class="card card-flush" style="margin-top:16px"><h3 class="panel-title" style="padding:18px 20px 0">Items</h3>' +
        (items.length ? '<div class="table-wrap"><table class="table stackable"><thead><tr><th>Product</th><th>Qty</th><th>Unit price</th><th>Subtotal</th></tr></thead><tbody>' + items.map(function (i) {
          return '<tr><td data-label="Product" class="strong"><a href="' + MH.url('products/details/') + '?id=' + i.productId + '">' + esc(i.productName) + '</a>' + (o.paymentStatus === 'PAID' && o.orderStatus !== 'CANCELLED' ? ' <a class="faint" style="font-size:13px;font-weight:500;margin-left:6px" href="' + MH.url('products/details/') + '?id=' + i.productId + '#reviews">Review</a>' : '') + '</td><td data-label="Qty">' + i.quantity + '</td><td data-label="Unit price">' + MH.money(i.unitPrice) + '</td><td data-label="Subtotal">' + MH.money(i.subtotal) + '</td></tr>';
        }).join('') + '</tbody></table></div>' : '<p class="muted" style="margin:0">No items found for this order.</p>') + '</section>' +
        '<div class="row" style="margin-top:16px" id="acts"></div>';
      content.innerHTML = html;
      var acts = document.getElementById('acts');
      if (o.paymentStatus !== 'PAID' && o.orderStatus !== 'CANCELLED') acts.innerHTML += '<a class="btn btn-primary" href="' + MH.url('customer/payments/') + '?orderId=' + o.orderId + '">Pay now</a>';
      if (o.orderStatus === 'PENDING' || o.orderStatus === 'CONFIRMED') {
        var ea = document.createElement('button'); ea.className = 'btn'; ea.textContent = 'Edit shipping address';
        ea.onclick = function () { editAddress(o); };
        acts.appendChild(ea);
      }
      if (o.orderStatus === 'PENDING') {
        var c = document.createElement('button'); c.className = 'btn btn-danger'; c.textContent = 'Cancel order';
        c.onclick = async function () {
          if (!await MH.confirm({ title: 'Cancel order', message: 'Cancel order #' + o.orderId + '? Items go back into stock and any payment is refunded.', confirmText: 'Cancel order', cancelText: 'Keep order', danger: true })) return;
          MH.busy(c, true);
          try { await MH.api.put('/orders/' + o.orderId + '/cancel'); MH.ok('Order cancelled.'); detail(); }
          catch (e) { MH.fail(e); MH.busy(c, false); }
        };
        acts.appendChild(c);
      }
    } catch (e) {
      content.innerHTML = e.status === 403 || e.status === 404
        ? MH.empty({ icon: 'alert', title: 'Order not found', text: 'This order does not exist or is not yours.', actionHtml: '<a class="btn" href="./">Back to my orders</a>' })
        : MH.errorState(e.message);
      var r = document.getElementById('retry'); if (r) r.onclick = detail;
    }
  }
  if (id === 'invalid') content.innerHTML = MH.empty({ icon: 'alert', title: 'Order not found', text: 'That order number is not valid.', actionHtml: '<a class="btn" href="./">Back to my orders</a>' });
  else if (id) detail(); else list();
})();
