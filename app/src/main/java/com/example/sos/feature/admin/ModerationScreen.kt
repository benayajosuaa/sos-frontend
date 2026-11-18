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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.AdminRepository
import com.example.sos.data.SosRepository
import com.example.sos.data.isOpen
import com.example.sos.data.model.*
import com.example.sos.ui.components.*

/** Moderasi Request: request yang dilaporkan, detail, tindakan, log. */
@Composable
fun AdminModerationScreen(initialId: String?, actions: AdminActions) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf(initialId) }

    LaunchedEffect(selectedId) { selectedId?.let { AdminRepository.markInReview(it) } }

    val all = AdminRepository.requestReports()
    val list = all.filter {
        when (filter) {
            0 -> it.status.isOpen
            1 -> it.status == CaseStatus.OPEN
            2 -> it.status == CaseStatus.IN_REVIEW
            else -> !it.status.isOpen
        }
    }.filter {
        val title = SosRepository.request(it.targetId)?.title.orEmpty()
        search.isBlank() || title.contains(search, true) || it.code.contains(search, true)
    }.sortedByDescending { it.priority == Priority.HIGH }
    val selected = selectedId?.let { AdminRepository.report(it) }

    AdminPage(
        title = "Moderasi Request",
        subtitle = "Tinjau request bermasalah dan jaga layanan kampus tetap aman.",
        onBack = actions.back,
        onLogout = actions.logout,
        onNotifications = actions.notifications,
        headerExtra = {
            Spacer(Modifier.height(10.dp))
            SosSearchField(search, { search = it }, Modifier.fillMaxWidth(), "Cari ID atau judul request")
        }
    ) {
        item { SectionHeader("Request ditandai", badge = "${all.count { it.status.isOpen }} terbuka") }
        item {
            FilterRow(
                listOf(
                    "Terbuka ${all.count { it.status.isOpen }}",
                    "Baru ${all.count { it.status == CaseStatus.OPEN }}",
                    "Ditinjau ${all.count { it.status == CaseStatus.IN_REVIEW }}",
                    "Selesai"
                ), filter
            ) { filter = it }
        }
        if (list.isEmpty()) item { EmptyNote("Tidak ada request di filter ini.") }
        list.forEach { report ->
            item(key = report.id) {
                val request = SosRepository.request(report.targetId)
                AdminCard(selected = report.id == selectedId, onClick = { selectedId = report.id }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(report.code, fontSize = 11.sp, color = SosGray, modifier = Modifier.weight(1f))
                        Pill(report.priority.label, if (report.priority == Priority.HIGH) PillKind.RED else PillKind.GRAY)
                    }
                    Text(request?.title.orEmpty(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
                    Text("${report.reason} · ${report.reportCount} laporan", fontSize = 11.sp, color = SosGray)
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Pill(report.status.label, caseKind(report.status))
                        if (request?.isHidden == true) { Spacer(Modifier.width(6.dp)); Pill("Disembunyikan", PillKind.GRAY) }
                        Spacer(Modifier.weight(1f))
                        Text("Periksa request →", fontSize = 12.sp, color = SosBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (selected != null) {
            item(key = "detail-${selected.id}") { ModerationDetail(selected) }
        } else {
            item { EmptyNote("Pilih request untuk melihat detail.") }
        }
    }
}

fun caseKind(status: CaseStatus) = when (status) {
    CaseStatus.OPEN -> PillKind.ORANGE
    CaseStatus.IN_REVIEW -> PillKind.BLUE
    CaseStatus.RESOLVED -> PillKind.GREEN
    CaseStatus.DISMISSED -> PillKind.GRAY
}

@Composable
private fun ModerationDetail(report: Report) = Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    val request = SosRepository.request(report.targetId)
    var action by remember(report.id) { mutableStateOf<ModerationAction?>(ModerationAction.HIDE) }
    var reason by rememberSaveable(report.id) { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }

    if (request == null) {
        EmptyNote("Request sudah tidak tersedia.")
        return@Column
    }

    AdminCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Detail request", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            Pill(if (request.isHidden) "Disembunyikan" else "Tayang", if (request.isHidden) PillKind.GRAY else PillKind.GREEN)
        }
        Text("${report.code} · ${adminDateTime(request.createdAt)}", fontSize = 11.sp, color = SosGray)
        Spacer(Modifier.height(8.dp))
        Text(
            "${userName(request.requesterId)} · ${SosRepository.user(request.requesterId)?.university.orEmpty()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(request.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
        Text("Anggaran ${rupiah(request.budget)} · ${deadlineLabel(request.deadline)}", fontSize = 11.sp, color = SosBlue)
        Spacer(Modifier.height(6.dp))
        Text(
            "“${request.description}”",
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F7FA), RoundedCornerShape(6.dp))
                .padding(10.dp)
        )
        Spacer(Modifier.height(6.dp))
        InfoRow("Kategori", request.subject)
        InfoRow("Status pengerjaan", if (SosRepository.assignmentsFor(request.id).isEmpty()) "Belum ada helper" else "Ada helper")
    }

    AdminCard {
        Text("Konteks pelaporan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        Pill(report.reason, PillKind.RED)
        Text(report.description, fontSize = 12.sp, color = Color.Black, modifier = Modifier.padding(top = 6.dp))
        Text(
            "${report.reportCount} laporan · terakhir ${agoId(report.createdAt)} · pelapor pertama ${userName(report.reporterId)}",
            fontSize = 11.sp,
            color = SosGray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    AdminCard {
        Text("Tindakan moderasi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        if (!report.status.isOpen) {
            Spacer(Modifier.height(6.dp))
            Pill(report.resolution ?: report.status.label, caseKind(report.status))
            Spacer(Modifier.height(10.dp))
            if (request.isHidden && request.status != RequestStatus.CANCELLED) {
                SecondaryButton("Pulihkan request", { AdminRepository.restoreRequest(report.id) })
            }
        } else {
            RadioGroup(ModerationAction.entries, action, { it.label }) { action = it }
            Spacer(Modifier.height(6.dp))
            AdminTextArea(
                "Alasan tindakan", reason, { reason = it },
                "Contoh: Permintaan mengerjakan ujian melanggar integritas akademik.",
                hint = "Pengguna menerima notifikasi berisi alasan ini."
            )
            Spacer(Modifier.height(10.dp))
            SosPrimaryButton("Simpan tindakan", { confirm = true }, enabled = action != null && reason.isNotBlank())
            if (request.isHidden) {
                Spacer(Modifier.height(8.dp))
                SecondaryButton("Pulihkan request", { AdminRepository.restoreRequest(report.id) })
            }
        }
    }

    AdminCard {
        Text("Log moderasi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        AuditTimeline(AdminRepository.logFor(request.id))
    }

    if (confirm) {
        SosConfirmDialog(
            title = "Simpan tindakan?",
            message = "${action?.label} untuk “${request.title}”.",
            confirmText = "Simpan",
            onConfirm = {
                action?.let { AdminRepository.moderateRequest(report.id, it, reason) }
                reason = ""
                confirm = false
            },
            onDismiss = { confirm = false }
        )
    }
}
