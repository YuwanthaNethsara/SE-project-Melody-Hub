/* Featured products management page (uses the shared CRUD engine) */
MH.crud({
  module: 'featured-products', title: 'Featured products', subtitle: 'Products highlighted on the store home page.',
  singular: 'Featured product', icon: 'star',
  endpoint: '/featured-products', idKey: 'featuredProductId', nameKey: 'productName',
  viewRoles: ['ADMIN', 'MARKETING_MANAGER'], writeRoles: ['ADMIN', 'MARKETING_MANAGER'], createLabel: 'Feature a product', defaultSort: 'featuredProductId',
  searchKeys: ['productName', 'status'],
  filters: [{ key: 'status', label: 'Status' }],
  columns: [
    { key: 'imageUrl', label: 'Image', type: 'image', alt: 'productName' }, { key: 'productName', label: 'Product' },
    { key: 'price', label: 'Price', type: 'money' }, { key: 'status', label: 'Status', type: 'badge' }, { key: 'featuredDate', label: 'Featured on', type: 'datetime' }
  ],
  fields: [
    { name: 'productId', label: 'Product', type: 'select', numeric: true, required: true, full: true, optionsFrom: { endpoint: '/products', value: 'productId', label: 'name' } }
  ],
  empty: { title: 'Nothing featured yet', text: 'Feature a product to show it on the home page.' }
});
