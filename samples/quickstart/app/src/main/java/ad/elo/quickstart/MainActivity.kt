package ad.elo.quickstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import ad.elo.androidsdk.Elo
import ad.elo.androidsdk.EloConfiguration
import ad.elo.androidsdk.EloNetworkConfiguration
import ad.elo.androidsdk.LogLevel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Elo.configure is the SDK's entry point. This sample uses Elo's own
        // demand only — no mediation adapters. To add a network, pass it in
        // `adapters` (each adapter joins Elo's parallel first-price auction).
        //
        // Publisher / ad-unit IDs come from BuildConfig — populated by the
        // sample's build.gradle.kts from samples/quickstart/local.properties
        // when present, otherwise from committed placeholders. Supply real
        // Elo dashboard IDs to see live fills.
        Elo.configure(
            context = this,
            configuration = EloConfiguration(
                elo = EloNetworkConfiguration(
                    publisherId = BuildConfig.GROWL_PUBLISHER_ID,
                    adUnitId = BuildConfig.GROWL_AD_UNIT_ID,
                ),
                logLevel = LogLevel.Debug,
            ),
        )

        setContent { MaterialTheme { ChatScreen() } }
    }
}
