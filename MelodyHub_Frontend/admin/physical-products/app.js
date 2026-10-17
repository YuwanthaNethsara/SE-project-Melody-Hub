/* Physical product details management page (uses the shared CRUD engine) */
MH.crud({
  module: 'physical-products', title: 'Physical details', subtitle: 'Weight and shipping class for products that ship in a box.',
  singular: 'Physical detail', icon: 'truck',
  endpoint: '/physical-products', idKey: 'productId', nameKey: 'productId',
  viewRoles: ['ADMIN', 'INVENTORY_MANAGER'], writeRoles: ['ADMIN', 'INVENTORY_MANAGER'],
  searchKeys: ['productId', 'shoppingClass'],
  preload: async function () {
    var list = await MH.api.get('/products'), map = {};
    list.forEach(function (p) { map[p.productId] = p.name; });
    return { names: map };
  },
  columns: [
    { key: 'productId', label: 'Product', render: function (r, c) { return MH.esc((c.names && c.names[r.productId]) || 'Product #' + r.productId); } },
    { key: 'weight', label: 'Weight (kg)', type: 'number' },
    { key: 'shoppingClass', label: 'Shipping class' }
  ],
  fields: [
    { name: 'productId', label: 'Product', type: 'select', numeric: true, required: true, lockOnEdit: true, full: true, optionsFrom: { endpoint: '/products', value: 'productId', label: 'name' } },
    { name: 'weight', label: 'Weight (kg)', type: 'number', kind: 'decimal', required: true, positive: true, max: 10000, decimals: 3, step: '0.001' },
    { name: 'shoppingClass', label: 'Shipping class', kind: 'name', required: true, minlength: 2, maxlength: 50, placeholder: 'e.g. Standard, Fragile' }
  ]
});
