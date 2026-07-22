# Changelog

## Unreleased

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
