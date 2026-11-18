# Use Cases — SOS

Disalin dari 5 use case diagram (PlantUML): Account & Personal Area, Request & Discovery, Collaboration & Task, Payment & Balance, Safety & Administration.

Notasi: `A ⟶include⟶ B` = A selalu memanggil B. `A ⟶extend⟶ B` = A adalah aksi opsional yang dapat dilakukan dari/setelah B.

## 1. Account & Personal Area — aktor: User

| Use case | Catatan |
|---|---|
| Register Account | include **Verify Email**, include **Submit Student ID** |
| Login | **Reset Password** extend Login |
| Manage Profile | data profil, skills, portfolio, LinkedIn, website |
| View Activity History | request, help, chat, completion, review |
| View Transaction History | pembayaran, refund, release, withdraw |

## 2. Request & Discovery — aktor: Requester, Helper

### Request Management (Requester)
- Create Request
- Edit Own Request
- Cancel Own Request
- View Request Details (juga Helper)

### Request Discovery
- **Explore Requests** (Helper) — include **Search / Filter / Sort Requests**
- **Reply to Request** (Helper)
- **Review Replies** (Requester)
- **Select Helper** (Requester) — extend Review Replies → membentuk Help Assignment

### Helper Discovery
- **Explore Helpers** (Requester) — include **Search / Filter / Sort Helpers**
- **View Helper Profile** — extend Explore Helpers
- **Send Direct Request** (Requester) — extend View Helper Profile
- **Respond to Direct Request** (Helper)

## 3. Collaboration & Task — aktor: Requester, Helper

### Agreement
- **Review Contract Agreement** (keduanya)
- **Approve Contract Agreement** (keduanya) — extend Review Contract Agreement

### Task Management
- **View Ongoing Task** (keduanya)
- **Request Completion** (keduanya)
- **Confirm Completion** (keduanya; pihak yang bukan pengaju)
- **Submit Rating & Review** (Requester) — extend Confirm Completion
- **Request Cancellation** (keduanya)
- **Confirm Cancellation** (keduanya; pihak yang bukan pengaju)

### Communication
- **Chat** (keduanya)
- **Send File / Attachment** — extend Chat

## 4. Payment & Balance — aktor: Requester, Helper, Payment Gateway

| Use case | Aktor | Relasi |
|---|---|---|
| Pay / Fund Assignment | Requester | include **Process Payment** |
| Refund Payment | Requester | include **Process Refund** |
| View Balance | Helper | — |
| Withdraw Earnings | Helper | include **Process Payout** |
| Release Payment to Helper | sistem (setelah completion) | include **Process Payout** |
| Process Payment / Refund / Payout | Payment Gateway | — |

> Catatan: di diagram, *Release Payment to Helper* terhubung ke *Process Payout*. Perlu dipastikan apakah release = langsung payout ke rekening Helper, atau masuk ke **balance** internal dulu lalu Helper melakukan *Withdraw Earnings*. Lihat `open-questions.md`.

## 5. Safety & Administration — aktor: User, Admin

### Reporting
- **Report User / Request** (User)
- **Review Reports** (Admin)

### Moderation (Admin)
- Verify User
- Moderate Request — extend Review Reports
- Suspend / Moderate User — extend Review Reports
- Handle Dispute

## Diagram

Use case diagram asli ada di `docs/diagrams/usecase-*.png`.
