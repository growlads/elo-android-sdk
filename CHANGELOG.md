# Changelog

## Unreleased

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
