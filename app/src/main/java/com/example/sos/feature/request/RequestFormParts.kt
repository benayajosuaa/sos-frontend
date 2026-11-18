package com.example.sos.feature.request

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.model.EstimatedTime
import com.example.sos.ui.components.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Pilihan deadline cepat seperti di desain. */
enum class DeadlineOption(val label: String, val days: Long?) {
    TODAY("Today", 0),
    TOMORROW("Tomorrow", 1),
    THREE_DAYS("3 days", 3),
    ONE_WEEK("1 week", 7),
    CUSTOM("Custom date", null)
}

@Composable
fun FormLabel(title: String, subtitle: String? = null) {
    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    subtitle?.let { Text(it, fontSize = 11.sp, color = Color.Black) }
    Spacer(Modifier.height(6.dp))
}

@Composable
fun EstimatedTimeDropdown(
    value: EstimatedTime,
    onValueChange: (EstimatedTime) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(6.dp),
            color = SosLightBlue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bolt, null, Modifier.size(14.dp), tint = Color.Black)
                Spacer(Modifier.width(4.dp))
                Text(value.label, fontSize = 13.sp, color = Color.Black, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Black)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EstimatedTime.entries.forEach {
                DropdownMenuItem(
                    text = { Text(it.label) },
                    onClick = { onValueChange(it); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeadlineSelector(
    option: DeadlineOption?,
    customDate: LocalDate?,
    onSelect: (DeadlineOption, LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DeadlineOption.entries.forEach { item ->
            val label = if (item == DeadlineOption.CUSTOM && option == item && customDate != null) {
                formatDate(customDate)
            } else item.label
            SosChoiceChip(
                text = label,
                selected = option == item,
                onClick = {
                    val days = item.days
                    if (days == null) showPicker = true
                    else onSelect(item, LocalDate.now().plusDays(days))
                }
            )
        }
    }

    if (showPicker) {
        val todayMillis = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = todayMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        onSelect(DeadlineOption.CUSTOM, date)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

/** Tombol "+ Add Attachment" yang membuka pemilih file. */
@Composable
fun AttachmentButton(
    attachmentUri: String?,
    attachmentName: String? = null,
    onPicked: (uri: String, name: String) -> Unit
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onPicked(it.toString(), fileNameFromUri(context, it)) }
    }
    Surface(
        onClick = { picker.launch("*/*") },
        shape = RoundedCornerShape(6.dp),
        color = SosLightBlue,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = when {
                attachmentName != null -> "📎 $attachmentName · Change"
                attachmentUri != null -> "Attachment added · Change"
                else -> "+ Add Attachment"
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun BudgetStepper(
    budget: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    SosStepper(
        text = formatIdr(budget),
        onIncrement = { onChange(budget + BUDGET_STEP) },
        onDecrement = { onChange((budget - BUDGET_STEP).coerceAtLeast(BUDGET_STEP)) },
        modifier = modifier
    )
}

const val BUDGET_STEP = 5_000L

/** Kotak keterangan kecil di bawah form. */
@Composable
fun FormHint(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = SosGray,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA), RoundedCornerShape(6.dp))
            .padding(10.dp)
    )
}
