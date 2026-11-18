package com.example.sos.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/*
 * Model domain SOS (lihat docs/domain-model.md).
 * Satu akun bisa jadi Requester dan Helper sekaligus: peran ditentukan
 * oleh relasi ke Request / HelpAssignment, bukan field role di User.
 */

data class User(
    val id: String,
    val fullName: String,
    val university: String,
    val major: String,
    /** Data sensitif: hanya ditampilkan ke pemilik akun. */
    val email: String = "",
    val rating: Double = 0.0,
    val helpsCount: Int = 0,
    val requestsCount: Int = 0,
    val reviewsCount: Int = 0,
    val skills: List<String> = emptyList(),
    val isVerified: Boolean = true,
    val presence: Presence = Presence.OFFLINE,
    val linkedInUrl: String? = null,
    val websiteUrl: String? = null,
    val portfolioUrl: String? = null,
    val recentHelps: List<RecentHelp> = emptyList(),
    val role: UserRole = UserRole.USER,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val suspension: SuspensionInfo? = null,
    val joinedAt: LocalDate = LocalDate.now().minusMonths(3)
)

enum class Presence(val label: String) {
    ONLINE("Online"),
    ONLINE_TODAY("Online today"),
    OFFLINE("Offline")
}

data class RecentHelp(
    val title: String,
    val rating: Int,
    val timeAgo: String,
    val tags: List<String>
)

enum class EstimatedTime(val label: String) {
    FAST("Fast (< 1 day)"),
    MEDIUM("Medium (1–3 days)"),
    LONG("Long (> 3 days)")
}

/** Status Request (lihat docs/business-rules.md bagian 1). */
enum class RequestStatus(val label: String) {
    OPEN("Floating"),
    IN_PROGRESS("On Progress"),
    CLOSED("Completed"),
    CANCELLED("Cancelled")
}

data class Request(
    val id: String,
    val requesterId: String,
    val subject: String,
    val title: String,
    val description: String,
    val tags: List<String>,
    val estimatedTime: EstimatedTime,
    val deadline: LocalDate,
    val deadlineTime: LocalTime? = null,
    val budget: Long,
    val helpersNeeded: Int = 1,
    val status: RequestStatus = RequestStatus.OPEN,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val attachmentUri: String? = null,
    val attachmentName: String? = null,
    val hasSampleImage: Boolean = false,
    /** Terisi kalau ini Direct Request ke Helper tertentu. */
    val targetHelperId: String? = null,
    /** Disembunyikan Admin hasil moderasi; tidak tampil di Explore. */
    val isHidden: Boolean = false
)

data class Reply(
    val id: String,
    val requestId: String,
    val helperId: String,
    val message: String,
    val portfolioUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/** Status Help Assignment (lihat docs/business-rules.md bagian 2). */
enum class AssignmentStatus(val label: String) {
    PENDING_AGREEMENT("Pending Agreement"),
    ON_PROGRESS("On Progress"),
    COMPLETION_REQUESTED("Waiting Confirmation"),
    CANCELLATION_REQUESTED("Cancellation Requested"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    val isActive: Boolean
        get() = this == PENDING_AGREEMENT ||
                this == ON_PROGRESS ||
                this == COMPLETION_REQUESTED ||
                this == CANCELLATION_REQUESTED
}

/** Unit kerja: 1 Requester ↔ 1 Helper untuk satu Request. */
data class HelpAssignment(
    val id: String,
    val requestId: String,
    val requesterId: String,
    val helperId: String,
    val status: AssignmentStatus,
    val agreedPrice: Long,
    val createdAt: LocalDate,
    val acceptedAt: LocalDate? = null,
    val deadline: LocalDate,
    val completionRequestedBy: String? = null,
    val completedAt: LocalDateTime? = null,
    val cancellationRequestedBy: String? = null,
    val cancellationReason: String? = null,
    val cancelledAt: LocalDateTime? = null,
    val rating: Int? = null,
    val reviewComment: String? = null
)

/**
 * Satu baris di Ongoing Tasks / History: Request beserta Assignment-nya.
 * assignment = null berarti request masih Floating (belum ada Helper).
 */
data class TaskItem(
    val request: Request,
    val assignment: HelpAssignment?
) {
    val statusLabel: String
        get() = assignment?.status?.label ?: request.status.label

    val isFloating: Boolean
        get() = assignment == null && request.status == RequestStatus.OPEN
}

// ---------- Chat ----------

/** Percakapan antara Requester dan satu Helper untuk satu Request. */
data class Conversation(
    val id: String,
    val requestId: String,
    val requesterId: String,
    val helperId: String,
    /** Requester menolak bantuan dari Helper ini (Decline help). */
    val declined: Boolean = false
)

enum class MessageType {
    TEXT,
    /** Kartu detail request (isi diambil dari Request terbaru). */
    REQUEST,
    FILE,
    /** Catatan sistem, mis. "Request has been sent." */
    SYSTEM
}

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val type: MessageType,
    val text: String = "",
    val attachmentName: String? = null,
    val sentAt: LocalDateTime = LocalDateTime.now()
)

// ---------- Wallet ----------

enum class TransactionType(val isCredit: Boolean) {
    TOP_UP(true),
    PAYMENT(false),
    RELEASE(true),
    REFUND(true),
    WITHDRAW(false)
}

/** Mutasi saldo. Hanya identifier & nominal, tanpa data kartu/rekening. */
data class Transaction(
    val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: Long,
    val title: String,
    val date: LocalDateTime,
    val assignmentId: String? = null
) {
    val signedAmount: Long
        get() = if (type.isCredit) amount else -amount
}
