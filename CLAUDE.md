# CLAUDE.md — SOS (Student On Study)

Konteks proyek untuk Claude Code. Baca file ini dulu, lalu buka file di `docs/` sesuai kebutuhan tugas.

## Ringkasan Produk

SOS adalah **aplikasi mobile marketplace bantuan antar mahasiswa**. Mahasiswa yang butuh bantuan (**Requester**) dipertemukan dengan mahasiswa yang punya kemampuan relevan (**Helper**). Seluruh alur — cari, chat, sepakati kontrak, bayar, kerjakan, selesaikan, review — berlangsung di satu aplikasi.

Alur inti:
`Register → Create/Find Request → Select Helper → Contract Agreement → Payment (fund) → Chat & Work → Completion (dua pihak) → Release Payment → Review → History`

- Project code: `SOS-KMR20` · Versi proposal: V2 · Klien/penerima dokumen: David Hareva
- Status dokumen sumber: tahap perancangan (repository belum ditetapkan saat proposal ditulis)

## Aturan Domain yang Tidak Boleh Dilanggar

1. **Satu akun, dua peran.** User bisa jadi Requester dan Helper dengan akun yang sama. Jangan buat tabel/akun terpisah per role, jangan buat field `role` permanen untuk Requester/Helper. Role hanya `USER` atau `ADMIN`. Peran Requester/Helper ditentukan oleh relasi terhadap Request/Assignment.
2. **Help Assignment = unit kerja.** Satu Request bisa butuh >1 Helper. Setiap Helper yang diterima membentuk **satu Help Assignment** (1 Requester ↔ 1 Helper) dengan agreement, chat, status, transaksi, completion, dan review **masing-masing**. Jangan menaruh status pengerjaan di level Request.
3. **Kontrak dulu, baru kerja.** Assignment tidak boleh `ON_PROGRESS` sebelum Contract Agreement disetujui kedua pihak **dan** dana sudah terbayar (funded).
4. **Completion dua pihak.** Satu pihak mengajukan completion, pihak lain mengonfirmasi. Tidak ada penyelesaian sepihak. Cancellation juga: satu pihak request, pihak lain konfirmasi (sengketa → Admin/dispute).
5. **Dana ditahan platform.** Requester membayar lewat payment provider; dana dicatat per assignment dan baru dilepas ke balance Helper setelah completion terkonfirmasi. Cancellation → refund sesuai ketentuan. Helper menarik dana via withdraw/payout.
6. **Review hanya setelah Completed**, diberikan Requester kepada Helper (membangun reputasi Helper).
7. **Data sensitif tidak pernah publik**: Student ID, dokumen verifikasi, email, data transaksi. Hanya pemilik data atau Admin.
8. **Jangan simpan data kartu/finansial sensitif.** Backend hanya menyimpan identifier provider, status, dan nominal.

Detail lengkap: `docs/domain-model.md` dan `docs/business-rules.md`.

## Tech Stack (sesuai proposal)

| Layer | Teknologi |
|---|---|
| Mobile | **Aktual di repo ini:** Android native, Kotlin + Jetpack Compose (Material 3, Navigation Compose). Proposal awal menyebut React Native + Expo. |
| Backend API | Node.js + Express.js + TypeScript |
| Database | PostgreSQL |
| Real-time | Socket.IO / WebSocket (chat, status pesan) |
| File storage | Supabase Storage / S3-compatible, **private bucket + signed URL** |
| Auth | JWT access token + refresh token; password hash Argon2/bcrypt |
| Push notification | Expo Notifications / Firebase Cloud Messaging |
| Payment | Midtrans / Xendit (belum diputuskan) |
| VCS | Git + GitHub (PR-based) |

Hal yang **belum diputuskan** di dokumen — tanyakan ke user sebelum memilih: ORM/query builder (Prisma/Drizzle/Knex), payment provider final, hosting, state management mobile, library validasi. Lihat `docs/open-questions.md`.

## Struktur Repo (aktual)

Repo ini berisi aplikasi **Android (Kotlin + Jetpack Compose)**. Backend belum ada.

