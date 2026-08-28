# Changelog

## Unreleased

## 0.4.1 — 2026-08-28

- **Fix: retained ad views no longer report duplicate renders or impressions
  after SDK reconfiguration.** Per-opportunity tracking latches now last for
  the process lifetime, including across `Elo.configure()` and
  `Elo.shutdown()`, so remounting an old view cannot bill the same opportunity
  again.

- **Fix: ads hidden by app or native-host state no longer count as
  impressions.** The one-second dwell now requires a resumed app, a focused
  host window, and an attached, visible, non-transparent native host hierarchy.
  Backgrounding, opening another window, hiding or detaching the host, or
  making its native hierarchy effectively transparent cancels an armed dwell;
  returning to a visible state starts a fresh one. The tracker also re-checks
  these signals when the dwell finishes rather than trusting state measured a
  second earlier. Scaled or otherwise transformed Compose hosts now measure the
  visible fraction entirely in window space instead of comparing transformed
  pixels with an untransformed layout size.

- **Change: the strip's creative mark is bigger and is a ringed circle, and
  the disclosure badge is restyled.** Brought to parity with the iOS SDK, which made these
  changes first.

  The strip's mark grew without either surface changing height. The strip's
  row is pinned to the CTA button's interactive minimum, so its mark grew into
  height the row already paid for (34dp → 40dp). The card's mark is unchanged
  at 56dp: the card's row is pinned to the mark itself, so a larger one would
  have had to come out of the card's own padding, and at that size it crowded
  the text column rather than reading as an accompanying mark. The card stays
  80dp tall. The strip's mark now carries a
  hairline grey ring, so a white or near-white logo has an edge of its own
  instead of dissolving into the surface at that size; the card's mark is
  large enough to stand without one. The card's mark stays a rounded square
  and the strip's stays a circle: creative artwork is a brand
  mark far more often than photography, and marks are routinely wide
  wordmarks, which a circular crop takes the ends off.

  The disclosure badge is a near-white disc with a hairline black ring and
  black copy at medium weight, squared up from a capsule since the default copy
  is two characters; longer copy relaxes it back rather than being squeezed.
  Those are the only fixed colors in the SDK — the badge sits on creative
  artwork rather than on a surface the theme controls, so a fill that followed
  light/dark would read against one creative and vanish into the next.
  `EloAdDisclosure.color` and `EloAdStyle.badgeColor` still override it, which
  takes the ring off and inverts the copy as before. The badge also moved from
  the mark's top-start corner to its top-end corner, and hangs off that corner
  rather than sitting flush inside it. Copy, the `sponsoredLabel` API, and the
  guarantee that the disclosure is always drawn and never truncated are
  unchanged.

- **New: `Elo.setUserIdentity(userIdentifier, userData)` writes both halves of
  the identity at once.** The identifier and the user data are persisted as one
  Keystore-encrypted record, so setting them with the two existing setters in
  sequence left an intermediate state on disk: an account switch stored
  `(B, A's contact details)` until the second call landed, and an ad request or
  a process kill in that window sent or retained one account's PII under the
  other's identifier. Prefer the new call from your auth-state hook whenever
  both are changing. `setUserIdentifier` and `setUserData` are unchanged and
  still correct when only one is.

- **The ad disclosure moved off the headline into a corner badge.** It used to
  lead the headline as `Ad • Headline`, which cost the headline the width of
  the disclosure plus its separator on every card. It is now a small capsule
  overlaid in the surface's top-end corner, so the headline gets the full width
  of the text column. It rides the corner of the creative's own image, on both
  the compact card and the inline banner; card height is unchanged. The
  disclosure stays untruncatable — it is outside the text flow entirely now,
  rather than merely at the head of it. A creative whose image fails to load
  has nothing to mark, so there the badge falls back to the surface's top-end
  corner and the row reserves its measured width on that edge, keeping a
  call-to-action button clear of it.

  If a Compose UI test asserted the combined `"Ad • Headline"` node, split it:
  the disclosure and the headline are separate nodes now, so assert each
  directly instead of a `startsWith` on the merged text.

- **The disclosure badge is no longer twice as tall as its own type.** It
  inherited the ambient body style's 24sp line height, so a 9sp capsule stood
  around 22dp tall and covered the artwork it is badged onto instead of
  sitting in its corner. Its line box is trimmed to the glyphs now. The badge
  is also drawn a step smaller on the keyboard strip than on the card, whose
  tile is larger. `EloAdDisclosure.fontSize` still overrides both.

- **Fix: the ad slot no longer changes height when the request lands.** The
  strip's row was sized by whatever filled its trailing slot — a call-to-action
  button carries a 48dp interactive minimum, a chevron is a fraction of that —
  so a CTA fill grew the slot on arrival and the loading placeholder matched
  only the chevron case. Both layouts and the placeholder now reserve the same
  row height (48dp on the strip, 56dp on the card), which also fixes the card's
  mirror image: a creative whose image fails to load drops its tile and used to
  *shrink* the card below the placeholder.

