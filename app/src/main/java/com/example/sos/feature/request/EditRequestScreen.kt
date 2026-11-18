package com.example.sos.feature.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.ui.components.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/** Edit Request: deskripsi, lampiran, deadline (tanggal + jam), budget. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRequestScreen(
    requestId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val request = SosRepository.request(requestId)
    if (request == null) {
        MissingContent(onBack)
        return
    }
    // Budget terkunci setelah ada agreement yang berjalan.
    val budgetLocked = SosRepository.assignmentsFor(requestId).any { it.status.isActive }

    var description by rememberSaveable { mutableStateOf(request.description) }
    var attachmentUri by rememberSaveable { mutableStateOf(request.attachmentUri) }
    var attachmentName by rememberSaveable { mutableStateOf(request.attachmentName) }
    var date by rememberSaveable { mutableStateOf(request.deadline) }
    var time by rememberSaveable { mutableStateOf(request.deadlineTime ?: LocalTime.of(23, 59)) }
    var budget by rememberSaveable { mutableLongStateOf(request.budget) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        SosTopBar(title = "Edit Request", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
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
            FormLabel("Deadline")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = { showDatePicker = true }, color = Color.Transparent) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(18.dp), tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(formatShortDate(date), fontSize = 13.sp, color = Color.Black)
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(onClick = { showTimePicker = true }, color = Color.Transparent) {
                    Text(
                        formatTime(time),
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                    )
                }
            }
            HorizontalDivider(color = SosBorder)

            Spacer(Modifier.height(16.dp))
            FormLabel("Budget")
            if (budgetLocked) {
                Text(formatIdr(budget), fontSize = 13.sp, color = Color.Black)
                Text(
                    "Budget is locked because a helper has already agreed to this price.",
                    fontSize = 11.sp,
                    color = SosGray
                )
            } else {
                BudgetStepper(budget, { budget = it }, Modifier.width(160.dp))
            }
        }

        SosPrimaryButton(
            text = "Save Changes",
            enabled = description.isNotBlank(),
            onClick = {
                SosRepository.editRequest(requestId, description, attachmentUri, attachmentName, date, time, budget)
                onSaved()
            },
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }

    if (showDatePicker) {
        val todayMillis = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = SosBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = SosBlue) }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Color.White,
            title = { Text("Enter time", fontSize = 13.sp, color = SosBlue) },
            text = { TimeInput(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(state.hour, state.minute)
                    showTimePicker = false
                }) { Text("OK", color = SosBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = SosBlue) }
            }
        )
    }
}
