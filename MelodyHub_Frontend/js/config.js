/* MelodyHub frontend configuration. Loaded first on every page. */
(function () {
  // Absolute URL of the frontend root, derived from this script's own URL,
  // so links work from any folder depth and any dev server path.
  var src = document.currentScript.src;
  window.MH = {
    API_BASE_URL: 'http://localhost:8080/api', // <-- change if the backend runs elsewhere
    ROOT: src.replace(/js\/config\.js.*$/, ''),
    CURRENCY: 'LKR',
    SESSION_KEY: 'melodyhub.session',
    ROLES: {
      CUSTOMER: 'CUSTOMER', ADMIN: 'ADMIN', INVENTORY_MANAGER: 'INVENTORY_MANAGER',
      ARTIST: 'ARTIST', MARKETING_MANAGER: 'MARKETING_MANAGER',
      SALES_MANAGER: 'SALES_MANAGER', SUPPLIER: 'SUPPLIER'
    },
    url: function (path) { return window.MH.ROOT + path; }
  };
})();
