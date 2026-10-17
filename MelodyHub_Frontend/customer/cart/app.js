/* Customer cart. Prices shown here come from the server; the backend recalculates everything at checkout. */
(function () {
  if (!MH.guards.requireRole(['CUSTOMER'])) return;
  var esc = MH.esc;
  var content = MH.mountPortal({ active: 'c-cart', title: 'Cart', subtitle: 'Prices and offers are confirmed again when you check out.' });
  var cart = null;

  async function load() {
    content.innerHTML = MH.skeletonRows(4);
    try { cart = await MH.api.get('/cart'); render(); }
    catch (e) { content.innerHTML = '<div class="card">' + MH.errorState(e.message) + '</div>'; document.getElementById('retry').onclick = load; }
  }

  function render() {
    var items = cart.items || [];
    if (!items.length) {
      content.innerHTML = '<div class="card">' + MH.empty({ icon: 'cart', title: 'Your cart is empty', text: 'Browse the store and add something you love.', actionHtml: '<a class="btn btn-primary" href="' + MH.url('products/') + '">Browse products</a>' }) + '</div>';
      MH.cart.refresh(); return;
    }
    var count = items.reduce(function (s, i) { return s + i.quantity; }, 0);
    content.innerHTML = '<div class="cart-layout"><section class="card"><h3 class="panel-title">' + count + ' item' + (count > 1 ? 's' : '') + ' in your cart</h3>' + items.map(function (i) {
      var max = i.quantityAvailable != null ? Math.min(99, i.quantityAvailable) : 99;
      var discounted = i.originalPrice != null && i.originalPrice > i.unitPrice;
      return '<div class="line" data-id="' + i.cartItemId + '" data-pid="' + i.productId + '">' +
        '<a href="' + MH.url('products/details/') + '?id=' + i.productId + '" tabindex="-1" aria-hidden="true">' + MH.img(i.imageUrl, i.productName) + '</a>' +
        '<div style="min-width:0"><a class="name" href="' + MH.url('products/details/') + '?id=' + i.productId + '">' + esc(i.productName) + '</a>' +
        '<div class="muted" style="font-size:14px">' + MH.money(i.unitPrice) + ' each' + (discounted ? ' <s class="faint">' + MH.money(i.originalPrice) + '</s>' : '') + '</div>' +
        (i.quantityAvailable != null && i.quantityAvailable <= 5 ? '<div class="amber" style="font-size:13px;font-weight:600">' + (i.quantityAvailable > 0 ? 'Only ' + i.quantityAvailable + ' left' : 'Out of stock') + '</div>' : '') + '</div>' +
        '<div class="right"><strong style="font-variant-numeric:tabular-nums">' + MH.money(i.subtotal) + '</strong>' +
        '<div class="qty" role="group" aria-label="Quantity of ' + esc(i.productName) + '"><button type="button" data-act="dec" aria-label="Decrease quantity"' + (i.quantity <= 1 ? ' disabled' : '') + '>−</button><input value="' + i.quantity + '" aria-label="Quantity" readonly><button type="button" data-act="inc" aria-label="Increase quantity"' + (i.quantity >= max ? ' disabled' : '') + '>+</button></div>' +
        '<button type="button" class="btn btn-sm btn-ghost" data-act="remove">' + MH.icon('trash', 14) + ' Remove</button></div></div>';
    }).join('') + '</section>' +
      '<aside class="card summary"><h3 class="panel-title">Order summary</h3>' +
      '<div class="sum-row"><span>Items (' + count + ')</span><span>' + MH.money(cart.totalAmount) + '</span></div>' +
      '<div class="sum-row"><span>Delivery</span><span>Free</span></div>' +
      '<div class="sum-row total"><span>Total</span><span>' + MH.money(cart.totalAmount) + '</span></div>' +
      '<a class="btn btn-primary btn-block btn-lg" href="' + MH.url('customer/checkout/') + '" style="margin-top:14px">Go to checkout</a>' +
      '<a class="btn btn-block btn-ghost" href="' + MH.url('products/') + '" style="margin-top:8px">Continue shopping</a>' +
      '<button type="button" class="btn btn-block btn-ghost" data-act="clear" style="margin-top:4px;color:var(--bad)">Clear cart</button></aside></div>';
    MH.cart.refresh();
  }

  content.addEventListener('click', async function (e) {
    var b = e.target.closest('[data-act]'); if (!b) return;
    var line = b.closest('.line'), act = b.dataset.act;
    try {
      if (act === 'clear') {
        if (!await MH.confirm({ title: 'Clear cart', message: 'Remove every item from your cart?', confirmText: 'Clear cart', danger: true })) return;
        await MH.api.del('/cart/clear'); await load(); return;
      }
      var id = line.dataset.id;
      var item = cart.items.filter(function (x) { return String(x.cartItemId) === id; })[0];
      MH.busy(b, true);
      if (act === 'remove') await MH.api.del('/cart/items/' + id);
      else {
        var q = item.quantity + (act === 'inc' ? 1 : -1);
        if (q < 1) { MH.busy(b, false); return; }
        if (q > 99) { MH.busy(b, false); MH.toast('You can order at most 99 units of one product.', 'error'); return; }
        await MH.api.put('/cart/items/' + id, undefined, { params: { quantity: q } });
      }
      await load();
    } catch (err) { MH.fail(err); MH.busy(b, false); load(); }
  });
  load();
})();
