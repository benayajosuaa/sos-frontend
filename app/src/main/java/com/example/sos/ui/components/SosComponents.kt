package com.example.sos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.model.Request

// ---------- Warna SOS (sesuai desain) ----------

val SosBlue = Color(0xFF2141AD)
val SosLightBlue = Color(0xFFE5EFF8)
val SosBorder = Color(0xFFE2E5E9)
val SosGray = Color(0xFF6B7280)
val SosStar = Color(0xFFFFC107)

private val GreenBg = Color(0xFFDDF3E1)
private val GreenText = Color(0xFF2E7D32)
private val OrangeBg = Color(0xFFFDE4DB)
private val OrangeText = Color(0xFFD84315)
private val RedText = Color(0xFFC62828)

// ---------- Struktur halaman ----------

/** Header dengan tombol kembali dan judul. */
@Composable
fun SosTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = SosBlue
            )
        }
        Text(
            text = title,
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun SosCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier.fillMaxWidth()
    val colors = CardDefaults.cardColors(containerColor = Color.White)
    val elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    val shape = RoundedCornerShape(12.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = colors,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(14.dp), content = content)
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = colors,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(14.dp), content = content)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

// ---------- Input ----------

@Composable
fun SosSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search"
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
        cursorBrush = SolidColor(SosBlue),
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color.Gray, fontSize = 14.sp)
                    }
                    inner()
                }
            }
        }
    )
}

/** TextField berlatar biru muda seperti di desain. */
@Composable
fun SosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(8.dp),
        textStyle = TextStyle(fontSize = 14.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = SosLightBlue,
            unfocusedContainerColor = SosLightBlue,
            focusedBorderColor = SosBlue,
            unfocusedBorderColor = SosBorder,
            cursorColor = SosBlue
        )
    )
}

// ---------- Tombol ----------

@Composable
fun SosPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = SosBlue,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontSize = 15.sp)
    }
}

@Composable
fun SosOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = SosBlue
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Text(text = text, fontSize = 15.sp)
    }
}

/** Tombol kecil seperti "Chat" atau "See post". */
@Composable
fun SosSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (filled) SosBlue else Color.White,
        border = if (filled) null else BorderStroke(1.dp, SosBlue)
    ) {
        Text(
            text = text,
            color = if (filled) Color.White else SosBlue,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

/** Chip filter: All / My requests / Completed, dll. */
@Composable
fun SosFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) SosBlue else Color.White,
        border = BorderStroke(1.dp, if (selected) SosBlue else Color.Gray)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Chip pilihan (Deadline: Today / Tomorrow / ...). */
@Composable
fun SosChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (selected) SosBlue else SosLightBlue
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/** Tab bergaris bawah: Request | Helper, My Requests | My Answers. */
@Composable
fun SosUnderlineTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = if (selected) SosBlue else SosGray,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(if (selected) 2.dp else 1.dp)
                        .background(if (selected) SosBlue else Color.Gray)
                )
            }
        }
    }
}

// ---------- Elemen kecil ----------

@Composable
fun TagChip(text: String) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 11.sp,
        modifier = Modifier
            .background(SosLightBlue, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagRow(tags: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tags.forEach { TagChip(it) }
    }
}

/** Badge status berdasarkan label status. */
@Composable
fun StatusBadge(label: String) {
    val (bg, fg) = when (label) {
        "Completed" -> GreenBg to GreenText
        "Floating" -> OrangeBg to OrangeText
        "Cancelled" -> Color(0xFFFDE0E0) to RedText
        else -> SosLightBlue to SosBlue
    }
    Text(
        text = label,
        color = fg,
        fontSize = 10.sp,
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .border(1.dp, fg, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun Avatar(size: Dp = 28.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFD9D9D9)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(size * 0.7f)
        )
    }
}

@Composable
fun VerifiedIcon() {
    Icon(
        imageVector = Icons.Default.Verified,
        contentDescription = "Verified",
        tint = Color.Black,
        modifier = Modifier.size(14.dp)
    )
}

/** "★ 4.9 • 23 helps" */
@Composable
fun RatingLine(rating: Double, helps: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = SosStar,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = "$rating  •  $helps helps",
            color = SosGray,
            fontSize = 11.sp
        )
    }
}

/** Baris bintang; kalau onRate diisi, bintang bisa diklik. */
@Composable
fun StarRating(
    rating: Int,
    size: Dp = 16.dp,
    onRate: ((Int) -> Unit)? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (1..5).forEach { star ->
            val filled = star <= rating
            Icon(
                imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "$star star",
                tint = if (filled) SosStar else SosGray,
                modifier = Modifier
                    .size(size)
                    .then(
                        if (onRate != null) Modifier.clickable { onRate(star) }
                        else Modifier
                    )
            )
        }
    }
}

@Composable
fun LinkBox(url: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SosLightBlue, RoundedCornerShape(2.dp))
            .border(1.dp, SosBlue, RoundedCornerShape(2.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Link,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = url, color = Color.Black, fontSize = 12.sp)
    }
}

@Composable
private fun MetaItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SosGray,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            color = SosGray,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Info request dua kolom: kategori, budget, estimasi, deadline. */
@Composable
fun RequestMetaGrid(request: Request) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row {
            MetaItem(Icons.Outlined.Category, request.subject, Modifier.weight(1f))
            MetaItem(Icons.Outlined.Schedule, request.estimatedTime.label, Modifier.weight(1f))
        }
        Row {
            MetaItem(Icons.Outlined.Payments, formatIdr(request.budget), Modifier.weight(1f))
            MetaItem(Icons.Outlined.Event, deadlineLabel(request.deadline), Modifier.weight(1f))
        }
    }
}

/** Info request satu baris (untuk kartu di Explore). */
@Composable
fun RequestMetaRow(request: Request, replyCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetaItem(Icons.Outlined.Payments, formatIdr(request.budget))
        MetaItem(Icons.Default.Bolt, request.estimatedTime.label)
        MetaItem(Icons.Outlined.ChatBubbleOutline, "$replyCount")
    }
}

/** Placeholder gambar lampiran request. */
@Composable
fun ImagePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .background(Color(0xFFDDDDDD)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = "Attachment",
            tint = Color(0xFF555555),
            modifier = Modifier.fillMaxSize(0.75f)
        )
    }
}

/** Stepper angka seperti Budget / Response di desain. */
@Composable
fun SosStepper(
    text: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .background(SosLightBlue, RoundedCornerShape(6.dp))
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 13.sp, color = Color.Black, modifier = Modifier.weight(1f))
        Column {
            Text(
                text = "▲",
                fontSize = 10.sp,
                color = Color.Black,
                modifier = Modifier
                    .clickable(onClick = onIncrement)
                    .padding(horizontal = 10.dp, vertical = 1.dp)
            )
            Text(
                text = "▼",
                fontSize = 10.sp,
                color = Color.Black,
                modifier = Modifier
                    .clickable(onClick = onDecrement)
                    .padding(horizontal = 10.dp, vertical = 1.dp)
            )
        }
    }
}
