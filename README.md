# Elo Android SDK

> Maven coordinates: `ad.elo:elo-android-sdk:2.5.2` — see [Installation](#installation).

Contextual ads for Android chat apps. Distributed via Maven Central.

Full documentation: [docs.elo.ad/android/getting-started](https://docs.elo.ad/android/getting-started/)

## Requirements

- Android API 26+ (`minSdk = 26`)
- JVM target 11 (Compose requirement)
- Compose BOM 2024.10.01+ in the consuming app
- Kotlin 2.0+
- Compile against API 36+

## Installation

Add to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("ad.elo:elo-android-sdk:2.5.2")
}
```

Maven Central is enabled by default. If your project uses a custom dependency-resolution config, ensure `mavenCentral()` is in the repositories list.

## Quick Start

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Elo.initialize(
            context = this,
            publisherId = "YOUR_PUBLISHER_ID",
            adUnitId = "YOUR_AD_UNIT_ID",
        )
        setContent { MaterialTheme { ChatScreen() } }
    }
}

@Composable
fun ChatScreen() {
    var ad by remember { mutableStateOf<AdResult?>(null) }
    val scope = rememberCoroutineScope()
    val messages = listOf(
        ChatMessage(MessageRole.USER, "What's a quick weeknight pasta recipe?"),
    )
    Column {
        Button(onClick = {
            scope.launch { ad = Elo.loadAd(messages) }
        }) { Text("Load ad") }

        ad?.let { result ->
            EloAdView(result = result, modifier = Modifier.fillMaxWidth())
        }
    }
}
```

`EloAdView` lives in `ad.elo.androidsdk.ui` and renders nothing on `AdResult.NoFill` / `AdResult.Error`, so it is safe to leave in the tree unconditionally.

`AdResult.Loaded` also exposes `eCpm` (USD-equivalent CPM of the winning bid) and `networkId` (`"elo"` or the id of a mediation adapter). For Elo wins, `eCpm` is the price the backend quoted on the bid response; for adapter wins, it's whatever the adapter reported. Use these to run a client-side auction against another SDK, or to attribute which network filled the slot. If you decide *not* to render the ad you received (e.g. an outer auction picks a different source), call `EloAd.release()` to release adapter-owned resources before rendering the other ad — ads you pass to `EloAdView` (or `Elo.trackRender`) are managed by the SDK and must not be released manually.

## Loading State

For the common path, let the view load and render the ad:

```kotlin
EloAdView(messages = messages)
```

This overload shows the built-in `EloAdLoadingView` while `Elo.loadAd(...)` is in flight, renders the ad on fill, and collapses on no-fill or error. Hide the loading placeholder when the host layout should reserve no ad space until a fill arrives:

```kotlin
EloAdView(
    messages = messages,
    showLoadingPlaceholder = false,
)
```

If you own the coroutine and result state yourself, render `EloAdLoadingView` during the load and swap to `EloAdView(result = ...)` afterward:

```kotlin
if (isLoading) {
    EloAdLoadingView()
} else {
    EloAdView(result = result)
}
```

`EloAdLoadingView` is a non-clickable skeleton. It does not fire render, impression, or click tracking. If the loaded ad omits its CTA pill with `ctaLabel = null`, pass `showCtaPlaceholder = false` so the loading slot matches the final layout.

## Ad formats

`EloAdView` (in `ad.elo.androidsdk.ui`) is the single ad surface the SDK ships. It renders Elo-direct fills as a horizontally-laid-out card and delegates adapter-rendered fills (e.g. AdMob native) to the adapter's own renderer.

![Standard ad](docs/screenshots/standard.png)

Render telemetry fires on first composition; impression telemetry fires once the view is ≥50% visible for one continuous second. Click tracking is automatic for Elo-direct fills and handled by the network's own SDK for adapter-rendered fills.

To host in XML / Fragments, wrap it in `ComposeView`:

```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/ad_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
binding.adView.setContent {
    MaterialTheme { EloAdView(result = result) }
}
```

## Mediation (optional)

Elo runs a parallel first-price auction across its own demand and any mediation adapters you register. Each adapter quotes an eCPM; the highest finite, non-negative bid wins. On exact ties, the first-party Elo lane wins (the publisher keeps 100% of the revenue on a first-party fill); ties between two non-Elo adapters fall back to registration order in `EloConfiguration.adapters`. The default auction timeout is 5s. Adapters are extra dependencies — add only the networks you actually want bidding.

The first-party AdMob adapter is published as a separate artifact:

```kotlin
dependencies {
    implementation("ad.elo:elo-android-sdk:2.5.2")
    implementation("ad.elo:elo-android-mediation-admob:0.1.2")
}
```

### Getting your AdMob App ID and Ad Unit ID

You'll need two distinct values from the [AdMob console](https://apps.admob.com), which look similar but are not interchangeable:

- **App ID** — `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY` (note the **`~`**). Goes in the Android manifest.
  1. Sign in at [apps.admob.com](https://apps.admob.com) → **Apps** → **Add app**, choose **Android**, and follow the prompts. (If your app isn't on Google Play yet, choose "No" when asked and you can link it later.)
  2. On the **Apps** list, copy the value in the **App ID** column.
- **Ad unit ID** — `ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY` (note the **`/`**). Passed to `AdMobNetworkAdapter(adUnitId = …)`.
  1. In the AdMob console, open the app you just created → **Ad units** → **Add ad unit**.
  2. Pick the `Native advanced` format — the Elo AdMob adapter only renders native ad units, and "Native advanced" is AdMob's name for the SDK-renderable native unit.
  3. On the **Configure ad unit settings** screen, fill the form with the values below — the adapter is built around them, so copy verbatim unless noted:

     | Field | Value |
     | --- | --- |
     | Ad unit name | `elo-mediation` (or any label you want in reports) |
     | Ad format | `Native advanced` (already selected) |
     | Partner bidding | unchecked — see note below |
     | Advanced → Media type | `Image` |
     | Advanced → eCPM floor | `Manual floor`, set to the same number you pass as `AdMobNetworkAdapter(expectedEcpm = …)` below |

     - **Partner bidding:** ticking it routes AdMob through a *different* mediation platform's RTB pipe and *permanently* disables AdMob mediation / Google Ads / AdX demand for this unit — the setting is locked after creation.
     - **Media type = `Image`:** the adapter renders image creatives only, not video.
     - **Manual floor:** AdMob will skip fills below this floor, which keeps AdMob's bid honest against Elo's server-quoted price in the parallel auction.
  4. Click **Create ad unit**, then copy the **Ad unit ID** shown on the confirmation screen.

New AdMob apps and ad units may take a few hours before they begin serving live impressions. While you wait, you can substitute Google's [official AdMob test IDs](https://developers.google.com/admob/android/test-ads) — they always fill with a test creative and are safe to commit.

AdMob's Play Services SDK requires the App ID in your manifest. Add it once:

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY" />
</application>
```

Then switch from `Elo.initialize` to `Elo.configure` so you can pass an `adapters` list:

```kotlin
import ad.elo.mediation.admob.AdMobNetworkAdapter

Elo.configure(
    context = this,
    configuration = EloConfiguration(
        elo = EloNetworkConfiguration(
            publisherId = "YOUR_PUBLISHER_ID",
            adUnitId = "YOUR_AD_UNIT_ID",
            // No eCPM is configured here: the Elo backend returns the winning
            // bid's price on every `/ad/request` response, and the adapter
            // bids that server-quoted value.
        ),
        adapters = listOf(
            AdMobNetworkAdapter(
                adUnitId = "ca-app-pub-…/…",
                // Your realized eCPM for this ad unit, from AdMob's dashboard
                // reports. A blended last-30-day figure is a reasonable
                // starting point. Must be finite and >= 0.0. Immutable for
                // the life of the adapter instance — construct a new adapter
                // and re-run `Elo.configure` to change it.
                expectedEcpm = 2.40,
            ),
        ),
    ),
)
```

The ad unit you provide is yours — configure floors and any AdMob-side mediation in your AdMob dashboard. The adapter loads that single unit on every bid.

AdMob's Mobile Ads SDK does not surface a programmatic bid price for native ads, so you tell the adapter what to bid via `expectedEcpm`. `expectedEcpm = 0.0` is permitted but is effectively last-resort backfill — Elo wins `0.0`-vs-`0.0` ties, so a zero AdMob bid only wins when Elo no-fills and no other adapter outbids `0.0`.

`EloAdView` rendering, click tracking, and impression telemetry are unchanged — adapter creatives surface through the same component. For AdMob and other renderer-backed fills, click tracking is handled by the network's own SDK and `EloAdListener.onAdDidReceiveClick` will not fire (observe clicks via the network's own dashboard).

Use `Elo.mediationDebugSnapshot()` to inspect adapter initialization state, per-adapter latest bid and latency, and the latest auction outcome and winning network — useful for distinguishing a generic no-fill from a timeout-driven no-winner.

The AdMob adapter ships from the same SDK release pipeline. To request additional networks, [open an integration question](https://github.com/growlads/elo-android-sdk/issues/new?template=integration_question.yml).

## Sample

A runnable Compose sample lives in [`samples/quickstart/`](./samples/quickstart). It pairs the SDK with a small canned-reply chat UI so you can see the contextual ad surface after each turn. The sample wires the AdMob mediation adapter alongside Elo's own demand, so you can observe the parallel auction end-to-end. Build it with:

```sh
cd samples/quickstart
./gradlew :app:assembleDebug
```

The sample reads publisher / ad-unit IDs from a gitignored `local.properties` file at `samples/quickstart/local.properties` — drop in your own without committing them:

```properties
elo.publisherId=YOUR_PUBLISHER_ID
elo.adUnitId=YOUR_AD_UNIT_ID
admob.appId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
admob.adUnitId=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
```

Without `local.properties`, the sample falls back to Google's official [AdMob test IDs](https://developers.google.com/admob/android/test-ads) for `admob.appId` / `admob.adUnitId`, so the AdMob adapter is registered and you'll see test-ad creatives end-to-end. Elo's own demand still no-fills on placeholder publisher/ad-unit IDs — supply real ones via `local.properties` to see Elo's contextual ads.

## Styling

```kotlin
EloAdView(
    result = result,
    style = EloAdStyle(
        cornerRadius = 16.dp,
        // see EloAdStyle for the full set of tunables (colors, padding, typography weights)
    ),
)
```

Pass the same style to `EloAdLoadingView(style = style)` when you want the loading skeleton to reserve a matching slot before the ad loads.

## Localization

`EloAdView` exposes the three user-visible strings as view-level parameters so you can localize per call without subclassing:

```kotlin
EloAdView(
    result = result,
    ctaLabel = stringResource(R.string.elo_cta),                          // default: "Learn more" (pass null to hide)
    sponsoredLabel = stringResource(R.string.elo_sponsored),              // default: "Sponsored"
    openLinkAccessibilityLabel = stringResource(R.string.elo_open_link),  // default: "Open sponsored link"
)
```

## Support

- **Bugs:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=bug_report.yml)
- **Integration questions:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=integration_question.yml)
- **SDK source changes:** for fixes or features in the SDK itself, contact the team — this repo does not accept code PRs against the SDK source (which is closed).

## License

The sample app and documentation in this repository are MIT licensed (see [LICENSE](./LICENSE)). The Elo Android SDK binary distributed via Maven Central is governed by the commercial license declared in the artifact's POM.
