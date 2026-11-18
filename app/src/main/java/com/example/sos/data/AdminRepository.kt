package com.example.sos.data

import androidx.compose.runtime.mutableStateListOf
import com.example.sos.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Data & aksi Admin (in-memory, simulasi).
 * Setiap aksi Admin menulis AuditEntry (docs/security.md: semua aksi Admin diaudit).
 *
 * TODO: Ganti dengan endpoint /admin/... di backend; otorisasi role ADMIN
 * wajib dicek di server, bukan di aplikasi.
 */
object AdminRepository {

    private val now: LocalDateTime = LocalDateTime.now()
    private val adminName: String
        get() = SosRepository.user(SosRepository.ADMIN_ID)?.fullName ?: "Admin"

    val verifications = mutableStateListOf(
        StudentVerification(
            "v1", "dimas", "2602118842", "Teknik Informatika", "BINUS University",
            now.minusMinutes(12), "Dimas_KTM.jpg", matchesProfile = true
        ),
        StudentVerification(
            "v2", "nadia", "2106701234", "Ilmu Komunikasi", "Universitas Indonesia",
            now.minusMinutes(38), "KTM_Nadia.pdf", matchesProfile = true,
            status = VerificationStatus.IN_REVIEW
        ),
        StudentVerification(
            "v3", "kevin", "15021077", "Teknik Sipil", "Institut Teknologi Bandung",
            now.minusHours(1), "kevin-ktm.jpg", matchesProfile = false,
            flag = "Perlu KTM baru"
        ),
        StudentVerification(
            "v4", SosRepository.CURRENT_USER_ID, "01082200045", "Product Design",
            "Universitas Pelita Harapan", now.minusMonths(2), "KTM_KimDokja.jpg",
            matchesProfile = true, status = VerificationStatus.APPROVED,
            decidedAt = now.minusMonths(2)
        )
    )

    val reports = mutableStateListOf(
        Report(
            "rp1", "REQ-2190", ReportTarget.REQUEST, "r20", "user1", "Integritas akademik",
            "Request meminta orang lain mengerjakan ujian online. Melanggar pedoman layanan.",
            reportCount = 3, priority = Priority.HIGH, createdAt = now.minusMinutes(42)
        ),
        Report(
            "rp2", "REQ-2201", ReportTarget.REQUEST, "r21", "mary", "Pelanggaran aturan kampus",
            "Meminta titip absen, termasuk kecurangan kehadiran.",
            reportCount = 1, priority = Priority.HIGH, createdAt = now.minusHours(2)
        ),
        Report(
            "rp3", "REQ-2184", ReportTarget.REQUEST, "r22", "chris", "Bukan bantuan akademik",
            "Request berupa jasa titip, bukan bantuan belajar.", reportCount = 2,
            status = CaseStatus.IN_REVIEW, createdAt = now.minusHours(5)
        ),
        Report(
            "rp4", "RPT-1051", ReportTarget.USER, "joshua", "faith", "Percakapan tidak pantas",
            "Mengirim pesan bernada kasar setelah tawaran bantuan ditolak.",
            createdAt = now.minusHours(3)
        ),
        Report(
            "rp5", "RPT-1042", ReportTarget.USER, "budi", "user1", "Menerima joki ujian",
            "Menawarkan diri mengerjakan ujian online untuk orang lain.",
            priority = Priority.HIGH, status = CaseStatus.RESOLVED,
            createdAt = now.minusDays(6), resolution = "Akun ditangguhkan 7 hari"
        )
    )

    val disputes = mutableStateListOf(
        Dispute(
            "d1", "DSP-208", "a10", "faith", "Hasil tugas belum lengkap",
            statements = listOf(
                DisputeStatement(
                    "chris",
                    "Saya meminta komponen tombol, input, dan kartu. File yang dikirim hanya berisi " +
                            "tombol dan belum bisa digunakan."
                ),
                DisputeStatement(
                    "faith",
                    "Saya mengirim versi awal. Input dan kartu masih dikerjakan, tetapi batas waktu " +
                            "sudah lewat dan requester langsung minta batal."
                )
            ),
            evidence = listOf("Komponen_button.fig · 420 KB", "UI_Kit_v1.fig · 2,1 MB", "Bukti_pembayaran.pdf · 180 KB"),
            createdAt = now.minusMinutes(50)
        ),
        Dispute(
            "d2", "DSP-205", "a11", "mary", "Hasil tidak sesuai kesepakatan",
            statements = listOf(
                DisputeStatement("mary", "Ringkasan hanya satu jurnal, padahal kesepakatannya dua."),
                DisputeStatement("chris", "Jurnal kedua sudah saya kirim lewat chat dalam bentuk PDF.")
            ),
            evidence = listOf("Ringkasan_jurnal_1.pdf · 230 KB"),
            priority = Priority.NORMAL, status = CaseStatus.IN_REVIEW,
            createdAt = now.minusHours(6)
        )
    )

