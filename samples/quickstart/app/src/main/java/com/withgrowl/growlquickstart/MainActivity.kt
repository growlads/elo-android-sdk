package com.withgrowl.growlquickstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.withgrowl.growlads.mediation.admob.AdMobNetworkAdapter
import com.withgrowl.growlads.mediation.admob.AdMobPriceTier
import com.withgrowl.growlandroidsdk.Growl
import com.withgrowl.growlandroidsdk.GrowlConfiguration
import com.withgrowl.growlandroidsdk.GrowlNetworkConfiguration
import com.withgrowl.growlandroidsdk.LogLevel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use Growl.configure (instead of Growl.initialize) so we can wire
        // mediation adapters alongside Elo's own demand. The AdMob adapter
        // participates in the parallel auction; Elo picks the highest-eCPM
        // bid for each ad request.
        //
        // Publisher / ad-unit IDs come from BuildConfig — populated by the
        // sample's build.gradle.kts from samples/quickstart/local.properties
        // when present. The committed defaults are placeholders, so the
        // AdMob adapter is only registered when a real ad unit is provided
        // (otherwise MobileAds.initialize would reject the placeholder).
        val admobAdUnitId = BuildConfig.ADMOB_AD_UNIT_ID
        val admobAdapters = if (admobAdUnitId.startsWith("ca-app-pub-")) {
            listOf(
                AdMobNetworkAdapter(
                    priceTiers = listOf(
                        AdMobPriceTier(adUnitId = admobAdUnitId, eCpm = 1.00),
                    ),
                    // Override the attribution chip if you ship in non-English
                    // markets — defaults to "Sponsored" otherwise.
                    // sponsoredLabel = "Werbung",
                ),
            )
        } else {
            emptyList()
        }

        Growl.configure(
            context = this,
            configuration = GrowlConfiguration(
                growl = GrowlNetworkConfiguration(
                    publisherId = BuildConfig.GROWL_PUBLISHER_ID,
                    adUnitId = BuildConfig.GROWL_AD_UNIT_ID,
                ),
                adapters = admobAdapters,
                auctionTimeoutMs = 3_000L,
                logLevel = LogLevel.Debug,
                enableAuctionPriceLogging = true,
            ),
        )

        setContent { MaterialTheme { ChatScreen() } }
    }
}
