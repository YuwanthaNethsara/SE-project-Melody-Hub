/* Customer dashboard: real numbers from the customer's own orders, payments, cart, claims, reviews and library. */
(function () {
  if (!MH.guards.requireRole(['CUSTOMER'])) return;
  var esc = MH.esc, user = MH.auth.getCurrentUser();
  var content = MH.mountPortal({ active: 'c-dashboard', title: 'Welcome back, ' + user.name.split(' ')[0], subtitle: 'Your MelodyHub account at a glance.' });

  var plural = MH.plural;
  function stat(label, value, sub, href, icon) {
    return '<a class="stat" href="' + MH.url(href) + '"><div class="label">' + esc(label) + '</div><span class="ic">' + MH.icon(icon, 19) + '</span>' +
      '<div class="value">' + value + '</div><div class="sub">' + esc(sub) + '</div></a>';
  }
  // one row of the "Next steps" card; only rows with something to do are shown
  function step(icon, title, text, href, action) {
    return '<li class="next-step"><span class="ic">' + MH.icon(icon, 18) + '</span><div><strong>' + esc(title) + '</strong><div class="muted">' + esc(text) + '</div></div>' +
      '<a class="btn btn-sm" href="' + MH.url(href) + '">' + esc(action) + '</a></li>';
  }

  async function load() {
    content.innerHTML = MH.skeletonRows(4);
    var failed = false;
    function g(p) { return MH.api.get(p).catch(function () { failed = true; return null; }); }
    var r = await Promise.all([g('/orders'), g('/cart'), g('/warranty-claims/my'), g('/payments'), g('/reviews/eligible'), g('/digital-music')]);
    var orders = r[0] || [], cart = r[1] || { items: [] }, claims = r[2] || [], pays = r[3] || [], reviewable = r[4] || [], tracks = r[5] || [];

    var open = orders.filter(function (o) { return ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED'].indexOf(o.orderStatus) >= 0; }).length;
    var paid = pays.filter(function (p) { return p.paymentStatus === 'PAID'; });
    var paidIds = {}; paid.forEach(function (p) { paidIds[p.orderId] = 1; });
    var spent = paid.reduce(function (s, p) { return s + p.amount; }, 0);
    var unpaid = orders.filter(function (o) { return o.paymentStatus !== 'PAID' && o.orderStatus !== 'CANCELLED' && !paidIds[o.orderId]; });
    var cartQty = (cart.items || []).reduce(function (s, i) { return s + i.quantity; }, 0);
    var toReview = reviewable.filter(function (e) { return !e.reviewId; });
    var pendingClaims = claims.filter(function (c) { return c.status === 'PENDING'; }).length;
    var recent = orders.slice().sort(function (a, b) { return b.orderId - a.orderId; }).slice(0, 5);

    var steps = [];
    if (unpaid.length) steps.push(step('card', plural(unpaid.length, 'order') + ' waiting for payment', 'Pay to confirm your order and start delivery.', 'customer/payments/', 'Pay now'));
    if (cartQty) steps.push(step('cart', plural(cartQty, 'item') + ' in your cart', MH.money(cart.totalAmount || 0) + ' ready for checkout.', 'customer/cart/', 'Go to cart'));
    if (toReview.length) steps.push(step('star', plural(toReview.length, 'product') + ' to review', 'Tell other customers how your purchase is working out.', 'customer/reviews/', 'Write a review'));
    if (tracks.length) steps.push(step('music', plural(tracks.length, 'track') + ' in the digital library', 'Download MP3s to listen offline.', 'customer/music/', 'Open library'));

    content.innerHTML = (failed ? '<div class="alert alert-warn">Some figures could not be loaded. Refresh to try again.</div>' : '') +
      '<div class="stats">' +
        stat('Items in cart', cartQty, MH.money(cart.totalAmount || 0), 'customer/cart/', 'cart') +
        stat('Open orders', open, plural(orders.length, 'order') + ' in total', 'customer/orders/', 'box') +
        stat('Total paid', MH.moneyHtml(spent), plural(paid.length, 'payment'), 'customer/payments/', 'card') +
        stat('Warranty claims', claims.length, pendingClaims + ' awaiting review', 'customer/warranty-claims/', 'shield') +
      '</div>' +
      '<div class="two-col">' +
        '<section class="card"><div class="panel-head"><h3 class="panel-title">Recent orders</h3><a class="btn btn-sm" href="' + MH.url('customer/orders/') + '">View all</a></div>' +
        (recent.length ? '<div class="table-wrap"><table class="table stackable"><thead><tr><th>Order</th><th>Date</th><th>Total</th><th>Status</th></tr></thead><tbody>' + recent.map(function (o) {
          return '<tr><td data-label="Order"><a href="' + MH.url('customer/orders/') + '?id=' + o.orderId + '">#' + o.orderId + '</a></td><td data-label="Date">' + MH.date(o.orderDate) + '</td><td data-label="Total">' + MH.money(o.totalAmount) + '</td><td data-label="Status">' + MH.badge(o.orderStatus) + '</td></tr>';
        }).join('') + '</tbody></table></div>'
          : MH.empty({ icon: 'box', title: 'No orders yet', text: 'Instruments, gear and accessories you buy will show up here.', actionHtml: '<a class="btn btn-primary" href="' + MH.url('products/') + '">Start shopping</a>' })) +
        '</section>' +
        '<section class="card"><h3 class="panel-title">Next steps</h3>' +
        (steps.length ? '<ul class="next-steps">' + steps.join('') + '</ul>'
          : '<p class="muted" style="margin:0">You are all caught up. Browse the store or the music catalogue for something new.</p>' +
            '<div class="row" style="margin-top:14px"><a class="btn btn-sm" href="' + MH.url('products/') + '">Shop products</a><a class="btn btn-sm" href="' + MH.url('artists/') + '">Browse artists</a></div>') +
        '</section>' +
      '</div>';
  }
  load();
})();
