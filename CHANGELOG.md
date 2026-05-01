# Changelog

## Unreleased

## 2.3.0 — 2026-05-01

- Merge pull request #8 from growlads/chore/rename-maven-coords-to-ad-elo
- ci(android): migrate dist-repo coord references in publish workflow
- chore(android): rename Maven coords to ad.elo:elo-android-sdk
- Merge pull request #7 from growlads/feat/android-ad-renderer
- fix(android): fail fast in badge/chat surfaces on adapter-rendered ads
- fix(android): rekey AndroidView on ad swap, add AdRenderer.release for native cleanup
- docs(android): clarify clickUrl semantics on renderer path in adapter guide
- docs(android): document AdRenderer hook in adapter author guide
- docs(android): note that badge/chat views skip AdRenderer; route adapter ads via GrowlAdView
- feat(android): GrowlAdView delegates to GrowlRenderedAdView for adapter-rendered ads
- feat(android): add GrowlRenderedAdView for adapter-owned native rendering
- feat(android): add optional AdRenderer hook to GrowlAd
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
- feat(android): host Growl ad view via ComposeView in birajchatapp
- refactor(android): drop legacy XML GrowlAdView and Glide plumbing
- feat(android): add Compose GrowlBadgeAdView and GrowlChatAdView formats
- feat(android): add Compose GrowlAdView with auto render/impression/click tracking
- refactor(android): reshape GrowlAdStyle for Compose (Color/Dp)
- feat(android): add Modifier.adImpressionTracking (>=50% for 1s)
- test(android): hold strong listener ref in mediator-routing test
- feat(android): add AdTrackingRegistry impression dedup singleton
- build(android): add Compose BOM + Coil; bump JVM target to 11; add kotlin-compose plugin
- refactor(android): apply Phase 2 review fixes
- Add .idea to gitignore
- feat(android): drive loadAd through ParallelAuctionMediator; validate adapters
- feat(android): add first-party GrowlNetworkAdapter
- feat(android): add ParallelAuctionMediator with adapter startup coordination
- feat(android): redesign mediation debug snapshot + add MediationDebugRecorder
- feat(android): replace AdNetworkAdapter marker with full contract
- refactor(android): GrowlAd carries AdTracker; drop public render/impression URLs

## 2.2.3 — 2026-04-08

Initial entry pinned to the version currently on Maven Central. Subsequent entries are auto-prepended by the source-repo's `update-dist-repo` job after each Maven publish.

- Public View-based ad rendering: `GrowlAdView` (`MaterialCardView`) with `show(result)`, `clear()`, `setStyle(style)`.
- `Growl.initialize(context, publisherId, adUnitId, …)` lifecycle entry.
- `Growl.loadAd(messages: List<ChatMessage>): AdResult` (suspend).
- `Growl.shutdown()` for teardown.

> The `Growl*` symbol names reflect the published Maven artifact (`com.withgrowl:growl-android-sdk`); the consumer-facing product brand is **Elo** (elo.ad).
- `AdResult` sealed type: `Loaded(GrowlAd)`, `NoFill`, `Error(String)`.
