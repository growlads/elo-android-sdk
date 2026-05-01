# Elo Android SDK

> Maven coordinates: `ad.elo:elo-android-sdk:2.3.0` — see [Installation](#installation).

Contextual ads for Android chat apps. Distributed via Maven Central.

## Requirements

- Android API 26+ (`minSdk = 26`)
- Kotlin 2.0+
- Compile against API 36+

## Installation

Add to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("ad.elo:elo-android-sdk:2.3.0")
}
```

Maven Central is enabled by default. If your project uses a custom dependency-resolution config, ensure `mavenCentral()` is in the repositories list.

## Quick Start

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Growl.initialize(
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
            scope.launch { ad = Growl.loadAd(messages) }
        }) { Text("Load ad") }

        ad?.let { result ->
            AndroidView(
                factory = { ctx -> GrowlAdView(ctx).apply { show(result) } },
                update = { it.show(result) },
            )
        }
    }
}
```

## Screenshots

| Default ad | With image |
| :---: | :---: |
| ![Default ad](docs/screenshots/default.png) | ![Ad with image](docs/screenshots/with-image.png) |

> Drop screenshot PNGs into `docs/screenshots/` and they will render here. Replace this list with the actual ad formats you ship.

## Sample

A runnable Compose sample lives in [`samples/quickstart/`](./samples/quickstart). Single Activity + Composable; reading `MainActivity.kt` and `ChatScreen.kt` is the integration. Build it with:

```sh
cd samples/quickstart
./gradlew :app:assembleDebug
```

The sample ships with placeholder publisher / ad-unit IDs. To see a live ad, swap them for the IDs from your Elo dashboard, or contact us for sandbox IDs.

## Ad rendering

`GrowlAdView` is a `MaterialCardView` subclass (View system). Embed it in Compose via `AndroidView { ctx -> GrowlAdView(ctx).apply { show(result) } }`, or place it directly in an XML layout.

A pure-Compose ad-view API is in development for an upcoming release.

## Styling

```kotlin
GrowlAdView(context).apply {
    setStyle(GrowlAdStyle(
        cardBackgroundDp = 12f,
        cornerRadiusDp = 16f,
    ))
    show(result)
}
```

See `GrowlAdStyle` for the full set of tunables.

## Docs

Full documentation: [elo.ad/docs/android](https://elo.ad/docs/android)

## Support

- **Bugs:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=bug_report.yml)
- **Integration questions:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=integration_question.yml)
- **SDK source changes:** for fixes or features in the SDK itself, contact the team — this repo does not accept code PRs against the SDK source (which is closed).

## License

The sample app and documentation in this repository are MIT licensed (see [LICENSE](./LICENSE)). The Elo Android SDK binary distributed via Maven Central is governed by the commercial license declared in the artifact's POM.
