package com.example.sos.feature.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sos.data.SosRepository
import com.example.sos.data.model.RecentHelp
import com.example.sos.feature.request.MissingContent
import com.example.sos.ui.components.*

/** Profil Helper lengkap supaya Requester bisa menilai sebelum memilih. */
@Composable
fun HelperInfoScreen(
    helperId: String,
    onBack: () -> Unit,
    onChatHelperClick: () -> Unit
) {
    val helper = SosRepository.user(helperId)
    if (helper == null) {
        MissingContent(onBack)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        SosTopBar(title = "Helper", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Avatar(96.dp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(helper.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    if (helper.isVerified) {
                        Spacer(Modifier.width(4.dp))
                        VerifiedIcon()
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, Modifier.size(14.dp), tint = SosStar)
                    Text(" ${helper.rating}", fontSize = 12.sp, color = SosGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.School, null, Modifier.size(13.dp), tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text("${helper.major}, ${helper.university}", fontSize = 11.sp, color = Color.Black)
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (helper.linkedInUrl != null) {
                        Icon(Icons.Default.Work, "LinkedIn", Modifier.size(18.dp), tint = Color.Black)
                    }
                    if (helper.websiteUrl != null) {
                        Icon(Icons.Default.Language, "Website", Modifier.size(18.dp), tint = Color.Black)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox("${helper.helpsCount}", "Helps", Modifier.weight(1f))
                StatBox("${helper.requestsCount}", "Requests", Modifier.weight(1f))
                StatBox("${helper.reviewsCount}", "Reviews", Modifier.weight(1f))
            }

            if (helper.recentHelps.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                SectionTitle("Recent Helps")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    helper.recentHelps.forEach { RecentHelpCard(it) }
                }
            }

            if (helper.skills.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                SectionTitle("Skills")
                Spacer(Modifier.height(8.dp))
                TagRow(helper.skills)
            }

            helper.portfolioUrl?.let {
                Spacer(Modifier.height(20.dp))
                SectionTitle("Portfolio")
                Spacer(Modifier.height(8.dp))
                LinkBox(it)
            }
            Spacer(Modifier.height(16.dp))
        }

        SosPrimaryButton(
            text = "Chat Helper",
            onClick = onChatHelperClick,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SosLightBlue, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SosBlue)
        Text(label, fontSize = 11.sp, color = SosBlue)
    }
}

@Composable
private fun RecentHelpCard(help: RecentHelp) {
    SosCard {
        Text(help.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarRating(help.rating, size = 14.dp)
            Text("  •  ${help.timeAgo}", fontSize = 11.sp, color = SosGray)
        }
        Spacer(Modifier.height(6.dp))
        TagRow(help.tags)
    }
}
