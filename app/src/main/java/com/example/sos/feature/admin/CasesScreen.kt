package com.example.sos.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.AdminRepository
import com.example.sos.data.SosRepository
import com.example.sos.data.isOpen
import com.example.sos.data.model.*
import com.example.sos.ui.components.*
import java.time.LocalDateTime

/** Satu baris antrean: laporan pengguna atau dispute. */
private data class CaseRow(
    val id: String,
    val code: String,
    val title: String,
    val subtitle: String,
    val status: CaseStatus,
    val priority: Priority,
    val createdAt: LocalDateTime,
    val isDispute: Boolean
)

/** Laporan dan Dispute: antrean kasus, detail, dan keputusan. */
@Composable
fun AdminCasesScreen(initialId: String?, actions: AdminActions) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf(initialId) }
    var showClosed by rememberSaveable { mutableStateOf(false) }

    val rows = buildList {
        AdminRepository.disputes.forEach { d ->
            val a = SosRepository.assignment(d.assignmentId)
            add(
                CaseRow(
                    d.id, d.code, SosRepository.request(a?.requestId.orEmpty())?.title.orEmpty(),
                    "${userName(a?.requesterId.orEmpty())} ↔ ${userName(a?.helperId.orEmpty())} · ${d.reason}",
                    d.status, d.priority, d.createdAt, true
                )
            )
        }
        AdminRepository.userReports().forEach { r ->
            add(
                CaseRow(
                    r.id, r.code, r.reason, "Dilaporkan: ${userName(r.targetId)} · oleh ${userName(r.reporterId)}",
                    r.status, r.priority, r.createdAt, false
                )
            )
        }
    }
    val disputeCount = rows.count { it.isDispute && it.status.isOpen }
    val reportCount = rows.count { !it.isDispute && it.status.isOpen }
    val list = rows
        .filter { if (showClosed) true else it.status.isOpen }
        .filter { when (filter) { 1 -> !it.isDispute; 2 -> it.isDispute; else -> true } }
        .filter { search.isBlank() || it.title.contains(search, true) || it.code.contains(search, true) || it.subtitle.contains(search, true) }
        .sortedWith(compareByDescending<CaseRow> { it.priority == Priority.HIGH }.thenByDescending { it.createdAt })

    AdminPage(
        title = "Laporan dan Dispute",
        subtitle = "Tinjau bukti dan berikan keputusan yang adil untuk kedua pihak.",
        onBack = actions.back,
        onLogout = actions.logout,
        onNotifications = actions.notifications,
        headerExtra = {
            Spacer(Modifier.height(10.dp))
            SosSearchField(search, { search = it }, Modifier.fillMaxWidth(), "Cari ID kasus atau nama pengguna")
        }
    ) {
        item { SectionHeader("Antrean kasus", badge = "${disputeCount + reportCount} terbuka") }
        item { FilterRow(listOf("Semua", "Laporan $reportCount", "Dispute $disputeCount"), filter) { filter = it } }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showClosed, onCheckedChange = { showClosed = it })
                Text("Tampilkan kasus selesai", fontSize = 12.sp, color = Color.Black)
            }
        }
        if (list.isEmpty()) item { EmptyNote("Tidak ada kasus di filter ini.") }
        list.forEach { row ->
            item(key = row.id) {
                AdminCard(selected = row.id == selectedId, onClick = { selectedId = row.id }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${row.code} · ${agoId(row.createdAt)}", fontSize = 11.sp, color = SosGray, modifier = Modifier.weight(1f))
                        Pill(row.status.label, caseKind(row.status))
                    }
                    Text(row.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
                    Text(row.subtitle, fontSize = 11.sp, color = SosGray)
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Pill(if (row.isDispute) "Dispute" else "Laporan", if (row.isDispute) PillKind.BLUE else PillKind.GRAY)
                        Spacer(Modifier.weight(1f))
                        Text("Lihat kasus →", fontSize = 12.sp, color = SosBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        val dispute = selectedId?.let { AdminRepository.dispute(it) }
        val report = selectedId?.let { AdminRepository.report(it) }
        when {
            dispute != null -> item(key = "d-${dispute.id}") { DisputeDetail(dispute) }
            report != null -> item(key = "r-${report.id}") { UserReportDetail(report, actions) }
            else -> item { EmptyNote("Pilih kasus untuk melihat detail.") }
        }
    }
}

@Composable
private fun DisputeDetail(dispute: Dispute) = Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    val a = SosRepository.assignment(dispute.assignmentId)
    val request = a?.let { SosRepository.request(it.requestId) }
    var decision by remember(dispute.id) { mutableStateOf<DisputeDecision?>(null) }
    var amountText by rememberSaveable(dispute.id) { mutableStateOf("") }
    var basis by rememberSaveable(dispute.id) { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }

    if (a == null || request == null) {
        EmptyNote("Data pekerjaan tidak ditemukan.")
        return@Column
    }
    val partialAmount = amountText.toLongOrNull() ?: 0
    val amountValid = decision != DisputeDecision.PARTIAL_REFUND || partialAmount in 1 until a.agreedPrice

    AdminCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Detail kasus · ${dispute.code}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            Pill(dispute.status.label, caseKind(dispute.status))
        }
        Spacer(Modifier.height(6.dp))
        Text("${userName(a.requesterId)} → ${userName(a.helperId)}", fontSize = 12.sp, color = SosGray)
        Text(request.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(rupiah(a.agreedPrice) + " · ditahan SOS", fontSize = 12.sp, color = SosBlue)
        Spacer(Modifier.height(6.dp))
        InfoRow("Dibuka", adminDateTime(dispute.createdAt))
        InfoRow("Status pekerjaan", a.status.label)
        InfoRow("Batas pengerjaan", adminDate(a.deadline))
        InfoRow("Diajukan oleh", userName(dispute.raisedBy))
    }

    AdminCard {
        Text("Alasan dispute", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        dispute.statements.forEach { s ->
            Spacer(Modifier.height(8.dp))
            val role = if (s.userId == a.requesterId) "Requester" else "Helper"
            Text("${userName(s.userId)} · $role", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SosBlue)
            Text("“${s.text}”", fontSize = 12.sp, color = Color.Black)
        }
    }

    AdminCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bukti kasus", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            Pill("${dispute.evidence.size} bukti", PillKind.BLUE)
        }
        if (dispute.evidence.isEmpty()) EmptyNote("Belum ada lampiran bukti.")
        dispute.evidence.forEach { file ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(SosLightBlue, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Description, null, Modifier.size(16.dp), tint = SosBlue)
                Spacer(Modifier.width(6.dp))
                Text(file, fontSize = 12.sp, color = SosBlue)
            }
        }
        // Cuplikan chat terakhir dari percakapan pekerjaan ini (kalau ada).
        val conversation = SosRepository.conversations.firstOrNull { it.requestId == a.requestId && it.helperId == a.helperId }
        val excerpt = conversation?.let { SosRepository.messagesFor(it.id) }
            ?.filter { it.type == MessageType.TEXT }?.takeLast(2).orEmpty()
        if (excerpt.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text("Cuplikan chat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB45309))
                excerpt.forEach {
                    Text("${userName(it.senderId)}: “${it.text}”", fontSize = 12.sp, color = Color.Black, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        Text("Data bukti hanya dipakai untuk keputusan kasus.", fontSize = 10.sp, color = SosGray, modifier = Modifier.padding(top = 6.dp))
    }

    AdminCard {
        Text("Keputusan penanganan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        if (!dispute.status.isOpen) {
            Spacer(Modifier.height(6.dp))
            Pill(dispute.resolution ?: "Selesai", PillKind.GREEN)
            Spacer(Modifier.height(8.dp))
            AuditTimeline(AdminRepository.logFor(dispute.id))
        } else {
            RadioGroup(DisputeDecision.entries, decision, { it.label }) { decision = it }
            if (decision == DisputeDecision.PARTIAL_REFUND) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter(Char::isDigit).take(9) },
                    label = { Text("Nominal refund (maks ${rupiah(a.agreedPrice - 1)})") },
                    prefix = { Text("Rp") },
                    singleLine = true,
                    isError = !amountValid && amountText.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (amountValid && partialAmount > 0) {
                    Text(
                        "Requester menerima ${rupiah(partialAmount)}, helper menerima ${rupiah(a.agreedPrice - partialAmount)}.",
                        fontSize = 11.sp,
                        color = SosGray
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            AdminTextArea(
                "Dasar keputusan", basis, { basis = it },
                "Contoh: Bukti chat menunjukkan komponen input dan kartu belum dikirim saat tenggat.",
                hint = "Dikirim ke kedua pihak dan tercatat di log audit."
            )
            Spacer(Modifier.height(10.dp))
            SosPrimaryButton(
                "Tetapkan keputusan", { confirm = true },
                enabled = decision != null && basis.isNotBlank() && amountValid
            )
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Minta bukti tambahan", { AdminRepository.requestMoreEvidence(dispute.id) })
            Spacer(Modifier.height(8.dp))
            AuditTimeline(AdminRepository.logFor(dispute.id))
        }
    }

    if (confirm) {
        SosConfirmDialog(
            title = "Tetapkan keputusan?",
            message = "${decision?.label} untuk ${dispute.code}. Keputusan bersifat final dan dana langsung diproses.",
            confirmText = "Tetapkan",
            onConfirm = {
                decision?.let { AdminRepository.resolveDispute(dispute.id, it, partialAmount, basis) }
                confirm = false
            },
            onDismiss = { confirm = false }
        )
    }
}

@Composable
private fun UserReportDetail(report: Report, actions: AdminActions) = Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    val target = SosRepository.user(report.targetId)
    var note by rememberSaveable(report.id) { mutableStateOf("") }
    var confirmSuspend by remember { mutableStateOf(false) }

    AdminCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Detail laporan · ${report.code}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            Pill(report.status.label, caseKind(report.status))
        }
        Spacer(Modifier.height(6.dp))
        Pill(report.reason, PillKind.RED)
        Text(report.description, fontSize = 12.sp, color = Color.Black, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(6.dp))
        InfoRow("Dilaporkan", target?.fullName.orEmpty())
        InfoRow("Pelapor", userName(report.reporterId))
        InfoRow("Status akun", target?.status?.label.orEmpty(),
            if (target?.status == AccountStatus.SUSPENDED) Color(0xFFC62828) else Color(0xFF2E7D32))
        InfoRow("Total laporan ke akun ini", "${AdminRepository.reportsAgainst(report.targetId)}")
        InfoRow("Dibuat", adminDateTime(report.createdAt))
        LinkText("Buka profil di Kelola pengguna") { actions.users(report.targetId, null) }
    }

    AdminCard {
        Text("Tindakan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        if (!report.status.isOpen) {
            Spacer(Modifier.height(6.dp))
            Pill(report.resolution ?: report.status.label, caseKind(report.status))
        } else {
            Spacer(Modifier.height(6.dp))
            AdminTextArea("Catatan admin", note, { note = it }, "Contoh: Bukti chat menunjukkan pesan kasar berulang.")
            Spacer(Modifier.height(10.dp))
            SosPrimaryButton("Beri peringatan", {
                AdminRepository.resolveUserReport(report.id, "Peringatan dikirim: ${note.trim()}")
            }, enabled = note.isNotBlank())
            Spacer(Modifier.height(8.dp))
            DangerButton("Tangguhkan 7 hari", { confirmSuspend = true }, enabled = note.isNotBlank())
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Tutup tanpa tindakan", { AdminRepository.dismissReport(report.id) })
        }
        Spacer(Modifier.height(8.dp))
        AuditTimeline(AdminRepository.logFor(report.id))
    }

    if (confirmSuspend) {
        SosConfirmDialog(
            title = "Tangguhkan akun?",
            message = "${target?.fullName} akan ditangguhkan 7 hari.",
            confirmText = "Tangguhkan",
            onConfirm = {
                AdminRepository.resolveUserReport(report.id, "Akun ditangguhkan 7 hari: ${note.trim()}", 7, suspend = true)
                confirmSuspend = false
            },
            onDismiss = { confirmSuspend = false }
        )
    }
}
