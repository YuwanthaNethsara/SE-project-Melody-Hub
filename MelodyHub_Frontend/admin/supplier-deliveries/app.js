/* Supplier deliveries: goods received against purchase orders (uses the shared CRUD engine) */
MH.crud({
  module: 'supplier-deliveries', title: 'Deliveries', subtitle: 'Goods that suppliers have delivered against your purchase orders.',
  singular: 'Delivery', icon: 'truck',
  endpoint: '/supplier-deliveries', idKey: 'deliveryId', nameKey: 'deliveryId',
  viewRoles: ['ADMIN'], writeRoles: ['ADMIN'], defaultSort: 'deliveryId',
  searchKeys: ['supplierName', 'productName', 'status', 'notes'],
  filters: [{ key: 'status', label: 'Status' }],
  columns: [
    { key: 'deliveryId', label: 'Delivery #', type: 'number' }, { key: 'purchaseOrderId', label: 'PO #', type: 'number' },
    { key: 'supplierName', label: 'Supplier' }, { key: 'productName', label: 'Product' },
    { key: 'quantityReceived', label: 'Received', render: function (r) { return MH.esc(r.quantityReceived) + ' <span class="faint">of ' + MH.esc(r.orderedQuantity) + '</span>'; } },
    { key: 'deliveryDate', label: 'Delivered', type: 'date' }, { key: 'status', label: 'Status', type: 'badge' }
  ],
  fields: [
    { name: 'purchaseOrderId', label: 'Purchase order', type: 'select', numeric: true, required: true, full: true,
      optionsFrom: { endpoint: '/purchase-orders', value: 'purchaseOrderId', label: function (p) { return '#' + p.purchaseOrderId + ', ' + p.supplierName + ', ' + p.productName + ' (' + p.quantity + ' ordered)'; } } },
    { name: 'deliveryDate', label: 'Delivery date', type: 'date', required: true, minDate: '2000-01-01', maxDate: 'today' },
    { name: 'quantityReceived', label: 'Quantity received', type: 'number', kind: 'int', required: true, min: 1, max: 1000000, step: '1' },
    { name: 'status', label: 'Status', type: 'select', required: true, options: ['RECEIVED', 'PARTIAL', 'REJECTED'], help: 'Rejected goods do not count towards the quantity ordered.' },
    { name: 'notes', label: 'Notes', type: 'textarea', maxlength: 500, full: true }
  ],
  defaults: { status: 'RECEIVED' },
  empty: { title: 'No deliveries recorded', text: 'When a supplier delivers goods, record them here against the purchase order.' }
});
