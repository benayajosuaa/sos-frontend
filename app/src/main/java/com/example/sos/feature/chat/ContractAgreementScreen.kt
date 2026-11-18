package com.example.sos.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.sos.data.AcceptResult
import com.example.sos.data.SosRepository
import com.example.sos.feature.request.MissingContent
import com.example.sos.ui.components.*

/**
 * Contract Agreement sebelum pekerjaan dimulai.
 * Agree & Continue = setuju kontrak + dana ditahan dari balance (simulasi).
 */
@Composable
fun ContractAgreementScreen(
    requestId: String,
    helperId: String,
    onBack: () -> Unit,
    onAgreed: (conversationId: String) -> Unit,
    onTopUp: () -> Unit
) {
    val request = SosRepository.request(requestId)
    val helper = SosRepository.user(helperId)
    if (request == null || helper == null) {
        MissingContent(onBack)
        return
    }

    val price = request.budget / request.helpersNeeded
    var agreed by remember { mutableStateOf(false) }
    var insufficient by remember { mutableStateOf<AcceptResult.InsufficientBalance?>(null) }
    var notAllowed by remember { mutableStateOf(false) }

    val sections = listOf(
        "Scope of Work" to "The helper agrees to complete the task as described in the request " +
                "details provided by the requester: “${request.title}”.",
        "Timeline" to "The helper will deliver the completed work within the agreed estimated " +
                "time (${request.estimatedTime.label}) and before the deadline " +
                "(${formatDate(request.deadline)}${request.deadlineTime?.let { ", " + formatTime(it) } ?: ""}).",
        "Payment" to "The requester agrees to pay ${formatIdr(price)} upon successful completion of the " +
                "task. Payment is held by SOS and released to the helper after the requester confirms completion.",
        "Quality Standards" to "The helper agrees to deliver work that meets reasonable quality " +
                "standards. The requester may request revisions if the delivered work does not match " +
                "the agreed scope.",
        "Cancellation Policy" to "Either party may request to cancel the agreement; the other party must " +
                "confirm. Once work has started, cancellation is subject to a partial payment based on " +
                "work completed.",
        "Confidentiality" to "Both parties agree to keep all shared materials and communications confidential.",
        "Dispute Resolution" to "Any disputes will be mediated through the platform’s resolution center (Admin)."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosTopBar(title = "Contract Agreement", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Ringkasan nilai kontrak.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SosBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(32.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Helper", fontSize = 11.sp, color = SosGray)
                    Text(helper.fullName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Payment", fontSize = 11.sp, color = SosGray)
                    Text(formatIdr(price), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SosBlue)
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SosLightBlue, RoundedCornerShape(10.dp))
                    .border(1.dp, SosBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Text("Help Service Agreement", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SosBlue)
                Spacer(Modifier.height(4.dp))
                Text(
                    "By accepting this request, both parties agree to the following terms:",
                    fontSize = 12.sp,
                    color = SosGray
                )
                sections.forEachIndexed { index, (title, body) ->
                    Spacer(Modifier.height(10.dp))
                    Text("${index + 1}. $title", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SosBlue)
                    Text(body, fontSize = 12.sp, color = SosGray)
                }
            }
        }

        HorizontalDivider(color = SosBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { agreed = !agreed }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreed,
                onCheckedChange = { agreed = it },
                colors = CheckboxDefaults.colors(checkedColor = SosBlue)
            )
            Text("I have read and agree to the terms and conditions", fontSize = 12.sp, color = Color.Black)
        }
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, SosBlue),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SosLightBlue, contentColor = SosBlue)
            ) { Text("Decline") }
            Button(
                onClick = {
                    when (val result = SosRepository.acceptHelp(requestId, helperId)) {
                        is AcceptResult.Success ->
                            SosRepository.openConversation(requestId, helperId)?.let(onAgreed)
                        is AcceptResult.InsufficientBalance -> insufficient = result
                        AcceptResult.NotAllowed -> notAllowed = true
                    }
                },
                enabled = agreed,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = SosBlue)
            ) { Text("Agree & Continue") }
        }
    }

    insufficient?.let { info ->
        SosConfirmDialog(
            title = "Insufficient balance",
            message = "You need ${formatIdr(info.needed)} but your balance is ${formatIdr(info.balance)}. " +
                    "Top up your balance to continue.",
            confirmText = "Top Up",
            onConfirm = { insufficient = null; onTopUp() },
            onDismiss = { insufficient = null }
        )
    }

    if (notAllowed) {
        SosConfirmDialog(
            title = "Can’t accept",
            message = "This request already has enough helpers or is no longer open.",
            confirmText = "OK",
            onConfirm = { notAllowed = false; onBack() },
            onDismiss = { notAllowed = false }
        )
    }
}
