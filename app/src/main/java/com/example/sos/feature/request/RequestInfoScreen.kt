package com.example.sos.feature.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.HelpAssignment
import com.example.sos.data.model.Reply
import com.example.sos.data.model.Request
import com.example.sos.data.model.RequestStatus
import com.example.sos.ui.components.*

/**
 * Detail request.
 * - Pemilik (Requester): melihat reply, bisa Chat / Select Helper, dan cancel.
 * - Orang lain (calon Helper): melihat reply lain dan bisa mengirim reply.
 */
@Composable
fun RequestInfoScreen(
    requestId: String,
    onBack: () -> Unit,
    onHelperClick: (String) -> Unit,
    onChatClick: (String) -> Unit,
    onOpenAssignment: (String) -> Unit,
    onSelectHelper: (helperId: String) -> Unit,
    onEditRequest: () -> Unit
) {
    val request = SosRepository.request(requestId)
    if (request == null) {
        MissingContent(onBack)
        return
    }

    val isOwner = request.requesterId == SosRepository.CURRENT_USER_ID
    val replies = SosRepository.repliesFor(requestId)
    val assignments = SosRepository.assignmentsFor(requestId)

    var showMenu by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var replyText by rememberSaveable { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (showReportDialog) {
        CancellationReasonDialog(
            title = "Report request",
            confirmText = "Report",
            reasons = ReportRequestReasons,
            onConfirm = {
                SosRepository.reportRequest(requestId, it)
                showReportDialog = false
                android.widget.Toast.makeText(context, "Thanks, our admin team will review it.", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showReportDialog = false }
        )
    }

    val canReply = !isOwner && !SosRepository.isSuspended &&
            request.status == RequestStatus.OPEN &&
            !SosRepository.hasReplied(requestId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        SosTopBar(title = "Request", onBack = onBack) {
            val editable = request.status == RequestStatus.OPEN || request.status == RequestStatus.IN_PROGRESS
            if (isOwner && editable) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit request") },
                            onClick = { showMenu = false; onEditRequest() }
                        )
                        if (assignments.none { it.status.isActive }) {
                            DropdownMenuItem(
                                text = { Text("Cancel request", color = Color(0xFFC62828)) },
                                onClick = { showMenu = false; showCancelDialog = true }
                            )
                        }
                    }
                }
            } else if (!isOwner) {
                // Lapor request yang melanggar pedoman → masuk antrean Moderasi Admin.
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Report request", color = Color(0xFFC62828)) },
                            onClick = { showMenu = false; showReportDialog = true }
                        )
                    }
                }
            }
        }

        if (SosRepository.isSuspended && !isOwner) {
            Text(
                "Your account is suspended. You can’t reply to requests right now.",
                fontSize = 12.sp,
                color = Color(0xFFC62828),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RequestDetailCard(request = request, showRequester = !isOwner)
            }

            item {
                Text(
                    text = "Replies (${replies.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (replies.isEmpty()) {
                item {
                    Text(
                        text = if (isOwner) "No replies yet. Helpers will reply soon."
                        else "Be the first to reply!",
                        fontSize = 13.sp,
                        color = SosGray
                    )
                }
            }

            items(replies, key = { it.id }) { reply ->
                ReplyCard(
                    reply = reply,
                    isOwner = isOwner,
                    request = request,
                    assignment = assignments.firstOrNull { it.helperId == reply.helperId },
                    onHelperClick = { onHelperClick(reply.helperId) },
                    onChatClick = { onChatClick(reply.helperId) },
                    onSelectClick = { onSelectHelper(reply.helperId) },
                    onOpenAssignment = onOpenAssignment
                )
            }
        }

        if (!isOwner) {
            ReplyBar(
                value = replyText,
                onValueChange = { replyText = it },
                enabled = canReply,
                hint = when {
                    SosRepository.hasReplied(requestId) -> "You have replied to this request"
                    request.status != RequestStatus.OPEN -> "This request is closed"
                    else -> "Post a reply..."
                },
                onSend = {
                    SosRepository.addReply(requestId, replyText)
                    replyText = ""
                }
            )
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel request?") },
            text = { Text("This request will be removed from Explore. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    if (SosRepository.cancelRequest(requestId)) onBack()
                }) { Text("Cancel request", color = Color(0xFFC62828)) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep") }
            }
        )
    }
}

@Composable
fun RequestDetailCard(request: Request, showRequester: Boolean) {
    SosCard {
        if (showRequester) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(28.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = SosRepository.user(request.requesterId)?.fullName.orEmpty(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        Text(request.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        RequestMetaGrid(request)
        Spacer(Modifier.height(8.dp))
        Text(request.description, fontSize = 13.sp, color = Color.Black)

        if (request.hasSampleImage || request.attachmentUri != null) {
            Spacer(Modifier.height(10.dp))
            ImagePlaceholder()
        }

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Color.LightGray)
        Spacer(Modifier.height(8.dp))
        Text("Tags", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        TagRow(request.tags)
    }
}

@Composable
private fun ReplyCard(
    reply: Reply,
    isOwner: Boolean,
    request: Request,
    assignment: HelpAssignment?,
    onHelperClick: () -> Unit,
    onChatClick: () -> Unit,
    onSelectClick: () -> Unit,
    onOpenAssignment: (String) -> Unit
) {
    val helper = SosRepository.user(reply.helperId) ?: return
    val isMine = reply.helperId == SosRepository.CURRENT_USER_ID

    SosCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onHelperClick, color = Color.Transparent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(28.dp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isMine) "${helper.fullName} (You)" else helper.fullName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            if (helper.isVerified) {
                                Spacer(Modifier.width(4.dp))
                                VerifiedIcon()
                            }
                        }
                        RatingLine(helper.rating, helper.helpsCount)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(reply.message, fontSize = 13.sp, color = Color.Black)
        reply.portfolioUrl?.let {
            Spacer(Modifier.height(8.dp))
            LinkBox(it)
        }

        if (isOwner) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                SosSmallButton(text = "Chat", onClick = onChatClick, filled = false)
                when {
                    assignment != null -> SosSmallButton(
                        text = "View progress",
                        onClick = { onOpenAssignment(assignment.id) }
                    )
                    request.status == RequestStatus.OPEN -> SosSmallButton(
                        text = "Accept help",
                        onClick = onSelectClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyBar(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    hint: String,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(28.dp)
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
            cursorBrush = SolidColor(SosBlue),
            singleLine = true,
            // Tombol Enter/Send di keyboard juga mengirim reply.
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSend() }),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SosLightBlue, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (value.isEmpty()) Text(hint, fontSize = 13.sp, color = Color.Gray)
                    inner()
                }
            }
        )
        if (enabled) {
            IconButton(onClick = onSend, enabled = value.isNotBlank()) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send reply",
                    tint = if (value.isNotBlank()) SosBlue else Color.Gray
                )
            }
        }
    }
}

@Composable
fun MissingContent(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosTopBar(title = "Not found", onBack = onBack)
        Text(
            "This item is no longer available.",
            color = SosGray,
            modifier = Modifier.padding(20.dp)
        )
    }
}
