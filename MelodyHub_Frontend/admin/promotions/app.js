/* Promotions management page (uses the shared CRUD engine) */
(function () {
  function discountText(r) {
    return r.discountAmount > 0 ? MH.money(r.discountAmount) + ' off' : MH.esc(r.discountPercentage) + '%';
  }
  MH.crud({
    module: 'promotions', title: 'Promotions', subtitle: 'Percentage or fixed-amount discounts. Active promotions are applied at checkout automatically.',
    singular: 'Promotion', icon: 'tag',
    endpoint: '/promotions', idKey: 'promotionId', nameKey: 'promotionName',
    viewRoles: ['ADMIN', 'MARKETING_MANAGER'], writeRoles: ['ADMIN', 'MARKETING_MANAGER'], defaultSort: 'promotionId',
    searchKeys: ['promotionName', 'productName', 'status'],
    filters: [{ key: 'status', label: 'Status' }],
    // product prices, used to check that a fixed discount is smaller than the price
    preload: async function () {
      var list = await MH.api.get('/products'), prices = {};
      list.forEach(function (p) { prices[p.productId] = Number(p.price); });
      return { prices: prices };
    },
    columns: [
      { key: 'promotionName', label: 'Promotion' }, { key: 'productName', label: 'Product' },
      { key: 'discountPercentage', label: 'Discount', render: discountText },
      { key: 'discountedPrice', label: 'Sale price', type: 'money' },
      { key: 'startDate', label: 'Starts', type: 'date' }, { key: 'endDate', label: 'Ends', type: 'date' }, { key: 'status', label: 'Status', type: 'badge' }
    ],
    fields: [
      { name: 'promotionName', label: 'Promotion name', kind: 'name', required: true, maxlength: 255, full: true },
      { name: 'productId', label: 'Product', type: 'select', numeric: true, required: true, full: true, optionsFrom: { endpoint: '/products', value: 'productId', label: 'name' } },
      { name: 'discountPercentage', label: 'Discount (%)', errorLabel: 'Discount', type: 'number', kind: 'percent', min: 0.01, max: 100, step: '0.01', help: 'Fill in this OR the fixed amount.' },
      { name: 'discountAmount', label: 'Fixed discount (LKR)', errorLabel: 'Fixed discount', type: 'number', kind: 'money', step: '0.01', help: 'Amount taken off the price.' },
      { name: 'startDate', label: 'Start date', type: 'date', required: true, minDate: '2000-01-01', maxDate: 'today+10y' },
      { name: 'endDate', label: 'End date', type: 'date', required: true, minDate: '2000-01-01', maxDate: 'today+10y' },
      { name: 'description', label: 'Description', type: 'textarea', full: true, maxlength: 1000 }
    ],
    // an existing fixed-amount promotion is stored with a 0% percentage; show that as an empty box
    toForm: function (r) {
      var fixed = r.discountAmount > 0;
      return Object.assign({}, r, { discountPercentage: fixed ? '' : r.discountPercentage, discountAmount: fixed ? r.discountAmount : '' });
    },
    // rules that involve several fields (the single-field rules are in the field specs above)
    validate: function (v, row, editing, ctx) {
      var errs = {}, hasPct = v.discountPercentage !== '', hasAmt = v.discountAmount !== '';
      if (!hasPct && !hasAmt) errs.discountPercentage = 'Enter a discount percentage or a fixed discount amount.';
      else if (hasPct && hasAmt) { errs.discountPercentage = 'Choose either a percentage or a fixed amount, not both.'; errs.discountAmount = errs.discountPercentage; }
      else if (hasAmt && v.productId && ctx && ctx.prices && ctx.prices[v.productId] != null && Number(v.discountAmount) >= ctx.prices[v.productId]) {
        errs.discountAmount = 'The fixed discount must be less than the product price (' + MH.money(ctx.prices[v.productId]) + ').';
      }
      var d = MH.v.dateRange(v.startDate, v.endDate);
      if (d) errs.endDate = d;
      return errs;
    },
    toPayload: function (p) { if (p.discountPercentage == null) delete p.discountPercentage; if (p.discountAmount == null) delete p.discountAmount; return p; }
  });
})();
