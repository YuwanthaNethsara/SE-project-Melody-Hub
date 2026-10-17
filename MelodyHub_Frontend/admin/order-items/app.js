/* Order items management page (uses the shared CRUD engine) */
MH.crud({
  module: 'order-items', title: 'Order items', subtitle: 'Every line item across all orders.',
  singular: 'Order item', icon: 'list', noCreate: true,
  endpoint: '/order-items', idKey: 'orderItemId', nameKey: 'productName',
  viewRoles: ['ADMIN', 'SALES_MANAGER'], writeRoles: ['ADMIN'], deleteRoles: ['ADMIN'],
  canEdit: function () { return false; }, defaultSort: 'orderItemId',
  searchKeys: ['productName', 'orderId'],
  columns: [
    { key: 'orderItemId', label: 'Item #', type: 'number' }, { key: 'orderId', label: 'Order #', type: 'number' }, { key: 'productName', label: 'Product' },
    { key: 'quantity', label: 'Qty', type: 'number' }, { key: 'unitPrice', label: 'Unit price', type: 'money' }, { key: 'subtotal', label: 'Subtotal', type: 'money' }
  ],
  empty: { title: 'No order items yet', text: 'Items appear here as soon as customers place orders.' }
});
