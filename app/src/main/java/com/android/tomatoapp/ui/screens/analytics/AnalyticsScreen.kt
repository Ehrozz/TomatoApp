package com.android.tomatoapp.ui.screens.analytics

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.tomatoapp.ui.components.AppButtonPrimary
import com.android.tomatoapp.ui.components.AppCard
import com.android.tomatoapp.ui.components.AppStatCard
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.Blue
import com.android.tomatoapp.ui.theme.CreamBackground
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.GreenAccent
import com.android.tomatoapp.ui.theme.GreenPrimary
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.RedDark
import com.android.tomatoapp.ui.theme.RedPrimary
import com.android.tomatoapp.ui.theme.SmallRadius
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary
import com.android.tomatoapp.ui.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(),
    onBack: () -> Unit = {},
    onExportPdfClick: () -> Unit = {},
    onExportCsvClick: () -> Unit = {}
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
        ) {
            // Red Gradient Header
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
                    .padding(bottom = 14.dp)
            ) {
                Column {
                    Text(
                        text = "📊",
                        style = TextStyle(fontSize = 22.sp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Analytics",
                        style = TextStyle(
                            fontFamily = PlayfairDisplayFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                    Text(
                        text = "Season performance overview",
                        style = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Stats Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    AppStatCard(
                        label = "Net Profit",
                        value = state.netProfit,
                        modifier = Modifier.weight(1f),
                        valueColor = GreenPrimary
                    )
                    AppStatCard(
                        label = "Completion",
                        value = state.completion,
                        modifier = Modifier.weight(1f),
                        valueColor = Blue
                    )
                }

                // Filters Card
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column {
                        FilterRow(
                            label = "View Mode",
                            selectedValue = state.filterViewMode,
                            options = listOf("Per Area", "Per Season"),
                            onValueChange = { viewModel.updateViewMode(it) }
                        )
                        Divider(
                            color = Border,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )

                        FilterRow(
                            label = "Cultivar",
                            selectedValue = state.filterCultivar,
                            options = listOf("All Varieties", "Roma VF", "Cherry"),
                            onValueChange = { viewModel.updateCultivar(it) }
                        )
                        Divider(
                            color = Border,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )

                        FilterRow(
                            label = "Season",
                            selectedValue = state.filterSeason,
                            options = listOf("2024–2025", "2023–2024", "2022–2023"),
                            onValueChange = { viewModel.updateSeason(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppButtonPrimary(
                                text = "Export PDF",
                                onClick = onExportPdfClick,
                                modifier = Modifier.weight(1f),
                                backgroundColor = GreenPrimary
                            )
                            AppButtonPrimary(
                                text = "Export CSV",
                                onClick = onExportCsvClick,
                                modifier = Modifier.weight(1f),
                                backgroundColor = Blue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bar Chart
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column {
                        Text(
                            text = "💰 Profit per Area (₱ thousands)",
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            BarChartBar(50f, "A1")
                            BarChartBar(75f, "A2")
                            BarChartBar(38f, "A3")
                            BarChartBar(62f, "A4")
                            BarChartBar(46f, "A5")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Line Chart (Simple representation)
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column {
                        Text(
                            text = "✅ Completion Rate Trend",
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        SimpleLineChart()
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun FilterRow(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        )

        Box {
            Box(
                modifier = Modifier
                    .background(CreamBackground, RoundedCornerShape(SmallRadius))
                    .border(1.dp, Border, RoundedCornerShape(SmallRadius))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedValue,
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceWhite)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option,
                                style = TextStyle(
                                    fontFamily = DmSansFamily,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BarChartBar(height: Float, label: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((height * 0.8).dp)
                .background(GreenAccent, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 9.sp,
                color = TextMuted
            ),
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
fun SimpleLineChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                val heights = listOf(52f, 38f, 43f, 22f, 28f, 18f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(heights[index].dp)
                        .background(Blue.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
