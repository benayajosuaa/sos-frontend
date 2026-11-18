package com.example.sos.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** Kerangka pop up sesuai desain: judul di tengah, garis, isi, dua tombol. */
@Composable
fun SosDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    dismissText: String = "Cancel",
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(14.dp), color = Color.White) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray)
                Spacer(Modifier.height(10.dp))
                content()
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, SosBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SosLightBlue,
                            contentColor = SosBlue
                        )
                    ) { Text(dismissText, fontSize = 13.sp) }
                    Button(
                        onClick = onConfirm,
                        enabled = confirmEnabled,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = SosBlue)
                    ) { Text(confirmText, fontSize = 13.sp) }
                }
            }
        }
    }
}

@Composable
fun SosConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Yes"
) {
    SosDialog(title = title, onDismiss = onDismiss, confirmText = confirmText, onConfirm = onConfirm) {
        Text(message, fontSize = 13.sp, color = Color.Black)
    }
}

/** "Select Cancellation Reason" dengan pilihan tunggal dan isian untuk Others. */
@Composable
fun CancellationReasonDialog(
    reasons: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select Cancellation Reason",
    confirmText: String = "Confirm"
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var other by remember { mutableStateOf("") }
    val isOther = selected == reasons.last()
    val finalReason = if (isOther) other.trim() else selected

    SosDialog(
        title = title,
        onDismiss = onDismiss,
        confirmText = confirmText,
        confirmEnabled = !finalReason.isNullOrBlank(),
        onConfirm = { finalReason?.let(onConfirm) }
    ) {
        reasons.forEach { reason ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selected = reason },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selected == reason,
                    onCheckedChange = { selected = reason },
                    colors = CheckboxDefaults.colors(checkedColor = SosBlue),
                    modifier = Modifier.size(36.dp)
                )
                Text(reason, fontSize = 13.sp, color = Color.Black)
            }
        }
        if (isOther) {
            Spacer(Modifier.height(6.dp))
            SosTextField(value = other, onValueChange = { other = it }, placeholder = "Tell us why")
        }
    }
}

val ReportRequestReasons = listOf(
    "Academic dishonesty (exam/assignment cheating)",
    "Not an academic help request",
    "Spam or scam",
    "Others"
)

val ReportUserReasons = listOf(
    "Inappropriate or abusive messages",
    "Asking to pay outside SOS",
    "Fake identity",
    "Others"
)

val DisputeReasons = listOf(
    "Work is incomplete or not as agreed",
    "Other party isn’t responding",
    "Disagree with cancellation",
    "Others"
)

/** Nama file dari Uri hasil pemilih file. */
fun fileNameFromUri(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index)?.let { return it }
            }
        }
    return uri.lastPathSegment ?: "attachment"
}
