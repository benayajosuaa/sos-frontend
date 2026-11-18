package com.example.sos.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/*
 * Model untuk area Admin (lihat docs/use-cases.md bagian 5 dan
 * docs/domain-model.md: StudentVerification, Report, Dispute, AuditLog).
 */

enum class UserRole { USER, ADMIN }

enum class AccountStatus(val label: String) {
    ACTIVE("Aktif"),
    SUSPENDED("Ditangguhkan")
}

enum class VerificationStatus(val label: String) {
    PENDING("Menunggu"),
    IN_REVIEW("Ditinjau"),
    APPROVED("Disetujui"),
    REJECTED("Ditolak")
}

/** Pengajuan verifikasi KTM. Data sensitif: hanya Admin dan pemilik. */
data class StudentVerification(
    val id: String,
    val userId: String,
    val nim: String,
    val program: String,
    val university: String,
    val submittedAt: LocalDateTime,
    val ktmFileName: String,
    val matchesProfile: Boolean,
    val flag: String? = null,
    val status: VerificationStatus = VerificationStatus.PENDING,
    val note: String? = null,
    val decidedAt: LocalDateTime? = null
)

enum class ReportTarget(val label: String) { REQUEST("Request"), USER("Pengguna") }

enum class CaseStatus(val label: String) {
    OPEN("Perlu tindakan"),
    IN_REVIEW("Ditinjau"),
    RESOLVED("Selesai"),
    DISMISSED("Ditutup")
}

enum class Priority(val label: String) { HIGH("Prioritas tinggi"), NORMAL("Normal") }

data class Report(
    val id: String,
    val code: String,
    val target: ReportTarget,
    val targetId: String,
    val reporterId: String,
    val reason: String,
    val description: String,
    val reportCount: Int = 1,
    val priority: Priority = Priority.NORMAL,
    val status: CaseStatus = CaseStatus.OPEN,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val resolution: String? = null
)

data class DisputeStatement(val userId: String, val text: String)

data class Dispute(
    val id: String,
    val code: String,
    val assignmentId: String,
    val raisedBy: String,
    val reason: String,
    val statements: List<DisputeStatement>,
    val evidence: List<String>,
    val priority: Priority = Priority.HIGH,
    val status: CaseStatus = CaseStatus.OPEN,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val resolution: String? = null
)

enum class DisputeDecision(val label: String) {
    FULL_REFUND("Refund penuh ke requester"),
    PARTIAL_REFUND("Refund sebagian"),
    RELEASE_TO_HELPER("Lepas dana ke helper")
}

enum class ModerationAction(val label: String) {
    HIDE("Sembunyikan request"),
    REMOVE("Hapus request"),
    KEEP("Biarkan tayang")
}

enum class PlatformTxType(val label: String) {
    PAYMENT("Pembayaran"),
    REFUND("Refund"),
    PAYOUT("Pencairan")
}

enum class PlatformTxStatus(val label: String) {
    SUCCESS("Berhasil"),
    PENDING("Menunggu"),
    FAILED("Gagal"),
    NEEDS_FIX("Menunggu perbaikan")
}

/** Transaksi seluruh platform untuk monitoring Admin (tanpa data kartu/rekening lengkap). */
data class PlatformTransaction(
    val id: String,
    val code: String,
    val type: PlatformTxType,
    val userId: String,
    val title: String,
    val amount: Long,
    val status: PlatformTxStatus,
    val method: String,
    val maskedAccount: String,
    val createdAt: LocalDateTime,
    val gatewayRef: String,
    val failureReason: String? = null
)

/** Jejak audit sederhana untuk entitas (user, request, kasus, transaksi). */
data class AuditEntry(
    val entityId: String,
    val time: LocalDateTime,
    val actor: String,
    val text: String
)

data class SuspensionInfo(
    val until: LocalDate?,
    val reason: String
)
