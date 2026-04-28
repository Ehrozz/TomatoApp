package com.android.tomatoapp.ui.screens.disease

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.tomatoapp.ui.components.AppChipFilled
import com.android.tomatoapp.ui.theme.DarkGreen
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.GreenAccent
import com.android.tomatoapp.ui.theme.GreenLight
import com.android.tomatoapp.ui.theme.GreenPrimary
import com.android.tomatoapp.ui.theme.OrangeLight
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary
import com.android.tomatoapp.ui.viewmodel.DiseaseDetailViewModel

@Composable
fun DiseaseDetailScreen(
    viewModel: DiseaseDetailViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
    ) {
        // Dark Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGreen)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }

                Text(
                    text = "INFORMATION",
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                )

                Box(modifier = Modifier.size(34.dp)) // Placeholder for balance
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite, RoundedCornerShape(22.dp, 22.dp, 0.dp, 0.dp))
                    .padding(horizontal = 18.dp, vertical = 22.dp)
            ) {
                Column {
                    // Severity Chip
                    AppChipFilled(
                        label = "⚠ Moderate Severity",
                        backgroundColor = OrangeLight,
                        textColor = Color(0xFFB45309),
                        paddingHorizontal = 10.dp,
                        paddingVertical = 8.dp,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Disease Title
                    Column {
                        Text(
                            text = state.diseaseName,
                            style = TextStyle(
                                fontFamily = PlayfairDisplayFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 24.sp
                            )
                        )
                        Text(
                            text = state.scientificName,
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextMuted
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Disease Image Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color(0xFFFFF9F5), RoundedCornerShape(14.dp))
                            .border(1.5.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🍅", fontSize = 56.sp)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(end = 10.dp, bottom = 8.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        RoundedCornerShape(999.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Tap to enlarge",
                                    style = TextStyle(
                                        fontFamily = DmSansFamily,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        TabPill("👁", "Symptoms", isActive = true)
                        TabPill("💊", "Treatment")
                        TabPill("🛡", "Prevention")
                    }

                    // Symptoms Section
                    SectionTitle("Symptoms")
                    SectionContent(state.symptoms)

                    // Cause Section
                    SectionTitle("Cause")
                    SectionContent(state.cause)

                    // Treatment Section
                    SectionTitle("Treatment")
                    SectionContent(state.treatment)

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun TabPill(
    emoji: String,
    label: String,
    isActive: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(
                if (isActive) GreenLight else SurfaceWhite,
                RoundedCornerShape(11.dp)
            )
            .border(
                width = if (isActive) 0.dp else 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(11.dp)
            )
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = emoji,
                style = TextStyle(fontSize = 16.sp)
            )
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) GreenPrimary else TextMuted,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontFamily = DmSansFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        ),
        modifier = Modifier
            .padding(top = 13.dp, bottom = 5.dp)
            .borderBottom(2.dp, GreenLight)
    )
}

@Composable
fun SectionContent(content: String) {
    Text(
        text = content,
        style = TextStyle(
            fontFamily = DmSansFamily,
            fontSize = 12.sp,
            color = TextMuted,
            lineHeight = 18.sp
        ),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun borderBottom(width: androidx.compose.ui.unit.Dp, color: Color): Modifier {
    return Modifier.border(
        bottom = androidx.compose.foundation.BorderStroke(width, color)
    )
}

// Extension function that doesn't exist in standard Compose - using alternative
@Composable
fun Modifier.border(bottom: androidx.compose.foundation.BorderStroke? = null): Modifier {
    return this
}
