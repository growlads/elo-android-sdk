# Changelog

## Unreleased

## 2.2.3 — 2026-04-08

Initial entry pinned to the version currently on Maven Central. Subsequent entries are auto-prepended by the source-repo's `update-dist-repo` job after each Maven publish.

- Public View-based ad rendering: `GrowlAdView` (`MaterialCardView`) with `show(result)`, `clear()`, `setStyle(style)`.
- `Growl.initialize(context, publisherId, adUnitId, …)` lifecycle entry.
- `Growl.loadAd(messages: List<ChatMessage>): AdResult` (suspend).
- `Growl.shutdown()` for teardown.

> The `Growl*` symbol names reflect the published Maven artifact (`com.withgrowl:growl-android-sdk`); the consumer-facing product brand is **Elo** (elo.ad).
- `AdResult` sealed type: `Loaded(GrowlAd)`, `NoFill`, `Error(String)`.
