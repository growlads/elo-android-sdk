package com.withgrowl.growlquickstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.withgrowl.growlads.mediation.admob.AdMobNetworkAdapter
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

        Growl.configure(
            context = this,
            configuration = GrowlConfiguration(
                growl = GrowlNetworkConfiguration(
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
