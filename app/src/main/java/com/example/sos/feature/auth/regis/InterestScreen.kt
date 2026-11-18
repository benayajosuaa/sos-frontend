package com.example.sos.feature.auth.regis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val InterestBlue = Color(0xFF2141AD)
private val InterestChipBackground = Color(0xFFE5EFF8)

// 10 bidang yang disarankan ke pengguna.
private val SuggestedFields = listOf(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestScreen(
    onCompleteClick: (selectedFields: List<String>) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedFields by rememberSaveable {
        mutableStateOf(listOf<String>())
    }

    val query = search.trim()

    // Saran yang belum dipilih dan cocok dengan pencarian.
    val suggestions = SuggestedFields.filter {
        it !in selectedFields && it.contains(query, ignoreCase = true)
    }

    fun addField(field: String) {
        val value = field.trim()
        if (value.isNotEmpty() &&
            selectedFields.none { it.equals(value, ignoreCase = true) }
        ) {
            selectedFields = selectedFields + value
        }
        search = ""
    }

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
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            Text(
                text = "Create an Account",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "What fields are you interested in?",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Add fields you are interested in to find " +
                        "requests and helpers that match.",
                color = Color.Black,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Text(
                        text = "⌕",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                // Tekan Enter untuk menambahkan bidang yang diketik sendiri.
                keyboardActions = KeyboardActions(
                    onDone = { addField(search) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = InterestBlue,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            if (selectedFields.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Selected (${selectedFields.size})",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedFields.forEach { field ->
                        InputChip(
                            selected = true,
                            onClick = {
                                selectedFields = selectedFields - field
                            },
                            label = { Text(field) },
                            trailingIcon = {
                                Text(text = "✕", fontSize = 12.sp)
                            },
                            shape = RoundedCornerShape(50),
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = InterestBlue,
                                selectedLabelColor = Color.White,
                                selectedTrailingIconColor = Color.White
                            ),
                            border = null
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Suggested",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            if (suggestions.isEmpty()) {
                Text(
                    text = if (query.isEmpty()) {
                        "All suggestions selected."
                    } else {
                        "No match. Press Enter to add \"$query\"."
                    },
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { field ->
                        SuggestionChip(
                            onClick = { addField(field) },
                            label = { Text("+ $field") },
                            shape = RoundedCornerShape(50),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = InterestChipBackground,
                                labelColor = Color.Black
                            ),
                            border = null
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onCompleteClick(selectedFields) },
            enabled = selectedFields.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = InterestBlue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Complete",
                fontSize = 16.sp
            )
        }
    }
}
