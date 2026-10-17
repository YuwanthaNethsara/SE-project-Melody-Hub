(function () {
  var esc = MH.esc, $ = function (id) { return document.getElementById(id); };
  $('logo-link').outerHTML = MH.logo();
  $('art-logo').outerHTML = MH.logo();
  $('auth-art').insertAdjacentHTML('beforeend', MH.eqBars(30, [8, 11, 12, 19]));
  if (MH.auth.isLoggedIn()) { location.replace(MH.auth.homeFor(MH.auth.getRole())); return; }

  if (MH.qs('expired')) $('notice').innerHTML = '<div class="alert alert-warn">Your session has expired. Please login again.</div>';
  if (MH.qs('registered')) $('notice').innerHTML = '<div class="alert alert-ok">Account created. Please log in.</div>';

  var tg = $('toggle'); tg.innerHTML = MH.icon('eye');
  tg.onclick = function () { var show = $('password').type === 'password'; $('password').type = show ? 'text' : 'password'; tg.setAttribute('aria-label', show ? 'Hide password' : 'Show password'); };

  function safeNext() {
    var n = MH.qs('next'); if (!n) return null;
    try { var root = new URL(MH.ROOT).pathname; return n.indexOf(root) === 0 ? n : null; } catch (e) { return null; }
  }

  var loginForm = $('form');
  // login only checks that the fields are filled and look right; the password rules belong to registration
  MH.attachSpecs(loginForm, {
    email: MH.v.specs.email('Email'),
    password: { label: 'Password', kind: 'text', required: true, maxlength: 100 }
  });
  var signingIn = false;
  loginForm.onsubmit = async function (e) {
    e.preventDefault();
    if (signingIn) return; // double click / Enter + click
    var f = e.target, btn = $('submit');
    $('form-error').innerHTML = '';
    if (!MH.validate(f, { silent: true })) return;
    signingIn = true;
    MH.busy(btn, true, 'Logging in…');
    try {
      var res = await MH.api.post('/auth/login', { email: f.email.value.trim(), password: f.password.value }, { auth: false });
      if (!res || !res.token) throw new Error('Login failed. Please try again.');
      MH.auth.save(res);
      location.href = safeNext() || MH.auth.homeFor(res.role);
    } catch (err) {
      $('form-error').innerHTML = '<div class="alert alert-error">' + esc(err.status === 401 || err.status === 400 && /password|credential/i.test(err.message) ? 'Incorrect email or password.' : err.message) + '</div>';
      MH.busy(btn, false); signingIn = false;
    }
  };

  /* ---------- forgot-password flow (Spring Boot AuthController) ----------
       POST /auth/forgot-password  { email }                                           -> generic "sent" message
       POST /auth/verify-otp       { email, otp }                                      -> { message, resetToken }
       POST /auth/reset-password   { email, resetToken, newPassword, confirmPassword } -> { message }
     No OTP is generated, stored or shown in the browser. */
  var forgot = $('forgot-view'), login = $('login-view'), expiryTimer = null, expirySec = 0, cooldownSec = 0, email = '', resetToken = '';
  var OTP_LIFETIME = 300, RESEND_COOLDOWN = 60; // seconds; the server enforces the same limits

  function showForgot() { login.hidden = true; forgot.hidden = false; setStep(1); clearForgotError(); $('forgot-email').value = $('email').value.trim(); $('forgot-email').focus(); }
  function showLogin() { stopTimer(); forgot.hidden = true; login.hidden = false; clearForgotState(); }
  function clearForgotState() {
    ['forgot-email', 'otp', 'new-password', 'confirm-password'].forEach(function (id) { $(id).value = ''; MH.setFieldError($(id), ''); });
    $('forgot-email-form').hidden = false; $('otp-form').hidden = true; $('reset-form').hidden = true; $('forgot-success').hidden = true; resetToken = ''; email = ''; clearForgotError();
  }
  function clearForgotError() { $('forgot-error').innerHTML = ''; }
  function error(msg) { $('forgot-error').innerHTML = '<div class="alert alert-error">' + esc(msg) + '</div>'; }
  function setStep(n) {
    $('forgot-step').textContent = 'Step ' + n + ' of 3';
    $('forgot-title').textContent = n === 1 ? 'Forgot your password?' : n === 2 ? 'Verify your email' : 'Create a new password';
    $('forgot-copy').textContent = n === 1 ? 'Enter the email address linked to your MelodyHub account and we’ll send you a verification code.' : n === 2 ? 'If an account exists for ' + email + ', we sent it a 6-digit verification code. Enter the code below to continue.' : 'Choose a new password for your MelodyHub account.';
  }
  function startTimer() {
    stopTimer(); expirySec = OTP_LIFETIME; cooldownSec = RESEND_COOLDOWN; tick();
    expiryTimer = setInterval(function () { expirySec--; if (cooldownSec > 0) cooldownSec--; tick(); if (expirySec <= 0) stopTimer(); }, 1000);
  }
  function stopTimer() { if (expiryTimer) { clearInterval(expiryTimer); expiryTimer = null; } }
  function two(n) { return String(n).padStart(2, '0'); }
  function tick() {
    $('otp-timer').textContent = expirySec > 0 ? 'Code expires in ' + two(Math.floor(expirySec / 60)) + ':' + two(expirySec % 60) : 'Code expired. Request a new one.';
    var r = $('resend-otp');
    if (!r.dataset.sending) { r.disabled = cooldownSec > 0; r.textContent = cooldownSec > 0 ? 'Resend OTP in ' + cooldownSec + 's' : 'Resend OTP'; }
  }

  $('forgot-link').onclick = showForgot;
  $('forgot-back').onclick = showLogin;
  $('back-to-login').onclick = showLogin;

  var sending = false;
  $('forgot-email-form').onsubmit = async function (e) {
    e.preventDefault(); clearForgotError();
    if (sending) return;
    var input = $('forgot-email'), msg = MH.v.check(MH.v.specs.email('Email address'), input.value);
    MH.setFieldError(input, msg);
    if (msg) return;
    email = input.value.trim();
    var btn = $('send-otp'); sending = true; MH.busy(btn, true, 'Sending OTP…');
    try {
      await MH.api.post('/auth/forgot-password', { email: email }, { auth: false });
      $('forgot-email-form').hidden = true; $('otp-form').hidden = false; setStep(2); startTimer(); $('otp').focus();
    } catch (err) {
      // 429 = the server's resend cooldown is still running; the code we already sent is still valid
      if (err.status === 429) { $('forgot-email-form').hidden = true; $('otp-form').hidden = false; setStep(2); error(err.message); }
      else error(err.message || 'Unable to send the verification code. Please try again.');
    }
    sending = false; MH.busy(btn, false);
  };

  $('otp').oninput = function () { this.value = this.value.replace(/\D/g, '').slice(0, 6); if (this.value.length) MH.setFieldError(this, ''); };
  var verifying = false;
  $('otp-form').onsubmit = async function (e) {
    e.preventDefault(); clearForgotError();
    if (verifying) return;
    var otp = $('otp').value.trim();
    if (!/^\d{6}$/.test(otp)) { MH.setFieldError($('otp'), otp ? 'Enter all 6 digits of the verification code.' : 'Verification code is required.'); return; }
    MH.setFieldError($('otp'), '');
    var btn = $('verify-otp'); verifying = true; MH.busy(btn, true, 'Verifying…');
    try {
      var res = await MH.api.post('/auth/verify-otp', { email: email, otp: otp }, { auth: false });
      resetToken = res && (res.resetToken || res.verificationToken || res.token);
      if (!resetToken) throw new Error('The server did not return a password-reset token. Please request a new code.');
      stopTimer(); $('otp-form').hidden = true; $('reset-form').hidden = false; setStep(3); $('new-password').focus();
    } catch (err) { error(err.message || 'The verification code is invalid or expired.'); }
    verifying = false; MH.busy(btn, false);
  };

  $('resend-otp').onclick = async function () {
    var btn = $('resend-otp');
    if (cooldownSec > 0 || btn.dataset.sending) return;
    clearForgotError(); btn.dataset.sending = '1'; btn.disabled = true; btn.textContent = 'Sending…';
    try { await MH.api.post('/auth/forgot-password', { email: email }, { auth: false }); delete btn.dataset.sending; startTimer(); $('otp').value = ''; $('otp').focus(); }
    catch (err) { delete btn.dataset.sending; error(err.message || 'Unable to resend the verification code.'); if (err.status === 429) cooldownSec = Math.max(cooldownSec, 15); tick(); }
  };

  var newToggle = $('toggle-new-password'); newToggle.innerHTML = MH.icon('eye');
  newToggle.onclick = function () { var show = $('new-password').type === 'password'; $('new-password').type = show ? 'text' : 'password'; $('confirm-password').type = show ? 'text' : 'password'; newToggle.setAttribute('aria-label', show ? 'Hide password' : 'Show password'); };
  $('new-password').oninput = function () { MH.setFieldError(this, ''); };
  $('confirm-password').oninput = function () { MH.setFieldError(this, ''); };

  var resetting = false;
  $('reset-form').onsubmit = async function (e) {
    e.preventDefault(); clearForgotError();
    if (resetting) return;
    var pw = $('new-password'), cf = $('confirm-password'), ok = true;
    var m1 = MH.v.check(MH.v.specs.password('New password'), pw.value);
    MH.setFieldError(pw, m1); if (m1) ok = false;
    var m2 = !cf.value ? 'Please confirm your new password.' : (!m1 && cf.value !== pw.value ? 'The passwords do not match.' : '');
    MH.setFieldError(cf, m2); if (m2) ok = false;
    if (!ok) return;
    var btn = $('reset-password'); resetting = true; MH.busy(btn, true, 'Resetting password…');
    try {
      await MH.api.post('/auth/reset-password', { email: email, resetToken: resetToken, newPassword: pw.value, confirmPassword: cf.value }, { auth: false });
      $('reset-form').hidden = true; $('forgot-success').hidden = false; resetToken = ''; pw.value = cf.value = '';
    } catch (err) { error(err.message || 'Unable to reset your password. Please request a new code and try again.'); }
    resetting = false; MH.busy(btn, false);
  };
})();
