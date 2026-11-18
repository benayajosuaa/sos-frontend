package com.example.sos.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sos.data.model.*
import com.example.sos.data.model.AssignmentStatus
import com.example.sos.data.model.Conversation
import com.example.sos.data.model.EstimatedTime
import com.example.sos.data.model.HelpAssignment
import com.example.sos.data.model.Message
import com.example.sos.data.model.MessageType
import com.example.sos.data.model.Presence
import com.example.sos.data.model.RecentHelp
import com.example.sos.data.model.Reply
import com.example.sos.data.model.Request
import com.example.sos.data.model.RequestStatus
import com.example.sos.data.model.TaskItem
import com.example.sos.data.model.Transaction
import com.example.sos.data.model.TransactionType
import com.example.sos.data.model.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/** Hasil Accept help / Agree & Continue di Contract Agreement. */
sealed interface AcceptResult {
    data class Success(val assignmentId: String) : AcceptResult
    data class InsufficientBalance(val needed: Long, val balance: Long) : AcceptResult
    data object NotAllowed : AcceptResult
}

/**
 * Repository sementara (in-memory) sampai backend siap.
 * Semua list memakai snapshot state, jadi UI Compose otomatis ikut berubah.
 *
 * TODO: Ganti dengan pemanggilan API. Aturan transisi status di sini
 * hanya simulasi; validasi sebenarnya wajib dilakukan di backend
 * (lihat docs/business-rules.md dan docs/security.md).
 */
object SosRepository {

    const val CURRENT_USER_ID = "me"
    const val MIN_WITHDRAW = 10_000L

    val subjects = listOf(
        "Engineering",
        "Mathematics",
        "Computer Science",
        "Physics",
        "Chemistry",
        "Biology",
        "Economics",
        "Business & Management",
        "Design",
        "Language & Literature"
    )

    val cancellationReasonsForRequester = listOf(
        "Helper hasn’t started the work",
        "Helper isn’t responsive",
        "I no longer need help",
        "Others"
    )

    val cancellationReasonsForHelper = listOf(
        "Requester isn’t responsive",
        "Requirements changed",
        "I can’t finish the work",
        "Others"
    )

    var isLoggedIn by mutableStateOf(false)
        private set

    /** Sesi saat ini milik Admin? (simulasi; role sebenarnya dari JWT backend) */
    var isAdminSession by mutableStateOf(false)
        private set

    /** Akun demo Admin. TODO: hapus setelah login lewat backend. */
    const val ADMIN_EMAIL = "admin@sos.ac.id"
    const val ADMIN_ID = "admin"

    var language by mutableStateOf("English")

    private val now: LocalDateTime = LocalDateTime.now()
    private val today: LocalDate = LocalDate.now()

    private val users = mutableStateListOf(
        User(
            id = CURRENT_USER_ID,
            fullName = "Kim Dokja",
            email = "KimDokja4951@gmail.com",
            university = "Universitas Pelita Harapan",
            major = "Product Design",
            rating = 4.9,
            helpsCount = 12,
            skills = listOf(
                "Design", "UI/UX", "Graphic Design",
                "Adobe Illustrator", "Adobe Photoshop"
            ),
            linkedInUrl = "linkedin.com/in/Kim-Dokja",
            websiteUrl = "WatcherofLightandDarkness.com"
        ),
        User(
            id = "klein",
            fullName = "Klein Moretti",
            university = "Universitas Pelita Harapan",
            major = "Product Design",
            rating = 4.9,
            helpsCount = 23,
            requestsCount = 14,
            reviewsCount = 18,
            skills = listOf(
                "Design", "UI/UX", "Graphic Design",
                "Adobe Illustrator", "Adobe Photoshop"
            ),
            presence = Presence.ONLINE,
            linkedInUrl = "linkedin.com/in/kleinmoretti",
            websiteUrl = "kleinmoretti.com",
            portfolioUrl = "PortfolioKlein.com",
            recentHelps = listOf(
                RecentHelp("Modern Landing Page Design", 5, "2d ago", listOf("Design", "UI/UX")),
                RecentHelp("Build landing page", 5, "1w ago", listOf("Design", "UI/UX")),
                RecentHelp("Logo design review", 5, "2w ago", listOf("Design", "Graphic Design"))
            )
        ),
        User(
            id = "faith",
            fullName = "Faith",
            university = "Universitas Indonesia",
            major = "Visual Communication Design",
            rating = 4.8,
            helpsCount = 49,
            requestsCount = 6,
            reviewsCount = 40,
            skills = listOf("Design", "Graphic Design", "Illustration"),
            presence = Presence.ONLINE,
            portfolioUrl = "PortfolioFaith.com",
            recentHelps = listOf(
                RecentHelp("Poster event kampus", 5, "3d ago", listOf("Design", "Illustration"))
            )
        ),
        User(
            id = "john",
            fullName = "John",
            university = "Institut Teknologi Bandung",
            major = "Product Design",
            rating = 4.7,
            helpsCount = 51,
            requestsCount = 9,
            reviewsCount = 44,
            skills = listOf("Design", "3D Modeling", "Adobe Photoshop"),
            presence = Presence.ONLINE,
            portfolioUrl = "PortfolioJohn.com",
            recentHelps = listOf(
                RecentHelp("3D product mockup", 4, "5d ago", listOf("Design", "3D Modeling"))
            )
        ),
        User(
            id = "mary",
            fullName = "Mary",
            university = "Universitas Pelita Harapan",
            major = "Product Design",
            rating = 4.9,
            helpsCount = 16,
            requestsCount = 3,
            reviewsCount = 15,
            skills = listOf("Design", "Product design", "3D Modeling"),
            presence = Presence.ONLINE_TODAY,
            portfolioUrl = "PortfolioMary.com"
        ),
        User(
            id = "chris",
            fullName = "Chris",
            university = "Universitas Gadjah Mada",
            major = "Informatics",
            rating = 4.9,
            helpsCount = 23,
            skills = listOf("Programming", "Mobile Development"),
            presence = Presence.ONLINE,
            portfolioUrl = "PortfolioChris.com"
        ),
        User(
            id = "joshua",
            fullName = "Joshua",
            university = "Universitas Bina Nusantara",
            major = "Information Systems",
            rating = 4.5,
            helpsCount = 4
        ),
        User(
            id = "user1",
            fullName = "User 1",
            university = "Universitas Pelita Harapan",
            major = "Accounting",
            rating = 4.6,
            helpsCount = 2
        ),
        User("william", "William James", "Universitas Pelita Harapan", "Management", presence = Presence.ONLINE),
        User("abby", "Abby", "Universitas Indonesia", "Computer Science", presence = Presence.ONLINE_TODAY),
        User("billy", "Billy", "Universitas Prasetiya Mulya", "Business"),
        User("coco", "Coco", "Universitas Tarumanagara", "Interior Design", presence = Presence.ONLINE),
        // Mahasiswa yang masih menunggu verifikasi KTM.
        User("dimas", "Dimas Ardi", "BINUS University", "Teknik Informatika", email = "dimas.ardi@binus.ac.id", isVerified = false, joinedAt = today.minusDays(1)),
        User("nadia", "Nadia Rahma", "Universitas Indonesia", "Ilmu Komunikasi", email = "nadia.rahma@ui.ac.id", isVerified = false, joinedAt = today.minusDays(1)),
        User("kevin", "Kevin Lukman", "Institut Teknologi Bandung", "Teknik Sipil", email = "kevin.lukman@itb.ac.id", isVerified = false, joinedAt = today.minusDays(2)),
        // Akun yang sedang ditangguhkan.
        User(
            "budi", "Budi Santoso", "BINUS University", "Sistem Informasi",
            email = "budi.santoso@binus.ac.id", rating = 4.5, helpsCount = 128,
            skills = listOf("Programming"), status = AccountStatus.SUSPENDED,
            suspension = SuspensionInfo(today.plusDays(5), "Menerima permintaan joki ujian online"),
            joinedAt = LocalDate.of(2025, 8, 24)
        ),
        // Akun Admin (role ADMIN, bukan Requester/Helper).
        User("admin", "Alya Putri", "SOS", "Trust & Safety", email = ADMIN_EMAIL, role = UserRole.ADMIN)
    )

