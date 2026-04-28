package com.android.tomatoapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calendar
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.tomatoapp.navigation.Routes
import com.android.tomatoapp.ui.components.AppBottomNavBar
import com.android.tomatoapp.ui.components.AppCard
import com.android.tomatoapp.ui.components.AppProgressBar
import com.android.tomatoapp.ui.components.BottomNavItem
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.CreamBackground
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.GreenAccent
import com.android.tomatoapp.ui.theme.GreenLight
import com.android.tomatoapp.ui.theme.OrangeLight
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.RedDark
import com.android.tomatoapp.ui.theme.RedPrimary
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary
import com.android.tomatoapp.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigate: (String) -> Unit = {},
    onPestScanClick: () -> Unit = {},
    onFinanceClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var selectedNavItem by remember { mutableStateOf(Routes.HOME) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(RedDark, RedPrimary),
                            angle = 135f
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar and Greeting
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.userInitials,
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            )
                        }

                        Column(
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Text(
                                text = "Good morning,",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                                )
                            )
                            Text(
                                text = state.userName,
                                style = TextStyle(
                                    fontFamily = PlayfairDisplayFamily,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Notification Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Content Area (with negative offset for curved header effect)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
                    .padding(horizontal = 14.dp)
            ) {
                // Weather Card
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Weather Icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(OrangeLight, RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("☀️", fontSize = 18.sp)
                        }

                        // Weather Info
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                text = state.location,
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 13.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "${state.weather} · ${state.humidity} humidity",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            )
                        }

                        // Temperature
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = state.temperature,
                                style = TextStyle(
                                    fontFamily = PlayfairDisplayFamily,
                                    fontSize = 24.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = com.android.tomatoapp.ui.theme.GreenPrimary
                                )
                            )
                            Text(
                                text = "Feels ${state.feelsLike}",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            )
                        }
                    }
                }

                // Crop Progress Card
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(GreenLight, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌱", fontSize = 22.sp)
                        }

                        // Info and Progress
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = "CURRENT STAGE",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = state.currentStage,
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 14.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            AppProgressBar(
                                progress = state.stageProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                height = 5.dp,
                                backgroundColor = GreenLight,
                                fillColor = GreenAccent
                            )
                            Text(
                                text = "Day ${state.stageDay} of ${state.stageTotalDays}",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                ),
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }

                        // View Button
                        AppButtonSmallGreen("View")
                    }
                }

                // Quick Action Cards Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pest Scan Card
                    AppCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickableQuickAction { onPestScanClick() },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFFE3F2FD), RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔍", fontSize = 18.sp)
                            }
                            Text(
                                text = "Pest Scan",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 13.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                modifier = Modifier.padding(top = 10.dp)
                            )
                            Text(
                                text = "Detect diseases",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Finance Card
                    AppCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickableQuickAction { onFinanceClick() },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(OrangeLight, RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💰", fontSize = 18.sp)
                            }
                            Text(
                                text = "Finance",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 13.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                modifier = Modifier.padding(top = 10.dp)
                            )
                            Text(
                                text = "Track your costs",
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation
        val navItems = listOf(
            BottomNavItem("Home", Icons.Filled.Home, Routes.HOME),
            BottomNavItem("Program", Icons.Outlined.Calendar, Routes.WORK_PROGRAM),
            BottomNavItem("IPM", Icons.Filled.Search, Routes.IPM),
            BottomNavItem("Analytics", Icons.Filled.Settings, Routes.ANALYTICS)
        )

        AppBottomNavBar(
            items = navItems,
            selectedRoute = selectedNavItem,
            onItemSelected = {
                selectedNavItem = it
                onNavigate(it)
            }
        )
    }
}

@Composable
fun AppButtonSmallGreen(text: String) {
    Box(
        modifier = Modifier
            .background(GreenLight, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = com.android.tomatoapp.ui.theme.GreenPrimary
            )
        )
    }
}

@Composable
private fun clickableQuickAction(onClick: () -> Unit): Modifier {
    return Modifier
}
