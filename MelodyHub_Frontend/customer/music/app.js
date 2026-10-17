/* Customer digital library: every ACTIVE track the store offers, with an authenticated MP3 download. */
(function () {
  if (!MH.guards.requireRole(['CUSTOMER'])) return;
  var esc = MH.esc;
  var content = MH.mountPortal({ active: 'c-music', title: 'Digital library', subtitle: 'MP3 tracks released by MelodyHub artists. Downloads are for your personal listening.' });
  var tracks = [], q = '', artist = MH.qs('artist') || ''; // ?artist= comes from the artist profile page

  async function load() {
    content.innerHTML = MH.skeletonRows(5);
    try {
      tracks = await MH.api.get('/digital-music');
      draw();
    } catch (e) { content.innerHTML = MH.errorState(e.message); document.getElementById('retry').onclick = load; }
  }

  function draw() {
    if (!tracks.length) {
      content.innerHTML = MH.empty({ icon: 'music', title: 'No music available yet.', text: 'New releases from our artists will appear here as soon as they are published.', actionHtml: '<a class="btn" href="' + MH.url('artists/') + '">Meet the artists</a>' });
      return;
    }
    var artists = Array.from(new Set(tracks.map(function (t) { return t.artistName; }).filter(Boolean))).sort();
    if (artist && artists.indexOf(artist) < 0) artists.push(artist); // linked from a profile without MP3s yet
    var needle = q.trim().toLowerCase();
    var list = tracks.filter(function (t) {
      if (artist && t.artistName !== artist) return false;
      return !needle || [t.songTitle, t.artistName, t.albumTitle].join(' ').toLowerCase().indexOf(needle) >= 0;
    });
    content.innerHTML = '<div class="card card-flush"><div class="toolbar"><div class="search-wrap">' + MH.icon('search', 16) + '<input class="input search" type="search" id="mq" placeholder="Search songs, artists or albums" aria-label="Search the library" value="' + esc(q) + '"></div>' +
      '<select class="select" id="ma" aria-label="Filter by artist"><option value="">All artists</option>' + artists.map(function (a) { return '<option' + (a === artist ? ' selected' : '') + '>' + esc(a) + '</option>'; }).join('') + '</select>' +
      '<span class="count">' + list.length + ' track' + (list.length === 1 ? '' : 's') + '</span></div>' +
      '<div style="padding:16px">' + (list.length ? '<div class="track-list">' + list.map(function (t) {
        // album cover, otherwise the artist photo, otherwise a music note
        var art = t.coverImageUrl ? MH.img(t.coverImageUrl, t.albumTitle || t.songTitle, '', 'vinyl record')
          : t.artistImageUrl ? MH.img(t.artistImageUrl, t.artistName, '', 'artist') : '<span style="color:var(--gold)">' + MH.icon('music', 24) + '</span>';
        var by = t.artistName ? '<a class="track-artist" href="' + MH.url('artists/profile/') + '?id=' + encodeURIComponent(t.artistId) + '">' + MH.img(t.artistImageUrl, t.artistName, 'avatar-xs', 'artist') + esc(t.artistName) + '</a>' : '';
        return '<div class="track"><div class="cover">' + art + '</div>' +
          '<div style="min-width:0"><div class="title">' + esc(t.songTitle || t.fileName) + '</div><div class="meta">' + by + (t.albumTitle ? '<span>' + esc(t.albumTitle) + '</span>' : '') + (by || t.albumTitle ? '' : 'MelodyHub') + '</div></div>' +
          '<div class="tail"><span class="len">' + esc(t.duration || '') + '</span><span class="faint" style="font-size:13px">' + MH.fileSize(t.fileSize) + '</span>' +
          '<button type="button" class="btn btn-sm btn-primary" data-dl="' + t.digitalMusicId + '" data-name="' + esc((t.artistName ? t.artistName + ' - ' : '') + (t.songTitle || 'track') + '.mp3') + '">' + MH.icon('download', 15) + ' Download MP3</button></div></div>';
      }).join('') + '</div>' : MH.empty({ icon: 'search', title: 'No tracks match', text: 'Try another search or choose all artists.' })) + '</div></div>';

    var mq = document.getElementById('mq');
    mq.oninput = MH.debounce(function () { q = mq.value; draw(); var n = document.getElementById('mq'); n.focus(); n.setSelectionRange(n.value.length, n.value.length); }, 200);
    document.getElementById('ma').onchange = function () { artist = this.value; draw(); };
  }

  content.addEventListener('click', async function (e) {
    var b = e.target.closest('[data-dl]'); if (!b) return;
    MH.busy(b, true, 'Downloading…');
    try {
      var r = await MH.api.download('/digital-music/' + encodeURIComponent(b.dataset.dl) + '/download', b.dataset.name);
      MH.ok('Downloaded ' + r.name + '.');
    } catch (err) {
      MH.fail(err);
      if (err.status === 404) load(); // the track was removed or deactivated meanwhile
    } finally { MH.busy(b, false); }
  });
  load();
})();
