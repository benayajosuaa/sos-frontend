package com.example.sos.feature.auth.regis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ProfileBlue = Color(0xFF2141AD)
private val ProfileInputBackground = Color(0xFFE5EFF8)
private val ProfileInputBorder = Color(0xFFE2E5E9)

@Composable
fun StudentProfileScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var university by rememberSaveable { mutableStateOf("") }
    var major by rememberSaveable { mutableStateOf("") }
    var linkedIn by rememberSaveable { mutableStateOf("") }
    var website by rememberSaveable { mutableStateOf("") }

    // University dan Major wajib diisi, LinkedIn dan Website opsional.
    val canContinue = university.isNotBlank() && major.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 32.dp, bottom = 24.dp)
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
                        color = ProfileBlue,
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

            StudentProfileField(
                label = "University",
                placeholder = "University",
                value = university,
                onValueChange = { university = it },
                isRequired = true
            )

            Spacer(Modifier.height(20.dp))

            StudentProfileField(
                label = "Major",
                placeholder = "Program",
                value = major,
                onValueChange = { major = it },
                isRequired = true
            )

            Spacer(Modifier.height(20.dp))

            StudentProfileField(
                label = "LinkedIn",
                placeholder = "LinkedIn",
                value = linkedIn,
                onValueChange = { linkedIn = it },
                keyboardType = KeyboardType.Uri
            )

            Spacer(Modifier.height(20.dp))

            StudentProfileField(
                label = "Website",
                placeholder = "Website.com",
                value = website,
                onValueChange = { website = it },
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            )
        }

        Button(
            onClick = onNextClick,
            enabled = canContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileBlue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Next",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun StudentProfileField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    Text(
        text = buildAnnotatedString {
            append(label)

            if (isRequired) {
                withStyle(SpanStyle(color = Color.Red)) {
                    append(" *")
                }
            }
        },
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = Color.Gray,
                fontSize = 14.sp
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = ProfileInputBackground,
            unfocusedContainerColor = ProfileInputBackground,
            focusedBorderColor = ProfileBlue,
            unfocusedBorderColor = ProfileInputBorder
        )
    )
}
