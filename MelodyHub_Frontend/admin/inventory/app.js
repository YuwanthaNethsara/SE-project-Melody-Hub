/* Inventory management page (uses the shared CRUD engine) */
MH.crud({
  module: 'inventory', title: 'Inventory', subtitle: 'Stock levels per product. Orders reduce these numbers automatically.',
  singular: 'Inventory record', icon: 'warehouse',
  endpoint: '/inventory', idKey: 'inventoryId', nameKey: 'productName',
  viewRoles: ['ADMIN', 'INVENTORY_MANAGER'], writeRoles: ['ADMIN', 'INVENTORY_MANAGER'], createLabel: 'Add stock record',
  defaultSort: 'quantityAvailable', searchKeys: ['productName', 'locationCode'],
  filters: [{ key: 'stockStatus', label: 'Stock', options: ['IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK'] }],
  columns: [
    { key: 'productName', label: 'Product' },
    { key: 'locationCode', label: 'Location' },
    { key: 'quantityAvailable', label: 'Available', type: 'number' },
    { key: 'reorderLevel', label: 'Reorder at', type: 'number' },
    { key: 'stockStatus', label: 'Status', type: 'badge' },
    { key: 'lastUpdated', label: 'Updated', type: 'datetime' }
  ],
  fields: [
    { name: 'productId', label: 'Product', type: 'select', numeric: true, required: true, lockOnEdit: true, full: true, optionsFrom: { endpoint: '/products', value: 'productId', label: 'name' } },
    { name: 'locationCode', label: 'Location code', kind: 'location', maxlength: 50, placeholder: 'e.g. A-12' },
    { name: 'quantityAvailable', label: 'Units available', errorLabel: 'Stock quantity', type: 'number', kind: 'int', required: true, min: 0, max: 1000000, step: '1' },
    { name: 'reorderLevel', label: 'Reorder level', type: 'number', kind: 'int', required: true, min: 0, max: 1000000, step: '1', help: 'Status turns to low stock at or below this number.' }
  ],
  empty: { title: 'No stock records yet', text: 'Add a stock record for a product to start tracking availability.' }
});