    val transactions = mutableStateListOf(
        PlatformTransaction(
            "pt1", "TRX-0918", PlatformTxType.PAYOUT, "dimas", "Pencairan saldo helper", 250_000,
            PlatformTxStatus.FAILED, "Transfer bank", "BCA •••• 4821", now.minusMinutes(25),
            "PO-139224-8314", failureReason = "Nama pemilik rekening tidak sesuai dengan nama akun."
        ),
        PlatformTransaction(
            "pt2", "RFD-0820", PlatformTxType.REFUND, SosRepository.CURRENT_USER_ID,
            "Refund: Review desain landing page", 15_000, PlatformTxStatus.PENDING, "Saldo SOS",
            "Dompet SOS", now.minusHours(1), "RF-882011-0042"
        ),
        PlatformTransaction(
            "pt3", "PAY-0912", PlatformTxType.PAYMENT, "kevin", "Bantu hitung struktur balok", 100_000,
            PlatformTxStatus.SUCCESS, "QRIS", "QRIS", now.minusHours(2), "PG-551902-7710"
        ),
        PlatformTransaction(
            "pt4", "PAY-0909", PlatformTxType.PAYMENT, SosRepository.CURRENT_USER_ID,
            "Gimana cara bikin UI Kit?", 15_000, PlatformTxStatus.SUCCESS, "Saldo SOS", "Dompet SOS",
            now.minusDays(1), "PG-551877-1203"
        ),
        PlatformTransaction(
            "pt5", "TRX-0901", PlatformTxType.PAYOUT, "klein", "Pencairan saldo helper", 120_000,
            PlatformTxStatus.SUCCESS, "Transfer bank", "Mandiri •••• 1190", now.minusDays(1),
            "PO-139101-2207"
        )
    )

    val audit = mutableStateListOf(
        AuditEntry("budi", now.minusDays(6), "Alya Putri", "Akun ditangguhkan 7 hari: menerima joki ujian online (RPT-1042)."),
        AuditEntry("budi", now.minusDays(6).minusHours(1), "Sistem", "3 laporan baru terhadap akun ini."),
        AuditEntry("r20", now.minusMinutes(40), "Sistem", "Request dilaporkan 3 kali dan masuk antrean prioritas."),
        AuditEntry("r20", now.minusHours(4), "Budi Santoso", "Request dipublikasikan."),
        AuditEntry("pt1", now.minusMinutes(25), "Gateway", "Bank menolak transfer: nama penerima tidak cocok."),
        AuditEntry("pt1", now.minusMinutes(26), "Sistem", "Pencairan diproses ke BCA •••• 4821."),
        AuditEntry("pt1", now.minusMinutes(30), "Dimas Ardi", "Mengajukan pencairan saldo."),
        AuditEntry("d1", now.minusMinutes(50), "Faith", "Mengajukan dispute atas pembatalan."),
        AuditEntry("d1", now.minusHours(2), "Chris", "Mengajukan pembatalan: hasil tugas belum lengkap.")
    )

    // ---------- Query ----------

    fun logFor(entityId: String): List<AuditEntry> =
        audit.filter { it.entityId == entityId }.sortedByDescending { it.time }

    fun verification(id: String) = verifications.firstOrNull { it.id == id }
    fun report(id: String) = reports.firstOrNull { it.id == id }
    fun dispute(id: String) = disputes.firstOrNull { it.id == id }
    fun transaction(id: String) = transactions.firstOrNull { it.id == id }

    val pendingVerifications: List<StudentVerification>
        get() = verifications.filter {
            it.status == VerificationStatus.PENDING || it.status == VerificationStatus.IN_REVIEW
        }.sortedBy { it.submittedAt }

    fun requestReports() = reports.filter { it.target == ReportTarget.REQUEST }.sortedByDescending { it.createdAt }
    fun userReports() = reports.filter { it.target == ReportTarget.USER }.sortedByDescending { it.createdAt }

    fun openRequestReports() = requestReports().filter { it.status.isOpen }
    fun openDisputes() = disputes.filter { it.status.isOpen }
    fun openUserReports() = userReports().filter { it.status.isOpen }
    fun problemTransactions() = transactions.filter {
        it.status == PlatformTxStatus.FAILED || it.status == PlatformTxStatus.PENDING ||
                it.status == PlatformTxStatus.NEEDS_FIX
    }

    /** Total item yang menunggu tindakan Admin (untuk badge notifikasi). */
    val pendingCount: Int
        get() = pendingVerifications.size + openRequestReports().size + openDisputes().size +
                openUserReports().size + problemTransactions().size

