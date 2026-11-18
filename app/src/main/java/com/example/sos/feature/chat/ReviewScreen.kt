package com.example.sos.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.feature.request.MissingContent
import com.example.sos.ui.components.*

/** Review setelah request selesai (hanya Requester, sekali). */
@Composable
fun ReviewScreen(
    assignmentId: String,
    onBack: () -> Unit,
    onSent: () -> Unit
) {
    val assignment = SosRepository.assignment(assignmentId)
    if (assignment == null) {
        MissingContent(onBack)
        return
    }
    val helperName = SosRepository.user(assignment.helperId)?.fullName.orEmpty()

    var rating by rememberSaveable { mutableIntStateOf(0) }
    var comment by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        SosTopBar(title = "", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Text("Request complete!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            Text(
                "Leave $helperName a review",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(Modifier.height(6.dp))
            StarRating(rating = rating, size = 28.dp, onRate = { rating = it })
            Spacer(Modifier.height(16.dp))
            SosTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = "Describe your experience (optional)",
                singleLine = false,
                minLines = 5
            )
        }
        SosPrimaryButton(
            text = "Send Review",
            enabled = rating > 0,
            onClick = {
                SosRepository.submitReview(assignmentId, rating, comment)
                onSent()
            },
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

/** Halaman setelah mengajukan pembatalan. */
@Composable
fun CancellationSentScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosTopBar(title = "", onBack = onDone)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Text("Cancellation request sent", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(10.dp))
            Text(
                "Your cancellation request has been sent to the other party. Please wait for them " +
                        "to confirm the cancellation on their side.",
                fontSize = 13.sp,
                color = Color.Black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "We’ll notify you once the request has been confirmed. If you can’t reach an " +
                        "agreement, our admin team will help resolve it.",
                fontSize = 13.sp,
                color = Color.Black
            )
        }
        SosPrimaryButton(
            text = "Done",
            onClick = onDone,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}
