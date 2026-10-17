/* Small pure helpers: escaping, formatting, status colours, images, placeholders */
(function (MH) {
  MH.esc = function (v) {
    return String(v == null ? '' : v).replace(/[&<>"']/g, function (m) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m];
    });
  };
  MH.money = function (v) {
    var n = Number(v || 0);
    return MH.CURRENCY + ' ' + n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };
  MH.plural = function (n, one, many) { return n + ' ' + (n === 1 ? one : (many || one + 's')); };
  // money for big stat numbers: the currency code is set smaller so the amount stays on one line
  MH.moneyHtml = function (v) {
    var n = Number(v || 0);
    return '<span class="cur">' + MH.CURRENCY + '</span> ' + n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };
  MH.date = function (v) {
    if (!v) return '—';
    var d = new Date(v);
    return isNaN(d) ? MH.esc(v) : d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
  };
  MH.dateTime = function (v) {
    if (!v) return '—';
    var d = new Date(v);
    return isNaN(d) ? MH.esc(v) : d.toLocaleString(undefined, { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };
  MH.humanize = function (s) {
    if (!s) return '';
    s = String(s).replace(/_/g, ' ').toLowerCase();
    return s.charAt(0).toUpperCase() + s.slice(1);
  };
  MH.initials = function (name) {
    return String(name || '?').trim().split(/\s+/).slice(0, 2).map(function (p) { return p.charAt(0); }).join('').toUpperCase() || '?';
  };
  MH.debounce = function (fn, ms) {
    var t; return function () { var a = arguments, c = this; clearTimeout(t); t = setTimeout(function () { fn.apply(c, a); }, ms || 250); };
  };
  MH.qs = function (name) { return new URLSearchParams(location.search).get(name); };
  MH.isEmail = function (v) { return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(String(v || '').trim()); };

  // 0..4 password strength
  MH.passwordScore = function (pw) {
    pw = pw || ''; var s = 0;
    if (pw.length >= 8) s++;
    if (/[a-z]/.test(pw) && /[A-Z]/.test(pw)) s++;
    if (/\d/.test(pw)) s++;
    if (/[^A-Za-z0-9]/.test(pw) || pw.length >= 14) s++;
    return s;
  };

  // status text -> badge colour
  var TONES = {
    ok: ['ACTIVE', 'PAID', 'DELIVERED', 'COMPLETED', 'APPROVED', 'IN_STOCK', 'CONFIRMED', 'AVAILABLE', 'RECEIVED', 'VISIBLE'],
    warn: ['PENDING', 'LOW_STOCK', 'PROCESSING', 'UPCOMING', 'DRAFT', 'ORDERED', 'SHIPPED', 'PAUSED', 'PARTIAL', 'PARTIALLY_RECEIVED'],
    bad: ['CANCELLED', 'REJECTED', 'FAILED', 'OUT_OF_STOCK', 'EXPIRED', 'INACTIVE', 'TERMINATED', 'DISCONTINUED', 'HIDDEN'],
    info: ['REFUNDED', 'RETURN', 'WARRANTY', 'MANUFACTURER', 'SELLER', 'ARCHIVED']
  };
  MH.tone = function (status) {
    var s = String(status || '').toUpperCase();
    for (var t in TONES) if (TONES[t].indexOf(s) >= 0) return t;
    return 'muted';
  };
  MH.badge = function (status) {
    if (status == null || status === '') return '<span class="faint">—</span>';
    return '<span class="badge badge-' + MH.tone(status) + '">' + MH.esc(MH.humanize(status)) + '</span>';
  };

  // Items without an uploaded image get a matching instrument illustration (see js/art.js); a real imageUrl always wins.
  MH.placeholder = function (name, hint) {
    var text = hint === 'artist' ? 'artist' : (name || '') + ' ' + (hint || '');
    return MH.art.uri(hint === 'artist' ? 'artist' : MH.art.pick(text));
  };
  // uploaded files are served by the backend (/uploads/...), not by the frontend server
  MH.assetUrl = function (url) {
    if (url && /^\/uploads\//.test(url)) return MH.API_BASE_URL.replace(/\/api\/?$/, '') + url;
    return url || '';
  };
  MH.img = function (url, name, cls, hint) {
    var src = url ? MH.assetUrl(url) : MH.placeholder(name, hint);
    return '<img ' + (cls ? 'class="' + cls + '" ' : '') + 'loading="lazy" decoding="async" src="' + MH.esc(src) + '" data-name="' + MH.esc(name || '') + '" data-hint="' + MH.esc(hint || '') + '" alt="' + MH.esc(name || '') + '">';
  };
  // small round photo + name (artist photos in tables and lists); no photo -> the default artist picture
  MH.personChip = function (url, name, hint) {
    return '<span class="person-chip">' + MH.img(url, name, 'avatar-xs', hint || 'artist') + '<span class="clip" title="' + MH.esc(name) + '">' + MH.esc(name) + '</span></span>';
  };
  MH.fileSize = function (b) {
    if (b == null) return '—';
    return b >= 1048576 ? (b / 1048576).toFixed(1) + ' MB' : Math.max(1, Math.round(b / 1024)) + ' KB';
  };
  // 4.5 -> five stars with 90% filled (screen readers get the number)
  MH.stars = function (rating, count, cls) {
    var r = Math.max(0, Math.min(5, Number(rating) || 0));
    return '<span class="stars ' + (cls || '') + '" title="' + r.toFixed(1) + ' out of 5"><span class="s" aria-hidden="true">★★★★★<span style="width:' + (r / 5 * 100) + '%">★★★★★</span></span>' +
      '<span class="sr-only">Rated ' + r.toFixed(1) + ' out of 5</span>' + (count != null ? '<span>' + (count ? r.toFixed(1) + ' (' + count + ')' : 'No reviews yet') + '</span>' : '') + '</span>';
  };
  // broken image URL -> illustration (once), so a broken-image icon never shows
  document.addEventListener('error', function (e) {
    var t = e.target;
    if (t && t.tagName === 'IMG' && !t.dataset.fb) { t.dataset.fb = '1'; t.src = MH.placeholder(t.dataset.name || '', t.dataset.hint || ''); }
  }, true);

  // mirrors the backend's "not for sale" statuses
  MH.isSellable = function (p) {
    var s = String(p.status || '').toUpperCase();
    return ['INACTIVE', 'DISCONTINUED', 'UNAVAILABLE', 'ARCHIVED', 'DISABLED', 'OUT_OF_STOCK'].indexOf(s) < 0;
  };
  MH.availability = function (p) {
    if (!MH.isSellable(p)) return { text: 'Unavailable', tone: 'bad', buyable: false };
    if (p.stockStatus === 'OUT_OF_STOCK' || p.quantityAvailable === 0) return { text: 'Out of stock', tone: 'bad', buyable: false };
    if (p.stockStatus === 'LOW_STOCK') return { text: 'Only ' + p.quantityAvailable + ' left', tone: 'warn', buyable: true };
    return { text: 'In stock', tone: 'ok', buyable: true };
  };
})(window.MH);
