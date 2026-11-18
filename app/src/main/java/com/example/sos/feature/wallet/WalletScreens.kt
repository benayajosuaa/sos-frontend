package com.example.sos.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.Transaction
import com.example.sos.feature.profile.BalanceCard
import com.example.sos.ui.components.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private val GreenAmount = Color(0xFF2E9E4F)
private val RedAmount = Color(0xFFE53935)

/** Top Up: pilih metode, lalu isi nominal (simulasi). */
@Composable
fun TopUpScreen(
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    var method by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosTopBar(title = "Top Up", onBack = onBack)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MethodRow(Icons.Outlined.AccountBalance, "Bank Transfer") { method = "Bank Transfer" }
            MethodRow(Icons.Outlined.CreditCard, "Debit Card") { method = "Debit Card" }
            Text(
                "Payments are processed securely by our payment partner. SOS never stores your card or bank details.",
                fontSize = 11.sp,
                color = SosGray,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }

    method?.let { selected ->
        AmountDialog(
            title = "Top up via $selected",
            confirmText = "Top Up",
            quickAmounts = listOf(50_000L, 100_000L, 250_000L),
            validate = { if (it < 10_000) "Minimum top up is ${formatIdr(10_000)}" else null },
            onConfirm = { amount ->
                // TODO: Arahkan ke halaman pembayaran provider; saldo bertambah dari webhook.
                SosRepository.topUp(amount, selected)
                method = null
                onDone()
            },
            onDismiss = { method = null }
        )
    }
}

@Composable
private fun MethodRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(10.dp), color = Color(0xFFEFEFEF)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(20.dp), tint = Color.Black)
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, color = Color.Black)
        }
    }
}

/** Withdraw dari balance ke rekening terdaftar (simulasi). */
@Composable
fun WithdrawDialog(onDismiss: () -> Unit, onDone: () -> Unit) {
    val balance = SosRepository.balance()
    AmountDialog(
        title = "Withdraw",
        subtitle = "Available: ${formatIdr(balance)}",
        confirmText = "Withdraw",
        quickAmounts = listOf(50_000L, 100_000L, balance).filter { it in SosRepository.MIN_WITHDRAW..balance }.distinct(),
        validate = {
            when {
                it < SosRepository.MIN_WITHDRAW -> "Minimum withdraw is ${formatIdr(SosRepository.MIN_WITHDRAW)}"
                it > balance -> "Amount exceeds your balance"
                else -> null
            }
        },
        onConfirm = { amount ->
            // TODO: Payout lewat payment provider ke rekening yang terverifikasi.
            if (SosRepository.withdraw(amount)) onDone()
        },
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountDialog(
    title: String,
    confirmText: String,
    quickAmounts: List<Long>,
    validate: (Long) -> String?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    subtitle: String? = null
) {
    var text by remember { mutableStateOf("") }
    val amount = text.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val error = if (text.isEmpty()) null else validate(amount)

    SosDialog(
        title = title,
        onDismiss = onDismiss,
        confirmText = confirmText,
        confirmEnabled = text.isNotEmpty() && error == null,
        onConfirm = { onConfirm(amount) }
    ) {
        subtitle?.let {
            Text(it, fontSize = 12.sp, color = SosGray)
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = text,
            onValueChange = { input -> text = input.filter { it.isDigit() }.take(9) },
            prefix = { Text("IDR ") },
            placeholder = { Text("0") },
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SosLightBlue,
                unfocusedContainerColor = SosLightBlue,
                focusedBorderColor = SosBlue,
                unfocusedBorderColor = SosBorder
            )
        )
        error?.let { Text(it, fontSize = 11.sp, color = RedAmount) }
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickAmounts.forEach { value ->
                SosChoiceChip(
                    text = formatIdr(value),
                    selected = amount == value,
                    onClick = { text = value.toString() }
                )
            }
        }
    }
}

/** Transaction History: saldo + daftar mutasi (+ hijau, − merah). */
@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    onTopUp: () -> Unit
) {
    var showWithdraw by remember { mutableStateOf(false) }
    val transactions = SosRepository.myTransactions()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosTopBar(title = "Transaction History", onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(3.dp),
                    colors = CardDefaults.cardColors(containerColor = SosLightBlue)
                ) {
                    BalanceCard(onWithdraw = { showWithdraw = true }, onTopUp = onTopUp)
                }
                Spacer(Modifier.height(6.dp))
            }
            if (transactions.isEmpty()) {
                item { Text("No transactions yet.", fontSize = 13.sp, color = SosGray) }
            }
            items(transactions, key = { it.id }) { TransactionCard(it) }
        }
    }

    if (showWithdraw) {
        WithdrawDialog(onDismiss = { showWithdraw = false }, onDone = { showWithdraw = false })
    }
}

private val transactionDate = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

@Composable
private fun TransactionCard(t: Transaction) {
    SosCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(t.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(
                    "${t.type.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }} • " +
                            t.date.format(transactionDate),
                    fontSize = 10.sp,
                    color = SosGray
                )
            }
            Spacer(Modifier.width(8.dp))
            val sign = if (t.type.isCredit) "+" else "-"
            Text(
                "$sign${"%,d".format(Locale.US, t.amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (t.type.isCredit) GreenAmount else RedAmount
            )
        }
    }
}
