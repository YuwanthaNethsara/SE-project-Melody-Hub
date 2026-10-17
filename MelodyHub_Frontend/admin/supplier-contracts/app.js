/* Supplier contracts management page (uses the shared CRUD engine) */
MH.crud({
  module: 'supplier-contracts', title: 'Supplier contracts', subtitle: 'Agreements with your suppliers and when they run out.',
  singular: 'Contract', icon: 'file',
  endpoint: '/supplier-contracts', idKey: 'contractId', nameKey: 'contractName',
  viewRoles: ['ADMIN'], writeRoles: ['ADMIN'], defaultSort: 'contractId',
  searchKeys: ['contractName', 'supplierName', 'status'],
  filters: [{ key: 'status', label: 'Status' }],
  columns: [
    { key: 'contractName', label: 'Contract' }, { key: 'supplierName', label: 'Supplier' },
    { key: 'startDate', label: 'Starts', type: 'date' }, { key: 'endDate', label: 'Ends', type: 'date' }, { key: 'status', label: 'Status', type: 'badge' }
  ],
  fields: [
    { name: 'contractName', label: 'Contract name', kind: 'name', required: true, maxlength: 255, full: true },
    { name: 'supplierId', label: 'Supplier', type: 'select', numeric: true, required: true, full: true, optionsFrom: { endpoint: '/suppliers', value: 'supplierId', label: 'supplierName' } },
    { name: 'startDate', label: 'Start date', type: 'date', required: true, minDate: '2000-01-01', maxDate: 'today+30y' },
    { name: 'endDate', label: 'End date', type: 'date', required: true, minDate: '2000-01-01', maxDate: 'today+30y' },
    { name: 'status', label: 'Status', type: 'select', required: true, options: ['ACTIVE', 'PENDING', 'EXPIRED', 'TERMINATED'] },
    { name: 'description', label: 'Description', type: 'textarea', full: true, maxlength: 1000 }
  ],
  defaults: { status: 'ACTIVE' },
  validate: function (v) { var m = MH.v.dateRange(v.startDate, v.endDate); return m ? { endDate: m } : {}; }
});
