# Roadmap & Fase — SOS

Jadwal dari proposal (Fase Pelaksanaan Projek, 2026).

| Fase | Nama | Periode |
|---|---|---|
| 1 | Initiation & Feasibility | 13–19 Sep |
| 2 | Requirements Engineering | 20–26 Sep |
| 3 | System & Architecture Design | 27 Sep – 3 Okt |
| 4 | Core Development | 4–24 Okt |
| 5 | QA & Testing | 25–31 Okt |
| 6 | Deployment | 1–7 Nov |
| 7 | Documentation & Evaluation | 8–14 Nov |

## Modul Core Development (fase 4) — urutan disarankan

| ID | Modul | Bergantung pada |
|---|---|---|
| 4.1 | Authentication & Account Management | — |
| 4.2 | User Profile & Student Verification | 4.1 |
| 4.3 | Request Management | 4.1 |
| 4.4 | Explore, Search, Filter & Helper Profile | 4.2, 4.3 |
| 4.5 | Reply & Helper Section | 4.3 |
| 4.6 | Help Assignment & Contract Agreement | 4.5 |
| 4.7 | Real-Time Chat & File Attachment | 4.6 |
| 4.8 | Ongoing Task & Request Status Management | 4.6 |
| 4.9 | Completion, Cancellation & Review | 4.8, 4.11 |
| 4.10 | History & Notification | semua modul di atas |
| 4.11 | Transaction / Payment Integration | 4.6 |
| 4.12 | Admin, Report & Moderation | 4.2, 4.3 |

## Fase lain yang relevan untuk kode

- 3.3 Database & ERD Design, 3.4 API & Backend Structure, 3.5 Repository & Dev Environment Setup
- 5.x Test case, functional test, integrasi FE–BE, regression, E2E & UAT (QA: Jayden)
- 6.x Environment produksi/testing, build & deploy, verifikasi final
- 7.x Dokumentasi teknis, user guide, UAT review, handover

## Definition of Done (usulan)

- Lolos lint + typecheck + test
- Endpoint baru lolos checklist di `security.md`
- Transisi status sesuai `business-rules.md` dan ada test untuk transisi valid & tidak valid
- Test case untuk QA diperbarui bila perilaku berubah
