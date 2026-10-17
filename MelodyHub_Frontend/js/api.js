/* One place for every HTTP call. Pages never call fetch() directly.
   Paths are always relative to MH.API_BASE_URL, e.g. MH.api.get('/products')
   (never '/api/products': API_BASE_URL already ends with /api). */
(function (MH) {
  function ApiError(status, message, data) {
    this.name = 'ApiError'; this.status = status; this.message = message; this.data = data;
  }
  ApiError.prototype = Object.create(Error.prototype);
  MH.ApiError = ApiError;

  // used when the server did not send a readable message
  var FALLBACK = {
    0: 'Cannot reach the server. Check your connection and that the backend is running on ' + MH.API_BASE_URL.replace(/\/api\/?$/, '') + '.',
    400: 'Some of the information is not valid. Please check the form and try again.',
    401: 'Your session has expired. Please log in again.',
    403: 'You do not have permission to perform this action.',
    404: 'The requested item was not found. It may have been deleted.',
    405: 'This action is not supported.',
    409: 'This conflicts with existing data. Refresh the page and try again.',
    413: 'The file is too large.',
    415: 'This file type or request format is not supported.',
    429: 'Too many attempts. Please wait a moment and try again.',
    500: 'Something went wrong on the server. Please try again in a moment.',
    503: 'This service is temporarily unavailable. Please try again later.'
  };

  function fullUrl(path, params) {
    if (/^https?:\/\//.test(path)) throw new Error('MH.api expects a path such as "/products", not a full URL.');
    if (path.indexOf('/api/') === 0) path = path.slice(4); // guard against /api/api/...
    var url = MH.API_BASE_URL.replace(/\/+$/, '') + (path.charAt(0) === '/' ? path : '/' + path);
    if (params) {
      var q = new URLSearchParams();
      Object.keys(params).forEach(function (k) {
        if (params[k] !== undefined && params[k] !== null && params[k] !== '') q.append(k, params[k]);
      });
      var qs = q.toString();
      if (qs) url += (url.indexOf('?') >= 0 ? '&' : '?') + qs;
    }
    return url;
  }

  function messageFor(status, data, opts) {
    var serverMsg = typeof data === 'string' ? data : (data && data.message) || '';
    if (/^\s*</.test(serverMsg) || serverMsg.length > 400) serverMsg = ''; // never show HTML / stack traces
    if (status === 401) return opts.auth === false && serverMsg ? serverMsg : FALLBACK[401];
    if (status >= 500) return status === 503 && serverMsg ? serverMsg : FALLBACK[status] || FALLBACK[500];
    if (status === 403) return serverMsg && serverMsg !== 'Forbidden' ? serverMsg : FALLBACK[403];
    return serverMsg || FALLBACK[status] || 'The request could not be completed. Please try again.';
  }

  async function send(method, path, opts) {
    opts = opts || {};
    var headers = { Accept: opts.accept || 'application/json' };
    var init = { method: method, headers: headers };
    if (opts.auth !== false) {
      var token = MH.auth.getToken();
      if (token) headers.Authorization = 'Bearer ' + token;
    }
    if (opts.formData) {
      init.body = opts.formData; // the browser sets the multipart boundary itself
    } else if (opts.body !== undefined) {
      headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(opts.body);
    }
    var res;
    try {
      res = await fetch(fullUrl(path, opts.params), init);
    } catch (e) {
      throw new ApiError(0, FALLBACK[0]);
    }
    if (res.ok) return res;

    var text = await res.text(), data = null;
    if (text) { try { data = JSON.parse(text); } catch (e) { data = text; } }
    // a rejected or expired token on a protected call: clear it and go to the login page
    if (res.status === 401 && opts.auth !== false && MH.auth.getToken()) MH.auth.expire();
    throw new ApiError(res.status, messageFor(res.status, data, opts), data);
  }

  async function request(method, path, opts) {
    var res = await send(method, path, opts);
    var text = await res.text();
    if (!text) return null;
    try { return JSON.parse(text); } catch (e) { return text; }
  }

  // builds multipart data: a JSON part called "data" and optional file parts
  function multipart(data, files) {
    var fd = new FormData();
    fd.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    Object.keys(files || {}).forEach(function (k) { if (files[k]) fd.append(k, files[k]); });
    return fd;
  }

  /** Downloads a protected file (Authorization header) and saves it with the server's file name. */
  async function download(path, fallbackName) {
    var res = await send('GET', path, { accept: '*/*' });
    var name = fallbackName || 'download';
    var cd = res.headers.get('Content-Disposition') || '';
    var star = /filename\*=UTF-8''([^;]+)/i.exec(cd), plain = /filename="?([^";]+)"?/i.exec(cd);
    try { if (star) name = decodeURIComponent(star[1]); else if (plain) name = plain[1]; } catch (e) { /* keep fallback */ }
    var blob = await res.blob();
    var url = URL.createObjectURL(blob), a = document.createElement('a');
    a.href = url; a.download = name; a.rel = 'noopener';
    document.body.appendChild(a); a.click(); a.remove();
    setTimeout(function () { URL.revokeObjectURL(url); }, 2000);
    return { name: name, size: blob.size };
  }

  MH.api = {
    url: fullUrl,
    get: function (p, o) { return request('GET', p, o); },
    post: function (p, body, o) { return request('POST', p, Object.assign({ body: body }, o)); },
    put: function (p, body, o) { return request('PUT', p, Object.assign({ body: body }, o)); },
    del: function (p, o) { return request('DELETE', p, o); },
    upload: function (p, formData, o) { return request('POST', p, Object.assign({ formData: formData }, o)); },
    postForm: function (p, data, files, o) { return request('POST', p, Object.assign({ formData: multipart(data, files) }, o)); },
    putForm: function (p, data, files, o) { return request('PUT', p, Object.assign({ formData: multipart(data, files) }, o)); },
    download: download
  };
})(window.MH);
