/* Product listing: filters run in the browser over the live catalog */
(async function () {
  var esc = MH.esc;
  MH.renderHeader('products'); MH.renderFooter();
  var grid = document.getElementById('grid');
  grid.innerHTML = MH.skeletonCards(8);

  var products, cats, promoMap, ratings = {};
  try {
    var d = await Promise.all([MH.api.get('/products', { auth: false }), MH.api.get('/categories', { auth: false }).catch(function () { return []; }), MH.loadPromoMap(), MH.loadRatingMap()]);
    products = d[0].filter(MH.isSellable); cats = d[1]; promoMap = d[2]; ratings = d[3];
  } catch (e) {
    grid.innerHTML = '<div class="card" style="grid-column:1/-1">' + MH.errorState(e.message, 'retry') + '</div>';
    document.getElementById('retry').onclick = function () { location.reload(); };
    return;
  }

  var st = { category: MH.qs('category') || '', q: MH.qs('q') || '', deals: MH.qs('deals') === '1', stock: false, max: '', sort: 'new' };
  var $ = function (id) { return document.getElementById(id); };
  $('q').value = st.q; $('f-deals').checked = st.deals;

  function effective(p) { var pr = promoMap[p.productId]; return pr ? pr.discountedPrice : Number(p.price); }

  function drawCats() {
    var all = '<button data-cat="" class="' + (st.category === '' ? 'active' : '') + '"><span>All products</span><span>' + products.length + '</span></button>';
    $('cat-list').innerHTML = all + cats.map(function (c) {
      var n = products.filter(function (p) { return p.categoryId === c.categoryId; }).length;
      return '<button data-cat="' + c.categoryId + '" class="' + (String(c.categoryId) === st.category ? 'active' : '') + '"><span>' + esc(c.categoryName) + '</span><span>' + n + '</span></button>';
    }).join('');
  }

  function draw() {
    var q = st.q.trim().toLowerCase();
    var list = products.filter(function (p) {
      if (st.category && String(p.categoryId) !== st.category) return false;
      if (q && (p.name + ' ' + (p.description || '') + ' ' + (p.categoryName || '')).toLowerCase().indexOf(q) < 0) return false;
      if (st.stock && !MH.availability(p).buyable) return false;
      if (st.deals && !promoMap[p.productId]) return false;
      if (st.max !== '' && effective(p) > Number(st.max)) return false;
      return true;
    });
    list.sort(function (a, b) {
      if (st.sort === 'low') return effective(a) - effective(b);
      if (st.sort === 'high') return effective(b) - effective(a);
      if (st.sort === 'name') return a.name.localeCompare(b.name);
      if (st.sort === 'rating') return ((ratings[b.productId] || {}).averageRating || 0) - ((ratings[a.productId] || {}).averageRating || 0);
      return b.productId - a.productId;
    });
    $('count').textContent = list.length + ' product' + (list.length === 1 ? '' : 's') + (st.q.trim() ? ' for "' + st.q.trim() + '"' : '');
    var c = cats.filter(function (x) { return String(x.categoryId) === st.category; })[0];
    $('shop-title').textContent = c ? c.categoryName : (st.deals ? 'Offers' : 'Shop');
    if (!products.length) grid.innerHTML = '<div class="card" style="grid-column:1/-1">' + MH.empty({ icon: 'box', title: 'No products available yet', text: 'Please check back soon.', actionHtml: MH.auth.getRole() === 'ADMIN' ? '<a class="btn btn-primary" href="' + MH.url('admin/products/') + '">Create your first product</a>' : '' }) + '</div>';
    else if (!list.length) grid.innerHTML = '<div class="card" style="grid-column:1/-1">' + MH.empty({ icon: 'search', title: 'Nothing matches those filters', text: 'Try a different search or clear the filters.' }) + '</div>';
    else grid.innerHTML = list.map(function (p) { return MH.productCard(p, promoMap[p.productId], ratings[p.productId]); }).join('');
  }

  drawCats(); draw(); MH.bindProductCards(grid);
  $('cat-list').onclick = function (e) { var b = e.target.closest('[data-cat]'); if (!b) return; st.category = b.dataset.cat; drawCats(); draw(); };
  $('q').oninput = MH.debounce(function () { st.q = $('q').value; draw(); }, 200);
  $('sort').onchange = function () { st.sort = $('sort').value; draw(); };
  $('f-stock').onchange = function () { st.stock = this.checked; draw(); };
  $('f-deals').onchange = function () { st.deals = this.checked; draw(); };
  $('f-max').oninput = MH.debounce(function () { st.max = $('f-max').value; draw(); }, 250);
  $('f-clear').onclick = function () { st = { category: '', q: '', deals: false, stock: false, max: '', sort: st.sort }; $('q').value = ''; $('f-stock').checked = $('f-deals').checked = false; $('f-max').value = ''; drawCats(); draw(); };
  $('filter-toggle').onclick = function () { var o = $('filters').classList.toggle('open'); this.setAttribute('aria-expanded', o); };
})();
