/* Warranty claims: ADMIN and SALES_MANAGER decide claims */
(function () {
  var esc = MH.esc;
  var NEXT = { PENDING: ['APPROVED', 'REJECTED'], APPROVED: ['COMPLETED', 'REJECTED'], REJECTED: [], COMPLETED: [] };
  var LABEL = { APPROVED: 'Approve', REJECTED: 'Reject', COMPLETED: 'Mark completed' };

  function decide(claim, status, ctx) {
    var rejecting = status === 'REJECTED';
    var form = document.createElement('form'); form.noValidate = true;
    form.innerHTML = '<div id="se"></div><p class="muted" style="margin-top:0">' + (claim.claimType === 'RETURN' ? 'Return' : 'Claim') + ' #' + claim.claimId + ' for <strong>' + esc(claim.productName) + '</strong> from ' + esc(claim.userName) + '.</p><p style="margin:0 0 14px"><strong>Reason:</strong> ' + esc(claim.reason) + '</p>' +
      MH.field({ name: 'resolution', label: rejecting ? 'Reason for rejecting' : 'Note to the customer', type: 'textarea', required: rejecting, minlength: rejecting ? 5 : 0, maxlength: 1000, full: true, placeholder: rejecting ? 'Explain why the claim cannot be accepted' : 'Optional' }, claim.resolution || '') +
      '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Save</button>';
    var busy = false;
    async function go(c, btn) {
      if (busy) return;
      if (!MH.validate(form)) return;
      busy = true; MH.busy(btn, true);
      try {
        await MH.api.put('/warranty-claims/' + claim.claimId + '/status', undefined, { params: { status: status, resolution: form.resolution.value.trim() } });
        c.close(); MH.ok((claim.claimType === 'RETURN' ? 'Return #' : 'Claim #') + claim.claimId + ' is now ' + MH.humanize(status).toLowerCase() + '.'); ctx.reload();
      } catch (e) { form.querySelector('#se').innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; }
      finally { busy = false; MH.busy(btn, false); }
    }
    var ctl = MH.modal({
      title: LABEL[status] + ' claim', body: form, keepOpen: true,
      footer: [{ label: 'Cancel' }, { label: LABEL[status], cls: rejecting ? 'btn-danger solid' : 'btn-primary', onClick: function (c, btn) { go(c, btn); } }]
    });
    form.onsubmit = function (e) { e.preventDefault(); go(ctl, ctl.btn(1)); };
  }

  var actions = [];
  ['APPROVED', 'COMPLETED', 'REJECTED'].forEach(function (s) {
    actions.push({
      label: LABEL[s], cls: s === 'REJECTED' ? 'btn-danger' : (s === 'APPROVED' ? 'btn-primary' : ''), roles: ['ADMIN', 'SALES_MANAGER'],
      show: function (r) { return (NEXT[r.status] || []).indexOf(s) >= 0; },
      run: function (r, ctx) { decide(r, s, ctx); }
    });
  });

  // the same page serves Warranty claims (claimType WARRANTY) and Returns (claimType RETURN)
  var TYPE = window.MH_CLAIM_TYPE === 'RETURN' ? 'RETURN' : 'WARRANTY';
  var RETURNS = TYPE === 'RETURN';
  MH.crud({
    module: RETURNS ? 'returns' : 'warranty-claims', title: RETURNS ? 'Returns' : 'Warranty claims',
    subtitle: RETURNS ? 'Return requests from customers. Approve, reject or complete each return.' : 'Warranty claims from customers. Review each claim and record the outcome.',
    rowFilter: function (r) { return String(r.claimType || 'WARRANTY').toUpperCase() === TYPE; },
    singular: RETURNS ? 'Return' : 'Claim', icon: RETURNS ? 'returns' : 'shield', noCreate: true,
    endpoint: '/warranty-claims', idKey: 'claimId', nameKey: 'claimId',
    viewRoles: ['ADMIN', 'SALES_MANAGER'], writeRoles: ['ADMIN', 'SALES_MANAGER'], deleteRoles: ['ADMIN'],
    canEdit: function () { return false; }, defaultSort: 'claimId',
    searchKeys: ['claimId', 'productName', 'userName', 'userEmail', 'reason', 'status'],
    filters: [{ key: 'status', label: 'Status', options: ['PENDING', 'APPROVED', 'REJECTED', 'COMPLETED'] }],
    columns: [
      { key: 'claimId', label: 'Claim #', type: 'number' }, { key: 'productName', label: 'Product' }, { key: 'userName', label: 'Customer' },
      { key: 'reason', label: 'Reason' },
      { key: 'status', label: 'Status', type: 'badge' }, { key: 'claimDate', label: 'Submitted', type: 'datetime' }
    ],
    viewFields: [
      { key: 'claimId', label: 'Claim #' }, { key: 'productName', label: 'Product' }, { key: 'orderId', label: 'Order #' },
      { key: 'userName', label: 'Customer' }, { key: 'userEmail', label: 'Email' }, { key: 'claimType', label: 'Type', type: 'badge' },
      { key: 'reason', label: 'Reason' }, { key: 'description', label: 'Details' }, { key: 'status', label: 'Status', type: 'badge' },
      { key: 'resolution', label: 'Resolution' }, { key: 'claimDate', label: 'Submitted', type: 'datetime' }
    ],
    rowActions: actions,
    empty: RETURNS ? { title: 'No return requests', readOnlyText: 'Return requests submitted by customers will appear here.' } : { title: 'No warranty claims', readOnlyText: 'Claims submitted by customers will appear here.' }
  });
})();
