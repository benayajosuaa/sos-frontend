package com.example.sos.feature.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.AssignmentStatus
import com.example.sos.data.model.Conversation
import com.example.sos.data.model.HelpAssignment
import com.example.sos.data.model.Message
import com.example.sos.data.model.MessageType
import com.example.sos.data.model.Presence
import com.example.sos.data.model.Request
import com.example.sos.data.model.RequestStatus
import com.example.sos.feature.request.MissingContent
import com.example.sos.ui.components.*

private enum class ChatDialog { DECLINE, CANCEL, COMPLETE, REQUEST_COMPLETION, CONFIRM_CANCEL, REPORT, DISPUTE }

/**
 * Ruang chat Requester ↔ Helper untuk satu request.
 * Tombol di atas mengikuti status assignment (lihat docs/business-rules.md).
 */
@Composable
fun ChatRoomScreen(
    conversationId: String,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAcceptHelp: (requestId: String, helperId: String) -> Unit,
    onEditRequest: (String) -> Unit,
    onCancellationSent: () -> Unit,
    onReview: (assignmentId: String) -> Unit
) {
    val conversation = SosRepository.conversation(conversationId)
    val request = conversation?.let { SosRepository.request(it.requestId) }
    if (conversation == null || request == null) {
        MissingContent(onBack)
        return
    }

    val context = LocalContext.current
    val iAmRequester = conversation.requesterId == SosRepository.CURRENT_USER_ID
    val otherId = otherPartyId(conversation)
    val other = SosRepository.user(otherId)
    val otherName = other?.fullName.orEmpty()
    val assignment = SosRepository.assignmentFor(request.id, conversation.helperId)
    val messages = SosRepository.messagesFor(conversationId)

    var input by rememberSaveable { mutableStateOf("") }
    var dialog by remember { mutableStateOf<ChatDialog?>(null) }
    val listState = rememberLazyListState()

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { SosRepository.sendFile(conversationId, fileNameFromUri(context, it)) }
    }

    // Jumlah item di list = pesan + pemisah tanggal; gulir ke paling bawah saat ada pesan baru.
    val itemCount = messages.size + messages.map { it.sentAt.toLocalDate() }.distinct().size
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Header: nama + status online.
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SosBlue)
            }
            Surface(onClick = { onProfileClick(otherId) }, color = Color.Transparent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(34.dp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(otherName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        val online = other?.presence == Presence.ONLINE
                        Text(
                            text = other?.presence?.label ?: "",
                            fontSize = 11.sp,
                            color = if (online) SosBlue else SosGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Box {
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (assignment != null && assignment.status.isActive &&
                        !SosRepository.hasOpenDispute(assignment.id)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Raise a dispute") },
                            onClick = { showMenu = false; dialog = ChatDialog.DISPUTE }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Report $otherName", color = Color(0xFFC62828)) },
                        onClick = { showMenu = false; dialog = ChatDialog.REPORT }
                    )
                }
            }
        }

        if (assignment != null && SosRepository.hasOpenDispute(assignment.id)) {
            Text(
                "Dispute in review. SOS admin will decide on the payment for this request.",
                fontSize = 12.sp,
                color = Color(0xFFB45309),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFFFFF4E0), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }
        if (SosRepository.isSuspended) {
            Text(
                "Your account is suspended. Messaging and new actions are disabled.",
                fontSize = 12.sp,
                color = Color(0xFFC62828),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        ChatActions(
            conversation = conversation,
            request = request,
            assignment = assignment,
            iAmRequester = iAmRequester,
            otherName = otherName,
            onDialog = { dialog = it },
            onAccept = { onAcceptHelp(request.id, conversation.helperId) },
            onReview = { assignment?.let { onReview(it.id) } }
        )

        HorizontalDivider(color = SosBorder)
        Text(
            text = request.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
        if (assignment != null && assignment.status.isActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Event, null, Modifier.size(14.dp), tint = Color.Black)
                Spacer(Modifier.width(4.dp))
                Text(
                    "Deadline in: ${deadlineText(assignment.deadline).replaceFirstChar { it.uppercase() }}",
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
        HorizontalDivider(color = SosBorder, modifier = Modifier.padding(top = 4.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            var lastDay: java.time.LocalDate? = null
            messages.forEach { message ->
                val day = message.sentAt.toLocalDate()
                if (day != lastDay) {
                    lastDay = day
                    item(key = "day-$day") {
                        Text(
                            chatDayLabel(day),
                            fontSize = 11.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        )
                    }
                }
                item(key = message.id) {
                    MessageItem(
                        message = message,
                        request = request,
                        canEditRequest = iAmRequester &&
                                request.status != RequestStatus.CLOSED &&
                                request.status != RequestStatus.CANCELLED,
                        onEditRequest = { onEditRequest(request.id) },
                        onAddAttachment = { onEditRequest(request.id) }
                    )
                }
            }
        }

        if (!conversation.declined) {
            MessageInput(
                value = input,
                onValueChange = { input = it },
                onAttach = { filePicker.launch("*/*") },
                onSend = {
                    SosRepository.sendText(conversationId, input)
                    input = ""
                }
            )
        }
    }

    when (dialog) {
        ChatDialog.DECLINE -> SosConfirmDialog(
            title = "Decline help?",
            message = "Are you sure you would like to decline help from this user?",
            onConfirm = { SosRepository.declineHelp(conversationId); dialog = null },
            onDismiss = { dialog = null }
        )

        ChatDialog.CANCEL -> CancellationReasonDialog(
            reasons = if (iAmRequester) SosRepository.cancellationReasonsForRequester
            else SosRepository.cancellationReasonsForHelper,
            onConfirm = { reason ->
                assignment?.let { SosRepository.requestCancellation(it.id, reason) }
                dialog = null
                onCancellationSent()
            },
            onDismiss = { dialog = null }
        )

        ChatDialog.COMPLETE -> SosConfirmDialog(
            title = "Confirmation",
            message = "Are you sure the work has been completed and you’re satisfied with the " +
                    "result? Once you confirm, the payment will be released to the helper.",
            onConfirm = {
                dialog = null
                assignment?.let {
                    SosRepository.completeAsRequester(it.id)
                    onReview(it.id)
                }
            },
            onDismiss = { dialog = null }
        )

        ChatDialog.REQUEST_COMPLETION -> SosConfirmDialog(
            title = "Request complete?",
            message = "$otherName will be asked to confirm that the work is complete. " +
                    "Payment is released after they confirm.",
            onConfirm = {
                assignment?.let { SosRepository.requestCompletion(it.id) }
                dialog = null
            },
            onDismiss = { dialog = null }
        )

        ChatDialog.CONFIRM_CANCEL -> SosConfirmDialog(
            title = "Confirm cancellation?",
            message = if (iAmRequester) "The request will be cancelled and your payment refunded."
            else "The request will be cancelled and the payment refunded to $otherName.",
            onConfirm = {
                assignment?.let { SosRepository.confirmCancellation(it.id) }
                dialog = null
            },
            onDismiss = { dialog = null }
        )

        ChatDialog.REPORT -> CancellationReasonDialog(
            title = "Report $otherName",
            confirmText = "Report",
            reasons = ReportUserReasons,
            onConfirm = {
                SosRepository.reportUser(otherId, it)
                dialog = null
                android.widget.Toast.makeText(context, "Thanks, our admin team will review it.", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDismiss = { dialog = null }
        )

        ChatDialog.DISPUTE -> CancellationReasonDialog(
            title = "Raise a dispute",
            confirmText = "Submit",
            reasons = DisputeReasons,
            onConfirm = { reason ->
                assignment?.let { SosRepository.raiseDispute(it.id, reason) }
                dialog = null
                android.widget.Toast.makeText(context, "Dispute submitted to SOS admin.", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDismiss = { dialog = null }
        )

        null -> Unit
    }
}

@Composable
private fun ChatActions(
    conversation: Conversation,
    request: Request,
    assignment: HelpAssignment?,
    iAmRequester: Boolean,
    otherName: String,
    onDialog: (ChatDialog) -> Unit,
    onAccept: () -> Unit,
    onReview: () -> Unit
) {
    val me = SosRepository.CURRENT_USER_ID
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        when {
            conversation.declined -> Banner("You declined help from $otherName.")

            assignment == null || assignment.status == AssignmentStatus.PENDING_AGREEMENT -> {
                if (request.status != RequestStatus.OPEN) {
                    Banner("This request is no longer open.")
                } else if (iAmRequester) {
                    ActionButtons("Decline help", { onDialog(ChatDialog.DECLINE) }, "Accept help", onAccept)
                } else {
                    Banner("Waiting for $otherName to accept your help.")
                }
            }

            assignment.status == AssignmentStatus.ON_PROGRESS -> ActionButtons(
                "Cancel request", { onDialog(ChatDialog.CANCEL) },
                "Request complete", {
                    onDialog(if (iAmRequester) ChatDialog.COMPLETE else ChatDialog.REQUEST_COMPLETION)
                }
            )

            assignment.status == AssignmentStatus.COMPLETION_REQUESTED -> {
                if (assignment.completionRequestedBy == me) {
                    Banner("Completion requested. Waiting for $otherName to confirm.")
                } else {
                    Banner("$otherName marked the work as complete. Please check the result.")
                    Spacer(Modifier.height(6.dp))
                    SosPrimaryButton("Confirm complete", { onDialog(ChatDialog.COMPLETE) }, Modifier.height(40.dp))
                }
            }

            assignment.status == AssignmentStatus.CANCELLATION_REQUESTED -> {
                if (assignment.cancellationRequestedBy == me) {
                    Banner("Cancellation requested. Waiting for $otherName to confirm.")
                } else {
                    Banner("$otherName wants to cancel: ${assignment.cancellationReason}")
                    Spacer(Modifier.height(6.dp))
                    ActionButtons(
                        "Decline", { SosRepository.rejectCancellation(assignment.id) },
                        "Confirm cancel", { onDialog(ChatDialog.CONFIRM_CANCEL) }
                    )
                }
            }

            assignment.status == AssignmentStatus.COMPLETED -> {
                Banner("Request complete")
                if (iAmRequester && assignment.rating == null) {
                    Spacer(Modifier.height(6.dp))
                    SosPrimaryButton("Leave a review", onReview, Modifier.height(40.dp))
                } else if (assignment.rating != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (iAmRequester) "Your rating  " else "$otherName rated you  ",
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        StarRating(assignment.rating, size = 14.dp)
                    }
                }
            }

            else -> Banner("Request cancelled")
        }
    }
}

@Composable
private fun ActionButtons(
    secondary: String,
    onSecondary: () -> Unit,
    primary: String,
    onPrimary: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onSecondary,
            modifier = Modifier.weight(1f).height(36.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, SosBlue),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = SosLightBlue, contentColor = SosBlue)
        ) { Text(secondary, fontSize = 13.sp) }
        Button(
            onClick = onPrimary,
            modifier = Modifier.weight(1f).height(36.dp),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SosBlue)
        ) { Text(primary, fontSize = 13.sp) }
    }
}

@Composable
private fun Banner(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = SosBlue,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(SosLightBlue, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageItem(
    message: Message,
    request: Request,
    canEditRequest: Boolean,
    onEditRequest: () -> Unit,
    onAddAttachment: () -> Unit
) {
    val mine = message.senderId == SosRepository.CURRENT_USER_ID

    if (message.type == MessageType.SYSTEM) {
        Text(
            text = message.text,
            fontSize = 11.sp,
            color = SosGray,
            textAlign = if (message.text == "Request has been sent.") TextAlign.End else TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    var showMenu by remember { mutableStateOf(false) }
    val isRequestCard = message.type == MessageType.REQUEST

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .then(
                        if (mine) Modifier.background(SosLightBlue, RoundedCornerShape(12.dp))
                        else Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    )
                    .then(
                        if (isRequestCard && canEditRequest) {
                            Modifier.combinedClickable(onClick = {}, onLongClick = { showMenu = true })
                        } else Modifier
                    )
                    .padding(12.dp)
            ) {
                when (message.type) {
                    MessageType.REQUEST -> {
                        Text(request.description, fontSize = 13.sp, color = Color.Black)
                        request.attachmentName?.let {
                            Spacer(Modifier.height(8.dp))
                            FileChip(it)
                        }
                    }
                    MessageType.FILE -> FileChip(message.attachmentName ?: "attachment")
                    else -> Text(message.text, fontSize = 13.sp, color = Color.Black)
                }
                Text(
                    text = formatClock(message.sentAt),
                    fontSize = 10.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }

            // Menu tekan lama pada kartu request (Frame 313).
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = SosBlue,
                shape = RoundedCornerShape(8.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Add attachment", color = Color.White, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.AttachFile, null, tint = Color.White) },
                    onClick = { showMenu = false; onAddAttachment() }
                )
                DropdownMenuItem(
                    text = { Text("Edit request", color = Color.White, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Outlined.EditNote, null, tint = Color.White) },
                    onClick = { showMenu = false; onEditRequest() }
                )
            }
        }
    }
    if (isRequestCard && canEditRequest && mine) {
        Text(
            "Hold the request card to edit it",
            fontSize = 10.sp,
            color = SosGray,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
        )
    }
}

@Composable
private fun FileChip(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F1F1), RoundedCornerShape(4.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (name.endsWith(".pdf")) Icons.Outlined.Description else Icons.Outlined.InsertDriveFile,
            null,
            Modifier.size(16.dp),
            tint = Color.Black
        )
        Spacer(Modifier.width(8.dp))
        Text(name, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
private fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
            cursorBrush = SolidColor(SosBlue),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSend() }),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F1F1), RoundedCornerShape(50))
                        .border(1.dp, Color.Gray, RoundedCornerShape(50))
                        .padding(start = 14.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f).padding(vertical = 8.dp)) {
                        if (value.isEmpty()) Text("Message", fontSize = 13.sp, color = Color.Gray)
                        inner()
                    }
                    IconButton(onClick = onAttach, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.AttachFile, "Attach file", Modifier.size(18.dp), tint = Color.DarkGray)
                    }
                }
            }
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank(),
            modifier = Modifier
                .size(36.dp)
                .background(SosBlue, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "Send", Modifier.size(18.dp), tint = Color.White)
        }
    }
}