- **Fix: creative artwork is fitted explicitly, at high filter quality.** The
  strip's brand mark also grew from 30dp to 34dp — no height change, since the
  row was already taller than it.

- **Breaking: `sponsoredLabel` now takes an `EloAdDisclosure`, not a `String`.**
  The new value carries the disclosure copy plus an optional `fontSize`,
  `fontWeight`, `color` and `testTag`, so you can restyle the disclosure and
  give Compose UI tests a stable handle on it. Every call site wraps:
  `sponsoredLabel = EloAdDisclosure(stringResource(R.string.elo_sponsored))`.
  Applies to every `EloAdView` overload, `EloInlineBannerAdView` and
  `EloKeyboardBannerAd`.

  It is a value rather than a composable slot on purpose. `EloAdView` draws the
  badge itself, overlaid on the surface, which is what makes the disclosure
  untruncatable: nothing in the layout can squeeze or clip it. A
  publisher-supplied composable would have to be laid out beside the headline,
  where a long headline can do both. `color` fills the capsule (the copy is
  drawn in the card color over it), and `testTag` names the badge, which is its
  own node now that it no longer shares the headline's text.

  `AdRenderOptions.sponsoredLabel` is unchanged and still a `String`: adapters
  build their own native views, so there is nothing there for type or a tag to
  bind to. No adapter needs a change, and mediated fills keep the look they
  have today — they take your disclosure *copy* and render it in the adapter's
  own treatment, colored from `EloAdStyle.badgeColor` like the rest of that
  card.

- **Breaking: `callToActionLabel` is gone from `EloAdView` and
  `EloKeyboardBannerAd`.** Call-to-action copy is backend-managed now: it
  arrives on the creative as `EloAd.ctaText` and labels the pill both layouts
  draw. The publisher-supplied parameter only ever reached renderer-backed
  fills and no bundled adapter read it, so nothing that shipped ever rendered
  it. Drop the argument from your call sites.
  `AdRenderOptions.callToActionLabel` and its deprecated `ctaLabel` alias are
  removed with it.

- **Breaking: `openLinkAccessibilityLabel` and the TalkBack click label it
  carried are both gone.** The parameter is removed from `EloAdView` and
  `EloKeyboardBannerAd`, and the clickable surfaces no longer pass an
  `onClickLabel`.

  This is a deliberate accessibility reduction, so it is worth stating plainly:
  TalkBack used to announce "Double tap to Open sponsored link", telling the
  listener that activating the ad leaves the app for an advertiser's
  destination. It now falls back to the generic "Double tap to activate".
  Sighted users still get that from the "Ad" disclosure, the button styling and
  the chevron. Nothing else about the ad's accessibility changes — the
  disclosure, the content descriptions and the button role are all untouched.

- **New: creatives can carry their own call-to-action button.** Some demand
  sources send a label such as "Learn more" with the creative. Where one
  arrives, both layouts draw it as a pill in the trailing slot in place of the
  disclosure chevron. `EloAd.ctaText` exposes the label, and
  `EloAdStyle.callToActionBackground` / `callToActionForeground` color the
  pill.

- **Change: the two layouts take deliberately different click rules.** The
  in-chat card is one click target from edge to edge, whether or not the
  creative sends a label — in a transcript the card reads as a single object,
  and there is no neighbouring control for a stray tap to hit, so its pill is
  decoration rather than a second button. The keyboard banner does the
  opposite: when the creative sends a label, only that button clicks, because
  the strip sits directly under the reader's thumb beside the composer and an
  edge-to-edge target there invites accidental clicks. A strip without a label
  keeps its whole row clickable behind the chevron. Either way the click opens
  the same destination, and render and impression tracking stay on the
  container, so viewability is unaffected.

- **Change: the keyboard banner shows its loading skeleton by default.**
  `showLoadingPlaceholder` on `EloKeyboardBannerAd` now defaults to `true`,
  matching `EloAdView`. The skeleton stands in at the strip's own
  size, so the composer above keeps its place when an ad arrives, and the slot
  reads as loading rather than as empty. Pass `false` to keep the keyboard
  edge completely clear until an ad fills.

- **Fix: a slow creative image no longer holds the whole ad in its skeleton.**
  The thumbnail now carries its own shimmer and swaps in when the image
  finishes, so the headline and description render as soon as the ad does.

- **Compatibility: adapters survive the `EloAd` change, but not the
  `AdRenderOptions` one.** `EloAd` gained `ctaText` in its primary
  constructor, which moves the JVM descriptors of that constructor and of
  `copy`; the previous descriptors are retained as hidden shims, so existing
  adapter bytecode still links across that change. Removing
  `AdRenderOptions.callToActionLabel` carries no such shim — an adapter that
  read it stops compiling, and already-built bytecode calling
  `getCallToActionLabel()` fails at runtime. Adapters take the SDK as
  `compileOnly`, so the app chooses the version and an old adapter meets the
  new class: recompile any adapter that touched that field. Adapters that
  ignored it, including both bundled ones, need nothing.

