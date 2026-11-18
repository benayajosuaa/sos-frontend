package com.example.sos.feature.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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

/** Post Request / Create Request. Setelah dipost, request tampil di Explore. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRequestScreen(
    onBack: () -> Unit,
    onPosted: (requestId: String) -> Unit
) {
    var subjectQuery by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var attachmentUri by rememberSaveable { mutableStateOf<String?>(null) }
    var attachmentName by rememberSaveable { mutableStateOf<String?>(null) }
    var estimatedTime by rememberSaveable { mutableStateOf(EstimatedTime.FAST) }
    var deadlineOption by rememberSaveable { mutableStateOf<DeadlineOption?>(null) }
    var deadline by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var budget by rememberSaveable { mutableLongStateOf(50_000L) }
    var helpersNeeded by rememberSaveable { mutableIntStateOf(1) }

    val subjectSuggestions = SosRepository.subjects.filter {
        it != subject && it.contains(subjectQuery.trim(), ignoreCase = true)
    }

    val canPost = subject != null &&
            title.isNotBlank() &&
            description.isNotBlank() &&
            deadline != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        SosTopBar(title = "Create Request", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            FormLabel("Subject")
            if (subject == null) {
                SosSearchField(
                    value = subjectQuery,
                    onValueChange = { subjectQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjectSuggestions.forEach {
                        SosChoiceChip(text = it, selected = false, onClick = {
                            subject = it
                            subjectQuery = ""
                        })
                    }
                }
            } else {
                Row {
                    SosChoiceChip(text = "$subject  ✕", selected = true, onClick = { subject = null })
                }
            }

            Spacer(Modifier.height(16.dp))
            FormLabel("Title")
            SosTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Need help with mobile UI design"
            )

            Spacer(Modifier.height(16.dp))
            FormLabel("Description")
            SosTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Explain what you need help with",
                singleLine = false,
                minLines = 4
            )

            Spacer(Modifier.height(12.dp))
            AttachmentButton(attachmentUri, attachmentName) { uri, name ->
                attachmentUri = uri
                attachmentName = name
            }

            Spacer(Modifier.height(16.dp))
            FormLabel("Estimated Time", "How long will it take to finish this task?")
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
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    FormLabel("Budget")
                    BudgetStepper(budget, { budget = it })
                }
                Column(Modifier.weight(1f)) {
                    FormLabel("Response")
                    SosStepper(
                        text = "$helpersNeeded",
                        onIncrement = { helpersNeeded = (helpersNeeded + 1).coerceAtMost(20) },
                        onDecrement = { helpersNeeded = (helpersNeeded - 1).coerceAtLeast(1) }
                    )
                }
            }
            Text(
                text = "Response = how many helpers you need for this request.",
                fontSize = 11.sp,
                color = SosGray,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        SosPrimaryButton(
            text = if (SosRepository.isSuspended) "Account suspended" else "Post Request",
            enabled = canPost && !SosRepository.isSuspended,
            onClick = {
                val request = SosRepository.createRequest(
                    subject = subject!!,
                    title = title.trim(),
                    description = description.trim(),
                    estimatedTime = estimatedTime,
                    deadline = deadline!!,
                    budget = budget,
                    helpersNeeded = helpersNeeded,
                    attachmentUri = attachmentUri,
                    attachmentName = attachmentName
                )
                onPosted(request.id)
            },
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}
