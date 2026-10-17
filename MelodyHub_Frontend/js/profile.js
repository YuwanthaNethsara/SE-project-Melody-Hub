/* Profile page shared by customers and staff */
MH.profilePage = async function (opts) {
  if (!MH.guards.requireRole(opts.roles)) return;
  var esc = MH.esc;
  var content = MH.mountPortal({ active: opts.active, title: 'My profile', subtitle: 'Your account details and password.' });
  content.innerHTML = MH.skeletonRows(4);
  var me;
  try { me = await MH.api.get('/auth/me'); } catch (e) { content.innerHTML = '<div class="card">' + MH.errorState(e.message) + '</div>'; document.getElementById('retry').onclick = function () { location.reload(); }; return; }

  content.innerHTML = '<div class="grid grid-2" style="align-items:start">' +
    '<section class="card"><h3 class="panel-title">Account</h3><div class="row" style="margin-bottom:18px;gap:14px"><span class="avatar" style="width:56px;height:56px;font-size:20px">' + esc(MH.initials(me.name)) + '</span><div><strong>' + esc(me.name) + '</strong><br><span class="muted">' + esc(me.email) + '</span><br><span class="role-pill">' + esc(MH.humanize(me.role)) + '</span></div></div>' +
    '<form id="pf" novalidate><div id="pf-msg"></div>' + MH.field({ name: 'name', label: 'Full name', kind: 'person', required: true, maxlength: 100 }, me.name) + MH.field({ name: 'email', label: 'Email', type: 'email', disabled: true, help: 'Contact an administrator to change your email address.' }, me.email) +
    '<button class="btn btn-primary" id="pf-save">Save changes</button></form></section>' +
    '<section class="card"><h3 class="panel-title">Change password</h3><form id="pw" novalidate><div id="pw-msg"></div>' +
    MH.field({ name: 'currentPassword', label: 'Current password', type: 'password', kind: 'text', required: true, maxlength: 100, autocomplete: 'current-password' }, '') +
    MH.field({ name: 'password', label: 'New password', type: 'password', required: true, minlength: 8, maxlength: 72, autocomplete: 'new-password', help: 'At least 8 characters, including a letter and a number.' }, '') +
    MH.field({ name: 'confirm', label: 'Confirm new password', type: 'password', kind: 'text', required: true, maxlength: 72, errorLabel: 'Confirm new password', autocomplete: 'new-password' }, '') +
    '<button class="btn btn-primary" id="pw-save">Update password</button></form></section></div>';

  var savingProfile = false, savingPw = false;
  document.getElementById('pf').onsubmit = async function (e) {
    e.preventDefault();
    if (savingProfile) return;
    var f = e.target, btn = document.getElementById('pf-save');
    if (!MH.validate(f)) return;
    savingProfile = true; MH.busy(btn, true);
    try { var u = await MH.api.put('/auth/me', { name: f.elements.name.value.trim() }); MH.auth.updateName(u.name); MH.ok('Profile updated.'); setTimeout(function () { location.reload(); }, 600); }
    catch (x) { document.getElementById('pf-msg').innerHTML = '<div class="alert alert-error">' + esc(x.message) + '</div>'; MH.busy(btn, false); savingProfile = false; }
  };
  var pwCross = function (v) {
    var errs = {};
    if (v.password && v.confirm && v.password !== v.confirm) errs.confirm = 'The passwords do not match.';
    if (v.password && v.currentPassword && v.password === v.currentPassword) errs.password = 'The new password must be different from the current password.';
    return errs;
  };
  document.getElementById('pw').onsubmit = async function (e) {
    e.preventDefault();
    if (savingPw) return;
    var f = e.target, btn = document.getElementById('pw-save');
    if (!MH.validate(f, { cross: pwCross })) return;
    savingPw = true; MH.busy(btn, true);
    document.getElementById('pw-msg').innerHTML = '';
    try { await MH.api.put('/auth/me', { currentPassword: f.currentPassword.value, password: f.password.value }); f.reset(); MH.ok('Password changed.'); }
    catch (x) { document.getElementById('pw-msg').innerHTML = '<div class="alert alert-error">' + esc(x.message) + '</div>'; }
    MH.busy(btn, false); savingPw = false;
  };
};
