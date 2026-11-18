package com.example.sos.feature.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val WelcomeBlue = Color(0xFF2141AD)

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Menyimpan callback terbaru tanpa mengulang timer.
    val currentOnStartClick by rememberUpdatedState(onStartClick)

    // Tunggu 2 detik, lalu jalankan perpindahan layar.
    LaunchedEffect(Unit) {
        delay(4_000L)
        currentOnStartClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WelcomeBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SOS",
                color = Color.White,
                fontSize = 96.sp,
                lineHeight = 96.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-5).sp
            )

            Text(
                text = "STUDENT ON STUDY",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 760
)
@Composable
private fun WelcomeScreenPreview() {
    WelcomeScreen(
        onStartClick = {}
    )
}