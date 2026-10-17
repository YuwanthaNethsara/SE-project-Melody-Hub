/* Payments: ADMIN and SALES_MANAGER view, ADMIN can correct a status */
(function () {
  var esc = MH.esc;
  function setStatus(p, ctx) {
    var form = document.createElement('form'); form.noValidate = true;
    form.innerHTML = '<div id="se"></div>' + MH.field({ name: 'status', label: 'Payment status', type: 'select', required: true, options: ['PENDING', 'PAID', 'FAILED', 'REFUNDED'], full: true }, p.paymentStatus);
    MH.modal({
      title: 'Update payment #' + p.paymentId, body: form,
      footer: [{ label: 'Cancel' }, { label: 'Save status', cls: 'btn-primary', onClick: async function (c, btn) {
        if (!MH.validate(form)) return;
        MH.busy(btn, true);
        try {
          await MH.api.put('/payments/' + p.paymentId + '/status', undefined, { params: { status: form.status.value } });
          c.close(); MH.ok('Payment status updated.'); ctx.reload();
        } catch (e) { form.querySelector('#se').innerHTML = '<div class="alert alert-error">' + esc(e.message) + '</div>'; MH.busy(btn, false); }
      } }]
    });
  }
  MH.crud({
    module: 'payments', title: 'Payments', subtitle: 'Every payment received for customer orders.',
    singular: 'Payment', icon: 'card', noCreate: true,
    endpoint: '/payments', idKey: 'paymentId', nameKey: 'paymentId',
    viewRoles: ['ADMIN', 'SALES_MANAGER'], writeRoles: ['ADMIN'], deleteRoles: ['ADMIN'],
    canEdit: function () { return false; }, defaultSort: 'paymentId',
    searchKeys: ['paymentId', 'orderId', 'paymentMethod', 'transactionId', 'paymentStatus'],
    filters: [{ key: 'paymentStatus', label: 'Status' }, { key: 'paymentMethod', label: 'Method' }],
    columns: [
      { key: 'paymentId', label: 'Payment #', type: 'number' }, { key: 'orderId', label: 'Order #', type: 'number' },
      { key: 'amount', label: 'Amount', type: 'money' }, { key: 'paymentMethod', label: 'Method', render: function (r) { return esc(MH.humanize(r.paymentMethod)); } },
      { key: 'paymentStatus', label: 'Status', type: 'badge' }, { key: 'paymentDate', label: 'Date', type: 'date' }, { key: 'transactionId', label: 'Reference', render: function (r) { return r.transactionId ? '<span class="clip faint" style="max-width:120px" title="' + esc(r.transactionId) + '">' + esc(r.transactionId) + '</span>' : '<span class="faint">—</span>'; } }
    ],
    rowActions: [{ label: 'Set status', roles: ['ADMIN'], run: setStatus }],
    empty: { title: 'No payments yet', text: 'Payments appear here once customers pay for their orders.' }
  });
})();
