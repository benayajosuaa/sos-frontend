package com.example.sos.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.Conversation
import com.example.sos.data.model.Message
import com.example.sos.data.model.MessageType
import com.example.sos.ui.components.*

/** Tab Chat: semua percakapan saya, terbaru di atas. */
@Composable
fun ChatListScreen(onConversationClick: (String) -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    val query = search.trim()

    val conversations = SosRepository.myConversations().filter { c ->
        val other = SosRepository.user(otherPartyId(c))?.fullName.orEmpty()
        val title = SosRepository.request(c.requestId)?.title.orEmpty()
        query.isEmpty() || other.contains(query, true) || title.contains(query, true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosSearchField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
        )

        LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
            if (conversations.isEmpty()) {
                item {
                    Text(
                        text = if (query.isEmpty()) "No conversations yet." else "No chats found.",
                        color = SosGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
            items(conversations, key = { it.id }) { c ->
                ConversationRow(c) { onConversationClick(c.id) }
            }
        }
    }
}

fun otherPartyId(c: Conversation): String =
    if (c.requesterId == SosRepository.CURRENT_USER_ID) c.helperId else c.requesterId

@Composable
private fun ConversationRow(c: Conversation, onClick: () -> Unit) {
    val other = SosRepository.user(otherPartyId(c))
    val request = SosRepository.request(c.requestId)
    val last = SosRepository.lastMessage(c.id)

    Surface(onClick = onClick, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = other?.fullName.orEmpty(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    last?.let { Text(shortTimeAgo(it.sentAt), fontSize = 11.sp, color = SosGray) }
                }
                request?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Description, null, Modifier.size(12.dp), tint = SosGray)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            it.title,
                            fontSize = 11.sp,
                            color = SosGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = last?.let { previewText(it) }.orEmpty(),
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun previewText(m: Message): String {
    val mine = m.senderId == SosRepository.CURRENT_USER_ID
    return when (m.type) {
        MessageType.TEXT, MessageType.SYSTEM -> m.text
        MessageType.REQUEST -> if (mine) "You sent the request details" else "Sent the request details"
        MessageType.FILE -> "📎 ${m.attachmentName}"
    }
}
