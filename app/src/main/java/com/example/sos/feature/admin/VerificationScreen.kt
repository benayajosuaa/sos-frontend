package com.example.sos.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Image
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
import com.example.sos.data.model.StudentVerification
import com.example.sos.data.model.VerificationStatus
import com.example.sos.ui.components.*

/** Verifikasi Mahasiswa: antrean KTM + detail + keputusan. */
@Composable
fun AdminVerificationScreen(initialId: String?, actions: AdminActions) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf(initialId) }
    var note by rememberSaveable(selectedId) { mutableStateOf("") }
    var showRejectError by remember(selectedId) { mutableStateOf(false) }

    LaunchedEffect(selectedId) { selectedId?.let { AdminRepository.startReview(it) } }

    val all = AdminRepository.verifications.sortedBy { it.submittedAt }
    val waiting = all.count { it.status == VerificationStatus.PENDING }
    val reviewing = all.count { it.status == VerificationStatus.IN_REVIEW }
    val list = all.filter {
        when (filter) {
            0 -> it.status == VerificationStatus.PENDING || it.status == VerificationStatus.IN_REVIEW
            1 -> it.status == VerificationStatus.PENDING
            2 -> it.status == VerificationStatus.IN_REVIEW
            else -> it.status == VerificationStatus.APPROVED || it.status == VerificationStatus.REJECTED
        }
    }.filter {
        val user = SosRepository.user(it.userId)
        search.isBlank() || user?.fullName?.contains(search, true) == true ||
                it.nim.contains(search) || it.university.contains(search, true)
    }
    val selected = selectedId?.let { AdminRepository.verification(it) }

    AdminPage(
        title = "Verifikasi Mahasiswa",
        subtitle = "Periksa identitas mahasiswa sebelum akun bisa membuat request dan menjadi helper.",
        onBack = actions.back,
        onLogout = actions.logout,
        onNotifications = actions.notifications,
        headerExtra = {
            Spacer(Modifier.height(10.dp))
            SosSearchField(search, { search = it }, Modifier.fillMaxWidth(), "Cari nama, NIM, atau kampus")
        }
    ) {
        item { SectionHeader("Antrean verifikasi", badge = "${waiting + reviewing} aktif") }
        item {
            FilterRow(listOf("Aktif ${waiting + reviewing}", "Menunggu $waiting", "Ditinjau $reviewing", "Selesai"), filter) { filter = it }
        }
        if (list.isEmpty()) item { EmptyNote("Tidak ada pengajuan di filter ini.") }
        list.forEach { v ->
            item(key = v.id) {
                val user = SosRepository.user(v.userId)
                AdminCard(selected = v.id == selectedId, onClick = { selectedId = v.id }) {
                    Text(user?.fullName.orEmpty(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("${v.university} · ${agoId(v.submittedAt)}", fontSize = 11.sp, color = SosGray)
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Pill(v.status.label, statusPill(v.status))
                        v.flag?.let { Spacer(Modifier.width(6.dp)); Pill(it, PillKind.RED) }
                        Spacer(Modifier.weight(1f))
                        Text("Tinjau →", fontSize = 12.sp, color = SosBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            Text(
                "Target: setiap pengajuan ditinjau maksimal 1×24 jam.",
                fontSize = 11.sp,
                color = SosGray
            )
        }

        if (selected != null) {
            item { VerificationDetail(selected) }
            item {
                AdminCard {
                    Text("Keputusan verifikasi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    if (selected.status == VerificationStatus.APPROVED || selected.status == VerificationStatus.REJECTED) {
                        Spacer(Modifier.height(6.dp))
                        Pill(selected.status.label, statusPill(selected.status))
                        selected.note?.let { Text("Catatan: $it", fontSize = 12.sp, color = Color.Black, modifier = Modifier.padding(top = 6.dp)) }
                        selected.decidedAt?.let { Text("Diputuskan ${adminDateTime(it)}", fontSize = 11.sp, color = SosGray) }
                    } else {
                        Text("Pastikan nama, NIM, dan masa berlaku KTM sesuai.", fontSize = 12.sp, color = SosGray)
                        Spacer(Modifier.height(10.dp))
                        AdminTextArea(
                            "Alasan / catatan admin", note, { note = it; showRejectError = false },
                            "Contoh: Nama dan NIM cocok dengan profil. KTM masih berlaku.",
                            hint = "Wajib diisi jika verifikasi ditolak."
                        )
                        if (showRejectError) {
                            Text("Isi alasan penolakan terlebih dahulu.", fontSize = 11.sp, color = Color(0xFFC62828))
                        }
                        Spacer(Modifier.height(12.dp))
                        SosPrimaryButton("Terima verifikasi", {
                            AdminRepository.decideVerification(selected.id, true, note)
                        })
                        Spacer(Modifier.height(8.dp))
                        DangerButton("Tolak verifikasi", {
                            if (note.isBlank()) showRejectError = true
                            else AdminRepository.decideVerification(selected.id, false, note)
                        })
                        Text(
                            "Keputusan dikirim ke mahasiswa lewat notifikasi dan tercatat di log audit.",
                            fontSize = 11.sp,
                            color = SosGray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            item { EmptyNote("Pilih pengajuan di atas untuk melihat detail.") }
        }
    }
}

fun statusPill(status: VerificationStatus) = when (status) {
    VerificationStatus.PENDING -> PillKind.ORANGE
    VerificationStatus.IN_REVIEW -> PillKind.BLUE
    VerificationStatus.APPROVED -> PillKind.GREEN
    VerificationStatus.REJECTED -> PillKind.RED
}

@Composable
private fun VerificationDetail(v: StudentVerification) = Column {
    val user = SosRepository.user(v.userId)
    AdminCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Detail verifikasi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            Pill(v.status.label, statusPill(v.status))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(36.dp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(user?.fullName.orEmpty(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(user?.email.orEmpty(), fontSize = 11.sp, color = SosGray)
            }
        }
        Spacer(Modifier.height(8.dp))
        InfoRow("Nomor induk mahasiswa", v.nim)
        InfoRow("Program studi", v.program)
        InfoRow("Kampus", v.university)
        InfoRow("Diajukan", adminDateTime(v.submittedAt))
    }
    Spacer(Modifier.height(12.dp))
    AdminCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Kartu Tanda Mahasiswa", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            Text("Perbesar ↗", fontSize = 11.sp, color = SosBlue)
        }
        Spacer(Modifier.height(8.dp))
        // Pratinjau KTM (file asli disimpan di private storage, dibuka via signed URL).
        Column(
            Modifier
                .fillMaxWidth()
                .border(1.dp, SosBorder, RoundedCornerShape(8.dp))
        ) {
            Text(
                v.university.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SosBlue, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(8.dp)
            )
            Row(Modifier.padding(10.dp)) {
                Box(
                    Modifier
                        .size(56.dp, 70.dp)
                        .background(Color(0xFFDDDDDD), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("KARTU TANDA MAHASISWA", fontSize = 9.sp, color = SosGray)
                    Text(user?.fullName?.uppercase().orEmpty(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(v.nim, fontSize = 11.sp, color = Color.Black)
                    Text(v.program, fontSize = 11.sp, color = Color.Black)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .background(SosLightBlue, RoundedCornerShape(4.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Image, null, Modifier.size(16.dp), tint = SosBlue)
            Spacer(Modifier.width(6.dp))
            Text(v.ktmFileName, fontSize = 12.sp, color = SosBlue)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (v.matchesProfile) Icons.Default.CheckCircle else Icons.Default.Warning,
                null,
                Modifier.size(16.dp),
                tint = if (v.matchesProfile) Color(0xFF2E7D32) else Color(0xFFB45309)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (v.matchesProfile) "Nama & NIM sesuai profil" else "Data KTM berbeda dengan profil / foto buram",
                fontSize = 12.sp,
                color = if (v.matchesProfile) Color(0xFF2E7D32) else Color(0xFFB45309)
            )
        }
        Text(
            "Dokumen hanya untuk verifikasi. Jangan dibagikan.",
            fontSize = 10.sp,
            color = SosGray,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
