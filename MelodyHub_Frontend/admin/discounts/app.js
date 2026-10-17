/* Discounts: the price reductions that promotions create, shown per product with the price the customer pays.
   Discounts are stored as promotions (percentage or fixed amount), so they are created and edited on the Promotions page. */
(function () {
  var esc = MH.esc;
  MH.crud({
    module: 'discounts', title: 'Discounts', subtitle: 'What each promotion takes off the price, and what customers pay at checkout.',
    singular: 'Discount', icon: 'percent', noCreate: true, strongColumn: 0,
    endpoint: '/promotions', idKey: 'promotionId', nameKey: 'promotionName',
    viewRoles: ['ADMIN', 'MARKETING_MANAGER'], writeRoles: ['ADMIN', 'MARKETING_MANAGER'],
    canEdit: function () { return false; }, noDelete: true, defaultSort: 'endDate',
    extraActionsHtml: '<a class="btn btn-primary" href="' + MH.url('admin/promotions/') + '">' + MH.icon('tag', 16) + ' Manage promotions</a>',
    introHtml: '<div class="alert alert-info">' + MH.icon('info', 16) + '<span>A discount is part of a promotion. To add, change or remove a discount, open <a href="' + MH.url('admin/promotions/') + '" style="font-weight:600">Promotions</a>. When several promotions cover one product, the customer always gets the lowest price.</span></div>',
    searchKeys: ['productName', 'promotionName', 'status', 'discountType'],
    filters: [{ key: 'status', label: 'Status', options: ['ACTIVE', 'UPCOMING', 'EXPIRED'] }, { key: 'discountType', label: 'Type' }],
    columns: [
      { key: 'productName', label: 'Product' },
      { key: 'promotionName', label: 'Promotion' },
      { key: 'discountType', label: 'Discount', render: function (r) { return r.discountAmount > 0 ? esc(MH.money(r.discountAmount)) + ' off' : esc(r.discountPercentage) + '% off'; } },
      { key: 'originalPrice', label: 'Regular price', type: 'money' },
      { key: 'discountedPrice', label: 'Customer pays', type: 'money' },
      { key: 'saving', label: 'Saving', sortable: false, render: function (r) { var s = (r.originalPrice || 0) - (r.discountedPrice || 0); return '<span class="amber" style="font-weight:600">' + MH.money(s) + '</span>'; } },
      { key: 'endDate', label: 'Valid', render: function (r) { return MH.date(r.startDate) + ' to ' + MH.date(r.endDate); } },
      { key: 'status', label: 'Status', type: 'badge' }
    ],
    empty: { title: 'No discounts yet', readOnlyText: 'Create a promotion to give customers a percentage or fixed amount off a product.' }
  });
})();
