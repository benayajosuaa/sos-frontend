package com.example.sos.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.AdminRepository
import com.example.sos.data.SosRepository
import com.example.sos.data.model.AccountStatus
import com.example.sos.data.model.User
import com.example.sos.ui.components.*

private val SuspendDurations = listOf<Pair<String, Int?>>(
    "3 hari" to 3, "7 hari" to 7, "30 hari" to 30, "Tanpa batas" to null
)

/** Kelola Pengguna: daftar, profil, riwayat, tangguhkan / aktifkan kembali. */
@Composable
fun AdminUsersScreen(initialId: String?, initialQuery: String?, actions: AdminActions) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf(initialQuery.orEmpty()) }
    var selectedId by rememberSaveable { mutableStateOf(initialId) }

    val users = SosRepository.allUsers()
    val active = users.count { it.status == AccountStatus.ACTIVE }
    val suspended = users.size - active
    val list = users.filter {
        when (filter) {
            1 -> it.status == AccountStatus.ACTIVE
            2 -> it.status == AccountStatus.SUSPENDED
            else -> true
        }
    }.filter {
        search.isBlank() || it.fullName.contains(search, true) ||
                it.email.contains(search, true) || it.university.contains(search, true)
    }.sortedWith(compareBy<User> { it.status != AccountStatus.SUSPENDED }.thenBy { it.fullName })
    val selected = selectedId?.let { SosRepository.user(it) }

    AdminPage(
        title = "Kelola Pengguna",
        subtitle = "Kelola profil, akses, dan keamanan akun di lingkungan kampus.",
        onBack = actions.back,
        onLogout = actions.logout,
        onNotifications = actions.notifications,
        headerExtra = {
            Spacer(Modifier.height(10.dp))
            SosSearchField(search, { search = it }, Modifier.fillMaxWidth(), "Cari nama, email, atau kampus")
        }
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SosBlue, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("Total pengguna", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                Text("${users.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("$active aktif · $suspended ditangguhkan", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }
        item { SectionHeader("Daftar pengguna") }
        item { FilterRow(listOf("Semua", "Aktif", "Ditangguhkan"), filter) { filter = it } }
        if (list.isEmpty()) item { EmptyNote("Tidak ada pengguna yang cocok.") }
        list.forEach { user ->
            item(key = user.id) {
                AdminCard(selected = user.id == selectedId, onClick = { selectedId = user.id }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(30.dp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user.fullName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("${roleLabel(user)} · ${user.university}", fontSize = 11.sp, color = SosGray)
                        }
                    }
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Pill(user.status.label, if (user.status == AccountStatus.ACTIVE) PillKind.GREEN else PillKind.RED)
                        if (!user.isVerified) { Spacer(Modifier.width(6.dp)); Pill("Belum terverifikasi", PillKind.ORANGE) }
                        Spacer(Modifier.weight(1f))
                        Text("Lihat profil →", fontSize = 12.sp, color = SosBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item { Text("${list.size} dari ${users.size} pengguna", fontSize = 11.sp, color = SosGray) }

        if (selected != null) {
            item(key = "profile-${selected.id}") { UserProfileSection(selected) }
        } else {
            item { EmptyNote("Pilih pengguna untuk melihat profil.") }
        }
    }
}

fun roleLabel(user: User): String {
    val asHelper = SosRepository.assignments.any { it.helperId == user.id } || user.skills.isNotEmpty()
    val asRequester = SosRepository.requests.any { it.requesterId == user.id }
    return when {
        asHelper && asRequester -> "Requester & Helper"
        asHelper -> "Helper"
        else -> "Requester"
    }
}

@Composable
private fun UserProfileSection(user: User) = Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    var reason by rememberSaveable(user.id) { mutableStateOf("") }
    var duration by remember(user.id) { mutableStateOf(SuspendDurations[1]) }
    var confirm by remember(user.id) { mutableStateOf(false) }
    var durationMenu by remember { mutableStateOf(false) }

    val requests = SosRepository.requests.count { it.requesterId == user.id }
    val reports = AdminRepository.reportsAgainst(user.id)

    AdminCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Profil pengguna", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            Pill(user.status.label, if (user.status == AccountStatus.ACTIVE) PillKind.GREEN else PillKind.RED)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(40.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(user.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(user.email.ifBlank { "-" }, fontSize = 11.sp, color = SosGray)
            }
        }
        Spacer(Modifier.height(8.dp))
        InfoRow("ID pengguna", "USR-${user.id.uppercase().take(6)}")
        InfoRow("Peran", roleLabel(user))
        InfoRow("Kampus", user.university)
        InfoRow("Verifikasi", if (user.isVerified) "Terverifikasi" else "Belum terverifikasi",
            if (user.isVerified) Color(0xFF2E7D32) else Color(0xFFB45309))
        InfoRow("Bergabung", adminDate(user.joinedAt))
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat("${user.helpsCount}", "Bantuan selesai", Modifier.weight(1f))
            MiniStat(if (user.rating > 0) "${user.rating} / 5" else "-", "Rating", Modifier.weight(1f))
            MiniStat("$reports", "Laporan", Modifier.weight(1f))
        }
        Text("$requests request dibuat", fontSize = 11.sp, color = SosGray, modifier = Modifier.padding(top = 6.dp))
    }

    AdminCard {
        Text("Status & riwayat akun", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        user.suspension?.let { s ->
            Spacer(Modifier.height(6.dp))
            Pill(s.until?.let { "Ditangguhkan sampai ${adminDate(it)}" } ?: "Ditangguhkan tanpa batas", PillKind.RED)
            Text("Alasan: ${s.reason}", fontSize = 12.sp, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(8.dp))
        AuditTimeline(AdminRepository.logFor(user.id))
    }

    AdminCard {
        if (user.status == AccountStatus.SUSPENDED) {
            Text("Aktifkan kembali akun", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Pengguna bisa kembali membuat request, membalas, dan chat.", fontSize = 12.sp, color = SosGray)
            Spacer(Modifier.height(10.dp))
            AdminTextArea("Alasan pengaktifan", reason, { reason = it }, "Contoh: Banding diterima, pengguna sudah memahami pedoman komunitas.")
            Spacer(Modifier.height(10.dp))
            SosPrimaryButton("Aktifkan kembali akun", { confirm = true }, enabled = reason.isNotBlank())
        } else {
            Text("Penangguhan akun aktif", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "Akun tidak bisa membuat request, membalas, atau chat selama ditangguhkan. Pekerjaan berjalan tetap bisa diselesaikan lewat Admin.",
                fontSize = 12.sp,
                color = SosGray
            )
            Spacer(Modifier.height(10.dp))
            Text("Durasi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Box {
                Surface(onClick = { durationMenu = true }, shape = RoundedCornerShape(6.dp), color = SosLightBlue, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("${duration.first}  ▾", fontSize = 13.sp, color = Color.Black, modifier = Modifier.padding(12.dp))
                }
                DropdownMenu(expanded = durationMenu, onDismissRequest = { durationMenu = false }) {
                    SuspendDurations.forEach { option ->
                        DropdownMenuItem(text = { Text(option.first) }, onClick = { duration = option; durationMenu = false })
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            AdminTextArea("Alasan penangguhan", reason, { reason = it }, "Contoh: Penangguhan sementara untuk meninjau dugaan penyalahgunaan layanan.", hint = "Alasan dikirim ke pengguna.")
            Spacer(Modifier.height(10.dp))
            DangerButton("Tangguhkan akun", { confirm = true }, enabled = reason.isNotBlank())
        }
    }

    if (confirm) {
        val suspending = user.status == AccountStatus.ACTIVE
        SosConfirmDialog(
            title = if (suspending) "Tangguhkan akun?" else "Aktifkan kembali akun?",
            message = if (suspending) "${user.fullName} akan ditangguhkan selama ${duration.first.lowercase()}."
            else "${user.fullName} akan bisa memakai SOS lagi.",
            confirmText = "Ya",
            onConfirm = {
                if (suspending) AdminRepository.suspendUser(user.id, duration.second, reason)
                else AdminRepository.reactivateUser(user.id, reason)
                reason = ""
                confirm = false
            },
            onDismiss = { confirm = false }
        )
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier) {
    Column(
        modifier
            .background(SosLightBlue, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SosBlue)
        Text(label, fontSize = 10.sp, color = SosBlue)
    }
}
