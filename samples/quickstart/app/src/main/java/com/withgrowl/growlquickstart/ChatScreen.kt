package com.withgrowl.growlquickstart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.withgrowl.growlandroidsdk.AdResult
import com.withgrowl.growlandroidsdk.ChatMessage
import com.withgrowl.growlandroidsdk.Growl
import com.withgrowl.growlandroidsdk.MessageRole
import com.withgrowl.growlandroidsdk.ui.GrowlAdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface ChatRow {
    data class Message(val role: MessageRole, val text: String) : ChatRow
    data class Ad(val result: AdResult) : ChatRow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val rows = remember { mutableStateListOf<ChatRow>(seedGreeting()) }
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(rows.size) {
        if (rows.isNotEmpty()) listState.animateScrollToItem(rows.size - 1)
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Elo Quickstart") }) },
        modifier = Modifier.imePadding(),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(rows) { row ->
                    when (row) {
                        is ChatRow.Message -> MessageBubble(row.role, row.text)
                        is ChatRow.Ad -> GrowlAdView(
                            result = row.result,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Composer(
                draft = draft,
                onDraftChange = { draft = it },
                enabled = !sending,
                onSend = {
                    val text = draft.trim()
                    if (text.isEmpty() || sending) return@Composer
                    draft = ""
                    sending = true
                    scope.launch {
                        rows += ChatRow.Message(MessageRole.USER, text)
                        delay(350)
                        rows += ChatRow.Message(MessageRole.ASSISTANT, cannedReply(text))

                        val transcript = rows.filterIsInstance<ChatRow.Message>()
                            .map { ChatMessage(it.role, it.text) }
                        rows += ChatRow.Ad(Growl.loadAd(transcript))
                        sending = false
                    }
                },
            )
        }
    }
}

@Composable
private fun MessageBubble(role: MessageRole, text: String) {
    val isUser = role == MessageRole.USER
    val bubbleColor =
        if (isUser) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant
    val textColor =
        if (isUser) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(text = text, color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    onDraftChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit,
) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                placeholder = { Text("Ask anything…") },
                enabled = enabled,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
            )
            Spacer(Modifier.padding(horizontal = 4.dp))
            IconButton(onClick = onSend, enabled = enabled && draft.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

private fun seedGreeting(): ChatRow.Message =
    ChatRow.Message(
        MessageRole.ASSISTANT,
        "Hi! Ask me anything — I'll respond with a canned reply, " +
            "and Elo will surface a contextual ad after each turn.",
    )

private fun cannedReply(prompt: String): String {
    val lower = prompt.lowercase()
    return when {
        "recipe" in lower || "cook" in lower || "pasta" in lower ->
            "Try cacio e pepe — pasta, pecorino, lots of black pepper. Done in 15 minutes."
        "movie" in lower || "watch" in lower ->
            "If you liked slow-burn sci-fi, try Arrival or Annihilation."
        "travel" in lower || "trip" in lower || "vacation" in lower ->
            "For a long weekend, Lisbon and Porto are easy to combine — fast train, great food."
        "code" in lower || "bug" in lower || "android" in lower ->
            "Reproduce it on a clean branch first, then bisect. Logs beat guesses."
        "book" in lower || "read" in lower ->
            "Recent favourite: Project Hail Mary. Easy to read, hard to put down."
        else ->
            "Got it. (This is a canned demo reply — wire your own LLM in for real responses.)"
    }
}
