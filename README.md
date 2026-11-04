# Elo Android SDK

> Maven coordinates: `ad.elo:elo-ads-android:0.8.0`. See [Installation](#installation).

Contextual ads for Android chat apps, rendered with Jetpack Compose. Distributed through Maven Central.

Full documentation: [docs.elo.ad/android/getting-started](https://docs.elo.ad/android/getting-started/)

## Requirements

- Android API 26+ (`minSdk = 26`)
- Compile against API 36+
- Kotlin 2.0+
- JVM target 11
- Compose BOM 2024.10.01+ in the consuming app

## Installation

Add to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("ad.elo:elo-ads-android:0.8.0")
}
```

Maven Central is enabled by default. If your project uses a custom dependency-resolution config, make sure `mavenCentral()` is in the repositories list.

## Quick Start

Configure the SDK once, early in your app lifecycle:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Elo.configure(
            context = this,
            publisherId = "YOUR_PUBLISHER_ID",
            adUnitId = "YOUR_AD_UNIT_ID",
        )
    }
}
```

Then pass the chat history to an `EloAdView`:

```kotlin
@Composable
fun AdSlot() {
    val messages = listOf(
        ChatMessage(MessageRole.USER, "What's a quick weeknight pasta recipe?"),
        ChatMessage(MessageRole.ASSISTANT, "Try aglio e olio: garlic, olive oil, chili flakes..."),
    )
    EloAdView(messages = messages, modifier = Modifier.fillMaxWidth())
}
```

This overload loads the ad itself. It takes no room while the request is in flight, renders the ad on fill, and stays empty on no-fill or error, so it is safe to leave in the tree. It loads again when `messages`, `contextObjects` or the slot size changes. Pass `onResult` to observe each outcome.

To load the ad yourself, call `Elo.loadAd` from a coroutine and render the `AdResult`:

```kotlin
val result = Elo.loadAd(messages)   // suspend; returns Loaded, NoFill or Error
EloAdView(result = result)          // renders nothing for NoFill and Error
```

`Elo.preloadAd(messages)` starts the same request without waiting for it, so a later `loadAd` with the same inputs can return at once.

`EloAdView` lives in `ad.elo.androidsdk.ui`; the other public types are in `ad.elo.androidsdk`.

## Ad formats

Elo returns the ad in one of four formats, and `EloAdView` renders whichever one arrives. You do not pick the format: you pass the height the slot has as `maxHeight`, and Elo returns the tallest format that fits.

| `maxHeight` | Format |
| --- | --- |
| Not set | Card: a surface for the space between chat messages |
| 272dp or more | Full: a large card for the space a closed keyboard leaves |
| 192 to 271dp | Card |
| 120 to 191dp | Compact: a short card for the space above the open keyboard |
| 72 to 119dp | Strip: a single row, 76dp tall |
| Below 72dp | No ad (`AdResult.NoFill`) |

Elo can also set a preferred format for your app. The server sends it whenever it fits the slot, so you can change what a slot shows without an app release. Read the format of a loaded ad from `EloAd.format`, an `EloAdFormat` (`Strip`, `Compact`, `Card` or `Full`). Later releases can add formats, so a `when` over it needs an `else` branch.

See [Ad Formats](https://docs.elo.ad/ads/ad-formats/) for what each format looks like.

## In the chat list

In a `LazyColumn`, add ads as list items after assistant replies and leave `maxHeight` unset, so the ad is a card:

```kotlin
LazyColumn {
    items(items, key = { it.id }) { item ->
        when (item) {
            is ChatItem.Message -> MessageBubble(item)
            is ChatItem.Ad -> EloAdView(ad = item.ad)
        }
    }
}
```

Use `"ad-${ad.id}"` as the ad item's key: `EloAd.id` identifies one showing.

## Ads above the keyboard

Put an `EloAdView` with your slot's height directly above your composer, in a column that pads for the keyboard. The ad moves up with the composer when the keyboard opens, with no keyboard tracking of your own:

```kotlin
Column(Modifier.fillMaxSize().imePadding()) {
    Transcript(messages, Modifier.weight(1f))
    EloAdView(messages = messages, maxHeight = 76.dp)
    Composer(draft, onSend = ::send)
}
```

Below 120dp the ad is a strip; from 120dp it is a compact card. Mount the view once the chat has messages: an empty transcript gives the request nothing to target.

- `imePadding()` needs an edge-to-edge window (`enableEdgeToEdge()`) with `android:windowSoftInputMode="adjustResize"`.
- Keep your composer's hit area out of the gap above the ad. Ad networks treat placements that invite accidental clicks next to text inputs as policy violations.

### In place of the closed keyboard

The full card fills the space the keyboard leaves when it closes. Load at the keyboard's height and render the result below the composer only while the keyboard is closed, so the impression counts only when the card is visible:

```kotlin
val result = Elo.loadAd(messages = messages, maxHeight = keyboardHeight)
EloAdView(
    result = result,
    fullCardHeight = keyboardHeight,
    onDismiss = { /* the user closed the card */ },
)
```

Without `fullCardHeight` the full card is 272dp tall (`EloAdViewDefaults.FullCardHeight`). Read the keyboard's height from `WindowInsets.imeAnimationTarget`, which gives its final height, not from `WindowInsets.ime`, which changes while the keyboard animates. The card has a close button: it hides the ad and calls `onDismiss`; nothing is sent to Elo.

### Two placements

With a slot above the composer and one in place of the closed keyboard, load once at the keyboard's height and read the format to pick the slot:

```kotlin
val result = Elo.loadAd(messages = messages, maxHeight = keyboardHeight)
if (result is AdResult.Loaded) {
    when (result.ad.format) {
        EloAdFormat.Full -> keyboardAd = result    // in place of the closed keyboard
        else -> aboveComposerAd = result           // strip, compact, card, or a later format
    }
}
```

## Tracking

`EloAdView` sends render, impression and click events itself. The impression is sent once the ad is at least 50% visible for one continuous second, while the app is resumed and its window is focused. Register an `EloAdListener` with `Elo.setListener` to observe loads, failures, impressions and clicks; the SDK holds it weakly, so keep your own reference.

By default a tap opens the ad's URL in the system browser. To open it in your own in-app browser, return `true` from `EloAdListener.onAdOpenClickUrl` and call `Elo.trackBrowserOpened(ad)` and `Elo.trackBrowserClosed(ad)` around it.

## Hosting in XML or a RecyclerView

Wrap the view in a `ComposeView`:

```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/ad_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
lifecycleScope.launch {
    val result = Elo.loadAd(messages)
    binding.adView.setContent {
        MaterialTheme { EloAdView(result = result) }
    }
}
```

In a `RecyclerView`, give each ad row its own `ComposeView` and call `setContent { EloAdView(ad = ad) }` in `bind`.

## Other ad networks

Elo is a single demand source. To run it with another network, keep the fallback in your own code: render the Elo ad on `AdResult.Loaded`, and give the slot to your other network on `NoFill` or `Error`.

`AdResult.Loaded.eCpm` is the price the Elo server quoted for the fill. When your other network also quotes a price, load both and render the higher one. Call `EloAd.releaseResources()` on an Elo ad you decide not to render; ads passed to `EloAdView` are released by the SDK. See [Other Ad Networks](https://docs.elo.ad/ads/mediation/).

## Styling

`EloAdView` uses your Material 3 theme colors by default. Override any of them with `EloAdStyle`:

```kotlin
EloAdView(
    result = result,
    style = EloAdStyle(
        cardBackground = Color(0xFF1A1A2E),
        titleColor = Color.White,
        cornerRadius = 16.dp,
        fontFamily = FontFamily(Font(R.font.inter)),
    ),
)
```

`EloAdStyle` also sets the description, badge, button and border colors, the border width, how a long one-line description overflows, and `chrome = EloAdChrome.Bare` to drop the ad's own background, border and corners.

The disclosure badge reads "Ad" by default. Localize it per view with `EloAdDisclosure`, which also sets the badge's font size, weight and color:

```kotlin
EloAdView(
    result = result,
    sponsoredLabel = EloAdDisclosure(stringResource(R.string.elo_ad)),
)
```

## Upgrading to 0.8.0

0.8.0 removes `EloKeyboardBannerAd`, `EloAdLoadingView`, and the `showLoadingPlaceholder` and `loadingAccessibilityLabel` parameters:

- In place of `EloKeyboardBannerAd`, put an `EloAdView` with your slot's `maxHeight` above your composer; see [Ads above the keyboard](#ads-above-the-keyboard).
- The slot now takes no room until an ad arrives. Reserve the space in your own layout when it must not move.

See the [CHANGELOG](./CHANGELOG.md) for every release.

## Sample

A runnable Compose sample lives in [`samples/quickstart/`](./samples/quickstart). It is a small chat app with a list of chats, each showing one of two placements: a strip in the message feed, and a strip directly above the composer that moves up with the keyboard. Build it with:

```sh
cd samples/quickstart
./gradlew :app:assembleDebug
```

The sample reads publisher and ad unit IDs from a gitignored `samples/quickstart/local.properties` file:

```properties
elo.publisherId=YOUR_PUBLISHER_ID
elo.adUnitId=YOUR_AD_UNIT_ID
```

Without that file the sample uses placeholder IDs, so Elo returns no fill and no ads show. Use the IDs of an active ad unit from your Elo dashboard to see contextual ads.

## Support

- **Bugs:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=bug_report.yml)
- **Integration questions:** [open an issue](https://github.com/growlads/elo-android-sdk/issues/new?template=integration_question.yml)
- **SDK source changes:** the SDK source is closed, so this repo does not accept code PRs against it. Contact the team for fixes or features in the SDK itself.

## License

The sample app and documentation in this repository are MIT licensed (see [LICENSE](./LICENSE)). The Elo Android SDK binary distributed through Maven Central is governed by the commercial license declared in the artifact's POM.
