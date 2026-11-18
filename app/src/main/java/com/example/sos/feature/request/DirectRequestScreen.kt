package com.example.sos.feature.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.EstimatedTime
import com.example.sos.ui.components.*
import java.time.LocalDate

/**
 * "Chat helper" landing: Add Request Details.
 * Membuat Direct Request ke satu Helper lalu membuka chat dengannya.
 */
@Composable
fun DirectRequestScreen(
    helperId: String,
    onBack: () -> Unit,
    onSent: (conversationId: String) -> Unit
) {
    val helper = SosRepository.user(helperId)
    if (helper == null) {
        MissingContent(onBack)
        return
    }

    var details by rememberSaveable { mutableStateOf("") }
    var attachmentUri by rememberSaveable { mutableStateOf<String?>(null) }
    var attachmentName by rememberSaveable { mutableStateOf<String?>(null) }
    var estimatedTime by rememberSaveable { mutableStateOf(EstimatedTime.FAST) }
    var deadlineOption by rememberSaveable { mutableStateOf<DeadlineOption?>(null) }
    var deadline by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var budget by rememberSaveable { mutableLongStateOf(50_000L) }

    val canSend = details.isNotBlank() && deadline != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        SosTopBar(title = "Add Request Details", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Sending a direct request to ${helper.fullName}",
                fontSize = 12.sp,
                color = SosGray
            )
            Spacer(Modifier.height(12.dp))

            FormLabel("Request details")
            SosTextField(
                value = details,
                onValueChange = { details = it },
                placeholder = "Input text",
                singleLine = false,
                minLines = 3
            )
            Spacer(Modifier.height(12.dp))
            AttachmentButton(attachmentUri, attachmentName) { uri, name ->
                attachmentUri = uri
                attachmentName = name
            }

            Spacer(Modifier.height(16.dp))
            FormLabel("Estimated time")
            EstimatedTimeDropdown(estimatedTime) { estimatedTime = it }

            Spacer(Modifier.height(16.dp))
            FormLabel("Deadline")
            DeadlineSelector(
                option = deadlineOption,
                customDate = deadline,
                onSelect = { option, date ->
                    deadlineOption = option
                    deadline = date
                }
            )

            Spacer(Modifier.height(16.dp))
            FormLabel("Budget")
            BudgetStepper(budget, { budget = it }, Modifier.width(160.dp))
        }

        SosPrimaryButton(
            text = "Send",
            enabled = canSend,
            onClick = {
                val conversationId = SosRepository.sendDirectRequest(
                    helperId = helperId,
                    details = details,
                    estimatedTime = estimatedTime,
                    deadline = deadline!!,
                    budget = budget,
                    attachmentUri = attachmentUri,
                    attachmentName = attachmentName
                )
                onSent(conversationId)
            },
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}
