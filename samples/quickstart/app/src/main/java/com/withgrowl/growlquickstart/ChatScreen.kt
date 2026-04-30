package com.withgrowl.growlquickstart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.withgrowl.growlandroidsdk.AdResult
import com.withgrowl.growlandroidsdk.ChatMessage
import com.withgrowl.growlandroidsdk.Growl
import com.withgrowl.growlandroidsdk.GrowlAdView
import com.withgrowl.growlandroidsdk.MessageRole
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    var ad by remember { mutableStateOf<AdResult?>(null) }
    val scope = rememberCoroutineScope()

    val messages = remember {
        listOf(
            ChatMessage(MessageRole.USER, "What's a quick weeknight pasta recipe?"),
            ChatMessage(MessageRole.ASSISTANT, "Cacio e pepe — pasta, pecorino, lots of black pepper."),
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            messages.forEach { Text("${it.role.name.lowercase()}: ${it.content}") }

            Button(onClick = {
                scope.launch { ad = Growl.loadAd(messages) }
            }) { Text("Load ad") }

            Spacer(Modifier.height(8.dp))

            ad?.let { result ->
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx -> GrowlAdView(ctx).apply { show(result) } },
                    update = { it.show(result) },
                )
            }
        }
    }
}
