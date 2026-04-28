package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.PillRadius
import com.android.tomatoapp.ui.theme.TextMuted

@Composable
fun AppChip(
    label: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Border,
    textColor: Color = TextMuted,
    icon: String? = null,
    iconColor: Color = textColor,
    onClose: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(PillRadius))
            .border(1.5.dp, borderColor, RoundedCornerShape(PillRadius))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Text(
                text = icon,
                modifier = Modifier.padding(end = 4.dp),
                style = TextStyle(fontSize = 12.sp)
            )
        }

        Text(
            text = label,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = textColor
            )
        )

        if (onClose != null) {
            Text(
                text = "✕",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onClose() },
                style = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = textColor
                )
            )
        }
    }
}

@Composable
fun AppChipFilled(
    label: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color = Color.White,
    icon: String? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp,
    paddingVertical: androidx.compose.ui.unit.Dp = 4.dp,
    paddingHorizontal: androidx.compose.ui.unit.Dp = 10.dp
) {
    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(PillRadius))
            .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Text(
                text = icon,
                modifier = Modifier.padding(end = 4.dp),
                style = TextStyle(fontSize = 12.sp)
            )
        }

        Text(
            text = label,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = fontSize,
                color = textColor
            )
        )
    }
}

@Composable
fun clickable(onClick: () -> Unit): Modifier {
    return Modifier
}
