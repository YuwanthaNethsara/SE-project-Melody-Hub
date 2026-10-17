/* Session handling: the JWT and the user returned by /api/auth/login live in localStorage. */
(function (MH) {
  function read() {
    try { return JSON.parse(localStorage.getItem(MH.SESSION_KEY) || 'null'); } catch (e) { return null; }
  }
  function decode(token) {
    try {
      var p = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(decodeURIComponent(atob(p).split('').map(function (c) { return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2); }).join('')));
    } catch (e) { return null; }
  }
  function expired(token) {
    var c = decode(token);
    return !c || (c.exp && c.exp * 1000 <= Date.now());
  }

  var staffRoles = ['ADMIN', 'INVENTORY_MANAGER', 'ARTIST', 'MARKETING_MANAGER', 'SALES_MANAGER', 'SUPPLIER'];

  MH.auth = {
    getToken: function () {
      var s = read();
      if (!s || !s.token) return null;
      if (expired(s.token)) { localStorage.removeItem(MH.SESSION_KEY); return null; }
      return s.token;
    },
    isLoggedIn: function () { return !!MH.auth.getToken(); },
    getCurrentUser: function () { return MH.auth.isLoggedIn() ? read() : null; },
    getRole: function () { var u = MH.auth.getCurrentUser(); return u ? u.role : null; },
    isStaff: function () { return staffRoles.indexOf(MH.auth.getRole()) >= 0; },
    save: function (loginResponse) {
      localStorage.setItem(MH.SESSION_KEY, JSON.stringify({
        token: loginResponse.token, id: loginResponse.id, name: loginResponse.name,
        email: loginResponse.email, role: loginResponse.role
      }));
    },
    updateName: function (name) {
      var s = read(); if (s) { s.name = name; localStorage.setItem(MH.SESSION_KEY, JSON.stringify(s)); }
    },
    // where each role lands after login
    homeFor: function (role) {
      return role === 'CUSTOMER' ? MH.url('customer/dashboard/') : MH.url('admin/dashboard/');
    },
    loginUrl: function (extra) {
      var next = location.pathname + location.search;
      return MH.url('auth/login/') + '?next=' + encodeURIComponent(next) + (extra || '');
    },
    logout: function (redirect) {
      localStorage.removeItem(MH.SESSION_KEY);
      location.href = redirect || MH.url('index.html');
    },
    // token rejected by the server or past its expiry
    expire: function () {
      localStorage.removeItem(MH.SESSION_KEY);
      location.href = MH.auth.loginUrl('&expired=1');
    }
  };
})(window.MH);