- **Change: the keyboard banner's strip was retuned to fit real creatives.**
  A circular brand mark, tighter gutters, and a smaller type scale on the
  attribution line — at the previous size an ordinary advertiser name
  ellipsized before the strip had drawn anything else.

- **New: `Elo.setUserData` shares first-party user data on ad requests.** Apps
  that know their signed-in user can pass age, gender, email, and phone number.
  The fields are recorded server-side for upcoming targeting features and have
  no effect on ad selection yet. Pass contact details as they are: they travel
  over HTTPS and are SHA-256 hashed at the ad server before anything is stored,
  so Elo never persists a plain email address or phone number, and you don't
  have to hash them yourself. Give a phone number its country code (E.164),
  since without one it is ambiguous and the server discards it. The SDK does
  not validate what you set — values are forwarded as supplied and the server
  normalizes each one and drops whatever it can't use, so there is one set of
  rules rather than two that can disagree. Like `setUserIdentifier`, the data
  is persisted on the device and encrypted at rest, so you set it once rather
  than on every launch; `shutdown()` erases the stored copy while a
  re-configure does not. Because it outlives a sign-out, clearing it on
  sign-out is an obligation — set both this and the identifier from one
  auth-state hook, which is also the place that keeps a stored copy from going
  stale when the user changes their email or phone. Each call
  replaces the whole object. The data never joins tracking pings, and the ad
  server discards it entirely on requests flagged COPPA or TFUA, and on any
  request where GDPR applies. Sharing contact details means your app transmits an email
  address and phone number to Elo — account for that in your privacy policy and
  Play Data safety form; see `PRIVACY.md`.

- **Change: publisher-supplied identity now survives an app restart.** The
  identifier from `Elo.setUserIdentifier` and the data from `Elo.setUserData`
  used to live in memory only, so an app that keeps people signed in between
  launches reported as anonymous on every relaunch until the user happened to
  sign in again. Both are now stored on the device as one encrypted record —
  sealed with AES-GCM under a key held in the Android
  Keystore, which is non-exportable and never travels in a backup — and are read back at
  launch, so you set identity once rather than on every start. The trade is
  that identity now outlives a sign-out you don't signal: call
  `Elo.setUserIdentifier(null)` and `Elo.setUserData(null)` when the user signs
  out, or the next person on that device inherits both. Upgrading needs no
  migration — there was never a stored record to read, so the first launch on
  this version starts anonymous exactly as before, and an integration that
  already sets identity each launch keeps working unchanged. What does change
  for it: app termination used to clear identity on its behalf, so an
  integration that never signalled sign-out was covered by the process boundary
  and no longer is. `shutdown()` erases the
  stored record. Nothing is ever written in plaintext — if the platform store
  is unavailable the SDK keeps identity in memory for that process instead of
  falling back to an unprotected file. Storing contact details on the device is
  worth a line in your privacy policy and Play Data safety form; see `PRIVACY.md`.


## 0.3.0 — 2026-08-14

- **Fix: calling `Elo.trackRender` or `Elo.trackImpression` yourself no longer
  double-counts.** Ads shown through `EloAdView` have always recorded exactly
  one render and one impression per ad opportunity, however often the
  composable recomposes or re-enters the viewport — but that limit lived in the
  view layer, so a fully custom layout calling the tracking hooks directly
  could report the same opportunity more than once. It now sits behind the
  public API and covers every path into it, so those hooks are safe to call on
  every recomposition. A repeat load that returns the same creative is a
  distinct opportunity and still records. A suppressed duplicate is silent: no
  ping, and no `EloAdListener.onAdDidTrackImpression` callback. Matches the iOS
  change of the same behavior.
- **Internal: render tracking moved into the impression-tracking modifier.**
  `EloAdView` and the adapter-rendered path each fired their own render ping
  next to the impression modifier they already applied; both now come from the
  modifier, so an ad surface cannot attach one and forget the other. No change
  to when a render fires or how it is deduped.
- **Fix: `EloAdListener.onAdDidTrackImpression` now fires only when the
  impression ping actually lands.** It is documented as firing after the SDK
  fires the ping, but it was dispatched alongside the attempt instead — so it
  reported impressions the ad server rejected or never received, and publishers
  counting impressions from it over-counted. Two things caused that, both
  fixed: the callback did not wait for the ping, and the ping itself reported
  success unconditionally. Render and impression pings now surface non-2xx
  responses and transport failures, which also means
  `EloDiagnosticsSnapshot.trackingTotals` can finally report a non-zero
  `failed` count and the Diagnostics funnel shows real impression failures.
  Matches iOS, which already gated its delegate callback on the delivery
  outcome. `onAdDidReceiveClick` is unchanged — it is documented as firing on
  the tap, before the click URL opens.
- **Fix: a render or impression is no longer consumed when the SDK cannot send
  it.** Each ad opportunity records at most one render and one impression for
  the process lifetime. Calling the tracking hooks before `configure` (or after
  `shutdown`) marked the opportunity as counted even though nothing was sent,
  so the real ping could never follow. The SDK now checks that it can deliver
  before spending that budget.
