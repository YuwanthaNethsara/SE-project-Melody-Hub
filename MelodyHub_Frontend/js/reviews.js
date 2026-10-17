/* Customer feedback: review form (stars + title + comment) shared by the product page and "My reviews". */
(function (MH) {
  var esc = MH.esc;
  var WORDS = { 1: 'Poor', 2: 'Fair', 3: 'Good', 4: 'Very good', 5: 'Excellent' };

  function starInput(value) {
    var h = '<fieldset class="field" data-field="rating" style="border:0;padding:0;margin:0 0 16px"><legend style="font-size:13.5px;font-weight:600;color:var(--ink);margin-bottom:6px">Your rating<span class="req" aria-hidden="true">*</span></legend><div class="row" style="gap:14px"><div class="star-input">';
    for (var i = 5; i >= 1; i--) {
      h += '<input type="radio" name="rating" id="star' + i + '" value="' + i + '"' + (Number(value) === i ? ' checked' : '') + '><label for="star' + i + '" title="' + WORDS[i] + '">★<span class="sr-only">' + i + ' star' + (i > 1 ? 's' : '') + ', ' + WORDS[i] + '</span></label>';
    }
    return h + '</div><span class="muted" id="star-word">' + (value ? WORDS[value] : 'Choose 1 to 5 stars') + '</span></div><div class="field-error" role="alert"></div></fieldset>';
  }

  /**
   * Opens the review form. opts: { productId, productName, review (existing review to edit) }
   * Resolves with the saved review, or null when cancelled.
   */
  MH.openReviewForm = function (opts) {
    return new Promise(function (resolve) {
      var r = opts.review || {}, editing = !!opts.review, done = false;
      var form = document.createElement('form'); form.noValidate = true;
      form.innerHTML = '<div id="rv-err"></div><p class="muted" style="margin-top:0">' + (editing ? 'Update your review of ' : 'Share what you think of ') + '<strong>' + esc(opts.productName) + '</strong>. Reviews are public and show your first name only.</p>' +
        starInput(r.rating) +
        MH.field({ name: 'title', label: 'Headline', kind: 'text', maxlength: 120, placeholder: 'For example: Great first guitar' }, r.title || '') +
        MH.field({ name: 'comment', label: 'Your review', type: 'textarea', kind: 'longtext', required: true, minlength: 10, maxlength: 1000, placeholder: 'What did you like? How was the sound, quality and delivery?' }, r.comment || '') +
        '<button type="submit" class="sr-only" tabindex="-1" aria-hidden="true">Save</button>';
      form.addEventListener('change', function (e) {
        if (e.target.name === 'rating') { form.querySelector('#star-word').textContent = WORDS[e.target.value]; MH.setFieldError(form.querySelector('#star1'), ''); }
      });
      var busy = false;
      async function save(c, btn) {
        if (busy) return;
        var rating = form.querySelector('input[name="rating"]:checked');
        var ok = MH.validate(form, { silent: true });
        if (!rating) { MH.setFieldError(form.querySelector('#star1'), 'Please choose a rating.'); ok = false; }
        if (!ok) return;
        busy = true; MH.busy(btn, true, 'Saving…');
        var body = { productId: opts.productId, rating: Number(rating.value), title: form.title.value.trim() || null, comment: form.comment.value.trim() };
        try {
          var saved = editing ? await MH.api.put('/reviews/' + r.reviewId, body) : await MH.api.post('/reviews', body);
          done = true; c.close(); MH.ok(editing ? 'Your review was updated.' : 'Thanks, your review is now live.'); resolve(saved);
        } catch (e) {
          form.querySelector('#rv-err').innerHTML = '<div class="alert alert-error">' + MH.icon('alert', 16) + '<span>' + esc(e.message) + '</span></div>';
        } finally { busy = false; MH.busy(btn, false); }
      }
      var ctl = MH.modal({
        title: editing ? 'Edit your review' : 'Write a review', body: form, keepOpen: true,
        onClose: function () { if (!done) resolve(null); },
        footer: [{ label: 'Cancel' }, { label: editing ? 'Save changes' : 'Post review', cls: 'btn-primary', onClick: function (c, btn) { save(c, btn); } }]
      });
      form.onsubmit = function (e) { e.preventDefault(); save(ctl, ctl.btn(1)); };
    });
  };

  MH.reviewItem = function (r, extraHtml) {
    return '<article class="review">' + MH.stars(r.rating) + (r.title ? '<h4>' + esc(r.title) + '</h4>' : '<div style="height:6px"></div>') + '<p>' + esc(r.comment) + '</p>' +
      '<div class="who"><strong>' + esc(r.reviewerName) + '</strong><span>' + MH.date(r.createdAt) + '</span>' + (r.verifiedPurchase ? '<span class="verified">' + MH.icon('check', 13) + 'Verified purchase</span>' : '') + (extraHtml || '') + '</div></article>';
  };
})(window.MH);
