/* Artists management page (uses the shared CRUD engine) */
MH.crud({
  module: 'artists', title: 'Artists', subtitle: 'Artist profiles shown on the store.', strongColumn: 1,
  singular: 'Artist', icon: 'mic',
  endpoint: '/artists', idKey: 'artistId', nameKey: 'artistName',
  viewRoles: ['ADMIN', 'ARTIST'], writeRoles: ['ADMIN', 'ARTIST'], defaultSort: 'artistId',
  searchKeys: ['artistName', 'genre', 'country'],
  filters: [{ key: 'genre', label: 'Genre' }, { key: 'country', label: 'Country' }],
  columns: [
    { key: 'imageUrl', label: 'Photo', type: 'image', alt: 'artistName', hint: 'artist', round: true }, { key: 'artistName', label: 'Artist' },
    { key: 'genre', label: 'Genre' }, { key: 'country', label: 'Country' }
  ],
  fields: [
    { name: 'artistName', label: 'Artist name', kind: 'name', required: true, maxlength: 255, full: true },
    { name: 'genre', label: 'Genre', kind: 'name', maxlength: 100 }, { name: 'country', label: 'Country', kind: 'person', minlength: 2, maxlength: 100 },
    { name: 'image', label: 'Artist photo', type: 'file', what: 'image', maxMB: 2, round: true, emptyIcon: 'user', full: true, existingValueKey: 'imageUrl',
      typeMsg: 'Please choose a JPG, JPEG, PNG or WEBP photo.', fileHint: 'Optional. Square photos look best.' },
    { name: 'bio', label: 'Biography', type: 'textarea', full: true, maxlength: 1000 }
  ]
});