    /** Dana yang sedang ditahan platform = assignment aktif yang sudah dibayar. */
    fun heldFunds(): Long = SosRepository.assignments
        .filter { it.status.isActive && it.status != AssignmentStatus.PENDING_AGREEMENT }
        .sumOf { it.agreedPrice }

    // ---------- Dari sisi user ----------

    fun submitReport(target: ReportTarget, targetId: String, reporterId: String, reason: String) {
        val existing = reports.firstOrNull { it.target == target && it.targetId == targetId && it.status.isOpen }
        if (existing != null) {
            replace(existing.copy(reportCount = existing.reportCount + 1,
                priority = if (existing.reportCount + 1 >= 3) Priority.HIGH else existing.priority))
        } else {
            val prefix = if (target == ReportTarget.REQUEST) "REQ" else "RPT"
            reports.add(
                Report(
                    UUID.randomUUID().toString(), "$prefix-${2200 + reports.size}", target, targetId,
                    reporterId, reason, "Dilaporkan dari aplikasi: $reason"
                )
            )
        }
        log(targetId, SosRepository.user(reporterId)?.fullName ?: "Pengguna", "Melaporkan: $reason")
    }

    fun submitDispute(assignmentId: String, raisedBy: String, reason: String) {
        if (disputes.any { it.assignmentId == assignmentId && it.status.isOpen }) return
        val dispute = Dispute(
            UUID.randomUUID().toString(), "DSP-${210 + disputes.size}", assignmentId, raisedBy, reason,
            statements = listOf(DisputeStatement(raisedBy, reason)),
            evidence = emptyList()
        )
        disputes.add(dispute)
        log(dispute.id, SosRepository.user(raisedBy)?.fullName ?: "Pengguna", "Mengajukan dispute: $reason")
    }

    // ---------- Verifikasi ----------

    fun startReview(id: String) {
        val v = verification(id) ?: return
        if (v.status == VerificationStatus.PENDING) replace(v.copy(status = VerificationStatus.IN_REVIEW))
    }

    fun decideVerification(id: String, approve: Boolean, note: String) {
        val v = verification(id) ?: return
        replace(
            v.copy(
                status = if (approve) VerificationStatus.APPROVED else VerificationStatus.REJECTED,
                note = note.trim().ifBlank { null },
                decidedAt = LocalDateTime.now()
            )
        )
        SosRepository.user(v.userId)?.let { SosRepository.replaceUser(it.copy(isVerified = approve)) }
        log(
            v.userId, adminName,
            if (approve) "Verifikasi KTM disetujui." else "Verifikasi KTM ditolak: ${note.trim()}"
        )
    }

    // ---------- Pengguna ----------

    /** days = null berarti penangguhan sampai dibuka manual. */
    fun suspendUser(userId: String, days: Int?, reason: String) {
        val user = SosRepository.user(userId) ?: return
        val until = days?.let { LocalDate.now().plusDays(it.toLong()) }
        SosRepository.replaceUser(
            user.copy(status = AccountStatus.SUSPENDED, suspension = SuspensionInfo(until, reason.trim()))
        )
        log(userId, adminName, "Akun ditangguhkan ${days?.let { "$it hari" } ?: "tanpa batas waktu"}: ${reason.trim()}")
    }

    fun reactivateUser(userId: String, reason: String) {
        val user = SosRepository.user(userId) ?: return
        SosRepository.replaceUser(user.copy(status = AccountStatus.ACTIVE, suspension = null))
        log(userId, adminName, "Akun diaktifkan kembali: ${reason.trim()}")
    }

    fun reportsAgainst(userId: String) = reports.count { it.target == ReportTarget.USER && it.targetId == userId }

    // ---------- Moderasi request ----------

    fun moderateRequest(reportId: String, action: ModerationAction, reason: String) {
        val report = report(reportId) ?: return
        when (action) {
            ModerationAction.HIDE -> SosRepository.setRequestHidden(report.targetId, true)
            ModerationAction.REMOVE -> SosRepository.removeRequest(report.targetId)
            ModerationAction.KEEP -> SosRepository.setRequestHidden(report.targetId, false)
        }
        replace(
            report.copy(
                status = if (action == ModerationAction.KEEP) CaseStatus.DISMISSED else CaseStatus.RESOLVED,
                resolution = action.label
            )
        )
        log(report.targetId, adminName, "${action.label}: ${reason.trim()}")
    }

    fun restoreRequest(reportId: String) {
        val report = report(reportId) ?: return
        SosRepository.setRequestHidden(report.targetId, false)
        replace(report.copy(status = CaseStatus.DISMISSED, resolution = "Request dipulihkan"))
        log(report.targetId, adminName, "Request dipulihkan dan tayang kembali.")
    }

