# Elo Android SDK

> Maven coordinates: `ad.elo:elo-android-sdk:2.5.0` — see [Installation](#installation).

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
    implementation("ad.elo:elo-android-sdk:2.5.0")
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

Elo runs a parallel first-price auction across its own demand and any mediation adapters you register. Each adapter quotes an eCPM; the highest bid at or above your configured floor wins. On exact ties, the first-party Elo lane wins (the publisher keeps 100% of the revenue on a first-party fill); ties between two non-Elo adapters fall back to registration order in `EloConfiguration.adapters`. The default auction timeout is 5s. Adapters are extra dependencies — add only the networks you actually want bidding.

The first-party AdMob adapter is published as a separate artifact:

```kotlin
dependencies {
    implementation("ad.elo:elo-android-sdk:2.5.0")
    implementation("ad.elo:elo-android-mediation-admob:0.1.0")
}
```

AdMob's Play Services SDK requires its app ID in your manifest. Add it once:

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
                // Optional: override the attribution chip for non-English markets.
                // sponsoredLabel = "Werbung",
            ),
        ),
    ),
)
```

The ad unit you provide is yours — configure floors and any AdMob-side mediation in your AdMob dashboard. The adapter loads that single unit on every bid.

AdMob's Mobile Ads SDK does not surface a programmatic bid price for native ads, so you tell the adapter what to bid via `expectedEcpm`. `expectedEcpm = 0.0` is permitted but is effectively last-resort backfill — Elo wins `0.0`-vs-`0.0` ties, and any positive floor filters AdMob out along with every below-floor bid.

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

## Support

- **Bugs:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=bug_report.yml)
- **Integration questions:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=integration_question.yml)
- **SDK source changes:** for fixes or features in the SDK itself, contact the team — this repo does not accept code PRs against the SDK source (which is closed).

## License

The sample app and documentation in this repository are MIT licensed (see [LICENSE](./LICENSE)). The Elo Android SDK binary distributed via Maven Central is governed by the commercial license declared in the artifact's POM.
