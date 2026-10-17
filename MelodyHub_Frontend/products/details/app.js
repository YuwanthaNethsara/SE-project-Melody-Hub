/* Product detail page: product, price, stock, warranty, reviews and related products */
(async function () {
  var esc = MH.esc, root = document.getElementById('detail-root');
  MH.renderHeader('products'); MH.renderFooter();
  var id = MH.qs('id');
  if (!id || !MH.v.isId(id)) { root.innerHTML = MH.empty({ icon: 'search', title: id ? 'Product not found' : 'No product selected', text: id ? 'That product link is not valid.' : 'Pick a product from the catalog.', actionHtml: '<a class="btn btn-primary" href="../">Browse products</a>' }); return; }

  var p;
  try { p = await MH.api.get('/products/' + encodeURIComponent(id), { auth: false }); }
  catch (e) {
    root.innerHTML = e.status === 404 || e.status === 400
      ? MH.empty({ icon: 'search', title: 'Product not found', text: 'It may have been removed from the store.', actionHtml: '<a class="btn btn-primary" href="../">Browse products</a>' })
      : MH.errorState(e.message, 'retry');
    var r = document.getElementById('retry'); if (r) r.onclick = function () { location.reload(); };
    return;
  }
  document.title = p.name + ' · MelodyHub';
  var isCustomer = MH.auth.getRole() === 'CUSTOMER';

  var extra = await Promise.all([
    MH.api.get('/warranties/product/' + p.productId, { auth: false }).catch(function () { return null; }),
    MH.api.get('/products/category/' + p.categoryId, { auth: false }).catch(function () { return []; }),
    MH.loadPromoMap(),
    MH.api.get('/reviews/product/' + p.productId, { auth: false }).catch(function () { return []; }),
    MH.loadRatingMap(),
    isCustomer ? MH.api.get('/reviews/eligible').catch(function () { return []; }) : Promise.resolve([])
  ]);
  var warranty = extra[0], promoMap = extra[2], reviews = extra[3] || [], ratings = extra[4], eligible = extra[5] || [];
  var promo = promoMap[p.productId], av = MH.availability(p);
  var related = (extra[1] || []).filter(function (x) { return x.productId !== p.productId && MH.isSellable(x); }).slice(0, 4);
  var mine = eligible.filter(function (e) { return e.productId === p.productId; })[0];

  var maxQty = p.quantityAvailable != null ? Math.max(1, Math.min(99, p.quantityAvailable)) : 99;
  var avg = reviews.length ? reviews.reduce(function (s, r) { return s + r.rating; }, 0) / reviews.length : 0;

  root.innerHTML =
    MH.breadcrumbs([['Home', MH.url('index.html')], ['Shop', '../'], [p.categoryName, '../?category=' + p.categoryId], [p.name]]) +
    '<div class="detail"><div class="detail-media">' + MH.img(p.imageUrl, p.name, '', p.categoryName) + '</div><div>' +
    '<div class="row" style="gap:10px"><span class="badge badge-' + av.tone + '">' + esc(av.text) + '</span>' + (reviews.length ? '<a href="#reviews">' + MH.stars(avg, reviews.length) + '</a>' : '') + '</div>' +
    '<h1 style="margin-top:14px">' + esc(p.name) + '</h1>' +
    '<div class="detail-price">' + (promo ? MH.money(promo.discountedPrice) + '<s>' + MH.money(p.price) + '</s>' : MH.money(p.price)) + '</div>' +
    (promo ? '<p class="amber" style="margin:6px 0 0;font-weight:600">' + esc(promo.promotionName) + ': ' + esc(MH.promoTag(promo)) + ' until ' + MH.date(promo.endDate) + '</p>' : '') +
    '<p style="margin-top:16px">' + esc(p.description || 'No description has been added for this product yet.') + '</p>' +
    '<div class="buy-row"><div class="qty" role="group" aria-label="Quantity"><button type="button" id="minus" aria-label="Decrease quantity">−</button><input id="qty" type="number" min="1" max="' + maxQty + '" value="1" aria-label="Quantity"><button type="button" id="plus" aria-label="Increase quantity">+</button></div>' +
    '<button type="button" class="btn btn-primary btn-lg" id="add"' + (av.buyable ? '' : ' disabled') + '>' + (av.buyable ? MH.icon('cart', 17) + ' Add to cart' : esc(av.text)) + '</button></div><div class="field-error" id="qty-err" role="alert" style="margin:-12px 0 12px"></div>' +
    '<div class="info-box"><h4>' + MH.icon('shield', 17) + 'Warranty</h4>' + (warranty && warranty.status === 'ACTIVE'
      ? '<p>' + MH.plural(warranty.warrantyPeriodMonths, 'month') + ', ' + esc(MH.humanize(warranty.warrantyType).toLowerCase()) + ' warranty. Claims are made online from your orders.</p>' + (warranty.termsAndConditions ? '<p style="margin-top:6px;font-size:13.5px">' + esc(warranty.termsAndConditions) + '</p>' : '')
      : '<p>This product does not come with a warranty.</p>') + '</div>' +
    '<div class="info-box"><h4>' + MH.icon('box', 17) + 'Product details</h4><dl class="kv" style="grid-template-columns:120px minmax(0,1fr)"><dt>Category</dt><dd>' + esc(p.categoryName) + '</dd>' + (p.sku ? '<dt>SKU</dt><dd>' + esc(p.sku) + '</dd>' : '') + (p.quantityAvailable != null ? '<dt>Stock</dt><dd>' + (p.quantityAvailable > 0 ? p.quantityAvailable + ' available' : 'Out of stock') + '</dd>' : '') + '</dl></div></div></div>' +
    '<section class="section" id="reviews" style="padding-bottom:0"><div class="section-head"><div><h2>Customer reviews</h2><p>From customers who bought this product.</p></div><div id="review-action"></div></div><div id="review-body"></div></section>' +
    (related.length ? '<section class="section" style="padding-bottom:0"><div class="section-head"><div><h2>More in ' + esc(p.categoryName) + '</h2></div><a class="btn btn-sm" href="../?category=' + p.categoryId + '">View category</a></div><div class="grid grid-products" id="related">' + related.map(function (x) { return MH.productCard(x, promoMap[x.productId], ratings[x.productId]); }).join('') + '</div></section>' : '');

  // ---------- reviews ----------
  function drawReviews() {
    var avg = reviews.length ? reviews.reduce(function (s, r) { return s + r.rating; }, 0) / reviews.length : 0;
    var action = document.getElementById('review-action');
    if (isCustomer && mine) action.innerHTML = '<button type="button" class="btn btn-primary" id="write">' + MH.icon('edit', 15) + (mine.reviewId ? ' Edit your review' : ' Write a review') + '</button>';
    else if (!MH.auth.isLoggedIn()) action.innerHTML = '<a class="btn" href="' + MH.auth.loginUrl() + '">Log in to review</a>';
    else action.innerHTML = '';
    var w = document.getElementById('write'); if (w) w.onclick = write;
    var box = document.getElementById('review-body');
    if (!reviews.length) {
      box.innerHTML = '<div class="card">' + MH.empty({ icon: 'star', title: 'No reviews yet', text: isCustomer && mine ? 'You bought this product. Tell other customers what you think.' : 'Customers who buy this product can review it here.' }) + '</div>';
      return;
    }
    var dist = [5, 4, 3, 2, 1].map(function (n) {
      var c = reviews.filter(function (r) { return r.rating === n; }).length;
      return '<div class="bar-row"><span>' + n + ' star</span><div class="bar-track"><div class="bar-fill gold" style="width:' + (c / reviews.length * 100) + '%"></div></div><strong>' + c + '</strong></div>';
    }).join('');
    box.innerHTML = '<div class="reviews"><div class="review-summary card"><div class="big">' + avg.toFixed(1) + '</div>' + MH.stars(avg, null, 'lg') + '<p class="muted" style="margin:6px 0 0">' + reviews.length + ' review' + (reviews.length > 1 ? 's' : '') + '</p><div class="dist">' + dist + '</div></div>' +
      '<div class="review-list">' + reviews.map(function (r) { return MH.reviewItem(r); }).join('') + '</div></div>';
  }
  async function write() {
    var existing = mine.reviewId ? (await MH.api.get('/reviews/my').catch(function () { return []; })).filter(function (r) { return r.reviewId === mine.reviewId; })[0] : null;
    var saved = await MH.openReviewForm({ productId: p.productId, productName: p.name, review: existing });
    if (!saved) return;
    mine.reviewId = saved.reviewId;
    reviews = await MH.api.get('/reviews/product/' + p.productId, { auth: false }).catch(function () { return reviews; });
    drawReviews();
  }
  drawReviews();

  // ---------- quantity + cart ----------
  var qty = document.getElementById('qty'), qerr = document.getElementById('qty-err');
  // explains what is wrong instead of silently changing what the customer typed
  function qtyError() {
    var raw = qty.value.trim();
    if (raw === '' || qty.validity.badInput) return 'Quantity is required.';
    if (!/^[+-]?\d+(\.0+)?$/.test(raw)) return 'Quantity must be a whole number.';
    var n = Number(raw);
    if (n < 1) return 'Quantity must be at least 1.';
    if (p.quantityAvailable != null && n > p.quantityAvailable) return 'Only ' + p.quantityAvailable + ' available.';
    if (n > 99) return 'You can order at most 99 units of one product.';
    return '';
  }
  function showQty(m) { qerr.textContent = m; qty.setAttribute('aria-invalid', m ? 'true' : 'false'); }
  function current() { var n = parseInt(qty.value, 10); return isNaN(n) ? 1 : n; }
  document.getElementById('minus').onclick = function () { qty.value = Math.max(1, current() - 1); showQty(''); };
  document.getElementById('plus').onclick = function () { qty.value = Math.min(maxQty, current() + 1); showQty(''); };
  qty.oninput = function () { if (qerr.textContent) showQty(qtyError()); };
  document.getElementById('add').onclick = function () {
    var m = qtyError(); showQty(m);
    if (m) { qty.focus(); return; }
    MH.cart.add(p.productId, parseInt(qty.value, 10), this);
  };
  var rel = document.getElementById('related'); if (rel) MH.bindProductCards(rel);
})();
