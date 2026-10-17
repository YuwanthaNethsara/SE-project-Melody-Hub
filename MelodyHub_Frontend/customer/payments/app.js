/* Customer payments: pay for an unpaid order (?orderId=) and payment history. */
(function () {
  if (!MH.guards.requireRole(['CUSTOMER'])) return;
  var esc = MH.esc, orderId = MH.v.isId(MH.qs('orderId')) ? MH.qs('orderId').trim() : null;
  var content = MH.mountPortal({ active: 'c-payments', title: 'Payments', subtitle: 'Pay for orders and review your payment history.' });
  var METHODS = [['CARD', 'Credit / debit card'], ['BANK_TRANSFER', 'Bank transfer'], ['CASH_ON_DELIVERY', 'Cash on delivery']];

  async function load() {
    content.innerHTML = MH.skeletonRows(4);
    try {
      var results = await Promise.all([MH.api.get('/orders'), MH.api.get('/payments')]);
      var orders = results[0], pays = results[1];
      var paidIds = {}; pays.forEach(function (p) { if (p.paymentStatus === 'PAID') paidIds[p.orderId] = 1; });
      var unpaid = orders.filter(function (o) { return o.paymentStatus !== 'PAID' && o.orderStatus !== 'CANCELLED' && !paidIds[o.orderId]; });
      var html = '';
      if (unpaid.length) {
        html += '<section class="card"><h3 style="margin-top:0">Orders waiting for payment</h3><div id="pay-err"></div>' + unpaid.map(function (o) {
          return '<div class="order-card" style="margin-bottom:12px"><div><div class="id">Order #' + o.orderId + '</div><div class="muted">' + MH.money(o.totalAmount) + '</div></div>' +
            '<div style="text-align:right"><button class="btn btn-primary btn-sm" data-pay="' + o.orderId + '" data-amt="' + o.totalAmount + '">Pay now</button></div></div>';
        }).join('') + '</section>';
      }
      html += '<section class="card" style="margin-top:16px"><h3 style="margin-top:0">Payment history</h3>' + (pays.length ?
        '<div class="table-wrap"><table class="table stackable"><thead><tr><th>Payment</th><th>Order</th><th>Amount</th><th>Method</th><th>Status</th><th>Date</th><th>Transaction</th></tr></thead><tbody>' +
        pays.sort(function (a, b) { return b.paymentId - a.paymentId; }).map(function (p) {
          return '<tr><td data-label="Payment">#' + p.paymentId + '</td><td data-label="Order"><a href="' + MH.url('customer/orders/') + '?id=' + p.orderId + '">#' + p.orderId + '</a></td><td data-label="Amount">' + MH.money(p.amount) + '</td><td data-label="Method">' + esc(MH.humanize(p.paymentMethod)) + '</td><td data-label="Status">' + MH.badge(p.paymentStatus) + '</td><td data-label="Date">' + MH.dateTime(p.paymentDate) + '</td><td data-label="Transaction">' + esc(p.transactionId || '—') + '</td></tr>';
        }).join('') + '</tbody></table></div>' : '<p class="muted" style="margin:0">No payments yet.</p>') + '</section>';
      content.innerHTML = html;
      if (orderId) { var b = content.querySelector('[data-pay="' + orderId + '"]'); if (b) pay(b); }
    } catch (e) { content.innerHTML = MH.errorState(e.message); document.getElementById('retry').onclick = load; }
  }

  function pay(btn) {
    var oid = btn.dataset.pay, form = document.createElement('form'); form.noValidate = true;
    form.innerHTML = '<div id="pe"></div><p>Amount due: <strong>' + MH.money(btn.dataset.amt) + '</strong> for order #' + esc(oid) + '</p>' +
      '<div class="pay-options">' + METHODS.map(function (m, i) { return '<label class="pay-option"><input type="radio" name="method" value="' + m[0] + '"' + (i === 0 ? ' checked' : '') + '><span><strong>' + m[1] + '</strong></span></label>'; }).join('') + '</div>' +
      '<p class="muted" style="font-size:13px">Simulated gateway: no real card is charged.</p>';
    var paying = false;
    MH.modal({ title: 'Pay for order #' + oid, body: form, keepOpen: true, footer: [{ label: 'Cancel' }, { label: 'Pay now', cls: 'btn-primary', onClick: async function (c, b) {
      if (paying) return; // one payment per click, even on a double click
      var chosen = form.querySelector('input[name="method"]:checked');
      if (!chosen || !METHODS.some(function (m) { return m[0] === chosen.value; })) { form.querySelector('#pe').innerHTML = '<div class="alert alert-error">Please choose a payment method.</div>'; return; }
      paying = true; MH.busy(b, true);
      try {
        await MH.api.post('/payments', { orderId: Number(oid), paymentMethod: chosen.value });
        c.close(); MH.ok('Payment received. Thank you!'); history.replaceState(null, '', location.pathname); orderId = null; load();
      } catch (e) { form.querySelector('#pe').innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; MH.busy(b, false); paying = false; }
    } }] });
  }
  content.addEventListener('click', function (e) { var b = e.target.closest('[data-pay]'); if (b) pay(b); });
  load();
})();
