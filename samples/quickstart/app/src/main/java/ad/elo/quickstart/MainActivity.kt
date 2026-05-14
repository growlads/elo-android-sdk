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
        // when present, otherwise from committed AdMob test defaults so the
        // sample always demos the parallel auction end-to-end.
        Elo.configure(
            context = this,
            configuration = EloConfiguration(
                elo = EloNetworkConfiguration(
                    publisherId = BuildConfig.GROWL_PUBLISHER_ID,
                    adUnitId = BuildConfig.GROWL_AD_UNIT_ID,
                ),
                adapters = listOf(
                    AdMobNetworkAdapter(
                        adUnitId = BuildConfig.ADMOB_AD_UNIT_ID,
                        // Required: AdMob's Mobile Ads SDK does not surface a
                        // programmatic bid price, so the adapter bids this
                        // value. Set it to your realized eCPM for this ad
                        // unit from AdMob's dashboard (a blended last-30-day
                        // figure is a reasonable starting point). 0.0 makes
                        // AdMob last-resort backfill — Elo wins 0.0 ties.
                        expectedEcpm = 0.0,
                        // Override the attribution chip if you ship in non-English
                        // markets — defaults to "Sponsored" otherwise.
                        // sponsoredLabel = "Werbung",
                    ),
                ),
                logLevel = LogLevel.Debug,
                enableAuctionPriceLogging = true,
            ),
        )

        setContent { MaterialTheme { ChatScreen() } }
    }
}
