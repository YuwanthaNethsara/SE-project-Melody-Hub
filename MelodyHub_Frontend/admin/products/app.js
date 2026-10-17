/* Products management page (uses the shared CRUD engine) */
MH.crud({
  module: 'products', title: 'Products', subtitle: 'Everything customers can browse and buy in the store.', strongColumn: 1,
  singular: 'Product', icon: 'box',
  endpoint: '/products', idKey: 'productId', nameKey: 'name',
  viewRoles: ['ADMIN', 'INVENTORY_MANAGER', 'MARKETING_MANAGER', 'SALES_MANAGER'], writeRoles: ['ADMIN', 'INVENTORY_MANAGER'],
  defaultSort: 'productId', searchKeys: ['name', 'sku', 'categoryName', 'status'],
  filters: [{ key: 'categoryName', label: 'Category' }, { key: 'status', label: 'Status' }],
  columns: [
    { key: 'imageUrl', label: 'Image', type: 'image' },
    { key: 'name', label: 'Name' },
    { key: 'categoryName', label: 'Category' },
    { key: 'price', label: 'Price', type: 'money' },
    { key: 'status', label: 'Status', type: 'badge' },
    { key: 'stockStatus', label: 'Stock', render: function (r) { return r.stockStatus ? MH.badge(r.stockStatus) + ' <span class="faint">(' + r.quantityAvailable + ')</span>' : '<span class="faint">Not tracked</span>'; } }
  ],
  viewFields: [
    { key: 'imageUrl', label: 'Image', type: 'image' }, { key: 'name', label: 'Name' }, { key: 'sku', label: 'SKU' }, { key: 'description', label: 'Description' },
    { key: 'categoryName', label: 'Category' }, { key: 'price', label: 'Price', type: 'money' }, { key: 'productType', label: 'Type', type: 'badge' },
    { key: 'status', label: 'Status', type: 'badge' }, { key: 'stockStatus', label: 'Stock', type: 'badge' }, { key: 'quantityAvailable', label: 'Units available' }, { key: 'createdAt', label: 'Created', type: 'datetime' }
  ],
  fields: [
    { name: 'name', label: 'Product name', kind: 'name', required: true, maxlength: 255, full: true },
    { name: 'sku', label: 'SKU / product code', errorLabel: 'SKU', kind: 'sku', maxlength: 50, placeholder: 'e.g. GTR-STR-001', help: 'Optional. Must be unique. Saved in capital letters.' },
    { name: 'categoryId', label: 'Category', type: 'select', numeric: true, required: true, optionsFrom: { endpoint: '/categories', value: 'categoryId', label: 'categoryName' } },
    { name: 'price', label: 'Price (LKR)', type: 'number', kind: 'money', required: true, min: 0.01, step: '0.01' },
    { name: 'status', label: 'Status', type: 'select', required: true, options: ['ACTIVE', 'INACTIVE', 'DISCONTINUED'], help: 'Inactive and discontinued products cannot be ordered.' },
    { name: 'productType', label: 'Product type', type: 'select', required: true, options: ['PHYSICAL'] },
    { name: 'image', label: 'Product image', type: 'file', what: 'image', full: true, requiredOnCreate: true, requiredMsg: 'Please choose a product image.', typeMsg: 'Please choose a JPG, JPEG, PNG or WEBP image.', existingValueKey: 'imageUrl', fileHint: 'Square photos look best.' },
    { name: 'description', label: 'Description', type: 'textarea', full: true, maxlength: 1000 }
  ],
  defaults: { status: 'ACTIVE', productType: 'PHYSICAL' },
  empty: { title: 'No products yet', text: 'Create your first product so customers have something to browse.' }
});
