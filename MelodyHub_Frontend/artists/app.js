/* Artists list: every artist with photo, genre, country, short bio and album count; each card opens the profile. */
(async function () {
  MH.renderHeader('artists'); MH.renderFooter();
  var box = document.getElementById('artists');
  var artists, albums;
  try {
    var d = await Promise.all([MH.api.get('/artists', { auth: false }), MH.api.get('/albums', { auth: false }).catch(function () { return []; })]);
    artists = d[0]; albums = d[1];
  } catch (e) {
    box.outerHTML = '<div class="card">' + MH.errorState(e.message, 'retry') + '</div>';
    document.getElementById('retry').onclick = function () { location.reload(); };
    return;
  }
  if (!artists.length) { box.outerHTML = '<div class="card">' + MH.empty({ icon: 'mic', title: 'No artists yet', text: 'Artist profiles will appear here.' }) + '</div>'; return; }
  artists.sort(function (a, b) { return String(a.artistName).localeCompare(String(b.artistName)); });
  box.innerHTML = artists.map(function (a) {
    var n = albums.filter(function (x) { return x.artistId === a.artistId; }).length;
    return MH.artistCard(a, { bio: true, albums: n });
  }).join('');
})();
