package com.example.sos.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.AssignmentStatus
import com.example.sos.data.model.TaskItem
import com.example.sos.ui.components.*

private enum class HistoryFilter(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    ON_PROGRESS("On Progress"),
    FLOATING("Floating");

    fun matches(item: TaskItem): Boolean = when (this) {
        ALL -> true
        COMPLETED -> item.assignment?.status == AssignmentStatus.COMPLETED
        ON_PROGRESS -> item.assignment?.status?.isActive == true
        FLOATING -> item.isFloating
    }
}

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onItemClick: (TaskItem) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var filter by rememberSaveable { mutableStateOf(HistoryFilter.ALL) }

    // Tab My Answers tidak punya status Floating.
    val filters = if (tab == 0) HistoryFilter.entries
    else HistoryFilter.entries - HistoryFilter.FLOATING

    val items = (if (tab == 0) SosRepository.myRequestItems() else SosRepository.myAnswerItems())
        .filter { filter.matches(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosTopBar(title = "History", onBack = onBack)

        SosUnderlineTabs(
            tabs = listOf("My Requests", "My Answers"),
            selectedIndex = tab,
            onSelect = {
                tab = it
                if (it == 1 && filter == HistoryFilter.FLOATING) filter = HistoryFilter.ALL
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach {
                SosFilterChip(text = it.label, selected = filter == it, onClick = { filter = it })
            }
        }

        Text(
            text = "Latest",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (items.isEmpty()) {
                item { Text("Nothing here yet.", color = SosGray, fontSize = 13.sp) }
            }
            items(items, key = { (it.assignment?.id ?: "") + it.request.id }) { item ->
                if (tab == 0) MyRequestCard(item) { onItemClick(item) }
                else MyAnswerCard(item) { onItemClick(item) }
            }
        }
    }
}

@Composable
private fun MyRequestCard(item: TaskItem, onClick: () -> Unit) {
    val request = item.request
    SosCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                request.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            StatusBadge(item.statusLabel)
        }
        Text(formatIdr(request.budget), fontSize = 11.sp, color = SosBlue)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.ChatBubbleOutline, null, Modifier.size(12.dp), tint = SosGray)
            Text(
                " ${SosRepository.repliesFor(request.id).size} replies  •  ${timeAgo(request.createdAt)}",
                fontSize = 11.sp,
                color = SosGray
            )
        }
        Spacer(Modifier.height(8.dp))
        TagRow(request.tags)
    }
}

@Composable
private fun MyAnswerCard(item: TaskItem, onClick: () -> Unit) {
    val request = item.request
    val assignment = item.assignment ?: return
    SosCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(24.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                SosRepository.user(request.requesterId)?.fullName.orEmpty(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(item.statusLabel)
        }
        Spacer(Modifier.height(6.dp))
        Text(request.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(4.dp))

        if (assignment.status == AssignmentStatus.COMPLETED) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Rating: ", fontSize = 12.sp, color = Color.Black)
                assignment.rating?.let { StarRating(it, size = 14.dp) }
                    ?: Text("Not rated yet", fontSize = 12.sp, color = SosGray)
            }
            Text(
                buildAnnotatedString {
                    append("Earned: ")
                    withStyle(SpanStyle(color = SosBlue)) { append(formatIdr(assignment.agreedPrice)) }
                },
                fontSize = 12.sp,
                color = Color.Black
            )
        } else {
            Text(
                buildAnnotatedString {
                    append("Deadline in: ")
                    withStyle(SpanStyle(color = SosBlue)) { append(deadlineText(assignment.deadline)) }
                },
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}