```
app/src/main/java/com/example/sos/
├── MainActivity.kt
├── navigation/
│   ├── AppNavigation.kt   # welcome → choose → login / registrasi → "main"
│   └── MainScreen.kt      # area client: bottom nav Home/Explore/Chat/Profile + route detail
├── data/
│   ├── model/Models.kt    # User, Request, Reply, HelpAssignment, Conversation, Message, Transaction, enum status
│   └── SosRepository.kt   # repository in-memory + data contoh (sementara, sampai API siap)
├── ui/components/         # SosComponents.kt (warna & komponen), SosDialogs.kt (pop up), Format.kt
└── feature/
    ├── welcome/
    ├── auth/{choose,login,regis}/   # regis: Regis → Verification (OTP) → StudentProfile → Interest
    ├── home/              # Home + Ongoing Tasks
    ├── explore/           # Explore (tab Request | Helper), HelperInfo
    ├── request/           # RequestInfo, CreateRequest, DirectRequest, EditRequest
    ├── chat/              # ChatList, ChatRoom, ContractAgreement, Review, CancellationSent
    ├── history/           # History (My Requests | My Answers), RequestDetails
    ├── profile/           # Profile (balance, History, Language, Logout), EditProfile
    ├── wallet/            # TopUp, Withdraw (dialog), TransactionHistory
    └── admin/             # Dashboard, Verifikasi, Kelola Pengguna, Moderasi, Laporan & Dispute, Monitoring Transaksi
navigation/AdminNavigation.kt  # area admin (login dengan email admin demo)
data/AdminRepository.kt        # data & aksi admin (simulasi) + audit log
docs/                      # dokumen konteks
CLAUDE.md
```

Catatan:
- `SosRepository` hanya simulasi. Transisi status di sana mengikuti `docs/business-rules.md`, tapi validasi sebenarnya wajib di backend.
- Alur kerja di aplikasi: Reply / Direct Request → Chat → Accept help → Contract Agreement (Agree & Continue = dana ditahan dari balance) → ON_PROGRESS → Request complete / Cancel request → Review.
- Interpretasi yang dipakai (konfirmasi ke System Analyst): "Request complete" oleh **Requester** langsung menyelesaikan dan melepas dana; oleh **Helper** menjadi pengajuan yang harus dikonfirmasi Requester. Pembatalan selalu butuh konfirmasi pihak lain. Pembayaran memakai balance internal (open question #9).
- Top up, withdraw, dan pembayaran masih simulasi tanpa payment provider.
- Area Admin: login `admin@sos.ac.id` (password bebas, simulasi) → `AdminNavigation`. Teks admin berbahasa Indonesia mengikuti desain. User bisa Report request/user dan Raise a dispute; datanya masuk ke antrean Admin. Akun yang ditangguhkan tidak bisa membuat request, reply, atau chat.
- Daftar endpoint backend: `docs/api-endpoints.md`.
- `MainActivity` memasang `SosColorScheme` supaya komponen bawaan Material (picker, checkbox) memakai biru SOS.

## Commands

- Compile: `./gradlew :app:compileDebugKotlin`
- Install ke emulator/device: `./gradlew :app:installDebug`
- Jalankan: `adb shell am start -n com.example.sos/.MainActivity`

## Konvensi Kode

- Mobile (Kotlin): satu file per layar di `feature/<area>/`, komponen bersama di `ui/components/`. Warna SOS: `SosBlue`, `SosLightBlue`, dll. di `SosComponents.kt`.
- Enum status (Request, Assignment, dll.) didefinisikan **satu kali** (`data/model/Models.kt` di mobile; nanti diselaraskan dengan backend).
- Backend per modul: `routes → controller → service → repository`. Business rule (transisi status, validasi kepemilikan) ada di **service**, bukan controller.
- Setiap endpoint: autentikasi + otorisasi berbasis **kepemilikan resource** (contoh: hanya Requester pemilik Request yang bisa edit/cancel; hanya pihak dalam Assignment yang bisa baca chat-nya).
- Validasi input server-side untuk semua endpoint; rate limiting di auth, chat, upload.
- Upload: batasi tipe & ukuran file, simpan di private storage, akses via signed URL berumur pendek.
- Transisi status penting (status assignment, cancellation, payment, aksi Admin, login) **wajib** dicatat ke audit/activity log.
- Perubahan status + mutasi saldo dalam **satu transaksi database**. Webhook payment harus idempotent dan diverifikasi signature-nya.
- Nama di kode dan API pakai bahasa Inggris (`request`, `helper`, `assignment`); dokumentasi/komentar boleh bahasa Indonesia.

## Tim

| Nama | Peran |
|---|---|
| Benaya | Project Manager & Frontend Engineer |
| Cathleen | UI/UX Designer |
| Fronli | Backend Engineer |
| Jayden | Quality Assurance |
| Yoel | System Analyst |

## Indeks Dokumen

- `docs/product-overview.md` — latar belakang, masalah, pengguna, fitur, UX/navigasi
- `docs/use-cases.md` — use case per area (dari 5 use case diagram)
- `docs/domain-model.md` — entitas, relasi, field utama
- `docs/business-rules.md` — state machine & aturan transisi
- `docs/security.md` — kebutuhan keamanan
- `docs/roadmap.md` — fase & modul pengembangan
- `docs/open-questions.md` — hal yang belum diputuskan