    fun markInReview(reportId: String) {
        val report = report(reportId) ?: return
        if (report.status == CaseStatus.OPEN) replace(report.copy(status = CaseStatus.IN_REVIEW))
    }

    // ---------- Laporan pengguna & dispute ----------

    fun resolveUserReport(reportId: String, resolution: String, suspendDays: Int? = null, suspend: Boolean = false) {
        val report = report(reportId) ?: return
        if (suspend) suspendUser(report.targetId, suspendDays, "${report.reason} (${report.code})")
        replace(report.copy(status = CaseStatus.RESOLVED, resolution = resolution))
        log(report.id, adminName, resolution)
    }

    fun dismissReport(reportId: String) {
        val report = report(reportId) ?: return
        replace(report.copy(status = CaseStatus.DISMISSED, resolution = "Laporan ditutup tanpa tindakan"))
        log(report.id, adminName, "Laporan ditutup tanpa tindakan.")
    }

    fun resolveDispute(disputeId: String, decision: DisputeDecision, refundAmount: Long, basis: String) {
        val dispute = dispute(disputeId) ?: return
        val assignment = SosRepository.assignment(dispute.assignmentId) ?: return
        SosRepository.applyDisputeDecision(assignment.id, decision, refundAmount)

        val title = SosRepository.request(assignment.requestId)?.title.orEmpty()
        val refund = when (decision) {
            DisputeDecision.FULL_REFUND -> assignment.agreedPrice
            DisputeDecision.PARTIAL_REFUND -> refundAmount.coerceIn(0, assignment.agreedPrice)
            DisputeDecision.RELEASE_TO_HELPER -> 0
        }
        if (refund > 0) addPlatformTx(PlatformTxType.REFUND, assignment.requesterId, "Refund: $title", refund)
        if (assignment.agreedPrice - refund > 0) {
            addPlatformTx(PlatformTxType.PAYOUT, assignment.helperId, "Rilis dana: $title", assignment.agreedPrice - refund)
        }

        val resolution = "${decision.label}${if (decision == DisputeDecision.PARTIAL_REFUND) " (${refund.idr()})" else ""}"
        replace(dispute.copy(status = CaseStatus.RESOLVED, resolution = resolution))
        log(dispute.id, adminName, "Keputusan: $resolution. Dasar: ${basis.trim()}")
    }

    fun requestMoreEvidence(disputeId: String) {
        val dispute = dispute(disputeId) ?: return
        replace(dispute.copy(status = CaseStatus.IN_REVIEW))
        log(dispute.id, adminName, "Meminta bukti tambahan dari kedua pihak.")
    }

    // ---------- Transaksi ----------

    fun requestPayoutFix(txId: String, note: String) {
        val tx = transaction(txId) ?: return
        replace(tx.copy(status = PlatformTxStatus.NEEDS_FIX))
        log(txId, adminName, "Meminta pengguna memperbaiki data rekening: ${note.trim()}")
    }

    fun retryTransaction(txId: String) {
        val tx = transaction(txId) ?: return
        replace(tx.copy(status = PlatformTxStatus.SUCCESS, failureReason = null))
        log(txId, adminName, "Transaksi diproses ulang dan berhasil.")
    }

    // ---------- Internal ----------

    private fun addPlatformTx(type: PlatformTxType, userId: String, title: String, amount: Long) {
        val prefix = when (type) {
            PlatformTxType.PAYMENT -> "PAY"
            PlatformTxType.REFUND -> "RFD"
            PlatformTxType.PAYOUT -> "TRX"
        }
        transactions.add(
            PlatformTransaction(
                UUID.randomUUID().toString(), "$prefix-${1000 + transactions.size}", type, userId, title,
                amount, PlatformTxStatus.SUCCESS, "Saldo SOS", "Dompet SOS", LocalDateTime.now(),
                "SOS-${(100000..999999).random()}"
            )
        )
    }

    private fun log(entityId: String, actor: String, text: String) {
        audit.add(AuditEntry(entityId, LocalDateTime.now(), actor, text))
    }

    private fun Long.idr() = "Rp" + String.format(java.util.Locale.GERMANY, "%,d", this)

    private fun replace(v: StudentVerification) { verifications.replaceById(v) { it.id == v.id } }
    private fun replace(r: Report) { reports.replaceById(r) { it.id == r.id } }
    private fun replace(d: Dispute) { disputes.replaceById(d) { it.id == d.id } }
    private fun replace(t: PlatformTransaction) { transactions.replaceById(t) { it.id == t.id } }

    private fun <T> MutableList<T>.replaceById(item: T, match: (T) -> Boolean) {
        val index = indexOfFirst(match)
        if (index >= 0) this[index] = item
    }
}

val CaseStatus.isOpen: Boolean
    get() = this == CaseStatus.OPEN || this == CaseStatus.IN_REVIEW
