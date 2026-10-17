/* Purchase orders management page (uses the shared CRUD engine) */
MH.crud({
  module: 'purchase-orders', title: 'Purchase orders', subtitle: 'Stock you have ordered from suppliers.',
  singular: 'Purchase order', icon: 'truck',
  endpoint: '/purchase-orders', idKey: 'purchaseOrderId', nameKey: 'purchaseOrderId',
  viewRoles: ['ADMIN'], writeRoles: ['ADMIN'], defaultSort: 'purchaseOrderId',
  searchKeys: ['supplierName', 'productName', 'status'],
  filters: [{ key: 'status', label: 'Status' }],
  columns: [
    { key: 'purchaseOrderId', label: 'PO #', type: 'number' }, { key: 'supplierName', label: 'Supplier' }, { key: 'productName', label: 'Product' },
    { key: 'quantity', label: 'Qty', type: 'number' }, { key: 'totalAmount', label: 'Total', type: 'money' },
    { key: 'orderDate', label: 'Ordered', type: 'date', low: true }, { key: 'status', label: 'Status', type: 'badge' }
  ],
  fields: [
    { name: 'supplierId', label: 'Supplier', type: 'select', numeric: true, required: true, optionsFrom: { endpoint: '/suppliers', value: 'supplierId', label: 'supplierName' } },
    { name: 'productId', label: 'Product', type: 'select', numeric: true, required: true, optionsFrom: { endpoint: '/products', value: 'productId', label: 'name' } },
    { name: 'quantity', label: 'Quantity', type: 'number', kind: 'int', required: true, min: 1, max: 1000000, step: '1' },
    { name: 'unitPrice', label: 'Unit price (LKR)', type: 'number', kind: 'money', required: true, min: 0.01, step: '0.01' },
    { name: 'status', label: 'Status', type: 'select', required: true, options: ['PENDING', 'ORDERED', 'RECEIVED', 'CANCELLED'], full: true }
  ],
  defaults: { status: 'PENDING' }
});
