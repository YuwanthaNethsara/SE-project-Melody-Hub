/* Users management page (uses the shared CRUD engine) */
MH.crud({
  module: 'users', title: 'Users', subtitle: 'Create staff accounts and manage who can sign in.',
  singular: 'User', icon: 'users',
  endpoint: '/auth/users', createEndpoint: '/auth/create-user', idKey: 'id', nameKey: 'name',
  viewRoles: ['ADMIN'], writeRoles: ['ADMIN'], defaultSort: 'id', createLabel: 'Add user',
  searchKeys: ['name', 'email', 'role'],
  filters: [{ key: 'role', label: 'Role' }],
  columns: [
    { key: 'id', label: 'ID', type: 'number' },
    { key: 'name', label: 'Name' },
    { key: 'email', label: 'Email' },
    { key: 'role', label: 'Role', type: 'badge' }
  ],
  fields: [
    { name: 'name', label: 'Full name', kind: 'person', required: true, maxlength: 100 },
    { name: 'email', label: 'Email', type: 'email', required: true, autocomplete: 'off' },
    { name: 'role', label: 'Role', type: 'select', required: true, options: ['CUSTOMER', 'ADMIN', 'INVENTORY_MANAGER', 'ARTIST', 'MARKETING_MANAGER', 'SALES_MANAGER', 'SUPPLIER'] },
    { name: 'password', label: 'Password', type: 'password', requiredOnCreate: true, minlength: 8, maxlength: 72, autocomplete: 'new-password', full: true, help: 'At least 8 characters with a letter and a number. When editing, leave it blank to keep the current password.' }
  ],
  toForm: function (r) { return { name: r.name, email: r.email, role: r.role, password: '' }; },
  toPayload: function (p) { if (!p.password) delete p.password; return p; }
});
