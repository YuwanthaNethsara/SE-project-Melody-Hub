/* My reviews: products the customer bought, with their rating and comment (create, edit, delete). */
(function () {
  if (!MH.guards.requireRole(['CUSTOMER'])) return;
  var esc = MH.esc;
  var content = MH.mountPortal({ active: 'c-reviews', title: 'My reviews', subtitle: 'Rate the products you have bought. Your reviews help other customers choose.' });

  async function load() {
    content.innerHTML = MH.skeletonRows(4);
    try {
      var r = await Promise.all([MH.api.get('/reviews/eligible'), MH.api.get('/reviews/my')]);
      draw(r[0] || [], r[1] || []);
    } catch (e) { content.innerHTML = MH.errorState(e.message); document.getElementById('retry').onclick = load; }
  }

  function draw(eligible, mine) {
    var byId = {}; mine.forEach(function (r) { byId[r.reviewId] = r; });
    var todo = eligible.filter(function (e) { return !e.reviewId; });
    var html = '';
    if (todo.length) {
      html += '<section class="card"><div class="panel-head"><h3 class="panel-title">Waiting for your review</h3><span class="muted">' + todo.length + ' product' + (todo.length > 1 ? 's' : '') + '</span></div><div class="track-list">' + todo.map(function (e) {
        return '<div class="track"><div class="cover" style="background:var(--canvas)">' + MH.img(e.imageUrl, e.productName) + '</div><div style="min-width:0"><div class="title">' + esc(e.productName) + '</div><div class="meta">You bought this product</div></div>' +
          '<div class="tail"><button type="button" class="btn btn-sm btn-primary" data-write="' + e.productId + '" data-name="' + esc(e.productName) + '">' + MH.icon('star', 15) + ' Write a review</button></div></div>';
      }).join('') + '</div></section>';
    }
    html += '<section class="card" style="margin-top:16px"><h3 class="panel-title">Your reviews</h3>' + (mine.length ? '<div class="review-list">' + mine.map(function (r) {
      return '<div class="review"><div class="row spread" style="align-items:flex-start"><div style="min-width:0"><a class="strong" href="' + MH.url('products/details/') + '?id=' + r.productId + '#reviews" style="font-weight:600">' + esc(r.productName) + '</a><div style="margin-top:4px">' + MH.stars(r.rating) + (r.status === 'HIDDEN' ? ' <span class="badge badge-bad" title="Hidden by MelodyHub staff">Hidden by staff</span>' : '') + '</div></div>' +
        '<div class="row" style="gap:6px"><button type="button" class="btn btn-sm" data-edit="' + r.reviewId + '">' + MH.icon('edit', 14) + ' Edit</button><button type="button" class="btn btn-sm btn-danger" data-del="' + r.reviewId + '">' + MH.icon('trash', 14) + ' Delete</button></div></div>' +
        (r.title ? '<h4>' + esc(r.title) + '</h4>' : '') + '<p>' + esc(r.comment) + '</p><div class="who"><span>' + (r.updatedAt && r.updatedAt !== r.createdAt ? 'Edited ' + MH.date(r.updatedAt) : 'Posted ' + MH.date(r.createdAt)) + '</span></div></div>';
    }).join('') + '</div>' : MH.empty({ icon: 'star', title: 'No reviews yet', text: eligible.length ? 'Pick a product above and tell other customers what you think.' : 'After you buy and pay for a product, you can review it here.', actionHtml: eligible.length ? '' : '<a class="btn btn-primary" href="' + MH.url('products/') + '">Browse products</a>' })) + '</section>';
    content.innerHTML = html;

    content.onclick = async function (ev) {
      var w = ev.target.closest('[data-write]'), ed = ev.target.closest('[data-edit]'), del = ev.target.closest('[data-del]');
      if (w && await MH.openReviewForm({ productId: Number(w.dataset.write), productName: w.dataset.name })) load();
      if (ed) { var r = byId[ed.dataset.edit]; if (await MH.openReviewForm({ productId: r.productId, productName: r.productName, review: r })) load(); }
      if (del) {
        var rv = byId[del.dataset.del];
        if (!await MH.confirm({ title: 'Delete review?', html: 'Your review of <strong>' + esc(rv.productName) + '</strong> will be removed from the store.', confirmText: 'Delete review', danger: true })) return;
        MH.busy(del, true);
        try { await MH.api.del('/reviews/' + rv.reviewId); MH.ok('Review deleted.'); load(); } catch (e) { MH.fail(e); MH.busy(del, false); }
      }
    };
  }
  load();
})();
