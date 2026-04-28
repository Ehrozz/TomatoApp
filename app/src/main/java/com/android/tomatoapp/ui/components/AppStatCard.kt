package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.SmallRadius
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary

@Composable
fun AppStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceWhite,
    valueColor: Color = TextPrimary,
    labelColor: Color = TextMuted,
    padding: Dp = 13.dp
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(SmallRadius))
            .padding(padding),
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 10.sp,
                    color = labelColor,
                    letterSpacing = 0.4.sp
                )
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = PlayfairDisplayFamily,
                    fontSize = 20.sp,
                    color = valueColor,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
fun AppStatRow(
    stats: List<Triple<String, String, Color>>, // label, value, color
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        stats.forEach { (label, value, color) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = PlayfairDisplayFamily,
                        fontSize = 18.sp,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = label,
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