- **New: `EloDiagnosticsSnapshot.trackingTotals` counts every render,
  impression, and click attempt since configure.** The existing
  `trackingEntries` list is a bounded ring buffer, so it could not tell you how
  many impression pings failed once a session got past the newest twenty — and
  a ping the SDK gives up on is a lost impression it will never retry. Each
  `EloTrackingTotals` carries `attempted`, `delivered`, `failed`,
  `unobservable`, and `inFlight`, is never evicted, and still records an
  outcome whose entry had already aged out. The counts also appear in
  `asExportableText()`. Cleared on configure and `shutdown()`, like the rest of
  diagnostics. Matches the iOS change of the same behavior.
- **Fix: an ad card no longer stays without its image when the same creative
  comes back in a later ad.** The card drops its thumbnail when a creative's
  image can't be loaded, so a broken URL leaves text rather than a blank
  square. That was remembered against the creative rather than the ad, so
  while the card stayed in composition, one failure also suppressed the image
  on every later ad that served the same creative. It now applies only to the
  ad it was recorded for. Matches the iOS fix of the same behavior.

- **Breaking: `EloAd.id` is now the ad opportunity, and the creative moved to a
  new `EloAd.creativeId`.** `id` previously carried the ad server's `ad_id` —
  the *creative* — which is stable across opportunities, so the same creative
  served twice produced two ads that looked identical to the SDK. The ad
  opportunity, which is what render, impression, and click URLs are keyed under
  server-side, was tucked away in an internal field. They have swapped places:
  `id` is the opportunity (one per showing) and `creativeId` names the artwork.
  Correlate delivery on `id`; group by `creativeId`.

  If you log or store `ad.id`, it now changes on every serve of the same
  creative. Switch to `ad.creativeId` wherever you meant the creative.

  **Adapter authors:** the `EloAd` constructor now takes both `id` and
  `creativeId`, and `id` must be unique per *fill*. Pass your network's
  per-response id if it has one, or mint a `UUID.randomUUID().toString()`.
  Passing a creative id there collapses every serve of that creative into a
  single tracked ad, so only the first reports a render and an impression. The
  AdMob adapter now mints one per fill, which fixes exactly that
  under-reporting on AdMob native fills.


## 0.2.0 — 2026-08-09

- **New: `Elo.setUserIdentifier` ties ad requests to your own user account.**
  Every request carries an anonymous, per-install `visitor_id` the SDK
  generates. Apps with a sign-in can now supply their own identifier instead,
  and it replaces that anonymous id on subsequent ad requests and their
  tracking pings — so delivery, frequency capping, and reporting follow the
  user across installs and devices rather than the install. Call it once the
  user is known and clear it on sign-out; the order relative to `Elo.configure`
  does not matter. The anonymous id is kept underneath, so clearing restores
  the same one the install had before. An ad keeps whichever identity it was
  requested under for its whole lifetime, so an impression that fires after a
  sign-out is still reported against the request that fetched it. The
  identifier lives in memory only (re-set it on each launch) and is cleared by
  `shutdown()`; a re-configure does not clear it, since a config refresh is not
  a sign-out. Whitespace is trimmed, a blank string clears it, and values over
  256 characters are ignored with a warning.

- **Fix: a scrolling ad description now scrolls a few times and then settles,
  instead of scrolling for as long as the ad is on screen.** A permanently
  animating line keeps the host app's UI from ever going idle, which stalls
  automation frameworks that wait for idle before each interaction and keeps a
  frame in flight for no reason. The description now makes three passes — the
  same budget as iOS — then stays tail-ellipsized like any other line. Scroll
  speed, the holds at each end, travel distance and row height are unchanged,
  and a new creative gets its own passes.

- **Fix: the loading placeholder's shimmer now sweeps a few times and then
  rests, and stays still for anyone who has turned animations off.** It
  previously repeated for as long as the placeholder was on screen, with the
  same never-goes-idle consequence, and a request that hangs holds the
  placeholder there indefinitely. It now shimmers well past the point a normal
  ad request returns, then rests as a plain skeleton.

- **Fix: creatives whose image is a `.ico` now show their thumbnail.**
  Publishers commonly supply a site favicon as a creative image, and favicons
  are often ICO — a format Android cannot decode, unlike iOS. The image load
  failed and the ad rendered with no thumbnail at all, so the same creative
  looked different on the two platforms. The SDK's image loader now decodes
  ICO itself (both the PNG-embedded and the uncompressed-DIB forms, picking
  the largest image in the file), closing the parity gap.

- **A creative image that fails to load now logs a warning.** The thumbnail
  drops out silently by design, which made an unreachable or undecodable image
  host indistinguishable from a creative that simply has no image. The failing
  URL and cause are now logged at warn level under the `Elo` tag.

