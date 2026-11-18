package com.example.sos.feature.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.AdminRepository
import com.example.sos.data.SosRepository
import com.example.sos.data.model.AuditEntry
import com.example.sos.ui.components.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------- Warna status (selaras dengan badge di aplikasi user) ----------

enum class PillKind(val bg: Color, val fg: Color) {
    BLUE(Color(0xFFE5EFF8), Color(0xFF2141AD)),
    RED(Color(0xFFFDE0E0), Color(0xFFC62828)),
    ORANGE(Color(0xFFFFF1DB), Color(0xFFB45309)),
    GREEN(Color(0xFFDDF3E1), Color(0xFF2E7D32)),
    GRAY(Color(0xFFEFEFEF), Color(0xFF555555))
}

private val idLocale = Locale.forLanguageTag("id-ID")
private val dateTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", idLocale)
private val clockFormat = DateTimeFormatter.ofPattern("HH:mm", idLocale)
private val shortDate = DateTimeFormatter.ofPattern("d MMM yyyy", idLocale)

/** 265000 → "Rp265.000" */
fun rupiah(amount: Long): String = "Rp" + String.format(Locale.GERMANY, "%,d", amount)

fun adminDateTime(time: LocalDateTime): String = time.format(dateTimeFormat) + " WIB"
fun adminClock(time: LocalDateTime): String = time.format(clockFormat)
fun adminDate(date: java.time.LocalDate): String = date.format(shortDate)

/** "12 menit lalu", "2 jam lalu" */
fun agoId(time: LocalDateTime): String {
    val minutes = Duration.between(time, LocalDateTime.now()).toMinutes()
    return when {
        minutes < 1 -> "baru saja"
        minutes < 60 -> "$minutes menit lalu"
        minutes < 60 * 24 -> "${minutes / 60} jam lalu"
        else -> "${minutes / (60 * 24)} hari lalu"
    }
}

fun userName(id: String): String = SosRepository.user(id)?.fullName ?: id

// ---------- Kerangka halaman ----------

/**
 * Kerangka halaman Admin: header workspace, breadcrumb, judul, lalu konten list.
 * onBack = null untuk dashboard (tanpa breadcrumb).
 */
@Composable
fun AdminPage(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)?,
    onLogout: () -> Unit,
    onNotifications: () -> Unit,
    headerExtra: @Composable ColumnScope.() -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        AdminHeader(onLogout = onLogout, onNotifications = onNotifications)
        HorizontalDivider(color = SosBorder)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    if (onBack != null) {
                        Row(
                            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, Modifier.size(16.dp), tint = SosBlue)
                            Text("Dasbor admin", fontSize = 12.sp, color = SosBlue)
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(subtitle, fontSize = 13.sp, color = SosGray)
                    headerExtra()
                }
            }
            content()
            item {
                Text(
                    "Data contoh · diperbarui ${adminClock(LocalDateTime.now())} WIB",
                    fontSize = 11.sp,
                    color = SosGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminHeader(onLogout: () -> Unit, onNotifications: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val admin = SosRepository.user(SosRepository.ADMIN_ID)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .background(SosBlue, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("SOS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text("SOS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Admin workspace", fontSize = 11.sp, color = SosGray)
        }
        Box {
            IconButton(onClick = onNotifications) {
                Icon(Icons.Outlined.Notifications, "Notifikasi", tint = Color.Black)
            }
            val count = AdminRepository.pendingCount
            if (count > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .height(16.dp)
                        .widthIn(min = 16.dp)
                        .background(Color(0xFFE53935), CircleShape)
                        .padding(horizontal = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (count > 9) "9+" else "$count", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Box {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SosLightBlue)
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                val initials = admin?.fullName?.split(" ")?.take(2)?.joinToString("") { it.take(1) } ?: "AD"
                Text(initials, color = SosBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(admin?.fullName.orEmpty(), fontWeight = FontWeight.SemiBold)
                            Text(admin?.email.orEmpty(), fontSize = 11.sp, color = SosGray)
                        }
                    },
                    onClick = {}
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Keluar", color = Color(0xFFC62828)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFC62828)) },
                    onClick = { showMenu = false; onLogout() }
                )
            }
        }
    }
}

// ---------- Blok konten ----------

@Composable
fun AdminCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(
                BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) SosBlue else SosBorder),
                RoundedCornerShape(10.dp)
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        content = content
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            badge?.let { Pill(it, PillKind.BLUE) }
        }
        subtitle?.let { Text(it, fontSize = 12.sp, color = SosGray) }
    }
}

@Composable
fun Pill(text: String, kind: PillKind) {
    Text(
        text = text,
        color = kind.fg,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(kind.bg, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = Color.Black) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontSize = 12.sp, color = SosGray, modifier = Modifier.weight(1f))
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )
    }
}

@Composable
fun LinkText(text: String, onClick: () -> Unit) {
    Text(
        "$text →",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = SosBlue,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp)
    )
}

@Composable
fun FilterRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEachIndexed { index, label ->
            SosFilterChip(text = label, selected = selected == index, onClick = { onSelect(index) })
        }
    }
}

/** Riwayat/log dengan waktu, pelaku, dan isi. */
@Composable
fun AuditTimeline(entries: List<AuditEntry>) {
    if (entries.isEmpty()) {
        Text("Belum ada riwayat.", fontSize = 12.sp, color = SosGray)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.forEach { e ->
            Row {
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .background(SosBlue, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "${adminClock(e.time)} · ${e.actor}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SosBlue
                    )
                    Text(e.text, fontSize = 12.sp, color = Color.Black)
                    Text(adminDate(e.time.toLocalDate()), fontSize = 10.sp, color = SosGray)
                }
            }
        }
    }
}

@Composable
fun AdminTextArea(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, hint: String? = null) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    Spacer(Modifier.height(4.dp))
    SosTextField(value = value, onValueChange = onValueChange, placeholder = placeholder, singleLine = false, minLines = 3)
    hint?.let { Text(it, fontSize = 11.sp, color = SosGray, modifier = Modifier.padding(top = 4.dp)) }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (enabled) Color(0xFFE57373) else Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFFF5F5), contentColor = Color(0xFFC62828))
    ) { Text(text, fontSize = 14.sp) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, SosBlue),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SosLightBlue, contentColor = SosBlue)
    ) { Text(text, fontSize = 14.sp) }
}

/** Kartu ringkasan angka (stat tile). */
@Composable
fun StatTile(label: String, value: String, note: String, modifier: Modifier = Modifier, noteColor: Color = Color(0xFF2E7D32)) {
    AdminCard(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = SosGray)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(note, fontSize = 10.sp, color = noteColor)
    }
}

/** Pilihan tunggal berbentuk radio (keputusan). */
@Composable
fun <T> RadioGroup(options: List<T>, selected: T?, label: (T) -> String, onSelect: (T) -> Unit) {
    Column {
        options.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(option) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = option == selected, onClick = { onSelect(option) })
                Text(label(option), fontSize = 13.sp, color = Color.Black)
            }
        }
    }
}
