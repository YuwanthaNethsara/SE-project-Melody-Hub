/* Categories management page (uses the shared CRUD engine) */
MH.crud({
  module: 'categories', title: 'Categories', subtitle: 'Group products so customers can find them.',
  singular: 'Category', icon: 'layers',
  endpoint: '/categories', idKey: 'categoryId', nameKey: 'categoryName',
  viewRoles: ['ADMIN', 'INVENTORY_MANAGER', 'MARKETING_MANAGER'], writeRoles: ['ADMIN', 'INVENTORY_MANAGER'],
  defaultSort: 'categoryId', searchKeys: ['categoryName', 'description'],
  columns: [{ key: 'categoryId', label: 'ID', type: 'number' }, { key: 'categoryName', label: 'Name' }, { key: 'description', label: 'Description' }],
  fields: [
    { name: 'categoryName', label: 'Category name', kind: 'name', required: true, maxlength: 100, full: true },
    { name: 'description', label: 'Description', type: 'textarea', full: true, maxlength: 500 }
  ]
});