- **Fix: a creative that wins more than once now records a render and an
  impression every time it's shown.** Render and impression dedup was keyed on
  the creative id, which is stable across ad requests — so the second and every
  later time the same creative won an auction in a single app session, the SDK
  suppressed both pings while the ad still displayed and still clicked.
  Those ad opportunities reached the server as a click with no render and no
  impression behind it. Dedup is now keyed on the ad opportunity, so repeated
  showings of one creative each report their own render and impression, while
  the guarantee that matters is unchanged: recomposition, scrolling an ad out
  of a lazy list and back, or a configuration change still reports exactly one
  render and one impression per opportunity.

  **Expect reported renders and impressions to rise** once this ships — the
  missing events were never counted. Click volume is unaffected. Publishers
  whose reporting showed clicks exceeding impressions for a placement should
  see that resolve.

  One consequence worth knowing: `Elo.shutdown()` now clears render dedup state
  as well as impression state, so an ad shown before shutdown can report again
  after a re-`configure`.


## 0.1.9 — 2026-07-29

- **An ad image that fails to load now hides the thumbnail instead of leaving
  a blank square.** The compact card and the inline banner used to keep an
  empty tile in the row when Coil couldn't fetch the creative's image; the tile
  and the 12dp gap after it now drop out and the text takes the space.
  Creatives that carry no image URL at all still get the monogram tile.

- **The ad disclosure now leads the attribution line: `Ad • Headline`.** It
  used to trail the headline (`Headline · Sponsored`), where a long headline
  pushed it into the ellipsis and it disappeared. Leading it makes truncation
  structurally unable to reach it. The separator is now a `•` (U+2022) rather
  than a `·` (U+00B7), and `sponsoredLabel`'s default changed from
  `"Sponsored"` to `"Ad"` — shorter, so it costs the headline less width. The
  parameter name is unchanged, so no call site breaks; keep passing a localized
  string for non-English surfaces. TalkBack now announces the disclosure first.

- **Creative descriptions are now a single line and scroll when they don't
  fit.** Copy wider than the surface moves right-to-left at 30dp/s until the end
  of the line is visible, holds there for 1.2s, then restarts from the beginning
  after another 1.2s pause — so the whole line is readable and two fragments of
  it are never on screen together. The full text stays in semantics, so TalkBack
  reads all of it. Motion stops while the ad is off screen and falls back to a
  static ellipsis when the device has animations disabled. Opt out with the new
  `EloAdStyle.descriptionOverflow = EloAdDescriptionOverflow.Truncate`.

  Note for layouts that reserve space: the compact card's description used to
  wrap to two lines, so its text column is shorter now — though the 56dp icon
  still sets the row's floor, so the card only loses a few dp at default font
  scale.

  Note for instrumentation tests: the marquee is an indefinite animation, so
  `waitForIdle()` won't settle while an overflowing ad is on screen. Use
  `descriptionOverflow = Truncate`, or set `animator_duration_scale 0`.

- **AdMob-rendered fills need the matching adapter release** for the reorder.
  An older adapter on this SDK renders `Headline · Ad` — internally consistent,
  but not the new order.


## 0.1.8 — 2026-07-22

First release on the new `ad.elo:elo-ads-android` coordinate — the version
line is reset to match the iOS SDK (0.1.6/0.1.7 were prepared but never
published), and the pre-rename `ad.elo:elo-android-sdk` 2.x line is frozen.
Everything below lands relative to 2.6.0:

- **New diagnostics surface for iOS parity:** public
  `Elo.diagnosticsSnapshot()` (mirrors iOS `Elo.Debug.snapshot()`) exposing
  integration status, `Elo.sdkVersion`, adapter count, the last 10
  load/preload operations with per-operation latency/outcome and a redacted
  copy of the Elo request payload (message content and context descriptions
  dropped; counts, roles, types, identity, consent, and `device` incl.
  `device.geo` retained), plus the last 20 render/impression/click tracking
  outcomes. In-memory only; cleared on reconfigure/shutdown. The request
  payload is captured only when the host app opts in via
  `Elo.setRequestPayloadCaptureEnabled(true)` (off by default), since it
  carries the advertising id, geolocation, and consent strings. Each ad
  operation also carries the server ad-opportunity id (ad response
  `request_id`) as `EloDiagnosticsEntry.serverRequestId` — recorded on fill
  and no-fill alike, since the server assigns it for every request it
  processes — so a request can be correlated to its server-side
  impression/click events (mirrors iOS `DiagnosticsEntry.serverRequestId`).
- **Impression/click diagnostics now carry the correlating opportunity id.**
  Each `EloTrackingDiagnosticsEntry` records the served creative's server
  ad-opportunity id (ad response `request_id`) as
  `EloTrackingDiagnosticsEntry.serverRequestId`. Because impression/click
  tracking URLs are keyed under this id server-side, reading it off the
  confirmed-impression entry correlates the exact request to its funnel row —
  instead of guessing which `loadAd` operation produced the displayed creative
  (transcript-driven reloads create several). Mirrors iOS
  `TrackingDiagnosticsEntry.serverRequestId`. No public ad-loading API changes.
