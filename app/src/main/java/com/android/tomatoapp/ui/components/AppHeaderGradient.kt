package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.PlayfairDisplayFamily
import com.android.tomatoapp.ui.theme.RedDark
import com.android.tomatoapp.ui.theme.RedPrimary

@Composable
fun AppHeaderGradient(
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
    paddingTop: Dp = 18.dp,
    paddingBottom: Dp = 56.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    angle = 135f
                )
            )
            .padding(top = paddingTop, bottom = paddingBottom, start = 18.dp, end = 18.dp)
    ) {
        content()
    }
}

@Composable
fun AppHeaderTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.White,
    subtitleColor: Color = Color.White.copy(alpha = 0.7f)
) {
    Box(modifier = modifier) {
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            )
        }
        Text(
            text = title,
            style = TextStyle(
                fontFamily = PlayfairDisplayFamily,
                fontSize = 26.sp,
                color = titleColor
            ),
            modifier = Modifier.padding(top = if (subtitle != null) 4.dp else 0.dp)
        )
    }
}

@Composable
fun AppHeaderSmall(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.White,
    backgroundColor: Color = RedPrimary
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = titleColor,
                letterSpacing = 2.sp
            )
        )
    }
}
