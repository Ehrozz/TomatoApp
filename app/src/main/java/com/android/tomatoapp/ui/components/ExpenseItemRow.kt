package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tomatoapp.ui.theme.CardRadius
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.SoftShadowElevation
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary

@Composable
fun ExpenseItemRow(
    label: String,
    sublabel: String,
    amount: String,
    icon: String,
    iconBackgroundColor: Color,
    amountColor: Color = Color(0xFF2D7A3A),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp),
        shape = RoundedCornerShape(CardRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = SoftShadowElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBackgroundColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = TextStyle(fontSize = 18.sp)
                )
            }

            // Label and Sublabel
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = sublabel,
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Amount
            Text(
                text = amount,
                style = TextStyle(
                    fontFamily = PlayfairDisplayFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            )
        }
    }
}
