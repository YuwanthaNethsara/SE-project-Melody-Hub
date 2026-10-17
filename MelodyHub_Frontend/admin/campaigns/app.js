/* Campaigns management page (uses the shared CRUD engine) */
MH.crud({
  module: 'campaigns', title: 'Campaigns', subtitle: 'Marketing campaigns and their run dates.',
  singular: 'Campaign', icon: 'megaphone',
  endpoint: '/campaigns', idKey: 'campaignId', nameKey: 'campaignName',
  viewRoles: ['ADMIN', 'MARKETING_MANAGER'], writeRoles: ['ADMIN', 'MARKETING_MANAGER'], defaultSort: 'campaignId',
  searchKeys: ['campaignName', 'description', 'status'],
  filters: [{ key: 'status', label: 'Status' }],
  columns: [
    { key: 'campaignName', label: 'Campaign' }, { key: 'budget', label: 'Budget', render: function (r) { return r.budget != null ? MH.money(r.budget) : '<span class="faint">—</span>'; } },
    { key: 'startDate', label: 'Starts', type: 'date' }, { key: 'endDate', label: 'Ends', type: 'date' }, { key: 'status', label: 'Status', type: 'badge' }
  ],
  fields: [
    { name: 'campaignName', label: 'Campaign name', kind: 'name', required: true, maxlength: 255, full: true },
    { name: 'budget', label: 'Budget (LKR)', type: 'number', kind: 'money', allowZero: true, min: 0, step: '0.01', help: 'Optional. Cannot be negative.' },
    { name: 'startDate', label: 'Start date', type: 'date', required: true, minDate: '2000-01-01', maxDate: 'today+10y' },
    { name: 'endDate', label: 'End date', type: 'date', required: true, minDate: '2000-01-01', maxDate: 'today+10y' },
    { name: 'description', label: 'Description', type: 'textarea', full: true, maxlength: 1000 }
  ],
  validate: function (v) { var m = MH.v.dateRange(v.startDate, v.endDate); return m ? { endDate: m } : {}; }
});