- **Unified ad card design (iOS parity).** The default `CompactHorizontal`
  card now matches the inline banner's visual language and the iOS card: a
  56dp icon beside a "Title · Sponsored" attribution line and body, on the
  plain surface background with a hairline border, a trailing disclosure
  chevron, and a 14dp corner radius. The separate uppercase "Sponsored"
  label above the row is gone. `EloAdLoadingView`'s skeleton mirrors the new
  single-row layout. No public API changes.

- **Privacy: visitor IDs are always anonymous.** The SDK generates a local
  `anon_<UUID>` and never derives the visitor ID from the Google Advertising
  ID anymore. `PRIVACY.md` is updated to match.
- **Privacy: removed the `Device-Name` and `System-Version` HTTP headers**
  (iOS never sent them and the backend never parsed them). The `User-Agent`
  is now built from `DeviceInfo` with the real SDK version instead of
  `System.getProperty("http.agent")` with a stale hardcoded fallback.
- **Renamed for iOS parity (old names deprecated, not removed):**
  `EloAd.release()` → `EloAd.releaseResources()`, and `EloAdView`'s
  `ctaLabel` parameter → `callToActionLabel` (forwarded to renderers as
  `AdRenderOptions.callToActionLabel`).
- New public `EloAdLayout` enum and CTA color style tokens
  (`EloAdStyle.callToActionBackground` / `callToActionForeground`), matching
  the iOS styling surface; renderer-backed fills receive the resolved colors
  via `AdRenderOptions`.
- Ad requests and same-origin tracking pings now carry the `X-Elo-State`
  session envelope header (base64 JSON: visitor, session, publisher, ad
  unit, timestamp), matching the iOS and web SDKs. It is never attached to
  URLs off the configured API origin.
- The mediation auction now runs one shared deadline spanning adapter
  `start()` + `bid()` (previously a fresh budget per phase), and the default
  auction timeout dropped from 5s to 3s — both matching iOS.
- Render pings are deduped process-wide via the tracking registry, so
  lazy-list re-entry can no longer re-fire `trackRender` for the same ad.
- Fixed a native-resource leak: `EloAdView(messages)` and
  `EloKeyboardBannerAd` now release the owned ad when they leave
  composition, and an in-flight load that outlives disposal is released
  instead of resurrecting a disposed result (previously adapter-backed
  ads such as AdMob `NativeAd` leaked on navigation).
- Preloaded ads now expire after 300 seconds, matching iOS.
- The default API base URL is production in all build types — debug builds
  no longer point at the dev endpoint (the sample app opts into dev
  explicitly). Request retries follow the iOS retry policy and failures
  surface as typed `EloError`s.
- `AdCreative` decoding is lenient: `source`, `offer_type`, and `format`
  are optional and unknown enum values no longer fail the whole response.
- Elo-direct click POST delivery is temporarily disabled. Tapping still opens
  the creative destination and dispatches the local click callback; third-party
  `click_trackers` remain server-owned.
- **New Maven coordinate:** the SDK is now published as
  `ad.elo:elo-ads-android`, with the version line reset to match the iOS SDK
  — 0.1.8 is this coordinate's first release (0.1.6 and 0.1.7 were prepared
  but never published, so Android lands in lockstep with iOS 0.1.8). The
  pre-announcement 2.x line (≤ 2.6.0) shipped as `ad.elo:elo-android-sdk`;
  that coordinate is frozen and will receive no further releases.
- **Behavior change for upgraders:** Prebid-style passive geo sharing is now
  **on by default** (`EloConfiguration.shareGeoLocation = true`), matching iOS
  0.1.6. Apps whose users already granted a location permission start
  attaching a rounded, coarse location to ad requests after upgrading. The SDK
  still never requests location permission itself. Opt out with
  `shareGeoLocation = false` or `Elo.setShareGeoLocation(false)` (the runtime
  toggle resets to the configured value after `shutdown()` + reconfigure).
- Coordinates are now never sent for `coppa`/`tfua` configurations, regardless
  of `shareGeoLocation` — same gating pattern as advertising identifiers.
- Ad requests now carry a settings-derived country (`device.geo.country`,
  ISO-3166 alpha-3 from the region setting) and UTC offset
  (`device.geo.utcoffset`, minutes) — permission-free, from locale/time-zone
  settings, not GPS. Sent unconditionally; the `shareGeoLocation` opt-out
  covers coordinates only.
- New keyboard banner format: `EloKeyboardBannerAd(messages)` pins a two-line
  inline banner strip above the software keyboard with one composable —
  loads with `AdDisplayPosition.Banner`, collapses on no-fill or error, and
  reloads when the messages change. Mirrors iOS 0.1.6's
  `.eloKeyboardBannerAd`. Requires an edge-to-edge window with
  `adjustResize` to pin (degrades to resting at the window bottom otherwise);
  see the composable's documentation for host-side caveats.
- Elo-rendered creatives no longer draw a CTA pill — the whole card has been
  the click target all along, and the pill was decorative. `EloAdView`'s
  `ctaLabel` parameter now only reaches renderer-backed fills, forwarded via
  `AdRenderOptions.ctaLabel` for `ConfigurableAdRenderer` implementations.
  `EloAdLoadingView(showCtaPlaceholder:)` is ignored (kept for source
  compatibility) and the skeleton no longer shows a CTA placeholder.
