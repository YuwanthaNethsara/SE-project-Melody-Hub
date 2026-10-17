/* Page guards. Hiding things in the UI is only a convenience; the backend enforces the real rules. */
(function (MH) {
  MH.guards = {
    // returns true when the page may continue to load
    requireAuth: function () {
      if (MH.auth.isLoggedIn()) return true;
      location.replace(MH.auth.loginUrl(localStorage.getItem(MH.SESSION_KEY) ? '&expired=1' : ''));
      return false;
    },
    requireRole: function (roles) {
      if (!MH.guards.requireAuth()) return false;
      var role = MH.auth.getRole();
      if (roles.indexOf(role) >= 0) return true;
      MH.guards.accessDenied();
      return false;
    },
    accessDenied: function () {
      var home = MH.auth.homeFor(MH.auth.getRole());
      document.body.innerHTML =
        '<div class="access-denied"><div>' +
        '<div class="code">403</div><h1>Access denied</h1>' +
        '<p class="muted">Your account does not have permission to open this page.</p>' +
        '<div class="row" style="justify-content:center"><a class="btn btn-primary" href="' + home + '">Go to my dashboard</a>' +
        '<a class="btn" href="' + MH.url('index.html') + '">Back to the store</a></div></div></div>';
    }
  };
})(window.MH);
