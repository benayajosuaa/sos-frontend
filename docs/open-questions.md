# Open Questions — SOS

Hal yang belum diputuskan atau ambigu di dokumen sumber. Claude: **tanyakan ke user** sebelum mengambil keputusan yang menyentuh poin ini, lalu catat jawabannya di sini.

## Teknis
1. ORM / query builder: Prisma, Drizzle, Knex, atau SQL murni?
2. Payment provider final: Midtrans atau Xendit?
3. Storage final: Supabase Storage atau S3-compatible lain?
4. Push: Expo Notifications atau FCM langsung?
5. Hosting backend & database (proposal: "Cloud Platform / Managed Server").
6. State management & data fetching mobile (Zustand/Redux, TanStack Query?), navigasi (Expo Router?).
7. Library validasi (Zod?) dan struktur monorepo (npm/pnpm workspaces, Turborepo?).
8. Panel Admin: di dalam aplikasi mobile atau web terpisah?

## Bisnis
9. Release payment: masuk **balance** internal lalu withdraw, atau langsung payout ke Helper? (Diagram punya *View Balance* dan *Withdraw Earnings*, tapi *Release Payment* juga include *Process Payout*.)
10. Fee/komisi platform: ada? berapa? dipotong dari mana?
11. Kebijakan refund pada cancellation: penuh, parsial, tergantung progres? Siapa yang menetapkan (agreement atau aturan platform)?
12. Apakah chat boleh dimulai **sebelum** assignment (diskusi awal sebelum Select Helper)? Proposal menyebut "setelah kedua pihak berkomunikasi melalui chat dan Requester memilih Helper".
13. Apakah agreement bisa direvisi/negosiasi (versi), atau sekali approve?
14. Harga per assignment = budget request dibagi jumlah Helper, atau dinegosiasikan per Helper?
15. Apakah user yang belum terverifikasi Student ID dibatasi fiturnya, atau hanya tidak mendapat badge?
16. Apakah Helper bisa mereview Requester (proposal hanya menyebut Requester → Helper)?
17. Batas waktu otomatis: jika pihak lain tidak konfirmasi completion dalam X hari, apakah auto-complete?
18. Status request saat jumlah Helper terpenuhi: tetap tampil di Explore atau disembunyikan?

## Catatan dokumen
- Riwayat dokumen proposal menyebut Feb–Mar 2026, sementara jadwal fase Sep–Nov 2026. Jadwal fase dipakai sebagai acuan.
