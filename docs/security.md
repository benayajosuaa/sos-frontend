# Security Requirements — SOS

Dari bagian *Implementasi Keamanan* di proposal. Semua poin ini wajib diterapkan saat menulis kode.

| Layer | Implementasi | Tujuan |
|---|---|---|
| Authentication | Password hash Argon2/bcrypt. JWT access token (umur pendek) + refresh token (rotasi, bisa di-revoke). | Tidak ada plaintext password; kurangi penyalahgunaan sesi |
| Authorization | Auth + authz di setiap endpoint. Role `USER`/`ADMIN`. Validasi **kepemilikan resource**. | Cegah akses/ubah data user lain |
| Transport | HTTPS/TLS untuk semua komunikasi app ↔ server | Lindungi data in transit |
| Input & API | Validasi + sanitasi server-side, rate limiting, error handling aman (tanpa stack trace ke client), batasan ukuran & tipe file | Cegah injection, spam, abuse, upload berbahaya |
| Data sensitif | Student ID, dokumen verifikasi, email, data transaksi tidak tampil publik; hanya pemilik/Admin | Privasi |
| File | Private storage + signed/authenticated URL berumur pendek; batasi format & ukuran | File pribadi tidak bisa diakses via URL publik |
| Transaksi | Data pembayaran sensitif diproses provider; backend simpan identifier, status, nominal saja | Tidak menyimpan data finansial sensitif |
| Moderasi | Report, block, suspend, moderasi Admin, dispute | Lindungi pengguna dari penyalahgunaan |
| Audit | Log login, perubahan status, cancellation, payment, aksi Admin | Investigasi dispute/insiden |
| Database | Tidak diakses langsung dari client; via backend, kredensial di env var, least privilege | Kredensial tidak terekspos |

## Checklist saat membuat endpoint baru

- [ ] Butuh login? Pasang middleware auth.
- [ ] Siapa yang boleh? Cek role **dan** kepemilikan (requester/helper dari assignment terkait).
- [ ] Validasi body/query/params dengan schema.
- [ ] Response tidak membocorkan field sensitif (email, studentId, passwordHash, providerRef internal).
- [ ] Transisi status → AuditLog + Notification.
- [ ] Perlu rate limit?

## Hal yang jangan dilakukan

- Jangan commit `.env`, kunci provider payment, atau service account.
- Jangan memakai public bucket untuk Student ID, attachment, atau file chat.
- Jangan mempercayai status pembayaran dari client; hanya dari webhook/verifikasi server ke provider.
- Jangan memakai Supabase/DB client langsung dari aplikasi mobile.
