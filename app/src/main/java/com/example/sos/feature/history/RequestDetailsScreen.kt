package com.example.sos.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.AssignmentStatus
import com.example.sos.data.model.HelpAssignment
import com.example.sos.data.model.Request
import com.example.sos.feature.request.MissingContent
import com.example.sos.ui.components.*

private enum class DetailsDialog { COMPLETE, REQUEST_COMPLETION, CANCEL, CONFIRM_CANCEL }

/**
 * Detail satu Help Assignment.
 * - Saya Requester: kartu Helper (+ review setelah selesai) dan kartu Request.
 * - Saya Helper: kartu Request beserta pemiliknya.
 * Tombol aksi di bawah mengikuti state machine di docs/business-rules.md.
 */
@Composable
fun RequestDetailsScreen(
    assignmentId: String,
    onBack: () -> Unit,
    onSeePost: (String) -> Unit,
    onChatClick: (requestId: String, helperId: String) -> Unit,
    onHelperClick: (String) -> Unit,
    onOpenContract: (requestId: String, helperId: String) -> Unit,
    onReview: (String) -> Unit,
    onCancellationSent: () -> Unit
) {
    val assignment = SosRepository.assignment(assignmentId)
    val request = assignment?.let { SosRepository.request(it.requestId) }
    if (assignment == null || request == null) {
        MissingContent(onBack)
        return
    }

    val iAmRequester = assignment.requesterId == SosRepository.CURRENT_USER_ID
    val otherPartyId = if (iAmRequester) assignment.helperId else assignment.requesterId
    val otherName = SosRepository.user(otherPartyId)?.fullName.orEmpty()
    val openChat = { onChatClick(request.id, assignment.helperId) }
    var dialog by remember { mutableStateOf<DetailsDialog?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosTopBar(title = "Request Details", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatusCard(assignment)

            if (iAmRequester) {
                SosCard {
                    SectionTitle("Helper")
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { onHelperClick(otherPartyId) },
                            color = Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(28.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(otherName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            }
                        }
                        SosSmallButton("Chat", onClick = openChat)
                    }

                    if (assignment.status == AssignmentStatus.COMPLETED) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(Modifier.height(10.dp))
                        Text("Review helper", fontSize = 13.sp, color = Color.Black)
                        Spacer(Modifier.height(6.dp))
                        if (assignment.rating == null) {
                            StarRating(rating = 0, size = 26.dp, onRate = { onReview(assignment.id) })
                            Text("Tap to leave a review.", fontSize = 11.sp, color = SosGray)
                        } else {
                            StarRating(rating = assignment.rating, size = 26.dp)
                            assignment.reviewComment?.let {
                                Spacer(Modifier.height(4.dp))
                                Text("“$it”", fontSize = 12.sp, color = SosGray)
                            }
                        }
                    }
                }
            }

            RequestSummaryCard(
                request = request,
                showRequester = !iAmRequester,
                onSeePost = { onSeePost(request.id) },
                onChatClick = openChat
            )

            ActionSection(
                assignment = assignment,
                iAmRequester = iAmRequester,
                otherName = otherName,
                onDialog = { dialog = it },
                onOpenContract = { onOpenContract(request.id, assignment.helperId) }
            )
        }
    }

    when (dialog) {
        DetailsDialog.COMPLETE -> SosConfirmDialog(
            title = "Confirmation",
            message = "Are you sure the work has been completed and you’re satisfied with the " +
                    "result? Once you confirm, the payment will be released to the helper.",
            onConfirm = {
                dialog = null
                SosRepository.completeAsRequester(assignment.id)
                onReview(assignment.id)
            },
            onDismiss = { dialog = null }
        )
        DetailsDialog.REQUEST_COMPLETION -> SosConfirmDialog(
            title = "Request complete?",
            message = "$otherName will be asked to confirm that the work is complete.",
            onConfirm = { SosRepository.requestCompletion(assignment.id); dialog = null },
            onDismiss = { dialog = null }
        )
        DetailsDialog.CANCEL -> CancellationReasonDialog(
            reasons = if (iAmRequester) SosRepository.cancellationReasonsForRequester
            else SosRepository.cancellationReasonsForHelper,
            onConfirm = {
                SosRepository.requestCancellation(assignment.id, it)
                dialog = null
                onCancellationSent()
            },
            onDismiss = { dialog = null }
        )
        DetailsDialog.CONFIRM_CANCEL -> SosConfirmDialog(
            title = "Confirm cancellation?",
            message = "The request will be cancelled and the payment refunded to the requester.",
            onConfirm = { SosRepository.confirmCancellation(assignment.id); dialog = null },
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}

@Composable
private fun StatusCard(assignment: HelpAssignment) {
    val title = when (assignment.status) {
        AssignmentStatus.COMPLETED -> "Request Complete"
        else -> "Request ${assignment.status.label}"
    }
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .background(SosBlue)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DateLine("Request made", formatDate(assignment.createdAt))
            assignment.acceptedAt?.let { DateLine("Request accepted", formatDate(it)) }
            DateLine("Deadline", formatDate(assignment.deadline))
            assignment.completedAt?.let { DateLine("Completed", formatDateTime(it)) }
            assignment.cancelledAt?.let { DateLine("Cancelled", formatDateTime(it)) }
            assignment.cancellationReason?.let { DateLine("Cancellation reason", it) }
        }
    }
}

