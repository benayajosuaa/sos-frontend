package com.example.sos.feature.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.AdminRepository
import com.example.sos.data.model.PlatformTransaction
import com.example.sos.data.model.PlatformTxStatus
import com.example.sos.data.model.PlatformTxType
import com.example.sos.ui.components.*
import java.time.LocalDate

/** Monitoring Transaksi: dana ditahan, aktivitas, detail & tindak lanjut. */
@Composable
fun AdminTransactionsScreen(initialId: String?, actions: AdminActions) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf(initialId) }

    val all = AdminRepository.transactions.sortedByDescending { it.createdAt }
    val today = all.filter { it.createdAt.toLocalDate() == LocalDate.now() }
    fun sum(type: PlatformTxType, status: PlatformTxStatus? = null) =
        today.filter { it.type == type && (status == null || it.status == status) }.sumOf { it.amount }

    val list = all.filter {
        when (filter) {
            1 -> it.type == PlatformTxType.PAYMENT
            2 -> it.type == PlatformTxType.REFUND
            3 -> it.type == PlatformTxType.PAYOUT
            else -> true
        }
    }.filter {
        search.isBlank() || it.code.contains(search, true) || userName(it.userId).contains(search, true) ||
                it.title.contains(search, true)
    }
    val selected = selectedId?.let { AdminRepository.transaction(it) }

    AdminPage(
        title = "Monitoring Transaksi",
        subtitle = "Pantau pembayaran, refund, dan pencairan dana dari satu tempat.",
        onBack = actions.back,
        onLogout = actions.logout,
        onNotifications = actions.notifications,
        headerExtra = {
            Spacer(Modifier.height(10.dp))
            SosSearchField(search, { search = it }, Modifier.fillMaxWidth(), "Cari ID transaksi atau nama pengguna")
        }
    ) {
        item { SectionHeader("Ringkasan hari ini", badge = adminDate(LocalDate.now())) }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SosBlue, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("Dana ditahan platform", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                Text(rupiah(AdminRepository.heldFunds()), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Dari pekerjaan yang sedang berjalan, dilepas setelah selesai.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }
        item {
            AdminCard {
                InfoRow("Pembayaran berhasil", rupiah(sum(PlatformTxType.PAYMENT, PlatformTxStatus.SUCCESS)))
                InfoRow("Refund menunggu", rupiah(sum(PlatformTxType.REFUND, PlatformTxStatus.PENDING)))
                InfoRow("Pencairan gagal", rupiah(sum(PlatformTxType.PAYOUT, PlatformTxStatus.FAILED)), Color(0xFFC62828))
            }
        }
        item { SectionHeader("Aktivitas transaksi", badge = "${list.size} transaksi") }
        item { FilterRow(listOf("Semua", "Bayar", "Refund", "Pencairan"), filter) { filter = it } }
        if (list.isEmpty()) item { EmptyNote("Tidak ada transaksi.") }
        list.forEach { tx ->
            item(key = tx.id) {
                AdminCard(selected = tx.id == selectedId, onClick = { selectedId = tx.id }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tx.code, fontSize = 11.sp, color = SosGray, modifier = Modifier.weight(1f))
                        Text(adminClock(tx.createdAt) + " WIB", fontSize = 11.sp, color = SosGray)
                    }
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${tx.type.label} · ${userName(tx.userId)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
                        Text(rupiah(tx.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Text("${tx.method} · ${tx.title}", fontSize = 11.sp, color = SosGray)
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Pill(tx.status.label, txPill(tx.status))
                        Spacer(Modifier.weight(1f))
                        Text("Lihat →", fontSize = 12.sp, color = SosBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (selected != null) {
            item(key = "detail-${selected.id}") { TransactionDetail(selected) }
        } else {
            item { EmptyNote("Pilih transaksi untuk melihat detail.") }
        }
    }
}

@Composable
private fun TransactionDetail(tx: PlatformTransaction) = Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    val context = LocalContext.current
    var note by rememberSaveable(tx.id) { mutableStateOf("") }

    AdminCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Detail ${tx.type.label.lowercase()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            Pill(tx.status.label, txPill(tx.status))
        }
        Text("${tx.code} · ${userName(tx.userId)}", fontSize = 11.sp, color = SosGray)
        Text(rupiah(tx.amount), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(vertical = 6.dp))
        InfoRow("Tujuan / metode", "${tx.method} · ${tx.maskedAccount}")
        InfoRow("Nama pengguna", userName(tx.userId))
        InfoRow("Keterangan", tx.title)
        InfoRow("Dibuat", adminDateTime(tx.createdAt))
        InfoRow("Referensi gateway", tx.gatewayRef)
        tx.failureReason?.let {
            Text(
                it,
                fontSize = 12.sp,
                color = Color(0xFFC62828),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(Color(0xFFFDE0E0), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            )
        }
    }

    AdminCard {
        Text("Riwayat pemrosesan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        AuditTimeline(AdminRepository.logFor(tx.id))
    }

    AdminCard {
        Text("Tindak lanjut admin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        if (tx.status == PlatformTxStatus.FAILED || tx.status == PlatformTxStatus.NEEDS_FIX) {
            AdminTextArea(
                "Catatan investigasi", note, { note = it },
                "Contoh: Minta pengguna mencocokkan nama rekening dengan identitas terverifikasi.",
                hint = "Pengguna akan diminta memperbarui data rekening di aplikasi."
            )
            Spacer(Modifier.height(10.dp))
            SosPrimaryButton("Kirim permintaan perbaikan", {
                AdminRepository.requestPayoutFix(tx.id, note)
                note = ""
            }, enabled = note.isNotBlank() && tx.status == PlatformTxStatus.FAILED)
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Proses ulang transaksi", { AdminRepository.retryTransaction(tx.id) })
        } else if (tx.status == PlatformTxStatus.PENDING) {
            Text("Transaksi masih menunggu konfirmasi dari payment gateway.", fontSize = 12.sp, color = SosGray)
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Tandai berhasil (cek manual)", { AdminRepository.retryTransaction(tx.id) })
        } else {
            Text("Tidak ada tindakan yang diperlukan.", fontSize = 12.sp, color = SosGray)
        }
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Ekspor detail transaksi", {
            // TODO: GET /admin/transactions/{id}/export
            Toast.makeText(context, "Detail ${tx.code} diekspor (simulasi)", Toast.LENGTH_SHORT).show()
        })
        Text(
            "Data rekening lengkap tidak disimpan SOS; hanya referensi gateway dan nominal.",
            fontSize = 10.sp,
            color = SosGray,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
