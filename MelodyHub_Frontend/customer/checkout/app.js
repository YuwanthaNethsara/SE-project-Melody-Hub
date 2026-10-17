/* Checkout: shipping details -> POST /orders/checkout -> POST /payments. The server prices everything. */
(function () {
  if (!MH.guards.requireRole(['CUSTOMER'])) return;
  var esc = MH.esc;
  var content = MH.mountPortal({ active: 'c-cart', title: 'Checkout', subtitle: 'Where should we send your order, and how would you like to pay?' });
  var METHODS = [
    { v: 'CARD', t: 'Credit / debit card', d: 'Pay now. Your order is confirmed immediately.' },
    { v: 'BANK_TRANSFER', t: 'Bank transfer', d: 'Pay now by bank transfer.' },
    { v: 'CASH_ON_DELIVERY', t: 'Cash on delivery', d: 'Pay when your order arrives.' }
  ];

  async function load() {
    content.innerHTML = MH.skeletonRows(4);
    var cart, user = MH.auth.getCurrentUser();
    try { cart = await MH.api.get('/cart'); }
    catch (e) { content.innerHTML = '<div class="card">' + MH.errorState(e.message) + '</div>'; document.getElementById('retry').onclick = load; return; }
    var items = cart.items || [];
    if (!items.length) {
      content.innerHTML = '<div class="card">' + MH.empty({ icon: 'cart', title: 'Your cart is empty', text: 'Add products to your cart, then come back to check out.', actionHtml: '<a class="btn btn-primary" href="' + MH.url('products/') + '">Browse products</a>' }) + '</div>';
      return;
    }
    content.innerHTML = '<div class="cart-layout"><form class="card" id="co" novalidate><div id="co-err"></div>' +
      '<h3 class="panel-title">Delivery address</h3><div class="form-grid">' +
      MH.field({ name: 'fullName', label: 'Full name', kind: 'person', required: true, maxlength: 100, autocomplete: 'name' }, user.name) +
      MH.field({ name: 'phone', label: 'Phone number', type: 'tel', required: true, maxlength: 20, autocomplete: 'tel', placeholder: '+94 77 123 4567' }) +
      MH.field({ name: 'address', label: 'Street address', kind: 'address', required: true, minlength: 5, maxlength: 150, full: true, autocomplete: 'street-address' }) +
      MH.field({ name: 'city', label: 'City', kind: 'name', required: true, minlength: 2, maxlength: 60, autocomplete: 'address-level2' }) +
      MH.field({ name: 'postal', label: 'Postal code', kind: 'postal', required: true, maxlength: 10, autocomplete: 'postal-code' }) +
      '</div><h3 class="panel-title" style="margin-top:8px">Payment method</h3><div class="pay-options">' + METHODS.map(function (m, i) {
        return '<label class="pay-option"><input type="radio" name="method" value="' + m.v + '"' + (i === 0 ? ' checked' : '') + '><span><strong>' + m.t + '</strong><small>' + m.d + '</small></span></label>';
      }).join('') + '</div><div class="field-error" id="method-err" role="alert"></div>' +
      '<div class="alert alert-info">' + MH.icon('info', 16) + '<span>Payments are handled by a test payment gateway. No card details are collected and no real money is charged.</span></div>' +
      '<button class="btn btn-primary btn-lg" type="submit" id="place">Place order</button></form>' +
      '<aside class="card summary"><h3 class="panel-title">Your order</h3>' + items.map(function (i) {
        return '<div class="sum-row"><span>' + esc(i.productName) + ' × ' + i.quantity + '</span><span>' + MH.money(i.subtotal) + '</span></div>';
      }).join('') + '<div class="sum-row total"><span>Total</span><span>' + MH.money(cart.totalAmount) + '</span></div>' +
      '<p class="muted" style="font-size:12.5px;margin-bottom:0">The final total is calculated by the server when you place the order.</p></aside></div>';
    document.getElementById('co').addEventListener('submit', submit);
  }

  var placing = false, placedOrder = null;
  async function submit(e) {
    e.preventDefault();
    if (placing) return; // a second click while the first order is being placed
    var form = e.target, btn = document.getElementById('place'), err = document.getElementById('co-err');
    err.innerHTML = '';
    var ok = MH.validate(form);
    var chosen = form.querySelector('input[name="method"]:checked');
    var okMethod = !!chosen && METHODS.some(function (m) { return m.v === chosen.value; });
    document.getElementById('method-err').textContent = okMethod ? '' : 'Please choose a payment method.';
    if (!ok || !okMethod) return;

    var v = MH.readForm(form), method = chosen.value;
    var shipping = v.fullName + ', ' + v.address + ', ' + v.city + ' ' + v.postal + ' (Tel: ' + v.phone + ')';
    placing = true;
    MH.busy(btn, true, 'Placing order…');
    var order = placedOrder;
    if (!order) {
      try { order = placedOrder = await MH.api.post('/orders/checkout', { shippingAddress: shipping }); }
      catch (ex) { err.innerHTML = '<div class="alert alert-error">' + MH.icon('alert', 16) + '<span>' + esc(ex.message) + '</span></div>'; err.scrollIntoView({ block: 'nearest' }); MH.busy(btn, false); placing = false; return; }
    }
    if (!order || !MH.v.isId(order.orderId)) { err.innerHTML = '<div class="alert alert-error">The order could not be confirmed. Please check My orders before trying again.</div>'; MH.busy(btn, false); placing = false; return; }
    try {
      await MH.api.post('/payments', { orderId: order.orderId, paymentMethod: method });
      location.href = MH.url('customer/orders/') + '?id=' + order.orderId + '&placed=1';
    } catch (ex) {
      MH.toast('Your order was placed but the payment did not go through. You can retry it now.', 'error');
      location.href = MH.url('customer/payments/') + '?orderId=' + order.orderId;
    }
  }
  load();
})();
