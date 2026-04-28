package com.android.tomatoapp.ui.screens.workprogram

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
import com.android.tomatoapp.ui.components.AppButtonPrimary
import com.android.tomatoapp.ui.components.AppCard
import com.android.tomatoapp.ui.components.AppChipFilled
import com.android.tomatoapp.ui.components.CalendarDayCell
import com.android.tomatoapp.ui.components.CalendarDayState
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.CreamBackground
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.GreenAccent
import com.android.tomatoapp.ui.theme.GreenLight
import com.android.tomatoapp.ui.theme.GreenPrimary
import com.android.tomatoapp.ui.theme.Orange
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.RedPrimary
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary
import com.android.tomatoapp.ui.viewmodel.WorkProgramViewModel

@Composable
fun WorkProgramScreen(
    viewModel: WorkProgramViewModel = viewModel(),
    onBack: () -> Unit = {},
    onViewExpensesClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // Scrollable Content
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
                    .padding(18.dp)
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍅", fontSize = 28.sp)
                    }

                    // Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = state.cropName,
                            style = TextStyle(
                                fontFamily = PlayfairDisplayFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        )
                        Text(
                            text = "Started: ${state.startDate}",
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White.copy(alpha = 0.75f)
                            ),
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        Row(
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            AppChipFilled(
                                label = state.season,
                                backgroundColor = Color.White.copy(alpha = 0.2f),
                                textColor = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calendar Card
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                Column {
                    Text(
                        text = "Crop Schedule Calendar",
                        style = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Calendar Navigation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(CreamBackground, CircleShape)
                                .border(0.5.dp, Border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "‹",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            )
                        }

                        Text(
                            text = state.currentMonth,
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(CreamBackground, CircleShape)
                                .border(0.5.dp, Border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "›",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            )
                        }
                    }

                    // Calendar Grid with Day Labels
                    val dayLabels = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                    Column {
                        // Day labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            dayLabels.forEach { label ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = TextStyle(
                                            fontFamily = DmSansFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                    )
                                }
                            }
                        }

                        // Calendar days grid
                        val daysCells = state.calendarDays.chunked(7)
                        daysCells.forEach { week ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                week.forEach { day ->
                                    val dayState = when {
                                        day.day == 0 -> CalendarDayState.Empty
                                        day.state == "vegetative" -> CalendarDayState.Vegetative(day.day)
                                        day.state == "flowering" -> CalendarDayState.Flowering(day.day)
                                        day.state == "fruiting" -> CalendarDayState.Fruiting(day.day)
                                        day.state == "today" -> CalendarDayState.Today(day.day)
                                        else -> CalendarDayState.Empty
                                    }
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CalendarDayCell(dayState)
                                    }
                                }
                            }
                        }
                    }

                    // Legend
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem("Vegetative", Color(0xFFC8E6C9))
                        LegendItem("Flowering", Color(0xFFFFE0B2))
                        LegendItem("Fruiting", Color(0xFFCE93D8))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(RedPrimary, CircleShape)
                        )
                        Text(
                            text = "Today",
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 11.sp,
                                color = TextMuted
                            ),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Season Summary Card
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
            ) {
                Text(
                    text = "Season Summary",
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(state.daysActive.toString(), "Days Active", GreenPrimary)
                    SummaryItem(state.expenses, "Expenses", Orange)
                    SummaryItem(state.tasksDue.toString(), "Tasks Due", Color(0xFF1976D2))
                }
            }

            // View Expenses Button
            AppButtonPrimary(
                text = "💰 View Current Expenses",
                onClick = onViewExpensesClick,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Orange
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = TextMuted
            )
        )
    }
}

@Composable
fun SummaryItem(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontFamily = PlayfairDisplayFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 10.sp,
                color = TextMuted
            )
        )
    }
}
