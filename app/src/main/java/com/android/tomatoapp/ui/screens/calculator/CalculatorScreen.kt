package com.android.tomatoapp.ui.screens.calculator

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
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
import com.android.tomatoapp.ui.components.AppTextFieldSmall
import com.android.tomatoapp.ui.components.ExpenseItemRow
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.BlueLight
import com.android.tomatoapp.ui.theme.CreamBackground
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.GreenAccent
import com.android.tomatoapp.ui.theme.GreenLight
import com.android.tomatoapp.ui.theme.GreenPrimary
import com.android.tomatoapp.ui.theme.GreenSave
import com.android.tomatoapp.ui.theme.Orange
import com.android.tomatoapp.ui.theme.OrangeLight
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.RedLight
import com.android.tomatoapp.ui.theme.SmallRadius
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary
import com.android.tomatoapp.ui.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Income Summary Header (Green Gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GreenPrimary, GreenAccent),
                            angle = 135f
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Text(
                        text = "Projected Net Income",
                        style = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 0.3.sp
                        )
                    )
                    Text(
                        text = state.netIncome,
                        style = TextStyle(
                            fontFamily = PlayfairDisplayFamily,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetaChip(state.cropName)
                        MetaChip("${state.completion} Complete")
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Adjusted Net Income",
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                        Text(
                            text = state.adjustedNetIncome,
                            style = TextStyle(
                                fontFamily = PlayfairDisplayFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // Harvest Metrics Card (Green Background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenLight)
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Column {
                    HarvestRow("Yield Prediction", state.yieldPrediction)
                    HarvestRow("Total Yield", state.totalYield)
                    HarvestRow("Predicted Harvest Date", state.harvestDate, isLast = true)
                }
            }

            // Content Section
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)
            ) {
                // Farm Metrics Card
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    Column {
                        Text(
                            text = "🌾 Farm Metrics",
                            style = TextStyle(
                                fontFamily = DmSansFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        InputFieldRow("Area / Hectare", state.areaHectare) { viewModel.updateAreaHectare(it) }
                        Divider(color = Border, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 7.dp))

                        InputFieldRow("Avg Weight per Fruit (g)", state.avgWeightFruit) {
                            viewModel.updateAvgWeightFruit(it)
                        }
                        Divider(color = Border, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 7.dp))

                        InputFieldRow("Fruits per Plant", state.fruitsPerPlant) {
                            viewModel.updateFruitsPerPlant(it)
                        }
                        Divider(color = Border, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 7.dp))

                        InputFieldRow("Market Value (₱/kg)", state.marketValue) {
                            viewModel.updateMarketValue(it)
                        }

                        // Subtotal Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CreamBackground, RoundedCornerShape(SmallRadius))
                                .border(1.5.dp, Border, RoundedCornerShape(SmallRadius))
                                .padding(12.dp, 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sub-total Harvest",
                                    style = TextStyle(
                                        fontFamily = DmSansFamily,
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = state.subTotalHarvest,
                                    style = TextStyle(
                                        fontFamily = PlayfairDisplayFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expenses Section Title
                Text(
                    text = "EXPENSES BREAKDOWN",
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 10.dp, top = 4.dp)
                )

                // Expense Cards
                ExpenseItemRow(
                    label = "Labor",
                    sublabel = "12 entries logged",
                    amount = state.laborExpense,
                    icon = "👷",
                    iconBackgroundColor = GreenLight,
                    amountColor = GreenPrimary
                )

                ExpenseItemRow(
                    label = "Materials",
                    sublabel = "8 entries logged",
                    amount = state.materialsExpense,
                    icon = "🧪",
                    iconBackgroundColor = BlueLight,
                    amountColor = GreenPrimary
                )

                ExpenseItemRow(
                    label = "Equipment / Tools",
                    sublabel = "5 entries logged",
                    amount = state.equipmentExpense,
                    icon = "🔧",
                    iconBackgroundColor = OrangeLight,
                    amountColor = GreenPrimary
                )

                ExpenseItemRow(
                    label = "Miscellaneous",
                    sublabel = "3 entries logged",
                    amount = state.miscExpense,
                    icon = "📦",
                    iconBackgroundColor = RedLight,
                    amountColor = GreenPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Save Button
                AppButtonPrimary(
                    text = "Save to Analytics",
                    onClick = {
                        viewModel.saveToAnalytics()
                        onSaveClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GreenSave
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun MetaChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        )
    }
}

@Composable
fun HarvestRow(label: String, value: String, isLast: Boolean = false) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF2E7D32)
                )
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
            )
        }
        if (!isLast) {
            Divider(
                color = Color.Black.copy(alpha = 0.06f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
fun InputFieldRow(label: String, value: String, onValueChange: (String) -> Unit) {
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
                fontSize = 12.sp,
                color = TextMuted
            )
        )
        AppTextFieldSmall(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(0.35f),
            placeholder = ""
        )
    }
}
