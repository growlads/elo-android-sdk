package ad.elo.quickstart

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ad.elo.androidsdk.ChatMessage
import ad.elo.androidsdk.EloAdLayout
import ad.elo.androidsdk.MessageRole
import ad.elo.androidsdk.ui.EloAdView
import ad.elo.androidsdk.ui.EloKeyboardBannerAd

/** Which ad format a chat demonstrates once you open it. */
private enum class AdFormat(val title: String, val blurb: String) {
    /** A banner strip rendered inline in the message feed (`EloAdLayout.InlineBanner`). */
    InlineBanner("Inline banner", "Renders in the message feed"),

    /** A banner pinned above the keyboard while the composer is focused (`EloKeyboardBannerAd`). */
    KeyboardBanner("Keyboard banner", "Pins above the keyboard — tap the composer"),
}

/** A demo conversation plus the ad format it shows. */
private data class Chat(
    val title: String,
    val messages: List<ChatMessage>,
    val format: AdFormat,
) {
    val preview: String get() = messages.lastOrNull()?.content.orEmpty()
}

// Two demo chats, each demonstrating one of the two banner formats. Each
// `EloAdView` / `EloKeyboardBannerAd` loads its own contextual ad from Elo's
// demand and renders the winning creative in the requested layout. (Register
// mediation adapters in EloConfiguration.adapters to add more demand.)
private val demoChats = listOf(
    Chat(
        title = "Marathon training",
        messages = listOf(
            ChatMessage(MessageRole.USER, "What's the best running shoe for marathon training?"),
            ChatMessage(
                MessageRole.ASSISTANT,
                "For marathon training, you'll want shoes with good cushioning and " +
                    "durability. Brands like Hoka, Nike, and Brooks are popular picks.",
            ),
        ),
        format = AdFormat.InlineBanner,
    ),
    Chat(
        title = "Home espresso",
        messages = listOf(
            ChatMessage(MessageRole.USER, "How do I make espresso at home without a fancy machine?"),
            ChatMessage(
                MessageRole.ASSISTANT,
                "A stovetop Moka pot gets you close for very little money. Use fine " +
                    "ground coffee, medium heat, and pull it off the stove as soon as " +
                    "it starts sputtering.",
            ),
        ),
        format = AdFormat.KeyboardBanner,
    ),
)

/**
 * Root: a list of chats. Opening one shows its conversation and the ad format
 * it demonstrates. (Compose Navigation isn't pulled in to keep the sample's
 * dependency surface minimal — a single selection state does the job.)
 */
@Composable
fun ChatScreen() {
    var openChat by remember { mutableStateOf<Chat?>(null) }

    val chat = openChat
    if (chat == null) {
        ChatListScreen(chats = demoChats, onOpen = { openChat = it })
    } else {
        BackHandler { openChat = null }
        ChatDetailScreen(chat = chat, onBack = { openChat = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListScreen(chats: List<Chat>, onOpen: (Chat) -> Unit) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Chats") }) },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(chats) { chat ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(chat) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(chat.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = chat.preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = chat.format.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

/**
 * A single conversation. Depending on the chat's format it renders either an
 * inline banner in the feed or a banner pinned above the keyboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailScreen(chat: Chat, onBack: () -> Unit) {
    var draft by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(chat.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = chat.format.blurb,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                chat.messages.forEach { message ->
                    MessageBubble(message.role, message.content)
                }

                // The inline-banner chat renders its ad in the feed. The
                // keyboard-banner chat shows its ad via EloKeyboardBannerAd
                // below instead.
                if (chat.format == AdFormat.InlineBanner) {
                    EloAdView(
                        messages = chat.messages,
                        layout = EloAdLayout.InlineBanner,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // EloKeyboardBannerAd pins a banner strip above the software
            // keyboard with one composable — it loads from the conversation,
            // collapses on no-fill or error, and rests at the bottom of the
            // screen while the composer is unfocused.
            if (chat.format == AdFormat.KeyboardBanner) {
                EloKeyboardBannerAd(
                    messages = chat.messages,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Composer(
                draft = draft,
                onDraftChange = { draft = it },
                onSend = { draft = "" },
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
                placeholder = { Text("Message") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
            )
            Spacer(Modifier.padding(horizontal = 4.dp))
            IconButton(onClick = onSend, enabled = draft.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}
