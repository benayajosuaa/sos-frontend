package com.example.sos.feature.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.Presence
import com.example.sos.data.model.Request
import com.example.sos.data.model.User
import com.example.sos.ui.components.*

private enum class RequestSort(val label: String) {
    NEWEST("Newest"),
    DEADLINE("Nearest deadline"),
    BUDGET("Highest budget")
}

private enum class HelperSort(val label: String) {
    RATING("Highest rating"),
    HELPS("Most helps")
}

@Composable
fun ExploreScreen(
    onRequestClick: (String) -> Unit,
    onHelperClick: (String) -> Unit,
    onCreateRequestClick: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf<String?>(null) }
    var requestSort by rememberSaveable { mutableStateOf(RequestSort.NEWEST) }
    var helperSort by rememberSaveable { mutableStateOf(HelperSort.RATING) }

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val query = search.trim()

    val requests = SosRepository.exploreRequests()
        .filter { subject == null || it.subject == subject || subject in it.tags }
        .filter {
            query.isEmpty() ||
                    it.title.contains(query, true) ||
                    it.tags.any { t -> t.contains(query, true) }
        }
        .let { list ->
            when (requestSort) {
                RequestSort.NEWEST -> list.sortedByDescending { it.createdAt }
                RequestSort.DEADLINE -> list.sortedBy { it.deadline }
                RequestSort.BUDGET -> list.sortedByDescending { it.budget }
            }
        }

    val helpers = SosRepository.exploreHelpers()
        .filter { subject == null || subject in it.skills }
        .filter {
            query.isEmpty() ||
                    it.fullName.contains(query, true) ||
                    it.skills.any { s -> s.contains(query, true) }
        }
        .let { list ->
            when (helperSort) {
                HelperSort.RATING -> list.sortedByDescending { it.rating }
                HelperSort.HELPS -> list.sortedByDescending { it.helpsCount }
            }
        }

    // Kategori filter = daftar subject + skill yang ada di data.
    val filterOptions = remember {
        (SosRepository.subjects + listOf("UI/UX", "Graphic Design", "Programming")).distinct()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SosSearchField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = SosBlue
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        if (tab == 0) {
                            RequestSort.entries.forEach {
                                DropdownMenuItem(
                                    text = { Text(it.label, fontWeight = if (it == requestSort) FontWeight.Bold else null) },
                                    onClick = { requestSort = it; showSortMenu = false }
                                )
                            }
                        } else {
                            HelperSort.entries.forEach {
                                DropdownMenuItem(
                                    text = { Text(it.label, fontWeight = if (it == helperSort) FontWeight.Bold else null) },
                                    onClick = { helperSort = it; showSortMenu = false }
                                )
                            }
                        }
                    }
                }
            }

            SosUnderlineTabs(
                tabs = listOf("Request", "Helper"),
                selectedIndex = tab,
                onSelect = { tab = it },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        onClick = { showFilterMenu = true },
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FilterList, null, Modifier.size(12.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Filter", fontSize = 11.sp, color = Color.Black)
                        }
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { subject = option; showFilterMenu = false }
                            )
                        }
                    }
                }

                subject?.let { selected ->
                    Surface(
                        onClick = { subject = null },
                        shape = RoundedCornerShape(50),
                        color = SosBlue
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selected, fontSize = 11.sp, color = Color.White)
                            Spacer(Modifier.width(3.dp))
                            Icon(Icons.Default.Close, "Remove filter", Modifier.size(12.dp), tint = Color.White)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (tab == 0) {
                    if (requests.isEmpty()) item { EmptyText("No requests found.") }
                    items(requests, key = { it.id }) { request ->
                        ExploreRequestCard(request) { onRequestClick(request.id) }
                    }
                } else {
                    if (helpers.isEmpty()) item { EmptyText("No helpers found.") }
                    items(helpers, key = { it.id }) { helper ->
                        HelperCard(helper) { onHelperClick(helper.id) }
                    }
                }
            }
        }

        if (tab == 0) {
            FloatingActionButton(
                onClick = onCreateRequestClick,
                shape = CircleShape,
                containerColor = SosBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create request", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(text, color = SosGray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
private fun ExploreRequestCard(request: Request, onClick: () -> Unit) {
    SosCard(onClick = onClick) {
        Text(
            text = request.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        RequestMetaRow(request, SosRepository.repliesFor(request.id).size)
        Spacer(Modifier.height(8.dp))
        TagRow(request.tags)
    }
}

@Composable
private fun HelperCard(helper: User, onClick: () -> Unit) {
    SosCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Avatar(28.dp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(helper.fullName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    if (helper.isVerified) {
                        Spacer(Modifier.width(4.dp))
                        VerifiedIcon()
                    }
                }
                RatingLine(helper.rating, helper.helpsCount)
            }
            PresenceBadge(helper.presence)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.School, null, Modifier.size(13.dp), tint = Color.Black)
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${helper.major}, ${helper.university}",
                fontSize = 11.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(8.dp))
        TagRow(helper.skills.take(3))
    }
}

@Composable
private fun PresenceBadge(presence: Presence) {
    val online = presence == Presence.ONLINE
    Row(
        modifier = Modifier
            .background(if (online) Color(0xFFDDF3E1) else Color(0xFFEDEDED), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(if (online) Color(0xFF2E7D32) else Color.Gray, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(presence.label, fontSize = 10.sp, color = Color.Black)
    }
}
