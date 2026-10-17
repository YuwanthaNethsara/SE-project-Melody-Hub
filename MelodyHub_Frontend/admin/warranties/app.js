/* Warranties management page (uses the shared CRUD engine) */
MH.crud({
  module: 'warranties', title: 'Warranty', subtitle: 'Warranty terms per product. Customers claim against these from their orders.', strongColumn: 0,
  singular: 'Warranty', icon: 'shield',
  endpoint: '/warranties', idKey: 'warrantyId', nameKey: 'productName',
  viewRoles: ['ADMIN', 'SALES_MANAGER'], writeRoles: ['ADMIN'], defaultSort: 'warrantyId',
  searchKeys: ['productName', 'warrantyType'],
  filters: [{ key: 'status', label: 'Status' }, { key: 'warrantyType', label: 'Type' }],
  columns: [
    { key: 'productName', label: 'Product' },
    { key: 'warrantyPeriodMonths', label: 'Period', render: function (r) { return MH.esc(MH.plural(r.warrantyPeriodMonths, 'month')); } },
    { key: 'warrantyType', label: 'Type', type: 'badge' }, { key: 'status', label: 'Status', type: 'badge' },
    { key: 'createdDate', label: 'Created', type: 'datetime' }
  ],
  fields: [
    { name: 'productId', label: 'Product', type: 'select', numeric: true, required: true, lockOnEdit: true, full: true, optionsFrom: { endpoint: '/products', value: 'productId', label: 'name' }, help: 'Each product can have one warranty.' },
    { name: 'warrantyPeriodMonths', label: 'Warranty period (months)', errorLabel: 'Warranty period', type: 'number', kind: 'int', required: true, min: 1, max: 120, step: '1' },
    { name: 'warrantyType', label: 'Warranty type', type: 'select', required: true, options: ['MANUFACTURER', 'SELLER'] },
    { name: 'status', label: 'Status', type: 'select', required: true, options: ['ACTIVE', 'INACTIVE', 'EXPIRED'], help: 'Customers can only claim against an active warranty.' },
    { name: 'termsAndConditions', label: 'Terms and conditions', type: 'textarea', full: true, maxlength: 1000 }
  ],
  defaults: { status: 'ACTIVE', warrantyType: 'MANUFACTURER' },
  empty: { title: 'No warranties yet', text: 'Add a warranty to a product so customers can claim against it.' }
});
