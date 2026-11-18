# Domain Model — SOS

Entitas di bawah diturunkan dari proposal (bagian Database, Strategi, Proses Bisnis) dan use case diagram. Nama field adalah **usulan**; ERD final menjadi acuan jika sudah ada (fase 3.3).

## Entitas Inti

### User
- id, fullName, email (unique), passwordHash, university, major, studentId
- emailVerifiedAt, verificationStatus (`UNVERIFIED | PENDING | VERIFIED | REJECTED`)
- role (`USER | ADMIN`) — **bukan** Requester/Helper
- profile: bio, skills[], portfolioUrl/files, linkedinUrl, websiteUrl, avatar
- status akun (`ACTIVE | SUSPENDED`)
- agregat reputasi (rating rata-rata, jumlah bantuan selesai) — boleh dihitung atau di-cache

### StudentVerification
- userId, studentIdFile (private storage), status, reviewedBy (Admin), reviewedAt, note

### Request
- id, requesterId → User
- subject/category, title, description, tags[]
- estimatedTime, deadline, budget, helpersNeeded (int ≥ 1)
- attachments[]
- status (lihat `business-rules.md`)
- type: dari Explore (publik) atau **Direct Request** ke Helper tertentu (targetHelperId)

### Reply
- id, requestId, helperId → User, message, createdAt
- status (`PENDING | ACCEPTED | DECLINED | WITHDRAWN`)
- Helper tidak boleh reply ke request miliknya sendiri

### HelpAssignment  ← pusat proses bisnis
- id, requestId, requesterId, helperId
- status (lihat state machine)
- agreedPrice, agreedDeadline
- completionRequestedBy, completionRequestedAt, completedAt
- cancellationRequestedBy, cancellationReason, cancelledAt
- Unik per (requestId, helperId)
- Jumlah assignment aktif per request ≤ `helpersNeeded`

### ContractAgreement
- id, assignmentId (1:1, atau versi jika boleh revisi)
- scope, timeline, paymentAmount, qualityStandard, cancellationPolicy, confidentiality, disputeResolution
- requesterApprovedAt, helperApprovedAt

### Conversation / Message
- Conversation per assignment (dan mungkin pra-assignment untuk diskusi awal — lihat open questions)
- Message: id, conversationId, senderId, type (`TEXT | IMAGE | FILE`), body, fileKey, createdAt, readAt

### Transaction
- id, assignmentId, userId, type (`PAYMENT | REFUND | RELEASE | PAYOUT/WITHDRAWAL`)
- amount, currency (IDR), status (`PENDING | SUCCESS | FAILED | EXPIRED`)
- provider (`MIDTRANS | XENDIT`), providerRef (identifier), createdAt
- **Tidak menyimpan** data kartu/rekening sensitif

### Balance / Wallet (Helper)
- userId, availableAmount, pendingAmount — atau dihitung dari ledger Transaction

### Review
- id, assignmentId (unik), reviewerId (Requester), revieweeId (Helper), rating (1–5), comment, createdAt

### Notification
- id, userId, type (chat, reply, agreement, deadline, completion, cancellation, payment, …), payload, readAt

### Report
- id, reporterId, targetType (`USER | REQUEST`), targetId, reason, status (`OPEN | IN_REVIEW | RESOLVED | DISMISSED`), handledBy (Admin)

### Dispute
- id, assignmentId, raisedBy, reason, status, resolution, handledBy (Admin)

### Block
- blockerId, blockedId

### AuditLog
- id, actorId, action, entityType, entityId, before, after, ip, createdAt
- Wajib untuk: login, perubahan status assignment, cancellation, payment, aksi Admin

## Relasi Ringkas

```
User 1─* Request (sebagai requester)
Request 1─* Reply *─1 User (helper)
Request 1─* HelpAssignment *─1 User (helper)
HelpAssignment 1─1 ContractAgreement
HelpAssignment 1─1 Conversation 1─* Message
HelpAssignment 1─* Transaction
HelpAssignment 1─0..1 Review
HelpAssignment 1─0..* Dispute
User 1─* Notification, Report, AuditLog
```
