/* Songs management page (uses the shared CRUD engine) */
MH.crud({
  module: 'songs', title: 'Songs', subtitle: 'Tracks that belong to albums.',
  singular: 'Song', icon: 'music',
  endpoint: '/songs', idKey: 'songId', nameKey: 'title',
  viewRoles: ['ADMIN', 'ARTIST'], writeRoles: ['ADMIN', 'ARTIST'], defaultSort: 'songId',
  searchKeys: ['title', 'albumTitle', 'artistName'],
  filters: [{ key: 'albumTitle', label: 'Album' }],
  columns: [
    { key: 'trackNumber', label: '#', type: 'number' }, { key: 'title', label: 'Title' },
    { key: 'albumTitle', label: 'Album' }, { key: 'artistName', label: 'Artist', type: 'avatar', imageKey: 'artistImageUrl' }, { key: 'duration', label: 'Length' }
  ],
  fields: [
    { name: 'title', label: 'Song title', kind: 'name', required: true, maxlength: 255, full: true },
    { name: 'albumId', label: 'Album', type: 'select', numeric: true, required: true, optionsFrom: { endpoint: '/albums', value: 'albumId', label: function (a) { return a.title + (a.artistName ? ' — ' + a.artistName : ''); } } },
    { name: 'trackNumber', label: 'Track number', type: 'number', kind: 'int', required: true, min: 1, max: 999, step: '1' },
    { name: 'duration', label: 'Duration', kind: 'duration', required: true, maxlength: 8, placeholder: '3:45', help: 'Format m:ss, for example 3:45.' }
  ]
});
