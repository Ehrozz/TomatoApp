package com.android.tomatoapp.ui.screens.ipm

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.tomatoapp.ui.components.AppCard
import com.android.tomatoapp.ui.theme.BlueLight
import com.android.tomatoapp.ui.theme.CreamBackground
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.GreenAccent
import com.android.tomatoapp.ui.theme.GreenLight
import com.android.tomatoapp.ui.theme.GreenPrimary
import com.android.tomatoapp.ui.theme.OrangeLight
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary
import com.android.tomatoapp.ui.viewmodel.IPMViewModel

@Composable
fun IPMScreen(
    viewModel: IPMViewModel = viewModel(),
    onBack: () -> Unit = {},
    onScanLeafClick: () -> Unit = {},
    onScanHistoryClick: () -> Unit = {},
    onDiseaseDatabaseClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 18.dp)
        ) {
            // Header Card with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GreenPrimary, GreenAccent),
                            angle = 135f
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .padding(bottom = 14.dp)
            ) {
                Column {
                    Text(
                        text = "🌿",
                        style = TextStyle(fontSize = 26.sp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Pest Management",
                        style = TextStyle(
                            fontFamily = PlayfairDisplayFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                    Text(
                        text = "Monitor, detect, and manage crop threats intelligently.",
                        style = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Cards
            IPMActionCard(
                icon = "📸",
                iconBackgroundColor = BlueLight,
                title = "Scan Leaf",
                description = "Real-time disease detection",
                onClick = onScanLeafClick
            )

            IPMActionCard(
                icon = "🕐",
                iconBackgroundColor = OrangeLight,
                title = "Scan History",
                description = "View past detections",
                onClick = onScanHistoryClick
            )

            IPMActionCard(
                icon = "📖",
                iconBackgroundColor = GreenLight,
                title = "Disease Database",
                description = "Browse 40+ tomato diseases",
                onClick = onDiseaseDatabaseClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tips Card
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
            ) {
                Column {
                    Text(
                        text = "QUICK SCAN TIPS",
                        style = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 7.dp)
                    )

                    TipItem("Photograph the underside of leaves for best results")
                    TipItem("Ensure good natural lighting; avoid harsh shadows")
                    TipItem("Scan multiple leaves across different plants")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun IPMActionCard(
    icon: String,
    iconBackgroundColor: Color,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBackgroundColor, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = TextStyle(fontSize = 20.sp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = description,
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Arrow
            Text(
                text = "›",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = TextMuted
                )
            )
        }
    }
}

@Composable
fun TipItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "·",
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GreenAccent
            )
        )
        Text(
            text = text,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        )
    }
}