    private const val UI_KIT_DESC = "I want to create a clean and modern homepage " +
            "layout but don't know how to structure it properly."

    val requests = mutableStateListOf(
        // Milik saya, sudah dikerjakan Klein (On Progress).
        Request(
            id = "r1", requesterId = CURRENT_USER_ID, subject = "Design",
            title = "Gimana cara bikin UI Kit?", description = UI_KIT_DESC,
            tags = listOf("Design", "UI/UX"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(2), deadlineTime = LocalTime.of(23, 30), budget = 15_000,
            status = RequestStatus.IN_PROGRESS, createdAt = now.minusHours(2),
            hasSampleImage = true, attachmentName = "ui ux assignment.pdf"
        ),
        // Milik saya, masih Floating dan sudah ada beberapa reply.
        Request(
            id = "r2", requesterId = CURRENT_USER_ID, subject = "Design",
            title = "Need help with mobile UI design", description = UI_KIT_DESC,
            tags = listOf("Design", "UI/UX"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(3), budget = 15_000,
            createdAt = now.minusHours(5), hasSampleImage = true
        ),
        // Milik saya, sudah selesai.
        Request(
            id = "r3", requesterId = CURRENT_USER_ID, subject = "Design",
            title = "Review desain landing page", description = UI_KIT_DESC,
            tags = listOf("Design", "UI/UX"), estimatedTime = EstimatedTime.FAST,
            deadline = today.minusDays(10), budget = 15_000,
            status = RequestStatus.CLOSED, createdAt = now.minusDays(14)
        ),
        // Milik User 1, saya yang bantu (On Progress).
        Request(
            id = "r4", requesterId = "user1", subject = "Mathematics",
            title = "Bantu kerjakan soal kalkulus", description = "Butuh penjelasan " +
                    "langkah demi langkah untuk 5 soal integral parsial.",
            tags = listOf("Mathematics"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(2), budget = 50_000,
            status = RequestStatus.IN_PROGRESS, createdAt = now.minusDays(1)
        ),
        // Milik User 1, saya yang bantu (Completed).
        Request(
            id = "r5", requesterId = "user1", subject = "Computer Science",
            title = "Debug tugas Java OOP", description = "Program error saat " +
                    "inheritance, butuh bantuan cari bug-nya.",
            tags = listOf("Computer Science", "Programming"),
            estimatedTime = EstimatedTime.FAST, deadline = today.minusDays(5),
            budget = 50_000, status = RequestStatus.CLOSED,
            createdAt = now.minusDays(8)
        ),
        // Milik saya, Faith sudah mengajukan completion (menunggu konfirmasi saya).
        Request(
            id = "r11", requesterId = CURRENT_USER_ID, subject = "Language & Literature",
            title = "Terjemahkan abstrak skripsi ke Inggris", description = "Abstrak " +
                    "sekitar 300 kata, butuh hasil yang natural dan akademik.",
            tags = listOf("Language & Literature"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(1), budget = 25_000,
            status = RequestStatus.IN_PROGRESS, createdAt = now.minusDays(2)
        ),
        // Direct Request saya ke Klein, belum diterima (Chat helper 1).
        Request(
            id = "r12", requesterId = CURRENT_USER_ID, subject = "Design",
            title = "Modern Landing Page Design", description = "Request details: I need a " +
                    "modern landing page for my startup project. Clean layout, hero section, " +
                    "feature list, and a pricing table.",
            tags = listOf("Design", "UI/UX"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(3), budget = 50_000, createdAt = now.minusMinutes(1),
            attachmentName = "ui ux assignment.pdf", targetHelperId = "klein"
        ),
        // Request William, saya yang bantu (Chat requester).
        Request(
            id = "r13", requesterId = "william", subject = "Business & Management",
            title = "Assignment Details", description = "Need help structuring my " +
                    "business plan assignment: market analysis and financial projection.",
            tags = listOf("Business & Management"), estimatedTime = EstimatedTime.MEDIUM,
            deadline = today.plusDays(2), budget = 40_000, status = RequestStatus.IN_PROGRESS,
            createdAt = now.minusHours(10), attachmentName = "ui ux assignment.pdf"
        ),
        Request(
            id = "r14", requesterId = "abby", subject = "Computer Science", title = "Project Alpha",
            description = "Bantu review arsitektur project Alpha.", tags = listOf("Computer Science"),
            estimatedTime = EstimatedTime.FAST, deadline = today.minusDays(5), budget = 15_000,
            status = RequestStatus.CLOSED, createdAt = now.minusDays(7)
        ),
        Request(
            id = "r15", requesterId = "billy", subject = "Business & Management", title = "Q3 Planning",
            description = "Bantu susun slide planning Q3.", tags = listOf("Business & Management"),
            estimatedTime = EstimatedTime.FAST, deadline = today.minusDays(7), budget = 15_000,
            status = RequestStatus.CLOSED, createdAt = now.minusDays(9)
        ),
        Request(
            id = "r16", requesterId = "coco", subject = "Design", title = "UX Audit",
            description = "Audit UX aplikasi kasir.", tags = listOf("Design", "UI/UX"),
            estimatedTime = EstimatedTime.MEDIUM, deadline = today.minusDays(8), budget = 25_000,
            status = RequestStatus.CLOSED, createdAt = now.minusDays(12)
        ),
        // Request yang dilaporkan (untuk Moderasi Request di Admin).
        Request(
            id = "r20", requesterId = "budi", subject = "Computer Science",
            title = "Bantu kerjakan ujian online", description = "Butuh orang buat ngerjain " +
                    "ujian online mata kuliah basis data malam ini. Jawaban dikirim sebelum 21.00.",
            tags = listOf("Computer Science"), estimatedTime = EstimatedTime.FAST,
            deadline = today, budget = 150_000, createdAt = now.minusHours(4)
        ),
        Request(
            id = "r21", requesterId = "joshua", subject = "Business & Management",
            title = "Titip absen kelas pagi", description = "Tolong absenin aku di kelas jam 7 " +
                    "pagi minggu ini, nanti aku bayar.",
            tags = listOf("Business & Management"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(2), budget = 20_000, createdAt = now.minusHours(7)
        ),
        Request(
            id = "r22", requesterId = "mary", subject = "Language & Literature",
            title = "Antar buku ke perpustakaan", description = "Butuh bantuan mengembalikan " +
                    "3 buku ke perpustakaan pusat sebelum jam 4 sore.",
            tags = listOf("Language & Literature"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(1), budget = 15_000, createdAt = now.minusHours(9)
        ),
        // Request dengan dispute (untuk Laporan & Dispute di Admin).
        Request(
            id = "r23", requesterId = "chris", subject = "Design",
            title = "Gimana cara bikin UI Kit untuk aplikasi kasir?", description = "Butuh UI kit " +
                    "berisi komponen tombol, input, dan kartu untuk aplikasi kasir.",
            tags = listOf("Design", "UI/UX"), estimatedTime = EstimatedTime.MEDIUM,
            deadline = today.minusDays(1), budget = 15_000, status = RequestStatus.IN_PROGRESS,
            createdAt = now.minusDays(5)
        ),
        Request(
            id = "r24", requesterId = "mary", subject = "Economics",
            title = "Ringkasan jurnal ekonomi makro", description = "Ringkas 2 jurnal " +
                    "tentang inflasi, masing-masing 1 halaman.",
            tags = listOf("Economics"), estimatedTime = EstimatedTime.FAST,
            deadline = today.minusDays(2), budget = 30_000, status = RequestStatus.IN_PROGRESS,
            createdAt = now.minusDays(4)
        ),
        // Request orang lain yang tampil di Explore.
        Request(
            id = "r6", requesterId = "joshua", subject = "Design",
            title = "Need help with mobile UI design", description = UI_KIT_DESC,
            tags = listOf("Design", "UI/UX"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(3), budget = 15_000,
            createdAt = now.minusHours(1), hasSampleImage = true
        ),
        Request(
            id = "r7", requesterId = "chris", subject = "Design",
            title = "How to make a UI Kit?", description = UI_KIT_DESC,
            tags = listOf("Design", "UI/UX"), estimatedTime = EstimatedTime.FAST,
            deadline = today.plusDays(4), budget = 15_000,
            createdAt = now.minusHours(3)
        ),
        Request(
            id = "r8", requesterId = "mary", subject = "Mathematics",
            title = "Penjelasan aljabar linear: eigenvalue", description = "Butuh " +
                    "teman belajar untuk memahami eigenvalue dan eigenvector.",
            tags = listOf("Mathematics"), estimatedTime = EstimatedTime.MEDIUM,
            deadline = today.plusDays(5), budget = 30_000,
            createdAt = now.minusHours(6)
        ),
        Request(
            id = "r9", requesterId = "john", subject = "Computer Science",
            title = "Setup Firebase di project Android", description = "Gagal " +
                    "konfigurasi google-services.json, butuh dibantu.",
            tags = listOf("Computer Science", "Mobile Development"),
            estimatedTime = EstimatedTime.FAST, deadline = today.plusDays(1),
            budget = 40_000, createdAt = now.minusHours(8)
        ),
        Request(
            id = "r10", requesterId = "faith", subject = "Economics",
            title = "Ringkasan teori permintaan & penawaran", description = "Butuh " +
                    "ringkasan materi untuk UTS minggu depan.",
            tags = listOf("Economics"), estimatedTime = EstimatedTime.MEDIUM,
            deadline = today.plusDays(6), budget = 20_000,
            createdAt = now.minusHours(10)
        )
    )

    val replies = mutableStateListOf(
        Reply("p1", "r2", "chris", "Hello, I can help you! Take a look at my portfolio here", "PortfolioChris.com"),
        Reply("p2", "r2", "john", "This is my portfolio.", "PortfolioJohn.com"),
        Reply("p3", "r2", "faith", "Hello, I can help you! Take a look at my portfolio here", "PortfolioFaith.com"),
        Reply("p4", "r6", "chris", "Hello, I can help you! Take a look at my portfolio here", "PortfolioChris.com"),
        Reply("p5", "r6", "john", "This is my portfolio.", "PortfolioJohn.com"),
        Reply("p6", "r6", "mary", "Hello, I can help you! Take a look at my portfolio here", "PortfolioMary.com"),
        Reply("p7", "r1", "klein", "Hello, I can help you! Take a look at my portfolio here", "PortfolioKlein.com"),
        Reply("p8", "r7", "faith", "Bisa, aku biasa bikin design system di Figma.", "PortfolioFaith.com")
    )

    val assignments = mutableStateListOf(
        HelpAssignment(
            id = "a1", requestId = "r1", requesterId = CURRENT_USER_ID,
            helperId = "klein", status = AssignmentStatus.ON_PROGRESS,
            agreedPrice = 15_000, createdAt = today.minusDays(2),
            acceptedAt = today.minusDays(1), deadline = today.plusDays(2)
        ),
        HelpAssignment(
            id = "a2", requestId = "r3", requesterId = CURRENT_USER_ID,
            helperId = "klein", status = AssignmentStatus.COMPLETED,
            agreedPrice = 15_000, createdAt = today.minusDays(14),
            acceptedAt = today.minusDays(13), deadline = today.minusDays(10),
            completedAt = now.minusDays(11).withHour(11).withMinute(45)
        ),
        HelpAssignment(
            id = "a3", requestId = "r4", requesterId = "user1",
            helperId = CURRENT_USER_ID, status = AssignmentStatus.ON_PROGRESS,
            agreedPrice = 50_000, createdAt = today.minusDays(1),
            acceptedAt = today, deadline = today.plusDays(2)
        ),
        HelpAssignment(
            id = "a4", requestId = "r5", requesterId = "user1",
            helperId = CURRENT_USER_ID, status = AssignmentStatus.COMPLETED,
            agreedPrice = 50_000, createdAt = today.minusDays(8),
            acceptedAt = today.minusDays(7), deadline = today.minusDays(5),
            completedAt = now.minusDays(6), rating = 5
        ),
        HelpAssignment(
            id = "a5", requestId = "r11", requesterId = CURRENT_USER_ID,
            helperId = "faith", status = AssignmentStatus.COMPLETION_REQUESTED,
            agreedPrice = 25_000, createdAt = today.minusDays(2),
            acceptedAt = today.minusDays(2), deadline = today.plusDays(1),
            completionRequestedBy = "faith"
        ),
        HelpAssignment(
            id = "a10", requestId = "r23", requesterId = "chris", helperId = "faith",
            status = AssignmentStatus.CANCELLATION_REQUESTED, agreedPrice = 15_000,
            createdAt = today.minusDays(5), acceptedAt = today.minusDays(5),
            deadline = today.minusDays(1), cancellationRequestedBy = "chris",
            cancellationReason = "Hasil tugas belum lengkap"
        ),
        HelpAssignment(
            id = "a11", requestId = "r24", requesterId = "mary", helperId = "chris",
            status = AssignmentStatus.COMPLETION_REQUESTED, agreedPrice = 30_000,
            createdAt = today.minusDays(4), acceptedAt = today.minusDays(4),
            deadline = today.minusDays(2), completionRequestedBy = "chris"
        ),
        HelpAssignment(
            id = "a6", requestId = "r13", requesterId = "william",
            helperId = CURRENT_USER_ID, status = AssignmentStatus.ON_PROGRESS,
            agreedPrice = 40_000, createdAt = today, acceptedAt = today,
            deadline = today.plusDays(2)
        ),
        HelpAssignment(
            id = "a7", requestId = "r14", requesterId = "abby", helperId = CURRENT_USER_ID,
            status = AssignmentStatus.COMPLETED, agreedPrice = 15_000,
            createdAt = today.minusDays(7), acceptedAt = today.minusDays(7),
            deadline = today.minusDays(5), completedAt = now.minusDays(5), rating = 5
        ),
        HelpAssignment(
            id = "a8", requestId = "r15", requesterId = "billy", helperId = CURRENT_USER_ID,
            status = AssignmentStatus.COMPLETED, agreedPrice = 15_000,
            createdAt = today.minusDays(9), acceptedAt = today.minusDays(9),
            deadline = today.minusDays(7), completedAt = now.minusDays(7), rating = 5
        ),
        HelpAssignment(
            id = "a9", requestId = "r16", requesterId = "coco", helperId = CURRENT_USER_ID,
            status = AssignmentStatus.COMPLETED, agreedPrice = 25_000,
            createdAt = today.minusDays(12), acceptedAt = today.minusDays(12),
            deadline = today.minusDays(8), completedAt = now.minusDays(8), rating = 4
        )
    )

    val conversations = mutableStateListOf(
        Conversation("c1", "r12", CURRENT_USER_ID, "klein"),
        Conversation("c2", "r13", "william", CURRENT_USER_ID),
        Conversation("c3", "r14", "abby", CURRENT_USER_ID),
        Conversation("c4", "r15", "billy", CURRENT_USER_ID),
        Conversation("c5", "r16", "coco", CURRENT_USER_ID),
        Conversation("c6", "r1", CURRENT_USER_ID, "klein"),
        Conversation("c7", "r11", CURRENT_USER_ID, "faith"),
        Conversation("c8", "r4", "user1", CURRENT_USER_ID)
    )

    private const val LOREM = "Request details lorem ipsum dolor sit amet, consectetur " +
            "adipiscing elit. In sed quam non nisi venenatis commodo."

    val messages = mutableStateListOf(
        // c1: Direct Request ke Klein (baru dikirim).
        Message("m1", "c1", CURRENT_USER_ID, MessageType.REQUEST, sentAt = now.minusMinutes(1)),
        Message("m2", "c1", CURRENT_USER_ID, MessageType.SYSTEM, "Request has been sent.", sentAt = now.minusMinutes(1).plusSeconds(1)),
        // c2: William (saya Helper).
        Message("m3", "c2", "william", MessageType.REQUEST, sentAt = now.minusHours(10)),
        Message("m4", "c2", CURRENT_USER_ID, MessageType.TEXT, "$LOREM Nam erat risus, tempor vel scelerisque nec, posuere sit amet.", sentAt = now.minusHours(10).plusMinutes(5)),
        Message("m5", "c2", "william", MessageType.TEXT, "Hi, this is the details of the assignment, thank you", sentAt = now.minusHours(10).plusMinutes(9)),
        // c3–c5: bantuan saya yang sudah selesai.
        Message("m6", "c3", "abby", MessageType.TEXT, "Okay, thank you for helping me!", sentAt = now.minusDays(5)),
        Message("m7", "c4", "billy", MessageType.TEXT, "Okay, thank you for helping me!", sentAt = now.minusDays(7)),
        Message("m8", "c5", "coco", MessageType.TEXT, "Okay, thank you for helping me!", sentAt = now.minusDays(8)),
        // c6: Klein mengerjakan request saya (Chat helper 2).
        Message("m9", "c6", CURRENT_USER_ID, MessageType.REQUEST, sentAt = now.minusDays(1)),
        Message("m10", "c6", "klein", MessageType.TEXT, LOREM, sentAt = now.minusDays(1).plusMinutes(3)),
        Message("m11", "c6", CURRENT_USER_ID, MessageType.TEXT, "lorem ipsum dolor sit amet.", sentAt = now.minusDays(1).plusMinutes(6)),
        // c7: Faith sudah selesai, menunggu konfirmasi saya.
        Message("m12", "c7", CURRENT_USER_ID, MessageType.REQUEST, sentAt = now.minusDays(2)),
        Message("m13", "c7", "faith", MessageType.FILE, attachmentName = "abstract_translation.docx", sentAt = now.minusHours(3)),
        Message("m14", "c7", "faith", MessageType.TEXT, "Sudah selesai ya, silakan dicek. Kalau oke tolong konfirmasi 🙏", sentAt = now.minusHours(3)),
        // c8: User 1 (saya Helper).
        Message("m15", "c8", "user1", MessageType.REQUEST, sentAt = now.minusDays(1)),
        Message("m16", "c8", CURRENT_USER_ID, MessageType.TEXT, "Siap, aku kerjakan hari ini ya.", sentAt = now.minusDays(1).plusMinutes(20))
    )

    val transactions = mutableStateListOf(
        Transaction("t1", CURRENT_USER_ID, TransactionType.TOP_UP, 300_000, "Top up via Bank Transfer", now.minusDays(20)),
        Transaction("t2", CURRENT_USER_ID, TransactionType.PAYMENT, 15_000, "Review desain landing page", now.minusDays(14), "a2"),
        Transaction("t3", CURRENT_USER_ID, TransactionType.RELEASE, 25_000, "UX Audit", now.minusDays(8), "a9"),
        Transaction("t4", CURRENT_USER_ID, TransactionType.RELEASE, 15_000, "Q3 Planning", now.minusDays(7), "a8"),
        Transaction("t5", CURRENT_USER_ID, TransactionType.RELEASE, 50_000, "Debug tugas Java OOP", now.minusDays(6), "a4"),
        Transaction("t6", CURRENT_USER_ID, TransactionType.RELEASE, 15_000, "Project Alpha", now.minusDays(5), "a7"),
        Transaction("t7", CURRENT_USER_ID, TransactionType.WITHDRAW, 100_000, "Withdraw to bank account", now.minusDays(4)),
        Transaction("t8", CURRENT_USER_ID, TransactionType.PAYMENT, 25_000, "Terjemahkan abstrak skripsi ke Inggris", now.minusDays(2), "a5"),
        Transaction("t9", CURRENT_USER_ID, TransactionType.PAYMENT, 15_000, "Gimana cara bikin UI Kit?", now.minusDays(1), "a1")
    )

    // ---------- Query ----------

    val currentUser: User
        get() = user(CURRENT_USER_ID)!!

    fun user(id: String): User? = users.firstOrNull { it.id == id }

    fun request(id: String): Request? = requests.firstOrNull { it.id == id }

    fun assignment(id: String): HelpAssignment? = assignments.firstOrNull { it.id == id }

    fun assignmentFor(requestId: String, helperId: String): HelpAssignment? =
        assignments.firstOrNull { it.requestId == requestId && it.helperId == helperId }

    fun repliesFor(requestId: String): List<Reply> =
        replies.filter { it.requestId == requestId }

    fun assignmentsFor(requestId: String): List<HelpAssignment> =
        assignments.filter { it.requestId == requestId }

    /** Request publik milik orang lain yang masih bisa dibantu (tab Explore → Request). */
    fun exploreRequests(): List<Request> = requests.filter {
        it.status == RequestStatus.OPEN &&
                !it.isHidden &&
                SosRepository.user(it.requesterId)?.status != AccountStatus.SUSPENDED &&
                it.requesterId != CURRENT_USER_ID &&
                it.targetHelperId == null
    }.sortedByDescending { it.createdAt }

    /** Semua user selain saya yang punya skill (tab Explore → Helper). */
    fun exploreHelpers(): List<User> = users.filter {
        it.id != CURRENT_USER_ID && it.skills.isNotEmpty() &&
                it.role == UserRole.USER && it.status == AccountStatus.ACTIVE
    }

    /** Request yang saya buat, satu baris per assignment (atau satu baris Floating). */
    fun myRequestItems(): List<TaskItem> = requests
        .filter { it.requesterId == CURRENT_USER_ID }
        .sortedByDescending { it.createdAt }
        .flatMap { request ->
            val list = assignmentsFor(request.id)
            if (list.isEmpty()) listOf(TaskItem(request, null))
            else list.map { TaskItem(request, it) }
        }

    /** Bantuan yang saya kerjakan sebagai Helper. */
    fun myAnswerItems(): List<TaskItem> = assignments
        .filter { it.helperId == CURRENT_USER_ID }
        .sortedByDescending { it.createdAt }
        .mapNotNull { a -> request(a.requestId)?.let { TaskItem(it, a) } }

    // ---------- Auth (simulasi) ----------

    /** TODO: Ganti dengan login ke backend (JWT). Sekarang hanya cek tidak kosong. */
    fun login(email: String, password: String): Boolean {
        isLoggedIn = email.isNotBlank() && password.isNotBlank()
        isAdminSession = isLoggedIn && email.trim().equals(ADMIN_EMAIL, ignoreCase = true)
        return isLoggedIn
    }

    fun logout() {
        isLoggedIn = false
        isAdminSession = false
    }

    /** Akun saya sedang ditangguhkan Admin? */
    val isSuspended: Boolean
        get() = currentUser.status == AccountStatus.SUSPENDED

    // ---------- Untuk Admin ----------

    fun allUsers(): List<User> = users.filter { it.role == UserRole.USER }

    fun replaceUser(updated: User) {
        val index = users.indexOfFirst { it.id == updated.id }
        if (index >= 0) users[index] = updated
    }

    fun setRequestHidden(requestId: String, hidden: Boolean) {
        request(requestId)?.let { replaceRequest(it.copy(isHidden = hidden)) }
    }

    fun removeRequest(requestId: String) {
        request(requestId)?.let { replaceRequest(it.copy(isHidden = true, status = RequestStatus.CANCELLED)) }
    }

    /**
     * Keputusan dispute oleh Admin. Refund → CANCELLED, release → COMPLETED.
     * refundAmount hanya dipakai untuk refund sebagian.
     */
    fun applyDisputeDecision(assignmentId: String, decision: DisputeDecision, refundAmount: Long) {
        val a = assignment(assignmentId) ?: return
        val now = LocalDateTime.now()
        val title = request(a.requestId)?.title.orEmpty()
        when (decision) {
            DisputeDecision.RELEASE_TO_HELPER -> {
                replaceAssignment(a.copy(status = AssignmentStatus.COMPLETED, completedAt = now))
                if (a.helperId == CURRENT_USER_ID) addTransaction(TransactionType.RELEASE, a.agreedPrice, title, a.id)
                systemMessage(a, "Admin decision: payment released to the helper.")
            }
            DisputeDecision.FULL_REFUND, DisputeDecision.PARTIAL_REFUND -> {
                val refund = if (decision == DisputeDecision.FULL_REFUND) a.agreedPrice
                else refundAmount.coerceIn(0, a.agreedPrice)
                replaceAssignment(a.copy(status = AssignmentStatus.CANCELLED, cancelledAt = now))
                if (a.requesterId == CURRENT_USER_ID && refund > 0) {
                    addTransaction(TransactionType.REFUND, refund, "Refund: $title", a.id)
                }
                if (a.helperId == CURRENT_USER_ID && a.agreedPrice - refund > 0) {
                    addTransaction(TransactionType.RELEASE, a.agreedPrice - refund, title, a.id)
                }
                systemMessage(a, "Admin decision: ${formatAmount(refund)} refunded to the requester.")
            }
        }
        request(a.requestId)?.let { r ->
            if (assignmentsFor(r.id).none { it.status.isActive }) {
                replaceRequest(r.copy(status = RequestStatus.CLOSED))
            }
        }
    }

    // ---------- Profil ----------

    fun updateProfile(
        fullName: String,
        email: String,
        university: String,
        major: String,
        websiteUrl: String,
        linkedInUrl: String,
        portfolioUrl: String?,
        skills: List<String>
    ) {
        val index = users.indexOfFirst { it.id == CURRENT_USER_ID }
        if (index < 0) return
        users[index] = users[index].copy(
            fullName = fullName.trim(),
            email = email.trim(),
            university = university.trim(),
            major = major.trim(),
            websiteUrl = websiteUrl.trim().ifBlank { null },
            linkedInUrl = linkedInUrl.trim().ifBlank { null },
            portfolioUrl = portfolioUrl,
            skills = skills
        )
    }

    // ---------- Request ----------

    fun createRequest(
        subject: String,
        title: String,
        description: String,
        estimatedTime: EstimatedTime,
        deadline: LocalDate,
        budget: Long,
        helpersNeeded: Int,
        attachmentUri: String?,
        attachmentName: String? = null,
        targetHelperId: String? = null
    ): Request {
        val request = Request(
            id = UUID.randomUUID().toString(),
            requesterId = CURRENT_USER_ID,
            subject = subject,
            title = title,
            description = description,
            tags = listOf(subject),
            estimatedTime = estimatedTime,
            deadline = deadline,
            budget = budget,
            helpersNeeded = helpersNeeded,
            attachmentUri = attachmentUri,
            attachmentName = attachmentName,
            targetHelperId = targetHelperId
        )
        requests.add(request)
        return request
    }

    /**
     * Direct Request ke Helper dari profilnya: buat request + percakapan
     * berisi kartu request. Mengembalikan id percakapan.
     */
    fun sendDirectRequest(
        helperId: String,
        details: String,
        estimatedTime: EstimatedTime,
        deadline: LocalDate,
        budget: Long,
        attachmentUri: String?,
        attachmentName: String?
    ): String {
        val helper = user(helperId)
        val request = createRequest(
            subject = helper?.skills?.firstOrNull() ?: "General",
            title = details.trim().lineSequence().first().take(60),
            description = details.trim(),
            estimatedTime = estimatedTime,
            deadline = deadline,
            budget = budget,
            helpersNeeded = 1,
            attachmentUri = attachmentUri,
            attachmentName = attachmentName,
            targetHelperId = helperId
        )
        val conversation = Conversation(UUID.randomUUID().toString(), request.id, CURRENT_USER_ID, helperId)
        conversations.add(conversation)
        addMessage(conversation.id, CURRENT_USER_ID, MessageType.REQUEST)
        addMessage(conversation.id, CURRENT_USER_ID, MessageType.SYSTEM, "Request has been sent.")
        return conversation.id
    }

    /** Edit request milik sendiri. Budget terkunci setelah ada agreement aktif. */
    fun editRequest(
        requestId: String,
        description: String,
        attachmentUri: String?,
        attachmentName: String?,
        deadline: LocalDate,
        deadlineTime: LocalTime?,
        budget: Long
    ) {
        val request = request(requestId) ?: return
        if (request.requesterId != CURRENT_USER_ID) return
        if (request.status == RequestStatus.CLOSED || request.status == RequestStatus.CANCELLED) return

        val hasActive = assignmentsFor(requestId).any { it.status.isActive }
        replaceRequest(
            request.copy(
                description = description.trim(),
                attachmentUri = attachmentUri,
                attachmentName = attachmentName,
                deadline = deadline,
                deadlineTime = deadlineTime,
                budget = if (hasActive) request.budget else budget
            )
        )
        // Deadline baru ikut berlaku untuk assignment yang sedang berjalan.
        assignmentsFor(requestId).filter { it.status.isActive }.forEach {
            replaceAssignment(it.copy(deadline = deadline))
        }
        conversations.filter { it.requestId == requestId }.forEach {
            addMessage(it.id, CURRENT_USER_ID, MessageType.SYSTEM, "Request details updated.")
        }
    }

    fun addReply(requestId: String, message: String) {
        val request = request(requestId) ?: return
        // Aturan: tidak boleh reply ke request milik sendiri.
        if (request.requesterId == CURRENT_USER_ID || message.isBlank() || isSuspended) return
        replies.add(
            Reply(
                id = UUID.randomUUID().toString(),
                requestId = requestId,
                helperId = CURRENT_USER_ID,
                message = message.trim(),
                portfolioUrl = currentUser.portfolioUrl
            )
        )
    }

    fun hasReplied(requestId: String): Boolean =
        replies.any { it.requestId == requestId && it.helperId == CURRENT_USER_ID }

    /** Pemilik membatalkan request yang belum punya assignment aktif. */
    fun cancelRequest(requestId: String): Boolean {
        val request = request(requestId) ?: return false
        if (request.requesterId != CURRENT_USER_ID) return false
        if (assignmentsFor(requestId).any { it.status.isActive }) return false
        replaceRequest(request.copy(status = RequestStatus.CANCELLED))
        return true
    }

    // ---------- Chat ----------

    fun conversation(id: String): Conversation? = conversations.firstOrNull { it.id == id }

    fun messagesFor(conversationId: String): List<Message> =
        messages.filter { it.conversationId == conversationId }.sortedBy { it.sentAt }

    fun lastMessage(conversationId: String): Message? =
        messages.filter { it.conversationId == conversationId }.maxByOrNull { it.sentAt }

    /** Percakapan saya, terbaru di atas. */
    fun myConversations(): List<Conversation> = conversations
        .filter { it.requesterId == CURRENT_USER_ID || it.helperId == CURRENT_USER_ID }
        .sortedByDescending { lastMessage(it.id)?.sentAt }

    /**
     * Ambil atau buat percakapan untuk (request, helper).
     * Percakapan baru diisi kartu request dan reply Helper (kalau ada).
     */
    fun openConversation(requestId: String, helperId: String): String? {
        conversations.firstOrNull { it.requestId == requestId && it.helperId == helperId }
            ?.let { return it.id }

        val request = request(requestId) ?: return null
        val conversation = Conversation(UUID.randomUUID().toString(), requestId, request.requesterId, helperId)
        conversations.add(conversation)
        addMessage(conversation.id, request.requesterId, MessageType.REQUEST, at = request.createdAt)
        replies.firstOrNull { it.requestId == requestId && it.helperId == helperId }?.let {
            addMessage(conversation.id, helperId, MessageType.TEXT, it.message, at = it.createdAt)
        }
        return conversation.id
    }

    fun sendText(conversationId: String, text: String) {
        if (text.isBlank() || isSuspended) return
        addMessage(conversationId, CURRENT_USER_ID, MessageType.TEXT, text.trim())
    }

    fun sendFile(conversationId: String, fileName: String) {
        addMessage(conversationId, CURRENT_USER_ID, MessageType.FILE, attachmentName = fileName)
    }

    /** Requester menolak bantuan dari Helper di percakapan ini. */
    fun declineHelp(conversationId: String) {
        val c = conversation(conversationId) ?: return
        if (c.requesterId != CURRENT_USER_ID) return
        replaceConversation(c.copy(declined = true))
        val name = user(c.helperId)?.fullName.orEmpty()
        addMessage(c.id, CURRENT_USER_ID, MessageType.SYSTEM, "You declined help from $name.")
    }

    // ---------- Assignment ----------

    /**
     * Agree & Continue di Contract Agreement.
     * Simulasi: agreement disetujui + dana ditahan dari balance Requester,
     * lalu assignment langsung ON_PROGRESS (aturan: kontrak + funded dulu).
     */
    fun acceptHelp(requestId: String, helperId: String): AcceptResult {
        val request = request(requestId) ?: return AcceptResult.NotAllowed
        if (request.requesterId != CURRENT_USER_ID || helperId == CURRENT_USER_ID || isSuspended) {
            return AcceptResult.NotAllowed
        }
        assignmentFor(requestId, helperId)?.let { existing ->
            if (existing.status != AssignmentStatus.PENDING_AGREEMENT) {
                return AcceptResult.Success(existing.id)
            }
        }

        val activeCount = assignmentsFor(requestId).count {
            it.status.isActive && it.helperId != helperId
        }
        if (activeCount >= request.helpersNeeded) return AcceptResult.NotAllowed

        val price = request.budget / request.helpersNeeded
        val balance = balance()
        if (balance < price) return AcceptResult.InsufficientBalance(price, balance)

        val existing = assignmentFor(requestId, helperId)
        val assignment = (existing ?: HelpAssignment(
            id = UUID.randomUUID().toString(),
            requestId = requestId,
            requesterId = CURRENT_USER_ID,
            helperId = helperId,
            status = AssignmentStatus.PENDING_AGREEMENT,
            agreedPrice = price,
            createdAt = LocalDate.now(),
            deadline = request.deadline
        )).copy(status = AssignmentStatus.ON_PROGRESS, acceptedAt = LocalDate.now())

        if (existing == null) assignments.add(assignment) else replaceAssignment(assignment)
        addTransaction(TransactionType.PAYMENT, price, request.title, assignment.id)

        if (activeCount + 1 >= request.helpersNeeded) {
            replaceRequest(request.copy(status = RequestStatus.IN_PROGRESS))
        }

        val conversationId = openConversation(requestId, helperId)
        if (conversationId != null) {
            conversation(conversationId)?.let { replaceConversation(it.copy(declined = false)) }
            addMessage(
                conversationId, CURRENT_USER_ID, MessageType.SYSTEM,
                "Contract agreed. ${formatAmount(price)} is held by SOS until the work is complete."
            )
        }
        return AcceptResult.Success(assignment.id)
    }

    /** Helper mengajukan completion; Requester yang mengonfirmasi. */
    fun requestCompletion(assignmentId: String) {
        val a = assignment(assignmentId) ?: return
        if (a.status != AssignmentStatus.ON_PROGRESS || a.helperId != CURRENT_USER_ID) return
        replaceAssignment(
            a.copy(status = AssignmentStatus.COMPLETION_REQUESTED, completionRequestedBy = CURRENT_USER_ID)
        )
        systemMessage(a, "Completion requested. Waiting for confirmation.")
    }

    /**
     * Requester menyatakan pekerjaan selesai (tombol "Request complete" atau
     * konfirmasi atas pengajuan Helper). Dana dilepas ke Helper.
     */
    fun completeAsRequester(assignmentId: String) {
        val a = assignment(assignmentId) ?: return
        if (a.requesterId != CURRENT_USER_ID) return
        if (a.status != AssignmentStatus.ON_PROGRESS &&
            a.status != AssignmentStatus.COMPLETION_REQUESTED
        ) return

        replaceAssignment(a.copy(status = AssignmentStatus.COMPLETED, completedAt = LocalDateTime.now()))
        // TODO: RELEASE ke balance Helper dilakukan backend (bukan saldo saya).
        systemMessage(a, "Request complete. Payment released to the helper.")

        val request = request(a.requestId) ?: return
        if (assignmentsFor(request.id).none { it.status.isActive }) {
            replaceRequest(request.copy(status = RequestStatus.CLOSED))
        }
    }

    /** Salah satu pihak mengajukan pembatalan; pihak lain mengonfirmasi. */
    fun requestCancellation(assignmentId: String, reason: String) {
        val a = assignment(assignmentId) ?: return
        if (a.status != AssignmentStatus.ON_PROGRESS) return
        if (a.requesterId != CURRENT_USER_ID && a.helperId != CURRENT_USER_ID) return
        replaceAssignment(
            a.copy(
                status = AssignmentStatus.CANCELLATION_REQUESTED,
                cancellationRequestedBy = CURRENT_USER_ID,
                cancellationReason = reason
            )
        )
        systemMessage(a, "Cancellation requested: $reason")
    }

    /** Pihak lain (bukan pengaju) menyetujui pembatalan → refund ke Requester. */
    fun confirmCancellation(assignmentId: String) {
        val a = assignment(assignmentId) ?: return
        if (a.status != AssignmentStatus.CANCELLATION_REQUESTED ||
            a.cancellationRequestedBy == CURRENT_USER_ID
        ) return

        replaceAssignment(a.copy(status = AssignmentStatus.CANCELLED, cancelledAt = LocalDateTime.now()))
        if (a.requesterId == CURRENT_USER_ID) {
            // TODO: besaran refund mengikuti cancellation policy (open question #11).
            addTransaction(TransactionType.REFUND, a.agreedPrice, "Refund: ${request(a.requestId)?.title}", a.id)
        }
        systemMessage(a, "Request cancelled.")

        val request = request(a.requestId) ?: return
        if (assignmentsFor(request.id).none { it.status.isActive }) {
            replaceRequest(
                request.copy(
                    status = if (assignmentsFor(request.id).any { it.status == AssignmentStatus.COMPLETED })
                        RequestStatus.CLOSED else RequestStatus.CANCELLED
                )
            )
        }
    }

    /** Pihak lain menolak pembatalan → kembali ON_PROGRESS (sengketa → Admin). */
    fun rejectCancellation(assignmentId: String) {
        val a = assignment(assignmentId) ?: return
        if (a.status != AssignmentStatus.CANCELLATION_REQUESTED ||
            a.cancellationRequestedBy == CURRENT_USER_ID
        ) return
        replaceAssignment(
            a.copy(status = AssignmentStatus.ON_PROGRESS, cancellationRequestedBy = null, cancellationReason = null)
        )
        systemMessage(a, "Cancellation declined. The work continues.")
    }

    /** Review hanya oleh Requester, setelah COMPLETED, satu kali. */
    fun submitReview(assignmentId: String, rating: Int, comment: String = "") {
        val a = assignment(assignmentId) ?: return
        if (a.status != AssignmentStatus.COMPLETED ||
            a.requesterId != CURRENT_USER_ID ||
            a.rating != null
        ) return
        replaceAssignment(a.copy(rating = rating.coerceIn(1, 5), reviewComment = comment.trim().ifBlank { null }))
    }

    // ---------- Wallet ----------

    fun balance(): Long = transactions
        .filter { it.userId == CURRENT_USER_ID }
        .sumOf { it.signedAmount }

    fun myTransactions(): List<Transaction> = transactions
        .filter { it.userId == CURRENT_USER_ID }
        .sortedByDescending { it.date }

    /** Simulasi top up. TODO: lewat payment provider (Midtrans/Xendit) + webhook. */
    fun topUp(amount: Long, method: String) {
        if (amount <= 0) return
        addTransaction(TransactionType.TOP_UP, amount, "Top up via $method")
    }

    /** Simulasi withdraw. TODO: payout lewat provider. */
    fun withdraw(amount: Long): Boolean {
        if (amount < MIN_WITHDRAW || amount > balance()) return false
        addTransaction(TransactionType.WITHDRAW, amount, "Withdraw to bank account")
        return true
    }

    // ---------- Report & dispute (diteruskan ke Admin) ----------

    fun reportRequest(requestId: String, reason: String) =
        AdminRepository.submitReport(ReportTarget.REQUEST, requestId, CURRENT_USER_ID, reason)

    fun reportUser(userId: String, reason: String) =
        AdminRepository.submitReport(ReportTarget.USER, userId, CURRENT_USER_ID, reason)

    /** Eskalasi ke Admin saat kedua pihak tidak sepakat. */
    fun raiseDispute(assignmentId: String, reason: String) {
        val a = assignment(assignmentId) ?: return
        if (!a.status.isActive) return
        AdminRepository.submitDispute(assignmentId, CURRENT_USER_ID, reason)
        systemMessage(a, "Dispute submitted. SOS admin will review this case.")
    }

    fun hasOpenDispute(assignmentId: String): Boolean = AdminRepository.disputes.any {
        it.assignmentId == assignmentId &&
                (it.status == CaseStatus.OPEN || it.status == CaseStatus.IN_REVIEW)
    }

    // ---------- Internal ----------

    private fun addTransaction(type: TransactionType, amount: Long, title: String, assignmentId: String? = null) {
        transactions.add(
            Transaction(UUID.randomUUID().toString(), CURRENT_USER_ID, type, amount, title, LocalDateTime.now(), assignmentId)
        )
    }

    private fun addMessage(
        conversationId: String,
        senderId: String,
        type: MessageType,
        text: String = "",
        attachmentName: String? = null,
        at: LocalDateTime = LocalDateTime.now()
    ) {
        messages.add(Message(UUID.randomUUID().toString(), conversationId, senderId, type, text, attachmentName, at))
    }

    private fun systemMessage(a: HelpAssignment, text: String) {
        conversations.firstOrNull { it.requestId == a.requestId && it.helperId == a.helperId }?.let {
            addMessage(it.id, CURRENT_USER_ID, MessageType.SYSTEM, text)
        }
    }

    private fun formatAmount(amount: Long) = "IDR " + String.format(java.util.Locale.US, "%,d", amount)

    private fun replaceRequest(updated: Request) {
        val index = requests.indexOfFirst { it.id == updated.id }
        if (index >= 0) requests[index] = updated
    }

    private fun replaceAssignment(updated: HelpAssignment) {
        val index = assignments.indexOfFirst { it.id == updated.id }
        if (index >= 0) assignments[index] = updated
    }

    private fun replaceConversation(updated: Conversation) {
        val index = conversations.indexOfFirst { it.id == updated.id }
        if (index >= 0) conversations[index] = updated
    }
}
