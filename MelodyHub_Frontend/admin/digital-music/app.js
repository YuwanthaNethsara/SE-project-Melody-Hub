/* Digital music: attach one MP3 to a song, publish or unpublish it, download it to check, delete it. */
(function () {
  var esc = MH.esc;

  async function upload(ctx) {
    var data;
    try { data = await Promise.all([MH.api.get('/songs'), MH.api.get('/digital-music')]); } catch (e) { MH.fail(e); return; }
    var taken = {}; data[1].forEach(function (m) { taken[m.songId] = true; });
    var songs = data[0].filter(function (s) { return !taken[s.songId]; });
    if (!data[0].length) { MH.toast('Add a song first, then you can upload its MP3.', 'info'); return; }
    if (!songs.length) { MH.toast('Every song already has an MP3. Add a new song, or delete an existing MP3 to replace it.', 'info'); return; }
    var form = document.createElement('form'); form.noValidate = true;
    form.innerHTML = '<div id="se"></div>' +
      MH.field({ name: 'songId', label: 'Song', type: 'select', required: true, full: true, placeholder: 'Choose a song without an MP3', options: songs.map(function (s) { return { value: s.songId, label: s.title + (s.artistName ? ' (' + s.artistName + (s.albumTitle ? ', ' + s.albumTitle : '') + ')' : '') }; }) }, '') +
      MH.field({ name: 'file', label: 'MP3 file', type: 'file', what: 'audio', required: true, full: true, requiredMsg: 'Please choose an MP3 file.', fileHelp: 'MP3 only, up to 30 MB. One file per song.' }) +
      MH.field({ name: 'status', label: 'Availability', type: 'select', required: true, full: true, options: [{ value: 'ACTIVE', label: 'Active: customers can download it' }, { value: 'INACTIVE', label: 'Inactive: hidden from customers' }] }, 'ACTIVE') +
      '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Upload</button>';
    MH.bindFileFields(form);
    var busy = false;
    async function go(c, btn) {
      if (busy) return;
      busy = true;
      try {
        if (!(await MH.validateAsync(form))) return;
        var fd = new FormData(); fd.append('songId', form.songId.value); fd.append('file', form.file.files[0]); fd.append('status', form.status.value);
        MH.busy(btn, true, 'Uploading…');
        form.querySelector('#se').innerHTML = '';
        await MH.api.upload('/digital-music/upload', fd);
        c.close(); MH.ok('MP3 uploaded.'); ctx.reload();
      } catch (e) { form.querySelector('#se').innerHTML = '<div class="alert alert-error">' + MH.icon('alert', 16) + '<span>' + esc(e.message) + '</span></div>'; }
      finally { busy = false; MH.busy(btn, false); }
    }
    var ctl = MH.modal({
      title: 'Upload digital music', body: form, keepOpen: true,
      footer: [{ label: 'Cancel' }, { label: 'Upload MP3', cls: 'btn-primary', onClick: function (c, btn) { go(c, btn); } }]
    });
    form.onsubmit = function (e) { e.preventDefault(); go(ctl, ctl.btn(1)); };
  }

  function setStatus(status) {
    return async function (r, ctx) {
      MH.busy(ctx.button, true);
      try {
        await MH.api.put('/digital-music/' + r.digitalMusicId + '/status', undefined, { params: { status: status } });
        MH.ok(status === 'ACTIVE' ? '"' + r.songTitle + '" is now available to customers.' : '"' + r.songTitle + '" is hidden from customers.');
        ctx.reload();
      } catch (e) { MH.fail(e); MH.busy(ctx.button, false); }
    };
  }

  MH.crud({
    module: 'digital-music', title: 'Digital music', subtitle: 'MP3 files attached to songs. Active tracks appear in every customer’s digital library.',
    singular: 'Digital track', icon: 'music', createLabel: 'Upload MP3', strongColumn: 0,
    endpoint: '/digital-music', idKey: 'digitalMusicId', nameKey: 'songTitle',
    viewRoles: ['ADMIN', 'ARTIST'], writeRoles: ['ADMIN', 'ARTIST'], canEdit: function () { return false; }, defaultSort: 'uploadedAt',
    searchKeys: ['songTitle', 'albumTitle', 'artistName', 'fileName'],
    filters: [{ key: 'status', label: 'Status', options: ['ACTIVE', 'INACTIVE'] }, { key: 'artistName', label: 'Artist', raw: true }],
    customCreate: upload,
    deleteWarning: function () { return 'The MP3 file is removed from the server and customers can no longer download it.'; },
    columns: [
      { key: 'songTitle', label: 'Song' }, { key: 'artistName', label: 'Artist', type: 'avatar', imageKey: 'artistImageUrl' }, { key: 'albumTitle', label: 'Album', low: true },
      { key: 'fileSize', label: 'Size', low: true, render: function (r) { return MH.fileSize(r.fileSize); } },
      { key: 'status', label: 'Status', type: 'badge' }, { key: 'uploadedAt', label: 'Uploaded', type: 'datetime' }
    ],
    viewFields: [
      { key: 'songTitle', label: 'Song' }, { key: 'artistName', label: 'Artist', type: 'avatar', imageKey: 'artistImageUrl' }, { key: 'albumTitle', label: 'Album' }, { key: 'duration', label: 'Song length' },
      { key: 'fileName', label: 'Uploaded file' }, { key: 'fileFormat', label: 'Format' }, { key: 'fileSize', label: 'Size', render: function (r) { return MH.fileSize(r.fileSize); } },
      { key: 'status', label: 'Status', type: 'badge' }, { key: 'uploadedAt', label: 'Uploaded', type: 'datetime' }
    ],
    rowActions: [
      { label: 'Download', run: async function (r, ctx) { MH.busy(ctx.button, true); try { await MH.api.download('/digital-music/' + r.digitalMusicId + '/download', r.songTitle + '.mp3'); } catch (e) { MH.fail(e); } finally { MH.busy(ctx.button, false); } } },
      { label: 'Publish', cls: 'btn-primary', show: function (r) { return r.status !== 'ACTIVE'; }, run: setStatus('ACTIVE') },
      { label: 'Unpublish', show: function (r) { return r.status === 'ACTIVE'; }, run: setStatus('INACTIVE') }
    ],
    empty: { title: 'No music uploaded yet', text: 'Upload an MP3 and attach it to one of your songs.' }
  });
})();
