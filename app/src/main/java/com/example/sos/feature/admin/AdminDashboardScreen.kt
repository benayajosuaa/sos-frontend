package com.example.sos.feature.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.AdminRepository
import com.example.sos.data.SosRepository
import com.example.sos.data.model.*
import com.example.sos.ui.components.*
import java.time.LocalDate
import java.time.LocalTime

/** Navigasi antar halaman Admin. id/query kosong = tanpa pilihan awal. */
class AdminActions(
    val back: () -> Unit,
    val logout: () -> Unit,
    val notifications: () -> Unit,
    val verification: (String?) -> Unit,
    val users: (id: String?, query: String?) -> Unit,
    val moderation: (String?) -> Unit,
    val cases: (String?) -> Unit,
    val transactions: (String?) -> Unit
)

@Composable
fun AdminDashboardScreen(actions: AdminActions) {
    val context = LocalContext.current
    val adminFirstName = userName(SosRepository.ADMIN_ID).substringBefore(" ")
    val greeting = when (LocalTime.now().hour) {
        in 4..10 -> "Selamat pagi"
        in 11..14 -> "Selamat siang"
        in 15..17 -> "Selamat sore"
        else -> "Selamat malam"
    }

    val users = SosRepository.allUsers()
    val newThisWeek = users.count { it.joinedAt.isAfter(LocalDate.now().minusDays(7)) }
    val verified = users.count { it.isVerified }
    val activeHelpers = users.count { it.skills.isNotEmpty() && it.status == AccountStatus.ACTIVE }
    val suspended = users.count { it.status == AccountStatus.SUSPENDED }
    val ongoing = SosRepository.requests.count {
        !it.isHidden && (it.status == RequestStatus.OPEN || it.status == RequestStatus.IN_PROGRESS)
    }
    val waitingHelper = SosRepository.requests.count { !it.isHidden && it.status == RequestStatus.OPEN }

    val pendingVerifications = AdminRepository.pendingVerifications
    val openReports = AdminRepository.openRequestReports() + AdminRepository.openUserReports()
    val openDisputes = AdminRepository.openDisputes()
    val problemTx = AdminRepository.problemTransactions()

    var search by rememberSaveable { mutableStateOf("") }

    AdminPage(
        title = "Admin dashboard",
        subtitle = "$greeting, $adminFirstName. Pantau verifikasi, laporan, dan transaksi yang perlu ditangani hari ini.",
        onBack = null,
        onLogout = actions.logout,
        onNotifications = actions.notifications,
        headerExtra = {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .background(SosLightBlue, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(14.dp), tint = SosBlue)
                    Spacer(Modifier.width(4.dp))
                    Text(adminDate(LocalDate.now()), fontSize = 12.sp, color = SosBlue)
                }
                Surface(
                    onClick = {
                        // TODO: GET /admin/reports/summary?format=csv
                        Toast.makeText(context, "Ringkasan diekspor (simulasi)", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(50),
                    color = SosBlue
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.FileDownload, null, Modifier.size(14.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Export ringkasan", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            SosSearchField(
                value = search,
                onValueChange = { search = it },
                placeholder = "Search users by name, email, or campus",
                modifier = Modifier.fillMaxWidth()
            )
            if (search.isNotBlank()) {
                LinkText("Cari “${search.trim()}” di Kelola pengguna") { actions.users(null, search.trim()) }
            }
        }
    ) {
        // ---------- Ringkasan ----------
        item { SectionHeader("User summary", "Platform activity · data saat ini") }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SosBlue, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("Total users", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                Text("%,d".format(users.size), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "+$newThisWeek new this week · $suspended suspended",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    "Verified students", "$verified",
                    "${pendingVerifications.size} waiting review", Modifier.weight(1f),
                    noteColor = if (pendingVerifications.isEmpty()) Color(0xFF2E7D32) else Color(0xFFB45309)
                )
                StatTile("Active helpers", "$activeHelpers", "Available to accept requests", Modifier.weight(1f), SosGray)
            }
        }
        item {
            AdminCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$ongoing", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Ongoing requests", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text("$waitingHelper waiting for a helper · ${rupiah(AdminRepository.heldFunds())} held", fontSize = 11.sp, color = SosGray)
                    }
                }
            }
        }

        // ---------- Verifikasi ----------
        item {
            SectionHeader(
                "Student verification",
                "Periksa KTM sebelum akun bisa membuat request atau jadi helper.",
                badge = "${pendingVerifications.size} pending"
            )
        }
        if (pendingVerifications.isEmpty()) item { EmptyNote("Tidak ada antrean verifikasi.") }
        pendingVerifications.take(3).forEach { v ->
            item(key = "v-${v.id}") {
                val user = SosRepository.user(v.userId)
                AdminCard(onClick = { actions.verification(v.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(30.dp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user?.fullName.orEmpty(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(user?.email.orEmpty(), fontSize = 11.sp, color = SosGray)
                        }
                    }
                    Text("${v.university} · NIM ${v.nim}", fontSize = 11.sp, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        when {
                            v.flag != null -> Pill(v.flag, PillKind.RED)
                            v.status == VerificationStatus.IN_REVIEW -> Pill("In review", PillKind.BLUE)
                            else -> Pill("Pending review", PillKind.ORANGE)
                        }
                        Spacer(Modifier.weight(1f))
                        SosSmallButton("Review", onClick = { actions.verification(v.id) })
                    }
                }
            }
        }
        item { LinkText("Lihat semua verifikasi (${pendingVerifications.size})") { actions.verification(null) } }

        // ---------- Laporan ----------
        item { SectionHeader("Reports", "Request dan pengguna yang dilaporkan.", badge = "${openReports.size} open") }
        if (openReports.isEmpty()) item { EmptyNote("Tidak ada laporan terbuka.") }
        openReports.sortedByDescending { it.priority == Priority.HIGH }.take(3).forEach { r ->
            item(key = "r-${r.id}") {
                val title = if (r.target == ReportTarget.REQUEST) SosRepository.request(r.targetId)?.title.orEmpty()
                else "Pengguna: ${userName(r.targetId)}"
                val open = { if (r.target == ReportTarget.REQUEST) actions.moderation(r.id) else actions.cases(r.id) }
                AdminCard(onClick = open) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${r.code} · ${agoId(r.createdAt)}", fontSize = 11.sp, color = SosGray, modifier = Modifier.weight(1f))
                        Pill(r.priority.label, if (r.priority == Priority.HIGH) PillKind.RED else PillKind.GRAY)
                    }
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
                    Text("${r.reason} · ${r.reportCount} laporan", fontSize = 11.sp, color = SosGray)
                    Row(Modifier.padding(top = 6.dp)) {
                        Spacer(Modifier.weight(1f))
                        SosSmallButton("Review", onClick = open)
                    }
                }
            }
        }
        item { LinkText("Lihat semua laporan") { actions.moderation(null) } }

        // ---------- Dispute ----------
        item { SectionHeader("Disputes", "Kasus yang butuh keputusan Admin soal pembayaran.", badge = "${openDisputes.size} open") }
        if (openDisputes.isEmpty()) item { EmptyNote("Tidak ada dispute terbuka.") }
        openDisputes.take(3).forEach { d ->
            item(key = "d-${d.id}") {
                val a = SosRepository.assignment(d.assignmentId)
                val request = a?.let { SosRepository.request(it.requestId) }
                AdminCard(onClick = { actions.cases(d.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${d.code} · ${agoId(d.createdAt)}", fontSize = 11.sp, color = SosGray, modifier = Modifier.weight(1f))
                        Pill(d.status.label, if (d.status == CaseStatus.OPEN) PillKind.ORANGE else PillKind.BLUE)
                    }
                    Text(request?.title.orEmpty(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
                    Text(
                        "${a?.let { userName(it.requesterId) }} → ${a?.let { userName(it.helperId) }} · ${d.reason}",
                        fontSize = 11.sp,
                        color = SosGray
                    )
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(rupiah(a?.agreedPrice ?: 0), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SosBlue)
                        Spacer(Modifier.weight(1f))
                        SosSmallButton("View case", onClick = { actions.cases(d.id) })
                    }
                }
            }
        }
        item { LinkText("Lihat semua kasus") { actions.cases(null) } }

        // ---------- Transaksi ----------
        item { SectionHeader("Problematic transactions", "Pembayaran, refund, dan pencairan yang perlu dicek.", badge = "${problemTx.size}") }
        if (problemTx.isEmpty()) item { EmptyNote("Semua transaksi normal.") }
        problemTx.take(3).forEach { t ->
            item(key = "t-${t.id}") {
                AdminCard(onClick = { actions.transactions(t.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${t.type.label} · ${userName(t.userId)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("${t.code} · ${agoId(t.createdAt)}", fontSize = 11.sp, color = SosGray)
                        }
                        Text(rupiah(t.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Pill(t.status.label, txPill(t.status))
                        Spacer(Modifier.weight(1f))
                        SosSmallButton("Check", onClick = { actions.transactions(t.id) })
                    }
                }
            }
        }
        item { LinkText("Buka monitoring transaksi") { actions.transactions(null) } }

        item {
            AdminCard {
                Text("Kelola pengguna", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("Lihat profil, status, dan tangguhkan atau aktifkan kembali akun.", fontSize = 12.sp, color = SosGray)
                LinkText("Buka kelola pengguna") { actions.users(null, null) }
            }
        }
    }
}

fun txPill(status: PlatformTxStatus) = when (status) {
    PlatformTxStatus.SUCCESS -> PillKind.GREEN
    PlatformTxStatus.FAILED -> PillKind.RED
    PlatformTxStatus.PENDING -> PillKind.ORANGE
    PlatformTxStatus.NEEDS_FIX -> PillKind.BLUE
}

@Composable
fun EmptyNote(text: String) {
    Text(text, fontSize = 12.sp, color = SosGray)
}

/** Pop up daftar item yang menunggu (dari ikon lonceng). */
@Composable
fun AdminNotificationsDialog(actions: AdminActions, onDismiss: () -> Unit) {
    val rows = listOf(
        Triple("Verifikasi mahasiswa", AdminRepository.pendingVerifications.size) { actions.verification(null) },
        Triple("Request dilaporkan", AdminRepository.openRequestReports().size) { actions.moderation(null) },
        Triple("Laporan pengguna & dispute", AdminRepository.openUserReports().size + AdminRepository.openDisputes().size) { actions.cases(null) },
        Triple("Transaksi bermasalah", AdminRepository.problemTransactions().size) { actions.transactions(null) }
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Perlu tindakan", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                rows.forEach { (label, count, open) ->
                    Surface(onClick = { onDismiss(); open() }, color = Color.Transparent) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(label, fontSize = 14.sp, color = Color.Black, modifier = Modifier.weight(1f))
                            Pill("$count", if (count > 0) PillKind.ORANGE else PillKind.GRAY)
                        }
                    }
                    HorizontalDivider(color = SosBorder)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}
