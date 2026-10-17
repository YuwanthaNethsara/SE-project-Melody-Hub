/* Feedback / reviews: ADMIN and MARKETING_MANAGER read every customer review and hide or remove inappropriate ones. */
(function () {
  var esc = MH.esc;
  function setStatus(status) {
    return async function (r, ctx) {
      MH.busy(ctx.button, true);
      try {
        await MH.api.put('/reviews/' + r.reviewId + '/status', undefined, { params: { status: status } });
        MH.ok(status === 'HIDDEN' ? 'Review hidden from the store.' : 'Review is visible again.');
        ctx.reload();
      } catch (e) { MH.fail(e); MH.busy(ctx.button, false); }
    };
  }
  MH.crud({
    module: 'reviews', title: 'Feedback / reviews', subtitle: 'Ratings and comments from customers who bought a product. Hide a review to remove it from the store without deleting it.',
    singular: 'Review', icon: 'message', noCreate: true, strongColumn: 1,
    endpoint: '/reviews', idKey: 'reviewId', nameKey: 'productName',
    viewRoles: ['ADMIN', 'MARKETING_MANAGER'], writeRoles: ['ADMIN', 'MARKETING_MANAGER'],
    canEdit: function () { return false; }, defaultSort: 'createdAt',
    searchKeys: ['productName', 'reviewerName', 'userEmail', 'title', 'comment'],
    filters: [{ key: 'status', label: 'Status', options: ['VISIBLE', 'HIDDEN'] }, { key: 'rating', label: 'Rating', options: [5, 4, 3, 2, 1], raw: true }],
    columns: [
      { key: 'rating', label: 'Rating', render: function (r) { return MH.stars(r.rating); } },
      { key: 'productName', label: 'Product' },
      { key: 'comment', label: 'Review', sortable: false, render: function (r) { var t = (r.title ? r.title + ': ' : '') + (r.comment || ''); return '<span class="clip" style="max-width:200px" title="' + esc(t) + '">' + esc(t) + '</span>'; } },
      { key: 'reviewerName', label: 'Customer', render: function (r) { return '<span title="' + esc(r.userEmail || '') + '">' + esc(r.reviewerName) + '</span>'; } },
      { key: 'status', label: 'Status', type: 'badge' },
      { key: 'createdAt', label: 'Posted', type: 'date', low: true }
    ],
    viewFields: [
      { key: 'productName', label: 'Product' }, { key: 'rating', label: 'Rating', render: function (r) { return MH.stars(r.rating) + ' <span class="muted">' + r.rating + ' of 5</span>'; } },
      { key: 'title', label: 'Headline' }, { key: 'comment', label: 'Review' }, { key: 'reviewerName', label: 'Customer' }, { key: 'userEmail', label: 'Email' },
      { key: 'status', label: 'Status', type: 'badge' }, { key: 'createdAt', label: 'Posted', type: 'datetime' }, { key: 'updatedAt', label: 'Last edited', type: 'datetime' }
    ],
    rowActions: [
      { label: 'Hide', show: function (r) { return r.status === 'VISIBLE'; }, run: setStatus('HIDDEN') },
      { label: 'Show', cls: 'btn-primary', show: function (r) { return r.status === 'HIDDEN'; }, run: setStatus('VISIBLE') }
    ],
    empty: { title: 'No reviews yet', readOnlyText: 'Customer reviews appear here after customers rate products they bought.' }
  });
})();
