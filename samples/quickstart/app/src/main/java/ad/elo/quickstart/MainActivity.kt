package ad.elo.quickstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import ad.elo.mediation.admob.AdMobNetworkAdapter
import ad.elo.androidsdk.Elo
import ad.elo.androidsdk.EloConfiguration
import ad.elo.androidsdk.EloNetworkConfiguration
import ad.elo.androidsdk.LogLevel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use Elo.configure (instead of Elo.initialize) so we can wire
        // mediation adapters alongside Elo's own demand. The AdMob adapter
        // participates in the parallel auction; Elo picks the highest-eCPM
        // bid for each ad request.
        //
        // Publisher / ad-unit IDs come from BuildConfig — populated by the
        // sample's build.gradle.kts from samples/quickstart/local.properties
        // when present. The committed defaults are placeholders. Both the
        // AdMob app ID (manifest meta-data) and the ad unit ID must be real
        // before we register the adapter; otherwise MobileAds.initialize
        // would later fail on the placeholder values.
        val admobAppId    = BuildConfig.ADMOB_APP_ID
        val admobAdUnitId = BuildConfig.ADMOB_AD_UNIT_ID
        val admobConfigured = admobAppId.startsWith("ca-app-pub-") &&
            admobAdUnitId.startsWith("ca-app-pub-")
        val admobAdapters = if (admobConfigured) {
            listOf(
                AdMobNetworkAdapter(
                    adUnitId = admobAdUnitId,
                    // Override the attribution chip if you ship in non-English
                    // markets — defaults to "Sponsored" otherwise.
                    // sponsoredLabel = "Werbung",
                ),
            )
        } else {
            emptyList()
        }

        Elo.configure(
            context = this,
            configuration = EloConfiguration(
                elo = EloNetworkConfiguration(
                    publisherId = BuildConfig.GROWL_PUBLISHER_ID,
                    adUnitId = BuildConfig.GROWL_AD_UNIT_ID,
                ),
                adapters = admobAdapters,
                logLevel = LogLevel.Debug,
                enableAuctionPriceLogging = true,
            ),
        )

        setContent { MaterialTheme { ChatScreen() } }
    }
}
