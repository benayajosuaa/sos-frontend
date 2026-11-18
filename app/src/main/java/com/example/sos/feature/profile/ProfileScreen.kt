package com.example.sos.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.ui.components.*

/** Tab Profile: info akun, balance, History, Language, Logout. */
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onHistoryClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onTopUpClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onLogout: () -> Unit
) {
    val user = SosRepository.currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                InitialAvatar(user.fullName, 64.dp)
                Spacer(Modifier.height(6.dp))
                Surface(onClick = onEditProfile, shape = RoundedCornerShape(50), color = SosBlue) {
                    Text(
                        "Edit Profile",
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(user.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(user.email, fontSize = 12.sp, color = Color.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, Modifier.size(12.dp), tint = SosStar)
                    Text(" ${user.rating}", fontSize = 11.sp, color = SosGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.School, null, Modifier.size(12.dp), tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${user.major}, ${user.university}",
                        fontSize = 11.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (user.linkedInUrl != null) Icon(Icons.Default.Work, "LinkedIn", Modifier.size(16.dp))
                    if (user.websiteUrl != null) Icon(Icons.Default.Language, "Website", Modifier.size(16.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        BalanceCard(
            onWithdraw = onWithdrawClick,
            onTopUp = onTopUpClick,
            onClick = onTransactionsClick
        )

        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MenuRow(Icons.Outlined.History, "History", onClick = onHistoryClick)
            MenuRow(Icons.Outlined.ReceiptLong, "Transaction History", onClick = onTransactionsClick)
            MenuRow(Icons.Outlined.Language, "Language", trailing = SosRepository.language) {
                showLanguageDialog = true
            }
            MenuRow(Icons.AutoMirrored.Filled.Logout, "Logout", showChevron = false) {
                showLogoutDialog = true
            }
        }
    }

    if (showLogoutDialog) {
        SosConfirmDialog(
            title = "Logout?",
            message = "You’ll need to log in again to use SOS.",
            confirmText = "Logout",
            onConfirm = { showLogoutDialog = false; onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showLanguageDialog) {
        var choice by remember { mutableStateOf(SosRepository.language) }
        SosDialog(
            title = "Language",
            onDismiss = { showLanguageDialog = false },
            confirmText = "Save",
            onConfirm = {
                SosRepository.language = choice
                showLanguageDialog = false
            }
        ) {
            listOf("English", "Bahasa Indonesia").forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { choice = option },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = choice == option,
                        onClick = { choice = option },
                        colors = RadioButtonDefaults.colors(selectedColor = SosBlue)
                    )
                    Text(option, fontSize = 13.sp, color = Color.Black)
                }
            }
            // TODO: Terjemahan Bahasa Indonesia belum tersedia; teks UI masih bahasa Inggris.
            Text(
                "App text is currently available in English only.",
                fontSize = 11.sp,
                color = SosGray
            )
        }
    }
}

/** Kartu saldo: "Your Balance" + Withdraw / Top Up. */
@Composable
fun BalanceCard(
    onWithdraw: () -> Unit,
    onTopUp: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SosLightBlue)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Your Balance", fontSize = 12.sp, color = Color.Black, modifier = Modifier.weight(1f))
            if (onClick != null) {
                Text("See transactions", fontSize = 11.sp, color = SosBlue)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(16.dp), tint = SosBlue)
            }
        }
        Text(
            formatIdr(SosRepository.balance()),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WalletButton(Icons.Outlined.AccountBalanceWallet, "Withdraw", onWithdraw)
            WalletButton(Icons.Outlined.Payments, "Top Up", onTopUp)
        }
    }
}

@Composable
private fun WalletButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(8.dp), color = SosBlue) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(14.dp), tint = Color.White)
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    trailing: String? = null,
    showChevron: Boolean = trailing == null,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, shape = RoundedCornerShape(10.dp), color = Color(0xFFEFEFEF)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(20.dp), tint = Color.Black)
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, color = Color.Black, modifier = Modifier.weight(1f))
            trailing?.let { Text(it, fontSize = 12.sp, color = Color.Black) }
            if (showChevron) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = SosGray)
            }
        }
    }
}

/** Avatar huruf awal dengan gradasi biru-ungu seperti di desain. */
@Composable
fun InitialAvatar(name: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF3B5BDB), Color(0xFF9B5DE5)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
