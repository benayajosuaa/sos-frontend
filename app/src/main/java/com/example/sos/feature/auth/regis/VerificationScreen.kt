package com.example.sos.feature.auth.regis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VerificationScreen(
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    onResendClick: () -> Unit = {}
) {
    val blue = Color(0xFF2141AD)

    var digits by rememberSaveable {
        mutableStateOf(List(4) { "" })
    }

    val focusRequesters = remember {
        List(4) { FocusRequester() }
    }

    val isComplete = digits.all { it.length == 1 }

    // Fokus otomatis ke kotak pertama saat layar dibuka.
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBackClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "←",
                    color = blue,
                    fontSize = 26.sp
                )
            }

            Text(
                text = "Create an Account",
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Verification Code",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Please enter the verification code that we have " +
                    "sent to your email address.",
            color = Color.Black,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            repeat(4) { index ->
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE5EFF8),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = blue
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = digits[index],
                            onValueChange = { input ->
                                val digit = input
                                    .filter { it in '0'..'9' }
                                    .takeLast(1)

                                digits = digits.toMutableList().also {
                                    it[index] = digit
                                }

                                if (digit.isNotEmpty() && index < 3) {
                                    focusRequesters[index + 1]
                                        .requestFocus()
                                } else if (digit.isEmpty() && index > 0) {
                                    // Saat angka dihapus, mundur ke kotak sebelumnya.
                                    focusRequesters[index - 1]
                                        .requestFocus()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequesters[index]),
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(blue)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onContinueClick,
            enabled = isComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = blue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Didn’t receive code?",
                color = Color.Black,
                fontSize = 14.sp
            )

            TextButton(
                onClick = onResendClick,
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(
                    text = "Resend",
                    color = blue,
                    fontSize = 14.sp
                )
            }
        }
    }
}