/* Advertisements management page (uses the shared CRUD engine) */
MH.crud({
  module: 'advertisements', title: 'Advertisements', subtitle: 'Banners shown on the store home page while their campaign is running.', strongColumn: 1,
  singular: 'Advertisement', icon: 'megaphone',
  endpoint: '/advertisements', idKey: 'advertisementId', nameKey: 'title',
  viewRoles: ['ADMIN', 'MARKETING_MANAGER'], writeRoles: ['ADMIN', 'MARKETING_MANAGER'], defaultSort: 'advertisementId',
  searchKeys: ['title', 'content', 'campaignName', 'status'],
  filters: [{ key: 'campaignName', label: 'Campaign' }, { key: 'status', label: 'Status' }],
  columns: [
    { key: 'imageUrl', label: 'Image', type: 'image', alt: 'title', hint: 'banner' },
    { key: 'title', label: 'Title' }, { key: 'campaignName', label: 'Campaign' },
    { key: 'content', label: 'Content', render: function (r) { var t = r.content || ''; return MH.esc(t.length > 60 ? t.slice(0, 57) + '…' : t); } },
    { key: 'status', label: 'Status', type: 'badge' }, { key: 'createdDate', label: 'Created', type: 'datetime' }
  ],
  viewFields: [
    { key: 'title', label: 'Title' }, { key: 'campaignName', label: 'Campaign' }, { key: 'content', label: 'Content' },
    { key: 'imageUrl', label: 'Image', type: 'image', alt: 'title', hint: 'banner' }, { key: 'campaignStatus', label: 'Campaign status', type: 'badge' }, { key: 'status', label: 'Status', type: 'badge' }, { key: 'createdDate', label: 'Created', type: 'datetime' }
  ],
  fields: [
    { name: 'title', label: 'Advertisement title', errorLabel: 'Advertisement title', kind: 'text', minlength: 2, required: true, maxlength: 150, full: true },
    { name: 'campaignId', label: 'Campaign', type: 'select', numeric: true, required: true, full: true, optionsFrom: { endpoint: '/campaigns', value: 'campaignId', label: 'campaignName' } },
    { name: 'content', label: 'Advertisement content', errorLabel: 'Advertisement content', type: 'textarea', kind: 'longtext', required: true, maxlength: 1000, full: true, help: 'The message customers will see.' },
    { name: 'image', label: 'Advertisement image', type: 'file', what: 'image', full: true, existingValueKey: 'imageUrl', fileHint: 'A wide picture (about 16:9) works best on the home page.' },
    { name: 'status', label: 'Status', type: 'select', required: true, options: ['ACTIVE', 'PAUSED', 'ARCHIVED'] }
  ],
  defaults: { status: 'ACTIVE' },
  empty: { title: 'No advertisements yet', text: 'Create a campaign first, then add its advertisements here.' }
});
