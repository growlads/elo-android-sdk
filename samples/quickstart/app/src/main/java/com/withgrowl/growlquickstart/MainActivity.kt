package com.withgrowl.growlquickstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.withgrowl.growlandroidsdk.Growl

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