- New `PRIVACY.md` host-app privacy guide (what leaves the device, Data
  safety form guidance, the `AD_ID` manifest-merge note, children/Families
  policy), linked from a new README Privacy section.
- **`Elo.initialize` is deprecated** and will be removed in a future major
  version. `Elo.configure` is now the single entry point, in two forms:
  - `Elo.configure(context, publisherId, adUnitId, shareGeoLocation,
    geoLocationPrecision)` — new convenience for Elo-only integrations; the
    geo controls keep the on-by-default sharing opt-out visible at the
    simplest entry point.
  - `Elo.configure(context, configuration)` — unchanged; mediation adapters,
    COPPA/TFUA, `logLevel`, and `baseUrl` live on `EloConfiguration`.
  Migrating from `initialize` is a rename for most apps; if you passed
  `coppa`/`tfua`, move them onto `EloConfiguration`.
  `EloError.NotConfigured`'s message now points at `Elo.configure()`.

## 2.6.0 — 2026-06-28

- Collects OpenRTB `device` signals (make, model, hardware, OS version, screen size/density, language, and connection type) and includes them in ad requests for better fill and relevance. The advertising identifier (`ifa`) is consent-gated and honors limit-ad-tracking, with an `ifv` fallback.
- Adds opt-in passive geo sharing via `shareGeoLocation` / `geoLocationPrecision` on `EloConfiguration`. It only reads an already-authorized last-known location and never prompts; coordinates are rounded to the configured precision.
- Updates examples to `ad.elo:elo-android-sdk:2.6.0`.

## 2.5.2 — 2026-05-31

- Adds `EloAdLoadingView` and an `EloAdView(messages = ...)` overload with an optional built-in loading placeholder.
- Makes AdMob-rendered fills inherit view-level labels and style tokens from `EloAdView`, so localization and card styling stay consistent across Elo and AdMob winners.
- Fixes AdMob native-ad ownership across Compose remounts so filled ads keep their content when tabs or screens are re-created.
- Updates examples to `ad.elo:elo-android-sdk:2.5.2` and `ad.elo:elo-android-mediation-admob:0.1.2`.

## 2.5.1 — 2026-05-14

- **Breaking(android):** `EloConfiguration` drops seven fields with no sensible publisher-tuned value: `floorECpm`, `enableAuctionPriceLogging`, `maxMessagesContext`, `character`, `conversationId`, `variantId`, `impressionTrigger`. Drop these from existing `EloConfiguration(...)` / `copy(...)` call sites. The viewability contract is unchanged (50% / 1s); `maxMessagesContext` is now an internal constant (`EloAdsTuning.MAX_MESSAGES_CONTEXT = 30`).
- **Breaking(android):** `Elo.impressionTrigger` getter removed.
- **Breaking(android):** `EloAd.requiresCustomRendering` removed (dead public property).
- **Breaking(android):** `EloAdStyle.ctaLabel` removed; CTA copy is now an `EloAdView` view-level parameter (`ctaLabel: String? = "Learn more"`). `EloAdView` also gains `sponsoredLabel: String = "Sponsored"` and `openLinkAccessibilityLabel: String = "Open sponsored link"` — both were previously hardcoded English strings inside the view. Localize per call: `EloAdView(ad, sponsoredLabel = stringResource(R.string.elo_sponsored))`.
- **Breaking(android):** `NoFillReason.BelowFloor` removed. Without a floor knob the variant was incoherent; auctions that filter to zero usable bids now fall through to `NoBids`.
- **Adapter authors(android):** `AdBidRequest.character` / `.conversationId` / `.variantId` removed (and dropped from the wire `AdRequest`). Adapters that ignored these per `ADAPTER_AUTHOR_GUIDE.md` see no behavior change.
- **Internal(android):** `ParallelAuctionMediator` no longer takes a `floorECpm` parameter; `selectWinner` filters with `eCpm.isFinite() && eCpm >= 0.0`. `MediationDebugEvent.Configured` and `EloAuctionDebugSnapshot` drop their `floorECpm` field.
- chore(android): bump AdMob adapter dep to `ad.elo:elo-android-mediation-admob:0.1.1` (tracking bump — adapter source unchanged, `expectedEcpm` semantics unchanged).
- docs(android): sweep README and quickstart sample for the floorECpm removal and the new `EloAdView` view-level CTA / sponsored / accessibility-label parameters.

## 2.5.0 — 2026-05-14

