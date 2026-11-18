# Product Overview — SOS (Student On Study)

Sumber: *Proposal Pengembangan Aplikasi SOS, V2 (Revisi-002)*.

## Latar Belakang

Mahasiswa umumnya mencari bantuan akademik secara informal: lewat teman, grup chat, komunitas, atau media sosial. Cara ini tidak memberi mekanisme terstruktur untuk:
- melihat kompetensi pemberi bantuan,
- mendiskusikan kebutuhan dan menyepakati pekerjaan,
- memantau proses pengerjaan,
- memberi penilaian setelah selesai,
- memastikan harga, waktu, kualitas, dan penyelesaian jika ada masalah.

SOS menyatukan pencarian Helper, komunikasi, agreement, pembayaran, pemantauan, penyelesaian, dan review dalam satu alur. Helper juga mendapat imbalan atas kemampuannya.

## Pengguna

| Aktor | Deskripsi |
|---|---|
| **User** | Mahasiswa terverifikasi. Bisa bertindak sebagai **Requester** (membuat request, memilih Helper, membayar, mereview) dan **Helper** (mencari request, merespons, mengerjakan, menerima imbalan) dengan akun yang sama. |
| **Admin** | Verifikasi pengguna, moderasi request, menangani report, dispute/masalah transaksi, suspend akun. |
| **Payment Gateway** | Aktor eksternal (Midtrans/Xendit) yang memproses payment, refund, payout. |

## Kebutuhan Fungsional Utama

1. Registrasi & verifikasi mahasiswa (nama lengkap, email, universitas, jurusan, Student ID; verifikasi email + Student ID)
2. Create / Edit / View / Cancel Request
3. Explore: pencarian, filter, sorting untuk **Request** dan **Helper** (dua tab)
4. Profil Helper: skills, universitas, jurusan, rating, jumlah bantuan, review, portfolio, LinkedIn, website
5. Reply ke request (Helper) dan Direct Request (Requester → Helper dari profil)
6. Accept Help / Select Helper → membentuk Help Assignment
7. Contract Agreement
8. Real-time chat + kirim gambar/file, riwayat percakapan tersimpan
9. Status pekerjaan, Ongoing Task
10. Completion & Cancellation (dua pihak)
11. Rating & review
12. History & notifikasi
13. Pembayaran, transaksi, balance, withdraw (via payment provider)
14. Fitur Admin: report, moderasi, verifikasi, dispute, suspend

## Field Request

subject/kategori, title, description, estimated time, deadline, budget, jumlah Helper (response) yang dibutuhkan, tags, attachment (opsional).

## Isi Contract Agreement

scope pekerjaan, timeline, pembayaran, standar hasil (quality standard), cancellation policy, confidentiality, dispute resolution.

## UX & Navigasi

- Bottom navigation: **Home, Explore, Chat, Profile**.
- **Home**: request aktif, ongoing task, rekomendasi Helper, request dengan deadline terdekat.
- **Explore**: dua tab (Request | Helper) + search, filter, sort.
- Interaksi lanjut via reply atau real-time chat.
- Alur penerimaan bertahap: komunikasi awal → Accept Help → Contract Agreement → pengerjaan → completion → review.
- Status tiap request/bantuan terlihat di **Ongoing Task** dan **History**.
- Profil Helper ditampilkan lengkap agar Requester bisa menilai sebelum memilih.

Desain UI/UX dibuat oleh Cathleen. Jika file desain (Figma, dll.) tersedia, ikuti desain tersebut; minta link/aset ke user bila perlu.
