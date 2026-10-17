/* Home page: everything below comes from the live API */
(async function () {
  var esc = MH.esc, $ = function (id) { return document.getElementById(id); };
  MH.renderHeader('home'); MH.renderFooter();

  $('why').innerHTML = [
    ['shield', 'Warranty you can claim', 'Every warranty is linked to your order, so you can open a claim or return online in a few clicks.'],
    ['tag', 'Prices checked at checkout', 'Totals and running offers are calculated by our server, so what you see is what you pay.'],
    ['box', 'Stock you can trust', 'Products are tracked in our own inventory and we tell you before an item runs out.'],
    ['music', 'Music from our artists', 'Albums, tracks and MP3 downloads published by the artists themselves.']
  ].map(function (w) { return '<div class="why"><h3>' + MH.icon(w[0], 18) + esc(w[1]) + '</h3><p>' + esc(w[2]) + '</p></div>'; }).join('');

  var user = MH.auth.getCurrentUser();
  if (user) {
    var cta = $('digital-cta');
    if (user.role === 'CUSTOMER') { cta.href = MH.url('customer/music/'); cta.textContent = 'Open my library'; }
    else if (user.role === 'ADMIN' || user.role === 'ARTIST') { cta.href = MH.url('admin/digital-music/'); cta.textContent = 'Manage digital music'; }
    else $('digital-section').hidden = true;
  }

  function safe(p) { return p.catch(function () { return null; }); }
  var data = await Promise.all([
    safe(MH.api.get('/products', { auth: false })), safe(MH.api.get('/featured-products/active', { auth: false })),
    safe(MH.api.get('/categories', { auth: false })), safe(MH.api.get('/artists', { auth: false })),
    MH.loadPromoMap(), safe(MH.api.get('/promotions/active', { auth: false })),
    safe(MH.api.get('/advertisements/active', { auth: false })), MH.loadRatingMap()
  ]);
  var products = data[0], featuredRaw = data[1], cats = data[2], artists = data[3], promoMap = data[4], promos = data[5], ads = data[6] || [], ratings = data[7];
  var byId = {}; (products || []).forEach(function (p) { byId[p.productId] = p; });
  var sellable = (products || []).filter(MH.isSellable);

  // ---------- hero stage: running advertisements first, otherwise featured products ----------
  var slides = ads.map(function (a) {
    return { kicker: a.campaignName || 'MelodyHub', title: a.title, text: a.content, image: a.imageUrl, hint: 'banner ' + a.title, href: MH.url('products/'), cta: 'Shop now' };
  });
  if (!slides.length) {
    var feat = (featuredRaw || []).map(function (f) { return byId[f.productId]; }).filter(function (p) { return p && MH.isSellable(p); });
    if (!feat.length) feat = sellable.slice().sort(function (a, b) { return b.productId - a.productId; });
    slides = feat.slice(0, 4).map(function (p) {
      var pr = promoMap[p.productId];
      return { kicker: pr ? pr.promotionName : (p.categoryName || 'Featured'), title: p.name, text: (pr ? 'Now ' + MH.money(pr.discountedPrice) + ', was ' + MH.money(p.price) + '. ' : MH.money(p.price) + '. ') + (p.description || ''), image: p.imageUrl, hint: p.categoryName, href: MH.url('products/details/') + '?id=' + p.productId, cta: 'View product' };
    });
  }
  var stage = $('hero-stage'), current = 0, timer = null;
  function drawStage() {
    if (!slides.length) {
      stage.innerHTML = MH.eqBars(24, [9, 10, 13]) + '<div class="stage-note">New arrivals will appear here.</div>';
      return;
    }
    var s = slides[current];
    stage.innerHTML = MH.eqBars(24, [7, 9, 12]) +
      '<a class="stage-card" href="' + s.href + '" style="color:inherit"><div class="media">' + MH.img(s.image, s.title, '', s.hint) + '</div>' +
      '<div><div class="kicker">' + esc(s.kicker) + '</div><h3>' + esc(s.title) + '</h3><p>' + esc(s.text || '') + '</p><span class="btn btn-sm btn-primary">' + esc(s.cta) + '</span></div></a>' +
      (slides.length > 1 ? '<div class="stage-dots">' + slides.map(function (_, i) { return '<button type="button" aria-label="Show item ' + (i + 1) + ' of ' + slides.length + '"' + (i === current ? ' class="on" aria-current="true"' : '') + ' data-i="' + i + '"></button>'; }).join('') + '</div>' : '');
    stage.querySelectorAll('.stage-dots button').forEach(function (b) { b.onclick = function () { current = Number(b.dataset.i); drawStage(); restart(); }; });
  }
  function restart() {
    clearInterval(timer);
    if (slides.length > 1 && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      timer = setInterval(function () { current = (current + 1) % slides.length; drawCard(); }, 6500);
    }
  }
  // later slides only swap the card, so the equalizer animation plays once
  function drawCard() {
    var s = slides[current], card = stage.querySelector('.stage-card');
    if (!card) return drawStage();
    card.href = s.href;
    card.innerHTML = '<div class="media">' + MH.img(s.image, s.title, '', s.hint) + '</div><div><div class="kicker">' + esc(s.kicker) + '</div><h3>' + esc(s.title) + '</h3><p>' + esc(s.text || '') + '</p><span class="btn btn-sm btn-primary">' + esc(s.cta) + '</span></div>';
    stage.querySelectorAll('.stage-dots button').forEach(function (b, i) { b.classList.toggle('on', i === current); if (i === current) b.setAttribute('aria-current', 'true'); else b.removeAttribute('aria-current'); });
  }
  drawStage(); restart();

  // ---------- featured ----------
  var featured = $('featured');
  if (!products) {
    featured.outerHTML = '<div class="card">' + MH.errorState('The catalog could not be loaded. Check that the backend is running, then try again.', 'retry') + '</div>';
    $('retry').onclick = function () { location.reload(); };
  } else {
    var list = (featuredRaw || []).map(function (f) { return byId[f.productId]; }).filter(function (p) { return p && MH.isSellable(p); });
    if (!list.length) {
      $('featured-title').textContent = 'New in the store';
      $('featured-sub').textContent = 'The latest products added to the catalog.';
      list = sellable.slice().sort(function (a, b) { return b.productId - a.productId; });
    }
    list = list.slice(0, 8);
    if (!list.length) featured.outerHTML = '<div class="card">' + MH.empty({ icon: 'box', title: 'No products available yet', text: 'New stock is on the way. Please check back soon.', actionHtml: MH.auth.getRole() === 'ADMIN' ? '<a class="btn btn-primary" href="' + MH.url('admin/products/') + '">Add your first product</a>' : '' }) + '</div>';
    else { featured.innerHTML = list.map(function (p) { return MH.productCard(p, promoMap[p.productId], ratings[p.productId]); }).join(''); MH.bindProductCards(featured); }

    $('hero-facts').innerHTML = '<div><dt>products in stock</dt><dd>' + sellable.filter(function (p) { return MH.availability(p).buyable; }).length + '</dd></div>' +
      (cats ? '<div><dt>categories</dt><dd>' + cats.length + '</dd></div>' : '') + (artists ? '<div><dt>artists</dt><dd>' + artists.length + '</dd></div>' : '');

    var catBox = $('cats');
    if (cats && cats.length) {
      catBox.innerHTML = cats.map(function (c) {
        var n = sellable.filter(function (p) { return p.categoryId === c.categoryId; }).length;
        return '<a class="cat-card" href="' + MH.url('products/') + '?category=' + c.categoryId + '"><div class="cat-glyph">' + MH.img(null, c.categoryName, '', c.categoryName + ' ' + (c.description || '')) + '</div><div><h3>' + esc(c.categoryName) + '</h3><p>' + (n ? n + ' product' + (n > 1 ? 's' : '') : 'Coming soon') + '</p></div></a>';
      }).join('');
    } else catBox.innerHTML = '<p class="muted">Categories will appear here once they are created.</p>';
  }

  // ---------- offers ----------
  if (promos && promos.length) {
    $('promo-section').hidden = false;
    $('promos').innerHTML = promos.slice(0, 6).map(function (p) {
      var prod = byId[p.productId] || {};
      return '<a class="offer" href="' + MH.url('products/details/') + '?id=' + p.productId + '"><div class="media">' + MH.img(prod.imageUrl, p.productName, '', prod.categoryName) + '</div>' +
        '<div><span class="off">' + esc(MH.promoTag(p)) + '</span><h3>' + esc(p.productName) + '</h3><div class="price"><strong>' + MH.money(p.discountedPrice) + '</strong><s>' + MH.money(p.originalPrice) + '</s></div><div class="meta">' + esc(p.promotionName) + ', ends ' + MH.date(p.endDate) + '</div></div></a>';
    }).join('');
  }

  // ---------- second advertisement as a banner ----------
  if (ads.length > 1) {
    var a = ads[1];
    $('ad-section').hidden = false;
    $('ad-banner').innerHTML = '<div class="ad-banner"><div class="copy"><span class="campaign">' + esc(a.campaignName || '') + '</span><h2>' + esc(a.title) + '</h2><p>' + esc(a.content) + '</p><div><a class="btn btn-primary" href="' + MH.url('products/') + '">Shop now</a></div></div><div class="media">' + MH.img(a.imageUrl, a.title, '', 'banner ' + a.title) + '</div></div>';
  }

  // ---------- artists ----------
  if (artists && artists.length) {
    $('artist-section').hidden = false;
    $('artists').innerHTML = artists.slice(0, 4).map(function (a) {
      return MH.artistCard(a);
    }).join('');
  }
})();
