(async function () {
  MH.renderHeader('categories'); MH.renderFooter();
  var box = document.getElementById('cats');
  try {
    var d = await Promise.all([MH.api.get('/categories', { auth: false }), MH.api.get('/products', { auth: false }).catch(function () { return []; })]);
    var cats = d[0], products = d[1].filter(MH.isSellable);
    if (!cats.length) { box.outerHTML = '<div class="card">' + MH.empty({ icon: 'layers', title: 'No categories yet', text: 'Categories will appear here once they are created.', actionHtml: MH.auth.getRole() === 'ADMIN' ? '<a class="btn btn-primary" href="' + MH.url('admin/categories/') + '">Create your first category</a>' : '' }) + '</div>'; return; }
    box.innerHTML = cats.map(function (c) {
      var n = products.filter(function (p) { return p.categoryId === c.categoryId; }).length;
      return '<a class="cat-card" href="' + MH.url('products/') + '?category=' + c.categoryId + '"><div class="cat-glyph">' + MH.img(null, c.categoryName, '', c.categoryName + ' ' + (c.description || '')) + '</div><div style="min-width:0"><h3>' + MH.esc(c.categoryName) + '</h3>' + (c.description ? '<p>' + MH.esc(c.description) + '</p>' : '') + '<p style="margin-top:6px;color:var(--gold-ink);font-weight:600">' + (n ? n + ' product' + (n > 1 ? 's' : '') : 'Coming soon') + '</p></div></a>';
    }).join('');
  } catch (e) { box.outerHTML = '<div class="card">' + MH.errorState(e.message, 'retry') + '</div>'; document.getElementById('retry').onclick = function () { location.reload(); }; }
})();
