(function () {
  var esc = MH.esc, $ = function (id) { return document.getElementById(id); };
  $('logo-link').outerHTML = MH.logo();
  $('art-logo').outerHTML = MH.logo();
  $('auth-art').insertAdjacentHTML('beforeend', MH.eqBars(30, [8, 11, 12, 19]));
  if (MH.auth.isLoggedIn()) { location.replace(MH.auth.homeFor(MH.auth.getRole())); return; }

  var tg = $('toggle'); tg.innerHTML = MH.icon('eye');
  tg.onclick = function () { var show = $('password').type === 'password'; $('password').type = $('confirm').type = show ? 'text' : 'password'; tg.setAttribute('aria-label', show ? 'Hide password' : 'Show password'); };

  var LEVELS = [['Too weak', '#c0281d', 12], ['Weak', '#c0281d', 30], ['Fair', '#e6a600', 55], ['Good', '#157a3c', 80], ['Strong', '#157a3c', 100]];
  $('password').oninput = function () {
    var v = $('password').value, l = v ? LEVELS[MH.passwordScore(v)] : null;
    $('bar').style.width = l ? l[2] + '%' : '0'; $('bar').style.background = l ? l[1] : 'transparent';
    $('strength-text').textContent = l ? 'Password strength: ' + l[0] : 'Use at least 8 characters, including a letter and a number.';
  };

  var form = $('form'), creating = false;
  MH.attachSpecs(form, {
    name: MH.v.specs.person('Full name'),
    email: MH.v.specs.email('Email'),
    password: MH.v.specs.password('Password'),
    confirm: { label: 'Confirm password', kind: 'text', required: true, maxlength: 72, errorLabel: 'Confirm password' }
  });
  function cross(v) { return v.confirm && v.password && v.confirm !== v.password ? { confirm: 'The passwords do not match.' } : {}; }
  form._mhCross = cross;
  // the matching rule is checked again while typing in either password box
  $('password').addEventListener('input', function () { if (form.confirm.value) { var wrap = form.confirm.closest('.field'); if (wrap.classList.contains('invalid')) return; if (cross(MH.readForm(form)).confirm) MH.setFieldError(form.confirm, 'The passwords do not match.'); } });

  form.onsubmit = async function (e) {
    e.preventDefault();
    if (creating) return;
    var f = e.target, btn = $('submit');
    $('form-error').innerHTML = '';
    if (!MH.validate(f, { cross: cross })) return;

    creating = true;
    MH.busy(btn, true, 'Creating account…');
    var creds = { email: f.email.value.trim(), password: f.password.value };
    try {
      await MH.api.post('/auth/register', { name: f.elements.name.value.trim(), email: creds.email, password: creds.password }, { auth: false });
    } catch (err) {
      $('form-error').innerHTML = '<div class="alert alert-error">' + esc(err.status === 409 || /already/i.test(err.message) ? 'An account with this email already exists. Try logging in instead.' : err.message) + '</div>';
      MH.busy(btn, false); creating = false; return;
    }
    MH.ok('Account created. Welcome to MelodyHub!');
    try { MH.auth.save(await MH.api.post('/auth/login', creds, { auth: false })); location.href = MH.auth.homeFor('CUSTOMER'); }
    catch (err) { location.href = '../login/?registered=1'; }
  };
})();