@Composable
private fun DateLine(label: String, value: String) {
    Text("$label: $value", fontSize = 13.sp, color = Color.Black)
}

@Composable
private fun RequestSummaryCard(
    request: Request,
    showRequester: Boolean,
    onSeePost: () -> Unit,
    onChatClick: () -> Unit
) {
    SosCard {
        SectionTitle("Request")
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = Color.LightGray)
        Spacer(Modifier.height(10.dp))
        if (showRequester) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(28.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    SosRepository.user(request.requesterId)?.fullName.orEmpty(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            Spacer(Modifier.height(6.dp))
        }
        Text(request.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        RequestMetaGrid(request)
        Spacer(Modifier.height(8.dp))
        Text("Tags", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            TagRow(request.tags, Modifier.weight(1f))
            if (showRequester) SosSmallButton("Chat", onClick = onChatClick)
            else SosSmallButton("See post", onClick = onSeePost)
        }
    }
}

/** Aksi sesuai status assignment (sama dengan tombol di chat). */
@Composable
private fun ActionSection(
    assignment: HelpAssignment,
    iAmRequester: Boolean,
    otherName: String,
    onDialog: (DetailsDialog) -> Unit,
    onOpenContract: () -> Unit
) {
    val me = SosRepository.CURRENT_USER_ID
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (assignment.status) {
            AssignmentStatus.PENDING_AGREEMENT -> {
                InfoNote("Work starts after the contract agreement is approved and the payment is funded.")
                if (iAmRequester) SosPrimaryButton("Review contract agreement", onOpenContract)
                else InfoNote("Waiting for $otherName to approve and pay.")
            }

            AssignmentStatus.ON_PROGRESS -> {
                SosPrimaryButton(
                    "Request complete",
                    { onDialog(if (iAmRequester) DetailsDialog.COMPLETE else DetailsDialog.REQUEST_COMPLETION) }
                )
                SosOutlinedButton("Cancel request", { onDialog(DetailsDialog.CANCEL) })
            }

            AssignmentStatus.COMPLETION_REQUESTED -> {
                if (assignment.completionRequestedBy == me) {
                    InfoNote("Completion requested. Waiting for $otherName to confirm.")
                } else {
                    InfoNote("$otherName marked this task as complete.")
                    SosPrimaryButton("Confirm complete", { onDialog(DetailsDialog.COMPLETE) })
                }
            }

            AssignmentStatus.CANCELLATION_REQUESTED -> {
                if (assignment.cancellationRequestedBy == me) {
                    InfoNote("Cancellation requested. Waiting for $otherName to confirm.")
                } else {
                    InfoNote("$otherName wants to cancel: ${assignment.cancellationReason}")
                    SosPrimaryButton("Confirm cancellation", { onDialog(DetailsDialog.CONFIRM_CANCEL) })
                    SosOutlinedButton("Decline", { SosRepository.rejectCancellation(assignment.id) })
                }
            }

            AssignmentStatus.COMPLETED, AssignmentStatus.CANCELLED -> Unit
        }
    }
}

@Composable
private fun InfoNote(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = SosBlue,
        modifier = Modifier
            .fillMaxWidth()
            .background(SosLightBlue, RoundedCornerShape(8.dp))
            .padding(12.dp)
    )
}
