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
            GrowlAdView(result = result, modifier = Modifier.fillMaxWidth())
        }
    }
}
```

`GrowlAdView` lives in `com.withgrowl.growlandroidsdk.ui` and renders nothing on `AdResult.NoFill` / `AdResult.Error`, so it is safe to leave in the tree unconditionally.

## Ad formats

Three Compose ad views ship in `com.withgrowl.growlandroidsdk.ui`. Pass an `AdResult` to any of them.

| Standard (`GrowlAdView`) | Badge (`GrowlBadgeAdView`) | Chat (`GrowlChatAdView`) |
| :---: | :---: | :---: |
| ![Standard ad](docs/screenshots/standard.png) | ![Badge ad](docs/screenshots/badge.png) | ![Chat ad](docs/screenshots/chat.png) |
| Horizontal card with thumbnail, headline, and description. | Compact pill-style row that fits inline between messages. | Tall card with a prominent image — feels like a chat-feed post. |

All three auto-fire render telemetry on first composition and impression telemetry once the view is ≥50% visible for one continuous second.

## Sample

A runnable Compose sample lives in [`samples/quickstart/`](./samples/quickstart). It pairs the SDK with a small canned-reply chat UI so you can see the contextual ad surface after each turn. Build it with:

```sh
cd samples/quickstart
./gradlew :app:assembleDebug
```

The sample ships with placeholder publisher / ad-unit IDs. To see live ads, swap them for the IDs from your Elo dashboard, or contact us for sandbox IDs.

## Styling

```kotlin
GrowlAdView(
    result = result,
    style = GrowlAdStyle(
        cornerRadius = 16.dp,
        // see GrowlAdStyle for the full set of tunables (colors, padding, typography weights)
    ),
)
```

## Docs

Full documentation: [elo.ad/docs/android](https://elo.ad/docs/android)

## Support

- **Bugs:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=bug_report.yml)
- **Integration questions:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=integration_question.yml)
- **SDK source changes:** for fixes or features in the SDK itself, contact the team — this repo does not accept code PRs against the SDK source (which is closed).

## License

The sample app and documentation in this repository are MIT licensed (see [LICENSE](./LICENSE)). The Elo Android SDK binary distributed via Maven Central is governed by the commercial license declared in the artifact's POM.
