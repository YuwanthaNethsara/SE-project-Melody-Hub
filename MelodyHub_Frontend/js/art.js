/* MelodyHub artwork: self-contained vector illustrations used wherever a product, artist or
   category has no uploaded image. They are drawn in code, so they always load (no network,
   no broken-image icons) and stay sharp at any size. A real imageUrl from the API always wins. */
(function (root) {
  var P = {}; // kind -> inner markup in a 400x400 box, resting on y=350

  P.guitar = '<defs><linearGradient id="gw" x1="0" x2="1"><stop offset="0" stop-color="#e9a93b"/><stop offset=".5" stop-color="#f7d27f"/><stop offset="1" stop-color="#d28a26"/></linearGradient><linearGradient id="gk" x1="0" x2="1"><stop offset="0" stop-color="#4a2a14"/><stop offset="1" stop-color="#6e4224"/></linearGradient></defs>' +
    '<g transform="rotate(-12 200 220)"><path d="M188 14h24l3 42h-30z" fill="url(#gk)"/>' +
    '<g fill="#d8d3c4"><circle cx="184" cy="24" r="3.5"/><circle cx="184" cy="38" r="3.5"/><circle cx="184" cy="51" r="3.5"/><circle cx="216" cy="24" r="3.5"/><circle cx="216" cy="38" r="3.5"/><circle cx="216" cy="51" r="3.5"/></g>' +
    '<rect x="191" y="54" width="18" height="150" fill="url(#gk)"/><rect x="193.5" y="56" width="13" height="148" fill="#2a1a10"/>' +
    '<g stroke="#b8b09a" stroke-width="1.2"><path d="M193.5 80h13M193.5 104h13M193.5 126h13M193.5 146h13M193.5 164h13M193.5 180h13"/></g>' +
    '<path d="M200 150C160 150 140 175 145 205c3 20-17 33-19 67-2 48 34 78 74 78s76-30 74-78c-2-34-22-47-19-67 5-30-15-55-55-55z" fill="url(#gw)" stroke="#b9792a" stroke-width="2"/>' +
    '<path d="M200 158c-34 0-50 20-46 46 3 20-16 34-18 68-1 40 30 68 64 68s65-28 64-68c-2-34-21-48-18-68 4-26-12-46-46-46z" fill="none" stroke="rgba(120,70,20,.25)" stroke-width="3"/>' +
    '<circle cx="200" cy="244" r="28" fill="#2a1a10"/><circle cx="200" cy="244" r="32" fill="none" stroke="#5b3418" stroke-width="4"/>' +
    '<path d="M234 256q24-6 27 14-5 22-33 19 12-16 6-33z" fill="#241a12"/><rect x="178" y="310" width="44" height="10" rx="3" fill="#3a2314"/>' +
    '<g stroke="#efe9d6" stroke-width="1"><path d="M194 56V312M197 56V312M200 56V312M203 56V312M206 56V312"/></g></g>';

  P.electric = '<defs><linearGradient id="eg" x1="0" x2="1"><stop offset="0" stop-color="#b92f17"/><stop offset=".5" stop-color="#e5532f"/><stop offset="1" stop-color="#a32612"/></linearGradient><linearGradient id="ek" x1="0" x2="1"><stop offset="0" stop-color="#8a5a32"/><stop offset="1" stop-color="#b98350"/></linearGradient></defs>' +
    '<g transform="rotate(-14 200 220)"><path d="M188 12h24l2 40h-28z" fill="#2a1a10"/><g fill="#cfcabb"><circle cx="186" cy="22" r="3"/><circle cx="186" cy="34" r="3"/><circle cx="186" cy="46" r="3"/><circle cx="214" cy="22" r="3"/><circle cx="214" cy="34" r="3"/><circle cx="214" cy="46" r="3"/></g>' +
    '<rect x="192" y="52" width="16" height="150" fill="url(#ek)"/><g stroke="#8c867a" stroke-width="1.2"><path d="M192 78h16M192 102h16M192 124h16M192 144h16M192 162h16M192 178h16"/></g>' +
    '<path d="M200 196c-30-22-66-6-62 28 2 16-10 24-8 48 2 38 38 70 84 66 34-3 60-22 60-56 0-18-12-24-6-44 6-22-8-44-34-44-12 0-22 2-34 2z" fill="url(#eg)" stroke="#7b1c0c" stroke-width="2"/>' +
    '<path d="M168 250q20-14 52-8 24 6 28 28-10 22-50 22-30 0-34-18z" fill="#f2ead2" opacity=".92"/><rect x="176" y="248" width="46" height="9" rx="2" fill="#1d1d1d"/><rect x="172" y="276" width="46" height="9" rx="2" fill="#1d1d1d"/><rect x="170" y="312" width="52" height="8" rx="2" fill="#cfcabb"/>' +
    '<circle cx="238" cy="296" r="7" fill="#cfcabb"/><circle cx="252" cy="280" r="7" fill="#cfcabb"/><g stroke="#e8e2d2" stroke-width="1"><path d="M195 52V316M198 52V316M200 52V316M202 52V316M205 52V316"/></g></g>';

  P.violin = '<defs><linearGradient id="vg" x1="0" x2="1"><stop offset="0" stop-color="#7a2f14"/><stop offset=".45" stop-color="#b4521f"/><stop offset="1" stop-color="#6a2610"/></linearGradient></defs>' +
    '<line x1="80" y1="340" x2="330" y2="70" stroke="#5b3418" stroke-width="5" stroke-linecap="round"/><line x1="86" y1="344" x2="336" y2="74" stroke="#efe8d6" stroke-width="2"/>' +
    '<g transform="rotate(14 200 230)"><rect x="193" y="58" width="14" height="110" rx="3" fill="#24150c"/><circle cx="200" cy="46" r="13" fill="url(#vg)"/><rect x="186" y="26" width="28" height="14" rx="5" fill="#24150c"/>' +
    '<path d="M200 148c-36-2-50 22-42 46 6 16-8 24-8 50 0 44 22 80 50 80s50-36 50-80c0-26-14-34-8-50 8-24-6-48-42-46z" fill="url(#vg)" stroke="#4a1a0a" stroke-width="2"/>' +
    '<path d="M180 232q-8 14 0 30M220 232q8 14 0 30" stroke="#1d0f08" stroke-width="4" fill="none" stroke-linecap="round"/><rect x="184" y="270" width="32" height="4" rx="2" fill="#f1e3c0"/><path d="M190 296h20l-3 34h-14z" fill="#24150c"/>' +
    '<g stroke="#e9e1cb" stroke-width="1"><path d="M196 62V296M200 62V296M204 62V296"/></g></g>';

  P.sax = '<path d="M262 48c-6-14 12-24 20-10l2 14" fill="none" stroke="#b78512" stroke-width="10" stroke-linecap="round"/>' +
    '<path d="M268 58L238 100 224 150V270c0 46-24 70-62 64-30-5-48-26-56-56" fill="none" stroke="#c99618" stroke-width="40" stroke-linecap="round" stroke-linejoin="round"/>' +
    '<path d="M268 58L238 100 224 150V270c0 46-24 70-62 64-30-5-48-26-56-56" fill="none" stroke="#f3cf5a" stroke-width="30" stroke-linecap="round" stroke-linejoin="round"/>' +
    '<path d="M268 58L238 100 224 150V270" fill="none" stroke="rgba(255,255,255,.55)" stroke-width="7" stroke-linecap="round" transform="translate(-6 0)"/>' +
    '<path d="M84 262c-14 16-18 34 2 40l40-8c14-10 2-30-14-44z" fill="#d9a521" stroke="#a97a10" stroke-width="3"/><ellipse cx="102" cy="292" rx="24" ry="9" fill="#6d4a0a" transform="rotate(-25 102 292)"/>' +
    '<g fill="#a97a10" stroke="#8a620a"><circle cx="224" cy="170" r="8"/><circle cx="224" cy="200" r="8"/><circle cx="224" cy="230" r="8"/><circle cx="224" cy="258" r="8"/></g>';

  P.drums = '<defs><linearGradient id="dg" x1="0" x2="1"><stop offset="0" stop-color="#d9a20d"/><stop offset=".5" stop-color="#fbd04a"/><stop offset="1" stop-color="#c58f08"/></linearGradient></defs>' +
    '<g stroke="#8d8a82" stroke-width="4"><path d="M78 120v220M322 100v240"/></g><ellipse cx="78" cy="118" rx="62" ry="10" fill="#d9a521" stroke="#a97a10" stroke-width="2"/><ellipse cx="322" cy="98" rx="66" ry="11" fill="#d9a521" stroke="#a97a10" stroke-width="2"/>' +
    '<g><rect x="108" y="176" width="86" height="58" rx="4" fill="url(#dg)"/><ellipse cx="151" cy="176" rx="43" ry="11" fill="#f7f4ea" stroke="#9a968a" stroke-width="3"/></g>' +
    '<g><rect x="208" y="176" width="86" height="58" rx="4" fill="url(#dg)"/><ellipse cx="251" cy="176" rx="43" ry="11" fill="#f7f4ea" stroke="#9a968a" stroke-width="3"/></g>' +
    '<g><rect x="132" y="236" width="136" height="104" rx="6" fill="url(#dg)" stroke="#a97a10" stroke-width="2"/><ellipse cx="200" cy="236" rx="68" ry="14" fill="#f7f4ea" stroke="#9a968a" stroke-width="3"/><circle cx="200" cy="296" r="26" fill="none" stroke="#fff" stroke-opacity=".7" stroke-width="3"/><g stroke="#8d8a82" stroke-width="3"><path d="M132 262v54M268 262v54"/></g></g>' +
    '<path d="M150 150L96 56M250 150L304 56" stroke="#6b3f22" stroke-width="5" stroke-linecap="round"/>';

  P.piano = '<rect x="30" y="140" width="340" height="180" rx="12" fill="#1f1d1a"/><rect x="30" y="140" width="340" height="38" rx="12" fill="#2d2a26"/>' +
    '<g fill="#fff" stroke="#bdb8aa" stroke-width="1.5"><rect x="42" y="190" width="38" height="120" rx="3"/><rect x="80" y="190" width="38" height="120" rx="3"/><rect x="118" y="190" width="38" height="120" rx="3"/><rect x="156" y="190" width="38" height="120" rx="3"/><rect x="194" y="190" width="38" height="120" rx="3"/><rect x="232" y="190" width="38" height="120" rx="3"/><rect x="270" y="190" width="38" height="120" rx="3"/><rect x="308" y="190" width="38" height="120" rx="3"/></g>' +
    '<g fill="#17150f"><rect x="68" y="190" width="24" height="72" rx="3"/><rect x="106" y="190" width="24" height="72" rx="3"/><rect x="182" y="190" width="24" height="72" rx="3"/><rect x="220" y="190" width="24" height="72" rx="3"/><rect x="258" y="190" width="24" height="72" rx="3"/></g>' +
    '<g fill="#f9bc09"><circle cx="64" cy="159" r="6"/><circle cx="90" cy="159" r="6"/></g><g fill="#6d6a60"><rect x="250" y="153" width="90" height="12" rx="3"/></g>';

  P.headphones = '<path d="M92 230V190C92 118 140 70 200 70s108 48 108 120v40" fill="none" stroke="#2a2723" stroke-width="16" stroke-linecap="round"/>' +
    '<path d="M104 200C108 134 148 90 200 90" fill="none" stroke="rgba(255,255,255,.35)" stroke-width="3" stroke-linecap="round"/>' +
    '<rect x="66" y="200" width="56" height="106" rx="26" fill="#b34a1c"/><rect x="278" y="200" width="56" height="106" rx="26" fill="#b34a1c"/>' +
    '<rect x="90" y="214" width="34" height="78" rx="16" fill="#2a2723"/><rect x="276" y="214" width="34" height="78" rx="16" fill="#2a2723"/>' +
    '<path d="M76 222q4-12 14-14M290 208q10 2 14 14" stroke="rgba(255,255,255,.4)" stroke-width="3" fill="none" stroke-linecap="round"/>';

  P.speaker = '<rect x="96" y="40" width="208" height="310" rx="16" fill="#2a2723"/><rect x="104" y="48" width="192" height="294" rx="12" fill="#37332e"/>' +
    '<circle cx="200" cy="120" r="34" fill="#1b1916" stroke="#8d8a82" stroke-width="3"/><circle cx="200" cy="120" r="14" fill="#4b463f"/><circle cx="200" cy="248" r="64" fill="#1b1916" stroke="#8d8a82" stroke-width="4"/><circle cx="200" cy="248" r="40" fill="#2f2b26" stroke="#4b463f" stroke-width="3"/><circle cx="200" cy="248" r="14" fill="#f9bc09"/>';

  P.mic = '<defs><linearGradient id="mg" x1="0" x2="1"><stop offset="0" stop-color="#8d8a82"/><stop offset=".5" stop-color="#e7e4da"/><stop offset="1" stop-color="#7a776f"/></linearGradient></defs>' +
    '<rect x="150" y="40" width="100" height="130" rx="50" fill="url(#mg)" stroke="#6d6a60" stroke-width="3"/><g stroke="#6d6a60" stroke-width="1.5" opacity=".7"><path d="M150 80h100M150 105h100M150 130h100M175 42v126M200 40v130M225 42v126"/></g>' +
    '<rect x="172" y="168" width="56" height="28" rx="6" fill="#2a2723"/><path d="M186 196L172 340M214 196l14 144" stroke="#2a2723" stroke-width="16" stroke-linecap="round"/><path d="M172 340h56" stroke="#2a2723" stroke-width="10" stroke-linecap="round"/><rect x="190" y="206" width="20" height="40" rx="6" fill="#f9bc09"/>';

  P.vinyl = '<rect x="60" y="48" width="240" height="240" rx="6" fill="#f9bc09"/><rect x="60" y="48" width="240" height="240" rx="6" fill="none" stroke="rgba(0,0,0,.12)" stroke-width="2"/><text x="78" y="84" font-family="Bricolage Grotesque,Arial,sans-serif" font-weight="800" font-size="20" fill="#1f1b0f">MELODYHUB</text>' +
    '<circle cx="230" cy="212" r="130" fill="#17150f"/><g fill="none" stroke="#2c2924" stroke-width="2"><circle cx="230" cy="212" r="116"/><circle cx="230" cy="212" r="100"/><circle cx="230" cy="212" r="84"/><circle cx="230" cy="212" r="68"/></g><path d="M230 212L330 120" stroke="rgba(255,255,255,.05)" stroke-width="40"/>' +
    '<circle cx="230" cy="212" r="38" fill="#f9bc09"/><circle cx="230" cy="212" r="6" fill="#17150f"/>';

  P.cd = '<defs><linearGradient id="cg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#e9eef3"/><stop offset=".3" stop-color="#f6e7b0"/><stop offset=".55" stop-color="#cfe3ec"/><stop offset=".8" stop-color="#f2d9e6"/><stop offset="1" stop-color="#e2e6ea"/></linearGradient></defs>' +
    '<circle cx="200" cy="196" r="150" fill="url(#cg)" stroke="#b9b6ac" stroke-width="3"/><circle cx="200" cy="196" r="140" fill="none" stroke="rgba(0,0,0,.08)" stroke-width="2"/><circle cx="200" cy="196" r="52" fill="#f7f6f1" stroke="#c9c5b8" stroke-width="3"/><circle cx="200" cy="196" r="18" fill="#fff" stroke="#b9b6ac" stroke-width="3"/>' +
    '<path d="M110 120q40-60 120-56" stroke="rgba(255,255,255,.8)" stroke-width="10" fill="none" stroke-linecap="round"/>';

  P.artist = '<rect width="400" height="400" fill="url(#ab)"/><circle cx="200" cy="160" r="64" fill="#e9eaee" opacity=".92"/><path d="M62 400c0-96 62-146 138-146s138 50 138 146z" fill="#e9eaee" opacity=".92"/><g fill="#f7b500" opacity=".95"><circle cx="318" cy="118" r="13"/><rect x="328" y="52" width="5" height="68"/><path d="M333 52q26 8 26 34-14-14-26-16z"/></g><g fill="#f7b500" opacity=".55"><circle cx="76" cy="96" r="9"/><rect x="83" y="50" width="4" height="48"/></g>';

  var BG = '<defs><linearGradient id="bg" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#f4f5f7"/><stop offset="1" stop-color="#e6e8ec"/></linearGradient><radialGradient id="sh"><stop offset="0" stop-color="rgba(0,0,0,.22)"/><stop offset="1" stop-color="rgba(0,0,0,0)"/></radialGradient>' +
    '<linearGradient id="ab" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#2a2c34"/><stop offset="1" stop-color="#15161a"/></linearGradient></defs>';

  var RULES = [
    [/electric|stratus|fender|telecaster|les paul|bass guitar/i, 'electric'],
    [/guitar|ukulele|acoustic|string instrument|banjo|mandolin/i, 'guitar'],
    [/violin|cello|viola|orchestr|bow/i, 'violin'],
    [/sax|flute|trumpet|clarinet|brass|wind|horn/i, 'sax'],
    [/drum|percussion|snare|cajon|cymbal|tom/i, 'drums'],
    [/piano|keyboard|synth|keys|organ|midi/i, 'piano'],
    [/speaker|amplifier|\bamp\b|monitor|subwoofer/i, 'speaker'],
    [/headphone|earphone|earbud|headset|audio|studio/i, 'headphones'],
    [/mic|microphone|karaoke|vocal/i, 'mic'],
    [/\bcd\b|compact disc|album|digital|mp3|disc/i, 'cd'],
    [/vinyl|record|\blp\b|turntable|gramophone/i, 'vinyl']
  ];
  var SCALE = { guitar: 1.0, electric: 1.0, violin: 1.0, sax: 1.04, piano: .98, drums: 1.04, cd: 1.0, vinyl: 1.04, speaker: 1.0, mic: 1.0 };
  var ROTATE = ['guitar', 'vinyl', 'headphones', 'piano', 'violin', 'cd', 'drums', 'sax'];

  function pick(text) {
    text = String(text || '');
    for (var i = 0; i < RULES.length; i++) if (RULES[i][0].test(text)) return RULES[i][1];
    var h = 0; for (var j = 0; j < text.length; j++) h = (h * 31 + text.charCodeAt(j)) >>> 0;
    return ROTATE[h % ROTATE.length]; // unknown product: stable but varied
  }
  // full tile (soft studio background + floor shadow)
  function svg(kind) {
    var body = P[kind] || P.vinyl;
    var inner = kind === 'artist' ? P.artist : '<ellipse cx="200" cy="356" rx="120" ry="10" fill="url(#sh)"/><g transform="translate(200 195) scale(' + (SCALE[kind] || 1.12) + ') translate(-200 -195)">' + body + '</g>';
    return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 400">' + BG + (kind === 'artist' ? '' : '<rect width="400" height="400" fill="url(#bg)"/>') + inner + '</svg>';
  }
  function uri(kind) { return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg(kind)); }
  // transparent instrument for composing scenes
  function raw(kind) { return P[kind]; }

  var art = { pick: pick, svg: svg, uri: uri, raw: raw, kinds: Object.keys(P) };
  if (typeof module !== 'undefined' && module.exports) module.exports = art;
  else if (root.MH) root.MH.art = art;
})(typeof window !== 'undefined' ? window : this);
