/* Artist profile: photo, biography, albums with tracklists, and other artists. */
(async function () {
  var esc = MH.esc, root = document.getElementById('artist-root');
  MH.renderHeader('artists'); MH.renderFooter();
  var back = '<a class="btn btn-primary" href="../">See all artists</a>';
  var id = MH.qs('id');
  if (!id || !MH.v.isId(id)) {
    root.innerHTML = MH.empty({ icon: 'mic', title: id ? 'Artist not found' : 'No artist selected', text: id ? 'That artist link is not valid.' : 'Pick an artist from the list.', actionHtml: back });
    return;
  }

  var artist;
  try { artist = await MH.api.get('/artists/' + encodeURIComponent(id), { auth: false }); }
  catch (e) {
    root.innerHTML = e.status === 404 || e.status === 400
      ? MH.empty({ icon: 'mic', title: 'Artist not found', text: 'This profile may have been removed.', actionHtml: back })
      : '<div class="card">' + MH.errorState(e.message, 'retry') + '</div>';
    var r = document.getElementById('retry'); if (r) r.onclick = function () { location.reload(); };
    return;
  }
  document.title = artist.artistName + ' · MelodyHub';

  function soft(p) { return MH.api.get(p, { auth: false }).catch(function () { return null; }); }
  var d = await Promise.all([soft('/albums/artist/' + artist.artistId), soft('/songs'), soft('/artists')]);
  var albums = (d[0] || []).slice().sort(function (a, b) { return String(b.releaseDate || '').localeCompare(String(a.releaseDate || '')); });
  var albumIds = {}; albums.forEach(function (a) { albumIds[a.albumId] = 1; });
  var songs = (d[1] || []).filter(function (s) { return albumIds[s.albumId]; });
  var others = (d[2] || []).filter(function (a) { return a.artistId !== artist.artistId; }).slice(0, 4);
  var meta = [artist.genre, artist.country].filter(Boolean).join(', ');

  // customers can go straight to this artist's MP3s; visitors are invited to log in
  var role = MH.auth.getRole(), libraryUrl = MH.url('customer/music/') + '?artist=' + encodeURIComponent(artist.artistName);
  var action = !songs.length ? '' : role === 'CUSTOMER' ? '<a class="btn btn-primary" href="' + libraryUrl + '">' + MH.icon('download', 16) + ' MP3s in your library</a>'
    : !MH.auth.isLoggedIn() ? '<a class="btn btn-primary" href="' + MH.url('auth/login/') + '?next=' + encodeURIComponent(new URL(libraryUrl).pathname + new URL(libraryUrl).search) + '">Log in to download MP3s</a>' : '';

  function album(a) {
    var tracks = songs.filter(function (s) { return s.albumId === a.albumId; }).sort(function (p, q) { return (p.trackNumber || 0) - (q.trackNumber || 0); });
    // no cover yet: the artist photo stands in, then the record illustration
    return '<article class="album-card"><div class="album-cover">' + MH.img(a.coverImageUrl || artist.imageUrl, a.title, '', a.coverImageUrl || !artist.imageUrl ? 'vinyl record' : 'artist') + '</div>' +
      '<div class="album-body"><h3>' + esc(a.title) + '</h3><div class="muted">' + (a.releaseDate ? 'Released ' + MH.date(a.releaseDate) : 'Release date not set') + ', ' + MH.plural(tracks.length, 'track') + '</div>' +
      (a.description ? '<p class="album-desc">' + esc(a.description) + '</p>' : '') +
      (tracks.length ? '<ol class="tracklist">' + tracks.map(function (s) {
        return '<li><span class="t">' + esc(s.title) + '</span><span class="faint">' + esc(s.duration || '') + '</span></li>';
      }).join('') + '</ol>' : '<p class="muted" style="margin:10px 0 0">No tracks listed yet.</p>') + '</div></article>';
  }

  root.innerHTML =
    MH.breadcrumbs([['Home', MH.url('index.html')], ['Artists', '../'], [artist.artistName]]) +
    '<section class="artist-hero"><div class="artist-hero-photo">' + MH.img(artist.imageUrl, artist.artistName, '', 'artist') + '</div>' +
    '<div class="artist-hero-copy">' + (meta ? '<div class="artist-meta">' + esc(meta) + '</div>' : '') +
    '<h1>' + esc(artist.artistName) + '</h1>' +
    '<p class="artist-bio">' + esc(artist.bio || 'This artist has not added a biography yet.') + '</p>' +
    '<dl class="hero-facts" style="margin-top:22px"><div><dt>' + (albums.length === 1 ? 'album' : 'albums') + '</dt><dd>' + albums.length + '</dd></div><div><dt>' + (songs.length === 1 ? 'track' : 'tracks') + '</dt><dd>' + songs.length + '</dd></div></dl>' +
    (action ? '<div class="row" style="margin-top:22px">' + action + '</div>' : '') + '</div></section>' +
    '<section class="section" style="padding-bottom:0"><div class="section-head"><div><h2>Albums</h2><p>Newest first, with the full tracklist.</p></div></div>' +
    (albums.length ? '<div class="album-grid">' + albums.map(album).join('') + '</div>'
      : '<div class="card">' + MH.empty({ icon: 'disc', title: 'No albums yet', text: 'Albums by ' + artist.artistName + ' will appear here once they are published.' }) + '</div>') + '</section>' +
    (others.length ? '<section class="section" style="padding-bottom:0"><div class="section-head"><div><h2>More artists</h2></div><a class="btn btn-sm" href="../">All artists</a></div>' +
      '<div class="grid" style="grid-template-columns:repeat(auto-fill,minmax(220px,1fr))">' + others.map(function (a) { return MH.artistCard(a); }).join('') + '</div></section>' : '');
})();
