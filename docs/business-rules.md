# Business Rules & State Machine — SOS

Status yang disebut proposal: **Open/Floating, Pending Agreement, On Progress, Completion Requested, Completed, Cancelled**. Pemetaan ke Request vs Assignment di bawah adalah interpretasi dari proposal; konfirmasi ke System Analyst (Yoel) bila ragu.

## 1. Status Request

```
OPEN (Floating)  ──(assignment aktif = helpersNeeded)──▶  FILLED / IN_PROGRESS
     │                                                         │
     └─(requester cancel, belum ada assignment aktif)─▶ CANCELLED
                                     (semua assignment selesai/batal) ─▶ CLOSED
```

- Request **OPEN** tampil di Explore dan bisa di-reply.
- Edit request hanya oleh pemilik dan hanya saat belum ada assignment `ON_PROGRESS` (usulan; konfirmasi).
- Cancel request oleh pemilik; assignment yang sudah berjalan mengikuti alur cancellation assignment, bukan dibatalkan diam-diam.

## 2. Status Help Assignment

```
            Select Helper / terima Direct Request
                          │
                          ▼
                 PENDING_AGREEMENT
        (kedua pihak approve agreement + pembayaran SUCCESS)
                          │
                          ▼
                    ON_PROGRESS ◀────────────────┐
                    │         │                  │ (pihak lain menolak)
   request completion         request cancellation
                    ▼         ▼                  │
     COMPLETION_REQUESTED   CANCELLATION_REQUESTED
          │ confirm              │ confirm
          ▼                      ▼
      COMPLETED              CANCELLED
   (release dana,         (refund sesuai
    buka review)           cancellation policy)
```

Aturan:
1. Dibuat saat Requester **Select Helper** (dari Reply) atau Helper **menerima Direct Request**.
2. `PENDING_AGREEMENT → ON_PROGRESS` hanya jika `requesterApprovedAt` & `helperApprovedAt` terisi **dan** transaksi PAYMENT untuk assignment berstatus SUCCESS.
3. Pembatalan saat `PENDING_AGREEMENT` (belum bayar) → `CANCELLED` tanpa refund.
4. Completion: pengaju ≠ pengonfirmasi. Penolakan mengembalikan ke `ON_PROGRESS`.
5. Cancellation saat `ON_PROGRESS`: pengaju ≠ pengonfirmasi. Jika tidak disepakati → **Dispute** ke Admin.
6. `COMPLETED` dan `CANCELLED` adalah status akhir (immutable kecuali keputusan Admin pada dispute, dan itu tercatat di AuditLog).
7. Setiap transisi: validasi status asal, validasi aktor adalah pihak yang berhak, tulis AuditLog, kirim Notification.

## 3. Pembayaran

- Requester **fund** assignment via payment provider → Transaction `PAYMENT`.
- Status pembayaran diperbarui dari **webhook** provider (verifikasi signature, idempotent berdasarkan providerRef).
- Saat `COMPLETED` → Transaction `RELEASE`; dana jadi milik Helper (balance atau payout langsung — lihat open questions).
- Saat `CANCELLED` setelah funded → Transaction `REFUND` sesuai cancellation policy di agreement.
- Helper **Withdraw Earnings** → Transaction `PAYOUT` via provider.
- Mutasi saldo + perubahan status dilakukan dalam satu transaksi DB.
- Komisi/fee platform belum didefinisikan di proposal.

## 4. Review & Reputasi

- Hanya untuk assignment `COMPLETED`, satu review per assignment, oleh Requester untuk Helper.
- Rating 1–5 + komentar. Agregat rating & jumlah bantuan selesai ditampilkan di profil Helper.

## 5. Verifikasi & Akses

- User harus email-verified dan Student ID terverifikasi (oleh Admin) sebelum bisa membuat request / menjadi Helper (usulan; konfirmasi apakah dibatasi atau hanya badge).
- User `SUSPENDED` tidak bisa membuat request, reply, atau chat baru.
- User tidak bisa reply / jadi Helper untuk request miliknya sendiri.
- User yang saling block tidak bisa saling chat atau direct request.

## 6. Admin

- Verify User (setujui/tolak Student ID)
- Review Reports → Moderate Request (sembunyikan/hapus) atau Suspend User
- Handle Dispute → putuskan release/refund (parsial?) untuk assignment terkait
- Semua aksi Admin masuk AuditLog
