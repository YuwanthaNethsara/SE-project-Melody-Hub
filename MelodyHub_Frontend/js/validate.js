/*
 * MelodyHub validation rules.
 *
 * Pure functions: they never touch the DOM, so the same rules are used by the
 * form engine (components.js), by hand-written forms (register, checkout, ...)
 * and by the automated tests.
 *
 * A "spec" describes one field:
 *   { label, kind, required, min, max, minlength, maxlength, decimals,
 *     minDate, maxDate, exts, mimes, maxMB, what }
 * MH.v.check(spec, value, ctx) returns '' when the value is fine, otherwise a
 * short message for the user.
 *
 * Frontend validation is a convenience for the user. The backend validates
 * everything again and is the real security boundary.
 */
(function (MH) {
  var V = MH.v = {};

  /* ---------------- wording helpers ---------------- */
  // "Price (LKR)" -> "Price"
  V.label = function (spec) {
    return String((spec && (spec.errorLabel || spec.label)) || 'This field').replace(/\s*\([^)]*\)\s*/g, ' ').replace(/\*/g, '').trim() || 'This field';
  };
  function lower(s) { return s.charAt(0).toLowerCase() + s.slice(1); }
  function aOrAn(s) { return /^[aeiou]/i.test(s) ? 'an' : 'a'; }
  var CONTROL = /[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/;

  /* ---------------- character sets ---------------- */
  // names of products, artists, albums, songs, campaigns, suppliers...
  var NAME = /^[\p{L}\p{N}][\p{L}\p{N}\p{M} .,'’&()\/:+#!?%\-–—_@"“”;\[\]*=]*$/u;
  // people: letters, spaces, dots, apostrophes, hyphens
  var PERSON = /^[\p{L}][\p{L}\p{M} .'’\-]*$/u;
  var EMAIL = /^[A-Za-z0-9._%+\-]+@[A-Za-z0-9](?:[A-Za-z0-9\-]*[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9\-]*[A-Za-z0-9])?)*\.[A-Za-z]{2,}$/;
  var LOCATION = /^[A-Za-z0-9][A-Za-z0-9 \-_\/.]*$/;
  var SKU = /^[A-Za-z0-9][A-Za-z0-9._-]{1,49}$/;
  var POSTAL = /^[A-Za-z0-9][A-Za-z0-9 \-]{2,9}$/;
  var DURATION = /^(?:\d{1,3}:[0-5]\d|\d{1,2}:[0-5]\d:[0-5]\d)$/;
  var DATE = /^(\d{4})-(\d{2})-(\d{2})$/;

  V.isEmail = function (s) { s = String(s || '').trim(); return s.length <= 254 && EMAIL.test(s) && s.indexOf('..') < 0; };
  MH.isEmail = V.isEmail; // one strict email rule for the whole site
  V.isPhone = function (s) {
    s = String(s || '').trim();
    if (!/^\+?\(?[0-9][0-9 \-().]*$/.test(s)) return false;
    var digits = s.replace(/\D/g, '').length;
    return digits >= 7 && digits <= 15 && s.length <= 20;
  };
  // positive whole number as used in ids / query strings
  V.isId = function (s) { return /^[1-9]\d{0,14}$/.test(String(s == null ? '' : s).trim()); };

  /* ---------------- dates (yyyy-mm-dd) ---------------- */
  V.todayStr = function () {
    var d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  };
  // true only for real calendar dates (rejects 2026-02-30)
  V.isRealDate = function (s) {
    var m = DATE.exec(String(s || ''));
    if (!m) return false;
    var y = +m[1], mo = +m[2], d = +m[3], dt = new Date(Date.UTC(y, mo - 1, d));
    return dt.getUTCFullYear() === y && dt.getUTCMonth() === mo - 1 && dt.getUTCDate() === d;
  };
  // 'today', or 'today+5y' / 'today-1y' for limits relative to today
  function resolveDate(x) {
    var m = /^today([+-]\d{1,3})y$/.exec(x || '');
    if (m) { var d = new Date(); d.setFullYear(d.getFullYear() + Number(m[1])); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); }
    return x === 'today' ? V.todayStr() : x;
  }
  V.resolveDate = resolveDate;
  V.dateRange = function (start, end, endLabel) {
    if (V.isRealDate(start) && V.isRealDate(end) && end < start) return (endLabel || 'End date') + ' cannot be earlier than start date.';
    return '';
  };

  /* ---------------- numbers ---------------- */
  var NUM = /^[+-]?(?:\d+\.?\d*|\.\d+)$/;
  function decimalsOf(s) { var i = s.indexOf('.'); return i < 0 ? 0 : s.length - i - 1; }

  function checkNumber(spec, s, ctx, L) {
    var kind = spec.kind;
    if (ctx && ctx.badInput) return L + ' must be a number.';
    if (!NUM.test(s)) return L + ' must be a number.';
    var n = Number(s);
    if (!isFinite(n)) return L + ' must be a number.';

    if (kind === 'int') {
      if (!/^[+-]?\d+$/.test(s)) return L + ' must be a whole number.';
    } else {
      var dec = spec.decimals != null ? spec.decimals : 2;
      if (decimalsOf(s) > dec) return L + (dec === 0 ? ' must be a whole number.' : ' can have at most ' + dec + ' decimal places.');
    }

    if (kind === 'percent') {
      if (n <= 0 || n > 100) return L + ' must be between 0% and 100%.';
      return '';
    }
    var min = spec.min, max = spec.max;
    if (kind === 'money') {
      if (spec.allowZero) { if (n < 0) return L + ' cannot be negative.'; }
      else if (n <= 0) return L + ' must be greater than 0.';
      if (max == null) max = 99999999.99;
    } else if (kind === 'int' || kind === 'decimal') {
      if (spec.positive && n <= 0) return L + ' must be greater than 0.';
      if (min === 0 && n < 0) return L + ' cannot be negative.';
      if (min === 1 && kind === 'int' && n < 1) return L + ' must be greater than 0.';
      if (min != null && n < min) return L + ' must be at least ' + min + '.';
      if (kind === 'int' && max == null) max = 2147483647;
    }
    if (max != null && n > max) return L + ' cannot be more than ' + max.toLocaleString('en-US') + '.';
    return '';
  }

  /* ---------------- the main entry ---------------- */
  /**
   * @param spec  field description (see top of file)
   * @param value the raw value as typed (string)
   * @param ctx   optional { badInput: bool, file: File }
   */
  V.check = function (spec, value, ctx) {
    spec = spec || {};
    ctx = ctx || {};
    var kind = spec.kind || 'text';
    var L = V.label(spec);

    if (kind === 'file') return V.checkFile(spec, ctx.file);

    var s = value == null ? '' : String(value);
    var t = s.trim();

    if (kind === 'select') {
      if (t === '') return spec.required ? (spec.requiredMsg || 'Please select ' + aOrAn(lower(L)) + ' ' + lower(L) + '.') : '';
      return '';
    }

    if (t === '') {
      if (ctx.badInput) {
        if (kind === 'date') return 'Invalid ' + lower(L) + '.';
        return L + ' must be a number.';
      }
      if (!spec.required) return '';
      return (s !== '' ? (spec.blankMsg || L + ' cannot be empty or spaces only.') : (spec.requiredMsg || L + ' is required.'));
    }

    if (CONTROL.test(s)) return L + ' contains unsupported characters.';

    switch (kind) {
      case 'password': {
        // BCrypt only uses the first 72 bytes, so longer passwords add nothing.
        var pmin = spec.minlength || 8, pmax = spec.maxlength || 72;
        if (s.length < pmin) return 'Use at least ' + pmin + ' characters.';
        if (s.length > pmax) return L + ' must be at most ' + pmax + ' characters.';
        if (!/\S/.test(s)) return L + ' cannot be spaces only.';
        // same rule as the backend: at least one letter and one number
        if (spec.strong !== false && !(/[A-Za-z]/.test(s) && /\d/.test(s))) return 'Use at least one letter and one number.';
        return '';
      }
      case 'email':
        return V.isEmail(t) ? '' : 'Please enter a valid email address.';
      case 'phone':
        return V.isPhone(t) ? '' : 'Please enter a valid phone number.';
      case 'url': {
        var u;
        try { u = new URL(t); } catch (e) { u = null; }
        if (!u || (u.protocol !== 'http:' && u.protocol !== 'https:') || !u.hostname || /\s/.test(t)) return L + ' must be a valid web address starting with http:// or https://.';
        if (t.length > (spec.maxlength || 255)) return L + ' must be at most ' + (spec.maxlength || 255) + ' characters.';
        return '';
      }
      case 'money': case 'decimal': case 'int': case 'percent':
        return checkNumber(spec, t, ctx, L);
      case 'date': {
        if (!V.isRealDate(t)) return 'Invalid ' + lower(L) + '.';
        var lo = resolveDate(spec.minDate) || '1900-01-01', hi = resolveDate(spec.maxDate) || '2100-12-31';
        if (t < lo) return L + (spec.minDate ? ' cannot be before ' + (spec.minDate === 'today' ? 'today' : lo) + '.' : ' is too far in the past.');
        if (t > hi) return L + (spec.maxDate === 'today' ? ' cannot be in the future.' : spec.maxDate ? ' cannot be after ' + hi + '.' : ' is too far in the future.');
        return '';
      }
      case 'duration': {
        if (!DURATION.test(t)) return L + ' must be in m:ss format, for example 3:45.';
        if (/^0+(:0+)+$/.test(t)) return L + ' must be greater than 0:00.';
        return '';
      }
    }

    /* ---- text-like kinds ---- */
    var min = spec.minlength != null ? spec.minlength : (kind === 'name' || kind === 'person' ? 2 : 0);
    var max = spec.maxlength != null ? spec.maxlength : (kind === 'longtext' ? 1000 : 255);
    if (t.length < min) return spec.minMsg || L + ' must be at least ' + min + ' characters.';
    if (t.length > max) return spec.maxMsg || L + ' must be at most ' + max + ' characters.';

    if (kind === 'name' && !NAME.test(t)) return L + ' contains invalid characters.';
    if (kind === 'person' && !PERSON.test(t)) return L + ' can only contain letters, spaces, dots, hyphens and apostrophes.';
    if (kind === 'location' && !LOCATION.test(t)) return L + ' can only contain letters, numbers, spaces and - _ / .';
    if (kind === 'sku' && !SKU.test(t)) return L + ' must be 2-50 characters: letters, numbers, dots, dashes or underscores.';
    if (kind === 'postal' && !POSTAL.test(t)) return 'Please enter a valid postal code.';
    if (kind === 'address' && !/[\p{L}\p{N}]/u.test(t)) return L + ' is not valid.';
    if (kind === 'text' || kind === 'longtext') { /* any printable text is fine */ }
    return '';
  };

  /* ---------------- files ---------------- */
  var FILE_KINDS = {
    image: {
      exts: ['jpg', 'jpeg', 'png', 'webp'], mimes: ['image/jpeg', 'image/png', 'image/webp'], maxMB: 5,
      missing: 'Please upload a valid image.', type: 'Please upload a supported image file (JPG, JPEG, PNG or WEBP).'
    },
    audio: {
      exts: ['mp3'], mimes: ['audio/mpeg', 'audio/mp3', 'audio/x-mpeg', 'audio/mpeg3'], maxMB: 30,
      missing: 'Please upload a valid audio file.', type: 'Please upload a valid audio file (MP3 only).'
    }
  };
  V.FILE_KINDS = FILE_KINDS;

  V.checkFile = function (spec, file) {
    var def = FILE_KINDS[spec.what || 'image'] || FILE_KINDS.image;
    var exts = spec.exts || def.exts, mimes = spec.mimes || def.mimes, maxMB = spec.maxMB || def.maxMB;
    if (!file) return spec.required ? (spec.requiredMsg || def.missing) : '';
    var name = String(file.name || ''), ext = (name.split('.').pop() || '').toLowerCase();
    if (name.indexOf('.') < 0 || exts.indexOf(ext) < 0) return spec.typeMsg || def.type;
    if (file.type && mimes.indexOf(String(file.type).toLowerCase()) < 0) return spec.typeMsg || def.type;
    if (!file.size) return 'The selected file is empty.';
    if (file.size > maxMB * 1048576) return 'This file is ' + (file.size / 1048576).toFixed(1) + ' MB. The maximum size is ' + maxMB + ' MB.';
    return '';
  };

  // Looks at the first bytes of the file, because a renamed file keeps a fake extension/type.
  V.sniff = function (file, what) {
    return new Promise(function (resolve) {
      if (!file || !file.slice) return resolve(true);
      var r = new FileReader();
      r.onerror = function () { resolve(false); };
      r.onload = function () {
        var b = new Uint8Array(r.result), n = b.length;
        function at(i, s) { for (var k = 0; k < s.length; k++) if (b[i + k] !== s.charCodeAt(k)) return false; return true; }
        if (what === 'audio') return resolve(at(0, 'ID3') || (n > 1 && b[0] === 0xFF && (b[1] & 0xE0) === 0xE0));
        var jpg = n > 2 && b[0] === 0xFF && b[1] === 0xD8 && b[2] === 0xFF;
        var png = n > 7 && b[0] === 0x89 && at(1, 'PNG') && b[4] === 0x0D && b[5] === 0x0A && b[6] === 0x1A && b[7] === 0x0A;
        var webp = n > 11 && at(0, 'RIFF') && at(8, 'WEBP');
        resolve(jpg || png || webp);
      };
      r.readAsArrayBuffer(file.slice(0, 16));
    });
  };

  /* ---------------- shared field specs ---------------- */
  // Spec fragments reused by several pages (keeps the rules identical everywhere).
  V.specs = {
    name: function (label, extra) { return Object.assign({ label: label, kind: 'name', required: true, maxlength: 255 }, extra); },
    person: function (label, extra) { return Object.assign({ label: label, kind: 'person', required: true, maxlength: 100 }, extra); },
    email: function (label, extra) { return Object.assign({ label: label || 'Email', kind: 'email', required: true, maxlength: 254 }, extra); },
    phone: function (label, extra) { return Object.assign({ label: label || 'Phone number', kind: 'phone', required: true }, extra); },
    password: function (label, extra) { return Object.assign({ label: label || 'Password', kind: 'password', required: true, minlength: 8, maxlength: 72 }, extra); }
  };
})(window.MH);
