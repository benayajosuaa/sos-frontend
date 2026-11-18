package com.example.sos.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.TaskItem
import com.example.sos.ui.components.*

private enum class TaskFilter(val label: String) {
    ALL("All"),
    MY_REQUESTS("My requests"),
    MY_RESPONSES("My responses")
}

@Composable
fun HomeScreen(
    onPostRequestClick: () -> Unit,
    onTaskClick: (TaskItem) -> Unit
) {
    var filter by rememberSaveable { mutableStateOf(TaskFilter.ALL) }

    // Ongoing = request saya yang belum selesai + bantuan yang sedang saya kerjakan.
    val myRequests = SosRepository.myRequestItems().filter {
        it.isFloating || it.assignment?.status?.isActive == true
    }
    val myResponses = SosRepository.myAnswerItems().filter {
        it.assignment?.status?.isActive == true
    }
    val tasks = when (filter) {
        TaskFilter.ALL -> myRequests + myResponses
        TaskFilter.MY_REQUESTS -> myRequests
        TaskFilter.MY_RESPONSES -> myResponses
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            HomeHeader(
                name = SosRepository.currentUser.fullName,
                onPostRequestClick = onPostRequestClick
            )
        }

        if (SosRepository.isSuspended) {
            item {
                val info = SosRepository.currentUser.suspension
                Text(
                    text = "Your account is suspended" +
                            (info?.until?.let { " until ${formatDate(it)}" } ?: "") +
                            ". Reason: ${info?.reason ?: "-"}. Contact SOS support if you think this is a mistake.",
                    fontSize = 12.sp,
                    color = Color(0xFFC62828),
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp)
                        .background(Color(0xFFFDE0E0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )
            }
        }

        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                Text(
                    text = "Ongoing Tasks",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskFilter.entries.forEach {
                        SosFilterChip(
                            text = it.label,
                            selected = filter == it,
                            onClick = { filter = it }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        if (tasks.isEmpty()) {
            item {
                Text(
                    text = "No ongoing tasks yet.",
                    color = SosGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }

        items(tasks, key = { (it.assignment?.id ?: "") + it.request.id }) { task ->
            Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                OngoingTaskCard(task = task, onClick = { onTaskClick(task) })
            }
        }
    }
}

@Composable
private fun HomeHeader(
    name: String,
    onPostRequestClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B2F8F), Color(0xFF2B4FD0))
                )
            )
    ) {
        // Ornamen lingkaran di pojok kanan atas.
        Canvas(Modifier.matchParentSize()) {
            val stroke = Stroke(width = 1.dp.toPx())
            val c = Color.White.copy(alpha = 0.35f)
            drawCircle(c, radius = size.width * 0.28f, center = Offset(size.width * 0.9f, size.height * 0.25f), style = stroke)
            drawCircle(c, radius = size.width * 0.2f, center = Offset(size.width * 0.75f, size.height * 0.45f), style = stroke)
        }

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 28.dp)
        ) {
            Text(text = "Hello, $name", color = Color.White, fontSize = 13.sp)
            Text(
                text = "Need Help Fast?",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ask anything and get answers\nfrom helpers quickly",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onPostRequestClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = SosBlue
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Post a request", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OngoingTaskCard(task: TaskItem, onClick: () -> Unit) {
    val request = task.request
    val isMyRequest = request.requesterId == SosRepository.CURRENT_USER_ID
    val replyCount = SosRepository.repliesFor(request.id).size

    SosCard(onClick = onClick) {
        if (!isMyRequest) {
            // Bantuan yang saya kerjakan: tampilkan pemilik request.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(24.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = SosRepository.user(request.requesterId)?.fullName.orEmpty(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(task.statusLabel)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = request.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    append("Deadline in: ")
                    withStyle(SpanStyle(color = SosBlue)) {
                        append(deadlineText(task.assignment?.deadline ?: request.deadline))
                    }
                },
                fontSize = 12.sp,
                color = Color.Black
            )
        } else {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = request.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(task.statusLabel)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = SosGray,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = " $replyCount replies  •  ${timeAgo(request.createdAt)}",
                    fontSize = 11.sp,
                    color = SosGray
                )
            }
            Spacer(Modifier.height(8.dp))
            TagRow(request.tags)
        }
    }
}
