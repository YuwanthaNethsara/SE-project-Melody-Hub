/* Albums management page (uses the shared CRUD engine) */
MH.crud({
  module: 'albums', title: 'Albums', subtitle: 'Albums released by your artists.', strongColumn: 1,
  singular: 'Album', icon: 'disc',
  endpoint: '/albums', idKey: 'albumId', nameKey: 'title',
  viewRoles: ['ADMIN', 'ARTIST'], writeRoles: ['ADMIN', 'ARTIST'], defaultSort: 'albumId',
  searchKeys: ['title', 'artistName'],
  filters: [{ key: 'artistName', label: 'Artist' }],
  columns: [
    { key: 'coverImageUrl', label: 'Cover', type: 'image', alt: 'title', hint: 'vinyl record', fallbackKey: 'artistImageUrl' }, { key: 'title', label: 'Title' },
    { key: 'artistName', label: 'Artist', type: 'avatar', imageKey: 'artistImageUrl' }, { key: 'releaseDate', label: 'Released', type: 'date' }
  ],
  fields: [
    { name: 'title', label: 'Album title', kind: 'name', required: true, maxlength: 255, full: true },
    { name: 'artistId', label: 'Artist', type: 'select', numeric: true, required: true, optionsFrom: { endpoint: '/artists', value: 'artistId', label: 'artistName' } },
    { name: 'releaseDate', label: 'Release date', type: 'date', required: true, minDate: '1900-01-01', maxDate: 'today+5y' },
    { name: 'image', label: 'Cover image', type: 'file', what: 'image', full: true, existingValueKey: 'coverImageUrl', emptyIcon: 'disc', fileHint: 'Square covers look best. Without a cover the artist photo is used.' },
    { name: 'description', label: 'Description', type: 'textarea', full: true, maxlength: 1000 }
  ]
});
