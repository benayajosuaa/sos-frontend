package com.example.sos.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.feature.request.AttachmentButton
import com.example.sos.ui.components.*

/** Edit Profile: data diri, tautan, portfolio, dan skills. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val user = SosRepository.currentUser

    var name by rememberSaveable { mutableStateOf(user.fullName) }
    var email by rememberSaveable { mutableStateOf(user.email) }
    var university by rememberSaveable { mutableStateOf(user.university) }
    var major by rememberSaveable { mutableStateOf(user.major) }
    var website by rememberSaveable { mutableStateOf(user.websiteUrl.orEmpty()) }
    var linkedIn by rememberSaveable { mutableStateOf(user.linkedInUrl.orEmpty()) }
    var portfolio by rememberSaveable { mutableStateOf(user.portfolioUrl) }
    var skills by rememberSaveable { mutableStateOf(user.skills) }
    var showAddSkill by remember { mutableStateOf(false) }

    val emailValid = email.contains("@") && email.contains(".")
    val canSave = name.isNotBlank() && emailValid && university.isNotBlank() && major.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        SosTopBar(title = "Edit Profile", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Label("Profile Picture")
                Spacer(Modifier.height(6.dp))
                InitialAvatar(name.ifBlank { user.fullName }, 56.dp)
            }
            UnderlineField("Name", name, { name = it })
            UnderlineField(
                "Email", email, { email = it },
                keyboardType = KeyboardType.Email,
                error = if (email.isNotEmpty() && !emailValid) "Enter a valid email" else null
            )
            UnderlineField("University", university, { university = it })
            UnderlineField("Major", major, { major = it })
            UnderlineField("Website", website, { website = it }, keyboardType = KeyboardType.Uri)
            UnderlineField("LinkedIn", linkedIn, { linkedIn = it }, keyboardType = KeyboardType.Uri)

            Column {
                Label("Add portfolio")
                Spacer(Modifier.height(6.dp))
                Box(Modifier.widthIn(max = 260.dp)) {
                    AttachmentButton(attachmentUri = null, attachmentName = portfolio) { _, fileName ->
                        portfolio = fileName
                    }
                }
            }

            Column {
                Label("Skills")
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(onClick = { showAddSkill = true }, shape = RoundedCornerShape(50), color = SosBlue) {
                        Text(
                            "+Add skills",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    skills.forEach { skill ->
                        Surface(
                            onClick = { skills = skills - skill },
                            shape = RoundedCornerShape(50),
                            color = SosLightBlue
                        ) {
                            Text(
                                "$skill  ✕",
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Text("Tap a skill to remove it.", fontSize = 10.sp, color = SosGray, modifier = Modifier.padding(top = 4.dp))
            }
        }

        SosPrimaryButton(
            text = "Save Changes",
            enabled = canSave,
            onClick = {
                SosRepository.updateProfile(name, email, university, major, website, linkedIn, portfolio, skills)
                onSaved()
            },
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }

    if (showAddSkill) {
        var newSkill by remember { mutableStateOf("") }
        val suggestions = (SosRepository.subjects + listOf("UI/UX", "Programming", "Mobile Development"))
            .filter { it !in skills && it.contains(newSkill.trim(), true) }
            .take(6)
        SosDialog(
            title = "Add skill",
            onDismiss = { showAddSkill = false },
            confirmText = "Add",
            confirmEnabled = newSkill.isNotBlank(),
            onConfirm = {
                val value = newSkill.trim()
                if (skills.none { it.equals(value, true) }) skills = skills + value
                showAddSkill = false
            }
        ) {
            SosTextField(value = newSkill, onValueChange = { newSkill = it }, placeholder = "e.g. Figma")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEach {
                    SosChoiceChip(text = it, selected = false, onClick = { newSkill = it })
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
}

@Composable
private fun UnderlineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null
) {
    var focused by remember { mutableStateOf(false) }
    val lineColor = when {
        error != null -> Color(0xFFC62828)
        focused -> SosBlue
        else -> Color.Black
    }
    Column {
        Label(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 13.sp, color = Color.DarkGray),
            cursorBrush = SolidColor(SosBlue),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .padding(top = 6.dp, bottom = 4.dp)
        )
        HorizontalDivider(thickness = if (focused) 2.dp else 1.dp, color = lineColor)
        error?.let { Text(it, fontSize = 11.sp, color = Color(0xFFC62828)) }
    }
}