- feat(android): expose winning `eCpm` and `networkId` on `AdResult.Loaded` — read the server-quoted CPM and the network that filled each slot.
- feat(android): use server-quoted eCPM from `AdResponse` for the first-party Elo lane; drop publisher-side `expectedEcpm` from `EloNetworkConfiguration`.
- feat(android): layered auction tiebreak — first-party Elo wins exact-eCPM ties; ties between non-Elo adapters fall back to registration order in `EloConfiguration.adapters`.
- feat(android): publish first-party AdMob adapter `ad.elo:elo-android-mediation-admob:0.1.0` — requires `expectedEcpm` at construction time (finite, `>= 0.0`), immutable for the life of the instance.
- fix(android): make `EloAd.release` and invalid bids safe across adapter shutdowns.
- fix(android): tighten auction resource and preload handling.
- chore(android): bump default auction timeout from 3s to 5s.
- docs(android): document `Elo.mediationDebugSnapshot()` — adapter init state, per-adapter latest bid and latency, latest auction outcome and winning network.
- docs(android): sweep README and quickstart sample for the server-driven eCPM model and the removal of the Badge / Chat ad views (`EloAdView` is now the single ad surface).

## 2.3.0 — 2026-05-01

- Merge pull request #8 from growlads/chore/rename-maven-coords-to-ad-elo
- ci(android): migrate dist-repo coord references in publish workflow
- chore(android): rename Maven coords to ad.elo:elo-android-sdk
- Merge pull request #7 from growlads/feat/android-ad-renderer
- fix(android): fail fast in badge/chat surfaces on adapter-rendered ads
- fix(android): rekey AndroidView on ad swap, add AdRenderer.release for native cleanup
- docs(android): clarify clickUrl semantics on renderer path in adapter guide
- docs(android): document AdRenderer hook in adapter author guide
- docs(android): note that badge/chat views skip AdRenderer; route adapter ads via EloAdView
- feat(android): EloAdView delegates to EloRenderedAdView for adapter-rendered ads
- feat(android): add EloRenderedAdView for adapter-owned native rendering
- feat(android): add optional AdRenderer hook to EloAd
- refactor(android): annotate AdRenderer with @MainThread; tighten test consistency
- feat(android): add AdRenderer interface for adapter-owned native rendering
- chore: gitignore .worktrees/ for isolated workspaces
- Merge pull request #6 from growlads/ci-android-dist-repo-sync
- ci(android): tag releases + open dist-repo PR after Maven publish
- Merge pull request #5 from growlads/feat/android-phase-4-docs-and-example
- feat(android): Phase 4 polish — example app refresh, dark-mode fix, doc rewrites
- docs(android): add CLAUDE.md, ADAPTER_AUTHOR_GUIDE.md; rewrite README.md
- feat(android): polish default visual design of the three Compose ad views
- fix(android): apply window insets so AppBar clears the status bar
- fix(android): bump example auctionTimeoutMs to 3000 to match iOS
- fix(android): land DiagnosticsFragment rewrite that git-mv lost
- feat(android): match iOS example app structure (4-tab showcase)
- Merge branch 'main' into feat/android-phase-4-docs-and-example
- feat(android): refresh birajchatapp + add mediation debug screen
- Merge pull request #4 from growlads/feat/android-phase-3-compose-ad-views
- build(android): add scripts/test.sh wrapper for Gradle tests
- refactor(android): address Phase 3 PR review comments
- Merge branch 'main' into feat/android-phase-3-compose-ad-views
- Merge pull request #3 from growlads/feat/android-phase-2-mediation-framework
- refactor(android): address Phase 2 PR review comments
- feat(android): host Elo ad view via ComposeView in birajchatapp
- refactor(android): drop legacy XML EloAdView and Glide plumbing
- feat(android): add Compose EloBadgeAdView and EloChatAdView formats
- feat(android): add Compose EloAdView with auto render/impression/click tracking
- refactor(android): reshape EloAdStyle for Compose (Color/Dp)
- feat(android): add Modifier.adImpressionTracking (>=50% for 1s)
- test(android): hold strong listener ref in mediator-routing test
- feat(android): add AdTrackingRegistry impression dedup singleton
- build(android): add Compose BOM + Coil; bump JVM target to 11; add kotlin-compose plugin
- refactor(android): apply Phase 2 review fixes
- Add .idea to gitignore
- feat(android): drive loadAd through ParallelAuctionMediator; validate adapters
- feat(android): add first-party EloNetworkAdapter
- feat(android): add ParallelAuctionMediator with adapter startup coordination
- feat(android): redesign mediation debug snapshot + add MediationDebugRecorder
- feat(android): replace AdNetworkAdapter marker with full contract
- refactor(android): EloAd carries AdTracker; drop public render/impression URLs

## 2.2.3 — 2026-04-08

Initial entry pinned to the version currently on Maven Central. Subsequent entries are auto-prepended by the source-repo's `update-dist-repo` job after each Maven publish.

- Public View-based ad rendering: `EloAdView` (`MaterialCardView`) with `show(result)`, `clear()`, `setStyle(style)`.
- `Elo.initialize(context, publisherId, adUnitId, …)` lifecycle entry.
- `Elo.loadAd(messages: List<ChatMessage>): AdResult` (suspend).
- `Elo.shutdown()` for teardown.

> The `Elo*` symbol names reflect the published Maven artifact (`ad.elo:elo-android-sdk`); the consumer-facing product brand is **Elo** (elo.ad).
- `AdResult` sealed type: `Loaded(EloAd)`, `NoFill`, `Error(String)`.
