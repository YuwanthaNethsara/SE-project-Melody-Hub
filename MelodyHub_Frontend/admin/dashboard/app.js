/* Role-based staff dashboard. Each role only requests data its permissions allow. */
(function () {
  var ROLES = ['ADMIN', 'INVENTORY_MANAGER', 'ARTIST', 'MARKETING_MANAGER', 'SALES_MANAGER', 'SUPPLIER'];
  if (!MH.guards.requireRole(ROLES)) return;
  var esc = MH.esc, role = MH.auth.getRole(), user = MH.auth.getCurrentUser();
  var TITLES = { ADMIN: 'Dashboard', INVENTORY_MANAGER: 'Inventory dashboard', ARTIST: 'Artist dashboard', MARKETING_MANAGER: 'Marketing dashboard', SALES_MANAGER: 'Sales dashboard', SUPPLIER: 'Supplier dashboard' };
  var content = MH.mountPortal({ active: 'dashboard', title: TITLES[role], subtitle: 'Welcome back, ' + user.name.split(' ')[0] + '. Here is how the store is doing today.', actionsHtml: '<a class="btn" href="' + MH.url('index.html') + '">' + MH.icon('store', 16) + ' View store</a>' });
  var failed = [];

  function get(path, label) {
    return MH.api.get(path).catch(function (e) { failed.push(label || path); return []; });
  }
  function stat(label, value, sub, tone, href, icon) {
    if (!icon && href) { var m = MH.MODULES.filter(function (x) { return x.path === href; })[0]; icon = m && m.icon; }
    var inner = '<div class="label">' + esc(label) + '</div>' + (icon ? '<span class="ic">' + MH.icon(icon, 19) + '</span>' : '') + '<div class="value">' + value + '</div>' + (sub ? '<div class="sub">' + esc(sub) + '</div>' : '');
    return href ? '<a class="stat ' + (tone || '') + '" href="' + MH.url(href) + '">' + inner + '</a>' : '<div class="stat ' + (tone || '') + '">' + inner + '</div>';
  }
  function count(arr, fn) { return arr.filter(fn).length; }
  function panel(title, html, link, flush) {
    return '<section class="card' + (flush ? ' card-flush' : '') + '"><div class="panel-head"' + (flush ? ' style="padding:18px 20px 0"' : '') + '><h3 class="panel-title">' + esc(title) + '</h3>' + (link ? '<a class="btn btn-sm" href="' + MH.url(link) + '">View all</a>' : '') + '</div>' + html + '</section>';
  }
  function miniTable(heads, rows, empty) {
    if (!rows.length) return '<p class="muted" style="margin:0;padding:4px 0 6px">' + esc(empty) + '</p>';
    return '<div class="table-wrap"><table class="table stackable"><thead><tr>' + heads.map(function (h) { return '<th>' + h + '</th>'; }).join('') + '</tr></thead><tbody>' +
      rows.map(function (r) { return '<tr>' + r.map(function (c, i) { return '<td data-label="' + heads[i] + '">' + c + '</td>'; }).join('') + '</tr>'; }).join('') + '</tbody></table></div>';
  }
  function statusBars(byStatus) {
    var order = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
    var max = Math.max.apply(null, order.map(function (s) { return byStatus[s] || 0; }).concat([1]));
    return '<div class="bars">' + order.map(function (s) {
      var n = byStatus[s] || 0;
      return '<div class="bar-row"><span>' + MH.humanize(s) + '</span><div class="bar-track"><div class="bar-fill' + (s === 'PENDING' ? ' gold' : '') + '" style="width:' + (n / max * 100) + '%"></div></div><strong>' + n + '</strong></div>';
    }).join('') + '</div>';
  }
  function countBy(orders) { var m = {}; orders.forEach(function (o) { m[o.orderStatus] = (m[o.orderStatus] || 0) + 1; }); return m; }
  function revenueChart(months) {
    var max = Math.max.apply(null, months.map(function (m) { return m.amount; }).concat([1]));
    var fmt = function (v) { return v >= 1000000 ? (v / 1000000).toFixed(1) + 'M' : v >= 1000 ? Math.round(v / 1000) + 'k' : String(Math.round(v)); };
    return '<div class="colchart" style="--n:' + months.length + '" role="img" aria-label="Paid revenue for the last ' + months.length + ' months">' + months.map(function (m, i) {
      var d = new Date(m.month + '-01T00:00:00');
      return '<div class="col' + (i === months.length - 1 ? ' current' : '') + '" title="' + esc(MH.money(m.amount)) + '"><span class="v">' + (m.amount ? fmt(m.amount) : '0') + '</span><div class="lane"><div class="fill" style="height:' + Math.max(1.5, m.amount / max * 100) + '%"></div></div><span class="m">' + d.toLocaleDateString(undefined, { month: 'short' }) + '</span></div>';
    }).join('') + '</div>';
  }
  var recentOrders = function (orders) {
    return miniTable(['Order', 'Customer', 'Date', 'Total', 'Status'], orders.slice().sort(function (a, b) { return b.orderId - a.orderId; }).slice(0, 6).map(function (o) {
      return ['<a class="strong" href="' + MH.url('admin/orders/') + '?q=' + o.orderId + '">#' + o.orderId + '</a>', esc(o.userName), MH.date(o.orderDate), MH.money(o.totalAmount), MH.badge(o.orderStatus)];
    }), 'No orders yet. They will appear here as soon as customers check out.');
  };

  async function admin() {
    var d = await Promise.all([MH.api.get('/admin/dashboard'), get('/inventory', 'inventory'), get('/warranty-claims', 'warranty claims')]);
    var st = d[0], inv = d[1], claims = d[2];
    var low = inv.filter(function (i) { return i.stockStatus !== 'IN_STOCK'; });
    var pendingClaims = claims.filter(function (c) { return c.status === 'PENDING'; });
    return '<div class="stats">' +
      stat('Total users', st.totalUsers, st.customers + ' customers, ' + st.staff + ' staff', '', 'admin/users/', 'users') +
      stat('Total products', st.totalProducts, st.activeProducts + ' on sale', '', 'admin/products/', 'box') +
      stat('Total orders', st.totalOrders, st.ordersByStatus.DELIVERED + ' delivered', '', 'admin/orders/', 'clipboard') +
      stat('Total revenue', MH.moneyHtml(st.totalRevenue), 'From paid payments', 'gold', 'admin/payments/', 'wallet') +
      stat('Digital music', st.digitalMusic, st.activeDigitalMusic + ' available to customers', '', 'admin/digital-music/', 'music') +
      stat('Active advertisements', st.activeAdvertisements, 'Running on the home page', '', 'admin/advertisements/', 'image') +
      stat('Pending payments', st.pendingPayments, st.pendingPayments ? 'Orders waiting for payment' : 'Nothing waiting', st.pendingPayments ? 'warn' : '', 'admin/payments/', 'card') +
      stat('Pending orders', st.pendingOrders, st.pendingOrders ? 'Waiting to be confirmed' : 'All orders confirmed', st.pendingOrders ? 'warn' : '', 'admin/orders/', 'clock') + '</div>' +
      '<div class="two-col">' + panel('Revenue, last 6 months', revenueChart(st.revenueByMonth || []) + '<p class="muted" style="margin:12px 0 0;font-size:13px">Paid payments per month in LKR. The current month is highlighted.</p>') + panel('Orders by status', statusBars(st.ordersByStatus || {}), 'admin/orders/') + '</div>' +
      '<div class="two-col">' + panel('Recent orders', recentOrders(st.recentOrders || []), 'admin/orders/', true) +
      panel('Needs attention', '<div class="bars">' +
        attention('Low or out of stock products', st.lowStockProducts, 'admin/inventory/') +
        attention('Warranty claims waiting for a decision', st.pendingWarrantyClaims, 'admin/warranty-claims/') +
        attention('Promotions running now', st.activePromotions, 'admin/promotions/', true) +
        attention('Customer reviews (average ' + (st.totalReviews ? st.averageRating.toFixed(1) + ' stars' : 'none yet') + ')', st.totalReviews, 'admin/reviews/', true) + '</div>' +
        (low.length ? '<h4 style="margin:20px 0 8px">Restock soon</h4>' + miniTable(['Product', 'Left', 'Status'], low.slice(0, 5).map(function (i) { return [esc(i.productName), i.quantityAvailable, MH.badge(i.stockStatus)]; }), '') : '') +
        (pendingClaims.length ? '<h4 style="margin:20px 0 8px">Latest claims</h4>' + miniTable(['Product', 'Customer', 'Type'], pendingClaims.slice(0, 4).map(function (c) { return [esc(c.productName), esc(c.userName), MH.badge(c.claimType)]; }), '') : '')) + '</div>';
  }
  function attention(label, n, href, neutral) {
    return '<a class="row spread" href="' + MH.url(href) + '" style="padding:10px 12px;border:1px solid var(--line);border-radius:10px;color:inherit"><span>' + esc(label) + '</span><strong class="' + (!neutral && n ? 'amber' : '') + '" style="font-variant-numeric:tabular-nums">' + n + '</strong></a>';
  }

  async function sales() {
    var d = await Promise.all([get('/orders', 'orders'), get('/payments', 'payments'), get('/warranty-claims', 'warranty claims')]);
    var orders = d[0], pays = d[1], claims = d[2];
    var revenue = pays.filter(function (p) { return p.paymentStatus === 'PAID'; }).reduce(function (s, p) { return s + p.amount; }, 0);
    return '<div class="stats">' + stat('Total orders', orders.length, '', '', 'admin/orders/') +
      stat('Pending orders', count(orders, function (o) { return o.orderStatus === 'PENDING'; }), 'Not yet confirmed', count(orders, function (o) { return o.orderStatus === 'PENDING'; }) ? 'warn' : '', 'admin/orders/') +
      stat('To ship', count(orders, function (o) { return o.orderStatus === 'CONFIRMED' || o.orderStatus === 'PROCESSING'; }), 'Confirmed or processing', '', 'admin/orders/') +
      stat('Delivered', count(orders, function (o) { return o.orderStatus === 'DELIVERED'; }), '', '', 'admin/orders/') +
      stat('Revenue received', MH.moneyHtml(revenue), MH.plural(pays.length, 'payment'), '', 'admin/payments/') +
      stat('Open warranty claims', count(claims, function (c) { return c.status === 'PENDING' || c.status === 'APPROVED'; }), '', '', 'admin/warranty-claims/') + '</div>' +
      '<div class="two-col">' + panel('Recent orders', recentOrders(orders), 'admin/orders/', true) + panel('Orders by status', statusBars(countBy(orders))) + '</div>';
  }

  async function inventory() {
    var d = await Promise.all([get('/products', 'products'), get('/inventory', 'inventory'), get('/categories', 'categories')]);
    var products = d[0], inv = d[1], cats = d[2];
    var low = inv.filter(function (i) { return i.stockStatus !== 'IN_STOCK'; });
    var untracked = products.filter(function (p) { return p.stockStatus == null; });
    return '<div class="stats">' + stat('Products', products.length, MH.plural(cats.length, 'category', 'categories'), '', 'admin/products/') +
      stat('Units in stock', inv.reduce(function (s, i) { return s + (i.quantityAvailable || 0); }, 0), MH.plural(inv.length, 'stock record'), '', 'admin/inventory/') +
      stat('Low stock', count(inv, function (i) { return i.stockStatus === 'LOW_STOCK'; }), 'At or below reorder level', count(inv, function (i) { return i.stockStatus === 'LOW_STOCK'; }) ? 'warn' : '', 'admin/inventory/') +
      stat('Out of stock', count(inv, function (i) { return i.stockStatus === 'OUT_OF_STOCK'; }), '', count(inv, function (i) { return i.stockStatus === 'OUT_OF_STOCK'; }) ? 'bad' : '', 'admin/inventory/') +
      stat('Not tracked', untracked.length, 'Products without a stock record', '', 'admin/inventory/') + '</div>' +
      '<div class="two-col">' + panel('Needs restocking', miniTable(['Product', 'Location', 'Left', 'Status'], low.map(function (i) { return [esc(i.productName), esc(i.locationCode || '—'), i.quantityAvailable, MH.badge(i.stockStatus)]; }).slice(0, 10), 'Every tracked product is well stocked.'), 'admin/inventory/') +
      panel('Products without stock records', miniTable(['Product', 'Price'], untracked.slice(0, 8).map(function (p) { return [esc(p.name), MH.money(p.price)]; }), 'All products are tracked.'), 'admin/inventory/') + '</div>';
  }

  async function marketing() {
    var d = await Promise.all([get('/promotions', 'promotions'), get('/campaigns', 'campaigns'), get('/featured-products', 'featured products'), get('/products', 'products')]);
    var promos = d[0], camps = d[1], feat = d[2], products = d[3];
    return '<div class="stats">' + stat('Active promotions', count(promos, function (p) { return p.status === 'ACTIVE'; }), promos.length + ' in total', '', 'admin/promotions/') +
      stat('Upcoming promotions', count(promos, function (p) { return p.status === 'UPCOMING'; }), '', '', 'admin/promotions/') +
      stat('Active campaigns', count(camps, function (c) { return c.status === 'ACTIVE'; }), camps.length + ' in total', '', 'admin/campaigns/') +
      stat('Featured products', feat.length, 'Shown on the home page', '', 'admin/featured-products/') +
      stat('Products', products.length, '', '', 'admin/products/') + '</div>' +
      '<div class="two-col">' + panel('Promotions', miniTable(['Promotion', 'Product', 'Discount', 'Ends', 'Status'], promos.slice(0, 8).map(function (p) { return [esc(p.promotionName), esc(p.productName), (p.discountAmount > 0 ? MH.money(p.discountAmount) : p.discountPercentage + '%'), MH.date(p.endDate), MH.badge(p.status)]; }), 'No promotions yet. Create one to attract customers.'), 'admin/promotions/') +
      panel('Campaigns', miniTable(['Campaign', 'Ends', 'Status'], camps.slice(0, 8).map(function (c) { return [esc(c.campaignName), MH.date(c.endDate), MH.badge(c.status)]; }), 'No campaigns yet.'), 'admin/campaigns/') + '</div>';
  }

  async function artist() {
    var d = await Promise.all([get('/artists', 'artists'), get('/albums', 'albums'), get('/songs', 'songs'), get('/digital-music', 'digital music')]);
    var artists = d[0], albums = d[1], songs = d[2], dig = d[3];
    return '<div class="stats">' + stat('Artists', artists.length, '', '', 'admin/artists/') + stat('Albums', albums.length, '', '', 'admin/albums/') +
      stat('Songs', songs.length, '', '', 'admin/songs/') + stat('Digital tracks', dig.length, count(dig, function (x) { return x.status === 'ACTIVE'; }) + ' active', '', 'admin/digital-music/') +
      stat('Songs without an MP3', Math.max(0, songs.length - dig.length), '', songs.length > dig.length ? 'warn' : '', 'admin/digital-music/') + '</div>' +
      '<div class="two-col">' + panel('Recent albums', miniTable(['Album', 'Artist', 'Released'], albums.slice().sort(function (a, b) { return b.albumId - a.albumId; }).slice(0, 8).map(function (a) { return [esc(a.title), esc(a.artistName), MH.date(a.releaseDate)]; }), 'No albums yet.'), 'admin/albums/') +
      panel('Latest uploads', miniTable(['Song', 'Artist', 'Status'], dig.slice().sort(function (a, b) { return b.digitalMusicId - a.digitalMusicId; }).slice(0, 8).map(function (x) { return [esc(x.songTitle), esc(x.artistName), MH.badge(x.status)]; }), 'No music uploaded yet.'), 'admin/digital-music/') + '</div>';
  }

  async function supplier() {
    return '<div class="card"><div class="empty"><div class="glyph">' + MH.icon('handshake', 28) + '</div><h3>Supplier portal is not available yet</h3><p>Your account is active, but supplier self-service has not been set up. Contact a MelodyHub administrator for purchase orders and contracts.</p><a class="btn" href="' + MH.url('admin/profile/') + '">Open my profile</a></div></div>';
  }

  var BUILD = { ADMIN: admin, SALES_MANAGER: sales, INVENTORY_MANAGER: inventory, MARKETING_MANAGER: marketing, ARTIST: artist, SUPPLIER: supplier };
  content.innerHTML = '<div class="stats">' + new Array(8).fill('<div class="skeleton" style="height:112px"></div>').join('') + '</div>';
  BUILD[role]().then(function (html) {
    content.innerHTML = (failed.length ? '<div class="alert alert-warn">' + MH.icon('alert', 16) + '<span>Some figures could not be loaded (' + esc(failed.join(', ')) + '). Refresh the page to try again.</span></div>' : '') + html;
  }).catch(function (e) { content.innerHTML = '<div class="card">' + MH.errorState(e.message) + '</div>'; document.getElementById('retry').onclick = function () { location.reload(); }; });
})();
