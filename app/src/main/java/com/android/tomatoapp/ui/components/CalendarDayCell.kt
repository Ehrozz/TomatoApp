package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.RedPrimary
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary

sealed class CalendarDayState {
    object Empty : CalendarDayState()
    data class Vegetative(val day: Int) : CalendarDayState()
    data class Flowering(val day: Int) : CalendarDayState()
    data class Fruiting(val day: Int) : CalendarDayState()
    data class Today(val day: Int) : CalendarDayState()
}

@Composable
fun CalendarDayCell(
    state: CalendarDayState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val defaultModifier = modifier
        .size(30.dp)
        .border(0.5.dp, Color.Transparent, RoundedCornerShape(8.dp))

    when (state) {
        is CalendarDayState.Empty -> {
            Box(modifier = defaultModifier)
        }
        is CalendarDayState.Vegetative -> {
            Box(
                modifier = defaultModifier
                    .background(
                        Color(0xFFC8E6C9), // Vegetative color
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.day.toString(),
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32)
                    )
                )
            }
        }
        is CalendarDayState.Flowering -> {
            Box(
                modifier = defaultModifier
                    .background(
                        Color(0xFFFFE0B2), // Flowering color
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.day.toString(),
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFE65100)
                    )
                )
            }
        }
        is CalendarDayState.Fruiting -> {
            Box(
                modifier = defaultModifier
                    .background(
                        Color(0xFFCE93D8), // Fruiting color
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.day.toString(),
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFC62828)
                    )
                )
            }
        }
        is CalendarDayState.Today -> {
            Box(
                modifier = defaultModifier
                    .background(
                        RedPrimary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.day.toString(),
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
