/* Suppliers management page (uses the shared CRUD engine) */
MH.crud({
  module: 'suppliers', title: 'Suppliers', subtitle: 'Companies you buy stock from.',
  singular: 'Supplier', icon: 'handshake',
  endpoint: '/suppliers', idKey: 'supplierId', nameKey: 'supplierName',
  viewRoles: ['ADMIN'], writeRoles: ['ADMIN'], defaultSort: 'supplierId',
  searchKeys: ['supplierName', 'contactPerson', 'email', 'phone'],
  filters: [{ key: 'status', label: 'Status' }],
  columns: [
    { key: 'supplierName', label: 'Supplier' }, { key: 'contactPerson', label: 'Contact' }, { key: 'email', label: 'Email' },
    { key: 'phone', label: 'Phone' }, { key: 'status', label: 'Status', type: 'badge' }
  ],
  fields: [
    { name: 'supplierName', label: 'Supplier name', kind: 'name', required: true, maxlength: 255, full: true },
    { name: 'contactPerson', label: 'Contact person', kind: 'person', required: true, maxlength: 100 },
    { name: 'email', label: 'Email', type: 'email', required: true },
    { name: 'phone', label: 'Phone', type: 'tel', errorLabel: 'Phone number', required: true, maxlength: 20, placeholder: '+94 77 123 4567' },
    { name: 'status', label: 'Status', type: 'select', required: true, options: ['ACTIVE', 'INACTIVE'] },
    { name: 'address', label: 'Address', type: 'textarea', kind: 'address', required: true, minlength: 5, maxlength: 500, full: true }
  ],
  defaults: { status: 'ACTIVE' }
});
